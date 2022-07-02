package com.hhvvg.ecm.ui.view

import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.core.view.isVisible
import com.hhvvg.ecm.databinding.InputBottomSheetDialogLayoutBinding
import com.hhvvg.ecm.ui.base.BaseBottomSheetDialog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InputBottomSheetDialog(context: Context) : BaseBottomSheetDialog(context) {
    private lateinit var binding : InputBottomSheetDialogLayoutBinding

    private var maxLines: Int = 1
    private var titleText: CharSequence = ""
    private var hintText: CharSequence = ""
    private var onCancelResult: CharSequence = ""
    private var inputType = InputType.TYPE_NULL
    private var result: CharSequence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InputBottomSheetDialogLayoutBinding.inflate(layoutInflater)
        binding.textInput.maxLines = maxLines
        binding.title.text = titleText
        binding.textInput.hint = hintText
        binding.textInput.inputType = inputType
        if (titleText.isEmpty()) {
            binding.title.isVisible = false
        }
        setContentView(binding.root)
    }

    suspend fun showDialog(): CharSequence = suspendCoroutine {
        show()
        setOnDismissListener { _ ->
            val realResult = result
            if (realResult == null) {
                it.resume(onCancelResult)
            } else {
                it.resume(realResult)
            }
        }
        binding.buttonCancel.setOnClickListener { _ ->
            dismiss()
        }
        binding.buttonConfirm.setOnClickListener { _ ->
            result = binding.textInput.text?.toString()
            dismiss()
        }
    }



    class Builder(context: Context) {
        private val dialog = InputBottomSheetDialog(context)

        fun setMaxLines(maxLines: Int): Builder {
            dialog.maxLines = maxLines
            return this
        }

        fun setTitle(title: CharSequence): Builder {
            dialog.titleText = title
            return this
        }

        fun setHint(hintText: CharSequence): Builder {
            dialog.hintText = hintText
            return this
        }

        fun setOnCancelResult(onCancelResult: CharSequence): Builder {
            dialog.onCancelResult = onCancelResult
            return this
        }

        fun setInputType(type: Int): Builder {
            dialog.inputType = type
            return this
        }

        fun build(): InputBottomSheetDialog {
            return dialog
        }
    }
}