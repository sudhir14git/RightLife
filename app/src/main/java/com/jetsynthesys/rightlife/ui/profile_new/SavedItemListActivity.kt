package com.jetsynthesys.rightlife.ui.profile_new

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivitySavedItemListBinding
import com.jetsynthesys.rightlife.databinding.PopupCategoryListBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.profile_new.adapter.SavedItemsAdapter
import com.jetsynthesys.rightlife.ui.profile_new.pojo.BookMarkContentDetails
import com.jetsynthesys.rightlife.ui.profile_new.pojo.BookmarkResponse
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SavedItemListActivity : BaseActivity() {
    private lateinit var binding: ActivitySavedItemListBinding
    private val contentDetails = mutableListOf<BookMarkContentDetails>()
    private val allContentDetails = mutableListOf<BookMarkContentDetails>()
    private lateinit var adapter: SavedItemsAdapter

    private var isLoading = false
    private var skip = 0
    private val limit = 10
    private var selectedContentType = "all"
    private var selectedText = "All"
    private var totalCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rlSelectedCategory.setOnClickListener {
            showCustomPopup(it)
        }

        binding.iconBack.setOnClickListener {
            finish()
        }

        adapter = SavedItemsAdapter(this, contentDetails, onBookMarkedClick = { item, position ->
            item.id?.let { it1 ->
                CommonAPICall.contentBookMark(this, it1, !item.isBookmarked) { success, message ->
                    if (success) {
                        item.isBookmarked = !item.isBookmarked
                        val msg = if (item.isBookmarked) {
                            "Added To Bookmarks"
                        } else {
                            "Removed From Bookmarks"
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        contentDetails.removeAt(position)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        binding.rvSavedItems.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvSavedItems.adapter = adapter

        // Add scroll listener for pagination
        binding.rvSavedItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()

                if (!isLoading && lastVisible == contentDetails.size - 1 && contentDetails.size < totalCount) {
                    fetchContent(skip, selectedContentType)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        skip = 0
        fetchContent(skip, selectedContentType)
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
        filterContent(selectedString)
    }

    private fun filterContent(type: String) {
        contentDetails.clear()
        /*selectedContentType = if (type.equals("All", ignoreCase = true))
            type.lowercase()
        else
            type.uppercase()
        skip = 0
        fetchContent(skip, selectedContentType)*/

        //local filter
        if (type == "All") {
            contentDetails.addAll(allContentDetails)
        } else {
            contentDetails.addAll(
                allContentDetails.filter { it.contentType.equals(type, ignoreCase = true) }
            )
        }
        adapter.notifyDataSetChanged()
    }

    private fun fetchContent(skipValue: Int, contentType: String) {
        if (skipValue == 0) {
            contentDetails.clear()
            allContentDetails.clear()
        }
        Utils.showLoader(this)
        isLoading = true
        val call = apiService.getBookmarkedContent(
            sharedPreferenceManager.accessToken,
            limit,
            skipValue
        )

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                Utils.dismissLoader(this@SavedItemListActivity)
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonString = response.body()?.string()

                    val responseObj: BookmarkResponse =
                        gson.fromJson(jsonString, BookmarkResponse::class.java)
                    totalCount = responseObj.data?.count ?: 0

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
                Utils.dismissLoader(this@SavedItemListActivity)
                handleNoInternetView(t)
            }
        })
    }
}