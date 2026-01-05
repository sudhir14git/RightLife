package com.jetsynthesys.rightlife.newdashboard

import PromotionWeeklyResponse
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.affirmations.AffirmationResponse
import com.jetsynthesys.rightlife.apimodel.rledit.RightLifeEditResponse
import com.jetsynthesys.rightlife.apimodel.servicepane.HomeService
import com.jetsynthesys.rightlife.apimodel.servicepane.ServicePaneResponse
import com.jetsynthesys.rightlife.apimodel.submodule.SubModuleResponse
import com.jetsynthesys.rightlife.apimodel.welnessresponse.ContentWellness
import com.jetsynthesys.rightlife.apimodel.welnessresponse.WellnessApiResponse
import com.jetsynthesys.rightlife.databinding.FragmentHomeExploreBinding
import com.jetsynthesys.rightlife.newdashboard.model.ContentDetails
import com.jetsynthesys.rightlife.newdashboard.model.ContentResponse
import com.jetsynthesys.rightlife.runWhenAttached
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.ActivityUtils.startFaceScanActivity
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity
import com.jetsynthesys.rightlife.ui.CardItem
import com.jetsynthesys.rightlife.ui.CircularCardAdapter
import com.jetsynthesys.rightlife.ui.NewCategoryListActivity
import com.jetsynthesys.rightlife.ui.ServicePaneAdapter
import com.jetsynthesys.rightlife.ui.TestAdapter
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.SeriesListActivity
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditFromActivity
import com.jetsynthesys.rightlife.ui.utility.FeatureFlags
import com.jetsynthesys.rightlife.ui.utility.NetworkUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.voicescan.VoiceScanActivity
import com.zhpan.bannerview.constants.PageStyle
import com.zhpan.indicator.enums.IndicatorStyle
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.math.abs


class HomeExploreFragment : BaseFragment() {
    private var _binding: FragmentHomeExploreBinding? = null
    private val binding get() = _binding!!

    private var sliderHandler: Handler? = null // Handler for scheduling auto-slide
    private var sliderRunnable: Runnable? = null
    private val cardItems: ArrayList<CardItem> = ArrayList()
    private var adapter: CircularCardAdapter? = null

    var rightLifeEditResponse: RightLifeEditResponse? = null
    var wellnessApiResponse: WellnessApiResponse? = null
    var ThinkRSubModuleResponse: SubModuleResponse? = null
    var MoveRSubModuleResponse: SubModuleResponse? = null
    var EatRSubModuleResponse: SubModuleResponse? = null
    var SleepRSubModuleResponse: SubModuleResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //(requireActivity() as? HomeNewActivity)?.showHeader(true)

        // Initialize Handler and Runnable for Auto-Sliding
        sliderHandler = Handler(Looper.getMainLooper())
        sliderRunnable = object : Runnable {
            override fun run() {
                val nextItem: Int = binding.viewPager.currentItem + 1 // Move to the next item
                binding.viewPager.setCurrentItem(nextItem, true)
                sliderHandler?.removeCallbacks(sliderRunnable!!)
                sliderHandler?.postDelayed(this, 5000) // Change slide every 3 seconds
            }
        }

        val swipeRefreshLayout = activity?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        swipeRefreshLayout?.setOnRefreshListener {
            // Call your refresh logic
            sliderHandler?.postDelayed(sliderRunnable!!, 3000)
            getPromotionListWeekly()
            //getPromotionList()
            getRightLifeEdit()
            getWellnessPlaylist()
            swipeRefreshLayout.isRefreshing = false // Stop the spinner
        }

        callAPIS()

        sliderHandler?.removeCallbacks(sliderRunnable!!)

        // Start Auto-Sliding
        sliderHandler?.postDelayed(sliderRunnable!!, 5000)

        /*binding.refreshLayout.setOnRefreshListener {
            callAPIS()
            binding.refreshLayout.isRefreshing = false
        }

        binding.scrollView.setOnScrollChangeListener { view: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            binding.refreshLayout.setEnabled(
                scrollY <= 5
            )
        }*/

