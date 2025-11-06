package com.jetsynthesys.rightlife.ui.aireport

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.BuildConfig
import com.jetsynthesys.rightlife.ai_package.ui.eatright.RatingMealBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.RatingReportFeedbackBottomSheet
import com.jetsynthesys.rightlife.databinding.ActivityAireportWebViewBinding
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

class AIReportWebViewActivity : BaseActivity(), RatingReportFeedbackBottomSheet.RatingReportFeedbackListener {

    private lateinit var binding: ActivityAireportWebViewBinding
    private var reportPageStartTime: Long = 0

    companion object {
        const val EXTRA_REPORT_ID = "extra_report_id"
        const val BASE_URL = BuildConfig.BASE_URL_AI + "pdf/view-report/"//"https://ai-qa.rightlife.com/pdf/view-report/"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAireportWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            if (SharedPreferenceManager.getInstance(this).userProfile?.isReportGenerated == true && SharedPreferenceManager.getInstance(
                    this
                ).userProfile?.reportView == false
            ) {

                ratingReportFeedbackDialog(true)
            }else{
                onBackPressedDispatcher.onBackPressed()
            }
        }

        val reportId = intent.getStringExtra(EXTRA_REPORT_ID)
        if (reportId.isNullOrEmpty()) {
            Toast.makeText(this, "Report ID not provided.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val fullUrl = "$BASE_URL$reportId?isReportView=True"
        val webView = binding.webView
        val webSettings = webView.settings

        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = android.view.View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = android.view.View.GONE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url ?: "")
                return true
            }

            override fun onReceivedError(
                view: WebView?, errorCode: Int, description: String?, failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(
                    this@AIReportWebViewActivity,
                    "Error loading page: $description",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        webView.loadUrl(fullUrl)
        SharedPreferenceManager.getInstance(applicationContext).setAIReportGeneratedView(true)


        AnalyticsLogger.logEvent(
            this,
            AnalyticsEvent.RL_AI_Report_PageOpen,
            mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis(), )
        )
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
            logReportPageDuration()
        }
    }

    private fun logReportPageDuration() {
        val endTime = System.currentTimeMillis()
        val totalDuration = endTime - reportPageStartTime

        // Only log if user spent meaningful time (more than 1 second) and page was loaded
        if (totalDuration > 1000) {
            AnalyticsLogger.logEvent(
                this,
                AnalyticsEvent.TOTAL_REPORT_PAGE_DURATION,
                mapOf(
                    AnalyticsParam.START_TIME to "" + reportPageStartTime,
                    AnalyticsParam.END_TIME to "" + endTime,
                    AnalyticsParam.TOTAL_DURATION to totalDuration,
                )
            )
        }
    }

    private fun ratingReportFeedbackDialog(isSave: Boolean) {
        val bottomSheet = RatingReportFeedbackBottomSheet().apply {
            isCancelable = true
            arguments = Bundle().apply {
                putBoolean("isSave", isSave)
            }
        }
        bottomSheet.show(supportFragmentManager, "RatingReportFeedbackBottomSheet")
    }

    override fun onReportFeedbackRating(rating: Double, isSave: Boolean) {
        onBackPressed()
        AnalyticsLogger.logEvent(
            this,
            AnalyticsEvent.RL_AI_Report_Rating_Submit,
            mapOf(
                AnalyticsParam.RATING to rating,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
            )
        )
    }
}