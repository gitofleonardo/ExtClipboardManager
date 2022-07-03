package com.hhvvg.ecm.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.ecm.databinding.AutoClearContentExclusionItemLayoutBinding

class AutoClearContentExclusionAdapter(private val items: ArrayList<String>) : RecyclerView.Adapter<Holder>() {
    private var onRemoveListener: ((CharSequence) -> Unit)? = null
    private var onItemClickListener: ((String, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = AutoClearContentExclusionItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
        holder.binding.icDelete.setOnClickListener {
            val content = items[position]
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size - position)
            onRemoveListener?.invoke(content)
        }
        holder.binding.root.setOnClickListener {
            onItemClickListener?.invoke(items[position], position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setOnRemoveClickListener(listener: (CharSequence) -> Unit) {
        onRemoveListener = listener
    }

    fun setOnItemClickListener(listener: (String, Int) -> Unit) {
        onItemClickListener = listener
    }
}

class Holder(val binding: AutoClearContentExclusionItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(content: CharSequence) {
        binding.contentText.text = content
    }
}