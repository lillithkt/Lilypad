package gay.lilyy.lilypad.core.modules.modules.spotify

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.adamratzman.spotify.*
import com.adamratzman.spotify.models.CurrentlyPlayingObject
import gay.lilyy.lilypad.core.Constants
import gay.lilyy.lilypad.core.HTTPServer
import gay.lilyy.lilypad.core.modules.Modules
import gay.lilyy.lilypad.core.modules.coremodules.chatbox.ChatboxModule
import gay.lilyy.lilypad.core.modules.modules.spotify.types.Lyrics
import gay.lilyy.lilypad.core.modules.modules.spotify.types.SyncType
import gay.lilyy.lilypad.openUrlInBrowser
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*


const val REDIRECT_URL = "/spotify/callback"

@Suppress("unused")
@DelicateCoroutinesApi
class Spotify : ChatboxModule<SpotifyConfig>() {
    override val name = "Spotify"


    override val configClass = SpotifyConfig::class
    
    private var progressMs: Int = 0

    override fun buildChatbox(): List<String?> {
        val output = mutableListOf<String?>()
        if (!config!!.enabled || nowPlaying == null || !nowPlaying!!.isPlaying) return output
        val track = nowPlaying!!.item
        if (track?.asTrack == null) {
            return output
        }
        output.add("\uD83D\uDCFB ${track.asTrack!!.name} - ${track.asTrack!!.artists.mapNotNull { it.name }.joinToString(", ")}")
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
                    } ?.lead?.joinToString(" ") { it.words }
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
            if (Modules.Core.config!!.logs.debug) Napier.d("Opening browser to $url")
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
                    if (Modules.Core.config!!.logs.errors) Napier.e("Failed to update now playing", e)
                    updateSpotifyClient(true)
                }
                Thread.sleep(config!!.updateInterval.toLong())
            }
        }

        Thread {
            while(true) {
                if (nowPlaying?.isPlaying == true) {
                    progressMs += 250
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
        if (!config!!.lyrics.enabled || nowPlaying == null || !nowPlaying!!.isPlaying) {
            lyrics = null
            return
        }

        val response = httpClient.get("${config!!.lyrics.provider}/lyrics/${nowPlaying!!.item!!.id}")
        if (response.status == HttpStatusCode.OK) {
            val body: JsonObject = response.body()
            val type = body["syncType"]?.jsonPrimitive.toString().replace("\"", "")
            lyrics = when (type) {
                SyncType.UNSYNCED.name -> Lyrics(unsynced = json.decodeFromJsonElement(body))
                SyncType.LINE_SYNCED.name -> Lyrics(lineSynced = json.decodeFromJsonElement(body))
                SyncType.SYLLABLE_SYNCED.name -> Lyrics(syllableSynced = json.decodeFromJsonElement(body))
                else -> null
            }
        }
    }

    private suspend fun updateNowPlaying() {
        if (!config!!.enabled) return
        val oldId = nowPlaying?.item?.id
        if (!config!!.useAuthConfig) {
            if (config!!.nonAuth.endpoint.isNotEmpty()) {
                val response = httpClient.get(config!!.nonAuth.endpoint)
                if (response.status == HttpStatusCode.OK) {
                    nowPlaying = response.body()
                }
            } else {
                return
            }
        } else {
            if (spotifyClient == null) {
                updateSpotifyClient()
            }
            if (spotifyClient != null) {
                nowPlaying = spotifyClient!!.player.getCurrentlyPlaying()
            }
        }

        if (nowPlaying?.progressMs != null) {
            progressMs = nowPlaying?.progressMs ?: 0
        }

        if (nowPlaying?.item?.id != oldId) {
            if (config!!.lyrics.enabled) {
                getLyrics()
            }
        }
    }

    override val hasSettingsUI = true

    @Composable
    override fun onSettingsUI() {
        var enabled by remember { mutableStateOf(config!!.enabled) }
        var updateInterval by remember { mutableStateOf(config!!.updateInterval) }

        Text("Enabled")
        Checkbox(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                config!!.enabled = it
                saveConfig()
            },
        )

        Text("Update Interval")
        TextField(
            value = updateInterval.toString(),
            onValueChange = { updateInterval = it.toIntOrNull() ?: 0
                             config!!.updateInterval = updateInterval
                             saveConfig() },
        )

        var useAuthConfig by remember { mutableStateOf(config!!.useAuthConfig) }
        Text("Use Auth Config")
        // Center it
        Text(config!!._useAuthConfigExplanation, style = MaterialTheme.typography.caption.merge(
            TextStyle(textAlign = TextAlign.Center)
        ))
        Checkbox(
            checked = useAuthConfig,
            onCheckedChange = {
                useAuthConfig = it
                config!!.useAuthConfig = it
                saveConfig()
            },
        )
        if (useAuthConfig) {
            var clientId by remember { mutableStateOf(config!!.auth.clientId) }
            var clientSecret by remember { mutableStateOf(config!!.auth.clientSecret) }

            val scope = rememberCoroutineScope()

            Text("Client ID")
            TextField(
                value = clientId,
                onValueChange = { clientId = it
                                config!!.auth.clientId = it
                                saveConfig()},
            )

            Text("Client Secret")
            TextField(
                value = clientSecret,
                onValueChange = { clientSecret = it
                                config!!.auth.clientSecret = it
                                saveConfig()},
            )

            Button(onClick = {
                scope.launch {
                    updateSpotifyClient()
                }
            }) {
                Text("Update Credentials")
            }
        } else {
            var endpoint by remember { mutableStateOf(config!!.nonAuth.endpoint) }

            Text("Endpoint")
            TextField(
                value = endpoint,
                onValueChange = { endpoint = it },
            )

            config!!.nonAuth = SpotifyNonAuthConfig(endpoint)
        }

        var lyricsEnabled by remember { mutableStateOf(config!!.lyrics.enabled) }
        Text("Lyrics Enabled")
        Checkbox(
            checked = lyricsEnabled,
            onCheckedChange = {
                lyricsEnabled = it
                config!!.lyrics.enabled = it
                saveConfig()
            },
        )

        if (lyricsEnabled) {
            var provider by remember { mutableStateOf(config!!.lyrics.provider) }
            Text("Provider")
            Text(config!!.lyrics._providerExplanation, style = MaterialTheme.typography.caption.merge(
                TextStyle(textAlign = TextAlign.Center)
            ))
            TextField(
                value = provider,
                onValueChange = { provider = it
                                 config!!.lyrics.provider = it
                                 saveConfig() },
            )
        }
    }
}