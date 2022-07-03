package com.hhvvg.ecm.ui.fragment

import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.ecm.configuration.Configuration
import com.hhvvg.ecm.ui.adapter.WorkModeListAdapter
import com.hhvvg.ecm.ui.base.BaseAppListFragment
import com.hhvvg.ecm.ui.data.AppItem
import com.hhvvg.ecm.util.getSystemExtClipboardService

class WorkModeListFragment : BaseAppListFragment<WorkListItem>() {
    private val service by lazy {
        requireContext().getSystemExtClipboardService()
    }
    private val workMode by lazy {
        service?.autoClearWorkMode ?: Configuration.WORK_MODE_WHITELIST
    }
    private val listByWorkMode: MutableSet<String> by lazy {
        val list = when(workMode) {
            Configuration.WORK_MODE_WHITELIST -> {
                service?.autoClearAppWhitelist
            }
            Configuration.WORK_MODE_BLACKLIST -> {
                service?.autoClearAppBlacklist
            }
            else -> mutableListOf()
        } ?: mutableListOf()
        list.toMutableSet()
    }

    override fun onCreateAppListAdapter(items: MutableList<WorkListItem>): RecyclerView.Adapter<*> {
        return WorkModeListAdapter(items, listByWorkMode)
    }

    override fun onCreateAppItem(appItem: AppItem): WorkListItem {
        return WorkListItem(appItem)
    }

    override fun onDestroy() {
        // Save on exit
        when(workMode) {
            Configuration.WORK_MODE_WHITELIST -> {
                service?.autoClearAppWhitelist = listByWorkMode.toList()
            }
            Configuration.WORK_MODE_BLACKLIST -> {
                service?.autoClearAppBlacklist = listByWorkMode.toList()
            }
        }
        super.onDestroy()
    }

}

class WorkListItem(appItem: AppItem): AppItem(appItem)