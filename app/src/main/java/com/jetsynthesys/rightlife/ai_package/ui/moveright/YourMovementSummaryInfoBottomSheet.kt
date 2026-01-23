package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R
import com.google.android.material.R as MaterialR

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
        val title = view.findViewById<TextView>(R.id.title)
        val viewN = view.findViewById<View>(R.id.view)
        val close = view.findViewById<ImageView>(R.id.close)
        val summary = view.findViewById<TextView>(R.id.tvMealSummary)
        val image = view.findViewById<ImageView>(R.id.image)
        image.visibility = View.GONE
        viewN.visibility = View.GONE
        close.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.close_breathwork))
        title.text = "Your Movement Summary"
        close.setOnClickListener { dismiss() }
        // HTML Content
        val htmlContentPart1 = """
        <h2>What is Your Movement Summary?</h2>
        <p><b>Your Movement Summary</b> gives you a quick overview of your daily physical activity, helping you track your workouts and steps in one place. Whether you're using a wearable device or just your phone, this section ensures you stay informed about your movement trends.</p>

        <h3>Workouts</h3>
        <p>This card provides a snapshot of your workout sessions. If you're using a wearable device, you'll see heart rate data and calorie burn estimates. If you're not using a wearable, you'll still see your workout duration as well as a personalised calorie estimation through your demographic data and average <b>MET (Metabolic Equivalent of Task)</b> activity values.</p>

        <p><i>'What are MET Values?' [Recommended Article]</i></p>

        <ul style="padding-left: 20px;">
          <li style="margin-bottom: 8px; padding-left: 8px;"><b>Workout Type</b> – Displays the type of workout you logged.</li>
          <li style="margin-bottom: 8px; padding-left: 8px;"><b>Total Duration</b> – The total time you spent exercising.</li>
          <li style="margin-bottom: 8px; padding-left: 8px;"><b>Calories Burned</b> – Estimated based on your workout and intensity.</li>
          <li style="margin-bottom: 8px; padding-left: 8px;"><b>Heart Rate Load (Wearable Users Only)</b> – Your heart rate data during the session, giving insights into workout intensity and effectiveness.</li>
        </ul>

        <p>Tap the card to view more details about your session, including trends over time and heart rate zones (for wearable users).</p>
        """.trimIndent()

        val htmlContentStepsHeading = "<h3>Steps</h3>"

        val htmlContentStepsBody = """
        <p>This card tracks your steps throughout the day by syncing data from your phone. It helps you understand your activity levels and compare them to your average and goal.</p>

        <ul style="padding-left: 20px;">
          <li style="margin-bottom: 8px; padding-left: 8px;"><b>Today's Steps</b> – The number of steps you've taken so far.</li>
          <li style="margin-bottom: 8px; padding-left: 8px;"><b>Average Steps</b> – Your typical step count, based on past data.</li>
          <li style="margin-bottom: 8px; padding-left: 8px;"><b>Goal Steps</b> – A customizable target to keep you motivated.</li>
          <li style="margin-bottom: 8px; padding-left: 8px;"><b>Trend Line</b> – Shows how your steps compare to your usual activity levels.</li>
        </ul>

        <p>Tap the card for a more detailed breakdown, including step trends, hourly movement, and progress over time.</p>
        """.trimIndent()

        val htmlContentPart2 = """
        <p>With the <b>Movement Summary</b>, you get a clear, at-a-glance view of your physical activity, helping you stay active and reach your goals effortlessly.</p>
        """.trimIndent()

        // Process texts
        val spannableString1 = SpannableStringBuilder(Html.fromHtml(htmlContentPart1, Html.FROM_HTML_MODE_LEGACY))
        applyHeadingStyle(spannableString1, "What is Your Movement Summary?")
        applyHeadingStyle(spannableString1, "Workouts")

        val spannableStepsHeading = SpannableStringBuilder(Html.fromHtml(htmlContentStepsHeading, Html.FROM_HTML_MODE_LEGACY))
        applyHeadingStyle(spannableStepsHeading, "Steps")

        val spannableStepsBodyAndClosing = SpannableStringBuilder(Html.fromHtml(htmlContentStepsBody + htmlContentPart2, Html.FROM_HTML_MODE_LEGACY))

        // Replace summary TextView
        val parentLayout = summary.parent as? ViewGroup
        val summaryIndex = parentLayout?.indexOfChild(summary) ?: -1

        if (parentLayout != null && summaryIndex != -1) {
            parentLayout.removeView(summary)

            val containerLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = summary.layoutParams
            }

            // FIRST SECTION - White background with 24dp top/bottom padding
            val firstSectionContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 12f
                }
                val padding16dp = (16 * resources.displayMetrics.density).toInt()
                setPadding(padding16dp, padding16dp, padding16dp, padding16dp)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = (8 * resources.displayMetrics.density).toInt()
                }
            }

            // Split content to insert image after "Workouts" heading
            val workoutsHeadingIndex = spannableString1.indexOf("Workouts")
            val workoutsHeadingEndIndex = spannableString1.indexOf("\n", workoutsHeadingIndex)

            val beforeWorkoutsImage = spannableString1.subSequence(0, workoutsHeadingEndIndex + 1)
            val afterWorkoutsImage = spannableString1.subSequence(workoutsHeadingEndIndex + 1, spannableString1.length)

            // Text before image
            val textView1 = TextView(requireContext()).apply {
                text = beforeWorkoutsImage
                textSize = summary.textSize / resources.displayMetrics.scaledDensity
                setTextColor(summary.currentTextColor)
                typeface = summary.typeface
                setIncludeFontPadding(false)

                val padding24dp = (24 * resources.displayMetrics.density).toInt()
                setPadding(0, padding24dp, 0, 0)
            }
            firstSectionContainer.addView(textView1)

            // Workouts Image
            val workoutsImage = ImageView(requireContext()).apply {
                setImageResource(R.drawable.workout_pic) // Apna workout image yahan change kar dena
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (16 * resources.displayMetrics.density).toInt()
                    bottomMargin = (20 * resources.displayMetrics.density).toInt()
                }
            }
            firstSectionContainer.addView(workoutsImage)

            // Text after image
            val textView2 = TextView(requireContext()).apply {
                text = afterWorkoutsImage
                textSize = summary.textSize / resources.displayMetrics.scaledDensity
                setTextColor(summary.currentTextColor)
                typeface = summary.typeface
                setIncludeFontPadding(false)

                val padding24dp = (24 * resources.displayMetrics.density).toInt()
                setPadding(0, 0, 0, padding24dp)
            }
            firstSectionContainer.addView(textView2)
            containerLayout.addView(firstSectionContainer)

            // STEPS SECTION - Pink background
            val stepsAndClosingContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#FFF4F4"))
                    cornerRadius = 12f
                }
                val padding16dp = (16 * resources.displayMetrics.density).toInt()
                setPadding(padding16dp, padding16dp, padding16dp, padding16dp)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (8 * resources.displayMetrics.density).toInt()
                    bottomMargin = (8 * resources.displayMetrics.density).toInt()
                }
            }

            // 1. Steps Heading
            val stepsHeadingView = TextView(requireContext()).apply {
                text = spannableStepsHeading
                textSize = summary.textSize / resources.displayMetrics.scaledDensity
                setTextColor(summary.currentTextColor)
                typeface = summary.typeface
            }
            stepsAndClosingContainer.addView(stepsHeadingView)

            // 2. Image - DIRECTLY UNDER "Steps" HEADING
            val stepsImage = ImageView(requireContext()).apply {
                setImageResource(R.drawable.step_pic)
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (16 * resources.displayMetrics.density).toInt()
                    bottomMargin = (20 * resources.displayMetrics.density).toInt()
                }
            }
            stepsAndClosingContainer.addView(stepsImage)

            // 3. Remaining body text + closing paragraph
            val bodyTextView = TextView(requireContext()).apply {
                text = spannableStepsBodyAndClosing
                textSize = summary.textSize / resources.displayMetrics.scaledDensity
                setTextColor(summary.currentTextColor)
                typeface = summary.typeface
            }
            stepsAndClosingContainer.addView(bodyTextView)

            containerLayout.addView(stepsAndClosingContainer)

            parentLayout.addView(containerLayout, summaryIndex)
        } else {
            // Fallback
            summary.text = SpannableStringBuilder()
                .append(spannableString1)
                .append("\n\n")
                .append(spannableStepsHeading)
                .append(spannableStepsBodyAndClosing)
        }

        // Full width bottom sheet fix
        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheetDialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            WindowCompat.setDecorFitsSystemWindows(bottomSheetDialog.window!!, false)

            val bottomSheet = bottomSheetDialog.findViewById<View>(MaterialR.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }

            bottomSheet?.let {
                BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isHideable = false
                }
            }
        }
    }

    private fun applyHeadingStyle(spannable: SpannableStringBuilder, headingText: String) {
        val startIndex = spannable.indexOf(headingText)
        if (startIndex != -1) {
            val endIndex = startIndex + headingText.length
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#B11414")),
                startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ResourcesCompat.getFont(requireContext(), R.font.dmsans_semibold)?.let {
                spannable.setSpan(CustomTypefaceSpan(it), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
        override fun updateDrawState(ds: TextPaint) = applyCustomTypeface(ds, typeface)
        override fun updateMeasureState(paint: TextPaint) = applyCustomTypeface(paint, typeface)
        private fun applyCustomTypeface(paint: TextPaint, tf: Typeface) {
            paint.typeface = tf
        }
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = YourMovementSummaryInfoBottomSheet()
    }

    override fun onDetach() {
        super.onDetach()
    }
}