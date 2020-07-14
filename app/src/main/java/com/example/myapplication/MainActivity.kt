package com.example.myapplication

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.background.CheckTouch
import com.example.background.service.BackgroundService


class MainActivity : AppCompatActivity()  {
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        if(!CheckTouch(this).chk()){

        }else{

        }
       // start_touch_service_btn()
        val action = intent.extras?.getString("action")
        if ( action!=null && action =="stop" ){
            service_action(action)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun service_start_btn(view: View?) {
        if(!CheckTouch(this).chk()){
            Toast.makeText( applicationContext, "접근성 권한필요해요", Toast.LENGTH_SHORT).show()
        }else{
            service_action("start")
        }

    }

    fun service_stop_btn(view: View?) {
        service_action("stop")
    }

    private fun service_action(action:String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(BackgroundService.newService(this,action))
            Toast.makeText(
                applicationContext,
                "if", Toast.LENGTH_SHORT
            ).show()
        }else{
            Toast.makeText(
                applicationContext,
                "else--------!!!"+Build.VERSION.SDK_INT, Toast.LENGTH_SHORT
            ).show()
            startService(BackgroundService.newService(this,action))

        }
        Log.d("action",action)

    }

}