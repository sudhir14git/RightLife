package com.jetsynthesys.rightlife.ui.breathwork

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.RowBreathworkContextBinding

class BreathWorkContextPagerAdapter(
    private val context: Context,
    private val image: String,
    private val textColor: Int,
    private val bgColor: Int,
    private val heading: List<String>,
    private val description: List<String>
) : RecyclerView.Adapter<BreathWorkContextPagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: RowBreathworkContextBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = RowBreathworkContextBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int = heading.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        with(holder.binding) {
            Glide.with(context)
                .load(ApiClient.CDN_URL_QA + image)
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(imageSlider)

            tvHeader.text = heading[position]
            tvDescription.text = description[position]

            try {
                tvHeader.setTextColor(textColor)
                tvDescription.setTextColor(textColor)
                llRowBreathWorkContext.backgroundTintList =
                    ColorStateList.valueOf(bgColor)
            } catch (e: IllegalArgumentException) {
                e.message?.let { Log.e("Error", it) }
            }
        }
    }
}
