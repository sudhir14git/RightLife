package com.jetsynthesys.rightlife.ui.Articles;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.apimodel.morelikecontent.Like;
import com.jetsynthesys.rightlife.apimodel.morelikecontent.MoreLikeContentResponse;
import com.jetsynthesys.rightlife.databinding.ActivityArticledetailBinding;
import com.jetsynthesys.rightlife.ui.Articles.models.Article;
import com.jetsynthesys.rightlife.ui.Articles.models.ArticleDetailsResponse;
import com.jetsynthesys.rightlife.ui.Articles.models.Artist;
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleBookmarkRequest;
import com.jetsynthesys.rightlife.ui.Articles.requestmodels.ArticleLikeRequest;
import com.jetsynthesys.rightlife.ui.CommonAPICall;
import com.jetsynthesys.rightlife.ui.YouMayAlsoLikeAdapter;
import com.jetsynthesys.rightlife.ui.therledit.EpisodeTrackRequest;
import com.jetsynthesys.rightlife.ui.therledit.ViewAllActivity;
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils;
import com.jetsynthesys.rightlife.ui.utility.Utils;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticlesDetailActivity extends BaseActivity {
    private static final String VIDEO_URL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"; // Free content URL
    private final boolean isFullscreen = false;
    // views
    ImageView ic_back_dialog, ic_save_article, iconArrow, image_like_article, image_share_article;
    TextView txt_inthisarticle, txt_inthisarticle_list, txt_article_content;
    ActivityArticledetailBinding binding;
    // video views
    private StyledPlayerView playerView;
    private PlayerControlView controlView;
    private ExoPlayer player;
    private ProgressBar progressBar;
    private ImageView fullscreenButton;
    private String contentId;
    private ArticleDetailsResponse articleDetailsResponse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_articledetail);

        binding = ActivityArticledetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ic_back_dialog = findViewById(R.id.ic_back_dialog);
        ic_save_article = findViewById(R.id.ic_save_article);
        txt_inthisarticle = findViewById(R.id.txt_inthisarticle);
        txt_inthisarticle_list = findViewById(R.id.txt_inthisarticle_list);

        iconArrow = findViewById(R.id.icon_arrow_article);

        iconArrow.setOnClickListener(v -> {
            if (txt_inthisarticle_list.getVisibility() == View.VISIBLE) {
                txt_inthisarticle_list.setVisibility(View.GONE);
                iconArrow.setRotation(360f); // Rotate by 180 degrees
            } else {
                txt_inthisarticle_list.setVisibility(View.VISIBLE);
                iconArrow.setRotation(180f); // Rotate by 180 degrees
            }
        });
        txt_inthisarticle.setOnClickListener(v -> {
            if (txt_inthisarticle_list.getVisibility() == View.VISIBLE) {
                txt_inthisarticle_list.setVisibility(View.GONE);
                iconArrow.setRotation(360f); // Rotate by 180 degrees
            } else {
                txt_inthisarticle_list.setVisibility(View.VISIBLE);
                iconArrow.setRotation(180f); // Rotate by 180 degrees
            }
        });
        ic_back_dialog.setOnClickListener(view -> finish());


        ic_save_article.setOnClickListener(view -> {
            ic_save_article.setImageResource(R.drawable.ic_save_article_active);
            // Call Save article api

            if (articleDetailsResponse.getData().getBookmarked()) {
                binding.icSaveArticle.setImageResource(R.drawable.ic_save_article);
                articleDetailsResponse.getData().setBookmarked(false);
                postArticleBookMark(articleDetailsResponse.getData().getId(), false);
            } else {
                binding.icSaveArticle.setImageResource(R.drawable.ic_save_article_active);
                articleDetailsResponse.getData().setBookmarked(true);
                postArticleBookMark(articleDetailsResponse.getData().getId(), true);
            }


        });

        binding.imageLikeArticle.setOnClickListener(v -> {
            binding.imageLikeArticle.setImageResource(R.drawable.like_article_active);
            int currentCount = getCurrentCount();
            if (articleDetailsResponse.getData().getIsLike()) {
                binding.imageLikeArticle.setImageResource(R.drawable.like_article_inactive);
                articleDetailsResponse.getData().setIsLike(false);
                postArticleLike(articleDetailsResponse.getData().getId(), false);
                int newCount = currentCount - 1;
                binding.txtLikeCount.setText(String.valueOf(Math.max(0, newCount)));
            } else {
                binding.imageLikeArticle.setImageResource(R.drawable.like_article_active);
                articleDetailsResponse.getData().setIsLike(true);
                postArticleLike(articleDetailsResponse.getData().getId(), true);
                int newCount = currentCount + 1;
                binding.txtLikeCount.setText(String.valueOf(Math.max(0, newCount)));
            }
        });



        binding.imageShareArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIntent();
            }
        });

        txt_inthisarticle_list.setText("• Introduction \n\n• Benefits \n\n• Considerations \n\n• Dosage and Side effects \n\n• Conclusion");

        contentId = getIntent().getStringExtra("contentId");
        //setVideoPlayerView();
        getArticleDetails(contentId);
        getRecommendedContent(contentId);

        binding.tvViewAll.setOnClickListener(view -> {
            Intent intent1 = new Intent(ArticlesDetailActivity.this, ViewAllActivity.class);
            intent1.putExtra("ContentId", contentId);
            startActivity(intent1);
        });
    }

    private int getCurrentCount() {
        try {
            String countText = binding.txtLikeCount.getText().toString();
            String numbersOnly = countText.replaceAll("[^0-9]", "");
            return numbersOnly.isEmpty() ? 0 : Integer.parseInt(numbersOnly);
        } catch (Exception e) {
            return 0;
        }
    }


    private void getArticleDetails(String contentId) {
        //-----------
        Utils.showLoader(this);

        // contentId = "679b1e6d4199ddf6752fdb20";
        //contentId = "67a9aeed7864652954596ecb";

        // Make the API call
        Call<JsonElement> call = apiService.getArticleDetails(sharedPreferenceManager.getAccessToken(), contentId);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement articleResponse = response.body();
                    Log.d("API Response", "Article response: " + articleResponse);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    articleDetailsResponse = gson.fromJson(jsonResponse, ArticleDetailsResponse.class);

                    Log.d("API Response body", "Article Title" + articleDetailsResponse.getData().getTitle());
                    if (articleDetailsResponse != null && articleDetailsResponse.getData() != null) {
                        handleArticleResponseData(articleDetailsResponse);
                    }
                    binding.scrollviewarticle.setVisibility(View.VISIBLE);
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                Utils.dismissLoader(ArticlesDetailActivity.this);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                handleNoInternetView(t);
                Utils.dismissLoader(ArticlesDetailActivity.this);
            }
        });

    }


    private void handleArticleResponseData(ArticleDetailsResponse articleDetailsResponse) {

        binding.tvHeaderArticle.setText(articleDetailsResponse.getData().getTitle());
        if (!articleDetailsResponse.getData().getArtist().isEmpty()) {
            Artist artist = articleDetailsResponse.getData().getArtist().get(0);
            binding.tvAuthorName.setText(String.format("%s %s", artist.getFirstName(), artist.getLastName()));
            binding.tvAuthorName.setOnClickListener(view -> {
                Intent intent = new Intent(this, ArticlesDetailActivity.class);
                intent.putExtra("ArtistId", artist.getId());
                startActivity(intent);
            });

            if (!this.isFinishing() && !this.isDestroyed())
                Glide.with(this).load(ApiClient.CDN_URL_QA + artist.getProfilePicture())
                        .transform(new RoundedCorners(25))
                        .placeholder(R.drawable.rl_profile)
                        .error(R.drawable.rl_profile)
                        .into(binding.authorImage);
        }
        binding.txtArticleDate.setText(DateTimeUtils.convertAPIDateMonthFormat(articleDetailsResponse.getData().getCreatedAt()));
        binding.txtCategoryArticle.setText(articleDetailsResponse.getData().getTags().get(0).getName());
        setModuleColor(binding.imageTag, articleDetailsResponse.getData().getModuleId());
        binding.txtReadtime.setText(articleDetailsResponse.getData().getReadingTime() + " min read");
        if (!isFinishing() && !isDestroyed()) {
            Glide.with(this).load(ApiClient.CDN_URL_QA + articleDetailsResponse.getData().getUrl())
                    .transform(new RoundedCorners(1))
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(binding.articleImageMain);
        }
        //setInThisArticleList(articleDetailsResponse.getData().getArticle());
        HandleArticleListView(articleDetailsResponse.getData().getArticle());
        if (articleDetailsResponse.getData().getTableOfContents() != null) {
            binding.llInthisarticle.setVisibility(View.VISIBLE);
            handleInThisArticle(articleDetailsResponse.getData().getTableOfContents());
        }
        // handle save icon
        if (articleDetailsResponse.getData().getBookmarked()) {
            binding.icSaveArticle.setImageResource(R.drawable.ic_save_article_active);
        } else {
            binding.icSaveArticle.setImageResource(R.drawable.ic_save_article);
        }

        if (articleDetailsResponse.getData().getIsLike()) {
            binding.imageLikeArticle.setImageResource(R.drawable.like_article_active);
        } else {
            binding.imageLikeArticle.setImageResource(R.drawable.like_article_inactive);
        }
        if (articleDetailsResponse.getData().getLikeCount() != null) {
            binding.txtLikeCount.setText(articleDetailsResponse.getData().getLikeCount().toString());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.txtKeytakeawayDesc.setText(Html.fromHtml(articleDetailsResponse.getData().getSummary(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            binding.txtKeytakeawayDesc.setText(Html.fromHtml(articleDetailsResponse.getData().getSummary()));
        }

        // article consumed
        EpisodeTrackRequest episodeTrackRequest = new EpisodeTrackRequest(sharedPreferenceManager.getUserId(), articleDetailsResponse.getData().getModuleId(),
                articleDetailsResponse.getData().getId(), "1.0", "1.0", "TEXT");
        CommonAPICall.INSTANCE.trackEpisodeOrContent(this, episodeTrackRequest);
    }

    private void handleInThisArticle(List<String> tocItems) {
        // Create a StringBuilder to build the text with bullet points
        StringBuilder textBuilder = new StringBuilder();

        // Add bullet points (•) before each item
        for (int i = 0; i < tocItems.size(); i++) {
            textBuilder.append("• ").append(tocItems.get(i));
            if (i < tocItems.size() - 1) { // Don't add newline after the last item
                textBuilder.append("\n\n");
            }
        }
        textBuilder.append("\n"); // Add final newline

        // Create a SpannableString to handle multiple spans
        SpannableString spannableString = new SpannableString(textBuilder.toString());

        //SpannableString spannableString = new SpannableString(String.join("\n\n", tocItems) + "\n");

        // Calculate the start and end indices for each item
        int start = 0;
        for (int i = 0; i < tocItems.size(); i++) {
            String item = tocItems.get(i);
            int end = start + item.length();

            // Create a ClickableSpan for each item
            final int position = i; // Capture the current index
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // Handle the click event and get the position
                    //Toast.makeText(ArticlesDetailActivity.this, "Clicked: " + tocItems.get(position) + " at position: " + position, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void updateDrawState(android.text.TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ContextCompat.getColor(ArticlesDetailActivity.this, R.color.color_in_this_article)); // Set your desired color
                    ds.setUnderlineText(false); // Remove underline if you don't want it
                }

            };

            // Set the ClickableSpan on the specific range
            spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end + 1; // Move to the next item (including the newline character)
        }

        // Set the SpannableString to the TextView
        binding.txtInthisarticleList.setText(spannableString);
        binding.txtInthisarticleList.setMovementMethod(LinkMovementMethod.getInstance()); // Enable link clicks
    }


    private void HandleArticleListView(List<Article> articleList) {
        ArticleListAdapter adapter = new ArticleListAdapter(this, articleList);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerViewArticle.setLayoutManager(horizontalLayoutManager);
        binding.recyclerViewArticle.setAdapter(adapter);
        //binding.bottomcardview.setVisibility(View.VISIBLE);
    }

    public void setModuleColor(ImageView imgtag, String moduleId) {
        if (moduleId.equalsIgnoreCase("EAT_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.eatright);
            binding.imageTag.setImageTintList(colorStateList);

        } else if (moduleId.equalsIgnoreCase("THINK_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.thinkright);
            binding.imageTag.setImageTintList(colorStateList);

        } else if (moduleId.equalsIgnoreCase("SLEEP_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.sleepright);
            binding.imageTag.setImageTintList(colorStateList);

        } else if (moduleId.equalsIgnoreCase("MOVE_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.moveright);
            binding.imageTag.setImageTintList(colorStateList);

        }
    }


    // set exoplayer videoview
    private void setVideoPlayerView() {
        playerView = findViewById(R.id.player_view);
        controlView = findViewById(R.id.control_view); // Initialize controlView
        progressBar = findViewById(R.id.progress_bar);
        fullscreenButton = findViewById(R.id.fullscreen_button);


        player = new ExoPlayer.Builder(this).build(); // Correct way to initialize

        playerView.setPlayer(player);
        controlView.setPlayer(player); // Set the player to the controlView

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(VIDEO_URL));
        player.setMediaItem(mediaItem);

        player.prepare();
        player.play(); // Autoplay
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) { // Check if player is initialized
            player.play(); // Resume playback when the activity is resumed
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause(); // Pause playback when the activity is paused
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release(); // Release the player resources
            player = null; // Important: Set player to null to avoid memory leaks
        }
    }


    private void postArticleLike(String contentId, boolean isLike) {
        ArticleLikeRequest request = new ArticleLikeRequest(contentId, isLike);
        // Make the API call
        Call<ResponseBody> call = apiService.ArticleLikeRequest(sharedPreferenceManager.getAccessToken(), request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseBody articleLikeResponse = response.body();
                    Log.d("API Response", "Article response: " + articleLikeResponse);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());


                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);

            }
        });

    }

    private void postArticleBookMark(String contentId, boolean isBookmark) {
        ArticleBookmarkRequest request = new ArticleBookmarkRequest(contentId, isBookmark);
        // Make the API call
        Call<ResponseBody> call = apiService.ArticleBookmarkRequest(sharedPreferenceManager.getAccessToken(), request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseBody articleLikeResponse = response.body();
                    Log.d("API Response", "Article Bookmark response: " + articleLikeResponse);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());


                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });

    }

    private void shareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String shareText = "“Been using this app called RightLife that tracks food, workouts, sleep, and mood. Super simple, no wearable needed.\n" +
                "                     Try it and get 7 days for free. Here’s the link:\n " + "Play Store Link  https://play.google.com/store/apps/details?id=${packageName} \n" +
                "App Store Link https://apps.apple.com/app/rightlife/id6444228850";

        intent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(intent, "Share"));
    }

    private void getRecommendedContent(String contentId) {
        // Make the API call
        Call<ResponseBody> call = apiService.getMoreLikeContent(sharedPreferenceManager.getAccessToken(), contentId, 0, 5);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        Gson gson = new Gson();
                        MoreLikeContentResponse responseObj = gson.fromJson(jsonString, MoreLikeContentResponse.class);

                        if (responseObj != null &&
                                responseObj.getData() != null &&
                                responseObj.getData().getLikeList() != null) {

                            List<Like> likeList = responseObj.getData().getLikeList();

                            if (!likeList.isEmpty()) {
                                setupListData(likeList);

                                if (likeList.size() < 5) {
                                    binding.tvViewAll.setVisibility(View.GONE);
                                } else {
                                    binding.tvViewAll.setVisibility(View.VISIBLE);
                                }
                            } else {
                                binding.txtAlsolikeHeader.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("JSON_PARSE_ERROR", "Error parsing response: " + e.getMessage());
                    }


                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });

    }

    private void setupListData(List<Like> contentList) {
        binding.txtAlsolikeHeader.setVisibility(View.VISIBLE);

        YouMayAlsoLikeAdapter adapter = new YouMayAlsoLikeAdapter(this, contentList);
        LinearLayoutManager horizontalLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        binding.recyclerViewAlsolike.setLayoutManager(horizontalLayoutManager);
        binding.recyclerViewAlsolike.setAdapter(adapter);
    }
}
