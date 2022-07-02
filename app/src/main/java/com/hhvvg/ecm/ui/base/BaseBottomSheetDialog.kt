package com.hhvvg.ecm.ui.base

import android.content.Context
import android.os.Bundle
import android.view.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hhvvg.ecm.R

abstract class BaseBottomSheetDialog(context: Context) : BottomSheetDialog(context, R.style.BaseBottomSheetStyle) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        }
    }
}