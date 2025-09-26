package com.jetsynthesys.rightlife.ui.Articles.requestmodels;

public class ArticleBookmarkRequest {
    private String contentId;
    private boolean isBookmarked;

    public ArticleBookmarkRequest(String contentId, boolean isBookmarked) {
        this.contentId = contentId;
        this.isBookmarked = isBookmarked;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setIsBookmarked(boolean isBookmarked) {

        isBookmarked = isBookmarked;
    }
}
