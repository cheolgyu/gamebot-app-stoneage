package com.example.background.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.OrientationEventListener
import android.view.WindowManager


class BackgroundServiceMP(
    val context: Context,
    val mediaProjectionManager: MediaProjectionManager,
    val display: Display,
    val imageReader: ImageReader,
    var mRotation: Int
) {

    var mProjectionStopped = true
    val TAG: String = ""
    var mDensity = 0
    var mWidth = 100
    var mHeight = 100
    val SCREENCAP_NAME = "screencap"
    val VIRTUAL_DISPLAY_FLAGS =
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
    var mHandler: Handler? = null
    var virtualDisplay: VirtualDisplay? = null
    var mediaProjection: MediaProjection? = null

    @SuppressLint("WrongConstant")
    fun createVirtualDisplay() {

        mediaProjection =
            mediaProjectionManager.getMediaProjection(
                my_resultCode!!,
                my_data!!
            )


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
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        mDensity = metrics.densityDpi

        // get width and height
        val size = Point()
        display.getSize(size)
        mWidth = size.x
        mHeight = size.y

//
//        // start capture reader
//        var imageReader = ImageReader.newInstance(
//            mWidth,
//            mHeight,
//            PixelFormat.RGBA_8888,
//            2
//        )

        virtualDisplay = get_virtualDisplay()!!

        var mHandler2: Handler? = null
        // start capture handling thread
//        object : Thread() {
//            override fun run() {
//                Looper.prepare()
//                mHandler2 = Handler()
//                Looper.loop()
//            }
//        }.start()

//        mHandler2.
//        while (true){
//            var img = imageReader.acquireLatestImage()
//            Log.e("test",img.toString())
//        }


//        imageReader.setOnImageAvailableListener(
//            BackgroundServiceMPListener(
//                mWidth,
//                mHeight,
//                STORE_DIRECTORY
//            ), null
//        )
        mProjectionStopped = false

        val orientationChangeCallback = OrientationChangeCallback()
        if (orientationChangeCallback.canDetectOrientation()) {
            orientationChangeCallback.enable();
        }


    }

    fun get_virtualDisplay(): VirtualDisplay? {

        return mediaProjection!!.createVirtualDisplay(
            SCREENCAP_NAME,
            mWidth,
            mHeight,
            mDensity,
            VIRTUAL_DISPLAY_FLAGS,
            imageReader.getSurface(),
            null,
            mHandler
        )
    }

    private inner class OrientationChangeCallback internal constructor(

    ) :
        OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {

            val rotation: Int = display.getRotation();

            if (rotation != mRotation) {

                mRotation = rotation
                if (virtualDisplay != null) {
                    virtualDisplay!!.release()

                }
                if (imageReader != null) {
                    // imageReader.setOnImageAvailableListener(null, null)
                    make_image_reader()

                }
                if (!mProjectionStopped) {
                    get_virtualDisplay()

                }
            } else {

            }
        }

        @SuppressLint("WrongConstant")
        fun make_image_reader() {

            // display metrics
            val metrics = DisplayMetrics()
            var wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            display!!.getMetrics(metrics)
            BackgroundService.mRotation = wm.getDefaultDisplay().getRotation()
            // get width and height
            val size = Point()
            display!!.getSize(size)
            BackgroundService.mWidth = size.x
            BackgroundService.mHeight = size.y
            var aa = size.toString()

            // start capture reader
            BackgroundService.imageReader = ImageReader.newInstance(
                BackgroundService.mWidth!!,
                BackgroundService.mHeight!!,
                PixelFormat.RGBA_8888,
                2
            )
        }
    }
}