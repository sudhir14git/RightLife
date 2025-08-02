package com.jetsynthesys.rightlife.ui.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityPreparingExportHealthDataBinding
import com.jetsynthesys.rightlife.ui.CommonResponse
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PreparingExportHealthDataActivity : BaseActivity() {

    private lateinit var binding: ActivityPreparingExportHealthDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreparingExportHealthDataBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.ivClose.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        AnalyticsLogger.logEvent(
            AnalyticsEvent.DATA_EXPORT_REQUESTED,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.USER_TYPE to if (sharedPreferenceManager.userProfile.isSubscribed) "Paid User" else "free User",
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.DATA_EXPORT_REQUESTED to true
            )
        )

        exportHealthData()
    }

    private fun exportHealthData() {

        apiService.exportHealthData(sharedPreferenceManager.accessToken)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(
                    call: Call<CommonResponse>,
                    response: Response<CommonResponse>
                ) {
                    if (response.isSuccessful) {
                        binding.llExportMain.visibility = View.GONE
                        binding.llSuccess.visibility = View.VISIBLE
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish()
                        }, 3000)
                    } else {
                        showToast(response.message())
                    }
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    handleNoInternetView(t)
                }

            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
