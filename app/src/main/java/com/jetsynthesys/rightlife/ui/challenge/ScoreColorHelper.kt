package com.jetsynthesys.rightlife.ui.challenge

import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.widget.SeekBar
import com.jetsynthesys.rightlife.R

object ScoreColorHelper {
    fun getColorCode(performance: String): String {

        return when (performance) {
            "Fair" -> "#F64840"
            "Good" -> "#F1A535"
            "Excellent" -> "#34C759"
            "Champ" -> "#1292E5"
            else -> "#F64840"
        }
    }

    fun getImageBasedOnStatus(status: String): Int {
        return when (status) {
            "INITIAL" -> R.drawable.ic_checklist_tick_bg
            "INPROGRESS" -> R.drawable.ic_checklist_pending
            "COMPLETED" -> R.drawable.ic_checklist_complete
            else -> R.drawable.ic_checklist_tick_bg
        }
    }

    fun setSeekBarProgressColor(seekBar: SeekBar, colorHex: String) {
        val progressDrawable = seekBar.progressDrawable.mutate()

        val progressLayer =
            (progressDrawable as LayerDrawable)
                .findDrawableByLayerId(android.R.id.progress)

        progressLayer?.setTint(Color.parseColor(colorHex))

        seekBar.progressDrawable = progressDrawable
    }

}