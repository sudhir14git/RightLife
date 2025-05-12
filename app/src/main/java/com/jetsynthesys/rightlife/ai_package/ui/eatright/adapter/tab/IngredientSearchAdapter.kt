package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab

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
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientLists
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultItem

class IngredientSearchAdapter(private val context: Context, private var dataLists: ArrayList<IngredientLists>,
                              private var clickPos: Int, private var ingredientData : IngredientLists?,
                              private var isClickView : Boolean, val onSearchIngredientItem: (IngredientLists, Int, Boolean) -> Unit) :
    RecyclerView.Adapter<IngredientSearchAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish_search_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.dishName.text = item.ingredient_name
        val imageUrl = getDriveImageUrl(item.photo_url)
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_breakfast)
            .error(R.drawable.ic_breakfast)
            .into(holder.dishImage)

        holder.layoutMain.setOnClickListener {
            onSearchIngredientItem(item, position, true)
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

    fun addAll(item : ArrayList<IngredientLists>?, pos: Int, ingredient : IngredientLists?, isClick : Boolean) {
        dataLists.clear()
        if (item != null) {
            dataLists = item
            clickPos = pos
            ingredientData = ingredient
            isClickView = isClick
        }
        notifyDataSetChanged()
    }

    fun updateList(newList: List<IngredientLists>) {
        dataLists = newList as ArrayList<IngredientLists>
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