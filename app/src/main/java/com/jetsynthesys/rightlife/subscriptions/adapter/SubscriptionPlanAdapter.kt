package com.jetsynthesys.rightlife.subscriptions.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import com.jetsynthesys.rightlife.databinding.RowSubscriptionBinding
import com.jetsynthesys.rightlife.subscriptions.pojo.PlanList
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils
import java.text.NumberFormat
import java.util.Currency

class SubscriptionPlanAdapter(
        private val plans: List<PlanList>,
        private val productDetailsMap: HashMap<String, ProductDetails>,
        private val onPlanSelected: (PlanList) -> Unit
) : RecyclerView.Adapter<SubscriptionPlanAdapter.PlanViewHolder>() {

    private var selectedPosition = -1

    inner class PlanViewHolder(private val binding: RowSubscriptionBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: PlanList, position: Int) {
            binding.planTitle.text = plan.title ?: ""
            binding.planName.text = plan.desc ?: ""

            // Get product details from Play Store
            val productDetails = plan.googlePlay?.let { productDetailsMap[it] }

            if (productDetails != null) {
                // Use Play Store pricing with proper currency
                displayPlayStorePricing(productDetails, plan)
            } else {
                // Fallback to backend pricing
                displayBackendPricing(plan)
            }

            // Show discount percentage if available
            if (plan.discountPercent != null && plan.discountPercent!! > 0) {
                binding.planOfferDiscount.visibility = View.VISIBLE
                binding.planOfferDiscount.text = " (${plan.discountPercent}% OFF)"
            } else {
                binding.planOfferDiscount.visibility = View.GONE
            }

            // Show status (ACTIVE, etc.)
            if (plan.status != null && plan.status!!.isNotEmpty()) {
                binding.tvCurrentPlan.visibility = View.VISIBLE
                binding.tvCurrentPlan.text = plan.status
            } else {
                binding.tvCurrentPlan.visibility = View.GONE
            }

            // Show validity date
            if (plan.endDateTime != null && plan.endDateTime!!.isNotEmpty()) {
                binding.trialEnds.visibility = View.VISIBLE
                binding.trialEnds.text = "Valid Till ${DateTimeUtils.convertAPIDate(plan.endDateTime)}"
            } else {
                binding.trialEnds.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                selectedPosition = adapterPosition
                onPlanSelected(plan)
                notifyDataSetChanged()
            }
        }

        private fun displayPlayStorePricing(productDetails: ProductDetails, plan: PlanList) {
            when (productDetails.productType) {
                "inapp" -> {
                    // For in-app purchases (consumables/boosters)
                    val pricing = productDetails.oneTimePurchaseOfferDetails
                    if (pricing != null) {
                        // Main price with currency from Play Store
                        binding.tvPlanAmmount.text = pricing.formattedPrice

                        // Calculate and show original price if discount exists
                        handleDiscountPricing(
                                pricing.priceAmountMicros,
                                pricing.priceCurrencyCode,
                                plan.discountPercent
                        )
                    } else {
                        displayBackendPricing(plan)
                    }
                }

                "subs" -> {
                    // For subscriptions
                    val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
                    val pricingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()

                    if (pricingPhase != null) {
                        // Main price with currency from Play Store
                        binding.tvPlanAmmount.text = pricingPhase.formattedPrice

                        // Calculate and show original price if discount exists
                        handleDiscountPricing(
                                pricingPhase.priceAmountMicros,
                                pricingPhase.priceCurrencyCode,
                                plan.discountPercent
                        )
                    } else {
                        displayBackendPricing(plan)
                    }
                }

                else -> {
                    displayBackendPricing(plan)
                }
            }
        }

        /**
         * Calculates and displays the original price before discount
         * Uses the same currency as Play Store for consistency
         */
        private fun handleDiscountPricing(
                priceAmountMicros: Long,
                currencyCode: String,
                discountPercent: Int?
        ) {
            if (discountPercent != null && discountPercent > 0) {
                try {
                    // Calculate original price from discounted price
                    // Formula: originalPrice = discountedPrice / (1 - discount%)
                    val discountedPrice = priceAmountMicros / 1_000_000.0
                    val originalPrice = discountedPrice / (1 - (discountPercent / 100.0))

                    // Format with proper currency symbol
                    val currency = Currency.getInstance(currencyCode)
                    val formatter = NumberFormat.getCurrencyInstance()
                    formatter.currency = currency
                    formatter.minimumFractionDigits = 0
                    formatter.maximumFractionDigits = 2

                    binding.planOffer.visibility = View.VISIBLE
                    binding.planOffer.text = formatter.format(originalPrice)
                    binding.planOffer.paintFlags = binding.planOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } catch (e: Exception) {
                    // If calculation fails, hide the discount price
                    binding.planOffer.visibility = View.GONE
                }
            } else {
                binding.planOffer.visibility = View.GONE
            }
        }

        /**
         * Fallback to backend pricing when Play Store data is unavailable
         */
        private fun displayBackendPricing(plan: PlanList) {
            // Use INR by default for backend pricing
            binding.tvPlanAmmount.text = "₹${plan.price?.inr ?: 0}"

            if (plan.discountPrice?.inr != null && plan.discountPrice!!.inr!! > 0) {
                binding.planOffer.visibility = View.VISIBLE
                binding.planOffer.text = "₹${plan.discountPrice?.inr}"
                binding.planOffer.paintFlags = binding.planOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.planOffer.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = RowSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(plans[position], position)
    }

    override fun getItemCount(): Int = plans.size
}
/*
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
            */
/*if (plan.desc?.contains("$", ignoreCase = true) == true) {
                binding.tvPlanAmmount.text = "$" + plan.price?.usd.toString()
                binding.planOffer.text = "$" + plan.discountPrice?.usd.toString()
                binding.planOffer.paintFlags = binding.planOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvPlanAmmount.text = "₹" + plan.price?.inr.toString()
                binding.planOffer.text = "₹" + plan.discountPrice?.inr.toString()
                binding.planOffer.paintFlags = binding.planOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }*//*


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
*/
