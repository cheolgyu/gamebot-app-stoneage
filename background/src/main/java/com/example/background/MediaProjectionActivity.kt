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

        fun newInstance(context: Context): Intent =
            Intent(null, MediaProjectionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

    val TAG: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        var captureIntent: Intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 1000)



    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            startService(
                BackgroundService.newService(
                    this,
                    "start2",
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