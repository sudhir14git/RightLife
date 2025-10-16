package com.jetsynthesys.rightlife.ui.new_design

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.new_design.pojo.StressManagement

class StressManagementAdapter(
    private val context: Context,
    private val stressManagementList: ArrayList<StressManagement>,
    private val onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<StressManagementAdapter.StressManagementViewHolder>() {
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StressManagementViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.row_stress_management, parent, false)
        return StressManagementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return stressManagementList.size
    }

    override fun onBindViewHolder(holder: StressManagementViewHolder, position: Int) {
        holder.tvHeader.text = stressManagementList[position].header
        holder.tvDescription.text = stressManagementList[position].description
        holder.imageView.setImageResource(stressManagementList[position].imageResource)

        holder.llStressManagement.background = AppCompatResources.getDrawable(
            context,
            if (selectedPosition == position) R.drawable.bg_light_red_rounded else R.drawable.bg_gray_border
        )

        holder.itemView.setOnClickListener {

            selectedPosition = position

            onItemClickListener.onItemClick(stressManagementList[position])
            stressManagementList[position].isSelected = !stressManagementList[position].isSelected
            notifyDataSetChanged()
        }
    }

    fun interface OnItemClickListener {
        fun onItemClick(stressManagement: StressManagement)
    }

    class StressManagementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.image_row_stress_management)
        var tvHeader: TextView = itemView.findViewById(R.id.tv_row_stress_management)
        var tvDescription: TextView = itemView.findViewById(R.id.tv_row_desc_stress_management)
        var llStressManagement: LinearLayout = itemView.findViewById(R.id.ll_stress_management)
    }
}