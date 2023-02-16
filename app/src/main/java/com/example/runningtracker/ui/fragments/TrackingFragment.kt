package com.example.runningtracker.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentTrackingBinding
import com.example.runningtracker.db.RunEntity
import com.example.runningtracker.other.Constant.ACTION_PAUSE_SERVICE
import com.example.runningtracker.other.Constant.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracker.other.Constant.ACTION_STOP_SERVICE
import com.example.runningtracker.other.Constant.CANCEL_TRACKING_DIALOG_TAG
import com.example.runningtracker.other.Constant.MAP_ZOOM
import com.example.runningtracker.other.Constant.POLYLINE_COLOR
import com.example.runningtracker.other.Constant.POLYLINE_WIDTH
import com.example.runningtracker.other.calculatePolylineLength
import com.example.runningtracker.other.createDialog
import com.example.runningtracker.other.getFormattedStopWatchTime
import com.example.runningtracker.services.Polyline
import com.example.runningtracker.services.TrackingService
import com.example.runningtracker.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking), MenuProvider {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    private var menu: Menu? = null

    @set:Inject
    var weight = 65f

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrackingBinding.bind(view)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        requestPermission()
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG
            ) as CancelTrackingDialog
            cancelTrackingDialog.setYesListener {
                stopRun()
            }
        }
        subscribeToObservers()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {

            } else {

            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            )
                    == PackageManager.PERMISSION_GRANTED -> {

            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) -> {
                val title = "Warning"
                val msg = "Push notification is required to see the tracking in small way"
                createDialog(
                    requireContext(), title, msg, "Ok",
                    { p0, p1 -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    "Close", { p0, p1 -> p0.dismiss() }
                )
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions().apply {
                color(POLYLINE_COLOR)
                width(POLYLINE_WIDTH)
                add(preLastLatLng)
                add(lastLatLng)
            }
            map?.addPolyline(polylineOptions)
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                ((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10).roundToInt() / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = RunEntity(
                image = bmp,
                timeStamp = dateTimestamp,
                avgSpeedInKMH = avgSpeed,
                distanceInMeters = distanceInMeters,
                timeInMillis = curTimeInMillis,
                caloriesBurned = caloriesBurned
            )
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions().apply {
                color(POLYLINE_COLOR)
                width(POLYLINE_WIDTH)
                addAll(polyline)
            }
            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.isVisible = false
        } else if (isTracking) {
            binding.btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.isVisible = true
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            curTimeInMillis = it
            val formattedTime = getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()

    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return true
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        if (curTimeInMillis > 0L)
            this.menu?.getItem(0)?.isVisible = true
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    @SuppressLint("SetTextI18n")
    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        binding.tvTimer.text = "00:00:00:00"
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

}