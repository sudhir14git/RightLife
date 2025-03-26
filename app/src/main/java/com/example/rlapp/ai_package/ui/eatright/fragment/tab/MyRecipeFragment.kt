package com.example.rlapp.ai_package.ui.eatright.fragment.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.adapter.tab.RecipeAdapter
import com.example.rlapp.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.example.rlapp.ai_package.ui.eatright.fragment.tab.createmeal.CreateRecipeFragment
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.databinding.FragmentMyRecipeBinding
import com.google.android.material.snackbar.Snackbar

class MyRecipeFragment : BaseFragment<FragmentMyRecipeBinding>() {

    private lateinit var progressBarConfirmation :ProgressBar
    private lateinit var recipeRecyclerView : RecyclerView
    private lateinit var imageCalender : ImageView
    private lateinit var addRecipeLayout : LinearLayoutCompat
    private lateinit var layoutNoRecipe : LinearLayoutCompat

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyRecipeBinding
        get() = FragmentMyRecipeBinding::inflate
    var snackbar: Snackbar? = null

    private val recipeAdapter by lazy { RecipeAdapter(requireContext(), arrayListOf(), -1, null,
        false, :: onMealDeleteItem, :: onMealLogItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        recipeRecyclerView = view.findViewById(R.id.recyclerview_recipe_item)
        addRecipeLayout = view.findViewById(R.id.layout_add_recipe)
        layoutNoRecipe = view.findViewById(R.id.layout_no_recipe)

        recipeRecyclerView.layoutManager = LinearLayoutManager(context)
        recipeRecyclerView.adapter = recipeAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = YourMealLogsFragment()
                val args = Bundle()

                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })

        onMyMealItemRefresh()

        addRecipeLayout.setOnClickListener {
            val fragment = CreateRecipeFragment()
            val args = Bundle()

            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun onMyMealItemRefresh (){

        val meal = listOf(
            MyMealModel("Tare Poha", "Poha, Sev", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Tare Poha", "Poha, Sev", "1", "1,157", "8", "308", "17", false)
        )

        if (meal.size > 0){
            recipeRecyclerView.visibility = View.VISIBLE
          //  layoutBottomCreateMeal.visibility = View.VISIBLE
            layoutNoRecipe.visibility = View.GONE
        }else{
            layoutNoRecipe.visibility = View.VISIBLE
            recipeRecyclerView.visibility = View.GONE
          //  layoutBottomCreateMeal.visibility = View.GONE
        }

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        recipeAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealDeleteItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MyMealModel("Tare Poha", "Poha, Sev", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Tare Poha", "Poha, Sev", "1", "1,157", "8", "308", "17",false)
        )

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        recipeAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onMealLogItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MyMealModel("Tare Poha", "Poha, Sev", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Tare Poha", "Poha, Sev", "1", "1,157", "8", "308", "17",false)
        )
        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        recipeAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }
}