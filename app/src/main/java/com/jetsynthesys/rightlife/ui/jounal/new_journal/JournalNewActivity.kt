package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityJournalNewBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.format.DateTimeFormatter

class JournalNewActivity : BaseActivity() {

    private lateinit var binding: ActivityJournalNewBinding
    private lateinit var adapter: JournalAdapter
    private var isFromTool = false
    private var whereToGo = ""
    //private var startDate = ""
    private var isFromThinkRight = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJournalNewBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        isFromTool = intent.getBooleanExtra("IS_FROM_TOOLS", false)
        whereToGo = intent.getStringExtra("TOOLS_VALUE").toString()
        //startDate = intent.getStringExtra("StartDate").toString()
        isFromThinkRight = intent.getBooleanExtra("FROM_THINK_RIGHT", false)
        /*if (startDate.isEmpty())
            startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())*/

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.iconBack.setOnClickListener {
            callPostMindFullDataAPI()
            finish()
        }

        fetchJournalData()
    }

    private fun fetchJournalData() {
        val call = apiService.getJournals(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JournalResponse> {
            override fun onResponse(
                call: Call<JournalResponse>,
                response: Response<JournalResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val journalList = response.body()?.data ?: emptyList()

                    adapter =
                        JournalAdapter(
                            this@JournalNewActivity,
                            journalList,
                            object : JournalAdapter.OnItemClickListener {
                                override fun onClick(journalItem: JournalItem) {
                                    startNextActivity(journalItem)
                                }

                                override fun onAddToolTip(journalItem: JournalItem) {
                                    CommonAPICall.addToToolKit(
                                        this@JournalNewActivity,
                                        journalItem.id,
                                        !journalItem.isAddedToToolKit
                                    )
                                }

                            })

                    var journalItemSend: JournalItem? = null
                    if (isFromTool) {
                        journalList.forEach { journalItem ->
                            if (journalItem.id.equals(whereToGo)) {
                                journalItemSend = journalItem
                                return@forEach
                            }
                        }
                        if (journalItemSend != null) {
                            startNextActivity(journalItemSend)
                            finish()
                        } else {
                            runOnUiThread {
                                binding.recyclerView.adapter = adapter
                            }
                        }
                    } else {
                        runOnUiThread {
                            binding.recyclerView.adapter = adapter
                        }
                    }
                } else {
                    Toast.makeText(
                        this@JournalNewActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JournalResponse>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun startNextActivity(journalItem: JournalItem?) {
        val intent = when (journalItem?.title) {
            "Free Form" -> Intent(
                this@JournalNewActivity,
                FreeFormJournalActivity::class.java
            )

            "Bullet" -> Intent(
                this@JournalNewActivity,
                JournalPromptActivity::class.java
            )

            else -> Intent(
                this@JournalNewActivity,
                JournalPromptActivity::class.java
            )
        }.apply {
            putExtra("Section", journalItem)
            //putExtra("StartDate", startDate)
            putExtra("FROM_THINK_RIGHT", isFromThinkRight)
        }

        startActivity(intent)
    }

    private fun callPostMindFullDataAPI() {
        /*val endDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        CommonAPICall.postMindFullData(this, "Journaling", startDate, endDate)*/
    }
}
