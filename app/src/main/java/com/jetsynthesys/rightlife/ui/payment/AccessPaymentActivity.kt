package com.jetsynthesys.rightlife.ui.payment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccessPaymentActivity : BaseActivity() {

    private lateinit var rvPaymentCard: RecyclerView
    private lateinit var adapter: AccessPaymentCardAdapter
    private val paymentCardList = ArrayList<PaymentCardList>()
    private lateinit var imagePaymentBg: ImageView
    private lateinit var tvHeader: TextView
    private var type: String? = "FACIAL_SCAN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setChildContentView(R.layout.activity_accesspayment)
        findViewById<View>(R.id.ic_back_dialog).setOnClickListener { finish() }
        rvPaymentCard = findViewById(R.id.rv_payment_card)
        imagePaymentBg = findViewById(R.id.image_payment_bg)
        tvHeader = findViewById(R.id.tv_header_htw)

        type = intent.getStringExtra("ACCESS_VALUE")

        adapter = AccessPaymentCardAdapter(this, paymentCardList, object :
            AccessPaymentCardAdapter.OnItemClickListener {
            override fun onItemClick(paymentCard: PaymentCardList) {
                Toast.makeText(
                    this@AccessPaymentActivity,
                    "OnItemClick",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onBuyClick(paymentCard: PaymentCard) {
                Toast.makeText(
                    this@AccessPaymentActivity,
                    "BuyClick",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onOfferClick(paymentCard: PaymentCard) {
                Toast.makeText(
                    this@AccessPaymentActivity,
                    "OfferClick",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })

        rvPaymentCard.setLayoutManager(LinearLayoutManager(this))
        rvPaymentCard.adapter = adapter

        getPaymentCardList(type)
    }


    // get user details
    private fun getPaymentCardList(type: String?) {
        Utils.showLoader(this)

        val call = apiService.getPaymentPlan(sharedPreferenceManager.accessToken, type)
        call.enqueue(object : Callback<PaymentCardResponse?> {
            override fun onResponse(
                call: Call<PaymentCardResponse?>,
                response: Response<PaymentCardResponse?>
            ) {
                Utils.dismissLoader(this@AccessPaymentActivity)
                if (response.isSuccessful && response.body() != null) {

                    val paymentResponse = response.body()?.data
                    paymentResponse?.result?.list?.let { paymentCardList.addAll(it) }
                    adapter.notifyDataSetChanged()

                    tvHeader.text = paymentResponse?.result?.service?.title

                    GlideApp.with(this@AccessPaymentActivity)
                        .load(ApiClient.CDN_URL_QA + paymentResponse?.result?.service?.moduleImageUrl)
                        .placeholder(R.drawable.ic_healthaudit_logo)
                        .into(imagePaymentBg)

                } else {
                    Toast.makeText(
                        this@AccessPaymentActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }

            override fun onFailure(call: Call<PaymentCardResponse?>, t: Throwable) {
                Utils.dismissLoader(this@AccessPaymentActivity)
                handleNoInternetView(t)
            }
        })
    }
}