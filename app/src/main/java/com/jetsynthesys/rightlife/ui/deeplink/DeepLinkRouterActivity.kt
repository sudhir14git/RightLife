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
        val path = data.path ?: return

        when {

            // Home
            path == "/home" -> start(HomeNewActivity::class.java)

            // My Health
            path == "/my-health" -> start(HealthPageMainActivity::class.java)

            // Dashboard
            path.startsWith("/dashboard") -> start(HomeNewActivity::class.java)

            // Journal
            path == "/journal" -> start(JournalingActivity::class.java)

            // Mind Audit
            path == "/mind-audit" -> start(MindAuditActivity::class.java)

            // Meal Log
            path == "/meal-log" -> openMealLog()

            // Meal Scan Results
            path == "/meal-scan-results" -> openMealScanResults()

            // Recipes list
            path == "/recipes" -> openRecipesList()

            // Recipe detail
            path.startsWith("/recipe/") -> {
                val id = path.substringAfterLast("/")
                openRecipeDetail(id)
            }

            // AI report
            path == "/ai-report" -> openAIReport()

            // Recently viewed
            path == "/recently-viewed" -> openRecentlyViewed()

            // Saved Items
            path == "/saved-items" -> openSavedItems()

            // Sleep screens
            path == "/sleep-performance" -> openSleepPerformance()
            path == "/ideal-vs-actual-sleep" -> openSleepPerformance()
            path == "/restorative-sleep" -> openSleepPerformance()
            path == "/sleep-consistency" -> openSleepPerformance()
            path == "/sleep-stages" -> openSleepStages()

            // Trackers
            path == "/weight-tracker" -> openWeightTracker()
            path == "/calorie-balance" -> openCalorieBalance()
            path == "/steps-tracker" -> openStepsTracker()
            path == "/hydration-tracker" -> openHydrationTracker()

            // Nutrition
            path == "/macros" -> openMacros()
            path == "/micros" -> openMicros()

            // Workout log
            path == "/workout-log" -> openWorkoutLog()

            // Routine
            path == "/routine" -> openRoutine()

            // Activities
            path == "/activities" -> openActivities()

            // Affirmations
            path == "/affirmations" -> openAffirmations()

            // Breathing
            path == "/breathing" -> start(BreathworkActivity::class.java)

            // Meditation
            path == "/meditation" -> openMeditation()

            // Tools
            path == "/tools" -> openTools()

            // Profile
            path == "/profile" -> openProfile()

            // Settings
            path == "/settings" -> openSettings()

            // Support
            path == "/support" -> openSupport()

            // Privacy Policy
            path == "/privacy-policy" -> openPrivacyPolicy()

            // Wellness content
            path.startsWith("/wellness") -> openWellness()

            // Module detail
            path.startsWith("/module") -> openModule()

            // Series detail
            path.startsWith("/series") -> openSeries()

            // Episode detail
            path.startsWith("/episode") -> openEpisode()

            else -> start(HomeNewActivity::class.java)
        }
    }

    private fun <T> start(cls: Class<T>) {
        startActivity(Intent(this, cls))
    }

    private fun openMealLog() {
        val i = Intent(this, MainAIActivity::class.java)
        i.putExtra("ModuleName", "HomeDashboard")
        i.putExtra("BottomSeatName", "MealLogTypeEat")
        startActivity(i)
    }

    private fun openRecipeDetail(id: String) {
        val i = Intent(this, ReceipeDetailActivity::class.java)
        i.putExtra("recipeId", id)
        startActivity(i)
    }

    // TODO: Add real Activity mappings for each function below:
    private fun openMealScanResults() {start(HomeNewActivity::class.java)}
    private fun openRecipesList() {start(HomeNewActivity::class.java)}
    private fun openAIReport() {start(HomeNewActivity::class.java)}
    private fun openRecentlyViewed() {start(HomeNewActivity::class.java)}
    private fun openSavedItems() {start(HomeNewActivity::class.java)}
    private fun openSleepPerformance() {start(HomeNewActivity::class.java)}
    private fun openSleepStages() {start(HomeNewActivity::class.java)}
    private fun openWeightTracker() {start(HomeNewActivity::class.java)}
    private fun openCalorieBalance() {start(HomeNewActivity::class.java)}
    private fun openStepsTracker() {start(HomeNewActivity::class.java)}
    private fun openHydrationTracker() {start(HomeNewActivity::class.java)}
    private fun openMacros() {start(HomeNewActivity::class.java)}
    private fun openMicros() {start(HomeNewActivity::class.java)}
    private fun openWorkoutLog() {start(HomeNewActivity::class.java)}
    private fun openRoutine() {start(HomeNewActivity::class.java)}
    private fun openActivities() {start(HomeNewActivity::class.java)}
    private fun openAffirmations() {start(HomeNewActivity::class.java)}
    private fun openMeditation() {start(HomeNewActivity::class.java)}
    private fun openTools() {start(HomeNewActivity::class.java)}
    private fun openProfile() {start(HomeNewActivity::class.java)}
    private fun openSettings() {start(HomeNewActivity::class.java)}
    private fun openSupport() {start(HomeNewActivity::class.java)}
    private fun openPrivacyPolicy() {start(HomeNewActivity::class.java)}
    private fun openWellness() {start(HomeNewActivity::class.java)}
    private fun openModule() {start(HomeNewActivity::class.java)}
    private fun openSeries() {start(HomeNewActivity::class.java)}
    private fun openEpisode() {start(HomeNewActivity::class.java)}
}

/*
class DeepLinkRouterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink()
        finish() // router is invisible
    }

    private fun handleDeepLink() {
        val data = intent?.data ?: return
        val path = data.path ?: return   // e.g. "/meal-log"

        when {
            path == "/home" -> {
                startActivity(Intent(this, HomeNewActivity::class.java))
            }
            path == "/my-health" -> {
                startActivity(Intent(this, HomeNewActivity::class.java))
            }
            path == "/meal-log" -> {
                //openMealLog()
            }
            path.startsWith("/recipe/") -> {
                //openRecipeDetails(path)
            }
            // more mapping here...
            else -> {
                // Unknown path → fallback
                startActivity(Intent(this, HomeNewActivity::class.java))
            }
        }
    }
}
*/
