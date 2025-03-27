package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ThinkRightAnswerRequest {
    @SerializedName("questionOne")
    @Expose
    var questionOne: TRQuestionOne? = null

    @SerializedName("questionTwo")
    @Expose
    var questionTwo: TRQuestionTwo? = null

    @SerializedName("questionThree")
    @Expose
    var questionThree: TRQuestionThree? = null

    @SerializedName("questionFour")
    @Expose
    var questionFour: TRQuestionFour? = null

    @SerializedName("questionFive")
    @Expose
    var questionFive: TRQuestionFive? = null

    @SerializedName("questionSix")
    @Expose
    var questionSix: TRQuestionSix? = null

    @SerializedName("questionSeven")
    @Expose
    var questionSeven: TRQuestionSeven? = null
}