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
        val TAG: String = "BackgroundService"
        private val FOREGROUND_SERVICE_ID = 1000
        private var serviceLooper: Looper? = null
        private var serviceHandler: ServiceHandler? = null

        private var handlerThread: HandlerThread? = null
        var mediaProjectionManager: MediaProjectionManager? = null

        fun newService(context: Context,action :String ): Intent =
            Intent(context, BackgroundService::class.java).apply {
                Run = true
                if (action == "stop"){
                    Run = false
                }
            }
    }



    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            if(msg.arg2 == 0){
                stopSelf(msg.arg1)
            }
            Log.d(TAG, "handleMessage" + msg.toString())
            Log.d(TAG, "handleMessage " + msg.arg2)
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                while (true){
                    Log.d(TAG, "" + msg.toString())
                    Log.d(TAG, "" + msg.arg1+","+msg.arg2)
                    Thread.sleep(500)
                }
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

                Thread.sleep(500000)
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1)
        }
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


        handlerThread = HandlerThread("서비스핸들러", Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread!!.start()
        serviceLooper = handlerThread!!.looper
        serviceHandler = ServiceHandler(handlerThread!!.looper)



    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("backend", "빌드 -onStartCommand------------"+Run)
        var arg2 = 0
        var jobId =0
        if(Run){
            startForegroundService()
             arg2 = 1
            jobId =1
            serviceHandler?.obtainMessage()?.also { msg ->
                msg.arg1 = jobId
                msg.arg2 = arg2
                serviceHandler?.sendMessage(msg)
            }
            Toast.makeText(this, "service starting~~~~~~~``", Toast.LENGTH_SHORT).show()
        }else{
            serviceHandler?.removeMessages(0)
            handlerThread!!.interrupt()
            handlerThread!!.quit()
            stopForegroundService()
        }





        // If we get killed, after returning from here, restart
        return START_STICKY
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
        //stopForeground(true)

        // Stop the foreground service.
        //stopSelf()
    }

     fun stopForegroundService() {
        Log.d("", "Stop foreground service.")

        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }

}