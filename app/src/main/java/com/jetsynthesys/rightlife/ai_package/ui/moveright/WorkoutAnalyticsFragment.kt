package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.model.CardItem
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar.CardioStrippedProgressBar
import com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar.FatBurnStrippedProgressBar
import com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar.LightStrippedprogressBar
import com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar.RestingProgressBar
import com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar.StripedProgressBar
import com.jetsynthesys.rightlife.databinding.FragmentWorkoutAnalyticsBinding
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

data class HRDataPoint(val time: Long, val bpm: Int)

class WorkoutAnalyticsFragment : BaseFragment<FragmentWorkoutAnalyticsBinding>() {

    private lateinit var heartRateGraph: CustomHeartRateGraph
    private lateinit var customProgressBar: StripedProgressBar
    private lateinit var transparentOverlay: View
    private lateinit var workOutsAnalyticsBackButton: ImageView
    private lateinit var customProgressBarCardio: CardioStrippedProgressBar
    private lateinit var transparentOverlayCardio: View
    private lateinit var customProgressBarLight: LightStrippedprogressBar
    private lateinit var transparentOverlayLight: View
    private lateinit var restingProgressBar: RestingProgressBar
    private lateinit var transparentRestingOverlay: View
    private lateinit var customProgressBarFatBurn: FatBurnStrippedProgressBar
    private lateinit var transparentOverlayFatBurn: View
    private lateinit var light_percentage_value: TextView
    private lateinit var peak_bpm_text: TextView
    private lateinit var cardio_bpm_text: TextView
    private lateinit var fat_burn_bpm_text: TextView
    private lateinit var light_bpm_text: TextView
    private lateinit var resting_bpm_text: TextView
    private lateinit var resting_percentage_value: TextView
    private lateinit var yourHeartRateZone : ImageView
    private lateinit var light_time_value: TextView
    private lateinit var resting_time_value: TextView
    private lateinit var fat_burn_time_value: TextView
    private lateinit var fat_burn_percentage_value: TextView
    private lateinit var cardio_text_time_value: TextView
    private lateinit var cardio_text_percentage_value: TextView
    private lateinit var peak_text_time_value: TextView
    private lateinit var light_text_percentage: TextView
    private lateinit var resting_text_percentage: TextView
    private var cardItem: CardItem? = null
    private var loadingOverlay: FrameLayout? = null

