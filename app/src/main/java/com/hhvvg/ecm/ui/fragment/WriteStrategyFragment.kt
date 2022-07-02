package com.hhvvg.ecm.ui.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hhvvg.ecm.R
import com.hhvvg.ecm.ui.viewmodel.WriteStrategyViewModel

class WriteStrategyFragment : Fragment() {

    companion object {
        fun newInstance() = WriteStrategyFragment()
    }

    private lateinit var viewModel: WriteStrategyViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_write_strategy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(WriteStrategyViewModel::class.java)
    }

}