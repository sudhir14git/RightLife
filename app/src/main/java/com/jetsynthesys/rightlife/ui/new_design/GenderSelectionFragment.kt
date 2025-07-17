package com.jetsynthesys.rightlife.ui.new_design

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class GenderSelectionFragment : Fragment() {

    private var selectedGender = ""
    private var tvDescription: TextView? = null
    private lateinit var handler: Handler
    private lateinit var llMale: LinearLayout
    private lateinit var llFemale: LinearLayout
    private lateinit var llSelectedGender: LinearLayout

    companion object {
        fun newInstance(pageIndex: Int): GenderSelectionFragment {
            val fragment = GenderSelectionFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_gender_selection, container, false)


        llMale = view.findViewById(R.id.ll_male)
        llFemale = view.findViewById(R.id.ll_female)
        llSelectedGender = view.findViewById(R.id.ll_selected_gender)
        val tvMale = view.findViewById<TextView>(R.id.tv_male)
        val tvFemale = view.findViewById<TextView>(R.id.tv_female)
        val tvSelectedGender = view.findViewById<TextView>(R.id.tv_selected_gender)
        val ivMale = view.findViewById<ImageView>(R.id.image_male)
        val ivFemale = view.findViewById<ImageView>(R.id.image_female)
        val ivSelectedGender = view.findViewById<ImageView>(R.id.image_selected_gender)
        tvDescription = view.findViewById(R.id.tv_description)

        handler = Handler(Looper.getMainLooper())

        val bgDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.bg_gray_border)

        val unwrappedDrawable =
            AppCompatResources.getDrawable(requireContext(), R.drawable.rounded_corder_border_gray)
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        DrawableCompat.setTint(
            wrappedDrawable,
            ContextCompat.getColor(requireContext(), R.color.color_green)
        )

        //(activity as OnboardingQuestionnaireActivity).tvSkip.visibility = INVISIBLE

        llMale.setOnClickListener {
            llMale.disableViewForSeconds()
            selectedGender = "Male"
            llMale.background = wrappedDrawable
            tvMale.setTextColor(requireContext().getColor(R.color.white))
            ivMale.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

            llFemale.background = bgDrawable
            tvFemale.setTextColor(requireContext().getColor(R.color.txt_color_header))
            ivFemale.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.menuselected),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

            handler.postDelayed({
                llMale.visibility = GONE
                llFemale.visibility = GONE
                llSelectedGender.visibility = VISIBLE
                tvSelectedGender.text = selectedGender
                ivSelectedGender.setImageResource(R.drawable.icon_male)
                ivSelectedGender.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_green
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                tvDescription?.visibility = GONE

                saveGender()

            }, 300)
        }

        llFemale.setOnClickListener {
            llFemale.disableViewForSeconds()
            selectedGender = "Female"
            llMale.background = bgDrawable
            tvFemale.setTextColor(requireContext().getColor(R.color.white))
            ivFemale.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

            llFemale.background = wrappedDrawable
            tvMale.setTextColor(requireContext().getColor(R.color.txt_color_header))
            ivMale.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.color_blue),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            handler.postDelayed({
                llMale.visibility = GONE
                llFemale.visibility = GONE
                llSelectedGender.visibility = VISIBLE
                tvSelectedGender.text = selectedGender
                ivSelectedGender.setImageResource(R.drawable.icon_female)
                ivSelectedGender.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_green
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                saveGender()
                tvDescription?.visibility = GONE
            }, 300)
        }

        return view
    }

    private fun saveGender() {
        val onboardingQuestionRequest =
            SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
        onboardingQuestionRequest.gender = selectedGender
        SharedPreferenceManager.getInstance(requireContext())
            .saveOnboardingQuestionAnswer(onboardingQuestionRequest)
        (activity as OnboardingQuestionnaireActivity).submitAnswer(onboardingQuestionRequest)
    }

    override fun onPause() {
        super.onPause()
        llMale.visibility = VISIBLE
        llFemale.visibility = VISIBLE
        llSelectedGender.visibility = GONE
        tvDescription?.visibility = VISIBLE
    }


}