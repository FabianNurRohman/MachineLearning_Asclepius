package com.dicoding.asclepius.helper

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.dicoding.asclepius.ml.CancerClassification
import org.tensorflow.lite.support.image.TensorImage
import java.io.IOException

class ImageClassifierHelper(private val context: Context) {

    private var model: CancerClassification? = null

    init {
        try {
            model = CancerClassification.newInstance(context)
        } catch (e: IOException) {
            Log.e("ImageClassifierHelper", "Error loading model", e)
        }
    }

    fun classifyStaticImage(imageUri: Uri, callback: (Pair<String, Float>) -> Unit) {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val tensorImage = TensorImage.fromBitmap(bitmap)

        try {
            val outputs = model?.process(tensorImage)
            val probability = outputs?.probabilityAsCategoryList
            val result = probability?.maxByOrNull { it.score }
            val resultLabel = result?.label ?: "Unknown"
            val resultScore = result?.score ?: 0.0f

            callback(Pair(resultLabel, resultScore))
        } catch (e: Exception) {
            Log.e("ImageClassifierHelper", "Error during inference", e)
            callback(Pair("Error", 0.0f))
        }
    }

    fun close() {
        model?.close()
    }
}
