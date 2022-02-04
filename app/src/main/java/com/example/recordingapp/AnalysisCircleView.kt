package com.example.recordingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class AnalysisCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mainPaint: Paint = Paint()
    private val backgroundPaint: Paint = Paint()
    private var rectangle: RectF? = null
    private var margin: Float
    private var arcProportion: Float = 0f

    init {
        mainPaint.isAntiAlias = true
        mainPaint.color = ContextCompat.getColor(context, R.color.colorLutea)
        mainPaint.style = Paint.Style.STROKE
        mainPaint.strokeWidth = 5.dpToPx()
        backgroundPaint.isAntiAlias = true
        backgroundPaint.color = ContextCompat.getColor(context, R.color.black_08)
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = 5.dpToPx()
        margin = 3.dpToPx() // margin should be >= strokeWidth / 2 (otherwise the arc is cut)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rectangle == null) {
            rectangle = RectF(0f + margin, 0f + margin, width.toFloat() - margin, height.toFloat() - margin)
        }
        canvas.drawArc(rectangle!!, -90f, arcProportion * 360, false, mainPaint)
        // This 2nd arc completes the circle. Remove it if you don't want it
        canvas.drawArc(rectangle!!, -90f + arcProportion * 360, (1 - arcProportion) * 360, false, backgroundPaint)
    }

    /**
     * @param arcProportion The proportion of the semi circle arc, from 0 to 1. Setting 0 makes the arc invisible, and 1
     * makes a whole circle.
     */
    fun setArcProportion(arcProportion: Float) {
        this.arcProportion = arcProportion
        invalidate()
    }

}