package gay.lilyy.lilypad.core.modules.modules.spotify

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.adamratzman.spotify.*
import com.adamratzman.spotify.models.CurrentlyPlayingObject
import gay.lilyy.lilypad.core.Constants
import gay.lilyy.lilypad.core.HTTPServer
import gay.lilyy.lilypad.core.CoreModules.Coremodules.chatbox.ChatboxModule
import gay.lilyy.lilypad.core.modules.CoreModules
import gay.lilyy.lilypad.core.modules.modules.spotify.spotube.Spotube
import gay.lilyy.lilypad.core.modules.modules.spotify.spotube.SpotubeTrack
import gay.lilyy.lilypad.core.modules.modules.spotify.types.Lyrics
import gay.lilyy.lilypad.core.modules.modules.spotify.types.SyncType
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


const val REDIRECT_URL = "/spotify/callback"

@Suppress("unused")
@DelicateCoroutinesApi
class Spotify : ChatboxModule<SpotifyConfig>() {
    override val name = "Spotify"


    override val configClass = SpotifyConfig::class

    private var sptProgressMs: Int = 0
    private val progressMs: Int
        get() {
            if (!config!!.enabled) return 0
            return if (config!!.spotubeIntegration) {
                Spotube.currentClient?.currentTrackPosition ?: 0
            } else {
                sptProgressMs
            }
        }

    private val isPlaying: Boolean
        get() {
            if (!config!!.enabled) return false
            return if (config!!.spotubeIntegration) {
                !(Spotube.currentClient?.currentTrack == null || Spotube.currentClient?.playing != true)
            } else {
                !(nowPlaying == null || !nowPlaying!!.isPlaying)
            }
        }
    private val track: SpotubeTrack?
        get() {
            if (!config!!.enabled) return null
            return if (config!!.spotubeIntegration) {
                Spotube.currentClient?.currentTrack
            } else {
                if (nowPlaying?.item?.asTrack == null) return null
                SpotubeTrack.fromTrack(nowPlaying!!.item!!.asTrack!!)
            }
        }

