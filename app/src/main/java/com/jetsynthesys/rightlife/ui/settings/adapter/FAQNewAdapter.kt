package com.jetsynthesys.rightlife.ui.settings.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.databinding.RowFaqQuestionBinding
import com.jetsynthesys.rightlife.databinding.RowFaqTitleBinding
import com.jetsynthesys.rightlife.ui.settings.pojo.FAQDetails

/**
 * Items is a mixed list:
 * - String  -> Section Title
 * - FAQDetails -> Question row
 *
 * Click contract remains SAME as before:
 * interface OnItemClickListener { fun onItemClick(faqData: FAQDetails) }
 */
class FAQNewAdapter(
        private val items: List<Any>,
        private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_QUESTION = 1
    }

    interface OnItemClickListener {
        fun onItemClick(faqData: FAQDetails)
    }

    inner class TitleViewHolder(val binding: RowFaqTitleBinding) :
            RecyclerView.ViewHolder(binding.root)

    inner class QuestionViewHolder(val binding: RowFaqQuestionBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) TYPE_TITLE else TYPE_QUESTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TITLE) {
            val binding = RowFaqTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
            TitleViewHolder(binding)
        } else {
            val binding = RowFaqQuestionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
            )
            QuestionViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TitleViewHolder -> {
                val title = items[position] as String
                holder.binding.tvSectionTitle.text = title
                // No click for titles
                holder.itemView.setOnClickListener(null)
            }
            is QuestionViewHolder -> {
                val faq = items[position] as FAQDetails
                holder.binding.tvQuestion.text = faq.question ?: ""
                holder.itemView.setOnClickListener {
                    onItemClickListener.onItemClick(faq)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}

