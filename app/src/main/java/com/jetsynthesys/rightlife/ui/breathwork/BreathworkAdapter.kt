package com.jetsynthesys.rightlife.ui.breathwork

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.ItemBreathworkBinding
import com.jetsynthesys.rightlife.ui.breathwork.pojo.BreathingData
import com.jetsynthesys.rightlife.ui.showBalloonWithDim
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

class BreathworkAdapter(
    private val items: List<BreathingData>,
    private val onItemClick: OnItemClickListener
) : RecyclerView.Adapter<BreathworkAdapter.BreathworkViewHolder>() {

    private var isClickable = true

    inner class BreathworkViewHolder(val binding: ItemBreathworkBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreathworkViewHolder {
        val binding =
            ItemBreathworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BreathworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BreathworkViewHolder, position: Int) {
        holder.itemView.isEnabled = isClickable
        val item = items[position]
        holder.binding.apply {
            titleTextView.text = item.title
            descriptionTextView.text = item.subTitle
            //imageView.setImageResource()

            Glide.with(imageView.context)
                .load(ApiClient.CDN_URL_QA + item.thumbnail)
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(imageView)

            /*  plusButton.setImageResource(
                  if (item.isAddedToToolKit) {
                      R.drawable.greentick
                  } else
                  { R.drawable.ic_bookmark_breathing}
              )*/

            if (item.isAddedToToolKit) {
                plusButton.setImageResource(R.drawable.ic_save_article_active)
                plusButton.imageTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            plusButton.context,
                            R.color.color_eat_right
                        )
                    )
            } else {
                plusButton.setImageResource(R.drawable.ic_save_article)
                plusButton.imageTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            plusButton.context,
                            R.color.black
                        )
                    )
            }

            plusButton.setOnClickListener {
                val sharedPreferenceManager =
                    SharedPreferenceManager.getInstance(holder.itemView.context)
                if (!sharedPreferenceManager.isTooltipShowed("BreathWorkAddButton")) {
                    sharedPreferenceManager.saveTooltip("BreathWorkAddButton", true)
                    holder.itemView.context.showBalloonWithDim(
                        plusButton,
                        "Tap to add this routine to your toolkit.",
                        "BreathWorkAdd", xOff = -200, yOff = 20, arrowPosition = 0.9f
                    )
                } else {
                    onItemClick.onAddToolTip(item)
                    item.isAddedToToolKit = !item.isAddedToToolKit
                    notifyDataSetChanged()
                }
            }

            cardView.setOnClickListener {
                if (isClickable) {
                    isClickable = false
                    onItemClick.onClick(item)
                    holder.itemView.postDelayed({ isClickable = true }, 500)
                }
            }

            // ✅ Background color based on breathing type
            val resColor = when (item.title?.trim()) {
                "Box Breathing" -> R.color.box_breathing_card_color
                "Alternate Nostril Breathing" -> R.color.alternate_breathing_card_color
                "4-7-8 Breathing" -> R.color.four_seven_breathing_card_color
                "Custom" -> R.color.custom_breathing_card_color
                else -> R.color.custom_breathing_card_color
            }
            val colorInt = ContextCompat.getColor(holder.itemView.context, resColor)
            cardView.setCardBackgroundColor(ColorStateList.valueOf(colorInt))

            val textColorRes = when (item.title?.trim()) {
                "Box Breathing" -> R.color.box_breathing_card_color_text
                "Alternate Nostril Breathing" -> R.color.alternate_breathing_card_color_text
                "4-7-8 Breathing" -> R.color.four_seven_breathing_card_color_text
                "Custom" -> R.color.custom_breathing_card_color_text
                else -> R.color.custom_breathing_card_color_text
            }

            val textColor = ContextCompat.getColor(holder.itemView.context, textColorRes)

            titleTextView.setTextColor(textColor)
            descriptionTextView.setTextColor(textColor)

            // Debug log (optional)
            Log.d("BreathworkAdapter", "position=$position title=${item.title} -> color=$resColor")
        }

    }

    override fun getItemCount() = items.size

    interface OnItemClickListener {
        fun onClick(breathingData: BreathingData)
        fun onAddToolTip(breathingData: BreathingData)
    }
}
