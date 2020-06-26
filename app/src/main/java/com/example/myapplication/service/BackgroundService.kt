package com.example.myapplication.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.view.LayoutInflater
import android.widget.Toast
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.MediaProjectionActivity
import com.example.myapplication.MediaProjectionDemo
import com.example.myapplication.R
import com.example.myapplication.notification.Noti

class BackgroundService : Service() {

    companion object {
        fun newService(context: Context): Intent =
            Intent(context, BackgroundService::class.java)
    }

    private  val FOREGROUND_SERVICE_ID = 1000
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    val TAG: String = "BackgroundService"
    var mediaProjectionManager: MediaProjectionManager? = null

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1)
        }
    }

    fun my_notify(){
        var noti = Noti(this)
        noti!!.createNotificationChannel()
        var notify = noti!!.build(11232131);
        startForeground(FOREGROUND_SERVICE_ID, notify)
    }

    fun my_media(){
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        //startActivityForResult(mediaProjectionManager!!.createScreenCaptureIntent(), this)
        startActivity(MediaProjectionActivity.newInstance(this))
    }

    override fun onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        Log.d(TAG, "onCreate=====================================")
        my_notify()
        my_media()

        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }


    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("backend","빌드 -onStartCommand")

        Toast.makeText(this, "service starting~~~~~~~``", Toast.LENGTH_SHORT).show()
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }
        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }

}