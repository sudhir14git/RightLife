package com.jetsynthesys.rightlife.ui.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            if (plan.planInfo?.contains("pack", ignoreCase = true) == true) {
                binding.trialEnds.visibility = View.INVISIBLE
            } else {
                binding.trialEnds.visibility = View.VISIBLE
            }
            binding.trialEnds.text = "Valid Till "+DateTimeUtils.convertAPIDateMonthFormat(plan.endDateTime)
            binding.tvPlanAmmount.text = "\u20B9" + plan.orderInfo?.amountPaid.toString()

            if (type == 1) {
                binding.tvCurrentPlan.visibility = View.INVISIBLE
            }else
            {
                binding.tvCurrentPlan.text = plan.status
                if (plan.status == "ACTIVE") {
                    binding.tvCurrentPlan.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.green_minimal
                        )
                    )
                    binding.tvCurrentPlan.text = "Current Plan"
                } else if (plan.status == "Cancelled") {
                    binding.tvCurrentPlan.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.menuselected
                        )
                    )
                } else {
                    binding.tvCurrentPlan.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.white
                        )
                    )
                }
                binding.tvCurrentPlan.apply {
                    //text = plan.status
                    setTextColor(when(plan.status) {
                        "Current Plan" -> ContextCompat.getColor(context, R.color.green_minimal)
                        "Cancelled" -> ContextCompat.getColor(context, R.color.menuselected)
                        else -> ContextCompat.getColor(context, R.color.white)
                    })
                }
            }


            binding.root.setOnClickListener {
                selectedPosition = adapterPosition
               // onPlanSelected(plan)
                notifyDataSetChanged()
            }
            binding.imgDownload.setOnClickListener {
                plan.invoice?.url?.let { url ->
                    onPlanSelected(plan) // pass plan back to Activity
                } ?: run {
                    Toast.makeText(binding.root.context, "No invoice available", Toast.LENGTH_SHORT).show()
                }
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
