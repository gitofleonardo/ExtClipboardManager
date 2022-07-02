package com.hhvvg.ecm.ui.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hhvvg.ecm.R
import com.hhvvg.ecm.ui.viewmodel.ReadStrategyViewModel

class ReadStrategyFragment : Fragment() {

    companion object {
        fun newInstance() = ReadStrategyFragment()
    }

    private lateinit var viewModel: ReadStrategyViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_read_strategy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ReadStrategyViewModel::class.java)
    }

}