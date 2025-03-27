package com.example.rlapp.ui.scan_history

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.RetrofitData.ApiClient
import com.example.rlapp.RetrofitData.ApiService
import com.example.rlapp.databinding.ActivityPastReportBinding
import com.example.rlapp.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PastReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPastReportBinding
    private lateinit var adapter: PastReportAdapter
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPastReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        fetchPastReports()
    }

    private fun fetchPastReports() {

        val apiService = ApiClient.getClient().create(ApiService::class.java)

        apiService.getPastReports(sharedPreferenceManager.accessToken)
            .enqueue(object : Callback<ScanHistoryResponse> {
                override fun onResponse(
                    call: Call<ScanHistoryResponse>,
                    response: Response<ScanHistoryResponse>
                ) {
                    if (response.isSuccessful) {
                        val reports = response.body()?.data ?: emptyList()
                        val groupedList = processApiResponse(reports)
                        adapter = PastReportAdapter(groupedList) { reportItem ->
                            Toast.makeText(
                                this@PastReportActivity,
                                "clicked " + reportItem.title,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        binding.recyclerView.adapter = adapter
                    }
                }

                override fun onFailure(call: Call<ScanHistoryResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Failed to fetch reports", t)
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
