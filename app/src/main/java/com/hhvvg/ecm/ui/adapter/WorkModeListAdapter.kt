package com.hhvvg.ecm.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.ecm.databinding.AutoWorkModeItemLayoutBinding
import com.hhvvg.ecm.ui.fragment.WorkListItem

class WorkModeListAdapter(private val items: List<WorkListItem>, private val filterPackages: MutableSet<String>) : RecyclerView.Adapter<AutoClearItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoClearItemHolder {
        val binding = AutoWorkModeItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AutoClearItemHolder(binding)
    }

    override fun onBindViewHolder(holder: AutoClearItemHolder, position: Int) {
        holder.bind(items[position], filterPackages)
    }

    override fun getItemCount(): Int = items.size
}

class AutoClearItemHolder(private val binding: AutoWorkModeItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: WorkListItem, filterPackages: MutableSet<String>) {
        binding.appIcon.setImageDrawable(item.appIcon)
        binding.packageName.text = item.packageName
        binding.appName.text = item.appName
        binding.checkbox.isChecked = filterPackages.contains(item.packageName)

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterPackages.add(item.packageName)
            } else {
                filterPackages.remove(item.packageName)
            }
        }
        binding.root.setOnClickListener {
            binding.checkbox.toggle()
        }
    }
}