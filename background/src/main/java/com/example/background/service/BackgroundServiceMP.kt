package com.example.background.service

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display


class BackgroundServiceMP(
    val mediaProjectionManager: MediaProjectionManager,
    val display: Display,
    val STORE_DIRECTORY: String
) {


    val TAG: String = ""
    var mDisplay: Display? = null
    var mDensity = 0
    var mWidth = 100
    var mHeight = 100
    val SCREENCAP_NAME = "screencap"
    val VIRTUAL_DISPLAY_FLAGS =
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
    var mHandler: Handler? = null

    @SuppressLint("WrongConstant")
    fun createVirtualDisplay() {

        var mediaProjection =
            mediaProjectionManager.getMediaProjection(
                BackgroundService.my_resultCode!!,
                BackgroundService.my_data!!
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
        mDisplay = display

        // get width and height
        val size = Point()
        display.getSize(size)
        mWidth = size.x
        mHeight = size.y


        // start capture reader
        var imageReader = ImageReader.newInstance(
            mWidth,
            mHeight,
            PixelFormat.RGBA_8888,
            2
        )

        mediaProjection.createVirtualDisplay(
            SCREENCAP_NAME,
            mWidth,
            mHeight,
            mDensity,
            VIRTUAL_DISPLAY_FLAGS,
            imageReader.getSurface(),
            null,
            mHandler
        )
        var mHandler2 : Handler? =null
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


        imageReader.setOnImageAvailableListener(
            BackgroundServiceMPListener(
                mWidth,
                mHeight,
                STORE_DIRECTORY
            ), null
        )
        Log.d(TAG, "22222222222222222222222222222222222")

    }
}