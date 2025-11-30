package com.jetsynthesys.rightlife.ui.sdkpackage

import ai.nuralogix.anurasdk.camera.CameraCapability
import ai.nuralogix.anurasdk.camera.CameraInfo
import ai.nuralogix.anurasdk.core.entity.MeasurementQuestionnaire
import ai.nuralogix.anurasdk.error.AnuraError
import ai.nuralogix.anurasdk.utils.AnuLogUtil
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.BuildConfig
import com.jetsynthesys.rightlife.databinding.ActivityHealthcamRecorderBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamFacialScanRequest
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamReportIdResponse
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.healthcam.ReportData
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.Utils
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.system.exitProcess

class HealthCamRecorderActivity : BaseActivity() {

    private lateinit var binding: ActivityHealthcamRecorderBinding
    private val exampleStartViewModel: StartViewModel by viewModels { StartViewModel.Factory }

    private var faceIndex = 0

    companion object {
        const val TAG = "ANURA_MainActivity"
        const val MY_PERMISSIONS_REQUEST = 1
        const val ANURA_REQUEST = 100

        var DFX_REST_URL = BuildConfig.DFX_REST_URL
        var DFX_WS_URL = BuildConfig.DFX_WS_URL
        var LICENSE_KEY = BuildConfig.DFX_LICENSE_KEY
        var STUDY_ID = BuildConfig.DFX_STUDY_ID

        var USER_PROFILE_HEIGHT = "162"
        var USER_PROFILE_WEIGHT = "62"
        var USER_PROFILE_AGE = "36"
        var USER_PROFILE_GENDER = "male"
        var PARTNER_ID = "AndroidTest"
        var REPORT_ID = "6777ff3f24e05ea22043ebf9"
        var measurementQuestionnaire = MeasurementQuestionnaire()

        const val REST_SERVER_KEY = "rest_server_key"
        const val WS_SERVER_KEY = "ws_server_key"
        const val STUDY_ID_KEY = "study_id_key"
        const val PARTNER_ID_KEY = "partner_id_key"
        const val USER_PROFILE_HEIGHT_KEY = "user_profile_height_key"
        const val USER_PROFILE_WEIGHT_KEY = "user_profile_weight_key"
        const val USER_PROFILE_GENDER_KEY = "user_profile_gender_key"
        const val USER_PROFILE_AGE_KEY = "user_profile_age_key"

        const val PREF_NAME = "nura_sample_config"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthcamRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        REPORT_ID = intent.getStringExtra("reportID").toString()
        USER_PROFILE_HEIGHT = intent.getStringExtra("USER_PROFILE_HEIGHT").toString()
        USER_PROFILE_WEIGHT = intent.getStringExtra("USER_PROFILE_WEIGHT").toString()
        USER_PROFILE_AGE = intent.getStringExtra("USER_PROFILE_AGE").toString()
        USER_PROFILE_GENDER = intent.getStringExtra("USER_PROFILE_GENDER").toString()




        exampleStartViewModel.readyToMeasure.observe(this) { handleTokenVerified(it) }
        exampleStartViewModel.error.observe(this) { handleError(it) }

        initialize()
        if (requestPermission()) {
            registerScan()
        }
        AnalyticsLogger.logEvent(
            AnalyticsEvent.FaceScan_Cam_PageOpen,
            mapOf("timestamp" to System.currentTimeMillis())
        )
    }


    private fun handleTokenVerified(isTokenVerified: Boolean) {
        Log.d(TAG, "=============>handleTokenVerified")
        if (isTokenVerified) {
            startScan()
        }
    }


    private fun startScan() {
        saveConfig()
        val anuraSupportedCameras: List<CameraInfo> =
            CameraCapability.createCameraCapabilityInstance(baseContext).getAnuraSupportedCameras(
                CameraInfo.CAMERA_CHECK_MIN_PIXEL_FLAG
            )
        if (anuraSupportedCameras.isEmpty()) {
            Toast.makeText(baseContext, "Camera does not support", Toast.LENGTH_LONG).show()
            return
        }
        faceIndex = 2
        if (USER_PROFILE_HEIGHT.isNotBlank()
            || USER_PROFILE_WEIGHT.isNotBlank()
            || USER_PROFILE_AGE.isNotBlank()
            || USER_PROFILE_GENDER.isNotBlank()
        ) {
            validateInputs(measurementQuestionnaire)
        } else {
            Utils.showLoader(this)
            launchRLAnuraMeasurementActivity()
        }
    }

