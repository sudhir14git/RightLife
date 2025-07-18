package com.jetsynthesys.rightlife.ui.breathwork

import android.view.LayoutInflater
import android.view.ViewGroup
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

    inner class BreathworkViewHolder(val binding: ItemBreathworkBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreathworkViewHolder {
        val binding =
            ItemBreathworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BreathworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BreathworkViewHolder, position: Int) {
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

            plusButton.setImageResource(
                if (item.isAddedToToolKit) R.drawable.greentick else R.drawable.add_journal
            )

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
                onItemClick.onClick(item)
            }
        }
    }

    override fun getItemCount() = items.size

    interface OnItemClickListener {
        fun onClick(breathingData: BreathingData)
        fun onAddToolTip(breathingData: BreathingData)
    }
}
