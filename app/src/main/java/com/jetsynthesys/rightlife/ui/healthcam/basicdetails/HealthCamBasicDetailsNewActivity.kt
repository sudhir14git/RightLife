package com.jetsynthesys.rightlife.ui.healthcam.basicdetails

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.newquestionrequestfacescan.AnswerFaceScan
import com.jetsynthesys.rightlife.apimodel.newquestionrequestfacescan.FaceScanQuestionRequest
import com.jetsynthesys.rightlife.databinding.ActivityHealthcamBasicDetailsNewBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetAgeSelectionBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.customviews.HeightPickerBottomSheet
import com.jetsynthesys.rightlife.ui.customviews.WeightPickerBottomSheet
import com.jetsynthesys.rightlife.ui.healthaudit.questionlist.Option
import com.jetsynthesys.rightlife.ui.healthaudit.questionlist.QuestionListHealthAudit
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamSubmitResponse
import com.jetsynthesys.rightlife.ui.sdkpackage.HealthCamRecorderActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.ConversionUtils
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


class HealthCamBasicDetailsNewActivity : BaseActivity() {
    private lateinit var binding: ActivityHealthcamBasicDetailsNewBinding
    private var selectedWeight = "75.0 kg"
    private var selectedHeight = "5.8"
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

        binding.infoIcon.setOnClickListener {
            it.disableViewForSeconds()
            DialogUtils.showCommonBottomSheetDialog(
                this,
                header = "Gender At Birth",
                description = "We support all forms of gender expression. However, we need this information to calculate the most actionable body metrics for you."
            )
        }

