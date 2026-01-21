package com.jetsynthesys.rightlife.ui.Articles;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.databinding.ArticleItemRowBinding;
import com.jetsynthesys.rightlife.ui.Articles.models.Article;
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity;
import com.jetsynthesys.rightlife.ui.contentdetailvideo.SeriesListActivity;
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils;
import com.jetsynthesys.rightlife.ui.utility.Utils;
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp;

import java.util.List;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ArticleViewHolder> {
    private List<Article> articleList;
    private Context context;

    public ArticleListAdapter(ArticlesDetailActivity articlesDetailActivity, List<Article> articleList) {
        this.articleList = articleList;
        context = articlesDetailActivity;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ArticleItemRowBinding binding = ArticleItemRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ArticleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articleList.get(position);

        //holder.binding.txtArticleContent.setText(article.getHtmlContent());
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.binding.txtArticleContent.setText(Html.fromHtml("<p>Read more on <a href=\"https://example.com\">this page</a>.</p>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.binding.txtArticleContent.setText(Html.fromHtml("<p>Read more on <a href=\"https://example.com\">this page</a>.</p>"));
        }*/

        // --- Render HTML content with images ---
        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                final URLDrawable urlDrawable = new URLDrawable();

                // Handle Base64 inline images (data:image/...)
                if (source.startsWith("data:image")) {
                    try {
                        String base64Data = source.substring(source.indexOf(",") + 1);
                        byte[] decodedString = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                        Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);

                        bitmapDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        urlDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        urlDrawable.drawable = bitmapDrawable;
                        return urlDrawable;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Handle normal URLs
                Glide.with(context)
                        .asBitmap()
                        .load(source)
                        .into(new com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                                BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), resource);
                                bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());

                                urlDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());
                                urlDrawable.drawable = bitmapDrawable;

                                // Refresh TextView after image load
                                holder.binding.txtArticleContent.setText(holder.binding.txtArticleContent.getText());
                            }

                            @Override
                            public void onLoadCleared(Drawable placeholder) {
                            }
                        });

                return urlDrawable;
            }
        };

        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(article.getHtmlContent(), Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
        } else {
            spanned = Html.fromHtml(article.getHtmlContent(), imageGetter, null);
        }

        holder.binding.txtArticleContent.setText(spanned);
        holder.binding.txtArticleContent.setMovementMethod(LinkMovementMethod.getInstance());
        holder.binding.txtArticleContent.setLinksClickable(true);


        Glide.with(context).load(ApiClient.CDN_URL_QA + article.getThumbnail())
                .transform(new RoundedCorners(1))
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(holder.binding.imageView);
        if (article.getThumbnail() != null && !article.getThumbnail().isEmpty()) {
            holder.binding.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imageView.setVisibility(View.GONE);
        }


        //holder.binding.imageView.setImageResource(article.getImageResId());

        // Hide Product cards initially
        if (article.getRecommendedProduct() != null && article.getRecommendedProduct().getSectionTitle() != null) {
            holder.binding.card1.setVisibility(View.VISIBLE);
            holder.binding.tvProductTitle.setText(article.getRecommendedProduct().getSectionTitle());
            holder.binding.txtDescCardProduct.setText(article.getRecommendedProduct().getDescription());
            holder.binding.txtPriceProduct.setText(String.format("₹ %s", article.getRecommendedProduct().getDiscountedPrice()));
            holder.binding.txtSavedPriceProduct.setText(String.format("₹ %s you save %s", article.getRecommendedProduct().getListPrice(), article.getRecommendedProduct().getTotalSavings()));
            //holder.binding.txtBtnBuyNow.setText(article.getRecommendedProduct().getButtonText());
            GlideApp.with(context).load(ApiClient.CDN_URL_QA + article.getRecommendedProduct().getImage())
                    .transform(new RoundedCorners(20))
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(holder.binding.imgThumbnailProduct);

        } else {
            holder.binding.card1.setVisibility(View.GONE);
        }

        // Service Card
        if (article.getRecommendedService() != null && article.getRecommendedService().getTitle() != null) {
            holder.binding.card2.setVisibility(View.VISIBLE);
            holder.binding.tvServiceCardTitle.setText(article.getRecommendedService().getTitle());
            holder.binding.txtDescCardService.setText(article.getRecommendedService().getDescription());
            holder.binding.txtTagService.setText(article.getRecommendedService().getTitle());

            GlideApp.with(context).load(ApiClient.CDN_URL_QA + article.getRecommendedService().getImageUrl())
                    .transform(new RoundedCorners(15))
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(holder.binding.imgThumbnailService);
            GlideApp.with(context).load(ApiClient.CDN_URL_QA + article.getRecommendedService().getModuleImageUrl())
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(holder.binding.imgTagService);
        } else {
            holder.binding.card2.setVisibility(View.GONE);
        }


        // Article Card
        if (article.getRecommendedArticle() != null && article.getRecommendedArticle().getTitle() != null) {

            /*if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("text")){

            }*/
            holder.binding.card3.setVisibility(View.GONE);
            holder.binding.card3Series.setVisibility(View.VISIBLE);
            holder.binding.card3.setVisibility(View.GONE);
            holder.binding.card3Series.setVisibility(View.VISIBLE);
            holder.binding.imgContentTypeSeries.setImageResource(R.drawable.ic_series_content);

            //holder.binding.tvTitleCardSeries.setText(article.getRecommendedArticle().getTitle());
            holder.binding.txtDescCardSeries.setText(article.getRecommendedArticle().getTitle());
            if (article.getRecommendedArticle().getThumbnail().getUrl() != null) {
                GlideApp.with(context).load(ApiClient.CDN_URL_QA + article.getRecommendedArticle().getThumbnail().getUrl())
                        .transform(new RoundedCorners(15))
                        .placeholder(R.drawable.rl_placeholder)
                        .error(R.drawable.rl_placeholder)
                        .into(holder.binding.imgThumbnailSeries);
            }

            holder.binding.txtTagSeries.setVisibility(View.VISIBLE);

            if (article.getRecommendedArticle().getViewCount() != null) {
                holder.binding.txtViewcountSeries.setText(String.valueOf(article.getRecommendedArticle().getViewCount()));
                holder.binding.txtViewcountSeries.setVisibility(View.VISIBLE);
            }

            holder.binding.imgContentTypeSeries.setImageResource(R.drawable.ic_text_content);
            if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("Audio")) {
                holder.binding.imgContentTypeVideo.setImageResource(R.drawable.ic_audio_content);
            } else if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("Video")) {
                holder.binding.imgContentTypeVideo.setImageResource(R.drawable.ic_video_content);
            } else if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("series")) {
                holder.binding.imgContentTypeVideo.setImageResource(R.drawable.ic_series_content);
            } else {
                holder.binding.imgContentTypeVideo.setImageResource(R.drawable.ic_text_content);
            }
            int color = Utils.getModuleColor(context, article.getRecommendedArticle().getModuleId());
            holder.binding.imgTagSeries.setBackgroundTintList(ColorStateList.valueOf(color));

            holder.binding.tvName.setText(article.getRecommendedArticle().getArtist().get(0).getFirstName() + " " + article.getRecommendedArticle().getArtist().get(0).getLastName());
            holder.binding.tvdateTime.setText(DateTimeUtils.convertAPIDateMonthFormat(article.getRecommendedArticle().getCreatedAt()));
            holder.binding.tvLeftTime.setText(article.getRecommendedArticle().getReadingTime() + " min read");


            holder.binding.card3Series.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("text")) {
                        Intent intent = new Intent(holder.itemView.getContext(), ArticlesDetailActivity.class);
                        intent.putExtra("contentId", article.getRecommendedArticle().getId());
                        holder.itemView.getContext().startActivity(intent);
                    } else if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("Audio")) {
                        Intent intent = new Intent(holder.itemView.getContext(), ContentDetailsActivity.class);
                        intent.putExtra("contentId", article.getRecommendedArticle().getId());
                        holder.itemView.getContext().startActivity(intent);
                    } else if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("Video")) {
                        Intent intent = new Intent(holder.itemView.getContext(), ContentDetailsActivity.class);
                        intent.putExtra("contentId", article.getRecommendedArticle().getId());
                        holder.itemView.getContext().startActivity(intent);
                    } else if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("series")) {
                        Intent intent = new Intent(holder.itemView.getContext(), SeriesListActivity.class);
                        intent.putExtra("contentId", article.getRecommendedArticle().getId());
                        holder.itemView.getContext().startActivity(intent);
                    } else {

                    }
                }
            });



