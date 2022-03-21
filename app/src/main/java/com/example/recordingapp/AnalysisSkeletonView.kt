package com.example.recordingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.roundToInt

class AnalysisSkeletonView@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dotPaint: Paint = Paint()
    private val linePaint: Paint = Paint()

    private val screenWidth: Int

    private val TAG = "debuglog"

    var pose: Pose? = null
/*
    var pose: Pose? = null
    set(value) {
        field = value
        invalidate()
    }

 */
    // HashMap<MlKit id, Coordinates>
    private var landmarks: HashMap<Int, Point> = hashMapOf()
        set(value) {
            field = value
            invalidate()
        }

    fun setLandmarks(pose: Pose) {
        this.pose = pose // debug

        val hashMap = hashMapOf<Int, Point>()

        landmarksSelection.map { id ->
            pose.getPoseLandmark(id)?.let { l ->
                if (l.inFrameLikelihood > 0.9f)
                    hashMap[id] = Point(l.position.x.roundToInt(), l.position.y.roundToInt())
            }
        }

        landmarks = hashMap
    }

    init {
        val wm = this.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        screenWidth = display.width

        val blue = ResourcesCompat.getColor(this.context.resources, R.color.blue, null)

        dotPaint.color = blue
        dotPaint.strokeWidth = 100f // convert to px
        dotPaint.style = Paint.Style.FILL

        linePaint.color = blue
        linePaint.strokeWidth = 20f // convert to px
        linePaint.style = Paint.Style.FILL
    }

    fun updateLandmark(landmarks: List<PoseLandmark>) {
        //for (landmark in landmarks) {
        //    path?.drawCircle(translateX(landmark.position.x),landmark.position.y,8.0f, paint)
        //}
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //Log.d("debuglog", "onDraw: ${coordinates.toString()}")

        /*
        pose?.let { p ->

            val selectedLandmarks = landmarksSelection.map {
                p.getPoseLandmark(it)?.let { l ->
                    if (l.inFrameLikelihood > 0.9f) l // todo
                    else null
                }
            }


            for (landmark in landmarks) {
                canvas?.drawCircle(translateX(landmark.x ?: 0f), landmark?.position?.y ?: 0f,50f, dotPaint) //todo convert to px
                //canvas?.drawCircle(200f,-200f,8.0f, paint)
            }
        }
             */

        landmarks.forEach { l ->
            canvas?.drawCircle(translateX(l.value.x.toFloat()), l.value.y.toFloat(),50f, dotPaint) //todo convert to px
        }

        val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]
        val rightShoulder = landmarks[PoseLandmark.RIGHT_SHOULDER]

        if (leftShoulder != null && rightShoulder != null)
            canvas?.drawLine(translateX(leftShoulder?.x?.toFloat() ?: 0f), leftShoulder?.y?.toFloat() ?: 0f, translateX(rightShoulder?.x?.toFloat() ?: 0f), rightShoulder?.x?.toFloat() ?: 0f, linePaint)

        Log.d(TAG, "onDraw: ${leftShoulder?.x} - ${leftShoulder?.y}")
        Log.d(TAG, "onDraw: ${rightShoulder?.x} - ${rightShoulder?.y}")
        Log.d(TAG, "pose: ${pose?.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.position?.x} - ${leftShoulder?.y}")
        Log.d(TAG, "pose: ${rightShoulder?.x} - ${rightShoulder?.y}")

        /*
        //todo : a custom ui model ?

        val leftShoulder = pose?.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose?.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose?.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose?.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose?.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose?.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose?.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose?.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose?.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose?.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = pose?.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose?.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        val leftHeel = pose?.getPoseLandmark(PoseLandmark.LEFT_HEEL)
        val rightHeel = pose?.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
        val leftFootIndex = pose?.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)

        // TORSO
        if (leftShoulder != null && rightShoulder != null)
            canvas?.drawLine(translateX(leftShoulder.position.x), leftShoulder.position.y, translateX(rightShoulder.position.x), rightShoulder.position.y, linePaint)

        // ARMS
        if (leftShoulder != null && leftElbow != null)
            canvas?.drawLine(translateX(leftShoulder.position.x), leftShoulder.position.y, translateX(leftElbow.position.x), leftElbow.position.y, linePaint)

        // LEGS

         */

