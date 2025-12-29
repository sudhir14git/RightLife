package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.R.color.transparent
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class YourMovementSummaryInfoBottomSheet : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_meal_summary_info_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_frequently_logged)
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(requireContext(), R.color.new_background))
        )
        val title = view.findViewById<TextView>(R.id.title)
        val viewN = view.findViewById<View>(R.id.view)
        val close = view.findViewById<ImageView>(R.id.close)
        val summary = view.findViewById<TextView>(R.id.tvMealSummary)
        val image = view.findViewById<ImageView>(R.id.image)
        image.visibility = View.GONE
        viewN.visibility = View.GONE
        close.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.close_journal)
        )

        image.setImageResource(R.drawable.your_heart_rate_analysis)
        title.text = "Your Movement Summary"

        close.setOnClickListener {
            dismiss()
        }

        val htmlContent = """
        <h2>What is Your Movement Summary?</h2>
        <p><b>Your Movement Summary</b> gives you a quick overview of your daily physical activity, helping you track your workouts and steps in one place. Whether you're using a wearable device or just your phone, this section ensures you stay informed about your movement trends.</p>

<h3>Workouts</h3>
<p>This card provides a snapshot of your workout sessions. If you're using a wearable device, you'll see heart rate data and calorie burn estimates. If you're not using a wearable, you'll still see your workout duration as well as a personalised calorie estimation through your demographic data and average MET (Metabolic Equivalent of Task) activity values.</p>

<p><i>'What are MET Values?' [Recommended Article]</i></p>

<ul>
  <li><b>Workout Type</b> – Displays the type of workout you logged.</li>
  <li><b>Total Duration</b> – The total time you spent exercising.</li>
  <li><b>Calories Burned</b> – Estimated based on your workout and intensity.</li>
  <li><b>Heart Rate Load (Wearable Users Only)</b> – Your heart rate data during the session, giving insights into workout intensity and effectiveness.</li>
</ul>

<p>Tap the card to view more details about your session, including trends over time and heart rate zones (for wearable users).</p>

<h3>Steps</h3>
<p>This card tracks your steps throughout the day by syncing data from your phone. It helps you understand your activity levels and compare them to your average and goal.</p>

<ul>
  <li><b>Today's Steps</b> – The number of steps you've taken so far.</li>
  <li><b>Average Steps</b> – Your typical step count, based on past data.</li>
  <li><b>Goal Steps</b> – A customizable target to keep you motivated.</li>
  <li><b>Trend Line</b> – Shows how your steps compare to your usual activity levels.</li>
</ul>

<p>Tap the card for a more detailed breakdown, including step trends, hourly movement, and progress over time.</p>

<p>With the <b>Movement Summary</b>, you get a clear, at-a-glance view of your physical activity, helping you stay active and reach your goals effortlessly.</p>
        """.trimIndent()

        // Convert HTML to Spanned text
        val spannedText = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY)

        // Apply custom styling to the heading
        val spannableString = SpannableStringBuilder(spannedText)
        val headingText = "What is Your Movement Summary?"
        val startIndex = spannableString.indexOf(headingText)

        if (startIndex != -1) {
            val endIndex = startIndex + headingText.length

            // Apply color #B11414
            spannableString.setSpan(
                ForegroundColorSpan(Color.parseColor("#B11414")),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Apply DM Sans font family
            val typeface = ResourcesCompat.getFont(requireContext(), R.font.dmsans_semibold)
            typeface?.let {
                spannableString.setSpan(
                    CustomTypefaceSpan(it),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        summary.text = spannableString
    }

    // Custom TypefaceSpan class for better font rendering
    private class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
        override fun updateDrawState(ds: TextPaint) {
            applyCustomTypeface(ds, typeface)
        }

        override fun updateMeasureState(paint: TextPaint) {
            applyCustomTypeface(paint, typeface)
        }

        private fun applyCustomTypeface(paint: TextPaint, tf: Typeface) {
            paint.typeface = tf
        }
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = YourMovementSummaryInfoBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
    }
}