package com.hhvvg.ecm.ui.fragment

import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hhvvg.ecm.R
import com.hhvvg.ecm.databinding.FragmentAutoClearContentExclusionBinding
import com.hhvvg.ecm.ui.adapter.AutoClearContentExclusionAdapter
import com.hhvvg.ecm.ui.view.InputBottomSheetDialog
import com.hhvvg.ecm.util.getSystemExtClipboardService
import kotlinx.coroutines.launch

class AutoClearContentExclusionFragment : Fragment() {
    private var binding: FragmentAutoClearContentExclusionBinding? = null
    private val items = ArrayList<String>()
    private val adapter = AutoClearContentExclusionAdapter(items).apply {
        setOnRemoveClickListener {
            saveServiceItems(items)
        }
        setOnItemClickListener { src, pos ->
            showEditDialogForItem(src, pos)
        }
    }
    private val service by lazy { requireContext().getSystemExtClipboardService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAutoClearContentExclusionBinding.inflate(layoutInflater, container, false)
        binding.contentRecyclerview.adapter = adapter
        binding.addFab.setOnClickListener {
            showAddItemInputDialog()
        }
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadContents()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.auto_clear_exclusion_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.clear_all -> {
                items.clear()
                adapter.notifyDataSetChanged()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveServiceItems(items: ArrayList<String>) {
        service?.autoClearContentExclusionList = items
    }

    private fun showEditDialogForItem(origin: String, position: Int) {
        val dialog = InputBottomSheetDialog.Builder(requireContext())
            .setText(origin)
            .setTitle(getString(R.string.exclusion_content_title))
            .setHint(getString(R.string.exclusion_content_hint))
            .setOnCancelResult("")
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .build()
        lifecycleScope.launch {
            val result = dialog.showDialog().toString()
            if (result.isNotEmpty()) {
                items[position] = result
                adapter.notifyItemChanged(position)
                saveServiceItems(items)
            }
        }
    }

    private fun showAddItemInputDialog() {
        val dialog = InputBottomSheetDialog.Builder(requireContext())
            .setTitle(getString(R.string.exclusion_content_title))
            .setHint(getString(R.string.exclusion_content_hint))
            .setOnCancelResult("")
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .build()
        lifecycleScope.launch {
            val result = dialog.showDialog().toString()
            if (result.isNotEmpty()) {
                items.add(result)
                adapter.notifyItemInserted(items.size - 1)
                adapter.notifyItemChanged(items.size - 1)
                saveServiceItems(items)
            }
        }
    }

    private fun loadContents() {
        val contents = service?.autoClearContentExclusionList ?: emptyList()
        items.clear()
        items.addAll(contents)
        adapter.notifyDataSetChanged()
    }
}
