package com.jetsynthesys.rightlife.ui.Articles

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.morelikecontent.Like
import com.jetsynthesys.rightlife.apimodel.morelikecontent.MoreLikeContentResponse
import com.jetsynthesys.rightlife.databinding.ActivityArticledetailBinding
import com.jetsynthesys.rightlife.shortVibrate
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.Articles.models.Article
import com.jetsynthesys.rightlife.ui.Articles.models.ArticleDetailsResponse
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleBookmarkRequest
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleLikeRequest
import com.jetsynthesys.rightlife.ui.CommonAPICall.trackEpisodeOrContent
import com.jetsynthesys.rightlife.ui.YouMayAlsoLikeAdapter
import com.jetsynthesys.rightlife.ui.therledit.EpisodeTrackRequest
import com.jetsynthesys.rightlife.ui.therledit.ViewAllActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArticlesDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityArticledetailBinding

    // video views
    private var playerView: StyledPlayerView? = null
    private var controlView: PlayerControlView? = null
    private var player: ExoPlayer? = null
    private var progressBar: ProgressBar? = null
    private var fullscreenButton: ImageView? = null
    private var contentId: String? = null
    private var articleDetailsResponse: ArticleDetailsResponse? = null
    private var startTime: Long = 0

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_articledetail)

        binding = ActivityArticledetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startTime = System.currentTimeMillis()

        binding.iconArrowArticle.setOnClickListener { v: View? ->
            if (binding.txtInthisarticleList.visibility == View.VISIBLE) {
                binding.txtInthisarticleList.visibility = View.GONE
                binding.iconArrowArticle.rotation = 360f // Rotate by 180 degrees
            } else {
                binding.txtInthisarticleList.visibility = View.VISIBLE
                binding.iconArrowArticle.rotation = 180f// Rotate by 180 degrees
            }
        }
        binding.txtInthisarticle.setOnClickListener { v: View? ->
            if (binding.txtInthisarticleList.visibility == View.VISIBLE) {
                binding.txtInthisarticleList.visibility = View.GONE
                binding.iconArrowArticle.rotation = 360f
            } else {
                binding.txtInthisarticleList.visibility = View.VISIBLE
                binding.iconArrowArticle.rotation = 180f
            }
        }

        binding.icBackDialog.setOnClickListener { finish() }


        binding.icSaveArticle.setOnClickListener {
            binding.icSaveArticle.setImageResource(R.drawable.ic_save_article_active)
            // Call Save article api
            if (articleDetailsResponse!!.data.bookmarked) {
                binding.icSaveArticle.setImageResource(R.drawable.ic_save_article)
                articleDetailsResponse!!.data.bookmarked = false
                postArticleBookMark(
                    articleDetailsResponse!!.data.id,
                    false,
                    articleDetailsResponse!!.data.contentType
                )
            } else {
                binding.icSaveArticle.setImageResource(R.drawable.ic_save_article_active)
                articleDetailsResponse!!.data.bookmarked = true
                postArticleBookMark(
                    articleDetailsResponse!!.data.id,
                    true,
                    articleDetailsResponse!!.data.contentType
                )
            }
        }

        binding.imageLikeArticle.setOnClickListener { v: View? ->
            v!!.shortVibrate(100)
            binding.imageLikeArticle.setImageResource(R.drawable.like_article_active)
            val currentCount = this.currentCount
            if (articleDetailsResponse!!.data.isLike) {
                binding.imageLikeArticle.setImageResource(R.drawable.like_article_inactive)
                articleDetailsResponse!!.data.isLike = false
                postArticleLike(articleDetailsResponse!!.data.id, false)
                val newCount = currentCount - 1
                binding.txtLikeCount.text = getLikeText(newCount)
            } else {
                binding.imageLikeArticle.setImageResource(R.drawable.like_article_active)
                articleDetailsResponse!!.data.isLike = true
                postArticleLike(articleDetailsResponse!!.data.id, true)
                val newCount = currentCount + 1
                binding.txtLikeCount.text = getLikeText(newCount)
            }
        }


        binding.imageShareArticle.setOnClickListener { shareIntent() }
        binding.txtTopic1.setOnClickListener { shareIntent() }

        binding.txtInthisarticleList.text =
            "• Introduction \n\n• Benefits \n\n• Considerations \n\n• Dosage and Side effects \n\n• Conclusion"

        contentId = intent.getStringExtra("contentId")
        getArticleDetails(contentId)
        getRecommendedContent(contentId)

        binding.tvViewAll.setOnClickListener {
            val intent1 = Intent(this@ArticlesDetailActivity, ViewAllActivity::class.java)
            intent1.putExtra("ContentId", contentId)
            startActivity(intent1)
        }
        // Log the event article opened
        logArticleOpenedEvent()
    }

    private fun getLikeText(count: Int): String {
        return when (count) {
            0 -> "0 like"
            1 -> "1 like"
            else -> "$count likes"
        }
    }

    private val currentCount: Int
        get() {
            try {
                val countText = binding.txtLikeCount.text.toString()
                val numbersOnly = countText.replace("[^0-9]".toRegex(), "")
                return if (numbersOnly.isEmpty()) 0 else numbersOnly.toInt()
            } catch (e: Exception) {
                return 0
            }
        }

    // API call to get article details
    private fun getArticleDetails(contentId: String?) {
        //-----------
        Utils.showLoader(this)
        // Make the API call
        val call = apiService.getArticleDetails(sharedPreferenceManager.accessToken, contentId)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(
                call: Call<JsonElement?>,
                response: Response<JsonElement?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val articleResponse: JsonElement = response.body()!!
                    Log.d("API Response", "Article response: $articleResponse")
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    articleDetailsResponse =
                        gson.fromJson(jsonResponse, ArticleDetailsResponse::class.java)

                    Log.d(
                        "API Response body",
                        "Article Title" + articleDetailsResponse!!.data.title
                    )
                    if (articleDetailsResponse != null && articleDetailsResponse!!.data != null) {
                        handleArticleResponseData(articleDetailsResponse!!)
                    }
                    binding.scrollviewarticle.visibility = View.VISIBLE
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                Utils.dismissLoader(this@ArticlesDetailActivity)
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
                Utils.dismissLoader(this@ArticlesDetailActivity)
            }
        })
    }

    override fun onStop() {
        super.onStop()
        logArticleOpenEvent(this@ArticlesDetailActivity, articleDetailsResponse, contentId)
    }

    private fun handleArticleResponseData(articleDetailsResponse: ArticleDetailsResponse) {
        binding.tvHeaderArticle.text = articleDetailsResponse.data.title
        if (!articleDetailsResponse.data.artist.isEmpty()) {
            val artist = articleDetailsResponse.data.artist[0]
            binding.tvAuthorName.text = String.format("%s %s", artist.firstName, artist.lastName)
            binding.tvAuthorName.setOnClickListener {
                val intent = Intent(this, ArticlesDetailActivity::class.java)
                intent.putExtra("ArtistId", artist.id)
                startActivity(intent)
            }

            if (!this.isFinishing && !this.isDestroyed) Glide.with(this)
                .load(ApiClient.CDN_URL_QA + artist.profilePicture)
                .transform(RoundedCorners(25))
                .placeholder(R.drawable.rl_profile)
                .error(R.drawable.rl_profile)
                .into(binding.authorImage)
        }
        binding.txtArticleDate.text = DateTimeUtils.convertAPIDateMonthFormat(
            articleDetailsResponse.data.createdAt
        )
        binding.txtCategoryArticle.text =
            articleDetailsResponse.data.categoryName //getTags().get(0).getName());
        setModuleColor(binding.imageTag, articleDetailsResponse.data.moduleId)
        binding.txtReadtime.text = articleDetailsResponse.data.readingTime + " min read"
        if (!isFinishing && !isDestroyed) {
            Glide.with(this).load(ApiClient.CDN_URL_QA + articleDetailsResponse.data.url)
                .transform(RoundedCorners(1))
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(binding.articleImageMain)
        }
        HandleArticleListView(articleDetailsResponse.data.article)
        if (articleDetailsResponse.data.tableOfContents != null) {
            binding.llInthisarticle.visibility = View.VISIBLE
            handleInThisArticle(articleDetailsResponse.data.tableOfContents)
        }
        // handle save icon
        if (articleDetailsResponse.data.bookmarked) {
            binding.icSaveArticle.setImageResource(R.drawable.ic_save_article_active)
        } else {
            binding.icSaveArticle.setImageResource(R.drawable.ic_save_article)
        }

        if (articleDetailsResponse.data.isLike) {
            binding.imageLikeArticle.setImageResource(R.drawable.like_article_active)
        } else {
            binding.imageLikeArticle.setImageResource(R.drawable.like_article_inactive)
        }
        if (articleDetailsResponse.data.likeCount != null) {
            binding.txtLikeCount.text = getLikeText(articleDetailsResponse.data.likeCount)
        }

        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(
                articleDetailsResponse.data.summary,
                Html.FROM_HTML_MODE_LEGACY
            )
        } else {
            Html.fromHtml(articleDetailsResponse.data.summary)
        }

        binding.txtKeytakeawayDesc.text = spanned

        // article consumed
        val episodeTrackRequest = EpisodeTrackRequest(
            sharedPreferenceManager.userId, articleDetailsResponse.data.moduleId,
            articleDetailsResponse.data.id, "1.0", "1.0", "TEXT"
        )
        trackEpisodeOrContent(this, episodeTrackRequest)
    }

    private fun handleInThisArticle(tocItems: MutableList<String?>) {
        val builder = SpannableStringBuilder()

        for (i in tocItems.indices) {
            val item = tocItems[i]

            // Add bullet point
            builder.append("• ")

            // Remember start position of the item text
            val itemStart = builder.length

            // Add the item text
            builder.append(item)
            val itemEnd = builder.length

            // Create and apply ClickableSpan
            val position = i
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(p0: View) {
                    val targetView = binding.recyclerViewArticle.layoutManager!!
                        .findViewByPosition(position)
                    if (targetView != null) {
                        binding.scrollviewarticle.smoothScrollTo(0, targetView.top)
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ContextCompat.getColor(
                        this@ArticlesDetailActivity,
                        R.color.color_in_this_article
                    )
                    ds.isUnderlineText = false
                }
            }

            builder.setSpan(clickableSpan, itemStart, itemEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Add spacing (newlines) except after the last item
            if (i < tocItems.size - 1) {
                builder.append("\n\n")
            }
        }

        builder.append("\n") // Final newline

        binding.txtInthisarticleList.text = builder
        binding.txtInthisarticleList.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun HandleArticleListView(articleList: MutableList<Article?>?) {
        val adapter = ArticleListAdapter(this, articleList)
        val horizontalLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewArticle.setLayoutManager(horizontalLayoutManager)
        binding.recyclerViewArticle.setAdapter(adapter)
        //binding.bottomcardview.setVisibility(View.VISIBLE);
    }

    fun setModuleColor(imgTag: ImageView?, moduleId: String) {
        if (moduleId.equals("EAT_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.eatright)
            binding.imageTag.imageTintList = colorStateList
        } else if (moduleId.equals("THINK_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.thinkright)
            binding.imageTag.imageTintList = colorStateList
        } else if (moduleId.equals("SLEEP_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.sleepright)
            binding.imageTag.imageTintList = colorStateList
        } else if (moduleId.equals("MOVE_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.moveright)
            binding.imageTag.imageTintList = colorStateList
        }
    }

    override fun onResume() {
        super.onResume()
        if (player != null) { // Check if player is initialized
            player!!.play() // Resume playback when the activity is resumed
        }
    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            player!!.pause() // Pause playback when the activity is paused
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player!!.release() // Release the player resources
            player = null // Important: Set player to null to avoid memory leaks
        }
        // Log the event article finished
        logArticleFinishedEvent()
    }


    private fun postArticleLike(contentId: String?, isLike: Boolean) {
        val request = ArticleLikeRequest(contentId, isLike)
        // Make the API call
        val call = apiService.ArticleLikeRequest(sharedPreferenceManager.accessToken, request)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val articleLikeResponse: ResponseBody = response.body()!!
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

    private fun postArticleBookMark(contentId: String, isBookmark: Boolean, contentType: String) {
        val request = ArticleBookmarkRequest(contentId, isBookmark, "", contentType)
        // Make the API call
        val call =
            apiService.ArticleBookmarkRequest(sharedPreferenceManager.accessToken, request)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val articleLikeResponse: ResponseBody = response.body()!!
                    Log.d("API Response", "Article Bookmark response: $articleLikeResponse")
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    var message = ""
                    message = if (isBookmark) {
                        "Added To Your Saved Items"
                    } else {
                        "Removed From Saved Items"
                    }
                    this@ArticlesDetailActivity.showCustomToast(message, isBookmark)
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
        intent.type = "text/plain"
        val shareText =
            ("Saw this on RightLife and thought of you, it’s got health tips that actually make sense. Check it out here."
                    + "\nPlay Store Link  https://play.google.com/store/apps/details?id=" + packageName +
                    "\nApp Store Link https://apps.apple.com/app/rightlife/id6444228850")

        intent.putExtra(Intent.EXTRA_TEXT, shareText)

        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun getRecommendedContent(contentId: String?) {
        // Make the API call
        val call =
            apiService.getMoreLikeContent(sharedPreferenceManager.getAccessToken(), contentId, 0, 5)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonString = response.body()!!.string()
                        val gson = Gson()
                        val responseObj = gson.fromJson(
                            jsonString,
                            MoreLikeContentResponse::class.java
                        )

                        if (responseObj != null && responseObj.data != null && responseObj.data
                                .likeList != null
                        ) {
                            val likeList = responseObj.data.likeList

                            if (!likeList.isEmpty()) {
                                setupListData(likeList)

                                if (likeList.size < 5) {
                                    binding.tvViewAll.visibility = View.GONE
                                } else {
                                    binding.tvViewAll.visibility = View.VISIBLE
                                }
                            } else {
                                binding.txtAlsolikeHeader.visibility = View.GONE
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("JSON_PARSE_ERROR", "Error parsing response: " + e.message)
                    }
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun setupListData(contentList: MutableList<Like?>) {
        binding.txtAlsolikeHeader.visibility = View.VISIBLE

        val adapter = YouMayAlsoLikeAdapter(this, contentList as List<Like>)
        val horizontalLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.recyclerViewAlsolike.setLayoutManager(horizontalLayoutManager)
        binding.recyclerViewAlsolike.setAdapter(adapter)
    }


    private fun logArticleOpenedEvent() {
        val params: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        params[AnalyticsParam.ARTICLE_ID] = if (contentId != null) contentId else ""
        AnalyticsLogger.logEvent(this, AnalyticsEvent.ARTICLE_OPENED, params as Map<String, Any>?)
    }

    private fun logArticleFinishedEvent() {
        val params: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        params[AnalyticsParam.ARTICLE_ID] = if (contentId != null) contentId else ""

        AnalyticsLogger.logEvent(this, AnalyticsEvent.ARTICLE_FINISHED, params as Map<String, Any>?)
    }

    private fun logArticleOpenEvent(
        context: Context,
        contentResponseObj: ArticleDetailsResponse?,
        contentId: String?
    ) {
        try {
            val params: MutableMap<String?, Any?> = HashMap()

            // Safely extract nested data once
            val data = contentResponseObj?.data

            val id = contentId?.trim { it <= ' ' } ?: ""
            val type = if (data != null && data.contentType != null) data.contentType
                .trim { it <= ' ' } else ""
            val module = if (data != null && data.moduleId != null) data.moduleId
                .trim { it <= ' ' } else ""

            params[AnalyticsParam.CONTENT_ID] = id
            params[AnalyticsParam.CONTENT_TYPE] = type
            params[AnalyticsParam.CONTENT_MODULE] = module
            val duration = System.currentTimeMillis() - startTime
            params[AnalyticsParam.TOTAL_DURATION] = duration

            if (!params.isEmpty()) {
                AnalyticsLogger.logEvent(
                    context, AnalyticsEvent.Article_Open, params as Map<String, Any>?
                )
            }
        } catch (e: Exception) {
            Log.e("AnalyticsLogger", "Video_Open event failed: " + e.localizedMessage, e)
        }
    }


    companion object {
        private const val VIDEO_URL =
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" // Free content URL
    }
}
