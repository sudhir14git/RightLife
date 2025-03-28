package com.example.rlapp.ui.affirmation

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.rlapp.R
import com.example.rlapp.RetrofitData.ApiClient
import com.example.rlapp.RetrofitData.ApiService
import com.example.rlapp.databinding.ActivityTodaysAffirmationBinding
import com.example.rlapp.ui.affirmation.adapter.AffirmationCardPagerAdapter
import com.example.rlapp.ui.affirmation.pojo.AffirmationCategoryData
import com.example.rlapp.ui.affirmation.pojo.AffirmationCategoryListResponse
import com.example.rlapp.ui.affirmation.pojo.AffirmationSelectedCategoryData
import com.example.rlapp.ui.affirmation.pojo.AffirmationSelectedCategoryResponse
import com.example.rlapp.ui.affirmation.pojo.CreateAffirmationPlaylistRequest
import com.example.rlapp.ui.affirmation.pojo.GetAffirmationPlaylistResponse
import com.example.rlapp.ui.utility.SharedPreferenceManager
import com.example.rlapp.ui.utility.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TodaysAffirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTodaysAffirmationBinding
    private lateinit var recyclerViewCategory: RecyclerView
    private lateinit var categoryBottomSheetDialog: BottomSheetDialog
    private lateinit var closeBottomSheetDialog: BottomSheetDialog
    private lateinit var discardBottomSheetDialog: BottomSheetDialog
    private val affirmationPlaylist: ArrayList<AffirmationSelectedCategoryData> = ArrayList()
    private val affirmationPlaylistRequest: ArrayList<String> = ArrayList()

    private val affirmationList: ArrayList<AffirmationSelectedCategoryData> = ArrayList()
    private lateinit var affirmationCardPagerAdapter: AffirmationCardPagerAdapter
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodaysAffirmationBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

        setupCategoryBottomSheet()
        setupCloseBottomSheet()
        setupDiscardBottomSheet()
        getAffirmationPlaylist()
        getCategoryList()


        binding.llCategorySelection.setOnClickListener {
            categoryBottomSheetDialog.show()
        }

        binding.infoAffirmation.setOnClickListener {
            showInfoDialog()
        }

        binding.ivClose.setOnClickListener {
            if (sharedPreferenceManager.firstTimeUserForAffirmation)
                closeBottomSheetDialog.show()
            else if (affirmationPlaylistRequest.isNotEmpty())
                discardBottomSheetDialog.show()
            else
                finish()
        }

        binding.addAffirmation.setOnClickListener {
            addCardToPlaylist()
        }

        binding.btnCreateAffirmation.setOnClickListener {
            createAffirmationPlaylist()
        }

        setSelectedCategoryAdapter(affirmationList)

    }

    private fun addCardToPlaylist() {
        binding.addAffirmation.isEnabled = false
        affirmationPlaylist.add(affirmationList[binding.cardViewPager.currentItem])
        binding.addAffirmation.setImageResource(R.drawable.playlist_added)
        affirmationList[binding.cardViewPager.currentItem].id?.let {
            affirmationPlaylistRequest.add(
                it
            )
        }
        if (affirmationPlaylist.size >= 3)
            binding.btnCreateAffirmation.isEnabled = true

        if (sharedPreferenceManager.firstTimeUserForAffirmation) {
            when (affirmationPlaylist.size) {
                1 -> {
                    Toast.makeText(this, "Great choice, keep going.", Toast.LENGTH_SHORT).show()
                }

                2 -> {
                    Toast.makeText(
                        this,
                        "One more and your playlist is ready to go.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                3 -> {
                    Toast.makeText(this, "Playlist Unlocked!", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(
                        this,
                        "${affirmationPlaylist.size} Affirmation Added!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                this,
                "${affirmationPlaylistRequest.size} Affirmation Added!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

   /* private fun setSelectedCategoryAdapter(affirmationList: ArrayList<AffirmationSelectedCategoryData>) {
        affirmationCardPagerAdapter =
            AffirmationCardPagerAdapter(affirmationList, this, binding.cardViewPager)
        binding.cardViewPager.setPageTransformer(true, AffirmationPageTransformer())
        binding.cardViewPager.adapter = affirmationCardPagerAdapter
        updateAddButtonImage(0)
        binding.cardViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                updateAddButtonImage(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }*/
   private fun setSelectedCategoryAdapter(affirmationList: ArrayList<AffirmationSelectedCategoryData>) {
       affirmationCardPagerAdapter =
           AffirmationCardPagerAdapter(affirmationList, this, binding.cardViewPager)
       binding.cardViewPager.setPageTransformer(true, AffirmationPageTransformer())
       binding.cardViewPager.adapter = affirmationCardPagerAdapter
       if (affirmationList.isNotEmpty())
           updateAddButtonImage(0)
       binding.cardViewPager.addOnPageChangeListener(object : OnPageChangeListener {
           override fun onPageScrolled(
               position: Int,
               positionOffset: Float,
               positionOffsetPixels: Int
           ) {

           }

           override fun onPageSelected(position: Int) {
               updateAddButtonImage(position)
           }

           override fun onPageScrollStateChanged(state: Int) {

           }
       })
   }

    private fun updateAddButtonImage(position: Int) {
        var flag = false
        for (playlist in affirmationPlaylist) {
            if (playlist.id == affirmationList[position].id) {
                flag = true
            }
        }
        if (flag) {
            binding.addAffirmation.setImageResource(R.drawable.playlist_added)
            binding.addAffirmation.isEnabled = false
        } else {
            binding.addAffirmation.setImageResource(R.drawable.add_affirmation)
            binding.addAffirmation.isEnabled = true
        }

    }

    private fun setupCategoryBottomSheet() {
        // Create and configure BottomSheetDialog
        categoryBottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)

        // Set up RecyclerView in the BottomSheet
        recyclerViewCategory = bottomSheetView.findViewById(R.id.recyclerView)
        recyclerViewCategory.setLayoutManager(LinearLayoutManager(this))
        //recyclerViewCategory.setAdapter(myAdapter)

        categoryBottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        val ivClose = bottomSheetView.findViewById<ImageView>(R.id.ivClose)
        ivClose.setOnClickListener {
            categoryBottomSheetDialog.dismiss()
        }

    }

    private fun setupDiscardBottomSheet() {
        // Create and configure BottomSheetDialog
        discardBottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_discard_bottomsheet, null)


        discardBottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetView.findViewById<Button>(R.id.btnNo).setOnClickListener {
            discardBottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<Button>(R.id.btnYes).setOnClickListener {
            finish()
        }

    }

    private fun setupCloseBottomSheet() {
        // Create and configure BottomSheetDialog
        closeBottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_close_bottomsheet, null)


        closeBottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        bottomSheetView.findViewById<Button>(R.id.btnGoBackAndSave3).setOnClickListener {
            closeBottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<Button>(R.id.btnQuitAnyway).setOnClickListener {
            finish()
        }

    }

    private fun setupCategoryAdapter(categoryList: List<AffirmationCategoryData>) {
        val affirmationCategoryListAdapter = AffirmationCategoryListAdapter(
            this, categoryList
        ) { category ->
            getSelectedCategoryData(category.id)
            if (categoryBottomSheetDialog.isShowing)
                categoryBottomSheetDialog.dismiss()
            binding.tvCategory.text = category.title
        }
        recyclerViewCategory.adapter = affirmationCategoryListAdapter
    }

    private fun getCategoryList() {
        val authToken = sharedPreferenceManager.accessToken
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val call = apiService.getAffirmationCategoryList(authToken)

        call.enqueue(object : Callback<AffirmationCategoryListResponse> {
            override fun onResponse(
                call: Call<AffirmationCategoryListResponse>,
                response: Response<AffirmationCategoryListResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.let {
                        setupCategoryAdapter(it)
                        if (it.isNotEmpty())
                            getSelectedCategoryData(it[0].id)
                    }
                } else {
                    Toast.makeText(
                        this@TodaysAffirmationActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AffirmationCategoryListResponse>, t: Throwable) {
                Toast.makeText(
                    this@TodaysAffirmationActivity,
                    "Network Error: " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun getSelectedCategoryData(id: String?) {
        Utils.showLoader(this)
        val authToken = sharedPreferenceManager.accessToken
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val call = apiService.getAffirmationSelectedCategoryData(authToken, id)

        call.enqueue(object : Callback<AffirmationSelectedCategoryResponse> {
            override fun onResponse(
                call: Call<AffirmationSelectedCategoryResponse>,
                response: Response<AffirmationSelectedCategoryResponse>
            ) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                if (response.isSuccessful && response.body() != null) {
                    affirmationList.clear()
                    response.body()?.data?.let { affirmationList.addAll(it) }
                    setSelectedCategoryAdapter(affirmationList)
                } else {
                    Toast.makeText(
                        this@TodaysAffirmationActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AffirmationSelectedCategoryResponse>, t: Throwable) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                Toast.makeText(
                    this@TodaysAffirmationActivity,
                    "Network Error: " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun getAffirmationPlaylist() {
        Utils.showLoader(this)
        val authToken = sharedPreferenceManager.accessToken
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        val call = apiService.getAffirmationPlaylist(authToken)

        call.enqueue(object : Callback<GetAffirmationPlaylistResponse> {
            override fun onResponse(
                call: Call<GetAffirmationPlaylistResponse>,
                response: Response<GetAffirmationPlaylistResponse>
            ) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.let { affirmationPlaylist.addAll(it) }
                    if (affirmationPlaylist.isNotEmpty()) {
                        binding.btnCreateAffirmation.text = "Save"
                        binding.btnCreateAffirmation.isEnabled = true
                    }
                } else {
                    Toast.makeText(
                        this@TodaysAffirmationActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<GetAffirmationPlaylistResponse>, t: Throwable) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                Toast.makeText(
                    this@TodaysAffirmationActivity,
                    "Network Error: " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun createAffirmationPlaylist() {
        Utils.showLoader(this)
        val authToken = sharedPreferenceManager.accessToken
        val apiService = ApiClient.getClient().create(ApiService::class.java)

        val createAffirmationPlaylistRequest = CreateAffirmationPlaylistRequest()
        createAffirmationPlaylistRequest.list?.addAll(affirmationPlaylistRequest)

        val call = apiService.createAffirmationPlaylist(authToken, createAffirmationPlaylistRequest)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                if (response.isSuccessful && response.body() != null) {
                    sharedPreferenceManager.firstTimeUserForAffirmation = false
                    if (sharedPreferenceManager.firstTimeUserForAffirmation) {
                        showCreatedUpdatedDialog("Playlist Created")
                    } else {
                        showCreatedUpdatedDialog("Changes Saved")
                    }
                } else {
                    Toast.makeText(
                        this@TodaysAffirmationActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Utils.dismissLoader(this@TodaysAffirmationActivity)
                Toast.makeText(
                    this@TodaysAffirmationActivity,
                    "Network Error: " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun showInfoDialog() {

        // Create the dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_info_affirmation)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window

        // Set the dim amount
        val layoutParams = window?.attributes
        layoutParams?.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window?.attributes = layoutParams

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        layoutParams?.width = width

        /*dialog.setOnCancelListener {

        }*/
        dialog.show()
    }

    private fun showCreatedUpdatedDialog(message: String) {

        // Create the dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_playlist_created)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window

        // Set the dim amount
        val layoutParams = window?.attributes
        layoutParams?.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window?.attributes = layoutParams

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        layoutParams?.width = width

        dialog.findViewById<TextView>(R.id.tvDialogPlaylistCreated).text = message

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            finish()
        }, 1000)
    }
}