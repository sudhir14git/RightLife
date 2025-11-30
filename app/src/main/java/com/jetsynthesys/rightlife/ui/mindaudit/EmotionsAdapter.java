package com.jetsynthesys.rightlife.ui.mindaudit;

import android.annotation.SuppressLint;
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
import com.jetsynthesys.rightlife.ui.utility.ButtonUtilsKt;

import java.util.ArrayList;

public class EmotionsAdapter extends RecyclerView.Adapter<EmotionsAdapter.EmotionsViewHolder> {

    private final Context context;
    private final ArrayList<Emotions> emotions;
    private final String type;
    private final OnItemClickListener onItemClickListener;

    public EmotionsAdapter(Context context, ArrayList<Emotions> emotions, String type, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.emotions = emotions;
        this.onItemClickListener = onItemClickListener;
        this.type = type;
    }

    @NonNull
    @Override
    public EmotionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_mind_audit_emotion, parent, false);
        return new EmotionsViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull EmotionsViewHolder holder, int position) {
        Emotions emotion = emotions.get(position);
        holder.tvEmotions.setText(emotion.getEmotion());
        switch (emotion.getEmotion().toUpperCase()) {
            case "LOW":
                holder.ivEmotions.setImageResource(R.drawable.img_mindau_low);
                break;
            case "ANXIOUS":
                holder.ivEmotions.setImageResource(R.drawable.img_mindau_anxious);
                break;
            case "IRRITABLE":
                holder.ivEmotions.setImageResource(R.drawable.img_mindau_irritable);
                break;
            case "FATIGUED":
                holder.ivEmotions.setImageResource(R.drawable.img_mindau_fatigue);
                break;
            case "STRESSED":
                holder.ivEmotions.setImageResource(R.drawable.img_mindau_stressed);
                break;
            case "UNFULFILLED":
                holder.ivEmotions.setImageResource(R.drawable.img_mindau_unfulfilled);
                break;
        }

        if (!type.equals("2"))
            if (emotion.isSelected()) {
                holder.linearLayout.setBackground(context.getDrawable(R.drawable.roundedcornerbutton_pink));
                holder.tvEmotions.setBackground(context.getDrawable(R.drawable.roundedcornerbottomshape_medium_pink));
                setMargin(1, holder.tvEmotions);
            } else {
                holder.linearLayout.setBackground(context.getDrawable(R.drawable.roundedcornerbutton_light_pink));
                holder.tvEmotions.setBackground(context.getDrawable(R.drawable.roundedcornerbottomshape));
                setMargin(0, holder.tvEmotions);
            }


        holder.itemView.setOnClickListener(v -> {

            emotion.setSelected(!emotion.isSelected());
            notifyItemChanged(position);
            onItemClickListener.onItemClick(emotion);
        });
        if (type.equals("2")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.thinkright);
            holder.linearLayout.setBackgroundTintList(colorStateList);
        }
    }

    @Override
    public int getItemCount() {
        return emotions.size();
    }

    private void setMargin(int margin, TextView textView) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                95
        );
        params.setMargins(margin, margin, margin, margin);
        textView.setLayoutParams(params);
    }

    public interface OnItemClickListener {
        void onItemClick(Emotions emotion);
    }

    static class EmotionsViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmotions;
        ImageView ivEmotions;
        LinearLayout linearLayout;

        public EmotionsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmotions = itemView.findViewById(R.id.tv_emotion_text);
            ivEmotions = itemView.findViewById(R.id.img_emotion);
            linearLayout = itemView.findViewById(R.id.ll_thinkright_category1);
        }
    }
}
