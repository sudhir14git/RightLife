package com.jetsynthesys.rightlife.ui.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.RowSubscriptionHistoryBinding
import com.jetsynthesys.rightlife.ui.settings.pojo.Subscription
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils

class SubscriptionHistoryAdapter(
    private val plans: List<Subscription>,
    private val type: Int = 0,
    private val onPlanSelected: (Subscription) -> Unit
) : RecyclerView.Adapter<SubscriptionHistoryAdapter.SubscriptionHistoryViewHolder>() {

    private var selectedPosition = -1

    inner class SubscriptionHistoryViewHolder(private val binding: RowSubscriptionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: Subscription, position: Int) {
            binding.planName.text = plan.planInfo
            binding.planDescription.text = plan.name
            binding.trialEnds.text = "Valid Till "+DateTimeUtils.convertAPIDate(plan.endDateTime)
            binding.tvPlanAmmount.text = "\u20B9" + plan.orderInfo?.amountPaid.toString()

            if (type == 1) {
                binding.tvCurrentPlan.visibility = View.INVISIBLE
            }else
            {
                binding.tvCurrentPlan.text = plan.status
                binding.tvCurrentPlan.apply {
                    text = plan.status
                    setTextColor(when(plan.status) {
                        "Current Plan" -> ContextCompat.getColor(context, R.color.green_minimal)
                        "Cancelled" -> ContextCompat.getColor(context, R.color.menuselected)
                        else -> ContextCompat.getColor(context, R.color.gray)
                    })
                }
            }


            binding.root.setOnClickListener {
                selectedPosition = adapterPosition
                onPlanSelected(plan)
                notifyDataSetChanged()
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubscriptionHistoryViewHolder {
        val binding = RowSubscriptionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubscriptionHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionHistoryViewHolder, position: Int) {
        holder.bind(plans[position], position)
    }

    override fun getItemCount(): Int = plans.size
}
