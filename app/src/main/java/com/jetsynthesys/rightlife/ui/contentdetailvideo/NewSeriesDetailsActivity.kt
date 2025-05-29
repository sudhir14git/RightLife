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
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeDetail.EpisodeDetailContentResponse
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeDetail.NextEpisode
import com.jetsynthesys.rightlife.databinding.ActivityNewSeriesDetailsBinding
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleLikeRequest
import com.jetsynthesys.rightlife.ui.CommonAPICall.trackEpisodeOrContent
import com.jetsynthesys.rightlife.ui.CommonAPICall.updateViewCount
import com.jetsynthesys.rightlife.ui.therledit.ArtistsDetailsActivity
import com.jetsynthesys.rightlife.ui.therledit.EpisodeTrackRequest
import com.jetsynthesys.rightlife.ui.therledit.ViewCountRequest
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
import java.net.URISyntaxException
import java.util.concurrent.TimeUnit

class NewSeriesDetailsActivity : BaseActivity() {
    private var isPlaying = false
    private val handler = Handler()
    private lateinit var mediaPlayer: MediaPlayer
    private var isExpanded = false
    private lateinit var player: ExoPlayer
    private lateinit var binding: ActivityNewSeriesDetailsBinding
    private var contentTypeForTrack: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewSeriesDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var contentId = intent.getStringExtra("contentId")
        var seriesId = intent.getStringExtra("seriesId")
        var episodeId = intent.getStringExtra("episodeId")
        //API Call
        if (seriesId != null) {

            if (seriesId != null) {
                if (episodeId != null) {
                    getSeriesDetails(seriesId, episodeId)
                }
            }
        }

        val viewCountRequest = ViewCountRequest()
        viewCountRequest.id = seriesId
        viewCountRequest.userId = sharedPreferenceManager.userId
        updateViewCount(this, viewCountRequest)

        binding.icBackDialog.setOnClickListener {
            finish()
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
                            val ContentResponseObj = gson.fromJson<EpisodeDetailContentResponse>(
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

            setArtistname(contentResponseObj)

            Glide.with(applicationContext)
                .load(
                    ApiClient.CDN_URL_QA + contentResponseObj.data.artist.firstOrNull()?.profilePicture
                )
                .placeholder(R.drawable.profile_man) // Replace with your placeholder image
                .circleCrop()
                .into(binding.profileImage)
            setModuleColor(contentResponseObj.data.moduleId)
            binding.category.text = contentResponseObj.data.tags.get(0).name

            if (contentResponseObj.data != null && contentResponseObj.data.youtubeUrl != null && !contentResponseObj.data.youtubeUrl.isEmpty()) {
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
            } else if (contentResponseObj.data.type.equals("AUDIO", ignoreCase = true)) {
                // For Audio Player
                setupMusicPlayer(contentResponseObj)
                binding.rlPlayerMusicMain.visibility = View.VISIBLE
                binding.rlVideoPlayerMain.visibility = View.GONE
                binding.tvHeaderHtw.text = "Audio"
                contentTypeForTrack = "AUDIO"
            } else if (contentResponseObj.data.type.equals("VIDEO", ignoreCase = true)) {
                // For video Player
                initializePlayer(contentResponseObj.data.previewUrl)
                binding.rlVideoPlayerMain.visibility = View.VISIBLE
                binding.rlPlayerMusicMain.visibility = View.GONE
                binding.tvHeaderHtw.text = "Video"
                contentTypeForTrack = "VIDEO"
            }
            setReadMoreView(contentResponseObj.data.desc)

            /*   binding.imageLikeArticle.setOnClickListener { v ->
                   binding.imageLikeArticle.setImageResource(R.drawable.like_article_active)
                   if (contentResponseObj.data.li) {
                       binding.imageLikeArticle.setImageResource(R.drawable.like)
                       contentResponseObj.data.like = false
                       postContentLike(contentResponseObj.data.id, false)
                   } else {
                       binding.imageLikeArticle.setImageResource(R.drawable.ic_like_receipe)
                       contentResponseObj.data.like = true
                       postContentLike(contentResponseObj.data.id, true)
                   }
               }*/
            binding.imageShareArticle.setOnClickListener { shareIntent() }
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
        callContentTracking(contentResponseObj, "1.0", "1.0")
    }

    private fun callContentTracking(
        contentResponseObj: EpisodeDetailContentResponse,
        duration: String,
        watchDuration: String
    ) {
// article consumed
        val episodeTrackRequest = EpisodeTrackRequest(
            sharedPreferenceManager.userId, contentResponseObj.data?.moduleId ?: "",
            contentResponseObj.data?._id ?: "", duration, watchDuration, contentTypeForTrack
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
                .error(R.drawable.img_logintop)
                .into(backgroundImage)
        }


        //val previewUrl = "media/cms/content/series/64cb6d97aa443ed535ecc6ad/45ea4b0f7e3ce5390b39221f9c359c2b.mp3"
        val url = ApiClient.CDN_URL_QA + (moduleContentDetail.data?.previewUrl
            ?: "") //episodes.get(1).getPreviewUrl();//"https://www.example.com/your-audio-file.mp3";  // Replace with your URL
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


    private fun extractVideoId(youtubeUrl: String): String? {
        try {
            val uri = URI(youtubeUrl)
            val query = uri.query

            if (query != null) {
                val params =
                    query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (param in params) {
                    val keyValue =
                        param.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (keyValue.size == 2 && keyValue[0] == "v") {
                        return keyValue[1]
                    }
                }
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        return null
    }

    private fun setupYouTubePlayer(videoId: String) {
        binding.youtubevideoPlayer.visibility = View.VISIBLE
        binding.youtubevideoPlayer.addYouTubePlayerListener(object :
            AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f)
                Log.d("YouTube", "Video loaded: $videoId")
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
        } else if (binding != null && binding.tvAuthorName != null) {
            binding.tvAuthorName.text = "" // or set some default value
        }
    }
}