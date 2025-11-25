package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class PopupMenuAdapter(private val context: Context, private val menuItems: List<MenuItemData>,
private val onItemClickListener: OnItemClickListener) : BaseAdapter() {

    override fun getCount(): Int = menuItems.size
    override fun getItem(position: Int): Any = menuItems[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.popup_menu_item, parent, false)
        
        val menuText = view.findViewById<TextView>(R.id.menuText)
        val menuIcon = view.findViewById<ImageView>(R.id.menuIcon)

        val item = menuItems[position]
        menuText.text = item.title
        menuText.setTextColor(ContextCompat.getColor(context, item.color))
        menuIcon.setImageResource(item.iconRes)

        view.setOnClickListener{
            it.disableViewForSeconds()
            onItemClickListener.onItemClick(item)
        }

        return view
    }

    fun interface OnItemClickListener{
        fun onItemClick(menuItem: MenuItemData)
    }
}

data class MenuItemData(val title: String, val iconRes: Int, val color: Int)
