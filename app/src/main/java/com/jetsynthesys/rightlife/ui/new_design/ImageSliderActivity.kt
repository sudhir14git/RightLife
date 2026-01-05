package com.jetsynthesys.rightlife.ui.new_design

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.viewpager2.widget.ViewPager2
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.BuildConfig
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.databinding.BottomsheetSwitchAccountBinding
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.drawermenu.PrivacyPolicyActivity
import com.jetsynthesys.rightlife.ui.drawermenu.TermsAndConditionsActivity
import com.jetsynthesys.rightlife.ui.new_design.pojo.GoogleLoginTokenResponse
import com.jetsynthesys.rightlife.ui.new_design.pojo.GoogleSignInRequest
import com.jetsynthesys.rightlife.ui.new_design.pojo.LoggedInUser
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.MetaEventLogger
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.zhpan.bannerview.indicator.DrawableIndicator
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageSliderActivity : BaseActivity() {

    private val RC_SIGN_IN = 9001
    private val TAG = "Googlelogin"

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val timeDurationForImageSlider = 2000L
    private lateinit var displayName: String
    private lateinit var mEmail: String
    private var isNewUser = false
    private var accessTokenGoogle = ""

    // List of images (replace with your own images)
    private val images = listOf(
        R.mipmap.mask_group69,
        R.mipmap.mask_group70,
        R.mipmap.mask_group71,
        R.mipmap.mask_group72
    )

    private val headers = listOf(
        "Build a Healthier You, \nOne Habit at a Time",
        "Meet Your Smart Health Companion",
        "Wellness That Fits Your Life",
        "Backed by Science, Not Fads"
    )

    private val descriptions = listOf(
        "Four foundations: mind, body, nutrition, and rest - working together for lifelong wellbeing.",
        "AI that listens to your body, predicts risks, and helps you build better habits.",
        "From stretching at sunrise to meditating at midnight, RightLife adapts to you and guides your progress.",
        "No influencer fluff. Just personalized content backed by evidence you can trust."
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_image_slider)
        val logger = AppEventsLogger.newLogger(this)
        logger.logEvent("my_debug_test_event")
        logger.flush()

        // Initialize the ViewPager2 and TabLayout
        viewPager = findViewById(R.id.viewPager_image_slider)
        tabLayout = findViewById(R.id.tabLayout)

        viewPager.adapter = ImageSliderAdapter(this, images, headers, descriptions)

        AnalyticsLogger.logEvent(
            AnalyticsEvent.LOGIN_SCREEN_VISIT,
            mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
        )

        clickForPrivacyPolicyAndTermsOfService()

        // Set up the TabLayoutMediator to sync dots with the images
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
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


        val selectedColor = ContextCompat.getColor(this@ImageSliderActivity, R.color.menuselected)
        val unselectedColor = ContextCompat.getColor(this@ImageSliderActivity, R.color.gray)
        val indicatorView = findViewById<DrawableIndicator>(R.id.indicator_view_pager1)
        val dp20 = resources.getDimensionPixelOffset(R.dimen.textsize_small)

        indicatorView.apply {
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
            setupWithViewPager(viewPager)
        }


        // Set up the auto-slide functionality
        handler = Handler(mainLooper)
        runnable = Runnable {
            val currentItem = viewPager.currentItem
            val nextItem = if (currentItem < images.size - 1) currentItem + 1 else 0
            viewPager.setCurrentItem(nextItem, true)
            handler.postDelayed(runnable, timeDurationForImageSlider)
        }

        // Auto-slide images every 2 seconds


        // Update the dots on page change
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDotColors(position)
            }
        })


        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            //.requestIdToken("376715991698-8lavu418dl8lgr5on0o0dg3au47gg36c.apps.googleusercontent.com")
