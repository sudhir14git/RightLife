package com.jetsynthesys.rightlife.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityExportHealthDataBinding

class ExportHealthDataActivity : BaseActivity() {

    private lateinit var binding: ActivityExportHealthDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportHealthDataBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnExport.setOnClickListener {
            startActivity(Intent(this, PreparingExportHealthDataActivity::class.java))
            //finish()
        }
    }
}
