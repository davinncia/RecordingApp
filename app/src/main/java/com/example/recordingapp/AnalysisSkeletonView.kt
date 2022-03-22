package com.example.recordingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.Size
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
    private val rectPaint: Paint = Paint()

    private val isLandscape: Boolean

    //private val screenWidth: Int
    //private val screenHeight: Int

    private val TAG = "debuglog"

    // [MlKit id : Coordinates]
    private var landmarks: HashMap<Int, Point> = hashMapOf()
        set(value) {
            field = value
            invalidate()
        }

    // Debug
    var frameRect: Rect? = null

    fun setLandmarks(pose: Pose, frameSize: Size) {
        val hashMap = hashMapOf<Int, Point>()

        landmarksSelection.map { id ->
            pose.getPoseLandmark(id)?.let { l ->
                if (l.inFrameLikelihood > MIN_FRAME_SCORE) {
                    //hashMap[id] = Point(l.position.x.roundToInt(), l.position.y.roundToInt())
                    val coordinate = Point(l.position.x.roundToInt(), l.position.y.roundToInt())
                    hashMap[id] = adaptCoordinate(coordinate, frameSize)
                }
            }
        }

        landmarks = hashMap
    }

    private fun adaptCoordinate(coordinate: Point, imageSize: Size): Point {

        Log.d(TAG, "view height: ${this.height}")

        val x = if (isLandscape) coordinate.x * (this.width / imageSize.width.toFloat())
                else coordinate.x * (this.width / imageSize.height.toFloat())

        val y = if (isLandscape) coordinate.y * (this.height / imageSize.height.toFloat())
                else coordinate.y * (this.height / imageSize.width.toFloat())

        return Point(x.roundToInt(), y.roundToInt())
    }

    init {
        val wm = this.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        isLandscape = display.width > display.height

        Log.d(TAG, "screen height: ${display.height}")
        Log.d(TAG, "view height: ${this.height}")

        val blue = ResourcesCompat.getColor(this.context.resources, R.color.blue, null)

        dotPaint.color = blue
        dotPaint.strokeWidth = 100f // convert to px
        dotPaint.style = Paint.Style.FILL

        linePaint.color = blue
        linePaint.strokeWidth = 20f // convert to px
        linePaint.style = Paint.Style.FILL

        rectPaint.color = blue
        rectPaint.strokeWidth = 5f // convert to px
        rectPaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        frameRect?.let {canvas?.drawRect(it, rectPaint) }

        canvas?.drawLine(frameRect?.left?.toFloat() ?: 0f, frameRect?.top?.toFloat() ?: 0f, frameRect?.right?.toFloat() ?: 0f, frameRect?.bottom?.toFloat() ?: 0f, linePaint)

        landmarks.forEach { l ->
            canvas?.drawCircle(translateX(l.value.x.toFloat()), l.value.y.toFloat(), 45f, dotPaint) //todo convert to px
        }

        val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]
        val rightShoulder = landmarks[PoseLandmark.RIGHT_SHOULDER]
        val leftElbow = landmarks[PoseLandmark.LEFT_ELBOW]
        val rightElbow = landmarks[PoseLandmark.RIGHT_ELBOW]
        val leftWrist = landmarks[PoseLandmark.LEFT_WRIST]
        val rightWrist = landmarks[PoseLandmark.RIGHT_WRIST]
        val leftHip = landmarks[PoseLandmark.LEFT_HIP]
        val rightHip = landmarks[PoseLandmark.RIGHT_HIP]
        val leftKnee = landmarks[PoseLandmark.LEFT_KNEE]
        val rightKnee = landmarks[PoseLandmark.RIGHT_KNEE]
        val leftAnkle = landmarks[PoseLandmark.LEFT_ANKLE]
        val rightAnkle = landmarks[PoseLandmark.RIGHT_ANKLE]

        // TORSO
        if (leftShoulder != null && rightShoulder != null)
            canvas?.drawLine(translateX(leftShoulder.x.toFloat()), leftShoulder.y.toFloat(), translateX(rightShoulder.x.toFloat()), rightShoulder.y.toFloat(), linePaint)
        if (leftShoulder != null && leftHip != null)
            canvas?.drawLine(translateX(leftShoulder.x.toFloat()), leftShoulder.y.toFloat(), translateX(leftHip.x.toFloat()), leftHip.y.toFloat(), linePaint)
        if (rightShoulder != null && rightHip != null)
                canvas?.drawLine(translateX(rightShoulder.x.toFloat()), rightShoulder.y.toFloat(), translateX(rightHip.x.toFloat()), rightHip.y.toFloat(), linePaint)
        if (rightShoulder != null && rightHip != null)
            canvas?.drawLine(translateX(rightShoulder.x.toFloat()), rightShoulder.y.toFloat(), translateX(rightHip.x.toFloat()), rightHip.y.toFloat(), linePaint)
        if (leftHip != null && rightHip != null)
            canvas?.drawLine(translateX(rightHip.x.toFloat()), rightHip.y.toFloat(), translateX(leftHip.x.toFloat()), leftHip.y.toFloat(), linePaint)

        // ARMS
        if (leftShoulder != null && leftElbow != null)
            canvas?.drawLine(translateX(leftShoulder.x.toFloat()), leftShoulder.y.toFloat(), translateX(leftElbow.x.toFloat()), leftElbow.y.toFloat(), linePaint)
        if (leftWrist != null && leftElbow != null)
            canvas?.drawLine(translateX(leftWrist.x.toFloat()), leftWrist.y.toFloat(), translateX(leftElbow.x.toFloat()), leftElbow.y.toFloat(), linePaint)
        if (rightShoulder != null && rightElbow != null)
            canvas?.drawLine(translateX(rightShoulder.x.toFloat()), rightShoulder.y.toFloat(), translateX(rightElbow.x.toFloat()), rightElbow.y.toFloat(), linePaint)
        if (rightWrist != null && rightElbow != null)
            canvas?.drawLine(translateX(rightWrist.x.toFloat()), rightWrist.y.toFloat(), translateX(rightElbow.x.toFloat()), rightElbow.y.toFloat(), linePaint)

        // LEGS
        if (leftHip != null && leftKnee != null)
            canvas?.drawLine(translateX(leftHip.x.toFloat()), leftHip.y.toFloat(), translateX(leftKnee.x.toFloat()), leftKnee.y.toFloat(), linePaint)
        if (rightHip != null && rightKnee != null)
            canvas?.drawLine(translateX(rightHip.x.toFloat()), rightHip.y.toFloat(), translateX(rightKnee.x.toFloat()), rightKnee.y.toFloat(), linePaint)
        if (leftKnee != null && leftAnkle != null)
            canvas?.drawLine(translateX(leftKnee.x.toFloat()), leftKnee.y.toFloat(), translateX(leftAnkle.x.toFloat()), leftAnkle.y.toFloat(), linePaint)
        if (rightKnee != null && rightAnkle != null)
            canvas?.drawLine(translateX(rightKnee.x.toFloat()), rightKnee.y.toFloat(), translateX(rightAnkle.x.toFloat()), rightAnkle.y.toFloat(), linePaint)
    }

    private fun translateX(x: Float): Float {
        // you will need this for the inverted image in case of using front camera
        return this.width.minus(x)
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
        PoseLandmark.LEFT_ANKLE,
        PoseLandmark.RIGHT_ANKLE,
    )

    companion object {
        private const val MIN_FRAME_SCORE = 0.97
    }
}