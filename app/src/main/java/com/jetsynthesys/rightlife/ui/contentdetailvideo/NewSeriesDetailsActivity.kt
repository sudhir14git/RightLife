package com.jetsynthesys.rightlife.ui.contentdetailvideo

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeDetail.EpisodeDetailContentResponse
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeDetail.NextEpisode
import com.jetsynthesys.rightlife.databinding.ActivityNewSeriesDetailsBinding
import com.jetsynthesys.rightlife.shortVibrate
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleLikeRequest
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.CommonAPICall.updateViewCount
import com.jetsynthesys.rightlife.ui.Wellness.SeriesListAdapter
import com.jetsynthesys.rightlife.ui.contentdetailvideo.model.Episode
import com.jetsynthesys.rightlife.ui.contentdetailvideo.model.SeriesResponse
import com.jetsynthesys.rightlife.ui.therledit.ArtistsDetailsActivity
import com.jetsynthesys.rightlife.ui.therledit.ViewCountRequest
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit

class NewSeriesDetailsActivity : BaseActivity() {
    private var episodeId: String? = null
    private var isPlaying = false
    private val handler = Handler()
    private lateinit var mediaPlayer: MediaPlayer
    private var isExpanded = false
    private lateinit var player: ExoPlayer
    private lateinit var binding: ActivityNewSeriesDetailsBinding
    private var contentTypeForTrack: String = ""
    private lateinit var ContentResponseObj: EpisodeDetailContentResponse
    private lateinit var seriesId: String
    private var lastPosition: Float = 0f
    private var isBookmarked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewSeriesDetailsBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        var contentId = intent.getStringExtra("contentId")
        seriesId = intent.getStringExtra("seriesId").toString()
         episodeId = intent.getStringExtra("episodeId")
        //API Call
        episodeId?.let { getSeriesDetails(seriesId, it) }
        getSeriesWithEpisodes(seriesId)

        val viewCountRequest = ViewCountRequest()
        viewCountRequest.id = seriesId
        viewCountRequest.userId = sharedPreferenceManager.userId
        updateViewCount(this, viewCountRequest)

        binding.icBackDialog.setOnClickListener {
            finish()
        }

