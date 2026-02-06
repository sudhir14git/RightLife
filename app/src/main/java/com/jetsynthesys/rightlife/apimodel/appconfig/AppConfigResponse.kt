package com.jetsynthesys.rightlife.apimodel.appconfig

data class AppConfigResponse(
    val success: Boolean? = null,
    val statusCode: Int? = null,
    val data: Data? = null
) {
    data class Data(
        val _id: String? = null,
        val __v: Int? = null,
        val appVersion: AppVersion? = null,
        val createdAt: String? = null,
        val forceUpdate: ForceUpdate? = null,
        val imageUrl: ImageUrl? = null,
        val maintenance: Maintenance? = null,
        val razorpay: Razorpay? = null,
        val signInWithEmail: Boolean? = null,
        val signInWithMobile: Boolean? = null,
        val updatedAt: String? = null,
        val isChallengeStart: Boolean = false
    )

    data class AppVersion(
        val android: PlatformVersion? = null,
        val ios: PlatformVersion? = null
    )

    data class PlatformVersion(
        val min: String? = null,
        val latest: String? = null
    )

    data class ForceUpdate(
        val enabled: Boolean? = null,
        val minIOSVersion: String? = null,
        val latestIOSVersion: String? = null,
        val updateIOSUrl: String? = null,
        val minAndroidVersion: String? = null,
        val latestAndroidVersion: String? = null,
        val updateAndroidUrl: String? = null,
        val message: String? = null
    )

    data class ImageUrl(
        val baseUrl: String? = null,
        val cdnUrl: String? = null
    )

    data class Maintenance(
        val ios: MaintenanceInfo,

        val android: MaintenanceInfo
    )

    data class MaintenanceInfo(
        val enabled: Boolean,
        val message: String
    )

    data class Razorpay(
        val enabled: Boolean? = null,
        val environment: String? = null,
        val keyId: String? = null
    )
}
