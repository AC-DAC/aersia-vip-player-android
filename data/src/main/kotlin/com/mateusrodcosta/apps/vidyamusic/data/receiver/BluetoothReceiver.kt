package com.mateusrodcosta.apps.vidyamusic.data.receiver

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.mateusrodcosta.apps.vidyamusic.domain.repository.PreferencesRepository
import java.io.File
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val CHANNEL_ID = "bluetooth_autolaunch"
private const val NOTIFICATION_ID = 1

class BluetoothReceiver : BroadcastReceiver(), KoinComponent {
    private val preferencesRepository: PreferencesRepository by inject()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val file = File(context.filesDir, "bt_debug.txt")
        file.appendText("onReceive fired: ${intent.action} at ${System.currentTimeMillis()}\n")

        if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED) return

        val pendingResult = goAsync()
        GlobalScope.launch {
            try {
                val enabled = preferencesRepository.bluetoothAutoLaunch.first()
                file.appendText("autoLaunch: $enabled\n")
                if (!enabled) return@launch

                val keyguardManager =
                    context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                val powerManager =
                    context.getSystemService(Context.POWER_SERVICE) as PowerManager

                file.appendText("isLocked: ${keyguardManager.isDeviceLocked}\n")
                file.appendText("isInteractive: ${powerManager.isInteractive}\n")

                if (!keyguardManager.isDeviceLocked && powerManager.isInteractive) {
                    val launchIntent = context.packageManager
                        .getLaunchIntentForPackage(context.packageName)
                        ?.apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("expand_player", true)
                        }
                    file.appendText("launchIntent: ${launchIntent != null}\n")

                    if (launchIntent != null) {
                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        val channel = NotificationChannel(
                            CHANNEL_ID,
                            "Bluetooth Auto-Launch",
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        notificationManager.createNotificationChannel(channel)

                        val contentIntent = PendingIntent.getActivity(
                            context,
                            0,
                            launchIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        val iconResId = context.resources.getIdentifier(
                            "media3_notification_small_icon", "drawable", context.packageName
                        )
                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(iconResId)
                            .setContentTitle("Aersia")
                            .setContentText("Tap to start playing")
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true)
                            .build()

                        notificationManager.notify(NOTIFICATION_ID, notification)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
