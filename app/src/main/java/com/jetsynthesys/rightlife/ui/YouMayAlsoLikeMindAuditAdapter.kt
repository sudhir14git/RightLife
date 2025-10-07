package com.jetsynthesys.rightlife.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.RowYouMayAlsoLikeBinding
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.SeriesListActivity
import com.jetsynthesys.rightlife.ui.mindaudit.Recommendation

class YouMayAlsoLikeMindAuditAdapter(
    private val context: Context,
    private val contentList: List<Recommendation>
) :
    RecyclerView.Adapter<YouMayAlsoLikeMindAuditAdapter.LikeViewHolder>() {

    inner class LikeViewHolder(val binding: RowYouMayAlsoLikeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Recommendation, position: Int) {
            // Load image using Glide
            Glide.with(binding.root.context)
                .load(ApiClient.CDN_URL_QA + (item.thumbnail?.url ?: ""))
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(binding.itemImage)

            binding.itemText.text = item.contentType ?: "Untitled"
            binding.tvTitle.text = item.title
            //binding.tvLeftTime.text = item.leftDuration
            //binding.tvdateTime.text = DateTimeUtils.convertAPIDateMonthFormat(item.date)
            binding.tvName.text = item.categoryName

            binding.imgIconview.setImageResource(
                if ("VIDEO".equals(item.contentType, ignoreCase = true))
                    R.drawable.video_jump_back_in
                else if ("AUDIO".equals(item.contentType, ignoreCase = true))
                    R.drawable.audio_jump_back_in
                else if ("TEXT".equals(item.contentType, ignoreCase = true))
                    R.drawable.ic_text_content
                else
                    R.drawable.series_jump_back_in
            )

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
                    /*context.startActivity(Intent(context, NewSeriesDetailsActivity::class.java).apply {
                            putExtra("seriesId", item.episodeDetails?.contentId)
                            putExtra("episodeId", item.episodeDetails?.id)
                        })*/
                    context.startActivity(
                        Intent(
                            context,
                            SeriesListActivity::class.java
                        ).apply {
                            putExtra("contentId", item.id)
                        })
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        val binding =
            RowYouMayAlsoLikeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return LikeViewHolder(binding)
    }

    override fun getItemCount(): Int = contentList.size

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        holder.bind(contentList[position], position)
    }
}