        setClickListeners()
    }

    private fun setClickListeners() {
        binding.imgJumpBackInNext.setOnClickListener {
            startActivity(Intent(requireContext(), JumpInBackActivity::class.java))
        }
        binding.relativeRledit3.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                callRlEditDetailActivity(2)
            } else showInternetError()
        }
        binding.relativeRledit2.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                callRlEditDetailActivity(1)
            } else showInternetError()
        }
        binding.relativeRledit1.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                callRlEditDetailActivity(0)
            } else showInternetError()
        }

        binding.relativeWellness1.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                callWellnessDetailActivity(0)
            } else showInternetError()
        }
        binding.relativeWellness2.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                callWellnessDetailActivity(1)
            } else showInternetError()
        }
        binding.relativeWellness3.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                callWellnessDetailActivity(2)
            } else showInternetError()
        }
        binding.relativeWellness4.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                callWellnessDetailActivity(3)
            } else showInternetError()
        }

        // set click listener
        binding.llThinkrightCategory1.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                if (ThinkRSubModuleResponse?.data?.isNotEmpty() == true) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        ThinkRSubModuleResponse?.data?.get(0)?.categoryId
                    )
                    intent.putExtra("moduleId", ThinkRSubModuleResponse?.data?.get(0)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()

        }
        binding.llThinkrightCategory2.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                if (ThinkRSubModuleResponse?.data?.size!! > 1) {
                    ThinkRSubModuleResponse?.data?.get(1)?.name
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        ThinkRSubModuleResponse?.data?.get(1)?.categoryId
                    )
                    intent.putExtra("moduleId", ThinkRSubModuleResponse?.data?.get(1)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }
        binding.llThinkrightCategory3.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                if (ThinkRSubModuleResponse?.data?.size!! > 2) {
                    ThinkRSubModuleResponse?.data?.get(2)?.name
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        ThinkRSubModuleResponse?.data?.get(2)?.categoryId
                    )
                    intent.putExtra("moduleId", ThinkRSubModuleResponse?.data?.get(2)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }
        binding.llThinkrightCategory4.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                if (ThinkRSubModuleResponse?.data?.size!! > 3) {
                    ThinkRSubModuleResponse?.data?.get(3)?.name
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        ThinkRSubModuleResponse?.data?.get(3)?.categoryId
                    )
                    intent.putExtra("moduleId", ThinkRSubModuleResponse?.data?.get(3)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }

        binding.llMoverightCategory1.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                if (MoveRSubModuleResponse?.data?.isNotEmpty() == true) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        MoveRSubModuleResponse?.data?.get(0)?.categoryId
                    )
                    intent.putExtra("moduleId", MoveRSubModuleResponse?.data?.get(0)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }
        binding.llMoverightCategor2.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                if (MoveRSubModuleResponse?.data?.size!! > 2) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        MoveRSubModuleResponse?.data?.get(2)?.categoryId
                    )
                    intent.putExtra("moduleId", MoveRSubModuleResponse?.data?.get(2)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }
        binding.llMoverightCategory3.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                if (MoveRSubModuleResponse?.data?.size!! > 1) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        MoveRSubModuleResponse?.data?.get(1)?.categoryId
                    )
                    intent.putExtra("moduleId", MoveRSubModuleResponse?.data?.get(1)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }

        binding.llEatrightCategory1.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                if (EatRSubModuleResponse?.data?.isNotEmpty() == true) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra("Categorytype", EatRSubModuleResponse?.data?.get(0)?.categoryId)
                    intent.putExtra("moduleId", EatRSubModuleResponse?.data?.get(0)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }
        binding.llEatrightCategory2.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                if (EatRSubModuleResponse?.data?.size!! > 1) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra("Categorytype", EatRSubModuleResponse?.data?.get(1)?.categoryId)
                    intent.putExtra("moduleId", EatRSubModuleResponse?.data?.get(1)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }
        binding.llEatrightCategory3.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                if (EatRSubModuleResponse?.data?.size!! > 2) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra("Categorytype", EatRSubModuleResponse?.data?.get(2)?.categoryId)
                    intent.putExtra("moduleId", EatRSubModuleResponse?.data?.get(2)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }
        binding.llEatrightCategory4.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                if (EatRSubModuleResponse?.data?.size!! > 3) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra("Categorytype", EatRSubModuleResponse?.data?.get(3)?.categoryId)
                    intent.putExtra("moduleId", EatRSubModuleResponse?.data?.get(3)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }

        binding.llSleeprightCategory1.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                if (SleepRSubModuleResponse?.data?.isNotEmpty() == true) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        SleepRSubModuleResponse?.data?.get(0)?.categoryId
                    )
                    intent.putExtra("moduleId", SleepRSubModuleResponse?.data?.get(0)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }
        binding.llSleeprightCategory2.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                if (SleepRSubModuleResponse?.data?.size!! > 1) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        SleepRSubModuleResponse?.data?.get(1)?.categoryId
                    )
                    intent.putExtra("moduleId", SleepRSubModuleResponse?.data?.get(1)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }
        binding.llSleeprightCategory3.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                if (SleepRSubModuleResponse?.data?.size!! > 2) {
                    val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
                    intent.putExtra(
                        "Categorytype",
                        SleepRSubModuleResponse?.data?.get(2)?.categoryId
                    )
                    intent.putExtra("moduleId", SleepRSubModuleResponse?.data?.get(2)?.moduleId)
                    startActivity(intent)
                }
            } else showInternetError()
        }

        binding.btnSrExplore.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                callExploreModuleActivity(SleepRSubModuleResponse!!)
            } else showInternetError()
        }
        binding.btnTrExplore.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                callExploreModuleActivity(ThinkRSubModuleResponse!!)
            } else showInternetError()
        }
        binding.btnErExplore.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                callExploreModuleActivity(EatRSubModuleResponse!!)
            } else showInternetError()
        }
        binding.btnMrExplore.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {

                callExploreModuleActivity(MoveRSubModuleResponse!!)
            } else showInternetError()
        }

        binding.btnWellnessPreference.setOnClickListener {

        }
    }

    override fun onResume() {
        super.onResume()
        // Resume auto-slide when activity is visible
        sliderHandler?.postDelayed(sliderRunnable!!, 3000)
        //getPromotionList()
        getPromotionListWeekly()
        getRightLifeEdit()
        getWellnessPlaylist()
        Handler(Looper.getMainLooper()).postDelayed({
            getJumpBackInData()
        }, 2000)

        (requireActivity() as? HomeNewActivity)?.showChallengeCard()
    }

    private fun callAPIS() {
        getPromotionList2() // ModuleService pane
        getRightLifeEdit()

        //getAffirmations()

        getWellnessPlaylist()

        getModuleContent()

        getSubModuleContent("THINK_RIGHT")
        getSubModuleContent("MOVE_RIGHT")
        getSubModuleContent("EAT_RIGHT")
        getSubModuleContent("SLEEP_RIGHT")

        getJumpBackInData()
    }

    // get Affirmation list
    private fun getAffirmations() {
        val call = apiService.getAffirmationList(
            sharedPreferenceManager.accessToken,
            sharedPreferenceManager.userId,
            true
        )
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    response.body()
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val responseObj = gson.fromJson(
                        jsonResponse,
                        AffirmationResponse::class.java
                    )
                    runWhenAttached { setupAffirmationContent(responseObj) }
                } else {
                    //Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    /*private fun getPromotionList() {
        val call = apiService.getPromotionList(
            sharedPreferenceManager.accessToken,
            "HOME_PAGE",
            sharedPreferenceManager.userId,
            "TOP"
        )
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    val promotionResponse = gson.fromJson(
                        jsonResponse,
                        PromotionResponse::class.java
                    )
                    if (promotionResponse.success) {
                        runWhenAttached { handlePromotionResponse(promotionResponse) }
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "Failed: " + promotionResponse.statusCode,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }*/


    private fun getPromotionListWeekly() {
        val call = apiService.getPromotionListWeekly(
            sharedPreferenceManager.accessToken,
            "HOME_PAGE",
            "TOP",
            sharedPreferenceManager.userId
        )
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    val promotionResponse = gson.fromJson(
                        jsonResponse,
                        PromotionWeeklyResponse::class.java
                    )
                    if (promotionResponse.success) {
                        runWhenAttached { handlePromotionResponseWeekly(promotionResponse) }
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "Failed: " + promotionResponse.statusCode,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun getPromotionList2() {
        val call = apiService.getPromotionList2(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val responseObj = gson.fromJson(
                        jsonResponse,
                        ServicePaneResponse::class.java
                    )
                    runWhenAttached { handleServicePaneResponse(responseObj) }
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun getRightLifeEdit() {
        val call = apiService.getRightlifeEdit(sharedPreferenceManager.accessToken, "HOME")
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    gson.fromJson(
                        jsonResponse,
                        RightLifeEditResponse::class.java
                    )
                    rightLifeEditResponse = gson.fromJson(
                        jsonResponse,
                        RightLifeEditResponse::class.java
                    )
                    runWhenAttached { setupRLEditContent(rightLifeEditResponse) }
                } else {
                    val statusCode = response.code()
                    try {
                        val errorMessage = response.errorBody()!!.string()
                        Log.e(
                            "Error",
                            "HTTP error code: $statusCode, message: $errorMessage"
                        )
                        Log.e("Error", "Error message: $errorMessage")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun getWellnessPlaylist() {
        val call =
            apiService.getWelnessPlaylist(sharedPreferenceManager.accessToken, "SERIES", "WELLNESS")
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    wellnessApiResponse = gson.fromJson(
                        jsonResponse,
                        WellnessApiResponse::class.java
                    )
                    wellnessApiResponse?.data?.contentList?.let {
                        runWhenAttached {
                            setupWellnessContent(
                                it
                            )
                        }
                    }
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun getModuleContent() {
        val call = apiService.getmodule(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    // LiveEventResponse ResponseObj = gson.fromJson(jsonResponse,LiveEventResponse.class);
                    //Log.d("API Response body", "Success:AuthorName " + ResponseObj.getData().get(0).getAuthorName());
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    // ----- Test API
    private fun getSubModuleContent(moduleid: String) {
        val call =
            apiService.getsubmodule(sharedPreferenceManager.accessToken, moduleid, "CATEGORY")
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val affirmationsResponse = response.body()
                    val gson = Gson()

                    runWhenAttached {
                        if (moduleid.equals("THINK_RIGHT", ignoreCase = true)) {
                            ThinkRSubModuleResponse = gson.fromJson(
                                affirmationsResponse.toString(),
                                SubModuleResponse::class.java
                            )
                            handleThinkRightResponse()
                        } else if (moduleid.equals("MOVE_RIGHT", ignoreCase = true)) {
                            MoveRSubModuleResponse = gson.fromJson(
                                affirmationsResponse.toString(),
                                SubModuleResponse::class.java
                            )
                            handleMoveRightResponse()
                        } else if (moduleid.equals("EAT_RIGHT", ignoreCase = true)) {
                            EatRSubModuleResponse = gson.fromJson(
                                affirmationsResponse.toString(),
                                SubModuleResponse::class.java
                            )
                            handleEatRightResponse()
                        } else if (moduleid.equals("SLEEP_RIGHT", ignoreCase = true)) {
                            SleepRSubModuleResponse = gson.fromJson(
                                affirmationsResponse.toString(),
                                SubModuleResponse::class.java
                            )
                            handleSleepRightResponse()
                        }
                        (requireActivity() as? HomeNewActivity)?.isCategoryModuleLoaded = true
                    }
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })
    }

    private fun handleThinkRightResponse() {
        if (ThinkRSubModuleResponse?.data?.isNotEmpty() == true) {
            with(binding) {
                tvThinkRightCategory1.text = ThinkRSubModuleResponse?.data?.get(0)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + ThinkRSubModuleResponse?.data?.get(0)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageThinkRightCategory1)
            }
        }
        if (ThinkRSubModuleResponse?.data?.size!! > 1) {
            with(binding) {
                tvThinkRightCategory2.text = ThinkRSubModuleResponse?.data?.get(1)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + ThinkRSubModuleResponse?.data?.get(1)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageThinkRightCategory2)
            }
        }
        if (ThinkRSubModuleResponse?.data?.size!! > 2) {
            with(binding) {
                tvThinkRightCategory3.text = ThinkRSubModuleResponse?.data?.get(2)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + ThinkRSubModuleResponse?.data?.get(2)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageThinkRightCategory3)
            }
        }
        if (ThinkRSubModuleResponse?.data?.size!! > 3) {
            with(binding) {
                tvThinkRightCategory4.text = ThinkRSubModuleResponse?.data?.get(3)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + ThinkRSubModuleResponse?.data?.get(3)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageThinkRightCategory4)
            }
        }
    }

    private fun handleMoveRightResponse() {
        if (MoveRSubModuleResponse?.data?.isNotEmpty() == true) {
            with(binding) {
                tvMoveRightCategory1.text = MoveRSubModuleResponse?.data?.get(0)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + MoveRSubModuleResponse?.data?.get(0)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageMoveRightCategory1)
            }
        }
        if (MoveRSubModuleResponse?.data?.size!! > 1) {
            with(binding) {
                tvMoveRightCategory3.text = MoveRSubModuleResponse?.data?.get(1)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + MoveRSubModuleResponse?.data?.get(1)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageMoveRightCategory3)
            }
        }
        if (MoveRSubModuleResponse?.data?.size!! > 2) {
            with(binding) {
                tvMoveRightCategory2.text = MoveRSubModuleResponse?.data?.get(2)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + MoveRSubModuleResponse?.data?.get(2)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageMoveRightCategory2)
            }
        }
    }

    private fun handleEatRightResponse() {
        if (EatRSubModuleResponse?.data?.isNotEmpty() == true) {
            with(binding) {
                tvEatRightCategory1.text = EatRSubModuleResponse?.data?.get(0)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + EatRSubModuleResponse?.data?.get(0)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageEatRightCategory1)
            }
        }
        if (EatRSubModuleResponse?.data?.size!! > 1) {
            with(binding) {
                tvEatRightCategory2.text = EatRSubModuleResponse?.data?.get(1)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + EatRSubModuleResponse?.data?.get(1)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageEatRightCategory2)
            }
        }
        if (EatRSubModuleResponse?.data?.size!! > 2) {
            with(binding) {
                tvEatRightCategory3.text = EatRSubModuleResponse?.data?.get(2)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + EatRSubModuleResponse?.data?.get(2)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageEatRightCategory3)
            }
        }
        if (EatRSubModuleResponse?.data?.size!! > 3) {
            with(binding) {
                tvEatRightCategory4.text = EatRSubModuleResponse?.data?.get(3)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + EatRSubModuleResponse?.data?.get(3)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageEatRightCategory4)
            }
        }
    }

    private fun handleSleepRightResponse() {
        if (SleepRSubModuleResponse?.data?.isNotEmpty() == true) {
            with(binding) {
                tvSleepRightCategory1.text = SleepRSubModuleResponse?.data?.get(0)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + SleepRSubModuleResponse?.data?.get(0)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageSleepRightCategory1)
            }
        }
        if (SleepRSubModuleResponse?.data?.size!! > 1) {
            with(binding) {
                tvSleepRightCategory2.text = SleepRSubModuleResponse?.data?.get(1)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + SleepRSubModuleResponse?.data?.get(1)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageSleepRightCategory2)
            }
        }
        if (SleepRSubModuleResponse?.data?.size!! > 2) {
            with(binding) {
                tvSleepRightCategory3.text = SleepRSubModuleResponse?.data?.get(2)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + SleepRSubModuleResponse?.data?.get(2)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageSleepRightCategory3)
            }
        }
    }

    private fun setupAffirmationContent(responseObj: AffirmationResponse) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.menuselected)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.gray)
        // Set up the ViewPager
        if (responseObj.data.sortedServices.isNotEmpty()) {
            binding.bannerViewpager.visibility = View.VISIBLE
            val testAdapter = TestAdapter(responseObj.data.sortedServices)
            binding.bannerViewpager.setAdapter(testAdapter)
            binding.bannerViewpager.setScrollDuration(1000)
            binding.bannerViewpager.setPageStyle(PageStyle.MULTI_PAGE)
            binding.bannerViewpager.setIndicatorSliderGap(20) // Adjust spacing if needed
                .setIndicatorStyle(IndicatorStyle.ROUND_RECT)
                .setIndicatorHeight(20) // Adjust height for a pill-like shape
                .setIndicatorSliderWidth(20, 50) // Unselected: 10px, Selected: 20px
                .setIndicatorSliderColor(
                    unselectedColor,
                    selectedColor
                ) // Adjust colors accordingly
                .create(responseObj.data.sortedServices)
        } else {
            binding.bannerViewpager.visibility = View.GONE
        }
    }

    /*private fun handlePromotionResponse(promotionResponse: PromotionResponse) {
        cardItems.clear()
        for (i in promotionResponse.promotiondata.promotionList.indices) {
            val cardItem = CardItem(
                promotionResponse.promotiondata.promotionList[i].id,
                promotionResponse.promotiondata.promotionList[i].name,
                R.drawable.facialconcept,
                promotionResponse.promotiondata.promotionList[i].thumbnail.url,
                promotionResponse.promotiondata.promotionList[i].content,
                promotionResponse.promotiondata.promotionList[i].buttonName,
                promotionResponse.promotiondata.promotionList[i].category,
                promotionResponse.promotiondata.promotionList[i].views.toString(),
                promotionResponse.promotiondata.promotionList[i].seriesId,
                promotionResponse.promotiondata.promotionList[i].seriesType,
                promotionResponse.promotiondata.promotionList[i].selectedContentType,
                promotionResponse.promotiondata.promotionList[i].titleImage,
                promotionResponse.promotiondata.promotionList[i].buttonImage
            )
            cardItems.add(i, cardItem)
        }

        if (cardItems.isNotEmpty()) {
            binding.viewPager.visibility = View.VISIBLE
            adapter = CircularCardAdapter(requireActivity(), cardItems) { item: CardItem ->

                if (item.seriesType.equals("daily", ignoreCase = true) ||
                    item.category.equals("CONTENT", ignoreCase = true) || item.category
                        .equals("Test Category", ignoreCase = true)
                ) {
                    //Call Content Activity here
                    callRlEditDetailActivity(item)
                } else if (item.category.equals("live", ignoreCase = true)) {
                    Toast.makeText(requireActivity(), "Live Content", Toast.LENGTH_SHORT).show()
                } else if (item.category.equals("MIND_AUDIT", ignoreCase = true) ||
                    item.category.equals("Mind Audit", ignoreCase = true) ||
                    item.category.equals("Health Audit", ignoreCase = true) ||
                    item.category.equals("mindAudit", ignoreCase = true)
                ) {
                    if ((requireActivity() as? HomeNewActivity)?.checkTrailEndedAndShowDialog() == true) {
                        ActivityUtils.startMindAuditActivity(requireContext())
                    }
                } else if (item.category.equals("VOICE_SCAN", ignoreCase = true)) {
                    val intent = Intent(requireActivity(), VoiceScanActivity::class.java)
                    // Optionally pass data
                    //intent.putExtra("key", "value");
                    startActivity(intent)
                } else if (item.category.equals("FACIAL_SCAN", ignoreCase = true)
                    || item.category.equals("FACE_SCAN", ignoreCase = true)
                    || item.category.equals("Health Cam", ignoreCase = true)
                ) {
                    val spm = SharedPreferenceManager.getInstance(requireActivity())
                    (requireActivity() as? HomeNewActivity)?.callFaceScanClick()
                    *//*  if (spm.userProfile != null && spm.userProfile.user_sub_status == 0) {
                          // Not subscribed → redirect to free trial
                          val intent = Intent(requireActivity(), BeginMyFreeTrialActivity::class.java)
                          intent.putExtra(
                              FeatureFlags.EXTRA_ENTRY_DEST,
                              FeatureFlags.FACE_SCAN
                          )
                          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                          startActivity(intent)
                      } else {
                          var isFacialScanService = false
                          try {
                              isFacialScanService = spm.userProfile.facialScanService
                          } catch (e: Exception) {
                              e.printStackTrace()
                          }

                          if (isFacialScanService) {
                              if (facialScanStatus) {
                                  val intent =
                                      Intent(
                                          requireActivity(),
                                          NewHealthCamReportActivity::class.java
                                      )
                                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                  startActivity(intent)
                              } else {
                                  startFaceScanActivity(requireActivity())
                              }
                          } else {
                              if (requireActivity() is HomeNewActivity) {
                                  (requireActivity() as HomeNewActivity)
                                      .showSwitchAccountDialog(requireActivity(), "", "")
                              } else {
                                  Toast.makeText(
                                      requireActivity(),
                                      "Please switch to your original account.",
                                      Toast.LENGTH_SHORT
                                  ).show()
                              }
                          }
                      }*//*
                }
            }
            binding.viewPager.adapter = adapter
            adapter?.notifyDataSetChanged()


            // Set up the initial position for circular effect
            val initialPosition = Int.MAX_VALUE / 2
            binding.viewPager.setCurrentItem(
                initialPosition - initialPosition % cardItems.size,
                false
            )

            binding.viewPager.apply {
                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = 5
                setPadding(60, 0, 60, 0)
            }

            // Set offscreen page limit and page margin
            binding.viewPager.offscreenPageLimit = 5 // Load adjacent pages
            binding.viewPager.clipToPadding = false
            binding.viewPager.clipChildren = false
            binding.viewPager.setPageTransformer { page, position ->
                val MIN_SCALE = 0.9f     // center = 1.0, side cards smaller
                val MIN_ALPHA = 0.7f
                val translationFactor = 4f  // controls overlap/spacing

                // Keep center card on top
                page.z = 1f - abs(position)

                // Scale cards
                val scale = MIN_SCALE + (1 - abs(position)) * (1 - MIN_SCALE)
                page.scaleX = scale
                page.scaleY = scale

                // Fade cards slightly
                val alpha = MIN_ALPHA + (1 - abs(position)) * (1 - MIN_ALPHA)
                page.alpha = alpha

                // Adjust horizontal position for peeking
                page.translationX = -position * page.width / translationFactor
            }

        } else {
            binding.viewPager.visibility = View.GONE
        }
    }*/

    private fun handlePromotionResponseWeekly(promotionResponse: PromotionWeeklyResponse) {
        cardItems.clear()
        promotionResponse.data.promotionList.indices.forEach { i ->
            val cardItem = CardItem(
                promotionResponse.data.promotionList[i]._id,
                promotionResponse.data.promotionList[i].name,
                R.drawable.facialconcept,
                promotionResponse.data.promotionList[i].desktopImage,
                promotionResponse.data.promotionList[i].content,
                promotionResponse.data.promotionList[i].buttonName,
                promotionResponse.data.promotionList[i].category,
                promotionResponse.data.promotionList[i].views.toString(),
                promotionResponse.data.promotionList[i].seriesId,
                promotionResponse.data.promotionList[i].seriesType,
                promotionResponse.data.promotionList[i].selectedContentType,
                promotionResponse.data.promotionList[i].titleImage,
                promotionResponse.data.promotionList[i].buttonImage
            )
            cardItems.add(i, cardItem)
        }

        if (cardItems.isNotEmpty()) {
            binding.viewPager.visibility = View.VISIBLE
            adapter = CircularCardAdapter(requireActivity(), cardItems) { item: CardItem ->

                if (item.seriesType.equals("daily", ignoreCase = true) ||
                    item.category.equals("CONTENT", ignoreCase = true) || item.category
                        .equals("Test Category", ignoreCase = true)
                ) {
                    //Call Content Activity here
                    callRlEditDetailActivity(item)
                } else if (item.category.equals("live", ignoreCase = true)) {
                    Toast.makeText(requireActivity(), "Live Content", Toast.LENGTH_SHORT).show()
                } else if (item.category.equals("MIND_AUDIT", ignoreCase = true) ||
                    item.category.equals("Mind Audit", ignoreCase = true) ||
                    item.category.equals("Health Audit", ignoreCase = true) ||
                    item.category.equals("mindAudit", ignoreCase = true)
                ) {
                    if ((requireActivity() as? HomeNewActivity)?.checkTrailEndedAndShowDialog() == true) {
                        ActivityUtils.startMindAuditActivity(requireContext())
                    }
                } else if (item.category.equals("VOICE_SCAN", ignoreCase = true)) {
                    val intent = Intent(requireActivity(), VoiceScanActivity::class.java)
                    // Optionally pass data
                    //intent.putExtra("key", "value");
                    startActivity(intent)
                } else if (item.category.equals("FACIAL_SCAN", ignoreCase = true)
                    || item.category.equals("FACE_SCAN", ignoreCase = true)
                    || item.category.equals("Health Cam", ignoreCase = true)
                ) {
                    val spm = SharedPreferenceManager.getInstance(requireActivity())
                    (requireActivity() as? HomeNewActivity)?.callFaceScanClick()
                } else if (item.category.equals("snap_meal", ignoreCase = true)
                        || item.category.equals("SNAP_MEAL", ignoreCase = true)
                        || item.category.equals("Snap_Meal", ignoreCase = true)
                ){

                }
            }
            binding.viewPager.adapter = adapter
            adapter?.notifyDataSetChanged()


            // Set up the initial position for circular effect
            val initialPosition = Int.MAX_VALUE / 2
            binding.viewPager.setCurrentItem(
                initialPosition - initialPosition % cardItems.size,
                false
            )

            binding.viewPager.apply {
                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = 5
                setPadding(60, 0, 60, 0)
            }

            // Set offscreen page limit and page margin
            binding.viewPager.offscreenPageLimit = 5 // Load adjacent pages
            binding.viewPager.clipToPadding = false
            binding.viewPager.clipChildren = false
            binding.viewPager.setPageTransformer { page, position ->
                val MIN_SCALE = 0.9f     // center = 1.0, side cards smaller
                val MIN_ALPHA = 0.7f
                val translationFactor = 4f  // controls overlap/spacing

                // Keep center card on top
                page.z = 1f - abs(position)

                // Scale cards
                val scale = MIN_SCALE + (1 - abs(position)) * (1 - MIN_SCALE)
                page.scaleX = scale
                page.scaleY = scale

                // Fade cards slightly
                val alpha = MIN_ALPHA + (1 - abs(position)) * (1 - MIN_ALPHA)
                page.alpha = alpha

                // Adjust horizontal position for peeking
                page.translationX = -position * page.width / translationFactor
            }

        } else {
            binding.viewPager.visibility = View.GONE
        }
    }

    private fun handleServicePaneResponse(responseObj: ServicePaneResponse) {
        val adapter = ServicePaneAdapter(
            requireActivity(), responseObj.data.homeServices
        ) { homeService: HomeService ->
            when (homeService.title) {
                "Voice Scan" -> {
                    val intentVoice =
                        Intent(requireContext(), MindAuditFromActivity::class.java)
                    startActivity(intentVoice)
                }

                "Mind Audit" -> {
                    if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                        if (NetworkUtils.isInternetAvailable(requireContext())) {
                            freeTrialDialogActivity()
                        } else {
                            showInternetError()
                        }
                    } else {
                        /*  ActivityUtils.startEatRightReportsActivity(
                              requireContext(),
                              "SnapMealTypeEat",
                              ""
                          )*/

                        (requireActivity() as? HomeNewActivity)?.callMindAuditClick()
                    }

                }

                "Meal Snap" -> {
                    if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                        if (NetworkUtils.isInternetAvailable(requireContext())) {
                            freeTrialDialogActivity(FeatureFlags.MEAL_SCAN)
                        } else {
                            showInternetError()
                        }
                    } else {
                        /*  ActivityUtils.startEatRightReportsActivity(
                              requireContext(),
                              "SnapMealTypeEat",
                              ""
                          )*/
                        (requireActivity() as? HomeNewActivity)?.callSnapMealClick()
                    }
                    //ActivityUtils.startMindAuditActivity(requireContext())
                }

                "Health Cam" -> {
                    if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                        if (NetworkUtils.isInternetAvailable(requireContext())) {
                            freeTrialDialogActivity(FeatureFlags.FACE_SCAN)
                        } else {
                            showInternetError()
                        }
                    } else {
                        //ActivityUtils.startFaceScanActivity(requireContext())
                        (requireActivity() as? HomeNewActivity)?.callFaceScanClick()
                    }
                }

                "Face Scan" -> {
                    if (sharedPreferenceManager.userProfile?.user_sub_status == 0) {
                        if (NetworkUtils.isInternetAvailable(requireContext())) {
                            freeTrialDialogActivity(FeatureFlags.FACE_SCAN)
                        } else {
                            showInternetError()
                        }
                    } else {
                        //ActivityUtils.startFaceScanActivity(requireContext())
                        (requireActivity() as? HomeNewActivity)?.callFaceScanClick()
                    }
                }

                else -> {
                    startFaceScanActivity(requireContext())
                }
            }
        }
        var spanCount = responseObj.data.homeServices.size
        spanCount = if ((spanCount > 3)) 2 else spanCount

        binding.rvServicePane.layoutManager =
            GridLayoutManager(requireContext(), spanCount)
        binding.rvServicePane.adapter = adapter
    }

    private fun setupRLEditContent(response: RightLifeEditResponse?) {
        if (response == null || response.data == null) return

        val topList = response.data.topList
        if (topList == null || topList.isEmpty()) {
            binding.rlRightlifeEdit.visibility = View.GONE
            return
        } else {
            binding.rlRightlifeEdit.visibility = View.VISIBLE
        }

        if (topList.size > 0) {
            val item0 = topList[0]
            binding.tvRledtContTitle1.text = item0.title

            if (item0.artist != null && !item0.artist.isEmpty()) {
                val artist = item0.artist[0]
                binding.nameeditor.text = (if (artist.firstName != null) artist.firstName else "") +
                        " " +
                        (if (artist.lastName != null) artist.lastName else "")
            }

            binding.count.text = item0.viewCount.toString()

            if ("VIDEO".equals(item0.contentType, ignoreCase = true)) {
                binding.imgContenttypeRledit.setImageResource(R.drawable.ic_playrledit)
            } else {
                binding.imgContenttypeRledit.setImageResource(R.drawable.read)
            }

            if (item0.thumbnail != null) {
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + item0.thumbnail.url)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(binding.imgRledit)
            }
            setcontenttypeIcon(binding.itemTextRledit, binding.imgIconviewRledit, item0.contentType)
        } else {
            binding.rlRightlifeEdit.visibility = View.GONE
        }

        if (topList.size > 1) {
            val item1 = topList[1]
            binding.tvRledtContTitle2.text = item1.title

            if (item1.artist != null && item1.artist.isNotEmpty()) {
                val artist = item1.artist[0]
                binding.nameeditor1.text =
                    (if (artist.firstName != null) artist.firstName else "") +
                            " " +
                            (if (artist.lastName != null) artist.lastName else "")
            }

            binding.count1.text = item1.viewCount.toString()

            if (item1.thumbnail != null) {
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + item1.thumbnail.url)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .transform(CenterCrop(), RoundedCorners(25))
                    .into(binding.imgRledit1)
            }
            setcontenttypeIcon(
                binding.itemTextRledit1,
                binding.imgIconviewRledit1,
                item1.contentType
            )
        } else {
            binding.relativeRledit2.visibility = View.GONE
        }

        if (topList.size > 2) {
            val item2 = topList[2]
            binding.tvRledtContTitle3.text = item2.title

            if (item2.artist != null && !item2.artist.isEmpty()) {
                val artist = item2.artist[0]
                binding.nameeditor2.text =
                    (if (artist.firstName != null) artist.firstName else "") +
                            " " +
                            (if (artist.lastName != null) artist.lastName else "")
            }

            binding.count2.text = item2.viewCount.toString()

            if (item2.thumbnail != null) {
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + item2.thumbnail.url)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .transform(CenterCrop(), RoundedCorners(25))
                    .into(binding.imgRledit2)
            }
            setcontenttypeIcon(
                binding.itemTextRledit2,
                binding.imgIconviewRledit2,
                item2.contentType
            )
        } else {
            binding.relativeRledit3.visibility = View.GONE
        }
    }

    private fun setcontenttypeIcon(
        contentTypeRledit1: TextView,
        imgContenttypeRledit: ImageView,
        contentType: String?
    ) {
        when {
            "VIDEO".equals(contentType, ignoreCase = true) -> {
                imgContenttypeRledit.setImageResource(R.drawable.video_jump_back_in)
                contentTypeRledit1.text = "Video"
            }

            "AUDIO".equals(contentType, ignoreCase = true) -> {
                imgContenttypeRledit.setImageResource(R.drawable.audio_jump_back_in)
                contentTypeRledit1.text = "Audio"
            }

            "TEXT".equals(contentType, ignoreCase = true) -> {
                imgContenttypeRledit.setImageResource(R.drawable.text_jump_back_in)
                contentTypeRledit1.text = "Text"
            }

            else -> {
                imgContenttypeRledit.setImageResource(R.drawable.series_jump_back_in)
                contentTypeRledit1.text = "Series"
            }
        }
    }


    private fun setupWellnessContent(contentList: List<ContentWellness>) {
        if (contentList.isEmpty()) return

        binding.rlWellnessMain.visibility = View.VISIBLE
        // Bind data for item 1
        if (contentList.isNotEmpty()) {
            bindContentToView(
                contentList[0],
                binding.tv1Header,
                binding.tv1,
                binding.img1,
                binding.tv1Viewcount,
                binding.img5,
                binding.imgtagTv1
            )
        } else {
            binding.relativeWellness1.visibility = View.GONE
        }

        // Bind data for item 2
        if (contentList.size > 1) {
            bindContentToView(
                contentList[1],
                binding.tv2Header,
                binding.tv2,
                binding.img2,
                binding.tv2Viewcount,
                binding.img6,
                binding.imgtagTv2
            )
        } else {
            binding.relativeWellness2.visibility = View.GONE
        }

        // Bind data for item 3
        if (contentList.size > 2) {
            bindContentToView(
                contentList[2],
                binding.tv3Header,
                binding.tv3,
                binding.img3,
                binding.tv3Viewcount,
                binding.img7,
                binding.imgtagTv3
            )
        } else {
            binding.relativeWellness3.visibility = View.GONE
        }

        // Bind data for item 4
        if (contentList.size > 3) {
            bindContentToView(
                contentList[3],
                binding.tv4Header,
                binding.tv4,
                binding.img4,
                binding.tv4Viewcount,
                binding.img8,
                binding.imgtagTv4
            )
        } else {
            binding.relativeWellness4.visibility = View.GONE
        }
    }

    //Bind Wellnes content
    private fun bindContentToView(
        content: ContentWellness,
        header: TextView,
        category: TextView,
        thumbnail: ImageView,
        viewcount: TextView,
        imgcontenttype: ImageView,
        imgtag: ImageView
    ) {
        // Set title in the header TextView
        header.text = content.title
        viewcount.text = "" + content.viewCount
        // Set categoryName in the category TextView
        category.text = content.categoryName

        // Load thumbnail using Glide
        if (!requireActivity().isFinishing && !requireActivity().isDestroyed) {
            Glide.with(requireActivity())
                .load(ApiClient.CDN_URL_QA + content.thumbnail.url) // URL of the thumbnail
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .transform(RoundedCorners(25)) // Optional error image
                .into(thumbnail)
        }
        setModuleColor(imgtag, content.moduleId)
    }

    private fun setModuleColor(imgtag: ImageView, moduleId: String) {
        if (moduleId.equals("EAT_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(requireContext(), R.color.eatright)
            imgtag.imageTintList = colorStateList
        } else if (moduleId.equals("THINK_RIGHT", ignoreCase = true)) {
            val colorStateList =
                ContextCompat.getColorStateList(requireContext(), R.color.thinkright)
            imgtag.imageTintList = colorStateList
        } else if (moduleId.equals("SLEEP_RIGHT", ignoreCase = true)) {
            val colorStateList =
                ContextCompat.getColorStateList(requireContext(), R.color.sleepright)
            imgtag.imageTintList = colorStateList
        } else if (moduleId.equals("MOVE_RIGHT", ignoreCase = true)) {
            val colorStateList =
                ContextCompat.getColorStateList(requireContext(), R.color.moveright)
            imgtag.imageTintList = colorStateList
        }
    }

    private fun callRlEditDetailActivity(position: Int) {
        val contentType = rightLifeEditResponse?.data?.topList?.get(position)?.contentType
        val contentId = rightLifeEditResponse?.data?.topList?.get(position)?.id
        if (contentType.equals("TEXT", ignoreCase = true)) {
            startActivity(Intent(requireContext(), ArticlesDetailActivity::class.java).apply {
                putExtra("contentId", contentId)
            })
        } else if (contentType.equals("VIDEO", ignoreCase = true) || contentType
                .equals("AUDIO", ignoreCase = true)
        ) {
            startActivity(Intent(requireContext(), ContentDetailsActivity::class.java).apply {
                putExtra("contentId", contentId)
            })
        } else if (contentType.equals("SERIES", ignoreCase = true)) {
            startActivity(Intent(requireContext(), SeriesListActivity::class.java).apply {
                putExtra("contentId", contentId)
            })
        }
    }

    private fun callRlEditDetailActivity(item: CardItem) {
        // Assuming rightLifeEditResponse is accessible in this class
        var contentType: String? = null
        var contentId: String? = null
        contentType = item.selectedContentType
        contentId = item.seriesId

        if (contentType != null && contentType.equals("TEXT", ignoreCase = true)) {
            val intent = Intent(requireActivity(), ArticlesDetailActivity::class.java)
            intent.putExtra("contentId", contentId)
            startActivity(intent)
        } else if (contentType != null && (contentType.equals(
                "VIDEO",
                ignoreCase = true
            ) || contentType.equals("AUDIO", ignoreCase = true))
        ) {
            val intent = Intent(requireActivity(), ContentDetailsActivity::class.java)
            intent.putExtra("contentId", contentId)
            startActivity(intent)
        } else if (contentType != null && contentType.equals("SERIES", ignoreCase = true)) {
            val intent = Intent(requireActivity(), SeriesListActivity::class.java)
            intent.putExtra("contentId", contentId)
            startActivity(intent)
        }
    }

    private fun callWellnessDetailActivity(position: Int) {
        if (wellnessApiResponse != null) {
            val gson = Gson()
            val json = gson.toJson(wellnessApiResponse)
            val intent = Intent(requireContext(), SeriesListActivity::class.java)
            intent.putExtra("responseJson", json)
            intent.putExtra("position", position)
            intent.putExtra("contentId", wellnessApiResponse!!.data.contentList[position]._id)
            startActivity(intent)
        } else {
            // Handle null case
            Toast.makeText(requireContext(), "Response is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callExploreModuleActivity(responseJson: SubModuleResponse) {
        val intent = Intent(requireContext(), NewCategoryListActivity::class.java)
        intent.putExtra("moduleId", responseJson.data[0].moduleId)
        startActivity(intent)
    }

    private fun showInternetError() {
        Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
    }

    private fun getJumpBackInData() {
        val call: Call<ResponseBody> =
            apiService.getContinueData(
                sharedPreferenceManager.accessToken,
                "continue",
                10,
                0,
                "all"
            )
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                if (!isFragmentSafe()) return
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonString = response.body()?.string()

                    val responseObj: ContentResponse =
                        gson.fromJson(jsonString, ContentResponse::class.java)

                    val contentDetails: List<ContentDetails>? = responseObj.data?.contentDetails

                    binding.rlJumpInBack.visibility =
                        if (contentDetails?.isEmpty() == true) View.GONE else View.VISIBLE

                    val adapter =
                        contentDetails?.let { ContentDetailsAdapter(requireContext(), it) }

                    // Horizontal LayoutManager
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.recyclerViewJumpBackIn.layoutManager = layoutManager
                    binding.recyclerViewJumpBackIn.adapter = adapter

                } else {
                    Log.e("API_ERROR", "Response Code: " + response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                if (!isFragmentSafe()) return
                handleNoInternetView(t)
            }
        })

    }


    private fun isFragmentSafe(): Boolean {
        return isAdded && activity != null && !requireActivity().isFinishing && !requireActivity().isDestroyed
    }

    private fun freeTrialDialogActivity(featureFlag: String = "") {
        val intent = Intent(requireActivity(), BeginMyFreeTrialActivity::class.java).apply {
            putExtra(FeatureFlags.EXTRA_ENTRY_DEST, featureFlag)
        }
        startActivity(intent)
    }


    // depplinking to detail pages
    fun deeplinkExploreModuleActivity() {
        callExploreModuleActivity(SleepRSubModuleResponse!!)
    }
}