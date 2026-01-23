package com.jetsynthesys.rightlife.subscriptions.pojo


data class OrderDataRazorpay(
        val orderId: String,
        val amount: Int,
        val currency: String,
        val receipt: String,
        val dbOrderId: String
)