package com.jetsynthesys.rightlife.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.databinding.ActivityGeneralInformationBinding
import com.jetsynthesys.rightlife.ui.settings.adapter.SettingsAdapter
import com.jetsynthesys.rightlife.ui.settings.pojo.GeneralInformationResponse
import com.jetsynthesys.rightlife.ui.settings.pojo.SettingItem
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GeneralInformationActivity : BaseActivity() {

    private lateinit var binding: ActivityGeneralInformationBinding
    private lateinit var settingsAdapter: SettingsAdapter
    private var generalInformationResponse: GeneralInformationResponse? = null
    private lateinit var whichInfo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneralInformationBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)
        whichInfo = intent.getStringExtra("INFO") ?: ""


        getGeneralInfo()

        setupGeneralInformationRecyclerView()

        //back button
        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupGeneralInformationRecyclerView() {
        val settingsItems = listOf(
            SettingItem("About Us"),
            SettingItem("Terms & Conditions"),
            SettingItem("Privacy Policy")
        )

        settingsAdapter = SettingsAdapter(settingsItems) { item ->
            startNextActivity(item.title)
        }

        binding.rvGeneralInformation.apply {
            layoutManager = LinearLayoutManager(this@GeneralInformationActivity)
            adapter = settingsAdapter
        }
    }

    private fun startNextActivity(title: String) {
        val intent = Intent(this, HtmlTextActivity::class.java)
        when (title) {
            "About Us" -> {
                intent.putExtra("GeneralInformation", generalInformationResponse?.data?.aboutus)
                intent.putExtra("title","About Us")
            }

            "Terms & Conditions" -> {
                intent.putExtra(
                    "GeneralInformation",
                    generalInformationResponse?.data?.termsConditions
                )
                intent.putExtra("title","Terms & Conditions")
            }

            "Privacy Policy" -> {
                intent.putExtra(
                    "GeneralInformation",
                    generalInformationResponse?.data?.privacyPolicy
                )
                intent.putExtra("title","Privacy Policy")
            }
        }
        startActivity(intent)
    }

    private fun getGeneralInfo() {
        Utils.showLoader(this)
        val call = apiService.getGeneralInformation()

        call.enqueue(object : Callback<GeneralInformationResponse> {
            override fun onResponse(
                call: Call<GeneralInformationResponse>,
                response: Response<GeneralInformationResponse>
            ) {
                Utils.dismissLoader(this@GeneralInformationActivity)
                if (response.isSuccessful && response.body() != null) {
                    generalInformationResponse = response.body()
                    if (whichInfo.isNotEmpty()) {
                        finish()
                        startNextActivity(whichInfo)
                    }
                } else {
                    showToast("Server Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<GeneralInformationResponse>, t: Throwable) {
                Utils.dismissLoader(this@GeneralInformationActivity)
                showToast("Network Error: " + t.message)
            }

        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}