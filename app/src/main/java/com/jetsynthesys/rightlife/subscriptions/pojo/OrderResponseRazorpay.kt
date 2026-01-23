package com.jetsynthesys.rightlife.subscriptions.pojo

data class  OrderResponseRazorpay
(
        val success: Boolean,
        val statusCode: Int,
        val data: OrderDataRazorpay
)