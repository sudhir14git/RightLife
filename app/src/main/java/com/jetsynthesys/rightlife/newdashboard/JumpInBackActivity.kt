package com.jetsynthesys.rightlife.newdashboard

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityJumpBackInBinding
import com.jetsynthesys.rightlife.databinding.PopupJumpBackInBinding
import com.jetsynthesys.rightlife.newdashboard.model.ContentDetails
import com.jetsynthesys.rightlife.newdashboard.model.ContentResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JumpInBackActivity : BaseActivity() {
    private lateinit var binding: ActivityJumpBackInBinding
    private lateinit var adapter: JumpInBackAdapter
    private val contentDetails = mutableListOf<ContentDetails>()
    private val allContentDetails = mutableListOf<ContentDetails>()

    private var isLoading = false
    private var skip = 0
    private val limit = 10
    private val pageType = "continue"
    private var selectedText = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJumpBackInBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.iconBack.setOnClickListener {
            finish()
        }

        binding.rlSelectedCategory.setOnClickListener {
            showCustomPopup(it)
        }

        adapter = JumpInBackAdapter(this, contentDetails)

        binding.rvJumpBackIn.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvJumpBackIn.adapter = adapter

        // Add scroll listener for pagination
        binding.rvJumpBackIn.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()

                if (!isLoading && lastVisible == contentDetails.size - 1) {
                    loadMoreData()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        contentDetails.clear()
        allContentDetails.clear()
        skip = 0
        fetchContent(skip)
    }

    private fun fetchContent(skipValue: Int) {
        isLoading = true
        val call = apiService.getContinueData(
            sharedPreferenceManager.accessToken,
            pageType,
            limit,
            skipValue
        )

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonString = response.body()?.string()

                    val responseObj: ContentResponse =
                        gson.fromJson(jsonString, ContentResponse::class.java)

                    val newItems = responseObj.data?.contentDetails ?: emptyList()
                    contentDetails.addAll(newItems)
                    allContentDetails.addAll(newItems)
                    adapter.notifyDataSetChanged()
                    skip += newItems.size
                }
                isLoading = false
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                isLoading = false
                handleNoInternetView(t)
            }
        })
    }

    private fun loadMoreData() {
        fetchContent(skip)
    }

    private fun showCustomPopup(anchorView: View) {
        val bindingDialog = PopupJumpBackInBinding.inflate(layoutInflater)
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

        // Show popup below the anchor view
        popupWindow.showAsDropDown(anchorView, 0, -100)
    }

    private fun popupOptionClicked(selectedString: String) {
        binding.tvSelectedCategory.text = selectedString
        selectedText = selectedString
        filterContent(selectedString)
    }

    private fun filterContent(type: String) {
        contentDetails.clear()

        if (type == "All") {
            contentDetails.addAll(allContentDetails)
        } else {
            contentDetails.addAll(
                allContentDetails.filter { it.contentType.equals(type, ignoreCase = true) }
            )
        }
        adapter.notifyDataSetChanged()
    }


}