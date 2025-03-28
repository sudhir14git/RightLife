package com.example.rlapp.newdashboard

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rlapp.R
import com.example.rlapp.RetrofitData.ApiClient
import com.example.rlapp.RetrofitData.ApiService
import com.example.rlapp.apimodel.userdata.UserProfileResponse
import com.example.rlapp.databinding.ActivityHomeDashboardBinding
import com.example.rlapp.newdashboard.NewHomeFragment.HomeFragment
import com.example.rlapp.ui.HomeActivity
import com.example.rlapp.ui.profile_new.ProfileSettingsActivity
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.utility.SharedPreferenceConstants
import com.example.rlapp.ui.utility.SharedPreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeDashboardBinding
    private var isAdd = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment
        loadFragment(HomeFragment())

        //set report list dummy for demo
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val heartRateList = mutableListOf<HeartRateData>()
        for (i in 1..52) {
            heartRateList.add(
                HeartRateData(
                    heartRate = (60..120).random(),  // Random heart rate
                    date = "Week $i",
                    //trendData = (60..120).map { it.toString() }.take(7) as ArrayList<String>
                    trendData = ArrayList(List(7) { (60..120).random().toString() })  // U
                )
            )
        }

        recyclerView.adapter = HeartRateAdapter(
            heartRateList,
            this
        )

        // Handle menu item clicks
        binding.menuHome.setOnClickListener {
            //loadFragment(HomeFragment())
            updateMenuSelection(R.id.menu_home)
        }

        binding.menuExplore.setOnClickListener {
            //loadFragment(ExploreFragment())
            startActivity(Intent(this, HomeActivity::class.java))
            updateMenuSelection(R.id.menu_explore)
        }

        // Set initial selection
        updateMenuSelection(R.id.menu_home)

        // Handle FAB click
        binding.fab.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.white)
        binding.fab.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
        binding.fab.setOnClickListener {
            /*val bottomSheetFragment = BottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)*/
            binding.fab.animate().rotationBy(180f).setDuration(60).setInterpolator(DecelerateInterpolator()).withEndAction {
                // Change icon after rotation
                if (isAdd) {
                    binding.fab.setImageResource(R.drawable.icon_quicklink_plus_black)  // Change to close icon
                    binding.fab.backgroundTintList = ContextCompat.getColorStateList(this, R.color.rightlife)
                    binding.fab.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
                } else {
                    binding.fab.setImageResource(R.drawable.icon_quicklink_plus)    // Change back to add icon
                    binding.fab.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
                    binding.fab.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.rightlife))
                }
                isAdd = !isAdd  // Toggle the state
            }.start()
        }
        // Api calls
        getUserDetails("")

        binding.progressBarOnboarding.post {
            val progressPercentage = binding.progressBarOnboarding.progress / binding.progressBarOnboarding.max.toFloat()
            val progressWidth = binding.progressBarOnboarding.width+80
            val thumbX = (progressPercentage) * progressWidth - binding.progressThumb.width / 2
            binding.progressThumb.translationX = thumbX
            binding.tvWeightlossZone.translationX = thumbX
        }

        // click listners for checklist
        binding.includeChecklist.rlChecklistEatright.setOnClickListener {
            Toast.makeText(this, "Eat Right", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, QuestionnaireEatRightActivity::class.java))
        }
        binding.includeChecklist.rlChecklistSleepright.setOnClickListener {
            Toast.makeText(this, "Think Right", Toast.LENGTH_SHORT).show()
           // startActivity(Intent(this, QuestionnaireThinkRightActivity::class.java))
        }
        binding.includeChecklist.rlChecklistSynchealth.setOnClickListener {
            //Toast.makeText(this, "Sync Health", Toast.LENGTH_SHORT).show()
        }
        binding.includeChecklist.rlChecklistProfile.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }
        binding.includeChecklist.rlChecklistSnapmeal.setOnClickListener {
            Toast.makeText(this, "Snap Meal", Toast.LENGTH_SHORT).show()
        }
        binding.includeChecklist.rlChecklistFacescan.setOnClickListener {
            Toast.makeText(this, "Face Scan", Toast.LENGTH_SHORT).show()
        }
    binding.profileImage.setOnClickListener {

        /*if (!drawer.isDrawerOpen(Gravity.LEFT)) drawer.openDrawer(Gravity.LEFT);
            else drawer.closeDrawer(Gravity.RIGHT);*/
        startActivity(Intent(this@HomeDashboardActivity, ProfileSettingsActivity::class.java))
    }

        binding.cardMoverightMain.setOnClickListener{
            //startActivity(Intent(this@HomeDashboardActivity, MoveRightActivity::class.java))
            Toast.makeText(this, "MoveRight AI Dashboard", Toast.LENGTH_SHORT).show()
        }
        binding.cardEatright.setOnClickListener{
            //startActivity(Intent(this@HomeDashboardActivity, MoveRightActivity::class.java))
            Toast.makeText(this, "EatRight AI Dashboard", Toast.LENGTH_SHORT).show()
        }
        binding.cardSleepright.setOnClickListener{
            //startActivity(Intent(this@HomeDashboardActivity, MoveRightActivity::class.java))
            Toast.makeText(this, "SleepRight AI Dashboard", Toast.LENGTH_SHORT).show()
        }
        binding.cardThinkright.setOnClickListener{
            //startActivity(Intent(this@HomeDashboardActivity, MoveRightActivity::class.java))
            Toast.makeText(this, "ThinkRight AI Dashboard", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateMenuSelection(selectedMenuId: Int) {
        // Reset all icons and labels
        binding.iconHome.setImageResource(R.drawable.new_home_unselected_svg) // Unselected icon
        binding.iconExplore.setImageResource(R.drawable.new_explore_unselected_svg) // Unselected icon
        binding.labelHome.setTextColor(ContextCompat.getColor(this, R.color.gray))
        binding.labelExplore.setTextColor(ContextCompat.getColor(this, R.color.gray))
        binding.labelHome.setTypeface(null, Typeface.NORMAL) // Reset to normal font
        binding.labelExplore.setTypeface(null, Typeface.NORMAL) // Reset to normal font

        // Highlight selected icon and label
        when (selectedMenuId) {
            R.id.menu_home -> {
                binding.iconHome.setImageResource(R.drawable.new_home_selected_svg) // Selected icon
                binding.labelHome.setTextColor(ContextCompat.getColor(this, R.color.rightlife))
                binding.labelHome.setTypeface(null, Typeface.BOLD) // Make text bold
            }
            R.id.menu_explore -> {
                binding.iconExplore.setImageResource(R.drawable.new_explore_selected_svg) // Selected icon
                binding.labelExplore.setTextColor(ContextCompat.getColor(this, R.color.rightlife))
                binding.labelExplore.setTypeface(null, Typeface.BOLD) // Make text bold
            }
        }
    }


    //APIs

    // get user details
    private fun getUserDetails(s: String) {
        //-----------
        val sharedPreferences =
            getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, MODE_PRIVATE)
        val accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null)

        val apiService = ApiClient.getClient().create(ApiService::class.java)

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        val call = apiService.getUserDetais(accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val promotionResponse2 = response.body()
                    Log.d("API Response", "User Details: " + promotionResponse2.toString())
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val ResponseObj = gson.fromJson(
                        jsonResponse,
                        UserProfileResponse::class.java
                    )
                    Log.d("API Response body", "Success: User Details" + ResponseObj.userdata.id)
                    SharedPreferenceManager.getInstance(applicationContext)
                        .saveUserId(ResponseObj.userdata.id)
                    SharedPreferenceManager.getInstance(applicationContext)
                        .saveUserProfile(ResponseObj)


                    if (ResponseObj.userdata.profilePicture != null) {
                        Glide.with(this@HomeDashboardActivity)
                            .load(ApiClient.CDN_URL_QA + ResponseObj.userdata.profilePicture)
                            .placeholder(R.drawable.profile_man).error(R.drawable.profile_man)
                            .into(binding.profileImage)
                    }
                    binding.userName.setText(ResponseObj.userdata.firstName)


                    Log.d(
                        "UserID", "USerID: User Details" + SharedPreferenceManager.getInstance(
                            applicationContext
                        ).userId
                    )
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                Toast.makeText(this@HomeDashboardActivity, "Network Error: " + t.message, Toast.LENGTH_SHORT)
                    .show()
                Log.e("API ERROR", "onFailure: " + t.message)
                t.printStackTrace() // Print the full stack trace for more details
            }
        })
    }
}