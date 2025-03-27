package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MoveRightAnswerRequest {
    @SerializedName("questionOne")
    @Expose
    var questionOne: MRQuestionOne? = null

    @SerializedName("questionTwo")
    @Expose
    var questionTwo: MRQuestionTwo? = null

    @SerializedName("questionThree")
    @Expose
    var questionThree: MRQuestionThree? = null

    @SerializedName("questionFour")
    @Expose
    var questionFour: MRQuestionFour? = null

    @SerializedName("questionFive")
    @Expose
    var questionFive: MRQuestionFive? = null

    @SerializedName("questionSix")
    @Expose
    var questionSix: MRQuestionSix? = null

    @SerializedName("questionSeven")
    @Expose
    var questionSeven: MRQuestionSeven? = null
}