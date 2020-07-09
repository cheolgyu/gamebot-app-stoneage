package com.example.tf.tflite

import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import android.util.Log
import android.util.Size
import com.example.tf.env.ImageUtils
import com.example.tf.tflite.Classifier.Recognition
import com.example.tf.tracking.MultiBoxTracker
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*


class Run(_context: Context) {
    private var tracker: MultiBoxTracker? = null
    protected var previewWidth = 0
    protected var previewHeight = 0
    private var sensorOrientation: Int? = null
    private var timestamp: Long = 0

    private val CONFIDENCE = 50
    private val MODEL_INPUT_SIZE = 300
    private val IS_MODEL_QUANTIZED = false
    private val MODEL_FILE = "stoneage_v_1.tflite"
    private val LABELS_FILE = "file:///android_asset/stoneage.txt"
    private val IMAGE_SIZE = Size(1080, 1794)

    private var detector: Classifier? = null
    private var croppedBitmap: Bitmap? = null
    private var oriBitmap: Bitmap? = null
    private var frameToCropTransform: Matrix? = null
    private var ori_frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null
    var context: Context = _context

    fun build() {
        previewWidth = 1794
        previewHeight = 1080

        //이값 수정해야대. 복붙한값 디바이스에따라 달라짐.
        sensorOrientation = 90
        oriBitmap= Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)

        tracker = MultiBoxTracker(context)
        tracker!!.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation!!)

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
        //val previewWidth: Int = IMAGE_SIZE.getWidth()
        //val previewHeight: Int = IMAGE_SIZE.getHeight()
        val sensorOrientation = 0
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888)
        var utils = ImageUtils()
        frameToCropTransform = ImageUtils.Companion.getTransformationMatrix(
            previewWidth,
            previewHeight,
            cropSize,
            cropSize,
            sensorOrientation,
            true
        )

        ori_frameToCropTransform = ImageUtils.Companion.getTransformationMatrix(
            previewWidth,
            previewHeight,
            previewWidth,
            previewHeight,
            sensorOrientation,
            false
        )

        cropToFrameTransform = Matrix()
        frameToCropTransform?.invert(cropToFrameTransform)

    }



   // @Throws(java.lang.Exception::class)
    fun get_xy( fullPath: String): FloatArray? {
       val save_result = true
        val canvas = Canvas(croppedBitmap!!)
       val ori_canvas = Canvas(oriBitmap!!)


       ori_loadImage(fullPath)?.let {
           ori_canvas.drawBitmap(
               it,
               Matrix(),
               null
           )
       }

        loadImage(fullPath)?.let {
            canvas.drawBitmap(
                it,
                Matrix(),
                null
            )
        }

       var full_arr2 = fullPath.split(".JPEG")
       var chk_file_str2 = full_arr2[0]+"_frameToCropTransform.JPEG"
       croppedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100,  FileOutputStream( File(chk_file_str2)));
       Log.e("모델결과","파일저장: $chk_file_str2")

        val results: List<Classifier.Recognition?>? = detector!!.recognizeImage(croppedBitmap)
//        for (item in results!!) {
//            Log.d("모델결과",item.toString())
//        }
        var f_arr = FloatArray(2)


       val paint = Paint()
       paint.color = Color.RED
       paint.style = Paint.Style.STROKE
       paint.strokeWidth = 2.0f
       ++timestamp
       val currTimestamp: Long = timestamp

        if (results!!.isNotEmpty()){
            Log.d("예측결과- results ",results.toString())

            if(save_result){
                var mappedRecognitions = LinkedList<Recognition>()
                val MINIMUM_CONFIDENCE_TF_OD_API = 0.1f
                val minimumConfidence: Float =
                    MINIMUM_CONFIDENCE_TF_OD_API
                for (result2 in results) {
                    val result : Recognition = result2!!
                    val location = result!!.getLocation()
                    Log.d("예측결과- all ",result.getConfidence_int().toString()+"% -"+result.getTitle()+"-"+result.getLocation())
                    val test = result.getConfidence()!! >= minimumConfidence
                    if (location != null && result.getTitle()=="skip" ) {

                       // cropToFrameTransform!!.mapRect(location)
                        result.setLocation(location)
                        mappedRecognitions.add(result)
                        canvas.drawRect(location, paint)

                    }else{
                        Log.d("예측결과- < minimumConfidence",""+result.getTitle()+"-"+result.getLocation())
                    }
                }

                // 캔버스에 이름 작성
                tracker!!.trackResults(mappedRecognitions, currTimestamp)
                tracker!!.draw(canvas)
                // canvas TO PNG
                var full_arr = fullPath.split(".JPEG")
                croppedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100,  FileOutputStream( File( full_arr[0]+"_chk_cropped.JPEG")));
                Log.e("모델결과","파일저장: "+full_arr[0]+"_chk_cropped.JPEG")

                oriBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100,  FileOutputStream( File(full_arr[0]+"_chk.JPEG")));
                Log.e("모델결과","파일저장: "+full_arr[0]+"_chk.JPEG")

            }
            var max_item = results[0]
            var con = max_item?.getConfidence_int()!!
            Log.d("모델결과-con: ","$con % ")
            if(con < CONFIDENCE){
                Log.d("모델결과-Confidence 너무낮아",max_item.toString())
                Log.d("모델결과-Confidence 너무낮아","$con")
                return  null
            }else{
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
        val options = BitmapFactory.Options()
        options.inSampleSize = 6
        val bitmap = BitmapFactory.decodeFile(fileName, options)
//        var fis   =  FileInputStream(fileName)
//        var bitmap = BitmapFactory.decodeStream(fis)
//        fis.close()

//        val assetManager: AssetManager =
//            context
//                .getAssets()
//        val inputStream = assetManager.open(fileName)
        return bitmap
    }
    private fun ori_loadImage(fileName: String): Bitmap? {

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