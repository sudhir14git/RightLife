package com.example.rlapp.ui.profile_new

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.rlapp.R
import com.example.rlapp.RetrofitData.ApiClient
import com.example.rlapp.RetrofitData.ApiService
import com.example.rlapp.apimodel.userdata.UserProfileResponse
import com.example.rlapp.apimodel.userdata.Userdata
import com.example.rlapp.databinding.ActivityProfileNewBinding
import com.example.rlapp.databinding.BottomsheetAgeSelectionBinding
import com.example.rlapp.databinding.BottomsheetGenderSelectionBinding
import com.example.rlapp.databinding.BottomsheetHeightSelectionBinding
import com.example.rlapp.databinding.BottomsheetWeightSelectionBinding
import com.example.rlapp.databinding.DialogOtpVerificationBinding
import com.example.rlapp.ui.new_design.RulerAdapter
import com.example.rlapp.ui.new_design.RulerAdapterVertical
import com.example.rlapp.ui.profile_new.pojo.OtpRequest
import com.example.rlapp.ui.profile_new.pojo.VerifyOtpRequest
import com.example.rlapp.ui.utility.ConversionUtils
import com.example.rlapp.ui.utility.SharedPreferenceManager
import com.example.rlapp.ui.utility.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shawnlin.numberpicker.NumberPicker
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor

class ProfileNewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileNewBinding
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private val CAMERA_REQUEST: Int = 100
    private val PICK_IMAGE_REQUEST: Int = 101
    private var cameraImageUri: Uri? = null
    private var selectedHeight = "5 Ft 10 In"
    private val numbers = mutableListOf<Float>()
    private lateinit var adapterHeight: RulerAdapterVertical
    private var selectedLabel: String = " feet"
    private lateinit var adapterWeight: RulerAdapter
    private lateinit var dialogOtp: Dialog
    private lateinit var userData: Userdata
    private lateinit var userDataResponse: UserProfileResponse

    // Activity Result Launcher to get image
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            try {
                uri?.let {
                    binding.ivProfileImage.setImageURI(it)
                    binding.ivProfileImage.visibility = VISIBLE
                    binding.tvProfileLetter.visibility = GONE
                } ?: run {
                    showToast("No image selected")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error loading image")
            }
        }

    // Camera launcher
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraImageUri != null) {
                binding.ivProfileImage.setImageURI(cameraImageUri)
                binding.ivProfileImage.visibility = VISIBLE
                binding.tvProfileLetter.visibility = GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

        userDataResponse = sharedPreferenceManager.userProfile
        userData = userDataResponse.userdata
        setUserData(userData)

        // Non-editable field click listeners
        binding.llAge.setOnClickListener {
            showAgeSelectionBottomSheet()
        }

        binding.tvGender.setOnClickListener {
            showGenderSelectionBottomSheet()
        }

        binding.tvHeight.setOnClickListener {
            showHeightSelectionBottomSheet()
        }

        binding.tvWeight.setOnClickListener {
            showWeightSelectionBottomSheet()
        }

        binding.llDeleteAccount.setOnClickListener {
            startActivity(Intent(this, DeleteAccountSelectionActivity::class.java))
        }

        binding.ivEditProfile.setOnClickListener {
            pickImageLauncher.launch("image/*") // Open gallery to pick image
            /*if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                checkPermissions()
            } else {
                openCamera()
            }*/
        }

        binding.btnVerify.setOnClickListener {
            val mobileNumber = binding.etMobile.text.toString()
            if (mobileNumber.isEmpty()) {
                showToast("Please enter 10 digit mobile number")
            } else if (mobileNumber.length != 10) {
                showToast("Please enter correct mobile number")
            } else {
                generateOtp("+91$mobileNumber")
            }
        }

        binding.btnSave.setOnClickListener {
            saveData()
        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setUserData(userData: Userdata) {

        binding.etFirstName.setText(userData.firstName)
        binding.etLastName.setText(userData.lastName)
        binding.etEmail.setText(userData.email)
        binding.etMobile.setText(userData.phoneNumber)
        if (userData.gender == "M")
            binding.tvGender.text = "Male"
        else
            binding.tvGender.text = "Female"
        binding.tvWeight.text = "${userData.weight} ${userData.weightUnit}"
        binding.tvProfileLetter.text = userData.firstName.first().toString()

        if (userData.heightUnit == "FT_AND_INCHES") {
            val height = userData.height.toString().split(".")
            binding.tvHeight.text = "${height[0]} Ft ${height[1]} In"
        } else {
            binding.tvHeight.text = "${userData.height} cms"
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        cameraImageUri?.let { uri ->
            cameraLauncher.launch(uri)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun showAgeSelectionBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetAgeSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
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

        dialogBinding.numberPicker.apply {
            minValue = 1
            maxValue = years.size
            displayedValues = years
            value = 13
            wheelItemCount = 7
        }
        var selectedAge = years[12]

        dialogBinding.numberPicker.setOnScrollListener { view, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                selectedAge = years[view.value - 1]
            }
        }

        dialogBinding.btnConfirm.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.tvAge.text = selectedAge
        }

        bottomSheetDialog.show()
    }

    private fun showWeightSelectionBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)
        var selectedLabel = " kg"
        var selectedWeight = binding.tvWeight.text.toString()
        if (selectedWeight.isEmpty()) {
            selectedWeight = "50 kg"
        } else {
            val w = selectedWeight.split(" ")
            selectedLabel = " ${w[1]}"
        }


        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetWeightSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
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
                selectedLabel = " kgs"
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

        for (i in 0..1000) {
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

        dialogBinding.btnConfirm.setOnClickListener {
            bottomSheetDialog.dismiss()
            dialogBinding.rulerView.adapter = null
            binding.tvWeight.text = selectedWeight
        }

        bottomSheetDialog.show()
    }

    private fun showHeightSelectionBottomSheet() {
        // Create and configure BottomSheetDialog
        val decimalFormat = DecimalFormat("###.##")
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetHeightSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        selectedHeight = binding.tvHeight.text.toString()
        if (selectedHeight.isEmpty()) {
            selectedHeight = "5 Ft 10 In"
        } else {
            val h = selectedHeight.split(" ")
            selectedLabel = if (h[1] == "cms")
                " cms"
            else
                " feet"
        }

        if (selectedLabel == " cms") {
            dialogBinding.switchHeightMetric.isChecked = true
        }

        dialogBinding.selectedNumberText.text = selectedHeight
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        dialogBinding.rulerView.layoutManager = layoutManager


        dialogBinding.switchHeightMetric.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val h = selectedHeight.split(" ")
                val feet = "${h[0]}.${h[2]}"
                selectedLabel = " cms"
                selectedHeight = ConversionUtils.convertFeetToCentimeter(feet)
                setCms()
                selectedHeight = decimalFormat.format(selectedHeight.toDouble())
                dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedHeight.toDouble()).toInt())
                selectedHeight += selectedLabel
            } else {
                val w = selectedHeight.split(" ")
                selectedLabel = " feet"
                selectedHeight = ConversionUtils.convertCentimeterToFtInch(w[0])
                setFtIn()
                val h = selectedHeight.split(".")
                val ft = h[0]
                val inch = h[1]
                dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedHeight.toDouble()).toInt() * 12)
                selectedHeight = "$ft Ft $inch In"
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
                        dialogBinding.selectedNumberText.text =
                            "${decimalFormat.format(snappedNumber)} $selectedLabel"
                        if (selectedLabel == " feet") {
                            val feet = decimalFormat.format(snappedNumber / 12)
                            val h = (feet).toString().split(".")
                            val ft = h[0]
                            var inch = "0"
                            if (h.size > 1) {
                                inch = h[1]
                            }
                            dialogBinding.selectedNumberText.text = "$ft Ft $inch In"
                        }
                        selectedHeight = dialogBinding.selectedNumberText.text.toString()

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

        dialogBinding.btnConfirm.setOnClickListener {
            if (validateInput()) {
                bottomSheetDialog.dismiss()
                dialogBinding.rulerView.adapter = null
                binding.tvHeight.text = selectedHeight
            }
        }

        bottomSheetDialog.show()
    }

    private fun showGenderSelectionBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetGenderSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.llMale.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.tvGender.text = dialogBinding.tvMale.text
        }

        dialogBinding.llFemale.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.tvGender.text = dialogBinding.tvFemale.text
        }

        bottomSheetDialog.show()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST
            )
        } else {
            openCamera()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setLbsValue() {
        numbers.clear()
        for (i in 0..2204) {
            numbers.add(i / 10f)
        }
        adapterWeight.notifyDataSetChanged()
    }

    private fun setKgsValue() {
        numbers.clear()
        for (i in 0..1000) {
            numbers.add(i / 10f) // Increment by 0.1
        }
        adapterWeight.notifyDataSetChanged()
    }

    private fun validateInput(): Boolean {
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

    private fun generateOtp(mobileNumber: String) {
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val call = apiService.generateOtpForPhoneNumber(
            sharedPreferenceManager.accessToken,
            OtpRequest(mobileNumber)
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    showToast("Otp sent to your mobile number")
                    showOtpDialog(this@ProfileNewActivity, mobileNumber)
                } else {
                    showToast(response.message())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("Network Error: " + t.message)
            }
        })
    }

    private fun showOtpDialog(activity: Activity, mobileNumber: String) {
        dialogOtp = Dialog(activity)
        val binding = DialogOtpVerificationBinding.inflate(LayoutInflater.from(activity))
        dialogOtp.setContentView(binding.root)
        dialogOtp.setCancelable(false)
        dialogOtp.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val otpFields = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        // Move focus automatically
        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        // Countdown Timer
        val timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                binding.tvCountdown.text = "Expired"
            }
        }
        timer.start()

        // Cancel Button
        binding.ivClose.setOnClickListener {
            timer.cancel()
            dialogOtp.dismiss()
        }

        // Verify Button
        binding.btnVerify.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString().trim() }
            if (otp.length == 6) {
                Toast.makeText(activity, "OTP Verified: $otp", Toast.LENGTH_SHORT).show()
                verifyOtp(mobileNumber, otp, binding)
                timer.cancel()
            } else {
                Toast.makeText(activity, "Enter all 6 digits", Toast.LENGTH_SHORT).show()
            }
        }

        dialogOtp.show()
    }

    private fun verifyOtp(
        mobileNumber: String,
        otp: String,
        binding: DialogOtpVerificationBinding
    ) {
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val call = apiService.verifyOtpForPhoneNumber(
            sharedPreferenceManager.accessToken,
            VerifyOtpRequest(
                phoneNumber = mobileNumber,
                otp = otp
            )
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    showToast(response.message())
                    binding.tvResult.text = "(Verification Success)"
                    binding.tvResult.setTextColor(getColor(R.color.color_green))
                } else {
                    showToast("Server Error: " + response.code())
                    binding.tvResult.text = "(Verification Failed-Incorrect OTP)"
                    binding.tvResult.setTextColor(getColor(R.color.menuselected))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("Network Error: " + t.message)
                binding.tvResult.text = "(Verification Failed-Incorrect OTP)"
                binding.tvResult.setTextColor(getColor(R.color.menuselected))
            }

        })
    }

    private fun saveData() {
        val firstName = binding.etFirstName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val email = binding.etEmail.text.toString()
        val mobileNumber = binding.etMobile.text.toString()
        val age = binding.tvAge.text.toString()
        val height = binding.tvHeight.text.toString()
        val weight = binding.tvWeight.text.toString()
        val gender = binding.tvGender.text.toString()
        if (firstName.isEmpty()) {
            showToast("First Name is required")
        } else if (lastName.isEmpty()) {
            showToast("Last Name is required")
        } else if (email.isEmpty()) {
            showToast("Email is required")
        } else if (!email.matches(Utils.emailPattern.toRegex())) {
            showToast("Invalid Email format")
        } else if (mobileNumber.isEmpty() || mobileNumber.length != 10) {
            showToast("Phone Number is incorrect")
        } else if (age.isEmpty()) {
            showToast("Please select Age")
        } else if (gender.isEmpty()) {
            showToast("Please select Gender")
        } else if (height.isEmpty()) {
            showToast("Please select Height")
        } else if (weight.isEmpty()) {
            showToast("Please select Weight")
        } else {
            userData.firstName = firstName
            userData.lastName = lastName
            userData.email = email
            userData.phoneNumber = mobileNumber
            if (gender.equals("Male", ignoreCase = true)) {
                userData.gender = "M"
            } else {
                userData.gender = "F"
            }
            val w = weight.split(" ")
            userData.weight = w[0].toDouble()
            if ("kgs".equals(w[1], ignoreCase = true)) {
                userData.weightUnit = "KG"
            } else {
                userData.weightUnit = "LBS"
            }

            val h = height.split(" ")
            if (h.size == 2) {
                userData.height = h[0].toDouble()
                userData.heightUnit = "CM"
            } else {
                userData.height = "${h[0]}.${h[2]}".toDouble()
                userData.heightUnit = "FT_AND_INCHES"
            }
            updateUserData(userData)
        }
    }

    private fun updateUserData(userdata: Userdata) {
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val call: Call<ResponseBody> =
            apiService.updateUser(sharedPreferenceManager.accessToken, userdata)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    setResult(RESULT_OK)
                    userDataResponse.userdata = userdata
                    sharedPreferenceManager.saveUserProfile(userDataResponse)
                    finish()
                } else {
                    Toast.makeText(
                        this@ProfileNewActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Toast.makeText(
                    this@ProfileNewActivity,
                    "Network Error: " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}
