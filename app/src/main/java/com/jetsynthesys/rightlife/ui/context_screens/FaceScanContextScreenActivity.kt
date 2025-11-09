package com.jetsynthesys.rightlife.ui.context_screens

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityFacescanContextScreenBinding
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamActivity
import com.jetsynthesys.rightlife.ui.healthcam.basicdetails.HealthCamBasicDetailsNewActivity
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class FaceScanContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityFacescanContextScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacescanContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        Glide.with(this)
            .asGif()
            .load(R.drawable.face_scan_context_screen) // or URL: "https://..."
            .into(binding.gifImageView)

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNext.setOnClickListener {
            /*startActivity(Intent(this, HealthCamActivity::class.java))
            finish()*/
            binding.btnNext.disableViewForSeconds()
            showDisclaimerDialog()
        }
    }
    private fun showDisclaimerDialog()
    {
        // Create the dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_disclaimer_health_cam)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window!!.attributes
        layoutParams.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window!!.attributes = layoutParams

        // Find views from the dialog layout
        //ImageView dialogIcon = dialog.findViewById(R.id.img_close_dialog);
        val dialogImage = dialog.findViewById<ImageView>(R.id.dialog_image)
        val dialogText = dialog.findViewById<TextView>(R.id.dialog_text)
        val dialogButtonStay = dialog.findViewById<Button>(R.id.dialog_button_stay)
        val dialogButtonExit = dialog.findViewById<Button>(R.id.dialog_button_exit)

        // Optional: Set dynamic content
        // dialogText.setText("Please find a quiet and comfortable place before starting");

        // Set button click listener
        dialogButtonStay.setOnClickListener { v: View? ->
            // Perform your action
            dialog.dismiss()
            //Toast.makeText(VoiceScanActivity.this, "Scan feature is Coming Soon", Toast.LENGTH_SHORT).show();
            val intent = Intent(this@FaceScanContextScreenActivity, HealthCamBasicDetailsNewActivity::class.java)
            startActivity(intent)
            finish()
        }
        dialogButtonExit.setOnClickListener { v: View? ->
            dialog.dismiss()
            this.finish()
        }

        // Show the dialog
        dialog.show()
    }
}