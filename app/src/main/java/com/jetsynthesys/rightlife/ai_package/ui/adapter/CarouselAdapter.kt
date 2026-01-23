package com.jetsynthesys.rightlife.ai_package.ui.adapter

import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.CardItem
import com.jetsynthesys.rightlife.ai_package.ui.moveright.graphs.LineGraphViewWorkout
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CarouselAdapter(
    private val cardItems: List<CardItem>,
    private val onGraphClick: (CardItem, Int) -> Unit
) : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_view_ai, parent, false)
        return CarouselViewHolder(view, onGraphClick)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val item = cardItems[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return cardItems.size
    }

    class CarouselViewHolder(
        itemView: View,
        private val onGraphClick: (CardItem, Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val cardTitle: TextView = itemView.findViewById(R.id.functional_strength_heading)
        private val durationText: TextView = itemView.findViewById(R.id.duration_text)
        private val caloriesText: TextView = itemView.findViewById(R.id.calories_text)
        private val workout_function_icon: ImageView = itemView.findViewById(R.id.workout_function_icon)
        private val avgHeartRateText: TextView = itemView.findViewById(R.id.avg_heart_rate_text_value)
        private val lineGraph: LineGraphViewWorkout = itemView.findViewById(R.id.line_graph_workout)
        private val noDataIcon: ImageView = itemView.findViewById(R.id.no_data_workout_icon)
        private val noDataText: TextView = itemView.findViewById(R.id.no_data_text_workout)
        private val noDataTextWorkoutLoggedManually: TextView = itemView.findViewById(R.id.no_data_text_workout_lpgged_manually)
        private val leftTimeLabel: TextView = itemView.findViewById(R.id.left_time_label)
        private val rightTimeLabel: TextView = itemView.findViewById(R.id.right_time_label)
        private val sourceIcon: ImageView = itemView.findViewById(R.id.sourceIcon)
        private val timeline_line1: View = itemView.findViewById(R.id.timeline_line1)
        private val timeline_line: View = itemView.findViewById(R.id.timeline_line)

        fun bind(item: CardItem, position: Int) {
            cardTitle.text = item.title
            val durationStr = item.duration
            val spannable = if (!durationStr.contains("hr") && durationStr.contains("mins")) {
                SpannableString("0 hr $durationStr")
            } else {
                SpannableString(durationStr)
            }
            val hrIndex = spannable.toString().indexOf("hr")
            val minsIndex = spannable.toString().indexOf("mins")
            if (hrIndex != -1) {
                spannable.setSpan(AbsoluteSizeSpan(10, true), hrIndex, hrIndex + 2, 0) // "hr" 10sp
            }
            if (minsIndex != -1) {
                spannable.setSpan(AbsoluteSizeSpan(10, true), minsIndex, minsIndex + 4, 0) // "mins" 10sp
            }
            durationText.text = spannable

            val caloriesStr =  item.caloriesBurned
            val caloriesSpannable = SpannableString(caloriesStr)
            val calIndex = caloriesStr.indexOf("cal")
            if (calIndex != -1) {
                caloriesSpannable.setSpan(AbsoluteSizeSpan(10, true), calIndex, calIndex + 3, 0) // "cal" 10sp
            }
            caloriesText.text = caloriesSpannable
            avgHeartRateText.text = item.avgHeartRate
            if(item.isSynced){
                sourceIcon.visibility = View.VISIBLE
            }else{
                sourceIcon.visibility = View.GONE
            }
            if (item.heartRateData.isNotEmpty()) {
                lineGraph.visibility = View.VISIBLE
                itemView.findViewById<View>(R.id.timeline_layout).visibility = View.VISIBLE
                itemView.findViewById<View>(R.id.timeline_layout_nodata).visibility = View.GONE
                itemView.findViewById<CardView>(R.id.avg_heartrate_Layout).visibility = View.VISIBLE
                noDataIcon.visibility = View.GONE
                noDataText.visibility = View.GONE
                noDataTextWorkoutLoggedManually.visibility = View.GONE
                val heartRates = item.heartRateData.map { it.heartRate.toFloat() }
                lineGraph.updateData(heartRates)
                leftTimeLabel.text = item.heartRateData.firstOrNull()?.date?.let { convertUtcToSystemLocal(it) } ?: "N/A"
                rightTimeLabel.text = item.heartRateData.lastOrNull()?.date?.let { convertUtcToSystemLocal(it) } ?: "N/A"
                val displayMetrics = itemView.context.resources.displayMetrics
                val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
                val layoutParams = timeline_line.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = if (screenWidthDp < 600) {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.timeline_width_small)
                } else {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.timeline_width_large)
                }
                timeline_line.layoutParams = layoutParams
            } else {
                lineGraph.visibility = View.GONE
                itemView.findViewById<View>(R.id.timeline_layout).visibility = View.GONE
                itemView.findViewById<View>(R.id.timeline_layout_nodata).visibility = View.VISIBLE
                itemView.findViewById<CardView>(R.id.avg_heartrate_Layout).visibility = View.GONE

                noDataIcon.visibility = View.VISIBLE
                noDataText.visibility = View.VISIBLE
                noDataTextWorkoutLoggedManually.visibility = View.VISIBLE
                val displayMetrics = itemView.context.resources.displayMetrics
                val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
                val layoutParams = timeline_line1.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = if (screenWidthDp < 600) {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.timeline_width_small)
                } else {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.timeline_width_large)
                }
                timeline_line1.layoutParams = layoutParams

            }
            when (item?.title) {
                "American Football" -> {
                    workout_function_icon.setImageResource(R.drawable.american_football)// Handle American Football
                }
                "Archery" -> {
                    workout_function_icon.setImageResource(R.drawable.archery)
                }
                "Athletics" -> {
                    workout_function_icon.setImageResource(R.drawable.athelete_search)
                }
                "Australian Football" -> {
                    workout_function_icon.setImageResource(R.drawable.australian_football)
                }
                "Badminton" -> {
                    workout_function_icon.setImageResource(R.drawable.badminton)
                }
                "Barre" -> {
                    workout_function_icon.setImageResource(R.drawable.barre)
                }
                "Baseball" -> {
                    workout_function_icon.setImageResource(R.drawable.baseball)
                }
                "Basketball" -> {
                    workout_function_icon.setImageResource(R.drawable.basketball)
                }
                "Boxing" -> {
                    workout_function_icon.setImageResource(R.drawable.boxing)
                }
                "Climbing" -> {
                    workout_function_icon.setImageResource(R.drawable.climbing)
                }
                "Core Training" -> {
                    workout_function_icon.setImageResource(R.drawable.core_training)
                }
                "Cycling" -> {
                    workout_function_icon.setImageResource(R.drawable.cycling)
                }
                "Cricket" -> {
                    workout_function_icon.setImageResource(R.drawable.cricket)
                }
                "Cross Training" -> {
                    workout_function_icon.setImageResource(R.drawable.cross_training)
                }
                "Dance" -> {
                    workout_function_icon.setImageResource(R.drawable.dance)
                }
                "Disc Sports" -> {
                    workout_function_icon.setImageResource(R.drawable.disc_sports)
                }
                "Elliptical" -> {
                    workout_function_icon.setImageResource(R.drawable.elliptical)
                }
                "Football" -> {
                    workout_function_icon.setImageResource(R.drawable.football)
                }
                "Functional Strength Training" -> {
                    workout_function_icon.setImageResource(R.drawable.functional_strength_training)
                }
                "Golf" -> {
                    workout_function_icon.setImageResource(R.drawable.golf)
                }
                "Gymnastics" -> {
                    workout_function_icon.setImageResource(R.drawable.gymnastics)
                }
                "Handball" -> {
                    workout_function_icon.setImageResource(R.drawable.handball)
                }
                "Hiking" -> {
                    workout_function_icon.setImageResource(R.drawable.hockey)
                }
                "Hockey" -> {
                    workout_function_icon.setImageResource(R.drawable.hiit)
                }
                "HIIT" -> {
                    workout_function_icon.setImageResource(R.drawable.hiking)
                }
                "High Intensity Interval Training" -> {
                    workout_function_icon.setImageResource(R.drawable.hiking)
                }
                "Kickboxing" -> {
                    workout_function_icon.setImageResource(R.drawable.kickboxing)
                }
                "Martial Arts" -> {
                    workout_function_icon.setImageResource(R.drawable.martial_arts)
                }
                "Other" -> {
                    workout_function_icon.setImageResource(R.drawable.other)
                }
                "Pickleball" -> {
                    workout_function_icon.setImageResource(R.drawable.pickleball)
                }
                "Pilates" -> {
                    workout_function_icon.setImageResource(R.drawable.pilates)
                }
                "Power Yoga" -> {
                    workout_function_icon.setImageResource(R.drawable.power_yoga)
                }
                "Powerlifting" -> {
                    workout_function_icon.setImageResource(R.drawable.powerlifting)
                }
                "Pranayama" -> {
                    workout_function_icon.setImageResource(R.drawable.pranayama)
                }
                "Running" -> {
                    workout_function_icon.setImageResource(R.drawable.running)
                }
                "Rowing Machine" -> {
                    workout_function_icon.setImageResource(R.drawable.rowing_machine)
                }
                "Rugby" -> {
                    workout_function_icon.setImageResource(R.drawable.rugby)
                }
                "Skating" -> {
                    workout_function_icon.setImageResource(R.drawable.skating)
                }
                "Skipping" -> {
                    workout_function_icon.setImageResource(R.drawable.skipping)
                }
                "Stairs" -> {
                    workout_function_icon.setImageResource(R.drawable.stairs)
                }
                "Squash" -> {
                    workout_function_icon.setImageResource(R.drawable.squash)
                }
                "Traditional Strength Training" -> {
                    workout_function_icon.setImageResource(R.drawable.traditional_strength_training)
                }
                "Strength Training" -> {
                    workout_function_icon.setImageResource(R.drawable.traditional_strength_training)
                }
                "Stretching" -> {
                    workout_function_icon.setImageResource(R.drawable.stretching)
                }
                "Swimming" -> {
                    workout_function_icon.setImageResource(R.drawable.swimming)
                }
                "Table Tennis" -> {
                    workout_function_icon.setImageResource(R.drawable.table_tennis)
                }
                "Tennis" -> {
                    workout_function_icon.setImageResource(R.drawable.tennis)
                }
                "Track and Field Events" -> {
                    workout_function_icon.setImageResource(R.drawable.track_field_events)
                }
                "Volleyball" -> {
                    workout_function_icon.setImageResource(R.drawable.volleyball)
                }
                "Walking" -> {
                    workout_function_icon.setImageResource(R.drawable.walking)
                }
                "Watersports" -> {
                    workout_function_icon.setImageResource(R.drawable.watersports)
                }
                "Wrestling" -> {
                    workout_function_icon.setImageResource(R.drawable.wrestling)
                }
                "Yoga" -> {
                    workout_function_icon.setImageResource(R.drawable.yoga)
                }
                else -> {
                    workout_function_icon.setImageResource(R.drawable.other)
                }
            }

            lineGraph.setOnClickListener {
                onGraphClick(item, position)
            }
        }

        private fun convertUtcToSystemLocal(utcTime: String): String {
            return try {
                val utcDateTime = ZonedDateTime.parse(utcTime)
                val localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault())
                localDateTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())).lowercase()
            } catch (e: Exception) {
                "N/A"
            }
        }


    }
}