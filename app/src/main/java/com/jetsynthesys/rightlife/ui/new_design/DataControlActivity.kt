package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.drawermenu.PrivacyPolicyActivity
import com.jetsynthesys.rightlife.ui.drawermenu.TermsAndConditionsActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam

class DataControlActivity : BaseActivity() {
    private val agreeString =
        "<html>I agree to the use of my personal data to enable RightLife features. Learn more in our <a href=\"https://example.com/privacy\">Privacy Policy</a>.</html>"
    private val policyTermsCondition =
        "I agree to the <a href=\"https://example.com/privacy\">Privacy Policy</a> and <a href=\"https://example.com/terms\">Terms and Conditions</a>"


    // Declare the TextView as a property
    private lateinit var tvTermsAndConditions: TextView
    private lateinit var tvPrivacyPolicy: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_data_control)
        val chkPrivacyPolicy: CheckBox = findViewById(R.id.ck_privacy_policy)
        val chkTermsAndConditions: CheckBox = findViewById(R.id.ck_terms_conditions)

        AnalyticsLogger.logEvent(
            AnalyticsEvent.DATA_CONTROL_SCREEN_VISIT,
            mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
        )

        tvPrivacyPolicy = findViewById(R.id.tv_privacy_policy)
        tvTermsAndConditions = findViewById(R.id.tv_terms_condition)

        val btnContinue = findViewById<Button>(R.id.btn_continue)

        tvTermsAndConditions.text =
            Html.fromHtml(policyTermsCondition, Html.FROM_HTML_MODE_COMPACT)
        tvPrivacyPolicy.text =
            Html.fromHtml(agreeString, Html.FROM_HTML_MODE_COMPACT)

        val colorStateListEnabled = ContextCompat.getColorStateList(this, R.color.menuselected)
        val colorStateListDisabled = ContextCompat.getColorStateList(this, R.color.rightlife)


        tvPrivacyPolicy.setOnClickListener {
            //startActivity(Intent(this, PrivacyPolicyActivity::class.java))

        }

        chkPrivacyPolicy.setOnClickListener {
            btnContinue.isEnabled = chkPrivacyPolicy.isChecked && chkTermsAndConditions.isChecked
            btnContinue.backgroundTintList =
                if (btnContinue.isEnabled) colorStateListEnabled else colorStateListDisabled
        }

        chkTermsAndConditions.setOnClickListener {
            btnContinue.isEnabled = chkPrivacyPolicy.isChecked && chkTermsAndConditions.isChecked
            btnContinue.backgroundTintList =
                if (btnContinue.isEnabled) colorStateListEnabled else colorStateListDisabled
        }

        btnContinue.setOnClickListener {
            if (chkPrivacyPolicy.isChecked && chkTermsAndConditions.isChecked) {
                startActivity(Intent(this, ImageSliderActivity::class.java))
                finish()
            }
        }

        // -------- second checkbox text
        TncClickMethod()
        // First checkbox text
        IagreeClickMethod()
    }

    private fun TncClickMethod() {

        val spannedText = Html.fromHtml(policyTermsCondition, Html.FROM_HTML_MODE_COMPACT)

        // Convert Spanned to SpannableStringBuilder for modification
        val spannableBuilder = SpannableStringBuilder(spannedText)

        // Get all URL spans from the text
        val urlSpans = spannableBuilder.getSpans(0, spannableBuilder.length, URLSpan::class.java)

        // Replace each URLSpan with a custom ClickableSpan
        for (urlSpan in urlSpans) {
            val start = spannableBuilder.getSpanStart(urlSpan)
            val end = spannableBuilder.getSpanEnd(urlSpan)
            val url = urlSpan.url

            // Remove the existing URLSpan
            spannableBuilder.removeSpan(urlSpan)

            // Add a custom ClickableSpan
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Handle the click based on the URL
                    when (url) {
                        "https://example.com/privacy" -> {
                            // Handle Privacy Policy click
                            // For example, navigate to another screen or show a dialog
                            //Toast.makeText(this@DataControlActivity,"Privacy Policy",Toast.LENGTH_SHORT).show()
                            val intent = Intent(
                                this@DataControlActivity,
                                PrivacyPolicyActivity::class.java
                            )
                            startActivity(intent)
                        }

                        "https://example.com/terms" -> {
                            // Handle Terms and Conditions click
                            //Toast.makeText(this@DataControlActivity,"Terms Policy",Toast.LENGTH_SHORT).show()
                            val intent = Intent(
                                this@DataControlActivity,
                                TermsAndConditionsActivity::class.java
                            )
                            startActivity(intent)
                        }
                    }
                }
            }
            spannableBuilder.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Set the modified text to the TextView
        tvTermsAndConditions.text = spannableBuilder
        tvTermsAndConditions.movementMethod = LinkMovementMethod.getInstance() // Enable link clicks


    }


    private fun IagreeClickMethod() {

        val spannedText = Html.fromHtml(agreeString, Html.FROM_HTML_MODE_COMPACT)

        // Convert Spanned to SpannableStringBuilder for modification
        val spannableBuilder = SpannableStringBuilder(spannedText)

        // Get all URL spans from the text
        val urlSpans = spannableBuilder.getSpans(0, spannableBuilder.length, URLSpan::class.java)

        // Replace each URLSpan with a custom ClickableSpan
        for (urlSpan in urlSpans) {
            val start = spannableBuilder.getSpanStart(urlSpan)
            val end = spannableBuilder.getSpanEnd(urlSpan)
            val url = urlSpan.url

            // Remove the existing URLSpan
            spannableBuilder.removeSpan(urlSpan)

            // Add a custom ClickableSpan
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Handle the click based on the URL
                    when (url) {
                        "https://example.com/privacy" -> {
                            // Handle Privacy Policy click
                            // For example, navigate to another screen or show a dialog
                            //Toast.makeText(this@DataControlActivity,"Privacy Policy",Toast.LENGTH_SHORT).show()
                            val intent = Intent(
                                this@DataControlActivity,
                                PrivacyPolicyActivity::class.java
                            )
                            startActivity(intent)
                            AnalyticsLogger.logEvent(
                                AnalyticsEvent.Onboarding_PrivacyPolicyPage_Open,
                                mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                                    AnalyticsParam.PLATFORM_TYPE to "Android",
                                    AnalyticsParam.DEVICE_NAME to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim())
                            )
                        }

                        "https://example.com/terms" -> {
                            // Handle Terms and Conditions click
                            // Toast.makeText(this@DataControlActivity,"Terms Policy",Toast.LENGTH_SHORT).show()
                            val intent = Intent(
                                this@DataControlActivity,
                                TermsAndConditionsActivity::class.java
                            )
                            startActivity(intent)
                        }
                    }
                }
            }
            spannableBuilder.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Set the modified text to the TextView
        tvPrivacyPolicy.text = spannableBuilder
        tvPrivacyPolicy.movementMethod = LinkMovementMethod.getInstance() // Enable link clicks


    }

}