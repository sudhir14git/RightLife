package com.jetsynthesys.rightlife.ui.Wellness;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeDetail.ContentData;
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeDetail.EpisodeDetailContentResponse;
import com.jetsynthesys.rightlife.apimodel.Episodes.EpisodeDetail.NextEpisode;
import com.jetsynthesys.rightlife.databinding.ActivitySeriesepisodeDetailLayoutBinding;
import com.jetsynthesys.rightlife.ui.CommonAPICall;
import com.jetsynthesys.rightlife.ui.therledit.ArtistsDetailsActivity;
import com.jetsynthesys.rightlife.ui.therledit.ViewCountRequest;
import com.jetsynthesys.rightlife.ui.utility.Utils;
import com.jetsynthesys.rightlife.ui.utility.svgloader.GlideApp;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeriesEpisodeDetailActivity extends BaseActivity {
    private final Handler handler = new Handler();
    private final String contentTypeForTrack = "";
    ActivitySeriesepisodeDetailLayoutBinding binding;
    String seriesId = "";
    String episodeId = "";
    private MediaPlayer mediaPlayer;
    // Update progress in SeekBar and Circular Progress Bar
    private final Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int totalDuration = mediaPlayer.getDuration();

                // Update seek bar and progress bar
                binding.seekBar.setProgress(currentPosition);
                // Update Circular ProgressBar
                int progressPercent = (int) ((currentPosition / (float) totalDuration) * 100);
                binding.circularProgressBar.setProgress(progressPercent);


                // Update time display
                String timeFormatted = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                        TimeUnit.MILLISECONDS.toSeconds(currentPosition) % 60);
                binding.currentTime.setText(timeFormatted);

                // Update every second
                handler.postDelayed(this, 1000);
            }
        }
    };
    private boolean isPlaying = false; // To track the current state of the player
    private ExoPlayer player;
    private PlayerView playerView;
    private EpisodeDetailContentResponse ContentResponseObj;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeriesepisodeDetailLayoutBinding.inflate(getLayoutInflater());
        setChildContentView(binding.getRoot());
        findViewById(R.id.ic_back_dialog).setOnClickListener(view -> {
            finish();
        });

        seriesId = getIntent().getStringExtra("seriesId");
        episodeId = getIntent().getStringExtra("episodeId");
        getSeriesDetails(seriesId, episodeId);

        ViewCountRequest viewCountRequest = new ViewCountRequest();
        viewCountRequest.setId(seriesId);
        viewCountRequest.setUserId(sharedPreferenceManager.getUserId());
        CommonAPICall.INSTANCE.updateViewCount(this, viewCountRequest);

        // Handle play/pause button click
        binding.playButton.setOnClickListener(v -> {
            if (isPlaying) {
                pausePlayer();
            } else {
                playPlayer();
            }
        });
    }

    private void playPlayer() {
        player.play();
        isPlaying = true;
        binding.playButton.setImageResource(R.drawable.ic_notifications_black_24dp); // Change to pause icon
    }

    private void pausePlayer() {
        player.pause();
        isPlaying = false;
        binding.playButton.setImageResource(R.drawable.ic_play); // Change to play icon
    }

    private void getSeriesDetails(String seriesId, String episodeId) {

        Utils.showLoader(this);

        Call<ResponseBody> call = apiService.getSeriesEpisodesDetails(
                sharedPreferenceManager.getAccessToken(),
                seriesId,
                episodeId
        );

        // Handle the response
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.dismissLoader(SeriesEpisodeDetailActivity.this);
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            String successMessage = response.body().string();
                            System.out.println("Request successful: " + successMessage);
                            //Log.d("API Response", "User Details: " + response.body().toString());
                            Gson gson = new Gson();
                            String jsonResponse = gson.toJson(response.body().toString());
                            Log.d("API Response", "Content Details: " + jsonResponse);
                            ContentResponseObj = gson.fromJson(successMessage, EpisodeDetailContentResponse.class);
                            setContentDetails(ContentResponseObj);
                            setupVideoContent(ContentResponseObj);
                            //getSeriesWithEpisodes(ContentResponseObj.getData().getId());
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
                Utils.dismissLoader(SeriesEpisodeDetailActivity.this);
                handleNoInternetView(t);
            }
        });

    }

    private void setContentDetails(EpisodeDetailContentResponse contentResponseObj) {


        binding.tvHeaderHtw.setText(contentResponseObj.data.title);
        binding.txtDesc.setText(contentResponseObj.data.desc);
        setModuleColor(binding.txtDesc, contentResponseObj.data.moduleId);

        /*binding.tvArtistname.setText(contentResponseObj.data.artist.get(0).firstName +
                " " + contentResponseObj.data.artist.get(0).lastName);*/
        setArtistname(contentResponseObj);
        Glide.with(getApplicationContext())
                .load(ApiClient.CDN_URL_QA + contentResponseObj.data.artist.get(0).profilePicture)
                .placeholder(R.drawable.rl_profile)
                .error(R.drawable.rl_profile)
                .circleCrop()
                .into(binding.imgArtist);

        binding.tvArtistname.setOnClickListener(view -> {
            Intent intent1 = new Intent(this, ArtistsDetailsActivity.class);
            intent1.putExtra("ArtistId", contentResponseObj.data.artist.get(0)._id);
            startActivity(intent1);
        });


        if (contentResponseObj.data.nextEpisode != null) {

            NextEpisode nextEpisode = contentResponseObj.data.nextEpisode;
            binding.txtEpisodesSection.setText("Next Episode" + contentResponseObj.data.episodeNumber);
            binding.itemText.setText(nextEpisode.title); // Use the same TextView for the title
            Glide.with(this)
                    .load(ApiClient.CDN_URL_QA + nextEpisode.thumbnail.url)
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .into(binding.itemImage); // Use the same ImageView for the thumbnail
            // ... (set other views for the next episode using the same IDs)
        } else {
            // Handle case where there is no next episode
            binding.llNextEpisode.setVisibility(View.GONE);
            binding.txtEpisodesSection.setVisibility(View.GONE);
        }
    }

    private void setArtistname(EpisodeDetailContentResponse contentResponseObj) {
        if (binding != null && binding.tvArtistname != null && contentResponseObj != null
                && contentResponseObj.data != null && contentResponseObj.data.artist != null
                && !contentResponseObj.data.artist.isEmpty()) {

            String name = "";
            if (contentResponseObj.data.artist.get(0).firstName != null) {
                name = contentResponseObj.data.artist.get(0).firstName;
            }
            if (contentResponseObj.data.artist.get(0).lastName != null) {
                name += (name.isEmpty() ? "" : " ") + contentResponseObj.data.artist.get(0).lastName;
            }

            binding.tvArtistname.setText(name);
        } else if (binding != null && binding.tvArtistname != null) {
            binding.tvArtistname.setText(""); // or set some default value
        }
    }

    private String extractVideoId(String youtubeUrl) {
        try {
            URI uri = new URI(youtubeUrl);
            String query = uri.getQuery();

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("v")) {
                        return keyValue[1];
                    }
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setupVideoContent(EpisodeDetailContentResponse contentResponseObj) {
        //binding.exoPlayerView.setVisibility(View.VISIBLE);
        binding.imgContentview.setVisibility(View.GONE);
        //initializePlayer();
        //player.setPlayWhenReady(true);
        if (contentResponseObj != null && contentResponseObj.data != null && contentResponseObj.data.youtubeUrl != null && !contentResponseObj.data.youtubeUrl.isEmpty()) {
            String videoId = extractVideoId(contentResponseObj.data.youtubeUrl);

            if (videoId != null) {
                Log.e("YouTube", "video ID - call player" + videoId);
                setupYouTubePlayer(videoId);
                //getLifecycle().addObserver(binding.youtubevideoPlayer);

            } else {
                Log.e("YouTube", "Invalid video ID");
                //Provide user feedback
            }
        } else if (contentResponseObj.data.type.equalsIgnoreCase("AUDIO")) {
            setupAudioContent(contentResponseObj);
            binding.imgContentview.setVisibility(View.GONE);
            binding.exoPlayerView.setVisibility(View.GONE);
        } else {
            Log.d("Received Content type", "Received category type: " + contentResponseObj.data.type);

            if (ContentResponseObj.data.previewUrl.isEmpty()) {
                finish();
            }
            binding.exoPlayerView.setVisibility(View.VISIBLE);
            binding.imgContentview.setVisibility(View.GONE);
            initializePlayer();
            player.setPlayWhenReady(true);
        }


    }

    private void setupYouTubePlayer(String videoId) {
        binding.youtubevideoPlayer.setVisibility(View.VISIBLE);
        binding.youtubevideoPlayer.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f);
                Log.d("YouTube", "Video loaded: " + videoId);
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                Log.d("YouTube", "Player state changed: " + state);
                if (state == PlayerConstants.PlayerState.UNSTARTED) {
                    Log.e("YouTube", "Player error");
                    //Handle the error
                }
                super.onStateChange(youTubePlayer, state);
            }
        });
    }

    // play video
    private void initializePlayer() {
        if (!ContentResponseObj.data.type.equalsIgnoreCase("VIDEO")) {
            return;
        }
        // Create a new ExoPlayer instance
        player = new ExoPlayer.Builder(this).build();
        binding.exoPlayerView.setPlayer(player);


        // Set media source (video URL)
        //Uri videoUri = Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        Uri videoUri = Uri.parse(ContentResponseObj.data.youtubeUrl);// responseObj.getPreviewUrl()
        //MediaItem mediaItem = MediaItem.fromUri(videoUri);
        //player.setMediaItem(mediaItem);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this))
                .createMediaSource(MediaItem.fromUri(videoUri));


        player.setMediaSource(mediaSource);
        // Prepare the player and start playing automatically
        player.prepare();

        player.play();

        int currentPosition = (int) player.getCurrentPosition();
        int totalDuration = (int) player.getDuration();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                long totalDuration = player.getDuration();
                long sixtyPercentDuration = (long) (totalDuration * 0.6);
                long currentPosition = player.getCurrentPosition();

            }
        });


    }

    // Audio content in series // note  -  need to ask why cant we make details response common for all type of content
    private void setupAudioContent(EpisodeDetailContentResponse responseObj) {
        setupMusicPlayer(responseObj);
    }

    private void setupMusicPlayer(EpisodeDetailContentResponse responseObj) {


        // Set progress to 50%

        ImageView backgroundImage = findViewById(R.id.backgroundImage);
        binding.rlPlayer.setVisibility(View.VISIBLE);
        String imageUrl = responseObj.data.thumbnail.url;   // "media/cms/content/series/64cb6d97aa443ed535ecc6ad/e9c5598c82c85de5903195f549a26210.jpg";

        GlideApp.with(SeriesEpisodeDetailActivity.this)
                .load(ApiClient.CDN_URL_QA + imageUrl)//episodes.get(1).getThumbnail().getUrl()
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(backgroundImage);


        String previewUrl = responseObj.data.previewUrl;//"media/cms/content/series/64cb6d97aa443ed535ecc6ad/45ea4b0f7e3ce5390b39221f9c359c2b.mp3";
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
            binding.seekBar.setMax(mediaPlayer.getDuration());
            isPlaying = true;
            // Update progress every second
            handler.post(updateProgress);
        });
        // Play/Pause Button Listener
        binding.playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                mediaPlayer.pause();
                binding.playPauseButton.setImageResource(R.drawable.ic_sound_play);
                handler.removeCallbacks(updateProgress);
            } else {
                mediaPlayer.start();
                binding.playPauseButton.setImageResource(R.drawable.ic_sound_pause);
                //updateProgress();
                handler.post(updateProgress);
            }
            isPlaying = !isPlaying;
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            Toast.makeText(this, "Playback finished", Toast.LENGTH_SHORT).show();
            handler.removeCallbacks(updateProgress);
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    // set background color of module
    private void setModuleColor(TextView txtDesc, String moduleId) {
        if (moduleId.equalsIgnoreCase("EAT_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.eatright);
            binding.txtDesc.setBackgroundTintList(colorStateList);
        } else if (moduleId.equalsIgnoreCase("THINK_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.thinkright);
            binding.txtDesc.setBackgroundTintList(colorStateList);
        } else if (moduleId.equalsIgnoreCase("SLEEP_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.sleepright);
            binding.txtDesc.setBackgroundTintList(colorStateList);
        } else if (moduleId.equalsIgnoreCase("MOVE_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.moveright);
            binding.txtDesc.setBackgroundTintList(colorStateList);
        }
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
            callTrackAPI((double) player.getCurrentPosition() / 1000);
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            callTrackAPI((double) mediaPlayer.getCurrentPosition() / 1000);
            mediaPlayer.release();
        }
        handler.removeCallbacks(updateProgress);
    }

    private void callTrackAPI(double watchDuration) {
        ContentData contentData = ContentResponseObj.data;
        if ((contentData.meta.duration - watchDuration) > 10)
            CommonAPICall.INSTANCE.postSeriesContentPlayedProgress(
                    this,
                    contentData.meta.duration,
                    contentData.moduleId,
                    watchDuration,
                    contentData.moduleId,
                    contentData.type,
                    contentData._id
            );
    }
}
