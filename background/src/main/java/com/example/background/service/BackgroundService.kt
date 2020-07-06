package com.example.background.service

//import com.example.tf.tflite.Run
import android.annotation.SuppressLint
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.background.notification.Noti
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


class BackgroundService : Service() {
    companion object {
        var imageReader: ImageReader? = null
        var mWidth: Int? = null
        var mHeight: Int? = null

        var Run = false
        private val FOREGROUND_SERVICE_ID = 1000
        private var cap_filename: String? = null
        var STORE_DIRECTORY: String? = null
        val TAG: String = com.example.background.MediaProjectionActivity::class.java.getName()

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

    private val windowManager: WindowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    @Throws(java.lang.Exception::class)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(
            TAG,
            "--------------BackgroundService --------onStartCommand---------Run=$Run,action=$my_action"
        )
        if (Run) {
            if (my_action == "createVirtualDisplay") {
                //
                /*

                백그라운드 서비스 에서 이미지리더 생성후
                var img = imageReader.acquireLatestImage() 로 이미지 가져오고
                tflite로 좌표구하고
                구한결과 접근성.터치 로 실행.

                 */

                make_image_reader()

                BackgroundServiceMP(
                    mediaProjectionManager,
                    windowManager.defaultDisplay,
                    imageReader!!,
                    STORE_DIRECTORY!!
                ).createVirtualDisplay()

                // start capture handling thread
                object : Thread() {
                    override fun run() {
                        while (true){
                            Log.e(
                                "쓰레드",
                                "--------------------------------------------"
                            )
                            var file = image_available()
                            if (file != null) {
                                var arr : FloatArray =  tflite_run(file)
                                if(arr.size >0){
                                    var x = arr.get(0)
                                    var y = arr.get(1)

                                    touchService!!.click(x,y)
                                }
                            }else{
                                Log.e(
                                    "쓰레드",
                                    "image_available null "
                                )
                            }
                        }
                    }
                }.start()

            }
            Toast.makeText(this, "service starting~~~~~~~``", Toast.LENGTH_SHORT).show()
        } else {
            stopForegroundService()
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    fun image_available(): String? {
        var image = imageReader!!.acquireLatestImage()
        if (image != null) {
            var fos: FileOutputStream? = null
            var IMAGES_PRODUCED = 0
            val planes: Array<Image.Plane> = image.getPlanes()
            val buffer: ByteBuffer = planes[0].getBuffer()
            val pixelStride: Int = planes[0].getPixelStride()
            val rowStride: Int = planes[0].getRowStride()
            val rowPadding: Int = rowStride - pixelStride * mWidth!!

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
            var my_file = STORE_DIRECTORY + "myscreen_" + IMAGES_PRODUCED + ".JPEG"
            fos =
                FileOutputStream(my_file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)

            IMAGES_PRODUCED++
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

    @SuppressLint("WrongConstant")
    fun make_image_reader() {
        // display metrics
        val metrics = DisplayMetrics()
        var display = windowManager.defaultDisplay
        display!!.getMetrics(metrics)

        // get width and height
        val size = Point()
        display!!.getSize(size)
         mWidth = size.x
         mHeight = size.y

        // start capture reader
        imageReader = ImageReader.newInstance(
            mWidth!!,
            mHeight!!,
            PixelFormat.RGBA_8888,
            2
        )
    }

    fun tflite_run(cap_filename: String): FloatArray {
        Log.d(TAG, "res=====================================tflite_run")
        var run = com.example.tf.tflite.Run(this)
        run.build()
        var res = run.get_xy(cap_filename)
        Log.d(TAG, "res=====================================modetflite_runl_test" + res.toString())
        var cmd: String = "input tap " + String.format("%.1f", res!!.get(0)) + " " + String.format(
            "%.1f",
            res!!.get(1)
        )
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
        startActivity(com.example.background.MediaProjectionActivity.newInstance(this))
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