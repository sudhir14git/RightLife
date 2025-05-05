package com.jetsynthesys.rightlife.ui.Wellness;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeModel;
import com.jetsynthesys.rightlife.apimodel.morelikecontent.Artist;
import com.jetsynthesys.rightlife.ui.contentdetailvideo.NewSeriesDetailsActivity;
import com.jetsynthesys.rightlife.ui.therledit.FavouriteRequest;
import com.jetsynthesys.rightlife.ui.therledit.OnFavouriteClickListener;
import com.jetsynthesys.rightlife.ui.utility.Utils;

import java.util.List;

public class SeriesListAdapter extends RecyclerView.Adapter<SeriesListAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private Context ctx;
    List<EpisodeModel> contentList;
    public SeriesListAdapter(Context context,List<EpisodeModel> contentList) {
        this.ctx = context;
        this.contentList = contentList;
        this.inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public SeriesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_seriesepisodelist_item, parent, false);
        return new SeriesListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesListAdapter.ViewHolder holder, int position) {
        holder.textView.setText(String.valueOf(position+1) +". "+contentList.get(position).getTitle());

        List<Artist> artists = contentList.get(position).getArtist();
        if (artists != null && !artists.isEmpty() && artists.get(0) != null) {
            String firstName = artists.get(0).getFirstName();
            String lastName = artists.get(0).getLastName();
            String fullName = firstName + (lastName != null ? " " + lastName : "");
            holder.tv_author_name.setText(fullName);
        } else {
            holder.tv_author_name.setText(""); // or "Unknown"
        }
        String result = formatTimeInMinSec(contentList.get(position).getMeta().getDuration()); // Output: "2.19 min"
        holder.tv_time.setText(result);
        holder.tv_time.setVisibility(View.VISIBLE);

        //holder.tv_author_name.setText(contentList.get(position).getArtist().get(0).getFirstName()+" "+contentList.get(position).getArtist().get(0).getLastName());
        if (contentList.get(position).getThumbnail().getUrl() != null && !contentList.get(position).getThumbnail().getUrl().isEmpty()) {
            //Glide.with(ctx).load(ApiClient.CDN_URL_QA+contentList.get(position).getThumbnail().getUrl()).transform(new RoundedCorners(15)).into(holder.imageView);
            Glide.with(ctx)
                    .load(ApiClient.CDN_URL_QA+contentList.get(position).getThumbnail().getUrl())
                    .into(holder.imageView);

            Log.d("Image URL List", "list Url: " + ApiClient.CDN_URL_QA+contentList.get(position).getThumbnail().getUrl());
            Log.d("Image URL List", "list title: " + contentList.get(position).getTitle());
        }
        //holder.imageView.setImageResource(itemImages[position]);
        if (contentList.get(position).getType().equalsIgnoreCase("TEXT")){
            holder.img_iconview.setImageResource(R.drawable.ic_read_category);
        }else {
            holder.img_iconview.setImageResource(R.drawable.ic_sound_category);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contentList.get(position).getType().equalsIgnoreCase("YOUTUBE"))
                {
                    Intent intent = new Intent(holder.itemView.getContext(), NewSeriesDetailsActivity.class);
                    intent.putExtra("seriesId", contentList.get(position).getContentId());
                    intent.putExtra("episodeId", contentList.get(position).get_id());
                    holder.itemView.getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(holder.itemView.getContext(), NewSeriesDetailsActivity.class);
                    intent.putExtra("seriesId", contentList.get(position).getContentId());
                    intent.putExtra("episodeId", contentList.get(position).get_id());
                    holder.itemView.getContext().startActivity(intent);
                }


            }
        });

        if (contentList.get(position).isFavourited()) {
            holder.favorite_image.setImageResource(R.drawable.favstarsolid);
        } else {
            holder.favorite_image.setImageResource(R.drawable.unfavorite);
        }

        holder.favorite_image.setOnClickListener(view -> {
            FavouriteRequest favouriteRequest = new FavouriteRequest();
            favouriteRequest.setFavourite(!contentList.get(position).isFavourited());
            favouriteRequest.setEpisodeId(contentList.get(position).get_id());
            Utils.addToFavourite(ctx, contentList.get(position).getContentId(), favouriteRequest, new OnFavouriteClickListener() {
                @Override
                public void onSuccess(boolean isSuccess, String message) {
                    Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                    if (isSuccess) {
                        contentList.get(position).setFavourited(!contentList.get(position).isFavourited());
                        notifyItemChanged(position);
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView,favorite_image,img_iconview;
        TextView textView,tv_author_name,tv_time;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            textView = itemView.findViewById(R.id.item_text);
            tv_author_name = itemView.findViewById(R.id.tv_author_name);
            tv_time = itemView.findViewById(R.id.tv_time);
            img_iconview = itemView.findViewById(R.id.img_iconview);
            favorite_image = itemView.findViewById(R.id.favorite_image);
            favorite_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(itemView.getContext(), "position - "+getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });

            //img_iconview.setImageResource(R.drawable.ic_read_category);
        }
    }
    private String formatTimeInMinSec(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;

        if (mins > 0 && secs > 0) {
            double formattedMin = mins + secs / 100.0;
            return String.format("%.2f min", formattedMin);
        } else if (mins > 0) {
            return mins + " min";
        } else {
            return secs + " sec";
        }
    }

}

