package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.R.color.transparent
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.RecipeDetailsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectMealTypeEatLandingBottomSheet : BottomSheetDialogFragment() {

    private lateinit var layoutBreakfast : LinearLayoutCompat
    private lateinit var layoutMorningSnack : LinearLayoutCompat
    private lateinit var layoutLunch : LinearLayoutCompat
    private lateinit var layoutEveningSnacks : LinearLayoutCompat
    private lateinit var layoutDinner : LinearLayoutCompat

    private var listener: OnOtherRecipeLogListener? = null
    private var loadingOverlay : FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    interface OnOtherRecipeLogListener {
        fun onOtherRecipeLog(mealData: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            context is OnOtherRecipeLogListener -> context
            parentFragment is OnOtherRecipeLogListener -> parentFragment as OnOtherRecipeLogListener
            else -> throw ClassCastException("$context must implement OnOtherRecipeLogListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_meal_type_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_frequently_logged)
        dialog.window?.setBackgroundDrawableResource(transparent)
        layoutBreakfast = view.findViewById(R.id.layoutBreakfast)
        layoutMorningSnack = view.findViewById(R.id.layoutMorningSnack)
        layoutLunch = view.findViewById(R.id.layoutLunch)
        layoutEveningSnacks = view.findViewById(R.id.layoutEveningSnacks)
        layoutDinner = view.findViewById(R.id.layoutDinner)

        val items = arrayOf("Breakfast", "Morning Snack", "Lunch", "Evening Snacks", "Dinner")
        val recipeId = arguments?.getString("recipeId").toString()

        layoutBreakfast.setOnClickListener {
            getRecipesDetails("breakfast", recipeId)
        }

        layoutMorningSnack.setOnClickListener {
            getRecipesDetails("morning_snack", recipeId)
        }

        layoutLunch.setOnClickListener {
            getRecipesDetails("lunch", recipeId)
        }

        layoutEveningSnacks.setOnClickListener {
            getRecipesDetails("evening_snack", recipeId)
        }

        layoutDinner.setOnClickListener {
            getRecipesDetails("dinner", recipeId)
        }
    }

    private fun getRecipesDetails(mealType: String, recipeId: String) {
        // ✅ show loader safely
        if (isAdded && view != null) {
            activity?.runOnUiThread {
                view?.let { showLoader(it) }
            }
        }
        val call = ApiClient.apiServiceFastApiV2.getRecipeDetailsById(recipeId = recipeId)
        call.enqueue(object : Callback<RecipeDetailsResponse> {
            override fun onResponse(
                call: Call<RecipeDetailsResponse>,
                response: Response<RecipeDetailsResponse>
            ) {
                // fragment gone? stop
                if (!isAdded || activity == null) return
                activity?.runOnUiThread {
                    view?.let { dismissLoader(it) }
                }
                if (response.isSuccessful) {
                    dismiss()
                    val ingredientRecipesDetails = response.body()?.data ?: return
                    // fragment gone? stop (double safety)
                    val act = activity ?: return
                    val fm = act.supportFragmentManager
                    val snapMealFragment = OtherRecipeLogFragment()
                    val args = Bundle().apply {
                        putString("ModuleName", "EatRightLanding")
                        putString("searchType", "SearchDish")
                        putString("mealId", ingredientRecipesDetails.id)
                        putString("mealName", ingredientRecipesDetails.recipe)
                        putString("snapImageUrl", ingredientRecipesDetails.photo_url)
                        putString("mealType", mealType)
                        putParcelable("ingredientRecipeDetails", ingredientRecipesDetails)
                    }
                    snapMealFragment.arguments = args
                    fm.beginTransaction()
                        .replace(R.id.flFragment, snapMealFragment, "Steps")
                        .addToBackStack(null)
                        .commitAllowingStateLoss() // ✅ prevents crash
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<RecipeDetailsResponse>, t: Throwable) {
                if (!isAdded || activity == null) return
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                activity?.runOnUiThread {
                    view?.let { dismissLoader(it) }
                }
            }
        })
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = SelectMealTypeEatLandingBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}

