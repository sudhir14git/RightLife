package com.jetsynthesys.rightlife.ui.contentdetailvideo

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeResponseModel
import com.jetsynthesys.rightlife.apimodel.modulecontentdetails.ModuleContentDetail
import com.jetsynthesys.rightlife.apimodel.morelikecontent.Like
import com.jetsynthesys.rightlife.apimodel.morelikecontent.MoreLikeContentResponse
import com.jetsynthesys.rightlife.databinding.ActivitySeriesListBinding
import com.jetsynthesys.rightlife.shortVibrate
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleBookmarkRequest
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleLikeRequest
import com.jetsynthesys.rightlife.ui.CommonAPICall.updateViewCount
import com.jetsynthesys.rightlife.ui.Wellness.SeriesListAdapter
import com.jetsynthesys.rightlife.ui.YouMayAlsoLikeAdapter
import com.jetsynthesys.rightlife.ui.contentdetailvideo.model.Episode
import com.jetsynthesys.rightlife.ui.contentdetailvideo.model.SeriesResponse
import com.jetsynthesys.rightlife.ui.therledit.ArtistsDetailsActivity
import com.jetsynthesys.rightlife.ui.therledit.ViewAllActivity
import com.jetsynthesys.rightlife.ui.therledit.ViewCountRequest
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeriesListActivity : BaseActivity() {
    private var isPlaying = false
    private val handler = Handler()
    private lateinit var mediaPlayer: MediaPlayer
    private var isExpanded = false
    private lateinit var player: ExoPlayer
    private lateinit var binding: ActivitySeriesListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySeriesListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var contentId = intent.getStringExtra("contentId")
        //API Call
        if (contentId != null) {
            getContendetails(contentId)
            // Episode list
            getSeriesWithEpisodes(contentId)
            // get morelike content
            getMoreLikeContent(contentId)
            binding.tvViewAll.setOnClickListener(View.OnClickListener { view: View? ->
                val intent1 = Intent(this, ViewAllActivity::class.java)
                intent1.putExtra("ContentId", contentId)
                startActivity(intent1)
            })
        }

        val viewCountRequest = ViewCountRequest()
        viewCountRequest.id = contentId
        viewCountRequest.userId = sharedPreferenceManager.userId
        updateViewCount(this, viewCountRequest)

        binding.icBackDialog.setOnClickListener {
            finish()
        }
    }

    // get single content details

    private fun getContendetails(categoryId: String) {
        Utils.showLoader(this)
        // Make the GET request
        val call: Call<ResponseBody> = apiService.getRLDetailpage(
            sharedPreferenceManager.accessToken,
            categoryId

        )

        // Handle the response
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Utils.dismissLoader(this@SeriesListActivity)
                if (response.isSuccessful) {
                    try {
                        if (response.body() != null) {
                            val successMessage = response.body()!!.string()
                            val gson = Gson()
                            val jsonResponse = gson.toJson(response.body().toString())
                            val contentResponseObj = gson.fromJson<ModuleContentDetail>(
                                successMessage,
                                ModuleContentDetail::class.java
                            )
                            setcontentDetails(contentResponseObj)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            val errorMessage = response.errorBody()!!.string()
                            println("Request failed with error: $errorMessage")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Utils.dismissLoader(this@SeriesListActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun setcontentDetails(contentResponseObj: ModuleContentDetail?) {
        binding.llContainerMain.visibility = View.VISIBLE
        binding.tvContentTitle.text = contentResponseObj?.data?.title
        binding.tvContentDesc.text = contentResponseObj?.data?.desc
        if (contentResponseObj != null) {
            val artistList = contentResponseObj.data.artist
            if (artistList.size > 0) {
                binding.authorName.text = artistList[0].firstName + " " + artistList[0].lastName
                Glide.with(applicationContext)
                    .load(ApiClient.CDN_URL_QA + artistList[0].profilePicture)
                    .placeholder(R.drawable.rl_profile)
                    .error(R.drawable.rl_profile)
                    .circleCrop()
                    .into(binding.profileImage)
                binding.authorName.setOnClickListener {
                    startActivity(Intent(this, ArtistsDetailsActivity::class.java).apply {
                        putExtra("ArtistId", artistList[0].id)
                    })
                }
            }

            Glide.with(applicationContext)
                .load(
                    ApiClient.CDN_URL_QA + contentResponseObj.data.thumbnail.url
                )
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(binding.imgContentview)

            setModuleColor(contentResponseObj.data.moduleId)
            binding.category.text = contentResponseObj.data.tags.get(0).name

            setReadMoreView(contentResponseObj.data.desc)

            binding.imageLikeArticle.setOnClickListener { v ->
                v.shortVibrate()
                if (contentResponseObj.data.like) {
                    binding.imageLikeArticle.setImageResource(R.drawable.like_article_inactive)
                    contentResponseObj.data.like = false
                    postContentLike(contentResponseObj.data.id, false)
                } else {
                    binding.imageLikeArticle.setImageResource(R.drawable.like_article_active)
                    contentResponseObj.data.like = true
                    postContentLike(contentResponseObj.data.id, true)
                }
            }
            if (contentResponseObj.data.like) {
                binding.imageLikeArticle.setImageResource(R.drawable.ic_like_receipe)
            }
            binding.imageShareArticle.setOnClickListener { shareIntent() }
            binding.txtLikeCount.text = contentResponseObj.data.likeCount.toString()


            binding.icBookmark.setOnClickListener {
                if (contentResponseObj != null) {
                    if (contentResponseObj.data.bookmarked) {
                        contentResponseObj.data.bookmarked = false
                        binding.icBookmark.setImageResource(R.drawable.ic_save_article)
                        postArticleBookMark(contentResponseObj.data.id, false)
                    } else {
                        contentResponseObj.data.bookmarked = true
                        binding.icBookmark.setImageResource(R.drawable.ic_save_article_active)
                        postArticleBookMark(contentResponseObj.data.id, true)
                    }
                }
            }
            if (contentResponseObj.data?.bookmarked == true) {
                binding.icBookmark.setImageResource(R.drawable.ic_save_article_active)
            }
        }

    }


    private fun setReadMoreView(desc: String?) {
        if (desc.isNullOrEmpty()) {
            binding.tvContentDesc.text = ""
            binding.llReadMore.visibility = View.GONE
            return
        }
        val shortDescription = desc.take(100) + "..."

        // If description is short, show full text and hide toggle
        if (desc.length <= 100) {
            binding.tvContentDesc.text = desc
            binding.llReadMore.visibility = View.GONE
        } else {
            binding.tvContentDesc.text = shortDescription
            binding.llReadMore.visibility = View.VISIBLE
            binding.readToggle.text = "Read More"
            binding.imgReadToggle.setImageResource(R.drawable.icon_arrow_article)
            isExpanded = false

            binding.llReadMore.setOnClickListener {
                isExpanded = !isExpanded
                if (isExpanded) {
                    binding.tvContentDesc.text = desc
                    binding.readToggle.text = "Read Less"
                    binding.imgReadToggle.setImageResource(R.drawable.icon_arrow_article)
                    binding.imgReadToggle.rotation = 180f // Rotate by 180 degrees
                } else {
                    binding.tvContentDesc.text = shortDescription
                    binding.readToggle.text = "Read More"
                    binding.imgReadToggle.setImageResource(R.drawable.icon_arrow_article)
                    binding.imgReadToggle.rotation = 360f // Rotate by 180 degrees
                }
            }
        }
    }


    fun setModuleColor(moduleId: String) {
        if (moduleId.equals("EAT_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.eatright)
            binding.imgModuleTag.imageTintList = colorStateList
            binding.imgModule.setImageResource(R.drawable.ic_db_eatright)
            binding.tvModulename.text = AppConstants.EAT_RIGHT
        } else if (moduleId.equals("THINK_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.thinkright)
            binding.imgModule.setImageResource(R.drawable.ic_db_thinkright)
            binding.imgModuleTag.imageTintList = colorStateList
            binding.tvModulename.text = AppConstants.THINK_RIGHT
        } else if (moduleId.equals("SLEEP_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.sleepright)
            binding.imgModuleTag.imageTintList = colorStateList
            binding.imgModule.setImageResource(R.drawable.ic_db_sleepright)
            binding.tvModulename.text = AppConstants.SLEEP_RIGHT
        } else if (moduleId.equals("MOVE_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.moveright)
            binding.imgModuleTag.imageTintList = colorStateList
            binding.imgModule.setImageResource(R.drawable.ic_db_moveright)
            binding.tvModulename.text = AppConstants.MOVE_RIGHT
        }
    }


    // For music player and audio content


    // post Bookmark api
    private fun postArticleBookMark(contentId: String, isBookmark: Boolean) {
        val request = ArticleBookmarkRequest(contentId, isBookmark)
        // Make the API call
        val call = apiService.ArticleBookmarkRequest(sharedPreferenceManager.accessToken, request)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    val articleLikeResponse = response.body()
                    Log.d(
                        "API Response",
                        "Article Bookmark response: $articleLikeResponse"
                    )
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    Utils.showCustomToast(this@SeriesListActivity, response.message())
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    // post like api
    private fun postContentLike(contentId: String, isLike: Boolean) {
        val request = ArticleLikeRequest(contentId, isLike)
        // Make the API call
        val call = apiService.ArticleLikeRequest(sharedPreferenceManager.accessToken, request)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    val articleLikeResponse = response.body()
                    Log.d("API Response", "Article response: $articleLikeResponse")
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun shareIntent() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")

        val shareText = "Saw this on RightLife and thought of you, it’s got health tips that actually make sense. " +
                "Check it out here. " +
                "\nPlay Store Link https://play.google.com/store/apps/details?id=${packageName} " +
                "\nApp Store Link https://apps.apple.com/app/rightlife/id6444228850"


        intent.putExtra(Intent.EXTRA_TEXT, shareText)

        startActivity(Intent.createChooser(intent, "Share"))
    }


    // get series list
    private fun getSeriesWithEpisodes(seriesId: String) {
        val call =
            apiService.getSeriesWithEpisodes(sharedPreferenceManager.accessToken, seriesId, true)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                Utils.dismissLoader(this@SeriesListActivity)
                if (response.isSuccessful && response.body() != null) {
                    val affirmationsResponse = response.body()
                    Log.d("API Response", "Wellness:episodes $affirmationsResponse")
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val episodeResponseModel = gson.fromJson(
                        jsonResponse,
                        EpisodeResponseModel::class.java
                    )
                    val seriesResponseModel =
                        gson.fromJson(jsonResponse, SeriesResponse::class.java)
                    //Log.d("API Response body", "Episode:SeriesList " + episodeResponseModel.getData().getEpisodes().get(0).getTitle());
                    //setupWellnessContent(wellnessApiResponse.getData().getContentList());
                    setupEpisodeListData(seriesResponseModel.data.episodes)
                    setupArtistList(seriesResponseModel)
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                Utils.dismissLoader(this@SeriesListActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun setupArtistList(seriesResponseModel: SeriesResponse?) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerArtists.layoutManager = layoutManager
        val adapter =
            seriesResponseModel?.data?.let {
                ArtistAdapter(
                    this,
                    it.artist
                )
            } // artistList is List<Artist>
        binding.recyclerArtists.adapter = adapter
    }

    private fun setupEpisodeListData(contentList: ArrayList<Episode>) {
        val adapter = SeriesListAdapter(this, contentList)
        val horizontalLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewSerieslist.setLayoutManager(horizontalLayoutManager)
        binding.recyclerViewSerieslist.setAdapter(adapter)
    }

    // more like this content
    private fun getMoreLikeContent(contentid: String) {
        val call =
            apiService.getMoreLikeContent(sharedPreferenceManager.accessToken, contentid, 0, 5)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        // Parse the raw JSON into the LikeResponse class
                        val jsonString = response.body()!!.string()
                        val gson = Gson()

                        val ResponseObj = gson.fromJson(
                            jsonString,
                            MoreLikeContentResponse::class.java
                        )
                        if (ResponseObj != null) {
                            if (!ResponseObj.data.likeList.isEmpty() && ResponseObj.data.likeList.size > 0) {
                                binding.rlMoreLikeSection.visibility = View.VISIBLE
                                setupListData(ResponseObj.data.likeList)
                                if (ResponseObj.data.likeList.size < 5) {
                                    binding.tvViewAll.visibility = View.GONE
                                } else {
                                    binding.tvViewAll.visibility = View.VISIBLE
                                }
                            } else {
                                binding.rlMoreLikeSection.visibility = View.GONE
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        Log.e("JSON_PARSE_ERROR", "Error parsing response: " + e.message)
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun setupListData(contentList: List<Like>) {
        binding.rlMoreLikeSection.visibility = View.VISIBLE
        val adapter = YouMayAlsoLikeAdapter(this, contentList)
        val horizontalLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.setLayoutManager(horizontalLayoutManager)
        binding.recyclerView.setAdapter(adapter)
    }


}