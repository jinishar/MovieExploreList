package com.example.movieexplorelist.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.movieexplorelist.data.db.AppDatabase
import com.example.movieexplorelist.data.repository.MovieRepository

class RentalReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "rental_reminder_channel"
        const val CHANNEL_NAME = "Rental Reminders"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(context)
            val repository = MovieRepository(db)
            val rentalCount = repository.getRentalCount()

            if (rentalCount > 0) {
                showRentalNotification(rentalCount)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showRentalNotification(rentalCount: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders about your active movie rentals"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("🎬 Movie Rental Reminder")
            .setContentText(
                if (rentalCount == 1)
                    "You have 1 active movie rental. Enjoy watching!"
                else
                    "You have $rentalCount active movie rentals. Time to watch!"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "You have $rentalCount rented movie${if (rentalCount > 1) "s" else ""} waiting for you. " +
                        "Open Movie Explorer to manage your rentals."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}