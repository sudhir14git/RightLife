package com.jetsynthesys.rightlife.newdashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.healthcam.ParameterModel

class PopupMenuAdapter(
    private val context: Context,
    private val options: ArrayList<ParameterModel>,
    private val onItemClick: (ParameterModel) -> Unit
) : RecyclerView.Adapter<PopupMenuAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOption: TextView = itemView.findViewById(R.id.tvWitale)
        val image = itemView.findViewById<ImageView>(R.id.img_popup_menu)

        init {
            itemView.setOnClickListener {
                onItemClick(options[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vital_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvOption.text = options[position].name
        Glide.with(context)
            .load(getReportIconByType(options[position].key))
            .placeholder(R.drawable.ic_db_report_heart_rate)
            .error(R.drawable.ic_db_report_heart_rate)
            .into(holder.image)
    }

    override fun getItemCount(): Int = options.size

    private fun getReportIconByType(type: String): Int {
        return when (type) {
            "BMI_CALC" -> R.drawable.ic_db_report_bmi
            "BP_RPP" -> R.drawable.ic_db_report_cardiak_workload
            "BP_SYSTOLIC" -> R.drawable.ic_db_report_bloodpressure
            "BP_DIASTOLIC" -> R.drawable.ic_db_report_bloodpressure
            "BP_CVD" -> R.drawable.ic_db_report_cvdrisk
            "MSI" -> R.drawable.ic_db_report_stresslevel
            "BR_BPM" -> R.drawable.ic_db_report_respiratory_rate
            "HRV_SDNN" -> R.drawable.ic_db_report_heart_variability
            "HEALTH_SCORE" -> R.drawable.ic_wellness_man_small
            else -> R.drawable.ic_db_report_heart_rate
        }
    }
}
