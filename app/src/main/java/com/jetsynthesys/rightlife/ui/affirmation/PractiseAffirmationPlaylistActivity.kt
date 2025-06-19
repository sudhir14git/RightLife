package com.jetsynthesys.rightlife.ui.affirmation

import android.Manifest
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityPratciseAffirmationPlaylistBinding
import com.jetsynthesys.rightlife.databinding.BottmsheetReminderSelectionBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetReminserSetBinding
import com.jetsynthesys.rightlife.databinding.DialogPraticeTimeAffirmationBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.affirmation.adapter.AffirmationCardPagerAdapter
import com.jetsynthesys.rightlife.ui.affirmation.adapter.WeekDayAdapter
import com.jetsynthesys.rightlife.ui.affirmation.pojo.AffirmationSelectedCategoryData
import com.jetsynthesys.rightlife.ui.affirmation.pojo.GetAffirmationPlaylistResponse
import com.jetsynthesys.rightlife.ui.affirmation.pojo.GetWatchedAffirmationPlaylistResponse
import com.jetsynthesys.rightlife.ui.affirmation.pojo.WatchAffirmationPlaylistRequest
import com.jetsynthesys.rightlife.ui.utility.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class PractiseAffirmationPlaylistActivity : BaseActivity() {
    private lateinit var binding: ActivityPratciseAffirmationPlaylistBinding

    private val affirmationList: ArrayList<AffirmationSelectedCategoryData> = ArrayList()
    private lateinit var affirmationCardPagerAdapter: AffirmationCardPagerAdapter
    private var seconds: Int = 0
    private var timerJob: Job? = null
    private val DELAY_TIME: Long = 1000
    private lateinit var reminderBottomSheetDialog: BottomSheetDialog
    private lateinit var reminderSetBottomSheetDialog: BottomSheetDialog
    private var selectedMorningTime: String? = ""
    private var selectedAfternoonTime: String? = ""
    private var selectedEveningTime: String? = ""
    private var watchedResponse: GetWatchedAffirmationPlaylistResponse? = null
    private var position = 0
    private var startDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPratciseAffirmationPlaylistBinding.inflate(layoutInflater)
        setChildContentView(binding.getRoot())
        startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        getAffirmationPlaylist()
        getWatchedAffirmationPlaylist(0)
        setupReminderBottomSheet()

        setCardPlaylistAdapter(affirmationList)

        onBackPressedDispatcher.addCallback(this) {
            callPostMindFullDataAPI()
            finish()
        }

        binding.ivDownload.setOnClickListener {
            val savedImageUri =
                affirmationCardPagerAdapter.getViewAt(binding.cardViewPager.currentItem)
                    ?.let { it1 -> saveLinearLayoutToGallery(it1) }
            savedImageUri?.let {
                Toast.makeText(this, "Image saved to gallery at: $it", Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.ivClose.setOnClickListener {
            stopTimer()
            updateWatchedAffirmationPlaylist()
        }

        binding.cardViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (position == affirmationList.size - 1) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        stopTimer()
                        updateWatchedAffirmationPlaylist()
                    }, 2000)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun setCardPlaylistAdapter(affirmationList: ArrayList<AffirmationSelectedCategoryData>) {
        affirmationCardPagerAdapter =
            AffirmationCardPagerAdapter(affirmationList, this, binding.cardViewPager)
        binding.cardViewPager.setPageTransformer(true, AffirmationPageTransformer())
        binding.cardViewPager.adapter = affirmationCardPagerAdapter
    }

    private fun saveLinearLayoutToGallery(view: View): String? {
        // Create a bitmap with the same dimensions as the LinearLayout
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        // Create a Canvas object to draw on the bitmap
        val canvas = Canvas(bitmap)

        // Draw the LinearLayout onto the canvas
        view.draw(canvas)

        // Check if the device is running Android Q (API level 29) or higher
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore to save the image to the gallery
            saveImageToMediaStore(bitmap, this)
        } else {
            // Fallback for older Android versions, using traditional file saving method
            saveImageToOldStorage(bitmap, this)
        }
    }

    // Save the bitmap using MediaStore (for Android 10 and above)
    private fun saveImageToMediaStore(bitmap: Bitmap, context: Context): String? {
        val contentResolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Card${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp") // Save to Pictures folder
        }

        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        return try {
            imageUri?.let { uri ->
                val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                outputStream?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                uri.toString() // Return the URI of the saved image
            } ?: run {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Fallback for devices below Android Q (before scoped storage)
    private fun saveImageToOldStorage(bitmap: Bitmap, context: Context): String? {
        val directory = context.getExternalFilesDir(null)
        val file = File(directory, "card.png")
        return try {
            FileOutputStream(file).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            }
            file.absolutePath // Return the path of the saved image
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun startTimer() {
        timerJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(DELAY_TIME)
                    seconds++
                    updateTimerDisplay()
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    private fun updateTimerDisplay() {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        val formattedTime = String.format("%02d:%02d", minutes, remainingSeconds)
        binding.tvTimer.text = formattedTime
    }

    private fun showPracticeCompleteDialog() {
        // Create the dialog
        val dialog = Dialog(this)
        val dialogBinding = DialogPraticeTimeAffirmationBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window

        // Set the dim amount
        val layoutParams = window?.attributes
        layoutParams?.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window?.attributes = layoutParams

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        layoutParams?.width = width

        dialogBinding.ivDialogClose.setOnClickListener {
            dialog.dismiss()
            reminderBottomSheetDialog.show()
        }

        val adapter = watchedResponse?.data?.let { WeekDayAdapter(it) }

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.weekRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(
            this@PractiseAffirmationPlaylistActivity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerView.adapter = adapter

        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayOfWeek: String = sdf.format(calendar.time)

        if (watchedResponse != null) {
            for (i in 0 until (watchedResponse?.data?.size ?: 0)) {
                val item = watchedResponse?.data?.get(i)
                if (dayOfWeek == item?.day)
                    position = i
            }
        }
        dialogBinding.tvDialogAffirmations.text =
            watchedResponse?.data?.get(position)?.readAffirmation.toString()
        dialogBinding.tvDialogDuration.text =
            watchedResponse?.data?.get(position)?.duration.toString()
        dialogBinding.tvDialogTotalSessions.text =
            watchedResponse?.data?.get(position)?.totalSession.toString()

        dialog.show()
    }

    private fun setupReminderBottomSheet() {
        // Create and configure BottomSheetDialog
        reminderBottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottmsheetReminderSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        reminderBottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }
        reminderBottomSheetDialog.setCancelable(false)

        dialogBinding.ivDialogClose.setOnClickListener {
            callPostMindFullDataAPI()
            finish()
        }

        dialogBinding.llMorningTime.isClickable = false
        dialogBinding.llAfternoonTime.isClickable = false
        dialogBinding.llEveningTime.isClickable = false

        dialogBinding.llMorningRadio.setOnClickListener {
            dialogBinding.radioMorning.isChecked = !dialogBinding.radioMorning.isChecked
            dialogBinding.llMorningTime.isClickable = dialogBinding.radioMorning.isChecked
        }

        dialogBinding.llAfternoonRadio.setOnClickListener {
            dialogBinding.radioAfternoon.isChecked = !dialogBinding.radioAfternoon.isChecked
            dialogBinding.llAfternoonTime.isClickable = dialogBinding.radioAfternoon.isChecked
        }

        dialogBinding.llEveningRadio.setOnClickListener {
            dialogBinding.radioAEvening.isChecked = !dialogBinding.radioAEvening.isChecked
            dialogBinding.llEveningTime.isClickable = dialogBinding.radioAEvening.isChecked
        }

        dialogBinding.llMorningTime.setOnClickListener {
            showTimePickerDialog(dialogBinding.tvTimeMorning, 1, 9)
        }

        dialogBinding.llAfternoonTime.setOnClickListener {
            showTimePickerDialog(dialogBinding.tvTimeAfternoon, 2, 13)
        }

        dialogBinding.llEveningTime.setOnClickListener {
            showTimePickerDialog(dialogBinding.tvTimeEvening, 3, 20)
        }

        dialogBinding.btnSetReminder.setOnClickListener {
            if (!dialogBinding.radioMorning.isChecked && !dialogBinding.radioAfternoon.isChecked && !dialogBinding.radioAEvening.isChecked) {
                Toast.makeText(
                    this@PractiseAffirmationPlaylistActivity,
                    "Please at least one reminder!!", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (dialogBinding.radioMorning.isChecked) {
                selectedMorningTime = dialogBinding.tvTimeMorning.text.toString()
                selectedMorningTime?.let { it1 -> setReminder(it1) }
            }
            if (dialogBinding.radioAfternoon.isChecked) {
                selectedAfternoonTime = dialogBinding.tvTimeAfternoon.text.toString()
                selectedAfternoonTime?.let { it1 -> setReminder(it1) }
            }
            if (dialogBinding.radioAEvening.isChecked) {
                selectedEveningTime = dialogBinding.tvTimeEvening.text.toString()
                selectedEveningTime?.let { it1 -> setReminder(it1) }
            }

            reminderBottomSheetDialog.dismiss()
            setupReminderSetBottomSheet()
            reminderSetBottomSheetDialog.show()
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
                return false
            } else {
                return true
            }
        } else {
            // Permission not required before Android 13
            return true
        }
    }

    private fun setReminder(time: String) {
        if (!checkPermission()) {
            return
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            action = "PRACTICE_ALARM_TRIGGERED"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            100, // Unique code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse "6:40 PM" properly
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = sdf.parse(time)

        val calendar = Calendar.getInstance()
        calendar.time = date!!
        // Set today's date
        val now = Calendar.getInstance()
        calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

        // If time already passed, schedule for tomorrow
        if (calendar.before(now)) {
            calendar.add(Calendar.DATE, 1)
        }

        val triggerTime = calendar.timeInMillis


        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    private fun setupReminderSetBottomSheet() {
        // Create and configure BottomSheetDialog
        reminderSetBottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetReminserSetBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        reminderSetBottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }
        reminderSetBottomSheetDialog.setCancelable(false)

        dialogBinding.ivDialogClose.setOnClickListener {
            callPostMindFullDataAPI()
            finish()
        }

        if (selectedMorningTime.isNullOrEmpty()) {
            dialogBinding.llMorningTime.visibility = View.GONE
            dialogBinding.tvMorningText.visibility = View.GONE
        } else {
            dialogBinding.tvTimeMorning.text = selectedMorningTime
        }

        if (selectedAfternoonTime.isNullOrEmpty()) {
            dialogBinding.llAfternoonTime.visibility = View.GONE
            dialogBinding.tvAfternoonText.visibility = View.GONE
        } else {
            dialogBinding.tvTimeAfternoon.text = selectedAfternoonTime
        }

        if (selectedEveningTime.isNullOrEmpty()) {
            dialogBinding.llEveningTime.visibility = View.GONE
            dialogBinding.tvEveningText.visibility = View.GONE
        } else {
            dialogBinding.tvTimeEvening.text = selectedEveningTime
        }

    }

    private fun showTimePickerDialog(
        textView: TextView, type: Int, hour: Int,
        minute: Int = 0
    ) {
        val mTimePicker = TimePickerDialog(
            this@PractiseAffirmationPlaylistActivity,
            { timePicker, selectedHour, selectedMinute ->
                run {

                    val amPm = if (selectedHour >= 12) "PM" else "AM"
                    val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                    val minuteFormatted = String.format("%02d", selectedMinute)
                    when (type) {
                        1 ->
                            if (selectedHour <= 12)
                                selectedMorningTime = textView.text.toString()
                            else {
                                Toast.makeText(
                                    this@PractiseAffirmationPlaylistActivity,
                                    "Please select a morning time (AM only)",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TimePickerDialog
                            }

                        2 ->
                            if (selectedHour in 12..18)
                                selectedAfternoonTime = textView.text.toString()
                            else {
                                Toast.makeText(
                                    this,
                                    "Please select a time in the afternoon.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TimePickerDialog
                            }

                        3 ->
                            if (selectedHour >= 18)
                                selectedEveningTime = textView.text.toString()
                            else {
                                Toast.makeText(
                                    this,
                                    "Please select a time in the evening (6:00 PM to 11:59 PM).",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    textView.text = "$hourFormatted:$minuteFormatted $amPm"
                }
            },
            hour,
            minute,
            false
        ) // 12 hour time
        mTimePicker.setTitle("Select Time")
        mTimePicker.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAffirmationPlaylist() {
        Utils.showLoader(this)
        val call = apiService.getAffirmationPlaylist(sharedPreferenceManager.accessToken)

        call.enqueue(object : Callback<GetAffirmationPlaylistResponse> {
            override fun onResponse(
                call: Call<GetAffirmationPlaylistResponse>,
                response: Response<GetAffirmationPlaylistResponse>
            ) {
                Utils.dismissLoader(this@PractiseAffirmationPlaylistActivity)
                if (response.isSuccessful && response.body() != null) {
                    affirmationList.clear()
                    response.body()?.data?.let { affirmationList.addAll(it) }
                    if (affirmationList.size == 0) {
                        startActivity(
                            Intent(
                                this@PractiseAffirmationPlaylistActivity,
                                TodaysAffirmationActivity::class.java
                            )
                        )
                    }
                    setCardPlaylistAdapter(affirmationList)
                    startTimer()
                } else {
                    Toast.makeText(
                        this@PractiseAffirmationPlaylistActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<GetAffirmationPlaylistResponse>, t: Throwable) {
                Utils.dismissLoader(this@PractiseAffirmationPlaylistActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun getWatchedAffirmationPlaylist(type: Int) {
        Utils.showLoader(this)
        val call = apiService.getWatchedAffirmationPlaylist(sharedPreferenceManager.accessToken)

        call.enqueue(object : Callback<GetWatchedAffirmationPlaylistResponse> {
            override fun onResponse(
                call: Call<GetWatchedAffirmationPlaylistResponse>,
                response: Response<GetWatchedAffirmationPlaylistResponse>
            ) {
                Utils.dismissLoader(this@PractiseAffirmationPlaylistActivity)
                if (response.isSuccessful && response.body() != null) {
                    watchedResponse = response.body()
                    if (type == 1) {
                        CommonAPICall.postWellnessStreak(
                            this@PractiseAffirmationPlaylistActivity,
                            "Affirmation"
                        )
                        showPracticeCompleteDialog()
                    }
                } else {
                    Toast.makeText(
                        this@PractiseAffirmationPlaylistActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<GetWatchedAffirmationPlaylistResponse>,
                t: Throwable
            ) {
                Utils.dismissLoader(this@PractiseAffirmationPlaylistActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun updateWatchedAffirmationPlaylist() {
        Utils.showLoader(this)
        val authToken = sharedPreferenceManager.accessToken

        val watchAffirmationPlaylistRequest = WatchAffirmationPlaylistRequest()
        watchAffirmationPlaylistRequest.readAffirmation = binding.cardViewPager.currentItem + 1
        val durationArray = binding.tvTimer.text.toString().split(":")
        val duration = durationArray[0].toInt() * 60 + durationArray[1].toInt()
        watchAffirmationPlaylistRequest.duration = duration
        watchAffirmationPlaylistRequest.totalSession =
            watchedResponse?.data?.get(position)?.totalSession?.plus(
                1
            )

        val call =
            apiService.updateAffirmationPlaylistWatch(authToken, watchAffirmationPlaylistRequest)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Utils.dismissLoader(this@PractiseAffirmationPlaylistActivity)
                if (response.isSuccessful && response.body() != null) {
                    getWatchedAffirmationPlaylist(1)
                } else {
                    Toast.makeText(
                        this@PractiseAffirmationPlaylistActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Utils.dismissLoader(this@PractiseAffirmationPlaylistActivity)
                handleNoInternetView(t)
            }

        })
    }

    private fun callPostMindFullDataAPI() {
        val endDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        CommonAPICall.postMindFullData(this, "Affirmation", startDate, endDate)
    }

}