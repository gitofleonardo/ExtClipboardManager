package com.hhvvg.ecm.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.ecm.databinding.AutoWorkModeItemLayoutBinding
import com.hhvvg.ecm.ui.fragment.WorkListItem

class WorkModeListAdapter(private val items: List<WorkListItem>, private val setOfPackages: MutableSet<String>) : RecyclerView.Adapter<AutoClearItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoClearItemHolder {
        val binding = AutoWorkModeItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AutoClearItemHolder(binding, this)
    }

    override fun onBindViewHolder(holder: AutoClearItemHolder, position: Int) {
        holder.bind(items[position], setOfPackages)
    }

    override fun getItemCount(): Int = items.size
}

class AutoClearItemHolder(private val binding: AutoWorkModeItemLayoutBinding, private val adapter: WorkModeListAdapter) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: WorkListItem, packages: MutableSet<String>) {
        binding.appIcon.setImageDrawable(item.appIcon)
        binding.packageName.text = item.packageName
        binding.appName.text = item.appName
        binding.checkbox.isChecked = item.checked
        binding.checkbox.setOnClickListener {
            item.checked = !item.checked
            if (item.checked) {
                packages.add(item.packageName)
            } else {
                packages.remove(item.packageName)
            }
        }
    }
}