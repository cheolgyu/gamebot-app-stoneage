package com.example.background.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.Context
import android.graphics.Path
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.FrameLayout
import com.example.background.R

var touchService: TouchService? = null

class TouchService : AccessibilityService() {
    var mLayout: FrameLayout? = null
    override fun onServiceConnected() {
        super.onServiceConnected()
        // Create an overlay and display the action bar

        // Create an overlay and display the action bar
        val wm =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP
        val inflater = LayoutInflater.from(this)

        inflater.inflate(R.layout.action_bar, mLayout)
        wm.addView(mLayout, lp)

        configure()

        touchService =this
    }

    fun configure(){
        configureSwipeButton()
        configureStopButton()
        configureStartButton()
    }

    fun click(x: Float, y: Float) {
        val clickPath = Path()
        clickPath.moveTo(x, y)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(StrokeDescription(clickPath, 0, 1000))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun configureSwipeButton() {
        val swipeButton: Button = mLayout!!.findViewById<View>(R.id.swipe) as Button
        swipeButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val swipePath = Path()
                swipePath.moveTo(1000F, 1000F)
                swipePath.lineTo(100F, 1000F)
                val gestureBuilder = GestureDescription.Builder()
                gestureBuilder.addStroke(StrokeDescription(swipePath, 0, 500))
                dispatchGesture(gestureBuilder.build(), null, null)
            }
        })
    }

    private fun configureStartButton() {
        val btn: Button = mLayout!!.findViewById<View>(R.id.start) as Button
        btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                startActivity(com.example.background.MediaProjectionActivity.newInstance(applicationContext))

            }
        })
    }

    private fun configureStopButton() {
        val btn: Button = mLayout!!.findViewById<View>(R.id.stop) as Button
        btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                startService(BackgroundService.newService(applicationContext,"stop"))
            }
        })
    }
    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }


}
