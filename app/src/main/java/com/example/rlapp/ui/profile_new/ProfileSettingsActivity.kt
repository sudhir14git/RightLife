package com.example.rlapp.ui.profile_new

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.RetrofitData.ApiClient
import com.example.rlapp.databinding.ActivityProfileSettingsBinding
import com.example.rlapp.ui.new_design.WellnessFocusActivity
import com.example.rlapp.ui.new_design.YourInterestActivity
import com.example.rlapp.ui.settings.AppSettingsActivity
import com.example.rlapp.ui.settings.SettingsNewActivity
import com.example.rlapp.ui.settings.SupportActivity
import com.example.rlapp.ui.settings.adapter.SettingsAdapter
import com.example.rlapp.ui.settings.pojo.SettingItem
import com.example.rlapp.ui.utility.SharedPreferenceManager
import com.example.rlapp.ui.utility.svgloader.GlideApp

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setUserData()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

        setUserData()
        setupUserRecyclerView()
        setupPersonalizationRecyclerView()

        binding.llProfile.setOnClickListener {
            activityResultLauncher.launch(Intent(this, ProfileNewActivity::class.java))
        }
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsNewActivity::class.java))
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupUserRecyclerView() {
        val items = listOf(
            SettingItem("Purchase History"),
            SettingItem("Payment Modes")
        )

        val userAdapter = SettingsAdapter(items) { item ->
            when (item.title) {
                "Purchase History" ->
                    startActivity(Intent(this, AppSettingsActivity::class.java))

                "Payment Modes" ->
                    startActivity(Intent(this, SupportActivity::class.java))
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
            SettingItem("Meal Customisations")
        )

        val personalizationAdapter = SettingsAdapter(items) { item ->
            when (item.title) {
                "Goals" ->
                    startActivity(Intent(this, WellnessFocusActivity::class.java).apply {
                        putExtra("FROM", "ProfileSetting")
                    })

                "Interests" ->
                    startActivity(Intent(this, YourInterestActivity::class.java).apply {
                        putExtra("FROM", "ProfileSetting")
                    })

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
        binding.userName.text = user.firstName.plus(" ${user.lastName}")
        if (user.profilePicture.isNullOrEmpty()) {
            binding.tvProfileLetter.text = user.firstName.first().toString()
        } else {
            GlideApp.with(this)
                .load(ApiClient.CDN_URL_QA + user.profilePicture)
                .into(binding.ivProfileImage)
        }
        //binding.tvUserAge.text = user.age
        binding.tvUserCity.text = user.country
    }
}