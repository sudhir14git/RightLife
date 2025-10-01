package com.jetsynthesys.rightlife.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.chipsmodulefilter.ModuleChipCategory
import com.jetsynthesys.rightlife.databinding.ActivityNewCategorylistBinding
import com.jetsynthesys.rightlife.databinding.PopupCategoryListBinding
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewCategoryListActivity : BaseActivity() {

    private lateinit var binding: ActivityNewCategorylistBinding
    private val contentDetails = mutableListOf<CategoryListItem>()
    private lateinit var adapter: NewCategoryListAdapter
    private var selectedModuleId = ""
    private var selectedCategoryId = ""
    private var isLoading = false
    private var skip = 0
    private val limit = 10
    private var selectedContentType = "all"
    private var selectedText = "All"
    private var totalCount = 0
    private lateinit var responseObj: ModuleChipCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCategorylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedCategoryId = intent.getStringExtra("Categorytype") ?: ""
        selectedModuleId = intent.getStringExtra("moduleId") ?: ""

        binding.rlSelectedCategory.setOnClickListener {
            showCustomPopup(it)
        }

        binding.iconBack.setOnClickListener {
            finish()
        }

        binding.tvHeader.text = getModuleText(selectedModuleId)

        getContentList(selectedModuleId)


        adapter =
            NewCategoryListAdapter(this, contentDetails, onBookMarkedClick = { item, position ->
                item.id?.let { it1 ->
                    CommonAPICall.contentBookMark(
                        this,
                        it1,
                        !item.isBookmarked
                    ) { success, message ->
                        if (success) {
                            item.isBookmarked = !item.isBookmarked
                            val msg = if (item.isBookmarked) {
                                "Added To Bookmarks"
                            } else {
                                "Removed From Bookmarks"
                            }
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                            adapter.notifyItemChanged(position)
                        } else {
                            Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            })

        binding.rvCategoryList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvCategoryList.adapter = adapter

        // Add scroll listener for pagination
        binding.rvCategoryList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()

                if (!isLoading && lastVisible == contentDetails.size - 1 && contentDetails.size < totalCount) {
                    fetchContent(skip, selectedContentType, selectedCategoryId)
                }
            }
        })

        fetchContent(skip, selectedContentType, selectedCategoryId)
        setupChipListeners()
    }

    private fun setupChipListeners() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { group: ChipGroup, checkedIds: List<Int> ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds[0] // since single selection
                val chip = group.findViewById<Chip>(checkedId)
                if (chip != null) {
                    var position = -1
                    for (i in 0 until group.childCount) {
                        val child = group.getChildAt(i)
                        if (child.id == checkedId) {
                            position = i
                            break
                        }
                    }

                    // Use the position as needed
                    if (position != -1) {
                        contentDetails.clear()
                        skip = 0
                        if (position == 0) {
                            selectedCategoryId = ""
                            fetchContent(skip, selectedContentType)
                        } else {
                            val result = responseObj.data.result[position - 1]
                            selectedCategoryId = result.categoryId
                            selectedModuleId = result.moduleId
                            fetchContent(skip, selectedContentType, selectedCategoryId)
                        }
                    }
                }
            }
        }
    }


    private fun getContentList(moduleId: String) {
        val call = apiService.getContent(
            sharedPreferenceManager.accessToken,
            "CATEGORY",
            moduleId,
            false,
            true
        )
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    responseObj = gson.fromJson(
                        jsonResponse,
                        ModuleChipCategory::class.java
                    )
                    addChip("All", selectedModuleId)
                    val result = responseObj.data.result
                    for (i in result.indices) {
                        addChip(result[i].name, result[i].id)
                    }
                    findViewById<Chip>(binding.filterChipGroup.checkedChipId)?.let { chip ->
                        binding.filterChipGroup.requestChildFocus(chip, chip)
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            val errorMessage = response.errorBody()!!.string()
                            Log.d("API Response 2", "Success: $errorMessage")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Toast.makeText(
                        this@NewCategoryListActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun addChip(category: String, categoryId: String) {
        val chip = Chip(this).apply {
            id = View.generateViewId() // Unique ID
            text = category
            isCheckable = true
            isChecked = categoryId == selectedCategoryId ||
                    (selectedCategoryId.isEmpty() && category.equals("All", ignoreCase = true))
            chipCornerRadius = 50f
            chipStrokeWidth = 0f
            /*chipStrokeColor =
                Utils.getModuleColorStateList(this@NewCategoryListActivity, selectedModuleId)*/
            textSize = 12f

            typeface =
                ResourcesCompat.getFont(this@NewCategoryListActivity, R.font.merriweather_regular)

            // Background color states
            chipBackgroundColor = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),   // Checked state
                    intArrayOf(-android.R.attr.state_checked)   // Unchecked state
                ),
                intArrayOf(
                    ContextCompat.getColor(
                        this@NewCategoryListActivity,
                        R.color.menuselected
                    ), // Checked color
                    ContextCompat.getColor(
                        this@NewCategoryListActivity,
                        R.color.gray_light_bg
                    )   // Unchecked color
                )
            )

            // Text colors (from XML selector)
            val textColorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),   // checked
                    intArrayOf(-android.R.attr.state_checked)   // unchecked
                ),
                intArrayOf(
                    ContextCompat.getColor(
                        this@NewCategoryListActivity,
                        R.color.white
                    ),              // checked color
                    ContextCompat.getColor(
                        this@NewCategoryListActivity,
                        R.color.txt_color_header
                    )    // unchecked color
                )
            )
            setTextColor(textColorStateList)
        }

        binding.filterChipGroup.addView(chip)
    }

    private fun showCustomPopup(anchorView: View) {
        val bindingDialog = PopupCategoryListBinding.inflate(layoutInflater)
        val widthInPx = (250 * resources.displayMetrics.density).toInt() // 280dp → px

        val popupWindow = PopupWindow(
            bindingDialog.root,
            widthInPx,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Allow dismiss when tapping outside
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.elevation = 10f

        when (selectedText) {
            "All" -> {
                bindingDialog.tvAll.setTextColor(
                    ContextCompat.getColor(this, R.color.menuselected)
                )
                bindingDialog.tvAudio.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvVideo.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvSeries.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvArticles.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
            }

            "Articles" -> {
                bindingDialog.tvAll.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvAudio.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvVideo.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvSeries.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvArticles.setTextColor(
                    ContextCompat.getColor(this, R.color.menuselected)
                )
            }

            "Series" -> {
                bindingDialog.tvAll.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvAudio.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvVideo.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvSeries.setTextColor(
                    ContextCompat.getColor(this, R.color.menuselected)
                )
                bindingDialog.tvArticles.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
            }

            "Video" -> {
                bindingDialog.tvAll.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvAudio.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvVideo.setTextColor(
                    ContextCompat.getColor(this, R.color.menuselected)
                )
                bindingDialog.tvSeries.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvArticles.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
            }

            else -> {
                bindingDialog.tvAll.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvAudio.setTextColor(
                    ContextCompat.getColor(this, R.color.menuselected)
                )
                bindingDialog.tvVideo.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvSeries.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
                bindingDialog.tvArticles.setTextColor(
                    ContextCompat.getColor(this, R.color.txt_color_header)
                )
            }

        }

        // Example click handling
        bindingDialog.tvAll.setOnClickListener {
            popupOptionClicked(bindingDialog.tvAll.text.toString())
            popupWindow.dismiss()
        }
        bindingDialog.tvSeries.setOnClickListener {
            popupOptionClicked(bindingDialog.tvSeries.text.toString())
            popupWindow.dismiss()
        }
        bindingDialog.tvVideo.setOnClickListener {
            popupOptionClicked(bindingDialog.tvVideo.text.toString())
            popupWindow.dismiss()
        }
        bindingDialog.tvAudio.setOnClickListener {
            popupOptionClicked(bindingDialog.tvAudio.text.toString())
            popupWindow.dismiss()
        }

        bindingDialog.tvArticles.setOnClickListener {
            popupOptionClicked(bindingDialog.tvArticles.text.toString())
            popupWindow.dismiss()
        }

        // Show popup below the anchor view
        popupWindow.showAsDropDown(anchorView, 0, -100)
    }

    private fun popupOptionClicked(selectedString: String) {
        binding.tvSelectedCategory.text = selectedString
        selectedText = selectedString
        skip = 0
        contentDetails.clear()
        selectedContentType = if (selectedString.equals("All", ignoreCase = true))
            selectedString.lowercase()
        else
            selectedString.uppercase()
        fetchContent(skip, selectedContentType, selectedCategoryId)
    }

    private fun fetchContent(skipValue: Int, contentType: String, categoryId: String = "") {
        Utils.showLoader(this)
        isLoading = true
        val call = if (categoryId.isEmpty())
            apiService.fetchCategoryList(
                sharedPreferenceManager.accessToken,
                limit,
                skipValue,
                selectedModuleId,
                contentType
            )
        else
            apiService.fetchCategoryList(
                sharedPreferenceManager.accessToken,
                limit,
                skipValue,
                selectedModuleId,
                categoryId,
                contentType
            )

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                Utils.dismissLoader(this@NewCategoryListActivity)
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonString = response.body()?.string()

                    val responseObj: CategoryListResponse =
                        gson.fromJson(jsonString, CategoryListResponse::class.java)
                    totalCount = responseObj.data?.count ?: 0

                    val newItems = responseObj.data?.contentList ?: emptyList()
                    contentDetails.addAll(newItems)
                    adapter.notifyDataSetChanged()
                    skip += newItems.size
                }
                isLoading = false
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                isLoading = false
                Utils.dismissLoader(this@NewCategoryListActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun getModuleText(module: String?): String = when (module) {
        null -> ""
        AppConstants.EAT_RIGHT, "EAT_RIGHT" -> "EatRight"
        AppConstants.THINK_RIGHT, "THINK_RIGHT" -> "ThinkRight"
        AppConstants.SLEEP_RIGHT, "SLEEP_RIGHT" -> "SleepRight"
        AppConstants.MOVE_RIGHT, "MOVE_RIGHT" -> "MoveRight"
        else -> module
    }

}