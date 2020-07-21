package com.highserpot.tf.tflite

import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import android.util.Log
import com.highserpot.tf.env.ImageUtils
import com.highserpot.tf.tflite.Classifier.Recognition
import com.highserpot.tf.tracking.MultiBoxTracker
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*


class Run(val context: Context, val rotation: Int) {
    private var tracker: MultiBoxTracker? = null
    private var ori_tracker: MultiBoxTracker? = null
    protected var previewWidth = 0
    protected var previewHeight = 0
    private var sensorOrientation: Int? = null
    private var timestamp: Long = 0

    private val CONFIDENCE = 15
    private val MODEL_INPUT_SIZE = 300
    private val IS_MODEL_QUANTIZED = false
    private val MODEL_FILE = "stoneage_v_3.tflite"
    private val LABELS_FILE = "file:///android_asset/stoneage.txt"

    private var detector: Classifier? = null
    private var croppedBitmap: Bitmap? = null
    private var oriBitmap: Bitmap? = null
    private var frameToCropTransform: Matrix? = null
    private var ori_frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null
    var frameToCanvasMatrix: Matrix? = null

    fun build(fullPath: String) {

        var bitmap = loadImage(fullPath)
        previewWidth = bitmap!!.width
        previewHeight = bitmap!!.height

        // 90,270 이 가로
        sensorOrientation = 0
        oriBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)

        tracker = MultiBoxTracker(context)
        tracker!!.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation!!)

        ori_tracker = MultiBoxTracker(context)
        ori_tracker!!.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation!!)

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

        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888)


        ori_frameToCropTransform = ImageUtils.getTransformationMatrix(
            previewWidth,
            previewHeight,
            previewWidth,
            previewHeight,
            sensorOrientation!!,
            false
        )

        frameToCropTransform = ImageUtils.getTransformationMatrix(
            previewWidth,
            previewHeight,
            cropSize,
            cropSize,
            sensorOrientation!!,
            false
        )

        cropToFrameTransform = ImageUtils.getTransformationMatrix(
            previewWidth,
            previewHeight,
            cropSize,
            cropSize,
            sensorOrientation!!,
            false
        )

        frameToCanvasMatrix = ImageUtils.getTransformationMatrix(
            cropSize,
            cropSize,
            previewWidth,
            previewHeight,
            sensorOrientation!!,
            false
        )

    }


    // @Throws(java.lang.Exception::class)
    fun get_xy(fullPath: String): FloatArray? {
        val save_result = true
        val canvas = Canvas(croppedBitmap!!)
        val ori_canvas = Canvas(oriBitmap!!)


        ori_loadImage(fullPath)?.let {
            ori_canvas.drawBitmap(
                it,
                ori_frameToCropTransform!!,
                null
            )
        }

        loadImage(fullPath)?.let {
            canvas.drawBitmap(
                it,
                cropToFrameTransform!!,
                null
            )
        }

        var full_arr2 = fullPath.split(".JPEG")
        var chk_file_str2 = full_arr2[0] + "_step1.JPEG"
        croppedBitmap!!.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            FileOutputStream(File(chk_file_str2))
        );
        Log.e("모델결과", "파일저장: $chk_file_str2")

        val results: List<Classifier.Recognition?>? = detector!!.recognizeImage(croppedBitmap)

        var f_arr = FloatArray(2)


        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.0f
        ++timestamp
        val currTimestamp: Long = timestamp
        Log.d("run--", fullPath)
        if (results!!.isNotEmpty()) {
            // Log.d("예측결과- results ",results.toString())
            if (save_result) {

                var mappedRecognitions = LinkedList<Recognition>()
                var ori_mappedRecognitions = LinkedList<Recognition>()
                val MINIMUM_CONFIDENCE_TF_OD_API = 0.3f
                val minimumConfidence: Float =
                    MINIMUM_CONFIDENCE_TF_OD_API
                for (result2 in results) {
                    val result: Recognition = result2!!
                    val location = result!!.getLocation()
                    //Log.d("예측결과- all ",result.getConfidence_int().toString()+"% -"+result.getTitle()+"-"+result.getLocation())
                    val test = result.getConfidence()!! >= minimumConfidence
                    if (location != null ) {
                        Log.d("run-예측결과- location 111--", result.getTitle()+"-"+result.getConfidence()+"-" + location.toString())
                        val ori_result  = result
                        result.setLocation(location)
                        mappedRecognitions.add(result)
                        canvas.drawRect(location, paint)

                        // 이게 다시 원래로 좌표로 인식크기 변환하는거니깐 일단 주석
                        val bbox = RectF()
                        frameToCanvasMatrix!!.mapRect(bbox, location)
                        ori_result.setLocation(bbox)
                        ori_mappedRecognitions.add(ori_result)
                        ori_canvas.drawRect(bbox, paint)

                        Log.d("run-예측결과- location 222---", "" + location.toString())
                        Log.d("run-예측결과- bbox ", "" + bbox.toString())


                    }
                }

                // 캔버스에 이름 작성
                tracker!!.trackResults(mappedRecognitions, currTimestamp)
                tracker!!.draw(canvas)


                // canvas TO PNG
                var full_arr = fullPath.split(".JPEG")
                croppedBitmap!!.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    FileOutputStream(File(full_arr[0] + "__cropped.JPEG"))
                );
                Log.e("모델결과", "파일저장-확대에  그린것: " + full_arr[0] + "_cropped.JPEG")

                ori_tracker!!.trackResults(ori_mappedRecognitions, currTimestamp)
                ori_tracker!!.draw(ori_canvas)

                oriBitmap!!.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    FileOutputStream(File(full_arr[0] + "_ori.JPEG"))
                );
                Log.e("모델결과", "파일저장-원본에 그린것: " + full_arr[0] + "_ori.JPEG")

            }
            var max_item = results[0]
            val location = max_item!!.getLocation()
            var con = max_item?.getConfidence_int()!!
            if (con < CONFIDENCE) {
                return null
            } else {
                val bbox = RectF()
                frameToCanvasMatrix!!.mapRect(bbox, location)
                Log.d("모델결과- location ", "" + location.toString())
                Log.d("모델결과- bbox     ", "" + bbox.toString())
                var item = if(save_result) location else  bbox

                var x = item.left + (item.right - item.left) / 2
                var y = item.top + (item.bottom - item.top) / 2
                if (x != null && y != null) {
                    f_arr.set(0, x)
                    f_arr.set(1, y)

                    Log.d("run-모델결과-x,y ", x.toString())
                    Log.d("run-모델결과-x,y ", y.toString())
                    if (x < 0 || y < 0) {
                        return null
                    }
                    return f_arr
                }
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


    private fun loadImage(fileName: String): Bitmap? {

        var fis = FileInputStream(fileName)
        var bitmap = BitmapFactory.decodeStream(fis)
        fis.close()

//        val assetManager: AssetManager =
//            context
//                .getAssets()
//        val inputStream = assetManager.open(fileName)
        return bitmap
    }


    private fun ori_loadImage(fileName: String): Bitmap? {

        var fis = FileInputStream(fileName)
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