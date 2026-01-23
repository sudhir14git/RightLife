package com.jetsynthesys.rightlife.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.apimodel.servicepane.HomeService;
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp;

import java.util.List;

public class ServicePaneAdapter extends RecyclerView.Adapter<ServicePaneAdapter.ServicePaneViewHolder> {

    private final Context context;
    private final List<HomeService> homeServiceList;
    private final OnItemClickListener onItemClickListener;

    public ServicePaneAdapter(Context context, List<HomeService> homeServiceList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.homeServiceList = homeServiceList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ServicePaneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (homeServiceList.size() % 2 == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.row_service_pane_even, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.row_service_pane_odd, parent, false);
        }
        return new ServicePaneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicePaneViewHolder holder, int position) {
        HomeService homeService = homeServiceList.get(position);
        if (homeServiceList.size() % 2 == 0) {
            GlideApp.with(context).load(ApiClient.CDN_URL_QA + homeService.getThumbnail().getUrl())
                    .placeholder(R.drawable.ic_healthcam_logo)
                    .into(holder.imgEven);

            var formattedText = homeService.getTitle().replaceFirst(" ", "\n");

            holder.tvHeaderEven.setText(formattedText);

            ColorStateList colorStateList;
            switch (homeService.getTitle()) {
                case "Voice Scan":
                    colorStateList = ContextCompat.getColorStateList(context, R.color.thinkright);
                    break;
                case "Mind Audit":
                    colorStateList = ContextCompat.getColorStateList(context, R.color.light_pink);
                    break;
                case "Health Cam":
                case "Face Scan":
                    colorStateList = ContextCompat.getColorStateList(context, R.color.moveright);
                    break;
                default:
                    colorStateList = ContextCompat.getColorStateList(context, R.color.healthauditgreen);
            }
            holder.llServicePaneEven.setBackgroundTintList(colorStateList);
        } else {
            GlideApp.with(context).load(ApiClient.CDN_URL_QA + homeService.getThumbnail().getUrl())
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(holder.imgOdd);

            holder.tvHeaderOdd.setText(homeService.getTitle());

            ColorStateList colorStateList;
            switch (homeService.getTitle()) {
                case "Voice Scan":
                    colorStateList = ContextCompat.getColorStateList(context, R.color.thinkright);
                    break;
                case "Mind Audit":
                    colorStateList = ContextCompat.getColorStateList(context, R.color.light_pink);
                    break;
                case "Face Scan":
                case "Health Cam":
                    colorStateList = ContextCompat.getColorStateList(context, R.color.moveright);
                    break;
                default:
                    colorStateList = ContextCompat.getColorStateList(context, R.color.healthauditgreen);
            }
            holder.llServicePaneOdd.setBackgroundTintList(colorStateList);
        }

        holder.itemView.setOnClickListener(view -> {
            onItemClickListener.onItemClick(homeService);
        });
    }

    @Override
    public int getItemCount() {
        return homeServiceList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(HomeService homeService);
    }

    public static class ServicePaneViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llServicePaneOdd, llServicePaneEven;
        TextView tvHeaderEven, tvHeaderOdd;
        ImageView imgEven, imgOdd;

        public ServicePaneViewHolder(@NonNull View itemView) {
            super(itemView);

            llServicePaneEven = itemView.findViewById(R.id.ll_service_pane_even);
            llServicePaneOdd = itemView.findViewById(R.id.ll_service_pane_odd);
            tvHeaderEven = itemView.findViewById(R.id.tv_service_pane_header_even);
            tvHeaderOdd = itemView.findViewById(R.id.tv_service_pane_header_odd);

            imgEven = itemView.findViewById(R.id.img_service_pane_even);
            imgOdd = itemView.findViewById(R.id.img_service_pane_odd);

        }
    }
}
