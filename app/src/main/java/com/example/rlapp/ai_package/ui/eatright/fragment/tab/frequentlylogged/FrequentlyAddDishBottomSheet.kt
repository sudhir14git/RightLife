package com.example.rlapp.ai_package.ui.eatright.fragment.tab.frequentlylogged

import android.R.color.transparent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import com.example.rlapp.R
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FrequentlyAddDishBottomSheet : BottomSheetDialogFragment() {

    private lateinit var flexboxLayout: FlexboxLayout
    private val ingredientsList = mutableListOf("Dal", "Rice", "Roti", "Spinach")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_frequently_add_meal_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_frequently_logged)
        dialog.window?.setBackgroundDrawableResource(transparent)
        flexboxLayout = view.findViewById(R.id.flexboxLayout)
        val btnAdd: LinearLayoutCompat = view.findViewById(R.id.layout_btnAdd)
        val btnLogMeal: LinearLayoutCompat = view.findViewById(R.id.layout_btnLogMeal)
        val layoutTitle = view.findViewById<LinearLayout>(R.id.layout_title)
        val checkCircle = view.findViewById<ImageView>(R.id.check_circle_icon)
        val loggedSuccess = view.findViewById<TextView>(R.id.tv_logged_success)
        flexboxLayout.visibility = View.VISIBLE
        layoutTitle.visibility = View.VISIBLE
        btnLogMeal.visibility = View.VISIBLE
        // Display default ingredients
        updateIngredientChips()

        // Add button clicked (For demonstration, adding a dummy ingredient)
        btnAdd.setOnClickListener {
            val newIngredient = "New Item ${ingredientsList.size + 1}"
            ingredientsList.add(newIngredient)
            updateIngredientChips()
        }

        // Log Meal button click
        btnLogMeal.setOnClickListener {
         //   Toast.makeText(context, "Meal Logged Successfully!", Toast.LENGTH_SHORT).show()
            flexboxLayout.visibility = View.GONE
            layoutTitle.visibility = View.GONE
            btnLogMeal.visibility = View.GONE
            checkCircle.visibility = View.VISIBLE
            loggedSuccess.visibility = View.VISIBLE
        }
    }

    // Function to update Flexbox with chips
    private fun updateIngredientChips() {
        flexboxLayout.removeAllViews() // Clear existing chips

        for (ingredient in ingredientsList) {
            val chipView = LayoutInflater.from(context).inflate(R.layout.chip_ingredient, flexboxLayout, false)
            val tvIngredient: TextView = chipView.findViewById(R.id.tvIngredient)
            val btnRemove: ImageView = chipView.findViewById(R.id.btnRemove)

            tvIngredient.text = ingredient
            btnRemove.setOnClickListener {
                ingredientsList.remove(ingredient)
                updateIngredientChips()
            }

            flexboxLayout.addView(chipView)
        }
    }


    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = FrequentlyAddDishBottomSheet().apply {
            arguments = Bundle().apply {
                // Add any required arguments here
            }
        }
    }
}

