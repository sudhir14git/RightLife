package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MacroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsViewResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SelectedMealLogList
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
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
    private lateinit var stepsContainer : LinearLayout
    private lateinit var addToTheMealTV : TextView
    private lateinit var searchType : String
    private lateinit var quantityEdit: EditText
    private lateinit var tvCheckOutRecipe: TextView
    private lateinit var tvChange: TextView
    private lateinit var tvMeasure : TextView
    private lateinit var vegTv : TextView
    private lateinit var foodType : TextView
    private lateinit var vegImage : ImageView
    private lateinit var recipeDescription : TextView
    private var mealLogRequests : SelectedMealLogList? = null
    private var snapMealLogRequests : SelectedMealLogList? = null
    private lateinit var mealType : String
    private  var recipeDetails :IngredientRecipeList? = null
    private var recipeId : String = ""
    private lateinit var backButton: ImageView
    private lateinit var mealTypeImage : ImageView
    private var loadingOverlay : FrameLayout? = null

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
        recipeId = arguments?.getString("recipeId").toString()
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
        stepsContainer = view.findViewById(R.id.stepsContainer)
        recipeDescription = view.findViewById(R.id.recipeDescription)
        layoutMacroTitle = view.findViewById(R.id.layoutMacroTitle)
        microUP = view.findViewById(R.id.microUP)
        icMacroUP = view.findViewById(R.id.icMacroUP)
        quantityEdit = view.findViewById(R.id.quantityEdit)
        backButton = view.findViewById(R.id.backButton)
        foodType = view.findViewById(R.id.foodType)
        vegTv = view.findViewById(R.id.vegTv)
        mealTypeImage = view.findViewById(R.id.mealTypeImage)
        vegImage = view.findViewById(R.id.veg_image)

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

        getRecipesDetails()

        icMacroUP.setImageResource(R.drawable.ic_feather_down)
        view.findViewById<View>(R.id.view_macro).visibility = View.GONE
        view.findViewById<LinearLayoutCompat>(R.id.calorie_layout).visibility = View.GONE
        view.findViewById<LinearLayoutCompat>(R.id.carbs_layout).visibility = View.GONE
        view.findViewById<LinearLayoutCompat>(R.id.protein_layout).visibility = View.GONE
        view.findViewById<LinearLayoutCompat>(R.id.fat_layout).visibility = View.GONE

        layoutMacroTitle.setOnClickListener {
            if (macroItemRecyclerView.isVisible){
                macroItemRecyclerView.visibility = View.GONE
                icMacroUP.setImageResource(R.drawable.ic_feather_down)
                view.findViewById<View>(R.id.view_macro).visibility = View.GONE
                view.findViewById<LinearLayoutCompat>(R.id.calorie_layout).visibility = View.GONE
                view.findViewById<LinearLayoutCompat>(R.id.carbs_layout).visibility = View.GONE
                view.findViewById<LinearLayoutCompat>(R.id.protein_layout).visibility = View.GONE
                view.findViewById<LinearLayoutCompat>(R.id.fat_layout).visibility = View.GONE
            }else{
                macroItemRecyclerView.visibility = View.VISIBLE
                icMacroUP.setImageResource(R.drawable.ic_feather_up)
                view.findViewById<View>(R.id.view_macro).visibility = View.VISIBLE
                view.findViewById<LinearLayoutCompat>(R.id.calorie_layout).visibility = View.VISIBLE
                view.findViewById<LinearLayoutCompat>(R.id.carbs_layout).visibility = View.VISIBLE
                view.findViewById<LinearLayoutCompat>(R.id.protein_layout).visibility = View.VISIBLE
                view.findViewById<LinearLayoutCompat>(R.id.fat_layout).visibility = View.VISIBLE
            }
        }

        macroItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        macroItemRecyclerView.adapter = macroNutrientsAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchType.contentEquals("EatRight")){
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", "EatRight")
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
            if (searchType.contentEquals("EatRight")){
                val fragment = HomeBottomTabFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
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
        var imageUrls : String? = ""
        imageUrls = if (imageUrl.contains("drive.google.com")) {
            getDriveImageUrl(imageUrl)
        }else{
            imageUrl
        }
       // val fileId = imageUrl.substringAfter("/d/").substringBefore("/view")
        val directImageUrl = imageUrls//"https://drive.google.com/uc?export=download&id=$fileId"
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
                    .url(directImageUrl!!)
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

    private fun getRecipesDetails() {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }

        Log.d("RecipeDetails", "getRecipesDetails called → recipeId: $recipeId")
        val call = ApiClient.apiServiceFastApiV2.getDetailsViewRecipeById(recipeId = recipeId)
        Log.d("RecipeDetails", "API call enqueued: getDetailsViewRecipeById for recipeId $recipeId")
        call.enqueue(object : Callback<RecipeDetailsViewResponse> {
            override fun onResponse(call: Call<RecipeDetailsViewResponse>, response: Response<RecipeDetailsViewResponse>) {
                Log.d("RecipeDetails", "onResponse received → isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
                if (response.isSuccessful) {
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val data = response.body()?.data
                    Log.d("RecipeDetails", "Success → recipe data received (null check: ${data != null})")
                    if (data != null) {
                        val ingredientsList = data.ingredients.orEmpty()
                        val ingredientsFormatted = ingredientsList.joinToString(separator = "\n") { "• $it" }
                        ingredients_description.text = ingredientsFormatted
                        Log.d("RecipeDetails", "Ingredients set → count: ${ingredientsList.size}")

                        val instructionsList = data.preparation_notes.orEmpty()
                        val instructionsText = instructionsList.joinToString(separator = "\n") { "- $it" }
                        steps_description.text = instructionsText
                        data.preparation_notes.forEachIndexed { index, text ->
                            val view = layoutInflater.inflate(R.layout.item_step, stepsContainer, false)
                            val tvNumber = view.findViewById<TextView>(R.id.tvStepNumber)
                            val tvText = view.findViewById<TextView>(R.id.tvDescription)
                            val line = view.findViewById<View>(R.id.verticalLine)
                            val arrowIcon = view.findViewById<TextView>(R.id.arrowIcon)
                            tvNumber.text = (index + 1).toString()
                            tvText.text = text
                            // Hide line for last item
                            if (index == data.preparation_notes.size - 1) {
                                line.visibility = View.INVISIBLE
                                arrowIcon.visibility = View.INVISIBLE
                            }
                            stepsContainer.addView(view)
                        }
                        Log.d("RecipeDetails", "Instructions set → count: ${instructionsList.size}")

                        serves_text.text = "${data.default_serving.value.toString() + " "  + data.default_serving.type.toString()}"
                        tvMealName.text = data.recipe.toString()
                        time_text.text = data.active_cooking_time_min.toString()
                        calorie_value.text = "${data.calories_kcal?.toInt().toString()} Kcal"
                        carbs_value.text = "${data.protein_g?.toInt()} g"
                        protein_value.text = "${data.carbs_g?.toInt()} g"
                        fat_value.text = "${data.fat_g?.toInt()} g"
                        Log.d("RecipeDetails", "Nutritional values & name/time set → calories: ${data.calories_kcal}, carbs: ${data.carbs_g}")

                        val foodTypeResult = getFoodType(data.category)
                        if (foodTypeResult == "Veg") {
                            vegImage.visibility = View.VISIBLE
                            vegImage.setImageResource(R.drawable.veg_new)
                            vegTv.text = "Veg"
                            Log.d("RecipeDetails", "Veg indicator set")
                        } else if (foodTypeResult == "Non-Veg") {
                            vegImage.visibility = View.VISIBLE
                            vegImage.setImageResource(R.drawable.non_veg_new)
                            vegTv.text = "Non-Veg"
                            Log.d("RecipeDetails", "Non-Veg indicator set")
                        } else if (foodTypeResult == "Veg & Vegan") {
                            vegImage.visibility = View.VISIBLE
                            vegImage.setImageResource(R.drawable.veg_new)
                            vegTv.text = "Veg & Vegan"
                            Log.d("RecipeDetails", "Veg & Vegan")
                        } else {
                            vegImage.visibility = View.INVISIBLE
                            Log.d("RecipeDetails", "No Veg/Non-Veg indicator (other category)")
                        }

                        foodType.text = data.meal_type
                        val input =  data.meal_type

                        val firstWord = input.split("/", ",").first().trim()
                        Log.d("RecipeDetails", "Meal type text set: ${data.meal_type}")

                        when (firstWord) {
                            "Breakfast" -> {
                                mealTypeImage.setImageResource(R.drawable.ic_breakfast)
                                Log.d("RecipeDetails", "Meal icon set: Breakfast")
                            }
                            "Snack" -> {
                                mealTypeImage.setImageResource(R.drawable.ic_morning_snack)
                                Log.d("RecipeDetails", "Meal icon set: Snack")
                            }
                            "Lunch" -> {
                                mealTypeImage.setImageResource(R.drawable.ic_lunch)
                                Log.d("RecipeDetails", "Meal icon set: Lunch")
                            }
                            "One Pot Dish" -> {
                                mealTypeImage.setImageResource(R.drawable.ic_evening_snack)
                                Log.d("RecipeDetails", "Meal icon set: One Pot Dish")
                            }
                            "Dinner" -> {
                                mealTypeImage.setImageResource(R.drawable.ic_dinner)
                                Log.d("RecipeDetails", "Meal icon set: Dinner")
                            }
                            else -> {
                                mealTypeImage.setImageResource(R.drawable.ic_view_meal_place)
                                Log.d("RecipeDetails", "Meal icon set: Default (unknown meal type)")
                            }
                        }

                        val imageUrl = data.photo_url
                        Log.d("RecipeDetails", "Loading image from URL: $imageUrl")
                        loadImageFromGoogleDrive(imageUrl, imgFood)
                    } else {
                        Log.w("RecipeDetails", "Success but data is null")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RecipeDetails", "API error → code: ${response.code()}, error body: $errorBody")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()

                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<RecipeDetailsViewResponse>, t: Throwable) {
                Log.e("RecipeDetails", "API call failed completely", t)
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()

                if (isAdded && view != null) {
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

//    fun getFoodType(category: String?): String {
//        if (category.isNullOrBlank()) return ""
//        val cat = category.lowercase()
//        // Check non-veg first (most specific)
//        val isNonVeg = cat.contains("non-vegetarian")
//                || cat.contains("chicken")
//                || cat.contains("fish")
//                || cat.contains("meat")
//                || cat.contains("egg")
//                || cat.contains("mutton")
//        // Check veg only AFTER excluding non-veg
//        val isVeg = !isNonVeg && (cat.contains("vegetarian") || cat.contains("vegan"))
//        return when {
//            isNonVeg -> "Non-Veg"
//            isVeg -> "Veg"
//            else -> ""
//        }
//    }

    fun getFoodType(category: String?): String {
        val cat = category
            ?.trim()
            ?.lowercase()
            ?: return ""
        if (cat.isEmpty()) return ""
        // Highest priority: Vegan
        if (cat.contains("vegan")) {
            return "Veg & Vegan"
        }
        // Next priority: Non-Veg
        val isNonVeg =
            cat.contains("non-vegetarian") ||
                    cat.contains("chicken") ||
                    cat.contains("fish") ||
                    cat.contains("meat") ||
                    cat.contains("egg") ||
                    cat.contains("mutton")
        if (isNonVeg) {
            return "Non-Veg"
        }
        // Veg (only if not non-veg and not vegan)
        if (cat.contains("vegetarian")) {
            return "Veg"
        }
        return ""
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

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}