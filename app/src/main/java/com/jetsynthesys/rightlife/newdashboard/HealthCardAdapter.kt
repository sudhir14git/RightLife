package com.jetsynthesys.rightlife.newdashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ItemHealthCardBinding
import com.jetsynthesys.rightlife.newdashboard.model.DiscoverDataItem

class HealthCardAdapter(
    private val cardList: List<DiscoverDataItem>?,
    val onItemClick: (DiscoverDataItem?) -> Unit
) :
    RecyclerView.Adapter<HealthCardAdapter.HealthCardViewHolder>() {

    inner class HealthCardViewHolder(val binding: ItemHealthCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthCardViewHolder {
        val binding =
            ItemHealthCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HealthCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HealthCardViewHolder, position: Int) {
        val item = cardList?.get(position)
        with(holder.binding) {
            if (item != null) {
                cardTitle.text = item.parameter
            }
            if (item != null) {
                tvDescription.text = item.def
            }

            val iconResReport = getReportIconByType(item?.key.toString())
            imageCard.setImageResource(iconResReport)

            /*Glide.with(root.context)
                .load(item.imageCard)
                .into(imageCard)

            Glide.with(root.context)
                .load(item.imageGraph)
                .into(imageGraph)*/

            btnDiscover.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int = cardList!!.size

    private fun getReportIconByType(type: String): Int {
        return when (type) {
            "BMI_CALC" -> R.drawable.ic_db_report_bmi
            "BP_RPP" -> R.drawable.ic_db_report_cardiak_workload
            "BP_SYSTOLIC" -> R.drawable.ic_db_report_bloodpressure
            "BP_CVD" -> R.drawable.ic_db_report_cvdrisk
            "MSI" -> R.drawable.ic_db_report_stresslevel
            "BR_BPM" -> R.drawable.ic_db_report_respiratory_rate
            "HRV_SDNN" -> R.drawable.ic_db_report_heart_variability
            else -> R.drawable.ic_db_report_heart_rate
        }
    }

}
