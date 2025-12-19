package com.jetsynthesys.rightlife.subscriptions

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivitySubscriptionCheckoutBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetAwakeNightBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetPaymentStatusResultBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetPlanSelectionBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.subscriptions.adapter.PlanSelectionAdapter
import com.jetsynthesys.rightlife.subscriptions.pojo.OrderDataRazorpay
import com.jetsynthesys.rightlife.subscriptions.pojo.OrderRequestRazorpay
import com.jetsynthesys.rightlife.subscriptions.pojo.OrderResponseRazorpay
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentIntentResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessRequest
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.PlanList
import com.jetsynthesys.rightlife.subscriptions.pojo.SdkDetail
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Currency

class SubscriptionCheckoutActivity : BaseActivity(), PurchasesUpdatedListener,
    PaymentResultListener {

    private val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    private lateinit var binding: ActivitySubscriptionCheckoutBinding
    private lateinit var planList: ArrayList<PlanList>
    private val productDetailsMap = HashMap<String, ProductDetails>()
    private lateinit var billingClient: BillingClient
    private var type: String = "FACIAL_SCAN"
    private var position: Int = 0
    private var receivedProductType = "BOOSTER"
    private var isSubscriptionTaken: Boolean = false
    private var lastClickTime = 0L
    private val CLICK_DEBOUNCE_TIME = 2000L
    private var receivedProductId: String? = null
    private lateinit var colorStateListSelected: ColorStateList
    private lateinit var colorStateListNonSelected: ColorStateList
    private var clickPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionCheckoutBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        position = intent.getIntExtra("POSITION", 0)
        planList = intent.getSerializableExtra("PLAN_LIST") as? ArrayList<PlanList>
            ?: arrayListOf()

        binding.iconBack.setOnClickListener {
            finish()
        }

        type = intent.getStringExtra("SUBSCRIPTION_TYPE").toString()

        if (type == "FACIAL_SCAN") {
            binding.tvChangePlan.text = getString(R.string.change_pack_underlined)
            val planName = planList[position].title?.split("-")
            binding.planTitle.text = planName?.get(0) ?: "Face Scan"
            binding.tvCancel.text = planName?.get(1) ?: "Pack of 1"
        } else {
            binding.planTitle.text = planList[position].title ?: ""
            binding.tvCancel.text = "Cancel anytime online"
        }

        colorStateListSelected = ContextCompat.getColorStateList(this, R.color.menuselected)!!
        colorStateListNonSelected = ContextCompat.getColorStateList(this, R.color.rightlife)!!

        binding.tvChangePlan.paintFlags =
            binding.tvChangePlan.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val iconListRazorPay = listOf(
            R.drawable.paytm,
            R.drawable.bhim,
            R.drawable.phonepay,
            R.drawable.googlepay,
            R.drawable.visa,
            R.drawable.ic_mastercard,
            R.drawable.ic_mastercard,
            R.drawable.ic_mastercard,
            R.drawable.ic_mastercard
        )

        val iconListPlayStore = listOf(
            R.drawable.playstore,
            R.drawable.visa,
            R.drawable.ic_mastercard,
            R.drawable.ic_mastercard,
            R.drawable.ic_mastercard
        )

        addPaymentIcons(binding.llIconsRazorPay, iconListRazorPay, 5)

        addPaymentIcons(binding.llIconsGooglePlay, iconListPlayStore, 3)

        addBulletPointsWithGreenTick(
            binding.llBulletPoints,
            listOf(
                "Priority customer support",
                "Multiple payment options",
                "Easy cancellation anytime"
            )
        )

        initializeBillingClient()

        //setPriceAmount(position)

        binding.tvChangePlan.setOnClickListener {
            showPlanListBottomSheet()
        }

        binding.llGooglePlay.setOnClickListener {
            //startGooglePay()
            clickPosition = 1
            binding.rlRazorPay.setBackgroundResource(
                R.drawable.bg_subscription_plan
            )
            binding.llGooglePlay.setBackgroundResource(
                R.drawable.bg_subscription_plan_selected
            )
            binding.btnContinue.isEnabled = true
            binding.btnContinue.backgroundTintList = colorStateListSelected
        }

        binding.layoutRazorPay.setOnClickListener {
            //startRazorPayPayment()
            clickPosition = 0
            binding.rlRazorPay.setBackgroundResource(
                R.drawable.bg_subscription_plan_selected
            )
            binding.llGooglePlay.setBackgroundResource(
                R.drawable.bg_subscription_plan
            )
            binding.btnContinue.isEnabled = true
            binding.btnContinue.backgroundTintList = colorStateListSelected
        }

        binding.btnContinue.setOnClickListener {
            if (clickPosition == 0)
                startRazorPayPayment()
            else
                startGooglePay()
        }

    }

    private fun startGooglePay() {
        val plan = planList[position]
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_TIME) {
            showToast("Please wait before making another purchase")
            return
        }
        lastClickTime = currentTime

        receivedProductId = plan.googlePlay

        if (type == "FACIAL_SCAN") {
            receivedProductType = "BOOSTER"
            if (plan.title?.contains("Pack of 12", true) == true)
                logPurchaseTapEvent(AnalyticsEvent.Booster_FaceScan12_Tap)
            else
                logPurchaseTapEvent(AnalyticsEvent.Booster_FaceScan1_Tap)

            plan.googlePlay?.let { launchPurchaseFlow(it, BillingClient.ProductType.INAPP) }
        } else {
            if (plan.status.equals("ACTIVE", ignoreCase = true)) {
                showToast("This plan is currently active.")
                return
            }

            var hasActiveSubscription = false
            planList.forEach {
                if (it.status.equals("ACTIVE", ignoreCase = true)) {
                    hasActiveSubscription = true
                    return@forEach
                }
            }

            if (hasActiveSubscription) {
                showToast("You have currently one Active Subscription!!")
            } else {
                receivedProductType = "SUBSCRIPTION"
                if (plan.title.equals("MONTHLY", true))
                    logPurchaseTapEvent(AnalyticsEvent.Subscription_Monthly_Tap)
                else
                    logPurchaseTapEvent(AnalyticsEvent.Subscription_Annual_Tap)

                plan.googlePlay?.let { launchPurchaseFlow(it, BillingClient.ProductType.SUBS) }
            }
        }
    }

    private fun setPriceAmount(position: Int) {

        val productDetails = planList[position].googlePlay?.let { productDetailsMap[it] }

        if (productDetails != null) {
            // Use Play Store pricing with proper currency
            displayPlayStorePricing(productDetails, planList[position])
        } else {
            // Fallback to backend pricing
            displayBackendPricing(planList[position])
        }

        // Show discount percentage if available
        if (planList[position].discountPercent != null && planList[position].discountPercent!! > 0) {
            binding.planOfferDiscount.visibility = View.VISIBLE
            binding.planOfferDiscount.text = " (${planList[position].discountPercent}% Off)"
        } else {
            binding.planOfferDiscount.visibility = View.GONE
        }
    }


    private fun addPaymentIcons(llIcons: LinearLayout, iconList: List<Int>, maxVisible: Int = 4) {
        llIcons.removeAllViews()

        val toShow = iconList.take(maxVisible)
        val remaining = iconList.size - maxVisible

        // Add visible icons
        toShow.forEachIndexed { index, iconRes ->
            val iv = ImageView(llIcons.context).apply {

                layoutParams = LinearLayout.LayoutParams(30.dp, 22.dp).apply {
                    if (index != 0) marginStart = 1.dp
                    marginEnd = 1.dp
                    topMargin = 1.dp
                    bottomMargin = 1.dp
                }

                setPadding(1.dp, 1.dp, 1.dp, 1.dp)

                background = ContextCompat.getDrawable(context, R.drawable.bg_icon_border)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setImageResource(iconRes)
            }
            llIcons.addView(iv)

        }

        // Add +X bubble
        if (remaining > 0) {
            val tv = TextView(llIcons.context).apply {
                text = "+$remaining"
                textSize = 14f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setPadding(10.dp, 3.dp, 10.dp, 3.dp)
                background = ContextCompat.getDrawable(context, R.drawable.bg_small_circle)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 8.dp
                }
            }
            llIcons.addView(tv)
        }
    }

    private fun addBulletPointsWithGreenTick(container: LinearLayout, points: List<String>) {

        container.removeAllViews()  // Clear old items

        points.forEachIndexed { index, point ->

            // Parent row
            val row = LinearLayout(container.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index != 0) topMargin = 6.dp
                }
            }

            // Tick icon
            val iv = ImageView(container.context).apply {
                setImageResource(R.drawable.greentick)  // your green tick drawable
                layoutParams = LinearLayout.LayoutParams(14.dp, 14.dp)
            }

            // Text
            val tv = TextView(container.context).apply {
                text = point
                setTextColor(Color.parseColor("#14142A"))
                typeface = ResourcesCompat.getFont(context, R.font.dmsans_regular)
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 8.dp
                }
            }

            row.addView(iv)
            row.addView(tv)

            container.addView(row)
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

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing", "Billing setup successful")
                    // Query product details if plans are already loaded
                    if (planList.isNotEmpty()) {
                        queryAllProductDetails()
                    }
                } else {
                    Log.e("Billing", "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("Billing", "Billing service disconnected")
                initializeBillingClient()
            }
        })
    }

    private fun queryAllProductDetails() {
        if (!::billingClient.isInitialized || !billingClient.isReady) {
            Log.w("Billing", "BillingClient not ready")
            return
        }

        lifecycleScope.launch {
            try {
                // Separate products by type
                val inAppProducts = mutableListOf<QueryProductDetailsParams.Product>()
                val subsProducts = mutableListOf<QueryProductDetailsParams.Product>()

                planList.forEach { plan ->
                    plan.googlePlay?.let { productId ->
                        val product = QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)

                        if (type == "FACIAL_SCAN") {
                            product.setProductType(BillingClient.ProductType.INAPP)
                            inAppProducts.add(product.build())
                        } else {
                            product.setProductType(BillingClient.ProductType.SUBS)
                            subsProducts.add(product.build())
                        }
                    }
                }

                // Query in-app products
                if (inAppProducts.isNotEmpty()) {
                    val inAppParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(inAppProducts)
                        .build()

                    val inAppResult = billingClient.queryProductDetails(inAppParams)
                    if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        inAppResult.productDetailsList?.forEach { productDetails ->
                            productDetailsMap[productDetails.productId] = productDetails
                            Log.d("Billing", "Loaded in-app product: ${productDetails.productId}")
                        }
                    }
                }

                // Query subscription products
                if (subsProducts.isNotEmpty()) {
                    val subsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(subsProducts)
                        .build()

                    val subsResult = billingClient.queryProductDetails(subsParams)
                    if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        subsResult.productDetailsList?.forEach { productDetails ->
                            productDetailsMap[productDetails.productId] = productDetails
                            Log.d("Billing", "Loaded subscription: ${productDetails.productId}")
                        }
                    }
                }

                setPriceAmount(position)

            } catch (e: Exception) {
                Log.e("Billing", "Error querying product details", e)
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "Purchase canceled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Purchase error: ${billingResult.debugMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            when (receivedProductType) {
                "BOOSTER" -> {
                    lifecycleScope.launch {
                        try {
                            val consumeParams = ConsumeParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()

                            val result = billingClient.consumePurchase(consumeParams)

                            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                Toast.makeText(
                                    this@SubscriptionCheckoutActivity,
                                    "Consumable purchase successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("Billing--", "Consumable purchase successful")
                                updateBackendForPurchase(purchase)
                                isSubscriptionTaken = true
                                showPaymentStatusBottomSheet()
                            } else {
                                Toast.makeText(
                                    this@SubscriptionCheckoutActivity,
                                    "Consume failed: ${result.billingResult.debugMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Log.e("Billing", "Error consuming purchase", e)
                            Toast.makeText(
                                this@SubscriptionCheckoutActivity,
                                "Error consuming purchase: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                "SUBSCRIPTION" -> {
                    if (!purchase.isAcknowledged) {
                        lifecycleScope.launch {
                            try {
                                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()

                                val result = billingClient.acknowledgePurchase(acknowledgeParams)

                                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                    Toast.makeText(
                                        this@SubscriptionCheckoutActivity,
                                        "Subscription acknowledged",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showSubscriptionStatus(purchase)
                                    updateBackendForPurchase(purchase)
                                    isSubscriptionTaken = true
                                    showPaymentStatusBottomSheet()
                                } else {
                                    Toast.makeText(
                                        this@SubscriptionCheckoutActivity,
                                        "Acknowledge failed: ${result.debugMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e("Billing", "Error acknowledging purchase", e)
                                Toast.makeText(
                                    this@SubscriptionCheckoutActivity,
                                    "Error acknowledging purchase: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        showSubscriptionStatus(purchase)
                    }
                }
            }
        }
    }

    private fun showSubscriptionStatus(purchase: Purchase) {
        val purchaseTime =
            java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(purchase.purchaseTime))
    }

    private fun updateBackendForPurchase(purchase: Purchase) {
        val paymentSuccessRequest = PaymentSuccessRequest()
        paymentSuccessRequest.planId = planList[position].id
        paymentSuccessRequest.planName = planList[position].purchase?.planName
        paymentSuccessRequest.paymentGateway = "googlePlay"
        paymentSuccessRequest.orderId = purchase.orderId
        paymentSuccessRequest.environment = "payment"
        paymentSuccessRequest.notifyType = "SDK"
        paymentSuccessRequest.couponId = ""
        paymentSuccessRequest.obfuscatedExternalAccountId = ""
        paymentSuccessRequest.price = planList[position].price?.inr.toString()

        val sdkDetail = SdkDetail()
        sdkDetail.price = planList[position].price?.inr.toString()
        sdkDetail.orderId = purchase.orderId ?: ""
        sdkDetail.title = ""
        sdkDetail.environment = "payment"
        sdkDetail.description = ""
        sdkDetail.currencyCode = "INR"
        sdkDetail.currencySymbol = "₹"

        paymentSuccessRequest.sdkDetail = sdkDetail
        saveSubscriptionSuccess(paymentSuccessRequest)
        logPurchaseEvent()
    }

    private fun saveSubscriptionSuccess(paymentSuccessRequest: PaymentSuccessRequest) {
        val call = apiService.savePaymentSuccess(
            sharedPreferenceManager.accessToken,
            paymentSuccessRequest
        )
        call.enqueue(object : Callback<PaymentSuccessResponse> {
            override fun onResponse(
                call: Call<PaymentSuccessResponse>,
                response: Response<PaymentSuccessResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.id?.let { updatePaymentId(it) }
                    showToast(response.message())
                } else {
                    showToast(response.message())
                }
            }

            override fun onFailure(call: Call<PaymentSuccessResponse>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun logPurchaseEvent() {
        if (receivedProductType == "BOOSTER") {
            AnalyticsLogger.logEvent(
                this,
                AnalyticsEvent.FACE_SCAN_PURCHASE_COMPLETED,
                mapOf(AnalyticsParam.PRODUCT_ID to "${planList[position].googlePlay}")
            )
        } else {
            AnalyticsLogger.logEvent(
                this,
                AnalyticsEvent.SUBSCRIPTION_PURCHASE_COMPLETED,
                mapOf(AnalyticsParam.PRODUCT_ID to "${planList[position].googlePlay}")
            )
        }
    }

    private fun updatePaymentId(paymentId: String) {
        val call = apiService.getPaymentIntent(sharedPreferenceManager.accessToken, paymentId)
        call.enqueue(object : Callback<PaymentIntentResponse> {
            override fun onResponse(
                call: Call<PaymentIntentResponse>,
                response: Response<PaymentIntentResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    if (response.body()?.status != "")
                        showToast(response.message())
                    //getSubscriptionList(type)
                } else {
                    showToast(response.message())
                }
            }

            override fun onFailure(call: Call<PaymentIntentResponse>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun showPlanListBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetPlanSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        if (type == "FACIAL_SCAN")
            dialogBinding.tvHeader.text = "Choose A Pack"
        else
            dialogBinding.tvHeader.text = "Choose A Plan"


        val adapter =
            PlanSelectionAdapter(type, planList, productDetailsMap, position) { selectedPosition ->
                position = selectedPosition
                setPriceAmount(position)
                bottomSheetDialog.dismiss()
            }

        dialogBinding.recyclerViewPlanList.layoutManager = LinearLayoutManager(this)
        dialogBinding.recyclerViewPlanList.adapter = adapter

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun logPurchaseTapEvent(EventName: String = AnalyticsEvent.Subscription_Monthly_Tap) {
        AnalyticsLogger.logEvent(
            this,
            EventName,
            mapOf(
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.PRODUCT_ID to "${planList[position].googlePlay}"
            )
        )
    }

    // NEW: Simplified launch purchase flow
    private fun launchPurchaseFlow(productId: String, productType: String) {
        val productDetails = productDetailsMap[productId]
        if (productDetails == null) {
            showToast("Product details not available. Please try again.")
            return
        }
        launchBillingFlow(productDetails)
    }

    private fun launchBillingFlow(productDetails: ProductDetails) {
        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
            if (offerDetails == null) {
                Toast.makeText(this, "No subscription offers available", Toast.LENGTH_SHORT).show()
                return
            }

            val offerToken = offerDetails.offerToken
            if (offerToken.isNullOrEmpty()) {
                Toast.makeText(this, "Offer token missing", Toast.LENGTH_SHORT).show()
                return
            }

            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            billingClient.launchBillingFlow(this, billingFlowParams)
        } else {
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            billingClient.launchBillingFlow(this, billingFlowParams)
        }
    }

    private fun startRazorPayPayment() {
        val plan = planList[position]

        // Show loading indicator
        binding.layoutRazorPay.isEnabled = false
        // You can show a progress dialog here if needed

        // Create order request
        val orderRequestRazorpay = OrderRequestRazorpay(
            userId = sharedPreferenceManager.userId
                ?: "", // Make sure you have userId in SharedPreferenceManager
            planId = plan.id ?: "",
            amount = plan.price?.inr ?: 0,
            currency = "INR",
            receipt = "rcpt_${System.currentTimeMillis()}"
        )

        // Call backend to create order
        val call = apiService.createPaymentOrder(
            sharedPreferenceManager.accessToken,
            orderRequestRazorpay
        )

        call.enqueue(object : Callback<OrderResponseRazorpay> {
            override fun onResponse(
                call: Call<OrderResponseRazorpay>,
                response: Response<OrderResponseRazorpay>
            ) {
                binding.layoutRazorPay.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val orderData = response.body()!!.data
                    initiateRazorpayCheckout(orderData)
                } else {
                    showToast("Failed to create order. Please try again.")
                    Log.e("RazorPay", "Order creation failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<OrderResponseRazorpay>, t: Throwable) {
                binding.layoutRazorPay.isEnabled = true
                handleNoInternetView(t)
                Log.e("RazorPay", "Order creation error", t)
            }
        })
    }

    private fun initiateRazorpayCheckout(orderData: OrderDataRazorpay) {
        val co = Checkout()
        co.setKeyID(getString(R.string.razorpay_key))

        try {
            val plan = planList[position]
            val options = JSONObject()

            // Use order ID from backend
            options.put("order_id", orderData.orderId)
            options.put("name", "RightLife")
            options.put("description", plan.title ?: "Subscription Plan")
            options.put("currency", orderData.currency)
            options.put("amount", orderData.amount * 100) // Convert to paise

            // Get user info from shared preferences
            val preFill = JSONObject()
            preFill.put("email", sharedPreferenceManager.email ?: "")
            preFill.put("contact", sharedPreferenceManager.mobile ?: "")
            options.put("prefill", preFill)

            // Theme customization (optional)
            val theme = JSONObject()
            theme.put("color", "#3399cc")
            options.put("theme", theme)

            // Store order data for success callback
            sharedPreferenceManager.saveString("pending_order_id", orderData.dbOrderId)
            sharedPreferenceManager.saveString("pending_razorpay_order_id", orderData.orderId)

            co.open(this, options)

        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error: ${e.localizedMessage}")
            Log.e("RazorPay", "Checkout error", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Log.d("RazorPay", "Payment successful: $razorpayPaymentId")
        showPaymentStatusBottomSheet()
        if (razorpayPaymentId.isNullOrEmpty()) {
            showToast("Payment ID not received")
            return
        }

        // Get stored order data
        val dbOrderId = sharedPreferenceManager.getString("pending_db_order_id", "")
        val razorpayOrderId = sharedPreferenceManager.getString("pending_razorpay_order_id", "")

        // Create payment success request
        val paymentSuccessRequest = PaymentSuccessRequest()
        paymentSuccessRequest.planId = planList[position].id
        paymentSuccessRequest.planName = planList[position].purchase?.planName
        paymentSuccessRequest.paymentGateway = "googlePlay"
        paymentSuccessRequest.orderId = razorpayOrderId
        paymentSuccessRequest.environment = "payment"
        paymentSuccessRequest.notifyType = "SDK"
        paymentSuccessRequest.couponId = ""
        paymentSuccessRequest.obfuscatedExternalAccountId = ""
        paymentSuccessRequest.price = planList[position].price?.inr.toString()

        val sdkDetail = SdkDetail()
        sdkDetail.price = planList[position].price?.inr.toString()
        sdkDetail.orderId = razorpayOrderId
        sdkDetail.title = planList[position].title ?: ""
        sdkDetail.environment = "payment"
        sdkDetail.description = "Razorpay Payment - $razorpayPaymentId"
        sdkDetail.currencyCode = "INR"
        sdkDetail.currencySymbol = "₹"

        paymentSuccessRequest.sdkDetail = sdkDetail

        // Clear stored order data
        sharedPreferenceManager.removeKey("pending_db_order_id")
        sharedPreferenceManager.removeKey("pending_razorpay_order_id")

        // Save to backend
        saveSubscriptionSuccess(paymentSuccessRequest)

        // Log analytics event
        logRazorpayPurchaseEvent()

        showToast("Payment successful!")
        isSubscriptionTaken = true
    }

    override fun onPaymentError(errorCode: Int, errorMessage: String?) {
        Log.e("RazorPay", "Payment failed: $errorCode - $errorMessage")

        // Clear stored order data
        sharedPreferenceManager.removeKey("pending_db_order_id")
        sharedPreferenceManager.removeKey("pending_razorpay_order_id")

        if (errorCode != 0)
            showCustomToast("Payment failed: ${errorMessage ?: "Unknown error"}")
    }

    private fun logRazorpayPurchaseEvent() {
        val eventName = if (type == "FACIAL_SCAN") {
            AnalyticsEvent.FACE_SCAN_PURCHASE_COMPLETED
        } else {
            AnalyticsEvent.SUBSCRIPTION_PURCHASE_COMPLETED
        }

        /*AnalyticsLogger.logEvent(
                this,
                eventName,
                mapOf(
                        AnalyticsParam.PRODUCT_ID to "${planList[position].id}",
                        AnalyticsParam.PAYMENT_METHOD to "razorpay",
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis()
                )
        )*/
    }



    // show Success bottom sheet
    private fun showPaymentStatusBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetPaymentStatusResultBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                    AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

      /*  dialogBinding.btnNO.setOnClickListener {
            dialogBinding.btnNO.disableViewForSeconds()
            bottomSheetDialog.dismiss()


        }

        dialogBinding.btnYes.setOnClickListener {
            dialogBinding.btnYes.disableViewForSeconds()
            bottomSheetDialog.dismiss()

        }*/
        dialogBinding.root.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Post a delayed action to dismiss the dialog after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
                                                        // Check if the dialog is still showing to prevent crashes if the activity is destroyed
                                                        if (bottomSheetDialog.isShowing) {
                                                            bottomSheetDialog.dismiss()
                                                        }
                                                    }, 3000) // 3000 milliseconds = 3 seconds

        bottomSheetDialog.show()
    }
}