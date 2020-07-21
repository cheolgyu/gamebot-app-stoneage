package com.highserpot.background.service

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class BackgroundServiceMPListener(val mWidth: Int, val mHeight: Int, val STORE_DIRECTORY: String) :
    ImageReader.OnImageAvailableListener {

    private var IMAGES_PRODUCED = 0


    override fun onImageAvailable(reader: ImageReader) {
        Log.d(TAG, "-----------------------onImageAvailable----------------------------------")
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

                // write bitmap to a file

                // write bitmap to a file
                var my_file = STORE_DIRECTORY + "myscreen_" + IMAGES_PRODUCED + ".JPEG"
                fos =
                    FileOutputStream(my_file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                //fos.close()
                //image.close()
                //reader.close()
                IMAGES_PRODUCED++
                Log.e(
                    TAG,
                    "captured image: " + my_file
                )

                Log.d(
                    TAG,
                    "-----------------------onImageAvailable----------------------------------" + my_file
                )
                //startForegroundService(BackgroundService.newService(applicationContext,"cap",my_file))
                // tflite_run(my_file)
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


