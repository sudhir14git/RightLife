package com.jetsynthesys.rightlife.ui.profile_new.pojo

data class VerifyOtpRequest(
    val phoneNumber: String,
    val otp: String
)

data class VerifyOtpEmailRequest(
    val email: String,
    val otp: String
)