/*
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)  ?:return
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) ?:return
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW) ?:return
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW) ?:return
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?:return
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST) ?:return
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP) ?:return
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) ?:return
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE) ?:return
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE) ?:return
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE) ?:return
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE) ?:return

        val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY) ?:return
        val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY) ?:return
        val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX) ?:return
        val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX) ?:return
        val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB) ?:return
        val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB) ?:return
        val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL) ?:return
        val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL) ?:return
        val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX) ?:return
        val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX) ?:return


        canvas?.drawLine(translateX(leftShoulder.position.x),leftShoulder.position.y,translateX(rightShoulder.position.x),rightShoulder.position.y,boundaryPaint)
        canvas?.drawLine(translateX(leftHip.position.x),leftHip.position.y,translateX(rightHip.position.x),rightHip.position.y,boundaryPaint)

        //Left body
        canvas?.drawLine(translateX(leftShoulder.position.x),leftShoulder.position.y,translateX(leftElbow.position.x),leftElbow.position.y,leftPaint)
        canvas?.drawLine(translateX(leftElbow.position.x),leftElbow.position.y,translateX(leftWrist.position.x),leftWrist.position.y,leftPaint)
        canvas?.drawLine(translateX(leftShoulder.position.x),leftShoulder.position.y,translateX(leftHip.position.x),leftHip.position.y,leftPaint)
        canvas?.drawLine(translateX(leftHip.position.x),leftHip.position.y,translateX(leftKnee.position.x),leftKnee.position.y,leftPaint)
        canvas?.drawLine(translateX(leftKnee.position.x),leftKnee.position.y,translateX(leftAnkle.position.x),leftAnkle.position.y,leftPaint)
        canvas?.drawLine(translateX(leftWrist.position.x),leftWrist.position.y,translateX(leftThumb.position.x),leftThumb.position.y,leftPaint)
        canvas?.drawLine(translateX(leftWrist.position.x),leftWrist.position.y,translateX(leftPinky.position.x),leftPinky.position.y,leftPaint)
        canvas?.drawLine(translateX(leftWrist.position.x),leftWrist.position.y,translateX(leftIndex.position.x),leftIndex.position.y,leftPaint)
        canvas?.drawLine(translateX(leftIndex.position.x),leftIndex.position.y,translateX(leftPinky.position.x),leftPinky.position.y,leftPaint)
        canvas?.drawLine(translateX(leftAnkle.position.x),leftAnkle.position.y,translateX(leftHeel.position.x),leftHeel.position.y,leftPaint)
        canvas?.drawLine(translateX(leftHeel.position.x),leftHeel.position.y,translateX(leftFootIndex.position.x),leftFootIndex.position.y,leftPaint)

        ////Right body
        canvas?.drawLine(translateX(rightShoulder.position.x),rightShoulder.position.y,translateX(rightElbow.position.x),rightElbow.position.y,rightPaint)
        canvas?.drawLine(translateX(rightElbow.position.x),rightElbow.position.y,translateX(rightWrist.position.x),rightWrist.position.y,rightPaint)
        canvas?.drawLine(translateX(rightShoulder.position.x),rightShoulder.position.y,translateX(rightHip.position.x),rightHip.position.y,rightPaint)
        canvas?.drawLine(translateX(rightHip.position.x),rightHip.position.y,translateX(rightKnee.position.x),rightKnee.position.y,rightPaint)
        canvas?.drawLine(translateX(rightKnee.position.x),rightKnee.position.y,translateX(rightAnkle.position.x),rightAnkle.position.y,rightPaint)
        canvas?.drawLine(translateX(rightWrist.position.x),rightWrist.position.y,translateX(rightThumb.position.x),rightThumb.position.y,rightPaint)
        canvas?.drawLine(translateX(rightWrist.position.x),rightWrist.position.y,translateX(rightPinky.position.x),rightPinky.position.y,rightPaint)
        canvas?.drawLine(translateX(rightWrist.position.x),rightWrist.position.y,translateX(rightIndex.position.x),rightIndex.position.y,rightPaint)
        canvas?.drawLine(translateX(rightIndex.position.x),rightIndex.position.y,translateX(rightPinky.position.x),rightPinky.position.y,rightPaint)
        canvas?.drawLine(translateX(rightAnkle.position.x),rightAnkle.position.y,translateX(rightHeel.position.x),rightHeel.position.y,rightPaint)
        canvas?.drawLine(translateX(rightHeel.position.x),rightHeel.position.y,translateX(rightFootIndex.position.x),rightFootIndex.position.y,rightPaint)


         */
    }

    private fun translateX(x: Float): Float {
        // you will need this for the inverted image in case of using front camera
        return screenWidth.minus(x)
    }

    private val landmarksSelection = listOf(
        PoseLandmark.NOSE,
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
        PoseLandmark.RIGHT_FOOT_INDEX
    )
}
// hashmap [id:Point]
class AnalysisSkeletonUiModel(
    landmarkSelection: List<Int>,

)