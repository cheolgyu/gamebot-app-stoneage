package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.service.AlertService
import com.example.background.service.BackgroundService



class MainActivity : AppCompatActivity()  , View.OnClickListener{
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val action = intent.extras?.getString("action")
        if ( action!=null && action =="stop" ){
            service_action(action)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val  button7 =
            findViewById<View>(R.id.button7) as Button
        button7.setOnClickListener {
            checkPermission()
            Toast.makeText(
                applicationContext,
                "222222222222222", Toast.LENGTH_SHORT
            ).show()
        }



        val button2 =
            findViewById<View>(R.id.button2) as Button
        button2.setOnClickListener {
            Toast.makeText(
                applicationContext,
                "222222222222222", Toast.LENGTH_SHORT
            ).show()
        }
        val button3 =
            findViewById<View>(R.id.button3) as Button
        button3.setOnClickListener {
            Toast.makeText(
                applicationContext,
                "33333333333333", Toast.LENGTH_SHORT
            ).show()
        }

        val button4 =
            findViewById<View>(R.id.button4) as Button
        button4.setOnClickListener {
            Toast.makeText(
                applicationContext,
                "4444444444444444444", Toast.LENGTH_SHORT
            ).show()
        }

    }

    fun checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            } else {
                startService(Intent(this@MainActivity, AlertService::class.java))
            }
        } else {
            startService(Intent(this@MainActivity, AlertService::class.java))
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {


                val x = event.getX()
                val y = event.getY()

            Log.d("터치를 입력받음 ",x.toString()+" / " +y)
                //Toast. makeText(applicationContext, msg, Toast.LENGTH_SHORT ).show();
                return true;
        }


        return super.onTouchEvent(event)
    }
    fun showMessage(view: View?) {
        Toast.makeText(this, "1111111111111111111", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
        Toast.makeText(this, "0000000000000000000", Toast.LENGTH_SHORT).show();
    }

    var Button4OnClick =
        View.OnClickListener {
            Toast.makeText(
                applicationContext,
                "Button4 Clicked!", Toast.LENGTH_SHORT
            ).show()
        }

    fun move_btn_demo(view: View?){
        val intent = Intent(applicationContext, com.example.background.MediaProjectionActivity::class.java)
        startActivity(intent)
    }

    fun service_start_btn(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            service_action("start")
        } else {
            service_action("start")
            Log.e("sdfafasd","버전맞춰!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        }
    }

    fun service_stop_btn(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            service_action("stop")
        } else {
            Toast.makeText(
                applicationContext,
                "stop !", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun service_action(action:String) {
        Toast.makeText(
            applicationContext,
            "else-Build.VERSION.SDK_INT="+Build.VERSION.SDK_INT, Toast.LENGTH_SHORT
        ).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(BackgroundService.newService(this,action))
            Toast.makeText(
                applicationContext,
                "if", Toast.LENGTH_SHORT
            ).show()
        }else{
            Toast.makeText(
                applicationContext,
                "else"+Build.VERSION.SDK_INT, Toast.LENGTH_SHORT
            ).show()
            startService(BackgroundService.newService(this,action))

        }
        Log.d("action",action)

    }

}