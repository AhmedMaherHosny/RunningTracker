package com.example.runningtracker.other

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import com.example.runningtracker.databinding.MarkerViewBinding
import com.example.runningtracker.db.RunEntity
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("ViewConstructor")
class CustomMarkerView(
    private val runs: List<RunEntity>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {
    private val binding = MarkerViewBinding.inflate(
        c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
        this,
        true
    )

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp!!
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKMH}km/h"
        binding.tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters?.div(1000f)}km"
        binding.tvDistance.text = distanceInKm

        binding.tvDuration.text = run.timeInMillis?.let { getFormattedStopWatchTime(it) }

        val caloriesBurned = "${run.caloriesBurned}kcal"
        binding.tvCaloriesBurned.text = caloriesBurned
    }
}