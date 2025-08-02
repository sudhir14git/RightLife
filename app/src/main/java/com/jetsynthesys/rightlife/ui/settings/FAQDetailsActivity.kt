package com.jetsynthesys.rightlife.ui.settings

import android.os.Bundle
import android.text.Html
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityFaqDetailsBinding
import com.jetsynthesys.rightlife.ui.settings.pojo.FAQDetails
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam

class FAQDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityFaqDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqDetailsBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        //back button
        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.writeToUsText.text = Html.fromHtml(
            "Didn’t find what you need? <br><b>Here’s how to get in touch:</b>",
            Html.FROM_HTML_MODE_COMPACT
        )

        val faqDetails: FAQDetails? = intent.getSerializableExtra("FAQDetails") as? FAQDetails

        if (faqDetails != null) {
            binding.tvQuestion.text = faqDetails.question
            binding.tvAnswer.text =
                Html.fromHtml(faqDetails.answer, Html.FROM_HTML_MODE_COMPACT)
        }

        binding.writeToUsButton.setOnClickListener {
            WriteToUsUtils.sendEmail(this)
            AnalyticsLogger.logEvent(
                AnalyticsEvent.USER_FEEDBACK_CLICK,
                mapOf(
                    AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                    AnalyticsParam.USER_TYPE to if (sharedPreferenceManager.userProfile.isSubscribed) "Paid User" else "free User",
                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                )
            )
        }
    }
}