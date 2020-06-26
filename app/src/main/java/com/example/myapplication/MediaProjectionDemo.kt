package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import java.util.*

class MediaProjectionDemo : Activity() {
    private var mScreenDensity = 0
    private var mProjectionManager: MediaProjectionManager? = null
    private var mDisplayWidth = 0
    private var mDisplayHeight = 0
    private var mScreenSharing = false
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mSurface: Surface? = null
    private var mSurfaceView: SurfaceView? = null
    private var mToggle: ToggleButton? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_projection)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi
        mSurfaceView = findViewById<View>(R.id.surface) as SurfaceView
        mSurface = mSurfaceView!!.holder.surface
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val arrayAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, RESOLUTIONS
        )
        val s = findViewById<View>(R.id.spinner) as Spinner
        s.adapter = arrayAdapter
        s.onItemSelectedListener = ResolutionSelector()
        s.setSelection(0)
        mToggle = findViewById<View>(R.id.screen_sharing_toggle) as ToggleButton
        mToggle!!.isSaveEnabled = false
    }

    override fun onStop() {
        stopScreenSharing()
        super.onStop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent
    ) {
        if (requestCode != PERMISSION_CODE) {
            Log.e(
                TAG,
                "Unknown request code: $requestCode"
            )
            return
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(
                this,
                "User denie d screen sharing permission", Toast.LENGTH_SHORT
            ).show()
            return
        }
        mMediaProjection = mProjectionManager!!.getMediaProjection(resultCode, data)

        mMediaProjection!!.registerCallback(MediaProjectionCallback(), null)
        mVirtualDisplay = createVirtualDisplay()
    }

    fun onToggleScreenShare(view: View) {
        if ((view as ToggleButton).isChecked) {
            shareScreen()
        } else {
            stopScreenSharing()
        }
    }

    private fun shareScreen() {
        mScreenSharing = true
        if (mSurface == null) {
            return
        }
        if (mMediaProjection == null) {
            startActivityForResult(
                mProjectionManager!!.createScreenCaptureIntent(),
                PERMISSION_CODE
            )
            return
        }
        mVirtualDisplay = createVirtualDisplay()
    }

    private fun stopScreenSharing() {
        if (mToggle!!.isChecked) {
            mToggle!!.isChecked = false
        }
        mScreenSharing = false
        if (mVirtualDisplay != null) {
            mVirtualDisplay!!.release()
            mVirtualDisplay = null
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay(
            "ScreenSharingDemo",
            mDisplayWidth, mDisplayHeight, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mSurface, null /*Callbacks*/, null /*Handler*/
        )
    }



    private fun resizeVirtualDisplay() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.resize(mDisplayWidth, mDisplayHeight, mScreenDensity)
    }

    private inner class ResolutionSelector : OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>,
            v: View,
            pos: Int,
            id: Long
        ) {
            val r = parent.getItemAtPosition(pos) as Resolution
            val lp = mSurfaceView!!.layoutParams
            if (resources.configuration.orientation
                == Configuration.ORIENTATION_LANDSCAPE
            ) {
                mDisplayHeight = r.y
                mDisplayWidth = r.x
            } else {
                mDisplayHeight = r.x
                mDisplayWidth = r.y
            }
            lp.height = mDisplayHeight
            lp.width = mDisplayWidth
            mSurfaceView!!.layoutParams = lp
        }

        override fun onNothingSelected(parent: AdapterView<*>?) { /* Ignore */
        }
    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            mMediaProjection = null
            stopScreenSharing()
        }
    }

    private inner class SurfaceCallbacks : SurfaceHolder.Callback {
        override fun surfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            mDisplayWidth = width
            mDisplayHeight = height
            resizeVirtualDisplay()
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            mSurface = holder.surface
            if (mScreenSharing) {
                shareScreen()
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            if (!mScreenSharing) {
                stopScreenSharing()
            }
        }
    }

    private class Resolution(var x: Int, var y: Int) {
        override fun toString(): String {
            return x.toString() + "x" + y
        }

    }

    companion object {
        private const val TAG = "MediaProjectionDemo"
        private const val PERMISSION_CODE = 1
        private val RESOLUTIONS: ArrayList<Resolution?> =
            object : ArrayList<Resolution?>() {
                init {
                    add(Resolution(640, 360))
                    add(Resolution(960, 540))
                    add(Resolution(1366, 768))
                    add(Resolution(1600, 900))
                }
            }
    }
}