package com.jetsynthesys.rightlife.ai_package.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.WorkoutList

class WorkoutAdapter(private var context :Context, private var workoutList: List<WorkoutList>, private val onItemClick: (WorkoutList) -> Unit) :
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val workoutTitle: TextView = view.findViewById(R.id.textTitle)
        private val workoutImage: ImageView = view.findViewById(R.id.iconUrl)
        private val forwardIcon: ImageView = view.findViewById(R.id.imageRightArrow)

        fun bind(item: WorkoutList) {
            workoutTitle.text = item.title
           /* val imageBaseUrl = ApiClient.CDN_URL_QA + item.iconUrl
            Glide.with(context)
                .load(imageBaseUrl)
                .placeholder(R.drawable.athelete_search)
                .error(R.drawable.athelete_search)
                .into(workoutImage)*/
            when (item?.title) {
                "American Football" -> {
                    workoutImage.setImageResource(R.drawable.american_football)// Handle American Football
                }
                "Archery" -> {
                    // Handle Archery
                    workoutImage.setImageResource(R.drawable.archery)
                }
                "Athletics" -> {
                    // Handle Athletics
                    workoutImage.setImageResource(R.drawable.athelete_search)
                }
                "Australian Football" -> {
                    // Handle Australian Football
                    workoutImage.setImageResource(R.drawable.australian_football)
                }
                "Badminton" -> {
                    // Handle Badminton
                    workoutImage.setImageResource(R.drawable.badminton)
                }
                "Barre" -> {
                    // Handle Barre
                    workoutImage.setImageResource(R.drawable.barre)
                }
                "Baseball" -> {
                    // Handle Baseball
                    workoutImage.setImageResource(R.drawable.baseball)
                }
                "Basketball" -> {
                    // Handle Basketball
                    workoutImage.setImageResource(R.drawable.basketball)
                }
                "Boxing" -> {
                    // Handle Boxing
                    workoutImage.setImageResource(R.drawable.boxing)

                }
                "Climbing" -> {
                    // Handle Climbing
                    workoutImage.setImageResource(R.drawable.climbing)
                }
                "Core Training" -> {
                    // Handle Core Training
                    workoutImage.setImageResource(R.drawable.core_training)
                }
                "Cycling" -> {
                    // Handle Cycling
                    workoutImage.setImageResource(R.drawable.cycling)
                }
                "Cricket" -> {
                    // Handle Cricket
                    workoutImage.setImageResource(R.drawable.cricket)
                }
                "Cross Training" -> {
                    // Handle Cross Training
                    workoutImage.setImageResource(R.drawable.cross_training)
                }
                "Dance" -> {
                    // Handle Dance
                    workoutImage.setImageResource(R.drawable.dance)
                }
                "Disc Sports" -> {
                    // Handle Disc Sports
                    workoutImage.setImageResource(R.drawable.disc_sports)
                }
                "Elliptical" -> {
                    // Handle Elliptical
                    workoutImage.setImageResource(R.drawable.elliptical)
                }
                "Football" -> {
                    // Handle Football
                    workoutImage.setImageResource(R.drawable.football)
                }
                "Functional Strength Training" -> {
                    // Handle Functional Strength Training
                    workoutImage.setImageResource(R.drawable.functional_strength_training)
                }
                "Golf" -> {
                    // Handle Golf
                    workoutImage.setImageResource(R.drawable.golf)
                }
                "Gymnastics" -> {
                    // Handle Gymnastics
                    workoutImage.setImageResource(R.drawable.gymnastics)
                }
                "Handball" -> {
                    // Handle Handball
                    workoutImage.setImageResource(R.drawable.handball)
                }
                "Hiking" -> {
                    // Handle Hiking
                    workoutImage.setImageResource(R.drawable.hockey)
                }
                "Hockey" -> {
                    // Handle Hockey
                    workoutImage.setImageResource(R.drawable.hiit)
                }
                "HIIT" -> {
                    // Handle HIIT
                    workoutImage.setImageResource(R.drawable.hiking)
                }
                "High Intensity Interval Training" -> {
                    // Handle HIIT
                    workoutImage.setImageResource(R.drawable.hiking)
                }
                "Kickboxing" -> {
                    // Handle Kickboxing
                    workoutImage.setImageResource(R.drawable.kickboxing)
                }
                "Martial Arts" -> {
                    // Handle Martial Arts
                    workoutImage.setImageResource(R.drawable.martial_arts)
                }
                "Other" -> {
                    // Handle Other
                    workoutImage.setImageResource(R.drawable.other)
                }
                "Pickleball" -> {
                    // Handle Pickleball
                    workoutImage.setImageResource(R.drawable.pickleball)
                }
                "Pilates" -> {
                    // Handle Pilates
                    workoutImage.setImageResource(R.drawable.pilates)
                }
                "Power Yoga" -> {
                    // Handle Power Yoga
                    workoutImage.setImageResource(R.drawable.power_yoga)
                }
                "Powerlifting" -> {
                    // Handle Powerlifting
                    workoutImage.setImageResource(R.drawable.powerlifting)
                }
                "Pranayama" -> {
                    // Handle Pranayama
                    workoutImage.setImageResource(R.drawable.pranayama)
                }
                "Running" -> {
                    // Handle Running
                    workoutImage.setImageResource(R.drawable.running)
                }
                "Rowing Machine" -> {
                    // Handle Rowing Machine
                    workoutImage.setImageResource(R.drawable.rowing_machine)
                }
                "Rugby" -> {
                    // Handle Rugby
                    workoutImage.setImageResource(R.drawable.rugby)
                }
                "Skating" -> {
                    // Handle Skating
                    workoutImage.setImageResource(R.drawable.skating)
                }
                "Skipping" -> {
                    // Handle Skipping
                    workoutImage.setImageResource(R.drawable.skipping)
                }
                "Stairs" -> {
                    // Handle Stairs
                    workoutImage.setImageResource(R.drawable.stairs)
                }
                "Squash" -> {
                    // Handle Squash
                    workoutImage.setImageResource(R.drawable.squash)
                }
                "Traditional Strength Training" -> {
                    // Handle Traditional Strength Training
                    workoutImage.setImageResource(R.drawable.traditional_strength_training)
                }
                "Strength Training" -> {
                    // Handle Traditional Strength Training
                    workoutImage.setImageResource(R.drawable.traditional_strength_training)
                }
                "Stretching" -> {
                    // Handle Stretching
                    workoutImage.setImageResource(R.drawable.stretching)
                }
                "Swimming" -> {
                    // Handle Swimming
                    workoutImage.setImageResource(R.drawable.swimming)
                }
                "Table Tennis" -> {
                    // Handle Table Tennis
                    workoutImage.setImageResource(R.drawable.table_tennis)
                }
                "Tennis" -> {
                    // Handle Tennis
                    workoutImage.setImageResource(R.drawable.tennis)
                }
                "Track and Field Events" -> {
                    // Handle Track and Field Events
                    workoutImage.setImageResource(R.drawable.track_field_events)
                }
                "Volleyball" -> {
                    // Handle Volleyball
                    workoutImage.setImageResource(R.drawable.volleyball)
                }
                "Walking" -> {
                    // Handle Walking
                    workoutImage.setImageResource(R.drawable.walking)
                }
                "Watersports" -> {
                    // Handle Watersports
                    workoutImage.setImageResource(R.drawable.watersports)
                }
                "Wrestling" -> {
                    // Handle Wrestling
                    workoutImage.setImageResource(R.drawable.wrestling)
                }
                "Strength Training" -> {
                    // Handle Traditional Strength Training
                    workoutImage.setImageResource(R.drawable.traditional_strength_training)
                }
                "Yoga" -> {
                    // Handle Yoga
                    workoutImage.setImageResource(R.drawable.yoga)
                }
                else -> {
                    // Handle unknown or null workoutType
                    workoutImage.setImageResource(R.drawable.other)
                }
            }

          //  forwardIcon.setImageResource(item.icon2)

            // Handle item click
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_layout_ai, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workoutList[position])

    }

    override fun getItemCount(): Int = workoutList.size

    fun updateList(newList: List<WorkoutList>) {
        workoutList = newList
        notifyDataSetChanged()
    }
}
