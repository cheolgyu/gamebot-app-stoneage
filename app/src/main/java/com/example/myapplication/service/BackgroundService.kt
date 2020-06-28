package com.example.myapplication.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import android.widget.Toast
import com.example.myapplication.MediaProjectionActivity
import com.example.myapplication.ShellExecuter
import com.example.myapplication.notification.Noti
import com.example.myapplication.tflite.Run


class BackgroundService : Service() {
    companion object {
        val ACTION_START_FOREGROUND_SERVICE: String= "ACTION_START_FOREGROUND_SERVICE"
        val ACTION_STOP_FOREGROUND_SERVICE: String= "ACTION_STOP_FOREGROUND_SERVICE"
        var Run = false
        fun newService(context: Context,action :String ): Intent =
            Intent(context, BackgroundService::class.java).apply {
                Run = true
                if (action == "stop"){
                    Run = false
                }
            }
    }

    private val FOREGROUND_SERVICE_ID = 1000
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

             //   Log.d(TAG, "res=====================================" + res.toString())
                var arr :FloatArray? =model_test()
                model_test().let {

                    var cmd :String = "input tap "+String.format("%.1f", it?.get(0))+" "+String.format("%.1f", it?.get(1))
                    //37 은 application에서 터치 이벤트 못받음 .....
                    var cmd2 = "input tap 489.9 37.7"
                    Log.d(TAG, "adb -e shell " + cmd.toString())
                    var sh_out= ShellExecuter().Executer(cmd)
                    Log.d(TAG, "sh_out=====================================" + sh_out.toString())
                }

                Thread.sleep(50000)
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1)
        }
    }

    fun model_test(): FloatArray? {
        Log.d(TAG, "res=====================================model_test" )
        var run = Run(this)
        run.build()
        return run.get_xy()
    }


    fun my_notify() {
        var noti = Noti(this)
        noti!!.createNotificationChannel()
        var notify = noti!!.build(11232131);
        startForeground(FOREGROUND_SERVICE_ID, notify)
    }

    fun my_media() {
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        //startActivityForResult(mediaProjectionManager!!.createScreenCaptureIntent(), this)
        startActivity(MediaProjectionActivity.newInstance(this))
    }

    override fun onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        Log.d(TAG, "onCreate====================================="+Run)

        my_notify()
        //my_media()
        // my_click(this);
        //getRunActivity();


        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }


    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("backend", "빌드 -onStartCommand-"+Run)
        if(Run){
            startForegroundService()
        }else{
            stopForegroundService()
        }


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
        Log.d("", "onDestroy")
        Run = false
        //Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }


    private fun startForegroundService() {
        Log.d("", "start foreground service.")

        // Stop foreground service and remove the notification.
       // stopForeground(true)

        // Stop the foreground service.
        ///stopSelf()
    }

     fun stopForegroundService() {
        Log.d("", "Stop foreground service.")

        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }

}