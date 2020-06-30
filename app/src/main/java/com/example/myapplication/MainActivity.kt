package com.example.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.service.BackgroundService
import com.example.myapplication.worker.StartWorker
import com.example.myapplication.worker.StopWorker


class MainActivity : AppCompatActivity()  , View.OnClickListener{
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val action = intent.extras?.getString("action")
        if ( action!=null && action =="stop" ){
            service_action(action)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    fun work_start(view: View?) {
        Toast.makeText(this, "work_start", Toast.LENGTH_SHORT).show()
        val uploadWorkRequest = OneTimeWorkRequestBuilder<StartWorker>()
            .build()
        WorkManager.getInstance(applicationContext).enqueue(uploadWorkRequest)
    }

    fun work_stop(view: View?) {
        Toast.makeText(this, "work_stop", Toast.LENGTH_SHORT).show()
        val uploadWorkRequest = OneTimeWorkRequestBuilder<StopWorker>()
            .build()
        WorkManager.getInstance(applicationContext).enqueue(uploadWorkRequest)
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
        val intent = Intent(applicationContext, MediaProjectionActivity::class.java)
        startActivity(intent)
    }

    fun service_start_btn(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            service_action("start")
        } else {
            Log.e("sdfafasd","버전맞춰!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        }
    }

    fun service_stop_btn(view: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            service_action("stop")
        } else {
            Log.e("sdfafasd","버전맞춰!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun service_action(action:String) {
        Log.d("action",action)
        startForegroundService(BackgroundService.newService(this,action))
    }

}