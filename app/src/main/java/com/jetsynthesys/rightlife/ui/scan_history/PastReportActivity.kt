package com.jetsynthesys.rightlife.ui.scan_history

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.databinding.ActivityPastReportBinding
import com.jetsynthesys.rightlife.ui.aireport.AIReportWebViewActivity
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditResultActivity
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PastReportActivity : BaseActivity() {

    private lateinit var binding: ActivityPastReportBinding
    private lateinit var adapter: PastReportAdapter
    private var isReportGenerated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPastReportBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        fetchPastReports()
        binding.llAiReport.setOnClickListener {
            var dynamicReportId = "" // This Is User ID
            dynamicReportId = SharedPreferenceManager.getInstance(applicationContext).userId
            if (dynamicReportId.isEmpty()) {
                // Some error handling if the ID is not available
            }else{
                val intent = Intent(this, AIReportWebViewActivity::class.java).apply {
                    // Put the dynamic ID as an extra
                    putExtra(AIReportWebViewActivity.EXTRA_REPORT_ID, dynamicReportId)
                }
                startActivity(intent)
            }
        }


        var isReportGenerated = sharedPreferenceManager.userProfile?.isReportGenerated
    /*    if (isReportGenerated == true) {
            binding.llAiReport.visibility = android.view.View.VISIBLE
        } else {
            binding.llAiReport.visibility = android.view.View.GONE
        }*/
    }

    private fun fetchPastReports() {

        apiService.getPastReports(sharedPreferenceManager.accessToken)
            .enqueue(object : Callback<ScanHistoryResponse> {
                override fun onResponse(
                    call: Call<ScanHistoryResponse>,
                    response: Response<ScanHistoryResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val reports = response.body()?.data ?: emptyList()
                        val groupedList = processApiResponse(reports)
                        adapter = PastReportAdapter(groupedList) { reportItem ->
                            when (reportItem.type) {
                                "FACIAL_SCAN" ->
                                    startActivity(
                                        Intent(
                                            this@PastReportActivity,
                                            NewHealthCamReportActivity::class.java
                                        ).apply {
                                            putExtra("REPORT_ID", reportItem._id)
                                        })

                                "MIND_AUDIT" ->
                                    startActivity(
                                        Intent(
                                            this@PastReportActivity,
                                            MindAuditResultActivity::class.java
                                        ).apply {
                                            putExtra("REPORT_ID", reportItem._id)
                                            putExtra("Assessment", reportItem.assessment)
                                        })

                                "AI_REPORT" ->
                                {
                                    val dynamicReportId = SharedPreferenceManager.getInstance(applicationContext).userId ?: ""

                                    if (dynamicReportId.isNotEmpty()) {
                                        startActivity(
                                            Intent(this@PastReportActivity, AIReportWebViewActivity::class.java)
                                                .putExtra(AIReportWebViewActivity.EXTRA_REPORT_ID, dynamicReportId)
                                        )
                                    } else {
                                        // Error handling if ID is empty
                                    }

                                }


                                else -> {
                                    //to do open Snap Meal Result
                                }

                            }

                        }
                        if (reports.isNotEmpty()) {
                            binding.recyclerView.visibility = android.view.View.VISIBLE
                            binding.recyclerView.adapter = adapter
                        }else{
                            binding.recyclerView.visibility = android.view.View.GONE
                            binding.noPastReports.visibility = android.view.View.VISIBLE
                        }

                    } else {
                        Toast.makeText(
                            this@PastReportActivity,
                            "Server Error: " + response.code(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ScanHistoryResponse>, t: Throwable) {
                    handleNoInternetView(t)
                }
            })
    }

    fun processApiResponse(response: List<ReportItem>): List<Any> {
        val groupedData = mutableListOf<Any>()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val displayDateFormat = DateTimeFormatter.ofPattern("d MMM, yyyy")

        val groupedReports = response.groupBy {
            when (val reportDate = LocalDate.parse(it.date, dateFormat)) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> reportDate.format(displayDateFormat)
            }
        }

        for ((date, reports) in groupedReports) {
            groupedData.add(date)  // Add Date Header
            groupedData.addAll(reports)  // Add Reports under it
        }

        return groupedData
    }


}
