package com.jetsynthesys.rightlife.ui.new_design

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.Status
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.BuildConfig
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.LoginResponseMobile
import com.jetsynthesys.rightlife.apimodel.SignupOtpRequest
import com.jetsynthesys.rightlife.apimodel.SubmitLoginOtpRequest
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.databinding.ActivityMobileLoginBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetDeleteSettingBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.new_design.pojo.GoogleLoginTokenResponse
import com.jetsynthesys.rightlife.ui.new_design.pojo.LoggedInUser
import com.jetsynthesys.rightlife.ui.profile_new.pojo.OtpRequest
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.AppSignatureHelper
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MobileLoginActivity : BaseActivity() {

    private lateinit var colorStateListSelected: ColorStateList
    private lateinit var colorStateListNonSelected: ColorStateList
    // Layouts
    private lateinit var layoutPhoneEntry: LinearLayout
    private lateinit var layoutOtp: LinearLayout

    // Phone entry
    private lateinit var ivBack: ImageView
    private lateinit var etPhone: EditText
    private lateinit var btnGetOtp: Button
    private lateinit var tvPhoneError: TextView

    // OTP views
    private lateinit var etOtp1: EditText
    private lateinit var etOtp2: EditText
    private lateinit var etOtp3: EditText
    private lateinit var etOtp4: EditText
    private lateinit var etOtp5: EditText
    private lateinit var etOtp6: EditText
    private lateinit var btnVerifyOtp: Button
    private lateinit var tvOtpError: TextView
    private lateinit var tvResend: TextView
    private lateinit var tvResendCounter: TextView
    private lateinit var tvOtpSubtitle: TextView

    // Binding variable
    private lateinit var binding: ActivityMobileLoginBinding

    // OTP / timer
    private var timer: CountDownTimer? = null
    private var failedOtpAttempts = 0
    private var lockedUntil: Long = 0L
    private val otpLength = 6

    // State
    private var phoneNumber: String = ""
    private var isNewUser = false

    // SMS Consent Launcher
    private val smsConsentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            message?.let {
                val otp = Regex("\\b(\\d{6})\\b").find(it)?.value
                if (!otp.isNullOrEmpty()) {
                    fillOtpFromSms(otp)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobileLoginBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        startSmsListener()
        // ADD THIS
        val appSignatureHelper = AppSignatureHelper(this)
        val hashes = appSignatureHelper.getAppSignatures()
        Log.d(TAG, "=== Your App Hash ===")
        hashes.forEach { hash ->
            Log.d(TAG, "App Hash: $hash")
        }
        Log.d(TAG, "SMS Hash: s11OcVsGU+c")
        Log.d(TAG, "Hashes Match: ${hashes.contains("s11OcVsGU+c")}")

        colorStateListSelected = ContextCompat.getColorStateList(this, R.color.menuselected)!!
        colorStateListNonSelected = ContextCompat.getColorStateList(this, R.color.rightlife)!!
        initViews()
        setupPhoneValidation()
        setupListeners()
        setupOtpInputs()

        AnalyticsLogger.logEvent(
                AnalyticsEvent.LOGIN_SCREEN_VISIT,
                mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
        )
    }

    // -------------------------------------------------------------------
    // INIT
    // -------------------------------------------------------------------

    private fun initViews() {
        layoutPhoneEntry = findViewById(R.id.layoutPhoneEntry)
        layoutOtp = findViewById(R.id.layoutOtpScreen)

        ivBack = findViewById(R.id.ivBack)
        etPhone = findViewById(R.id.etPhone)
        btnGetOtp = findViewById(R.id.btnGetOtp)
        tvPhoneError = findViewById(R.id.tvValidationError)

        etOtp1 = findViewById(R.id.et_otp1)
        etOtp2 = findViewById(R.id.et_otp2)
        etOtp3 = findViewById(R.id.et_otp3)
        etOtp4 = findViewById(R.id.et_otp4)
        etOtp5 = findViewById(R.id.et_otp5)
        etOtp6 = findViewById(R.id.et_otp6)

        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        tvOtpError = findViewById(R.id.tvOtpError)
        tvResend = findViewById(R.id.tvResend)
        tvResendCounter = findViewById(R.id.tvResendCounter)
        tvOtpSubtitle = findViewById(R.id.tvOtpSubtitle)

        layoutOtp.visibility = View.GONE
        //btnGetOtp.isEnabled = false
        //btnVerifyOtp.isEnabled = false
        tvPhoneError.visibility = View.INVISIBLE
        tvOtpError.visibility = View.GONE
        binding.tvValidationError.visibility = View.GONE
    }

    // -------------------------------------------------------------------
    // PHONE ENTRY
    // -------------------------------------------------------------------

    private fun setupPhoneValidation() {
        etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val valid = s?.length == 10
                //btnGetOtp.isEnabled = valid
                if (valid) {
                    binding.tvValidationError.visibility = View.GONE
                    tvPhoneError.visibility = View.INVISIBLE
                    btnGetOtp.isEnabled = true

                    binding.btnGetOtp.backgroundTintList =
                            if (valid) colorStateListSelected else colorStateListNonSelected
                }else{
                    binding.tvValidationError.visibility = View.VISIBLE
                    binding.tvValidationError.text = "Enter a valid phone number."
                    btnGetOtp.isEnabled = false
                }
                binding.btnGetOtp.backgroundTintList =
                        if (valid) colorStateListSelected else colorStateListNonSelected
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupListeners() {
        ivBack.setOnClickListener {
            if (layoutOtp.visibility == View.VISIBLE) {
                showPhoneEntry()
            } else {
                finish()
            }
        }

        btnGetOtp.setOnClickListener {
            phoneNumber = etPhone.text.toString().trim()

            if (phoneNumber.length != 10) {
                tvPhoneError.visibility = View.VISIBLE
                tvPhoneError.text = "Enter a valid 10-digit phone number"
                return@setOnClickListener
            }

          /*  AnalyticsLogger.logEvent(
                    AnalyticsEvent.MOBILE_LOGIN_INITIATED,
                    mapOf(
                            AnalyticsParam.PHONE_NUMBER to phoneNumber,
                            AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                    )
            )*/

            //requestOtp(phoneNumber)
            fetchDeviceInfo(phoneNumber)
            //generateOtp("+91$phoneNumber")
            binding.tvMobileNumber.text = phoneNumber
        }




        btnVerifyOtp.setOnClickListener {
            val otp = getEnteredOtp()
            if (otp.length == otpLength) {
                verifyOtp(phoneNumber, otp)
            }
        }

        tvResend.setOnClickListener {
            if (tvResend.isClickable && tvResend.text.toString().startsWith("Resend")) {
               /* AnalyticsLogger.logEvent(
                        AnalyticsEvent.RESEND_OTP_CLICK,
                        mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
                )*/
                requestOtp(phoneNumber)
            }
        }

        binding.changeNumber.setOnClickListener {
            showPhoneEntry()
        }
    }

    private fun showPhoneEntry() {
        layoutPhoneEntry.visibility = View.VISIBLE
        layoutOtp.visibility = View.GONE
        tvOtpError.visibility = View.GONE
        clearOtpBoxes()
        timer?.cancel()
        tvResendCounter.visibility = View.GONE
    }

    // -------------------------------------------------------------------
    // OTP INPUT HANDLING (6 BOXES)
    // -------------------------------------------------------------------

    private fun setupOtpInputs() {
        val boxes = listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6)

        boxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (index < boxes.size - 1) {
                            boxes[index + 1].requestFocus()
                        } else {
                            editText.clearFocus()
                            hideKeyboard(editText)
                        }
                    }
                    validateOtpReady()
                    tvOtpError.visibility = View.GONE
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        boxes[index - 1].requestFocus()
                        boxes[index - 1].setText("")
                    }
                }
                false
            }
        }
    }

    private fun clearOtpBoxes() {
        etOtp1.text.clear()
        etOtp2.text.clear()
        etOtp3.text.clear()
        etOtp4.text.clear()
        etOtp5.text.clear()
        etOtp6.text.clear()
        btnVerifyOtp.isEnabled = false
    }

    private fun getEnteredOtp(): String {
        return etOtp1.text.toString() +
                etOtp2.text.toString() +
                etOtp3.text.toString() +
                etOtp4.text.toString() +
                etOtp5.text.toString() +
                etOtp6.text.toString()
    }

    private fun validateOtpReady() {
        btnVerifyOtp.isEnabled = getEnteredOtp().length == otpLength
        binding.btnVerifyOtp.backgroundTintList =
                if (getEnteredOtp().length == otpLength) colorStateListSelected else colorStateListNonSelected
    }

    private fun fillOtpFromSms(otp: String) {
        if (otp.length == otpLength) {
            etOtp1.setText(otp[0].toString())
            etOtp2.setText(otp[1].toString())
            etOtp3.setText(otp[2].toString())
            etOtp4.setText(otp[3].toString())
            etOtp5.setText(otp[4].toString())
            etOtp6.setText(otp[5].toString())
            validateOtpReady()
        }
    }

    // -------------------------------------------------------------------
    // OTP FLOW (REQUEST / VERIFY) - API INTEGRATION
    // -------------------------------------------------------------------

    private fun requestOtp(phone: String) {
        if (phone.isBlank()) return

        failedOtpAttempts = 0
        lockedUntil = 0L
        tvOtpError.visibility = View.GONE

        // Show loading
        //btnGetOtp.isEnabled = false
        //btnGetOtp.text = "Sending..."

        val signupOtpRequest = SignupOtpRequest("+91$phone")

        val call = apiService.generateOtpSignup(signupOtpRequest)

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
            ) {
                btnGetOtp.isEnabled = true
                btnGetOtp.text = "Get OTP"

                if (response.isSuccessful && response.body() != null) {
                    try {
                        val responseString = response.body()?.string()
                        Log.d(TAG, "OTP Request Response: $responseString")

                        val jsonObject = JSONObject(responseString ?: "{}")
                        val statusCode = jsonObject.optInt("statusCode", 0)

                        if (statusCode == 200) {
                            Utils.showNewDesignToast(this@MobileLoginActivity, "OTP sent successfully", true)

                            showOtpScreen()

                            startTimer()

                          /*  AnalyticsLogger.logEvent(
                                    AnalyticsEvent.OTP_SENT_SUCCESS,
                                    mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
                            )*/
                        }else if (statusCode == 429) {
                            tvOtpError.text = "Too many attempts. Try again later."
                            tvOtpError.visibility = View.VISIBLE
                        } else if (statusCode == 400) {
                            tvOtpError.text = "Incorrect OTP"
                            tvOtpError.visibility = View.VISIBLE
                        }else {
                            val displayMessage = jsonObject.optString("displayMessage", "Failed to send OTP")
                            tvPhoneError.text = displayMessage
                            tvPhoneError.visibility = View.VISIBLE
                            Utils.showNewDesignToast(this@MobileLoginActivity, displayMessage, false)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing OTP response", e)
                        tvPhoneError.text = "Failed to send OTP. Please try again."
                        tvPhoneError.visibility = View.VISIBLE
                        Utils.showNewDesignToast(this@MobileLoginActivity, "Failed to send OTP", false)
                    }
                } else {
                    tvPhoneError.text = "Failed to send OTP. Please try again."
                    tvPhoneError.visibility = View.VISIBLE
                    Utils.showNewDesignToast(this@MobileLoginActivity, "Failed to send OTP", false)
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable)
            {
                btnGetOtp.isEnabled = true
                btnGetOtp.text = "Get OTP"
                tvPhoneError.text = "Network error. Please check your connection."
                tvPhoneError.visibility = View.VISIBLE
                handleNoInternetView(t)
            }

        })
    }

    private fun showOtpScreen() {
        layoutPhoneEntry.visibility = View.GONE
        layoutOtp.visibility = View.VISIBLE
        clearOtpBoxes()
        tvOtpError.visibility = View.GONE

        tvOtpSubtitle.text = "Please enter the 6 digit code sent to"
        etOtp1.requestFocus()
    }

    private fun verifyOtp(phone: String, otp: String) {
      /*  if (System.currentTimeMillis() < lockedUntil) {
            tvOtpError.text = "Too many attempts. Try again later."
            tvOtpError.visibility = View.VISIBLE
            return
        }*/

        // Show loading
        btnVerifyOtp.isEnabled = false
        btnVerifyOtp.text = "Verifying..."

        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        val deviceId = Utils.getDeviceId(this)

        val submitLoginOtpRequest = SubmitLoginOtpRequest(
                "+91$phone",
                otp,
                deviceId,
                deviceName,
                "android",
                "dummytokenfortest"
        )

        val call = apiService.submitOtpLogin(submitLoginOtpRequest)

        call.enqueue(object : Callback<GoogleLoginTokenResponse> {
            override fun onResponse(
                    call: Call<GoogleLoginTokenResponse>,
                    response: Response<GoogleLoginTokenResponse>
            ) {
                btnVerifyOtp.isEnabled = true
                btnVerifyOtp.text = "Verify"

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!

                    // Print JSON for debugging
                    val gson = Gson()
                    val jsonResponse = gson.toJson(apiResponse)
                    Log.d(TAG, "Login Response JSON: $jsonResponse")

                    Utils.showNewDesignToast(this@MobileLoginActivity, "Verification Successful", true)

                    // Save access token
                    val accessToken = apiResponse.accessToken
                    if (!accessToken.isNullOrEmpty()) {
                        sharedPreferenceManager.saveAccessToken(accessToken)
                        saveAccessToken(accessToken)
                    }

                    // Check if new user (adjust based on actual response field)
                     isNewUser = apiResponse.isNewUser ?: false

                 /*   AnalyticsLogger.logEvent(
                            AnalyticsEvent.OTP_VERIFY_SUCCESS,
                            mapOf(
                                    AnalyticsParam.USER_TYPE to if (isNewUser) "New User" else "Returning User",
                                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                            )
                    )*/

                    handleSuccessfulVerification(phone)
                } else {
                    failedOtpAttempts++
                    tvOtpError.text = "Incorrect OTP"
                    tvOtpError.visibility = View.VISIBLE

                    /*if (failedOtpAttempts >= 5) {
                        lockedUntil = System.currentTimeMillis() + (15 * 60 * 1000)
                        tvOtpError.text = "Too many attempts. Try again later."
                    }*/

                    Utils.showNewDesignToast(this@MobileLoginActivity, "Incorrect OTP", false)
                }
            }

            override fun onFailure(call: Call<GoogleLoginTokenResponse?>, t: Throwable)
            {
                btnVerifyOtp.isEnabled = true
                btnVerifyOtp.text = "Verify"
                tvOtpError.text = "Network error. Please try again."
                tvOtpError.visibility = View.VISIBLE
                handleNoInternetView(t)
            }

        })
    }

    private fun handleSuccessfulVerification(phone: String) {
        Handler(Looper.getMainLooper()).postDelayed({
                                                        val email = "+91$phone" // Using phone as identifier
                                                        var loggedInUser: LoggedInUser? = null

                                                        for (user in sharedPreferenceManager.loggedUserList) {
                                                            if (email == user.email) {
                                                                loggedInUser = user
                                                            }
                                                        }

                                                        if (!isNewUser || loggedInUser?.isOnboardingComplete == true) {
                                                            // Existing user - go to main screen
                                                            val loggedInUser = LoggedInUser(email = email, isOnboardingComplete = true)
                                                            sharedPreferenceManager.setLoggedInUsers(arrayListOf(loggedInUser))
                                                            ActivityUtils.startRightLifeContextScreenActivity(this@MobileLoginActivity)
                                                        } else {
                                                            // New user - go to username creation
                                                            val intent = Intent(this@MobileLoginActivity, CreateUsernameActivity::class.java).apply {
                                                                putExtra("USERNAME_KEY", phone)
                                                                putExtra("EMAIL", email)
                                                            }

                                                            val loggedInUsers = sharedPreferenceManager.loggedUserList
                                                            loggedInUsers.add(LoggedInUser(email = email))
                                                            sharedPreferenceManager.setLoggedInUsers(loggedInUsers)
                                                            sharedPreferenceManager.email = email

                                                            startActivity(intent)
                                                        }
                                                        finishAffinity()
                                                    }, 1000)
    }

    private fun saveAccessToken(accessToken: String) {
        SharedPreferenceManager.getInstance(this).saveAccessToken(accessToken)
        val sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(SharedPreferenceConstants.ACCESS_TOKEN, accessToken)
        editor.putBoolean(SharedPreferenceConstants.IS_LOGGED_IN, true)
        editor.apply()
        getUserDetails()
    }

    private fun getUserDetails() {
        val call = apiService.getUserDetais(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    val responseObj = gson.fromJson(jsonResponse, UserProfileResponse::class.java)

                    sharedPreferenceManager.saveUserId(responseObj.userdata.id)
                    sharedPreferenceManager.saveUserProfile(responseObj)
                    sharedPreferenceManager.setAIReportGeneratedView(responseObj.reportView)

                    var productId = ""
                    sharedPreferenceManager.userProfile?.subscription?.forEach { subscription ->
                        if (subscription.status) {
                            productId = subscription.productId
                        }
                    }

                    AnalyticsLogger.logEvent(
                            AnalyticsEvent.USER_LOGIN,
                            mapOf(
                                    AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                                    AnalyticsParam.USER_TYPE to if (isNewUser) "New User" else "Returning User",
                                    AnalyticsParam.USER_PLAN to productId,
                                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                            )
                    )
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    // -------------------------------------------------------------------
    // TIMER (RESEND OTP)
    // -------------------------------------------------------------------

    private fun startTimer() {
        timer?.cancel()
        tvResendCounter.visibility = View.VISIBLE

        timer = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                tvResendCounter.text = "(${sec}s)"
                tvResendCounter.setTextColor(ContextCompat.getColor(this@MobileLoginActivity, R.color.menuselected))
                tvResend.text = "Resend code in"
                tvResend.isClickable = false
            }

            override fun onFinish() {
                tvResend.text = "Resend OTP"
                tvResend.setTextColor(ContextCompat.getColor(this@MobileLoginActivity, R.color.black))
                tvResend.isClickable = true
                tvResendCounter.visibility = View.GONE
            }
        }.start()
    }

    // -------------------------------------------------------------------
    // SMS RETRIEVER (AUTO FILL OTP)
    // -------------------------------------------------------------------

    private fun startSmsListener() {
        val client = SmsRetriever.getClient(this)
        client.startSmsUserConsent(null)

        ContextCompat.registerReceiver(
                this,
                smsReceiver,
                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
                ContextCompat.RECEIVER_EXPORTED
        )
    }

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive called with action=${intent?.action}")
            if (SmsRetriever.SMS_RETRIEVED_ACTION != intent?.action) return

            val extras = intent.extras ?: return
            val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status ?: return
            Log.d(TAG, "SmsRetriever status: ${status.statusCode}")

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                    try {
                        consentIntent?.let { smsConsentLauncher.launch(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting SMS consent activity", e)
                    }
                }

                CommonStatusCodes.TIMEOUT -> {
                    Log.e(TAG, "SMS Retriever timeout")
                }
            }
        }
    }

    // -------------------------------------------------------------------
    // UTIL
    // -------------------------------------------------------------------

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        try {
            unregisterReceiver(smsReceiver)
        } catch (_: Exception) {
            // Receiver was not registered or already unregistered
        }
    }

    companion object {
        private const val TAG = "MobileLoginActivity"
    }

    fun generateOtp(mobileNumber: String) {
        val call = apiService.generateOtpForPhoneNumber(
                sharedPreferenceManager.accessToken,
                OtpRequest(mobileNumber)
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    showCustomToast("OTP sent to your mobile number", true)

                } else {
                    showCustomToast("Retry too early")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    // first check device id if already used
    private fun fetchDeviceInfo(phoneNumber: String) {
        var deviceId = Utils.getDeviceId(this@MobileLoginActivity)
        val call = apiService.getDeviceInfoMobile(deviceId,"+91$phoneNumber")

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val responseString = response.body()?.string()

                    try {
                        val jsonObject = JSONObject(responseString)

                        val statusCode = jsonObject.optInt("statusCode")
                        val displayMessage = jsonObject.optString("displayMessage")

                        if (statusCode == 200) {
                            requestOtp(phoneNumber)
                        } else if (statusCode == 500) {
                            handleApiError(jsonObject)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                                this@MobileLoginActivity,
                                "Parsing Error",
                                Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    // Handle HTTP Errors (like 500 Internal Server Error)
                    val responseCode = response.code()

                    if (responseCode == 500) {
                        /*  Toast.makeText(
                              this@ImageSliderActivity,
                              "Server Error: 500 - Internal Server Error",
                              Toast.LENGTH_SHORT
                          ).show()*/
                        showSwitchBottomSheet()
                    } else {
                        // Try to parse errorBody if present
                        val errorBodyString = response.errorBody()?.string()
                        if (!errorBodyString.isNullOrEmpty()) {
                            try {
                                val jsonObject = JSONObject(errorBodyString)
                                handleApiError(jsonObject)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                        this@MobileLoginActivity,
                                        "Error Code: $responseCode",
                                        Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                    this@MobileLoginActivity,
                                    "Error Code: $responseCode - No Error Body",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun handleApiError(jsonObject: JSONObject) {
        val statusCode = jsonObject.optInt("statusCode")
        val displayMessage = jsonObject.optString("displayMessage")
        val errorCode = jsonObject.optString("errorCode")
        val errorMessage = jsonObject.optString("errorMessage")

        Log.e("API_ERROR", "StatusCode: $statusCode, ErrorCode: $errorCode, Message: $errorMessage")

        when (errorCode) {
            "DEVICE_ALREADY_EXISTS" -> {
                Toast.makeText(
                        this@MobileLoginActivity,
                        "Device already registered!",
                        Toast.LENGTH_SHORT
                ).show()
                // You can navigate or perform a specific action here if needed
                showSwitchBottomSheet()
            }

            else -> {
                Toast.makeText(this@MobileLoginActivity, displayMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSwitchBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogBinding = BottomsheetDeleteSettingBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                    AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.tvTitle.text = "You're Logged In with a Different Account"
        dialogBinding.tvDescription.text = "This device is already logged in with a different account. As a result, free services are not available. Please log out and sign in with your original account to access free features."

        dialogBinding.ivDialogClose.setImageResource(R.drawable.close_breathwork)

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnYes.text = "Switch Account"
        dialogBinding.btnCancel.text = "OK"

        dialogBinding.btnCancel.setOnClickListener {
            requestOtp(phoneNumber)
            bottomSheetDialog.dismiss()
        }
        /*dialogBinding.btnYes.setOnClickListener {
            bottomSheetDialog.dismiss()
        }*/
        dialogBinding.btnYes.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}