package com.jetsynthesys.rightlife.ai_package.ui.eatright

import android.R.color.transparent
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

class RatingReportFeedbackBottomSheet : BottomSheetDialogFragment() {

    private var listener: RatingReportFeedbackListener? = null
    private var isRating: Boolean = false
    private var context: Context? = null

    interface RatingReportFeedbackListener {
        fun onReportFeedbackRating(rating: Double, isSave: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        listener = when {
            context is RatingReportFeedbackListener -> context
            parentFragment is RatingReportFeedbackListener -> parentFragment as RatingReportFeedbackListener
            else -> throw ClassCastException("$context must implement RatingReportFeedbackListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rating_report_feedback_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_meal_scan_results)
        dialog.window?.setBackgroundDrawableResource(transparent)
        val closeIcon = view.findViewById<ImageView>(R.id.closeIc)
        val layoutSubmit = view.findViewById<LinearLayoutCompat>(R.id.layoutSubmit)
        val ratingLayout = view.findViewById<ConstraintLayout>(R.id.ratingLayout)
        val afterRatingLayout = view.findViewById<ConstraintLayout>(R.id.afterRatingLayout)
        val successLayout = view.findViewById<ConstraintLayout>(R.id.rateSuccessFullLayout)
        val disabledLayoutCancel = view.findViewById<LinearLayoutCompat>(R.id.disabled_layoutCancel)
        val layoutNotNow = view.findViewById<LinearLayoutCompat>(R.id.layoutNotNow)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val tvTitles = view.findViewById<TextView>(R.id.tvTitles)
        val layoutSubmitAfter = view.findViewById<LinearLayoutCompat>(R.id.layoutSubmitAfter)
        val layoutNotNowAfter = view.findViewById<LinearLayoutCompat>(R.id.layoutNotNowAfter)
        val editText = view.findViewById<EditText>(R.id.editText)
        val tvRateDescriptions = view.findViewById<TextView>(R.id.tvRateDescriptions)
        val tvCount = view.findViewById<TextView>(R.id.tvCount)

        val isSave = arguments?.getBoolean("isSave") ?: false
        /*  if (isRating){
              layoutCancel.visibility = View.VISIBLE
              layoutCancel.setBackgroundResource(R.drawable.add_cart_button_background)
              layoutCancel.isEnabled = true
             // disabledLayoutCancel.visibility =View.GONE
          }else{
              layoutCancel.visibility = View.VISIBLE
              layoutCancel.setBackgroundResource(R.drawable.add_card_background_disabled)
              layoutCancel.isEnabled = false
              //disabledLayoutCancel.visibility =View.VISIBLE
          }*/
        layoutSubmit.setBackgroundResource(R.drawable.add_card_background_disabled)
        layoutSubmit.isEnabled = false

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            isRating = true
            layoutSubmit.setBackgroundResource(R.drawable.add_cart_button_background)
            layoutSubmit.isEnabled = true
            if (rating > 3){
                tvTitles.text = "What did we get right ?"
                tvRateDescriptions.text = "Your insights help us understand what’s working so we can keep doing more of it."
            }else{
                tvTitles.text = "What Can We Do Better ?"
                tvRateDescriptions.text = "Help us improve by sharing what felt off or missing."
            }
           // Toast.makeText(context, "You rated $rating stars", Toast.LENGTH_SHORT).show()
        }

        closeIcon.setOnClickListener {
            dismiss()
        }

        layoutSubmit.setOnClickListener {
            ratingLayout.visibility = View.GONE
            afterRatingLayout.visibility = View.VISIBLE
            //dismiss()
            val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        }

        layoutNotNow.setOnClickListener {
            dismiss()
            //   Toast.makeText(view.context, "Dish Removed", Toast.LENGTH_SHORT).show()
            listener?.onReportFeedbackRating(1.0, isSave)
        }

        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val count = s?.length ?: 0
                tvCount.text = "$count /300 Characters"
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        layoutSubmitAfter.setOnClickListener {
            if (editText.text.length > 1 && editText.text.isNotEmpty()){
                ratingLayout.visibility = View.GONE
                afterRatingLayout.visibility = View.GONE
                successLayout.visibility = View.VISIBLE
                //dismiss()
                view.postDelayed({
                    listener?.onReportFeedbackRating(1.0, isSave)
                }, 1000) // 5000ms = 5 seconds

                val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
            }else{
                Toast.makeText(view.context, "Please input comment", Toast.LENGTH_SHORT).show()
            }
        }

        layoutNotNowAfter.setOnClickListener {
            dismiss()
            //   Toast.makeText(view.context, "Dish Removed", Toast.LENGTH_SHORT).show()
            listener?.onReportFeedbackRating(1.0, isSave)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        const val TAG = "LoggedBottomSheet"

        @JvmStatic
        fun newInstance() = RatingReportFeedbackBottomSheet().apply {
            arguments = Bundle().apply {
                // Add any required arguments here
            }
        }
    }
}