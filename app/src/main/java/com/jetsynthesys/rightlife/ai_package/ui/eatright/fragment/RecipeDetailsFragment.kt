package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MacroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeResponseNew
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SelectedMealLogList
import com.jetsynthesys.rightlife.databinding.FragmentRecipeDetailsBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class RecipeDetailsFragment  : BaseFragment<FragmentRecipeDetailsBinding>() {

    private lateinit var macroItemRecyclerView : RecyclerView
    private lateinit var addToTheMealLayout : LinearLayoutCompat
    private lateinit var layoutMacroTitle : ConstraintLayout
    private lateinit var icMacroUP : ImageView
    private lateinit var microUP : ImageView
    private lateinit var imgFood : ImageView
    private lateinit var tvMealName : TextView
    private lateinit var like_text : TextView
    private lateinit var time_text : TextView
    private lateinit var calorie_value : TextView
    private lateinit var carbs_value : TextView
    private lateinit var protein_value : TextView
    private lateinit var fat_value : TextView
    private lateinit var serves_text : TextView
    private lateinit var ingredients_description : TextView
    private lateinit var steps_description : TextView
    private lateinit var addToTheMealTV : TextView
    private lateinit var searchType : String
    private lateinit var quantityEdit: EditText
    private lateinit var tvCheckOutRecipe: TextView
    private lateinit var tvChange: TextView
    private lateinit var tvMeasure : TextView
    private lateinit var recipeDescription : TextView
    private var mealLogRequests : SelectedMealLogList? = null
    private var snapMealLogRequests : SelectedMealLogList? = null
    private lateinit var mealType : String
    private  var recipeList :SnapRecipeList? = null
    private lateinit var backButton: ImageView

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRecipeDetailsBinding
        get() = FragmentRecipeDetailsBinding::inflate

    private val macroNutrientsAdapter by lazy { MacroNutrientsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMacroNutrientsItemClick) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        recipeList = arguments?.getParcelable("snapRecipeList")
        getSnapMealRecipesList()
        macroItemRecyclerView = view.findViewById(R.id.recyclerview_macro_item)
        addToTheMealLayout = view.findViewById(R.id.layout_addToTheMeal)
        tvCheckOutRecipe = view.findViewById(R.id.tv_CheckOutRecipe)
        tvChange = view.findViewById(R.id.tv_change)
        tvMeasure = view.findViewById(R.id.tvMeasure)
        addToTheMealTV = view.findViewById(R.id.tv_addToTheMeal)
        tvMealName = view.findViewById(R.id.tvMealName)
        like_text = view.findViewById(R.id.like_text)
        time_text = view.findViewById(R.id.time_text)
        serves_text = view.findViewById(R.id.serves_text)
        calorie_value = view.findViewById(R.id.calorie_value)
        carbs_value = view.findViewById(R.id.carbs_value)
        protein_value = view.findViewById(R.id.protein_value)
        fat_value = view.findViewById(R.id.fat_value)
        imgFood = view.findViewById(R.id.imgFood)
        ingredients_description = view.findViewById(R.id.ingredients_description)
        steps_description = view.findViewById(R.id.steps_description)
        recipeDescription = view.findViewById(R.id.recipeDescription)
        layoutMacroTitle = view.findViewById(R.id.layoutMacroTitle)
        microUP = view.findViewById(R.id.microUP)
        icMacroUP = view.findViewById(R.id.icMacroUP)
        quantityEdit = view.findViewById(R.id.quantityEdit)
        backButton = view.findViewById(R.id.backButton)

        searchType = arguments?.getString("searchType").toString()
        mealType = arguments?.getString("mealType").toString()


        val selectedMealLogListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("selectedMealLogList", SelectedMealLogList::class.java)
        } else {
            arguments?.getParcelable("selectedMealLogList")
        }

        val selectedSnapMealLogListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("selectedSnapMealLogList", SelectedMealLogList::class.java)
        } else {
            arguments?.getParcelable("selectedSnapMealLogList")
        }

        if (selectedMealLogListModels != null){
            mealLogRequests = selectedMealLogListModels
        }

        if (selectedSnapMealLogListModels != null){
            snapMealLogRequests = selectedSnapMealLogListModels
        }

        icMacroUP.setImageResource(R.drawable.ic_down)
        view.findViewById<View>(R.id.view_macro).visibility = View.GONE
        view.findViewById<ConstraintLayout>(R.id.calorie_layout).visibility = View.GONE
        view.findViewById<ConstraintLayout>(R.id.carbs_layout).visibility = View.GONE
        view.findViewById<ConstraintLayout>(R.id.protein_layout).visibility = View.GONE
        view.findViewById<ConstraintLayout>(R.id.fat_layout).visibility = View.GONE

        layoutMacroTitle.setOnClickListener {
            if (macroItemRecyclerView.isVisible){
                macroItemRecyclerView.visibility = View.GONE
                icMacroUP.setImageResource(R.drawable.ic_down)
                view.findViewById<View>(R.id.view_macro).visibility = View.GONE
                view.findViewById<ConstraintLayout>(R.id.calorie_layout).visibility = View.GONE
                view.findViewById<ConstraintLayout>(R.id.carbs_layout).visibility = View.GONE
                view.findViewById<ConstraintLayout>(R.id.protein_layout).visibility = View.GONE
                view.findViewById<ConstraintLayout>(R.id.fat_layout).visibility = View.GONE
            }else{
                macroItemRecyclerView.visibility = View.VISIBLE
                icMacroUP.setImageResource(R.drawable.ic_up)
                view.findViewById<View>(R.id.view_macro).visibility = View.VISIBLE
                view.findViewById<ConstraintLayout>(R.id.calorie_layout).visibility = View.VISIBLE
                view.findViewById<ConstraintLayout>(R.id.carbs_layout).visibility = View.VISIBLE
                view.findViewById<ConstraintLayout>(R.id.protein_layout).visibility = View.VISIBLE
                view.findViewById<ConstraintLayout>(R.id.fat_layout).visibility = View.VISIBLE
            }
        }

        macroItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        macroItemRecyclerView.adapter = macroNutrientsAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchType.contentEquals("EatRight")){
                    val fragment = RecipesSearchFragment()
                    val args = Bundle()
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }else{
                    val fragment = RecipesSearchFragment()
                    val args = Bundle()
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            }
        })

        backButton.setOnClickListener {
            val fragment = RecipesSearchFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }
    }

    private fun loadImageFromGoogleDrive(imageUrl: String?, imgFood: ImageView) {
        if (imageUrl.isNullOrEmpty()) {
            // If the URL is null or empty, load the error image
            Glide.with(imgFood.context)
                .load(R.drawable.ic_view_meal_place)
                .into(imgFood)
            return
        }
        // Extract the FILE_ID from the URL
        val fileId = imageUrl.substringAfter("/d/").substringBefore("/view")
        val directImageUrl = "https://drive.google.com/uc?export=download&id=$fileId"
        // First, try loading directly with Glide
        Glide.with(imgFood.context)
            .load(directImageUrl)
            .placeholder(R.drawable.ic_view_meal_place)
            .error(R.drawable.ic_view_meal_place)
            .into(imgFood)
            .waitForLayout() // Ensure the image view is ready
        // If the direct load fails, download the image manually
        Thread {
            try {
                val client = OkHttpClient.Builder()
                    .followRedirects(true) // Handle redirects
                    .build()

                val request = Request.Builder()
                    .url(directImageUrl)
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    // Load the bitmap into Glide on the main thread
                    imgFood.post {
                        Glide.with(imgFood.context)
                            .load(bitmap)
                            .placeholder(R.drawable.ic_view_meal_place)
                            .error(R.drawable.ic_view_meal_place)
                            .into(imgFood)
                    }
                    inputStream?.close()
                } else {
                    imgFood.post {
                        Glide.with(imgFood.context)
                            .load(R.drawable.ic_view_meal_place)
                            .into(imgFood)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                imgFood.post {
                    Glide.with(imgFood.context)
                        .load(R.drawable.ic_view_meal_place)
                        .into(imgFood)
                }
            }
        }.start()
    }

    private fun getSnapMealRecipesList() {
        val call = ApiClient.apiServiceFastApi.getSnapMealRecipeById(recipeId = recipeList?.id.toString())
        call.enqueue(object : Callback<RecipeResponseNew> {
            override fun onResponse(call: Call<RecipeResponseNew>, response: Response<RecipeResponseNew>) {
                if (response.isSuccessful) {
                    val ingredientsList = response.body()?.data?.ingredients.orEmpty()
                    val ingredientsFormatted = ingredientsList.joinToString(separator = "\n") { "• $it" }
                    ingredients_description.text = ingredientsFormatted
                    // Format instructions as a numbered list
                    val instructionsList = response.body()?.data?.instructions.orEmpty()
                    val instructionsFormatted = instructionsList.mapIndexed { index, instruction ->
                        "${index + 1}. ${instruction.replaceFirstChar { it.uppercase() }}"
                    }.joinToString(separator = "\n")
                    steps_description.text = instructionsFormatted
                    serves_text.text = "Serves ${response.body()?.data?.servings.toString()}"
                    tvMealName.text = response.body()?.data?.recipe_name.toString()
                    time_text.text = response.body()?.data?.total_time.toString()
                    calorie_value.text = "${response.body()?.data?.calories} Kcal"
                    carbs_value.text = "${response.body()?.data?.carbs} g"
                    protein_value.text = "${response.body()?.data?.protein} g"
                    fat_value.text = "${response.body()?.data?.fat} g"
                    val imageUrl = response.body()?.data?.photo_url
                    loadImageFromGoogleDrive(imageUrl, imgFood)
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<RecipeResponseNew>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onMacroNutrientsItemClick(macroNutrientsModel: MacroNutrientsModel, position: Int, isRefresh: Boolean) {

    }

    fun getDriveImageUrl(originalUrl: String): String? {
        val regex = Regex("(?<=/d/)(.*?)(?=/|$)")
        val matchResult = regex.find(originalUrl)
        val fileId = matchResult?.value
        return if (!fileId.isNullOrEmpty()) {
            "https://drive.google.com/uc?export=view&id=$fileId"
        } else {
            null
        }
    }
}