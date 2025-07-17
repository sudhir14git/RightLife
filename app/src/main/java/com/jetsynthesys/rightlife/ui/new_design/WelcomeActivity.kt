package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity

class WelcomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_welcome)

        val textView = findViewById<TextView>(R.id.textview1)

        val originalText = "Welcome! \nBegin Your RightLife Transformation."

        val spannable = SpannableString(originalText)

        val start = originalText.indexOf("RightLife")
        val end = start + "RightLife".length

        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.rightlife)),
            start,                         // Start index of "RightLife"
            end,                           // End index of "RightLife"
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
        Handler(mainLooper).postDelayed({
            startActivity(Intent(this, HomeNewActivity::class.java))
            finishAffinity()
        },1000)
    }
}