package com.example.rlapp.ai_package.ui.moveright

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.model.YourActivityLogMeal
import com.example.rlapp.ai_package.ui.adapter.YourActivitiesListAdapter
import com.example.rlapp.ai_package.ui.home.HomeBottomTabFragment
import com.example.rlapp.databinding.FragmentYourActivityBinding
import com.google.android.material.snackbar.Snackbar

class YourActivityFragment : BaseFragment<FragmentYourActivityBinding>() {
    private lateinit var usernameReset: EditText
    private lateinit var signupConfirm: TextView
    private lateinit var activitySync: ImageView
    private lateinit var confirmResetBtn: AppCompatButton
    private lateinit var progressBarConfirmation: ProgressBar
    private lateinit var mealLogDateListAdapter: RecyclerView
    private lateinit var imageCalender: ImageView
    private lateinit var btnLogMeal: LinearLayoutCompat
    private lateinit var addWorkout: LinearLayoutCompat

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentYourActivityBinding
        get() = FragmentYourActivityBinding::inflate
    var snackbar: Snackbar? = null

    private val mealLogDateAdapter by lazy {
        YourActivitiesListAdapter(
            requireContext(),
            arrayListOf(),
            -1,
            null,
            false,
            ::onMealLogDateItem
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        mealLogDateListAdapter = view.findViewById(R.id.recyclerview_calender)
        imageCalender = view.findViewById(R.id.image_calender)
        btnLogMeal = view.findViewById(R.id.layout_btn_log_meal)
        activitySync = view.findViewById(R.id.activities_sync)
        addWorkout = view.findViewById(R.id.apple_health_sync_button)

        showTooltipsSequentially()

        activitySync.setOnClickListener {
            val bottomSheet = ActivitySyncBottomSheet()
            bottomSheet.show(parentFragmentManager, "ActivitySyncBottomSheet")
        }

        addWorkout.setOnClickListener {
            val fragment = AddWorkoutSearchFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "AddWorkoutSearchFragment")
                addToBackStack("AddWorkoutSearchFragment")
                commit()
            }
        }

        mealLogDateListAdapter.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mealLogDateListAdapter.adapter = mealLogDateAdapter

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()

                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            })

        onMealLogDateItemRefresh()

        imageCalender.setOnClickListener {
            val fragment = ActivitySyncCalenderFragment()
            val args = Bundle()

            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        btnLogMeal.setOnClickListener {
            val fragment = SearchWorkoutFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "searchWorkoutFragment")
                addToBackStack("searchWorkoutFragment")
                commit()
            }
        }
    }

    private fun showTooltipsSequentially() {
        Handler(Looper.getMainLooper()).postDelayed({
            showTooltipDialogSync(activitySync, "You can sync to apple \n health / google health \n from here.")
        }, 1000)

        Handler(Looper.getMainLooper()).postDelayed({
            showTooltipDialog(imageCalender, "You can access calendar \n view from here.")
        }, 5000)
    }

    private fun showTooltipDialog(anchorView: View, tooltipText: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.tooltip_layout)
        val tvTooltip = dialog.findViewById<TextView>(R.id.tvTooltipText)
        tvTooltip.text = tooltipText

        // Set transparent background for rounded tooltip
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Get screen dimensions
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // Get anchor view position
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Tooltip width (adjust as needed)
        val tooltipWidth = 250

        // Set dialog position
        val params = dialog.window?.attributes
        // Align tooltip to the **right** of the button
        params?.x = (location[0] + anchorView.width) + tooltipWidth
        // Position tooltip **above** the button
        params?.y = location[1] - anchorView.height + 15  // Add some spacing
        dialog.window?.attributes = params
        dialog.window?.setGravity(Gravity.TOP)

        // Show the dialog
        dialog.show()

        // Auto dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 3000)
    }
    private fun showTooltipDialogSync(anchorView: View, tooltipText: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.tooltip_sync_layout)
        val tvTooltip = dialog.findViewById<TextView>(R.id.tvTooltipText)
        tvTooltip.text = tooltipText

        // Set transparent background for rounded tooltip
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Get screen dimensions
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // Get anchor view position
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Tooltip width (adjust as needed)
        val tooltipWidth = 250

        // Set dialog position
        val params = dialog.window?.attributes
        // Align tooltip to the **right** of the button
        params?.x = (location[0] + anchorView.width) + tooltipWidth
        // Position tooltip **above** the button
        params?.y = location[1] - anchorView.height + 15  // Add some spacing
        dialog.window?.attributes = params
        dialog.window?.setGravity(Gravity.TOP)

        // Show the dialog
        dialog.show()

        // Auto dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 3000)
    }



    private fun onMealLogDateItemRefresh() {
        val mealLogs = listOf(
            YourActivityLogMeal("01", "M", true),
            YourActivityLogMeal("02", "T", false),
            YourActivityLogMeal("03", "W", true),
            YourActivityLogMeal("04", "T", false),
            YourActivityLogMeal("05", "F", true),
            YourActivityLogMeal("06", "S", true),
            YourActivityLogMeal("07", "S", true)
        )

        val valueLists: ArrayList<YourActivityLogMeal> = ArrayList()
        valueLists.addAll(mealLogs as Collection<YourActivityLogMeal>)
        val mealLogDateData: YourActivityLogMeal? = null
        mealLogDateAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealLogDateItem(mealLogDateModel: YourActivityLogMeal, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            YourActivityLogMeal("01", "M", true),
            YourActivityLogMeal("02", "T", false),
            YourActivityLogMeal("03", "W", true),
            YourActivityLogMeal("04", "T", false),
            YourActivityLogMeal("05", "F", true),
            YourActivityLogMeal("06", "S", true),
            YourActivityLogMeal("07", "S", true)
        )

        val valueLists: ArrayList<YourActivityLogMeal> = ArrayList()
        valueLists.addAll(mealLogs as Collection<YourActivityLogMeal>)
        mealLogDateAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }
}