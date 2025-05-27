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
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.ActivityProfileSettingsBinding
import com.jetsynthesys.rightlife.ui.new_design.UserInterestActivity
import com.jetsynthesys.rightlife.ui.new_design.WellnessFocusActivity
import com.jetsynthesys.rightlife.ui.settings.PurchasePlansActivity
import com.jetsynthesys.rightlife.ui.settings.SettingsNewActivity
import com.jetsynthesys.rightlife.ui.settings.SubscriptionHistoryActivity
import com.jetsynthesys.rightlife.ui.settings.SupportActivity
import com.jetsynthesys.rightlife.ui.settings.adapter.SettingsAdapter
import com.jetsynthesys.rightlife.ui.settings.pojo.SettingItem
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

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
            SettingItem("Subscription History"),
            SettingItem("Purchase Plans")
        )

        val userAdapter = SettingsAdapter(items) { item ->
            when (item.title) {
                "Subscription History" ->
                    startActivity(Intent(this, SubscriptionHistoryActivity::class.java))

                "Purchase Plans" ->
                    startActivity(Intent(this, PurchasePlansActivity::class.java))
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
            //SettingItem("Meal Customisations")
        )

        val personalizationAdapter = SettingsAdapter(items) { item ->
            when (item.title) {
                "Goals" ->
                    startActivity(Intent(this, WellnessFocusActivity::class.java).apply {
                        putExtra("FROM", "ProfileSetting")
                    })

                "Interests" ->
                    startActivity(Intent(this, UserInterestActivity::class.java).apply {
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
        var name = user.firstName
        if (!user.lastName.isNullOrEmpty())
            name = name.plus(" ${user.lastName}")
        binding.userName.text = name
        if (user.profilePicture.isNullOrEmpty()) {
            if (user.firstName.isNotEmpty())
                binding.tvProfileLetter.text = user.firstName.first().toString()
            else
                binding.tvProfileLetter.text = "R"
        } else {
            binding.ivProfileImage.visibility = VISIBLE
            binding.tvProfileLetter.visibility = GONE
            Glide.with(this)
                .load(ApiClient.CDN_URL_QA + user.profilePicture)
                .into(binding.ivProfileImage)
        }
        if (user.age != null) {
            binding.tvUserAge.text = user.age.toString() + " years"
        }
        binding.tvUserCity.text = user.country
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}