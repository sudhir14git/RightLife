package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.new_design.pojo.HealthGoal
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class HealthGoalFragment : Fragment() {

    private lateinit var llSelectedHealthGoal: LinearLayout
    private lateinit var rlHealthGoal: RelativeLayout
    private lateinit var tvSelectedHealthGoal: TextView
    private lateinit var tvDescription: TextView
    private var selectedHealthGoal = ""

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HealthGoalAdapter
    private val healthGoalList = ArrayList<HealthGoal>()

    companion object {
        fun newInstance(pageIndex: Int): HealthGoalFragment {
            val fragment = HealthGoalFragment()
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
        val view: View =
            inflater.inflate(R.layout.fragment_health_goals, container, false)

        llSelectedHealthGoal = view.findViewById(R.id.ll_selected_health_goals)
        rlHealthGoal = view.findViewById(R.id.rl_health_goals)
        tvSelectedHealthGoal = view.findViewById(R.id.tv_selected_health_goals)
        recyclerView = view.findViewById(R.id.rv_health_goals)
        tvDescription = view.findViewById(R.id.tv_description)

        if (!(activity as OnboardingQuestionnaireActivity).forProfileChecklist) {
            (activity as OnboardingQuestionnaireActivity).tvSkip.visibility = VISIBLE
        }

        healthGoalList.add(HealthGoal("0-10 minutes"))
        healthGoalList.add(HealthGoal("10-20 minutes"))
        healthGoalList.add(HealthGoal("20-30 minutes"))
        healthGoalList.add(HealthGoal("30+ minutes"))

        val btnContinue = view.findViewById<Button>(R.id.btn_continue)

        val colorStateListSelected =
            ContextCompat.getColorStateList(requireContext(), R.color.menuselected)

        adapter = HealthGoalAdapter(requireContext(), healthGoalList) { healthGoal ->
            btnContinue.isEnabled = true
            btnContinue.backgroundTintList = colorStateListSelected
            selectedHealthGoal = healthGoal.header
        }

        recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        recyclerView.adapter = adapter

        (activity as OnboardingQuestionnaireActivity).tvSkip.setOnClickListener {
            startActivity(
                Intent(
                    requireActivity(),
                    AwesomeScreenActivity::class.java
                )
            )
            finishAffinity(requireActivity())
            SharedPreferenceManager.getInstance(requireActivity())
                .clearOnboardingQuestionRequest()
        }

        btnContinue.setOnClickListener {
            llSelectedHealthGoal.visibility = VISIBLE
            rlHealthGoal.visibility = GONE
            tvSelectedHealthGoal.text = selectedHealthGoal
            tvDescription.visibility = GONE

            btnContinue.disableViewForSeconds()

            val onboardingQuestionRequest =
                SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
            onboardingQuestionRequest.dailyGoalAchieveTime = selectedHealthGoal
            SharedPreferenceManager.getInstance(requireContext())
                .saveOnboardingQuestionAnswer(onboardingQuestionRequest)

            (activity as OnboardingQuestionnaireActivity).submitAnswer(onboardingQuestionRequest)
        }


        return view
    }

    override fun onPause() {
        super.onPause()
        llSelectedHealthGoal.visibility = GONE
        rlHealthGoal.visibility = VISIBLE
        tvDescription.visibility = VISIBLE
    }
}