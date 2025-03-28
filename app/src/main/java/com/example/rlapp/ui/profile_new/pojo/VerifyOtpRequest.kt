package com.example.rlapp.ui.profile_new.pojo

data class VerifyOtpRequest(
    val phoneNumber: String,
    val otp: String
)