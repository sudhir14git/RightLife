package com.jetsynthesys.rightlife.ui.deeplink


import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.new_design.DataControlActivity


class DeepLinkRouterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authToken = sharedPreferenceManager.accessToken
        if (authToken.isEmpty()) {
            val intent = Intent(this, DataControlActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            handleDeepLink()
        }
    }

    private fun handleDeepLink() {
        val data = intent?.data ?: return
        val path = data.path ?: return   // e.g. "/meal-log"

        val intent = Intent(this, HomeNewActivity::class.java)

        Log.d("Umesh", "Path = $path")

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

            path == "/affirmationPlaylist" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_AFFIRMATION_PLAYLIST

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

            path == "/thinkright" || path == "/thinkright-home" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_THINKRIGHT_HOME
                )
            }
            // sleep-log
            /* path == "/sleep-log" -> {
                 intent.putExtra(
                         HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                         HomeNewActivity.TARGET_SLEEP_LOG
                 )
             }*/


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

            path == "/moveright-home" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_MOVERIGHT_HOME
                )
            }

            path == "/eatright-home" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_EATRIGHT_HOME
                )
            }

            path == "/sleepright-home" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_SLEEPRIGHT_HOME
                )
            }

            path == "/weight-log" || path == "/weight_log" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_WEIGHT_LOG_DEEP
                )
            }

            path == "/water-log" || path == "/water_log" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_WATER_LOG_DEEP
                )
            }

            path == "/snap-meal" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_SNAP_MEAL_DEEP
                )
            }

            path == "/food-log" || path == "/food_log" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_FOOD_LOG_DEEP
                )
            }

            path == "/sleep-log" || path == "/sleep_log" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_SLEEP_LOG_DEEP
                )
            }

            // Profile
            path == "/saveditems" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_SAVED_ITEMS
                )
            }

            path == "/workoutlog" || path == "/workout-log" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_WORKOUT_LOG_DEEP
                )
            }

            // Profile
            path == "/categorylist" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_CATEGORY_LIST
                )
            }

            // Challenge Page
            path == "/challenge-home" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_CHALLENGE_HOME
                )
            }

            // Challenge LeaderBoard
            path == "/challenge-leaderboard" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_CHALLENGE_LEADERBOARD
                )
            }

            path == "/plans/SUBSCRIPTION_PLAN" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_SUBSCRIPTION_PLAN
                )
            }

            path == "/plans/BOOSTER_PLAN" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_BOOSTER_PLAN
                )
            }

            // MindAudit info
            path == "/mind-audit/phq9Info" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_MIND_AUDIT_PHQ9
                )
            }

            path == "/mind-audit/GAD7" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_MIND_AUDIT_GAD7
                )
            }

            path == "/mind-audit/ohq" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_MIND_AUDIT_OHQ
                )
            }

            path == "/mind-audit/cas" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_MIND_AUDIT_CAS
                )
            }

            path == "/mind-audit/dass21" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_MIND_AUDIT_DASS21
                )
            }

            // Breathing Types
            path == "/breathing-alternate" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_BREATHING_ALTERNATE
                )
            }

            path == "/breathing-boxbreathing" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_BREATHING_BOX
                )
            }

            path == "/breathing-custom" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_BREATHING_CUSTOM
                )
            }

            path == "/breathing-breathing-4-7-8" -> {
                intent.putExtra(
                    HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                    HomeNewActivity.TARGET_BREATHING_4_7_8
                )
            }


            // TODO: map other paths similarly:
            // "/ai-report", "/saved-items", "/sleep-performance", "/weight-tracker", etc.
            // each gets a DEEP_LINK_TARGET, and HomeNewActivity will handle them.

            else -> {
                if (path.contains(HomeNewActivity.TARGET_ARTICLE)
                    || path.contains(HomeNewActivity.TARGET_AUDIO)
                    || path.contains(HomeNewActivity.TARGET_VIDEO)
                    || path.contains(HomeNewActivity.TARGET_SERIES)
                    || path.contains(HomeNewActivity.TARGET_SERIES_DETAILS)) {
                    intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET, path
                    )
                } else
                // fallback → open home
                    intent.putExtra(
                        HomeNewActivity.EXTRA_DEEP_LINK_TARGET,
                        HomeNewActivity.TARGET_HOME
                    )
            }
        }

        startActivity(intent)
        finish()
    }
}


