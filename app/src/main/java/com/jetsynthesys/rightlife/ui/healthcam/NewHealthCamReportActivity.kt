package com.jetsynthesys.rightlife.ui.healthcam

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.newreportfacescan.FacialReportResponseNew
import com.jetsynthesys.rightlife.apimodel.newreportfacescan.HealthCamItem
import com.jetsynthesys.rightlife.databinding.ActivityNewhealthcamreportBinding
import com.jetsynthesys.rightlife.databinding.ItemScanCircleBinding
import com.jetsynthesys.rightlife.databinding.LayoutScanProgressBinding
import com.jetsynthesys.rightlife.newdashboard.FacialScanReportDetailsActivity
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity
import com.jetsynthesys.rightlife.ui.CommonAPICall.updateChecklistStatus
import com.jetsynthesys.rightlife.ui.healthcam.basicdetails.HealthCamBasicDetailsNewActivity
import com.jetsynthesys.rightlife.ui.settings.SubscriptionHistoryActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.ConversionUtils
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class NewHealthCamReportActivity : BaseActivity() {
    var binding: ActivityNewhealthcamreportBinding? = null
    private var scanBinding: LayoutScanProgressBinding? = null
    private var allHealthCamItems: MutableList<HealthCamItem> = ArrayList()
    private var facialReportResponseNew: FacialReportResponseNew? = null
    private var reportId: String? = null
    private var isFrom: String? = null
    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_newhealthcamreport)
        binding = ActivityNewhealthcamreportBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        scanBinding = LayoutScanProgressBinding.bind(binding!!.scanProgressLayout.root)
        reportId = intent.getStringExtra("REPORT_ID")
        isFrom = intent.getStringExtra("FROM")

        // Register the result launcher
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    //val returnedValue = data.getBooleanExtra("result", false)
                    myRLHealthCamResult
                }
            }
        }

        findViewById<View>(R.id.ic_back_dialog).setOnClickListener { onBackPressHandle() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressHandle()
            }
        })


        binding!!.icCloseDialog.setOnClickListener { showDisclaimerDialog() }
        binding!!.cardviewLastCheckin.setOnClickListener {
            val intent = Intent(this, SubscriptionPlanListActivity::class.java)
            intent.putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
            resultLauncher!!.launch(intent)
        }

        binding!!.cardviewRightlifedays.setOnClickListener { startNextOverAllWellnessDetails() }

        binding!!.imageNext.setOnClickListener { startNextOverAllWellnessDetails() }

        binding!!.btnBuyFacescan.setOnClickListener {
            // put check here if face scan remaining count is 0 buy new else Scan again
            if (facialReportResponseNew!!.data.boosterLimit > 0 && facialReportResponseNew!!.data.boosterUsed < facialReportResponseNew!!.data.boosterLimit) {
                startActivity(
                    Intent(
                        this@NewHealthCamReportActivity,
                        HealthCamBasicDetailsNewActivity::class.java
                    )
                )
            } else {
                val intent = Intent(this, SubscriptionPlanListActivity::class.java)
                intent.putExtra("SUBSCRIPTION_TYPE", "FACIAL_SCAN")
                resultLauncher!!.launch(intent)
            }
        }
        binding!!.scanProgressLayout.btnScanAgain.setOnClickListener {
            startActivity(
                Intent(
                    this@NewHealthCamReportActivity, HealthCamBasicDetailsNewActivity::class.java
                )
            )
        }
        myRLHealthCamResult

        updateChecklistStatus()

        var productId = ""
        sharedPreferenceManager.userProfile.subscription.forEach { subscription ->
            if (subscription.status) {
                productId = subscription.productId
            }
        }

        AnalyticsLogger.logEvent(
            AnalyticsEvent.FACE_SCAN_COMPLETE,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.FACE_SCAN_COMPLETE to true,
                AnalyticsParam.USER_TYPE to if (sharedPreferenceManager.userProfile.isSubscribed) "Paid User" else "free User",
                AnalyticsParam.USER_PLAN to productId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
            )
        )

        binding!!.btnSyncNow.setOnClickListener {
            downloadReport(
                facialReportResponseNew!!.data.pdf
            )
        }
    }

    private fun startNextOverAllWellnessDetails() {
        val unifiedList = unifiedParameterList
        val intent = Intent(this, FacialScanReportDetailsActivity::class.java)
        val healthCamItems: List<HealthCamItem> = ArrayList()
        intent.putExtra("healthCamItemList", ArrayList(healthCamItems)) // Serializable list
        intent.putExtra("UNIFIED_LIST", ArrayList(unifiedList)) // Serializable list
        intent.putExtra("position", 0)
        startActivity(intent)
    }

    private val unifiedParameterList: ArrayList<ParameterModel>
        get() {
            val resultList = ArrayList<ParameterModel>()
            if (facialReportResponseNew!!.data.overallWellnessScore != null) {
                val key = facialReportResponseNew!!.data.overallWellnessScore.fieldName
                val name = facialReportResponseNew!!.data.overallWellnessScore.parameter
                resultList.add(ParameterModel(key, name))
            }
            return resultList
        }

    private fun onBackPressHandle() {
        finish()
        if (isFrom != null && isFrom!!.isNotEmpty()) {
            val intent = Intent(this, HomeNewActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra("finish_healthCam", true)
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        myRLHealthCamResult
    }

    private fun updateChecklistStatus() {
        updateChecklistStatus(this, "vital_facial_scan", AppConstants.CHECKLIST_COMPLETED, null)
    }

    private val myRLHealthCamResult: Unit
        get() {
            Utils.showLoader(this)
            val call =
                if (reportId != null && reportId!!.isNotEmpty()) apiService.getHealthCamByReportId(
                    sharedPreferenceManager.accessToken,
                    reportId
                )
                else apiService.getMyRLHealthCamResult(sharedPreferenceManager.accessToken)

            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    Utils.dismissLoader(this@NewHealthCamReportActivity)
                    if (response.isSuccessful && response.body() != null) {
                        //Toast.makeText(NewHealthCamReportActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                        try {
                            val jsonString = response.body()!!.string()
                            val gson = Gson()
                            facialReportResponseNew =
                                gson.fromJson(jsonString, FacialReportResponseNew::class.java)
                            handleNewReportUI(facialReportResponseNew)
                            //  HandleContinueWatchUI(facialReportResponseNew);
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        }
                    } else {
                        //   Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.d("MyRLHealthCamResult", "Error:" + response.message())
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Utils.dismissLoader(this@NewHealthCamReportActivity)
                    handleNoInternetView(t)
                }
            })
        }

    private fun handleNewReportUI(facialReportResponseNew: FacialReportResponseNew?) {
        if (facialReportResponseNew!!.success) {
            if (facialReportResponseNew.data.overallWellnessScore != null) {
                val formatted = ConversionUtils.decimalFormat0Decimal.format(
                    facialReportResponseNew.data.overallWellnessScore.value
                )
                Log.d("FormattedValue", formatted)
                binding!!.txtWellnessScore1.text = formatted
                setTextAccordingToWellnessScore(facialReportResponseNew.data.overallWellnessScore.value)
                binding!!.halfCurveProgressBar.setProgress(
                    facialReportResponseNew.data.overallWellnessScore.value.toFloat(),
                    Color.parseColor("#" + facialReportResponseNew.data.overallWellnessScore.colour)
                )
            }

            binding!!.txtAlertMessage.text = facialReportResponseNew.data.summary
            binding!!.tvLastReportDate.text = DateTimeUtils.convertAPIDateMonthFormatWithTime(
                facialReportResponseNew.data.createdAt
            )
            allHealthCamItems.clear()
            if (facialReportResponseNew.data.healthCamReportByCategory != null) {
                if (facialReportResponseNew.data.healthCamReportByCategory.healthCamGood != null) {
                    val healthCamGoodItems =
                        facialReportResponseNew.data.healthCamReportByCategory.healthCamGood
                    allHealthCamItems.addAll(healthCamGoodItems)
                }
                if (facialReportResponseNew.data.healthCamReportByCategory.healthCamPayAttention != null) {
                    val healthCamPayAttentionItems =
                        facialReportResponseNew.data.healthCamReportByCategory.healthCamPayAttention
                    allHealthCamItems.addAll(healthCamPayAttentionItems)
                }


                // Combine the lists if you want to display them together
                val adapter = HealthCamVitalsAdapter(this, allHealthCamItems)

                binding!!.recyclerViewVitalCards.layoutManager = GridLayoutManager(
                    this, 2
                ) // 2 columns
                binding!!.recyclerViewVitalCards.adapter = adapter
                if (facialReportResponseNew.data.limit > 0) {  //if (facialReportResponseNew.data.usedCount > 0 && facialReportResponseNew.data.limit > 0) {
                    setupScanTracker(
                        scanBinding,
                        facialReportResponseNew.data.usedCount,
                        facialReportResponseNew.data.limit
                    )
                    binding!!.cardFacescanBooster.visibility = View.VISIBLE
                    binding!!.scanProgressLayout.scanContainer.visibility = View.VISIBLE
                    binding!!.cardviewLastCheckin.visibility = View.GONE
                } else {
                    binding!!.cardviewLastCheckin.visibility = View.VISIBLE
                    binding!!.cardFacescanBooster.visibility = View.VISIBLE
                    binding!!.scanProgressLayout.scanContainer.visibility = View.GONE
                }
                setupBoosterTracker(
                    facialReportResponseNew.data.boosterUsed,
                    facialReportResponseNew.data.boosterLimit
                )
            }
        }
    }

    private fun setTextAccordingToWellnessScore(wellnessScore: Double) {
        val message = if (wellnessScore in 0.0..19.99) {
            "Your wellness needs urgent attention—let’s work on getting you back on track."
        } else if (wellnessScore <= 40.0) {
            "Your wellness is under strain—time to make a few adjustments for improvement."
        } else if (wellnessScore <= 60.0) {
            "You’re doing okay, but there’s room to optimize your health and resilience."
        } else if (wellnessScore <= 80.0) {
            "Great job! Your vitals are strong, and you’re building a solid foundation."
        } else if (wellnessScore <= 100.0) {
            "You're thriving! Your vitals are at their peak—keep up the fantastic work."
        } else {
            "Invalid wellness score."
        }

        binding!!.txtWellStreak.text = message
    }

    /*private void HandleContinueWatchUI(FacialReportResponseNew facialReportResponseNew) {
        if (!facialReportResponseNew.data.recommendation.isEmpty()) {
            HealthCamRecommendationAdapter adapter = new HealthCamRecommendationAdapter(this, facialReportResponseNew.data.recommendation);
            LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.recyclerViewContinue.setLayoutManager(horizontalLayoutManager);
            binding.recyclerViewContinue.setAdapter(adapter);
        }
    }*/
    private fun showDisclaimerDialog() {
        // Create the dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_disclaimer_facescan_result)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window!!.attributes
        layoutParams.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window.attributes = layoutParams

        // Find views from the dialog layout
        //ImageView dialogIcon = dialog.findViewById(R.id.img_close_dialog);
        //val dialogImage = dialog.findViewById<ImageView>(R.id.dialog_image)
        //val dialogText = dialog.findViewById<TextView>(R.id.dialog_text)
        val dialogButtonStay = dialog.findViewById<Button>(R.id.dialog_button_stay)
        //val dialogButtonExit = dialog.findViewById<Button>(R.id.dialog_button_exit)

        // Optional: Set dynamic content
        // dialogText.setText("Please find a quiet and comfortable place before starting");

        // Set button click listener
        dialogButtonStay.setOnClickListener {
            // Perform your action
            dialog.dismiss()
        }
        // Show the dialog
        dialog.show()
    }


    // new Booster ui
    @SuppressLint("SetTextI18n")
    private fun setupBoosterTracker(boosterUsed: Int, boosterLimit: Int) {
        if ((boosterLimit - boosterUsed) == 0) {
            //binding.txtBoosterCount.setText("0");
            binding!!.txtBoosterCount.visibility = View.GONE
            binding!!.txtRemainingMsg.text = "Need More Scans? \nBuy booster packs anytime."
            binding!!.viewDivider.visibility = View.GONE
        } else if (boosterLimit > 0 && boosterUsed < boosterLimit) {
            binding!!.btnBuyFacescan.text = "Scan Again"
            binding!!.txtBoosterCount.text = (boosterLimit - boosterUsed).toString()
            binding!!.viewDivider.visibility = View.VISIBLE
            binding!!.txtRemainingMsg.text = "Your scans remaining."
        }
    }

    // new scan ui
    @SuppressLint("SetTextI18n")
    private fun setupScanTracker(layout: LayoutScanProgressBinding?, usedCount: Int, limit: Int) {
        layout!!.scanIndicators.removeAllViews()

        val inflater = LayoutInflater.from(layout.root.context)

        for (i in 1..limit) {
            val itemBinding = ItemScanCircleBinding.inflate(inflater)

            itemBinding.circleNumber.text = i.toString()

            if (i <= usedCount) {
                itemBinding.circleImage.setImageResource(R.drawable.ic_checklist_complete)
                itemBinding.circleNumber.setTextColor(
                    ContextCompat.getColor(
                        layout.root.context,
                        android.R.color.black
                    )
                )
                itemBinding.txtGreenDot.visibility = View.VISIBLE
            } else {
                itemBinding.circleImage.setImageResource(R.drawable.ic_checklist_tick_bg)
                itemBinding.circleNumber.setTextColor(
                    ContextCompat.getColor(
                        layout.root.context,
                        android.R.color.black
                    )
                )
                itemBinding.txtGreenDot.visibility = View.INVISIBLE
            }

            layout.scanIndicators.addView(itemBinding.root)
        }
        if (usedCount == 1) {
            layout.infoText.text = "One scan a week is all you need to stay on top of your vitals."
        } else {
            layout.infoText.text = "One scan a week is all you need to stay on top of your vitals."
            // layout.infoText.setText("One step closer to better health! Your scans refresh monthly—stay on track!");
        }
        if (usedCount == limit) {
            layout.buttonText.text = "Scan Again"
            layout.btnScanAgain.visibility = View.GONE
            layout.infoText.text =
                "One step closer to better health! Your scans refresh monthly—stay on track!"
            binding!!.cardFacescanBooster.visibility = View.VISIBLE
        } else {
            layout.buttonText.text = "Scan Again"
            layout.btnScanAgain.visibility = View.VISIBLE
        }
        layout.btnScanAgain.setOnClickListener {
            if (layout.buttonText.text.toString().equals("Scan Again @ 99", ignoreCase = true)) {
                startActivity(
                    Intent(
                        this@NewHealthCamReportActivity,
                        SubscriptionHistoryActivity::class.java
                    )
                )
            } else {
                startActivity(
                    Intent(
                        this@NewHealthCamReportActivity,
                        HealthCamBasicDetailsNewActivity::class.java
                    )
                )
            }
        }
    }

    private fun downloadReport(pdf: String) {

        val request = DownloadManager.Request(Uri.parse(pdf))
        request.setTitle("Downloading PDF")
        request.setDescription("Downloading file, please wait...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "sample.pdf")

        // Enqueue download
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(this, "Download Started...", Toast.LENGTH_SHORT).show()
    }
}