    private fun saveConfig() {
        val pref = getSharedPreferences(PREF_NAME, 0)
        val editor = pref.edit()
        editor.putString(REST_SERVER_KEY, DFX_REST_URL)
        editor.putString(WS_SERVER_KEY, DFX_WS_URL)
        editor.putString(LICENSE_KEY, LICENSE_KEY)
        editor.putString(STUDY_ID_KEY, STUDY_ID)
        editor.putString(USER_PROFILE_HEIGHT_KEY, USER_PROFILE_HEIGHT)
        editor.putString(USER_PROFILE_WEIGHT_KEY, USER_PROFILE_WEIGHT)
        editor.putString(USER_PROFILE_AGE_KEY, USER_PROFILE_AGE)
        editor.putString(USER_PROFILE_GENDER_KEY, USER_PROFILE_GENDER)
        editor.putString(PARTNER_ID_KEY, PARTNER_ID)
        editor.apply()
    }

    /**
     * Generic method to handle errors. Your application is responsible for gracefully handling
     * errors and explaining what's happening to your end users
     */
    private fun handleError(errorMsg: String?) {
        Toast.makeText(this, "Error:$errorMsg", Toast.LENGTH_SHORT).show()
//        returnErrorResult("Error:$errorMsg")
    }


    private fun initialize() {
        SharedPreferencesHelper.initialize(application)
        AnuLogUtil.setShowLog(BuildConfig.DEBUG)
    }

