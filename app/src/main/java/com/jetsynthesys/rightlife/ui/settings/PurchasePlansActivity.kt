package com.jetsynthesys.rightlife.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityPurchasePlansBinding
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity
import com.jetsynthesys.rightlife.ui.settings.adapter.SettingsAdapter
import com.jetsynthesys.rightlife.ui.settings.pojo.SettingItem

class PurchasePlansActivity : BaseActivity() {
    private lateinit var binding: ActivityPurchasePlansBinding
    private lateinit var settingsAdapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchasePlansBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        //back button
        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        setupPurchasePlansRecyclerView()
    }

    private fun setupPurchasePlansRecyclerView() {
        val settingsItems = listOf(
            SettingItem("Subscription Plans"),
            SettingItem("Booster Packs")
        )

        settingsAdapter = SettingsAdapter(settingsItems) { item ->
            when (item.title) {
                "Subscription Plans" -> {
                    startActivity(Intent(this, SubscriptionPlanListActivity::class.java).apply {
                        putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                    })
                }

                "Booster Packs" -> {
                    startActivity(Intent(this, SubscriptionPlanListActivity::class.java).apply {
                        putExtra("SUBSCRIPTION_TYPE", "FACIAL_SCAN")
                    })
                }
            }
        }

        binding.rvPurchasePlans.apply {
            layoutManager = LinearLayoutManager(this@PurchasePlansActivity)
            adapter = settingsAdapter
        }
    }
}