package com.hhvvg.ecm.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Checkable
import com.hhvvg.ecm.R
import kotlin.math.min


class ExtSwitch : View, Checkable, ValueAnimator.AnimatorUpdateListener, View.OnClickListener {
    companion object {
        private const val DEFAULT_WIDTH_IN_DP = 64
        private const val DEFAULT_HEIGHT_IN_DP = 32
        private const val DEFAULT_THUMB_GAP = 5
        private const val DEFAULT_DURATION = 250L
        private const val DEFAULT_DISABLE_ALPHA = 0.75F
        private const val DEFAULT_UNCHECKED_ALPHA = 0.9F

        private const val STATE_CLOSE = 0
        private const val STATE_ANIMATING_CLOSE = 1
        private const val STATE_OPEN = 2
        private const val STATE_ANIMATING_OPEN = 3

        private const val MAX_THUMB_FACTOR = 0.50F
    }

    private var _backgroundColor: Int = 0
    private var _thumbColor: Int = 0
    private var _primaryColor: Int = 0

    var switchBackgroundColor: Int
        get() = _backgroundColor
        set(value) {
            _backgroundColor = value
            invalidate()
        }

    var switchThumbColor: Int
        get() = _thumbColor
        set(value) {
            _thumbColor = value
            invalidate()
        }
    var switchPrimaryColor: Int
        get() = _primaryColor
        set(value) {
            _primaryColor = value
            invalidate()
        }

    private val backgroundRect = RectF()
    private val thumbStartRect = RectF()
    private val thumbEndRect = RectF()
    private var radius = 0.0F
    private var thumbRadius = 0.0F
    private var state: Int = STATE_CLOSE
    private var animatingProgress = 0.0F
    private var switchAnimator: ValueAnimator? = null
    private var thumbGap = 0.0F
    private var maxThumbExpandSize = 0.0F
    private var currentChecked = false

    private val paint = Paint()
    private val colorEvaluator = ArgbEvaluator()

    private var onCheckChangedListener: OnCheckChangedListener? = null
    private var markFromUser = false

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ExtSwitch, defStyle, 0
        )
        _backgroundColor = a.getColor(R.styleable.ExtSwitch_backgroundColor, 0)
        _thumbColor = a.getColor(R.styleable.ExtSwitch_thumbColor, 0)
        _primaryColor = a.getColor(R.styleable.ExtSwitch_colorPrimary, 0)
        a.recycle()
        paint.isAntiAlias = true
        setOnClickListener(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST) {
            width = DEFAULT_WIDTH_IN_DP.dp2px()
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            height = DEFAULT_HEIGHT_IN_DP.dp2px()
        }
        setMeasuredDimension(width, height)
        calculateParams()
    }

    private fun calculateParams() {
        val padStart = paddingStart + paddingLeft
        val padEnd = paddingEnd + paddingRight
        val availableWidth = measuredWidth - padStart - padEnd
        val availableHeight = measuredHeight - paddingTop - paddingBottom
        backgroundRect.set(
            padStart.toFloat(),
            paddingTop.toFloat(),
            (availableWidth - padEnd).toFloat(),
            (paddingTop + availableHeight).toFloat()
        )
        thumbGap = DEFAULT_THUMB_GAP.dp2px().toFloat()
        radius = min(availableWidth, availableHeight) / 2.0F
        thumbRadius = (min(availableWidth, availableHeight) - 2 * thumbGap) / 2.0F
        maxThumbExpandSize = (availableWidth - 2 * thumbRadius - 2 * thumbGap) * MAX_THUMB_FACTOR
        thumbStartRect.set(
            backgroundRect.left + thumbGap,
            backgroundRect.top + thumbGap,
            backgroundRect.left + thumbGap + thumbRadius * 2,
            backgroundRect.top + thumbRadius * 2 + thumbGap
        )
        thumbEndRect.set(
            backgroundRect.right - thumbGap - thumbRadius * 2,
            backgroundRect.top + thumbGap,
            backgroundRect.right - thumbGap,
            backgroundRect.top + thumbRadius * 2 + thumbGap
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawThumb(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        var bgColor =
            colorEvaluator.evaluate(animatingProgress, _backgroundColor, _primaryColor) as Int
        bgColor = if (isEnabled) {
            bgColor
        } else {
            makeAlphaColor(DEFAULT_DISABLE_ALPHA, bgColor)
        }
        paint.color = bgColor
        canvas.drawRoundRect(backgroundRect, radius, radius, paint)
    }

    private fun makeAlphaColor(alpha: Float, color: Int): Int {
        val afterAlpha = Color.alpha(color) * alpha
        return Color.argb(
            afterAlpha.toInt(),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }

    private fun drawThumb(canvas: Canvas) {
        paint.color = switchThumbColor
        val currentProgress = animatingProgress
        val currentExpanded = if (currentProgress < 0.5F) {
            maxThumbExpandSize * currentProgress * 2
        } else {
            -2 * maxThumbExpandSize * currentProgress + 2 * maxThumbExpandSize
        }
        val left = thumbStartRect.left + (thumbEndRect.left - thumbStartRect.left) * currentProgress
        val right = left + thumbRadius * 2 + currentExpanded
        val top = thumbStartRect.top
        val bottom = thumbStartRect.bottom
        canvas.drawRoundRect(left, top, right, bottom, thumbRadius, thumbRadius, paint)
    }

    private fun animateChecked() {
        switchAnimator?.cancel()
        switchAnimator = ValueAnimator.ofFloat(animatingProgress, 1.0F).apply {
            duration = DEFAULT_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener(this@ExtSwitch)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    state = STATE_ANIMATING_OPEN
                }

                override fun onAnimationEnd(animation: Animator?) {
                    state = STATE_OPEN
                }

                override fun onAnimationCancel(animation: Animator?) {
                    state = STATE_CLOSE
                }
            })
            start()
        }
    }

    private fun animateUnchecked() {
        switchAnimator?.cancel()
        switchAnimator = ValueAnimator.ofFloat(animatingProgress, 0.0F).apply {
            duration = DEFAULT_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener(this@ExtSwitch)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    state = STATE_ANIMATING_CLOSE
                }

                override fun onAnimationEnd(animation: Animator?) {
                    state = STATE_CLOSE
                }

                override fun onAnimationCancel(animation: Animator?) {
                    state = STATE_OPEN
                }
            })
            start()
        }
    }

    override fun setChecked(checked: Boolean) {
        if (isHapticFeedbackEnabled) {
            performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
        currentChecked = checked
        if (currentChecked) {
            animateChecked()
        } else {
            animateUnchecked()
        }
        onCheckChangedListener?.onCheckChanged(checked, markFromUser)
        markFromUser = false
    }

    override fun isChecked(): Boolean = currentChecked

    override fun toggle() {
        isChecked = !isChecked
    }

    private fun Int.dp2px(): Int {
        return TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics)
            .toInt()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        animatingProgress = animation.animatedValue as Float
        invalidate()
    }

    override fun onClick(v: View) {
        markFromUser = true
        toggle()
    }

    @FunctionalInterface
    interface OnCheckChangedListener {
        fun onCheckChanged(isChecked: Boolean, fromUser: Boolean)
    }
}
