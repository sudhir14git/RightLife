package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.R.*
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.request.WeightIntakeRequest
import com.jetsynthesys.rightlife.ai_package.model.response.LogWeightResponse
import com.jetsynthesys.rightlife.ai_package.model.response.WaterIntakeResponse
import com.jetsynthesys.rightlife.ai_package.model.response.WeightResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.LogWeightRulerAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.macros.MultilineXAxisRenderer
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.utils.BadgeLimitLineRenderer
import com.jetsynthesys.rightlife.databinding.BottomsheetLogWeightSelectionBinding
import com.jetsynthesys.rightlife.databinding.FragmentWeightTrackerBinding
import com.jetsynthesys.rightlife.ui.utility.ConversionUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.floor

class WeightTrackerFragment : BaseFragment<FragmentWeightTrackerBinding>() {

    private lateinit var lineChart: LineChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var stripsContainer: FrameLayout
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var backwardImage: ImageView
    private lateinit var weight_tracker_layout: CardView
    private lateinit var forwardImage: ImageView
    private lateinit var selectedDate: TextView
    private lateinit var weight_description_heading: TextView
    private lateinit var weight_description_text: TextView
    private var selectedWeekDate: String = ""
    private var selectedMonthDate: String = ""
    private var selectedHalfYearlyDate: String = ""
    private lateinit var selectedItemDate : TextView
    private lateinit var selectHeartRateLayout : CardView
    private lateinit var selectedCalorieTv : TextView
    private lateinit var loss_new_weight_filled : LinearLayout
    private lateinit var logWeightRulerAdapter: LogWeightRulerAdapter
    private val numbers = mutableListOf<Float>()
    private lateinit var weightIntake: TextView
    private lateinit var weightIntakeUnit: TextView
    private lateinit var averageWaterIntake: TextView
    private lateinit var averageHeading: TextView
    private lateinit var percentageTv: TextView
    private lateinit var weightLastLogDateTv : TextView
    private lateinit var averageGoalLayout : LinearLayoutCompat
    private var loadingOverlay : FrameLayout? = null
    private val viewModel: HydrationViewModelNew by viewModels()


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWeightTrackerBinding
        get() = FragmentWeightTrackerBinding::inflate


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), color.meal_log_background))

        lineChart = view.findViewById(R.id.heartLineChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        stripsContainer = view.findViewById(R.id.stripsContainer)
        layoutLineChart = view.findViewById(R.id.lyt_line_chart)
        backwardImage = view.findViewById(R.id.backward_image_calorie)
        forwardImage = view.findViewById(R.id.forward_image_calorie)
        selectedDate = view.findViewById(R.id.selectedDate)
        selectHeartRateLayout = view.findViewById(R.id.selectHeartRateLayout)
        selectedItemDate = view.findViewById(R.id.selectedItemDate)
        selectedCalorieTv = view.findViewById(R.id.selectedCalorieTv)
        weight_description_heading = view.findViewById(R.id.weight_description_heading)
        weight_description_text = view.findViewById(R.id.weight_description_text)
        weight_tracker_layout = view.findViewById(R.id.weight_tracker_layout)
        loss_new_weight_filled = view.findViewById(R.id.loss_new_weight_filled)
        weightIntake = view.findViewById(R.id.weightIntake)
        weightIntakeUnit = view.findViewById(R.id.weightIntakeUnit)
        averageGoalLayout = view.findViewById(R.id.averageGoalLayout)
        percentageTv = view.findViewById(R.id.percentage_text)
        averageWaterIntake = view.findViewById(R.id.average_number)
        val backIc = view.findViewById<ImageView>(R.id.backIc)
        weightLastLogDateTv = view.findViewById(R.id.weightLastLogDateTv)

        backIc.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle()
            args.putString("ModuleName", "EatRight")
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        // Show Week data by default
        radioGroup.check(R.id.rbMonth)
        fetchWeightData("last_monthly")
        //setupLineChart()
        loss_new_weight_filled.setOnClickListener {
            showLogWeightBottomSheet()
        }

        // Handle Radio Button Selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            weight_tracker_layout.visibility = if (checkedId == R.id.rbWeek) View.VISIBLE else View.GONE

            when (checkedId) {
                R.id.rbWeek ->{
                    layoutLineChart.visibility =View.VISIBLE
                    fetchWeightData("last_weekly")
                }
                R.id.rbMonth ->{
                    layoutLineChart.visibility =View.VISIBLE
                    fetchWeightData("last_monthly")
                }
                R.id.rbSixMonths -> {
                    Toast.makeText(requireContext(),"Comming Soon",Toast.LENGTH_SHORT).show()
                    layoutLineChart.visibility =View.GONE
                    //fetchWaterIntakeData("last_six_months")
                }
            }
        }

        backwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab: String = "Month"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }

            /*if (selectedTab.contentEquals("Week")) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dateString = selectedWeekDate
                val date = dateFormat.parse(dateString)
                calendar.time = date!!
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.set(year, month, day)
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val dateStr = dateFormat.format(calendar.time)
                selectedWeekDate = dateStr
                fetchWeightData("last_weekly")
            } else*/ if (selectedTab.contentEquals("Month")) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dateString = selectedMonthDate
                val date = dateFormat.parse(dateString)
                calendar.time = date!!
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            val dateStr = dateFormat.format(calendar.time)
            // val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
            selectedMonthDate = dateStr
            fetchWeightData("last_monthly")
        } else {
            Toast.makeText(requireContext(),"Comming Soon",Toast.LENGTH_SHORT).show()
            layoutLineChart.visibility =View.GONE
            /*selectedHalfYearlyDate = ""
            fetchWeightData("last_six_months")*/
        }
        }

        forwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab: String = "Month"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val currentDate: String = currentDateTime.format(formatter)

            /*if (selectedTab.contentEquals("Week")) {
                if (!selectedWeekDate.contentEquals(currentDate)) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val dateString = selectedWeekDate
                    val date = dateFormat.parse(dateString)
                    calendar.time = date!!
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, +7)
                    val dateStr = dateFormat.format(calendar.time)
                    selectedWeekDate = dateStr
                    fetchWeightData("last_weekly")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }
            } else*/ if (selectedTab.contentEquals("Month")) {
                if (!selectedMonthDate.contentEquals(currentDate)) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val dateString = selectedMonthDate
                    val date = dateFormat.parse(dateString)
                    calendar.time = date!!
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, +30)
                    val dateStr = dateFormat.format(calendar.time)
                    //  val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                    selectedMonthDate = dateStr
                    fetchWeightData("last_monthly")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }
            } else {
            Toast.makeText(requireContext(),"Comming Soon",Toast.LENGTH_SHORT).show()
            layoutLineChart.visibility =View.GONE
                /*if (!selectedHalfYearlyDate.contentEquals(currentDate)) {
                    selectedHalfYearlyDate = ""
                    fetchWeightData("last_six_months")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }*/
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = HomeBottomTabFragment()
            val args = Bundle()
            args.putString("ModuleName", "EatRight")
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }
    }

   private fun showLogWeightBottomSheet() {
       // Create and configure BottomSheetDialog
       val bottomSheetDialog = BottomSheetDialog(requireActivity())
       var selectedLabel = " kg"
       var selectedWeight =  weightIntake.text.toString() + " " +  weightIntakeUnit.text.toString()
       if (selectedWeight.isEmpty()) {
           selectedWeight = "50 kg"
       } else {
           val w = selectedWeight.split(" ")
           selectedLabel = " ${w[1]}"
       }
       // Inflate the BottomSheet layout
       val dialogBinding = BottomsheetLogWeightSelectionBinding.inflate(layoutInflater)
       val bottomSheetView = dialogBinding.root
       bottomSheetDialog.setContentView(bottomSheetView)
       dialogBinding.selectedNumberText.text = selectedWeight
     //  if (selectedLabel == " lbs") {
          // dialogBinding.switchWeightMetric.isChecked = true
          // dialogBinding.tabGroup.i
     //  }

//       val thumbColors = ColorStateList(
//           arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
//           intArrayOf(Color.parseColor("#03B27B"), Color.parseColor("#03B27B"))
//       )
//
//       val trackColors = ColorStateList(
//           arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
//           intArrayOf(Color.parseColor("#F2F2F2"), Color.parseColor("#F2F2F2"))
//       )
//
//       dialogBinding.switchWeightMetric.thumbTintList = thumbColors
//       dialogBinding.switchWeightMetric.trackTintList = trackColors
//       dialogBinding.switchWeightMetric.setOnCheckedChangeListener { buttonView, isChecked ->
//           val w = selectedWeight.split(" ")
//           if (isChecked) {
//               selectedLabel = " lbs"
//               selectedWeight = ConversionUtils.convertLbsToKgs(w[0])
//               setLbsValue()
//           } else {
//               selectedLabel = " kgs"
//               selectedWeight = ConversionUtils.convertKgToLbs(w[0])
//               setKgsValue()
//           }
//           dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedWeight.toDouble() * 10).toInt())
//           selectedWeight += selectedLabel
//           dialogBinding.selectedNumberText.text = selectedWeight
//       }

       val layoutManager =
           LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
       dialogBinding.rulerView.layoutManager = layoutManager
       // Generate numbers with increments of 0.1

       for (i in 0..2000) {
           numbers.add(i / 10f) // Increment by 0.1
       }

       logWeightRulerAdapter = LogWeightRulerAdapter(numbers) { number ->
           // Handle the selected number
       }
       dialogBinding.rulerView.adapter = logWeightRulerAdapter

       if (selectedLabel == " kg"){
           dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_weight_log_left_selected)
           dialogBinding.kgOption.setTextColor(Color.WHITE)
           dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_weight_log_right_unselected)
           dialogBinding.lbsOption.setTextColor(Color.BLACK)
           setKgsValue()
           dialogBinding.selectedNumberText.text = selectedWeight
       }else{
           dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_weight_log_right_selected)
           dialogBinding.lbsOption.setTextColor(Color.WHITE)
           dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_weight_log_left_unselected)
           dialogBinding.kgOption.setTextColor(Color.BLACK)
           setLbsValue()
           dialogBinding.selectedNumberText.text = selectedWeight
       }

       dialogBinding.kgOption.setOnClickListener {
           dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_weight_log_left_selected)
           dialogBinding.kgOption.setTextColor(Color.WHITE)
           dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_weight_log_right_unselected)
           dialogBinding.lbsOption.setTextColor(Color.BLACK)
           val w = selectedWeight.split(" ")
           selectedLabel = " kg"
           selectedWeight = ConversionUtils.convertKgToLbs(w[0])
           setKgsValue()
           dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedWeight.toDouble() * 10).toInt())
           selectedWeight += selectedLabel
           dialogBinding.selectedNumberText.text = selectedWeight
       }

       dialogBinding.lbsOption.setOnClickListener {
           dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_weight_log_right_selected)
           dialogBinding.lbsOption.setTextColor(Color.WHITE)
           dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_weight_log_left_unselected)
           dialogBinding.kgOption.setTextColor(Color.BLACK)
           val w = selectedWeight.split(" ")
           selectedLabel = " lbs"
           selectedWeight = ConversionUtils.convertLbsToKgs(w[0])
           setLbsValue()
           dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedWeight.toDouble() * 10).toInt())
           selectedWeight += selectedLabel
           dialogBinding.selectedNumberText.text = selectedWeight
       }

       // Center number with snap alignment
       val snapHelper: SnapHelper = LinearSnapHelper()
       snapHelper.attachToRecyclerView(dialogBinding.rulerView)

       dialogBinding.rulerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
           override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
               super.onScrollStateChanged(recyclerView, newState)
               if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                   // Get the currently snapped position
                   val snappedView = snapHelper.findSnapView(recyclerView.layoutManager)
                   if (snappedView != null) {
                       val position = recyclerView.layoutManager!!.getPosition(snappedView)
                       val snappedNumber = numbers[position]
                       //selected_number_text.setText("$snappedNumber Kg")
                       dialogBinding.selectedNumberText.text = "$snappedNumber $selectedLabel"
                       selectedWeight = dialogBinding.selectedNumberText.text.toString()
                   }
               }
           }
       })

       dialogBinding.rlRulerContainer.post {
           // Get the width of the parent LinearLayout
           val parentWidth: Int = dialogBinding.rlRulerContainer.width
           // Calculate horizontal padding (half of parent width)
           val paddingHorizontal = parentWidth / 2
           // Set horizontal padding programmatically
           dialogBinding.rulerView.setPadding(
               paddingHorizontal,
               dialogBinding.rulerView.paddingTop,
               paddingHorizontal,
               dialogBinding.rulerView.paddingBottom
           )
       }

       // Scroll to the center after layout is measured
       dialogBinding.rulerView.post {
           // Calculate the center position
           val itemCount =
               if (dialogBinding.rulerView.adapter != null) dialogBinding.rulerView.adapter!!.itemCount else 0
           val centerPosition = itemCount / 2
           // Scroll to the center position
           layoutManager.scrollToPositionWithOffset(centerPosition, 0)
       }

       dialogBinding.rulerView.post {
           dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedWeight.split(" ")[0].toDouble() * 10).toInt())
       }

       dialogBinding.btnConfirm.setOnClickListener {
           // bottomSheetDialog.dismiss()
           dialogBinding.rulerView.adapter = null
           val fullWeight = selectedWeight.trim()
           val parts = fullWeight.split(Regex("\\s+"))
           val weightValue = parts[0]   // "50.7"
           val weightUnit = parts.getOrElse(1) { "kg" }
          /* weightIntake.text = weightValue
           weightIntakeUnit.text = weightUnit*/
           val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
          // val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
           val nowInstant = Instant.now()
           val currentDate = nowInstant.getMealLogDate(4)
           val request = WeightIntakeRequest(
               userId = userId,
               source = "android",
               type = weightUnit,
               waterMl = weightValue.toFloat(),
               date = currentDate
           )
           val call = ApiClient.apiServiceFastApi.logWeightIntake(request)
           call.enqueue(object : Callback<LogWeightResponse> {
               override fun onResponse(
                   call: Call<LogWeightResponse>,
                   response: Response<LogWeightResponse>
               ) {
                   if (response.isSuccessful) {
                       val responseBody = response.body()
                       bottomSheetDialog.dismiss()
                       dialogBinding.rulerView.adapter = null
                       val fullWeight = selectedWeight.trim()
                       val parts = fullWeight.split(Regex("\\s+"))
                       val weightValue = parts[0]   // "50.7"
                       val weightUnit = parts.getOrElse(1) { "kg" }
                       weightIntake.text = response.body()?.weight.toString()
                       weightIntakeUnit.text = response.body()?.type.toString()
                       weightLastLogDateTv.text = response.body()?.date.toString()
                       Log.d("LogWaterAPI", "Success: $responseBody")
                       fetchWeightData("last_monthly")
                   } else {
                       bottomSheetDialog.dismiss()
                       Log.e(
                           "LogWaterAPI",
                           "Error: ${response.code()} - ${response.errorBody()?.string()}"
                       )
                   }
               }
               override fun onFailure(call: Call<LogWeightResponse>, t: Throwable) {
                   bottomSheetDialog.dismiss()
                   Log.e("LogWaterAPI", "Failure: ${t.localizedMessage}")
               }
           })
           //   binding.tvWeight.text = selectedWeight
       }

       dialogBinding.closeIV.setOnClickListener {
           bottomSheetDialog.dismiss()
           dialogBinding.rulerView.adapter = null
       }
       bottomSheetDialog.show()
       fun logUserWaterIntake(
           userId: String,
           source: String,
           waterMl: Int,
           date: String
       ) {
       }
   }
    private fun setKgsValue() {
        numbers.clear()
        for (i in 0..2000) {
            numbers.add(i / 10f) // Increment by 0.1
        }
        logWeightRulerAdapter.notifyDataSetChanged()
    }

    private fun setLbsValue() {
        numbers.clear()
        for (i in 0..4410) {
            numbers.add(i / 10f)
        }
        logWeightRulerAdapter.notifyDataSetChanged()
    }
    private fun fetchWeightData(period: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        showLoader(requireView())
                    }
                }
                val userId = SharedPreferenceManager.getInstance(requireContext()).userId // Replace with dynamic user ID if needed
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var selectedDate: String

                if (period.contentEquals("last_weekly")) {
                    if (selectedWeekDate.isEmpty()) {
                        selectedDate = currentDateTime.format(formatter)
                        selectedWeekDate = selectedDate
                    } else {
                        selectedDate = selectedWeekDate
                    }
                    setSelectedDate(selectedWeekDate)
                } else if (period.contentEquals("last_monthly")) {
                    if (selectedMonthDate.isEmpty()) {
                        selectedDate = currentDateTime.format(formatter)
//                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
//                        selectedDate = firstDateOfMonth
                        selectedMonthDate = selectedDate
                    } else {
                        //val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = selectedMonthDate
                    }
                    setSelectedDateMonth(selectedMonthDate, "Month")
                } else {
                    if (selectedHalfYearlyDate.isEmpty()) {
                        selectedDate = currentDateTime.format(formatter)
//                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
//                        selectedDate = firstDateOfMonth
                        selectedHalfYearlyDate = selectedDate
                    } else {
                      //  val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = selectedHalfYearlyDate
                    }
                    setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                }
                val response = ApiClient.apiServiceFastApi.getLogWeight(
                    userId = userId,
                    period = period,
                    date = selectedDate
                )
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val waterData = response.body()
                    waterData?.let { data ->
                        val (entries, labels, labelsDate) = when (period) {
                            "last_weekly" -> processWeeklyData(data, selectedDate)
                            "last_monthly" -> processMonthlyData(data, selectedDate)
                            "last_six_months" -> processSixMonthsData(data, selectedDate)
                            else -> Triple(emptyList(), emptyList(), emptyList())
                        }
                        withContext(Dispatchers.Main) {
                            weight_description_heading.text = data.heading
                            weight_description_text.text = markdownToBold(data.description)

                            if (data.lastWeightLog != null){
                                weightIntakeUnit.text = data.lastWeightLog?.type
                                if (data.lastWeightLog?.type.equals("lbs")){
                                    val selectedWeight = ConversionUtils.convertLbsToKgs(data.lastWeightLog.totalWeight?.toFloat().toString())
                                    weightIntake.text = selectedWeight
                                }else{
                                    weightIntake.text = data.lastWeightLog.totalWeight.toString()
                                }
                                weightLastLogDateTv.text = data.lastWeightLog.date.toString()
                            }
                           // weightIntakeUnit.text = data.lastWeightLog.
                            if (data.weightTotals.size > 31) {
                                layoutLineChart.visibility = View.VISIBLE
                                lineChartForSixMonths()
                            } else {
                                layoutLineChart.visibility = View.VISIBLE
                                updateChart(entries, labels, labelsDate, data)
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No water intake data received", Toast.LENGTH_SHORT).show()
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
        }
    }

    fun markdownToBold(text: String): Spanned {
        val htmlText = text
            .replace("\n", "<br>")
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
        return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    private fun setupLineChart() {
        lineChart.apply {
            axisLeft.apply {
                axisMaximum = viewModel.yMax.toFloat() * 1.2f
                axisMinimum = 0f
                setDrawGridLines(true)
                textColor = Color.BLACK
                textSize = 10f
                setLabelCount(6, true)
                valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value == 0f) "0" else "${(value / 1000).toInt()}k"
                    }
                }
                setDrawAxisLine(true)
            }
            axisRight.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.BLACK
                textSize = 10f
                setDrawGridLines(true)
                setDrawLabels(true)
                granularity = 1f
                setAvoidFirstLastClipping(true)
            }
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setDrawBorders(false)
            setNoDataText("Loading data...")
        }
    }

    fun Instant.getMealLogDate(rolloverHour: Int = 4): String {
        val zone = ZoneId.systemDefault()
        val effectiveDate = this.effectiveDateForApi(rolloverHour, zone)
        return effectiveDate
            .atZone(zone)
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    private fun Instant.effectiveDateForApi(
        rolloverHour: Int,
        zone: ZoneId
    ): Instant {
        val now = Instant.now()
        val thisDate = this.atZone(zone).toLocalDate()
        val today = now.atZone(zone).toLocalDate()
        // ✅ Only apply rollover if THIS date is today
        if (thisDate != today) {
            return thisDate.atStartOfDay(zone).toInstant()
        }
        val currentHour = now.atZone(zone).hour
        return if (currentHour < rolloverHour) {
            // before rollover → yesterday
            today
                .minusDays(1)
                .atStartOfDay(zone)
                .toInstant()
        } else {
            // after rollover → today
            today
                .atStartOfDay(zone)
                .toInstant()
        }
    }

    private fun lineChartForSixMonths() {
        activity?.runOnUiThread {
            val entries = if (viewModel.filteredData.isNotEmpty()) {
                viewModel.filteredData.mapIndexed { index, item ->
                    Entry(index.toFloat(), item.waterMl.toFloat())
                }
            } else {
                listOf(Entry(0f, 0f), Entry(1f, 0f))
            }
            Log.d("HydrationTracker", "lineChartForSixMonths: entries size = ${entries.size}")

            val dataSet = LineDataSet(entries, "Water Intake (ml)").apply {
                color = Color.rgb(255, 102, 128)
                lineWidth = 1f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.LINEAR
                fillAlpha = 20
                fillColor = Color.rgb(255, 102, 128)
                setDrawFilled(true)
            }

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(
                viewModel.monthlyGroups.map { formatDate(it.startDate, "LLL\nyyyy") }.ifEmpty { listOf("No Data", "No Data") }
            )
            lineChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.BLACK
                textSize = 10f
                setDrawGridLines(true)
                setDrawLabels(true)
                granularity = 1f
                labelCount = viewModel.monthlyGroups.size.coerceAtLeast(2)
                setAvoidFirstLastClipping(true)
            }

            lineChart.axisLeft.apply {
                axisMaximum = viewModel.yMax.toFloat() * 1.2f
                axisMinimum = 0f
                setDrawGridLines(true)
                textColor = Color.BLACK
                textSize = 10f
                setLabelCount(6, true)
                valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value == 0f) "0" else "${(value / 1000).toInt()}k"
                    }
                }
                setDrawAxisLine(true)
            }
            lineChart.axisRight.isEnabled = false

            lineChart.animateX(1000)
            lineChart.animateY(1000)
            lineChart.invalidate()

            lineChart.post {
                val transformer = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
                val hMargin = 40f
                val vMargin = 20f
                if (viewModel.currentRange == RangeTypeChartsNew.SIX_MONTHS) {
                    val overallStart = viewModel.filteredData.firstOrNull()?.date ?: viewModel.startDate
                    val overallEnd = viewModel.filteredData.lastOrNull()?.date ?: viewModel.endDate
                    val totalInterval = if (overallEnd.time > overallStart.time) overallEnd.time - overallStart.time.toDouble() else 1.0

                    Log.d("HydrationTracker", "filteredData size: ${viewModel.filteredData.size}, overallStart: $overallStart, overallEnd: $overallEnd")

                    stripsContainer.removeAllViews()
                    viewModel.monthlyGroups.forEach { group ->
                        val monthEntries = viewModel.filteredData.filter { it.date >= group.startDate && it.date <= group.endDate }
                        if (monthEntries.isNotEmpty()) {
                            val startIndex = viewModel.filteredData.indexOfFirst { it.date >= group.startDate }
                            val endIndex = viewModel.filteredData.indexOfLast { it.date <= group.endDate }
                            if (startIndex >= 0 && endIndex >= 0) {
                                val xStartFraction = startIndex.toDouble() / viewModel.filteredData.size
                                val xEndFraction = endIndex.toDouble() / viewModel.filteredData.size
                                val yFraction = group.avgWater.toDouble() / viewModel.yMax
                                val pixelStart = transformer.getPixelForValues(
                                    (xStartFraction * (lineChart.width - 2 * hMargin)).toFloat() + hMargin,
                                    ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
                                )
                                val pixelEnd = transformer.getPixelForValues(
                                    (xEndFraction * (lineChart.width - 2 * hMargin)).toFloat() + hMargin,
                                    ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
                                )

                                // Draw red capsule
                                val capsuleView = View(requireContext()).apply {
                                    val width = (pixelEnd.x - pixelStart.x).toInt().coerceAtLeast(20)
                                    layoutParams = FrameLayout.LayoutParams(width, 8)
                                    background = GradientDrawable().apply {
                                        shape = GradientDrawable.RECTANGLE
                                        cornerRadius = 4f
                                        setColor(Color.RED)
                                    }
                                    x = pixelStart.x.toFloat()
                                    y = (pixelStart.y - 3).toFloat()
                                }
                                stripsContainer.addView(capsuleView)

                                // Average label
                                val avgText = TextView(requireContext()).apply {
                                    text = "${group.avgWater / 1000}k"
                                    setTextColor(Color.BLACK)
                                    textSize = 12f
                                    setPadding(6, 4, 6, 4)
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    x = ((pixelStart.x + pixelEnd.x) / 2 - 20).toFloat()
                                    y = (pixelStart.y - 20).toFloat()
                                }
                                stripsContainer.addView(avgText)

                                // Difference label
                                val diffText = TextView(requireContext()).apply {
                                    text = group.monthDiffString
                                    setTextColor(if (group.isPositiveChange) Color.GREEN else Color.RED)
                                    textSize = 12f
                                    setPadding(6, 4, 6, 4)
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    x = ((pixelStart.x + pixelEnd.x) / 2 - 20).toFloat()
                                    y = (pixelStart.y + 20).toFloat()
                                }
                                stripsContainer.addView(diffText)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun formatDate(date: Date, format: String): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    private fun updateChart(entries: List<Entry>, labels: List<String>, labelsDate: List<String>, data: WeightResponse) {
        activity?.runOnUiThread {
            selectHeartRateLayout.visibility = View.INVISIBLE
            val dataSet = LineDataSet(entries, "")
            dataSet.color = context?.let { ContextCompat.getColor(it, R.color.border_green) }!!
            dataSet.valueTextColor = Color.BLACK
            dataSet.valueTextSize = 12f
            dataSet.setCircleColor(context?.let { ContextCompat.getColor(it, R.color.border_green) }!!)
            dataSet.circleRadius = 5f
            dataSet.lineWidth = 2f
            dataSet.setDrawValues(entries.size <= 7)

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // X-axis label handling
            val combinedLabels = if (entries.size == 30) {
                labels
            } else {
                labels.take(entries.size).zip(labelsDate.take(entries.size)) { label, date ->
                    val cleanedDate = date.substringBefore(",")
                    "$label\n$cleanedDate"
                }
            }

            val xAxis = lineChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(combinedLabels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textSize = 10f
            xAxis.granularity = 1f
            xAxis.labelCount =  entries.size
            xAxis.setDrawLabels(true)
            xAxis.labelRotationAngle = 0f
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
            xAxis.yOffset = 15f

            if (entries.size == 30) {
                xAxis.axisMinimum = -0.5f
                xAxis.axisMaximum = 29.5f
                xAxis.setCenterAxisLabels(false)
            } else {
                xAxis.axisMinimum = -0.5f
                xAxis.axisMaximum = entries.size - 0.5f
                xAxis.setCenterAxisLabels(false)
            }

            // Custom XAxis Renderer (multiline support)
            val customRenderer = MultilineXAxisRenderer(
                lineChart.viewPortHandler,
                lineChart.xAxis,
                lineChart.getTransformer(YAxis.AxisDependency.LEFT)
            )
            (lineChart as BarLineChartBase<*>).setXAxisRenderer(customRenderer)

            // Y-axis
            val leftYAxis: YAxis = lineChart.axisLeft
            leftYAxis.textSize = 12f
            leftYAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
            leftYAxis.setDrawGridLines(true)
            leftYAxis.axisMinimum = 0f
          //  leftYAxis.axisMaximum = entries.maxByOrNull { it.y }?.y?.plus(100f) ?: 1000f
            leftYAxis.granularity = 1f

            if (entries.size == 30){
                val minValue = minOf(
                    entries.minOfOrNull { it.y } ?: 0f,
                    data.totalWeight.toFloat(),
                    data.currentAvgWeight.toFloat()
                )
                val maxValue = maxOf(
                    entries.maxOfOrNull { it.y } ?: 0f,
                    data.totalWeight.toFloat(),
                    data.currentAvgWeight.toFloat()
                )
                // Include stepsGoal in max check
                val axisMax = maxOf(maxValue, data.totalWeight.toFloat())

                leftYAxis.axisMinimum = if (minValue < 0) minValue * 1.2f else 0f
                leftYAxis.axisMaximum = axisMax * 1.2f
                leftYAxis.setDrawZeroLine(true)
                // leftYAxis.zeroLineColor = Color.BLACK
                leftYAxis.zeroLineWidth = 1f
                lineChart.rendererLeftYAxis = BadgeLimitLineRenderer(
                    lineChart.viewPortHandler,
                    lineChart.axisLeft,
                    lineChart.getTransformer(YAxis.AxisDependency.LEFT)
                )

//                val totalStepsLine = LimitLine(data.todaysWaterLog.goal.toFloat(), "G")
//                totalStepsLine.lineColor = ContextCompat.getColor(requireContext(), R.color.border_green)
//                totalStepsLine.lineWidth = 1f
//                totalStepsLine.enableDashedLine(10f, 10f, 0f)
//                totalStepsLine.textColor = ContextCompat.getColor(requireContext(), R.color.border_green)
//                totalStepsLine.textSize = 10f
//                totalStepsLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP

                val avgStepsLine = LimitLine(data.currentAvgWeight.toFloat(), "A")
                avgStepsLine.lineColor = ContextCompat.getColor(requireContext(), R.color.text_color_kcal)
                avgStepsLine.lineWidth = 1f
                avgStepsLine.enableDashedLine(10f, 10f, 0f)
                avgStepsLine.textColor = ContextCompat.getColor(requireContext(), R.color.text_color_kcal)
                avgStepsLine.textSize = 10f
                avgStepsLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP

                leftYAxis.removeAllLimitLines()
               // leftYAxis.addLimitLine(totalStepsLine)
                leftYAxis.addLimitLine(avgStepsLine)
                averageGoalLayout.visibility = View.VISIBLE
            }else{
                leftYAxis.removeAllLimitLines()
                averageGoalLayout.visibility = View.GONE
            }

            lineChart.axisRight.isEnabled = false
            lineChart.description.isEnabled = false
            lineChart.setScaleEnabled(false)
            lineChart.isDoubleTapToZoomEnabled = false
            lineChart.isHighlightPerTapEnabled = true
            lineChart.isHighlightPerDragEnabled = false
            // Optional chart description
            val description = Description().apply {
                text = ""
                textColor = Color.BLACK
                textSize = 14f
                setPosition(lineChart.width / 2f, lineChart.height.toFloat() - 10f)
            }
            lineChart.description = description
            lineChart.setExtraOffsets(0f, 0f, 0f, 25f)
            // Legend
            val legend = lineChart.legend
            legend.setDrawInside(false)
            legend.isEnabled = false

            lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    selectHeartRateLayout.visibility = View.VISIBLE
                    if (e != null) {
                        val x = e.x.toInt()
                        val y = e.y
                        Log.d("ChartClick", "Clicked X: $x, Y: $y")
                        selectedItemDate.text = labelsDate.get(x)
                        selectedCalorieTv.text = y.toInt().toString()
                    }
                }
                override fun onNothingSelected() {
                    Log.d("ChartClick", "Nothing selected")
                    selectHeartRateLayout.visibility = View.INVISIBLE
                }
            })
            lineChart.animateY(1000)
            lineChart.invalidate()
        }
    }

    private suspend fun processWeeklyData(data: WeightResponse, currentDate: String): Triple<List<Entry>, List<String>,  List<String>> {
        return withContext(Dispatchers.Main) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = currentDate
            val date = dateFormat.parse(currentDate)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -6)

            val weightMap = mutableMapOf<String, Float>()
            val labels = mutableListOf<String>()
            val labelsDate = mutableListOf<String>()
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

            repeat(7) {
                val dateStr = dateFormat.format(calendar.time)
                weightMap[dateStr] = 0f
                val dateLabel = "${convertDate(dateStr)}, $year"
                val dayString = dayFormat.format(calendar.time)
                labels.add(dayString)
                labelsDate.add(dateLabel)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (data.weightTotals.isNotEmpty()) {
                data.weightTotals.forEach { intake ->
                    val startDate = dateFormat.parse(intake.date)?.let { Date(it.time) }
                    if (startDate != null) {
                        val dayKey = dateFormat.format(startDate)
                        weightMap[dayKey] = weightMap[dayKey]!! + (intake.weight.toFloat() ?: 0f)
                    }
                }
            }
            setLastAverageValue(data, "% Past Week")
            val entries = weightMap.values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
            Triple(entries, labels, labelsDate)
        }
    }

    private suspend fun processMonthlyData(data: WeightResponse, currentDate: String): Triple<List<Entry>, List<String>,  List<String>> {
        return withContext(Dispatchers.Main) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val date = dateFormat.parse(currentDate)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -29)

            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val weightMap = mutableMapOf<String, Float>()
            val dateList = mutableListOf<String>()
            val weeklyLabels = mutableListOf<String>()
            val labelsDate = mutableListOf<String>()

            // Initialize water map for all days in the month
            repeat(30) {
                val dateStr = dateFormat.format(calendar.time)
                weightMap[dateStr] = 0f
                dateList.add(dateStr)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val labelsWithEmpty = generateLabeled30DayListWithEmpty(dateList[0])
            val labels = generateWeeklyLabelsFor30Days(dateList[0])
            weeklyLabels.addAll(labelsWithEmpty)
            labelsDate.addAll(labels)
            // Populate weight data
            if (data.weightTotals.isNotEmpty()) {
                data.weightTotals.forEach { intake ->
                    val startDate = dateFormat.parse(intake.date)?.let { Date(it.time) }
                    if (startDate != null) {
                        val dayKey = dateFormat.format(startDate)
                        weightMap[dayKey] = weightMap[dayKey]!! + (intake.weight.toFloat() ?: 0f)
                    }
                }
            }
            setLastAverageValue(data, "% Past Week")
            // Create entries for the chart
            val entries = weightMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
            Triple(entries, weeklyLabels, labels)
        }
    }

    private fun processSixMonthsData(data: WeightResponse, currentDate: String): Triple<List<Entry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 24)
        calendar.add(Calendar.MONTH, -5)

        val waterMap = mutableMapOf<Int, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        repeat(6) { month ->
            waterMap[month] = 0f
            labels.add(monthFormat.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }

        data.weightTotals.forEach { intake ->
            val date = dateFormat.parse(intake.date)
            if (date != null) {
                calendar.time = date
                val monthDiff = ((2025 - 1900) * 12 + Calendar.MARCH) - ((calendar.get(Calendar.YEAR) - 1900) * 12 + calendar.get(Calendar.MONTH))
                val monthIndex = 5 - monthDiff
                if (monthIndex in 0..5) {
                    waterMap[monthIndex] = waterMap[monthIndex]!! + intake.weight.toFloat()
                }
            }
        }
        val entries = waterMap.values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        return Triple(entries, labels, labelsDate)
    }

    private fun setSelectedDate(selectedWeekDate: String) {
        requireActivity().runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedWeekDate
            val date = dateFormat.parse(dateString)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            val dateStr = dateFormat.format(calendar.time)
            val dateView: String = convertDate(dateStr) + "-" + convertDate(selectedWeekDate) + "," + year.toString()
            this.selectedDate.text = dateView
            this.selectedDate.gravity = Gravity.CENTER
        }
    }

    private fun setSelectedDateMonth(selectedMonthDate: String, dateViewType: String) {
        activity?.runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedMonthDate
            val date = dateFormat.parse(dateString)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -29)
            val dateStr = dateFormat.format(calendar.time)
            if (dateViewType.contentEquals("Month")){
//                val lastDayOfMonth = getDaysInMonth(month+1 , year)
//                val lastDateOfMonth = getFirstDateOfMonth(selectedMonthDate, lastDayOfMonth)
                //               val dateView : String = convertDate(selectedMonthDate) + "-" + convertDate(lastDateOfMonth)+","+ year.toString()
                val dateView : String = convertDate(dateStr.toString()) + "-" + convertDate(selectedMonthDate)+","+ year.toString()
                selectedDate.text = dateView
                selectedDate.gravity = Gravity.CENTER
            }else{
                selectedDate.text = year.toString()
                selectedDate.gravity = Gravity.CENTER
            }
        }
    }

    private fun setLastAverageValue(data: WeightResponse, type: String) {
        activity?.runOnUiThread {
            averageWaterIntake.text = data.currentAvgWeight.toInt().toString()
            // totalCalorie.text = data.totalWater.toInt().toString()
            if (data.progressSign == "plus") {
                percentageTv.text = "${data.progressPercentage.toInt()} $type"
                // percentageIc.setImageResource(R.drawable.ic_up)
            } else if (data.progressSign == "minus") {
                percentageTv.text = "${data.progressPercentage.toInt()} $type"
                // percentageIc.setImageResource(R.drawable.ic_down)
            }
        }
    }

    private fun generateWeeklyLabelsFor30Days(startDateStr: String): List<String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        val startDate = dateFormat.parse(startDateStr)!!
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val endDate = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.DAY_OF_MONTH, 29)
        }.time

        val result = mutableListOf<String>()

        while (calendar.time <= endDate) {
            val weekStart = calendar.time
            val weekStartIndex = result.size
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            val weekEnd = if (calendar.time.after(endDate)) endDate else calendar.time

            val startDay = dayFormat.format(weekStart)
            val endDay = dayFormat.format(weekEnd)
            val startMonth = monthFormat.format(weekStart)
            val endMonth = monthFormat.format(weekEnd)
            val dateItem = LocalDate.parse(startDateStr)
            val yearItem = dateItem.year

            val label = if (startMonth == endMonth) {
                "$startDay–$endDay $startMonth"+"," + yearItem.toString()
            } else {
                "$startDay $startMonth–$endDay $endMonth"+"," + yearItem.toString()
            }
            val daysInThisWeek = 7.coerceAtMost(30 - result.size)
            repeat(daysInThisWeek) {
                result.add(label)
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1) // move to next week start
        }
        return result
    }

    private fun generateLabeled30DayListWithEmpty(startDateStr: String): List<String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        val startDate = dateFormat.parse(startDateStr)!!
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val endDate = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.DAY_OF_MONTH, 29) // total 30 days
        }.time

        val fullList = MutableList(30) { "" } // default 30 items with empty strings
        var labelIndex = 0
        var startIndex = 0

        while (calendar.time <= endDate && startIndex < 30) {
            val weekStart = calendar.time
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            val weekEnd = if (calendar.time.after(endDate)) endDate else calendar.time
            val startDay = dayFormat.format(weekStart)
            val endDay = dayFormat.format(weekEnd)
            val startMonth = monthFormat.format(weekStart)
            val endMonth = monthFormat.format(weekEnd)
            val newLine = "\n"
            val label = if (startMonth == endMonth) {
                "$startDay–$endDay$newLine$startMonth"
            } else {
                "$startDay$startMonth–$endDay$newLine$endMonth"
            }
            fullList[startIndex] = label // set label at start of week
            // Move to next start index
            startIndex += 7
            calendar.add(Calendar.DAY_OF_MONTH, 1) // move past last week end
        }
        return fullList
    }

    private fun convertDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        return try {
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    private fun getDaysInMonth(month: Int, year: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getFirstDateOfMonth(inputDate: String, value: Int): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val parsedDate = LocalDate.parse(inputDate, formatter)
        val firstDayOfMonth = parsedDate.withDayOfMonth(value)
        return firstDayOfMonth.format(formatter)
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}

class HydrationViewModelNew : ViewModel() {
    val allDailyWater = mutableListOf<DailyWaterNew>()
    var currentRange = RangeTypeChartsNew.SIX_MONTHS
    var startDate = Date()
    var endDate = Date()

    init {
        generateMockData()
        setInitialRange(RangeTypeChartsNew.SIX_MONTHS)
    }

    private fun generateMockData() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val start = sdf.parse("2025/01/01") ?: Date()
        val end = sdf.parse("2025/07/01") ?: Date()

        var current = start
        while (current <= end) {
            val randomWater = (1000..3000).random()
            allDailyWater.add(DailyWaterNew(current, randomWater))
            calendar.time = current
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            current = calendar.time
        }
        Log.d("HydrationViewModel", "Generated ${allDailyWater.size} days of data from ${formatDate(start, "d MMM yyyy")} to ${formatDate(end, "d MMM yyyy")}")
    }

    val filteredData: List<DailyWaterNew>
        get() = allDailyWater.filter { it.date >= startDate && it.date <= endDate }

    val monthlyGroups: List<MonthGroupsNew>
        get() = if (currentRange != RangeTypeChartsNew.SIX_MONTHS || filteredData.isEmpty()) emptyList() else {
            val calendar = Calendar.getInstance()
            val grouped = filteredData.groupBy {
                calendar.time = it.date
                Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            }
            val sorted = grouped.toList().sortedWith(compareBy({ it.first.first }, { it.first.second }))
            var results = mutableListOf<MonthGroupsNew>()
            var previousAvg: Int? = null
            for ((_, days) in sorted) {
                val sortedDays = days.sortedBy { it.date }
                val totalWater = sortedDays.sumOf { it.waterMl }
                val avg = if (sortedDays.isNotEmpty()) totalWater / sortedDays.size else 0
                val diffString = previousAvg?.let {
                    val delta = avg - it
                    val pct = (delta.toDouble() / it * 100).toInt()
                    if (pct >= 0) "+$pct%" else "$pct%"
                } ?: ""
                val isPositive = diffString.startsWith('+')
                previousAvg = avg

                val monthStart = calendar.apply {
                    time = sortedDays.first().date
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
                val monthEnd = calendar.apply {
                    time = sortedDays.last().date
                    set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                }.time
                val clampedStart = maxOf(monthStart, startDate)
                val clampedEnd = minOf(monthEnd, endDate)
                results.add(MonthGroupsNew(sortedDays, avg, diffString, isPositive, clampedStart, clampedEnd))
            }
            results
        }

    fun setInitialRange(range: RangeTypeChartsNew) {
        currentRange = range
        val calendar = Calendar.getInstance()
        val now = Date()
        when (range) {
            RangeTypeChartsNew.WEEK -> {
                val interval = calendar.getActualMinimum(Calendar.DAY_OF_WEEK)
                calendar.time = now
                calendar.set(Calendar.DAY_OF_WEEK, interval)
                startDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                endDate = calendar.time
            }
            RangeTypeChartsNew.MONTH -> {
                calendar.time = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = calendar.time
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                endDate = calendar.time
            }
            RangeTypeChartsNew.SIX_MONTHS -> {
                calendar.time = now
                calendar.add(Calendar.MONTH, -6)
                startDate = calendar.time
                endDate = now
            }
        }
        val minDate = allDailyWater.minByOrNull { it.date }?.date ?: startDate
        val maxDate = allDailyWater.maxByOrNull { it.date }?.date ?: endDate
        startDate = maxOf(startDate, minDate)
        endDate = minOf(endDate, maxDate)
        Log.d("HydrationViewModel", "Set range: $currentRange, startDate: ${formatDate(startDate, "d MMM yyyy")}, endDate: ${formatDate(endDate, "d MMM yyyy")}")
    }

    val yMax: Double
        get() = filteredData.maxOfOrNull { it.waterMl.toDouble() }?.times(1.15) ?: 3000.0

    private fun formatDate(date: Date, format: String): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }
}

data class DailyWaterNew(val date: Date, val waterMl: Int)
enum class RangeTypeChartsNew { WEEK, MONTH, SIX_MONTHS }
data class MonthGroupsNew(
    val days: List<DailyWaterNew>,
    val avgWater: Int,
    val monthDiffString: String,
    val isPositiveChange: Boolean,
    val startDate: Date,
    val endDate: Date
)