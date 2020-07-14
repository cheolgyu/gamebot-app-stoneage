package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.background.CheckTouch
import com.example.background.service.BackgroundService


class MainActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        if(!CheckTouch(this).chk()){

        }

        val action = intent.extras?.getString("action")
        if ( action!=null && action =="stop" ){
            startService(BackgroundService.newService(this,"stop"))
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun service_start_btn(view: View?) {
        if(!CheckTouch(this).chk()){
            Toast.makeText( applicationContext, "접근성 권한필요해요", Toast.LENGTH_SHORT).show()
        }else{
            var mediaProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            var captureIntent: Intent = mediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(captureIntent, 1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            startService(BackgroundService.newService(this,"start",resultCode,data!!))
        } else {
            Log.e("", "=================else====================")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun service_stop_btn(view: View?) {
        startService(BackgroundService.newService(this,"stop"))
    }


}