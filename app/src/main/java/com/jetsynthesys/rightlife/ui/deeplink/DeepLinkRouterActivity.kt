package com.jetsynthesys.rightlife.ui.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.healthpagemain.HealthPageMainActivity


import com.jetsynthesys.rightlife.ui.jounal.JournalingActivity
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditActivity
import com.jetsynthesys.rightlife.ui.breathwork.BreathworkActivity
import com.jetsynthesys.rightlife.ui.Articles.ReceipeDetailActivity
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity


class DeepLinkRouterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink()
        finish()
    }

    private fun handleDeepLink() {
        val data = intent?.data ?: return
        val path = data.path ?: return   // e.g. "/meal-log"

        val intent = Intent(this, HomeNewActivity::class.java)

        when {
            // Home
            path == "/home" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_HOME
                )
            }

            // My Health
            path == "/my_health" || path.startsWith("/dashboard") -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_MY_HEALTH
                )
                // You can ALSO set OPEN_MY_HEALTH for backwards compatibility if you want:
                intent.putExtra("OPEN_MY_HEALTH", true)
            }

            // Journal
            path == "/journal" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_JOURNAL
                )
            }

            // Meal Log
            path == "/meal-log" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_MEAL_LOG
                )
            }

            // Breathing
            path == "/breathing" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_BREATHING
                )
            }

            // Profile
            path == "/profile" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_PROFILE
                )
            }
            // Profile
            path == "/categorylist" -> {
                intent.putExtra(HomeNewActivity.EXTRA_DEEP_LINK_TARGET, HomeNewActivity.TARGET_CATEGORY_LIST)
            }

            // TODO: map other paths similarly:
            // "/ai-report", "/saved-items", "/sleep-performance", "/weight-tracker", etc.
            // each gets a DEEP_LINK_TARGET, and HomeNewActivity will handle them.

            else -> {
                // fallback → open home
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_HOME
                )
            }
        }

        startActivity(intent)
    }
}
