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
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.CardItem
import com.jetsynthesys.rightlife.ai_package.ui.moveright.graphs.LineGraphViewWorkout
import java.text.SimpleDateFormat
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
            //durationText.text = item.duration
            val durationStr = item.duration
            val spannable = if (!durationStr.contains("hr") && durationStr.contains("mins")) {
                // If only minutes are present, prepend "0 hr "
                SpannableString("0 hr $durationStr")
            } else {
                // Use original string if it contains "hr" or other cases
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
// Rest of the text (numbers) will remain 19sp (default or set in XML)
            durationText.text = spannable

           // caloriesText.text = item.caloriesBurned
            val caloriesStr =  item.caloriesBurned// e.g., "305 cal"
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
                    itemView.context.resources.getDimensionPixelSize(R.dimen.timeline_width_small) // 50dp (reused dimen)
                } else {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.timeline_width_large) // 56dp (reused dimen)
                }
                timeline_line.layoutParams = layoutParams
                /*when (item?.title) {
                    "American Football" -> {
                        workout_function_icon.setImageResource(R.drawable.american_football)// Handle American Football
                    }
                    "Archery" -> {
                        // Handle Archery
                        workout_function_icon.setImageResource(R.drawable.archery)
                    }
                    "Athletics" -> {
                        // Handle Athletics
                        workout_function_icon.setImageResource(R.drawable.athelete_search)
                    }
                    "Australian Football" -> {
                        // Handle Australian Football
                        workout_function_icon.setImageResource(R.drawable.australian_football)
                    }
                    "Badminton" -> {
                        // Handle Badminton
                        workout_function_icon.setImageResource(R.drawable.badminton)
                    }
                    "Barre" -> {
                        // Handle Barre
                        workout_function_icon.setImageResource(R.drawable.barre)
                    }
                    "Baseball" -> {
                        // Handle Baseball
                        workout_function_icon.setImageResource(R.drawable.baseball)
                    }
                    "Basketball" -> {
                        // Handle Basketball
                        workout_function_icon.setImageResource(R.drawable.basketball)
                    }
                    "Boxing" -> {
                        // Handle Boxing
                        workout_function_icon.setImageResource(R.drawable.boxing)

                    }
                    "Climbing" -> {
                        // Handle Climbing
                        workout_function_icon.setImageResource(R.drawable.climbing)
                    }
                    "Core Training" -> {
                        // Handle Core Training
                        workout_function_icon.setImageResource(R.drawable.core_training)
                    }
                    "Cycling" -> {
                        // Handle Cycling
                        workout_function_icon.setImageResource(R.drawable.cycling)
                    }
                    "Cricket" -> {
                        // Handle Cricket
                        workout_function_icon.setImageResource(R.drawable.cricket)
                    }
                    "Cross Training" -> {
                        // Handle Cross Training
                        workout_function_icon.setImageResource(R.drawable.cross_training)
                    }
                    "Dance" -> {
                        // Handle Dance
                        workout_function_icon.setImageResource(R.drawable.dance)
                    }
                    "Disc Sports" -> {
                        // Handle Disc Sports
                        workout_function_icon.setImageResource(R.drawable.disc_sports)
                    }
                    "Elliptical" -> {
                        // Handle Elliptical
                        workout_function_icon.setImageResource(R.drawable.elliptical)
                    }
                    "Football" -> {
                        // Handle Football
                        workout_function_icon.setImageResource(R.drawable.football)
                    }
                    "Functional Strength Training" -> {
                        // Handle Functional Strength Training
                        workout_function_icon.setImageResource(R.drawable.functional_strength_training)
                    }
                    "Golf" -> {
                        // Handle Golf
                        workout_function_icon.setImageResource(R.drawable.golf)
                    }
                    "Gymnastics" -> {
                        // Handle Gymnastics
                        workout_function_icon.setImageResource(R.drawable.gymnastics)
                    }
                    "Handball" -> {
                        // Handle Handball
                        workout_function_icon.setImageResource(R.drawable.handball)
                    }
                    "Hiking" -> {
                        // Handle Hiking
                        workout_function_icon.setImageResource(R.drawable.hiking)
                    }
                    "Hockey" -> {
                        // Handle Hockey
                        workout_function_icon.setImageResource(R.drawable.hockey)
                    }
                    "HIIT" -> {
                        // Handle HIIT
                        workout_function_icon.setImageResource(R.drawable.hiit)
                    }
                    "Kickboxing" -> {
                        // Handle Kickboxing
                        workout_function_icon.setImageResource(R.drawable.kickboxing)
                    }
                    "Martial Arts" -> {
                        // Handle Martial Arts
                        workout_function_icon.setImageResource(R.drawable.martial_arts)
                    }
                    "Other" -> {
                        // Handle Other
                        workout_function_icon.setImageResource(R.drawable.other)
                    }
                    "Pickleball" -> {
                        // Handle Pickleball
                        workout_function_icon.setImageResource(R.drawable.pickleball)
                    }
                    "Pilates" -> {
                        // Handle Pilates
                        workout_function_icon.setImageResource(R.drawable.pilates)
                    }
                    "Power Yoga" -> {
                        // Handle Power Yoga
                        workout_function_icon.setImageResource(R.drawable.power_yoga)
                    }
                    "Powerlifting" -> {
                        // Handle Powerlifting
                        workout_function_icon.setImageResource(R.drawable.powerlifting)
                    }
                    "Pranayama" -> {
                        // Handle Pranayama
                        workout_function_icon.setImageResource(R.drawable.pranayama)
                    }
                    "Running" -> {
                        // Handle Running
                        workout_function_icon.setImageResource(R.drawable.running)
                    }
                    "Rowing Machine" -> {
                        // Handle Rowing Machine
                        workout_function_icon.setImageResource(R.drawable.rowing_machine)
                    }
                    "Rugby" -> {
                        // Handle Rugby
                        workout_function_icon.setImageResource(R.drawable.rugby)
                    }
                    "Skating" -> {
                        // Handle Skating
                        workout_function_icon.setImageResource(R.drawable.skating)
                    }
                    "Skipping" -> {
                        // Handle Skipping
                        workout_function_icon.setImageResource(R.drawable.skipping)
                    }
                    "Stairs" -> {
                        // Handle Stairs
                        workout_function_icon.setImageResource(R.drawable.stairs)
                    }
                    "Squash" -> {
                        // Handle Squash
                        workout_function_icon.setImageResource(R.drawable.squash)
                    }
                    "Traditional Strength Training" -> {
                        // Handle Traditional Strength Training
                        workout_function_icon.setImageResource(R.drawable.traditional_strength_training)
                    }
                    "Stretching" -> {
                        // Handle Stretching
                        workout_function_icon.setImageResource(R.drawable.stretching)
                    }
                    "Swimming" -> {
                        // Handle Swimming
                        workout_function_icon.setImageResource(R.drawable.swimming)
                    }
                    "Table Tennis" -> {
                        // Handle Table Tennis
                        workout_function_icon.setImageResource(R.drawable.table_tennis)
                    }
                    "Tennis" -> {
                        // Handle Tennis
                        workout_function_icon.setImageResource(R.drawable.tennis)
                    }
                    "Track and Field Events" -> {
                        // Handle Track and Field Events
                        workout_function_icon.setImageResource(R.drawable.track_field_events)
                    }
                    "Volleyball" -> {
                        // Handle Volleyball
                        workout_function_icon.setImageResource(R.drawable.volleyball)
                    }
                    "Walking" -> {
                        // Handle Walking
                        workout_function_icon.setImageResource(R.drawable.walking)
                    }
                    "Watersports" -> {
                        // Handle Watersports
                        workout_function_icon.setImageResource(R.drawable.watersports)
                    }
                    "Wrestling" -> {
                        // Handle Wrestling
                        workout_function_icon.setImageResource(R.drawable.wrestling)
                    }
                    "Yoga" -> {
                        // Handle Yoga
                        workout_function_icon.setImageResource(R.drawable.yoga)
                    }
                    else -> {
                        // Handle unknown or null workoutType
                        workout_function_icon.setImageResource(R.drawable.other)
                    }
                }*/
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
                    itemView.context.resources.getDimensionPixelSize(R.dimen.timeline_width_small) // 50dp (reused dimen)
                } else {
                    itemView.context.resources.getDimensionPixelSize(R.dimen.timeline_width_large) // 56dp (reused dimen)
                }
                timeline_line1.layoutParams = layoutParams

                /*Glide.with(itemView.context)
                    .load(item.icon)
                    .placeholder(R.drawable.workout_function_icon)
                    .into(workout_function_icon)*/
            }
            when (item?.title) {
                "American Football" -> {
                    workout_function_icon.setImageResource(R.drawable.american_football)// Handle American Football
                }
                "Archery" -> {
                    // Handle Archery
                    workout_function_icon.setImageResource(R.drawable.archery)
                }
                "Athletics" -> {
                    // Handle Athletics
                    workout_function_icon.setImageResource(R.drawable.athelete_search)
                }
                "Australian Football" -> {
                    // Handle Australian Football
                    workout_function_icon.setImageResource(R.drawable.australian_football)
                }
                "Badminton" -> {
                    // Handle Badminton
                    workout_function_icon.setImageResource(R.drawable.badminton)
                }
                "Barre" -> {
                    // Handle Barre
                    workout_function_icon.setImageResource(R.drawable.barre)
                }
                "Baseball" -> {
                    // Handle Baseball
                    workout_function_icon.setImageResource(R.drawable.baseball)
                }
                "Basketball" -> {
                    // Handle Basketball
                    workout_function_icon.setImageResource(R.drawable.basketball)
                }
                "Boxing" -> {
                    // Handle Boxing
                    workout_function_icon.setImageResource(R.drawable.boxing)

                }
                "Climbing" -> {
                    // Handle Climbing
                    workout_function_icon.setImageResource(R.drawable.climbing)
                }
                "Core Training" -> {
                    // Handle Core Training
                    workout_function_icon.setImageResource(R.drawable.core_training)
                }
                "Cycling" -> {
                    // Handle Cycling
                    workout_function_icon.setImageResource(R.drawable.cycling)
                }
                "Cricket" -> {
                    // Handle Cricket
                    workout_function_icon.setImageResource(R.drawable.cricket)
                }
                "Cross Training" -> {
                    // Handle Cross Training
                    workout_function_icon.setImageResource(R.drawable.cross_training)
                }
                "Dance" -> {
                    // Handle Dance
                    workout_function_icon.setImageResource(R.drawable.dance)
                }
                "Disc Sports" -> {
                    // Handle Disc Sports
                    workout_function_icon.setImageResource(R.drawable.disc_sports)
                }
                "Elliptical" -> {
                    // Handle Elliptical
                    workout_function_icon.setImageResource(R.drawable.elliptical)
                }
                "Football" -> {
                    // Handle Football
                    workout_function_icon.setImageResource(R.drawable.football)
                }
                "Functional Strength Training" -> {
                    // Handle Functional Strength Training
                    workout_function_icon.setImageResource(R.drawable.functional_strength_training)
                }
                "Golf" -> {
                    // Handle Golf
                    workout_function_icon.setImageResource(R.drawable.golf)
                }
                "Gymnastics" -> {
                    // Handle Gymnastics
                    workout_function_icon.setImageResource(R.drawable.gymnastics)
                }
                "Handball" -> {
                    // Handle Handball
                    workout_function_icon.setImageResource(R.drawable.handball)
                }
                "Hiking" -> {
                    // Handle Hiking
                    workout_function_icon.setImageResource(R.drawable.hockey)
                }
                "Hockey" -> {
                    // Handle Hockey
                    workout_function_icon.setImageResource(R.drawable.hiit)
                }
                "HIIT" -> {
                    // Handle HIIT
                    workout_function_icon.setImageResource(R.drawable.hiking)
                }
                "High Intensity Interval Training" -> {
                    // Handle HIIT
                    workout_function_icon.setImageResource(R.drawable.hiking)
                }
                "Kickboxing" -> {
                    // Handle Kickboxing
                    workout_function_icon.setImageResource(R.drawable.kickboxing)
                }
                "Martial Arts" -> {
                    // Handle Martial Arts
                    workout_function_icon.setImageResource(R.drawable.martial_arts)
                }
                "Other" -> {
                    // Handle Other
                    workout_function_icon.setImageResource(R.drawable.other)
                }
                "Pickleball" -> {
                    // Handle Pickleball
                    workout_function_icon.setImageResource(R.drawable.pickleball)
                }
                "Pilates" -> {
                    // Handle Pilates
                    workout_function_icon.setImageResource(R.drawable.pilates)
                }
                "Power Yoga" -> {
                    // Handle Power Yoga
                    workout_function_icon.setImageResource(R.drawable.power_yoga)
                }
                "Powerlifting" -> {
                    // Handle Powerlifting
                    workout_function_icon.setImageResource(R.drawable.powerlifting)
                }
                "Pranayama" -> {
                    // Handle Pranayama
                    workout_function_icon.setImageResource(R.drawable.pranayama)
                }
                "Running" -> {
                    // Handle Running
                    workout_function_icon.setImageResource(R.drawable.running)
                }
                "Rowing Machine" -> {
                    // Handle Rowing Machine
                    workout_function_icon.setImageResource(R.drawable.rowing_machine)
                }
                "Rugby" -> {
                    // Handle Rugby
                    workout_function_icon.setImageResource(R.drawable.rugby)
                }
                "Skating" -> {
                    // Handle Skating
                    workout_function_icon.setImageResource(R.drawable.skating)
                }
                "Skipping" -> {
                    // Handle Skipping
                    workout_function_icon.setImageResource(R.drawable.skipping)
                }
                "Stairs" -> {
                    // Handle Stairs
                    workout_function_icon.setImageResource(R.drawable.stairs)
                }
                "Squash" -> {
                    // Handle Squash
                    workout_function_icon.setImageResource(R.drawable.squash)
                }
                "Traditional Strength Training" -> {
                    // Handle Traditional Strength Training
                    workout_function_icon.setImageResource(R.drawable.traditional_strength_training)
                }
                "Strength Training" -> {
                    // Handle Traditional Strength Training
                    workout_function_icon.setImageResource(R.drawable.traditional_strength_training)
                }
                "Stretching" -> {
                    // Handle Stretching
                    workout_function_icon.setImageResource(R.drawable.stretching)
                }
                "Swimming" -> {
                    // Handle Swimming
                    workout_function_icon.setImageResource(R.drawable.swimming)
                }
                "Table Tennis" -> {
                    // Handle Table Tennis
                    workout_function_icon.setImageResource(R.drawable.table_tennis)
                }
                "Tennis" -> {
                    // Handle Tennis
                    workout_function_icon.setImageResource(R.drawable.tennis)
                }
                "Track and Field Events" -> {
                    // Handle Track and Field Events
                    workout_function_icon.setImageResource(R.drawable.track_field_events)
                }
                "Volleyball" -> {
                    // Handle Volleyball
                    workout_function_icon.setImageResource(R.drawable.volleyball)
                }
                "Walking" -> {
                    // Handle Walking
                    workout_function_icon.setImageResource(R.drawable.walking)
                }
                "Watersports" -> {
                    // Handle Watersports
                    workout_function_icon.setImageResource(R.drawable.watersports)
                }
                "Wrestling" -> {
                    // Handle Wrestling
                    workout_function_icon.setImageResource(R.drawable.wrestling)
                }
                "Yoga" -> {
                    // Handle Yoga
                    workout_function_icon.setImageResource(R.drawable.yoga)
                }
                else -> {
                    // Handle unknown or null workoutType
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

        // Helper function to format the timestamp to a time string (e.g., "7:00 am")
        private fun formatTime(timestamp: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(timestamp)
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                timeFormat.format(date).lowercase()
            } catch (e: Exception) {
                "N/A"
            }
        }
    }
}