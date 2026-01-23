package com.jetsynthesys.rightlife.ui.NewSleepSounds.bottomplaylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.ItemPlaylistBinding
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.Service

class PlaylistAdapter(
    private val list: ArrayList<Service>,

     var currentIndex: Int,

    private val onItemClick: (Int) -> Unit,

    private val onItemRemoved: (Int) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service, position: Int) {
            binding.songTitle.text = service.title
            binding.songSubtitle.text = service.meta.duration.toString() ?: ""
            binding.songSubtitle.text = formatDuration(service.meta?.duration ?: 0)
            Glide.with(binding.root.context)
                .load(ApiClient.CDN_URL_QA + service.image)
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(binding.songImage)

            if (position == currentIndex) {
                binding.playingIcon.visibility = View.VISIBLE
                binding.deleteIcon.visibility = View.GONE
                binding.songTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.blue_bar))
            } else {
                binding.playingIcon.visibility = View.GONE
                binding.deleteIcon.visibility = View.VISIBLE
                binding.songTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.txt_color_journal_date))
                // Add click listener for delete icon
                binding.deleteIcon.setOnClickListener {
                    removeItem(adapterPosition)
                }
            }

            binding.root.setOnClickListener {
                onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(list[position], position)
    }
    fun removeItem(position: Int) {

        /*list.removeAt(position)

        notifyItemRemoved(position)

        notifyItemRangeChanged(position, list.size)*/

        onItemRemoved(position)

    }
    private fun formatDuration(durationInSeconds: Int): String {
        val minutes = durationInSeconds / 60
        val seconds = durationInSeconds % 60
        return String.format("%d:%02d min", minutes, seconds)
    }
}
