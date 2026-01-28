package com.jetsynthesys.rightlife.ui.new_design

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.new_design.pojo.StressManagement
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StressManagementSelectionFragment : Fragment() {
    private var submitJob: Job? = null

    private lateinit var llSelectedStressManagement: LinearLayout
    private lateinit var rlStressManagement: RelativeLayout
    private lateinit var tvSelectedStressManagementHeader: TextView
    private lateinit var tvSelectedStressManagementDesc: TextView
    private lateinit var selectedStressManagement: StressManagement
    private lateinit var tvStressManagementHeader: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StressManagementAdapter
    private val stressManagementList = ArrayList<StressManagement>()

    companion object {
        fun newInstance(pageIndex: Int, header: String): StressManagementSelectionFragment {
            val fragment = StressManagementSelectionFragment()
            val args = Bundle()
            args.putString("HEADER", header)
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
            inflater.inflate(R.layout.fragment_stress_management_selection, container, false)

        llSelectedStressManagement = view.findViewById(R.id.ll_selected_stress_management)
        rlStressManagement = view.findViewById(R.id.rl_stress_management)
        tvSelectedStressManagementHeader =
            view.findViewById(R.id.tv_selected_stress_management_header)
        tvSelectedStressManagementDesc = view.findViewById(R.id.tv_selected_stress_management_desc)

        recyclerView = view.findViewById(R.id.rv_stress_management)
        tvStressManagementHeader = view.findViewById(R.id.tv_header_stress_management)
        /*if (!(activity as OnboardingQuestionnaireActivity).forProfileChecklist) {
            (activity as OnboardingQuestionnaireActivity).tvSkip.visibility = VISIBLE
        }*/

        val sharedPreferenceManager = SharedPreferenceManager.getInstance(requireContext())
        AnalyticsLogger.logEvent(
            AnalyticsEvent.STRESS_MANAGEMENT_VISIT,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule
            )
        )

        val btnContinue = view.findViewById<Button>(R.id.btn_continue)


        recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))

        adapter =
            StressManagementAdapter(requireContext(), stressManagementList) { stressManagement ->
                selectedStressManagement = stressManagement
                btnContinue.isEnabled = true
                val colorStateList =
                    ContextCompat.getColorStateList(requireContext(), R.color.menuselected)
                btnContinue.backgroundTintList = colorStateList
            }

        setList()

        recyclerView.adapter = adapter


        btnContinue.setOnClickListener {
            llSelectedStressManagement.visibility = VISIBLE
            rlStressManagement.visibility = GONE
            tvSelectedStressManagementHeader.text = selectedStressManagement.header
            tvSelectedStressManagementDesc.text = selectedStressManagement.description

            btnContinue.disableViewForSeconds()
            val onboardingQuestionRequest =
                SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
            onboardingQuestionRequest.experienceStressMindfulManagement =
                selectedStressManagement.header
            SharedPreferenceManager.getInstance(requireContext())
                .saveOnboardingQuestionAnswer(onboardingQuestionRequest)

            AnalyticsLogger.logEvent(
                AnalyticsEvent.STRESS_MANAGEMENT_SELECTION,
                mapOf(
                    AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                    AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                    AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
                    AnalyticsParam.GENDER to (onboardingQuestionRequest.gender ?: "Male"),
                    AnalyticsParam.AGE to (onboardingQuestionRequest.age ?: 27),
                    AnalyticsParam.HEIGHT to (onboardingQuestionRequest.height ?: "173 cm"),
                    AnalyticsParam.WEIGHT to (onboardingQuestionRequest.weight ?: "75 kg"),
                    AnalyticsParam.BODY_FAT to (onboardingQuestionRequest.bodyFat ?: ""),
                    AnalyticsParam.STRESS_MANAGEMENT to selectedStressManagement.header
                )
            )

         /*   Handler(Looper.getMainLooper()).postDelayed({
                (activity as OnboardingQuestionnaireActivity).submitAnswer(onboardingQuestionRequest)
            }, 500)*/
            // cancel any pending submit
            submitJob?.cancel()

            // launch lifecycle-aware delayed submit
            submitJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(500)
                // only submit if we’re still at least STARTED (i.e., not backing away)
                if (!isAdded || !lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return@launch
                (activity as? OnboardingQuestionnaireActivity)?.submitAnswer(onboardingQuestionRequest)
            }
        }

        return view
    }

