package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.MyRecipeAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.CreateRecipeFragment
import com.jetsynthesys.rightlife.databinding.FragmentMyRecipeBinding
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientDetail
import com.jetsynthesys.rightlife.ai_package.model.response.MyRecipe
import com.jetsynthesys.rightlife.ai_package.model.response.MyRecipeResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.IngredientLocalListModel
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyRecipeFragment : BaseFragment<FragmentMyRecipeBinding>() , DeleteRecipeBottomSheet.OnRecipeDeletedListener {

    private lateinit var addRecipeLayout : LinearLayoutCompat
    private lateinit var recipeRecyclerView : RecyclerView
    private lateinit var addRecipeEmptyLayout : LinearLayoutCompat
    private lateinit var layoutNoRecipe : LinearLayoutCompat
    private lateinit var yourRecipesLayout : ConstraintLayout
    private lateinit var mealType : String
    private var ingredientLocalListModel : IngredientLocalListModel? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyRecipeBinding
        get() = FragmentMyRecipeBinding::inflate

    private val recipeAdapter by lazy { MyRecipeAdapter(requireContext(), arrayListOf(), -1, null,
        false, :: onDeleteRecipeItem, :: onEditRecipeItem, :: onLogRecipeItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))
        recipeRecyclerView = view.findViewById(R.id.recyclerview_recipe_item)
        addRecipeEmptyLayout = view.findViewById(R.id.layout_add_recipe)
        layoutNoRecipe = view.findViewById(R.id.layout_no_recipe)
        yourRecipesLayout = view.findViewById(R.id.yourRecipesLayout)
        addRecipeLayout = view.findViewById(R.id.addRecipeLayout)

        mealType = arguments?.getString("mealType").toString()
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

        getRecipeList()

        addRecipeEmptyLayout.setOnClickListener {
            val fragment = CreateRecipeFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

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

    private fun onMyRecipeLists(myRecipeList: List<MyRecipe>) {

        activity?.runOnUiThread {
            if (myRecipeList.isNotEmpty()){
                recipeRecyclerView.visibility = View.VISIBLE
                //  layoutBottomCreateMeal.visibility = View.VISIBLE
                layoutNoRecipe.visibility = View.GONE
                yourRecipesLayout.visibility = View.VISIBLE
            }else{
                layoutNoRecipe.visibility = View.VISIBLE
                recipeRecyclerView.visibility = View.GONE
                //  layoutBottomCreateMeal.visibility = View.GONE
                yourRecipesLayout.visibility = View.GONE
            }
            val valueLists : ArrayList<MyRecipe> = ArrayList()
            valueLists.addAll(myRecipeList as Collection<MyRecipe>)
            val mealLogDateData: MyRecipe? = null
            recipeAdapter.addAll(valueLists, -1, mealLogDateData, false)
        }
    }

    private fun onDeleteRecipeItem(myRecipe : MyRecipe, position: Int, isRefresh: Boolean) {
        val deleteRecipeBottomSheet = DeleteRecipeBottomSheet()
        deleteRecipeBottomSheet.isCancelable = true
        val bundle = Bundle()
        bundle.putString("recipeId", myRecipe._id)
        bundle.putString("deleteType", "MyRecipe")
        deleteRecipeBottomSheet.arguments = bundle
        parentFragment.let { deleteRecipeBottomSheet.show(childFragmentManager, "DeleteRecipeBottomSheet") }
    }

    private fun onEditRecipeItem(myRecipe: MyRecipe, position: Int, isRefresh: Boolean) {
        if (myRecipe != null){
            val ingredientList = myRecipe.ingredients_per_serving
            val ingredientLists : ArrayList<IngredientDetail> = ArrayList()
            ingredientList?.forEach { foodData ->
                val ingredientData = IngredientDetail(
                    id = foodData.ingredient_id,
                    ingredient_code = "",
                    ingredient_name = foodData.ingredient_name,
                    ingredient_category = foodData.ingredient_category,
                    photo_url = foodData.photo_url,
                    calories = foodData.calories,
                    carbs = foodData.carbs,
                    sugar = foodData.sugar,
                    fiber = foodData.fiber,
                    protein = foodData.protein,
                    fat = foodData.fat,
                    cholesterol = foodData.calories,
                    vitamin_a = foodData.vitamin_a,
                    vitamin_c = foodData.vitamin_c,
                    vitamin_k = foodData.vitamin_k,
                    vitamin_d = foodData.vitamin_d,
                    folate = foodData.folate,
                    iron = foodData.iron,
                    calcium = foodData.calcium,
                    magnesium = foodData.magnesium,
                    sodium = foodData.sodium,
                    potassium = foodData.potassium,
                    zinc = foodData.zinc,
                    quantity =  foodData.quantity,
                    unit = foodData.measure
                )
                ingredientLists.add(ingredientData)
            }
            ingredientLocalListModel = IngredientLocalListModel(ingredientLists)
            val fragment = CreateRecipeFragment()
            val args = Bundle()
            args.putString("recipeId", myRecipe._id)
            args.putString("recipeName", myRecipe.recipe_name)
            args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun onLogRecipeItem(myRecipe: MyRecipe, position: Int, isRefresh: Boolean) {

//        val valueLists : ArrayList<MyRecipe> = ArrayList()
//        valueLists.addAll(mealLogs as Collection<MyRecipe>)
//        recipeAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun getRecipeList() {
        // LoaderUtil.showLoader(requireActivity())
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApi.getMyRecipeList("0", userId)
        call.enqueue(object : Callback<MyRecipeResponse> {
            override fun onResponse(call: Call<MyRecipeResponse>, response: Response<MyRecipeResponse>) {
                if (response.isSuccessful) {
//                    LoaderUtil.dismissLoader(requireActivity())
                    if (response.body() != null){
                        val myRecipeList = response.body()!!.data
                        onMyRecipeLists(myRecipeList)
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    //  LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<MyRecipeResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                //  LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }

    override fun onRecipeDeleted(recipeData: String) {
        getRecipeList()
    }
}