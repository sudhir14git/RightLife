package com.jetsynthesys.rightlife.subscriptions

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityPlanInfoBinding
import com.jetsynthesys.rightlife.subscriptions.adapter.PlanSliderAdapter
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle

class PlanInfoActivity : BaseActivity() {
    private lateinit var binding: ActivityPlanInfoBinding
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val timeDurationForImageSlider = 2000L

    private val images = listOf(
        R.drawable.planinfo1,
        R.drawable.planinfo2,
        R.drawable.planinfo3,
        R.drawable.planinfo4,
        R.drawable.planinfo5,
        R.drawable.planinfo6,
        R.drawable.planinfo
    )

    private val headers = listOf(
        "Comprehensive health insights with only a phone",
        "Monitor your vitals effortlessly",
        "Smart nutrition tracking, personalised for you",
        "Optimise your activity and performance",
        "Strengthen your mind with powerful tools",
        "Better sleep, deeper recovery, every night",
        "Seamless health tracking, all in one place"
    )

    private val descriptions = listOf(
        "Everyone deserves best-in-class health guidance.\nLog your stats, and we’ll handle the insights for you.",
        "Use our face scan to check BP,Stess,and heart health\nGet expert-backed guidance to optimize  your vitals",
        "Log meals effortlessly and track macros in real.\nStay hydrated and monitor weight trends with ease.",
        "Monitor daily activity(heart rates, steps,calories) and analyze workout/vital data for enhanced recovery.",
        "Track moods, journal, and practice guided breathing.\nUse affirmations and audits to build mental resilience.",
        "Track your sleep stages and consistency, then relax with guided sounds and personalised sleep insights.",
        "Automatically sync data from your wearable.\nMake your data more actionable with deeper insight."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanInfoBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        val combinedHtmlText =
            "<p><b>Premium Features</b></p>" + "<p><b><img src='heart_icon' width='20' height='20' align='texttop'/> My Health</b></p>" + "<b><p>Face Scan</p></b>" + "<p>Measure vitals like BP, stress, and heart health in seconds.</p>" + "<b><p>Wearable Sync & Analysis</b></p>" + "<p>Consolidate and analyse all your wearable data in one place.</p>" + "<p><b><img src='thinkright_icon' width='20' height='20' align='texttop'/> ThinkRight</b></p>" + "<p><b>Mindfulness Minutes</b></p>" + "<p>Log your meditation and relaxation time for better mental well-being.</p>" + "<p><b>Guided Journaling</b></p>" + "<p>Reflect and express with structured journaling options.</p>" + "<p><b>Affirmations</b></p>" + "<p>Boost motivation with daily positive affirmations.</p>" + "<p><b>Breathing Techniques</b></p>" + "<p>Practice four guided breathing exercises for stress relief.</p>" + "<p><b>Inspirational Quotes</b></p>" + "<p>Receive daily doses of motivation and wisdom.</p>" + "<p><b>Mind Audits</b></p>" + "<p>Assess mental well-being with standardised evaluations for stress, depression, and more.</p>" + "<p><b><img src='moveright_icon' width='20' height='20' align='texttop'/> MoveRight</b></p>" + "<p><b>Calorie Analysis</b></p>" + "<p>Understand whether you're burning or consuming more calories daily.</p>" + "<p><b>Workout HR Analysis</b></p>" + "<p>Track heart rate performance during workouts for better training.</p>" + "<p><b>Step Tracker</b></p>" + "<p>Keep an eye on your daily step count to maintain activity levels.</p>" + "<p><b>Vitals Monitoring</b></p>" + "<p>Access key health metrics like HRV, RHR, Active Burn, and Average HR.</p>" + "<p><b><img src='eatright_icon' width='20' height='20' align='texttop'/> EatRight</b></p>" + "<p><b>Daily Macro & Micro Nutrient Analysis</b></p>" + "<p>Get a complete breakdown of your daily intake to ensure balanced nutrition.</p>" + "<p><b>Recipes</b></p>" + "<p>Explore thousands of healthy, goal-aligned recipes.</p>" + "<p><b>Snap-to-Log Calories</b></p>" + "<p>Instantly log meals by snapping a photo—fast, easy, and accurate.</p>" + "<p><b>Meal Logging</b></p>" + "<p>Track your food intake effortlessly to stay on top of your nutrition.</p>" + "<p><b>Meal Insights</b></p>" + "<p>Compare logged meals with your nutritional targets for better choices.</p>" + "<p><b>Hydration Tracker</b></p>" + "<p>Stay hydrated with daily water intake tracking.</p>" + "<p><b>Weight Tracker</b></p>" + "<p>Monitor weight trends and progress over time.</p>" + "<p><b><img src='sleepright_icon' width='20' height='20' align='texttop'/> SleepRight</b></p>" + "<p><b>Sleep Music, Stories & Sounds</b></p>" + "<p>Relax and drift off with soothing audio aids.</p>" + "<p><b>Daily Ideal Sleep Needed</b></p>" + "<p>Get dynamic sleep duration recommendations based on daily activity.</p>" + "<p><b>Sleep Consistency Tracking</b></p>" + "<p>Monitor bedtime regularity and improve sleep patterns.</p>" + "<p><b>Sleep Stage Analysis</b></p>" + "<p>Understand your sleep cycles, from deep to REM sleep.</p>" + "<p><b>FAQs</b></p>" + "<p><b>1. What happens now that my free trial has ended?</b></p>" + "<p>You will still have access to app content under the <b>Explore tab</b>, but <b>Premium Features will be locked</b>. To continue your health journey, subscribe to a monthly or annual plan..</p>" + "<p><b>2. Which features will be limited if I don’t subscribe?</b></p>" + "<p>All <b>Premium Features</b>—including <b>My Health, Think Right, Move Right, Eat Right, and Sleep Right</b>—will be locked until you activate a paid subscription.</p>" + "<p><b>3. How do I subscribe to RightLife?</b></p>" + "<p>You can subscribe through the RightLife app by selecting a monthly or annual plan. We’ll also send you notifications with simple steps to complete your subscription. iOS users can also use the Subscriptions page in the App Store to view our plans.</p>" + "<p><b>4. Can I retrieve my data if I subscribe later?</b></p>" + "<p>Yes! Your data remains safe and secure under GDPR & HIPAA compliance. Once you subscribe, you can access past analytics, trends, and scores that you had viewed during your trial period.</p>" + "<p><b>5. Will I lose my past logs and progress if I don’t subscribe?</b></p>" + "<p>No, your past logs and progress from the trial period will remain saved! However, for seamless tracking and continued progress, we recommend subscribing as soon as possible.</p>" + "<p><b>6. Is there a way to access premium features without subscribing?</b></p>" + "<p>Currently, the <b>7-day free trial</b> is the only way to explore all Premium Features. After the trial, a subscription is required to maintain full access.</p>" + "<p><b>7. Do you offer any discounts or referral benefits for subscriptions?</b></p>" + "<p>At this time, we do not offer additional discounts. However, the <b>7-day free trial</b> allows you to experience all Premium Features before subscribing.</p>" + "<p><b>8. Can I choose a monthly or annual subscription plan?</b></p>" + "<p>Yes, you can select <b>either a monthly or annual plan</b> based on what suits you best for continued access to RightLife. We currently have no tiers so all our features are available for both plans.</p>" + "<p><b>9. Can I cancel my subscription plan anytime?</b></p>" + "<p>Yes, you can cancel your plan at any time by visiting “Manage Subscription” section in our App. However, since you have already used the service partially or fully, <b>refunds will not be processed</b>. You will retain access to <b>RightLife</b> until your next renewal date.</p>".trimIndent()


        binding.tvHtmlText.text =
            Html.fromHtml(combinedHtmlText, Html.FROM_HTML_MODE_LEGACY, CoilImageGetter(), null)

        val htmlPremiumTitleText = "Premium features included in both <b>Monthly</b> and <b>Yearly</b> plans"
        binding.selectPlanTitle.text = Html.fromHtml(htmlPremiumTitleText, Html.FROM_HTML_MODE_LEGACY)
        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.viewPagerImageSlider.adapter =
            PlanSliderAdapter(this, images, headers, descriptions)

        // Set up the TabLayoutMediator to sync dots with the images
        TabLayoutMediator(binding.tabLayout, binding.viewPagerImageSlider) { tab, position ->
            // You can set custom content for tabs if needed, but we are using default dots
            val dot = ImageView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ) // Size of the dot
            params.setMargins(0, 0, 0, 0) // Adjust the margin between dots
            dot.layoutParams = params
            dot.setImageResource(R.drawable.dot) // Default inactive dot image
            tab.customView = dot
        }.attach()

