package com.jetsynthesys.rightlife.ui.new_design

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.ui.new_design.pojo.OnboardingModuleResultDataList

class UnlockPowerAdapter(
    private val context: Context,
    private val unlockPowerList: ArrayList<OnboardingModuleResultDataList>
) : RecyclerView.Adapter<UnlockPowerAdapter.UnlockPowerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnlockPowerViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.row_unlock_power, parent, false)
        return UnlockPowerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return unlockPowerList.size
    }

    override fun onBindViewHolder(holder: UnlockPowerViewHolder, position: Int) {
        val unlockPower = unlockPowerList[position]
        //holder.imageView.setImageResource(unlockPower.imageResource)

        Glide.with(context).load(ApiClient.CDN_URL_QA + unlockPower.onboardingModuleThumbnail)
            .placeholder(R.drawable.visualise_manifest)
            .error(R.drawable.visualise_manifest)
            .into(holder.imageView)

        holder.tvHeader.text = unlockPower.onboardingModuleTopic
        holder.tvDescription.text = unlockPower.onboardingModuleTopicData
    }

    class UnlockPowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.image_row_unlock_power)
        var tvHeader: TextView = itemView.findViewById(R.id.tv_row_header_unlock_power)
        var tvDescription: TextView = itemView.findViewById(R.id.tv_row_desc_unlock_power)
    }
}