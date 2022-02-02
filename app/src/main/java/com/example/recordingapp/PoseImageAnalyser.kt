package com.example.recordingapp

import android.util.Log
import android.view.View
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class PoseImageAnalyser(
    private val poseDetector: PoseDetector,
    private val poseListener: PoseListener
) : ImageAnalysis.Analyzer {

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        Log.d("debuglog", "analysing")

        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Pass image to an ML Kit Vision API

            image ?: return

            poseDetector.process(image)
                .addOnSuccessListener {
                    poseListener.onPoseAnalysed(it)
                    imageProxy.close()
                }
                .addOnFailureListener {
                    Log.d("debuglog", "ANALYSIS FAILURE")
                    imageProxy.close()
                }

        }

    }

    interface PoseListener {
        fun onPoseAnalysed(pose: Pose)
    }

}