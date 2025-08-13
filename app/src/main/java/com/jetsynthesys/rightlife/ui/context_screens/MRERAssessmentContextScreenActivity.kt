package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityMrerAssessmentContextScreenBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity

class MRERAssessmentContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityMrerAssessmentContextScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMrerAssessmentContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, QuestionnaireEatRightActivity::class.java))
            finish()
        }
    }
}