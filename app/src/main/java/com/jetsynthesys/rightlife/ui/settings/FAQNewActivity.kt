package com.jetsynthesys.rightlife.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityFaqNewBinding
import com.jetsynthesys.rightlife.ui.settings.adapter.FAQNewAdapter
import com.jetsynthesys.rightlife.ui.settings.pojo.FAQDetails
import com.jetsynthesys.rightlife.ui.settings.pojo.FAQResponse
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FAQNewActivity : BaseActivity() {
    private lateinit var binding: ActivityFaqNewBinding
    private lateinit var faqNewAdapter: FAQNewAdapter
    private val mixedItems = mutableListOf<Any>()   // Now stores both section titles and FAQDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqNewBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

        // Back button
        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup RecyclerView
        binding.rvFaq.layoutManager = LinearLayoutManager(this)

        // Create adapter (empty for now)
        faqNewAdapter = FAQNewAdapter(mixedItems, object : FAQNewAdapter.OnItemClickListener {
            override fun onItemClick(faqData: FAQDetails) {
                // Open details screen exactly as before
                startActivity(Intent(this@FAQNewActivity, FAQDetailsActivity::class.java).apply {
                    putExtra("FAQDetails", faqData)
                })
            }
        })
        binding.rvFaq.adapter = faqNewAdapter

        // Load data
        getFAQ()
    }

    private fun getFAQ() {
        val call = apiService.getFAQData(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<FAQResponse> {
            override fun onResponse(call: Call<FAQResponse>, response: Response<FAQResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val faqResponse = response.body()
                    mixedItems.clear()

                    // ✅ Build combined list: section title + questions
                    faqResponse?.data?.forEach { section ->
                        section.title?.let { title ->
                            mixedItems.add(title)   // Add section header
                        }
                        section.details?.let { details ->
                            mixedItems.addAll(details)   // Add question rows
                        }
                    }

                    faqNewAdapter.notifyDataSetChanged()
                } else {
                    showToast("Server Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FAQResponse>, t: Throwable) {
                showToast("Network Error: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
