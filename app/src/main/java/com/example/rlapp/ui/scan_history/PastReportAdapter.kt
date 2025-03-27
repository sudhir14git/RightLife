package com.example.rlapp.ui.scan_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.databinding.ItemDateHeaderBinding
import com.example.rlapp.databinding.ItemPastReportBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PastReportAdapter(
    private val items: List<Any>,
    private val listener: (ReportItem) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_DATE_HEADER = 0
        private const val VIEW_TYPE_REPORT_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) VIEW_TYPE_DATE_HEADER else VIEW_TYPE_REPORT_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_DATE_HEADER) {
            val binding =
                ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DateHeaderViewHolder(binding)
        } else {
            val binding =
                ItemPastReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReportViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateHeaderViewHolder) {
            holder.bind(items[position] as String)
        } else if (holder is ReportViewHolder) {
            holder.bind(items[position] as ReportItem)
            holder.itemView.setOnClickListener { listener(items[position] as ReportItem) }
        }
    }

    override fun getItemCount(): Int = items.size

    class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.txtDateHeader.text = date
        }
    }

    class ReportViewHolder(private val binding: ItemPastReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(report: ReportItem) {
            binding.txtTitle.text = report.title
            binding.txtTime.text = formatTime(report.createdAt)

            binding.imgIcon.setImageResource(
                when (report.type) {
                    "FACIAL_SCAN" -> R.drawable.ic_face_scan
                    "MIND_AUDIT" -> R.drawable.ic_mind_audit
                    "FOOD_SNAP" -> R.drawable.ic_food_snap
                    else -> R.drawable.bg_circle
                }
            )
        }

        private fun formatTime(isoDate: String): String {
            val instant = Instant.parse(isoDate)
            val formatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault())
            return formatter.format(instant)
        }
    }
}
