package com.jetsynthesys.rightlife.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivitySupportBinding
import com.jetsynthesys.rightlife.ui.settings.adapter.SettingsAdapter
import com.jetsynthesys.rightlife.ui.settings.pojo.SettingItem
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam

class SupportActivity : BaseActivity() {

    private lateinit var binding: ActivitySupportBinding
    private lateinit var settingsAdapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        //back button
        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        setupSupportRecyclerView()
    }

    private fun setupSupportRecyclerView() {
        val settingsItems = listOf(
            SettingItem("FAQ"),
            SettingItem("Write to us!")
        )

        settingsAdapter = SettingsAdapter(settingsItems) { item ->
            when (item.title) {
                "FAQ" -> {
                    startActivity(Intent(this, FAQNewActivity::class.java))
                }

                "Write to us!" -> {
                    WriteToUsUtils.sendEmail(this@SupportActivity)
                    AnalyticsLogger.logEvent(
                        AnalyticsEvent.USER_FEEDBACK_CLICK,
                        mapOf(
                            AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                            AnalyticsParam.USER_TYPE to if (sharedPreferenceManager.userProfile.isSubscribed) "Paid User" else "free User",
                            AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        binding.rvSupport.apply {
            layoutManager = LinearLayoutManager(this@SupportActivity)
            adapter = settingsAdapter
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}