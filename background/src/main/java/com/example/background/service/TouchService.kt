package com.example.background.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.Context
import android.graphics.Path
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import com.example.background.R

var touchService: TouchService? = null

class TouchService : AccessibilityService() {
    var mLayout: FrameLayout? = null
    override fun onServiceConnected() {
        Log.d("TouchService-0000000000","4444444444444")
        Log.d("TouchService-onServiceConnected","start")
        touchService =this

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
        lp.gravity = Gravity.LEFT
        val inflater = LayoutInflater.from(this)

        inflater.inflate(R.layout.action_bar, mLayout)
        wm.addView(mLayout, lp)

        super.onServiceConnected()
    }

    fun click(x: Float, y: Float) {
        val clickPath = Path()
        clickPath.moveTo(x, y)
        val gestureBuilder = GestureDescription.Builder()
        Log.d("클릭","x=$x,y=$y")
        gestureBuilder.addStroke(StrokeDescription(clickPath, 0, 1000))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    override fun onInterrupt() {
        Log.d("TouchService-3333333333","4444444444444")
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        Log.d("TouchService-111111111","2222222222222222222222")
    }




}
