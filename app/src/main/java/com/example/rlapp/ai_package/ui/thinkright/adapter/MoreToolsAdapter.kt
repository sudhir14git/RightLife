package com.example.rlapp.ai_package.ui.thinkright.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R

class MoreToolsAdapter (private val context: Context, private var dataLists: Int,)
    : RecyclerView.Adapter<MoreToolsAdapter.ViewHolder>() {

    private var selectedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tools_ai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoreToolsAdapter.ViewHolder, position: Int) {


    }

    override fun getItemCount(): Int {
        return 4
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

}