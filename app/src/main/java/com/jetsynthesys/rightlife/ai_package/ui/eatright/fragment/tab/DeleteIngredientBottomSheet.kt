package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import android.R.color.transparent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.CreateMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.CreateRecipeFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.IngredientLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapDishLocalListModel

class DeleteIngredientBottomSheet : BottomSheetDialogFragment() {

    private var recipeId : String = ""
    private var recipeName : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        deleteTitle.text = "Delete Ingredient"
        deleteConfirmTv.text = "Are you sure you want to delete this ingredient entry?"

        val ingredientName = arguments?.getString("ingredientName").toString()
        recipeId = arguments?.getString("recipeId").toString()
        recipeName = arguments?.getString("recipeName").toString()

        val ingredientLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("ingredientLocalListModel", IngredientLocalListModel::class.java)
        } else {
            arguments?.getParcelable("ingredientLocalListModel")
        }

        layoutDelete.setOnClickListener {
            if (ingredientLocalListModels != null) {
                if (ingredientLocalListModels.data.size > 0) {
                    for (item in ingredientLocalListModels.data) {
                        if (item.ingredient_name.contentEquals(ingredientName)) {
                            ingredientLocalListModels.data.remove(item)
                            dismiss()
                            Toast.makeText(view.context, "Ingredient Removed", Toast.LENGTH_SHORT).show()
                            val fragment = CreateRecipeFragment()
                            val args = Bundle()
                            args.putString("recipeId", recipeId)
                            args.putString("recipeName", recipeName)
                            args.putParcelable("ingredientLocalListModel", ingredientLocalListModels)
                            fragment.arguments = args
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                replace(R.id.flFragment, fragment, "mealLog")
                                addToBackStack("mealLog")
                                commit()
                            }
                            break
                        }
                    }
                }
            }
        }

        layoutCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = DeleteIngredientBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }
}

