package com.example.recordingapp

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark

class PoseImageAnalyser(
    private val poseDetector: PoseDetector,
    private val poseListener: PoseListener
) : ImageAnalysis.Analyzer {

    private var frameCount = SCORE_DELIVERY_FRAME_RATE
    // We store n scores then analyse body placement given average value
    private val scores = arrayListOf<Double>()

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            image ?: return

            poseDetector.process(image)
                .addOnSuccessListener { pose ->

                    val selectedLandmarks = landmarksIds.map { id ->
                        pose.getPoseLandmark(id)
                    }

                    // Return early if no landmarks detected
                    if (selectedLandmarks.isEmpty()) {
                        imageProxy.close()
                        return@addOnSuccessListener
                    }

                    var score = 0.0
                    selectedLandmarks.map { mark ->
                        score += (mark?.inFrameLikelihood ?: 0f)
                    }

                    scores.add(score)

                    frameCount--
                    if (frameCount < 0) {
                        frameCount = SCORE_DELIVERY_FRAME_RATE
                        Log.d("debuglog", scores.toString())
                        poseListener.fullBodyInFrame(scores.average() > (selectedLandmarks.size - SCORE_DELTA))
                        scores.clear()
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

    private val landmarksIds = listOf(
        PoseLandmark.LEFT_EYE,
        PoseLandmark.RIGHT_EYE,
        PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.LEFT_WRIST,
        PoseLandmark.RIGHT_WRIST,
        PoseLandmark.LEFT_HIP,
        PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_KNEE,
        PoseLandmark.RIGHT_KNEE,
        PoseLandmark.LEFT_ANKLE,
        PoseLandmark.RIGHT_ANKLE,
        PoseLandmark.LEFT_FOOT_INDEX,
        PoseLandmark.RIGHT_FOOT_INDEX,
        PoseLandmark.LEFT_MOUTH,
        PoseLandmark.RIGHT_MOUTH,
        PoseLandmark.NOSE
    )

    /*
    val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
    val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
    val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
    val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
    val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
    val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
    val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
    val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
    val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
    val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
    val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
    val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
    val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)
    val leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE)
    val rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)
        val leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH)
    val rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH)
val nose = pose.getPoseLandmark(PoseLandmark.NOSE)

    val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
    val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
    val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
    val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
    val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
    val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
    val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
    val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
    val leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER)
    val leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER)
    val rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER)
    val rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER)
    val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
    val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
     */


    companion object {
        private const val SCORE_DELTA = 0.18 //Permissiveness constant (arbitrary)
        private const val SCORE_DELIVERY_FRAME_RATE = 10 //Trigger score every x video frame
    }

    interface PoseListener {
        fun onPoseAnalysed(pose: Pose)
        fun fullBodyInFrame(inFrame: Boolean)
    }

}