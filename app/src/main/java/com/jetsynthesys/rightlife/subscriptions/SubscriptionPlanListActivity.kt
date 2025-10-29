package com.jetsynthesys.rightlife.subscriptions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
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
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivitySubscriptionPlanListBinding
import com.jetsynthesys.rightlife.subsciptions.BillingActivity
import com.jetsynthesys.rightlife.subscriptions.adapter.SubscriptionPlanAdapter
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentIntentResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessRequest
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.PlanList
import com.jetsynthesys.rightlife.subscriptions.pojo.SdkDetail
import com.jetsynthesys.rightlife.subscriptions.pojo.SubscriptionPlansResponse
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SubscriptionPlanListActivity : BaseActivity(), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    private lateinit var binding: ActivitySubscriptionPlanListBinding
    private lateinit var adapter: SubscriptionPlanAdapter
    private var type: String = "FACIAL_SCAN" // Default value, can be overridden by intent
    private val planList = ArrayList<PlanList>()
    private var selectedPlan: PlanList? = null
    private var receivedProductId: String? = null // Will be set from intent
    private var receivedProductType = "BOOSTER" // Default value, can be overridden by intent
    private var isSubscriptionTaken: Boolean = false
    private var lastClickTime = 0L
    private val CLICK_DEBOUNCE_TIME = 2000L // 2 seconds


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionPlanListBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        type = intent.getStringExtra("SUBSCRIPTION_TYPE").toString()
        //val type = "FACIAL_SCAN"
        if (type == "FACIAL_SCAN") {
            binding.tvHeader.text = "Face Scan Booster"
        }
        getSubscriptionList(type)

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

        binding.iconInfo.setOnClickListener {
            startActivity(Intent(this, PlanInfoActivity::class.java))
        }


        adapter = SubscriptionPlanAdapter(planList) { plan ->
            // showToast("Plan Clicked - " + plan.googlePlay)

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < CLICK_DEBOUNCE_TIME) {
                showToast("Please wait before making another purchase")
                return@SubscriptionPlanAdapter
            }
            lastClickTime = currentTime

            selectedPlan = plan // Save plan for later
            val intent = Intent(this, BillingActivity::class.java)
            intent.putExtra("PRODUCT_ID", plan.googlePlay) // Replace "your_specific_product_id"
            receivedProductId = plan.googlePlay // Save product ID for later use
            if (type == "FACIAL_SCAN") {
                intent.putExtra("PRODUCT_TYPE", "BOOSTER") // Replace "your_specific_product_id"
                receivedProductType = "BOOSTER"
                setupBillingClient()
            } else {
                if (plan.status.equals("ACTIVE", ignoreCase = true)) {
                    showToast("This plan is currently active.")
                    return@SubscriptionPlanAdapter
                } else {
                    var flag = false
                    planList.forEach {
                        if (it.status.equals("ACTIVE", ignoreCase = true)) {
                            flag = true
                            return@forEach
                        }
                    }

                    if (flag) {
                        showToast("You have currently one Active Subscription!!")
                    } else {
                        intent.putExtra(
                            "PRODUCT_TYPE",
                            "SUBSCRIPTION"
                        ) // Replace "your_specific_product_id"
                        receivedProductType = "SUBSCRIPTION"
                        setupBillingClient()
                    }
                }
            }
            //startActivity(intent)
        }

        binding.plansRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.plansRecyclerView.adapter = adapter

        binding.cancelButton.setOnClickListener {
            openPlayStoreSubscriptionPage()
        }
        binding.continueButton.setOnClickListener {
            openPlayStoreSubscriptionPage()
        }
        if (type == "FACIAL_SCAN") {
            binding.cancelButton.visibility = View.GONE
            binding.continueButton.visibility = View.GONE
            binding.iconInfo.visibility = View.GONE
        } else {
            binding.cancelButton.visibility = View.VISIBLE
            binding.continueButton.visibility = View.GONE
        }
    }

    private fun openPlayStoreSubscriptionPage() {
        val packageName = applicationContext.packageName
        val uri =
            Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.android.vending") // Optional: force it to open in Play Store app
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Play Store not found on device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSubscriptionList(type: String) {
        val call = apiService.getSubscriptionPlanList(
            sharedPreferenceManager.accessToken,
            type
        )
        call.enqueue(object : Callback<SubscriptionPlansResponse> {
            override fun onResponse(
                call: Call<SubscriptionPlansResponse>,
                response: Response<SubscriptionPlansResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    planList.clear()
                    response.body()?.data?.result?.list?.let { planList.addAll(it) }
                    adapter.notifyDataSetChanged()
                } else {
                    showToast(response.message())
                }
            }

            override fun onFailure(call: Call<SubscriptionPlansResponse>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
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
                    //showToast("Subscribed Successfully!!")
                    showToast(response.message())
                    response.message()
                    getSubscriptionList(type)
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

    // billing flow

    private fun setupBillingClient() {
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
                    //restoreSubscription()
                    if (receivedProductType == "BOOSTER") {
                        if (receivedProductId != null) {
                            queryProductDetails(
                                receivedProductId!!,
                                BillingClient.ProductType.INAPP
                            )
                            //queryProductDetails("product_test_2", BillingClient.ProductType.INAPP)
                        }
                    } else if (receivedProductType == "SUBSCRIPTION") {
                        if (receivedProductId != null) {
                            queryProductDetails(receivedProductId!!, BillingClient.ProductType.SUBS)
                        }
                    }
                } else {
                    Log.e("Billing", "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("Billing", "Billing service disconnected")
                setupBillingClient()
            }
        })
    }

    private fun queryProductDetails(productId: String, productType: String) {
        Log.d("Billing", "Querying product: $productId of type: $productType")

        lifecycleScope.launch {
            try {
                val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(productId)
                                .setProductType(productType)
                                .build()
                        )
                    ).build()

                val result = billingClient.queryProductDetails(queryProductDetailsParams)

                if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (result.productDetailsList?.isNotEmpty() == true) {
                        launchBillingFlow(result.productDetailsList!![0])
                    } else {
                        Toast.makeText(
                            this@SubscriptionPlanListActivity,
                            "Product not found: $productId",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@SubscriptionPlanListActivity,
                        "Query failed: ${result.billingResult.debugMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("Billing", "Error querying product details", e)
                Toast.makeText(
                    this@SubscriptionPlanListActivity,
                    "Error querying product: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
                .setOfferToken(offerToken) // ✅ REQUIRED
                .build()

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            billingClient.launchBillingFlow(this, billingFlowParams)
        } else {
            // For consumables or non-subscription
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            billingClient.launchBillingFlow(this, billingFlowParams)
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
            val productId = purchase.products.firstOrNull()

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
                                //call your method to update UI or backend
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
                                    //call your method to update UI or backend
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
        //subscriptionStatusText.text = "Subscription Active\nStart: $purchaseTime"
    }

    private fun updateBackendForPurchase(purchase: Purchase) {
        // Update backend or UI for booster

        val paymentSuccessRequest = PaymentSuccessRequest()
        paymentSuccessRequest.planId = selectedPlan?.id
        paymentSuccessRequest.planName = selectedPlan?.purchase?.planName
        paymentSuccessRequest.paymentGateway = "googlePlay"
        paymentSuccessRequest.orderId = purchase.orderId//may be getting from payment response
        paymentSuccessRequest.environment = "payment"
        paymentSuccessRequest.notifyType = "SDK"
        paymentSuccessRequest.couponId = ""//may be getting from payment response
        paymentSuccessRequest.obfuscatedExternalAccountId = ""//may be getting from payment response
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
}


/*
package com.jetsynthesys.rightlife.subscriptions

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivitySubscriptionPlanListBinding
import com.jetsynthesys.rightlife.subsciptions.BillingActivity
import com.jetsynthesys.rightlife.subscriptions.adapter.SubscriptionPlanAdapter
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentIntentResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessRequest
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.PlanList
import com.jetsynthesys.rightlife.subscriptions.pojo.SdkDetail
import com.jetsynthesys.rightlife.subscriptions.pojo.SubscriptionPlansResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SubscriptionPlanListActivity : BaseActivity(), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    private lateinit var binding: ActivitySubscriptionPlanListBinding
    private lateinit var adapter: SubscriptionPlanAdapter
    private var type: String = "FACIAL_SCAN" // Default value, can be overridden by intent
    private val planList = ArrayList<PlanList>()
    private var selectedPlan: PlanList? = null
    private var receivedProductId: String? = null // Will be set from intent
    private var receivedProductType = "BOOSTER" // Default value, can be overridden by intent
    private var isSubscriptionTaken: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionPlanListBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        type = intent.getStringExtra("SUBSCRIPTION_TYPE").toString()
        //val type = "FACIAL_SCAN"
        if (type == "FACIAL_SCAN") {
            binding.tvHeader.text = "Face Scan Booster"
        }
        getSubscriptionList(type)

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

        binding.iconInfo.setOnClickListener {
            startActivity(Intent(this, PlanInfoActivity::class.java))
        }

        adapter = SubscriptionPlanAdapter(planList) { plan ->
            // showToast("Plan Clicked - " + plan.googlePlay)
            selectedPlan = plan // Save plan for later
            val intent = Intent(this, BillingActivity::class.java)
            intent.putExtra("PRODUCT_ID", plan.googlePlay) // Replace "your_specific_product_id"
            receivedProductId = plan.googlePlay // Save product ID for later use
            if (type == "FACIAL_SCAN") {
                intent.putExtra("PRODUCT_TYPE", "BOOSTER") // Replace "your_specific_product_id"
                receivedProductType = "BOOSTER"
                setupBillingClient()
            } else {
                if (plan.status.equals("ACTIVE", ignoreCase = true)) {
                    showToast("This plan is currently active.")
                    return@SubscriptionPlanAdapter
                } else {
                    var flag = false
                    planList.forEach {
                        if (it.status.equals("ACTIVE", ignoreCase = true)) {
                            flag = true
                            return@forEach
                        }
                    }

                    if (flag) {
                        showToast("You have currently one Active Subscription!!")
                    } else {
                        intent.putExtra(
                            "PRODUCT_TYPE",
                            "SUBSCRIPTION"
                        ) // Replace "your_specific_product_id"
                        receivedProductType = "SUBSCRIPTION"
                        setupBillingClient()
                    }
                }
            }
            //startActivity(intent)


        }

        binding.plansRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.plansRecyclerView.adapter = adapter

        binding.cancelButton.setOnClickListener {
            openPlayStoreSubscriptionPage()
        }
        binding.continueButton.setOnClickListener {
            openPlayStoreSubscriptionPage()
        }
        if (type == "FACIAL_SCAN") {
            binding.cancelButton.visibility = View.GONE
            binding.continueButton.visibility = View.GONE
        } else {
            binding.cancelButton.visibility = View.VISIBLE
            binding.continueButton.visibility = View.GONE
        }
    }

    private fun openPlayStoreSubscriptionPage() {
        val packageName = applicationContext.packageName
        val uri =
            Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.android.vending") // Optional: force it to open in Play Store app
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Play Store not found on device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSubscriptionList(type: String) {
        val call = apiService.getSubscriptionPlanList(
            sharedPreferenceManager.accessToken,
            type
        )
        call.enqueue(object : Callback<SubscriptionPlansResponse> {
            override fun onResponse(
                call: Call<SubscriptionPlansResponse>,
                response: Response<SubscriptionPlansResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    planList.clear()
                    response.body()?.data?.result?.list?.let { planList.addAll(it) }
                    adapter.notifyDataSetChanged()
                } else {
                    showToast(response.message())
                }
            }

            override fun onFailure(call: Call<SubscriptionPlansResponse>, t: Throwable) {
                handleNoInternetView(t)
            }

        })
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
                    showToast("Subscribed Successfully!!")
                    getSubscriptionList(type)
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

    // billing flow

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing", "Billing setup successful")
                    //restoreSubscription()
                    if (receivedProductType == "BOOSTER") {
                        if (receivedProductId != null) {
                            queryProductDetails(
                                receivedProductId!!,
                                BillingClient.ProductType.INAPP
                            )
                            //queryProductDetails("product_test_2", BillingClient.ProductType.INAPP)
                        }
                    } else if (receivedProductType == "SUBSCRIPTION") {
                        if (receivedProductId != null) {
                            queryProductDetails(receivedProductId!!, BillingClient.ProductType.SUBS)
                        }
                    }
                } else {
                    Log.e("Billing", "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("Billing", "Billing service disconnected")
                setupBillingClient()
            }
        })
    }

    private fun queryProductDetails(productId: String, productType: String) {
        Log.d("Billing", "Querying product: $productId of type: $productType")
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(productType)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList.isNotEmpty()) {
                    launchBillingFlow(productDetailsList[0])
                } else {
                    Toast.makeText(this, "Product not found: $productId", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Query failed: ${billingResult.debugMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
                .setOfferToken(offerToken) // ✅ REQUIRED
                .build()

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            billingClient.launchBillingFlow(this, billingFlowParams)
        } else {
            // For consumables or non-subscription
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            billingClient.launchBillingFlow(this, billingFlowParams)
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
            val productId = purchase.products.firstOrNull()

            when (receivedProductType) {
                "BOOSTER" -> {
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(
                                this,
                                "Consumable purchase successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("Billing--", "Consumable purchase successful")
                            //call your method to update UI or backend
                        } else {
                            Toast.makeText(
                                this,
                                "Consume failed: ${billingResult.debugMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                "SUBSCRIPTION" -> {
                    if (!purchase.isAcknowledged) {
                        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()

                        billingClient.acknowledgePurchase(acknowledgeParams) { result ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                Toast.makeText(
                                    this,
                                    "Subscription acknowledged",
                                    Toast.LENGTH_SHORT
                                ).show()
                                //call your method to update UI or backend
                                showSubscriptionStatus(purchase)
                            } else {
                                Toast.makeText(
                                    this,
                                    "Acknowledge failed: ${result.debugMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        showSubscriptionStatus(purchase)
                    }
                }
            }
            updateBackendForPurchase(purchase)
            isSubscriptionTaken = true
        }
    }

    private fun showSubscriptionStatus(purchase: Purchase) {
        val purchaseTime =
            java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(purchase.purchaseTime))
        //subscriptionStatusText.text = "Subscription Active\nStart: $purchaseTime"
    }


    private fun updateBackendForPurchase(purchase: Purchase) {
        // Update backend or UI for booster

        val paymentSuccessRequest = PaymentSuccessRequest()
        paymentSuccessRequest.planId = selectedPlan?.id
        paymentSuccessRequest.planName = selectedPlan?.purchase?.planName
        paymentSuccessRequest.paymentGateway = "googlePlay"
        paymentSuccessRequest.orderId = purchase.orderId//may be getting from payment response
        paymentSuccessRequest.environment = "payment"
        paymentSuccessRequest.notifyType = "SDK"
        paymentSuccessRequest.couponId = ""//may be getting from payment response
        paymentSuccessRequest.obfuscatedExternalAccountId = ""//may be getting from payment response
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

    }

}
 */