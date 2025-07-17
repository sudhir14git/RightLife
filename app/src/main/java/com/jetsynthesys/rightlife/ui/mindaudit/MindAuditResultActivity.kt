package com.jetsynthesys.rightlife.ui.mindaudit

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityMindAuditResultBinding
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class MindAuditResultActivity : BaseActivity() {
    private val allAssessments = java.util.ArrayList<String>()
    private val suggestedAssessments = java.util.ArrayList<String>()
    private var userEmotionsString: ArrayList<String> = ArrayList()
    private lateinit var binding: ActivityMindAuditResultBinding
    private var suggestedAssessmentAdapter: OtherAssessmentsAdapter? = null
    private var suggestedAssessmentString = java.util.ArrayList<String>()
    private var selectedAssessment = "CAS"
    private var emotionsAdapter: EmotionsAdapter? = null
    private var reportId: String? = null
    private var isFrom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMindAuditResultBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        reportId = intent.getStringExtra("REPORT_ID")
        isFrom = intent.getStringExtra("FROM")

        binding.iconBack.setOnClickListener {
            onBackPressHandle()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressHandle()
            }
        })

        binding.btnTakeAssessment.setOnClickListener {
            //startActivity(Intent(this, MindAuditFromActivity::class.java))
            val intent = Intent(
                this,
                MASuggestedAssessmentActivity::class.java
            )
            intent.putExtra("SelectedAssessment", selectedAssessment)
            startActivity(intent)
        }

        if (sharedPreferenceManager.userEmotions != null) {
            var userEmotions = sharedPreferenceManager.userEmotions
            getSuggestedAssessment(userEmotions)
        } else {
            var userEmotions = UserEmotions(userEmotionsString)
            getSuggestedAssessment(userEmotions)
        }

        val assessmentHeader = intent.getStringExtra("Assessment") ?: "CAS"

        if (reportId != null) {
            getAssessmentResultWithId(assessmentHeader, reportId!!)
        } else {
            getAssessmentResult(assessmentHeader)
        }
        binding.tvAssessmentTaken.text = assessmentHeader + " " + "Score"
        selectedAssessment = assessmentHeader
        binding.rvSuggestedAssessment.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )


        suggestedAssessmentAdapter = OtherAssessmentsAdapter(
            this, suggestedAssessmentString
        ) { header: String? -> this.showDisclaimerDialog(header) }
        binding.rvSuggestedAssessment.setAdapter(suggestedAssessmentAdapter)
        //binding.rvSuggestedAssessment.scrollToPosition(0)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        isFrom = intent.getStringExtra("FROM")
        val assessmentHeader = intent.getStringExtra("Assessment") ?: "CAS"
        binding.tvAssessmentTaken.text = assessmentHeader + " " + "Score"
        selectedAssessment = assessmentHeader

        if (sharedPreferenceManager.userEmotions != null) {
            val userEmotions = sharedPreferenceManager.userEmotions
            getSuggestedAssessment(userEmotions)
        } else {
            val userEmotions = UserEmotions(userEmotionsString)
            getSuggestedAssessment(userEmotions)
        }

        getAssessmentResult(assessmentHeader)

        binding.llOtherSection.visibility = View.GONE
        binding.scrollviewResult.visibility = View.VISIBLE
    }

    private fun onBackPressHandle() {
        finish()
        if (isFrom != null && isFrom?.isNotEmpty() == true) {
            val intent = Intent(this, HomeNewActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra("finish_MindAudit", true)
            startActivity(intent)
        }
    }

    private fun showDisclaimerDialog(header: String?) {
        val intent = Intent(
            this,
            MASuggestedAssessmentActivity::class.java
        )
        intent.putExtra("SelectedAssessment", header)
        startActivity(intent)
    }


    private fun getAssessmentResult(assessment: String) {
        val call =
            apiService.getMindAuditAssessmentResult(sharedPreferenceManager.accessToken, assessment)
        call.enqueue(object : Callback<MindAuditResultResponse?> {
            override fun onResponse(
                call: Call<MindAuditResultResponse?>,
                response: Response<MindAuditResultResponse?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    /*      try {
                              if (response.body()!!.result.isNullOrEmpty()){

                                  binding.rlAssessmentNotTaken.visibility = View.GONE
                                  binding.scrollviewResult.visibility = View.VISIBLE
                              // binding.tvMainScore.text = response.body()!!.result[0].assessmentsTaken[0].interpretations.anger.level.toString() + " " + response.body()!!.result[0].assessmentsTaken[0].interpretations.anger.score.toString()
                                  handleAssessmentScore(response)
                              }else{
                                  binding.rlAssessmentNotTaken.visibility = View.VISIBLE
                                  binding.scrollviewResult.visibility = View.GONE
                              }
                          } catch (e: Exception) {
                              e.printStackTrace()
                          }*/

                    try {
                        val resultList = response.body()?.result

                        if (!resultList.isNullOrEmpty()) {
                            // Result exists, show result layout
                            binding.rlAssessmentNotTaken.visibility = View.GONE
                            binding.scrollviewResult.visibility = View.VISIBLE

                            handleAssessmentScore(response)
                        } else {
                            // No result, show "not taken" layout
                            binding.rlAssessmentNotTaken.visibility = View.VISIBLE
                            binding.scrollviewResult.visibility = View.GONE
                            setDialogText(
                                binding.tvAssessmentNotTakenMsg,
                                binding.tvAssessmentNotTaken,
                                assessment
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        this@MindAuditResultActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MindAuditResultResponse?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun getAssessmentResultWithId(assessment: String, reportId: String) {
        val call =
            apiService.getMindAuditAssessmentResultWithId(
                sharedPreferenceManager.accessToken,
                assessment,
                reportId
            )
        call.enqueue(object : Callback<MindAuditResultResponse?> {
            override fun onResponse(
                call: Call<MindAuditResultResponse?>,
                response: Response<MindAuditResultResponse?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    /*      try {
                              if (response.body()!!.result.isNullOrEmpty()){

                                  binding.rlAssessmentNotTaken.visibility = View.GONE
                                  binding.scrollviewResult.visibility = View.VISIBLE
                              // binding.tvMainScore.text = response.body()!!.result[0].assessmentsTaken[0].interpretations.anger.level.toString() + " " + response.body()!!.result[0].assessmentsTaken[0].interpretations.anger.score.toString()
                                  handleAssessmentScore(response)
                              }else{
                                  binding.rlAssessmentNotTaken.visibility = View.VISIBLE
                                  binding.scrollviewResult.visibility = View.GONE
                              }
                          } catch (e: Exception) {
                              e.printStackTrace()
                          }*/

                    try {
                        val resultList = response.body()?.result

                        if (!resultList.isNullOrEmpty()) {
                            // Result exists, show result layout
                            binding.rlAssessmentNotTaken.visibility = View.GONE
                            binding.scrollviewResult.visibility = View.VISIBLE

                            handleAssessmentScore(response)
                        } else {
                            // No result, show "not taken" layout
                            binding.rlAssessmentNotTaken.visibility = View.VISIBLE
                            binding.scrollviewResult.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        this@MindAuditResultActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MindAuditResultResponse?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun handleAssessmentScore(response: Response<MindAuditResultResponse?>) {
        val assessmentTaken = response.body()!!.result[0].assessmentsTaken[0]
        sharedPreferenceManager.saveUserEmotions(UserEmotions(response.body()!!.result[0].emotionalState))
        with(binding) {
            when (assessmentTaken.assessment) {
                "DASS-21" -> {
                    assessmentTaken.interpretations.anxiety?.let {
                        cardviewMainscore.visibility = View.VISIBLE
                        binding.mainScoreTitle.text = "Anxiety"
                        binding.mainScoreTitle.visibility = View.VISIBLE
                        binding.mainScoreLevel.text = assessmentTaken.interpretations.anxiety.level
                        binding.mainScoreLevel.visibility = View.VISIBLE
                        binding.tvMainScore.text =
                            assessmentTaken.interpretations.anxiety.score.toString()
                        binding.cardviewMainscore.setCardBackgroundColor(
                            getResources().getColor(
                                getColorResForScore(assessmentTaken.interpretations.anxiety.score)
                            )
                        )
                        setExplanationTitle(
                            assessmentTaken.interpretations.anxiety.level,
                            "Anxiety"
                        )
                        setRainbowView(assessmentTaken.interpretations.anxiety.score.toInt())
                        //setLeftRainbowView(assessmentTaken.interpretations.anxiety.score.toInt())

                        val explanation = getDASS21AnxietyExplanation(
                            assessmentTaken.interpretations.anxiety.score.toFloat()
                        )
                        binding.tvResultExplanationTitle.text = explanation.first
                        binding.tvResultExplanation.text = explanation.second
                    }
                    assessmentTaken.interpretations.depression?.let {
                        cardviewMainscore2.visibility = View.VISIBLE
                        binding.mainScoreTitle2.text = "Depression"
                        binding.mainScoreTitle2.visibility = View.VISIBLE
                        binding.mainScoreLevel2.text =
                            assessmentTaken.interpretations.depression.level
                        binding.mainScoreLevel2.visibility = View.VISIBLE
                        binding.tvMainScore2.text =
                            assessmentTaken.interpretations.depression.score.toString()
                        setExplanationTitle(
                            assessmentTaken.interpretations.depression.level,
                            "Depression"
                        )
                        binding.cardviewMainscore2.setCardBackgroundColor(
                            getResources().getColor(
                                getColorResForScore(assessmentTaken.interpretations.depression.score)
                            )
                        )
                        //setCenterRainbowView(assessmentTaken.interpretations.depression.score.toInt())
                        val explanation = getDASS21DepressionExplanation(
                            assessmentTaken.interpretations.depression.score.toFloat()
                        )
                        binding.tvResultExplanationTitle.text = explanation.first
                        binding.tvResultExplanation.text = explanation.second
                    }
                    assessmentTaken.interpretations.stress?.let {
                        cardviewMainscore3.visibility = View.VISIBLE
                        binding.mainScoreTitle3.text = "Stress"
                        binding.mainScoreTitle3.visibility = View.VISIBLE
                        binding.mainScoreLevel3.text = assessmentTaken.interpretations.stress.level
                        binding.mainScoreLevel3.visibility = View.VISIBLE
                        binding.tvMainScore3.text =
                            assessmentTaken.interpretations.stress.score.toString()
                        binding.cardviewMainscore3.setCardBackgroundColor(
                            getResources().getColor(
                                getColorResForScore(assessmentTaken.interpretations.stress.score)
                            )
                        )
                        setExplanationTitle(assessmentTaken.interpretations.stress.level, "Stress")
                        //setRightRainbowView(assessmentTaken.interpretations.stress.score.toInt())

                        val explanation = getDASS21StressExplanation(
                            assessmentTaken.interpretations.stress.score.toFloat()
                        )
                        binding.tvResultExplanationTitle.text = explanation.first
                        binding.tvResultExplanation.text = explanation.second

                    }
                    scoreBarContainer.visibility = View.VISIBLE
                    scoreBarContainerhappiness.visibility = View.GONE
                }

                "PHQ-9" -> {
                    assessmentTaken.interpretations.depression?.let {
                        binding.mainScoreTitle.text = "Depression"
                        binding.mainScoreTitle.visibility = View.VISIBLE
                        binding.mainScoreLevel.text =
                            assessmentTaken.interpretations.depression.level
                        binding.mainScoreLevel.visibility = View.VISIBLE
                        binding.tvMainScore.text =
                            assessmentTaken.interpretations.depression.score.toString()
                        setRainbowView(assessmentTaken.interpretations.depression.score.toInt())
                        binding.cardviewMainscore.setCardBackgroundColor(
                            getResources().getColor(
                                getColorResForScore(assessmentTaken.interpretations.depression.score)
                            )
                        )
                        setExplanationTitle(
                            assessmentTaken.interpretations.depression.level,
                            "Depression"
                        )
                        val explanation = getPHQ9Explanation(assessmentTaken.interpretations.depression.score.toFloat())
                        binding.tvResultExplanationTitle.text = explanation.first
                        binding.tvResultExplanation.text = explanation.second

                    }
                    cardviewMainscore3.visibility = View.GONE
                    cardviewMainscore2.visibility = View.GONE
                    scoreBarContainer.visibility = View.VISIBLE
                    scoreBarContainerhappiness.visibility = View.GONE
                }

                "OHQ" -> {
                    assessmentTaken.interpretations.happiness?.let {
                        binding.mainScoreTitle.text = "Happiness"
                        binding.mainScoreTitle.visibility = View.VISIBLE
                        binding.mainScoreLevel.text =
                            assessmentTaken.interpretations.happiness.level
                        binding.mainScoreLevel.visibility = View.VISIBLE
                        binding.tvMainScore.text =
                            assessmentTaken.interpretations.happiness.score.toString()
                        setHappinessRainbowView(assessmentTaken.interpretations.happiness.score.toFloat())
                        binding.cardviewMainscore.setCardBackgroundColor(
                            getResources().getColor(
                                getColorForHappinessScore(assessmentTaken.interpretations.happiness.score.toFloat())
                            )
                        )
                        setExplanationTitle(
                            assessmentTaken.interpretations.happiness.level,
                            "Happiness"
                        )
                        val explanation = getHappinessExplanation(assessmentTaken.interpretations.happiness.score.toFloat())
                        binding.tvResultExplanationTitle.text = explanation.first
                        binding.tvResultExplanation.text = explanation.second
                    }
                    cardviewMainscore3.visibility = View.GONE
                    cardviewMainscore2.visibility = View.GONE
                    scoreBarContainer.visibility = View.GONE
                    scoreBarContainerhappiness.visibility = View.VISIBLE
                }

                "GAD-7" -> {
                    assessmentTaken.interpretations.anxiety?.let {
                        binding.mainScoreTitle.text = "Anxiety"
                        binding.mainScoreTitle.visibility = View.VISIBLE
                        binding.mainScoreLevel.text = assessmentTaken.interpretations.anxiety.level
                        binding.mainScoreLevel.visibility = View.VISIBLE
                        binding.tvMainScore.text =
                            assessmentTaken.interpretations.anxiety.score.toString()
                        setRainbowView(assessmentTaken.interpretations.anxiety.score)
                        binding.cardviewMainscore.setCardBackgroundColor(
                            getResources().getColor(
                                getColorResForScore(assessmentTaken.interpretations.anxiety.score)
                            )
                        )
                        setExplanationTitle(
                            assessmentTaken.interpretations.anxiety.level,
                            "Anxiety"
                        )

                        val explanation = getGAD7Explanation(assessmentTaken.interpretations.anxiety.score.toFloat())
                        binding.tvResultExplanationTitle.text = explanation.first
                        binding.tvResultExplanation.text = explanation.second
                    }
                    cardviewMainscore3.visibility = View.GONE
                    cardviewMainscore2.visibility = View.GONE
                    scoreBarContainer.visibility = View.VISIBLE
                    scoreBarContainerhappiness.visibility = View.GONE
                }

                "CAS" -> {
                    assessmentTaken.interpretations.anger?.let {
                        binding.mainScoreTitle.text = "Anger"
                        binding.mainScoreTitle.visibility = View.VISIBLE
                        binding.mainScoreLevel.text = assessmentTaken.interpretations.anger.level
                        binding.mainScoreLevel.visibility = View.VISIBLE
                        binding.tvMainScore.text =
                            assessmentTaken.interpretations.anger.score.toString()
                        setRainbowView(assessmentTaken.interpretations.anger.score)
                        binding.cardviewMainscore.setCardBackgroundColor(
                            getResources().getColor(
                                getColorResForScore(assessmentTaken.interpretations.anger.score)
                            )
                        )
                        setExplanationTitle(assessmentTaken.interpretations.anger.level, "Anger")
                    }
                    val explanation = getCASExplanation(assessmentTaken.interpretations.anger.score.toFloat())
                    binding.tvResultExplanationTitle.text = explanation.first
                    binding.tvResultExplanation.text = explanation.second

                    cardviewMainscore3.visibility = View.GONE
                    cardviewMainscore2.visibility = View.GONE
                    scoreBarContainer.visibility = View.VISIBLE
                    scoreBarContainerhappiness.visibility = View.GONE
                }

                else -> {}
            }
        }


    }

    private fun setLeftRainbowView(score: Int) {
        binding.leftArc.visibility = View.VISIBLE
        binding.leftArc.setRainbowColors(getColorArrayForScore(score))
        binding.leftArc.setStrokeWidth(30f)
        binding.leftArc.setArcSpacing(8f)
        binding.leftArc.startAngle = 180f
        binding.leftArc.sweepAngle = 90f
        binding.leftArc.invalidate()
    }

    private fun setRightRainbowView(score: Int) {
        binding.rightArc.visibility = View.VISIBLE
        binding.rightArc.setRainbowColors(getColorArrayForScore(score))
        binding.rightArc.setStrokeWidth(30f)
        binding.rightArc.setArcSpacing(8f)
        binding.rightArc.startAngle = 270f
        binding.rightArc.sweepAngle = 90f
    }

    private fun setCenterRainbowView(score: Int) {
        binding.centerArc.visibility = View.VISIBLE
        binding.centerArc.setRainbowColors(getColorArrayForScore(score))
        binding.centerArc.setStrokeWidth(30f)
        binding.centerArc.setArcSpacing(8f)
        binding.centerArc.startAngle = 180f
        binding.centerArc.sweepAngle = 180f
    }

    private fun setRainbowView(score: Int) {
        binding.rainbowView.visibility = View.VISIBLE
        binding.rainbowView.setRainbowColors(getColorArrayForScore(score))
        binding.rainbowView.setStrokeWidth(60f)
        binding.rainbowView.setArcSpacing(8f)
    }

    private fun setHappinessRainbowView(score: Float) {
        binding.rainbowView.visibility = View.VISIBLE
        binding.rainbowView.setRainbowColors(getColorArrayForHappinessScore(score))
        binding.rainbowView.setStrokeWidth(40f)
        binding.rainbowView.setArcSpacing(8f)
    }

    private fun setExplanationTitle(level: String?, title: String) {
        binding.tvResultExplanationTitle.text = level + " " + title
    }

    //get all assesments
    private fun getSuggestedAssessment(userEmotions: UserEmotions) {
        val call =
            apiService.getSuggestedAssessment(sharedPreferenceManager.accessToken, userEmotions)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    //Toast.makeText(requireContext(), "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        val jsonString = response.body()!!.string()
                        val gson = Gson()
                        val assessments = gson.fromJson(
                            jsonString,
                            Assessments::class.java
                        )
                        handleAssessmentResult(assessments)

                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                } else {
                    Toast.makeText(
                        this@MindAuditResultActivity,
                        "Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun handleAssessmentResult(assessments: Assessments?) {
        allAssessments.clear()
        suggestedAssessments.clear()
        binding.chipGroup1.removeAllViews()
        val assessment = assessments!!.allAssessment

        if (assessment!!.dass21 != null) {
            allAssessments.add(assessment.dass21)
        } else {
            suggestedAssessments.add("DASS-21")
        }
        // need to add this after sleep audit functionality is added
        /*if (assessment.sleepAudit != null) {
            allAssessments.add(assessment.sleepAudit)
        } else {
            suggestedAssessments.add("Sleep Audit")
        }*/

        if (assessment.gad7 != null) {
            allAssessments.add(assessment.gad7)
        } else {
            suggestedAssessments.add("GAD-7")
        }
        if (assessment.ohq != null) {
            allAssessments.add(assessment.ohq)
        } else {
            suggestedAssessments.add("OHQ")
        }

        if (assessment.cas != null) {
            allAssessments.add(assessment.cas)
        } else {
            suggestedAssessments.add("CAS")
        }

        if (assessment.phq9 != null) {
            allAssessments.add(assessment.phq9)
        } else {
            suggestedAssessments.add("PHQ-9")
        }

        for (assessment in assessments.savedAssessment) {
            if (assessment.isCompleted && !suggestedAssessments.contains(assessment.assessmentType)) suggestedAssessments.add(
                assessment.assessmentType
            )
            else
                allAssessments.remove(assessment.assessmentType)
        }

        for (assessment in suggestedAssessments) {
            addChip(assessment, assessment == selectedAssessment)
        }
        if (allAssessments.isNotEmpty()) {
            addChip("Other")
        }

        suggestedAssessmentString = allAssessments
        suggestedAssessmentAdapter = OtherAssessmentsAdapter(
            this, allAssessments
        ) { header: String? -> this.showDisclaimerDialog(header) }
        binding.rvSuggestedAssessment.setAdapter(suggestedAssessmentAdapter)

        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewEmotions.setLayoutManager(gridLayoutManager)

        var emotionsList = ArrayList<Emotions>()
        if (sharedPreferenceManager.userEmotions != null) {
            for (s in sharedPreferenceManager.userEmotions.emotions) {
                emotionsList.add(Emotions(Utils.toTitleCase(s), false))
            }
            emotionsAdapter = EmotionsAdapter(
                this, emotionsList, "2"
            ) {
            }
        }
        binding.recyclerViewEmotions.setAdapter(emotionsAdapter)
    }

    private fun addChip(
        name: String,
        isSelected: Boolean = false
    ) {
        val chip = Chip(this)
        chip.id = View.generateViewId() // Generate unique ID
        chip.text = name
        chip.isCheckable = true
        chip.isChecked = false
        chip.chipCornerRadius = 50f
        chip.chipStrokeColor = ContextCompat.getColorStateList(
            this,
            R.color.color_think_right
        )

        chip.textSize = 13f

        val heightInDp = 50 // or whatever height you want
        val heightInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            heightInDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            heightInPx
        )

        chip.layoutParams = layoutParams

        chip.isChecked = isSelected

        // Set different colors for selected state
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.color_think_right),
                ContextCompat.getColor(this, R.color.white)
            )
        )

        val textColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.white),
                ContextCompat.getColor(this, R.color.color_think_right)
            )
        )
        chip.chipBackgroundColor = colorStateList


        chip.setOnClickListener { view ->
            val position = binding.chipGroup1.indexOfChild(view)
            val selectedChip = binding.chipGroup1.getChildAt(position) as Chip
            Log.d("selected chip", " --" + selectedChip.text.toString())
            if (selectedChip.text.toString().equals("Sleep Audit")) {

            } else {
                if (selectedChip.text.toString().equals("Other")) {
                    binding.llOtherSection.visibility = View.VISIBLE
                    binding.scrollviewResult.visibility = View.GONE
                    binding.rlAssessmentNotTaken.visibility = View.GONE
                } else {
                    binding.llOtherSection.visibility = View.GONE
                    binding.scrollviewResult.visibility = View.VISIBLE
                    getAssessmentResult(selectedChip.text.toString())
                    binding.tvAssessmentTaken.text = selectedChip.text.toString() + " " + "Score"
                    selectedAssessment = selectedChip.text.toString()
                }

            }
        }

        // Text color for selected state
        chip.setTextColor(textColorStateList)
        if (isSelected)
            binding.chipGroup1.addView(chip, 0)
        else
            binding.chipGroup1.addView(chip)
    }

    private fun getColorResForScore(score: Int): Int {
        return when (score) {
            in 0..3 -> R.color.green_minimal
            in 4..8 -> R.color.cyan_mild
            in 9..13 -> R.color.blue_moderate
            in 14..18 -> R.color.orange_severe
            else -> R.color.red_ext_severe
        }
    }

    fun getColorForHappinessScore(score: Float): Int {
        return when (score) {
            in 0f..2f -> R.color.pastel_lavender    // pastel lavender
            in 2f..3f -> R.color.soft_lilac    // soft lilac
            in 3f..4f -> R.color.light_sky_blue    // light sky blue
            in 4f..5f -> R.color.mint_green    // mint green
            in 5f..6f -> R.color.soft_peach    // soft peach
            in 6f..7f -> R.color.light_pink_lavender    // light pink-lavender
            else -> R.color.soft_beige    // soft beige
        }
    }

    fun getColorArrayForScore(score: Int): IntArray {
        val colorLevels = listOf(
            0xFF06B27B.toInt(), // green_minimal
            0xFF54C8DB.toInt(), // cyan_mild
            0xFF57A3FC.toInt(), // blue_moderate
            0xFFFFBD44.toInt(), // orange_severe
            0xFFFC6656.toInt()  // red_ext_severe
        )

        val fallbackColor = 0xFFEFF0F6.toInt() // light gray for unfilled slots

        val activeColorCount = when (score) {
            in 0..3 -> 1
            in 4..8 -> 2
            in 9..13 -> 3
            in 14..18 -> 4
            else -> 5
        }

        return IntArray(5) { index ->
            if (index < activeColorCount) colorLevels[index] else fallbackColor
        }
    }

    fun getColorArrayForHappinessScore(score: Float): IntArray {
        /*val colorLevels = listOf(
            0xFF06B27B.toInt(), // green_minimal
            0xFF54C8DB.toInt(), // cyan_mild
            0xFF57A3FC.toInt(), // blue_moderate
            0xFFFFBD44.toInt(), // orange_severe
            0xFFFC6656.toInt()  // red_ext_severe
        )*/
        //#F7E6B7,#FDE6FF,#FDD3D2,#A6E0CE,#C5DEF9,#D5C6ED,#D9DBE9
        val colorLevels = listOf(
            0xFFD9DBE9.toInt(), // pastel lavender
            0xFFD5C6ED.toInt(), // soft lilac
            0xFFC5DEF9.toInt(), // light sky blue
            0xFFA6E0CE.toInt(), // mint green
            0xFFFDD3D2.toInt(), // soft peach
            0xFFFDE6FF.toInt(), // light pink-lavender
            0xFFF7E6B7.toInt()  // soft beige
        )


        val fallbackColor = 0xFFEFF0F6.toInt() // light gray for unfilled slots

        val activeColorCount = when (score) {
            in 0f..2f -> 1
            in 2f..3f -> 2
            in 3f..4f -> 3
            in 4f..5f -> 4
            in 5f..6f -> 5
            in 6f..7f -> 6
            else -> 7
        }

        return IntArray(7) { index ->
            if (index < activeColorCount) colorLevels[index] else fallbackColor
        }
    }

    // not taken assessment texts
    private fun setDialogText(tvItem1: TextView, tvItem2: TextView, header: String) {
        when (header) {
            "DASS-21" -> {
                tvItem1.text =
                    AppConstants.dass21FirstPara + " " + AppConstants.dass21SecondPara + " " + AppConstants.dass21ThirdPara
                tvItem2.text = "DASS-21"
            }

            "Sleep Audit" -> {
                tvItem1.text =
                    AppConstants.ssFirstPara + " " + AppConstants.ssSecondPara + " " + AppConstants.ssThirdPara
                tvItem2.text = header
            }

            "GAD-7" -> {
                tvItem1.text =
                    AppConstants.gad7FirstPara + " " + AppConstants.gad7SecondPara + " " + AppConstants.gad7ThirdPara
                tvItem2.text = header
            }

            "OHQ" -> {
                tvItem1.text =
                    AppConstants.ohqFirstPara + " " + AppConstants.ohqSecondPara + " " + AppConstants.ohqThirdPara
                tvItem2.text = header
            }

            "CAS" -> {
                tvItem1.text =
                    AppConstants.casFirstPara + " " + AppConstants.casSecondPara + " " + AppConstants.casThirdPara
                tvItem2.text = header
            }

            "PHQ-9" -> {
                tvItem1.text =
                    AppConstants.phq9FirstPara + " " + AppConstants.phq9SecondPara + " " + AppConstants.phq9ThirdPara
                tvItem2.text = header
            }

        }
    }



    private fun getHappinessExplanation(score: Float): Pair<String, String> {
        return when {
            score in 1f..1.9999f -> "Weathering the storm, seeking sunnier days." to
                    "In moments of struggle, it’s common to experience a lack of happiness and perceive our situations more negatively than they truly are. Just as storm clouds eventually give way to clearer skies, remember that your current outlook can shift toward a sunnier perspective."

            score in 2f..2.999999f -> "Navigating the happiness spectrum, one step at a time." to
                    "While you might feel somewhat unhappy now, emotions shift like changing landscapes. Just a step on your journey, leading to brighter moments ahead."

            score in 3f..3.9999f -> "Finding equilibrium on the happiness scale." to
                    "At this juncture, you find yourself in a state of equilibrium—neither swaying towards happiness nor tipping into unhappiness. This neutral phase signifies a pause in the emotional spectrum, allowing you to find your footing before the journey continues."

            score == 4f -> "Cruising the highway of contentment." to
                    "Picture yourself cruising down the highway of contentment, where your emotions land somewhere between moderate happiness and mild satisfaction. This range often represents the score of an average individual, reflecting a state of balance and ease."

            score in 4.00001f..4.9999f -> "On the brink of brighter smiles." to
                    "You seem to be on the edge of radiant smiles, feeling quite content and happy."

            score in 5f..5.9999f -> "Catching happiness rays, making life golden." to
                    "Beyond its immediate pleasantness, happiness brings a cascade of benefits. It intertwines with better health, stronger relationships, and realizing aspirations. A foundation of happiness serves as a launchpad, enabling you to expand and construct a path towards even greater accomplishments."

            score >= 6f -> "Balancing joy like a happiness pro." to
                    "Happiness is the delicate equilibrium between holding on and letting go, and at this moment, you’ve mastered it with finesse. Your happiness overflows, filling your cup to the brim, and spilling on to others around you."

            else -> "No Data" to "Score not within defined range."
        }
    }

    private fun getPHQ9Explanation(score: Float): Pair<String, String> {
        return when (score) {
            in 1f..4f -> "Minimal Depression" to
                    "You’re showing signs of minimal depression. It’s like having a small cloud in a clear sky. With the right strategies, brighter days are certainly ahead. Let’s take the first step together."

            in 5f..9f -> "Mild Depression" to
                    "Your score indicates mild depression. With consistent care and small daily actions, we can lift the fog and find the sunshine."

            in 10f..14f -> "Moderate Depression" to
                    "Your results suggest moderate depression. Remember, every step forward is progress, and we’re here to share the load and walk with you towards lighter days."

            in 15f..19f -> "Moderately Severe Depression" to
                    "You’re experiencing moderately severe depression. Storms don’t last forever, and with support and guidance, we can navigate to calmer waters together."

            in 20f..27f -> "Severe Depression" to
                    "Your score is indicative of severe depression. But even the longest nights end, and we’re here to help you await the dawn. Step by step, let’s welcome a new day."

            else -> "No Data" to "Score not within defined range."
        }
    }
    private fun getGAD7Explanation(score: Float): Pair<String, String> {
        return when (score) {
            in 0f..4f -> "Minimal Anxiety" to
                    "Minimal anxiety’s on the radar—it’s life whispering for a bit of TLC. Some deep breaths and a relaxing playlist could be just what you need."

            in 5f..9f -> "Mild Anxiety" to
                    "Mild anxiety is like a nudge from your body saying, ‘Hey, let’s slow down a sec.’ A short walk or a quick meditation session might be the perfect response."

            in 10f..14f -> "Moderate Anxiety" to
                    "Moderate anxiety can feel like too many tabs open in your brain. It might help to ‘close’ a few with a soothing hobby or by chatting with a friend."

            in 15f..21f -> "Severe Anxiety" to
                    "Severe anxiety can be quite the handful, but it’s not the boss of you. Support is key—maybe a chat with a pro or joining a yoga class. You’re not alone on this journey."

            else -> "No Data" to "Score not within defined GAD-7 range."
        }
    }
    private fun getCASExplanation(score: Float): Pair<String, String> {
        return when (score) {
            in 0f..13f -> "Minimal Clinical Anger" to
                    "Your anger levels are pretty minimal. Keeping a cool head is your superpower. Keep using those chill vibes to navigate life's ups and downs."

            in 14f..19f -> "Mild Clinical Anger" to
                    "You're experiencing mild anger — manageable with some mindful cooling. Perhaps some deep breaths or a walk could be your go-to."

            in 20f..28f -> "Moderate Clinical Anger" to
                    "Your score shows moderate anger. It might feel like a storm brewing, but finding calm strategies, like talking it out or hitting pause on heated moments, can help."

            in 29f..63f -> "Severe Clinical Anger" to
                    "Looks like anger is weighing heavily on you. It's important, and okay, to seek support—whether it's through therapy, anger management techniques, or mindfulness practices. You've got the strength to work through this."

            else -> "No Data" to "Score not within defined CAS range."
        }
    }

    private fun getDASS21DepressionExplanation(score: Float): Pair<String, String> {
        return when (score) {
            in 0f..9f -> "Normal" to
                    "Your score’s in the normal range, and that’s great news! Still, remember to keep a check on your well-being. A little self-care can ensure you stay on this track."

            in 10f..13f -> "Mild" to
                    "You’re experiencing mild depression. It’s a signal to take extra care of yourself. Small acts of self-care and talking about your feelings can make a big difference."

            in 14f..20f -> "Moderate" to
                    "Seems you’re in a bit of a fog. Let’s clear it up with some support, be it a heart-to-heart, a hobby, or a helping hand from a pro."

            in 21f..27f -> "Severe" to
                    "Things are feeling pretty heavy, huh? It’s okay to lean on others. A chat with a professional can be a lifeline. Remember, it’s brave to ask for help."

            in 28f..100f -> "Extremely Severe" to
                    "Things may seem tough right now, but reaching out to a professional is a strong move. Together, you can start turning things around. You’re not on this path alone."

            else -> "No Data" to "Score not within defined DASS-21 depression range."
        }
    }

    private fun getDASS21AnxietyExplanation(score: Float): Pair<String, String> {
        return when (score) {
            in 0f..9f -> "Normal" to
                    "You're in the normal range for anxiety, which is fantastic! Still, life throws curveballs, so keeping those relaxation techniques handy is always a good idea."

            in 10f..13f -> "Mild" to
                    "Experiencing mild anxiety? It's a gentle nudge that it’s time to breathe and maybe stretch out those worries. Small daily mindfulness practices can work wonders."

            in 14f..20f -> "Moderate" to
                    "Moderate anxiety can feel like carrying an extra backpack of worries. Remember, it’s okay to put it down through activities like journaling, walks, or seeking support."

            in 21f..27f -> "Severe" to
                    "Your score indicates severe anxiety. It’s a heavy load but sharing it can lighten it. Professional guidance and peer support are valuable tools on your path to calm."

            in 28f..100f -> "Extremely Severe" to
                    "Extremely severe anxiety is tough, but so are you. It’s crucial to reach out for professional help now. There’s a community ready to stand by you as you take steps towards healing."

            else -> "No Data" to "Score not within defined DASS-21 anxiety range."
        }
    }


    private fun getDASS21StressExplanation(score: Float): Pair<String, String> {
        return when (score) {
            in 0f..9f -> "Normal" to
                    "Your stress levels are looking normal—a sign of smooth sailing. Keep those stress-busting activities close to maintain this peaceful vibe."

            in 10f..13f -> "Mild" to
                    "A hint of mild stress detected. It might be life's way of saying, 'Let's find more moments to unwind.' A quick walk or a fun hobby could be your perfect antidote."

            in 14f..20f -> "Moderate" to
                    "Dealing with moderate stress? It can feel like juggling more balls than you have hands. Time to set some down and focus on your breath. Why not try a meditation on our app or a relaxing playlist?"

            in 21f..27f -> "Severe" to
                    "Severe stress is no small feat to manage. But remember, asking for help is a strength, not a weakness. This could be a good time to explore stress management techniques or talk to someone who can help."

            in 28f..100f -> "Extremely Severe" to
                    "Facing extremely severe stress is challenging, but you're not alone. Professional support can offer new strategies to navigate through this storm. Together, let’s find your way back to a less stressful life."

            else -> "No Data" to "Score not within defined DASS-21 stress range."
        }
    }


}