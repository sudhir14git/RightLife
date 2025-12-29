data class PromotionWeeklyResponse(
    val success: Boolean,
    val statusCode: Int,
    val data: Data
) {

    data class Data(
        val total: Int,
        val promotionList: List<PromotionItem>,
        val sectionTitle: String,
        val sectionSubtitle: String,
        val isShowGrid: Boolean,
        val scrollTime: Int,
        val isScrollReverse: Boolean
    )

    data class PromotionItem(
        val name: String,
        val contentType: String,
        val category: String,
        val content: String,
        val isActive: Boolean,
        val navigationModule: String,
        val views: Int,
        val order: Int,
        val buttonName: String,
        val titleImage: String,
        val buttonImage: String,
        val desktopImage: String,
        val seriesType: String,
        val seriesId: String,
        val contentId: String,
        val moduleId: String,
        val selectedContentType: String,
        val day: String,
        val deviceType: DeviceType,
        val isDeleted: Boolean,
        val _id: String
    )

    data class DeviceType(
        val ios: Boolean,
        val android: Boolean,
        val web: Boolean
    )
}
