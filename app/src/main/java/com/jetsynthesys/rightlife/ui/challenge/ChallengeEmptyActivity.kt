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
                        "Saw this on RightLife and thought of you — it’s got health tips that actually make sense.\n\n" +
                                "👉 Play Store: https://play.google.com/store/apps/details?id=$packageName\n" +
                                "👉 App Store: https://apps.apple.com/app/rightlife/id6444228850"
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