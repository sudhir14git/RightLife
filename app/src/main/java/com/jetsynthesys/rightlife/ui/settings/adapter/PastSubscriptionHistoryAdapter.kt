package com.jetsynthesys.rightlife.ui.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.databinding.RowPastSubscriptionHistoryBinding
import com.jetsynthesys.rightlife.databinding.RowSubscriptionHistoryBinding
import com.jetsynthesys.rightlife.ui.settings.pojo.Subscription
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils

class PastSubscriptionHistoryAdapter(
    private val plans: List<Subscription>,
    private val type: Int = 0,
    private val onPlanSelected: (Subscription) -> Unit
) : RecyclerView.Adapter<PastSubscriptionHistoryAdapter.SubscriptionHistoryViewHolder>() {

    private var selectedPosition = -1

    inner class SubscriptionHistoryViewHolder(private val binding: RowPastSubscriptionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: Subscription, position: Int) {
            binding.planName.text = plan.planInfo
            binding.planDescription.text = plan.name
            binding.trialEnds.text = "Expired on "+DateTimeUtils.convertAPIDate(plan.endDateTime)
            binding.tvPlanAmmount.text = "\u20B9" + plan.orderInfo?.amountPaid.toString()

            if (type == 1) {
                //binding.tvCurrentPlan.visibility = View.INVISIBLE
            }

            binding.root.setOnClickListener {
                selectedPosition = adapterPosition
                //onPlanSelected(plan)
                notifyDataSetChanged()
            }
            binding.imgDownload.setOnClickListener {
                plan.invoice?.url?.let { url ->
                    onPlanSelected(plan) // pass plan back to Activity
                } ?: run {
                    Toast.makeText(binding.root.context, "No invoice available", Toast.LENGTH_SHORT).show()
                }
            }
            // "planInfo": "FREE TRIAL",
            if (plan.planInfo == "FREE TRIAL" || plan.planInfo?.contains("free", ignoreCase = true) == true) {
                binding.imgDownload.visibility = View.INVISIBLE
            } else {
                binding.imgDownload.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubscriptionHistoryViewHolder {
        val binding = RowPastSubscriptionHistoryBinding.inflate(
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
