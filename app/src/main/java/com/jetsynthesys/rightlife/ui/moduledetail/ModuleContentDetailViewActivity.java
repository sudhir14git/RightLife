package com.jetsynthesys.rightlife.ui.moduledetail;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeModel;
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeResponseModel;
import com.jetsynthesys.rightlife.apimodel.modulecontentdetails.ModuleContentDetail;
import com.jetsynthesys.rightlife.apimodel.modulecontentlist.ModuleContentDetailsList;
import com.jetsynthesys.rightlife.apimodel.morelikecontent.Like;
import com.jetsynthesys.rightlife.apimodel.morelikecontent.MoreLikeContentResponse;
import com.jetsynthesys.rightlife.apimodel.welnessresponse.WellnessApiResponse;
import com.jetsynthesys.rightlife.ui.Wellness.EpisodesListAdapter2;
import com.jetsynthesys.rightlife.ui.therledit.ArtistsDetailsActivity;
import com.jetsynthesys.rightlife.ui.therledit.RLEditDetailMoreAdapter;
import com.jetsynthesys.rightlife.ui.therledit.ViewAllActivity;
import com.jetsynthesys.rightlife.ui.utility.Utils;
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModuleContentDetailViewActivity extends BaseActivity {
    public WellnessApiResponse wellnessApiResponse;
    ImageView ic_back_dialog, close_dialog;
    TextView txt_desc, tv_header_htw, txt_episodes_section;
    RelativeLayout rl_more_like_section;
    String[] itemNames;
    int[] itemImages;
    int position;
    String contentId = "";
    String contentUrl = "";
    List<Like> contentList = Collections.emptyList();
    private ModuleContentDetail ContentResponseObj;
    private RecyclerView recyclerView, recyclerViewEpisode;
    private VideoView videoView;
    private ImageButton playButton;
    private boolean isPlaying = false; // To track the current state of the player
    private PlayerView playerView;
    private ExoPlayer player;
    private ImageButton fullscreenButton;
    private ImageButton playPauseButton;
    private ImageView img_contentview, img_artist;
    private TextView tv_artistname, tvViewAll;
    private final boolean isFullscreen = false;

    // Music player
    private MediaPlayer mediaPlayer;
    private ImageButton playPauseButtonmusic;
    private final boolean isPlayingmusic = false;

    private SeekBar seekBar;
    private ProgressBar circularProgressBar;
    private TextView currentTime;
    private final Handler handler = new Handler();
    // Update progress in SeekBar and Circular Progress Bar
    private final Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int totalDuration = mediaPlayer.getDuration();

                // Update seek bar and progress bar
                seekBar.setProgress(currentPosition);
                // Update Circular ProgressBar
                int progressPercent = (int) ((currentPosition / (float) totalDuration) * 100);
                circularProgressBar.setProgress(progressPercent);


                // Update time display
                String timeFormatted = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                        TimeUnit.MILLISECONDS.toSeconds(currentPosition) % 60);
                currentTime.setText(timeFormatted);

                // Update every second
                handler.postDelayed(this, 1000);
            }
        }
    };
    private RelativeLayout rl_player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_wellness_detail_layout);

        // for music player new
        rl_player = findViewById(R.id.rl_player);
        playPauseButtonmusic = findViewById(R.id.playPauseButton);


        img_artist = findViewById(R.id.img_artist);
        tv_artistname = findViewById(R.id.tv_artistname);
        txt_episodes_section = findViewById(R.id.txt_episodes_section);
        rl_more_like_section = findViewById(R.id.rl_more_like_section);
        playerView = findViewById(R.id.exoPlayerView);
        playPauseButton = findViewById(R.id.playButton);
        img_contentview = findViewById(R.id.img_contentview);
        Intent intent = getIntent();
        String categoryType = intent.getStringExtra("Categorytype");
        position = 0;//= intent.getIntExtra("position", 0);
        contentId = intent.getStringExtra("contentId");

        tvViewAll = findViewById(R.id.tv_view_all);
        tvViewAll.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, ViewAllActivity.class);
            intent1.putExtra("ContentId", contentId);
            startActivity(intent1);
        });

