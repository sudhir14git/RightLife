package com.jetsynthesys.rightlife.ui.exploremodule;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.apimodel.submodule.SubModuleData;
import com.jetsynthesys.rightlife.ui.CategoryListActivity;
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp;
import com.google.gson.Gson;

import java.util.List;

public class ExploreModuleRecyclerAdapter extends RecyclerView.Adapter<ExploreModuleRecyclerAdapter.ViewHolder> {

    private String[] itemNames;
    private int[] itemImages;
    private LayoutInflater inflater;
    private Context ctx;
    List<SubModuleData> contentList;
    public ExploreModuleRecyclerAdapter(Context context, String[] itemNames, int[] itemImages, List<SubModuleData> contentList) {
        this.ctx = context;
        this.itemNames = itemNames;
        this.itemImages = itemImages;
        this.contentList = contentList;
        this.inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_explore_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(contentList.get(position).getName());
        //holder.item_text1.setText(contentList.get(position).getContentType());

        if (contentList.get(position).getImageUrl() != null && !contentList.get(position).getImageUrl().isEmpty()) {
          //  Glide.with(ctx).load(ApiClient.CDN_URL_QA+contentList.get(position).getImageUrl()).into(holder.imageView);


            GlideApp.with(ctx)
                    .load(ApiClient.CDN_URL_QA+contentList.get(position).getImageUrl())
                    .error(R.drawable.img_logintop)
                    .into(holder.imageView);
            //
            // .placeholder(R.drawable.placeholder)

            Log.d("Image URL List", "list Url: " + ApiClient.CDN_URL_QA+contentList.get(position).getImageUrl());
            Log.d("Image URL List", "list title: " + contentList.get(position).getImageUrl());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(ctx, "image clicked - "+holder.getBindingAdapterPosition(), Toast.LENGTH_SHORT).show();
                Gson gson = new Gson();
                String json = gson.toJson(contentList);
              /*  Intent intent = new Intent(holder.itemView.getContext(), ModuleContentDetailViewActivity.class);
                intent.putExtra("Categorytype", contentList.get(position).getId());
                intent.putExtra("position", position);
                intent.putExtra("contentId", contentList.get(position).getId());
                holder.itemView.getContext().startActivity(intent);*/

                Intent intent = new Intent(ctx, CategoryListActivity.class);
                intent.putExtra("Categorytype", contentList.get(holder.getBindingAdapterPosition()).getCategoryId());
                intent.putExtra("moduleId", contentList.get(holder.getBindingAdapterPosition()).getModuleId());
                intent.putExtra("contentId", contentList.get(position).getId());

                ctx.startActivity(intent);
            }
        });
       holder.setModuleColor(holder.rl_main_bg,contentList.get(holder.getBindingAdapterPosition()).getModuleId());
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView,favorite_image,img_iconview;
        TextView textView,item_text1;
        RelativeLayout rl_main_bg;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            textView = itemView.findViewById(R.id.txt_item_title);
            item_text1 = itemView.findViewById(R.id.item_text1);
            img_iconview = itemView.findViewById(R.id.img_iconview);
            favorite_image = itemView.findViewById(R.id.favorite_image);
            rl_main_bg  = itemView.findViewById(R.id.rl_main_bg);
            favorite_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(itemView.getContext(), "position - "+getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });

          //  rl_main_bg.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.sleepright));
            //img_iconview.setImageResource(R.drawable.ic_read_category);

        }

        private void setModuleColor(RelativeLayout rl_main_bg, String moduleId) {
            if (moduleId.equalsIgnoreCase("EAT_RIGHT")) {
                ColorStateList colorStateList = ContextCompat.getColorStateList(itemView.getContext(), R.color.eatright);
                rl_main_bg.setBackgroundTintList(colorStateList);
                rl_main_bg.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.eatright));
            } else if (moduleId.equalsIgnoreCase("THINK_RIGHT")) {
                ColorStateList colorStateList = ContextCompat.getColorStateList(itemView.getContext(), R.color.thinkright);
                rl_main_bg.setBackgroundTintList(colorStateList);
                rl_main_bg.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.thinkright));
            } else if (moduleId.equalsIgnoreCase("SLEEP_RIGHT")) {
                ColorStateList colorStateList = ContextCompat.getColorStateList(itemView.getContext(), R.color.sleepright);
                rl_main_bg.setBackgroundTintList(colorStateList);
                rl_main_bg.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.sleepright));
            } else if (moduleId.equalsIgnoreCase("MOVE_RIGHT")) {
                ColorStateList colorStateList = ContextCompat.getColorStateList(itemView.getContext(), R.color.moveright);
                rl_main_bg.setBackgroundTintList(colorStateList);
                rl_main_bg.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.moveright));
            }
        }
    }
}

