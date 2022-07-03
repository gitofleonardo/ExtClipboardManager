package com.hhvvg.ecm.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import androidx.preference.TwoStatePreference
import com.hhvvg.ecm.R

class ExtSwitchPreference(context: Context, attrs: AttributeSet?, defStyleAttr:Int, defStyleRes: Int) : TwoStatePreference(context) {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, -1)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, -1)
    constructor(context: Context): this(context, null)

    private lateinit var iconView: ImageView
    private lateinit var titleView: TextView
    private lateinit var summaryView: TextView
    private lateinit var switchView: ExtSwitch

    init {
        layoutResource = R.layout.ext_swith_preference_layout
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ExtSwitchPreference, defStyleAttr, defStyleRes
        )
        key = a.getString(R.styleable.ExtSwitchPreference_key) ?: ""
        title = a.getString(R.styleable.ExtSwitchPreference_title)
        summary = a.getString(R.styleable.ExtSwitchPreference_summary)
        icon = a.getDrawable(R.styleable.ExtSwitchPreference_icon)
        dependency = a.getString(R.styleable.ExtSwitchPreference_dependency)
        isEnabled = a.getBoolean(R.styleable.ExtSwitchPreference_enabled, true)
        a.recycle()
    }


    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        iconView = holder.findViewById(R.id.icon_view) as ImageView
        titleView = holder.findViewById(R.id.title_text) as TextView
        summaryView = holder.findViewById(R.id.summary_text) as TextView
        switchView = holder.findViewById(R.id.ext_switch) as ExtSwitch

        iconView.setImageDrawable(icon)
        titleView.text = title
        summaryView.text = summary
        switchView.isChecked = isChecked
        switchView.setOnClickListener(null)
    }
}
