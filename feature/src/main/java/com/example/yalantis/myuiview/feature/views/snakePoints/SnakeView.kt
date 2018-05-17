package com.example.yalantis.myuiview.feature.views.snakePoints

import android.animation.*
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.example.yalantis.myuiview.feature.R
import kotlinx.android.synthetic.main.snake_layout.view.*
import java.util.*

class SnakeView @JvmOverloads constructor(context: Context,
                attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "RubberIndicator"
        private const val DEFAULT_SMALL_CIRCLE_COLOR = -0x20727f
        private const val DEFAULT_LARGE_CIRCLE_COLOR = -0x50c7ac
        private const val DEFAULT_OUTER_CIRCLE_COLOR = -0xaccbaa

        private const val SMALL_CIRCLE_RADIUS = 20
        private const val LARGE_CIRCLE_RADIUS = 25
        private const val OUTER_CIRCLE_RADIUS = 50

        private const val BEZIER_CURVE_ANCHOR_DISTANCE = 30

        private const val CIRCLE_TYPE_SMALL = 0x00
        private const val CIRCLE_TYPE_LARGE = 0x01
        private const val CIRCLE_TYPE_OUTER = 0x02
    }

    /**
     * colors
     */
    private var smallCircleColor: Int = 0
    private var largeCircleColor: Int = 0
    private var outerCircleColor: Int = 0

    /**
     * coordinate values
     */
    private var smallCircleRadius: Int = 0
    private var largeCircleRadius: Int = 0
    private var outerCircleRadius: Int = 0

    /**
     * views
     */
    private var largeCircle: PointView? = null
    private var mSmallCircle: PointView? = null
    private var outerCircle: PointView? = null
    private var circleViews: MutableList<PointView>? = null

    /**
     * animations
     */
    private var mAnim: AnimatorSet? = null
    private var mPvhScaleX: PropertyValuesHolder? = null
    private var mPvhScaleY: PropertyValuesHolder? = null
    private var mPvhScale: PropertyValuesHolder? = null
    private var mPvhRotation: PropertyValuesHolder? = null

    private var pendingAnimations: LinkedList<Boolean>? = null

    /**
     * Movement Path
     */
    private var smallCirclePath: Path? = null

    /**
     * Indicator movement listener
     */
    private var mOnMoveListener: OnMoveListener? = null

    /**
     * Helper values
     */
    private var focusPosition = -1
    private val mBezierCurveAnchorDistance = dp2px(BEZIER_CURVE_ANCHOR_DISTANCE)

    init {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet? = null, defStyle: Int = 0) {
        /** Get XML attributes  */
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.RubberIndicator, defStyle, 0)
        smallCircleColor = styledAttributes.getColor(R.styleable.RubberIndicator_smallCircleColor, DEFAULT_SMALL_CIRCLE_COLOR)
        largeCircleColor = styledAttributes.getColor(R.styleable.RubberIndicator_largeCircleColor, DEFAULT_LARGE_CIRCLE_COLOR)
        outerCircleColor = styledAttributes.getColor(R.styleable.RubberIndicator_outerCircleColor, DEFAULT_OUTER_CIRCLE_COLOR)
        styledAttributes.recycle()

        /** Initialize views  */
        val rootView = inflate(context, R.layout.snake_layout, this)
        outerCircle = rootView.findViewById(R.id.outer_circle) as PointView

        // Apply outer color to outerCircle and background shape
        val containerWrapper = rootView.findViewById(R.id.container_wrapper) as View
        outerCircle?.setColor(outerCircleColor)
        val shape = containerWrapper.background as GradientDrawable
        shape.setColor(outerCircleColor)

        /** values  */
        smallCircleRadius = dp2px(SMALL_CIRCLE_RADIUS)
        largeCircleRadius = dp2px(LARGE_CIRCLE_RADIUS)
        outerCircleRadius = dp2px(OUTER_CIRCLE_RADIUS)

        /** animators  */
        mPvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.8f, 1f)
        mPvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.8f, 1f)
        mPvhScale = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.5f, 1f)
        mPvhRotation = PropertyValuesHolder.ofFloat("rotation", 0f)

        smallCirclePath = Path()

        pendingAnimations = LinkedList()

        /** circle view list  */
        circleViews = ArrayList()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        // Prevent crash if the count as not been set
        if (largeCircle != null) {
            outerCircle!!.setCenter(largeCircle!!.getCenter())
        }
    }

    fun setCount(count: Int) {
        if (focusPosition == -1) {
            focusPosition = 0
        }
        setCount(count, focusPosition)
    }

    /**
     * This method must be called before [.setCount], otherwise the focus position will
     * be set to the default value - zero.
     * @param pos the focus position
     */
    fun setFocusPosition(pos: Int) {
        focusPosition = pos
    }

    fun setCount(count: Int, focusPos: Int) {
        if (count < 2) {
            throw IllegalArgumentException("count must be greater than 2")
        }

        if (focusPos >= count) {
            throw IllegalArgumentException("focus position must be less than count")
        }

        /* Check if the number on indicator has changed since the last setCount to prevent duplicate */
        if (circleViews!!.size != count) {
            container.removeAllViews()
            circleViews!!.clear()

            var i = 0
            while (i < focusPos) {
                addSmallCircle()
                i++
            }

            addLargeCircle()

            i = focusPos + 1
            while (i < count) {
                addSmallCircle()
                i++
            }
        }

        focusPosition = focusPos
    }

    fun getFocusPosition(): Int {
        return focusPosition
    }

    fun moveToLeft() {
        if (mAnim != null && mAnim!!.isRunning) {
            pendingAnimations!!.add(false)
            return
        }
        move(false)
    }

    fun moveToRight() {
        mAnim?.let {
            if (it.isRunning) {
                pendingAnimations?.add(true)
                return
            }
        }
        move(true)
    }

    fun setOnMoveListener(moveListener: OnMoveListener) {
        mOnMoveListener = moveListener
    }

    private fun addSmallCircle() {
        val smallCircle = createCircleView(CIRCLE_TYPE_SMALL)
        circleViews?.add(smallCircle)
        container.addView(smallCircle)
    }

    private fun addLargeCircle() {
        largeCircle = createCircleView(CIRCLE_TYPE_LARGE)
        largeCircle?.let {
            circleViews?.add(it)
            container.addView(it)
        }
    }

    private fun createCircleView(type: Int): PointView {
        val circleView = PointView(context)

        val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.weight = 1f
        params.gravity = Gravity.CENTER_VERTICAL

        when (type) {
            CIRCLE_TYPE_SMALL -> {
                params.width = smallCircleRadius shl 1
                params.height = params.width
                circleView.setColor(smallCircleColor)
            }
            CIRCLE_TYPE_LARGE -> {
                params.width = largeCircleRadius shl 1
                params.height = params.width
                circleView.setColor(largeCircleColor)
            }
            CIRCLE_TYPE_OUTER -> {
                params.width = outerCircleRadius shl 1
                params.height = params.width
                circleView.setColor(outerCircleColor)
            }
        }

        circleView.layoutParams = params

        return circleView
    }

    private fun getNextPosition(toRight: Boolean): Int {
        val nextPos = focusPosition + if (toRight) 1 else -1
        return if (nextPos < 0 || nextPos >= circleViews!!.size) -1 else nextPos
    }

    private fun swapCircles(currentPos: Int, nextPos: Int) {
        val circleView = circleViews!![currentPos]
        circleViews!![currentPos] = circleViews!![nextPos]
        circleViews!![nextPos] = circleView
    }

    @SuppressLint("ObjectAnimatorBinding")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun move(toRight: Boolean) {
        val nextPos = getNextPosition(toRight)
        if (nextPos == -1) return

        mSmallCircle = circleViews!![nextPos]

        // Calculate the new x coordinate for circles.
        val smallCircleX = if (toRight)
            largeCircle!!.x
        else
            largeCircle!!.x + largeCircle!!.width - mSmallCircle!!.width
        val largeCircleX = if (toRight)
            mSmallCircle!!.x + mSmallCircle!!.width - largeCircle!!.width
        else
            mSmallCircle!!.x
        val outerCircleX = outerCircle!!.x + largeCircleX - largeCircle!!.x

        // animations for large circle and outer circle.
        var pvhX: PropertyValuesHolder? = null
        largeCircle?.let {
            pvhX = PropertyValuesHolder.ofFloat("x", it.x, largeCircleX)
        }
        val largeCircleAnim = ObjectAnimator.ofPropertyValuesHolder(
                largeCircle, pvhX, mPvhScaleX, mPvhScaleY)

        pvhX = PropertyValuesHolder.ofFloat("x", outerCircle!!.x, outerCircleX)
        val outerCircleAnim = ObjectAnimator.ofPropertyValuesHolder(
                outerCircle, pvhX, mPvhScaleX, mPvhScaleY)

        // Animations for small circle
        val smallCircleCenter = mSmallCircle!!.getCenter()
        val smallCircleEndCenter = PointF(
                smallCircleCenter.x - (mSmallCircle!!.x - smallCircleX), smallCircleCenter.y)

        // Create motion anim for small circle.
        smallCirclePath?.reset()
        smallCirclePath?.moveTo(smallCircleCenter.x, smallCircleCenter.y)
        smallCirclePath?.quadTo(smallCircleCenter.x, smallCircleCenter.y,
                (smallCircleCenter.x + smallCircleEndCenter.x) / 2,
                (smallCircleCenter.y + smallCircleEndCenter.y) / 2 + mBezierCurveAnchorDistance)
        smallCirclePath?.lineTo(smallCircleEndCenter.x, smallCircleEndCenter.y)

        val smallCircleAnim: ValueAnimator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            smallCircleAnim = ObjectAnimator.ofObject(mSmallCircle, "center", PointFEvaluator(), smallCirclePath)
        } else {
            val pathMeasure = PathMeasure(smallCirclePath, false)
            val point = FloatArray(2)
            smallCircleAnim = ValueAnimator.ofFloat(0.0f, 1.0f)
            smallCircleAnim.addUpdateListener { animation ->
                pathMeasure.getPosTan(
                        pathMeasure.length * animation.animatedFraction, point, null)
                mSmallCircle?.setCenter(PointF(point[0], point[1]))
            }
        }

        mPvhRotation?.setFloatValues(0f, if (toRight) -30f else 30f, 0f, if (toRight) 30f else -30f, 0f)
        val otherAnim = ObjectAnimator.ofPropertyValuesHolder(mSmallCircle, mPvhRotation, mPvhScale)

        mAnim = AnimatorSet()
        mAnim?.play(smallCircleAnim)?.with(otherAnim)?.with(largeCircleAnim)?.with(outerCircleAnim)
        mAnim?.interpolator = AccelerateDecelerateInterpolator()
        mAnim?.duration = 500
        mAnim?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                swapCircles(focusPosition, nextPos)
                focusPosition = nextPos

                if (mOnMoveListener != null) {
                    if (toRight) {
                        mOnMoveListener?.onMovedToRight()
                    } else {
                        mOnMoveListener?.onMovedToLeft()
                    }
                }
                pendingAnimations?.let {
                    if (!it.isEmpty()) {
                        move(it.removeFirst())
                    }
                }

            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        mAnim!!.start()
    }

    private fun dp2px(dpValue: Int): Int {
        return context.resources.displayMetrics.density.toInt() * dpValue
    }

    interface OnMoveListener {
        fun onMovedToLeft()
        fun onMovedToRight()
    }

}