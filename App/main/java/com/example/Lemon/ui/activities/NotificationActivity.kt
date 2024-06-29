package com.example.arcus.ui.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.arcus.R

class NotificationActivity : ComponentActivity() {

    private val CHANNEL_ID = "channel_id"
    private val NOTIFICATION_ID = 101

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nameOfLocation = intent.getStringExtra("nameOfLocation") ?: "Unknown location"
        val currentTemperatureRoundedToInt = intent.getIntExtra("currentTemperatureRoundedToInt", -1)
        val shortDescription = intent.getStringExtra("shortDescription") ?: "No description"


        createNotificationChannel()
        if (checkNotificationPermission()) {
            displayContinuousNotification(
                nameOfLocation,
                currentTemperatureRoundedToInt,
                shortDescription,

            )
        } else {
            requestNotificationPermission()
        }
        finish()
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Example Notification Channel"
            val descriptionText = "Description for Example Notification Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun displayNotification() {
        // Intent to open MainActivity when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_day_clear)
            .setContentTitle("Test Notification")
            .setContentText("This is a test notification from Jetpack Compose.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission check and request logic
            return
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notificationBuilder.build())
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_ID // Use any integer for the request code
        )
    }

    private fun displayContinuousNotification(
        nameOfLocation: String,
        currentTemperatureRoundedToInt: Int,
        shortDescription: String,

    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_day_clear)// Use the icon passed from the intent
            .setContentTitle("$nameOfLocation Weather Update")
            .setContentText("Temp: $currentTemperatureRoundedToInt°C, $shortDescription.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Makes this notification continuous

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notificationBuilder.build())
    }


    private fun displayConditionalNotification(weatherCode: Int) {
        val weatherCodeToDescriptionMap = mapOf(
            0 to "Clear sky",
            1 to "Mainly clear",
            2 to "Partly cloudy",
            3 to "Overcast",
            45 to "Fog",
            48 to "Depositing rime fog",
            51 to "Drizzle",
            53 to "Drizzle",
            55 to "Drizzle",
            56 to "Freezing drizzle",
            57 to "Freezing drizzle",
            61 to "Slight rain",
            63 to "Moderate rain",
            65 to "Heavy rain",
            66 to "Light freezing rain",
            67 to "Heavy freezing rain",
            71 to "Slight snow fall",
            73 to "Moderate snow fall",
            75 to "Heavy snow fall",
            77 to "Snow grains",
            80 to "Slight rain showers",
            81 to "Moderate rain showers",
            82 to "Violent rain showers",
            85 to "Slight snow showers",
            86 to "Heavy snow showers",
            95 to "Thunderstorms",
            96 to "Thunderstorms with slight hail",
            99 to "Thunderstorms with heavy hail",
        )
        val weatherDescription = weatherCodeToDescriptionMap[weatherCode]
        if (weatherDescription != null && weatherCode in setOf(61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_day_clear)
                .setContentTitle("Weather Alert")
                .setContentText("Alert: $weatherDescription")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID + 1, notificationBuilder.build())
        }
    }



    @Composable
    fun NotificationScreen() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Notification has been sent. Check your notification tray.")
        }
    }
}
