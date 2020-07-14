package com.example.background.service

//import com.example.tf.tflite.Run
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import com.example.background.notification.Noti
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


var RUN_BACKGROUND = false
var my_data: Intent? = null
var my_resultCode: Int? = null
var STORE_DIRECTORY: String? = null


class BackgroundService : Service() {
    companion object {
        var mBackgroundThread: BackgroundThread? = null
        var imageReader: ImageReader? = null
        var mWidth: Int? = null
        var mHeight: Int? = null
        var mRotation: Int? = null
        private val FOREGROUND_SERVICE_ID = 1000

        val TAG: String = com.example.background.MediaProjectionActivity::class.java.getName()

        var context: Context? = null
        var my_action: String? = null

        fun newService(_context: Context, _action: String): Intent =
            Intent(_context, BackgroundService::class.java).apply {
                my_action = _action
                context = _context
                if (_action == "start") {
                    RUN_BACKGROUND = true
                } else if (_action == "stop") {
                    RUN_BACKGROUND = false
                }
            }

        fun newService(_context: Context, _action: String, resultCode: Int, data: Intent): Intent =
            Intent(_context, BackgroundService::class.java).apply {
                my_data = data
                my_resultCode = resultCode
                my_action = _action
                context = _context
                //cap_bitmap = bitmap
                RUN_BACKGROUND = true
            }

    }

    override fun onCreate() {
        Log.e(TAG, "--------------BackgroundService --------onCreate----------------------")
        run_notify()
        ready_media()
        if(my_action=="start"){

        }
        if (RUN_BACKGROUND && my_action=="start2") {

        }
    }

    val mediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    val windowManager: WindowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }


    @Throws(java.lang.Exception::class)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(
            TAG,
            "--------------BackgroundService --------onStartCommand---------RUN_BACKGROUND=$RUN_BACKGROUND,action=$my_action"
        )
        if (RUN_BACKGROUND) {

            BackgroundServiceMP(
                context!!,
                mediaProjectionManager,
                windowManager
            ).createVirtualDisplay()

            // start capture handling thread
            mBackgroundThread = BackgroundThread()
            mBackgroundThread!!.start()

            Toast.makeText(this, "service starting~~~~~~~``", Toast.LENGTH_SHORT).show()
        } else {
            stopForegroundService()
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }


    //mp 서비스에서 구현
    fun image_available(): String? {
        var image = imageReader!!.acquireLatestImage()
        if (image != null) {
            var fos: FileOutputStream? = null
            val planes: Array<Image.Plane> = image.getPlanes()
            val buffer: ByteBuffer = planes[0].getBuffer()
            val pixelStride: Int = planes[0].getPixelStride()
            val rowStride: Int = planes[0].getRowStride()
            val rowPadding: Int = rowStride - pixelStride * mWidth!!

            Log.d("리사이즈---",mWidth.toString()+",이미지:w= "+mWidth!! + rowPadding / pixelStride+",mHeight="+mHeight.toString())
            // create bitmap
            var bitmap = Bitmap.createBitmap(
                mWidth!! + rowPadding / pixelStride,
                mHeight!!,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            image.close()
            // write bitmap to a file

            // write bitmap to a file
            val file_id = SystemClock.uptimeMillis()
            var my_file = STORE_DIRECTORY + file_id + ".JPEG"
            fos =
                FileOutputStream(my_file)
            Log.d("리사이즈---bitmap-정보",bitmap.width.toString()+","+bitmap.height.toString())
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)

            Log.e(
                ContentValues.TAG,
                "captured image: " + my_file
            )

            Log.d(
                ContentValues.TAG,
                "-----------------------onImageAvailable----------------------------------" + my_file
            )
            return my_file

        }
        return null
    }

    fun getScreenOrientation(): Int {
        return when (windowManager.getDefaultDisplay().getRotation()) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            else -> 0
        }
    }

    fun tflite_run(full_path: String): FloatArray? {
        val so = getScreenOrientation()
        var run = com.example.tf.tflite.Run(this, so)
        run.build(full_path)
        var res = run.get_xy(full_path)

        //return null
        return res
    }


    fun run_notify() {
        var noti = Noti(this)
        noti!!.createNotificationChannel()
        var notify = noti!!.build(11232131);
        startForeground(FOREGROUND_SERVICE_ID, notify)
        Log.e(
            TAG,
            "--------------BackgroundService --------my_notify----------------------RUN_BACKGROUND=" + RUN_BACKGROUND
        )
    }

    fun ready_media() {
        mkdir()
       //startActivity(com.example.background.MediaProjectionActivity.newInstance(applicationContext))
        Log.e(
            TAG,
            "--------------BackgroundService --------my_media----------------------RUN_BACKGROUND=" + RUN_BACKGROUND
        )
    }

    fun mkdir() {
        val externalFilesDir = getExternalFilesDir(null)
        if (externalFilesDir != null) {
            STORE_DIRECTORY =
                externalFilesDir.absolutePath + "/screenshots/"
            val storeDirectory =
                File(STORE_DIRECTORY)
            storeDirectory.deleteRecursively()
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
        RUN_BACKGROUND = false
        //Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }

    fun stopForegroundService() {
        Log.d("", "Stop foreground service.")

        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }

    inner class BackgroundThread : Thread() {

        override fun run() {
            while (RUN_BACKGROUND) {
                Thread.sleep(100)

                Log.e(
                    "쓰레드",
                    "--------------------------------------------"
                )

                Log.e(
                    "쓰레드",
                    "-------------------mWidth=$mWidth--mHeight=$mHeight-----------------------"
                )
                var full_path = image_available()

                if (full_path != null && full_path != "") {
                    var arr: FloatArray? = tflite_run(full_path)
                    if (arr != null) {
                        var x = arr.get(0)
                        var y = arr.get(1)
                        Log.e(
                            "쓰레드",
                            "--------------$x---$y---------------------------"
                        )
                        touchService!!.click(x, y)
                    } else {
                        Log.e(
                            "쓰레드",
                            "tflite_run return null "
                        )
                    }
                } else {
                    Log.d(
                        "쓰레드",
                        "image_available null "
                    )
                }
            }
        }

    }


}