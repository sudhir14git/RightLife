package com.jetsynthesys.rightlife.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityNotificationsNewBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.settings.adapter.SettingsAdapter
import com.jetsynthesys.rightlife.ui.settings.pojo.SettingItem

class NotificationsNewActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationsNewBinding
    private lateinit var settingsAdapter: SettingsAdapter
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsNewBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        setupEmailNotificationsRecyclerView()

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        CommonAPICall.getNotificationSettings(this) { data ->
            binding.pushNotificationsSwitch.isChecked = data.pushNotification == true
            sharedPreferenceManager.enableNotificationServer = data.pushNotification == true
        }

        binding.pushNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            val requestBody = mapOf("pushNotification" to isChecked)
            CommonAPICall.updateNotificationSettings(this, requestBody) { result, message ->
                if (count > 0) {
                    showToast(message)
                    CommonAPICall.getNotificationSettings(this) { data ->
                        sharedPreferenceManager.enableNotificationServer = data.pushNotification == true
                    }
                }
                count++
                if (!result) binding.pushNotificationsSwitch.isChecked = !isChecked
            }
        }
    }

    private fun setupEmailNotificationsRecyclerView() {
        val settingsItems = listOf(
            SettingItem("Email Notifications")
        )

        settingsAdapter = SettingsAdapter(settingsItems) { item ->
            when (item.title) {
                "Email Notifications" -> {
                    startActivity(Intent(this, EmailNotificationsNewActivity::class.java))
                }
            }
        }

        binding.rvEmailNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationsNewActivity)
            adapter = settingsAdapter
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}