    private fun requestPermission(): Boolean {
        val permissionList = ArrayList<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(Manifest.permission.CAMERA)
        }
        val permissionStrings = arrayOfNulls<String>(permissionList.size)
        permissionList.toArray(permissionStrings)
        return if (permissionList.size > 0) {
            ActivityCompat.requestPermissions(this, permissionStrings, MY_PERMISSIONS_REQUEST)
            false
        } else {
            AnuLogUtil.d(TAG, "have all the required permissions")
            true
        }
    }

    private fun registerScan() {
        /**
         * Check if the application has been configured with a DeepAffex License Key and Study ID
         */
        if (checkEmbeddedDeepAffexLicenseAndStudyID()) {
            lifecycleScope.launch {
                /**
                 * Before launching cam, we need to ensure that the
                 * application has a valid DeepAffex Cloud access token. The application also needs
                 * to ensure it has the latest study configuration binary that's required to
                 * initialize DeepAffex Extraction Library
                 */
                exampleStartViewModel.verifyDeepAffexTokenAndStudyFile()
            }
        } else {
            /**
             * If either the DeepAffex License Key or Study ID are not configured, show an error
             * dialog box and exit the app
             */
            showExitAppDialog(
                "App Configuration Error",
                "Your DFX_LICENSE_KEY and DFX_STUDY_ID are not set in server.properties"
            )
        }
    }

    private fun checkEmbeddedDeepAffexLicenseAndStudyID(): Boolean {
        return !(BuildConfig.DFX_LICENSE_KEY.isEmpty() || BuildConfig.DFX_STUDY_ID.isEmpty())
    }

    private fun showExitAppDialog(title: String, msg: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(msg)
            .setNegativeButton("Exit")
            { _, _ -> exitProcess(0) }
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && requestCode == MY_PERMISSIONS_REQUEST
        ) {
            registerScan()
        } else {
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    /**
     * Use the [MeasurementQuestionnaire] to validate and persist the user
     *  profile information
     */
    private fun validateInputs(
        measurementQuestionnaire: MeasurementQuestionnaire
    ) {
        var invalidInputs = ""

        if (USER_PROFILE_HEIGHT.isBlank()
            || (USER_PROFILE_HEIGHT.isNotBlank() && measurementQuestionnaire.setHeightInCm(
                USER_PROFILE_HEIGHT.toDouble().toInt()
            ) == AnuraError.Core.INVALID_INPUT)
        ) {
            invalidInputs = invalidInputs.plus("|Height|")
        }
        if (USER_PROFILE_WEIGHT.isBlank() || (USER_PROFILE_WEIGHT.isNotBlank() && measurementQuestionnaire.setWeightInKg(
                USER_PROFILE_WEIGHT.toDouble().toInt()
            ) == AnuraError.Core.INVALID_INPUT)
        ) {
            invalidInputs = invalidInputs.plus("|Weight|")
        }
        if (USER_PROFILE_AGE.isBlank() || (USER_PROFILE_AGE.isNotBlank() && measurementQuestionnaire.setAge(
                USER_PROFILE_AGE.toInt()
            ) == AnuraError.Core.INVALID_INPUT)
        ) {
            invalidInputs = invalidInputs.plus("|Age|")
        }
        if (measurementQuestionnaire.setSexAssignedAtBirth(
                USER_PROFILE_GENDER
            ) == AnuraError.Core.INVALID_INPUT
        ) {
            invalidInputs = invalidInputs.plus("|Sex At Birth|")
        }

        if (invalidInputs.isNotBlank()) {
            showInvalidInputErrorToast(invalidInputs)
        } else {
            Utils.showLoader(this)
            launchRLAnuraMeasurementActivity()
        }
    }

    private fun launchRLAnuraMeasurementActivity() {
        val intent = Intent(this@HealthCamRecorderActivity, RlAnuraMeasurementActivity::class.java)
        startActivityForResult(intent, ANURA_REQUEST)
    }

    private fun showInvalidInputErrorToast(inputWithError: String) {
        Toast.makeText(this, "Invalid Inputs: $inputWithError", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        binding.tvNotify.text = "Please wait!! Generating Result"
        if (resultCode == Activity.RESULT_OK && requestCode == ANURA_REQUEST) {
            val healthCamFacialScanRequest = HealthCamFacialScanRequest()
            healthCamFacialScanRequest.status = "SUCCESS"
            healthCamFacialScanRequest.reportId = REPORT_ID

            val reportData1 = data?.getSerializableExtra("resultObject") as ReportData
            healthCamFacialScanRequest.reportData = reportData1

            submitFacialScan(healthCamFacialScanRequest)

        } else {
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun submitReport(healthCamFacialScanRequest: HealthCamFacialScanRequest) {

        val call = apiService.submitHealthCamReport(
            sharedPreferenceManager.accessToken,
            healthCamFacialScanRequest
        )
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Utils.dismissLoader(this@HealthCamRecorderActivity)
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body()?.string())
                    CommonAPICall.postWellnessStreak(this@HealthCamRecorderActivity, "faceScan")
                    val intent = Intent(
                        this@HealthCamRecorderActivity,
                        NewHealthCamReportActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("FROM", "RECORDER")
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Utils.dismissLoader(this@HealthCamRecorderActivity)
                    Toast.makeText(
                        this@HealthCamRecorderActivity,
                        "Server Error: " + response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun submitFacialScan(healthCamFacialScanRequest: HealthCamFacialScanRequest) {
        Utils.showLoader(this)
        val call = apiService.getHealthCamByReportId(
            sharedPreferenceManager.accessToken,
            healthCamFacialScanRequest.reportId
        )

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Utils.dismissLoader(this@HealthCamRecorderActivity)
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse: String? = null
                    try {
                        val reportIdResponse = gson.fromJson(
                            response.body()!!.string(),
                            HealthCamReportIdResponse::class.java
                        )

                        healthCamFacialScanRequest.reportId = reportIdResponse.data?.id
                        submitReport(healthCamFacialScanRequest)

                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                    Log.d("AAAA API Response", "ReportId submit - : $jsonResponse")
                } else {
                    Toast.makeText(
                        this@HealthCamRecorderActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Utils.dismissLoader(this@HealthCamRecorderActivity)
                handleNoInternetView(t)
            }
        })
    }

}