    private val dataPoints = mutableListOf<HRDataPoint>()
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWorkoutAnalyticsBinding
        get() = FragmentWorkoutAnalyticsBinding::inflate

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val cardItems = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("cardItem", CardItem::class.java)
        } else {
            arguments?.getParcelable("cardItem")
        }
        cardItem = cardItems
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        heartRateGraph = view.findViewById(R.id.heartRateGraph)
        customProgressBar = view.findViewById(R.id.customProgressBar)
        transparentOverlay = view.findViewById(R.id.transparentOverlay)
        customProgressBarCardio = view.findViewById(R.id.customProgressBarCardio)
        transparentOverlayCardio = view.findViewById(R.id.transparentOverlayCardio)
        customProgressBarLight = view.findViewById(R.id.customProgressBarLight)
        transparentOverlayLight = view.findViewById(R.id.transparentOverlayLight)
        restingProgressBar = view.findViewById(R.id.customProgressBarResting)
        transparentRestingOverlay = view.findViewById(R.id.transparentOverlayResting)
        customProgressBarFatBurn = view.findViewById(R.id.customProgressBarFatBurn)
        transparentOverlayFatBurn = view.findViewById(R.id.transparentOverlayFatBurn)
        workOutsAnalyticsBackButton = view.findViewById(R.id.back_button)
        light_percentage_value = view.findViewById(R.id.light_percentage_value)
        light_time_value = view.findViewById(R.id.light_time_value)
        peak_bpm_text = view.findViewById(R.id.peak_bpm_text)
        cardio_bpm_text = view.findViewById(R.id.cardio_bpm_text)
        fat_burn_bpm_text = view.findViewById(R.id.fat_burn_bpm_text)
        light_bpm_text = view.findViewById(R.id.light_bpm_text)
        resting_bpm_text = view.findViewById(R.id.resting_bpm_text)
        resting_percentage_value = view.findViewById(R.id.resting_text_percentage)
        resting_time_value = view.findViewById(R.id.resting_time_value)
        fat_burn_time_value = view.findViewById(R.id.fat_burn_time_value)
        fat_burn_percentage_value = view.findViewById(R.id.fat_burn_percentage_value)
        cardio_text_time_value = view.findViewById(R.id.cardio_text_time_value)
        cardio_text_percentage_value = view.findViewById(R.id.cardio_text_percentage_value)
        peak_text_time_value = view.findViewById(R.id.peak_text_time_value)
        light_text_percentage = view.findViewById(R.id.light_text_percentage)
        resting_text_percentage = view.findViewById(R.id.resting_text_percentage)
        yourHeartRateZone = view.findViewById(R.id.yourHeartRateZone)

        val yourActivity = arguments?.getString("YourActivity").toString()

        workOutsAnalyticsBackButton.setOnClickListener {
            if (yourActivity.equals("YourActivity")){
                val fragment = YourActivityFragment()
                val args = Bundle()
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.flFragment, fragment, "YourActivityFragment")
                    .addToBackStack("YourActivityFragment")
                    .commit()
            }else{
                val fragment = HomeBottomTabFragment()
                val args = Bundle().apply {
                    putString("ModuleName", "MoveRight")
                }
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                    addToBackStack(null)
                    commit()
                }
            }
        }

        // Back press handling
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (yourActivity.equals("YourActivity")){
                    val fragment = YourActivityFragment()
                    val args = Bundle()
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.flFragment, fragment, "YourActivityFragment")
                        .addToBackStack("YourActivityFragment")
                        .commit()
                }else{
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle().apply {
                        putString("ModuleName", "MoveRight")
                    }
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                        addToBackStack(null)
                        commit()
                    }
                }
            }
        })
        // Retrieve the CardItem from the Bundle
        val cardItems = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("cardItem", CardItem::class.java)
        } else {
            arguments?.getParcelable("cardItem")
        }
        cardItem = cardItems
        // Set the CardItem data to the UI
        cardItem?.let { item ->
            // Set the title
            view.findViewById<TextView>(R.id.functional_strength_text).text = item.title
            when (item?.title) {
                "American Football" -> {
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.american_football)// Handle American Football
                }
                "Archery" -> {
                    // Handle Archery
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.archery)
                }
                "Athletics" -> {
                    // Handle Athletics
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.athelete_search)
                }
                "Australian Football" -> {
                    // Handle Australian Football
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.australian_football)
                }
                "Badminton" -> {
                    // Handle Badminton
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.badminton)
                }
                "Barre" -> {
                    // Handle Barre
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.barre)
                }
                "Baseball" -> {
                    // Handle Baseball
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.baseball)
                }
                "Basketball" -> {
                    // Handle Basketball
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.basketball)
                }
                "Boxing" -> {
                    // Handle Boxing
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.boxing)

                }
                "Climbing" -> {
                    // Handle Climbing
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.climbing)
                }
                "Core Training" -> {
                    // Handle Core Training
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.core_training)
                }
                "Cycling" -> {
                    // Handle Cycling
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.cycling)
                }
                "Cricket" -> {
                    // Handle Cricket
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.cricket)
                }
                "Cross Training" -> {
                    // Handle Cross Training
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.cross_training)
                }
                "Dance" -> {
                    // Handle Dance
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.dance)
                }
                "Disc Sports" -> {
                    // Handle Disc Sports
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.disc_sports)
                }
                "Elliptical" -> {
                    // Handle Elliptical
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.elliptical)
                }
                "Football" -> {
                    // Handle Football
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.football)
                }
                "Functional Strength Training" -> {
                    // Handle Functional Strength Training
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.functional_strength_training)
                }
                "Golf" -> {
                    // Handle Golf
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.golf)
                }
                "Gymnastics" -> {
                    // Handle Gymnastics
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.gymnastics)
                }
                "Handball" -> {
                    // Handle Handball
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.handball)
                }
                "Hiking" -> {
                    // Handle Hiking
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.hiking)
                }
                "Hockey" -> {
                    // Handle Hockey
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.hockey)
                }
                "HIIT" -> {
                    // Handle HIIT
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.hiit)
                }
                "Kickboxing" -> {
                    // Handle Kickboxing
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.kickboxing)
                }
                "Martial Arts" -> {
                    // Handle Martial Arts
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.martial_arts)
                }
                "Other" -> {
                    // Handle Other
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.other)
                }
                "Pickleball" -> {
                    // Handle Pickleball
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.pickleball)
                }
                "Pilates" -> {
                    // Handle Pilates
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.pilates)
                }
                "Power Yoga" -> {
                    // Handle Power Yoga
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.power_yoga)
                }
                "Powerlifting" -> {
                    // Handle Powerlifting
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.powerlifting)
                }
                "Pranayama" -> {
                    // Handle Pranayama
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.pranayama)
                }
                "Running" -> {
                    // Handle Running
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.running)
                }
                "Rowing Machine" -> {
                    // Handle Rowing Machine
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.rowing_machine)
                }
                "Rugby" -> {
                    // Handle Rugby
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.rugby)
                }
                "Skating" -> {
                    // Handle Skating
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.skating)
                }
                "Skipping" -> {
                    // Handle Skipping
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.skipping)
                }
                "Stairs" -> {
                    // Handle Stairs
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.stairs)
                }
                "Squash" -> {
                    // Handle Squash
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.squash)
                }
                "Traditional Strength Training" -> {
                    // Handle Traditional Strength Training
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.traditional_strength_training)
                }
                "Stretching" -> {
                    // Handle Stretching
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.stretching)
                }
                "Swimming" -> {
                    // Handle Swimming
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.swimming)
                }
                "Table Tennis" -> {
                    // Handle Table Tennis
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.table_tennis)
                }
                "Tennis" -> {
                    // Handle Tennis
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.tennis)
                }
                "Track and Field Events" -> {
                    // Handle Track and Field Events
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.track_field_events)
                }
                "Volleyball" -> {
                    // Handle Volleyball
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.volleyball)
                }
                "Walking" -> {
                    // Handle Walking
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.walking)
                }
                "Watersports" -> {
                    // Handle Watersports
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.watersports)
                }
                "Wrestling" -> {
                    // Handle Wrestling
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.wrestling)
                }
                "Yoga" -> {
                    // Handle Yoga
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.yoga)
                }
                else -> {
                    // Handle unknown or null workoutType
                    view.findViewById<ImageView>(R.id.functional_strength_icon).setImageResource(R.drawable.other)
                }
            }
            // Set the timeline (start and end times)
         //   val startTime = item.heartRateData.firstOrNull()?.date?.let { convertUtcToSystemLocal(it) } ?: "N/A"
          //  val endTime = item.heartRateData.lastOrNull()?.date?.let { convertUtcToSystemLocal(it) } ?: "N/A"
         //   view.findViewById<TextView>(R.id.timeline_text).text = "$startTime to $endTime"
            /*if (item.heartRateData.lastOrNull()?.date != null && item.heartRateData.firstOrNull()?.date != null) {
                view.findViewById<TextView>(R.id.timeline_text).text = getTimeDifference(item.heartRateData.lastOrNull()?.date!!, item.heartRateData.firstOrNull()?.date!!)
            }*/
            // Set the duration, calories burned, and average heart rate
            view.findViewById<TextView>(R.id.duration_text).text = item.duration
            val convertedDurationText = run {
                val parts = item.duration.split(" ")
                val hours = if (parts.contains("hr")) parts[0].toIntOrNull() ?: 0 else 0
                val minutes = if (parts.contains("hr")) parts[2].toIntOrNull() ?: 0 else parts[0].toIntOrNull() ?: 0
                String.format("%02d:%02d:%02d", hours + (minutes / 60), minutes % 60, 0)
            }
            view.findViewById<TextView>(R.id.timeline_text).text = convertedDurationText
            view.findViewById<TextView>(R.id.calories_text).text = item.caloriesBurned
            view.findViewById<TextView>(R.id.avg_heart_rate_text_value).text = item.avgHeartRate.split(" ").getOrNull(0)
            fun calculatePercentageDuration(convertedDurationText: String, percentage: Double): String {
                // Step 1: Parse convertedDurationText to total seconds
                val timeParts = convertedDurationText.split(":")
                val hours = timeParts[0].toIntOrNull() ?: 0
                val minutes = timeParts[1].toIntOrNull() ?: 0
                val seconds = timeParts[2].toIntOrNull() ?: 0
                val totalSeconds = hours * 3600 + minutes * 60 + seconds

                // Step 2: Calculate percentage of total seconds and round to nearest integer
                val resultSeconds = (totalSeconds * percentage / 100.0).roundToInt()

                // Step 3: Convert back to HH:mm:ss format
                val resultHours = resultSeconds / 3600
                val resultMinutes = (resultSeconds % 3600) / 60
                val resultRemainingSeconds = resultSeconds % 60
                return String.format("%02d:%02d:%02d", resultHours, resultMinutes, resultRemainingSeconds)
            }
            // Set heart rate zone percentages and minutes
            light_text_percentage.text = "${item.heartRateZonePercentages.lightZone}%"
            light_percentage_value.text = "${item.heartRateZonePercentages.fatBurnZone}%"
            fat_burn_percentage_value.text = "${item.heartRateZonePercentages.cardioZone}%"
            cardio_text_percentage_value.text = "${item.heartRateZonePercentages.peakZone}%"
            peak_text_time_value.text =  item?.heartRateZonePercentages?.peakZone?.toDouble()
                ?.let { calculatePercentageDuration(convertedDurationText, it) }
            cardio_text_time_value.text = item?.heartRateZonePercentages?.cardioZone?.toDouble()
                ?.let { calculatePercentageDuration(convertedDurationText, it) }
            fat_burn_time_value.text =  item?.heartRateZonePercentages?.fatBurnZone?.toDouble()
                ?.let { calculatePercentageDuration(convertedDurationText, it) }
            light_time_value.text =  item?.heartRateZonePercentages?.lightZone?.toDouble()
                ?.let { calculatePercentageDuration(convertedDurationText, it) }
            resting_time_value.text = item?.heartRateZonePercentages?.belowLight?.toDouble()
                ?.let { calculatePercentageDuration(convertedDurationText, it) }
            resting_text_percentage.text = "${item.heartRateZonePercentages.belowLight}%"
            val bpmValue = item.heartRateZones.lightZone.firstOrNull()
            resting_bpm_text.text = bpmValue?.let { "< $it BPM" } ?: "--"
            val lightzoneend = item.heartRateZones.lightZone[1]
            light_bpm_text.text = "${bpmValue}-${lightzoneend} BPM"
            val firstFat = item.heartRateZones.fatBurnZone[0]
            val fatSecond = item.heartRateZones.fatBurnZone[1]
            fat_burn_bpm_text.text = "${firstFat}-${fatSecond} BPM"
            val cardioFirst = item.heartRateZones.cardioZone[0]
            val cardioSecond = item.heartRateZones.cardioZone[1]
            cardio_bpm_text.text = "${cardioFirst}-${cardioSecond} BPM"
            val peakFirst = item.heartRateZones.peakZone[0]
            val peakSecond = item.heartRateZones.peakZone[1]
            peak_bpm_text.text = "${peakFirst}-${peakSecond} BPM"

            // Set progress bar percentages and overlay widths from heartRateZonePercentages
            customProgressBar.post {
                customProgressBar.progress = item.heartRateZonePercentages.peakZone
                val progressBarWidth = customProgressBar.width
                val filledWidth = (progressBarWidth * (item.heartRateZonePercentages.peakZone / 100.0)).toInt()
                val layoutParams = transparentOverlay.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = filledWidth
                layoutParams.startToStart = customProgressBar.id
                layoutParams.marginStart = 0
                transparentOverlay.layoutParams = layoutParams
            }

            customProgressBarLight.post {
                customProgressBarLight.progress = item.heartRateZonePercentages.lightZone
                val progressBarWidth = customProgressBarLight.width
                val filledWidth = (progressBarWidth * (item.heartRateZonePercentages.lightZone / 100.0)).toInt()
                val layoutParams = transparentOverlayLight.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = filledWidth
                layoutParams.startToStart = customProgressBarLight.id
                layoutParams.marginStart = 0
                transparentOverlayLight.layoutParams = layoutParams
            }

            restingProgressBar.post {
                restingProgressBar.progress = item.heartRateZonePercentages.belowLight
                val progressBarWidth = restingProgressBar.width
                val filledWidth = (progressBarWidth * (item.heartRateZonePercentages.belowLight / 100.0)).toInt()
                val layoutParams = transparentOverlayLight.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = filledWidth
                layoutParams.startToStart = restingProgressBar.id
                layoutParams.marginStart = 0
                transparentRestingOverlay.layoutParams = layoutParams
            }

            customProgressBarFatBurn.post {
                customProgressBarFatBurn.progress = item.heartRateZonePercentages.fatBurnZone
                val progressBarWidth = customProgressBarFatBurn.width
                val filledWidth = (progressBarWidth * (item.heartRateZonePercentages.fatBurnZone / 100.0)).toInt()
                val layoutParams = transparentOverlayFatBurn.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = filledWidth
                layoutParams.startToStart = customProgressBarFatBurn.id
                layoutParams.marginStart = 0
                transparentOverlayFatBurn.layoutParams = layoutParams
            }

            customProgressBarCardio.post {
                customProgressBarCardio.progress = item.heartRateZonePercentages.cardioZone
                val progressBarWidth = customProgressBarCardio.width
                val filledWidth = (progressBarWidth * (item.heartRateZonePercentages.cardioZone / 100.0)).toInt()
                val layoutParams = transparentOverlayCardio.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.width = filledWidth
                layoutParams.startToStart = customProgressBarCardio.id
                layoutParams.marginStart = 0
                transparentOverlayCardio.layoutParams = layoutParams
            }
            // Convert heartRateData to HRDataPoint and set it on the graph
            val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
            dataPoints.clear()
            item.heartRateData.forEach { heartRateData ->
                val zdt = ZonedDateTime.parse(heartRateData.date)
                val millis = zdt.toInstant().toEpochMilli()
               // val time = parseTime(heartRateData.date)
             //   val formattedTime = zdt.format(formatter).uppercase(Locale.ENGLISH)
                val bpm = heartRateData.heartRate.toInt()
                dataPoints.add(HRDataPoint(millis, bpm))
            }
            heartRateGraph.setData(dataPoints)
            heartRateGraph.setWorkoutData(item)
        } ?: run {
            // Handle the case where no CardItem is provided
            view.findViewById<TextView>(R.id.functional_strength_text).text = "No Data"
            Toast.makeText(requireContext(), "No workout data available", Toast.LENGTH_SHORT).show()
        }

        yourHeartRateZone.setOnClickListener {
            val yourHeartRateZonesInfoBottomSheet = YourHeartRateZonesInfoBottomSheet()
            yourHeartRateZonesInfoBottomSheet.isCancelable = true
            parentFragment.let { yourHeartRateZonesInfoBottomSheet.show(childFragmentManager, "YourHeartRateZonesInfoBottomSheet") }
        }
    }

    fun getTimeDifference(isoTime1: String, isoTime2: String): String {
        val time1 = ZonedDateTime.parse(isoTime1)
        val time2 = ZonedDateTime.parse(isoTime2)
        val duration = Duration.between(time2, time1).abs()
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    // Helper function to format the timestamp to a time string (e.g., "6:30 am")
    private fun formatTime(timestamp: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(timestamp)
            timeFormat.format(date).uppercase()
        } catch (e: Exception) {
            "N/A"
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
    // Helper function to parse the timestamp to a Long (milliseconds)
    private fun parseTime(timestamp: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.parse(timestamp)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    private fun setProgressLineBarProgress(progressLineBar: View, progress: Int) {
        val progressPercentage = progress.coerceIn(0, 100)
        val totalWidth = progressLineBar.width
        val pinkWidth = (progressPercentage / 100.0 * totalWidth).toInt()
        val pinkDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#FFC0CB"))
            cornerRadius = 0f
        }
        val whiteGreyDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
            setStroke(2, Color.GRAY)
            cornerRadius = 0f
        }
        val layerDrawable = LayerDrawable(arrayOf(pinkDrawable, whiteGreyDrawable))
        layerDrawable.setLayerInset(0, 0, 0, totalWidth - pinkWidth, 0)
        layerDrawable.setLayerInset(1, pinkWidth, 0, 0, 0)
        progressLineBar.background = layerDrawable
    }

    private fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }

    private fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}