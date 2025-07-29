package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.model.WorkoutSessionRecord
import com.jetsynthesys.rightlife.ai_package.ui.moveright.viewmodel.WorkoutViewModel
import com.jetsynthesys.rightlife.databinding.FragmentSearchWorkoutBinding
import com.google.android.material.tabs.TabLayout
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SearchWorkoutFragment : BaseFragment<FragmentSearchWorkoutBinding>() {

    private val workoutViewModel: WorkoutViewModel by activityViewModels()
    private lateinit var searchWorkoutBackButton: ImageView
    private var workoutList = ArrayList<WorkoutSessionRecord>()
    private var mSelectedDate = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchWorkoutBinding
        get() = FragmentSearchWorkoutBinding::inflate

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        val selectedDate = arguments?.getString("selected_date")
        val routine = arguments?.getString("routine")
        val routineName = arguments?.getString("routineName")
        workoutList = arguments?.getParcelableArrayList("workoutList") ?: ArrayList()
        var selectedTab = arguments?.getInt("selectedTab", 0) ?: 0  // 🔥 get selected tab index
        val searchEditText: EditText = view.findViewById(R.id.searchEditText)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        if (selectedDate != null){
            mSelectedDate = convertDate(selectedDate)
        }
        searchWorkoutBackButton = view.findViewById(R.id.search_workout_back_button)

        searchWorkoutBackButton.setOnClickListener {
            navigateToYourActivityFragment()
        }

        // Set up tabs
        val tabTitles = arrayOf("All Workouts", "My Routine", "Frequently Logged")
        for (title in tabTitles) {
            val tab = tabLayout.newTab()
            val customView = LayoutInflater.from(context).inflate(R.layout.custom_search_tab, null) as TextView
            customView.text = title
            tab.customView = customView
            tabLayout.addTab(tab)
        }

        // Initial fragment load based on selectedTab
        if (savedInstanceState == null) {
            val selectTab = arguments?.getString("routineBack")
            if (selectTab == "routineBack") {
                selectedTab = 1
            }
            val initialFragment: Fragment = when (selectedTab) {
                1 -> MyRoutineFragment(mSelectedDate)
                2 -> FrequentlyLoggedSearchFragment()
                else -> AllWorkoutFragment().apply {
                    arguments = Bundle().apply {
                        putString("routine", routine)
                        putString("routineName", routineName)
                        putString("selected_date", mSelectedDate)
                        putParcelableArrayList("workoutList", workoutList)
                    }
                }
            }
            replaceFragment(initialFragment)
            tabLayout.getTabAt(selectedTab)?.select() // ✅ highlight correct tab
        }

        // Handle tab clicks
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        val allWorkoutFragment = AllWorkoutFragment().apply {
                            arguments = Bundle().apply {
                                putString("routine", routine)
                                putString("routineName", routineName)
                                putString("selected_date", mSelectedDate)
                                putParcelableArrayList("workoutList", workoutList)
                            }
                        }
                        replaceFragment(allWorkoutFragment)
                    }
                    1 -> replaceFragment(MyRoutineFragment(mSelectedDate))
                    2 -> replaceFragment(FrequentlyLoggedSearchFragment())
                }
                updateTabColors()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Search listener
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                workoutViewModel.setSearchQuery(query)
                Log.d("SearchWorkoutFragment", "Search query set in ViewModel: $query")
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToYourActivityFragment()
            }
        })
    }

    private fun convertDate(date: String): String{
        val originalFormatter = DateTimeFormatter.ofPattern("E, d MMM yyyy")
        val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val dateStr = date
        val dateFormat = LocalDate.parse(dateStr, originalFormatter)

        val formattedDate = dateFormat.format(targetFormatter)
        return formattedDate
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun updateTabColors() {
        for (i in 0 until bi.tabLayout.tabCount) {
            val tab = bi.tabLayout.getTabAt(i)
            val tabText = tab?.customView?.findViewById<TextView>(R.id.tabText)
            tabText?.setTextColor(
                if (tab?.isSelected == true) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.black_no_meals)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun navigateToYourActivityFragment() {
        val fragment = YourActivityFragment()
        val args = Bundle()
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment, "YourActivityFragment")
            .addToBackStack("YourActivityFragment")
            .commit()
    }
}
