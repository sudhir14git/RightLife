package com.jetsynthesys.rightlife.ui.healthcam

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityHealthcamBinding
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.healthcam.basicdetails.HealthCamBasicDetailsNewActivity
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class HealthCamActivity : BaseActivity() {
    var adapter: HealthCamPagerAdapter? = null
    private lateinit var binding: ActivityHealthcamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthcamBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        healthCamResult

        binding.btnHowitworks.isEnabled = false


        binding.indicatorView.setSliderHeight(21f)
        binding.indicatorView.setSliderWidth(80f)
        // Array of layout resources to use in the ViewPager
        val layouts = intArrayOf(
            R.layout.page_one_health_cam,  // Define these layout files in your res/layout directory
        )

        // Set up the adapter
        adapter = HealthCamPagerAdapter(layouts)

        binding.viewPager.adapter = adapter
        binding.indicator.setViewPager(binding.viewPager)
        binding.indicatorView.setupWithViewPager(binding.viewPager)


        binding.icBackDialog.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            adapter?.itemCount
            if (currentItem == 0) {
                finish()
            } else {
                binding.viewPager.currentItem = currentItem - 1
            }
        }

        binding.icCloseDialog.setOnClickListener {
            showExitDialog()
        }

        binding.btnHowitworks.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            val totalItems = binding.viewPager.adapter?.itemCount ?: 0

            if (currentItem < totalItems - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                val desc =
                    "This Face scan is intended to improve your awareness of general wellness. It does not diagnose, treat or mitigate any disease, disorder or abnormal physical state. Please consult with a healthcare professional or emergency services if you believe you have a medical emergency."
                DialogUtils.showCommonBottomSheetDialog(
                    this, description = desc,
                    onOkayClick = {
                        startActivity(
                            Intent(
                                this@HealthCamActivity,
                                HealthCamBasicDetailsNewActivity::class.java
                            )
                        )
                    },
                    onCloseClick = {
                        finish()
                    })
            }
        }

        // Add page change callback to update button text
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonText(position)
            }
        })
    }

    // Method to update button text based on the current page
    @SuppressLint("SetTextI18n")
    private fun updateButtonText(position: Int) {
        val currentItem = binding.viewPager.currentItem
        val totalItems = binding.viewPager.adapter?.itemCount ?: 0

        if (currentItem < totalItems - 1) {
            binding.btnHowitworks.text = "Start Now"
        } else {
            binding.btnHowitworks.text = "Start Now"
        }
    }

    private fun showExitDialog() {
        // Create the dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_exit_dialog_mind)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window?.attributes
        layoutParams?.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window?.attributes = layoutParams

        // Find views from the dialog layout
        //ImageView dialogIcon = dialog.findViewById(R.id.img_close_dialog);
        //val dialogImage = dialog.findViewById<ImageView>(R.id.dialog_image)
        //val dialogText = dialog.findViewById<TextView>(R.id.dialog_text)
        val dialogButtonStay = dialog.findViewById<Button>(R.id.dialog_button_stay)
        val dialogButtonExit = dialog.findViewById<Button>(R.id.dialog_button_exit)

        // Set button click listener
        dialogButtonStay.setOnClickListener {
            // Perform your action
            dialog.dismiss()
        }
        dialogButtonExit.setOnClickListener {
            dialog.dismiss()
            this.finish()
        }

        // Show the dialog
        dialog.show()
    }

    private val healthCamResult: Unit
        get() {
            Utils.showLoader(this)
            val call = apiService.getMyRLHealthCamResult(sharedPreferenceManager.accessToken)

            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                ) {
                    binding.btnHowitworks.isEnabled = true
                    Utils.dismissLoader(this@HealthCamActivity)
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            finish()
                            startActivity(
                                Intent(
                                    this@HealthCamActivity,
                                    NewHealthCamReportActivity::class.java
                                )
                            )
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        }
                    } else {
                        //   Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.d("HealthCamResult", "Error:" + response.message())
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    handleNoInternetView(t)
                    Utils.dismissLoader(this@HealthCamActivity)
                    binding.btnHowitworks.isEnabled = true
                }
            })
        }
}
