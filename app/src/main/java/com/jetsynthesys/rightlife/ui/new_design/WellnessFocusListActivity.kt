package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.ui.new_design.pojo.ModuleTopic
import com.jetsynthesys.rightlife.ui.new_design.pojo.OnBoardingDataModuleResponse
import com.jetsynthesys.rightlife.ui.new_design.pojo.OnboardingModuleRequest
import com.jetsynthesys.rightlife.ui.profile_new.ProfileSettingsActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_wellness_focus_list)

        var header = intent.getStringExtra("WellnessFocus")
        isFrom = intent.getStringExtra("FROM").toString()
        if (header.isNullOrEmpty()) {
            header = SharedPreferenceManager.getInstance(this).selectedWellnessFocus
        }
        val tvHeader = findViewById<TextView>(R.id.tv_header)
        val rvWellnessFocusList = findViewById<RecyclerView>(R.id.rv_wellness_focus_list)
        val btnContinue = findViewById<Button>(R.id.btn_continue)
        val imgHeader = findViewById<ImageView>(R.id.img_header)

        // Initialize the TextViews in onCreate
        tv_ur_journey = findViewById(R.id.tv_ur_journey)
        tv_choose_str = findViewById(R.id.tv_choose_str)

        when (header) {
            "MoveRight" -> imgHeader.setImageResource(R.drawable.header_move_right)
            "SleepRight" -> imgHeader.setImageResource(R.drawable.header_sleep_right)
            "EatRight" -> imgHeader.setImageResource(R.drawable.header_eat_right)
            else
            -> imgHeader.setImageResource(R.drawable.header_think_right)
        }
        tvHeader.text = header

        if (isFrom.isNotEmpty() && isFrom == "ProfileSetting")
            btnContinue.text = "Save"

        tvHeader.setTextColor(Utils.getModuleDarkColor(this, header))
        getOnboardingDataModule(header)

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val colorStateListSelected = ContextCompat.getColorStateList(this, R.color.menuselected)
        val colorStateList = ContextCompat.getColorStateList(this, R.color.rightlife)

        wellnessFocusListAdapter =
            WellnessFocusListAdapter(
                this,
                topicList,
                { wellnessFocus ->
                    if (wellnessFocus.isSelected)
                        selectedWellnessFocus.remove(wellnessFocus)
                    else
                        selectedWellnessFocus.add(wellnessFocus)

                    if (selectedWellnessFocus.isEmpty()) {
                        btnContinue.backgroundTintList = colorStateList
                        btnContinue.isEnabled = false
                    } else {
                        btnContinue.backgroundTintList = colorStateListSelected
                        btnContinue.isEnabled = true
                    }
                },
                module = header!!
            )

        val gridLayoutManager = GridLayoutManager(this, 1)
        rvWellnessFocusList.setLayoutManager(gridLayoutManager)

        rvWellnessFocusList.adapter = wellnessFocusListAdapter

        btnContinue.setOnClickListener {
            val selectedOptions = ArrayList<String>()
            selectedWellnessFocus.forEach {
                it.id?.let { it1 -> selectedOptions.add(it1) }
            }
            updateOnBoardingModule(header, selectedOptions)
            sharedPreferenceManager.selectedOnboardingSubModule = selectedWellnessFocus[0].moduleName
            if (isFrom.isNotEmpty() && isFrom == "ProfileSetting") {
                finish()
                startActivity(
                    Intent(
                        this@WellnessFocusListActivity,
                        ProfileSettingsActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("start_profile", true)
                    })
            } else {
                val intent = Intent(this, UnlockPowerOfYourMindActivity::class.java)
                intent.putExtra("WellnessFocus", header)
                intent.putExtra("SelectedTopic", selectedWellnessFocus)
                SharedPreferenceManager.getInstance(this)
                    .setWellnessFocusTopics(selectedWellnessFocus)
                startActivity(intent)
            }
        }
    }

    private fun getOnboardingDataModule(moduleName: String?) {
        Utils.showLoader(this)

        val call =
            apiService.getOnboardingDataModule(sharedPreferenceManager.accessToken, moduleName)
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

    private fun updateOnBoardingModule(selectedModule: String, selectedOptions: List<String>) {
        val onboardingModuleRequest = OnboardingModuleRequest()
        onboardingModuleRequest.selectedModule = selectedModule
        onboardingModuleRequest.selectedOptions = selectedOptions

        val call = apiService.onboardingModuleResult(
            sharedPreferenceManager.accessToken,
            onboardingModuleRequest
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    //Nothing to do here
                } else {
                    Toast.makeText(
                        this@WellnessFocusListActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }

        })
    }
}