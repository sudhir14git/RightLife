package com.jetsynthesys.rightlife.ui.healthcam

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.newreportfacescan.Recommendation
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.SeriesListActivity
import com.jetsynthesys.rightlife.ui.utility.Utils

// Import your Utils class
class HealthCamRecommendationAdapter(
    private val context: Context,
    private val recommendations: List<Recommendation>
) : RecyclerView.Adapter<HealthCamRecommendationAdapter.RecommendationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.health_cam_recommendation_item, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = recommendations[position]

        holder.titleTextView.text = recommendation.title
        holder.categoryTextView.text = recommendation.categoryName

        if (recommendation.artist != null && recommendation.artist.isNotEmpty()) {
            val artistName =
                recommendation.artist[0].firstName + " " + recommendation.artist[0].lastName
            holder.artistTextView.text = artistName
        } else {
            holder.artistTextView.text = ""
        }

        if (recommendation.thumbnail != null && recommendation.thumbnail.url != null && recommendation.thumbnail.url.isNotEmpty()) {
            Glide.with(context)
                .load(ApiClient.CDN_URL_QA + recommendation.thumbnail.url)
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .transform(RoundedCorners(25))
                .into(holder.thumbnailImageView)
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.image1_rlpage_edit) // Default image
        }

        holder.moduleNameTextView.text = Utils.getModuleText(recommendation.moduleId)
        val startDrawable = getModuleTypeDrawable(context, recommendation.moduleId)
        startDrawable!!.setBounds(0, 0, startDrawable.intrinsicWidth, startDrawable.intrinsicHeight)
        holder.moduleNameTextView.setCompoundDrawables(startDrawable, null, null, null)

        val text = if ("SERIES" == recommendation.contentType) {
            recommendation.episodeCount.toString() + "  videos" // " videos/audios";
        } else {
            "" //contentList.get(position).getLeftDuration() + " left";
        }

        /* if ("SERIES".equals(recommendation.seriesType)) {
            text = recommendation.episodeCount + " episodes";
        } else {
            // You might need a way to determine "left duration" for other content types
            text = "";
        }*/
        holder.timeLeftTextView.text = text

        holder.itemView.setOnClickListener {
            val contentType = recommendation.contentType
            val contentId = recommendation.id
            if (contentType.equals("TEXT", ignoreCase = true)) {
                context.startActivity(Intent(context, ArticlesDetailActivity::class.java).apply {
                    putExtra("contentId", contentId)
                })
            } else if (contentType
                    .equals("VIDEO", ignoreCase = true) || contentType
                    .equals("AUDIO", ignoreCase = true)
            ) {
                context.startActivity(
                    Intent(
                        holder.itemView.context,
                        ContentDetailsActivity::class.java
                    ).apply {
                        putExtra("contentId", contentId)
                    })
            } else if (contentType.equals("SERIES", ignoreCase = true)) {
                context.startActivity(Intent(context, SeriesListActivity::class.java).apply {
                    putExtra("contentId", contentId)
                })
            }
        }
    }

    private fun getModuleTypeDrawable(ctx: Context, moduleName: String): Drawable? {
        val startDrawable = ContextCompat.getDrawable(ctx, R.drawable.rlpage_thinkright_svg)
        if ("THINK_RIGHT".equals(moduleName, ignoreCase = true)) {
            return ContextCompat.getDrawable(ctx, R.drawable.rlpage_thinkright_svg)
        } else if ("SLEEP_RIGHT".equals(moduleName, ignoreCase = true)) {
            return ContextCompat.getDrawable(ctx, R.drawable.rlpage_sleepright_svg)
        } else if ("MOVE_RIGHT".equals(moduleName, ignoreCase = true)) {
            return ContextCompat.getDrawable(ctx, R.drawable.rlpage_moveright_svg)
        } else if ("EAT_RIGHT".equals(moduleName, ignoreCase = true)) {
            return ContextCompat.getDrawable(ctx, R.drawable.rlpage_eatright_svg)
        }
        return startDrawable
    }

    override fun getItemCount(): Int {
        return recommendations.size
    }

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thumbnailImageView: ImageView = itemView.findViewById(R.id.rlimg1)
        var titleTextView: TextView = itemView.findViewById(R.id.item_text)
        var categoryTextView: TextView = itemView.findViewById(R.id.tv_category)
        var artistTextView: TextView = itemView.findViewById(R.id.tv_artistname)
        var timeLeftTextView: TextView = itemView.findViewById(R.id.tvTimeLeft)
        var moduleNameTextView: TextView = itemView.findViewById(R.id.tv_modulename)
    }
}
