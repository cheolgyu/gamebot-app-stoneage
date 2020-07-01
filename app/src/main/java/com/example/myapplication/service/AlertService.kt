package com.example.myapplication.service

import android.app.Service
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import com.example.myapplication.R


class AlertService : Service() {
    var wm: WindowManager? = null
    var mView: View? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        val inflate =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        wm =
            getSystemService(WINDOW_SERVICE) as WindowManager?
        val params =
            WindowManager.LayoutParams( /*ViewGroup.LayoutParams.MATCH_PARENT*/
                300,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
        params.gravity = Gravity.LEFT or Gravity.TOP
        mView = inflate!!.inflate(R.layout.service_alert, null)
        val textView2 = mView?.findViewById(R.id.textView2) as TextView
        val button10 = mView!!.findViewById(R.id.button10) as Button
        button10.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                textView2.text = "on click!!"
            }
        })
        wm!!.addView(mView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wm != null) {
            if (mView != null) {
                wm!!.removeView(mView)
                mView = null
            }
            wm = null
        }
    }
}