package com.hhvvg.ecm.ui.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.hhvvg.ecm.R
import com.hhvvg.ecm.databinding.FragmentAutoClearStrategyBinding
import com.hhvvg.ecm.ui.adapter.AutoClearListAdapter
import com.hhvvg.ecm.ui.data.AutoClearAppItem
import com.hhvvg.ecm.ui.viewmodel.AutoClearStrategyViewModel

class AutoClearStrategyFragment : Fragment(), SearchView.OnQueryTextListener {

    companion object {
        fun newInstance() = AutoClearStrategyFragment()
    }

    private lateinit var viewModel: AutoClearStrategyViewModel
    private lateinit var searchView: SearchView
    private var binding: FragmentAutoClearStrategyBinding? = null
    private val items = ArrayList<AutoClearAppItem>()
    private var adapter = AutoClearListAdapter(items)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auto_clear_strategy, container, false)
        binding = FragmentAutoClearStrategyBinding.bind(view)
        binding?.autoClearRecyclerview?.apply {
            adapter = this@AutoClearStrategyFragment.adapter
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
            items.addAll(it)
            adapter.notifyDataSetChanged()
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
