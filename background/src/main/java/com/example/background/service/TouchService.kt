package com.example.background.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent

var touchService: TouchService? = null

class TouchService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("onServiceConnected","start")
        touchService =this
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
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }




}
