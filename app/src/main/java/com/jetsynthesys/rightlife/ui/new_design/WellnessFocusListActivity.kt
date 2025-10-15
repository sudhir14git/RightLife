package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.new_design.pojo.ModuleTopic
import com.jetsynthesys.rightlife.ui.new_design.pojo.OnBoardingDataModuleResponse
import com.jetsynthesys.rightlife.ui.new_design.pojo.OnboardingModuleRequest
import com.jetsynthesys.rightlife.ui.profile_new.ProfileSettingsActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WellnessFocusListActivity : BaseActivity() {

    private val selectedWellnessFocus = ArrayList<ModuleTopic>()
    private lateinit var wellnessFocusListAdapter: WellnessFocusListAdapter
    val topicList = ArrayList<ModuleTopic>()
    private lateinit var isFrom: String

    // Declare the TextViews as class-level properties
    private lateinit var tv_ur_journey: TextView
    private lateinit var tv_choose_str: TextView
    private lateinit var btnContinue: Button
    private lateinit var llHeader: LinearLayout
    private lateinit var colorStateListSelected: ColorStateList
    private lateinit var colorStateList: ColorStateList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_wellness_focus_list)

        //var header = intent.getStringExtra("WellnessFocus")
        isFrom = intent.getStringExtra("FROM").toString()

        val tvHeader = findViewById<TextView>(R.id.tv_header)
        val rvWellnessFocusList = findViewById<RecyclerView>(R.id.rv_wellness_focus_list)
        btnContinue = findViewById(R.id.btn_continue)
        llHeader = findViewById(R.id.ll_header)
        val imgHeader = findViewById<ImageView>(R.id.img_header)

        AnalyticsLogger.logEvent(
            AnalyticsEvent.SUB_GOAL_SELECTION_VISIT,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
            )
        )

        // Initialize the TextViews in onCreate
        tv_ur_journey = findViewById(R.id.tv_ur_journey)
        tv_choose_str = findViewById(R.id.tv_choose_str)

        /*when (header) {
            "MoveRight" -> imgHeader.setImageResource(R.drawable.header_move_right)
            "SleepRight" -> imgHeader.setImageResource(R.drawable.header_sleep_right)
            "EatRight" -> imgHeader.setImageResource(R.drawable.header_eat_right)
            else
            -> imgHeader.setImageResource(R.drawable.header_think_right)
        }
        tvHeader.text = header*/

        if (isFrom.isNotEmpty() && isFrom == "ProfileSetting") {
            btnContinue.text = "Save"
            llHeader.visibility = View.VISIBLE
        } else {
            llHeader.visibility = View.GONE
        }

        //tvHeader.setTextColor(Utils.getModuleDarkColor(this, header))
        getOnboardingDataModule()

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        colorStateListSelected = ContextCompat.getColorStateList(this, R.color.menuselected)!!
        colorStateList = ContextCompat.getColorStateList(this, R.color.rightlife)!!

        wellnessFocusListAdapter =
            WellnessFocusListAdapter(
                this,
                topicList
            ) { wellnessFocus ->
                if (wellnessFocus.isSelected)
                    selectedWellnessFocus.remove(wellnessFocus)
                else
                    selectedWellnessFocus.add(wellnessFocus)

                if (selectedWellnessFocus.size in 2..4) {
                    btnContinue.backgroundTintList = colorStateListSelected
                } else {
                    btnContinue.backgroundTintList = colorStateList
                }
            }

        val gridLayoutManager = GridLayoutManager(this, 1)
        rvWellnessFocusList.setLayoutManager(gridLayoutManager)

        rvWellnessFocusList.adapter = wellnessFocusListAdapter

        btnContinue.setOnClickListener {

            if (selectedWellnessFocus.size in 2..4) {
                val selectedOptions = ArrayList<String>()
                selectedWellnessFocus.forEach {
                    it.id?.let { it1 -> selectedOptions.add(it1) }
                }
                updateOnBoardingModule(selectedOptions)
            } else if (selectedWellnessFocus.size < 2) {
                Utils.showNewDesignToast(this, "Please choose at least 2 goals ", false)
            } else {
                Utils.showNewDesignToast(this, "You can select up to 4 goals only.", false)
            }
        }

        AnalyticsLogger.logEvent(
            AnalyticsEvent.SUB_GOAL_SELECTION,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule
            )
        )
    }

    private fun getOnboardingDataModule() {
        Utils.showLoader(this)

        val call =
            apiService.getOnboardingDataModule(sharedPreferenceManager.accessToken, "all")
        call.enqueue(object : Callback<OnBoardingDataModuleResponse> {
            override fun onResponse(
                call: Call<OnBoardingDataModuleResponse>,
                response: Response<OnBoardingDataModuleResponse>
            ) {
                Utils.dismissLoader(this@WellnessFocusListActivity)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()

                    val data = apiResponse?.data
                    data?.data?.let { topicList.addAll(it) }
                    wellnessFocusListAdapter.notifyDataSetChanged()
                    tv_ur_journey.text = data?.sectionTitle
                    tv_choose_str.text = data?.sectionSubtitle

                    topicList.forEach {
                        if (it.isSelected)
                            selectedWellnessFocus.add(it)
                    }

                    if (selectedWellnessFocus.size in 2..4) {
                        btnContinue.backgroundTintList = colorStateListSelected
                    } else {
                        btnContinue.backgroundTintList = colorStateList
                    }

                } else {
                    Toast.makeText(
                        this@WellnessFocusListActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<OnBoardingDataModuleResponse>, t: Throwable) {
                Utils.dismissLoader(this@WellnessFocusListActivity)
                handleNoInternetView(t)
            }

        })
    }

    private fun updateOnBoardingModule(/*selectedModule: String, */selectedOptions: List<String>) {
        val onboardingModuleRequest = OnboardingModuleRequest()
        //onboardingModuleRequest.selectedModule = selectedModule
        onboardingModuleRequest.selectedOptions = selectedOptions

        val call = apiService.onboardingModuleResult(
            sharedPreferenceManager.accessToken,
            onboardingModuleRequest
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    sharedPreferenceManager.selectedOnboardingSubModule =
                        selectedWellnessFocus[0].moduleName
                    showCustomToast("Goals Saved", true)
                    if (isFrom.isNotEmpty() && isFrom == "ProfileSetting") {
                        finish()
                        startActivity(
                            Intent(
                                this@WellnessFocusListActivity,
                                ProfileSettingsActivity::class.java
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("start_profile", true)
                            })
                    } else {
                        val intent =
                            Intent(this@WellnessFocusListActivity, UserInterestActivity::class.java)
                        //intent.putExtra("WellnessFocus", header)
                        intent.putExtra("SelectedTopic", selectedWellnessFocus)
                        SharedPreferenceManager.getInstance(this@WellnessFocusListActivity)
                            .setWellnessFocusTopics(selectedWellnessFocus)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(
                        this@WellnessFocusListActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showCustomToast("Couldn’t save. Check connection and try again.")
            }

        })
    }
}