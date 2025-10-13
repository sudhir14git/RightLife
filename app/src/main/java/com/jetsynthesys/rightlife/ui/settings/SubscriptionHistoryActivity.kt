package com.jetsynthesys.rightlife.ui.settings

import android.app.DownloadManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivitySubscriptionHistoryBinding
import com.jetsynthesys.rightlife.ui.settings.adapter.PastSubscriptionHistoryAdapter
import com.jetsynthesys.rightlife.ui.settings.adapter.SubscriptionHistoryAdapter
import com.jetsynthesys.rightlife.ui.settings.pojo.PurchaseHistoryResponse
import com.jetsynthesys.rightlife.ui.settings.pojo.Subscription
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubscriptionHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivitySubscriptionHistoryBinding
    private lateinit var pastSubscriptionAdapter: PastSubscriptionHistoryAdapter
    private lateinit var activeSubscriptionAdapter: SubscriptionHistoryAdapter
    private val activeSubscriptions = ArrayList<Subscription>()
    private val pastSubscriptions = ArrayList<Subscription>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionHistoryBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        getSubscriptionHistory()

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        pastSubscriptionAdapter =
            PastSubscriptionHistoryAdapter(pastSubscriptions) { subscription -> // code here for selected item from past subscriptions
                subscription.invoice?.url?.let { url ->
                    downloadInvoiceFile(url, subscription.invoice?.id ?: "invoice")
                } ?: showToast("No invoice available for this plan")
            }
        binding.rvPastSubscription.layoutManager = LinearLayoutManager(this)
        binding.rvPastSubscription.adapter = pastSubscriptionAdapter

        activeSubscriptionAdapter =
            SubscriptionHistoryAdapter(activeSubscriptions) { subscription -> // code here for selected item from active subscriptions
                subscription.invoice?.url?.let { url ->
                    downloadInvoiceFile(url, subscription.invoice?.id ?: "invoice")
                } ?: showToast("No invoice available for this plan")
            }
        binding.rvActiveSubscription.layoutManager = LinearLayoutManager(this)
        binding.rvActiveSubscription.adapter = activeSubscriptionAdapter

    }

    private fun getSubscriptionHistory() {
        Utils.showLoader(this)
        val call = apiService.getSubscriptionHistory(
            SharedPreferenceManager.getInstance(this).accessToken,
            "PURCHASES"
        )
        call.enqueue(object : Callback<PurchaseHistoryResponse> {
            override fun onResponse(
                call: Call<PurchaseHistoryResponse>,
                response: Response<PurchaseHistoryResponse>
            ) {
                Utils.dismissLoader(this@SubscriptionHistoryActivity)
                if (response.isSuccessful && response.body() != null) {
                    response.body()?.data?.subscriptions?.forEach { subscription ->
                        if (subscription.status == "ACTIVE")
                            activeSubscriptions.add(subscription)
                        else
                            pastSubscriptions.add(subscription)

                        if (pastSubscriptions.isEmpty()) {
                            binding.rvPastSubscription.visibility = View.GONE
                            binding.cardViewNoPastSubscription.visibility = View.VISIBLE
                        } else {
                            binding.rvPastSubscription.visibility = View.VISIBLE
                            binding.cardViewNoPastSubscription.visibility = View.GONE
                        }

                        if (activeSubscriptions.isEmpty()) {
                            binding.rvActiveSubscription.visibility = View.GONE
                            binding.cardViewNoActiveSubscription.visibility = View.VISIBLE
                        } else {
                            binding.rvActiveSubscription.visibility = View.VISIBLE
                            binding.cardViewNoActiveSubscription.visibility = View.GONE
                        }

                        activeSubscriptionAdapter.notifyDataSetChanged()
                        pastSubscriptionAdapter.notifyDataSetChanged()
                    }
                } else {
                    Utils.dismissLoader(this@SubscriptionHistoryActivity)
                    showToast(response.message())
                }
            }

            override fun onFailure(call: Call<PurchaseHistoryResponse>, t: Throwable) {
                handleNoInternetView(t)
            }

        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // downloading invice using url.pdf
    private fun downloadInvoiceFile(url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(android.net.Uri.parse(url))
            request.setTitle("Downloading Invoice")
            request.setDescription("Downloading your invoice PDF...")
            request.setDestinationInExternalPublicDir(
                android.os.Environment.DIRECTORY_DOWNLOADS,
                "$fileName.pdf"
            )
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(this, "Invoice download started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to start download", Toast.LENGTH_SHORT).show()
        }
    }

}