package com.jetsynthesys.rightlife.ai_package.ui.sleepright.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.BuildConfig
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.ContentList
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter.RecommendationAdapter
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.SeriesListActivity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RecommendedAdapterSleep(val context: Context, private val items: ArrayList<ContentList>) :
    RecyclerView.Adapter<RecommendedAdapterSleep.RecommendationViewHolder>() {

    inner class RecommendationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.titleText)
        val recommended_layout: LinearLayout = view.findViewById(R.id.recommended_layout)
        val tag: TextView = view.findViewById(R.id.topicTag2)
        val tag1: TextView = view.findViewById(R.id.topicTag)
        val author: TextView = view.findViewById(R.id.authorText)
        val author2: TextView = view.findViewById(R.id.authorText2)
        val author4: TextView = view.findViewById(R.id.authorText4)
        val image: ImageView = view.findViewById(R.id.thumbnailImage)
        val moduleNameImage: ImageView = view.findViewById(R.id.moduleNameImage)
        val image_recommended: ImageView = view.findViewById(R.id.image_recommended)
        val overlay: ImageView = view.findViewById(R.id.overlayIcon)
        val viewLine: View = view.findViewById(R.id.view_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recomendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val spannableBuilder = SpannableStringBuilder()
        val item = items[position]
        holder.title.text = item.title
        holder.tag.text = "${item.subCategories.getOrNull(0)?.name}"
        holder.tag1.text = "${item.moduleName}"
        holder.recommended_layout.setOnClickListener {

        }
        val inputTime = item.createdAt.toString()
        val formattedTime = convertUtcTo12HourFormat(inputTime)
        val artist = item.artist.getOrNull(0)
        val hasValidName = !artist?.firstName.isNullOrBlank() && !artist?.lastName.isNullOrBlank()
        val artistName = if (hasValidName) "${artist?.firstName} ${artist?.lastName}" else ""
        //holder.author.text = if (hasValidName) "$artistName   |   $formattedTime" else formattedTime
        val isoDate = item.createdAt
        val formattedDate = isoDate?.let { convertIsoToCustomDate(it) }
        val categoryName = item.categoryName ?: ""

        val categorySpan = SpannableString("$categoryName              |          ")
        categorySpan.setSpan(StyleSpan(Typeface.NORMAL), 0, categoryName.length, 0)
        categorySpan.setSpan(RelativeSizeSpan(14f / 14f), 0, categoryName.length, 0) // 14sp relative to default
        categorySpan.setSpan(ForegroundColorSpan(ContextCompat.getColor(holder.author.context, android.R.color.black)), 0, categoryName.length, 0)
        spannableBuilder.append(categorySpan)

        // Part 2: formattedDate and duration (12sp, dark grey, medium)
        var duration = ""

        if (item.contentType.equals("SERIES")){
            duration = item.episodeCount.toString() + " ep"
        }else{
            duration = item.meta?.duration.toString()
        }
        when (item.contentType) {
            "SERIES" -> {
                duration=  "${item.episodeCount ?: 0} ep"
            }

            "TEXT" -> {
               duration = "${item.readingTime ?: ""} min read"
            }

            else -> {

                duration = item.meta?.duration?.let { formattedDuration(it) }.toString()
            }
        }

       // val dateDurationSpan = SpannableString("$formattedDate              |          $duration")

// Set 12sp size for entire span
       // dateDurationSpan.setSpan(RelativeSizeSpan(12f / 14f), 0, dateDurationSpan.length, 0)

// Set medium weight for entire span
       // dateDurationSpan.setSpan(StyleSpan(Typeface.NORMAL), 0, dateDurationSpan.length, 0) // Medium approximated as normal

// Set dark grey for formattedDate and duration (excluding "|")
       /* val pipeIndex = formattedDate?.length // Index where " | " starts
        if (pipeIndex != null) {
            dateDurationSpan.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(holder.author.context, android.R.color.darker_gray)),
                0,
                pipeIndex,
                0
            )
        }
        if (pipeIndex != null) {
            dateDurationSpan.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(holder.author.context, android.R.color.darker_gray)),
                pipeIndex + 3, // After " | " (3 characters)
                dateDurationSpan.length,
                0
            )
        }

// Set black color for " | "
        if (pipeIndex != null) {
            dateDurationSpan.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(holder.author.context, android.R.color.black)),
                pipeIndex,
                pipeIndex + 3, // " | " length is 3
                0
            )
        }

        spannableBuilder.append(dateDurationSpan)*/

        // Set to TextView
        holder.author.text = categoryName.toString()
        holder.author2.text = formattedDate.toString()
        holder.author4.text = duration
        holder.itemView.setOnClickListener {
            if (item.contentType.equals("TEXT", ignoreCase = true)) {
                context.startActivity(Intent(context, ArticlesDetailActivity::class.java).apply {
                    putExtra("contentId", item.Id)
                })
            } else if (item.contentType
                    .equals("VIDEO", ignoreCase = true) || item.contentType
                    .equals("AUDIO", ignoreCase = true)
            ) {
                context.startActivity(
                    Intent(
                        holder.itemView.context,
                        ContentDetailsActivity::class.java
                    ).apply {
                        putExtra("contentId", item.Id)
                    })
            } else if (item.contentType.equals("SERIES", ignoreCase = true)) {
                context.startActivity(Intent(context, SeriesListActivity::class.java).apply {
                    putExtra("contentId", item.Id)
                })
            }
        }
        Glide.with(context)
            .load( BuildConfig.CDN_URL+item.thumbnail?.url)
            .placeholder(R.drawable.ic_galory)
            .into(holder.image)
        Glide.with(context)
            .load( "")
            .placeholder(R.drawable.ic_db_sleepright)
            .into(holder.moduleNameImage)
        Glide.with(context)
            .load( "")
            .placeholder(R.drawable.sleep_tag)
            .into(holder.image_recommended)
        when(item.contentType) {
            "AUDIO" ->{
                Glide.with(context)
                    .load( R.drawable.music_mini_icon)
                    .placeholder(R.drawable.music_mini_icon)
                    .into(holder.overlay)
            }
            "SERIES" ->{
                //holder.overlay.visibility = View.GONE
                Glide.with(context)
                    .load( R.drawable.book_mini_icon)
                    .placeholder(R.drawable.book_mini_icon)
                    .into(holder.overlay)
            }
            "TEXT" ->{
                Glide.with(context)
                    .load( R.drawable.book_mini_icon)
                    .placeholder(R.drawable.book_mini_icon)
                    .into(holder.overlay)
            }
            "VIDEO"  ->{
                Glide.with(context)
                    .load( R.drawable.play_mini_icon)
                    .placeholder(R.drawable.play_mini_icon)
                    .into(holder.overlay)
            }
            "YOUTUBE"  ->{
                Glide.with(context)
                    .load( R.drawable.video_mini_icon)
                    .placeholder(R.drawable.video_mini_icon)
                    .into(holder.overlay)
            }
        }
        if (position == items.size -1){
            holder.viewLine.visibility = View.GONE
        }else{
            holder.viewLine.visibility = View.VISIBLE
        }
    }
    fun formattedDuration(duration:Int): String {
        val totalSeconds = duration ?: 0
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    fun convertIsoToCustomDate(isoDateString: String): String {
        // Parse ISO 8601 string to LocalDateTime (assuming UTC/Z)
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val dateTime = LocalDateTime.parse(isoDateString, formatter)

        // Convert to desired format (dd MMMM, yyyy)
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy").withZone(ZoneId.of("UTC"))
        return outputFormatter.format(dateTime)
    }
    fun convertUtcTo12HourFormat(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .withZone(ZoneId.of("UTC")) // Input is in UTC
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a")
            .withZone(ZoneId.systemDefault()) // Convert to device local time
        val instant = Instant.parse(input)
        return outputFormatter.format(instant)
    }

    override fun getItemCount(): Int = items.size
}