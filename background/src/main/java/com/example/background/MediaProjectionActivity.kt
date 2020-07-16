package com.example.background

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.background.service.BackgroundService
import kotlinx.android.synthetic.main.activity_mediaprojection.*

open class MediaProjectionActivity : AppCompatActivity() {
    var msg = "접근성 권한이 필요해요."

    override fun onResume() {
        super.onResume()
        if(CheckTouch(this).chk()){
            msg="접근성 권한을 얻었습니다."
            textView2.setText(msg)
        }else{
            textView2.setText("접근성 권한이 필요해요.")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mediaprojection)
        textView2.setText(msg)

        val action = intent.extras?.getString("action")
        if ( action!=null && action =="stop" ){
            startService(BackgroundService.newService(this,"stop"))
        }

    }

    fun service_stop_btn(view: View?) {
        startService(BackgroundService.newService(this,"stop"))
    }

    fun service_start_btn(view: View?) {
        if(CheckTouch(this).chk()){
            msg="접근성 권한을 얻었습니다."
            textView2.setText(msg)
            var mediaProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            var captureIntent: Intent = mediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(captureIntent, 1000)
        }else{
            msg="접근성 권한이 필요해요."
            textView2.setText(msg)
            Toast.makeText( applicationContext, "접근성 권한이 필요해요.", Toast.LENGTH_SHORT).show()

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
}