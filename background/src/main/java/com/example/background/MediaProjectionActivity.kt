package com.example.background

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.background.service.BackgroundService

class MediaProjectionActivity : AppCompatActivity() {
    companion object {
        private const val REQ_CODE_MEDIA_PROJECTION = 1000
        private const val REQUEST_CODE = 100

        fun newInstance(context: Context): Intent =
            Intent(context, MediaProjectionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

    val TAG: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(
            TAG,
            "=====================================\"MediaProjectionActivity====================================="
        )

        var mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        var captureIntent: Intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 1000)
        super.onCreate(savedInstanceState)


    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(TAG, "--------------onActivityResult----------------------")
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "--------------Activity.RESULT_OK----------------------")
            startForegroundService(
                BackgroundService.newService(
                    this,
                    "createVirtualDisplay",
                    resultCode,
                    data!!
                )
            )

        } else {
            Log.e(TAG, "=================else====================")
        }

        super.onActivityResult(requestCode, resultCode, data)

    }


}