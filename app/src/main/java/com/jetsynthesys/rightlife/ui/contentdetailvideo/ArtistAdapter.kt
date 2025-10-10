package com.jetsynthesys.rightlife.ui.contentdetailvideo

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.ItemHorizontalArtistBinding
import com.jetsynthesys.rightlife.ui.contentdetailvideo.model.Artist
import com.jetsynthesys.rightlife.ui.therledit.ArtistsDetailsActivity
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp

class ArtistAdapter(
    private val context: Context,
    private val artists: List<Artist>
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    inner class ArtistViewHolder(val binding: ItemHorizontalArtistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemHorizontalArtistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.binding.tvArtistname.text = "${artist.firstName ?: ""}"// ${artist.lastName ?: ""}"

        val profileUrl =
            if (artist.profilePicture != null && artist.profilePicture.startsWith("http")) {
                artist.profilePicture
            } else {
                ApiClient.CDN_URL_QA + artist.profilePicture
            }

        GlideApp.with(holder.itemView.context)
            .load(profileUrl)
            .placeholder(R.drawable.rl_profile)
            .error(R.drawable.rl_profile)
            .into(holder.binding.imageProfile)

        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context, ArtistsDetailsActivity::class.java).apply {
                putExtra("ArtistId", artist._id)
            })
        }
    }

    override fun getItemCount(): Int = artists.size
}
