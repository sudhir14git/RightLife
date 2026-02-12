package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.R.color.transparent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel

class DeleteSnapMealBottomSheet : BottomSheetDialogFragment() {

    private var currentPhotoPathsecound : Uri? = null
    private var mealId : String = ""
    private var mealName : String = ""
    private var snapImageUrl: String = ""
    private  var descriptionName: String = ""
    private var mealType : String = ""
    private var snapMealLog : String = ""
    private var homeTab : String = ""
    private var selectedMealDate : String = ""
    private var currentToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delete_snap_meal_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_meal_scan_results)
        dialog.window?.setBackgroundDrawableResource(transparent)
        val closeIcon = view.findViewById<ImageView>(R.id.closeIc)
        val layoutCancel = view.findViewById<LinearLayoutCompat>(R.id.layoutCancel)
        val layoutDelete = view.findViewById<LinearLayoutCompat>(R.id.layoutDelete)

        val snapRecipeName = arguments?.getString("snapRecipeName").toString()
        mealId = arguments?.getString("mealId").toString()
        mealName = arguments?.getString("mealName").toString()
        snapImageUrl = arguments?.getString("snapImageUrl").toString()
        mealType = arguments?.getString("mealType").toString()
        snapMealLog = arguments?.getString("snapMealLog").toString()
        homeTab = arguments?.getString("homeTab").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
        descriptionName = arguments?.getString("description").toString()
        val imagePathString = arguments?.getString("ImagePathsecound")
        if (imagePathString != null){
            currentPhotoPathsecound = imagePathString?.let { Uri.parse(it) }!!
        }else{
            currentPhotoPathsecound = null
        }

        val recipeDetailsLocalListModel = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", RecipeDetailsLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        layoutDelete.setOnClickListener {
            if (recipeDetailsLocalListModel != null) {
                if (recipeDetailsLocalListModel.data.size > 0) {
                    for (item in recipeDetailsLocalListModel.data) {
                        if (item.food_name.contentEquals(snapRecipeName)) {
                            recipeDetailsLocalListModel.data.remove(item)
                            dismiss()
                            val ctx = context ?: return@setOnClickListener
                            showCustomToast(ctx, "Dish Removed")
                          //  Toast.makeText(view.context, "Dish Removed", Toast.LENGTH_SHORT).show()
                            val fragment = MealScanResultFragment()
                            val args = Bundle()
                            args.putString("ModuleName", arguments?.getString("ModuleName").toString())
                            args.putString("mealId", mealId)
                            args.putString("mealName", mealName)
                            args.putString("snapImageUrl", snapImageUrl)
                            args.putString("description", descriptionName)
                            args.putString("mealType", mealType)
                            args.putString("snapMealLog", snapMealLog)
                            args.putString("homeTab", homeTab)
                            args.putString("selectedMealDate", selectedMealDate)
                            args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
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

        closeIcon.setOnClickListener {
            dismiss()
        }

        layoutCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun showCustomToast(context: Context, message: String?) {
        // Cancel any old toast
        currentToast?.cancel()
        val inflater = LayoutInflater.from(context)
        val toastLayout = if (message.equals("Please add atleast one dish to log the meal.")){
            inflater.inflate(R.layout.custom_toast_ai_sleep, null)
        }else{
            inflater.inflate(R.layout.custom_toast_ai_sleep, null)
        }

        val textView = toastLayout.findViewById<TextView>(R.id.toast_message)
        textView.text = message
        // ✅ Wrap layout inside FrameLayout to apply margins
        val container = FrameLayout(context)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val marginInPx = (20 * context.resources.displayMetrics.density).toInt()
        params.setMargins(marginInPx, 0, marginInPx, 0)
        toastLayout.layoutParams = params
        container.addView(toastLayout)
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = container
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 100)
        currentToast = toast
        toast.show()
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = DeleteSnapMealBottomSheet().apply {
            arguments = Bundle().apply {
                // Add any required arguments here
            }
        }
    }
}

