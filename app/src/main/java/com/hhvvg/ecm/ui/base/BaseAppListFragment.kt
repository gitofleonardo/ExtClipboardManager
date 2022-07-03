package com.hhvvg.ecm.ui.base

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.ecm.R
import com.hhvvg.ecm.databinding.FragmentBaseAppListBinding
import com.hhvvg.ecm.ui.data.AppItem
import com.hhvvg.ecm.ui.viewmodel.AutoClearStrategyViewModel

abstract class BaseAppListFragment<T : AppItem> : Fragment(), SearchView.OnQueryTextListener {

    abstract fun onCreateAppListAdapter(items: MutableList<T>): RecyclerView.Adapter<*>
    abstract fun onCreateAppItem(appItem: AppItem): T

    private lateinit var viewModel: AutoClearStrategyViewModel
    private lateinit var searchView: SearchView
    private var binding: FragmentBaseAppListBinding? = null
    private val items = ArrayList<T>()
    private val adapter by lazy {
        onCreateAppListAdapter(items)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_base_app_list, container, false)
        binding = FragmentBaseAppListBinding.bind(view)
        binding?.autoClearRecyclerview?.apply {
            adapter = this@BaseAppListFragment.adapter
        }
        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AutoClearStrategyViewModel::class.java)
        viewModel.appItems.observe(viewLifecycleOwner) {
            items.clear()
            items.addAll(it.map { item -> onCreateAppItem(item) })
            adapter.notifyDataSetChanged()

            binding?.apply {
                autoClearRecyclerview.isVisible = true
                progressBar.isVisible = false
            }
        }
        viewModel.loadApps(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.auto_clear_menu, menu)
        searchView = menu.findItem(R.id.filter_text).actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_system_packages -> {
                val showSystemPackages = !item.isChecked
                item.isChecked = showSystemPackages
                viewModel.filter(showSystemPackages)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val text = newText ?: return false
        viewModel.filter(filterText = text)
        return true
    }
}
