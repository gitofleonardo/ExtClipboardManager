package com.hhvvg.ecm.ui.base

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hhvvg.ecm.R

abstract class BaseBottomSheetDialog(context: Context) : BottomSheetDialog(context, R.style.BaseBottomSheetStyle), View.OnTouchListener, GestureDetector.OnGestureListener {
    private val detector = GestureDetector(context, this)
    private var animator: ValueAnimator? = null
    private var lastTapTime = -1L

    protected open var dialogCancelable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
            decorView.setOnTouchListener(this@BaseBottomSheetDialog)
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return detector.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val eventTime = e.eventTime
        if (dialogCancelable && eventTime - lastTapTime < ViewConfiguration.getDoubleTapTimeout()) {
            // Cancel dialog
            dismiss()
            return true
        }
        lastTapTime = eventTime
        animator?.cancel()
        animator = ValueAnimator.ofInt(100, 0).apply {
            addUpdateListener {
                val offset = it.animatedValue as Int
                behavior.expandedOffset = offset
            }
        }
        animator?.start()
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }
}