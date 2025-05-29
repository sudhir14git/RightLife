package com.jetsynthesys.rightlife.ui.healthcam.basicdetails

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.newquestionrequestfacescan.AnswerFaceScan
import com.jetsynthesys.rightlife.apimodel.newquestionrequestfacescan.FaceScanQuestionRequest
import com.jetsynthesys.rightlife.databinding.ActivityHealthcamBasicDetailsNewBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetAgeSelectionBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetHeightSelectionBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetWeightSelectionBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.healthaudit.questionlist.Option
import com.jetsynthesys.rightlife.ui.healthaudit.questionlist.QuestionListHealthAudit
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamSubmitResponse
import com.jetsynthesys.rightlife.ui.new_design.RulerAdapter
import com.jetsynthesys.rightlife.ui.new_design.RulerAdapterVertical
import com.jetsynthesys.rightlife.ui.sdkpackage.HealthCamRecorderActivity
import com.jetsynthesys.rightlife.ui.utility.ConversionUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.floor


class HealthCamBasicDetailsNewActivity : BaseActivity() {
    private lateinit var binding: ActivityHealthcamBasicDetailsNewBinding
    private val numbers = mutableListOf<Float>()
    private lateinit var adapterHeight: RulerAdapterVertical
    private lateinit var adapterWeight: RulerAdapter

    private val smokeOptions = ArrayList<Option>()
    private val diabeticsOptions = ArrayList<Option>()
    private val bpMedicationOptions = ArrayList<Option>()
    private val genderOptions = ArrayList<Option>()

