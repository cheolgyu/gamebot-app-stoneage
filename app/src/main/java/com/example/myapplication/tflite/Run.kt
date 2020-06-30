package com.example.myapplication.tflite

import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import android.util.Log
import android.util.Size
import com.example.myapplication.env.ImageUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*


class Run(_context: Context) {
    private val MODEL_INPUT_SIZE = 300
    private val IS_MODEL_QUANTIZED = false
    private val MODEL_FILE = "test/detect_copy2.tflite"
    private val LABELS_FILE = "file:///android_asset/test/labelmap.txt"
    private val IMAGE_SIZE = Size(640, 480)

    private var detector: Classifier? = null
    private var croppedBitmap: Bitmap? = null
    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null
    var context: Context = _context

    fun build() {
        val assetManager: AssetManager =
            context.getAssets()
        detector = TFLiteObjectDetectionAPIModel.create(
            assetManager,
            MODEL_FILE,
            LABELS_FILE,
            MODEL_INPUT_SIZE,
            IS_MODEL_QUANTIZED
        )
        val cropSize: Int = MODEL_INPUT_SIZE
        val previewWidth: Int = IMAGE_SIZE.getWidth()
        val previewHeight: Int = IMAGE_SIZE.getHeight()
        val sensorOrientation = 0
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888)
        var utils = ImageUtils()
        frameToCropTransform = utils.getTransformationMatrix(
            previewWidth,
            previewHeight,
            cropSize,
            cropSize,
            sensorOrientation,
            false
        )
        cropToFrameTransform = Matrix()
        frameToCropTransform?.invert(cropToFrameTransform)

    }



   // @Throws(java.lang.Exception::class)
    fun get_xy(cap_filename: String): FloatArray? {
        val canvas = Canvas(croppedBitmap!!)

        loadImage(cap_filename)?.let {
            canvas.drawBitmap(
                it,
                frameToCropTransform!!,
                null
            )
        }
        val results: List<Classifier.Recognition?>? = detector!!.recognizeImage(croppedBitmap)
//        for (item in results!!) {
//            Log.d("모델결과",item.toString())
//        }
        var f_arr = FloatArray(2)
        if (results!!.isNotEmpty()){
            var max_item = results[0]
            var x = max_item?.getLocation()?.centerX()
            var y = max_item?.getLocation()?.centerY()
            if (x != null && y != null) {
                f_arr.set(0,x)
                f_arr.set(1,y)
                Log.d("모델결과-max_item ",max_item.toString())
                Log.d("모델결과-x,y ",x.toString())
                Log.d("모델결과-x,y ",y.toString())
                return f_arr
            }
        }
        return null
    }

    // Confidence tolerance: absolute 1%
    private fun matchConfidence(a: Float, b: Float): Boolean {
        return Math.abs(a - b) < 0.01
    }

    private fun matchBoundingBoxes(a: RectF, b: RectF): Boolean {
        val areaA = a.width() * a.height()
        val areaB = b.width() * b.height()
        val overlapped = RectF(
            Math.max(a.left, b.left),
            Math.max(a.top, b.top),
            Math.min(a.right, b.right),
            Math.min(a.bottom, b.bottom)
        )
        val overlappedArea = overlapped.width() * overlapped.height()
        return overlappedArea > 0.95 * areaA && overlappedArea > 0.95 * areaB
    }



    //@Throws(Exception::class)
    private fun loadImage(fileName: String): Bitmap? {

        var fis   =  FileInputStream(fileName)
        var bitmap = BitmapFactory.decodeStream(fis)
        fis.close()
//        val assetManager: AssetManager =
//            context
//                .getAssets()
//        val inputStream = assetManager.open(fileName)
        return bitmap
    }

    // The format of result:
    // category bbox.left bbox.top bbox.right bbox.bottom confidence
    // ...
    // Example:
    // Apple 99 25 30 75 80 0.99
    // Banana 25 90 75 200 0.98
    // ...
    @Throws(Exception::class)
    private fun loadRecognitions(fileName: String): List<Classifier.Recognition> {
        val assetManager: AssetManager =
            context
                .getAssets()
        val inputStream = assetManager.open(fileName)
        val scanner = Scanner(inputStream)
        val result: MutableList<Classifier.Recognition> = ArrayList<Classifier.Recognition>()
        while (scanner.hasNext()) {
            var category = scanner.next()
            category = category.replace('_', ' ')
            if (!scanner.hasNextFloat()) {
                break
            }
            val left = scanner.nextFloat()
            val top = scanner.nextFloat()
            val right = scanner.nextFloat()
            val bottom = scanner.nextFloat()
            val boundingBox = RectF(left, top, right, bottom)
            val confidence = scanner.nextFloat()
            val recognition = Classifier.Recognition(null, category, confidence, boundingBox)
            result.add(recognition)
        }
        return result
    }
}