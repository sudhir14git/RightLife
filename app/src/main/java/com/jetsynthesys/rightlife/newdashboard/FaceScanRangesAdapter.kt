package com.jetsynthesys.rightlife.newdashboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.databinding.RowRangesFaceScanDetailsBinding
import com.jetsynthesys.rightlife.newdashboard.model.FacialScanRange

class FaceScanRangesAdapter(private val rangeList: List<FacialScanRange>?) :
    RecyclerView.Adapter<FaceScanRangesAdapter.RangesViewHolder>() {

    inner class RangesViewHolder(val binding: RowRangesFaceScanDetailsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RangesViewHolder {
        val binding =
            RowRangesFaceScanDetailsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return RangesViewHolder(binding)
    }

    override fun getItemCount(): Int = rangeList?.size ?: 0

    override fun onBindViewHolder(holder: RangesViewHolder, position: Int) {
        val secondReport = rangeList?.get(position)
        with(holder.binding) {
            indicatorRange.text = secondReport?.indicator
            tvIndicatorExplainRange.text =
                Html.fromHtml(secondReport?.implication, Html.FROM_HTML_MODE_LEGACY)

            tvIndicatorValueBgRange.text =
                "${secondReport?.lowerRange}-${secondReport?.upperRange} ${secondReport?.unit}"

            val colorHexString1 = "#" + secondReport?.colour // Construct the correct hex string
            val colorInt1 = Color.parseColor(colorHexString1)
            val colorStateList = ColorStateList.valueOf(colorInt1)
            tvIndicatorValueBgRange.backgroundTintList = colorStateList
        }
    }

}