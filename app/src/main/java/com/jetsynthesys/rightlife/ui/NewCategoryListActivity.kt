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
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.chipsmodulefilter.ModuleChipCategory
import com.jetsynthesys.rightlife.databinding.ActivityNewCategorylistBinding
import com.jetsynthesys.rightlife.databinding.PopupCategoryListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewCategoryListActivity : BaseActivity() {

    private lateinit var binding: ActivityNewCategorylistBinding
    private var selectedModuleId = ""
    private var selectedCategoryId = ""
    private var selectedText = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCategorylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedCategoryId = intent.getStringExtra("Categorytype") ?: ""
        selectedModuleId = intent.getStringExtra("moduleId") ?: ""

        binding.rlSelectedCategory.setOnClickListener {
            showCustomPopup(it)
        }

        getContentList(selectedModuleId)
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
                    val responseObj = gson.fromJson(
                        jsonResponse,
                        ModuleChipCategory::class.java
                    )
                    addChip("All", selectedModuleId)
                    for (i in responseObj.data.result.indices) {
                        addChip(
                            responseObj.data.result[i].name,
                            responseObj.data.result[i].id
                        )
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
        //filterContent(selectedString)
    }

}