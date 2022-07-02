package com.hhvvg.ecm.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.ecm.configuration.AutoClearStrategyInfo
import com.hhvvg.ecm.databinding.AutoClearStrategyItemLayoutBinding
import com.hhvvg.ecm.ui.data.AutoClearAppItem

class AutoClearListAdapter(private val items: List<AutoClearAppItem>) : RecyclerView.Adapter<AutoClearItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoClearItemHolder {
        val binding = AutoClearStrategyItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AutoClearItemHolder(binding)
    }

    override fun onBindViewHolder(holder: AutoClearItemHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

class AutoClearItemHolder(private val binding: AutoClearStrategyItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: AutoClearAppItem) {
        binding.appIcon.setImageDrawable(item.appIcon)
        binding.packageName.text = item.packageName
        binding.appName.text = item.appName
        val flag = item.strategy.clearFlag
        when {
            flag and AutoClearStrategyInfo.FLAG_CLEAR_IGNORE != 0-> {

            }
        }
    }
}