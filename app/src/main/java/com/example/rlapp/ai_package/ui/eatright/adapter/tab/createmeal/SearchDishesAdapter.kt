package com.example.rlapp.ai_package.ui.eatright.adapter.tab.createmeal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.bumptech.glide.Glide
import com.example.rlapp.ai_package.model.RecipeList

class SearchDishesAdapter(private val context: Context, private var dataLists: ArrayList<RecipeList>,
                          private var clickPos: Int, private var mealLogListData : RecipeList?,
                          private var isClickView : Boolean, val onSearchDishItem: (RecipeList, Int, Boolean) -> Unit) :
    RecyclerView.Adapter<SearchDishesAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish_search_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataLists[position]

        holder.dishName.text = item.name
        Glide.with(context)
            .load(item.image)
            .placeholder(R.drawable.ic_breakfast)
            .error(R.drawable.ic_breakfast)
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

    fun addAll(item : ArrayList<RecipeList>?, pos: Int, mealLogItem : RecipeList?, isClick : Boolean) {
        dataLists.clear()
        if (item != null) {
            dataLists = item
            clickPos = pos
            mealLogListData = mealLogItem
            isClickView = isClick
        }
        notifyDataSetChanged()
    }

    fun updateList(newList: List<RecipeList>) {
        dataLists = newList as ArrayList<RecipeList>
        notifyDataSetChanged()
    }
}