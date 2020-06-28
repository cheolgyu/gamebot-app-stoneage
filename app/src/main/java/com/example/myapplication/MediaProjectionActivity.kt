package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.MotionEvent
import android.view.OrientationEventListener
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class MediaProjectionActivity : AppCompatActivity() {
    companion object {
        private const val REQ_CODE_MEDIA_PROJECTION = 1000

        fun newInstance(context: Context): Intent =
            Intent(context, MediaProjectionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

    private var mOrientationChangeCallback: OrientationChangeCallback? = null

    private var IMAGES_PRODUCED = 0
    var STORE_DIRECTORY: String? = null
    val TAG: String = com.example.myapplication.MediaProjectionActivity::class.java.getName()
    var mediaProjectionManager: MediaProjectionManager? = null
    var mediaProjection: MediaProjection? = null
    var mDisplay: Display? = null
    var mVirtualDisplay: VirtualDisplay? = null
    var mDensity = 0
    var mWidth = 100
    var mHeight = 100
    var mRotation = 0
    var mImageReader: ImageReader? = null
    val SCREENCAP_NAME = "screencap"
    val VIRTUAL_DISPLAY_FLAGS =
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
    val mHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MediaProjectionActivity=====================================")


        // 캡처 주석
        mediaProjectionManager =
           getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager!!.createScreenCaptureIntent(), 1234)



    }




    internal class MediaProjectionResultContract : ActivityResultContract<Intent, Intent>() {

        override fun createIntent(context: Context, input: Intent?): Intent =
            input!!

        override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
            return if (resultCode == Activity.RESULT_OK && intent != null) {
                // MediaProjectionAccessBroadcastReceiver.newInstance(resultCode, intent)
                intent
            } else {
                // MediaProjectionAccessBroadcastReceiver.newReject()
                intent
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "11111111111111111onActivityResult11111111111111")
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "1111111111111111111111111111111")
            mediaProjection =
                data?.let { mediaProjectionManager?.getMediaProjection(resultCode, it) };
            //this.finish();
            file()
            createVirtualDisplay()

            mOrientationChangeCallback = OrientationChangeCallback(this);
            if (mOrientationChangeCallback!!.canDetectOrientation()) {
                mOrientationChangeCallback!!.enable();
            }

            // register media projection stop callback
            mediaProjection!!.registerCallback(MediaProjectionStopCallback(), mHandler)

        } else {
            Log.e(TAG, "122=====================================")
        }

    }

    fun file() {
        val externalFilesDir: File? = getExternalFilesDir(null)
        if (externalFilesDir != null) {
            STORE_DIRECTORY = externalFilesDir.getAbsolutePath().toString() + "/screenshots/"
            val storeDirectory = File(STORE_DIRECTORY)
            if (!storeDirectory.exists()) {
                val success: Boolean = storeDirectory.mkdirs()
                if (!success) {
                    Log.e(TAG, "failed to create file storage directory.")
                    return
                }
            }
        } else {
            Log.e(
                TAG,
                "failed to create file storage directory, getExternalFilesDir is null."
            )
            return
        }

        val metrics: DisplayMetrics = resources.displayMetrics
        mDensity = metrics.densityDpi
        mDisplay = windowManager.defaultDisplay
    }

    @SuppressLint("WrongConstant")
    private fun createVirtualDisplay() {
        // get width and height
        val size = Point()
        mDisplay?.getSize(size)
        mWidth = size.x
        mHeight = size.y

        // start capture reader
        mImageReader = ImageReader.newInstance(
            mWidth,
            mHeight,
            PixelFormat.RGBA_8888,
            2
        )
        mVirtualDisplay = mediaProjection?.createVirtualDisplay(
            SCREENCAP_NAME,
            mWidth,
            mHeight,
            mDensity,
            VIRTUAL_DISPLAY_FLAGS,
            mImageReader!!.getSurface(),
            null,
            mHandler
        )
        mImageReader!!.setOnImageAvailableListener(ImageAvailableListener(), mHandler)
    }

    inner class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            Log.e("ScreenCapture", "stopping projection.")
            if (mHandler != null) {
                mHandler.post(Runnable {
                    mVirtualDisplay?.release()
                    mImageReader?.setOnImageAvailableListener(null, null)
                    mOrientationChangeCallback?.disable()
                    mediaProjection?.unregisterCallback(this@MediaProjectionStopCallback)
                })
            }
        }
    }

    inner class ImageAvailableListener : OnImageAvailableListener {


        override fun onImageAvailable(reader: ImageReader) {
            var image: Image? = null
            var fos: FileOutputStream? = null
            var bitmap: Bitmap? = null
            try {
                image = reader.acquireLatestImage()
                if (image != null) {
                    val width = image.width
                    val height = image.height
                    val planes: Array<Image.Plane> = image.getPlanes()
                    val buffer: ByteBuffer = planes[0].getBuffer()
                    val pixelStride: Int = planes[0].getPixelStride()
                    val rowStride: Int = planes[0].getRowStride()
                    val rowPadding: Int = rowStride - pixelStride * mWidth

                    // create bitmap
                    bitmap = Bitmap.createBitmap(
                        mWidth + rowPadding / pixelStride,
                        mHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)
                    val tsLong = System.currentTimeMillis() / 1000
                    val ts = tsLong.toString()
                    // write bitmap to a file
                    fos =
                        FileOutputStream(STORE_DIRECTORY.toString() +ts+ ".JPEG")
                    bitmap.compress(CompressFormat.JPEG, 100, fos)
                    IMAGES_PRODUCED++
                    Log.e(TAG, "captured image: $STORE_DIRECTORY$IMAGES_PRODUCED")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (ioe: IOException) {
                        ioe.printStackTrace()
                    }
                }
                bitmap?.recycle()
                if (image != null) {
                    image.close()
                }
            }
        }
    }

    inner class OrientationChangeCallback internal constructor(context: Context?) :
        OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            val rotation: Int = mDisplay!!.getRotation()
            if (rotation != mRotation) {
                mRotation = rotation
                try {
                    // clean up
                    mVirtualDisplay?.release()
                    mImageReader?.setOnImageAvailableListener(null, null)

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


}