package com.example.background.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.OrientationEventListener
import android.view.WindowManager


class BackgroundServiceMP(
    val context: Context,
    val mediaProjectionManager: MediaProjectionManager,
    val wm: WindowManager
) {

    var mProjectionStopped = true
    var mDensity = 0
    val SCREENCAP_NAME = "screencap"
    val VIRTUAL_DISPLAY_FLAGS =
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
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

        virtualDisplay = get_virtualDisplay()!!
        mProjectionStopped = false

        val orientationChangeCallback = OrientationChangeCallback()
        if (orientationChangeCallback.canDetectOrientation()) {
            orientationChangeCallback.enable();
        }


    }

    fun get_virtualDisplay(): VirtualDisplay? {
        make_image_reader()

        return mediaProjection!!.createVirtualDisplay(
            SCREENCAP_NAME,
            BackgroundService.mWidth!!,
            BackgroundService.mHeight!!,
            mDensity,
            VIRTUAL_DISPLAY_FLAGS,
            BackgroundService.imageReader!!.surface,
            null,
            mHandler
        )
    }

    @SuppressLint("WrongConstant")
    fun make_image_reader() {


        var metrics = DisplayMetrics()
        var display = wm.defaultDisplay
        wm.defaultDisplay.getMetrics(metrics)
        mDensity = metrics.densityDpi

        display!!.getMetrics(metrics)
        BackgroundService.mRotation = wm!!.getDefaultDisplay().getRotation()
        // get width and height
        var size = Point()
        display.getRealSize(size)
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

    inner class OrientationChangeCallback internal constructor(

    ) :
        OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            val display = wm.defaultDisplay
            val rotation: Int = display.getRotation();
            if (rotation != BackgroundService.mRotation) {

                if (virtualDisplay != null) {
                    virtualDisplay!!.release()
                }
                if (BackgroundService.imageReader != null) {
                    BackgroundService.imageReader!!.setOnImageAvailableListener(null, null)
                }
                if (!mProjectionStopped) {
                    virtualDisplay = get_virtualDisplay()!!
                }
            }
        }


    }


}