    private var responseObj: QuestionListHealthAudit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthcamBasicDetailsNewBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        getQuestionerList()

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.edtAge.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    showAgeSelectionBottomSheet()
                }
            }
        }

        binding.edtHeight.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    showHeightSelectionBottomSheet()
                }
            }
        }

        binding.edtWeight.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    showWeightSelectionBottomSheet()
                }
            }
        }

        binding.edtGender.setOnClickListener {
            showOptionPopup(binding.edtGender, genderOptions, "Select Gender") { option: Option ->
                binding.edtGender.setText(option.optionText)
            }
        }

        binding.edtBloodPressure.setOnClickListener {
            showOptionPopup(binding.edtBloodPressure, bpMedicationOptions) { option: Option ->
                binding.edtBloodPressure.setText(option.optionText)
            }
        }

        binding.edtSmoke.setOnClickListener {
            showOptionPopup(binding.edtSmoke, smokeOptions) { option: Option ->
                binding.edtSmoke.setText(option.optionText)
            }
        }

        binding.edtDiabetic.setOnClickListener {
            showOptionPopup(binding.edtDiabetic, diabeticsOptions) { option: Option ->
                binding.edtDiabetic.setText(option.optionText)
            }
        }

        binding.btnProceed.setOnClickListener {
            val name = binding.edtName.text.toString()
            val height = binding.edtHeight.text.toString()
            val weight = binding.edtWeight.text.toString()
            val age = binding.edtAge.text.toString()
            val gender = binding.edtGender.text.toString()
            val smoke = binding.edtSmoke.text.toString()
            val bpMedication = binding.edtBloodPressure.text.toString()
            val diabetic = binding.edtDiabetic.text.toString()

            if (name.isEmpty())
                showCustomToast("Name is Required")
            else if (height.isEmpty())
                showCustomToast("Please select Height")
            else if (weight.isEmpty())
                showCustomToast("Please select Weight")
            else if (age.isEmpty())
                showCustomToast("Please select Age")
            else if (gender.isEmpty())
                showCustomToast("Please select Gender")
            else if (smoke.isEmpty())
                showCustomToast("Please select Smoking Habit")
            else if (bpMedication.isEmpty())
                showCustomToast("Please select Blood Pressure Medication")
            else if (diabetic.isEmpty())
                showCustomToast("Please select Diabetic Level")
            else {
                val answerFaceScans = ArrayList<AnswerFaceScan>()

                val heightWithUnit = height.split(" ")
                val weightWithUnit = weight.split(" ")

                for (question in responseObj!!.questionData.questionList) {
                    val answer = AnswerFaceScan()
                    answer.question = question.question
                    when (question.question) {
                        "first_name" -> answer.answer = name
                        "height" -> answer.answer = heightWithUnit[0].toDouble()
                        "weight" -> answer.answer = weightWithUnit[0].toDouble()
                        "age" -> answer.answer = age
                        "gender" -> answer.answer = gender
                        "smoking" -> {
                            var selectedSmoke = "0"
                            for (sm in smokeOptions) {
                                if (sm.optionText == smoke) {
                                    selectedSmoke = sm.optionPosition
                                }
                            }
                            answer.answer = selectedSmoke
                        }

                        "bloodpressuremedication" -> {
                            var selectedBP = "0"
                            for (bp in bpMedicationOptions) {
                                if (bp.optionText == bpMedication) {
                                    selectedBP = bp.optionPosition
                                }
                            }
                            answer.answer = selectedBP
                        }

                        "diabetes" -> {
                            var selectedDiabetic = "0"
                            for (dia in diabeticsOptions) {
                                if (dia.optionText == diabetic) {
                                    selectedDiabetic = dia.optionPosition
                                }
                            }
                            answer.answer = selectedDiabetic
                        }

                        /*"height_unit" -> {
                            var selectedHeightUnit = 0
                            for (heightUnit in heightUnits) {
                                if (heightUnit.optionText == heightUnit) {
                                    selectedHeightUnit = heightUnit.optionPosition.toInt()
                                }
                            }
                            answer.answer = selectedHeightUnit
                        }

                        "weight_unit" -> {
                            var selectedWeightUnit = 0
                            for (weightUnit in weightUnits) {
                                if (weightUnit.optionText == weightUnit) {
                                    selectedWeightUnit = weightUnit.optionPosition.toInt()
                                }
                            }
                            answer.answer = selectedWeightUnit
                        }*/
                    }
                    answerFaceScans.add(answer)
                }


                val heightInCms = if (heightWithUnit.size == 2)
                    heightWithUnit[0]
                else
                    "${heightWithUnit[0]}.${heightWithUnit[2]}"


                val weightInKg =
                    if (weightWithUnit[1].equals(
                            "kgs",
                            ignoreCase = true
                        )
                    ) weightWithUnit[0] else ConversionUtils.convertLbsToKgs(
                        weightWithUnit[0]
                    )

                val faceScanQuestionRequest = FaceScanQuestionRequest()
                faceScanQuestionRequest.questionId = responseObj?.questionData?.id
                faceScanQuestionRequest.answers = answerFaceScans

                binding.btnProceed.isEnabled = false
                Handler().postDelayed({
                    binding.btnProceed.isEnabled = true
                }, 5000)
                submitAnswerRequest(
                    faceScanQuestionRequest,
                    heightInCms,
                    weightInKg,
                    age.split(" ")[0]
                )
            }
        }
    }


    private fun showAgeSelectionBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetAgeSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout =
            bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        val years = arrayOf(
            "13 years",
            "14 years",
            "15 years",
            "16 years",
            "17 years",
            "18 years",
            "19 years",
            "20 years",
            "21 years",
            "22 years",
            "23 years",
            "24 years",
            "25 years",
            "26 years",
            "27 years",
            "28 years",
            "29 years",
            "30 years",
            "31 years",
            "32 years",
            "33 years",
            "34 years",
            "35 years",
            "36 years",
            "37 years",
            "38 years",
            "39 years",
            "40 years",
            "41 years",
            "42 years",
            "43 years",
            "44 years",
            "45 years",
            "46 years",
            "47 years",
            "48 years",
            "49 years",
            "50 years",
            "51 years",
            "52 years",
            "53 years",
            "54 years",
            "55 years",
            "56 years",
            "57 years",
            "58 years",
            "59 years",
            "60 years",
            "61 years",
            "62 years",
            "63 years",
            "64 years",
            "65 years",
            "66 years",
            "67 years",
            "68 years",
            "69 years",
            "70 years",
            "71 years",
            "72 years",
            "73 years",
            "74 years",
            "75 years",
            "76 years",
            "77 years",
            "78 years",
            "79 years",
            "80 years",
            "81 years",
            "82 years",
            "83 years",
            "84 years",
            "85 years",
            "86 years",
            "87 years",
            "88 years",
            "89 years",
            "90 years",
            "91 years",
            "92 years",
            "93 years",
            "94 years",
            "95 years",
            "96 years",
            "97 years",
            "98 years",
            "99 years",
            "100 years",
            "101 years",
            "102 years",
            "103 years",
            "104 years",
            "105 years",
            "106 years",
            "107 years",
            "108 years",
            "109 years",
            "110 years",
            "111 years",
            "112 years",
            "113 years",
            "114 years",
            "115 years",
            "116 years",
            "117 years",
            "118 years",
            "119 years",
            "120 years"
        )
        val selectedAgeArray = binding.tvAge.text.toString().split(" ")
        val selectedAgeFromUi =
            if (selectedAgeArray.isNotEmpty() && selectedAgeArray[0].toInt() >= 13) {
                binding.tvAge.text.toString()
            } else
                ""
        val value1 = if (selectedAgeFromUi.isNotEmpty())
            years.indexOf(selectedAgeFromUi) + 1
        else 15

        dialogBinding.numberPicker.apply {
            minValue = 1
            maxValue = years.size
            displayedValues = years
            value = value1
            wheelItemCount = 7
        }

        var selectedAge = if (selectedAgeFromUi.isNotEmpty())
            years[years.indexOf(selectedAgeFromUi)]
        else years[14]

        dialogBinding.numberPicker.setOnScrollListener { view, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                selectedAge = years[view.value - 1]
                binding.edtAge.setText(selectedAge)
            }
        }

        dialogBinding.btnConfirm.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.edtAge.setText(selectedAge)
        }

        bottomSheetDialog.show()
    }

    private fun showWeightSelectionBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)
        var selectedLabel = " KGS"
        var selectedWeight = binding.edtWeight.text.toString()
        if (selectedWeight.isEmpty()) {
            selectedWeight = "50 KGS"
        } else {
            val w = selectedWeight.split(" ")
            selectedLabel = " ${w[1].lowercase(Locale.getDefault())}"
        }


        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetWeightSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)
        dialogBinding.switchWeightMetric.apply {
            trackTintList = ContextCompat.getColorStateList(context, R.color.switch_track_color)
            thumbTintList = ContextCompat.getColorStateList(context, R.color.switch_thumb_color)
        }

        // Set up the animation
        val bottomSheetLayout =
            bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.selectedNumberText.text = selectedWeight
        if (selectedLabel == " lbs") {
            dialogBinding.switchWeightMetric.isChecked = true
        }

        dialogBinding.switchWeightMetric.setOnCheckedChangeListener { buttonView, isChecked ->
            val w = selectedWeight.split(" ")
            if (isChecked) {
                selectedLabel = " lbs"
                selectedWeight = ConversionUtils.convertLbsToKgs(w[0])
                setLbsValue()
            } else {
                selectedLabel = " KGS"
                selectedWeight = ConversionUtils.convertKgToLbs(w[0])
                setKgsValue()
            }
            dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedWeight.toDouble() * 10).toInt())
            selectedWeight += selectedLabel
            dialogBinding.selectedNumberText.text = selectedWeight
        }

        val layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dialogBinding.rulerView.layoutManager = layoutManager

        // Generate numbers with increments of 0.1

        for (i in 0..3000) {
            numbers.add(i / 10f) // Increment by 0.1
        }


        adapterWeight = RulerAdapter(numbers) { number ->
            // Handle the selected number
        }
        dialogBinding.rulerView.adapter = adapterWeight


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
            bottomSheetDialog.dismiss()
            dialogBinding.rulerView.adapter = null
            binding.edtWeight.setText(selectedWeight)
        }

        bottomSheetDialog.show()
    }

    private fun showHeightSelectionBottomSheet() {
        var selectedHeight = "5 Ft 10 In"
        var selectedLabel = " feet"
        // Create and configure BottomSheetDialog
        val decimalFormat = DecimalFormat("###.##")
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetHeightSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)
        dialogBinding.switchHeightMetric.apply {
            trackTintList = ContextCompat.getColorStateList(context, R.color.switch_track_color)
            thumbTintList = ContextCompat.getColorStateList(context, R.color.switch_thumb_color)
        }
        // Set up the animation
        val bottomSheetLayout =
            bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        selectedHeight = binding.edtHeight.text.toString()
        if (selectedHeight.isEmpty()) {
            selectedHeight = "5 Ft 10 In"
        } else {
            val h = selectedHeight.split(" ")
            selectedLabel = if (h[1].equals("cms", ignoreCase = true))
                " cms"
            else
                " feet"
        }

        dialogBinding.switchHeightMetric.isChecked = selectedLabel == " cms"

        dialogBinding.selectedNumberText.text = selectedHeight
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        dialogBinding.rulerView.layoutManager = layoutManager


        dialogBinding.switchHeightMetric.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                selectedLabel = " cms"
                val result = CommonAPICall.convertFeetInchToCmWithIndex(selectedHeight)

                selectedHeight = result.cmText
                setCms()

                dialogBinding.rulerView.scrollToPosition(result.cmIndex)

            } else {
                selectedLabel = " feet"
                val result = CommonAPICall.convertCmToFeetInchWithIndex(selectedHeight)

                selectedHeight = result.feetInchText
                setFtIn()

                dialogBinding.rulerView.scrollToPosition((result.inchIndex))

            }
            dialogBinding.selectedNumberText.text = selectedHeight
        }

        adapterHeight = RulerAdapterVertical(numbers) { number ->
            // Handle the selected number
        }
        if (selectedLabel == " cms") {
            setCms()
        } else {
            setFtIn()
        }
        dialogBinding.rulerView.adapter = adapterHeight

        // Attach a LinearSnapHelper for center alignment
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(dialogBinding.rulerView)

        // Add scroll listener to handle snapping logic
        dialogBinding.rulerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snappedView = snapHelper.findSnapView(recyclerView.layoutManager)
                    if (snappedView != null) {
                        val position =
                            recyclerView.layoutManager?.getPosition(snappedView) ?: return
                        val snappedNumber = numbers[position]
                        if (dialogBinding.selectedNumberText != null) {
                            dialogBinding.selectedNumberText.text =
                                "${decimalFormat.format(snappedNumber)} $selectedLabel"
                            if (selectedLabel == " feet") {
                                val feet = decimalFormat.format(snappedNumber / 12)
                                val remainingInches = snappedNumber.toInt() % 12
                                val h = (feet).toString().split(".")
                                val ft = h[0]
                                dialogBinding.selectedNumberText.text =
                                    "$ft Ft $remainingInches In"
                            }
                            selectedHeight = dialogBinding.selectedNumberText.text.toString()
                        }

                    }
                }
            }
        })

        // Set vertical padding for the marker view programmatically
        dialogBinding.rlRulerContainer.post {
            val parentHeight = dialogBinding.rlRulerContainer.height
            val paddingVertical = parentHeight / 2
            dialogBinding.markerView.setPadding(
                dialogBinding.markerView.paddingLeft,
                paddingVertical,
                dialogBinding.markerView.paddingRight,
                paddingVertical
            )
        }

        dialogBinding.rulerView.post {

            selectedHeight = binding.edtHeight.text.toString()
            if (selectedHeight.isEmpty()) {
                selectedHeight = "5 Ft 10 In"
            } else {
                val h = selectedHeight.split(" ")
                selectedLabel = if (h[1].equals("cms", ignoreCase = true))
                    " cms"
                else
                    " feet"
            }

            dialogBinding.switchHeightMetric.isChecked = selectedLabel == " cms"

            if (selectedLabel == " feet") {
                val regex = Regex("(\\d+)\\s*Ft\\s*(\\d+)\\s*In", RegexOption.IGNORE_CASE)
                val matchResult = regex.find(selectedHeight)

                if (matchResult != null && matchResult.groupValues.size == 3) {
                    val feet = matchResult.groupValues[1].toInt()
                    val inches = matchResult.groupValues[2].toInt()
                    val index = (feet * 12) + inches
                    dialogBinding.rulerView.scrollToPosition(index + 8)
                }
            } else {
                dialogBinding.rulerView.scrollToPosition(floor(selectedHeight.split(" ")[0].toDouble()).toInt() + 8)
            }
        }

        dialogBinding.btnConfirm.setOnClickListener {
            if (validateInput(selectedLabel, selectedHeight)) {
                bottomSheetDialog.dismiss()
                dialogBinding.rulerView.adapter = null
                binding.edtHeight.setText(selectedHeight)
            }
        }

        bottomSheetDialog.show()
    }

    private fun validateInput(selectedLabel: String, selectedHeight: String): Boolean {
        var returnValue: Boolean
        if (selectedLabel == " feet") {
            val h = selectedHeight.split(" ")
            val feet = "${h[0]}.${h[2]}".toDouble()
            if (feet in 4.0..7.0) {
                returnValue = true
            } else {
                returnValue = false
                showToast("Height should be in between 4 feet to 7 feet")
            }
        } else {
            val w = selectedHeight.split(" ")
            val height = w[0].toDouble()
            if (height in 120.0..220.0) {
                returnValue = true
            } else {
                returnValue = false
                showToast("Height should be in between 120 cms to 220 cms")
            }

        }
        return returnValue
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setCms() {
        numbers.clear()
        for (i in 0..250) {
            numbers.add(i * 1f) // Increment by 0.1  numbers.add(i * 1f)
        }
        adapterHeight.setType("cms")
        adapterHeight.notifyDataSetChanged()
    }

    private fun setFtIn() {
        numbers.clear()
        for (i in 0..250) {
            numbers.add(i * 1f) // Increment by 0.1  numbers.add(i * 1f)
        }
        adapterHeight.setType("feet")
        adapterHeight.notifyDataSetChanged()
    }

    private fun setLbsValue() {
        numbers.clear()
        for (i in 0..6615) {
            numbers.add(i / 10f)
        }
        adapterWeight.notifyDataSetChanged()
    }

    private fun setKgsValue() {
        numbers.clear()
        for (i in 0..3000) {
            numbers.add(i / 10f) // Increment by 0.1
        }
        adapterWeight.notifyDataSetChanged()
    }

    private fun showOptionPopup(
        anchor: View,
        options: ArrayList<Option>,
        header: String = "Select An Option",
        onSelected: (Option) -> Unit
    ) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_selector, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val recyclerView = popupView.findViewById<RecyclerView>(R.id.recyclerOptions)
        val tvLabel = popupView.findViewById<TextView>(R.id.tvLabel)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PopupOptionAdapter(options) {
            onSelected(it)
            popupWindow.dismiss()
        }

        tvLabel.text = header

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 16f
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(anchor, 0, 0)
    }

    private fun getQuestionerList() {
        Utils.showLoader(this)
        val call =
            apiService.getsubmoduletest(sharedPreferenceManager.accessToken, "FACIAL_SCAN")
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(
                call: Call<JsonElement?>,
                response: Response<JsonElement?>
            ) {
                Utils.dismissLoader(this@HealthCamBasicDetailsNewActivity)
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    responseObj = gson.fromJson(
                        jsonResponse,
                        QuestionListHealthAudit::class.java
                    )
                    setData()
                } else {
                    Toast.makeText(
                        this@HealthCamBasicDetailsNewActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                Utils.dismissLoader(this@HealthCamBasicDetailsNewActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun setData() {
        val userdata = SharedPreferenceManager.getInstance(this).userProfile.userdata
        for (question in responseObj!!.questionData.questionList) {
            when (question.question) {
                "first_name" -> {
                    if (userdata.firstName != null) binding.edtName.setText(userdata.firstName)
                }

                "height" -> {
                    if (userdata.height != null)
                        if (userdata.heightUnit == "FT_AND_INCHES") {
                            val height = userdata.height.toString().split(".")
                            binding.edtHeight.setText("${height[0]} Ft ${height[1]} In")
                        } else {
                            binding.edtHeight.setText("${userdata.height} cms")
                        }
                }

                "weight" -> {
                    if (userdata.weight != null) {
                        binding.edtWeight.setText("${userdata.weight} ${userdata.weightUnit}")
                    }
                }


                "age" -> {
                    binding.edtAge.setText(userdata.age.toString() + " years")
                }

                "gender" -> {
                    genderOptions.addAll(question.options)
                    if (userdata.gender == "M" || userdata.gender.equals(
                            "Male",
                            ignoreCase = true
                        )
                    ) binding.edtGender.setText("Male")
                    else binding.edtGender.setText("Female")
                }

                "smoking" -> {
                    smokeOptions.addAll(question.options)
                }

                "bloodpressuremedication" -> {
                    bpMedicationOptions.addAll(question.options)
                }

                "diabetes" -> {
                    diabeticsOptions.addAll(question.options)
                }
            }
        }
    }

    private fun submitAnswerRequest(
        requestAnswer: FaceScanQuestionRequest?,
        heightInCms: String,
        weightInKg: String,
        age: String
    ) {
        Utils.showLoader(this)
        val call = apiService.postAnswerRequest(
            sharedPreferenceManager.accessToken,
            "FACIAL_SCAN",
            requestAnswer
        )
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                Utils.dismissLoader(this@HealthCamBasicDetailsNewActivity)
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val healthCamSubmitResponse = gson.fromJson(
                        jsonResponse,
                        HealthCamSubmitResponse::class.java
                    )
                    if (healthCamSubmitResponse.status.equals(
                            "PAYMENT_INPROGRESS",
                            ignoreCase = true
                        )
                    ) {
                        val intent = Intent(
                            this@HealthCamBasicDetailsNewActivity,
                            SubscriptionPlanListActivity::class.java
                        )
                        intent.putExtra("SUBSCRIPTION_TYPE", "FACIAL_SCAN")
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(
                            this@HealthCamBasicDetailsNewActivity,
                            HealthCamRecorderActivity::class.java
                        )
                        intent.putExtra("reportID", healthCamSubmitResponse.data.answerId)
                        intent.putExtra("USER_PROFILE_HEIGHT", heightInCms)
                        intent.putExtra("USER_PROFILE_WEIGHT", weightInKg)
                        intent.putExtra("USER_PROFILE_AGE", age)
                        intent.putExtra("USER_PROFILE_GENDER", binding.edtGender.text.toString())
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@HealthCamBasicDetailsNewActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                Utils.dismissLoader(this@HealthCamBasicDetailsNewActivity)
                handleNoInternetView(t)
            }
        })
    }

}