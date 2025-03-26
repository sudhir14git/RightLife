package com.example.rlapp.ai_package.ui.moveright

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.data.repository.ApiClient
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.model.WorkoutList
import com.example.rlapp.ai_package.model.WorkoutResponseModel
import com.example.rlapp.ai_package.ui.adapter.WorkoutAdapter
import com.example.rlapp.ai_package.ui.moveright.viewmodel.WorkoutViewModel
import com.example.rlapp.ai_package.utils.AppPreference
import com.example.rlapp.databinding.FragmentAllWorkoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllWorkoutFragment : BaseFragment<FragmentAllWorkoutBinding>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WorkoutAdapter
    private lateinit var appPreference: AppPreference
    private lateinit var progressDialog: ProgressDialog
    private val workoutViewModel: WorkoutViewModel by activityViewModels()
    private lateinit var searchResultTextView: TextView
    private lateinit var searchResultView: View
    private var workoutList : ArrayList<WorkoutList> = ArrayList()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAllWorkoutBinding
        get() = FragmentAllWorkoutBinding::inflate

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        appPreference = AppPreference(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)
        appPreference = AppPreference(requireContext())
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchResultTextView = view.findViewById(R.id.search_result_allWorkouts)
        searchResultView = view.findViewById(R.id.view_search_result)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        progressDialog.show()
        getWorkoutList()
    }

    private fun filterWorkouts(query: String) {
        val filteredList = if (query.isEmpty()) workoutList
        else workoutList.filter { it.title.contains(query, ignoreCase = true) }

        adapter.updateList(filteredList)
        // Show/hide search result and update text
        if (query.isNotEmpty()) {
            searchResultTextView.isVisible = true
            searchResultView.isVisible = true
            searchResultTextView.text = "Search Result: ${filteredList.size}"
        } else {
            searchResultTextView.isVisible = false
            searchResultView.isVisible = false
        }
    }

    private fun openAddWorkoutFragment(workout: WorkoutList) {
        val fragment = AddWorkoutFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null) // Allows back navigation
            .commit()
    }

    private fun getWorkoutList() {
        val userId = appPreference.getUserId().toString()
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjc4NTFmZTQ3MTZkOGQ0ZmQyZDhkNzEzIiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiIiLCJsYXN0TmFtZSI6IiIsImRldmljZUlkIjoiVFAxQS4yMjA5MDUuMDAxIiwibWF4RGV2aWNlUmVhY2hlZCI6ZmFsc2UsInR5cGUiOiJhY2Nlc3MtdG9rZW4ifSwiaWF0IjoxNzM3MDE1Nzk5LCJleHAiOjE3NTI3NDA1OTl9.kNSe36d1dnlPrho7rxs7wKpAR6wwa9ToTguSJtifkmU"
        val call = ApiClient.apiService.getWorkoutList(token)
        call.enqueue(object : Callback<WorkoutResponseModel> {
            override fun onResponse(call: Call<WorkoutResponseModel>, response: Response<WorkoutResponseModel>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    val workoutLists = response.body()?.data ?: emptyList()
                    workoutList.addAll(workoutLists)
                    // Pass click listener to adapter
                    adapter = WorkoutAdapter(context!!, workoutList) { selectedWorkout ->
                        openAddWorkoutFragment(selectedWorkout)
                    }
                    recyclerView.adapter = adapter

                    workoutViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
                        filterWorkouts(query)
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<WorkoutResponseModel>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }
}
