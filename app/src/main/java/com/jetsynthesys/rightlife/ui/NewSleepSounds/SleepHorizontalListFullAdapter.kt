package com.jetsynthesys.rightlife.ui.NewSleepSounds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.ItemHorrizontalSongCardFullBinding
import com.jetsynthesys.rightlife.ui.NewSleepSounds.newsleepmodel.Service
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

class SleepHorizontalListFullAdapter(
    private val soundList: ArrayList<Service>,
    private val type: String = "SleepSound",
    private val onItemClick: (ArrayList<Service>, position: Int) -> Unit,
    private val onAddToPlaylistClick: (Service, position: Int) -> Unit
) : RecyclerView.Adapter<SleepHorizontalListFullAdapter.SoundViewHolder>() {

    inner class SoundViewHolder(val binding: ItemHorrizontalSongCardFullBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.tvItemName.text = service.title
            binding.tvItemTime.text = formatDuration(service.meta.duration)

            // Load image using Glide/Picasso


            Glide.with(binding.itemImage.context)
                .load(ApiClient.CDN_URL_QA + service.image)
                .apply(
                    RequestOptions().transform(
                        CenterCrop(), // Ensures proper cropping first
                        RoundedCorners(30) // Adjust radius as needed
                    )
                )
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(binding.itemImage)

            binding.root.setOnClickListener {
                onItemClick(soundList, adapterPosition)
            }

            if (type == "Playlist") {
                binding.ivAddPlaylist.visibility = View.GONE
            } else {
                binding.ivAddPlaylist.setImageResource(if (service.isActive) R.drawable.ic_added_to_playlist else R.drawable.ic_add_playlist)
            }

            binding.ivAddPlaylist.setOnClickListener {
                val sharedPreferenceManager =
                    SharedPreferenceManager.getInstance(binding.ivAddPlaylist.context)
                /*if (!sharedPreferenceManager.isTooltipShowed("SleepSoundAddButton")) {
                    sharedPreferenceManager.saveTooltip("SleepSoundAddButton", true)
                    binding.ivAddPlaylist.context.showBalloonWithDim(
                        binding.ivAddPlaylist,
                        "Tap to add to your playlist.",
                        "SleepSoundAdd", xOff = -200, yOff = 20, arrowPosition = 0.9f
                    )
                } else {*/
                service.isActive = !service.isActive
                onAddToPlaylistClick(service, adapterPosition) // 👈 Call new lambda
                notifyDataSetChanged()
                //}
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val binding = ItemHorrizontalSongCardFullBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SoundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        holder.bind(soundList[position])
    }

    override fun getItemCount(): Int = soundList.size

    private fun formatDuration(durationInSeconds: Int): String {
        val minutes = durationInSeconds / 60
        val seconds = durationInSeconds % 60
        return String.format("%d:%02d min", minutes, seconds)
    }
}
