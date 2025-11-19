package com.jetsynthesys.rightlife.ui.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.healthpagemain.HealthPageMainActivity

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