/*
            if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("video") || article.getRecommendedArticle().getContentType().equalsIgnoreCase("Audio"))
            {
                holder.binding.card3.setVisibility(View.VISIBLE);

                holder.binding.tvArticlecard3TitleVideo.setText(article.getRecommendedArticle().getTitle());
                holder.binding.txtDescCard3Video.setText(article.getRecommendedArticle().getDesc());
                if (article.getRecommendedArticle().getThumbnail().getUrl()!=null) {
                    GlideApp.with(context).load(ApiClient.CDN_URL_QA + article.getRecommendedArticle().getThumbnail().getUrl())
                            .transform(new RoundedCorners(15))
                            .placeholder(R.drawable.rl_placeholder)
                            .error(R.drawable.rl_placeholder)
                            .into(holder.binding.imgThumbnailCard3Video);
                }



                if (article.getRecommendedArticle().getViewCount()!=null) {
                    holder.binding.txtViewcountVideo.setText(String.valueOf(article.getRecommendedArticle().getViewCount()));
                    holder.binding.txtViewcountVideo.setVisibility(View.VISIBLE);
                }

                if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("Audio")) {
                    holder.binding.imgContentTypeVideo.setImageResource(R.drawable.ic_audio_content);
                }else {
                    holder.binding.imgContentTypeVideo.setImageResource(R.drawable.ic_video_content);
                }

                holder.binding.card3Series.setVisibility(View.GONE);
                int color = Utils.getModuleColor(context,article.getRecommendedArticle().getModuleId());
                holder.binding.imgTagVideo.setBackgroundTintList(ColorStateList.valueOf(color));

                holder.binding.card3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), ContentDetailsActivity.class);
                        intent.putExtra("contentId", article.getRecommendedArticle().getId());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

            }else if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("series")){
                holder.binding.card3.setVisibility(View.GONE);
                holder.binding.card3Series.setVisibility(View.VISIBLE);
                holder.binding.imgContentTypeSeries.setImageResource(R.drawable.ic_series_content);

                //holder.binding.tvTitleCardSeries.setText(article.getRecommendedArticle().getTitle());
                holder.binding.txtDescCardSeries.setText(article.getRecommendedArticle().getTitle());
                if (article.getRecommendedArticle().getThumbnail().getUrl()!=null) {
                    GlideApp.with(context).load(ApiClient.CDN_URL_QA + article.getRecommendedArticle().getThumbnail().getUrl())
                            .transform(new RoundedCorners(15))
                            .placeholder(R.drawable.rl_placeholder)
                            .error(R.drawable.rl_placeholder)
                            .into(holder.binding.imgThumbnailSeries);
                }


                holder.binding.txtTagSeries.setVisibility(View.VISIBLE);

                if (article.getRecommendedArticle().getViewCount()!=null) {
                    holder.binding.txtViewcountSeries.setText(String.valueOf(article.getRecommendedArticle().getViewCount()));
                    holder.binding.txtViewcountSeries.setVisibility(View.VISIBLE);
                }

                holder.binding.imgContentTypeSeries.setImageResource(R.drawable.ic_series_content);
                int color = Utils.getModuleColor(context,article.getRecommendedArticle().getModuleId());
                holder.binding.imgTagSeries.setBackgroundTintList(ColorStateList.valueOf(color));

                holder.binding.card3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), SeriesListActivity.class);
                        intent.putExtra("contentId",article.getRecommendedArticle().getId());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });



            }else if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("text")){
                holder.binding.card3.setVisibility(View.GONE);
                holder.binding.card3Series.setVisibility(View.VISIBLE);
                holder.binding.card3.setVisibility(View.GONE);
                holder.binding.card3Series.setVisibility(View.VISIBLE);
                holder.binding.imgContentTypeSeries.setImageResource(R.drawable.ic_series_content);

                //holder.binding.tvTitleCardSeries.setText(article.getRecommendedArticle().getTitle());
                holder.binding.txtDescCardSeries.setText(article.getRecommendedArticle().getTitle());
                if (article.getRecommendedArticle().getThumbnail().getUrl()!=null) {
                    GlideApp.with(context).load(ApiClient.CDN_URL_QA + article.getRecommendedArticle().getThumbnail().getUrl())
                            .transform(new RoundedCorners(15))
                            .placeholder(R.drawable.rl_placeholder)
                            .error(R.drawable.rl_placeholder)
                            .into(holder.binding.imgThumbnailSeries);
                }

                holder.binding.txtTagSeries.setVisibility(View.VISIBLE);

                if (article.getRecommendedArticle().getViewCount()!=null) {
                    holder.binding.txtViewcountSeries.setText(String.valueOf(article.getRecommendedArticle().getViewCount()));
                    holder.binding.txtViewcountSeries.setVisibility(View.VISIBLE);
                }

                holder.binding.imgContentTypeSeries.setImageResource(R.drawable.ic_text_content);
                int color = Utils.getModuleColor(context,article.getRecommendedArticle().getModuleId());
                holder.binding.imgTagSeries.setBackgroundTintList(ColorStateList.valueOf(color));

                holder.binding.tvName.setText(article.getRecommendedArticle().getArtist().get(0).getFirstName() + " " + article.getRecommendedArticle().getArtist().get(0).getLastName());
                holder.binding.tvdateTime.setText(DateTimeUtils.convertAPIDateMonthFormat(article.getRecommendedArticle().getCreatedAt()));
                holder.binding.tvLeftTime.setText(article.getRecommendedArticle().getReadingTime() + " min read");

            }*/

        }

