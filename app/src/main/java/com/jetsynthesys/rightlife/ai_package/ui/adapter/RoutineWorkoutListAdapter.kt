package com.jetsynthesys.rightlife.ai_package.ui.adapter

import android.content.Context
import android.graphics.Typeface
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
import com.jetsynthesys.rightlife.BuildConfig
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.RoutineWorkoutDisplayModel
import com.jetsynthesys.rightlife.ai_package.ui.moveright.RemoveWorkoutBottomSheet

class RoutineWorkoutListAdapter(
    private val context: Context,
    private var dataList: ArrayList<RoutineWorkoutDisplayModel>,
    private val onItemClick: (RoutineWorkoutDisplayModel, Int) -> Unit,
    private val onListEmpty: () -> Unit,
    private val onItemRemove: (Int) -> Unit // New callback for removing item from parent fragment
) : RecyclerView.Adapter<RoutineWorkoutListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_your_workouts_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        // Bind data to views
        holder.mealTitle.text = item.name
        var imageUrl = ""
        if (item.icon.startsWith(BuildConfig.CDN_URL)) {
            // For Cross Training URL, prepend the jetsynthesisqa base URL
            imageUrl = item.icon
        } else if (item.icon.startsWith("https")) {
            // For other HTTPS URLs (Athletics, Archery), use directly
            imageUrl = item.icon
        } else {
            // For non-HTTPS URLs, prepend the jetsynthesisqa base URL
            imageUrl = "https://jetsynthesisqa-us-east-1.s3-accelerate.amazonaws.com/" + item.icon
        }
       /* Glide.with(context)
            .load(imageUrl)
            .into(holder.workoutIcon)*/
        val formattedtime = formatTimeString(item.duration)
        holder.servesCount.text = formattedtime
        val formattedCalories = item.caloriesBurned!!.substringBefore(".")
        holder.calValue.text = formattedCalories
        holder.calUnit.visibility = View.VISIBLE
        holder.calUnit.text = "cal"
        holder.subtractionValue.text = "${item.intensity} Intensity"

        when (item?.name) {
            "American Football" -> {
                holder.workoutIcon.setImageResource(R.drawable.american_football)// Handle American Football
            }
            "Archery" -> {
                // Handle Archery
                holder.workoutIcon.setImageResource(R.drawable.archery)
            }
            "Athletics" -> {
                // Handle Athletics
                holder.workoutIcon.setImageResource(R.drawable.athelete_search)
            }
            "Australian Football" -> {
                // Handle Australian Football
                holder.workoutIcon.setImageResource(R.drawable.australian_football)
            }
            "Badminton" -> {
                // Handle Badminton
                holder.workoutIcon.setImageResource(R.drawable.badminton)
            }
            "Barre" -> {
                // Handle Barre
                holder.workoutIcon.setImageResource(R.drawable.barre)
            }
            "Baseball" -> {
                // Handle Baseball
                holder.workoutIcon.setImageResource(R.drawable.baseball)
            }
            "Basketball" -> {
                // Handle Basketball
                holder.workoutIcon.setImageResource(R.drawable.basketball)
            }
            "Boxing" -> {
                // Handle Boxing
                holder.workoutIcon.setImageResource(R.drawable.boxing)

            }
            "Climbing" -> {
                // Handle Climbing
                holder.workoutIcon.setImageResource(R.drawable.climbing)
            }
            "Core Training" -> {
                // Handle Core Training
                holder.workoutIcon.setImageResource(R.drawable.core_training)
            }
            "Cycling" -> {
                // Handle Cycling
                holder.workoutIcon.setImageResource(R.drawable.cycling)
            }
            "Cricket" -> {
                // Handle Cricket
                holder.workoutIcon.setImageResource(R.drawable.cricket)
            }
            "Cross Training" -> {
                // Handle Cross Training
                holder.workoutIcon.setImageResource(R.drawable.cross_training)
            }
            "Dance" -> {
                // Handle Dance
                holder.workoutIcon.setImageResource(R.drawable.dance)
            }
            "Disc Sports" -> {
                // Handle Disc Sports
                holder.workoutIcon.setImageResource(R.drawable.disc_sports)
            }
            "Elliptical" -> {
                // Handle Elliptical
                holder.workoutIcon.setImageResource(R.drawable.elliptical)
            }
            "Football" -> {
                // Handle Football
                holder.workoutIcon.setImageResource(R.drawable.football)
            }
            "Functional Strength Training" -> {
                // Handle Functional Strength Training
                holder.workoutIcon.setImageResource(R.drawable.functional_strength_training)
            }
            "Golf" -> {
                // Handle Golf
                holder.workoutIcon.setImageResource(R.drawable.golf)
            }
            "Gymnastics" -> {
                // Handle Gymnastics
                holder.workoutIcon.setImageResource(R.drawable.gymnastics)
            }
            "Handball" -> {
                // Handle Handball
                holder.workoutIcon.setImageResource(R.drawable.handball)
            }
            "Hiking" -> {
                // Handle Hiking
                holder.workoutIcon.setImageResource(R.drawable.hockey)
            }
            "Hockey" -> {
                // Handle Hockey
                holder.workoutIcon.setImageResource(R.drawable.hiit)

            }
            "HIIT" -> {
                // Handle HIIT
                holder.workoutIcon.setImageResource(R.drawable.hiking)

            }
            "High Intensity Interval Training" -> {
                // Handle HIIT
                holder.workoutIcon.setImageResource(R.drawable.hiking)
            }
            "Kickboxing" -> {
                // Handle Kickboxing
                holder.workoutIcon.setImageResource(R.drawable.kickboxing)

            }
            "Martial Arts" -> {
                // Handle Martial Arts
                holder.workoutIcon.setImageResource(R.drawable.martial_arts)

            }
            "Other" -> {
                // Handle Other
                holder.workoutIcon.setImageResource(R.drawable.other)

            }
            "Pickleball" -> {
                // Handle Pickleball
                holder.workoutIcon.setImageResource(R.drawable.pickleball)

            }
            "Pilates" -> {
                // Handle Pilates
                holder.workoutIcon.setImageResource(R.drawable.pilates)

            }
            "Power Yoga" -> {
                // Handle Power Yoga
                holder.workoutIcon.setImageResource(R.drawable.power_yoga)

            }
            "Powerlifting" -> {
                // Handle Powerlifting
                holder.workoutIcon.setImageResource(R.drawable.powerlifting)

            }
            "Pranayama" -> {
                // Handle Pranayama
                holder.workoutIcon.setImageResource(R.drawable.pranayama)

            }
            "Running" -> {
                // Handle Running
                holder.workoutIcon.setImageResource(R.drawable.running)

            }
            "Rowing Machine" -> {
                // Handle Rowing Machine
                holder.workoutIcon.setImageResource(R.drawable.rowing_machine)

            }
            "Rugby" -> {
                // Handle Rugby
                holder.workoutIcon.setImageResource(R.drawable.rugby)

            }
            "Skating" -> {
                // Handle Skating
                holder.workoutIcon.setImageResource(R.drawable.skating)

            }
            "Skipping" -> {
                // Handle Skipping
                holder.workoutIcon.setImageResource(R.drawable.skipping)

            }
            "Stairs" -> {
                // Handle Stairs
                holder.workoutIcon.setImageResource(R.drawable.stairs)

            }
            "Squash" -> {
                // Handle Squash
                holder.workoutIcon.setImageResource(R.drawable.squash)

            }
            "Traditional Strength Training" -> {
                // Handle Traditional Strength Training
                holder.workoutIcon.setImageResource(R.drawable.traditional_strength_training)

            }
            "Strength Training" -> {
                // Handle Traditional Strength Training
                holder.workoutIcon.setImageResource(R.drawable.traditional_strength_training)

            }
            "Stretching" -> {
                // Handle Stretching
                holder.workoutIcon.setImageResource(R.drawable.stretching)

            }
            "Swimming" -> {
                // Handle Swimming
                holder.workoutIcon.setImageResource(R.drawable.swimming)

            }
            "Table Tennis" -> {
                // Handle Table Tennis
                holder.workoutIcon.setImageResource(R.drawable.table_tennis)

            }
            "Tennis" -> {
                // Handle Tennis
                holder.workoutIcon.setImageResource(R.drawable.tennis)

            }
            "Track and Field Events" -> {
                // Handle Track and Field Events
                holder.workoutIcon.setImageResource(R.drawable.track_field_events)

            }
            "Volleyball" -> {
                // Handle Volleyball
                holder.workoutIcon.setImageResource(R.drawable.volleyball)

            }
            "Walking" -> {
                // Handle Walking
                holder.workoutIcon.setImageResource(R.drawable.walking)

            }
            "Watersports" -> {
                // Handle Watersports
                holder.workoutIcon.setImageResource(R.drawable.watersports)

            }
            "Wrestling" -> {
                // Handle Wrestling
                holder.workoutIcon.setImageResource(R.drawable.wrestling)

            }
            "Yoga" -> {
                // Handle Yoga
                holder.workoutIcon.setImageResource(R.drawable.yoga)

            }
            else -> {
                // Handle unknown or null workoutType
                holder.workoutIcon.setImageResource(R.drawable.other)

            }
        }
        // Hide unused views
        holder.mealName.visibility = View.GONE
        holder.delete.visibility = View.GONE
        holder.circlePlus.visibility = View.VISIBLE

        // Set up edit button click listener
        holder.edit.setOnClickListener {
            val bottomSheet = RemoveWorkoutBottomSheet.newInstance(position)
            // Set callback to remove item from list
            bottomSheet.setOnRemoveSuccessListener {
                dataList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, dataList.size)
                onItemRemove(position) // Notify parent fragment to remove from workoutList
                if (dataList.isEmpty()) {
                    onListEmpty()
                }
            }
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "RemoveWorkoutBottomSheet")
        }

        // Set up item click listener
        holder.itemView.setOnClickListener {
            onItemClick(item, position)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealTitle: TextView = itemView.findViewById(R.id.tv_meal_title)
        val workoutIcon: ImageView = itemView.findViewById(R.id.main_heading_icon)
        val delete: ImageView = itemView.findViewById(R.id.image_delete)
        val edit: ImageView = itemView.findViewById(R.id.image_edit)
        val circlePlus: ImageView = itemView.findViewById(R.id.image_circle_plus)
        val mealName: TextView = itemView.findViewById(R.id.tv_meal_name)
        val serve: ImageView = itemView.findViewById(R.id.image_serve)
        val serves: TextView = itemView.findViewById(R.id.tv_serves)
        val servesCount: TextView = itemView.findViewById(R.id.tv_serves_count)
        val cal: ImageView = itemView.findViewById(R.id.image_cal)
        val calValue: TextView = itemView.findViewById(R.id.tv_cal_value)
        val calUnit: TextView = itemView.findViewById(R.id.tv_cal_unit)
        val subtraction: ImageView = itemView.findViewById(R.id.image_subtraction)
        val subtractionValue: TextView = itemView.findViewById(R.id.tv_subtraction_value)
        val subtractionUnit: TextView = itemView.findViewById(R.id.tv_subtraction_unit)
        val baguette: ImageView = itemView.findViewById(R.id.image_baguette)
        val baguetteValue: TextView = itemView.findViewById(R.id.tv_baguette_value)
        val baguetteUnit: TextView = itemView.findViewById(R.id.tv_baguette_unit)
        val dewpoint: ImageView = itemView.findViewById(R.id.image_dewpoint)
        val dewpointValue: TextView = itemView.findViewById(R.id.tv_dewpoint_value)
        val dewpointUnit: TextView = itemView.findViewById(R.id.tv_dewpoint_unit)
    }
    private fun formatTimeString(timeString: String): SpannableStringBuilder {
        // Step 1: Parse the input string (e.g., "1439 min")
        var hours = 0
        var minutes = 0
        val parts = timeString.split(" ")
        if (parts.size == 2 && parts[1].startsWith("min", ignoreCase = true)) {
            // Input format: "1439 min"
            val totalMinutes = parts[0].toIntOrNull() ?: 0
            hours = totalMinutes / 60 // Convert total minutes to hours
            minutes = totalMinutes % 60 // Remaining minutes
        } else {
            // Fallback for other formats (e.g., "2 hr 01 mins")
            when (parts.size) {
                2 -> { // Format: "61 mins"
                    minutes = parts[0].toIntOrNull() ?: 0
                }
                4 -> { // Format: "2 hr 01 mins"
                    hours = parts[0].toIntOrNull() ?: 0
                    minutes = parts[2].toIntOrNull() ?: 0
                }
            }
        }

        // Step 2: Build the formatted string (e.g., "23 hr 59 mins")
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

        // Apply bold and 14sp to numbers
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
                AbsoluteSizeSpan(14, true), // 14sp
                0,
                hours.toString().length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // Style the "hr" unit (normal, 10sp, no bold)
            spannable.setSpan(
                AbsoluteSizeSpan(10, true), // 10sp
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
                AbsoluteSizeSpan(14, true), // 14sp
                minutesStart,
                minutesEnd,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // Style the "mins" unit (normal, 10sp, no bold)
            spannable.setSpan(
                AbsoluteSizeSpan(10, true), // 10sp
                minutesEnd,
                formattedText.length, // " mins"
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }
    fun setData(newData: ArrayList<RoutineWorkoutDisplayModel>) {
        dataList.clear()
        dataList.addAll(newData)
        notifyDataSetChanged()
    }
}