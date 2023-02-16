package com.example.runningtracker.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runningtracker.R
import com.example.runningtracker.adapters.RunAdapter
import com.example.runningtracker.databinding.FragmentRunBinding
import com.example.runningtracker.other.*
import com.example.runningtracker.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentRunBinding
    private lateinit var runAdapter: RunAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRunBinding.bind(view)

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
        requestPermission()
    }

    /********************************Location foreground*************************************/
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = !permissions.values.any { !it }
            if (allGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    requestBackGroundPermission()
            } else {
//                TODO("dialog to settings here")
            }
        }

    private fun requestPermission() {
        when {
            hasLocationPermissions(requireContext()) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    requestBackGroundPermission()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) -> {
                val title = "Warning"
                val msg = "Location access is required to see the maps!"
                createDialog(
                    requireContext(), title, msg, "Ok",
                    { p0, p1 -> requestPermissionLauncher.launch(Constant.LOCATION_PERMISSIONS) },
                    "Close", { p0, p1 -> p0.dismiss() }
                )
            }
            else -> {
                requestPermissionLauncher.launch(Constant.LOCATION_PERMISSIONS)
            }
        }

    }

    /********************************Location foreground*************************************/


    private val requestBackgroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                allPermissionsGranted()
            } else {
//                TODO("dialog to settings here")
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackGroundPermission() {
        when {
            hasBackGroundLocationPermission(requireContext()) -> {
                allPermissionsGranted()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ) -> {
                val title = "Warning"
                val msg = "Location background access is required to track the run!"
                createDialog(
                    requireContext(), title, msg, "Ok",
                    { p0, p1 -> requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) },
                    "Close", { p0, p1 -> p0.dismiss() }
                )
            }
            else -> {
                requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private fun allPermissionsGranted() {
        setupRecyclerView()
        swipeToDelete()
        subscribeToObservers()
    }

    private fun swipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val changedList = runAdapter.getList().toMutableList()
                val runItem = changedList[pos]
                changedList.removeAt(pos)
                runAdapter.submitList(changedList.toList())
                Snackbar.make(binding.rvRuns, "Run is deleted", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        changedList.add(pos, runItem)
                        runAdapter.submitList(changedList.toList())
                    }
                    addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            if (event != DISMISS_EVENT_ACTION) {
                                viewModel.deleteRun(runItem)
                            }
                        }
                    })
                }.show()
            }
        }).attachToRecyclerView(binding.rvRuns)
    }

    private fun setupRecyclerView() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(activity)
    }

    private fun subscribeToObservers() {
        when (viewModel.sortType) {
            SortType.DATE -> binding.spFilter.setSelection(0)
            SortType.RUNNING_TIME -> binding.spFilter.setSelection(1)
            SortType.DISTANCE -> binding.spFilter.setSelection(2)
            SortType.AVG_SPEED -> binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> binding.spFilter.setSelection(4)
        }
        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        viewModel.runs.observe(viewLifecycleOwner) {
            runAdapter.submitList(it)
        }
    }
}