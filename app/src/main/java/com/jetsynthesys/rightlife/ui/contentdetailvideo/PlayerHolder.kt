package com.jetsynthesys.rightlife.ui.contentdetailvideo

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer

object PlayerHolder {
    var player: ExoPlayer? = null
    var lastPosition: Long = 0L
    var needsPrepare = true

    fun initPlayer(context: Context): ExoPlayer {
        if (player == null) {
            val renderersFactory = DefaultRenderersFactory(context)
                .setEnableDecoderFallback(true)
            player = ExoPlayer.Builder(context, renderersFactory).build()
            player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            needsPrepare = true
        }
        return player!!
    }

    fun release() {
        player?.release()
        player = null
        needsPrepare = true
        lastPosition = 0L
    }
}
