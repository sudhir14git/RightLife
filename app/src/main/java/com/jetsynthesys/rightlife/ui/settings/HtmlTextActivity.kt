package com.jetsynthesys.rightlife.ui.settings

import android.os.Bundle
import android.text.Html
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityHtmlTextBinding

class HtmlTextActivity : BaseActivity() {

    private lateinit var binding: ActivityHtmlTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHtmlTextBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        val string = intent.getStringExtra("GeneralInformation")
        val title = intent.getStringExtra("title")

        if (!title.isNullOrEmpty())
            binding.tvHeader.text = title

        if (!string.isNullOrEmpty())
            binding.tvAnswer.text =
                Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT)

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}