//            .requestIdToken("376715991698-1o4qmabjng7lp9umkcjkb8i6fsu8he5l.apps.googleusercontent.com")
//            .requestServerAuthCode("376715991698-1o4qmabjng7lp9umkcjkb8i6fsu8he5l.apps.googleusercontent.com")
            //production
            //.requestIdToken("341535838422-d0tnfv0vm1gbuea70gc9o6etavbs0t9p.apps.googleusercontent.com")
            //.requestServerAuthCode("341535838422-d0tnfv0vm1gbuea70gc9o6etavbs0t9p.apps.googleusercontent.com")

            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID) // Use your actual client ID from build config
            .requestServerAuthCode(BuildConfig.GOOGLE_WEB_CLIENT_ID) // Use your actual client ID from build config
            .requestEmail()
            .requestProfile()
            .requestScopes(
                Scope("https://www.googleapis.com/auth/userinfo.email"),
                Scope("https://www.googleapis.com/auth/userinfo.profile"),
                Scope("openid")
            )
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Sign out the user
        googleSignInClient.signOut()
            .addOnCompleteListener(this) {
                // Update your UI to reflect the sign-out


            }
        val btnGoogle = findViewById<TextView>(R.id.btn_google)
        btnGoogle.setOnClickListener {
//            startActivity(Intent(this, CreateUsernameActivity::class.java))
//            finish()

            AnalyticsLogger.logEvent(
                AnalyticsEvent.CONTINUE_WITH_GOOGLE_CLICK,
                mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
            )

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        val btnMobile = findViewById<TextView>(R.id.btn_mobile)
        btnMobile.setOnClickListener {
            AnalyticsLogger.logEvent(
                AnalyticsEvent.CONTINUE_WITH_PHONE_NUMBER,
                mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
            )
            startActivity(Intent(this, MobileLoginActivity::class.java))
        }


    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, timeDurationForImageSlider)

        //AppEventsLogger.activateApp(this)


        val logger = AppEventsLogger.newLogger(this)

        // Optional: one app-start test event
        val params = Bundle().apply {
            putString("source", "app_start")
            putString("build_type", if (BuildConfig.DEBUG) "debug" else "release")
        }
        logger.logEvent("rl_app_start_test", params)
        logger.logEvent(
            "test_event_code", bundleOf(
                "test_param" to "debug_value",
                "test_event_code" to "TEST86160",
                "app_version" to BuildConfig.VERSION_NAME
            )
        )

        logger.logEvent("this_is_some_event")
        logger.logEvent("TEST86160")
        logger.flush()

        Log.d("MetaSDK", "Sent rl_app_start_test")
    }

    override fun onPause() {
        super.onPause()
        // Stop the auto-slide when the activity is paused
        handler.removeCallbacks(runnable)
    }

    private fun updateDotColors(position: Int) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val dot = tab?.customView as? ImageView
            if (i == position) {
                dot?.setImageResource(R.drawable.dot_selected) // Active dot image
            } else {
                dot?.setImageResource(R.drawable.dot) // Inactive dot image
            }
        }
    }


    // handle googel signin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    val email = it.email
                    val scope = "oauth2:https://www.googleapis.com/auth/userinfo.profile"

                    // Use a coroutine to fetch the access token
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Retrieve the access token
                            accessTokenGoogle =
                                GoogleAuthUtil.getToken(this@ImageSliderActivity, email!!, scope)
                            Log.d("AccessToken", "Access Token: $accessTokenGoogle")

                            // Use the access token for API requests

                            if (account != null) {
                                // User is signed in, display user information
                                displayName = account.displayName!!
                                val firstName = displayName.split(" ").get(0)
                                displayName = firstName
                                mEmail = account.email.toString()
                                val authcode = account.serverAuthCode
                            }

                            fetchDeviceInfo(
                                Utils.getDeviceId(this@ImageSliderActivity),
                                mEmail,
                                accessTokenGoogle
                            )
                        } catch (e: Exception) {
                            Log.e("GoogleAuthUtil", "Error retrieving access token", e)
                            Utils.showNewDesignToast(
                                this@ImageSliderActivity,
                                "Verification Failed",
                                false
                            )
                        }
                    }
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Sign-in failed", e)
            }

            //  handleSignInResult(task)
        }
    }

    private fun fetchApiData(accessTokenGoogle: String) {
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        val deviceId = Utils.getDeviceId(this)
        println("Device ID: $deviceId")
        val googleSignInRequest = GoogleSignInRequest(
            accessTokenGoogle,
            deviceId,
            deviceName,
            "dummytokenfortest"
        )
        submitAnswer(googleSignInRequest)
    }

    /*private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }*/

    /*private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            // User is signed in, display user information
            val displayName = account.displayName
            val email = account.email
            val authcode = account.serverAuthCode
            // Update your UI with user information
            Log.d(TAG, "User  signed in: $displayName, $email,$authcode")
            val intent = Intent(this, CreateUsernameActivity::class.java)
            intent.putExtra("USERNAME_KEY", displayName) // Add the username as an extra
            //startActivity(intent)
            //finish()
        } else {
            // User is not signed in, show sign-in button
            Log.d(TAG, "User  not signed in")
        }
    }*/


    private fun submitAnswer(googleSignInRequest: GoogleSignInRequest) {

        val call = apiService.submitGoogleLogin("android", googleSignInRequest)

        call.enqueue(object : Callback<GoogleLoginTokenResponse> {
            override fun onResponse(
                call: Call<GoogleLoginTokenResponse>,
                response: Response<GoogleLoginTokenResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Utils.showNewDesignToast(
                        this@ImageSliderActivity,
                        "Verification Successful",
                        true
                    )
                    val apiResponse = response.body()
                    SharedPreferenceManager.getInstance(this@ImageSliderActivity)
                        .saveAccessToken(apiResponse?.accessToken)
                    saveAccessToken(apiResponse?.accessToken!!)
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Send username to next Activity
                        var loggedInUser: LoggedInUser? = null
                        for (user in sharedPreferenceManager.loggedUserList) {
                            if (mEmail == user.email) {
                                loggedInUser = user
                            }
                        }
                        isNewUser = apiResponse.isNewUser ?: false
                        if (apiResponse.isNewUser == false || loggedInUser?.isOnboardingComplete == true) {

                            val loggedInUser =
                                LoggedInUser(email = mEmail, isOnboardingComplete = true)
                            sharedPreferenceManager.setLoggedInUsers(arrayListOf(loggedInUser))
                            ActivityUtils.startRightLifeContextScreenActivity(this@ImageSliderActivity)
                        } else {
                            val intent =
                                Intent(
                                    this@ImageSliderActivity,
                                    CreateUsernameActivity::class.java
                                ).apply {
                                    putExtra(
                                        "USERNAME_KEY",
                                        displayName
                                    ) // Add the username as an extra
                                    putExtra("EMAIL", mEmail)
                                }
                            val loggedInUsers = sharedPreferenceManager.loggedUserList
                            loggedInUsers.add(LoggedInUser(email = mEmail))
                            sharedPreferenceManager.setLoggedInUsers(loggedInUsers)
                            sharedPreferenceManager.email = mEmail
                            sharedPreferenceManager.displayName = displayName
                            startActivity(intent)
                        }
                        finishAffinity()

                    }, 1000)

                } else {
                    Toast.makeText(
                        this@ImageSliderActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<GoogleLoginTokenResponse>, t: Throwable) {
                handleNoInternetView(t)
            }

        })
    }


    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun saveAccessToken(accessToken: String) {
        SharedPreferenceManager.getInstance(this).saveAccessToken(accessToken)
        val sharedPreferences =
            getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(SharedPreferenceConstants.ACCESS_TOKEN, accessToken)
        editor.putBoolean(SharedPreferenceConstants.IS_LOGGED_IN, true)
        editor.apply()
        getUserDetails()
    }

    private fun getUserDetails() {
        // Make the API call
        val call = apiService.getUserDetais(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val ResponseObj = gson.fromJson(
                        jsonResponse, UserProfileResponse::class.java
                    )
                    sharedPreferenceManager.saveUserId(ResponseObj.userdata.id)
                    sharedPreferenceManager
                        .saveUserProfile(ResponseObj)

                    sharedPreferenceManager
                        .setAIReportGeneratedView(ResponseObj.reportView)
                }

                //AnalyticsLogger.logEvent(this@ImageSliderActivity, AnalyticsEvent.USER_LOGIN)
                var productId = ""
                sharedPreferenceManager.userProfile?.subscription?.forEach { subscription ->
                    if (subscription.status) {
                        productId = subscription.productId
                    }
                }
                //AnalyticsParam.USER_PLAN to productId,  no need to pass it now
                AnalyticsLogger.logEvent(
                    AnalyticsEvent.USER_LOGIN, mapOf(
                        AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                        AnalyticsParam.USER_TYPE to if (isNewUser) "New User" else "Returning User",
                        AnalyticsParam.USER_PLAN to if (sharedPreferenceManager.userProfile?.isSubscribed == true) "Premium" else "free User",
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                    )
                )
                // ✅ Meta
                MetaEventLogger.log(
                        this@ImageSliderActivity,
                        AnalyticsEvent.USER_LOGIN,
                        mapOf(
                                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                                AnalyticsParam.USER_TYPE to if (isNewUser) "New User" else "Returning User",
                                AnalyticsParam.USER_PLAN to if (sharedPreferenceManager.userProfile?.isSubscribed == true) "Premium" else "free User",
                                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                        )
                )
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    // API to check device id

    private fun fetchDeviceInfo(deviceId: String, emailId: String, accessTokenGoogle: String) {
        val call = apiService.getDeviceInfo(deviceId, emailId)

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
                            fetchApiData(accessTokenGoogle)
                        } else if (statusCode == 500) {
                            handleApiError(jsonObject)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@ImageSliderActivity,
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
                                    this@ImageSliderActivity,
                                    "Error Code: $responseCode",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@ImageSliderActivity,
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
                    this@ImageSliderActivity,
                    "Device already registered!",
                    Toast.LENGTH_SHORT
                ).show()
                // You can navigate or perform a specific action here if needed
                showSwitchBottomSheet()
            }

            else -> {
                Toast.makeText(this@ImageSliderActivity, displayMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSwitchBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogBinding = BottomsheetSwitchAccountBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.tvTitle.text = "You're Logged In with a Different Account"
        dialogBinding.tvDescription.text =
            "This device is already logged in with a different account. As a result, free services are not available. \n\nPlease log out and sign in with your original account to access free features."

        dialogBinding.ivDialogClose.setImageResource(R.drawable.close_breathwork)

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnYes.text = "Skip"
        dialogBinding.btnYes.setTextColor(ContextCompat.getColor(this, R.color.menuselected))
        dialogBinding.btnCancel.text = "Switch Account"

        dialogBinding.btnYes.setOnClickListener {
            fetchApiData(accessTokenGoogle)
            bottomSheetDialog.dismiss()
        }
        /*dialogBinding.btnYes.setOnClickListener {
            bottomSheetDialog.dismiss()
        }*/
        dialogBinding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()

            // Force a full logout and new chooser
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestServerAuthCode(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestProfile()
                .requestScopes(
                    Scope("https://www.googleapis.com/auth/userinfo.email"),
                    Scope("https://www.googleapis.com/auth/userinfo.profile"),
                    Scope("openid")
                )
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this@ImageSliderActivity, gso)

            // Step 1: Sign out
            googleSignInClient.signOut().addOnCompleteListener {
                // Step 2: Revoke access to forget the previous account
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    // Step 3: Launch new sign-in chooser
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun clickForPrivacyPolicyAndTermsOfService() {
        val textView = findViewById<TextView>(R.id.textView)
        val fullText = "By signing up, I agree to RightLife’s Terms of Service & Privacy Policy."

        val spannableString = SpannableString(fullText)

// Define clickable ranges
        val termsStart = fullText.indexOf("Terms of Service")
        val termsEnd = termsStart + "Terms of Service".length

        val privacyStart = fullText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

// ClickableSpan for Terms of Service
        val termsClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(
                    this@ImageSliderActivity,
                    TermsAndConditionsActivity::class.java
                )
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#808080") // link color (blue)
            }
        }

// ClickableSpan for Privacy Policy
        val privacyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(
                    this@ImageSliderActivity,
                    PrivacyPolicyActivity::class.java
                )
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#808080")
            }
        }

// Apply spans
        spannableString.setSpan(
            termsClickable,
            termsStart,
            termsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            privacyClickable,
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT

    }
}