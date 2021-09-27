// Copyright 2021, Qualcomm Innovation Center, Inc.
// SPDX-License-Identifier: BSL-1.0
package org.khronos.openxr.runtime_broker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.khronos.openxr.runtime_broker.adapter.InstalledRuntimeAdapter
import org.khronos.openxr.runtime_broker.databinding.FragmentRuntimeChooserBinding
import org.khronos.openxr.runtime_broker.viewmodel.InstalledRuntimeViewModel

@AndroidEntryPoint
class RuntimeChooserFragment : Fragment() {

    private var _binding: FragmentRuntimeChooserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val runtimeViewModel: InstalledRuntimeViewModel by viewModels()

    private lateinit var adapter: InstalledRuntimeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentRuntimeChooserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InstalledRuntimeAdapter()
        adapter.setOnItemClickListener { runtimeInformation, position ->
            val checked = !runtimeInformation.selected
            val checkedPosition = if (checked) position else -1

            for ((index, item) in adapter.currentList.withIndex()) {
                item.selected = (index == checkedPosition)
            }
            adapter.notifyDataSetChanged()

            runtimeViewModel.updateSelection(adapter.currentList[position])
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.runtimeList.layoutManager = layoutManager
        binding.runtimeList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                layoutManager.orientation
            )
        )
        binding.runtimeList.adapter = adapter

        runtimeViewModel.runtimes.observe(viewLifecycleOwner, { runtimes ->
            adapter.submitList(runtimes)
        })
        runtimeViewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        setTitle(getString(R.string.runtime_list_fragment_label))
    }

    private fun setTitle(title: String) {
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_runtime_chooser, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.about -> {
                findNavController().navigate(R.id.action_runtime_list_to_about_libs)
                setTitle(getString(R.string.about))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
