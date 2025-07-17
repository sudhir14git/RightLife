package com.jetsynthesys.rightlife.newdashboard

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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.PromotionResponse
import com.jetsynthesys.rightlife.apimodel.affirmations.AffirmationResponse
import com.jetsynthesys.rightlife.apimodel.rledit.RightLifeEditResponse
import com.jetsynthesys.rightlife.apimodel.servicepane.HomeService
import com.jetsynthesys.rightlife.apimodel.servicepane.ServicePaneResponse
import com.jetsynthesys.rightlife.apimodel.submodule.SubModuleResponse
import com.jetsynthesys.rightlife.apimodel.welnessresponse.ContentWellness
import com.jetsynthesys.rightlife.apimodel.welnessresponse.WellnessApiResponse
import com.jetsynthesys.rightlife.databinding.FragmentHomeExploreBinding
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity
import com.jetsynthesys.rightlife.ui.CardItem
import com.jetsynthesys.rightlife.ui.CategoryListActivity
import com.jetsynthesys.rightlife.ui.CircularCardAdapter
import com.jetsynthesys.rightlife.ui.ServicePaneAdapter
import com.jetsynthesys.rightlife.ui.TestAdapter
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity
import com.jetsynthesys.rightlife.ui.contentdetailvideo.SeriesListActivity
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamActivity
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditActivity
import com.zhpan.bannerview.constants.PageStyle
import com.zhpan.indicator.enums.IndicatorStyle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

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

        (requireActivity() as? HomeNewActivity)?.showHeader(false)

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
        binding.relativeRledit3.setOnClickListener {
            callRlEditDetailActivity(2)
        }
        binding.relativeRledit2.setOnClickListener {
            callRlEditDetailActivity(1)
        }
        binding.relativeRledit1.setOnClickListener {
            callRlEditDetailActivity(0)
        }

        binding.relativeWellness1.setOnClickListener {
            callWellnessDetailActivity(0)
        }
        binding.relativeWellness2.setOnClickListener {
            callWellnessDetailActivity(1)
        }
        binding.relativeWellness3.setOnClickListener {
            callWellnessDetailActivity(2)
        }
        binding.relativeWellness4.setOnClickListener {
            callWellnessDetailActivity(3)
        }

        // set click listener
        binding.llThinkrightCategory1.setOnClickListener {
            if (ThinkRSubModuleResponse?.data?.isNotEmpty() == true) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", ThinkRSubModuleResponse?.data?.get(0)?.categoryId)
                intent.putExtra("moduleId", ThinkRSubModuleResponse?.data?.get(0)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llThinkrightCategory2.setOnClickListener {
            if (ThinkRSubModuleResponse?.data?.size!! > 1) {
                ThinkRSubModuleResponse?.data?.get(1)?.name
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", ThinkRSubModuleResponse?.data?.get(1)?.categoryId)
                intent.putExtra("moduleId", ThinkRSubModuleResponse?.data?.get(1)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llThinkrightCategory3.setOnClickListener {
            if (ThinkRSubModuleResponse?.data?.size!! > 2) {
                ThinkRSubModuleResponse?.data?.get(2)?.name
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", ThinkRSubModuleResponse?.data?.get(2)?.categoryId)
                intent.putExtra("moduleId", ThinkRSubModuleResponse?.data?.get(2)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llThinkrightCategory4.setOnClickListener {
            if (ThinkRSubModuleResponse?.data?.size!! > 3) {
                ThinkRSubModuleResponse?.data?.get(3)?.name
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", ThinkRSubModuleResponse?.data?.get(3)?.categoryId)
                intent.putExtra("moduleId", ThinkRSubModuleResponse?.data?.get(3)?.moduleId)
                startActivity(intent)
            }
        }

        binding.llMoverightCategory1.setOnClickListener {
            if (MoveRSubModuleResponse?.data?.isNotEmpty() == true) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", MoveRSubModuleResponse?.data?.get(0)?.categoryId)
                intent.putExtra("moduleId", MoveRSubModuleResponse?.data?.get(0)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llMoverightCategor2.setOnClickListener {
            if (MoveRSubModuleResponse?.data?.size!! > 1) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", MoveRSubModuleResponse?.data?.get(1)?.categoryId)
                intent.putExtra("moduleId", MoveRSubModuleResponse?.data?.get(1)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llMoverightCategory3.setOnClickListener {
            if (MoveRSubModuleResponse?.data?.size!! > 2) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", MoveRSubModuleResponse?.data?.get(2)?.categoryId)
                intent.putExtra("moduleId", MoveRSubModuleResponse?.data?.get(2)?.moduleId)
                startActivity(intent)
            }
        }

        binding.llEatrightCategory1.setOnClickListener {
            if (EatRSubModuleResponse?.data?.isNotEmpty() == true) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", EatRSubModuleResponse?.data?.get(0)?.categoryId)
                intent.putExtra("moduleId", EatRSubModuleResponse?.data?.get(0)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llEatrightCategory2.setOnClickListener {
            if (EatRSubModuleResponse?.data?.size!! > 1) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", EatRSubModuleResponse?.data?.get(1)?.categoryId)
                intent.putExtra("moduleId", EatRSubModuleResponse?.data?.get(1)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llEatrightCategory3.setOnClickListener {
            if (EatRSubModuleResponse?.data?.size!! > 2) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", EatRSubModuleResponse?.data?.get(2)?.categoryId)
                intent.putExtra("moduleId", EatRSubModuleResponse?.data?.get(2)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llEatrightCategory4.setOnClickListener {
            if (EatRSubModuleResponse?.data?.size!! > 3) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", EatRSubModuleResponse?.data?.get(3)?.categoryId)
                intent.putExtra("moduleId", EatRSubModuleResponse?.data?.get(3)?.moduleId)
                startActivity(intent)
            }
        }

        binding.llSleeprightCategory1.setOnClickListener {
            if (SleepRSubModuleResponse?.data?.isNotEmpty() == true) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", SleepRSubModuleResponse?.data?.get(0)?.categoryId)
                intent.putExtra("moduleId", SleepRSubModuleResponse?.data?.get(0)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llSleeprightCategory2.setOnClickListener {
            if (SleepRSubModuleResponse?.data?.size!! > 1) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", SleepRSubModuleResponse?.data?.get(1)?.categoryId)
                intent.putExtra("moduleId", SleepRSubModuleResponse?.data?.get(1)?.moduleId)
                startActivity(intent)
            }
        }
        binding.llSleeprightCategory3.setOnClickListener {
            if (SleepRSubModuleResponse?.data?.size!! > 2) {
                val intent = Intent(requireContext(), CategoryListActivity::class.java)
                intent.putExtra("Categorytype", SleepRSubModuleResponse?.data?.get(2)?.categoryId)
                intent.putExtra("moduleId", SleepRSubModuleResponse?.data?.get(2)?.moduleId)
                startActivity(intent)
            }
        }

        binding.btnSrExplore.setOnClickListener {
            callExploreModuleActivity(SleepRSubModuleResponse!!)
        }
        binding.btnTrExplore.setOnClickListener {
            callExploreModuleActivity(ThinkRSubModuleResponse!!)
        }
        binding.btnErExplore.setOnClickListener {
            callExploreModuleActivity(EatRSubModuleResponse!!)
        }
        binding.btnMrExplore.setOnClickListener {
            callExploreModuleActivity(MoveRSubModuleResponse!!)
        }

        binding.btnWellnessPreference.setOnClickListener {

        }
    }

    override fun onResume() {
        super.onResume()
        // Resume auto-slide when activity is visible
        sliderHandler?.postDelayed(sliderRunnable!!, 3000)
        getPromotionList()
        getRightLifeEdit()
        getWellnessPlaylist()
    }

    private fun callAPIS() {
        getPromotionList2() // ModuleService pane
        getRightLifeEdit()

        getAffirmations()

        getWellnessPlaylist()

        getModuleContent()

        getSubModuleContent("THINK_RIGHT")
        getSubModuleContent("MOVE_RIGHT")
        getSubModuleContent("EAT_RIGHT")
        getSubModuleContent("SLEEP_RIGHT")
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
                if (response.isSuccessful && response.body() != null) {
                    response.body()
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val responseObj = gson.fromJson(
                        jsonResponse,
                        AffirmationResponse::class.java
                    )
                    setupAffirmationContent(responseObj)
                } else {
                    //Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun getPromotionList() {
        val call = apiService.getPromotionList(
            sharedPreferenceManager.accessToken,
            "HOME_PAGE",
            sharedPreferenceManager.userId,
            "TOP"
        )
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    val promotionResponse = gson.fromJson(
                        jsonResponse,
                        PromotionResponse::class.java
                    )
                    if (promotionResponse.success) {
                        handlePromotionResponse(promotionResponse)
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
                handleNoInternetView(t)
            }
        })
    }

    private fun getPromotionList2() {
        val call = apiService.getPromotionList2(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val responseObj = gson.fromJson(
                        jsonResponse,
                        ServicePaneResponse::class.java
                    )
                    handleServicePaneResponse(responseObj)
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun getRightLifeEdit() {
        val call = apiService.getRightlifeEdit(sharedPreferenceManager.accessToken, "HOME")
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
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
                    setupRLEditContent(rightLifeEditResponse)
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
                handleNoInternetView(t)
            }
        })
    }

    private fun getWellnessPlaylist() {
        val call =
            apiService.getWelnessPlaylist(sharedPreferenceManager.accessToken, "SERIES", "WELLNESS")
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    wellnessApiResponse = gson.fromJson(
                        jsonResponse,
                        WellnessApiResponse::class.java
                    )
                    wellnessApiResponse?.data?.contentList?.let { setupWellnessContent(it) }
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun getModuleContent() {
        val call = apiService.getmodule(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
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
                if (response.isSuccessful && response.body() != null) {
                    val affirmationsResponse = response.body()
                    val gson = Gson()

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
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun handleThinkRightResponse(){
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

    private fun handleMoveRightResponse(){
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
                tvMoveRightCategory2.text = MoveRSubModuleResponse?.data?.get(1)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + MoveRSubModuleResponse?.data?.get(1)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageMoveRightCategory2)
            }
        }
        if (MoveRSubModuleResponse?.data?.size!! > 2) {
            with(binding) {
                tvMoveRightCategory3.text = MoveRSubModuleResponse?.data?.get(2)?.name
                Glide.with(requireActivity())
                    .load(ApiClient.CDN_URL_QA + MoveRSubModuleResponse?.data?.get(2)?.imageUrl)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(imageMoveRightCategory3)
            }
        }
    }

    private fun handleEatRightResponse(){
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
        if (ThinkRSubModuleResponse?.data?.size!! > 3) {
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

    private fun handleSleepRightResponse(){
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

    private fun handlePromotionResponse(promotionResponse: PromotionResponse) {
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
                promotionResponse.promotiondata.promotionList[i].titleImage,
                promotionResponse.promotiondata.promotionList[i].buttonImage
            )
            cardItems.add(i, cardItem)
        }

        if (cardItems.isNotEmpty()) {
            binding.viewPager.visibility = View.VISIBLE
            adapter = CircularCardAdapter(requireActivity(), cardItems)
            binding.viewPager.adapter = adapter
        } else {
            binding.viewPager.visibility = View.GONE
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun handleServicePaneResponse(responseObj: ServicePaneResponse) {
        val adapter = ServicePaneAdapter(
            requireActivity(), responseObj.data.homeServices
        ) { homeService: HomeService ->
            when (homeService.title) {
                "Voice Scan" -> {
                    val intentVoice =
                        Intent(requireContext(), MindAuditActivity::class.java)
                    startActivity(intentVoice)
                }

                "Mind Audit" -> {
                    val intentMind =
                        Intent(requireContext(), MindAuditActivity::class.java)
                    startActivity(intentMind)
                }

                "Health Cam" -> startActivity(
                    Intent(
                        requireContext(),
                        HealthCamActivity::class.java
                    )
                )

                else -> {
                    val intentHealthAudit = Intent(
                        requireContext(),
                        HealthCamActivity::class.java
                    )
                    startActivity(intentHealthAudit)
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
            binding.tvRledtContTitle1.text = item0.desc

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
        } else {
            binding.relativeRledit3.visibility = View.GONE
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
        if (rightLifeEditResponse?.data?.topList?.get(position)?.contentType
                .equals("TEXT", ignoreCase = true)
        ) {
            val intent = Intent(requireContext(), ArticlesDetailActivity::class.java)
            intent.putExtra(
                "contentId",
                rightLifeEditResponse?.data?.topList?.get(position)?.id
            )
            startActivity(intent)
        } else {
            val gson = Gson()
            val json = gson.toJson(rightLifeEditResponse)
            val intent = Intent(requireContext(), ContentDetailsActivity::class.java)
            intent.putExtra(
                "Categorytype",
                rightLifeEditResponse?.data?.topList?.get(position)?.id
            )
            intent.putExtra("position", position)
            intent.putExtra(
                "contentId",
                rightLifeEditResponse?.data?.topList?.get(position)?.id
            )
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
        val intent = Intent(requireContext(), CategoryListActivity::class.java)
        intent.putExtra("moduleId", responseJson.data[0].moduleId)
        startActivity(intent)
    }


}