        binding.icBookmark.setOnClickListener {
            CommonAPICall.contentBookMark(
                this,
                seriesId,
                isBookmarked,
                episodeId!!,
                contentTypeForTrack
            ) { success, message ->
                if (success) {
                    isBookmarked = !isBookmarked
                    val msg = if (isBookmarked) {
                        "Added To Your Saved Items"
                    } else {
                        "Removed From Saved Items"
                    }
                    showCustomToast(msg, isBookmarked)
                    binding.icBookmark.setImageResource(if (isBookmarked) R.drawable.ic_save_article_active else R.drawable.ic_save_article)
                } else {
                    Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    // get single content details

    private fun getSeriesDetails(seriesId: String, episodeId: String) {
        Utils.showLoader(this)

        val call = apiService.getSeriesEpisodesDetails(
            sharedPreferenceManager.accessToken,
            seriesId,
            episodeId
        )

        // Handle the response
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Utils.dismissLoader(this@NewSeriesDetailsActivity)
                if (response.isSuccessful) {
                    try {
                        if (response.body() != null) {
                            val successMessage = response.body()!!.string()
                            println("Request successful: $successMessage")
                            //Log.d("API Response", "User Details: " + response.body().toString());
                            val gson = Gson()
                            val jsonResponse = gson.toJson(response.body().toString())
                            Log.d("API Response", "Content Details: $jsonResponse")
                            ContentResponseObj = gson.fromJson<EpisodeDetailContentResponse>(
                                successMessage,
                                EpisodeDetailContentResponse::class.java
                            )
                            setcontentDetails(ContentResponseObj)

                            //getSeriesWithEpisodes(ContentResponseObj.getData().getId());
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
                Utils.dismissLoader(this@NewSeriesDetailsActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun setcontentDetails(contentResponseObj: EpisodeDetailContentResponse) {
        binding.tvContentTitle.text = contentResponseObj.data?.title ?: ""
        binding.tvContentDesc.text = contentResponseObj.data?.desc
        binding.category.text = contentResponseObj.data?.tags?.get(0)?.name ?: ""
        binding.tvTime.text = contentResponseObj.data?.meta?.let { formatTimeInMinSec(it.duration) }

        if (contentResponseObj != null) {
            //binding.authorName.setText(contentResponseObj.data.artist.get(0).firstName + " " + contentResponseObj.data.artist.get(0).lastName)
            isBookmarked = contentResponseObj.data.isBookmarked
            binding.icBookmark.setImageResource(if (isBookmarked) R.drawable.ic_save_article_active else R.drawable.ic_save_article)
            setArtistname(contentResponseObj)

            Glide.with(applicationContext)
                .load(
                    ApiClient.CDN_URL_QA + contentResponseObj.data.artist.firstOrNull()?.profilePicture
                )
                .placeholder(R.drawable.rl_profile)
                .error(R.drawable.rl_profile)
                .circleCrop()
                .into(binding.profileImage)
            setModuleColor(contentResponseObj.data.moduleId)
            binding.category.text = contentResponseObj.data.tags.get(0).name

            /*if (contentResponseObj?.data != null && contentResponseObj.data.youtubeUrl != null && !contentResponseObj.data.youtubeUrl.isEmpty()) {
                val videoId: String = extractVideoId(contentResponseObj.data.youtubeUrl).toString()

                if (videoId != null) {
                    Log.e("YouTube", "video ID - call player$videoId")
                    setupYouTubePlayer(videoId)

                    //getLifecycle().addObserver(binding.youtubevideoPlayer);
                } else {
                    Log.e("YouTube", "Invalid video ID")
                    //Provide user feedback
                }
                contentTypeForTrack = "VIDEO"
            }else */
            if (contentResponseObj.data.type.equals("AUDIO", ignoreCase = true)) {
                // For Audio Player
                setupMusicPlayer(contentResponseObj)
                binding.rlPlayerMusicMain.visibility = View.VISIBLE
                binding.rlVideoPlayerMain.visibility = View.GONE
                binding.tvHeaderHtw.text = "Audio"
                contentTypeForTrack = "AUDIO"
            } else if (contentResponseObj.data.type.equals("VIDEO", ignoreCase = true)) {
                if (contentResponseObj.data != null && contentResponseObj.data.youtubeUrl != null && !contentResponseObj.data.youtubeUrl.isEmpty()) {
                    val videoId: String =
                        extractVideoId(contentResponseObj.data.youtubeUrl).toString()

                    if (videoId != null) {
                        Log.e("YouTube", "video ID - call player$videoId")
                        setupYouTubePlayer(videoId)

                        //getLifecycle().addObserver(binding.youtubevideoPlayer);
                    } else {
                        Log.e("YouTube", "Invalid video ID")
                        //Provide user feedback
                    }
                    contentTypeForTrack = "VIDEO"
                } else {
                    // For video Player
                    initializePlayer(contentResponseObj.data.previewUrl)
                    binding.rlVideoPlayerMain.visibility = View.VISIBLE
                    binding.rlPlayerMusicMain.visibility = View.GONE
                    binding.tvHeaderHtw.text = "Video"
                    contentTypeForTrack = "VIDEO"
                }
            }
            setReadMoreView(contentResponseObj.data.desc)

               binding.imageLikeArticle.setOnClickListener { v ->
                   v.shortVibrate()
                   binding.imageLikeArticle.setImageResource(R.drawable.like_article_active)
                   if (contentResponseObj.data.isLike) {
                       binding.imageLikeArticle.setImageResource(R.drawable.like)
                       contentResponseObj.data.isLike = false
                       postContentLike(contentResponseObj.data._id, false)
                   } else {
                       binding.imageLikeArticle.setImageResource(R.drawable.ic_like_receipe)
                       contentResponseObj.data.isLike = true
                       postContentLike(contentResponseObj.data._id, true)
                   }
               }
            binding.imageShareArticle.setOnClickListener { shareIntent() }
            if (contentResponseObj.data.isLike) {
                binding.imageLikeArticle.setImageResource(R.drawable.ic_like_receipe)
            }
            binding.imageShareArticle.setOnClickListener { shareIntent() }
            binding.txtLikeCount.text = contentResponseObj.data.likeCount.toString()
        }


        if (contentResponseObj.data.nextEpisode != null) {
            val nextEpisode = contentResponseObj.data.nextEpisode
            binding.cardviewEpisodeSingle.visibility = View.VISIBLE
            //binding.txtEpisodesSection.setText("Next Episode" + contentResponseObj.data.episodeNumber)
            binding.itemText.text = nextEpisode.title // Use the same TextView for the title
            binding.category2.text = nextEpisode.tags.get(0).name
            setAuthorname(nextEpisode)

            Glide.with(this)
                .load(ApiClient.CDN_URL_QA + nextEpisode.thumbnail.url)
                .into(binding.itemImage) // Use the same ImageView for the thumbnail
            // ... (set other views for the next episode using the same IDs)

            binding.cardviewEpisodeSingle.setOnClickListener {
                val intent = Intent(this, NewSeriesDetailsActivity::class.java)
                intent.putExtra("seriesId", nextEpisode.contentId)
                intent.putExtra("episodeId", nextEpisode._id)
                startActivity(intent)
            }
        } else {
            // Handle case where there is no next episode
            binding.cardviewEpisodeSingle.visibility = View.GONE


        }

        logVideoOpenEvent(this, contentResponseObj, episodeId, AnalyticsEvent.SeriesEpisode_Open)
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


    private fun initializePlayer(previewUrl: String) {
        // Create a new ExoPlayer instance
        player = ExoPlayer.Builder(this).build()
        player.playWhenReady = true
        binding.exoPlayerView.setPlayer(player)
        val videoUri = Uri.parse(
            ApiClient.CDN_URL_QA + previewUrl
        )
        Log.d("Received Content type", "Video URL: " + videoUri)
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSourceFactory(this)
        ).createMediaSource(MediaItem.fromUri(videoUri))
        player.setMediaSource(mediaSource)
        // Prepare the player and start playing automatically
        player.prepare()
        player.play()
    }


    fun setModuleColor(moduleId: String) {
        if (moduleId.equals("EAT_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.eatright)
            binding.imgModuleTag.imageTintList = colorStateList
        } else if (moduleId.equals("THINK_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.thinkright)
            binding.imgModuleTag.imageTintList = colorStateList
        } else if (moduleId.equals("SLEEP_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.sleepright)
            binding.imgModuleTag.imageTintList = colorStateList
        } else if (moduleId.equals("MOVE_RIGHT", ignoreCase = true)) {
            val colorStateList = ContextCompat.getColorStateList(this, R.color.moveright)
            binding.imgModuleTag.imageTintList = colorStateList
        }
    }


    // For music player and audio content
    private fun setupMusicPlayer(moduleContentDetail: EpisodeDetailContentResponse) {
        val backgroundImage = findViewById<ImageView>(R.id.backgroundImage)
        binding.rlPlayerMusicMain.visibility = View.VISIBLE


        if (moduleContentDetail != null) {
            GlideApp.with(this@NewSeriesDetailsActivity)
                .load(ApiClient.CDN_URL_QA + moduleContentDetail.data.thumbnail.url) //episodes.get(1).getThumbnail().getUrl()
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(backgroundImage)
        }


        //val previewUrl = "media/cms/content/series/64cb6d97aa443ed535ecc6ad/45ea4b0f7e3ce5390b39221f9c359c2b.mp3"
        val url = ApiClient.VIDEO_CDN_URL + (moduleContentDetail.data?.url
            ?: "") //episodes.get(1).getPreviewUrl();//"https://www.example.com/your-audio-file.mp3";  // Replace with your URL
        Log.d("API Response", "Sleep aid URL: $url")
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync() // Load asynchronously
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to load audio", Toast.LENGTH_SHORT).show()
        }

        mediaPlayer.setOnPreparedListener { mp: MediaPlayer? ->
            mediaPlayer.start()
            binding.seekBar.max = mediaPlayer.duration
            isPlaying = true
            binding.playPauseButton.setImageResource(R.drawable.ic_sound_pause)
            // Update progress every second
            handler.post(updateProgress)
        }
        // Play/Pause Button Listener
        binding.playPauseButton.setOnClickListener { v: View? ->
            if (isPlaying) {
                mediaPlayer.pause()
                binding.playPauseButton.setImageResource(R.drawable.ic_sound_play)
                handler.removeCallbacks(updateProgress)
            } else {
                mediaPlayer.start()
                binding.playPauseButton.setImageResource(R.drawable.ic_sound_pause)
                //updateProgress();
                handler.post(updateProgress)
            }
            isPlaying = !isPlaying
        }
        mediaPlayer.setOnCompletionListener { mp: MediaPlayer? ->
            Toast.makeText(this, "Playback finished", Toast.LENGTH_SHORT).show()
            handler.removeCallbacks(updateProgress)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    private val updateProgress: Runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying) {
                val currentPosition: Int = mediaPlayer.currentPosition
                val totalDuration: Int = mediaPlayer.duration

                // Update seek bar and progress bar
                binding.seekBar.progress = currentPosition
                // Update Circular ProgressBar
                val progressPercent = ((currentPosition / totalDuration.toFloat()) * 100).toInt()
                binding.circularProgressBar.progress = progressPercent


                // Update time display
                val timeFormatted = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(currentPosition.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(currentPosition.toLong()) % 60
                )
                binding.currentTime.text = timeFormatted

                // Update every second
                handler.postDelayed(this, 1000)
            }
        }
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

        val shareText =
            "Saw this on RightLife and thought of you, it’s got health tips that actually make sense. " +
                    "Check it out here. " +
                    "\nPlay Store Link https://play.google.com/store/apps/details?id=${packageName} " +
                    "\nApp Store Link https://apps.apple.com/app/rightlife/id6444228850"


        intent.putExtra(Intent.EXTRA_TEXT, shareText)

        startActivity(Intent.createChooser(intent, "Share"))
    }


    // more like this content
    /*private fun getMoreLikeContent(contentid: String) {
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
                                setupListData(ResponseObj.data.likeList)
                                if (ResponseObj.data.likeList.size < 5) {
                                    binding.tvViewAll.setVisibility(View.GONE)
                                } else {
                                    binding.tvViewAll.setVisibility(View.VISIBLE)
                                }
                            } else {
                                binding.rlMoreLikeSection.setVisibility(View.GONE)
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
    }*/

    /*    private fun setupListData(contentList: List<Like>) {
            binding.rlMoreLikeSection.setVisibility(View.VISIBLE)
            val adapter = RLEditDetailMoreAdapter(this, contentList)
            val horizontalLayoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerView.setLayoutManager(horizontalLayoutManager)
            binding.recyclerView.setAdapter(adapter)
        }*/


    private fun playPlayer() {
        player.play()
        isPlaying = true
        binding.playPauseButton.setImageResource(R.drawable.ic_notifications_black_24dp) // Change to pause icon
    }

    private fun pausePlayer() {
        player.pause()
        isPlaying = false
        binding.playPauseButton.setImageResource(R.drawable.ic_play) // Change to play icon
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            //  initializePlayer();
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if (::player.isInitialized) {
            callTrackAPI(player.currentPosition.toDouble() / 1000)
            player.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            callTrackAPI(mediaPlayer.currentPosition.toDouble() / 1000)
            mediaPlayer.release()
        } else {
            callTrackAPI((lastPosition).toDouble())
        }
        handler.removeCallbacks(updateProgress)
        Log.d("contentDetails", "onDestroyCalled")
    }

    private fun callTrackAPI(watchDuration: Double) {
        // ✅ Check if ContentResponseObj is initialized before accessing
        if (!::ContentResponseObj.isInitialized) {
            Log.d("contentDetails", "ContentResponseObj not initialized, skipping track API")
            return
        }

        val contentData = ContentResponseObj.data
        //if ((contentData.meta.duration.toDouble() - watchDuration).toInt() > 10)
            CommonAPICall.postSeriesContentPlayedProgress(
                this,
                contentData.meta.duration.toDouble(),
                contentData.contentId,
                watchDuration,
                contentData.moduleId,
                "SERIES",
                contentData._id
            )
    }


    private fun extractVideoId(youtubeUrl: String): String? {
        return try {
            val uri = URI(youtubeUrl)
            val host = uri.host ?: return null

            when {
                host.contains("youtu.be") -> {
                    // Example: https://youtu.be/VIDEO_ID
                    uri.path?.substring(1)
                }

                host.contains("youtube.com") -> {
                    val query = uri.query
                    if (query != null) {
                        val params = query.split("&")
                        for (param in params) {
                            val keyValue = param.split("=")
                            if (keyValue.size == 2 && keyValue[0] == "v") {
                                return keyValue[1]
                            }
                        }
                    }

                    // Example: https://www.youtube.com/embed/VIDEO_ID
                    val path = uri.path ?: return null
                    val segments = path.split("/")
                    if (segments.contains("embed")) {
                        return segments.lastOrNull()
                    }

                    null
                }

                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun setupYouTubePlayer(videoId: String) {
        binding.youtubevideoPlayer.visibility = View.VISIBLE
        binding.youtubevideoPlayer.addYouTubePlayerListener(object :
            AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f)
                Log.d("YouTube", "Video loaded: $videoId")
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                // This is called every second during playback
                lastPosition = second
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerState) {
                Log.d("YouTube", "Player state changed: $state")
                if (state == PlayerState.UNSTARTED) {
                    Log.e("YouTube", "Player error")
                    //Handle the error
                }
                super.onStateChange(youTubePlayer, state)
            }
        })
    }

    private fun formatTimeInMinSec(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60

        return when {
            mins > 0 && secs > 0 -> String.format("%.2f min", mins + secs / 100.0)
            mins > 0 -> "$mins min"
            else -> "$secs sec"
        }
    }

    private fun setArtistname(contentResponseObj: EpisodeDetailContentResponse?) {
        //if (binding != null && binding.tvAuthorName != null && contentResponseObj != null && contentResponseObj.data != null && contentResponseObj.data.artist != null && !contentResponseObj.data.artist.isEmpty())
        if (contentResponseObj != null && contentResponseObj.data != null && contentResponseObj.data.artist.size > 0) {
            var name = ""
            if (contentResponseObj.data.artist[0].firstName != null) {
                name = contentResponseObj.data.artist[0].firstName
            }
            if (contentResponseObj.data.artist[0].lastName != null) {
                name += (if (name.isEmpty()) "" else " ") + contentResponseObj.data.artist[0].lastName
            }

            binding.tvArtistname.text = name
            binding.llAuthorMain.setOnClickListener {
                startActivity(Intent(this, ArtistsDetailsActivity::class.java).apply {
                    putExtra("ArtistId", contentResponseObj.data.artist[0]._id)
                })
            }
        } else if (binding != null && binding.tvAuthorName != null) {
            binding.tvArtistname.text = "" // or set some default value
        }
    }

    private fun setAuthorname(nextEpisode: NextEpisode) {
        //if (binding != null && binding.tvAuthorName != null && contentResponseObj != null && contentResponseObj.data != null && contentResponseObj.data.artist != null && !contentResponseObj.data.artist.isEmpty())
        if (nextEpisode != null && nextEpisode.artist != null) {
            var name = ""
            if (nextEpisode.artist[0].firstName != null) {
                name = nextEpisode.artist[0].firstName
            }
            if (nextEpisode.artist[0].lastName != null) {
                name += (if (name.isEmpty()) "" else " ") + nextEpisode.artist[0].lastName
            }

            binding.tvArtistname.text = name
            binding.tvArtistname.setOnClickListener {
                startActivity(Intent(this, ArtistsDetailsActivity::class.java).apply {
                    putExtra("ArtistId", nextEpisode.artist[0]._id)
                })
            }
        } else if (binding != null && binding.tvAuthorName != null) {
            binding.tvAuthorName.text = "" // or set some default value
        }
    }

    private fun getSeriesWithEpisodes(seriesId: String) {
        val call =
            apiService.getSeriesWithEpisodes(sharedPreferenceManager.accessToken, seriesId, true)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                Utils.dismissLoader(this@NewSeriesDetailsActivity)
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())
                    val seriesResponseModel =
                        gson.fromJson(jsonResponse, SeriesResponse::class.java)
                    setupEpisodeListData(
                        seriesResponseModel.data.episodes,
                        seriesResponseModel.data.categoryName,
                        seriesResponseModel.data.moduleId
                    )
                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                Utils.dismissLoader(this@NewSeriesDetailsActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun setupEpisodeListData(contentList: ArrayList<Episode>, categoryName: String, moduleId: String) {
        val adapter = SeriesListAdapter(this, contentList, categoryName,moduleId)
        val horizontalLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvAllEpisodes.setLayoutManager(horizontalLayoutManager)
        binding.rvAllEpisodes.setAdapter(adapter)
    }

    fun logVideoOpenEvent(context: NewSeriesDetailsActivity, contentResponseObj: EpisodeDetailContentResponse, contentId: String?, eventName: String = AnalyticsEvent.Video_Open) {
        runCatching {
            val data = contentResponseObj?.data

            val params = mutableMapOf<String, Any>()

            // helper for non-null values trimmed & limited to 100 chars
            fun putSafe(key: String, value: Any?) {
                when (value) {
                    is String -> if (value.isNotBlank()) params[key] = value.trim().take(100)
                    is Number, is Boolean -> params[key] = value
                }
            }

            putSafe(AnalyticsParam.VIDEO_ID, contentId)
            putSafe(AnalyticsParam.CONTENT_MODULE, data?.moduleId ?: "unknown_module")
            putSafe(AnalyticsParam.CONTENT_TYPE, data?.title ?: "unknown_type")
            putSafe(AnalyticsParam.SERIES_TYPE, data?.type ?: "unknown_type")

            if (params.isEmpty()) return // nothing to log

            AnalyticsLogger.logEvent(
                    context,
                    AnalyticsEvent.SeriesEpisode_Open,
                    params
            )
        }.onFailure { e ->
            Log.e("AnalyticsLogger", "Video_Open event failed: ${e.localizedMessage}", e)
        }
    }
}