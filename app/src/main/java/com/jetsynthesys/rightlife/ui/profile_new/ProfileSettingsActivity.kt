package com.jetsynthesys.rightlife.ui.profile_new

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.databinding.ActivityProfileSettingsBinding
import com.jetsynthesys.rightlife.newdashboard.BeginMyFreeTrialActivity
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistManager
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.new_design.UserInterestActivity
import com.jetsynthesys.rightlife.ui.new_design.WellnessFocusListActivity
import com.jetsynthesys.rightlife.ui.scan_history.PastReportActivity
import com.jetsynthesys.rightlife.ui.settings.PurchasePlansActivity
import com.jetsynthesys.rightlife.ui.settings.SettingsNewActivity
import com.jetsynthesys.rightlife.ui.settings.SubscriptionHistoryActivity
import com.jetsynthesys.rightlife.ui.settings.SupportActivity
import com.jetsynthesys.rightlife.ui.settings.adapter.SettingsAdapter
import com.jetsynthesys.rightlife.ui.settings.pojo.SettingItem
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class ProfileSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setUserData()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

        setUserData()
        getUserDetails()
        setupPersonalizationRecyclerView()

        binding.llProfile.setOnClickListener {

            if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                freeTrialDialogActivity()
            } else if (!DashboardChecklistManager.checklistStatus) {
                DialogUtils.showCheckListQuestionCommonDialog(this) {
                    startActivity(
                        Intent(
                            this@ProfileSettingsActivity, HomeNewActivity::class.java
                        ).putExtra("OPEN_MY_HEALTH", true)
                    )
                    finishAffinity()
                }
            } else {
                activityResultLauncher.launch(Intent(this, ProfileNewActivity::class.java))
            }
        }
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsNewActivity::class.java))
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun freeTrialDialogActivity() {
        val intent = Intent(this, BeginMyFreeTrialActivity::class.java)
        startActivity(intent)
    }

    private fun setupUserRecyclerView(isShowViewPastReports: Boolean) {
        val items = if (isShowViewPastReports) {
            listOf(
                SettingItem("Subscription History"),
                SettingItem("Purchase Plans"),
                SettingItem("View Past Reports")
            )
        } else {
            listOf(
                SettingItem("Subscription History"),
                SettingItem("Purchase Plans"),
                SettingItem("View Past Reports")
            )
        }

        val userAdapter = SettingsAdapter(items) { item ->
            when (item.title) {
                "Subscription History" ->
                    startActivity(Intent(this, SubscriptionHistoryActivity::class.java))

                "Purchase Plans" ->
                    startActivity(Intent(this, PurchasePlansActivity::class.java))

                "View Past Reports" -> {
                    /*if (isShowViewPastReports)
                        startActivity(Intent(this, PastReportActivity::class.java))
                    else
                        Utils.showNewDesignToast(this, "No past reports available", false)*/
                    startActivity(Intent(this, PastReportActivity::class.java))
                }

            }
        }

        binding.profileRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProfileSettingsActivity)
            adapter = userAdapter
        }
    }

    private fun setupPersonalizationRecyclerView() {
        val items = listOf(
            SettingItem("Goals"),
            SettingItem("Interests"),
            SettingItem("Your Saved Items"),
            //SettingItem("Meal Customisations")
        )

        val personalizationAdapter = SettingsAdapter(items) { item ->
            when (item.title) {
                "Goals" ->
                    startActivity(Intent(this, WellnessFocusListActivity::class.java).apply {
                        putExtra("FROM", "ProfileSetting")
                    })

                "Interests" ->
                    startActivity(Intent(this, UserInterestActivity::class.java).apply {
                        putExtra("FROM", "ProfileSetting")
                    })

                "Your Saved Items" ->
                    startActivity(Intent(this, SavedItemListActivity::class.java))

                "Meal Customisations" ->
                    startActivity(Intent(this, SupportActivity::class.java))
            }
        }

        binding.rvPersonalization.apply {
            layoutManager = LinearLayoutManager(this@ProfileSettingsActivity)
            adapter = personalizationAdapter
        }
    }

    private fun setUserData() {
        val user = sharedPreferenceManager.userProfile.userdata
        var name = user.firstName
        if (!user.lastName.isNullOrEmpty())
            name = name.plus(" ${user.lastName}")
        binding.userName.text = name
        if (user.profilePicture.isNullOrEmpty()) {
            /*if (user.firstName.isNotEmpty())
                binding.tvProfileLetter.text = user.firstName.first().toString()
            else
                binding.tvProfileLetter.text = "R"*/
            binding.ivProfileImage.visibility = VISIBLE
        } else {
            binding.ivProfileImage.visibility = VISIBLE
            binding.tvProfileLetter.visibility = GONE
            Glide.with(this)
                .load(ApiClient.CDN_URL_QA + user.profilePicture)
                .placeholder(R.drawable.rl_profile)
                .error(R.drawable.rl_profile)
                .into(binding.ivProfileImage)
        }
        if (user.age != 0) {
            binding.tvUserAge.text = user.age.toString() + " years"
        } else {
            binding.tvUserAge.visibility = GONE
        }
        val locale = Locale.getDefault()
        val countryCode = locale.country
        binding.tvUserCity.text = user.country
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getUserDetails() {
        // Make the API call
        val call = apiService.getUserDetais(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val profileResponse = gson.fromJson(
                        jsonResponse, UserProfileResponse::class.java
                    )
                    sharedPreferenceManager.saveUserId(profileResponse.userdata.id)
                    sharedPreferenceManager.saveUserProfile(profileResponse)

                    sharedPreferenceManager.setAIReportGeneratedView(profileResponse.reportView)
                    setupUserRecyclerView(profileResponse.isReportGenerated)
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }
}