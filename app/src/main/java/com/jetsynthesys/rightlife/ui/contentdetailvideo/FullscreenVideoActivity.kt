package com.jetsynthesys.rightlife.ui.contentdetailvideo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.jetsynthesys.rightlife.databinding.ActivityFullscreenPlayerBinding

class FullscreenVideoActivity : ComponentActivity() {

    private lateinit var binding: ActivityFullscreenPlayerBinding
    private lateinit var player: ExoPlayer
    private var videoUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFullscreenPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enterFullscreen()

        videoUrl = intent.getStringExtra("VIDEO_URL") ?: ""

        player = PlayerHolder.initPlayer(this)
        binding.fullPlayerView.player = player

        if (player.currentMediaItem == null) {
            val mediaItem = MediaItem.fromUri(videoUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
        }

        // resume from last position
        player.seekTo(PlayerHolder.lastPosition)
        player.playWhenReady = true

        binding.btnExitFullscreen.setOnClickListener {
            PlayerHolder.lastPosition = player.currentPosition
            binding.fullPlayerView.player = null
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.fullPlayerView.player = PlayerHolder.player
    }


    override fun onStop() {
        super.onStop()
        PlayerHolder.lastPosition = player.currentPosition
    }

    private fun enterFullscreen() {
        val controller = WindowInsetsControllerCompat(window, binding.root)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
