package com.jetsynthesys.rightlife.ui.challenge

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityLeaderboardBinding
import com.jetsynthesys.rightlife.ui.challenge.adapters.LeaderboardAdapter
import com.jetsynthesys.rightlife.ui.challenge.pojo.LeaderboardResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LeaderboardActivity : BaseActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var adapter: LeaderboardAdapter
    private var isLoading = false
    private var currentType: String = "daily" // this is the landing tab

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        setupToolbar()
        setupTabs()
        setupRecycler()

        // default  (Week)
        binding.tabGroupNew.check(R.id.rbDay)
        loadLeaderboard(currentType)
    }

    private fun setupToolbar() {
        binding.icBackDialog.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupTabs() {
        binding.tabGroupNew.setOnCheckedChangeListener { _, checkedId ->
            if (isLoading) return@setOnCheckedChangeListener

            when (checkedId) {
                R.id.rbDay -> loadLeaderboard("daily")
                R.id.rbWeek -> loadLeaderboard("weekly")
                R.id.rbAllTime -> loadLeaderboard("all")
            }
        }
    }

    private fun setupRecycler() {
        adapter = LeaderboardAdapter()
        val rv = findViewById<RecyclerView>(R.id.rvTop)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun loadLeaderboard(type: String) {
        currentType = type
        isLoading = true

        val call = apiService.getLeaderboard(
            sharedPreferenceManager.accessToken,
            type
        )

        call.enqueue(object : Callback<ResponseBody?> {

            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                isLoading = false

                if (response.isSuccessful && response.body() != null) {
                    try {
                        val gson = Gson()
                        val jsonString = response.body()!!.string()

                        val responseObj =
                            gson.fromJson(jsonString, LeaderboardResponse::class.java)

                        if (responseObj.success) {
                            bindLeaderboard(responseObj)
                        } else {
                            Toast.makeText(
                                this@LeaderboardActivity,
                                "Failed to load leaderboard",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@LeaderboardActivity,
                            "Leaderboard parse error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LeaderboardActivity,
                        "Leaderboard API error: ${response.code()}",
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

    private fun bindLeaderboard(response: LeaderboardResponse) {
        val data = response.data

        // ----- Your Position pinned -----
        data.yourRank?.let { my ->
            bindMyPosition(rank = my.rank, score = my.totalScore)
        }

        // ----- Scrollable leaderboard -----
        val items = data.leaderboard.map {
            LeaderboardAdapter.Item(
                rank = it.rank,
                name = it.name,
                score = it.totalScore
            )
        }

        // Remove duplication of "You" if backend includes it in leaderboard list
        val myRank = data.yourRank?.rank
        val filtered = if (myRank != null) items.filterNot { it.rank == myRank } else items

        adapter.submitList(filtered)
    }

    private fun bindMyPosition(rank: Int, score: Int) {
        // include overrides root id => includeMyPosition IS the CardView
        val card = findViewById<CardView>(R.id.includeMyPosition)

        val circle = card.findViewById<FrameLayout>(R.id.flRank)
        val tvRank = card.findViewById<TextView>(R.id.tvRank)
        val tvName = card.findViewById<TextView>(R.id.tvName)
        val tvScore = card.findViewById<TextView>(R.id.tvScore)

        tvRank.text = rank.toString()
        tvName.text = sharedPreferenceManager.userProfile.userdata.firstName
        tvScore.text = score.toString()

        LeaderboardUiStyler.apply(rank, card, circle)
    }
}
