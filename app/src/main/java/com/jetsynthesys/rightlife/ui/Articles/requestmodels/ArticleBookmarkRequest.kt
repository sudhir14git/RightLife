package com.jetsynthesys.rightlife.ui.Articles.requestmodels

class ArticleBookmarkRequest(
    var contentId: String, private val isBookmarked: Boolean,
    episodeId: String = "", contentType: String
) {
    fun isBookmarked(): Boolean {
        return isBookmarked
    }

    fun setIsBookmarked(isBookmarked: Boolean) {
        var isBookmarked = isBookmarked
    }
}
