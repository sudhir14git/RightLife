package com.jetsynthesys.rightlife.ui.profile_new

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.UploadImage
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.apimodel.userdata.Userdata
import com.jetsynthesys.rightlife.databinding.ActivityProfileNewBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetAgeSelectionBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetGenderSelectionBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetHeightSelectionBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetWeightSelectionBinding
import com.jetsynthesys.rightlife.databinding.DialogOtpVerificationBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.new_design.RulerAdapter
import com.jetsynthesys.rightlife.ui.new_design.RulerAdapterVertical
import com.jetsynthesys.rightlife.ui.profile_new.pojo.OtpRequest
import com.jetsynthesys.rightlife.ui.profile_new.pojo.PreSignedUrlData
import com.jetsynthesys.rightlife.ui.profile_new.pojo.PreSignedUrlResponse
import com.jetsynthesys.rightlife.ui.profile_new.pojo.VerifyOtpRequest
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.shawnlin.numberpicker.NumberPicker
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor

class ProfileNewActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileNewBinding
    private val CAMERA_REQUEST: Int = 100
    private val PICK_IMAGE_REQUEST: Int = 101
    private var cameraImageUri: Uri? = null
    private val numbers = mutableListOf<Float>()
    private lateinit var adapterHeight: RulerAdapterVertical
    private lateinit var adapterWeight: RulerAdapter
    private var dialogOtp: Dialog? = null
    private lateinit var userData: Userdata
    private lateinit var userDataResponse: UserProfileResponse
    private var preSignedUrlData: PreSignedUrlData? = null

    // Activity Result Launcher to get image
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            try {
                uri?.let {
                    binding.ivProfileImage.setImageURI(it)
                    binding.ivProfileImage.visibility = VISIBLE
                    binding.tvProfileLetter.visibility = GONE
                    getUrlFromURI(it)
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
                getUrlFromURI(cameraImageUri!!)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileNewBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        userDataResponse = sharedPreferenceManager.userProfile
        userData = userDataResponse.userdata
        setUserData(userData)

        // Non-editable field click listeners
        binding.llAge.setOnClickListener {
            showAgeSelectionBottomSheet()
        }
        binding.arrowAge.setOnClickListener {
            showAgeSelectionBottomSheet()
        }

        binding.tvGender.setOnClickListener {
            showGenderSelectionBottomSheet()
        }
        binding.arrowGender.setOnClickListener {
            showGenderSelectionBottomSheet()
        }

        binding.tvHeight.setOnClickListener {
            showHeightSelectionBottomSheet(userData.gender)
        }
        binding.arrowHeight.setOnClickListener {
            showHeightSelectionBottomSheet(userData.gender)
        }

        binding.tvWeight.setOnClickListener {
            showWeightSelectionBottomSheet(userData.gender)
        }
        binding.arrowWeight.setOnClickListener {
            showWeightSelectionBottomSheet(userData.gender)
        }

        binding.arrowDeleteAccount.setOnClickListener {
            startActivity(Intent(this, DeleteAccountSelectionActivity::class.java))
        }
        binding.llDeleteAccount.setOnClickListener {
            startActivity(Intent(this, DeleteAccountSelectionActivity::class.java))
        }

        binding.ivEditProfile.setOnClickListener {
            showImagePickerDialog()
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

        if (userData.firstName != null)
            binding.etFirstName.setText(userData.firstName)
        if (userData.lastName != null)
            binding.etLastName.setText(userData.lastName)
        if (userData.email != null)
            binding.etEmail.setText(userData.email)
        if (userData.phoneNumber != null)
            binding.etMobile.setText(userData.phoneNumber)
        if (userData.age != null)
            binding.tvAge.text = userData.age.toString() + " years"
        if (userData.gender == "M")
            binding.tvGender.text = "Male"
        else
            binding.tvGender.text = "Female"

        if (userData.weight != null)
            binding.tvWeight.text = "${userData.weight} ${userData.weightUnit}"

        if (userData.profilePicture.isNullOrEmpty())
            if (userData.firstName.isNotEmpty())
                binding.tvProfileLetter.text = userData.firstName.first().toString()
            else
                binding.tvProfileLetter.text = "R"
        else {
            binding.ivProfileImage.visibility = VISIBLE
            binding.tvProfileLetter.visibility = GONE
            Glide.with(this)
                .load(ApiClient.CDN_URL_QA + userData.profilePicture)
                .placeholder(R.drawable.rl_profile)
                .error(R.drawable.rl_profile)
                .into(binding.ivProfileImage)
        }

        if (userData.height != null)
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

    private fun getFileNameAndSize(context: Context, uri: Uri): Pair<String, Long>? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst()) {
                val name = it.getString(nameIndex)
                val size = it.getLong(sizeIndex)
                return Pair(name, size)
            }
        }
        return null
    }

    private fun getUrlFromURI(uri: Uri) {
        val uploadImage = UploadImage()
        uploadImage.isPublic = false
        uploadImage.fileType = "USER_FILES"
        if (uri != null) {
            val (fileName, fileSize) = getFileNameAndSize(this, uri) ?: return
            uploadImage.fileSize = fileSize
            uploadImage.fileName = fileName
        }
        uriToFile(uri)?.let { getPreSignedUrl(uploadImage, it) }
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

    @SuppressLint("ClickableViewAccessibility")
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
                binding.tvAge.text = selectedAge
            }
        }

        dialogBinding.numberPicker.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val picker = v as NumberPicker

                val y = event.y
                val height = picker.height
                val visibleItems = 5 // NumberPicker typically shows 3 visible rows
                val rowHeight = height / visibleItems
                val centerY = height / 2

                val tolerance = rowHeight / 2

                // Check if touch was within center row (selected value)
                if (y > centerY - tolerance && y < centerY + tolerance) {
                    bottomSheetDialog.dismiss()
                    return@setOnTouchListener true
                }
            }
            false
        }

        dialogBinding.btnConfirm.setOnClickListener {
            bottomSheetDialog.dismiss()
            binding.tvAge.text = selectedAge
        }

        bottomSheetDialog.show()
    }

    private fun showWeightSelectionBottomSheet(gender: String) {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)
        var selectedLabel = " KGS"
        var selectedWeight = binding.tvWeight.text.toString()
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

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.selectedNumberText.text = selectedWeight

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

        if (selectedLabel == " kg") {
            dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_left_selected)
            dialogBinding.kgOption.setTextColor(Color.WHITE)

            dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            dialogBinding.lbsOption.setTextColor(Color.BLACK)
            setKgsValue()

            dialogBinding.selectedNumberText.text = selectedWeight
        } else {
            dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_right_selected)
            dialogBinding.lbsOption.setTextColor(Color.WHITE)

            dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_left_unselected)
            dialogBinding.kgOption.setTextColor(Color.BLACK)
            setLbsValue()

            dialogBinding.selectedNumberText.text = selectedWeight
        }

        dialogBinding.kgOption.setOnClickListener {
            dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_left_selected)
            dialogBinding.kgOption.setTextColor(Color.WHITE)

            dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            dialogBinding.lbsOption.setTextColor(Color.BLACK)

            selectedLabel = " kg"
            selectedWeight = if (gender == "Male" || gender == "M")
                "75 kg"
            else
                "55 kg"
            setKgsValue()

            dialogBinding.rulerView.layoutManager?.scrollToPosition(if (gender == "Male" || gender == "M") 750 else 550)
            dialogBinding.selectedNumberText.text = selectedWeight
        }

        dialogBinding.lbsOption.setOnClickListener {
            dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_right_selected)
            dialogBinding.lbsOption.setTextColor(Color.WHITE)

            dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_left_unselected)
            dialogBinding.kgOption.setTextColor(Color.BLACK)

            selectedLabel = " lbs"
            selectedWeight = if (gender == "Male" || gender == "M")
                "165 lbs"
            else
                "120 lbs"
            setLbsValue()

            dialogBinding.rulerView.layoutManager?.scrollToPosition(if (gender == "Male" || gender == "M") 1650 else 1200)
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
                        dialogBinding.selectedNumberText.text = "$snappedNumber$selectedLabel"
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

        dialogBinding.rulerView.post {
            dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedWeight.split(" ")[0].toDouble() * 10).toInt())
        }

        dialogBinding.btnConfirm.setOnClickListener {
            bottomSheetDialog.dismiss()
            dialogBinding.rulerView.adapter = null
            binding.tvWeight.text = selectedWeight
        }

        bottomSheetDialog.show()
    }

    private fun showHeightSelectionBottomSheet(gender: String) {
        var selectedHeight = "5 Ft 10 In"
        var selectedLabel = " feet"
        // Create and configure BottomSheetDialog
        val decimalFormat = DecimalFormat("###.##")
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetHeightSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        dialogBinding.rulerView.layoutManager = layoutManager


        // Set up the animation
        val bottomSheetLayout =
            bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        adapterHeight = RulerAdapterVertical(numbers) { number ->
            // Handle the selected number
        }
        dialogBinding.rulerView.adapter = adapterHeight

        selectedHeight = binding.tvHeight.text.toString()
        if (selectedHeight.isEmpty()) {
            selectedHeight = "5 Ft 10 In"
        } else {
            val h = selectedHeight.split(" ")
            selectedLabel = if (h[1].equals("cms", ignoreCase = true))
                " cms"
            else
                " feet"
            if (selectedLabel == " feet") {
                dialogBinding.feetOption.setBackgroundResource(R.drawable.bg_left_selected)
                dialogBinding.feetOption.setTextColor(Color.WHITE)

                dialogBinding.cmsOption.setBackgroundResource(R.drawable.bg_right_unselected)
                dialogBinding.cmsOption.setTextColor(Color.BLACK)
                setFtIn()
            } else {
                dialogBinding.cmsOption.setBackgroundResource(R.drawable.bg_right_selected)
                dialogBinding.cmsOption.setTextColor(Color.WHITE)

                dialogBinding.feetOption.setBackgroundResource(R.drawable.bg_left_unselected)
                dialogBinding.feetOption.setTextColor(Color.BLACK)
                setCms()
            }
        }

        dialogBinding.selectedNumberText.text = selectedHeight


        dialogBinding.feetOption.setOnClickListener {
            dialogBinding.feetOption.setBackgroundResource(R.drawable.bg_left_selected)
            dialogBinding.feetOption.setTextColor(Color.WHITE)

            dialogBinding.cmsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            dialogBinding.cmsOption.setTextColor(Color.BLACK)

            selectedLabel = " feet"

            selectedHeight = if (gender == "Male" || gender == "M")
                "5 Ft 8 In"
            else
                "5 Ft 4 In"
            setFtIn()

            dialogBinding.rulerView.post {
                if (gender == "Male" || gender == "M") {
                    dialogBinding.rulerView.scrollToPosition(68 + 8)
                } else {
                    dialogBinding.rulerView.scrollToPosition(64 + 8)
                }
            }
            dialogBinding.selectedNumberText.text = selectedHeight
        }

        dialogBinding.cmsOption.setOnClickListener {
            dialogBinding.cmsOption.setBackgroundResource(R.drawable.bg_right_selected)
            dialogBinding.cmsOption.setTextColor(Color.WHITE)

            dialogBinding.feetOption.setBackgroundResource(R.drawable.bg_left_unselected)
            dialogBinding.feetOption.setTextColor(Color.BLACK)

            selectedLabel = " cms"

            selectedHeight = if (gender == "Male" || gender == "M")
                "173 cms"
            else
                "163 cms"
            setCms()

            dialogBinding.rulerView.post {
                if (gender == "Male" || gender == "M") {
                    dialogBinding.rulerView.scrollToPosition(173 + 8)
                } else {
                    dialogBinding.rulerView.scrollToPosition(163 + 8)
                }
            }
            dialogBinding.selectedNumberText.text = selectedHeight
        }

        if (selectedLabel == " cms") {
            setCms()
        } else {
            setFtIn()
        }

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
                                "${decimalFormat.format(snappedNumber)}$selectedLabel"
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

            selectedHeight = binding.tvHeight.text.toString()
            if (selectedHeight.isEmpty()) {
                selectedHeight = "5 Ft 10 In"
            } else {
                val h = selectedHeight.split(" ")
                selectedLabel = if (h[1].equals("cms", ignoreCase = true))
                    " cms"
                else
                    " feet"
            }

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
        val call = apiService.generateOtpForPhoneNumber(
            sharedPreferenceManager.accessToken,
            OtpRequest(mobileNumber)
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    showToast("Otp sent to your mobile number")
                    if (dialogOtp != null && dialogOtp?.isShowing == true) {
                        dialogOtp?.dismiss()
                    }
                    showOtpDialog(this@ProfileNewActivity, mobileNumber)
                } else {
                    showToast(response.message())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun showOtpDialog(activity: Activity, mobileNumber: String) {
        dialogOtp = Dialog(activity)
        val binding = DialogOtpVerificationBinding.inflate(LayoutInflater.from(activity))
        dialogOtp?.setContentView(binding.root)
        dialogOtp?.setCancelable(false)
        dialogOtp?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

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
                binding.tvCountdown.visibility = GONE
            }
        }
        timer.start()

        // Cancel Button
        binding.ivClose.setOnClickListener {
            timer.cancel()
            dialogOtp?.dismiss()
        }

        binding.tvChange.setOnClickListener {
            timer.cancel()
            dialogOtp?.dismiss()
        }

        binding.tvResend.setOnClickListener {
            timer.cancel()
            generateOtp(mobileNumber)
        }

        binding.tvPhone.text = mobileNumber

        // Verify Button
        binding.btnVerify.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString().trim() }
            if (otp.length == 6) {
                showToast("OTP Verified: $otp")
                verifyOtp(mobileNumber, otp, binding)
                timer.cancel()
            } else {
                showToast("Enter all 6 digits")
            }
        }

        dialogOtp?.show()
    }

    private fun verifyOtp(
        mobileNumber: String,
        otp: String,
        bindingDialog: DialogOtpVerificationBinding
    ) {
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
                    bindingDialog.tvResult.text = "(Verification Success)"
                    bindingDialog.tvResult.setTextColor(getColor(R.color.color_green))
                    binding.btnVerify.text = "Verified"
                    binding.btnVerify.isEnabled = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialogOtp?.dismiss()
                    }, 2000)
                } else {
                    bindingDialog.tvResult.text = "(Verification Failed-Incorrect OTP)"
                    bindingDialog.tvResult.setTextColor(getColor(R.color.menuselected))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
                bindingDialog.tvResult.text = "(Verification Failed-Incorrect OTP)"
                bindingDialog.tvResult.setTextColor(getColor(R.color.menuselected))
            }

        })
    }

    private fun uriToFile(uri: Uri): File? {
        val contentResolver = contentResolver
        val fileName = getFileName(uri) ?: "temp_image_file"
        val tempFile = File(cacheDir, fileName)

        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val returnCursor = contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
        return name
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
            val ageArray = age.split(" ")
            userData.age = ageArray[0].toInt()
            if (gender.equals("Male", ignoreCase = true)) {
                userData.gender = "M"
            } else {
                userData.gender = "F"
            }
            if (weight.isNotEmpty()) {
                val w = weight.split(" ")
                userData.weight = w[0].toDouble()
                if ("kgs".equals(w[1], ignoreCase = true) || "kg".equals(w[1], ignoreCase = true)) {
                    userData.weightUnit = "KG"
                } else {
                    userData.weightUnit = "LBS"
                }
            }

            if (height.isNotEmpty()) {
                val h = height.split(" ")
                if (h.size == 2) {
                    userData.height = h[0].toDouble()
                    userData.heightUnit = "CM"
                } else {
                    userData.height = "${h[0]}.${h[2]}".toDouble()
                    userData.heightUnit = "FT_AND_INCHES"
                }
            }

            if (preSignedUrlData != null) {
                userData.profilePicture = preSignedUrlData?.file?.url
            }
            updateUserData(userData)
            updateChecklistStatus()
            var productId = ""
            sharedPreferenceManager.userProfile.subscription.forEach { subscription ->
                if (subscription.status) {
                    productId = subscription.productId
                }
            }
            AnalyticsLogger.logEvent(
                AnalyticsEvent.CHECKLIST_PROFILE_COMPLETE, mapOf(
                    AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                    AnalyticsParam.TIME_TO_COMPLETE to "",
                    AnalyticsParam.GENDER to userData.gender,
                    AnalyticsParam.AGE to userData.age,
                    AnalyticsParam.WEIGHT to userData.weight,
                    AnalyticsParam.HEIGHT to userData.height,
                    AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                    AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
                    AnalyticsParam.USER_PLAN to productId,
                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                )
            )
        }
    }

    private fun updateChecklistStatus() {
        CommonAPICall.updateChecklistStatus(this, "profile", AppConstants.CHECKLIST_COMPLETED)
    }

    private fun updateUserData(userdata: Userdata) {
        val call: Call<ResponseBody> =
            apiService.updateUser(sharedPreferenceManager.accessToken, userdata)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    setResult(RESULT_OK)
                    userDataResponse.userdata = userdata
                    sharedPreferenceManager.saveUserProfile(userDataResponse)
                    showToast("Profile Updated Successfully")
                    finish()
                } else {
                    showToast("Server Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun getPreSignedUrl(uploadImage: UploadImage, file: File) {
        val call: Call<PreSignedUrlResponse> =
            apiService.getPreSignedUrl(sharedPreferenceManager.accessToken, uploadImage)

        call.enqueue(object : Callback<PreSignedUrlResponse?> {
            override fun onResponse(
                call: Call<PreSignedUrlResponse?>,
                response: Response<PreSignedUrlResponse?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.let { preSignedUrlData = it }
                    response.body()?.data?.url?.let {
                        CommonAPICall.uploadImageToPreSignedUrl(
                            this@ProfileNewActivity,
                            file, it
                        ) { success ->
                            if (success) {
                                showToast("Image uploaded successfully!")
                            } else {
                                showToast("Upload failed!")
                            }
                        }
                    }
                } else {
                    showToast("Server Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<PreSignedUrlResponse?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Upload Profile Picture")
            .setItems(options) { dialog, which ->
                when (options[which]) {
                    "Take Photo" -> checkPermissions()
                    "Choose from Gallery" -> openGallery()
                    "Cancel" -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*") // Open gallery to pick image
    }

}
