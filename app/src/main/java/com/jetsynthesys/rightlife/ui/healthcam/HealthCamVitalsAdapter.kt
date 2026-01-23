package com.jetsynthesys.rightlife.ui.healthcam

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.newreportfacescan.HealthCamItem
import com.jetsynthesys.rightlife.databinding.HealthCamVitalsItemBinding
import com.jetsynthesys.rightlife.newdashboard.FacialScanReportDetailsActivity
import com.jetsynthesys.rightlife.ui.NumberUtils.smartRound
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamVitalsAdapter.HealthCamVitalsViewHolder
import com.jetsynthesys.rightlife.ui.utility.ConversionUtils
import com.jetsynthesys.rightlife.ui.utility.Utils

class HealthCamVitalsAdapter(
    private val context: Context,
    private val healthCamItems: List<HealthCamItem>?
) : RecyclerView.Adapter<HealthCamVitalsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthCamVitalsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HealthCamVitalsItemBinding.inflate(inflater, parent, false)
        return HealthCamVitalsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HealthCamVitalsViewHolder, position: Int) {
        val item = healthCamItems!![position]


        val `val` = smartRound(item.value, 2)
        holder.binding.valueTextView.text = `val`.toString()

        val formatedValue = getFormatedValue(item.fieldName, item.value.toString())
        holder.binding.valueTextView.text = formatedValue //String.valueOf(item.value));

        holder.binding.unitTextView.text = item.unit
        holder.binding.indicatorTextView.text = item.indicator
        holder.binding.parameterTextView.text = item.parameter
        holder.binding.rlMainBg.backgroundTintList =
            ColorStateList.valueOf(Utils.getColorFromColorCode(item.colour))
        holder.binding.indicatorTextView.setTextColor(Utils.getColorFromColorCode(item.colour))

        holder.itemView.setOnClickListener {
            val unifiedList = unifiedParameterList
            val intent = Intent(context, FacialScanReportDetailsActivity::class.java)
            intent.putExtra(
                "healthCamItemList", ArrayList(
                    healthCamItems
                )
            ) // Serializable list
            intent.putExtra("UNIFIED_LIST", ArrayList(unifiedList)) // Serializable list
            intent.putExtra("position", holder.bindingAdapterPosition)
            context.startActivity(intent)
        }
        Glide.with(context)
            .load(getReportIconByType(item.fieldName))
            .placeholder(R.drawable.ic_db_report_heart_rate)
            .error(R.drawable.ic_db_report_heart_rate)
            .into(holder.binding.imgUnit)
    }

    private val unifiedParameterList: ArrayList<ParameterModel>
        get() {
            val resultList = ArrayList<ParameterModel>()

            if (healthCamItems != null) {
                for (item in healthCamItems) {
                    val key = item.fieldName
                    val name = item.parameter

                    if (key == null || name == null) {
                        continue
                    }

                    resultList.add(ParameterModel(key, name))
                }
            }

            return resultList
        }


    private fun getReportIconByType(type: String): Int {
        return when (type) {
            "BMI_CALC" -> R.drawable.ic_db_report_bmi
            "BP_RPP" -> R.drawable.ic_db_report_cardiak_workload
            "BP_SYSTOLIC","BP_DIASTOLIC" -> R.drawable.ic_db_report_bloodpressure
            "BP_CVD" -> R.drawable.ic_db_report_cvdrisk
            "MSI" -> R.drawable.ic_db_report_stresslevel
            "BR_BPM" -> R.drawable.ic_db_report_respiratory_rate
            "HRV_SDNN" -> R.drawable.ic_db_report_heart_variability
            else -> R.drawable.ic_db_report_heart_rate
        }
    }

    override fun getItemCount(): Int {
        return healthCamItems!!.size
    }

    class HealthCamVitalsViewHolder(var binding: HealthCamVitalsItemBinding) :
        RecyclerView.ViewHolder(
            binding.root
        )


    private fun getFormatedValue(key: String, value: String): String {
        try {
            val `val` = value.toDouble()

            return when (key) {
                "HRV_SDNN", "PHYSIO_SCORE", "BMI_CALC", "BP_CVD" -> ConversionUtils.decimalFormat1Decimal.format(
                    `val`
                )

                "heartRateVariability", "waistToHeight", "systolic", "BP_RPP" -> ConversionUtils.decimalFormat2Decimal.format(
                    `val`
                )

                "IHB_COUNT", "MENTAL_SCORE", "BP_DIASTOLIC", "BP_SYSTOLIC" -> ConversionUtils.decimalFormat0Decimal.format(
                    `val`
                )

                else -> ConversionUtils.decimalFormat1Decimal.format(`val`)
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return value
        }
    }
}