/*    override fun onPause() {
        super.onPause()
        llSelectedStressManagement.visibility = GONE
        rlStressManagement.visibility = VISIBLE
    }*/

    override fun onPause() {
        super.onPause()
        // ensure pending submit won't fire after back
        submitJob?.cancel()
        llSelectedStressManagement.visibility = GONE
        rlStressManagement.visibility = VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        submitJob?.cancel()
    }

    private fun setList() {
        /*when (header) {
            AppConstants.EAT_RIGHT, "EAT_RIGHT" -> {
                tvStressManagementHeader.text = "How would you describe your current eating?"
                stressManagementList.add(
                    StressManagement(
                        "Beginner",
                        "I don’t pay much attention to what I eat.",
                        R.drawable.beginer
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Intermediate",
                        "I try to eat balanced meals but sometimes struggle",
                        R.drawable.intermediate
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Advanced",
                        "I follow a specific diet and track my nutrition regularly.",
                        R.drawable.advance
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Expert",
                        "I follow a specific diet and track my nutrition regularly.",
                        R.drawable.expert
                    )
                )
            }

            AppConstants.THINK_RIGHT, "THINK_RIGHT" -> {
                tvStressManagementHeader.text =
                    "What is your experience with mindfulness or stress management?"
                stressManagementList.add(
                    StressManagement(
                        "Beginner",
                        "I occasionally meditate or practise breathing exercises.",
                        R.drawable.beginer
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Intermediate",
                        "I haven’t tried mindfulness or stress management yet.",
                        R.drawable.intermediate
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Advanced",
                        "I regularly practise mindfulness, journaling, or stress-relief techniques.",
                        R.drawable.advance
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Expert",
                        "I have a solid routine for managing stress and maintaining mental clarity.",
                        R.drawable.expert
                    )
                )
            }

            AppConstants.SLEEP_RIGHT, "SLEEP_RIGHT" -> {
                tvStressManagementHeader.text = "How would you rate your current sleep routine?"
                stressManagementList.add(
                    StressManagement(
                        "Beginner",
                        "I rarely sleep on time and often feel tired.",
                        R.drawable.beginer
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Intermediate",
                        "I try to maintain a sleep schedule but it’s inconsistent.",
                        R.drawable.intermediate
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Advanced",
                        "I follow a regular sleep routine and feel rested most days.",
                        R.drawable.advance
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Expert",
                        "I prioritise sleep hygiene and consistently get quality sleep",
                        R.drawable.expert
                    )
                )
            }

            AppConstants.MOVE_RIGHT, "MOVE_RIGHT" -> {
                tvStressManagementHeader.text = "How active are you on a regular basis?"

                stressManagementList.add(
                    StressManagement(
                        "Beginner",
                        "I rarely exercise or engage in physical activity.",
                        R.drawable.beginer
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Intermediate",
                        "I stay active a few times a week (walks, light workouts).",
                        R.drawable.intermediate
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Advanced",
                        "I workout regularly and have a structured fitness routine.",
                        R.drawable.advance
                    )
                )

                stressManagementList.add(
                    StressManagement(
                        "Expert",
                        "I craft my own progressive workouts & specialised training programs, and track key metrics.",
                        R.drawable.expert
                    )
                )
            }

            else -> {

            }
        }*/


        tvStressManagementHeader.text = "How confident are you in improving your health habits?"

        stressManagementList.add(
            StressManagement(
                "Beginner",
                "I’m unsure how to start and often feel stuck.",
                R.drawable.beginer
            )
        )

        stressManagementList.add(
            StressManagement(
                "Intermediate",
                "I believe I can improve but need more guidance.",
                R.drawable.intermediate
            )
        )

        stressManagementList.add(
            StressManagement(
                "Advanced",
                "I feel confident about making progress with the right tools.",
                R.drawable.advance
            )
        )

        stressManagementList.add(
            StressManagement(
                "Expert",
                "I’m very confident and motivated to keep improving my health habits.",
                R.drawable.expert
            )
        )

        adapter.notifyDataSetChanged()
    }
}