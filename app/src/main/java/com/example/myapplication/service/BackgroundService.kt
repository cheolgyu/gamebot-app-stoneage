package com.example.myapplication.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.myapplication.MediaProjectionActivity
import com.example.myapplication.ShellExecuter
import com.example.myapplication.notification.Noti
import com.example.myapplication.tflite.Run
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer


class BackgroundService : Service() {
    companion object {
        var Run = false
        val TAG: String = "BackgroundService"
        private val FOREGROUND_SERVICE_ID = 1000
        private var serviceLooper: Looper? = null
        private var serviceHandler: ServiceHandler? = null
        private var cap_filename: String? = null
        private var handlerThread: HandlerThread? = null

        var context: Context? = null
        var my_action: String? = null

        var my_data: Intent? = null
        var my_resultCode: Int? = null

        fun newService(_context: Context,_action :String ): Intent =
            Intent(_context, BackgroundService::class.java).apply {
                my_action= _action
                context = _context
                if (_action == "start" || _action=="createVirtualDisplay"){
                    Run = true
                }else if (_action == "stop"){
                    Run = false
                }
            }
        fun newService(_context: Context,_action :String ,filename: String): Intent =
            Intent(_context, BackgroundService::class.java).apply {
                my_action= _action
                context = _context
                cap_filename = filename
                Run = true
            }

        fun newService(_context: Context,_action :String ,resultCode :Int,data:Intent): Intent =
            Intent(_context, BackgroundService::class.java).apply {
                my_data = data
                my_resultCode = resultCode
                my_action= _action
                context = _context
                //cap_bitmap = bitmap
                Run = true
            }
    }



    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            try {
                if(msg.obj != null){

//                    var arr :FloatArray? =model_test(msg.obj as String)
//
//                    if(arr != null){
//                        Log.e(TAG, "res============model_test not null=========================" )
//                        var cmd :String = "input tap "+String.format("%.1f", arr!!.get(0))+" "+String.format("%.1f", arr!!.get(1))
//                        Log.d(TAG, "adb -e shell " + cmd)
//                        var sh_out= ShellExecuter().Executer(cmd)
//                    }else{
//                        Log.e(TAG, "res============model_test null=========================" )
//                    }



                }else{
                    Log.i(TAG, "res============handleMessage==== msg.obj null=====================" )
                }

               // Thread.sleep(500000)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            stopSelf(msg.arg1)
        }
    }

    override fun onCreate() {
        Log.e(TAG, "--------------BackgroundService --------onCreate----------------------")

        my_notify()
        my_media()

        handlerThread = HandlerThread("서비스핸들러", Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread!!.start()
        serviceLooper = handlerThread!!.looper
        serviceHandler = ServiceHandler(handlerThread!!.looper)

    }

    private val mediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "--------------BackgroundService --------onStartCommand---------Run=$Run,action=$my_action")
        if(Run){
            if(my_action=="createVirtualDisplay"){
                createVirtualDisplay()
            }else if(my_action=="cap"){
                serviceHandler?.obtainMessage()?.also { msg ->
                    msg.obj = cap_filename
                    serviceHandler?.sendMessage(msg)
                }
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

    fun model_test(cap_filename:String): FloatArray? {
        Log.d(TAG, "res=====================================model_test" )
       var run = Run(this)
       run.build()
        var res =  run.get_xy(cap_filename)
      Log.d(TAG, "res=====================================model_test"+res.toString() )
        var cmd :String = "input tap "+String.format("%.1f", res!!.get(0))+" "+String.format("%.1f", res!!.get(1))
       // cmd = "input tap 1505.0 33.0"
       // var cmd = "/system/bin/input tap 520 1826.9"
        Log.d(TAG, "adb -e shell " + cmd)
        var sh_out= ShellExecuter().Executer(cmd)
        return null
       // return res
    }


    fun my_notify() {
        var noti = Noti(this)
        noti!!.createNotificationChannel()
        var notify = noti!!.build(11232131);
        startForeground(FOREGROUND_SERVICE_ID, notify)
        Log.e(TAG, "--------------BackgroundService --------my_notify----------------------Run="+Run)
    }

    fun my_media() {
        my_file()
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
        Log.e(TAG, "--------------BackgroundService --------my_media----------------------Run="+Run)
    }

    fun my_file(){
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



    private var IMAGES_PRODUCED = 0
    var STORE_DIRECTORY: String? = null
    val TAG: String = com.example.myapplication.MediaProjectionActivity::class.java.getName()

    var mediaProjection: MediaProjection? = null
    var mDisplay: Display? = null
    var mVirtualDisplay: VirtualDisplay? = null
    var mDensity = 0
    var mWidth = 100
    var mHeight = 100
    var mRotation = 0
    var mImageReader: ImageReader? = null
    val SCREENCAP_NAME = "screencap"
    val VIRTUAL_DISPLAY_FLAGS =
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
    var mHandler: Handler? = null
    @SuppressLint("WrongConstant")
    private fun createVirtualDisplay() {

        val intent = Intent(applicationContext, MediaProjectionActivity::class.java)
      //  val resultCode = intent.getIntExtra("", "")
      //  val data = intent.getParcelableExtra<Intent>("request_data")
        if(mediaProjectionManager == null){
            Log.e(TAG, "mediaProjectionManager null~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~````````")
        }
        var my_mediaProjection = mediaProjectionManager.getMediaProjection(my_resultCode!!, my_data!!)
        // start capture handling thread

        // start capture handling thread
        object : Thread() {
            override fun run() {
                Looper.prepare()
                mHandler = Handler()
                Looper.loop()
            }
        }.start()

        Log.d(TAG, "1111111111111111111createVirtualDisplay1111111111111111111111")

        // display metrics

        // display metrics
        val metrics = resources.displayMetrics
        mDensity = metrics.densityDpi
        var  windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mDisplay = windowManager.getDefaultDisplay()



        // get width and height
        val size = Point()
        mDisplay?.getSize(size)
        mWidth = size.x
        mHeight = size.y


        // start capture reader
        var my_mImageReader = ImageReader.newInstance(
            mWidth,
            mHeight,
            PixelFormat.RGBA_8888,
            2
        )

        var my_mVirtualDisplay = my_mediaProjection.createVirtualDisplay(
            SCREENCAP_NAME,
            mWidth,
            mHeight,
            mDensity,
            VIRTUAL_DISPLAY_FLAGS,
            my_mImageReader.getSurface(),
            null,
            mHandler
        )

        my_mImageReader.setOnImageAvailableListener(ImageAvailableListener(), mHandler)
        Log.d(TAG, "22222222222222222222222222222222222")

    }

    inner class ImageAvailableListener : ImageReader.OnImageAvailableListener {



        override fun onImageAvailable(reader: ImageReader) {
            Log.d(TAG, "-----------------------onImageAvailable----------------------------------")
            var image: Image? = null
            var fos: FileOutputStream? = null
            var bitmap: Bitmap? = null
            try {
                image = reader.acquireLatestImage()
                if (image != null) {
                    val width = image.width
                    val height = image.height
                    val planes: Array<Image.Plane> = image.getPlanes()
                    val buffer: ByteBuffer = planes[0].getBuffer()
                    val pixelStride: Int = planes[0].getPixelStride()
                    val rowStride: Int = planes[0].getRowStride()
                    val rowPadding: Int = rowStride - pixelStride * mWidth

                    // create bitmap
                    bitmap = Bitmap.createBitmap(
                        mWidth + rowPadding / pixelStride,
                        mHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)

                    // write bitmap to a file

                    // write bitmap to a file
                    var my_file = STORE_DIRECTORY + "myscreen_" + IMAGES_PRODUCED + ".JPEG"
                    fos =
                        FileOutputStream(my_file)
                    bitmap.compress(CompressFormat.JPEG, 100, fos)
                    //fos.close()
                    //image.close()
                    //reader.close()
                    IMAGES_PRODUCED++
                    Log.e(
                        TAG,
                        "captured image: " + my_file
                    )

                    Log.d(TAG, "-----------------------onImageAvailable----------------------------------"+my_file)
                    //startForegroundService(BackgroundService.newService(applicationContext,"cap",my_file))
                    model_test(my_file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {

                if (fos != null) {
                    try {
                        fos.close()
                    } catch (ioe: IOException) {
                        ioe.printStackTrace()
                    }
                }
                bitmap?.recycle()
                if (image != null) {
                    image.close()
                }
            }
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