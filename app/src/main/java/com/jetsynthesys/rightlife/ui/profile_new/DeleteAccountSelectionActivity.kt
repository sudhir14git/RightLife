package com.jetsynthesys.rightlife.ui.profile_new

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityDeleteAccountSelectionBinding
import com.jetsynthesys.rightlife.ui.profile_new.adapter.DeleteReasonAdapter
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam

class DeleteAccountSelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityDeleteAccountSelectionBinding
    private lateinit var adapter: DeleteReasonAdapter

    private val reasons = listOf(
        "I've achieved my health goals and no longer need the app",
        "I found a different app that better suits my needs",
        "The app is too complicated or difficult to use",
        "Other Reasons"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountSelectionBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        adapter = DeleteReasonAdapter(reasons)
        binding.reasonRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reasonRecyclerView.adapter = adapter

        binding.btnCancel.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnContinue.setOnClickListener {
            val selectedReasons = adapter.getSelectedReason()
            if (selectedReasons?.isEmpty() == true) {
                Toast.makeText(this, "Please select at least one reason.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (selectedReasons.equals("Other Reasons")) {
                    startActivity(Intent(this, DeleteAccountReasonActivity::class.java).apply {
                        putExtra("SelectedReasons", selectedReasons)
                    })
                }else{
                    //Toast.makeText(this, "Selected: $selectedReasons", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DeleteAccountEmailDataActivity::class.java).apply {
                        putExtra("SelectedReasons", selectedReasons)
                    })
                }

                AnalyticsLogger.logEvent(
                        this,
                        AnalyticsEvent.DeleteAccount_ExportData_Continue,
                        mapOf(
                                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                        )
                )
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
