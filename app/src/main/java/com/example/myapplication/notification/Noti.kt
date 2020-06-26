package com.example.myapplication.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.MediaProjectionDemo
import com.example.myapplication.R

class Noti(val _context: Context)  {
    val CHANNEL_ID = "1000212121"
    var notificationManager:NotificationManager ?= null

    fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = _context.getString(R.string.channel_name)
            val descriptionText = _context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            notificationManager =
                _context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager!!.createNotificationChannel(channel)
        }else{
            Log.e("noti","버전확인!!!")
        }
    }

    fun build( notificationId : Int,title: String="게임봇",text:String="게임봇이 활성화중입니다."): Notification {

        val notificationIntent = Intent(_context, MediaProjectionDemo::class.java)
        val pendingIntent = PendingIntent.getActivity(
            _context,
            0, notificationIntent, 0
        )

        var builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(_context!!, CHANNEL_ID)

        } else {
            TODO("VERSION.SDK_INT < O")
            Notification.Builder(_context!!)
        }
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent).build()
        return builder
    }

}