    override fun buildChatbox(): List<String?> {
        val output = mutableListOf<String?>()
        if (!isPlaying || track == null) return output
        output.add(
            "\uD83D\uDCFB ${track!!.name} - ${
                track!!.artists.joinToString(", ") { it.name }
            }"
        )
        if (config!!.lyrics.enabled && lyrics !== null && lyrics!!.unsynced === null) {
            output.add(when {
                lyrics!!.lineSynced !== null -> {
                    lyrics!!.lineSynced!!.lines.find { it.start <= progressMs && it.end >= progressMs }?.text
                        ?: lyrics!!.lineSynced!!.lines.findLast { it.start <= progressMs }?.text
                }

                lyrics!!.syllableSynced !== null -> {
                    lyrics!!.syllableSynced!!.lines.find {
                        val syllable = it.lead?.first()
                        if (syllable != null) {
                            syllable.start <= progressMs && syllable.end >= progressMs
                        } else {
                            it.start <= progressMs && it.end >= progressMs
                        }
                    }?.lead?.joinToString(" ") { it.words }
                }

                else -> null
            })
        }
        return output
    }


    private val httpClient: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    override fun httpServerRouting(routing: Routing): Boolean {
        routing.get("/spotify/callback") {
            val code = call.request.queryParameters["code"]
            if (code.isNullOrEmpty()) {
                call.respondText("Failed to log in to Spotify.")
                return@get
            }
            spotifyClient = spotifyClientApi(
                clientId = config!!.auth.clientId,
                clientSecret = config!!.auth.clientSecret,
                redirectUri = "http://localhost:${Constants.HTTP_PORT}$REDIRECT_URL",
                authorization = SpotifyUserAuthorization(
                    authorizationCode = code,
                )
            ) {
                onTokenRefresh = {
                    config!!.auth.token = it.token.accessToken
                    config!!.auth.refreshToken = it.token.refreshToken
                    saveConfig()
                }
            }.build()
            config!!.auth.token = spotifyClient!!.token.accessToken
            config!!.auth.refreshToken = spotifyClient!!.token.refreshToken
            saveConfig(true)
            call.respondText("Logged in! You may close this tab now.")
            HTTPServer.unlock("spotify")
        }
        return true
    }

    private var spotifyClient: SpotifyClientApi? = null

    private suspend fun updateSpotifyClient(reauth: Boolean = false) {
        if (config!!.useAuthConfig) {
            if (config!!.auth.clientId.isEmpty() || config!!.auth.clientSecret.isEmpty()) {
                spotifyClient = null
                return
            }
            if (!config!!.auth.token.isNullOrEmpty() && !reauth) {
                spotifyClient = spotifyClientApi(
                    clientId = config!!.auth.clientId,
                    clientSecret = config!!.auth.clientSecret,
                    redirectUri = "http://localhost:${Constants.HTTP_PORT}$REDIRECT_URL",
                    authorization = SpotifyUserAuthorization(
                        tokenString = config!!.auth.token,
//                        refreshTokenString = config!!.auth.refreshToken
                    )
                ) {
                    onTokenRefresh = {
                        config!!.auth.token = it.token.accessToken
                        config!!.auth.refreshToken = it.token.refreshToken
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
                clientId = config!!.auth.clientId,
                redirectUri = "http://localhost:${Constants.HTTP_PORT}$REDIRECT_URL"
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

        Spotube.init()


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
                    updateSpotifyClient(true)
                }
                Thread.sleep(config!!.updateInterval.toLong())
            }
        }

        Thread {
            var lastTrackId = track?.id
            while (true) {
                if (nowPlaying?.isPlaying == true) {
                    sptProgressMs += 250
                }
                for (client in Spotube.clients.values) {
                    if (client.playing) client.currentTrackPosition = client.currentTrackPosition?.plus(250)
                }
                if (lastTrackId != track?.id) {
                    lastTrackId = track?.id
                    lyrics = null
                    scope.launch {
                        getLyrics()
                    }
                }
                Thread.sleep(250)
            }
        }.start()
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }


    private var nowPlaying: CurrentlyPlayingObject? = null
    private var lyrics: Lyrics? = null

    private suspend fun getLyrics() {
        if (!isPlaying || track?.id == null) {
            lyrics = null
            return
        }


        val response = httpClient.get("${config!!.lyrics.provider}/lyrics/${track!!.id}")
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
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun updateNowPlaying() {
        if (!config!!.enabled) return
        val oldId = track?.id
        if (!config!!.useAuthConfig) {
            if (config!!.nonAuth.endpoint.isNotEmpty()) {
                val response = httpClient.get(config!!.nonAuth.endpoint)
                if (response.status == HttpStatusCode.OK) {
                    try {
                        nowPlaying = response.body()
                    } catch (e: Exception) {
                        if (e.instanceOf(MissingFieldException::class) || e.instanceOf(JsonConvertException::class)) {
                            if (CoreModules.Core.config!!.logs.debug) Napier.d("No song playing")
                            nowPlaying = null
                        } else {
                            if (CoreModules.Core.config!!.logs.errors) Napier.e("Failed to get currently playing", e)
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
                        if (CoreModules.Core.config!!.logs.errors) Napier.e("Failed to get currently playing", e)
                        updateSpotifyClient(true)
                    }
                }
            }
        }

        if (nowPlaying?.progressMs != null) {
            sptProgressMs = nowPlaying?.progressMs ?: 0
        }

        if (track?.id != oldId) {
            if (config!!.lyrics.enabled) {
                getLyrics()
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

        var useAuthConfig by remember { mutableStateOf(config!!.useAuthConfig) }

        LText.Caption("If enabled, the app will use the auth config. If disabled, the app will use the non-auth config. Auth config is standard oauth, non-auth config is an endpoint configured to return the spotify listening data with no authentication, such as https://github.com/ImLvna/spotify-listening")
        LabeledCheckbox(
            label = "Use Auth Config",
            checked = useAuthConfig,
            onCheckedChange = {
                useAuthConfig = it
                config!!.useAuthConfig = it
                saveConfig()
            },
        )


        AnimatedVisibility(visible = useAuthConfig) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                var clientId by remember { mutableStateOf(config!!.auth.clientId) }
                var clientSecret by remember { mutableStateOf(config!!.auth.clientSecret) }

                val scope = rememberCoroutineScope()

                TextField(
                    label = { Text("Client ID") },
                    value = clientId,
                    onValueChange = {
                        clientId = it
                        config!!.auth.clientId = it
                        saveConfig()
                    },
                )

                TextField(
                    label = { Text("Client Secret") },
                    value = clientSecret,
                    onValueChange = {
                        clientSecret = it
                        config!!.auth.clientSecret = it
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
                var endpoint by remember { mutableStateOf(config!!.nonAuth.endpoint) }

                TextField(
                    label = { Text("Endpoint") },
                    value = endpoint,
                    onValueChange = { endpoint = it },
                )

                config!!.nonAuth = SpotifyNonAuthConfig(endpoint)
            }
        }

        var lyricsEnabled by remember { mutableStateOf(config!!.lyrics.enabled) }
        LabeledCheckbox(
            label = "Lyrics Enabled",
            checked = lyricsEnabled,
            onCheckedChange = {
                lyricsEnabled = it
                config!!.lyrics.enabled = it
                saveConfig()
            },
        )

        AnimatedVisibility(visible = lyricsEnabled) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                var provider by remember { mutableStateOf(config!!.lyrics.provider) }
                LText.Caption("The provider to use for lyrics. This is an instance of https://github.com/imlvna/spotify-lyrics-api")
                TextField(
                    label = { Text("Provider") },
                    value = provider,
                    onValueChange = {
                        provider = it
                        config!!.lyrics.provider = it
                        saveConfig()
                    },
                )
            }
        }

        var spotubeIntegration by remember { mutableStateOf(config!!.spotubeIntegration) }
        LText.Caption("If enabled, the app can pull the now playing state from Spotube, an app for spotify that imo works better on quest")
        LabeledCheckbox(
            label = "Spotube Integration",
            checked = spotubeIntegration,
            onCheckedChange = {
                spotubeIntegration = it
                config!!.spotubeIntegration = it
                saveConfig()
            },
        )
        AnimatedVisibility(visible = spotubeIntegration) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Dropdown of all clients
                var selectedClient by remember { Spotube.selectedClient }

                val clients = remember { Spotube.clients }

                var expanded by remember { mutableStateOf(false) }
                LText.Caption("Selecting nothing will default to the first client")
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {
                    expanded = !expanded
                }) {
                    TextField(value = Spotube.currentClient?.name ?: "Select a client",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },

                    )

                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        clients.forEach { client ->
                            DropdownMenuItem(onClick = {
                                if (selectedClient == client.key) {
                                    selectedClient = null
                                } else {
                                    selectedClient = client.key
                                }
                                expanded = false
                            }) { Text(client.value.name) }
                        }
                    }
                }
            }

        }
    }
}