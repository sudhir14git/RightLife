package com.jetsynthesys.rightlife.subscriptions.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.databinding.RowSubscriptionBinding
import com.jetsynthesys.rightlife.subscriptions.pojo.PlanList
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils

class SubscriptionPlanAdapter(
    private val plans: List<PlanList>,
    private val onPlanSelected: (PlanList) -> Unit
) : RecyclerView.Adapter<SubscriptionPlanAdapter.PlanViewHolder>() {

    private var selectedPosition = -1

    inner class PlanViewHolder(private val binding: RowSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: PlanList, position: Int) {
            binding.planTitle.text = plan.title?: ""
            binding.planName.text = plan.desc?: ""
            /*if (plan.desc?.contains("$", ignoreCase = true) == true) {
                binding.tvPlanAmmount.text = "$" + plan.price?.usd.toString()
                binding.planOffer.text = "$" + plan.discountPrice?.usd.toString()
                binding.planOffer.paintFlags = binding.planOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvPlanAmmount.text = "₹" + plan.price?.inr.toString()
                binding.planOffer.text = "₹" + plan.discountPrice?.inr.toString()
                binding.planOffer.paintFlags = binding.planOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }*/

            binding.tvPlanAmmount.text = "₹" + plan.price?.inr.toString()
            binding.planOffer.text = "₹" + plan.discountPrice?.inr.toString()
            binding.planOffer.paintFlags = binding.planOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            binding.planOfferDiscount.text =  " (${plan.discountPercent ?: 0}% OFF)"
            if (plan.status != null && plan.status!!.isNotEmpty()) {
                binding.tvCurrentPlan.visibility = View.VISIBLE
                binding.tvCurrentPlan.text = plan.status
            } else
                binding.tvCurrentPlan.visibility = View.GONE

            if (plan.endDateTime != null && plan.endDateTime!!.isNotEmpty()) {
                binding.trialEnds.visibility = View.VISIBLE
                binding.trialEnds.text =
                    "Valid Till ${DateTimeUtils.convertAPIDate(plan.endDateTime)}"
            } else
                binding.trialEnds.visibility = View.GONE


            binding.root.setOnClickListener {
                selectedPosition = adapterPosition
                onPlanSelected(plan)
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding =
            RowSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(plans[position], position)
    }

    override fun getItemCount(): Int = plans.size
}
