package com.jetsynthesys.rightlife.ui.profile_new

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityDeleteAccountReasonBinding

class DeleteAccountReasonActivity : BaseActivity() {
    private lateinit var binding: ActivityDeleteAccountReasonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountReasonBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        val reasonsList = intent.getStringExtra("SelectedReasons")

        // --- Character counter setup ---
        val maxLen = 1000
        binding.etMessage.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val len = s?.length ?: 0
                binding.tvCharCount.text = "$len/$maxLen Ch"
                if (len > maxLen) {
                    // Trim to max length immediately
                    binding.etMessage.setText(s?.substring(0, maxLen))
                    binding.etMessage.setSelection(maxLen)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        // initialize once
        binding.tvCharCount.text = "0/$maxLen Ch"

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCancel.setOnClickListener {
            finish()
            startActivity(
                Intent(
                    this@DeleteAccountReasonActivity,
                    ProfileSettingsActivity::class.java
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("start_profile", true)
                })
        }

        binding.btnContinue.setOnClickListener {
            /*if (binding.etMessage.text.isEmpty()){
                Toast.makeText(this,"Please enter message",Toast.LENGTH_SHORT).show()
            }else{
                //delete API call pending*/
            startActivity(Intent(this, DeleteAccountEmailDataActivity::class.java).apply {
                putExtra("SelectedReasons", reasonsList)
                putExtra("Message",binding.etMessage.text.toString())
            })
        }
    }
}