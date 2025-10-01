package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.createmeal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeList

class SearchDishesAdapter(private val context: Context, private var dataLists: ArrayList<IngredientRecipeList>,
                          private var clickPos: Int, private var mealLogListData : IngredientRecipeList?,
                          private var isClickView : Boolean, val onSearchDishItem: (IngredientRecipeList, Int, Boolean) -> Unit) :
    RecyclerView.Adapter<SearchDishesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish_search_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.dishName.text = item.recipe
        var imageUrl : String? = ""
        imageUrl = if (item.photo_url.contains("drive.google.com")) {
            getDriveImageUrl(item.photo_url)
        }else{
            item.photo_url
        }
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_view_meal_place)
            .error(R.drawable.ic_view_meal_place)
            .into(holder.dishImage)

        holder.layoutMain.setOnClickListener {
            onSearchDishItem(item, position, true)
        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

     inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

         val dishName: TextView = itemView.findViewById(R.id.tv_dish_name)
         val rightArrow: ImageView = itemView.findViewById(R.id.image_right_arrow)
         val dishImage: ImageView = itemView.findViewById(R.id.image_dish)
         val layoutMain : LinearLayout = itemView.findViewById(R.id.lyt_meal_item)
     }

    fun addAll(item : ArrayList<IngredientRecipeList>?, pos: Int, mealLogItem : IngredientRecipeList?, isClick : Boolean) {
        dataLists.clear()
        if (item != null) {
            dataLists = item
            clickPos = pos
            mealLogListData = mealLogItem
            isClickView = isClick
        }
        notifyDataSetChanged()
    }

    fun updateList(newList: List<IngredientRecipeList>) {
        dataLists = newList as ArrayList<IngredientRecipeList>
        notifyDataSetChanged()
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