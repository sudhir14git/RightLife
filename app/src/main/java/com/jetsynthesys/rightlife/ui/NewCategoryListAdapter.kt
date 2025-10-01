package com.jetsynthesys.rightlife.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.RowCategoryListNewBinding
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.NewSeriesDetailsActivity
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils

class NewCategoryListAdapter(
    private val context: Context,
    private val contentDetails: MutableList<CategoryListItem>,
    val onBookMarkedClick: (CategoryListItem, Int) -> Unit
) :
    RecyclerView.Adapter<NewCategoryListAdapter.CategoryListViewHolder>() {

    inner class CategoryListViewHolder(val binding: RowCategoryListNewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryListItem, position: Int) {
            // Load image using Glide
            Glide.with(binding.root.context)
                .load(ApiClient.CDN_URL_QA + (item.thumbnail?.url ?: ""))
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(binding.itemImage)

            binding.itemText.text = item.contentType ?: "Untitled"
            binding.tvTitle.text = item.title
            binding.tvLeftTime.text = item.leftDuration
            binding.tvdateTime.text = DateTimeUtils.convertAPIDateMonthFormat(item.date)
            binding.tvName.text = item.categoryName

            binding.imgSave.setImageResource(if (item.isBookmarked) R.drawable.save_jump_in_back else R.drawable.unsave_jump_in_back)

            binding.imgIconview.setImageResource(
                if ("VIDEO".equals(item.contentType, ignoreCase = true))
                    R.drawable.video_jump_back_in
                else if ("AUDIO".equals(item.contentType, ignoreCase = true))
                    R.drawable.audio_jump_back_in
                else
                    R.drawable.series_jump_back_in
            )

            // Calculate progress
            if ("SERIES".equals(item.contentType, ignoreCase = true)) {
                val progress = item.leftDuration?.let { calculateProgress(it) } ?: 0
                binding.progressBar.progress = progress
                binding.imgCompleteTick.visibility =
                    if (progress == 100) View.VISIBLE else View.GONE
            } else {

                val duration = item.meta?.duration ?: 0 // total duration in seconds
                val left = item.leftDurationINT ?: 0
                val progress = if (left == duration) 100
                else if (duration > 0) {
                    val completed = (duration - left).coerceAtLeast(0)
                    ((completed.toFloat() / duration) * 100).toInt().coerceIn(0, 100)
                } else
                    0
                binding.progressBar.progress = progress
                binding.imgCompleteTick.visibility =
                    if (left == duration) View.VISIBLE else View.GONE
            }

            binding.imgSave.setOnClickListener {
                onBookMarkedClick(item, position)
            }

            // Click listener
            binding.root.setOnClickListener {
                if (item.contentType.equals(
                        "TEXT",
                        ignoreCase = true
                    ) || item.contentType.equals("Articles", ignoreCase = true)
                ) {
                    context.startActivity(
                        Intent(
                            context,
                            ArticlesDetailActivity::class.java
                        ).apply {
                            putExtra("contentId", item.id)
                        })
                } else if (item.contentType.equals(
                        "VIDEO",
                        ignoreCase = true
                    ) || item.contentType
                        .equals("AUDIO", ignoreCase = true)
                ) {
                    context.startActivity(
                        Intent(
                            context,
                            ContentDetailsActivity::class.java
                        ).apply {
                            putExtra("contentId", item.id)
                        })
                } else if (item.contentType.equals("SERIES", ignoreCase = true)) {
                    context.startActivity(
                        Intent(
                            context,
                            NewSeriesDetailsActivity::class.java
                        ).apply {
                            putExtra("seriesId", item.episodeDetails?.contentId)
                            putExtra("episodeId", item.episodeDetails?.id)
                        })
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryListViewHolder {
        val binding =
            RowCategoryListNewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return CategoryListViewHolder(binding)
    }

    override fun getItemCount(): Int = contentDetails.size

    override fun onBindViewHolder(holder: CategoryListViewHolder, position: Int) {
        holder.bind(contentDetails[position], position)
    }

    private fun calculateProgress(value: String): Int {
        return try {
            val parts = value.split("/")
            if (parts.size == 2) {
                val completed = parts[0].trim().toFloatOrNull() ?: 0f
                val total = parts[1].trim().toFloatOrNull() ?: 0f
                if (total > 0) {
                    ((completed / total) * 100).toInt().coerceIn(0, 100)
                } else 0
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

}