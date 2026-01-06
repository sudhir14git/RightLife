package com.jetsynthesys.rightlife.subscriptions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivitySubscriptionPlanListBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.subscriptions.adapter.SubscriptionPlanAdapter
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentIntentResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessRequest
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.PlanList
import com.jetsynthesys.rightlife.subscriptions.pojo.SdkDetail
import com.jetsynthesys.rightlife.subscriptions.pojo.SubscriptionPlansResponse
import com.jetsynthesys.rightlife.ui.settings.GeneralInformationActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.Utils.showCustomToast
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SubscriptionPlanListActivity : BaseActivity(), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    private lateinit var binding: ActivitySubscriptionPlanListBinding
    private lateinit var adapter: SubscriptionPlanAdapter
    private var planType: String = "FACIAL_SCAN"
    private val planList = ArrayList<PlanList>()
    private var selectedPlan: PlanList? = null
    private var receivedProductId: String? = null
    private var receivedProductType = "BOOSTER"
    private var isSubscriptionTaken: Boolean = false
    private var lastClickTime = 0L
    private val CLICK_DEBOUNCE_TIME = 2000L

    // NEW: Store product details from Play Store
    private val productDetailsMap = HashMap<String, ProductDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionPlanListBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        planType = intent.getStringExtra("SUBSCRIPTION_TYPE").toString()

        if (planType == "FACIAL_SCAN") {
            binding.tvHeader.text = "Booster Packs"
        } else
            binding.tvHeader.text = "Subscription Plans"

        // Initialize billing client first
        initializeBillingClient()

        // Then get subscription list
        getSubscriptionList(planType)

        binding.continueButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.menuselected)

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this) {
            if (isSubscriptionTaken) {
                val returnIntent = Intent()
                returnIntent.putExtra("result", isSubscriptionTaken)
                setResult(RESULT_OK, returnIntent)
            }
            finish()
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, GeneralInformationActivity::class.java).apply {
                putExtra("INFO", "Privacy Policy")
            })
        }
        binding.tvTermsCondition.setOnClickListener {
            startActivity(Intent(this, GeneralInformationActivity::class.java).apply {
                putExtra("INFO", "Terms & Conditions")
            })
        }

        binding.iconInfo.setOnClickListener {
            startActivity(Intent(this, PlanInfoActivity::class.java))
        }

        adapter = SubscriptionPlanAdapter(
            planType,
            plans = planList,
            productDetailsMap = productDetailsMap,

            onPlanSelected = { selectedPlan, position ->
                startActivity(Intent(this, SubscriptionCheckoutActivity::class.java).apply {
                    putExtra("PLAN_LIST", planList)
                    putExtra("POSITION", position)
                    putExtra("SUBSCRIPTION_TYPE", planType)
                })
                finish()
            },

            onBuyClick = { position ->
                startActivity(Intent(this, SubscriptionCheckoutActivity::class.java).apply {
                    putExtra("PLAN_LIST", planList)
                    putExtra("POSITION", position)
                    putExtra("SUBSCRIPTION_TYPE", planType)
                })
                finish()
            }
        )

        binding.plansRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.plansRecyclerView.adapter = adapter

        binding.cancelButton.setOnClickListener {
            openPlayStoreSubscriptionPage()
            AnalyticsLogger.logEvent(
                this,
                AnalyticsEvent.ManageSubs_CancelSubs,
                mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
            )

            AnalyticsLogger.logEvent(
                this,
                AnalyticsEvent.Subscription_CancelSubscription_Tap,
                mapOf(AnalyticsParam.TIMESTAMP to System.currentTimeMillis())
            )
        }

        binding.continueButton.setOnClickListener {
            restorePurchases()
        }

        if (planType == "FACIAL_SCAN") {
            binding.cancelButton.visibility = View.GONE
            binding.continueButton.visibility = View.VISIBLE
            binding.iconInfo.visibility = View.GONE
        } else {
            binding.continueButton.visibility = View.GONE
            binding.continueButton.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        getSubscriptionList(planType)
    }

    private fun openPlayStoreSubscriptionPage() {
        val packageName = applicationContext.packageName
        val uri =
            Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.android.vending")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Play Store not found on device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSubscriptionList(type: String) {
        val call = apiService.getSubscriptionPlanList(sharedPreferenceManager.accessToken, type)
        call.enqueue(object : Callback<SubscriptionPlansResponse> {
            override fun onResponse(
                call: Call<SubscriptionPlansResponse>,
                response: Response<SubscriptionPlansResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    planList.clear()
                    response.body()?.data?.result?.list?.let {
                        planList.addAll(it)
                        // NEW: Query product details for all plans
                        queryAllProductDetails()
                    }
                } else {
                    showToast(response.message())
                }
            }

            override fun onFailure(call: Call<SubscriptionPlansResponse>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    // NEW: Initialize billing client once at startup
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

    // NEW: Query all product details at once
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

                        if (planType == "FACIAL_SCAN") {
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

                // Refresh adapter with new pricing data
                adapter.notifyDataSetChanged()

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
                                    this@SubscriptionPlanListActivity,
                                    "Consumable purchase successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("Billing--", "Consumable purchase successful")
                                updateBackendForPurchase(purchase)
                                isSubscriptionTaken = true
                            } else {
                                Toast.makeText(
                                    this@SubscriptionPlanListActivity,
                                    "Consume failed: ${result.billingResult.debugMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Log.e("Billing", "Error consuming purchase", e)
                            Toast.makeText(
                                this@SubscriptionPlanListActivity,
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
                                        this@SubscriptionPlanListActivity,
                                        "Subscription acknowledged",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showSubscriptionStatus(purchase)
                                    updateBackendForPurchase(purchase)
                                    isSubscriptionTaken = true
                                } else {
                                    Toast.makeText(
                                        this@SubscriptionPlanListActivity,
                                        "Acknowledge failed: ${result.debugMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e("Billing", "Error acknowledging purchase", e)
                                Toast.makeText(
                                    this@SubscriptionPlanListActivity,
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
        paymentSuccessRequest.planId = selectedPlan?.id
        paymentSuccessRequest.planName = selectedPlan?.purchase?.planName
        paymentSuccessRequest.paymentGateway = "googlePlay"
        paymentSuccessRequest.orderId = purchase.orderId
        paymentSuccessRequest.environment = "payment"
        paymentSuccessRequest.notifyType = "SDK"
        paymentSuccessRequest.couponId = ""
        paymentSuccessRequest.obfuscatedExternalAccountId = ""
        paymentSuccessRequest.price = selectedPlan?.price?.inr.toString()

        val sdkDetail = SdkDetail()
        sdkDetail.price = selectedPlan?.price?.inr.toString()
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

    private fun updatePaymentId(paymentId: String) {
        val call = apiService.getPaymentIntent(sharedPreferenceManager.accessToken, paymentId)
        call.enqueue(object : Callback<PaymentIntentResponse> {
            override fun onResponse(
                call: Call<PaymentIntentResponse>,
                response: Response<PaymentIntentResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    showToast(response.message())
                    getSubscriptionList(planType)
                } else {
                    showToast(response.message())
                }
            }

            override fun onFailure(call: Call<PaymentIntentResponse>, t: Throwable) {
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
                mapOf(AnalyticsParam.PRODUCT_ID to "${selectedPlan?.googlePlay}")
            )
        } else {
            AnalyticsLogger.logEvent(
                this,
                AnalyticsEvent.SUBSCRIPTION_PURCHASE_COMPLETED,
                mapOf(AnalyticsParam.PRODUCT_ID to "${selectedPlan?.googlePlay}")
            )
        }
    }

    // RESTORE: Safely query existing purchases and route them through existing handlePurchase()
    private fun restorePurchases() {
        if (!::billingClient.isInitialized) {
            initializeBillingClient()
            Toast.makeText(this, "Please try again in a moment…", Toast.LENGTH_SHORT).show()
            return
        }

        if (!billingClient.isReady) {
            Toast.makeText(this, "Billing service not ready. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        var anyPurchaseFound = false

        // --- Restore INAPP (boosters) ---
        val inAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

        billingClient.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                // Filter only purchases that match our known productIds
                val matchingPurchases = purchases.filter { purchase ->
                    purchase.products.any { productId ->
                        planList.any { plan -> plan.googlePlay == productId }
                    }
                }

                if (matchingPurchases.isNotEmpty()) {
                    anyPurchaseFound = true
                    // BOOSTER purchases
                    receivedProductType = "BOOSTER"
                    matchingPurchases.forEach { purchase ->
                        handlePurchase(purchase)
                    }
                }
            } else {
                Log.d("Billing-Restore", "No INAPP purchases to restore: ${billingResult.debugMessage}")
            }

            // We only show "nothing to restore" once both INAPP and SUBS queries are done.
            // So we don't show toast here yet.
        }

        // --- Restore SUBSCRIPTIONS ---
        val subsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

        billingClient.queryPurchasesAsync(subsParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                val matchingPurchases = purchases.filter { purchase ->
                    purchase.products.any { productId ->
                        planList.any { plan -> plan.googlePlay == productId }
                    }
                }

                if (matchingPurchases.isNotEmpty()) {
                    anyPurchaseFound = true
                    // SUBSCRIPTION purchases
                    receivedProductType = "SUBSCRIPTION"
                    matchingPurchases.forEach { purchase ->
                        handlePurchase(purchase)
                    }

                    // For subscriptions, this will usually just call showSubscriptionStatus()
                    // if already acknowledged, which is safe.
                }
            } else {
                Log.d("Billing-Restore", "No SUBS purchases to restore: ${billingResult.debugMessage}")
            }

            // After SUBS query completes, if nothing at all was restored, tell the user.
            // After SUBS query completes, if nothing at all was restored, tell the user.
            if (!anyPurchaseFound) {
                showToastOnUiThread("No purchases found to restore for this account.")
                Log.d("Billing-Restore2", "No purchases found to restore for this account.")
            } else {
                showToastOnUiThread("Checking and restoring your purchases…")
                Log.d("Billing-Restore2", "purchases found to restore for this account.")
            }

        }
    }
    private fun showToastOnUiThread(message: String) {
        if (isFinishing || isDestroyed) {
            Log.w("Billing-Restore", "Activity finishing/destroyed, skipping toast: $message")
            return
        }

        runOnUiThread {
            Toast.makeText(
                    applicationContext,   // use applicationContext to be extra safe
                    message,
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStop()
    {
        super.onStop()
        billingClient.endConnection()

    }
}