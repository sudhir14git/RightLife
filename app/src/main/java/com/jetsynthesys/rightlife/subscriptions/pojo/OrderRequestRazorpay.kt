package com.jetsynthesys.rightlife.subscriptions.pojo

data class OrderRequestRazorpay(
        val userId: String,
        val planId: String,
        val amount: Any,
        val currency: String = "INR",
        val receipt: String
)
