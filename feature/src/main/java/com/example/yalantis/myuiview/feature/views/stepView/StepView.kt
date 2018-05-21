package com.example.yalantis.myuiview.feature.views.stepView

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.support.annotation.Dimension
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.View
import com.example.yalantis.myuiview.feature.R
import java.util.*

class StepView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val ANIMATE_STEP_TRANSITION = 0
        private const val IDLE = 1

        private const val START_STEP = 0
    }

    private var displayMode = DisplayMode.DISPLAY_MODE_WITH_TEXT
    private val steps = ArrayList<String>()
    // for display mode DISPLAY_MODE_NO_TEXT
    private var stepsNumber = 0
    private var currentStep = START_STEP
    private var nextAnimatedStep: Int = 0
    private var state = IDLE

    private var animationType: AnimationType = AnimationType.ANIMATION_ALL
    @ColorInt
    private var selectedCircleColor: Int = 0
    @Dimension
    private var selectedCircleRadius: Int = 0
    @ColorInt
    private var selectedTextColor: Int = 0
    @ColorInt
    private var doneCircleColor: Int = 0
    @Dimension
    private var doneCircleRadius: Int = 0
    @ColorInt
    private var doneTextColor: Int = 0
    @ColorInt
    private var nextTextColor: Int = 0
    @Dimension
    private var stepPadding: Int = 0
    @ColorInt
    private var nextStepLineColor: Int = 0
    @ColorInt
    private var doneStepLineColor: Int = 0
    @Dimension
    private var stepLineWidth: Int = 0
    @Dimension(unit = Dimension.SP)
    private var textSize: Float = 0f
    @Dimension
    private var textPadding: Int = 0
    private var selectedStepNumberColor: Int = 0
    @Dimension(unit = Dimension.SP)
    private var stepNumberTextSize: Float = 0f
    @ColorInt
    private var doneStepMarkColor: Int = 0
    private var animationDuration: Int = 0

    private var paint: Paint? = null
    private var animator: ValueAnimator? = null

    private var circlesX: IntArray = intArrayOf()
    private var startLinesX: IntArray = intArrayOf()
    private var endLinesX: IntArray = intArrayOf()
    private var circlesY: Int = 0
    private var textY: Int = 0
    private var animatedFraction: Float = 0f
    private var done: Boolean = false

    private val bounds = Rect()

    init {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint?.textAlign = Paint.Align.CENTER
        applyStyles(context, attrs, defStyleAttr)
        drawEditMode()
    }

    private fun applyStyles(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.StepView, defStyleAttr, R.style.StepView)
        selectedCircleColor = ta.getColor(R.styleable.StepView_sv_selectedCircleColor, 0)
        selectedCircleRadius = ta.getDimensionPixelSize(R.styleable.StepView_sv_selectedCircleRadius, 0)
        selectedTextColor = ta.getColor(R.styleable.StepView_sv_selectedTextColor, 0)
        selectedStepNumberColor = ta.getColor(R.styleable.StepView_sv_selectedStepNumberColor, 0)
        doneStepMarkColor = ta.getColor(R.styleable.StepView_sv_doneStepMarkColor, 0)
        doneCircleColor = ta.getColor(R.styleable.StepView_sv_doneCircleColor, 0)
        doneCircleRadius = ta.getDimensionPixelSize(R.styleable.StepView_sv_doneCircleRadius, 0)
        doneTextColor = ta.getColor(R.styleable.StepView_sv_doneTextColor, 0)
        nextTextColor = ta.getColor(R.styleable.StepView_sv_nextTextColor, 0)
        stepPadding = ta.getDimensionPixelSize(R.styleable.StepView_sv_stepPadding, 0)
        nextStepLineColor = ta.getColor(R.styleable.StepView_sv_nextStepLineColor, 0)
        doneStepLineColor = ta.getColor(R.styleable.StepView_sv_doneStepLineColor, 0)
        stepLineWidth = ta.getDimensionPixelSize(R.styleable.StepView_sv_stepLineWidth, 0)
        textPadding = ta.getDimensionPixelSize(R.styleable.StepView_sv_textPadding, 0)
        stepNumberTextSize = ta.getDimension(R.styleable.StepView_sv_stepNumberTextSize, 0f)
        textSize = ta.getDimension(R.styleable.StepView_sv_textSize, 0f)
        animationDuration = ta.getInteger(R.styleable.StepView_sv_animationDuration, 0)
        animationType = AnimationType.values()[ta.getInteger(R.styleable.StepView_sv_animationType, 0)]
        stepsNumber = ta.getInteger(R.styleable.StepView_sv_stepsNumber, 0)
        val descriptions = ta.getTextArray(R.styleable.StepView_sv_steps)
        displayMode = if (descriptions != null) {
            for (item in descriptions) {
                steps.add(item.toString())
            }
            DisplayMode.DISPLAY_MODE_WITH_TEXT
        } else {
            DisplayMode.DISPLAY_MODE_NO_TEXT
        }
        val background = ta.getDrawable(R.styleable.StepView_sv_background)
        if (background != null) {
            setBackground(background)
        }
        val fontId = ta.getResourceId(R.styleable.StepView_sv_typeface, 0)
        if (fontId != 0) {
            val typeface = ResourcesCompat.getFont(context, fontId)
            if (typeface != null) {
                paint?.typeface = typeface
            }
        }
        ta.recycle()
    }


    private fun drawEditMode() {
        if (isInEditMode) {
            if (displayMode == DisplayMode.DISPLAY_MODE_WITH_TEXT) {
                if (steps.isEmpty()) {
                    steps.add("Step 1")
                    steps.add("Step 2")
                    steps.add("Step 3")
                }
                setSteps(steps)
            } else {
                if (stepsNumber == 0) {
                    stepsNumber = 4
                }
                setStepsNumber(stepsNumber)
            }
        }
    }

    fun setSteps(steps: MutableList<String>?) {
        stepsNumber = 0
        displayMode = DisplayMode.DISPLAY_MODE_WITH_TEXT
        this.steps.clear()
        if (steps != null) {
            this.steps.addAll(steps)
        }
        requestLayout()
        go(START_STEP, false)
    }

    fun setStepsNumber(number: Int) {
        steps.clear()
        displayMode = DisplayMode.DISPLAY_MODE_NO_TEXT
        stepsNumber = number
        requestLayout()
        go(START_STEP, false)
    }

    fun getState(): State {
        return State()
    }

    fun go(step: Int, animate: Boolean) {
        if (step >= START_STEP && step < getStepCount()) {
            if (animate && animationType != AnimationType.ANIMATION_NONE) {
                if (Math.abs(step - currentStep) > 1) {
                    endAnimation()
                    currentStep = step
                    invalidate()
                } else {
                    nextAnimatedStep = step
                    state = ANIMATE_STEP_TRANSITION
                    animate(step)
                    invalidate()
                }
            } else {
                currentStep = step
                invalidate()
            }
        }
    }

    fun done(isDone: Boolean) {
        done = isDone
        invalidate()
    }

    private fun endAnimation() {
        animator?.let {
            if (it.isRunning) {
                it.end()
            }
        }
    }

    private fun animate(step: Int) {
        endAnimation()
        animator = getAnimator(step)
        if (animator == null) {
            return
        }
        animator?.addUpdateListener { valueAnimator ->
            animatedFraction = valueAnimator.animatedFraction
            invalidate()
        }
        animator?.addListener(object : AnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                state = IDLE
                currentStep = step
                invalidate()
            }
        })
        animator?.duration = animationDuration.toLong()
        animator?.start()
    }

    private fun getAnimator(step: Int): ValueAnimator? {
        var animator: ValueAnimator? = null
        val i: Int
        if (step > currentStep) {
            when (animationType) {
                AnimationType.ANIMATION_LINE -> {
                    i = step - 1
                    animator = ValueAnimator.ofInt(startLinesX[i], endLinesX[i])
                }
                AnimationType.ANIMATION_CIRCLE -> animator = ValueAnimator.ofInt(0, selectedCircleRadius)
                AnimationType.ANIMATION_ALL -> {
                    i = step - 1
                    animator = ValueAnimator.ofInt(0, (endLinesX[i] - startLinesX[i] + selectedCircleRadius) / 2)
                }
                else -> {
                }
            }
        } else if (step < currentStep) {
            when (animationType) {
                AnimationType.ANIMATION_LINE -> {
                    i = step
                    animator = ValueAnimator.ofInt(endLinesX[i], startLinesX[i])
                }
                AnimationType.ANIMATION_CIRCLE -> animator = ValueAnimator.ofInt(0, selectedCircleRadius)
                AnimationType.ANIMATION_ALL -> {
                    i = step
                    animator = ValueAnimator.ofInt(0, (endLinesX[i] - startLinesX[i] + selectedCircleRadius) / 2)
                }
                else -> {
                }
            }
        }
        return animator
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.let {
            if (it.isRunning) {
                it.cancel()
            }
        }
    }

    fun getCurrentStep(): Int {
        return currentStep
    }

    fun getStepCount(): Int {
        return if (displayMode == DisplayMode.DISPLAY_MODE_WITH_TEXT) steps.size else stepsNumber
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
        measureAttributes()
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        return MeasureSpec.getSize(widthMeasureSpec)
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        var height = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (heightMode == MeasureSpec.AT_MOST) {
            height = (paddingTop
                    + paddingBottom
                    + Math.max(selectedCircleRadius, doneCircleRadius) * 2
                    + if (displayMode == DisplayMode.DISPLAY_MODE_WITH_TEXT) textPadding else 0)
            if (!steps.isEmpty()) {
                height += measureStepsHeight()
            }
        }

        return height
    }

    private fun fontHeight(): Int {
        return Math.ceil((paint!!.descent() - paint!!.ascent()).toDouble()).toInt()
    }

    private fun measureStepsHeight(): Int {
        paint?.textSize = textSize
        val fontHeight = fontHeight()
        var max = 0
        for (i in steps.indices) {
            val text = steps[i]
            val split = text.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            max = if (split.size == 1) {
                Math.max(fontHeight, max)
            } else {
                Math.max(fontHeight * split.size, max)
            }
        }
        return max
    }

    private fun measureAttributes() {
        circlesY = (measuredHeight - paddingTop - paddingBottom) / 2
        if (displayMode == DisplayMode.DISPLAY_MODE_NO_TEXT) {
            circlesY += paddingTop
        }
        circlesX = getCirclePositions()
        if (displayMode == DisplayMode.DISPLAY_MODE_NO_TEXT) {
            paint?.textSize = stepNumberTextSize
        } else {
            paint?.textSize = stepNumberTextSize
            paint?.textSize = textSize
            textY = circlesY + selectedCircleRadius + textPadding + fontHeight() / 2
        }
        measureLines()
    }

    private fun getCirclePositions(): IntArray {
        return if (displayMode == DisplayMode.DISPLAY_MODE_WITH_TEXT) {
            getCirclePositionsWithText(measureSteps())
        } else {
            getCirclePositionsWithoutText()
        }
    }

    private fun measureSteps(): IntArray {
        val result = IntArray(steps.size)
        for (i in steps.indices) {
            result[i] = paint?.measureText(steps[i])?.toInt()!! + /* correct possible conversion error */ 1
        }
        return result
    }

    private fun getCirclePositionsWithText(textWidth: IntArray): IntArray {
        val result = IntArray(textWidth.size)
        if (result.isEmpty()) {
            return result
        }
        result[0] = paddingLeft + Math.max(textWidth[0] / 2, selectedCircleRadius)
        if (result.size == 1) {
            return result
        }
        result[textWidth.size - 1] = measuredWidth - paddingRight -
                Math.max(textWidth[textWidth.size - 1] / 2, selectedCircleRadius)
        if (result.size < 3) {
            return result
        }
        val spaceLeft = (result[textWidth.size - 1] - result[0]).toFloat()
        val margin = (spaceLeft / (textWidth.size - 1)).toInt()
        for (i in 1 until textWidth.size - 1) {
            result[i] = result[i - 1] + margin
        }
        return result
    }

    private fun getCirclePositionsWithoutText(): IntArray {
        val result = IntArray(getStepCount())
        if (result.isEmpty()) {
            return result
        }
        result[0] = paddingLeft + selectedCircleRadius
        if (result.size == 1) {
            return result
        }
        result[result.size - 1] = measuredWidth - paddingRight - selectedCircleRadius
        val spaceLeft = (result[result.size - 1] - result[0]).toFloat()
        val margin = (spaceLeft / (result.size - 1)).toInt()
        for (i in 1 until result.size - 1) {
            result[i] = result[i - 1] + margin
        }
        return result
    }

    private fun measureLines() {
        startLinesX = IntArray(getStepCount() - 1)
        endLinesX = IntArray(getStepCount() - 1)
        val padding = selectedCircleRadius

        for (i in 1 until getStepCount()) {
            startLinesX[i - 1] = circlesX[i - 1] + padding
            endLinesX[i - 1] = circlesX[i] - padding
        }
    }

    override fun onDraw(canvas: Canvas) {
        val stepSize = getStepCount()

        if (stepSize == 0) {
            return
        }

        for (i in 0 until stepSize) {
            drawStep(canvas, i, circlesX[i], circlesY)
        }

        for (i in startLinesX.indices) {
            if (state == ANIMATE_STEP_TRANSITION && i == nextAnimatedStep - 1
                    && nextAnimatedStep > currentStep && (animationType == AnimationType.ANIMATION_LINE || animationType == AnimationType.ANIMATION_ALL)) {
                val animatedX = (startLinesX[i] + animatedFraction * (endLinesX[i] - startLinesX[i])).toInt()
                drawLine(canvas, startLinesX[i], animatedX, circlesY, true)
                drawLine(canvas, animatedX, endLinesX[i], circlesY, false)
            } else if (state == ANIMATE_STEP_TRANSITION && i == nextAnimatedStep
                    && nextAnimatedStep < currentStep && (animationType == AnimationType.ANIMATION_LINE || animationType == AnimationType.ANIMATION_ALL)) {
                val animatedX = (endLinesX[i] - animatedFraction * (endLinesX[i] - startLinesX[i])).toInt()
                drawLine(canvas, startLinesX[i], animatedX, circlesY, true)
                drawLine(canvas, animatedX, endLinesX[i], circlesY, false)
            } else if (i < currentStep) {
                drawLine(canvas, startLinesX[i], endLinesX[i], circlesY, true)
            } else {
                drawLine(canvas, startLinesX[i], endLinesX[i], circlesY, false)
            }
        }
    }

    private fun drawStep(canvas: Canvas, step: Int, circleCenterX: Int, circleCenterY: Int) {
        val text = if (displayMode == DisplayMode.DISPLAY_MODE_WITH_TEXT) steps[step] else ""
        val isSelected = step == currentStep
        val isDone = if (done) step <= currentStep else step < currentStep
        val number = (step + 1).toString()
        val padding = 2f
        paint?.let {
            if (isSelected && !isDone) {

                val radius: Int = if (state == ANIMATE_STEP_TRANSITION && (animationType == AnimationType.ANIMATION_CIRCLE || animationType == AnimationType.ANIMATION_ALL)
                        && nextAnimatedStep < currentStep) {
                    (selectedCircleRadius - selectedCircleRadius * animatedFraction).toInt()
                } else {
                    selectedCircleRadius
                }

                it.color = doneStepLineColor
                canvas.drawCircle(circleCenterX.toFloat(), circlesY.toFloat(), radius.toFloat(), paint)

                it.color = selectedCircleColor
                canvas.drawCircle(circleCenterX.toFloat(), circleCenterY.toFloat(), radius - padding, paint)



                it.color = selectedStepNumberColor
                it.textSize = stepNumberTextSize
                drawNumber(canvas, number, circleCenterX, it)

                it.color = selectedTextColor
                it.textSize = textSize
                drawText(canvas, text, circleCenterX, textY, it)
            } else if (isDone) {

                it.color = doneCircleColor
                canvas.drawCircle(circleCenterX.toFloat(), circleCenterY.toFloat(), doneCircleRadius.toFloat(), paint)

                drawCheckMark(canvas, circleCenterX, circleCenterY)

                if (state == ANIMATE_STEP_TRANSITION && step == nextAnimatedStep && nextAnimatedStep < currentStep) {
                    it.color = selectedTextColor
                    val alpha = Math.max(Color.alpha(doneTextColor), (animatedFraction * 255).toInt())
                    it.alpha = alpha
                } else {
                    it.color = doneTextColor
                }
                it.textSize = textSize
                drawText(canvas, text, circleCenterX, textY, it)
            } else {
                if (state == ANIMATE_STEP_TRANSITION && step == nextAnimatedStep && nextAnimatedStep > currentStep) {
                    if (animationType == AnimationType.ANIMATION_CIRCLE || animationType == AnimationType.ANIMATION_ALL) {
                        val animatedRadius = (selectedCircleRadius * animatedFraction).toInt()
                        it.color = selectedCircleColor
                        canvas.drawCircle(circleCenterX.toFloat(), circleCenterY.toFloat(), animatedRadius - padding, paint)
                    }
                    if (animationType != AnimationType.ANIMATION_NONE) {
                        if (animationType == AnimationType.ANIMATION_CIRCLE || animationType == AnimationType.ANIMATION_ALL) {
                            it.color = selectedStepNumberColor
                            val alpha = (animatedFraction * 255).toInt()
                            it.alpha = alpha
                            it.textSize = stepNumberTextSize * animatedFraction
                            drawNumber(canvas, number, circleCenterX, it)
                        } else {
                            it.textSize = stepNumberTextSize
                            it.color = nextTextColor
                            drawNumber(canvas, number, circleCenterX, it)
                        }
                    } else {
                        it.textSize = stepNumberTextSize
                        it.color = nextTextColor
                        drawNumber(canvas, number, circleCenterX, it)
                    }

                    it.textSize = textSize
                    it.color = nextTextColor
                    val alpha = Math.max(Color.alpha(nextTextColor).toFloat(), animatedFraction * 255).toInt()
                    it.alpha = alpha
                    drawText(canvas, text, circleCenterX, textY, it)
                } else {
                    it.color = doneStepLineColor
                    canvas.drawCircle(circleCenterX.toFloat(), circleCenterY.toFloat(), selectedCircleRadius.toFloat(), paint)
                    it.color = Color.WHITE
                    canvas.drawCircle(circleCenterX.toFloat(), circleCenterY.toFloat(), selectedCircleRadius - padding, paint)

                    it.textSize = stepNumberTextSize
                    it.color = nextTextColor
//                    drawNumber(canvas, number, circleCenterX, it)

                    it.textSize = textSize
                    drawText(canvas, text, circleCenterX, textY, it)
                }
            }
        }
    }

    private fun drawNumber(canvas: Canvas, number: String, circleCenterX: Int, paint: Paint) {
        paint.getTextBounds(number, 0, number.length, bounds)
        val padding = 2f
        canvas.drawCircle(circleCenterX.toFloat(), circlesY.toFloat(), (bounds.bottom - bounds.top - padding), paint)
    }

    private fun drawText(canvas: Canvas, text: String, x: Int, y: Int, paint: Paint) {
        if (text.isEmpty()) {
            return
        }
        val split = text.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (split.size == 1) {
            canvas.drawText(text, x.toFloat(), y.toFloat(), paint)
        } else {
            for (i in split.indices) {
                canvas.drawText(split[i], x.toFloat(), (y + i * fontHeight()).toFloat(), paint)
            }
        }
    }

    private fun drawCheckMark(canvas: Canvas, circleCenterX: Int, circleCenterY: Int) {
        paint?.color = doneStepMarkColor
        val width = stepNumberTextSize * 0.1f
        paint?.strokeWidth = width
        val bounds = Rect(
                (circleCenterX - width * 4.5).toInt(),
                (circleCenterY - width * 3.5).toInt(),
                (circleCenterX + width * 4.5).toInt(),
                (circleCenterY + width * 3.5).toInt())
        canvas.drawLine(
                bounds.left + 0.5f * width,
                bounds.bottom - 3.25f * width,
                bounds.left + 3.25f * width,
                bounds.bottom - 0.75f * width, paint)
        canvas.drawLine(
                bounds.left + 2.75f * width,
                bounds.bottom - 0.75f * width,
                bounds.right - 0.375f * width,
                bounds.top + 0.75f * width, paint)
    }

    private fun drawLine(canvas: Canvas, startX: Int, endX: Int, centerY: Int, highlight: Boolean) {
        if (highlight) {
            paint?.color = doneStepLineColor
            paint?.strokeWidth = stepLineWidth.toFloat()
            canvas.drawLine(startX.toFloat(), centerY.toFloat(), endX.toFloat(), centerY.toFloat(), paint)
        } else {
            paint?.color = nextStepLineColor
            paint?.strokeWidth = stepLineWidth.toFloat()
            canvas.drawLine(startX.toFloat(), centerY.toFloat(), endX.toFloat(), centerY.toFloat(), paint)
        }
    }

    inner class State {
        private var steps: MutableList<String>? = null
        private var stepsNumber: Int = 0

        private var animationType: AnimationType = this@StepView.animationType
        @ColorInt
        private var selectedCircleColor = this@StepView.selectedCircleColor
        @Dimension
        private var selectedCircleRadius = this@StepView.selectedCircleRadius
        @ColorInt
        private var selectedTextColor = this@StepView.selectedTextColor
        @ColorInt
        private var doneCircleColor = this@StepView.doneCircleColor
        @Dimension
        private var doneCircleRadius = this@StepView.doneCircleRadius
        @ColorInt
        private var doneTextColor = this@StepView.doneTextColor
        @ColorInt
        private var nextTextColor = this@StepView.nextTextColor
        @Dimension
        private var stepPadding = this@StepView.stepPadding
        @ColorInt
        private var nextStepLineColor = this@StepView.nextStepLineColor
        @ColorInt
        private var doneStepLineColor = this@StepView.doneStepLineColor
        @Dimension
        private var stepLineWidth = this@StepView.stepLineWidth
        @Dimension(unit = Dimension.SP)
        private var textSize = this@StepView.textSize
        @Dimension
        private var textPadding = this@StepView.textPadding
        @ColorInt
        private var selectedStepNumberColor = this@StepView.selectedStepNumberColor
        @Dimension(unit = Dimension.SP)
        private var stepNumberTextSize = this@StepView.stepNumberTextSize
        @ColorInt
        private var doneStepMarkColor = this@StepView.doneStepMarkColor
        private var animationDuration = this@StepView.animationDuration
        private var typeface = paint?.typeface

        fun animationType(animationType: AnimationType): State {
            this.animationType = animationType
            return this
        }

        fun selectedCircleColor(@ColorInt selectedCircleColor: Int): State {
            this.selectedCircleColor = selectedCircleColor
            return this
        }

        fun selectedCircleRadius(@Dimension selectedCircleRadius: Int): State {
            this.selectedCircleRadius = selectedCircleRadius
            return this
        }

        fun selectedTextColor(@ColorInt selectedTextColor: Int): State {
            this.selectedTextColor = selectedTextColor
            return this
        }

        fun doneCircleColor(@ColorInt doneCircleColor: Int): State {
            this.doneCircleColor = doneCircleColor
            return this
        }

        fun doneCircleRadius(@Dimension doneCircleRadius: Int): State {
            this.doneCircleRadius = doneCircleRadius
            return this
        }

        fun doneTextColor(@ColorInt doneTextColor: Int): State {
            this.doneTextColor = doneTextColor
            return this
        }

        fun nextTextColor(@ColorInt nextTextColor: Int): State {
            this.nextTextColor = nextTextColor
            return this
        }

        fun stepPadding(@Dimension stepPadding: Int): State {
            this.stepPadding = stepPadding
            return this
        }

        fun nextStepLineColor(@ColorInt nextStepLineColor: Int): State {
            this.nextStepLineColor = nextStepLineColor
            return this
        }

        fun doneStepLineColor(@ColorInt doneStepLineColor: Int): State {
            this.doneStepLineColor = doneStepLineColor
            return this
        }

        fun stepLineWidth(@Dimension stepLineWidth: Int): State {
            this.stepLineWidth = stepLineWidth
            return this
        }

        fun textSize(@Dimension(unit = Dimension.SP) textSize: Int): State {
            this.textSize = textSize.toFloat()
            return this
        }

        fun textPadding(@Dimension textPadding: Int): State {
            this.textPadding = textPadding
            return this
        }

        fun selectedStepNumberColor(@ColorInt selectedStepNumberColor: Int): State {
            this.selectedStepNumberColor = selectedStepNumberColor
            return this
        }

        fun stepNumberTextSize(@Dimension(unit = Dimension.SP) stepNumberTextSize: Int): State {
            this.stepNumberTextSize = stepNumberTextSize.toFloat()
            return this
        }

        fun doneStepMarkColor(@ColorInt doneStepMarkColor: Int): State {
            this.doneStepMarkColor = doneStepMarkColor
            return this
        }

        fun animationDuration(animationDuration: Int): State {
            this.animationDuration = animationDuration
            return this
        }

        fun steps(steps: MutableList<String>): State {
            this.steps = steps
            return this
        }

        fun stepsNumber(stepsNumber: Int): State {
            this.stepsNumber = stepsNumber
            return this
        }

        fun typeface(typeface: Typeface): State {
            this.typeface = typeface
            return this
        }

        fun commit() {
            this@StepView.animationType = animationType
            this@StepView.selectedTextColor = selectedTextColor
            this@StepView.selectedCircleRadius = selectedCircleRadius
            this@StepView.selectedTextColor = selectedTextColor
            this@StepView.doneCircleColor = doneCircleColor
            this@StepView.doneCircleRadius = doneCircleRadius
            this@StepView.doneTextColor = doneTextColor
            this@StepView.nextTextColor = nextTextColor
            this@StepView.stepPadding = stepPadding
            this@StepView.nextStepLineColor = nextStepLineColor
            this@StepView.doneStepLineColor = doneStepLineColor
            this@StepView.stepLineWidth = stepLineWidth
            this@StepView.textSize = textSize
            this@StepView.textPadding = textPadding
            this@StepView.selectedStepNumberColor = selectedStepNumberColor
            this@StepView.stepNumberTextSize = stepNumberTextSize
            this@StepView.doneStepMarkColor = doneStepMarkColor
            this@StepView.animationDuration = animationDuration
            paint?.typeface = typeface
            if (steps != null && this@StepView.steps != steps) {
                this@StepView.setSteps(steps)
            } else if (stepsNumber != 0 && stepsNumber != this@StepView.stepsNumber) {
                this@StepView.setStepsNumber(stepsNumber)
            } else {
                this@StepView.invalidate()
            }
        }
    }

    enum class AnimationType {
        ANIMATION_LINE, ANIMATION_CIRCLE, ANIMATION_ALL, ANIMATION_NONE
    }

    enum class DisplayMode {
        DISPLAY_MODE_WITH_TEXT, DISPLAY_MODE_NO_TEXT
    }

}