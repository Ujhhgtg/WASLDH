package dev.ujhhgtg.wasldh

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.ujhhgtg.wasldh.ui.theme.WASLDHTheme
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.hd.wauxv.hook.factory.MagicFactory

class MainActivity : ComponentActivity() {

    private val serverScope = CoroutineScope(Dispatchers.IO)
    private var serverInstance: EmbeddedServer<*, *>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WASLDHTheme {
                var isServerRunning by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ServerControlScreen(
                        isRunning = isServerRunning,
                        onToggleServer = { shouldStart ->
                            if (shouldStart) {
                                startServer { isServerRunning = true }
                            } else {
                                stopServer { isServerRunning = false }
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun startServer(onStarted: () -> Unit) {
        serverScope.launch {
            try {
                if (serverInstance == null) {
                    serverInstance = embeddedServer(CIO, port = 8080) {
                        routing {
                            get("/decrypt") {
                                val keyParam = call.parameters["key"]?.toLongOrNull()

                                if (keyParam != null) {
                                    try {
                                        val decryptedResult = MagicFactory.get(keyParam, p000.AbstractC3590Ujhhgtgfeyxiexzf.f11170Ujhhgtgfeyxiexzf)
                                        call.respondText(decryptedResult)
                                    } catch (e: Exception) {
                                        call.respondText("JNI Error: ${e.message}", status = HttpStatusCode.InternalServerError)
                                    }
                                } else {
                                    call.respondText("Missing or invalid 'key' parameter.", status = HttpStatusCode.BadRequest)
                                }
                            }

                            get("/decryptBatch") {
                                val keysParam = call.parameters["keys"] ?: ""
                                val keys = keysParam.split(",").mapNotNull { it.trim().toLongOrNull() }

                                if (keys.isEmpty()) {
                                    call.respondText("Missing or invalid 'keys' parameter (comma-separated).", status = HttpStatusCode.BadRequest)
                                } else {
                                    val sb = StringBuilder()
                                    for (k in keys) {
                                        try {
                                            val result = MagicFactory.get(k, p000.AbstractC3590Ujhhgtgfeyxiexzf.f11170Ujhhgtgfeyxiexzf)
                                            val escaped = result.replace("\n", "\\n").replace("\r", "\\r")
                                            sb.append(k).append(":").append(escaped).append('\n')
                                        } catch (e: Exception) {
                                            sb.append(k).append(":JNI Error: ").append(e.message).append('\n')
                                        }
                                    }
                                    call.respondText(sb.toString())
                                }
                            }
                        }
                    }
                }

                // Important: wait = false stops Ktor from blocking the coroutine execution loop permanently
                serverInstance?.start(wait = false)
                Log.d("RPC_Server", "Server engine active on port 8080.")
                onStarted()
            } catch (e: Exception) {
                Log.e("RPC_Server", "Failed to spin up Ktor instance", e)
            }
        }
    }

    private fun stopServer(onStopped: () -> Unit) {
        serverScope.launch {
            try {
                // Gracefully teardown requests within 500ms
                serverInstance?.stop(gracePeriodMillis = 500, timeoutMillis = 1000)
                serverInstance = null
                Log.d("RPC_Server", "Server engine destroyed.")
                onStopped()
            } catch (e: Exception) {
                Log.e("RPC_Server", "Error encountered during teardown", e)
            }
        }
    }
}

@Composable
fun ServerControlScreen(
    isRunning: Boolean,
    onToggleServer: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRunning) "Server status: RUNNING (Port 8080)" else "Server status: OFFLINE",
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(onClick = { onToggleServer(!isRunning) }) {
            Text(text = if (isRunning) "Stop Decryption Server" else "Start Decryption Server")
        }
    }
}