package dev.ujhhgtg.wasldh

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import me.hd.wauxv.hook.factory.MagicFactory

class DecryptService : Service() {

    companion object {
        const val CHANNEL_ID = "decrypt_server"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "dev.ujhhgtg.wasldh.STOP_SERVER"
        private const val TAG = "DecryptService"
    }

    private var serverInstance: EmbeddedServer<*, *>? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopServer()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            val notification = buildNotification()
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot start foreground: notification permission denied.", e)
        }

        if (serverInstance == null) {
            startServer()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --- Server ---

    private fun startServer() {
        serverInstance = embeddedServer(CIO, port = 8080) {
            routing {
                get("/decrypt") {
                    val keyParam = call.parameters["key"]?.toLongOrNull()
                    if (keyParam != null) {
                        try {
                            val result = MagicFactory.get(
                                keyParam,
                                p000.AbstractC3590Ujhhgtgfeyxiexzf.f11170Ujhhgtgfeyxiexzf
                            )
                            call.respondText(result)
                        } catch (e: Exception) {
                            call.respondText(
                                "JNI Error: ${e.message}",
                                status = HttpStatusCode.InternalServerError
                            )
                        }
                    } else {
                        call.respondText(
                            "Missing or invalid 'key' parameter.",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }

                get("/decryptBatch") {
                    val keysParam = call.parameters["keys"] ?: ""
                    val keys = keysParam.split(",").mapNotNull { it.trim().toLongOrNull() }

                    if (keys.isEmpty()) {
                        call.respondText(
                            "Missing or invalid 'keys' parameter (comma-separated).",
                            status = HttpStatusCode.BadRequest
                        )
                    } else {
                        val sb = StringBuilder()
                        for (k in keys) {
                            try {
                                val result = MagicFactory.get(
                                    k,
                                    p000.AbstractC3590Ujhhgtgfeyxiexzf.f11170Ujhhgtgfeyxiexzf
                                )
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

        try {
            serverInstance?.start(wait = false)
            Log.d(TAG, "Server started on port 8080")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server", e)
        }
    }

    private fun stopServer() {
        try {
            serverInstance?.stop(gracePeriodMillis = 500, timeoutMillis = 1000)
            serverInstance = null
            Log.d(TAG, "Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
    }

    // --- Notification ---

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Decryption Server",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "WASLDH decryption API server status"
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("WASLDH Server")
            .setContentText("Listening on port 8080")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
    }
}
