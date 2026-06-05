package dev.ujhhgtg.wasldh

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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

class MainActivity : ComponentActivity() {

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
                                val intent = Intent(this@MainActivity, DecryptService::class.java)
                                startForegroundService(intent)
                                isServerRunning = true
                            } else {
                                val intent = Intent(this@MainActivity, DecryptService::class.java)
                                intent.action = DecryptService.ACTION_STOP
                                startService(intent)
                                isServerRunning = false
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
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
            text = if (isRunning) "Server is running on port 8080" else "Server is stopped",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = { onToggleServer(!isRunning) }) {
            Text(if (isRunning) "Stop Server" else "Start Server")
        }
    }
}