        val selectedColor = ContextCompat.getColor(this, R.color.menuselected)
        val unselectedColor = ContextCompat.getColor(this, R.color.gray)

        binding.indicatorViewPager1.apply {
            setSliderColor(unselectedColor, selectedColor)
            setIndicatorDrawable(R.drawable.dot, R.drawable.dot_selected)
            setSliderGap(30F)
            setSliderWidth(10F)
            setSliderHeight(10F)
            setCheckedSlideWidth(50F)
            setIndicatorGap(resources.getDimensionPixelOffset(R.dimen.textsize_small))
            //setIndicatorSize(dp20, dp20, dp20, dp20)
            setCheckedSlideWidth(50F)

            setSlideMode(IndicatorSlideMode.SCALE)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setupWithViewPager(binding.viewPagerImageSlider)
        }

        startAutoSlide()

        // Update the dots on page change
        binding.viewPagerImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDotColors(position)
            }
        })
    }

    private fun startAutoSlide() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val currentItem = binding.viewPagerImageSlider.currentItem
                val nextItem = if (currentItem < images.size - 1) currentItem + 1 else 0
                binding.viewPagerImageSlider.setCurrentItem(nextItem, true)
                handler.postDelayed(this, timeDurationForImageSlider)
            }
        }
        handler.postDelayed(runnable, timeDurationForImageSlider)
    }

    override fun onPause() {
        super.onPause()
        // Stop the auto-slide when the activity is paused
        handler.removeCallbacks(runnable)
    }

    private fun updateDotColors(position: Int) {
        for (i in 0 until binding.tabLayout.tabCount) {
            val tab = binding.tabLayout.getTabAt(i)
            val dot = tab?.customView as? ImageView
            if (i == position) {
                dot?.setImageResource(R.drawable.dot_selected) // Active dot image
            } else {
                dot?.setImageResource(R.drawable.dot) // Inactive dot image
            }
        }
    }

    inner class CoilImageGetter : Html.ImageGetter {
        override fun getDrawable(source: String): Drawable? {
            val resourceId = when (source) {
                "heart_icon" -> R.drawable.ic_heartbeat
                "thinkright_icon" -> R.drawable.thinkrightsvg
                "moveright_icon" -> R.drawable.moverightsvg
                "eatright_icon" -> R.drawable.eatright_headericon
                "sleepright_icon" -> R.drawable.sleeprightsvg
                else -> 0
            }

            if (resourceId != 0) {
                return ContextCompat.getDrawable(this@PlanInfoActivity, resourceId)?.apply {
                    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                }
            }
            return null
        }
    }
}