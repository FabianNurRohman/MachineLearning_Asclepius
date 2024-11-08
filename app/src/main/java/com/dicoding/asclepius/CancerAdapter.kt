package com.dicoding.asclepius

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.database.Cancer
import com.dicoding.asclepius.databinding.ItemCancerBinding

class CancerAdapter : ListAdapter<Cancer, CancerAdapter.CancerViewHolder>(CancerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancerViewHolder {
        val binding = ItemCancerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CancerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CancerViewHolder, position: Int) {
        val cancer = getItem(position)
        holder.bind(cancer)
    }

    class CancerViewHolder(private val binding: ItemCancerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cancer: Cancer) {
            binding.eventName.text = cancer.event_name
            binding.eventOwner.text = "Score: ${cancer.event_owner}"
            if (cancer.mediaCover != null) {
                Glide.with(binding.root.context)
                    .load(cancer.mediaCover)
                    .into(binding.imageView)
            }
        }
    }

    class CancerDiffCallback : DiffUtil.ItemCallback<Cancer>() {
        override fun areItemsTheSame(oldItem: Cancer, newItem: Cancer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cancer, newItem: Cancer): Boolean {
            return oldItem == newItem
        }
    }
}
