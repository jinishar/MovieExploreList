package com.example.movieexplorelist

import android.app.Application
import androidx.work.*
import com.example.movieexplorelist.worker.RentalReminderWorker
import java.util.concurrent.TimeUnit

class MovieExploreApp : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleRentalReminder()
    }

    private fun scheduleRentalReminder() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<RentalReminderWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RentalReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}