/*
        if (article.getRecommendedLive() != null && article.getRecommendedLive().getTitle() != null) {
            //if (article.getRecommendedArticle().getContentType().equalsIgnoreCase("video"))
            holder.binding.card1.setVisibility(View.VISIBLE);
            holder.binding.tvProductTitle.setText(article.getRecommendedLive().getSectionTitle());
            holder.binding.txtDescCardProduct.setText(article.getRecommendedLive().getSectionSubtitle());
            holder.binding.txtPriceProduct.setText(String.format("₹ %s", article.getRecommendedLive().getPriceInINR()));
            holder.binding.txtSavedPriceProduct.setText(String.format("₹%s", article.getRecommendedLive().getOriginalPriceInINR()));
            //holder.binding.txtBtnBuyNow.setText(article.getRecommendedProduct().getButtonText());
            GlideApp.with(context).load(ApiClient.CDN_URL_QA + article.getRecommendedLive().getThumbnail().getUrl())
                    .transform(new RoundedCorners(15))
                    .into(holder.binding.imgThumbnailProduct);

        } else {
            holder.binding.card1.setVisibility(View.GONE);
        }*/

        // funfact Card
        if (article.getFunFacts() != null && article.getFunFacts().getDescription() != null) {
            holder.binding.card4.setVisibility(View.VISIBLE);
            holder.binding.tvFunfactHeading.setText(article.getFunFacts().getHeading());
            holder.binding.tvFunfact.setText(article.getFunFacts().getDescription());
        } else {
            holder.binding.card4.setVisibility(View.GONE);
        }


        // Show the correct recommended  card based on the condition
      /*  if (article.getVisibleCardIndex() == 1) {
            holder.binding.card1.setVisibility(View.VISIBLE);
        } else if (article.getVisibleCardIndex() == 2) {
            holder.binding.card2.setVisibility(View.VISIBLE);
        } else if (article.getVisibleCardIndex() == 3) {
            holder.binding.card3.setVisibility(View.VISIBLE);
        } else if (article.getVisibleCardIndex() == 4) {
            holder.binding.card4.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        ArticleItemRowBinding binding;

        public ArticleViewHolder(ArticleItemRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class URLDrawable extends BitmapDrawable {
        public Drawable drawable;

        @Override
        public void draw(Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
}