// Now you can use the categoryType variable to perform actions or set up the UI


        recyclerView = findViewById(R.id.recycler_view);
        recyclerViewEpisode = findViewById(R.id.recycler_view_episode);


        ic_back_dialog = findViewById(R.id.ic_back_dialog);
        close_dialog = findViewById(R.id.ic_close_dialog);
        txt_desc = findViewById(R.id.txt_desc);
        tv_header_htw = findViewById(R.id.tv_header_htw);

        itemNames = new String[]{"Sleep Right with sounds", "Move right", "Sleep music", "Video category", "Audio 1", "Audio 2", "Audio 2", "Audio 2", "Audio 2", "Audio 2"};
        itemImages = new int[]{R.drawable.contents, R.drawable.eat_home, R.drawable.facial_scan,
                R.drawable.first_look, R.drawable.generic_02, R.drawable.meal_plan, R.drawable.generic_02,
                R.drawable.meal_plan, R.drawable.generic_02, R.drawable.meal_plan};


        //API Call
        //getContentlistdetails(categoryType);
        getContendetails(contentId);

        // get morelike content
        getMoreLikeContent(contentId);


        List<Like> contentList1 = Collections.emptyList();
        RLEditDetailMoreAdapter adapter = new RLEditDetailMoreAdapter(this, contentList1);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setAdapter(adapter);


        RLEditDetailMoreAdapter episideAdapter = new RLEditDetailMoreAdapter(this, contentList1);
        recyclerViewEpisode.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerViewEpisode.setAdapter(episideAdapter);
        // showCustomDialog();


        ic_back_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                showExitDialog();
            }
        });


        // Handle play/pause button click
        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                pausePlayer();
            } else {
                playPlayer();
            }
        });

        //--- for Audio content


    }

    private void setModuleColor(TextView txtDesc, String moduleId) {
        if (moduleId.equalsIgnoreCase("EAT_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.eatright);
            txt_desc.setBackgroundTintList(colorStateList);
        } else if (moduleId.equalsIgnoreCase("THINK_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.thinkright);
            txt_desc.setBackgroundTintList(colorStateList);
        } else if (moduleId.equalsIgnoreCase("SLEEP_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.sleepright);
            txt_desc.setBackgroundTintList(colorStateList);
        } else if (moduleId.equalsIgnoreCase("MOVE_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.moveright);
            txt_desc.setBackgroundTintList(colorStateList);
        }
    }

    private void setContentDetails(ModuleContentDetail responseObj) {
        txt_desc.setText(responseObj.getData().getDesc());
        setModuleColor(txt_desc, responseObj.getData().getModuleId());

        tv_header_htw.setText(responseObj.getData().getTitle());
//if (false)
        if (!responseObj.getData().getContentType().equalsIgnoreCase("VIDEO") && !responseObj.getData().getContentType().equalsIgnoreCase("AUDIO")) {
            img_contentview.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            Glide.with(getApplicationContext())
                    .load(ApiClient.CDN_URL_QA + responseObj.getData().getThumbnail().getUrl())
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(img_contentview);

            Log.d("Received Content type", "Received category type: " + responseObj.getData().getContentType());

        } else if (responseObj.getData().getContentType().equalsIgnoreCase("AUDIO")) {
            setupAudioContent(responseObj);
            img_contentview.setVisibility(View.GONE);
            playerView.setVisibility(View.GONE);
        } else {
            Log.d("Received Content type", "Received category type: " + responseObj.getData().getContentType());

            if (ContentResponseObj.getData().getPreviewUrl().isEmpty()) {
                finish();
            }
            playerView.setVisibility(View.VISIBLE);
            img_contentview.setVisibility(View.GONE);
            initializePlayer();
            player.setPlayWhenReady(true);
        }


        tv_artistname.setText(responseObj.getData().getArtist().get(0).getFirstName() +
                " " + responseObj.getData().getArtist().get(0).getLastName());
        Glide.with(getApplicationContext())
                .load(ApiClient.CDN_URL_QA + responseObj.getData().getArtist().get(0).getProfilePicture())
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .circleCrop()
                .into(img_artist);

        tv_artistname.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, ArtistsDetailsActivity.class);
            intent1.putExtra("ArtistId", responseObj.getData().getArtist().get(0).getId());
            startActivity(intent1);
        });

        playButton = findViewById(R.id.playButton);

        // Set up the video URL
        // Set the video URL
        String videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        Uri videoUri = Uri.parse(videoUrl);

    }

    private void setupAudioContent(ModuleContentDetail responseObj) {
        setupMusicPlayer("");
    }

    private void showExitDialog() {
        // Create the dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_exit_dialog_mind);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window = dialog.getWindow();
        // Set the dim amount
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.dimAmount = 0.7f; // Adjust the dim amount (0.0 - 1.0)
        window.setAttributes(layoutParams);

        // Find views from the dialog layout
        //ImageView dialogIcon = dialog.findViewById(R.id.img_close_dialog);
        ImageView dialogImage = dialog.findViewById(R.id.dialog_image);
        TextView dialogText = dialog.findViewById(R.id.dialog_text);
        Button dialogButtonStay = dialog.findViewById(R.id.dialog_button_stay);
        Button dialogButtonExit = dialog.findViewById(R.id.dialog_button_exit);

        // Optional: Set dynamic content
        // dialogText.setText("Please find a quiet and comfortable place before starting");

        // Set button click listener
        dialogButtonStay.setOnClickListener(v -> {
            // Perform your action
            dialog.dismiss();
            //Toast.makeText(VoiceScanActivity.this, "Scan feature is Coming Soon", Toast.LENGTH_SHORT).show();


        });
        dialogButtonExit.setOnClickListener(v -> {
            dialog.dismiss();
            this.finish();
        });

        // Show the dialog
        dialog.show();
    }

    private void getContentlistdetails(String categoryId) {
        // Make the GET request
        Call<ResponseBody> call = apiService.getContentdetailslist(
                sharedPreferenceManager.getAccessToken(),
                "THINK_RIGHT_POSITIVE_PSYCHOLOGY",
                10,
                0,
                "THINK_RIGHT"
        );

        // Handle the response
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            String successMessage = response.body().string();
                            System.out.println("Request successful: " + successMessage);
                            //Log.d("API Response", "User Details: " + response.body().toString());
                            Gson gson = new Gson();
                            String jsonResponse = gson.toJson(response.body().toString());
                            Log.d("API Response", "User Details: " + successMessage);
                            ModuleContentDetailsList ResponseObj = gson.fromJson(successMessage, ModuleContentDetailsList.class);
                            Log.d("API Response", "User Details: " + ResponseObj.getData().getContentList().size()
                                    + " " + ResponseObj.getData().getContentList().get(0).getTitle());
                            //  setupListData(ResponseObj.getData().getContentList());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorMessage = response.errorBody().string();
                            System.out.println("Request failed with error: " + errorMessage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });

    }

    //getRLDetailpage
    private void getContendetails(String categoryId) {
        Utils.showLoader(this);
        // Make the GET request
        Call<ResponseBody> call = apiService.getRLDetailpage(
                sharedPreferenceManager.getAccessToken(),
                categoryId

        );

        // Handle the response
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.dismissLoader(ModuleContentDetailViewActivity.this);
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            String successMessage = response.body().string();
                            System.out.println("Request successful: " + successMessage);
                            //Log.d("API Response", "User Details: " + response.body().toString());
                            Gson gson = new Gson();
                            String jsonResponse = gson.toJson(response.body().toString());
                            Log.d("API Response", "Content Details: " + jsonResponse);
                            ContentResponseObj = gson.fromJson(successMessage, ModuleContentDetail.class);
                            setContentDetails(ContentResponseObj);
                            getSeriesWithEpisodes(ContentResponseObj.getData().getId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorMessage = response.errorBody().string();
                            System.out.println("Request failed with error: " + errorMessage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.dismissLoader(ModuleContentDetailViewActivity.this);
                handleNoInternetView(t);
            }
        });

    }

    // more like this content
    private void getMoreLikeContent(String contentid) {
        Call<ResponseBody> call = apiService.getMoreLikeContent(sharedPreferenceManager.getAccessToken(), contentid, 0, 5);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parse the raw JSON into the LikeResponse class
                        String jsonString = response.body().string();
                        Gson gson = new Gson();

                        MoreLikeContentResponse ResponseObj = gson.fromJson(jsonString, MoreLikeContentResponse.class);
                        if (ResponseObj != null) {
                            if (!ResponseObj.getData().getLikeList().isEmpty() && ResponseObj.getData().getLikeList().size() > 0) {
                                setupListData(ResponseObj.getData().getLikeList());
                                if (ResponseObj.getData().getLikeList().size() < 5) {
                                    tvViewAll.setVisibility(View.GONE);
                                } else {
                                    tvViewAll.setVisibility(View.VISIBLE);
                                }
                            } else {
                                rl_more_like_section.setVisibility(View.GONE);
                            }
                        }


                    } catch (Exception e) {
                        Log.e("JSON_PARSE_ERROR", "Error parsing response: " + e.getMessage());
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });

    }

    private void getSeriesWithEpisodes(String seriesId) {
        Call<JsonElement> call = apiService.getSeriesWithEpisodes(sharedPreferenceManager.getAccessToken(), seriesId, true);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Wellness:episodes " + affirmationsResponse);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    EpisodeResponseModel episodeResponseModel = gson.fromJson(jsonResponse, EpisodeResponseModel.class);
                    Log.d("API Response body", "Episode:SeriesList " + episodeResponseModel.getData().getEpisodes().get(0).getTitle());
                    //setupWellnessContent(wellnessApiResponse.getData().getContentList());
                    if (episodeResponseModel != null) {
                        if (!episodeResponseModel.getData().getEpisodes().isEmpty() && episodeResponseModel.getData().getEpisodes().size() > 0) {
                            setupEpisodeListData(episodeResponseModel.getData().getEpisodes());
                        }
                    }

                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                handleNoInternetView(t);

            }
        });

    }

    private void setupListData(List<Like> contentList) {
        rl_more_like_section.setVisibility(View.VISIBLE);
        RLEditDetailMoreAdapter adapter = new RLEditDetailMoreAdapter(this, contentList);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    /*  private void setupEpisodeListData(List<Like> contentList) {
          EpisodesListAdapter adapter = new EpisodesListAdapter(this, itemNames, itemImages, contentList);
          LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
          recyclerViewEpisode.setLayoutManager(horizontalLayoutManager);
          recyclerViewEpisode.setAdapter(adapter);
      }*/
    private void setupEpisodeListData(List<EpisodeModel> contentList) {
        txt_episodes_section.setVisibility(View.VISIBLE);
        EpisodesListAdapter2 adapter = new EpisodesListAdapter2(this, itemNames, itemImages, contentList);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewEpisode.setLayoutManager(horizontalLayoutManager);
        recyclerViewEpisode.setAdapter(adapter);
    }

    // play video
    private void initializePlayer() {
        if (!ContentResponseObj.getData().getContentType().equalsIgnoreCase("VIDEO") || ContentResponseObj.getData().getContentType().equalsIgnoreCase("AUDIO")) {
            return;
        }
        // Create a new ExoPlayer instance
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);


        // Set media source (video URL)
        //Uri videoUri = Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        Uri videoUri = Uri.parse(ApiClient.CDN_URL_QA + ContentResponseObj.getData().getPreviewUrl());// responseObj.getPreviewUrl()
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this))
                .createMediaSource(MediaItem.fromUri(videoUri));


        player.setMediaSource(mediaSource);
        // Prepare the player and start playing automatically
        player.prepare();

        player.play();
    }

    private void playPlayer() {
        player.play();
        isPlaying = true;
        playPauseButton.setImageResource(R.drawable.ic_notifications_black_24dp); // Change to pause icon
    }

    private void pausePlayer() {
        player.pause();
        isPlaying = false;
        playPauseButton.setImageResource(R.drawable.ic_play); // Change to play icon
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            //  initializePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacks(updateProgress);
    }

    private void setupMusicPlayer(String S) {
        seekBar = findViewById(R.id.seekBar);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        // Set progress to 50%
        currentTime = findViewById(R.id.currentTime);
        ImageView backgroundImage = findViewById(R.id.backgroundImage);
        rl_player.setVisibility(View.VISIBLE);
        String imageUrl = "media/cms/content/series/64cb6d97aa443ed535ecc6ad/e9c5598c82c85de5903195f549a26210.jpg";

        GlideApp.with(ModuleContentDetailViewActivity.this)
                .load(ApiClient.CDN_URL_QA + imageUrl)//episodes.get(1).getThumbnail().getUrl()
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(backgroundImage);


        String previewUrl = "media/cms/content/series/64cb6d97aa443ed535ecc6ad/45ea4b0f7e3ce5390b39221f9c359c2b.mp3";
        String url = ApiClient.CDN_URL_QA + previewUrl; //episodes.get(1).getPreviewUrl();//"https://www.example.com/your-audio-file.mp3";  // Replace with your URL
        Log.d("API Response", "Sleep aid URL: " + url);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();  // Load asynchronously
        } catch (IOException e) {
            Toast.makeText(this, "Failed to load audio", Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.setOnPreparedListener(mp -> {
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration());
            isPlaying = true;
            playPauseButtonmusic.setImageResource(R.drawable.ic_sound_pause);
            // Update progress every second
            handler.post(updateProgress);
        });
        // Play/Pause Button Listener
        playPauseButtonmusic.setOnClickListener(v -> {
            if (isPlaying) {
                mediaPlayer.pause();
                playPauseButtonmusic.setImageResource(R.drawable.ic_sound_play);
                handler.removeCallbacks(updateProgress);
            } else {
                mediaPlayer.start();
                playPauseButtonmusic.setImageResource(R.drawable.ic_sound_pause);
                //updateProgress();
                handler.post(updateProgress);
            }
            isPlaying = !isPlaying;
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            Toast.makeText(this, "Playback finished", Toast.LENGTH_SHORT).show();
            handler.removeCallbacks(updateProgress);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


}
