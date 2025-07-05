package com.jetsynthesys.rightlife.ui.new_design

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.ui.new_design.pojo.OnBoardingModuleResponse
import com.jetsynthesys.rightlife.ui.new_design.pojo.StressManagement
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StressManagementSelectionFragment : Fragment() {
    private lateinit var llSelectedStressManagement: LinearLayout
    private lateinit var rlStressManagement: RelativeLayout
    private lateinit var tvSelectedStressManagementHeader: TextView
    private lateinit var tvSelectedStressManagementDesc: TextView
    private lateinit var selectedStressManagement: StressManagement
    private lateinit var tvStressManagementHeader: TextView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StressManagementAdapter
    private val stressManagementList = ArrayList<StressManagement>()
    private lateinit var header: String

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
        if (!(activity as OnboardingQuestionnaireActivity).forProfileChecklist) {
            (activity as OnboardingQuestionnaireActivity).tvSkip.visibility = VISIBLE
        }

        val btnContinue = view.findViewById<Button>(R.id.btn_continue)

        header = arguments?.getString("HEADER").toString()

        if (header.isNullOrEmpty()) {
            header = SharedPreferenceManager.getInstance(requireContext()).selectedWellnessFocus
        }

        recyclerView.setLayoutManager(LinearLayoutManager(requireContext()))

        adapter =
            StressManagementAdapter(requireContext(), stressManagementList) { stressManagement ->
                selectedStressManagement = stressManagement
                btnContinue.isEnabled = true
                val colorStateList =
                    ContextCompat.getColorStateList(requireContext(), R.color.menuselected)
                btnContinue.backgroundTintList = colorStateList
            }

        if (header.isNullOrEmpty())
            getModuleList()
        else
            setList()

        recyclerView.adapter = adapter


        btnContinue.setOnClickListener {
            llSelectedStressManagement.visibility = VISIBLE
            rlStressManagement.visibility = GONE
            tvSelectedStressManagementHeader.text = selectedStressManagement.header
            tvSelectedStressManagementDesc.text = selectedStressManagement.description

            val onboardingQuestionRequest =
                SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
            onboardingQuestionRequest.experienceStressMindfulManagement =
                selectedStressManagement.header
            SharedPreferenceManager.getInstance(requireContext())
                .saveOnboardingQuestionAnswer(onboardingQuestionRequest)
            (activity as OnboardingQuestionnaireActivity).submitAnswer(onboardingQuestionRequest)
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        llSelectedStressManagement.visibility = GONE
        rlStressManagement.visibility = VISIBLE
    }

    private fun getModuleList() {
        val authToken = SharedPreferenceManager.getInstance(requireContext()).accessToken
        val apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        val call = apiService.getOnboardingModule(authToken)

        call.enqueue(object : Callback<OnBoardingModuleResponse> {
            override fun onResponse(
                call: Call<OnBoardingModuleResponse>,
                response: Response<OnBoardingModuleResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()
                    // Access the 'data' and 'services' fields
                    val data = apiResponse?.data
                    data?.services?.forEach { item ->
                        if (item.isSelected) {
                            header = item.moduleName.toString()
                            setList()
                            return
                        }
                    }

                }
            }

            override fun onFailure(call: Call<OnBoardingModuleResponse>, t: Throwable) {

            }

        })
    }

    private fun setList() {
        when (header) {
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
        }
        adapter.notifyDataSetChanged()
    }
}