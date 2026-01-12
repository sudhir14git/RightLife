package com.jetsynthesys.rightlife.ui.challenge

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityDailyStreakBinding
import com.jetsynthesys.rightlife.ui.challenge.adapters.ChallengeStreakAdapter
import com.jetsynthesys.rightlife.ui.challenge.pojo.ChallengeStreakResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DailyStreakActivity : BaseActivity() {

    private lateinit var binding: ActivityDailyStreakBinding
    private lateinit var adapter: ChallengeStreakAdapter
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyStreakBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        setupToolbar()
        setupRecycler()

        loadStreak()
    }

    private fun setupToolbar() {
        binding.icBackDialog.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.tvHeaderHtw.text = "Daily Streak"
    }

    private fun setupRecycler() {
        adapter = ChallengeStreakAdapter()
        binding.rvJourney.layoutManager = LinearLayoutManager(this)
        binding.rvJourney.adapter = adapter
    }

    private fun loadStreak() {
        if (isLoading) return
        isLoading = true

        val call = apiService.getChallengeStreak(sharedPreferenceManager.accessToken)

        call.enqueue(object : Callback<ResponseBody?> {

            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                isLoading = false

                if (response.isSuccessful && response.body() != null) {
                    try {
                        val gson = Gson()
                        val jsonString = response.body()!!.string()

                        val responseObj =
                            gson.fromJson(jsonString, ChallengeStreakResponse::class.java)

                        if (responseObj.success) {
                            bindStreak(responseObj)
                        } else {
                            Toast.makeText(
                                this@DailyStreakActivity,
                                "Failed to load streak",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@DailyStreakActivity,
                            "Streak parse error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@DailyStreakActivity,
                        "Streak API error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                isLoading = false
                handleNoInternetView(t)
            }
        })
    }

    private fun bindStreak(response: ChallengeStreakResponse) {
        // top number
        binding.tvStreakCount.text = response.data.streak.toString()

        // list
        val items = response.data.journey.map {
            ChallengeStreakAdapter.Item(
                label = it.label,
                date = it.date,
                status = it.status
            )
        }
        adapter.submitList(items)
    }
}
