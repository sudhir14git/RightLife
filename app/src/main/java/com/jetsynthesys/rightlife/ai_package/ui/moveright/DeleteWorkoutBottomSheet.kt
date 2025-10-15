package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog

class DeleteWorkoutBottomSheet : BottomSheetDialogFragment() {

    private var calorieId: String? = null
    private var userId: String? = null
    private var onDeleteSuccess: (() -> Unit)? = null
    private var onDeleteSuccessWithRefresh: (() -> Unit)? = null // New callback for refresh

    companion object {
        const val ARG_CALORIE_ID = "calorie_id"
        const val ARG_USER_ID = "user_id"

        fun newInstance(calorieId: String, userId: String): DeleteWorkoutBottomSheet {
            return DeleteWorkoutBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_CALORIE_ID, calorieId)
                    putString(ARG_USER_ID, userId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            calorieId = it.getString(ARG_CALORIE_ID)
            userId = it.getString(ARG_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setStyle(STYLE_NORMAL, R.style.LoggedBottomSheetDialogTheme)
        return inflater.inflate(R.layout.fragment_edit_workout_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val yesButton = view.findViewById<LinearLayoutCompat>(R.id.yes_btn_bottom_sheet)
        val noButton = view.findViewById<LinearLayoutCompat>(R.id.layout_btn_log_meal)

        yesButton.setOnClickListener {
            calorieId?.let { id ->
                userId?.let { uid ->
                    deleteCalorieRecord(id, uid)
                } ?: run {
                    Toast.makeText(context, "User ID is missing", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            } ?: run {
                Toast.makeText(context, "Calorie ID is missing", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        noButton.setOnClickListener {
            dismiss()
        }
    }

    fun setOnDeleteSuccessListener(listener: () -> Unit) {
        this.onDeleteSuccess = listener
    }

    // New method to set refresh callback
    fun setOnDeleteSuccessWithRefreshListener(listener: () -> Unit) {
        this.onDeleteSuccessWithRefresh = listener
    }

    private fun deleteCalorieRecord(calorieId: String, userId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiServiceFastApi.deleteCalorie(
                    calorieId = calorieId,
                    userId = userId
                )

                if (response.isSuccessful) {
                    val deleteResponse = response.body()
                    withContext(Dispatchers.Main) {
                        if (deleteResponse != null) {
                            Toast.makeText(context, "Workout deleted: ${deleteResponse.message}", Toast.LENGTH_SHORT).show()

                            // Call both callbacks
                            onDeleteSuccess?.invoke()
                            onDeleteSuccessWithRefresh?.invoke() // This will refresh the fragment
                        } else {
                            Toast.makeText(context, "Deletion failed: Empty response", Toast.LENGTH_SHORT).show()
                        }
                        dismiss()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${response.code()} - $errorBody", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }
}