        binding.edtAge.setOnClickListener {
            it.disableViewForSeconds()
            hideKeyboard()
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    showAgeSelectionBottomSheet()
                }
            }
        }

        binding.edtHeight.setOnClickListener {
            it.disableViewForSeconds()
            hideKeyboard()
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    val unit = if (binding.edtHeight.text.toString().trim().contains("ft")) "ft" else "cm"
                    openHeightPicker(unit)
                }
            }

        }

        binding.edtWeight.setOnClickListener {
            it.disableViewForSeconds()
            hideKeyboard()
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    val weightParts = binding.edtWeight.text.toString().trim().split(" ")

                    val value = weightParts.getOrNull(0)?.toDoubleOrNull() ?: 75.0
                    val unit = weightParts.getOrNull(1) ?: "kg"

                    openWeightPicker(value, unit)
                }
            }
        }

        binding.edtGender.setOnClickListener {
            it.disableViewForSeconds()
            showOptionPopup(binding.edtGender, genderOptions, "Select Gender") { option: Option ->
                binding.edtGender.setText(option.optionText)
            }
        }

        binding.edtBloodPressure.setOnClickListener {
            it.disableViewForSeconds()
            showOptionPopup(binding.edtBloodPressure, bpMedicationOptions) { option: Option ->
                binding.edtBloodPressure.setText(option.optionText)
            }
        }

        binding.edtSmoke.setOnClickListener {
            it.disableViewForSeconds()
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
            it.disableViewForSeconds()
            val name = binding.edtName.text.toString()
            val height = binding.edtHeight.text.toString()
            val weight = binding.edtWeight.text.toString()
            val age = binding.edtAge.text.toString()
            val gender = binding.edtGender.text.toString()
            val smoke = binding.edtSmoke.text.toString()
            val bpMedication = binding.edtBloodPressure.text.toString()
            val diabetic = binding.edtDiabetic.text.toString()

            if (name.isEmpty() || height.isEmpty() || weight.isEmpty() || age.isEmpty()
                || gender.isEmpty() || smoke.isEmpty() || bpMedication.isEmpty() || diabetic.isEmpty()
            )
                showCustomToast("Please fill all required fields before proceeding.")
            else if (age.split(" ")[0].toInt() !in 13..80)
                showCustomToast("Face Scan is available only for users aged 13–80.")
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
                    CommonAPICall.convertFeetInchToCmWithIndex(height).cmIndex.toString()


                /*      val weightInKg =
                          if (weightWithUnit[1].equals(
                                  "kgs",
                                  ignoreCase = true
                              )
                          ) weightWithUnit[0] else ConversionUtils.convertKgToLbs(
                              weightWithUnit[0]
                          )*/

                val unit = listOfNotNull(
                    weightWithUnit.getOrNull(1),
                    weightWithUnit.getOrNull(2)
                )

                val weightInKg = if (unit.any { it.equals("kg", true) || it.equals("kgs", true) }) {
                    weightWithUnit[0]
                } else {
                    ConversionUtils.convertKgToLbs(weightWithUnit[0])
                }

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
                AnalyticsLogger.logEvent(
                    this,
                    AnalyticsEvent.FaceScan_ProceedtoScan_Tap,
                    mapOf(
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    fun openHeightPicker(unit: String) {
        val bottomSheet = HeightPickerBottomSheet.newInstance(unit = unit)
        bottomSheet.setOnHeightSelectedListener { height, unit ->
            selectedHeight = height
            if (unit == "ft") {
                selectedHeight = "$height $unit"
                val height = height.toString().split(".")
                binding.edtHeight.setText("${height[0]} ft ${height[1]} in")
            } else {
                selectedHeight = "$height $unit"
                binding.edtHeight.setText("${height}")

            }
            //binding.edtHeight.setText("${selectedHeight}")
        }
        bottomSheet.show(supportFragmentManager, "HeightPicker")
    }

    private fun openWeightPicker(initialWeight: Double, unit: String) {
        val bottomSheet = WeightPickerBottomSheet.newInstance(initialWeight, unit)
        bottomSheet.setOnWeightSelectedListener { weight, unit ->
            selectedWeight = String.format("%.1f %s", weight, unit)
            binding.edtWeight.setText("$selectedWeight")
        }
        bottomSheet.show(supportFragmentManager, "WeightPicker")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showAgeSelectionBottomSheet() {
        var selectedAge = "27 years"
        binding.edtAge.setText(selectedAge)
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
        )


        val selectedAgeArray = binding.edtAge.text.toString().split(" ")
        val selectedAgeFromUi =
            if (selectedAgeArray.isNotEmpty() && selectedAgeArray[0].toInt() >= 13) {
                binding.edtAge.text.toString()
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

        selectedAge = if (selectedAgeFromUi.isNotEmpty())
            years[years.indexOf(selectedAgeFromUi)]
        else years[14]

        dialogBinding.numberPicker.setOnScrollListener { view, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                selectedAge = years[view.value - 1]
                binding.edtAge.setText(selectedAge)
            }
        }

        var startY = 0f
        var isScrolling = false

        dialogBinding.numberPicker.setOnTouchListener { v, event ->
            val picker = v as NumberPicker

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    isScrolling = false
                }

                MotionEvent.ACTION_MOVE -> {
                    // If finger moved enough vertically, mark as scrolling
                    if (Math.abs(event.y - startY) > picker.height / 20) {
                        isScrolling = true
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (!isScrolling) {
                        val height = picker.height
                        val visibleItems = 5
                        val rowHeight = height / visibleItems
                        val centerY = height / 2
                        val tolerance = rowHeight / 2

                        // Only dismiss if tapped on center row
                        if (event.y > centerY - tolerance && event.y < centerY + tolerance) {
                            bottomSheetDialog.dismiss()
                            return@setOnTouchListener true
                        }
                    }
                }
            }
            false
        }


        dialogBinding.btnConfirm.setOnClickListener {
            it.disableViewForSeconds()
            bottomSheetDialog.dismiss()
            binding.edtAge.setText(selectedAge)
        }

        // 👇 ADD THIS BLOCK TO FORCE EXPANDED STATE
        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            // Get the bottom sheet internal view
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout

            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                // Force the sheet to expand to its full WRAP_CONTENT height
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        bottomSheetDialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
        val userdata = sharedPreferenceManager.userProfile.userdata ?: return
        for (question in responseObj!!.questionData.questionList) {
            when (question.question) {
                "first_name" -> {
                    if (userdata.firstName != null) binding.edtName.setText(userdata.firstName)
                }

                "height" -> {
                    if (userdata.height != null)
                        if (userdata.heightUnit == "FT_AND_INCHES") {
                            val height = userdata.height.toString().split(".")
                            binding.edtHeight.setText("${height[0]} ft ${height[1]} in")
                        } else {
                            binding.edtHeight.setText("${userdata.height} cm")
                        }
                }

                "weight" -> {
                    if (userdata.weight != null) {
                        binding.edtWeight.setText(
                            "${userdata.weight} ${
                                userdata.weightUnit.lowercase(
                                    Locale.getDefault()
                                )
                            }"
                        )
                    }
                }


                /*   "age" -> {
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
                   }*/
                "age" -> {
                    // Check if age is NOT null and NOT zero before setting
                    if (userdata.age != null && userdata.age != 0) {
                        binding.edtAge.setText(userdata.age.toString() + " years")
                    } else {
                        binding.edtAge.setText("") // Set empty if null or zero
                    }
                }

                "gender" -> {
                    genderOptions.addAll(question.options)
                    val genderText = when (userdata.gender?.uppercase(Locale.getDefault())) {
                        "M", "MALE" -> "Male"
                        "F", "FEMALE" -> "Female"
                        else -> "" // Set to empty string if the value is null or neither Male nor Female
                    }
                    binding.edtGender.setText(genderText)
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
            override fun onResponse(
                call: Call<JsonElement?>,
                response: Response<JsonElement?>
            ) {
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
                        intent.putExtra(
                            "reportID",
                            healthCamSubmitResponse.data?.answerId
                        )
                        intent.putExtra("USER_PROFILE_HEIGHT", heightInCms)
                        intent.putExtra("USER_PROFILE_WEIGHT", weightInKg)
                        intent.putExtra("USER_PROFILE_AGE", age)
                        intent.putExtra(
                            "USER_PROFILE_GENDER",
                            binding.edtGender.text.toString()
                        )
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

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

}