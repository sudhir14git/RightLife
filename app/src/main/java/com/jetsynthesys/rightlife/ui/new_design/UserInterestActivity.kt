package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityUserInterestBinding
import com.jetsynthesys.rightlife.ui.new_design.pojo.InterestTopic
import com.jetsynthesys.rightlife.ui.new_design.pojo.SaveUserInterestRequest
import com.jetsynthesys.rightlife.ui.new_design.pojo.SaveUserInterestResponse
import com.jetsynthesys.rightlife.ui.new_design.pojo.UserInterestData
import com.jetsynthesys.rightlife.ui.new_design.pojo.UserInterestResponse
import com.jetsynthesys.rightlife.ui.profile_new.ProfileSettingsActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserInterestActivity : BaseActivity() {
    private val selectedInterests: ArrayList<InterestTopic> = ArrayList()
    private lateinit var binding: ActivityUserInterestBinding
    private lateinit var isFrom: String
    private lateinit var colorStateListSelected: ColorStateList
    private lateinit var colorStateListNonSelected: ColorStateList

    val iconList = listOf(
        R.drawable.move_interest,
        R.drawable.eat_interest,
        R.drawable.sleep_interest,
        R.drawable.think_interest
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInterestBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        var header = intent.getStringExtra("WellnessFocus")
        isFrom = intent.getStringExtra("FROM").toString()
        if (header.isNullOrEmpty()) {
            header = sharedPreferenceManager.selectedWellnessFocus
        }

        AnalyticsLogger.logEvent(
            AnalyticsEvent.INTEREST_SELECTION_VISIT,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule
            )
        )

        colorStateListSelected = ContextCompat.getColorStateList(this, R.color.menuselected)!!
        colorStateListNonSelected = ContextCompat.getColorStateList(this, R.color.rightlife)!!

        getInterests()

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSaveInterest.setOnClickListener {
            val ids = ArrayList<String>()
            selectedInterests.forEach { interest ->
                interest.id?.let { it1 -> ids.add(it1) }
            }
            val saveUserInterestRequest = SaveUserInterestRequest()
            saveUserInterestRequest.intrestId = ids
            saveUserInterest(saveUserInterestRequest, header!!)
        }
    }

    private fun getInterests() {
        Utils.showLoader(this)
        val call = apiService.getUserInterestNew(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<UserInterestResponse> {
            override fun onResponse(
                call: Call<UserInterestResponse>,
                response: Response<UserInterestResponse>
            ) {
                Utils.dismissLoader(this@UserInterestActivity)
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.let { handleUserInterestResponse(it) }
                } else {
                    Toast.makeText(
                        this@UserInterestActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserInterestResponse>, t: Throwable) {
                Utils.dismissLoader(this@UserInterestActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun handleUserInterestResponse(userInterests: UserInterestData) {
        userInterests.data?.forEachIndexed { index, userInterest ->

            // Section Layout
            val titleLayout = LinearLayout(this@UserInterestActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 24, 0, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
            }
            val iconRes = iconList.getOrElse(index) { R.drawable.move_interest }
            //section Image
            val iconView = ImageView(this@UserInterestActivity).apply {
                val sizeInDp = 20
                val sizeInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    sizeInDp.toFloat(),
                    resources.displayMetrics
                ).toInt()

                layoutParams = LinearLayout.LayoutParams(sizeInPx, sizeInPx).apply {
                    marginEnd = 8.dpToPx()
                }

                setImageResource(iconRes) // Replace with your actual icon
                scaleType = ImageView.ScaleType.FIT_XY
            }

            // Section title
            val titleView = TextView(this@UserInterestActivity).apply {
                text = userInterest.title
                setTextAppearance(android.R.style.TextAppearance_Medium)
                setPadding(0, 24, 0, 12)
                setTypeface(typeface, Typeface.BOLD)
                textSize = 18F
            }

            // Add views to layout
            titleLayout.addView(iconView)
            titleLayout.addView(titleView)

            // ChipGroup
            val chipGroup = ChipGroup(this@UserInterestActivity).apply {
                isSingleLine = false
                chipSpacingHorizontal = 8
                chipSpacingVertical = 12
                setPadding(0,0,0,0)
            }

            userInterest.topics?.forEach { topic ->
                if (topic.isSelected)
                    selectedInterests.add(topic)
                binding.btnSaveInterest.backgroundTintList =
                    if (selectedInterests.size >= 2) colorStateListSelected else colorStateListNonSelected
                binding.btnSaveInterest.isEnabled = selectedInterests.size >= 2

                val chip = Chip(this).apply {
                    text = topic.topic
                    isCheckable = true
                    isClickable = true
                    isChecked = false
                    chipCornerRadius = 50f
                    chipStrokeColor = ContextCompat.getColorStateList(
                        this@UserInterestActivity,
                        R.color.dark_red
                    )
                    textSize = 12f
                    setEnsureMinTouchTargetSize(false)

                    val heightInDp = 35 // or whatever height you want
                    val heightInPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        heightInDp.toFloat(),
                        resources.displayMetrics
                    ).toInt()

                    val layoutParamsCustom = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        heightInPx
                    )
                    layoutParams = layoutParamsCustom
                    isChecked = topic.isSelected

                    // Set different colors for selected state
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        ),
                        intArrayOf(
                            ContextCompat.getColor(this@UserInterestActivity, R.color.dark_red),
                            ContextCompat.getColor(this@UserInterestActivity, R.color.white)
                        )
                    )

                    val textColorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        ),
                        intArrayOf(
                            ContextCompat.getColor(this@UserInterestActivity, R.color.white),
                            ContextCompat.getColor(this@UserInterestActivity, R.color.black)
                        )
                    )
                    chipBackgroundColor = colorStateList
                    setTextColor(textColorStateList)

                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) selectedInterests.add(topic)
                        else selectedInterests.remove(topic)

                        binding.btnSaveInterest.backgroundTintList =
                            if (selectedInterests.size >= 2) colorStateListSelected else colorStateListNonSelected
                        binding.btnSaveInterest.isEnabled = selectedInterests.size >= 2
                    }
                }
                chipGroup.addView(chip)
            }

            binding.llInterestContainer.addView(titleLayout)
            binding.llInterestContainer.addView(chipGroup)
        }
    }

    private fun saveUserInterest(saveUserInterestRequest: SaveUserInterestRequest, header: String) {
        Utils.showLoader(this)
        val call = apiService.saveUserInterest(
            sharedPreferenceManager.accessToken,
            saveUserInterestRequest
        )

        call.enqueue(object : Callback<SaveUserInterestResponse> {
            override fun onResponse(
                call: Call<SaveUserInterestResponse>,
                response: Response<SaveUserInterestResponse>
            ) {
                Utils.dismissLoader(this@UserInterestActivity)
                if (response.isSuccessful && response.body() != null) {
                    if (isFrom.isNotEmpty() && isFrom == "ProfileSetting") {
                        sharedPreferenceManager.setSavedInterest(selectedInterests)
                        finish()
                        startActivity(
                            Intent(
                                this@UserInterestActivity,
                                ProfileSettingsActivity::class.java
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("start_profile", true)
                            })
                    } else {
                        val intent =
                            Intent(this@UserInterestActivity, EnableNotificationActivity::class.java)
                        intent.putExtra("WellnessFocus", header)
                        sharedPreferenceManager.interest = true
                        startActivity(intent)
                    }
                    val selectedInterestString = selectedInterests.joinToString(",") { it.topic.toString() }
                    AnalyticsLogger.logEvent(
                        AnalyticsEvent.SAVE_INTEREST,
                        mapOf(
                            AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                            AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                            AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                            AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
                            AnalyticsParam.SAVED_INTEREST to selectedInterestString
                        )
                    )

                } else {
                    Toast.makeText(
                        this@UserInterestActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SaveUserInterestResponse>, t: Throwable) {
                Utils.dismissLoader(this@UserInterestActivity)
                handleNoInternetView(t)
            }

        })
    }
    fun Int.dpToPx(): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()
}