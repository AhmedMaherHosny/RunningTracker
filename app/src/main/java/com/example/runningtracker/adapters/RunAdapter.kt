package com.example.runningtracker.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningtracker.databinding.ItemRunBinding
import com.example.runningtracker.db.RunEntity
import com.example.runningtracker.other.getFormattedStopWatchTime
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {
    private lateinit var binding: ItemRunBinding

    private val diffCallback = object : DiffUtil.ItemCallback<RunEntity>() {
        override fun areItemsTheSame(oldItem: RunEntity, newItem: RunEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RunEntity, newItem: RunEntity): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    private val differ = AsyncListDiffer(this, diffCallback)
    fun submitList(list: List<RunEntity>) = differ.submitList(list)
    fun getList(): MutableList<RunEntity> = differ.currentList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunAdapter.RunViewHolder {
        binding = ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RunViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunAdapter.RunViewHolder, position: Int) {
        val runEntity = differ.currentList[position]
        holder.bind(runEntity)
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class RunViewHolder(binding: ItemRunBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(runEntity: RunEntity) {
            Glide.with(binding.root).load(runEntity.image).into(binding.ivRunImage)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = runEntity.timeStamp!!
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(calendar.time)
            binding.tvAvgSpeed.text = "${runEntity.avgSpeedInKMH}km/h"
            binding.tvDistance.text = "${runEntity.distanceInMeters!! / 1000f}km"
            binding.tvTime.text = getFormattedStopWatchTime(runEntity.timeInMillis!!)
            binding.tvCalories.text = "${runEntity.caloriesBurned}kcal"
        }
    }
}
