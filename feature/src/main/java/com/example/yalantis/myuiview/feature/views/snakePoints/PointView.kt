package com.example.yalantis.myuiview.feature.views.snakePoints

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.example.yalantis.myuiview.feature.R

class PointView @JvmOverloads constructor(context: Context,
                                          attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_COLOR = Color.BLACK
    }

    private var circleColor: Int = 0
    private var mRadius: Float = 0.toFloat()
    private var centerX: Float = 0.toFloat()
    private var centerY: Float = 0.toFloat()

    private var paint: Paint? = null


    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleView)
        try {
            circleColor = a.getColor(R.styleable.CircleView_cv_color, DEFAULT_COLOR)
        } finally {
            a.recycle()
        }
        init()
    }

    private fun init() {
        paint = Paint()
        paint?.let {
            it.isAntiAlias = true
            it.color = circleColor
            it.strokeWidth = 1f
            it.style = Paint.Style.FILL_AND_STROKE
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(centerX, centerY, mRadius - 5, paint)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        centerX = ((right - left) / 2).toFloat()
        centerY = ((bottom - top) / 2).toFloat()

        mRadius = Math.min(centerX, centerY)
    }

    fun setColor(colorValue: Int) {
        paint?.color = colorValue
        invalidate()
    }

    fun getCenter(): PointF {
        return PointF(x + width / 2, y + height / 2)
    }

    fun setCenter(center: PointF) {
        setCenter(center.x, center.y)
    }

    fun setCenter(cx: Float, cy: Float) {
        x = cx - width / 2
        y = cy - height / 2
    }

    fun setRadius(radius: Float) {
        mRadius = radius
        invalidate()
    }

    fun getRadius(): Float {
        return mRadius
    }
}