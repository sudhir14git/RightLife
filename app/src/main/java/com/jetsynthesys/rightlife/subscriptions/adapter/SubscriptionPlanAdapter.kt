package com.jetsynthesys.rightlife.subscriptions.adapter

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.RowSubscriptionBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.subscriptions.pojo.PlanList
import java.text.NumberFormat
import java.util.Currency

class SubscriptionPlanAdapter(
    private val type: String,
    private val plans: List<PlanList>,
    private val productDetailsMap: HashMap<String, ProductDetails>,
    private val onPlanSelected: (PlanList, Int) -> Unit,
    private val onBuyClick: (Int) -> Unit
) : RecyclerView.Adapter<SubscriptionPlanAdapter.PlanViewHolder>() {

    private var selectedPosition = -1
    val Int.dp: Int
        get() =
            (this * Resources.getSystem().displayMetrics.density).toInt()

    inner class PlanViewHolder(private val binding: RowSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: PlanList, position: Int) {
            if (type == "FACIAL_SCAN") {
                val planName = plan.title?.split("-")
                binding.planTitle.text = planName?.get(0) ?: "Face Scan"
                binding.tvCancel.text = planName?.get(1) ?: "Pack of 1"
                binding.tvCancel.visibility = View.VISIBLE
                binding.tvBadge.visibility = if (plan.title?.contains("Pack of 12", true) == true)
                    View.VISIBLE
                else
                    View.GONE
            } else {
                binding.planTitle.text = plan.title ?: ""
                binding.tvCancel.visibility = View.GONE
            }
            binding.planName.text = plan.desc ?: ""

            if (plan.status.equals("ACTIVE", ignoreCase = true)) {
                binding.tvBuy.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.color_green)
                binding.tvBuy.text = "Active"
            } else {
                binding.tvBuy.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.menuselected)
                binding.tvBuy.text = "Buy"
            }

            binding.tvBadge.visibility = if (plan.title?.contains("Pack of 12", true) == true
                || plan.title?.contains("Yearly", true) == true
                || plan.title?.contains("Annual", true) == true
            )
                View.VISIBLE
            else
                View.GONE

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
                binding.planOfferDiscount.text = " (${plan.discountPercent}% Off)"
            } else {
                binding.planOfferDiscount.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                /*selectedPosition = bindingAdapterPosition
                onPlanSelected(plan, bindingAdapterPosition)
                notifyDataSetChanged()*/
            }

            binding.tvBuy.setOnClickListener {
                if (!plan.status.equals("ACTIVE", ignoreCase = true)) {
                    if (isAnyPackPurchased()) {
                        binding.tvBuy.context.showCustomToast("You already have an active subscription",false)
                    } else {
                        selectedPosition = bindingAdapterPosition
                        onBuyClick(bindingAdapterPosition)
                        notifyDataSetChanged()
                    }
                }
            }

            val bulletPoints = if (plan.title?.contains("monthly", true) == true)
                listOf(
                    "RightLife Premium, monthly",
                    "Everything unlocked, all tools and content",
                    "Unlimited Meal Snap, scan any meal, get calories and macros in seconds",
                    "4 Face Scans every month, HR, HRV, stress, breathing rate, CVD risk",
                    "Auto renews; cancel anytime"
                )
            else if (plan.title?.contains("yearly", true) == true
                || plan.title?.contains("Annual", true) == true
            )
                listOf(
                    "RightLife Premium, yearly",
                    "Everything unlocked, all tools and content",
                    "Unlimited Meal Snap, scan any meal, get calories and macros in seconds",
                    "4 Face Scans every month, HR, HRV, stress, breathing rate, CVD risk",
                    "Auto renews; cancel anytime"
                )
            else if (plan.title?.contains("Pack of 12", true) == true)
                listOf(
                    "12 Face Scans - continuous clarity!",
                    "Good all year - use as you please within 12 months."
                )
            else
                listOf(
                    "Face Scan reveals heart rate, HRV, stress, breathing, and CVD risk - instant clarity!",
                    "Use anytime within 30 days."
                )

            addBulletPoints(binding.llBulletPoints, bulletPoints)
        }

        private fun isAnyPackPurchased(): Boolean {
            plans.forEach {
                if (it.status.equals("ACTIVE", ignoreCase = true)) {
                    return true
                }
            }
            return false
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
                    binding.planOffer.paintFlags =
                        binding.planOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
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
                binding.planOffer.paintFlags =
                    binding.planOffer.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.planOffer.visibility = View.GONE
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

    private fun addBulletPoints(container: LinearLayout, points: List<String>) {

        container.removeAllViews()  // Clear old bullets

        points.forEachIndexed { index, point ->

            val tv = TextView(container.context).apply {
                text = "• $point"
                setTextColor(Color.parseColor("#14142A"))
                textSize = 13f
                setPadding(0, if (index == 0) 0 else 6.dp, 0, 0)
            }

            container.addView(tv)
        }
    }

}
