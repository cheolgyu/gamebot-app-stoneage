package com.example.background.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast


abstract class BackgroundServiceMP : Service() {
    var RUN_BACKGROUND = false
    var my_data: Intent? = null
    var my_resultCode: Int? = null

    //display
    var mWidth = 0
    var mHeight = 0
    var mRotation = -1
    var mDensity = 0
    var imageReader: ImageReader? = null

    var mProjectionStopped = true
    val SCREENCAP_NAME = "screencap"
    val VIRTUAL_DISPLAY_FLAGS =
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
    var mHandler: Handler? = null
    var virtualDisplay: VirtualDisplay? = null
    var mediaProjection: MediaProjection? = null
    val mediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    fun set_display() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        var metrics = DisplayMetrics()
        var display = wm.defaultDisplay
        wm.defaultDisplay.getMetrics(metrics)
        mDensity = metrics.densityDpi

        display!!.getMetrics(metrics)
        mRotation = wm!!.getDefaultDisplay().getRotation()
        // get width and height
        var size = Point()
        display.getRealSize(size)
        mWidth = size.x
        mHeight = size.y
        Log.d("chk_display!!~~~~set_display", "조회한 xy $mWidth $mHeight")
        chk_display()
    }

    fun getScreenOrientation(): Int {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var t1 = wm.getDefaultDisplay().getRotation()
        Log.d("chk_display!!~~~~getScreenOrientation에서", "조회한 각도 $t1")
        return when (wm.getDefaultDisplay().getRotation()) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            else -> 0
        }
    }

    fun chk_display() {
        var str = "mWidth=$mWidth,"
        str += "mHeight=$mHeight,"
        str += "mRotation=$mRotation,"
        Log.d("---chk_display 현재-------", str)
    }


    @SuppressLint("WrongConstant")
    fun createVirtualDisplay() {
    chk_display()
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
        set_display()
        chk_display()
        make_image_reader()

        virtualDisplay = get_virtualDisplay()!!
        mProjectionStopped = false

        val orientationChangeCallback = OrientationChangeCallback()
        if (orientationChangeCallback.canDetectOrientation()) {
            orientationChangeCallback.enable();
        }


    }

    fun get_virtualDisplay(): VirtualDisplay? {
        make_image_reader()
        var vd = mediaProjection!!.createVirtualDisplay(
            SCREENCAP_NAME,
            mWidth,
            mHeight,
            mDensity,
            VIRTUAL_DISPLAY_FLAGS,
            imageReader!!.getSurface(),
            null,
            mHandler
        )
        mProjectionStopped = false

        return vd
    }

    @SuppressLint("WrongConstant")
    fun make_image_reader() {
        // start capture reader
        imageReader = ImageReader.newInstance(
            mWidth,
            mHeight,
            PixelFormat.RGBA_8888,
            2
        )
    }

    inner class OrientationChangeCallback internal constructor(

    ) :
        OrientationEventListener(this) {
        override fun onOrientationChanged(orientation: Int) {
            chk_display()
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val rotation: Int = display.getRotation();
            var cur_rotation = rotation// getScreenOrientation()
            Log.d(
                "chk_display",
                "============cur_rotation=$cur_rotation===============mRotation=$mRotation==========================="
            )

            if (mRotation != cur_rotation) {
                Log.d("chk_display", "==================111111====================================")

                set_display()
                make_image_reader()
                chk_display()
                if (virtualDisplay != null) {
                    virtualDisplay!!.release()
                }
                if (imageReader != null) {
                    imageReader!!.setOnImageAvailableListener(null, null)
                }
                if (!mProjectionStopped) {
                    virtualDisplay = get_virtualDisplay()!!
                }

            } else {
                Log.d(
                    "chk_display",
                    "==================22222222===================================="
                )
            }

            chk_display()

        }


    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }


    override fun onDestroy() {
        Log.d("", "onDestroy")
        RUN_BACKGROUND = false
        Toast.makeText(this, "service done onDestroy", Toast.LENGTH_SHORT).show()
    }

    fun stopForegroundService() {
        Log.d("", "Stop foreground service.")
        Toast.makeText(this, "service done stopForegroundService", Toast.LENGTH_SHORT).show()
        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }

}

