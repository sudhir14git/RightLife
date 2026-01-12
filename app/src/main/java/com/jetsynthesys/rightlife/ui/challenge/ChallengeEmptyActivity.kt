package com.jetsynthesys.rightlife.ui.challenge

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityEmptyChallengeBinding
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showChallengeInfoBottomSheet
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showScoreInfoBottomSheet
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showTaskInfoBottomSheet
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.getDaysFromToday

class ChallengeEmptyActivity : BaseActivity() {

    private lateinit var binding: ActivityEmptyChallengeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmptyChallengeBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        val challengeStartDate = intent.getStringExtra("CHALLENGE_START_DATE")

        binding.btnBack.setOnClickListener {
            finish()
        }
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        binding.layoutChallengeCountDownDays.tvCountDownDays.text =
            getDaysFromToday(challengeStartDate ?: "01 Feb 2026, 09:00 AM").toString()

        binding.layoutChallengeCountDownDays.imgInfoChallege.setOnClickListener {
            showChallengeInfoBottomSheet(this@ChallengeEmptyActivity)
        }

        binding.scoreCard.imgInfo.setOnClickListener {
            showScoreInfoBottomSheet(this@ChallengeEmptyActivity)
        }

        binding.sharingCard.apply {
            btnReferNow.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Been using this app called RightLife that tracks food, workouts, sleep, and mood. Super simple, no wearable needed. Try it and get 7 days for free without any credit card details.\n" +
                                "Here’s the link:\n" +
                                "https://onelink.to/rightlife"
                    )
                }

                startActivity(Intent.createChooser(shareIntent, "Refer via"))
            }
        }

        binding.challengeTasks.apply {
            rlAboutChallenge.setOnClickListener {
                showChallengeInfoBottomSheet(this@ChallengeEmptyActivity)
            }
            rlWhyTaskComplete.setOnClickListener {
                showTaskInfoBottomSheet(this@ChallengeEmptyActivity)
            }
            imgQuestionChallenge.setOnClickListener {
                showTaskInfoBottomSheet(this@ChallengeEmptyActivity)
            }
        }

    }
}