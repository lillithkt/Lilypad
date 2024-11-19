package gay.lilyy.lilypad.core.modules.modules.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.adamratzman.spotify.*
import com.adamratzman.spotify.models.CurrentlyPlayingObject
import com.adamratzman.spotify.models.Track
import gay.lilyy.lilypad.core.Constants
import gay.lilyy.lilypad.core.HTTPServer
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.ChatboxModule
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.modules.music.types.LastFMTrack
import gay.lilyy.lilypad.core.modules.modules.music.types.Lyrics
import gay.lilyy.lilypad.core.modules.modules.music.types.SyncType
import gay.lilyy.lilypad.openUrlInBrowser
import gay.lilyy.lilypad.ui.components.LText
import gay.lilyy.lilypad.ui.components.LabeledCheckbox
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.json.*


const val SPOTIFY_REDIRECT_URL = "/spotify/callback"

@Suppress("unused")
@DelicateCoroutinesApi
class Music : ChatboxModule<MusicConfig>() {
    override val name = "Music"


    override val configClass = MusicConfig::class

    private var sptProgressMs: Int = 0

    private val isPlaying: Boolean
        get() {
            if (!config!!.enabled) return false
            when (config!!.type) {
                MusicType.SPOTIFY -> {
                    if (nowPlaying == null) return false
                    return nowPlaying!!.isPlaying
                }

                MusicType.LASTFM -> {
                    if (lastFMTrack == null) return false
                    return lastFMTrack!!.attr.nowPlaying
                }
            }

        }
    private val spotifyTrack: Track?
        get() {
            if (!config!!.enabled) return null
            if (nowPlaying?.item?.asTrack == null) return null
            return nowPlaying!!.item!!.asTrack!!
        }

    private var lastFMTrack: LastFMTrack? = null

    override fun buildChatbox(): List<String?> {
        val output = mutableListOf<String?>()
        if (!isPlaying) return output
        when (config!!.type) {
            MusicType.SPOTIFY -> {
                output.add(
                    "\uD83D\uDCFB ${spotifyTrack!!.name}${if (config!!.showArtist) " - " + spotifyTrack!!.artists.first().name else ""}" +
                            if (config!!.spotify.showTimestamp) {
                                " (${sptProgressMs / 1000 / 60}:${if ((sptProgressMs / 1000 % 60) < 10) "0" else ""}${sptProgressMs / 1000 % 60} / ${spotifyTrack!!.durationMs / 1000 / 60}:${if ((spotifyTrack!!.durationMs / 1000 % 60) < 10) "0" else ""}${spotifyTrack!!.durationMs / 1000 % 60})"
                            } else {
                                ""
                            }
                )
            }

            MusicType.LASTFM -> {
                output.add("\uD83D\uDCFB ${lastFMTrack!!.name}${if (config!!.showArtist) " - " + lastFMTrack!!.artist else ""}")
            }
        }
        if (config!!.type == MusicType.SPOTIFY) {
            if (config!!.spotify.lyrics.enabled && lyrics !== null && lyrics!!.unsynced === null) {
                output.add(when {
                    lyrics!!.lineSynced !== null -> {
                        lyrics!!.lineSynced!!.lines.find { it.start <= sptProgressMs && it.end >= sptProgressMs }?.text
                            ?: lyrics!!.lineSynced!!.lines.findLast { it.start <= sptProgressMs }?.text
                    }


                    lyrics!!.syllableSynced !== null -> {
                        var lyricStr = ""
                        lyrics!!.syllableSynced!!.lines.findLast {
                            val syllable = it.lead?.first()
                            if (syllable != null) {
                                syllable.start <= sptProgressMs// && syllable.end >= progressMs
                            } else {
                                it.start <= sptProgressMs// && it.end >= progressMs
                            }
                        }?.lead?.forEach {
                            lyricStr += it.words
                            if (!it.part) {
                                lyricStr += " "
                            }
                        }
                        lyricStr
                    }


                    else -> null
                })
            }
        }
        return output
    }


