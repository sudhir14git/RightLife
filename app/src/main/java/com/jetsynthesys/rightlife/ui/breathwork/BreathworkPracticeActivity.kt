package com.jetsynthesys.rightlife.ui.breathwork


import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityBreathworkPracticeBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetBreathworkCompleteBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetEarlyFinishBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.breathwork.pojo.BreathingData
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import java.time.Instant
import java.time.format.DateTimeFormatter

class BreathworkPracticeActivity : BaseActivity() {

    private lateinit var binding: ActivityBreathworkPracticeBinding

    private var totalSets = 3
    private var currentSet = 1
    private var sessionDurationSeconds = 135 // Default session duration
    private var countDownTimer: CountDownTimer? = null
    private var breathingData: BreathingData? = null
    private var inhaleTime: Long = 0
    private var exhaleTime: Long = 0
    private var holdTime: Long = 0
    private var startDate = ""
    private var startTime: Long = 0
    private var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathworkPracticeBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        startTime = System.currentTimeMillis()

        // Retrieve the selected breathing practice from the intent
        breathingData = intent.getSerializableExtra("BREATHWORK") as BreathingData
        startDate = intent.getStringExtra("StartDate").toString()
        if (startDate.isEmpty())
            startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

        val sessionCount = intent.getIntExtra("sessionCount", 3)
        totalSets = sessionCount
        inhaleTime = breathingData?.breathInhaleTime?.toLong()!! * 1000
        exhaleTime = breathingData?.breathExhaleTime?.toLong()!! * 1000
        holdTime = breathingData?.breathHoldTime?.toLong()!! * 1000

        // Calculate session duration based on the selected practice
        val cycleDuration = inhaleTime +
                holdTime +
                exhaleTime +
                (if (breathingData?.title == "Box Breathing") holdTime else 0L)
        sessionDurationSeconds = (totalSets * cycleDuration / 1000).toInt()

        // Set initial values
        binding.setIndicator.text = "Set $currentSet/$totalSets"
        updateSessionTimer(sessionDurationSeconds * 1000L)

        // Set click listeners
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.finishEarlyButton.setOnClickListener {
            showDeleteBottomSheet()
        }

        // Start the breathing session
        startSessionTimer()
        startBreathingCycle()
    }

    private fun startSessionTimer() {
        object : CountDownTimer(sessionDurationSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateSessionTimer(millisUntilFinished)
            }

            override fun onFinish() {
                binding.sessionTimer.text = "00:00"
            }
        }.start()
    }

    private fun updateSessionTimer(millisUntilFinished: Long) {
        val minutes = (millisUntilFinished / 1000) / 60
        val seconds = (millisUntilFinished / 1000) % 60
        binding.sessionTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun startBreathingCycle() {
        if (currentSet <= totalSets) {
            AnalyticsLogger.logEvent(
                AnalyticsEvent.BREATHING_SESSION_STARTED, mapOf(
                    AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                    AnalyticsParam.USER_TYPE to if (sharedPreferenceManager.userProfile.isSubscribed) "Paid User" else "free User",
                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                    AnalyticsParam.BREATHING_TYPE_ID to breathingData?.id!!,
                    AnalyticsParam.BREATHING_TYPE_NAME to "",
                    AnalyticsParam.BREATHING_SESSION_DURATION to breathingData?.duration!!
                )
            )
            binding.setIndicator.text = "Set $currentSet/$totalSets"
            startBreathIn()
        } else {
            //finish()
            CommonAPICall.postWellnessStreak(this, "breathing")
            showCompletedBottomSheet()
            AnalyticsLogger.logEvent(
                AnalyticsEvent.BREATHING_SESSION_COMPLETED, mapOf(
                    AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                    AnalyticsParam.USER_TYPE to if (sharedPreferenceManager.userProfile.isSubscribed) "Paid User" else "free User",
                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                    AnalyticsParam.BREATHING_TYPE_ID to breathingData?.id!!,
                    AnalyticsParam.BREATHING_TYPE_NAME to "",
                    AnalyticsParam.BREATHING_SESSION_DURATION to System.currentTimeMillis() - startTime
                )
            )
        }
    }

    private fun startBreathIn() {
        binding.breathingPhase.text = "Breath In"
        animateCircle(1f, 1.5f, inhaleTime)
        startCountdown(inhaleTime) {
            if (holdTime > 0) startHold() else startBreathOut()
        }
    }

    private fun startHold() {
        binding.breathingPhase.text = "Hold"
        animateCircle(1.5f, 1.5f, holdTime)
        startCountdown(holdTime) { startBreathOut() }
    }

    private fun startBreathOut() {
        binding.breathingPhase.text = "Breath Out"
        animateCircle(1.5f, 1f, exhaleTime)
        startCountdown(exhaleTime) {
            if (breathingData?.title == "Box Breathing") {
                startFinalHold()
            } else {
                currentSet++
                startBreathingCycle()
            }
        }
    }

    private fun startFinalHold() {
        binding.breathingPhase.text = "Hold"
        animateCircle(1f, 1f, holdTime)
        startCountdown(holdTime) {
            currentSet++
            startBreathingCycle()
        }
    }

    private fun startCountdown(duration: Long, onFinish: () -> Unit) {
        var secondsLeft = (duration / 1000).toInt()
        binding.circleTimer.text = secondsLeft.toString()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft--
                binding.circleTimer.text = secondsLeft.toString()
            }

            override fun onFinish() {
                onFinish()
            }
        }.start()
    }

    private fun animateCircle(fromScale: Float, toScale: Float, duration: Long) {
        val scaleAnimation = ScaleAnimation(
            fromScale, toScale, fromScale, toScale,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = duration
        scaleAnimation.fillAfter = true
        binding.circleView.startAnimation(scaleAnimation)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    /*show erly finish bottomsheet*/
    private fun showDeleteBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetEarlyFinishBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)


        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.tvTitle.text = "Leaving early?"
        dialogBinding.tvDescription.text =
            "A few more minutes of breathing practise will make a world of difference."

        dialogBinding.btnCancel.text = "Continue Practise"
        dialogBinding.btnYes.text = "Leave"

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnYes.setOnClickListener {
            //deleteJournal(journalEntry)
            bottomSheetDialog.dismiss()
            callPostMindFullDataAPI()
            finish()
        }
        bottomSheetDialog.show()
    }


    private fun showCompletedBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetBreathworkCompleteBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)


        // Set up the animation
        val bottomSheetLayout =
            bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet2)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.tvTitle.text = "How do you feel after the exercise?"
        dialogBinding.tvDescription.text =
            "A few more minutes of breathing practise will make a world of difference."

        dialogBinding.btnBetter.text = "better than before"
        dialogBinding.btnSame.text = "same as before"
        dialogBinding.btnWorse.text = "worse than before"

        bottomSheetDialog.setOnDismissListener {
            callPostMindFullDataAPI()
            finish()
        }

        bottomSheetDialog.setOnCancelListener {
            callPostMindFullDataAPI()
            finish()
        }

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnBetter.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnSame.setOnClickListener {
            //deleteJournal(journalEntry)
            bottomSheetDialog.dismiss()
        }
        dialogBinding.btnWorse.setOnClickListener {
            //deleteJournal(journalEntry)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun callPostMindFullDataAPI() {
        val endDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        CommonAPICall.postMindFullData(
            this,
            breathingData?.title ?: "Breathing",
            startDate,
            endDate
        )
    }
}
