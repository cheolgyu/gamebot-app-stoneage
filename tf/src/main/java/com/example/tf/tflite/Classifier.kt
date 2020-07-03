package com.example.tf.tflite

import android.graphics.Bitmap
import android.graphics.RectF

interface Classifier {
    fun recognizeImage(bitmap: Bitmap?): List<Recognition?>?

    fun enableStatLogging(debug: Boolean)

    fun getStatString(): String?

    fun close()

    fun setNumThreads(num_threads: Int)

    fun setUseNNAPI(isChecked: Boolean)

    /** An immutable result returned by a Classifier describing what was recognized.  */
    class Recognition(
        private val id: String?,
        private val title: String,
        private val confidence: Float,
        private var location: RectF
    ) {

        fun getId(): String? {
            return id
        }

        fun getTitle(): String {
            return title
        }
        fun getConfidence(): Float? {
            return confidence
        }

        fun getLocation(): RectF {
            return RectF(location)
        }

        fun setLocation(location: RectF) {
            this.location = location
        }

        override fun toString(): String {
            var resultString = ""
            if (id != null) {
                resultString += "[$id] "
            }
            if (title != null) {
                resultString += "$title "
            }
            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f)
            }
            if (location != null) {
                resultString += location.toString() + " "
            }
            return resultString.trim { it <= ' ' }
        }



    }
}