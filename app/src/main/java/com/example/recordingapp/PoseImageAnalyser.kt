package com.example.recordingapp

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetector

class PoseImageAnalyser(
    private val poseDetector: PoseDetector,
    private val poseListener: PoseListener
) : ImageAnalysis.Analyzer {

    private var frameCount = SCORE_DELIVERY_FRAME_RATE

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            image ?: return

            poseDetector.process(image)
                .addOnSuccessListener {
                    // Return early if no landmarks detected
                    if (it.allPoseLandmarks.size < 1) {
                        imageProxy.close()
                        return@addOnSuccessListener
                    }

                    var score = 0.0
                    it.allPoseLandmarks.map { mark ->
                        score += mark.inFrameLikelihood
                    }

                    frameCount--
                    if (frameCount < 0) {
                        frameCount = SCORE_DELIVERY_FRAME_RATE
                        poseListener.fullBodyInFrame(score > (it.allPoseLandmarks.size - SCORE_DELTA))
                    }

                    // SKELETON
                    //poseListener.onPoseAnalysed(it)

                    imageProxy.close()
                }
                .addOnFailureListener {
                    Log.d("debuglog", "ANALYSIS FAILURE")
                    imageProxy.close()
                }

        }
    }

    companion object {
        private const val SCORE_DELTA = 0.3 //Permissiveness constant
        private const val SCORE_DELIVERY_FRAME_RATE = 10 //Trigger score every x video frame
    }

    interface PoseListener {
        fun onPoseAnalysed(pose: Pose)
        fun fullBodyInFrame(inFrame: Boolean)
    }

}