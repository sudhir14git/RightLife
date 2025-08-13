package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityTrsrAssessmentContextScreenBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity

class TRSRAssessmentContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityTrsrAssessmentContextScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrsrAssessmentContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, QuestionnaireThinkRightActivity::class.java))
            finish()
        }
    }
}