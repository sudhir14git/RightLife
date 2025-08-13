package com.jetsynthesys.rightlife.ui;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.RetrofitData.ApiService;
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity;
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditActivity;
import com.jetsynthesys.rightlife.ui.therledit.ViewCountRequest;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager;
import com.jetsynthesys.rightlife.ui.utility.Utils;
import com.jetsynthesys.rightlife.ui.voicescan.VoiceScanActivity;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CircularCardAdapter extends RecyclerView.Adapter<CircularCardAdapter.CardViewHolder> {

    private final Context mContext;
    private final List<CardItem> items; // Replace CardItem with your model class

    public CircularCardAdapter(Context context, List<CardItem> items) {
        this.items = items;
        this.mContext = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        // Circular scrolling logic
        if (items.isEmpty())
            return;
        CardItem item = items.get(position % items.size());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(view.getContext(), "Clicked on: " + item.getTitle()+ holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
                // Start new activity here

                if (item.getCategory().equalsIgnoreCase("daily") ||
                        item.getCategory().equalsIgnoreCase("CONTENT")) {
                    Intent intent = new Intent(mContext, ContentDetailsActivity.class);
                    intent.putExtra("contentId", item.getSeriesId());
                    mContext.startActivity(intent);
                } else if (item.getCategory().equalsIgnoreCase("live")) {
                    Toast.makeText(mContext, "Live Content", Toast.LENGTH_SHORT).show();
                } else if (item.getCategory().equalsIgnoreCase("MIND_AUDIT") ||
                        item.getCategory().equalsIgnoreCase("Mind Audit") ||
                        item.getCategory().equalsIgnoreCase("Health Audit") ||
                        item.getCategory().equalsIgnoreCase("mindAudit")) {
                    Intent intent = new Intent(mContext, MindAuditActivity.class);
                    // Optionally pass data
                    //intent.putExtra("key", "value");
                    mContext.startActivity(intent);

                } else if (item.getCategory().equalsIgnoreCase("VOICE_SCAN")) {
                    Intent intent = new Intent(mContext, VoiceScanActivity.class);
                    // Optionally pass data
                    //intent.putExtra("key", "value");
                    mContext.startActivity(intent);

                } else if (item.getCategory().equalsIgnoreCase("FACIAL_SCAN") ||
                        item.getCategory().equalsIgnoreCase("Health Cam") || item.getCategory().equalsIgnoreCase("FACE_SCAN")) {
                    ActivityUtils.INSTANCE.startFaceScanActivity(mContext);
                }

                ViewCountRequest viewCountRequest = new ViewCountRequest();
                viewCountRequest.setId(item.getId());
                viewCountRequest.setUserId(SharedPreferenceManager.getInstance(mContext).getUserId());
                updateViewCount(viewCountRequest, holder.getBindingAdapterPosition());
            }
        });
        Utils.logDebug("CircularCardAdapter", "" + holder.getBindingAdapterPosition());
        holder.bind(item);

    }

    @Override
    public int getItemCount() {
        // To enable infinite scrolling
        return Integer.MAX_VALUE;
    }

    public void updateData(List<CardItem> newData) {
        // 1. Clear the existing data
        items.clear();

        // 2. Add the new data
        items.addAll(newData);

        // 3. Notify the adapter
        notifyDataSetChanged();
    }

    private void updateViewCount(ViewCountRequest viewCountRequest, int position) {
        String authToken = SharedPreferenceManager.getInstance(mContext).getAccessToken();
        ApiService apiService = ApiClient.getClient(mContext).create(ApiService.class);
        Call<ResponseBody> call = apiService.UpdateBannerViewCount(authToken, viewCountRequest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = jsonString = response.body().string();
                        Log.d("API_RESPONSE", "View Count content: " + jsonString);

                        //     int currentViewCount = Integer.parseInt(items.get(position).getViewCount());
                        //       items.get(position).setViewCount(String.valueOf(currentViewCount + 1));
                        //notifyDataSetChanged();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("API_FAILURE", "Failure: " + t.getMessage());
            }
        });
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private final TextView cardTitle;
        private final TextView cardbtntext;
        private final TextView cardbtntextDesc;
        private final TextView workshop_tag1;
        private final ImageView cardImage, img_btn_icon, img_title_icon;
        private TextView workshop_tag3;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTitle = itemView.findViewById(R.id.promotitle);
            cardbtntextDesc = itemView.findViewById(R.id.promodescription);
            cardbtntext = itemView.findViewById(R.id.promobtntxt);
            cardImage = itemView.findViewById(R.id.cardImage);
            workshop_tag1 = itemView.findViewById(R.id.workshop_tag1);
            img_btn_icon = itemView.findViewById(R.id.img_btn_icon);
            img_title_icon = itemView.findViewById(R.id.img_title_icon);
        }

        public void bind(CardItem item) {
            // cardTitle.setText(item.getTitle());
            cardImage.setImageResource(item.getImageResId());
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(ApiClient.CDN_URL_QA + item.getImageUrl())
                        .placeholder(R.drawable.rl_placeholder)
                        .error(R.drawable.rl_placeholder)
                        .into(cardImage);
            }
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(ApiClient.CDN_URL_QA + item.getButtonImage())
                        .placeholder(R.drawable.ic_banner_t_healthcam)
                        .error(R.drawable.ic_banner_t_healthcam)
                        .into(img_btn_icon);
            }
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(ApiClient.CDN_URL_QA + item.getTitleImage())
                        .placeholder(R.drawable.ic_banner_t_healthcam)
                        .error(R.drawable.ic_banner_t_healthcam)
                        .into(img_title_icon);
            }

            cardbtntext.setText(item.getButtonText());
            cardTitle.setText(item.getTitle());
            cardbtntextDesc.setText(item.getContent());
            workshop_tag1.setText(item.getViewCount());

            //Drawable drawable = itemView.getContext().getResources().getDrawable(R.drawable.ic_home_black_24dp);

            if (item.getCategory().equalsIgnoreCase("MIND_AUDIT")) {
                Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_banner_t_mindaudit);
                //cardTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                Drawable drawable1 = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_banner_chekinnow);
                //cardbtntext.setCompoundDrawablesWithIntrinsicBounds(drawable1, null, null, null);

            } else if (item.getCategory().equalsIgnoreCase("VOICE_SCAN")) {

                Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_banner_t_voicescan);
                //cardTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                Drawable drawable1 = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_banner_recordnow);
                //cardbtntext.setCompoundDrawablesWithIntrinsicBounds(drawable1, null, null, null);

            } else {
                //else if (item.getCategory().equalsIgnoreCase("FACIAL_SCAN"))
                Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_banner_t_healthcam);
                //cardTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                Drawable drawable1 = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_banner_scannow);
                //cardbtntext.setCompoundDrawablesWithIntrinsicBounds(drawable1, null, null, null);
            }

        }
    }
}
