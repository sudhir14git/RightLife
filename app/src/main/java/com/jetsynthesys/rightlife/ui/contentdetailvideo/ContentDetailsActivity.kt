package com.jetsynthesys.rightlife.ui.contentdetailvideo

import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
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
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.modulecontentdetails.ModuleContentDetail
import com.jetsynthesys.rightlife.apimodel.morelikecontent.Like
import com.jetsynthesys.rightlife.apimodel.morelikecontent.MoreLikeContentResponse
import com.jetsynthesys.rightlife.databinding.ActivityContentDetailsBinding
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleBookmarkRequest
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleLikeRequest
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.CommonAPICall.trackEpisodeOrContent
import com.jetsynthesys.rightlife.ui.YouMayAlsoLikeAdapter
import com.jetsynthesys.rightlife.ui.therledit.ArtistsDetailsActivity
import com.jetsynthesys.rightlife.ui.therledit.EpisodeTrackRequest
import com.jetsynthesys.rightlife.ui.therledit.ViewAllActivity
import com.jetsynthesys.rightlife.ui.therledit.ViewCountRequest
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class ContentDetailsActivity : BaseActivity() {
    private var isPlaying = false
    private val handler = Handler()
    private lateinit var mediaPlayer: MediaPlayer
    private var isExpanded = false
    private lateinit var player: ExoPlayer
    private lateinit var binding: ActivityContentDetailsBinding
    private var contentTypeForTrack: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var contentId = intent.getStringExtra("contentId")
        //API Call
        if (contentId != null) {
            getContendetails(contentId)
            // get morelike content
            getMoreLikeContent(contentId)
            binding.tvViewAll.setOnClickListener(View.OnClickListener { view: View? ->
                val intent1 = Intent(this, ViewAllActivity::class.java)
                intent1.putExtra("ContentId", contentId)
                startActivity(intent1)
            })
        }
        binding.icBackDialog.setOnClickListener {
            finish()
        }

        val viewCountRequest = ViewCountRequest()
        viewCountRequest.id = contentId
        viewCountRequest.userId = sharedPreferenceManager.userId
        CommonAPICall.updateViewCount(this, viewCountRequest)
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
                Utils.dismissLoader(this@ContentDetailsActivity)
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
                Utils.dismissLoader(this@ContentDetailsActivity)
                handleNoInternetView(t)
            }
        })
    }

    private fun setcontentDetails(contentResponseObj: ModuleContentDetail?) {

        binding.tvContentTitle.text = contentResponseObj?.data?.title
        binding.tvContentDesc.text = contentResponseObj?.data?.desc
        if (contentResponseObj != null) {
            if (contentResponseObj.data.artist.isNotEmpty()) {
                binding.authorName.text = contentResponseObj.data.artist[0]
                    .firstName + " " + contentResponseObj.data.artist[0]
                    .lastName

                Glide.with(applicationContext)
                    .load(
                        ApiClient.CDN_URL_QA + contentResponseObj.data.artist[0]
                            .profilePicture
                    )
                    .placeholder(R.drawable.rl_profile)
                    .error(R.drawable.rl_profile)// Replace with your placeholder image
                    .circleCrop()
                    .into(binding.profileImage)

                binding.llAuthorMain.setOnClickListener {
                    startActivity(Intent(this, ArtistsDetailsActivity::class.java).apply {
                        putExtra("ArtistId", contentResponseObj.data.artist[0].id)
                    })
                }
            }

            setModuleColor(contentResponseObj.data.moduleId)
            binding.category.text = contentResponseObj.data.categoryName
            if (contentResponseObj.data.contentType.equals("AUDIO", ignoreCase = true)) {
                // For Audio Player
                setupMusicPlayer(contentResponseObj)
                binding.rlPlayerMusicMain.visibility = View.VISIBLE
                binding.rlVideoPlayerMain.visibility = View.GONE
                binding.tvHeaderHtw.text = "Audio"
                contentTypeForTrack = "AUDIO"
            } else {
                // For video Player
                initializePlayer(contentResponseObj.data.url)
                binding.rlVideoPlayerMain.visibility = View.VISIBLE
                binding.rlPlayerMusicMain.visibility = View.GONE
                binding.tvHeaderHtw.text = "Video"
                contentTypeForTrack = "VIDEO"
            }
            setReadMoreView(contentResponseObj.data.desc)

            binding.imageLikeArticle.setOnClickListener { v ->
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
        }
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
        if (contentResponseObj?.data?.bookmarked == true) {
            binding.icBookmark.setImageResource(R.drawable.ic_save_article_active)
        }

        callContentTracking(contentResponseObj, "1.0", "1.0")

    }

    private fun callContentTracking(
        contentResponseObj: ModuleContentDetail?,
        duration: String,
        watchDuration: String
    ) {
// article consumed
        val episodeTrackRequest = EpisodeTrackRequest(
            sharedPreferenceManager.userId, contentResponseObj?.data?.moduleId ?: "",
            contentResponseObj?.data?.id ?: "", duration, watchDuration, contentTypeForTrack
        )

        trackEpisodeOrContent(this, episodeTrackRequest)
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


    /*private fun initializePlayer(previewUrl: String) {
        // Create a new ExoPlayer instance
        player = ExoPlayer.Builder(this).build()
        //player.playWhenReady = true
        binding.exoPlayerView.setPlayer(player)
        val videoUri = Uri.parse(
            ApiClient.CDN_URL_QA + previewUrl
        )
        Log.d("Received Content type", "Video URL: " + videoUri)


        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSourceFactory(this@ContentDetailsActivity)
        ).createMediaSource(MediaItem.fromUri(videoUri))
        player.setMediaSource(mediaSource)
        // Prepare the player and start playing automatically

        player.addListener(object : com.google.android.exoplayer2.Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == com.google.android.exoplayer2.Player.STATE_READY) {
                    Log.d("ExoPlayer", "Player is ready to play")
                    player.play()
                } else if (playbackState == com.google.android.exoplayer2.Player.STATE_ENDED) {
                    Log.d("ExoPlayer", "Playback ended")
                }
            }
        })
        player.prepare()

    }*/

    /*private fun initializePlayer(previewUrl: String) {
        // Create a new ExoPlayer instance
        player = ExoPlayer.Builder(this).build()
        player.playWhenReady = true // Set to true to auto-start once ready
        binding.exoPlayerView.setPlayer(player)

        // *** Use a known good test URL ***
        val testVideoUri = Uri.parse("https://live-hls-abr-cdn.livepush.io/live/bigbuckbunnyclip/index.m3u8")
       *//* val testVideoUri = Uri.parse(
            ApiClient.CDN_URL_QA + previewUrl
        )*//*

        // Log the test URL
        Log.d("ExoPlayerTest", "Testing with known good URL: $testVideoUri")

        val dataSourceFactory = DefaultHttpDataSource.Factory()
        // No need for specific Accept header for general testing, keep it simple
        // .setDefaultRequestProperties(mapOf("Accept" to "video/mp4"))

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(testVideoUri)) // Use the test URI

        player.setMediaSource(mediaSource)

        player.addListener(object : com.google.android.exoplayer2.Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == com.google.android.exoplayer2.Player.STATE_READY) {
                    Log.d("ExoPlayer", "Player is ready to play - TEST URL")
                    // player.play() // No need if playWhenReady is true
                } else if (playbackState == com.google.android.exoplayer2.Player.STATE_ENDED) {
                    Log.d("ExoPlayer", "Playback ended - TEST URL")
                }
                // Add more detailed state logging for debugging
                when (playbackState) {
                    com.google.android.exoplayer2.Player.STATE_IDLE -> Log.d("ExoPlayer", "State: IDLE")
                    com.google.android.exoplayer2.Player.STATE_BUFFERING -> Log.d("ExoPlayer", "State: BUFFERING")
                    com.google.android.exoplayer2.Player.STATE_READY -> Log.d("ExoPlayer", "State: READY")
                    com.google.android.exoplayer2.Player.STATE_ENDED -> Log.d("ExoPlayer", "State: ENDED")
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayer", "Playback error from test URL: ${error.message}", error)
                error.printStackTrace()
            }
        })
        player.prepare()
    }*/


    private fun initializePlayer(previewUrl: String) {
        // Ensure 'this' is a valid Context.
        // If in an Activity, 'this' is fine.
        // If in a Fragment, use 'requireContext()':
        player = ExoPlayer.Builder(this).build()
        player?.playWhenReady = true
        binding.exoPlayerView.player = player

        val fullVideoUrl = ApiClient.VIDEO_CDN_URL + previewUrl // Re-integrating your original URL logic
        val videoUri = Uri.parse(fullVideoUrl)

        Log.d("ExoPlayerInit", "Attempting to play URL: $videoUri")

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            // Set a User-Agent to improve compatibility and avoid 403 errors with some servers/CDNs
            //.setUserAgent("ExoPlayer/2.x (Linux; Android) YourAppName/1.0") // Replace YourAppName and version

        val mediaSource = if (videoUri.toString().endsWith(".m3u8", ignoreCase = true)) {
            HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri))
        } else if (videoUri.toString().endsWith(".mp4", ignoreCase = true)) {
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri))
        } else {
            // Handle other formats or throw an error if the format is not supported
            Log.e("ExoPlayerInit", "Unsupported video format for URL: $videoUri")
            // Optionally, return a dummy media source or throw an exception
            return // Exit the function if format is unsupported
        }

        player?.setMediaSource(mediaSource)

        player?.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> Log.d("ExoPlayer", "State: IDLE")
                    Player.STATE_BUFFERING -> Log.d("ExoPlayer", "State: BUFFERING")
                    Player.STATE_READY -> {
                        Log.d("ExoPlayer", "Player is ready to play")
                    }
                    Player.STATE_ENDED -> Log.d("ExoPlayer", "Playback ended")
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayer", "Playback error: ${error.message}", error)
                error.printStackTrace()
                // Optionally, show a Toast or dialog to the user about the error
                // Toast.makeText(this@YourActivity, "Error playing video: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })

        player?.prepare()
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
    private fun setupMusicPlayer(moduleContentDetail: ModuleContentDetail?) {
        val backgroundImage = findViewById<ImageView>(R.id.backgroundImage)
        binding.rlPlayerMusicMain.visibility = View.VISIBLE


        if (moduleContentDetail != null) {
            GlideApp.with(this@ContentDetailsActivity)
                .load(ApiClient.CDN_URL_QA + moduleContentDetail.data.thumbnail.url) //episodes.get(1).getThumbnail().getUrl()
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(backgroundImage)
        }


        //val previewUrl = "media/cms/content/series/64cb6d97aa443ed535ecc6ad/45ea4b0f7e3ce5390b39221f9c359c2b.mp3"
        val url = ApiClient.CDN_URL_QA + (moduleContentDetail?.data?.previewUrl?: "") //episodes.get(1).getPreviewUrl();//"https://www.example.com/your-audio-file.mp3";  // Replace with your URL
        Log.d("API Response", "Sleep aid URL: $url")
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync() // Load asynchronously
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to load audio", Toast.LENGTH_SHORT).show()
        }

        mediaPlayer.setOnPreparedListener(OnPreparedListener { mp: MediaPlayer? ->
            mediaPlayer.start()
            binding.seekBar.max = mediaPlayer.duration
            isPlaying = true
            binding.playPauseButton.setImageResource(R.drawable.ic_sound_pause)
            // Update progress every second
            handler.post(updateProgress)
        })
        // Play/Pause Button Listener
        binding.playPauseButton.setOnClickListener(View.OnClickListener { v: View? ->
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
        })
        mediaPlayer.setOnCompletionListener(OnCompletionListener { mp: MediaPlayer? ->
            Toast.makeText(this, "Playback finished", Toast.LENGTH_SHORT).show()
            handler.removeCallbacks(updateProgress)
        })

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
                    Utils.showCustomToast(this@ContentDetailsActivity, response.message())
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
                    Utils.showCustomToast(this@ContentDetailsActivity, response.message())
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
            """
                I'm inviting you to join me on the RightLife App, where health meets happiness in every tap.
                HealthCam and Voice Scan: Get insights into your physical and emotional well-being through facial recognition and voice pattern analysis.
                
                Together, let's craft our own adventure towards wellbeing!
                Download Now:  https://rightlife.sng.link/Afiju/ui5f/r_ccc1b019cd
                """.trimIndent()

        intent.putExtra(Intent.EXTRA_TEXT, shareText)

        startActivity(Intent.createChooser(intent, "Share"))
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
            player.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        handler.removeCallbacks(updateProgress)
        Log.d("contentDetails", "onDestroyCalled")
    }
}