package com.jetsynthesys.rightlife.ui;

public class CardItem {
    private String id;
    private final String title;
    private final int imageResId;
    private final String imageUrl;
    private final String content;
    private final String buttonText;
    private final String category;
    private String seriesId;
    private boolean isAffirmation = false;
    private String viewCount;
    private String titleImage;
    private String ButtonImage;
    private String seriesType;
    private String selectedContentType;

    public String getSeriesType() {
        return seriesType;
    }

    public void setSeriesType(String seriesType) {
        seriesType = seriesType;
    }



    public String getSelectedContentType() {
        return selectedContentType;
    }

    public void setSelectedContentType(String selectedContentType) {
        this.selectedContentType = selectedContentType;
    }



    public String getTitleImage() {
        return titleImage;
    }

    public void setTitleImage(String titleImage) {
        this.titleImage = titleImage;
    }

    public String getButtonImage() {
        return ButtonImage;
    }

    public void setButtonImage(String buttonImage) {
        ButtonImage = buttonImage;
    }

    public CardItem(String id, String title, int imageResId, String imageUrl, String content, String buttonText, String category, String viewCount, String seriesId,String seriesType,String selectedContentType, String titleImage, String ButtonImage) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.imageResId = imageResId;
        this.buttonText = buttonText;
        this.content = content;
        this.category = category;
        this.viewCount = viewCount;
        this.seriesId = seriesId;
        this.seriesType = seriesType;
        this.selectedContentType = selectedContentType;
        this.titleImage = titleImage;
        this.ButtonImage = ButtonImage;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getButtonText() {
        return buttonText;
    }

    public String getContent() {
        return content;
    }

    public boolean isAffirmation() {
        return isAffirmation;
    }

    public void setAffirmation(boolean affirmation) {
        isAffirmation = affirmation;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
