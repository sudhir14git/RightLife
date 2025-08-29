package com.jetsynthesys.rightlife.ui.breathwork

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityBreathworkBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetBreathworkContextBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.breathwork.pojo.BreathingData
import com.jetsynthesys.rightlife.ui.breathwork.pojo.GetBreathingResponse
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.format.DateTimeFormatter

class BreathworkActivity : BaseActivity() {

    private lateinit var adapter: BreathworkAdapter
    private val breathWorks = ArrayList<BreathingData>()
    private var isFromTool = false
    private var whereToGo = ""
    private var startDate = ""

    private lateinit var binding: ActivityBreathworkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_breathwork)
        binding = ActivityBreathworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isFromTool = intent.getBooleanExtra("IS_FROM_TOOLS", false)
        whereToGo = intent.getStringExtra("TOOLS_VALUE").toString()
        startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

        getBreathingWork()

        binding.icBackDialog.setOnClickListener {
            callPostMindFullDataAPI()
            finish()
        }

        onBackPressedDispatcher.addCallback(this) {
            callPostMindFullDataAPI()
            finish()
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)

        adapter = BreathworkAdapter(breathWorks, object : BreathworkAdapter.OnItemClickListener {
            override fun onClick(breathingData: BreathingData) {
                val prefName = when (breathingData.title) {
                    "Box Breathing" -> SharedPreferenceConstants.BOX_BREATH_WORK
                    "Alternate Nostril Breathing" -> SharedPreferenceConstants.ALTERNATE_BREATH_WORK
                    "4-7-8 Breathing" -> SharedPreferenceConstants.FOUR_7_8_BREATH_WORK
                    "Custom" -> SharedPreferenceConstants.CUSTOM_BREATH_WORK
                    "Equal Breathing" -> SharedPreferenceConstants.EQUAL_BREATH_WORK
                    else -> SharedPreferenceConstants.CUSTOM_BREATH_WORK
                }
                if (sharedPreferenceManager.getFirstTimeView(prefName)) {
                    sharedPreferenceManager.setFirstTimeView(prefName)
                    showBottomSheet(breathingData)
                } else {
                    startNextActivity(breathingData)
                }
            }

            override fun onAddToolTip(breathingData: BreathingData) {
                CommonAPICall.addToToolKit(
                    this@BreathworkActivity,
                    breathingData.id,
                    !breathingData.isAddedToToolKit
                )
            }

        })

        binding.recyclerView.adapter = adapter
    }

    private fun getBreathingWork() {
        apiService.getBreathingWork(sharedPreferenceManager.accessToken)
            .enqueue(object : Callback<GetBreathingResponse> {
                override fun onResponse(
                    call: Call<GetBreathingResponse>,
                    response: Response<GetBreathingResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.data?.let { breathWorks.addAll(it) }
                        var breathingDataSend: BreathingData? = null
                        if (isFromTool) {
                            breathWorks.forEach { breathingData ->
                                if (breathingData.id.equals(whereToGo)) {
                                    breathingDataSend = breathingData
                                    return@forEach
                                }
                            }
                            if (breathingDataSend != null) {
                                startNextActivity(breathingDataSend!!)
                                finish()
                            } else
                                adapter.notifyDataSetChanged()
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            this@BreathworkActivity,
                            "Server Error: " + response.code(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GetBreathingResponse>, t: Throwable) {
                    handleNoInternetView(t)
                }
            })
    }

    private fun callPostMindFullDataAPI() {
        val endDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        CommonAPICall.postMindFullData(this, "Breathing", startDate, endDate)
    }

    private fun showBottomSheet(breathingData: BreathingData) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogBinding = BottomsheetBreathworkContextBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root
        bottomSheetDialog.setContentView(bottomSheetView)

        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }
        bottomSheetDialog.setCancelable(false)

        val textColorRes: Int
        val bgColorRes: Int
        val heading: ArrayList<String> = ArrayList()
        val description: ArrayList<String> = ArrayList()
        val selectedDrawable: Int

        when (breathingData.title) {
            "Box Breathing" -> {
                bgColorRes = R.color.box_breathing_card_color
                textColorRes = R.color.box_breathing_card_color_text
                selectedDrawable = R.drawable.breathwork_dot_box
                heading.clear()
                description.clear()
                heading.add("What This Is")
                description.add("A four-part breathing pattern: inhale, hold, exhale, hold — each for the same count. It brings rhythm and structure to your breath.")

                heading.add("Why It Helps")
                description.add("It slows the heart rate, lowers stress, and boosts focus. Perfect for grounding yourself in tense or overwhelming moments.")

                heading.add("How to Practice")
                description.add("Inhale for 4, hold for 4, exhale for 4, hold again for 4. Repeat calmly for a few rounds.")
            }

            "4-7-8 Breathing" -> {
                bgColorRes = R.color.four_seven_breathing_card_color
                textColorRes = R.color.four_seven_breathing_card_color_text
                selectedDrawable = R.drawable.breathwork_dot_selected
                heading.clear()
                description.clear()
                heading.add("What This Is")
                description.add("A paced breathing method: inhale for 4, hold for 7, exhale for 8. It helps shift your body into deep relaxation.")

                heading.add("Why It Helps")
                description.add("Known to ease anxiety and improve sleep by slowing the breath and calming the mind.")

                heading.add("How to Practice")
                description.add("Inhale (4), hold (7), exhale (8). Repeat for 4 rounds. Let each breath soften your body.")
            }

            "Equal Breathing" -> {
                bgColorRes = R.color.alternate_breathing_card_color
                textColorRes = R.color.alternate_breathing_card_color_text
                heading.clear()
                description.clear()
                selectedDrawable = R.drawable.breathwork_dot_custom
                heading.add("What This Is")
                description.add("Create your own breath rhythm by setting how long you inhale, hold, and exhale — tailored to your mood, energy, or focus.")

                heading.add("Why It Helps")
                description.add("Your breath is personal. Custom Breathing gives you control to match your pace — whether you're calming down or tuning in.")

                heading.add("How to Practice")
                description.add("Set your durations, follow the animation and voice cues, and adjust anytime to suit your comfort or goal.")
            }

            "Alternate Nostril Breathing" -> {
                bgColorRes = R.color.alternate_breathing_card_color
                textColorRes = R.color.alternate_breathing_card_color_text
                heading.clear()
                description.clear()
                selectedDrawable = R.drawable.breathwork_dot_alternate
                heading.add("What This Is")
                description.add("A traditional breath technique where you inhale through one nostril and exhale through the other, alternating between the left and right to create balance in body and mind.")

                heading.add("Why It Helps")
                description.add("Breathing through alternate nostrils calms the nervous system, clears mental clutter, and brings focus. It’s especially helpful during stress, emotional swings, or before sleep.")

                heading.add("How to Practice")
                description.add("Close right nostril with thumb. Inhale from left. Close left nostril with ring finger. Exhale from right. Inhale right. Close. Exhale left. Repeat.")
            }

            else -> {
                bgColorRes = R.color.custom_breathing_card_color
                textColorRes = R.color.custom_breathing_card_color_text
                heading.clear()
                description.clear()
                selectedDrawable = R.drawable.breathwork_dot_custom
                heading.add("What This Is")
                description.add("Create your own breath rhythm by setting how long you inhale, hold, and exhale — tailored to your mood, energy, or focus.")

                heading.add("Why It Helps")
                description.add("Your breath is personal. Custom Breathing gives you control to match your pace — whether you're calming down or tuning in.")

                heading.add("How to Practice")
                description.add("Set your durations, follow the animation and voice cues, and adjust anytime to suit your comfort or goal.")
            }
        }

        val textColor = ContextCompat.getColor(this, textColorRes)
        val bgColor = ContextCompat.getColor(this, bgColorRes)
        dialogBinding.tvTitle.text = breathingData.title
        dialogBinding.btnContinue.setTextColor(textColor)
        dialogBinding.btnContinue.backgroundTintList =
            ColorStateList.valueOf(bgColor)
        dialogBinding.tvTitle.setTextColor(textColor)

        val adapter = BreathWorkContextPagerAdapter(
            this,
            breathingData.thumbnail ?: "",
            textColor,
            bgColor,
            heading,
            description
        )
        dialogBinding.viewPagerBreathWork.adapter = adapter

        dialogBinding.indicatorViewPager1.apply {
            setIndicatorDrawable(R.drawable.breathwork_dot, selectedDrawable)
            setSliderGap(4F)
            setSliderWidth(10F)
            setSliderHeight(10F)
            setCheckedSlideWidth(25F)
            setIndicatorGap(resources.getDimensionPixelOffset(R.dimen.textsize_small))
            //setIndicatorSize(dp20, dp20, dp20, dp20)
            setSlideMode(IndicatorSlideMode.SCALE)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setupWithViewPager(dialogBinding.viewPagerBreathWork)
        }

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
            startNextActivity(breathingData)
        }

        dialogBinding.btnContinue.setOnClickListener {
            val current = dialogBinding.viewPagerBreathWork.currentItem
            val lastPage = adapter.itemCount - 1
            if (current < lastPage) {
                // Move to next page
                dialogBinding.viewPagerBreathWork.currentItem = current + 1
            } else {
                // Last page → Finish
                bottomSheetDialog.dismiss()
                startNextActivity(breathingData)
            }


        }

        bottomSheetDialog.show()
    }

    private fun startNextActivity(breathingData: BreathingData) {
        val intent =
            Intent(this@BreathworkActivity, BreathworkSessionActivity::class.java).apply {
                putExtra("BREATHWORK", breathingData)
                putExtra("StartDate", startDate)
            }
        startActivity(intent)
    }
}