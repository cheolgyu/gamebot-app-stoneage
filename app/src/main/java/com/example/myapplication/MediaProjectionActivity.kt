package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.OrientationEventListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.service.BackgroundService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class MediaProjectionActivity : AppCompatActivity() {
    companion object {
        private const val REQ_CODE_MEDIA_PROJECTION = 1000
        private const val REQUEST_CODE = 100

        fun newInstance(context: Context): Intent =
            Intent(context, MediaProjectionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

    private var IMAGES_PRODUCED = 0
    var STORE_DIRECTORY: String? = null
    val TAG: String = com.example.myapplication.MediaProjectionActivity::class.java.getName()
    var mediaProjectionManager: MediaProjectionManager? = null
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
    val mHandler: Handler? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "=====================================\"MediaProjectionActivity=====================================")

        var mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        var captureIntent :Intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 1000);
        super.onCreate(savedInstanceState)



    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(TAG, "--------------onActivityResult----------------------")
        if (resultCode == Activity.RESULT_OK) {

            Log.d(TAG, "--------------Activity.RESULT_OK----------------------")


            //createVirtualDisplay()

            startForegroundService(BackgroundService.newService(this,"createVirtualDisplay",resultCode,data!!))

        } else {
            Log.e(TAG, "=================else====================")
        }

        super.onActivityResult(requestCode, resultCode, data)

    }


}