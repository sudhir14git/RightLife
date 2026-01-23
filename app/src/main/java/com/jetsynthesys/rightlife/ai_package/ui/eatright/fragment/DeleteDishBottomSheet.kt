package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.R.color.transparent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.CreateMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel

class DeleteDishBottomSheet : BottomSheetDialogFragment() {

    private var mealId : String = ""
    private lateinit var mealType : String
    private var mealName : String = ""
    private var moduleName : String = ""
    private var selectedMealDate : String = ""

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
       // val closeIcon = view.findViewById<ImageView>(R.id.closeIc)
        val layoutCancel = view.findViewById<LinearLayoutCompat>(R.id.noBtn)
        val layoutDelete = view.findViewById<LinearLayoutCompat>(R.id.yesBtn)

        moduleName = arguments?.getString("ModuleName").toString()
        val snapRecipeName = arguments?.getString("snapRecipeName").toString()
        mealId = arguments?.getString("mealId").toString()
        mealType = arguments?.getString("mealType").toString()
        mealName = arguments?.getString("mealName").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()

        val recipeDetailsLocalListModel = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", RecipeDetailsLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        layoutDelete.setOnClickListener {
            if (recipeDetailsLocalListModel != null) {
                if (recipeDetailsLocalListModel.data.size > 0) {
                    for (item in recipeDetailsLocalListModel.data) {
                        if (item.recipe.contentEquals(snapRecipeName)) {
                            recipeDetailsLocalListModel.data.remove(item)
                            dismiss()
                            Toast.makeText(view.context, "Dish Removed", Toast.LENGTH_SHORT).show()
                            val fragment = CreateMealFragment()
                            val args = Bundle()
                            args.putString("ModuleName", moduleName)
                            args.putString("selectedMealDate", selectedMealDate)
                            args.putString("mealId", mealId)
                            args.putString("mealName", mealName)
                            args.putString("mealType", mealType)
                            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
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

//        closeIcon.setOnClickListener {
//            dismiss()
//        }

        layoutCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = DeleteDishBottomSheet().apply {
            arguments = Bundle().apply {
                // Add any required arguments here
            }
        }
    }
}

