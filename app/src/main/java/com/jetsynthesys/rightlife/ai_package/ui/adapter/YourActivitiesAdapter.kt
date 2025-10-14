package com.jetsynthesys.rightlife.ai_package.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.ActivityModel
import com.jetsynthesys.rightlife.ai_package.model.CardItem
import com.jetsynthesys.rightlife.ai_package.ui.moveright.DeleteWorkoutBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.moveright.WorkoutAnalyticsFragment

class YourActivitiesAdapter(private val context: Context, private var dataLists: ArrayList<ActivityModel>, private var syncedCardItems: List<CardItem>,
    private var clickPos: Int,
    private var workoutData: ActivityModel?,
    private var isClickView: Boolean, private val onRefreshDateClick: () -> Unit,
    val onWorkoutItemClick: (ActivityModel, Int, Boolean) -> Unit,
    private val onCirclePlusClick: (ActivityModel, Int) -> Unit
) : RecyclerView.Adapter<YourActivitiesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_your_workouts_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.mealTitle.text = item.workoutType
        holder.viewLine.visibility = View.GONE
        if (item.isSynced){
            holder.edit.visibility = View.GONE
            holder.delete.visibility = View.GONE
            holder.tv_subtraction_value_Intensity.visibility = View.GONE
            holder.bpmUnit.visibility = View.VISIBLE
            holder.wearable.visibility = View.VISIBLE
            holder.bpmUnit.text = "bpm"

            holder.subtractionValue.text = item.averageHeartRate.toInt().toString()
            holder.subtraction.setImageResource(R.drawable.avg_heart_rate)
            if (item.averageHeartRate> 0){
                holder.layoutMain.isEnabled = true
            }else{
                holder.layoutMain.isEnabled = false
            }
            /*when (item?.workoutType) {
                "American Football" -> {
                    holder.main_heading_icon.setImageResource(R.drawable.american_football)// Handle American Football
                }
                "Archery" -> {
                    // Handle Archery
                    holder.main_heading_icon.setImageResource(R.drawable.archery)
                }
                "Athletics" -> {
                    // Handle Athletics
                    holder.main_heading_icon.setImageResource(R.drawable.athelete_search)
                }
                "Australian Football" -> {
                    // Handle Australian Football
                    holder.main_heading_icon.setImageResource(R.drawable.australian_football)
                }
                "Badminton" -> {
                    // Handle Badminton
                    holder.main_heading_icon.setImageResource(R.drawable.badminton)
                }
                "Barre" -> {
                    // Handle Barre
                    holder.main_heading_icon.setImageResource(R.drawable.barre)
                }
                "Baseball" -> {
                    // Handle Baseball
                    holder.main_heading_icon.setImageResource(R.drawable.baseball)
                }
                "Basketball" -> {
                    // Handle Basketball
                    holder.main_heading_icon.setImageResource(R.drawable.basketball)
                }
                "Boxing" -> {
                    // Handle Boxing
                    holder.main_heading_icon.setImageResource(R.drawable.boxing)

                }
                "Climbing" -> {
                    // Handle Climbing
                    holder.main_heading_icon.setImageResource(R.drawable.climbing)
                }
                "Core Training" -> {
                    // Handle Core Training
                    holder.main_heading_icon.setImageResource(R.drawable.core_training)
                }
                "Cycling" -> {
                    // Handle Cycling
                    holder.main_heading_icon.setImageResource(R.drawable.cycling)
                }
                "Cricket" -> {
                    // Handle Cricket
                    holder.main_heading_icon.setImageResource(R.drawable.cricket)
                }
                "Cross Training" -> {
                    // Handle Cross Training
                    holder.main_heading_icon.setImageResource(R.drawable.cross_training)
                }
                "Dance" -> {
                    // Handle Dance
                    holder.main_heading_icon.setImageResource(R.drawable.dance)
                }
                "Disc Sports" -> {
                    // Handle Disc Sports
                    holder.main_heading_icon.setImageResource(R.drawable.disc_sports)
                }
                "Elliptical" -> {
                    // Handle Elliptical
                    holder.main_heading_icon.setImageResource(R.drawable.elliptical)
                }
                "Football" -> {
                    // Handle Football
                    holder.main_heading_icon.setImageResource(R.drawable.football)
                }
                "Functional Strength Training" -> {
                    // Handle Functional Strength Training
                    holder.main_heading_icon.setImageResource(R.drawable.functional_strength_training)
                }
                "Golf" -> {
                    // Handle Golf
                    holder.main_heading_icon.setImageResource(R.drawable.golf)
                }
                "Gymnastics" -> {
                    // Handle Gymnastics
                    holder.main_heading_icon.setImageResource(R.drawable.gymnastics)
                }
                "Handball" -> {
                    // Handle Handball
                    holder.main_heading_icon.setImageResource(R.drawable.handball)
                }
                "Hiking" -> {
                    // Handle Hiking
                    holder.main_heading_icon.setImageResource(R.drawable.hiking)
                }
                "Hockey" -> {
                    // Handle Hockey
                    holder.main_heading_icon.setImageResource(R.drawable.hockey)
                }
                "HIIT" -> {
                    // Handle HIIT
                    holder.main_heading_icon.setImageResource(R.drawable.hiit)
                }
                "Kickboxing" -> {
                    // Handle Kickboxing
                    holder.main_heading_icon.setImageResource(R.drawable.kickboxing)
                }
                "Martial Arts" -> {
                    // Handle Martial Arts
                    holder.main_heading_icon.setImageResource(R.drawable.martial_arts)
                }
                "Other" -> {
                    // Handle Other
                    holder.main_heading_icon.setImageResource(R.drawable.other)
                }
                "Pickleball" -> {
                    // Handle Pickleball
                    holder.main_heading_icon.setImageResource(R.drawable.pickleball)
                }
                "Pilates" -> {
                    // Handle Pilates
                    holder.main_heading_icon.setImageResource(R.drawable.pilates)
                }
                "Power Yoga" -> {
                    // Handle Power Yoga
                    holder.main_heading_icon.setImageResource(R.drawable.power_yoga)
                }
                "Powerlifting" -> {
                    // Handle Powerlifting
                    holder.main_heading_icon.setImageResource(R.drawable.powerlifting)
                }
                "Pranayama" -> {
                    // Handle Pranayama
                    holder.main_heading_icon.setImageResource(R.drawable.pranayama)
                }
                "Running" -> {
                    // Handle Running
                    holder.main_heading_icon.setImageResource(R.drawable.running)
                }
                "Rowing Machine" -> {
                    // Handle Rowing Machine
                    holder.main_heading_icon.setImageResource(R.drawable.rowing_machine)
                }
                "Rugby" -> {
                    // Handle Rugby
                    holder.main_heading_icon.setImageResource(R.drawable.rugby)
                }
                "Skating" -> {
                    // Handle Skating
                    holder.main_heading_icon.setImageResource(R.drawable.skating)
                }
                "Skipping" -> {
                    // Handle Skipping
                    holder.main_heading_icon.setImageResource(R.drawable.skipping)
                }
                "Stairs" -> {
                    // Handle Stairs
                    holder.main_heading_icon.setImageResource(R.drawable.stairs)
                }
                "Squash" -> {
                    // Handle Squash
                    holder.main_heading_icon.setImageResource(R.drawable.squash)
                }
                "Traditional Strength Training" -> {
                    // Handle Traditional Strength Training
                    holder.main_heading_icon.setImageResource(R.drawable.traditional_strength_training)
                }
                "Stretching" -> {
                    // Handle Stretching
                    holder.main_heading_icon.setImageResource(R.drawable.stretching)
                }
                "Swimming" -> {
                    // Handle Swimming
                    holder.main_heading_icon.setImageResource(R.drawable.swimming)
                }
                "Table Tennis" -> {
                    // Handle Table Tennis
                    holder.main_heading_icon.setImageResource(R.drawable.table_tennis)
                }
                "Tennis" -> {
                    // Handle Tennis
                    holder.main_heading_icon.setImageResource(R.drawable.tennis)
                }
                "Track and Field Events" -> {
                    // Handle Track and Field Events
                    holder.main_heading_icon.setImageResource(R.drawable.track_field_events)
                }
                "Volleyball" -> {
                    // Handle Volleyball
                    holder.main_heading_icon.setImageResource(R.drawable.volleyball)
                }
                "Walking" -> {
                    // Handle Walking
                    holder.main_heading_icon.setImageResource(R.drawable.walking)
                }
                "Watersports" -> {
                    // Handle Watersports
                    holder.main_heading_icon.setImageResource(R.drawable.watersports)
                }
                "Wrestling" -> {
                    // Handle Wrestling
                    holder.main_heading_icon.setImageResource(R.drawable.wrestling)
                }
                "Yoga" -> {
                    // Handle Yoga
                    holder.main_heading_icon.setImageResource(R.drawable.yoga)
                }
                else -> {
                    // Handle unknown or null workoutType
                    holder.main_heading_icon.setImageResource(R.drawable.other)
                }
            }*/
        }else{
            holder.edit.visibility = View.VISIBLE
            holder.delete.visibility = View.VISIBLE
            holder.tv_subtraction_value_Intensity.visibility = View.GONE
            holder.wearable.visibility = View.GONE
            holder.bpmUnit.visibility = View.GONE
            holder.subtractionValue.text = "${item.intensity} Intensity"
            holder.subtraction.setImageResource(R.drawable.intensity_meter)
            holder.layoutMain.isEnabled = false
            /*Glide.with(context)
                .load(item.icon) // <-- your image URL string
                .into(holder.main_heading_icon)*/
        }
        when (item?.workoutType) {
            "American Football" -> {
                holder.main_heading_icon.setImageResource(R.drawable.american_football)// Handle American Football
            }
            "Archery" -> {
                // Handle Archery
                holder.main_heading_icon.setImageResource(R.drawable.archery)
            }
            "Athletics" -> {
                // Handle Athletics
                holder.main_heading_icon.setImageResource(R.drawable.athelete_search)
            }
            "Australian Football" -> {
                // Handle Australian Football
                holder.main_heading_icon.setImageResource(R.drawable.australian_football)
            }
            "Badminton" -> {
                // Handle Badminton
                holder.main_heading_icon.setImageResource(R.drawable.badminton)
            }
            "Barre" -> {
                // Handle Barre
                holder.main_heading_icon.setImageResource(R.drawable.barre)
            }
            "Baseball" -> {
                // Handle Baseball
                holder.main_heading_icon.setImageResource(R.drawable.baseball)
            }
            "Basketball" -> {
                // Handle Basketball
                holder.main_heading_icon.setImageResource(R.drawable.basketball)
            }
            "Boxing" -> {
                // Handle Boxing
                holder.main_heading_icon.setImageResource(R.drawable.boxing)

            }
            "Climbing" -> {
                // Handle Climbing
                holder.main_heading_icon.setImageResource(R.drawable.climbing)
            }
            "Core Training" -> {
                // Handle Core Training
                holder.main_heading_icon.setImageResource(R.drawable.core_training)
            }
            "Cycling" -> {
                // Handle Cycling
                holder.main_heading_icon.setImageResource(R.drawable.cycling)
            }
            "Cricket" -> {
                // Handle Cricket
                holder.main_heading_icon.setImageResource(R.drawable.cricket)
            }
            "Cross Training" -> {
                // Handle Cross Training
                holder.main_heading_icon.setImageResource(R.drawable.cross_training)
            }
            "Dance" -> {
                // Handle Dance
                holder.main_heading_icon.setImageResource(R.drawable.dance)
            }
            "Disc Sports" -> {
                // Handle Disc Sports
                holder.main_heading_icon.setImageResource(R.drawable.disc_sports)
            }
            "Elliptical" -> {
                // Handle Elliptical
                holder.main_heading_icon.setImageResource(R.drawable.elliptical)
            }
            "Football" -> {
                // Handle Football
                holder.main_heading_icon.setImageResource(R.drawable.football)
            }
            "Functional Strength Training" -> {
                // Handle Functional Strength Training
                holder.main_heading_icon.setImageResource(R.drawable.functional_strength_training)
            }
            "Golf" -> {
                // Handle Golf
                holder.main_heading_icon.setImageResource(R.drawable.golf)
            }
            "Gymnastics" -> {
                // Handle Gymnastics
                holder.main_heading_icon.setImageResource(R.drawable.gymnastics)
            }
            "Handball" -> {
                // Handle Handball
                holder.main_heading_icon.setImageResource(R.drawable.handball)
            }
            "Hiking" -> {
                // Handle Hiking
                holder.main_heading_icon.setImageResource(R.drawable.hockey)
            }
            "Hockey" -> {
                // Handle Hockey
                holder.main_heading_icon.setImageResource(R.drawable.hiit)
            }
            "HIIT" -> {
                // Handle HIIT
                holder.main_heading_icon.setImageResource(R.drawable.hiking)
            }
            "High Intensity Interval Training" -> {
                // Handle HIIT
                holder.main_heading_icon.setImageResource(R.drawable.hiking)
            }
            "Kickboxing" -> {
                // Handle Kickboxing
                holder.main_heading_icon.setImageResource(R.drawable.kickboxing)
            }
            "Martial Arts" -> {
                // Handle Martial Arts
                holder.main_heading_icon.setImageResource(R.drawable.martial_arts)
            }
            "Other" -> {
                // Handle Other
                holder.main_heading_icon.setImageResource(R.drawable.other)
            }
            "Pickleball" -> {
                // Handle Pickleball
                holder.main_heading_icon.setImageResource(R.drawable.pickleball)
            }
            "Pilates" -> {
                // Handle Pilates
                holder.main_heading_icon.setImageResource(R.drawable.pilates)
            }
            "Power Yoga" -> {
                // Handle Power Yoga
                holder.main_heading_icon.setImageResource(R.drawable.power_yoga)
            }
            "Powerlifting" -> {
                // Handle Powerlifting
                holder.main_heading_icon.setImageResource(R.drawable.powerlifting)
            }
            "Pranayama" -> {
                // Handle Pranayama
                holder.main_heading_icon.setImageResource(R.drawable.pranayama)
            }
            "Running" -> {
                // Handle Running
                holder.main_heading_icon.setImageResource(R.drawable.running)
            }
            "Rowing Machine" -> {
                // Handle Rowing Machine
                holder.main_heading_icon.setImageResource(R.drawable.rowing_machine)
            }
            "Rugby" -> {
                // Handle Rugby
                holder.main_heading_icon.setImageResource(R.drawable.rugby)
            }
            "Skating" -> {
                // Handle Skating
                holder.main_heading_icon.setImageResource(R.drawable.skating)
            }
            "Skipping" -> {
                // Handle Skipping
                holder.main_heading_icon.setImageResource(R.drawable.skipping)
            }
            "Stairs" -> {
                // Handle Stairs
                holder.main_heading_icon.setImageResource(R.drawable.stairs)
            }
            "Squash" -> {
                // Handle Squash
                holder.main_heading_icon.setImageResource(R.drawable.squash)
            }
            "Traditional Strength Training" -> {
                // Handle Traditional Strength Training
                holder.main_heading_icon.setImageResource(R.drawable.traditional_strength_training)
            }
            "Strength Training" -> {
                // Handle Traditional Strength Training
                holder.main_heading_icon.setImageResource(R.drawable.traditional_strength_training)
            }
            "Stretching" -> {
                // Handle Stretching
                holder.main_heading_icon.setImageResource(R.drawable.stretching)
            }
            "Swimming" -> {
                // Handle Swimming
                holder.main_heading_icon.setImageResource(R.drawable.swimming)
            }
            "Table Tennis" -> {
                // Handle Table Tennis
                holder.main_heading_icon.setImageResource(R.drawable.table_tennis)
            }
            "Tennis" -> {
                // Handle Tennis
                holder.main_heading_icon.setImageResource(R.drawable.tennis)
            }
            "Track and Field Events" -> {
                // Handle Track and Field Events
                holder.main_heading_icon.setImageResource(R.drawable.track_field_events)
            }
            "Volleyball" -> {
                // Handle Volleyball
                holder.main_heading_icon.setImageResource(R.drawable.volleyball)
            }
            "Walking" -> {
                // Handle Walking
                holder.main_heading_icon.setImageResource(R.drawable.walking)
            }
            "Watersports" -> {
                // Handle Watersports
                holder.main_heading_icon.setImageResource(R.drawable.watersports)
            }
            "Wrestling" -> {
                // Handle Wrestling
                holder.main_heading_icon.setImageResource(R.drawable.wrestling)
            }
            "Strength Training" -> {
                // Handle Traditional Strength Training
                holder.main_heading_icon.setImageResource(R.drawable.traditional_strength_training)
            }
            "Yoga" -> {
                // Handle Yoga
                holder.main_heading_icon.setImageResource(R.drawable.yoga)
            }
            else -> {
                // Handle unknown or null workoutType
                holder.main_heading_icon.setImageResource(R.drawable.other)
            }
        }
        val formattedTime = formatTimeString(item.duration!!)
        holder.duration.text = formattedTime
        val formattedCalories = item.caloriesBurned!!.substringBefore(".")
        holder.calValue.text = formattedCalories
        holder.calUnit.visibility = View.VISIBLE
        holder.calUnit.text = "cal"
        holder.mealName.visibility = View.GONE

        holder.delete.setOnClickListener {
            val bottomSheet = DeleteWorkoutBottomSheet.newInstance(
                calorieId = item.id!!,
                userId = item.userId!! // Replace with dynamic userId if available
            )

            bottomSheet.setOnDeleteSuccessListener {
                dataLists.removeAt(position)
                onRefreshDateClick()
                notifyDataSetChanged()
            }

            // Add new refresh callback
            bottomSheet.setOnDeleteSuccessWithRefreshListener {
                // This will trigger the fragment to refresh current date data
                onRefreshDateClick()
            }

            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "DeleteWorkoutBottomSheet")
        }

        holder.edit.setOnClickListener {
            onCirclePlusClick(item, position)
        }

        holder.layoutMain.setOnClickListener {
            if (syncedCardItems.isNotEmpty()){
                 var cardItem: CardItem? = null
                for (syncedItem in syncedCardItems){
                    if (item.workoutType == syncedItem.title && item.duration == syncedItem.duration){
                        cardItem = syncedItem
                        val fragment = WorkoutAnalyticsFragment().apply {
                            arguments = Bundle().apply {
                                putString("YourActivity", "YourActivity")
                                putSerializable("cardItem", cardItem)
                            }
                        }
                        (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.flFragment, fragment, "workoutAnalysisFragment")
                            .addToBackStack(null)
                            .commit()
                        break
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealTitle: TextView = itemView.findViewById(R.id.tv_meal_title)
        val wearable: ImageView = itemView.findViewById(R.id.image_delete)
        val delete: ImageView = itemView.findViewById(R.id.image_edit)
        val edit: ImageView = itemView.findViewById(R.id.image_circle_plus)
        val mealName: TextView = itemView.findViewById(R.id.tv_meal_name)
        val serve: ImageView = itemView.findViewById(R.id.image_serve)
        val serves: TextView = itemView.findViewById(R.id.tv_serves)
        val duration: TextView = itemView.findViewById(R.id.tv_serves_count)
        val tv_subtraction_value_Intensity: TextView = itemView.findViewById(R.id.tv_subtraction_value_Intensity)
        val cal: ImageView = itemView.findViewById(R.id.image_cal)
        val calValue: TextView = itemView.findViewById(R.id.tv_cal_value)
        val calUnit: TextView = itemView.findViewById(R.id.tv_cal_unit)
        val main_heading_icon: ImageView = itemView.findViewById(R.id.main_heading_icon)
        val subtraction: ImageView = itemView.findViewById(R.id.image_subtraction)
        val subtractionValue: TextView = itemView.findViewById(R.id.tv_subtraction_value)
        val bpmUnit: TextView = itemView.findViewById(R.id.tv_subtraction_unit)
        val baguette: ImageView = itemView.findViewById(R.id.image_baguette)
        val baguetteValue: TextView = itemView.findViewById(R.id.tv_baguette_value)
        val baguetteUnit: TextView = itemView.findViewById(R.id.tv_baguette_unit)
        val dewpoint: ImageView = itemView.findViewById(R.id.image_dewpoint)
        val dewpointValue: TextView = itemView.findViewById(R.id.tv_dewpoint_value)
        val dewpointUnit: TextView = itemView.findViewById(R.id.tv_dewpoint_unit)
        val viewLine: View = itemView.findViewById(R.id.view)
        val layoutMain : androidx.constraintlayout.widget.ConstraintLayout = itemView.findViewById(R.id.layout_main)
    }

    fun addAll(
        items: ArrayList<ActivityModel>?,
        syncedCardItemsView: List<CardItem>,
        pos: Int,
        workoutItem: ActivityModel?,
        isClick: Boolean,
    ) {
        dataLists.clear()
        if (items != null) {
            dataLists = items
            syncedCardItems = syncedCardItemsView
            clickPos = pos
            workoutData = workoutItem
            isClickView = isClick
        }
        notifyDataSetChanged()
    }
    private fun formatCalorieString(calorieString: String): SpannableStringBuilder {
        // Step 1: Parse the input string (e.g., "488 kcal" -> 488)
        val calories = calorieString.replace(" kcal", "").toIntOrNull() ?: 0
        val formattedText = "$calories cal"
        // Step 2: Create SpannableStringBuilder for styling
        val spannable = SpannableStringBuilder(formattedText)
        // Step 3: Apply bold and 18sp to the number
        val numberLength = calories.toString().length
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            numberLength,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(18, true), // 18sp
            0,
            numberLength,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        // Step 4: Apply normal and 12sp to "kcal"
        spannable.setSpan(
            AbsoluteSizeSpan(12, true), // 12sp
            numberLength,
            formattedText.length, // " kcal"
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun formatTimeString(timeString: String): SpannableStringBuilder {
        // Step 1: Parse the input string (e.g., "2 hr 01 mins")
        var hours = 0
        var minutes = 0
        val parts = timeString.split(" ")
        when (parts.size) {
            2 -> { // Format: "61 mins"
                minutes = parts[0].toIntOrNull() ?: 0
            }
            4 -> { // Format: "2 hr 01 mins"
                hours = parts[0].toIntOrNull() ?: 0
                minutes = parts[2].toIntOrNull() ?: 0
            }
        }
        // Step 2: Build the formatted string (e.g., "2 hr 1 mins")
        val formattedText = buildString {
            if (hours > 0) {
                append("$hours hr ")
            }
            if (minutes > 0 || hours == 0) { // Show minutes even if 0 when hours is 0
                append("$minutes mins")
            }
        }.trim()
        // Step 3: Create SpannableStringBuilder for styling
        val spannable = SpannableStringBuilder(formattedText)

        // Apply bold and 18sp to numbers
        var currentIndex = 0
        if (hours > 0) {
            // Style the hours number
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                hours.toString().length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                AbsoluteSizeSpan(14, true), // 18sp
                0,
                hours.toString().length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // Style the "hr" unit (normal, 12sp, no bold)
            spannable.setSpan(
                AbsoluteSizeSpan(10, true), // 12sp
                hours.toString().length,
                hours.toString().length + 3, // " hr "
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            currentIndex = hours.toString().length + 4 // Move index past "X hr "
        }

        // Style the minutes number
        if (minutes > 0 || hours == 0) {
            val minutesStart = currentIndex
            val minutesEnd = minutesStart + minutes.toString().length
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                minutesStart,
                minutesEnd,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                AbsoluteSizeSpan(14, true), // 18sp
                minutesStart,
                minutesEnd,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // Style the "mins" unit (normal, 12sp, no bold)
            spannable.setSpan(
                AbsoluteSizeSpan(10, true), // 12sp
                minutesEnd,
                formattedText.length, // " mins"
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}