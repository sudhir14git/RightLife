package com.example.rlapp.ui.new_design

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.rlapp.R
import com.example.rlapp.RetrofitData.ApiClient
import com.example.rlapp.RetrofitData.ApiService
import com.example.rlapp.ui.HomeActivity
import com.example.rlapp.ui.new_design.pojo.GoogleLoginTokenResponse
import com.example.rlapp.ui.new_design.pojo.GoogleSignInRequest
import com.example.rlapp.ui.new_design.pojo.LoggedInUser
import com.example.rlapp.ui.utility.SharedPreferenceConstants
import com.example.rlapp.ui.utility.SharedPreferenceManager
import com.example.rlapp.ui.utility.Utils
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zhpan.bannerview.indicator.DrawableIndicator
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ImageSliderActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 9001
    private val TAG = "Googlelogin"

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val timeDurationForImageSlider = 2000L
    private lateinit var displayName: String
    private lateinit var mEmail: String
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    // List of images (replace with your own images)
    private val images = listOf(
        R.mipmap.mask_group69,
        R.mipmap.mask_group70,
        R.mipmap.mask_group71,
        R.mipmap.mask_group72
    )

    private val headers = listOf(
        "Four Foundations, One Powerful App",
        "AI-Powered Personal Health Guide",
        "Wellness, Anytime, Anywhere",
        "No Social Media Fads. Just Science"
    )

    private val descriptions = listOf(
        "The only app you need to optimize your mind, body,\n nutrition, and sleep, all from your smartphone.",
        "Predict risks, get personalized insights, and transform\n your health with data-driven recommendations.",
        "Live classes, meditation, sleep sounds, nutrition\n tracking, tailored just for you, accessible 24/7.",
        "Forget the gimmicks—our information is backed by\n evidence and tailored to your unique health goals."
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_slider)

        // Initialize the ViewPager2 and TabLayout
        viewPager = findViewById(R.id.viewPager_image_slider)
        tabLayout = findViewById(R.id.tabLayout)

        viewPager.adapter = ImageSliderAdapter(this, images, headers, descriptions)

        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

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
            .requestIdToken("376715991698-djfb8tj4brdsv6h7c5cpvr5pavt3nbns.apps.googleusercontent.com")
            .requestServerAuthCode("376715991698-djfb8tj4brdsv6h7c5cpvr5pavt3nbns.apps.googleusercontent.com")
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

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, timeDurationForImageSlider)
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
                            val accessTokenGoogle =
                                GoogleAuthUtil.getToken(this@ImageSliderActivity, email!!, scope)
                            Log.d("AccessToken", "Access Token: $accessTokenGoogle")

                            // Use the access token for API requests

                            if (account != null) {
                                // User is signed in, display user information
                                displayName = account.displayName!!
                                mEmail = account.email.toString()
                                val authcode = account.serverAuthCode
                            }
                            fetchApiData(accessTokenGoogle)
                        } catch (e: Exception) {
                            Log.e("GoogleAuthUtil", "Error retrieving access token", e)
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
        val deviceId = Utils.getDeviceId(this)
        println("Device ID: $deviceId")
        val googleSignInRequest = GoogleSignInRequest(
            accessTokenGoogle,
            deviceId,
            "androidDevice",
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
        val apiService = ApiClient.getClient().create(ApiService::class.java)

        val call = apiService.submitGoogleLogin("android", googleSignInRequest)

        call.enqueue(object : Callback<GoogleLoginTokenResponse> {
            override fun onResponse(
                call: Call<GoogleLoginTokenResponse>,
                response: Response<GoogleLoginTokenResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
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
                        if (apiResponse.isNewUser == false || loggedInUser?.isOnboardingComplete == true) {
                            val loggedInUser = LoggedInUser(email = mEmail, isOnboardingComplete = true)
                            sharedPreferenceManager.setLoggedInUsers(arrayListOf(loggedInUser))
                            startActivity(
                                Intent(
                                    this@ImageSliderActivity,
                                    HomeActivity::class.java
                                )
                            )
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
                Toast.makeText(
                    this@ImageSliderActivity,
                    "Network Error: " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
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
    }
}