package com.jetsynthesys.rightlife.ui.challenge

import android.graphics.Color
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.R

object LeaderboardUiStyler {

    fun apply(rank: Int, card: CardView, rankCircle: FrameLayout) {
        when (rank) {
            1 -> {
                card.setCardBackgroundColor(Color.parseColor("#B79BFF"))
                rankCircle.setBackgroundResource(R.drawable.leaderbaord_1)
            }
            2 -> {
                card.setCardBackgroundColor(Color.parseColor("#FFBE63"))
                rankCircle.setBackgroundResource(R.drawable.leaderbaord_2)
            }
            3 -> {
                card.setCardBackgroundColor(Color.parseColor("#BFD6FF"))
                rankCircle.setBackgroundResource(R.drawable.leaderbaord_3)
            }
            else -> {
                card.setCardBackgroundColor(Color.WHITE)
                rankCircle.setBackgroundResource(R.drawable.roundedcornershape)
                //rankCircle.backgroundTintList = ContextCompat.getColorStateList(rankCircle.context, R.color.white)
            }
        }
    }
}
