package com.csci442.geolocatr

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture


class LocatrWorker(private val context: Context, workerParams: WorkerParameters) :
    ListenableWorker(context, workerParams) {

    private val logTag = "448.locatrWorker"

    override fun startWork(): ListenableFuture<Result> {
        Log.d(logTag, "Work triggered")

        return CallbackToFutureAdapter.getFuture { completer ->
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            // Check permissions
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    Log.d(logTag, "Got a location: $location")

                    val notificationManager = NotificationManagerCompat.from(context)
                    val channelID =
                        context.resources.getString(R.string.notification_channel_id)
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val name = context.resources.getString(R.string.notification_channel_name)
                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                        val descriptionText = context.resources.getString(R.string.notification_channel_desc)
                        val channel = NotificationChannel(channelID, name, importance). apply {
                            description = descriptionText
                        }
                        notificationManager.createNotificationChannel(channel)

                        // Create intent for new activity
                        val intent =
                            DrawerActivity.createIntent(context, location).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

                        val notification = NotificationCompat.Builder(context, channelID)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle(context.resources.getString(R.string.notification_title))
                            .setContentText(location.toString())
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .build()
                        notificationManager.notify(0, notification)
                    }
                    completer.set(Result.success())
                }
                fusedLocationProviderClient.lastLocation.addOnFailureListener {
                    completer.set(Result.failure())
                }
                fusedLocationProviderClient.lastLocation.addOnCanceledListener {
                    completer.set(Result.failure())
                }
            }
        }
    }
}