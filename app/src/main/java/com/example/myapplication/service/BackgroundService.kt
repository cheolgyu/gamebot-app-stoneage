package com.example.myapplication.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.myapplication.MediaProjection
import com.example.myapplication.MediaProjectionActivity
import com.example.myapplication.notification.Noti
import com.example.tf.tflite.Run
import java.io.File


class BackgroundService : Service() {
    companion object {
        var Run = false
        private val FOREGROUND_SERVICE_ID = 1000
        private var cap_filename: String? = null
        var STORE_DIRECTORY: String? = null
        val TAG: String = com.example.myapplication.MediaProjectionActivity::class.java.getName()

        var context: Context? = null
        var my_action: String? = null

        var my_data: Intent? = null
        var my_resultCode: Int? = null

        fun newService(_context: Context, _action: String): Intent =
            Intent(_context, BackgroundService::class.java).apply {
                my_action = _action
                context = _context
                if (_action == "start" || _action == "createVirtualDisplay") {
                    Run = true
                } else if (_action == "stop") {
                    Run = false
                }
            }

        fun newService(_context: Context, _action: String, resultCode: Int, data: Intent): Intent =
            Intent(_context, BackgroundService::class.java).apply {
                my_data = data
                my_resultCode = resultCode
                my_action = _action
                context = _context
                //cap_bitmap = bitmap
                Run = true
            }
    }

    override fun onCreate() {
        Log.e(TAG, "--------------BackgroundService --------onCreate----------------------")
        run_notify()
        ready_media()
    }

    private val mediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(
            TAG,
            "--------------BackgroundService --------onStartCommand---------Run=$Run,action=$my_action"
        )
        if (Run) {
            if (my_action == "createVirtualDisplay") {
                var windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                MediaProjection(
                    mediaProjectionManager,
                    windowManager.defaultDisplay,
                    STORE_DIRECTORY!!
                ).createVirtualDisplay()
            }
            Toast.makeText(this, "service starting~~~~~~~``", Toast.LENGTH_SHORT).show()
        } else {
            stopForegroundService()
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    fun tflite_run(cap_filename: String): FloatArray? {
        Log.d(TAG, "res=====================================tflite_run")
        var run = Run(this)
        run.build()
        var res = run.get_xy(cap_filename)
        Log.d(TAG, "res=====================================modetflite_runl_test" + res.toString())
        var cmd: String = "input tap " + String.format("%.1f", res!!.get(0)) + " " + String.format(
            "%.1f",
            res!!.get(1)
        )
        return null
        // return res
    }


    fun run_notify() {
        var noti = Noti(this)
        noti!!.createNotificationChannel()
        var notify = noti!!.build(11232131);
        startForeground(FOREGROUND_SERVICE_ID, notify)
        Log.e(
            TAG,
            "--------------BackgroundService --------my_notify----------------------Run=" + Run
        )
    }

    fun ready_media() {
        mkdir()
        /*
        1. 메인 액티비에서  서비스 실행
        2. 서비스에서 권한 얻기 는 서브액티비에서.
        3.  서브액티에서는
        mediaProjectionManager.createScreenCaptureIntent()
        찍고
        4. 결과는 서비스로 전달달         */
        //생성

        //권환얻기=> 는 액티비티
        startActivity(MediaProjectionActivity.newInstance(this))
        Log.e(
            TAG,
            "--------------BackgroundService --------my_media----------------------Run=" + Run
        )
    }

    fun mkdir() {
        val externalFilesDir = getExternalFilesDir(null)
        if (externalFilesDir != null) {
            STORE_DIRECTORY =
                externalFilesDir.absolutePath + "/screenshots/"
            val storeDirectory =
                File(STORE_DIRECTORY)
            if (!storeDirectory.exists()) {
                val success: Boolean = storeDirectory.mkdirs()
                if (!success) {
                    Log.e(
                        TAG,
                        "failed to create file storage directory."
                    )
                    return
                }
            }
        } else {
            Log.e(
                TAG,
                "failed to create file storage directory, getExternalFilesDir is null."
            )
            return
        }
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

    fun stopForegroundService() {
        Log.d("", "Stop foreground service.")

        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }

}