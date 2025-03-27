package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SleepRightAnswerRequest {
    @SerializedName("questionOne")
    @Expose
    var questionOne: SRQuestionOne? = null

    @SerializedName("questionTwo")
    @Expose
    var questionTwo: SRQuestionTwo? = null

    @SerializedName("questionThree")
    @Expose
    var questionThree: SRQuestionThree? = null

    @SerializedName("questionFour")
    @Expose
    var questionFour: SRQuestionFour? = null

    @SerializedName("questionFive")
    @Expose
    var questionFive: SRQuestionFive? = null

    @SerializedName("questionSix")
    @Expose
    var questionSix: SRQuestionSix? = null
}