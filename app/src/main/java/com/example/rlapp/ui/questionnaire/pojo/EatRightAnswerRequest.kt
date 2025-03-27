package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class EatRightAnswerRequest {
    @SerializedName("questionOne")
    @Expose
    var questionOne: ERQuestionOne? = null

    @SerializedName("questionTwo")
    @Expose
    var questionTwo: ERQuestionTwo? = null

    @SerializedName("questionThree")
    @Expose
    var questionThree: ERQuestionThree? = null

    @SerializedName("questionFour")
    @Expose
    var questionFour: ERQuestionFour? = null

    @SerializedName("questionFive")
    @Expose
    var questionFive: ERQuestionFive? = null

    @SerializedName("questionSix")
    @Expose
    var questionSix: ERQuestionSix? = null

    @SerializedName("questionSeven")
    @Expose
    var questionSeven: ERQuestionSeven? = null
}