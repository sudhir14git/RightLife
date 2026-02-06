package com.jetsynthesys.rightlife.ai_package.ui.steps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.ui.moveright.MoveRightLandingFragment
import com.jetsynthesys.rightlife.ai_package.ui.moveright.StepFragment
import com.jetsynthesys.rightlife.databinding.FragmentSetYourStepGoalBinding
import com.jetsynthesys.rightlife.ai_package.ui.moveright.StepIntake
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetYourStepGoalFragment : BaseFragment<FragmentSetYourStepGoalBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSetYourStepGoalBinding
        get() = FragmentSetYourStepGoalBinding::inflate

    private lateinit var stepsSliderView: StepIntake
    private lateinit var setTargetButton: TextView
    private lateinit var tvCurrentGoal: TextView
    private lateinit var backButton : ImageView

    private var currentGoal = 10000 // Increased initial goal to start in the middle of the range
    private val maxSteps = 20000

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_color_background_workout)
        val args = arguments
        currentGoal = args?.getInt("currentGoal") ?: 10000
        // Initialize views
        stepsSliderView = view.findViewById(R.id.steps_slider_view)
        setTargetButton = view.findViewById(R.id.tv_set_target)
        tvCurrentGoal = view.findViewById(R.id.step_count_text_5)
        backButton = view.findViewById(R.id.back_button)

        // Configure StepIntake
        stepsSliderView.setMinSteps(0)
        stepsSliderView.setMaxSteps(maxSteps)
        stepsSliderView.setFillPercentage(currentGoal.toFloat() / maxSteps)
        stepsSliderView.setIntervalColors(ContextCompat.getColor(requireContext(), R.color.green_text))
        stepsSliderView.setOnStepCountChangeListener(object : StepIntake.OnStepCountChangeListener {
            override fun onStepCountChanged(stepCount: Int, recommendedSteps: Int) {
                Log.d("SetYourStepGoalFragment", "Step count changed: $stepCount, Recommended: $recommendedSteps")
                currentGoal = stepCount
                updateCurrentGoalText()
                // Update recommendation text (assuming you have a TextView for it)
                /*recommendationTextView.text = if (stepCount < 12000) {
                    "We recommend increasing by 500 steps gradually, at your own pace, until your average is in the optimal range of 12,000 or more. Next target: $recommendedSteps steps."
                } else {
                    "Great job! Your step count is in the optimal range of 12,000 or more."
                }*/
            }
        })

        // Set Target Button Click Listener
        setTargetButton.setOnClickListener {
            context?.let { it1 ->
                AnalyticsLogger.logEvent(
                    it1, AnalyticsEvent.MR_Steps_SetGoal_SetTarget
                )
            }
            setStepsGoal(currentGoal)
        }

        // Back Press Handler
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMoveRightLandingFragment()
            }
        })

        backButton.setOnClickListener {
            navigateToMoveRightLandingFragment()
        }

        // Initial text update
        updateCurrentGoalText()
    }

    /** Function to set the steps goal via API */
    private fun setStepsGoal(stepsGoal: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                    ?: "680790d0a8d2c1b4456e5c7d" // Default user ID if not found
                val source = "samsung" // Hardcoded source as per the API call

                val response = ApiClient.apiServiceFastApi.setStepsGoal(
                    userId = userId,
                    source = source,
                    stepsGoal = stepsGoal
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val setStepsGoalResponse = response.body()
                        if (setStepsGoalResponse?.status_code == 200) {
                            Toast.makeText(
                                requireContext(),
                                setStepsGoalResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            // Navigate back to MoveRightLandingFragment on success
                            navigateToMoveRightLandingFragment()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to set steps goal: ${setStepsGoalResponse?.message ?: "Unknown error"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToMoveRightLandingFragment() {
        val fragment = StepFragment()
        val args = Bundle()
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, "landing")
            addToBackStack("landing")
            commit()
        }
    }

    private fun updateCurrentGoalText() {
        tvCurrentGoal.text = "${currentGoal}"
    }
}