package com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeList

class RecipeSearchAdapter(
    private val context: Context,
    private var dataLists: ArrayList<SnapRecipeList>,
    private var clickPos: Int,
    private var mealLogListData: SnapRecipeList?,
    private var isClickView: Boolean,
    val onSearchDishItem: (SnapRecipeList, Int, Boolean) -> Unit
) : RecyclerView.Adapter<RecipeSearchAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.dishName.text = item.recipe
        val imageUrl = getDriveImageUrl(item.photo_url)
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_view_meal_place)
            .error(R.drawable.ic_view_meal_place)
            .into(holder.dishImage)

        val type = item.tags
            ?.substringBefore("_")
            ?.trim()
            ?.lowercase()
        holder.statusDot.visibility = View.VISIBLE
        when (type) {
            "veg", "vegan" -> holder.statusDot.setImageResource(R.drawable.green_circle)
            "non-veg", "nonveg" -> holder.statusDot.setImageResource(R.drawable.red_circle)
            "egg" -> holder.statusDot.setImageResource(R.drawable.red_circle)
            else -> holder.statusDot.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            onSearchDishItem(item, position, true)
        }
    }

    override fun getItemCount(): Int {
        return dataLists.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dishName: TextView = itemView.findViewById(R.id.recipeName)
        val dishImage: ImageView = itemView.findViewById(R.id.imageView)
        val statusDot : ImageView = itemView.findViewById(R.id.statusDot)
    }

    fun addAll(item: ArrayList<SnapRecipeList>?, pos: Int, mealLogItem: SnapRecipeList?, isClick: Boolean) {
        dataLists.clear()
        if (item != null) {
            dataLists = item
            clickPos = pos
            mealLogListData = mealLogItem
            isClickView = isClick
            notifyDataSetChanged()
        }
    }

    fun updateList(newList: List<SnapRecipeList>) {
        dataLists.clear()
        dataLists = newList as ArrayList<SnapRecipeList>
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