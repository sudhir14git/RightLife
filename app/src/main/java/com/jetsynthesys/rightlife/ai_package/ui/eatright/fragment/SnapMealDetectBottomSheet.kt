package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.R.color.transparent
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SnapMealDetectBottomSheet : BottomSheetDialogFragment() {

    private var listener: SnapMealDetectListener? = null
    private var context: Context? = null
    private var mealId : String = ""
    private var mealName : String = ""
    private var snapImageUrl: String = ""
    private var mealType : String = ""
    private var snapMealLog : String = ""
    private var homeTab : String = ""
    private var selectedMealDate : String = ""

    interface SnapMealDetectListener {
        fun onSnapMealDetect(notDetect: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
        listener = when {
            context is SnapMealDetectListener -> context
            parentFragment is SnapMealDetectListener -> parentFragment as SnapMealDetectListener
            else -> throw ClassCastException("$context must implement SnapMealDetectListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_snap_meal_detect_bottom_sheet, container, false)
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

   //     val snapRecipeName = arguments?.getString("snapRecipeName").toString()
//        mealId = arguments?.getString("mealId").toString()
//        mealName = arguments?.getString("mealName").toString()
//        snapImageUrl = arguments?.getString("snapImageUrl").toString()
//        mealType = arguments?.getString("mealType").toString()
//        snapMealLog = arguments?.getString("snapMealLog").toString()
//        homeTab = arguments?.getString("homeTab").toString()
//        selectedMealDate = arguments?.getString("selectedMealDate").toString()
//        val imagePathString = arguments?.getString("ImagePathsecound")
//        if (imagePathString != null){
//            currentPhotoPathsecound = imagePathString?.let { Uri.parse(it) }!!
//        }else{
//            currentPhotoPathsecound = null
//        }

//        val recipeDetailsLocalListModel = if (Build.VERSION.SDK_INT >= 33) {
//            arguments?.getParcelable("snapDishLocalListModel", RecipeDetailsLocalListModel::class.java)
//        } else {
//            arguments?.getParcelable("snapDishLocalListModel")
//        }

        layoutDelete.setOnClickListener {
            dismiss()
            listener?.onSnapMealDetect(true)
        }

        closeIcon.setOnClickListener {
            dismiss()
        }

        layoutCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = SnapMealDetectBottomSheet().apply {
            arguments = Bundle().apply {
                // Add any required arguments here
            }
        }
    }
}

