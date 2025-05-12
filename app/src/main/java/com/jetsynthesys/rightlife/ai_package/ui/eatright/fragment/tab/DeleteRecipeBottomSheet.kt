package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import android.R.color.transparent
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.utils.LoaderUtil
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DeleteRecipeBottomSheet : BottomSheetDialogFragment() {

    private var listener: OnRecipeDeletedListener? = null
    private var recipeId : String = ""
    private var recipeName : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    interface OnRecipeDeletedListener {
        fun onRecipeDeleted(recipeData: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            context is OnRecipeDeletedListener -> context
            parentFragment is OnRecipeDeletedListener -> parentFragment as OnRecipeDeletedListener
            else -> throw ClassCastException("$context must implement OnRecipeDeletedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delete_meal_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_meal_scan_results)
        dialog.window?.setBackgroundDrawableResource(transparent)
        val deleteTitle = view.findViewById<TextView>(R.id.deleteTitle)
        val deleteConfirmTv = view.findViewById<TextView>(R.id.deleteConfirmTv)
        val layoutCancel = view.findViewById<LinearLayoutCompat>(R.id.noBtn)
        val layoutDelete = view.findViewById<LinearLayoutCompat>(R.id.yesBtn)
        deleteTitle.text = "Delete Recipe"
        deleteConfirmTv.text = "Are you sure you want to delete this recipe entry?"

        recipeId = arguments?.getString("recipeId").toString()
        recipeName = arguments?.getString("recipeName").toString()
        val deleteType = arguments?.getString("deleteType").toString()

        layoutDelete.setOnClickListener {
            if (deleteType.contentEquals("MyRecipe")){
                deleteRecipe(recipeId)
            }
        }

        layoutCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun deleteRecipe(recipeId: String) {
        LoaderUtil.showLoader(requireActivity())
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val call = ApiClient.apiServiceFastApi.deleteMyRecipe(recipeId, userId)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    LoaderUtil.dismissLoader(requireActivity())
                    val mealData = response.body()?.message
                    Toast.makeText(context, mealData, Toast.LENGTH_SHORT).show()
                    listener?.onRecipeDeleted("")
                    dismiss()
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                    LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<MealUpdateResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = DeleteRecipeBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}

