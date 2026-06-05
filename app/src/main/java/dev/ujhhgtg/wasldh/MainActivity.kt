package dev.ujhhgtg.wasldh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
                val pm = getSystemService(PowerManager::class.java)
                val isBatteryOptIgnored = pm.isIgnoringBatteryOptimizations(packageName)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ServerControlScreen(
                        isRunning = isServerRunning,
                        isBatteryOptIgnored = isBatteryOptIgnored,
                        onToggleServer = { shouldStart ->
                            if (shouldStart) {
                                window.addFlags(
                                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                )
                                val intent = Intent(this@MainActivity, DecryptService::class.java)
                                startForegroundService(intent)
                                isServerRunning = true
                            } else {
                                window.clearFlags(
                                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                )
                                val intent = Intent(this@MainActivity, DecryptService::class.java)
                                intent.action = DecryptService.ACTION_STOP
                                startService(intent)
                                isServerRunning = false
                            }
                        },
                        onRequestBatteryOpt = {
                            val intent = Intent(
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:$packageName")
                            )
                            startActivity(intent)
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
    isBatteryOptIgnored: Boolean,
    onToggleServer: (Boolean) -> Unit,
    onRequestBatteryOpt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRunning) "Server is running on port 8080\nScreen will stay on"
                   else "Server is stopped",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = { onToggleServer(!isRunning) }) {
            Text(if (isRunning) "Stop Server" else "Start Server")
        }

        Spacer(Modifier.height(24.dp))

        if (!isBatteryOptIgnored) {
            OutlinedButton(onClick = onRequestBatteryOpt) {
                Text("Disable Battery Optimization")
            }
        } else {
            Text(
                text = "Battery optimization disabled ✓",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