    private val httpClient: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }


    override fun httpServerRouting(routing: Routing): Boolean {
        routing.get(SPOTIFY_REDIRECT_URL) {
            val code = call.request.queryParameters["code"]
            if (code.isNullOrEmpty()) {
                call.respondText("Failed to log in to Spotify.")
                return@get
            }
            spotifyClient = spotifyClientApi(
                clientId = config!!.spotify.auth.clientId,
                clientSecret = config!!.spotify.auth.clientSecret,
                redirectUri = "http://localhost:${Constants.HTTP_PORT}$SPOTIFY_REDIRECT_URL",
                authorization = SpotifyUserAuthorization(
                    authorizationCode = code,
                )
            ) {
                onTokenRefresh = {
                    config!!.spotify.auth.token = it.token.accessToken
                    config!!.spotify.auth.refreshToken = it.token.refreshToken
                    saveConfig()
                }
            }.build()
            config!!.spotify.auth.token = spotifyClient!!.token.accessToken
            config!!.spotify.auth.refreshToken = spotifyClient!!.token.refreshToken
            saveConfig(true)
            call.respondText("Logged in! You may close this tab now.")
            HTTPServer.unlock("spotify")
        }

        return true
    }

    private var spotifyClient: SpotifyClientApi? = null

    private suspend fun updateSpotifyClient(reauth: Boolean = false) {
        if (config!!.spotify.useAuthConfig) {
            if (config!!.spotify.auth.clientId.isEmpty() || config!!.spotify.auth.clientSecret.isEmpty()) {
                spotifyClient = null
                return
            }
            if (!config!!.spotify.auth.token.isNullOrEmpty() && !reauth) {
                spotifyClient = spotifyClientApi(
                    clientId = config!!.spotify.auth.clientId,
                    clientSecret = config!!.spotify.auth.clientSecret,
                    redirectUri = "http://localhost:${Constants.HTTP_PORT}$SPOTIFY_REDIRECT_URL",
                    authorization = SpotifyUserAuthorization(
                        tokenString = config!!.spotify.auth.token,
//                        refreshTokenString = config!!.auth.refreshToken
                    )
                ) {
                    onTokenRefresh = {
                        config!!.spotify.auth.token = it.token.accessToken
                        config!!.spotify.auth.refreshToken = it.token.refreshToken
                        saveConfig()
                    }
                }.build()
                return
            }
            if (HTTPServer.hasLock("spotify")) {
                return
            }
            val url = getSpotifyAuthorizationUrl(
                SpotifyScope.UserReadCurrentlyPlaying,
                SpotifyScope.UserReadPlaybackState,
                SpotifyScope.UserReadPlaybackPosition,
                clientId = config!!.spotify.auth.clientId,
                redirectUri = "http://localhost:${Constants.HTTP_PORT}$SPOTIFY_REDIRECT_URL"
            )
            HTTPServer.lock("spotify")
            if (CoreModules.Core.config!!.logs.debug) Napier.d("Opening browser to $url")
            openUrlInBrowser(url)
        } else {
            spotifyClient = null
        }
    }


    override fun init() {
        super.init()

        // Call the suspend updateNowPlaying function every 5 seconds in Dispatchers.IO
        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            while (true) {
                try {
                    updateNowPlaying()
                } catch (e: Exception) {
                    if (CoreModules.Core.config!!.logs.errors) Napier.e("Failed to update now playing", e)
                    nowPlaying = null
                    sptProgressMs = 0
                   lyrics = null
                   if (config!!.type == MusicType.SPOTIFY) {
                       updateSpotifyClient(true)

                   }
                }
                Thread.sleep(config!!.updateInterval.toLong())
            }
        }

        if (config!!.type == MusicType.SPOTIFY) {
            Thread {
                var lastTrackId = spotifyTrack?.id
                while (true) {
                    if (nowPlaying?.isPlaying == true) {
                        sptProgressMs += 250
                    }
                    if (lastTrackId != spotifyTrack?.id) {
                        lastTrackId = spotifyTrack?.id
                        lyrics = null
                        scope.launch {
                            getLyrics()
                        }
                    }
                    Thread.sleep(250)
                }
            }.start()
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }


    private var nowPlaying: CurrentlyPlayingObject? = null
    private var lyrics: Lyrics? = null

    private suspend fun getLyrics() {
        if (!isPlaying || spotifyTrack?.id == null) {
            lyrics = null
            return
        }


        try {
            val response = httpClient.get("${config!!.spotify.lyrics.provider}/lyrics/${spotifyTrack!!.id}")
            if (response.status == HttpStatusCode.OK) {
                val body: JsonObject = response.body()
                val type = body["syncType"]?.jsonPrimitive.toString().replace("\"", "")
                lyrics = when (type) {
                    SyncType.UNSYNCED.name -> Lyrics(unsynced = json.decodeFromJsonElement(body))
                    SyncType.LINE_SYNCED.name -> Lyrics(lineSynced = json.decodeFromJsonElement(body))
                    SyncType.SYLLABLE_SYNCED.name -> Lyrics(syllableSynced = json.decodeFromJsonElement(body))
                    else -> null
                }
            } else {
                lyrics = null
            }
        } catch (e: Exception) {
            if (CoreModules.Core.config!!.logs.errors) Napier.e("Failed to get lyrics", e)
            lyrics = null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun updateNowPlaying() {
        if (!config!!.enabled) return

        when (config!!.type) {
            MusicType.SPOTIFY -> {
                val oldId = spotifyTrack?.id
                if (!config!!.spotify.useAuthConfig) {
                    if (config!!.spotify.nonAuth.endpoint.isNotEmpty()) {
                        val response = httpClient.get(config!!.spotify.nonAuth.endpoint)
                        if (response.status == HttpStatusCode.OK) {
                            try {
                                nowPlaying = response.body()
                            } catch (e: Exception) {
                                if (e.instanceOf(MissingFieldException::class) || e.instanceOf(JsonConvertException::class)) {
                                    if (CoreModules.Core.config!!.logs.debug) Napier.d("No song playing")
                                    nowPlaying = null
                                } else {
                                    if (CoreModules.Core.config!!.logs.errors) Napier.e(
                                        "Failed to get currently playing from non-auth endpoint",
                                        e
                                    )
                                }
                            }
                        }
                    } else {
                        return
                    }
                } else {
                    if (spotifyClient == null) {
                        updateSpotifyClient()
                    }
                    if (spotifyClient != null) {
                        try {
                            nowPlaying = spotifyClient!!.player.getCurrentlyPlaying()
                        } catch (e: Exception) {
                            if (e.instanceOf(MissingFieldException::class) || e.instanceOf(JsonConvertException::class)) {
                                if (CoreModules.Core.config!!.logs.debug) Napier.d("No song playing")
                                nowPlaying = null
                            } else {
                                if (CoreModules.Core.config!!.logs.errors) Napier.e(
                                    "Failed to get currently playing from Spotify",
                                    e
                                )
                                updateSpotifyClient(true)
                            }
                        }
                    }
                }

                if (nowPlaying?.progressMs != null) {
                    sptProgressMs = nowPlaying?.progressMs ?: 0
                }

                if (spotifyTrack?.id != oldId) {
                    if (config!!.spotify.lyrics.enabled) {
                        getLyrics()
                    }
                }
            }

            MusicType.LASTFM -> {
                if (config!!.lastFM.username == null || config!!.lastFM.apiKey == null) {

                    return
                }
                val params = mutableMapOf<String, String>()
                params["method"] = "user.getrecenttracks"
                params["user"] = config!!.lastFM.username!!
                params["api_key"] = config!!.lastFM.apiKey!!
                params["format"] = "json"
                params["limit"] = "1"

                val response = httpClient.get("http://ws.audioscrobbler.com/2.0") {
                    url {
                        parameters.appendAll(Parameters.build {
                            params.forEach { (key, value) -> append(key, value) }
                        })
                    }
                }
                if (response.status == HttpStatusCode.OK) {
                    try {
                        val body: JsonElement = response.body()
                        val trackElem = body.jsonObject["recenttracks"]?.jsonObject?.get("track")?.jsonArray?.get(0)
                        if (trackElem == null) {
                            lastFMTrack = null
                            return
                        }
                        val track = json.decodeFromJsonElement<LastFMTrack>(trackElem)
                        lastFMTrack = track
                    } catch (e: Exception) {
                        if (CoreModules.Core.config!!.logs.errors) Napier.e(
                            "Failed to get currently playing from LastFM",
                            e
                        )
                        lastFMTrack = null
                    }
                }
            }
        }
    }

    override val hasSettingsUI = true

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun onSettingsUI() {
        var enabled by remember { mutableStateOf(config!!.enabled) }
        var updateInterval by remember { mutableStateOf(config!!.updateInterval) }

        LabeledCheckbox(
            label = "Enabled",
            checked = enabled,
            onCheckedChange = {
                enabled = it
                config!!.enabled = it
                saveConfig()
            },
        )

        TextField(
            label = { Text("Update Interval") },
            value = updateInterval.toString(),
            onValueChange = {
                updateInterval = it.toIntOrNull() ?: 0
                config!!.updateInterval = updateInterval
                saveConfig()
            },
        )

        var showArtist by remember { mutableStateOf(config!!.showArtist) }
        LabeledCheckbox(
            label = "Show Artist",
            checked = showArtist,
            onCheckedChange = {
                showArtist = it
                config!!.showArtist = it
                saveConfig()
            },
        )

        var type by remember { mutableStateOf(config!!.type) }

        LText.Caption("The type of music to use. Spotify requires an auth config to be set up.")
        var typeDropdown by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = typeDropdown, onExpandedChange = { typeDropdown = it }) {
            TextField(
                value = type.type,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdown) },

                )
            ExposedDropdownMenu(
                expanded = typeDropdown,
                onDismissRequest = { typeDropdown = false },
            ) {
                MusicType.entries.forEach {
                    DropdownMenuItem(onClick = {
                        type = it
                        config!!.type = it
                        if (it !== MusicType.SPOTIFY) {
                            sptProgressMs = 0
                            lyrics = null
                            nowPlaying = null
                        }
                        saveConfig()
                        typeDropdown = false
                    }) {
                        Text(it.type)
                    }
                }
            }
        }

        AnimatedVisibility(visible = type == MusicType.SPOTIFY) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                var showTimestamps by remember { mutableStateOf(config!!.spotify.showTimestamp) }
                LabeledCheckbox(
                    label = "Show Timestamps",
                    checked = showTimestamps,
                    onCheckedChange = {
                        showTimestamps = it
                        config!!.spotify.showTimestamp = it
                        saveConfig()
                    },
                )

                var useAuthConfig by remember { mutableStateOf(config!!.spotify.useAuthConfig) }

                LText.Caption("If enabled, the app will use the auth config. If disabled, the app will use the non-auth config. Auth config is standard oauth, non-auth config is an endpoint configured to return the spotify listening data with no authentication, such as https://github.com/ImLvna/spotify-listening")
                LabeledCheckbox(
                    label = "Use Auth Config",
                    checked = useAuthConfig,
                    onCheckedChange = {
                        useAuthConfig = it
                        config!!.spotify.useAuthConfig = it
                        saveConfig()
                    },
                )


                AnimatedVisibility(visible = useAuthConfig) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        var clientId by remember { mutableStateOf(config!!.spotify.auth.clientId) }
                        var clientSecret by remember { mutableStateOf(config!!.spotify.auth.clientSecret) }

                        val scope = rememberCoroutineScope()

                        TextField(
                            label = { Text("Client ID") },
                            value = clientId,
                            onValueChange = {
                                clientId = it
                                config!!.spotify.auth.clientId = it
                                saveConfig()
                            },
                        )

                        TextField(
                            label = { Text("Client Secret") },
                            value = clientSecret,
                            onValueChange = {
                                clientSecret = it
                                config!!.spotify.auth.clientSecret = it
                                saveConfig()
                            },
                        )

                        Button(onClick = {
                            scope.launch {
                                updateSpotifyClient()
                            }
                        }) {
                            Text("Update Credentials")
                        }
                    }
                }
                AnimatedVisibility(visible = !useAuthConfig) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        var endpoint by remember { mutableStateOf(config!!.spotify.nonAuth.endpoint) }

                        TextField(
                            label = { Text("Endpoint") },
                            value = endpoint,
                            onValueChange = { endpoint = it },
                        )

                        config!!.spotify.nonAuth = SpotifyNonAuthConfig(endpoint)
                    }
                }

                var lyricsEnabled by remember { mutableStateOf(config!!.spotify.lyrics.enabled) }
                LabeledCheckbox(
                    label = "Lyrics Enabled",
                    checked = lyricsEnabled,
                    onCheckedChange = {
                        lyricsEnabled = it
                        config!!.spotify.lyrics.enabled = it
                        saveConfig()
                    },
                )

                AnimatedVisibility(visible = lyricsEnabled) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        var provider by remember { mutableStateOf(config!!.spotify.lyrics.provider) }
                        LText.Caption("The provider to use for lyrics. This is an instance of https://github.com/imlvna/spotify-lyrics-api")
                        TextField(
                            label = { Text("Provider") },
                            value = provider,
                            onValueChange = {
                                provider = it
                                config!!.spotify.lyrics.provider = it
                                saveConfig()
                            },
                        )
                    }

                }
            }
        }

        AnimatedVisibility(visible = type == MusicType.LASTFM) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                var username by remember { mutableStateOf(config!!.lastFM.username) }
                var apiKey by remember { mutableStateOf(config!!.lastFM.apiKey) }

                TextField(
                    label = { Text("Username") },
                    value = username ?: "",
                    onValueChange = {
                        username = it
                        config!!.lastFM.username = it
                        saveConfig()
                    },
                )

                TextField(
                    label = { Text("API Key") },
                    value = apiKey ?: "",
                    onValueChange = {
                        apiKey = it
                        config!!.lastFM.apiKey = it
                        saveConfig()
                    },
                )
            }
        }
    }
}