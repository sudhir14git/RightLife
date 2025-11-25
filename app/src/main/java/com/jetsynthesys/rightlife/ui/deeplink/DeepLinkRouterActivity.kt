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
            path == "/my-health" || path.startsWith("/dashboard") -> {
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

            // /ai-report
            path == "/ai-report" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_AI_REPORT
                )
            }
            // /mind-audit
            path == "/mind-audit" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_MIND_AUDIT
                )
            }
            //face-scan
            path == "/face-scan" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_FACE_SCAN
                )
            }
            //face-scan
            path == "/snap-meal" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_SNAP_MEAL

                )
            }

            //sleepsound
            path == "/sleep-sound" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_SLEEP_SOUND

                )
            }
            //Affirmation
            path == "/affirmation" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_AFFIRMATION

                )
            }

            // Breathing
            path == "/breathing" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_BREATHING
                )
            }

            // activity-log
            path == "/activity-log" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_ACTIVITY_LOG
                )
            }
            // Quick link section

            // weight-log
            path == "/weight-log" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_WEIGHT_LOG
                )
            }
            // water-log
            path == "/water-log" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_WATER_LOG
                )
            }

            // sleep-log
            path == "/sleep-log" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_SLEEP_LOG
                )
            }


            // food-log
            path == "/food-log" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_FOOD_LOG
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
            path == "/jumpback" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_JUMPBACK
                )
            }
            // thinkright-explore Section
            path == "/thinkright-explore" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_THINK_EXPLORE
                )
            }
            // eatright-explore Section
            path == "/eatright-explore" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_EAT_EXPLORE
                )
            }
            // sleepright-explore  Section
            path == "/sleepright-explore" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_SLEEP_EXPLORE
                )
            }
            // thinkright-explore Section
            path == "/moveright-explore" -> {
                intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_MOVE_EXPLORE
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


