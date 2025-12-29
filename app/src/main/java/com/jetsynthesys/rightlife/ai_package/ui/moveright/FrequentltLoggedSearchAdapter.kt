package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.annotation.SuppressLint
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
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.model.FrequentLoggedRoutine
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.frequentlylogged.LoggedBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MyMealModel

class FrequentltLoggedSearchAdapter(private val context: Context, private var dataLists: ArrayList<FrequentLoggedRoutine>,
                                    private var clickPos: Int, private var mealLogListData : FrequentLoggedRoutine?,
                                    private var isClickView : Boolean, val onMealLogDateItem: (FrequentLoggedRoutine, Int, Boolean) -> Unit,) :
    RecyclerView.Adapter<FrequentltLoggedSearchAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_frequently_logged_search_layout_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = dataLists[position]

        holder.mealTitle.text = item.mealName
       /* Glide.with(context)
            .load(item.icon) // <-- your image URL string
            .into(holder.mealIcon)*/
        holder.mealName.text = item.mealType
        holder.servesCount.text = item.serve
        holder.calValue.text = formatCaloriesString(item.cal.toString())
        holder.subtractionValue.text = item.subtraction
       // holder.baguetteValue.text = item.baguette
        holder.dewpointValue.text = item.dewpoint
        holder.serves.text = item.baguette?.let { formatTimeString(it) }


        holder.edit.setOnClickListener {

        }
        holder.delete.setOnClickListener {

        }
        holder.addToWorkout.setOnClickListener {
            val bottomSheet = LoggedBottomSheet()
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "EditWorkoutBottomSheet")
        }
        when (item?.mealName) {
            "American Football" -> {
                holder.mealIcon.setImageResource(R.drawable.american_football)// Handle American Football
            }
            "Archery" -> {
                // Handle Archery
                holder.mealIcon.setImageResource(R.drawable.archery)
            }
            "Athletics" -> {
                // Handle Athletics
                holder.mealIcon.setImageResource(R.drawable.athelete_search)
            }
            "Australian Football" -> {
                // Handle Australian Football
                holder.mealIcon.setImageResource(R.drawable.australian_football)
            }
            "Badminton" -> {
                // Handle Badminton
                holder.mealIcon.setImageResource(R.drawable.badminton)
            }
            "Barre" -> {
                // Handle Barre
                holder.mealIcon.setImageResource(R.drawable.barre)
            }
            "Baseball" -> {
                // Handle Baseball
                holder.mealIcon.setImageResource(R.drawable.baseball)
            }
            "Basketball" -> {
                // Handle Basketball
                holder.mealIcon.setImageResource(R.drawable.basketball)
            }
            "Boxing" -> {
                // Handle Boxing
                holder.mealIcon.setImageResource(R.drawable.boxing)

            }
            "Climbing" -> {
                // Handle Climbing
                holder.mealIcon.setImageResource(R.drawable.climbing)
            }
            "Core Training" -> {
                // Handle Core Training
                holder.mealIcon.setImageResource(R.drawable.core_training)
            }
            "Cycling" -> {
                // Handle Cycling
                holder.mealIcon.setImageResource(R.drawable.cycling)
            }
            "Cricket" -> {
                // Handle Cricket
                holder.mealIcon.setImageResource(R.drawable.cricket)
            }
            "Cross Training" -> {
                // Handle Cross Training
                holder.mealIcon.setImageResource(R.drawable.cross_training)
            }
            "Dance" -> {
                // Handle Dance
                holder.mealIcon.setImageResource(R.drawable.dance)
            }
            "Disc Sports" -> {
                // Handle Disc Sports
                holder.mealIcon.setImageResource(R.drawable.disc_sports)
            }
            "Elliptical" -> {
                // Handle Elliptical
                holder.mealIcon.setImageResource(R.drawable.elliptical)
            }
            "Football" -> {
                // Handle Football
                holder.mealIcon.setImageResource(R.drawable.football)
            }
            "Functional Strength Training" -> {
                // Handle Functional Strength Training
                holder.mealIcon.setImageResource(R.drawable.functional_strength_training)
            }
            "Golf" -> {
                // Handle Golf
                holder.mealIcon.setImageResource(R.drawable.golf)
            }
            "Gymnastics" -> {
                // Handle Gymnastics
                holder.mealIcon.setImageResource(R.drawable.gymnastics)
            }
            "Handball" -> {
                // Handle Handball
                holder.mealIcon.setImageResource(R.drawable.handball)
            }
            "Hiking" -> {
                // Handle Hiking
                holder.mealIcon.setImageResource(R.drawable.hockey)
            }
            "Hockey" -> {
                // Handle Hockey
                holder.mealIcon.setImageResource(R.drawable.hiit)
            }
            "HIIT" -> {
                // Handle HIIT
                holder.mealIcon.setImageResource(R.drawable.hiking)
            }
            "High Intensity Interval Training" -> {
                // Handle HIIT
                holder.mealIcon.setImageResource(R.drawable.hiking)
            }
            "Kickboxing" -> {
                // Handle Kickboxing
                holder.mealIcon.setImageResource(R.drawable.kickboxing)
            }
            "Martial Arts" -> {
                // Handle Martial Arts
                holder.mealIcon.setImageResource(R.drawable.martial_arts)
            }
            "Other" -> {
                // Handle Other
                holder.mealIcon.setImageResource(R.drawable.other)
            }
            "Pickleball" -> {
                // Handle Pickleball
                holder.mealIcon.setImageResource(R.drawable.pickleball)
            }
            "Pilates" -> {
                // Handle Pilates
                holder.mealIcon.setImageResource(R.drawable.pilates)
            }
            "Power Yoga" -> {
                // Handle Power Yoga
                holder.mealIcon.setImageResource(R.drawable.power_yoga)
            }
            "Powerlifting" -> {
                // Handle Powerlifting
                holder.mealIcon.setImageResource(R.drawable.powerlifting)
            }
            "Pranayama" -> {
                // Handle Pranayama
                holder.mealIcon.setImageResource(R.drawable.pranayama)
            }
            "Running" -> {
                // Handle Running
                holder.mealIcon.setImageResource(R.drawable.running)
            }
            "Rowing Machine" -> {
                // Handle Rowing Machine
                holder.mealIcon.setImageResource(R.drawable.rowing_machine)
            }
            "Rugby" -> {
                // Handle Rugby
                holder.mealIcon.setImageResource(R.drawable.rugby)
            }
            "Skating" -> {
                // Handle Skating
                holder.mealIcon.setImageResource(R.drawable.skating)
            }
            "Skipping" -> {
                // Handle Skipping
                holder.mealIcon.setImageResource(R.drawable.skipping)
            }
            "Stairs" -> {
                // Handle Stairs
                holder.mealIcon.setImageResource(R.drawable.stairs)
            }
            "Squash" -> {
                // Handle Squash
                holder.mealIcon.setImageResource(R.drawable.squash)
            }
            "Traditional Strength Training" -> {
                // Handle Traditional Strength Training
                holder.mealIcon.setImageResource(R.drawable.traditional_strength_training)
            }
            "Strength Training" -> {
                // Handle Traditional Strength Training
                holder.mealIcon.setImageResource(R.drawable.traditional_strength_training)
            }
            "Stretching" -> {
                // Handle Stretching
                holder.mealIcon.setImageResource(R.drawable.stretching)
            }
            "Swimming" -> {
                // Handle Swimming
                holder.mealIcon.setImageResource(R.drawable.swimming)
            }
            "Table Tennis" -> {
                // Handle Table Tennis
                holder.mealIcon.setImageResource(R.drawable.table_tennis)
            }
            "Tennis" -> {
                // Handle Tennis
                holder.mealIcon.setImageResource(R.drawable.tennis)
            }
            "Track and Field Events" -> {
                // Handle Track and Field Events
                holder.mealIcon.setImageResource(R.drawable.track_field_events)
            }
            "Volleyball" -> {
                // Handle Volleyball
                holder.mealIcon.setImageResource(R.drawable.volleyball)
            }
            "Walking" -> {
                // Handle Walking
                holder.mealIcon.setImageResource(R.drawable.walking)
            }
            "Watersports" -> {
                // Handle Watersports
                holder.mealIcon.setImageResource(R.drawable.watersports)
            }
            "Wrestling" -> {
                // Handle Wrestling
                holder.mealIcon.setImageResource(R.drawable.wrestling)
            }
            "Strength Training" -> {
                // Handle Traditional Strength Training
                holder.mealIcon.setImageResource(R.drawable.traditional_strength_training)
            }
            "Yoga" -> {
                // Handle Yoga
                holder.mealIcon.setImageResource(R.drawable.yoga)
            }
            else -> {
                // Handle unknown or null workoutType
                holder.mealIcon.setImageResource(R.drawable.other)
            }
        }

    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val mealTitle: TextView = itemView.findViewById(R.id.tv_meal_title)
        val mealIcon: ImageView = itemView.findViewById(R.id.main_heading_icon)
        val delete: ImageView = itemView.findViewById(R.id.image_delete)
        val deleteLayout: LinearLayoutCompat = itemView.findViewById(R.id.layout_delete)
        val edit: ImageView = itemView.findViewById(R.id.image_edit)
        val editDeleteLayout: CardView = itemView.findViewById(R.id.btn_edit_delete)
        val addToWorkout: LinearLayoutCompat = itemView.findViewById(R.id.layout_btn_log)
        val circlePlus : ImageView = itemView.findViewById(R.id.image_circle_plus)
        val threedots : ImageView = itemView.findViewById(R.id.image_circle_plus)
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
        // Step 1: Convert string to total minutes (handles "60.0" format)
        val totalMinutes = timeString.toDoubleOrNull()?.toInt() ?: 0

        // Step 2: Convert total minutes to hours and minutes
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        // Step 3: Build the formatted string (e.g., "1 hr 0 mins")
        val formattedText = buildString {
            if (hours > 0) {
                append("$hours hr ")
            }
            if (minutes > 0 || hours == 0) { // Show minutes even if 0 when hours is 0
                append("$minutes mins")
            }
        }.trim()

        // Step 4: Create SpannableStringBuilder for styling
        val spannable = SpannableStringBuilder(formattedText)

        // Apply bold and 14sp to numbers, 10sp to units
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
                hours.toString().length + 3, // " hr"
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
    private fun formatCaloriesString(caloriesString: String): SpannableStringBuilder {
        // Step 1: Convert string to integer (ignores decimal part)
        val calories = caloriesString.toDoubleOrNull()?.toInt() ?: 0

        // Step 2: Build the formatted string (e.g., "516 cal")
        val formattedText = "$calories cal"

        // Step 3: Create SpannableStringBuilder for styling
        val spannable = SpannableStringBuilder(formattedText)

        // Style the number (bold, 14sp)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            calories.toString().length,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            AbsoluteSizeSpan(14, true),
            0,
            calories.toString().length,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Style the unit "cal" (normal, 10sp)
        spannable.setSpan(
            AbsoluteSizeSpan(10, true),
            calories.toString().length,
            formattedText.length,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }

    fun addAll(item : ArrayList<FrequentLoggedRoutine>?, pos: Int, mealLogItem : FrequentLoggedRoutine?, isClick : Boolean) {
        dataLists.clear()
        if (item != null) {
            dataLists = item
            clickPos = pos
            mealLogListData = mealLogItem
            isClickView = isClick
        }
        notifyDataSetChanged()
    }
}