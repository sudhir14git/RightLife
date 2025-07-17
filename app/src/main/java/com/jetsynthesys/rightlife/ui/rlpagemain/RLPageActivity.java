package com.jetsynthesys.rightlife.ui.rlpagemain;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.apimodel.exploremodules.affirmations.ExploreAffirmationsListActivity;
import com.jetsynthesys.rightlife.apimodel.rlpagemain.nextdate.MindAuditNextDate;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.RLPageVoiceResult.RLPageVoiceScanResultResponse;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.continuemodela.RlPageContinueWatchResponse;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.faceScanReportForId.FacialReportData;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.faceScanReportForId.FacialScanResponse;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.journal.RLpageJournalResponse;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.scanresultfacescan.Data;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.scanresultfacescan.FaceScanPastResultResponse;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.scanresultfacescan.Report;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.uniquelyyours.UniquelyYoursResponse;
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse;
import com.jetsynthesys.rightlife.apimodel.userdata.Userdata;
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity;
import com.jetsynthesys.rightlife.ui.exploremodule.ExploreSleepSoundsActivity;
import com.jetsynthesys.rightlife.ui.healthaudit.HealthAuditActivity;
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamActivity;
import com.jetsynthesys.rightlife.ui.jounal.JournalingActivity;
import com.jetsynthesys.rightlife.ui.jounal.JournalingListActivity;
import com.jetsynthesys.rightlife.ui.mindaudit.AllAssessment;
import com.jetsynthesys.rightlife.ui.mindaudit.Assessments;
import com.jetsynthesys.rightlife.ui.mindaudit.MASuggestedAssessmentActivity;
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditActivity;
import com.jetsynthesys.rightlife.ui.mindaudit.UserEmotions;
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager;
import com.jetsynthesys.rightlife.ui.utility.Utils;
import com.jetsynthesys.rightlife.ui.voicescan.VoiceScanActivity;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RLPageActivity extends BaseActivity implements View.OnClickListener {

    LinearLayout rlmenu, ll_homemenuclick, bottom_sheet,
            ll_journal, ll_affirmations, ll_sleepsounds;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private RecyclerView recyclerViewContinue, recyclerViewrecent;
    private FloatingActionButton add_fab;
    private RelativeLayout rl_verify_view, layout_rl_journalarrow, rllayout_button_completeprofile;
    private ImageView img_homemenu, img_healthmenu, quicklinkmenu, iv_yellow_exclamation;
    private TextView txt_healthmenu, txt_recently_view_header, txt_continue_view_header;
    private CardView cardview_healthcam, cardview_healthcam_new_user, cardview_mindaudit, cardview_voicescan;
    private TextView txtuserName, txt_rldays, txt_well_streak_count, txt_next_date, txt_mindaudit_days_count;
    private Button btn_continue_healthcam, btn_recheck_health, btn_rerecord_voicescan;
    private TextView tv_title_journal, tv_journal_desc, txt_journal_date;
    private LinearLayout ll_journal_main, ll_normal_journal, ll_guided_journal;
    // Uniquely Section
    private TextView txt_section_title_uniquely, txt_title_uniquely, txt_desc_uniquely;
    private Button btn_uniquely_yours;
    private ImageView img_uniquely;
    private View cardview_mindaudit_new_user_include;
    private ImageView img_right_arrow_start_mindaudit;
    private CardView cardview_uniquely;
    // voice Scan
    private TextView txt_voicescore_rlpage;
    private LinearLayout ll_rlpage_phq9, ll_rlpage_gad7, ll_rlpage_ohq, ll_rlpage_dass21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_rlpage);
        recyclerViewContinue = findViewById(R.id.recyclerViewContinue);
        recyclerViewrecent = findViewById(R.id.recyclerViewrecent);

        txt_continue_view_header = findViewById(R.id.txt_continue_view_header);
        txt_recently_view_header = findViewById(R.id.txt_recently_view_header);

        rl_verify_view = findViewById(R.id.rl_verify_view);
        iv_yellow_exclamation = findViewById(R.id.iv_yellow_exclamation);


        rllayout_button_completeprofile = findViewById(R.id.rllayout_button_completeprofile);

        layout_rl_journalarrow = findViewById(R.id.layout_rl_journalarrow);
        rlmenu = findViewById(R.id.rlmenu);
        rlmenu.setOnClickListener(this);
        img_healthmenu = findViewById(R.id.img_healthmenu);
        txt_healthmenu = findViewById(R.id.txt_healthmenu);
        //img_homemenu.setOnClickListener(this);
        rlmenu = findViewById(R.id.rlmenu);
        rlmenu.setOnClickListener(this);
        quicklinkmenu = findViewById(R.id.quicklinkmenu);
        quicklinkmenu.setOnClickListener(this);
        ll_homemenuclick = findViewById(R.id.ll_homemenuclick);
        ll_homemenuclick.setOnClickListener(this);
        ll_journal = findViewById(R.id.ll_journal);
        ll_journal.setOnClickListener(this);
        ll_affirmations = findViewById(R.id.ll_affirmations);
        ll_affirmations.setOnClickListener(this);
        ll_sleepsounds = findViewById(R.id.ll_sleepsounds);
        ll_sleepsounds.setOnClickListener(this);
        bottom_sheet = findViewById(R.id.bottom_sheet);

        ll_rlpage_phq9 = findViewById(R.id.ll_rlpage_phq9);
        ll_rlpage_gad7 = findViewById(R.id.ll_rlpage_gad7);
        ll_rlpage_ohq = findViewById(R.id.ll_rlpage_ohq);
        ll_rlpage_dass21 = findViewById(R.id.ll_rlpage_dass21);

        ll_rlpage_phq9.setOnClickListener(this);
        ll_rlpage_gad7.setOnClickListener(this);
        ll_rlpage_ohq.setOnClickListener(this);
        ll_rlpage_dass21.setOnClickListener(this);

        // Setup ui
        btn_continue_healthcam = findViewById(R.id.btn_continue_healthcam);
        btn_continue_healthcam.setOnClickListener(this);
        btn_recheck_health = findViewById(R.id.btn_recheck_health);
        btn_recheck_health.setOnClickListener(this);

        btn_rerecord_voicescan = findViewById(R.id.btn_rerecord_voicescan);
        btn_rerecord_voicescan.setOnClickListener(this);

        cardview_healthcam_new_user = findViewById(R.id.cardview_healthcam_new_user);
        cardview_healthcam = findViewById(R.id.cardview_healthcam);
        cardview_mindaudit = findViewById(R.id.cardview_mindaudit);
        cardview_mindaudit_new_user_include = findViewById(R.id.cardview_mindaudit_new_user_include);
        cardview_mindaudit_new_user_include.setVisibility(View.GONE);
        img_right_arrow_start_mindaudit = cardview_mindaudit_new_user_include.findViewById(R.id.img_right_arrow_start_mindaudit);
        cardview_healthcam = findViewById(R.id.cardview_healthcam);
        cardview_voicescan = findViewById(R.id.cardview_voicescan);
        txt_voicescore_rlpage = findViewById(R.id.txt_voicescore_rlpage);


        txtuserName = findViewById(R.id.txtuserName);
        txt_rldays = findViewById(R.id.txt_rldays);
        txt_well_streak_count = findViewById(R.id.txt_well_streak_count);
        txt_next_date = findViewById(R.id.txt_next_date);
        txt_mindaudit_days_count = findViewById(R.id.txt_mindaudit_days_count);

        //journal card
        tv_title_journal = findViewById(R.id.tv_title_journal);
        tv_journal_desc = findViewById(R.id.tv_journal_desc);
        txt_journal_date = findViewById(R.id.txt_journal_date);
        ll_journal_main = findViewById(R.id.ll_journal_main);
        ll_guided_journal = findViewById(R.id.ll_guided_journal);
        ll_normal_journal = findViewById(R.id.ll_normal_journal);
        ll_guided_journal.setOnClickListener(this);
        ll_normal_journal.setOnClickListener(this);
        layout_rl_journalarrow.setOnClickListener(this);

        //Uniqely Card
        cardview_uniquely = findViewById(R.id.cardview_uniquely);
        cardview_uniquely = findViewById(R.id.cardview_uniquely);
        txt_section_title_uniquely = findViewById(R.id.txt_section_title_uniquely);
        txt_title_uniquely = findViewById(R.id.txt_title_uniquely);
        txt_desc_uniquely = findViewById(R.id.txt_desc_uniquely);
        img_uniquely = findViewById(R.id.img_uniquely);
        btn_uniquely_yours = findViewById(R.id.btn_uniquely_yours);
        // Api Calls

        GetMyRLContent();
        FirstLookReport();
        MyRLContinueWatching();
        MyRLJournal();
        getFaceScanResultsForId();
        getMyRLHealthCamResult();
        getMyRLGetMindAuditDate();
        ArrayList<String> userEmotionsString = new ArrayList<>();
        UserEmotions userEmotions = new UserEmotions(userEmotionsString);
        getSuggestedAssessment(userEmotions);
        MyRLRecentlyWatched();

        // get voice scan data call
        getMyRLVoiceScanCheckinResult();
        //--
        ImageView ivProfileImage = findViewById(R.id.iv_profile_image);
        UserProfileResponse userProfileResponse = SharedPreferenceManager.getInstance(this).getUserProfile();
        if (userProfileResponse == null) return;
        Userdata userdata = userProfileResponse.getUserdata();
        if (userdata != null) {
            Glide.with(this).load(ApiClient.CDN_URL_QA + userdata.getProfilePicture())
                    .placeholder(R.drawable.rl_profile)
                    .error(R.drawable.rl_profile)
                    .circleCrop()
                    .into(ivProfileImage);

            txtuserName.setText("Hi " + userdata.getFirstName());
            txt_rldays.setText(String.valueOf(userProfileResponse.getDaysCount()));
            txt_well_streak_count.setText(String.valueOf(userProfileResponse.getWellnessStreak()));
        }
        rllayout_button_completeprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        img_right_arrow_start_mindaudit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RLPageActivity.this, MindAuditActivity.class);
                startActivity(intent);
            }
        });
    }

    private void GetMyRLContent() {
        Call<ResponseBody> call = apiService.getMyRLContent(sharedPreferenceManager.getAccessToken());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " My RL Content - " + jsonString);
                        Gson gson = new Gson();
                        UniquelyYoursResponse uniquelyYoursResponse = gson.fromJson(jsonString, UniquelyYoursResponse.class);

                        HandleUniquelyYoursUI(uniquelyYoursResponse);


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

    private void HandleUniquelyYoursUI(UniquelyYoursResponse uniquelyYoursResponse) {
        if (uniquelyYoursResponse.getData() == null) {
            return;
        }
        cardview_uniquely.setVisibility(View.VISIBLE);
        txt_section_title_uniquely.setVisibility(View.VISIBLE);
        txt_section_title_uniquely.setText(uniquelyYoursResponse.getData().getSectionTitle());
        txt_title_uniquely.setText(uniquelyYoursResponse.getData().getServices().get(0).getTitle());
        txt_desc_uniquely.setText(uniquelyYoursResponse.getData().getServices().get(0).getSubtitle());

        Glide.with(this).load(ApiClient.CDN_URL_QA + uniquelyYoursResponse.getData().getServices().get(0).getThumbnail().getUrl())
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)
                .into(img_uniquely);

        btn_uniquely_yours.setText(uniquelyYoursResponse.getData().getServices().get(0).getButtonName());
    }

    private void FirstLookReport() {
        Call<ResponseBody> call = apiService.getMyRLFirstLookReport(sharedPreferenceManager.getAccessToken());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " My RL Firstlook Report - " + jsonString);
                        Gson gson = new Gson();
                        //getEmotions = gson.fromJson(jsonString, GetEmotions.class);


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

    private void MyRLContinueWatching() {
        Call<ResponseBody> call = apiService.getMyRLContinueWatching(sharedPreferenceManager.getAccessToken(), "continue", 4, 0);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " My RL Continue watching - " + jsonString);
                        Gson gson = new Gson();
                        RlPageContinueWatchResponse rlPageContinueWatchResponse = gson.fromJson(jsonString, RlPageContinueWatchResponse.class);

                        HandleContinueWatchUI(rlPageContinueWatchResponse);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

    private void HandleContinueWatchUI(RlPageContinueWatchResponse rlPageContinueWatchResponse) {
        if (rlPageContinueWatchResponse.getData().getContentDetails().size() > 0) {
            txt_continue_view_header.setVisibility(View.VISIBLE);
            RLContinueListAdapter adapter = new RLContinueListAdapter(this, rlPageContinueWatchResponse.getData().getContentDetails());
            LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerViewContinue.setLayoutManager(horizontalLayoutManager);
            recyclerViewContinue.setAdapter(adapter);
        } else {
            txt_continue_view_header.setVisibility(View.GONE);
        }

    }

    // Get Recently Watched Content List here
    private void MyRLRecentlyWatched() {

        Call<ResponseBody> call = apiService.getMyRLRecentlyWatched(sharedPreferenceManager.getAccessToken(), "recently", 4, 0);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " My RL Continue watching - " + jsonString);
                        Gson gson = new Gson();
                        RlPageContinueWatchResponse rlPageContinueWatchResponse = gson.fromJson(jsonString, RlPageContinueWatchResponse.class);

                        HandleRecentlyWatchedUI(rlPageContinueWatchResponse);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }


    private void HandleRecentlyWatchedUI(RlPageContinueWatchResponse rlPageContinueWatchResponse) {
        if (rlPageContinueWatchResponse.getData().getContentDetails().size() > 0) {
            txt_recently_view_header.setVisibility(View.VISIBLE);
            recyclerViewrecent.setVisibility(View.VISIBLE);
            RLRecentlyWatchedListAdapter adapter = new RLRecentlyWatchedListAdapter(this, rlPageContinueWatchResponse.getData().getContentDetails());
            LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerViewrecent.setLayoutManager(horizontalLayoutManager);
            recyclerViewrecent.setAdapter(adapter);
        } else {
            txt_recently_view_header.setVisibility(View.GONE);
        }

    }

    //getMyRLHealthCamResult
    private void getMyRLHealthCamResult() {

        Call<ResponseBody> call = apiService.getMyRLHealthCamResult(sharedPreferenceManager.getAccessToken());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " My RL HEalth Cam Result - " + jsonString);
                        Gson gson = new Gson();
                        //getEmotions = gson.fromJson(jsonString, GetEmotions.class);
                        // show healhcam card and hind new user card
                        cardview_healthcam.setVisibility(View.VISIBLE);
                        cardview_healthcam_new_user.setVisibility(View.GONE);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    //   Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.d("MyRLHealthCamResult", "Error:" + response.message());
                    cardview_healthcam.setVisibility(View.GONE);
                    cardview_healthcam_new_user.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
                cardview_healthcam.setVisibility(View.GONE);
                cardview_healthcam_new_user.setVisibility(View.VISIBLE);
            }
        });
    }

    //getMyRLGetMindAuditDate
    private void getMyRLGetMindAuditDate() {
        Call<ResponseBody> call = apiService.getMyRLGetMindAuditDate(sharedPreferenceManager.getAccessToken());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " My RL Mind Audit Next - " + jsonString);
                        Gson gson = new Gson();
                        MindAuditNextDate mindAuditNextDate = gson.fromJson(jsonString, MindAuditNextDate.class);

                        setMindAuditDate(mindAuditNextDate);


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    //   Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.d("MyRLHealthCamResult", "Error:" + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }


    // get voice scan Result data here // check api with backend is this the correct one

    private void getMyRLVoiceScanCheckinResult() {
        Call<ResponseBody> call = apiService.getVoiceScanCheckInData(sharedPreferenceManager.getAccessToken(), true, 0, 0);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Success RLPAGE Voice Scan check in: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " My RL Mind Audit Next - " + jsonString);
                        Gson gson = new Gson();
                        RLPageVoiceScanResultResponse rlPageVoiceScanResultResponse = gson.fromJson(jsonString, RLPageVoiceScanResultResponse.class);
                        if (rlPageVoiceScanResultResponse.getData() != null) {
                            HandleVoiceScanCardUI(rlPageVoiceScanResultResponse);
                        } else {
                            cardview_voicescan.setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    //   Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.d("MyRLHealthCamResult", "Error:" + response.message());
                    cardview_voicescan.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

    private void HandleVoiceScanCardUI(RLPageVoiceScanResultResponse rlPageVoiceScanResultResponse) {
        if (rlPageVoiceScanResultResponse.getData() != null) {
            if (rlPageVoiceScanResultResponse.getData().getScore() != null) {
                cardview_voicescan.setVisibility(View.VISIBLE);
                txt_voicescore_rlpage.setText(String.valueOf(rlPageVoiceScanResultResponse.getData().getScore()));
            } else {
                cardview_voicescan.setVisibility(View.GONE);
            }
        } else {
            cardview_voicescan.setVisibility(View.GONE);
        }

    }

    private void MyRLJournal() {
        Call<ResponseBody> call = apiService.getMyRLJournal(sharedPreferenceManager.getAccessToken(), 0, 10);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " My RL journal - " + jsonString);
                        Gson gson = new Gson();
                        RLpageJournalResponse rLpageJournalResponse = gson.fromJson(jsonString, RLpageJournalResponse.class);
                        Log.d("Response Body", " My RL journal - " + jsonString);
                        HandleJournalUI(rLpageJournalResponse);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

    private void HandleJournalUI(RLpageJournalResponse rLpageJournalResponse) {
        if (rLpageJournalResponse.getData().getJournalsList().isEmpty()) {
            ll_journal_main.setVisibility(View.GONE);
        } else {
            txt_journal_date.setText(DateTimeUtils.convertAPIDateMonthFormatWithTime(rLpageJournalResponse.getData().getJournalsList().get(0).getUpdatedAt()));
            tv_title_journal.setText(rLpageJournalResponse.getData().getJournalsList().get(0).getTitle());
            tv_journal_desc.setText(rLpageJournalResponse.getData().getJournalsList().get(0).getJournal());
        }
    }


    //Mind Audit Section
    private void getSuggestedAssessment(UserEmotions userEmotions) {

        Call<ResponseBody> call = apiService.getSuggestedAssessment(sharedPreferenceManager.getAccessToken(), userEmotions);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " My RL Mind Audit suggestion - " + jsonString);
                        Gson gson = new Gson();
                        Assessments assessments = gson.fromJson(jsonString, Assessments.class);
                        setupAssesmentsUi(assessments);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

    private void setupAssesmentsUi(Assessments assessments) {
        AllAssessment assessment = assessments.allAssessment;

    }

    private void setMindAuditDate(MindAuditNextDate mindAuditNextDate) {
        if (mindAuditNextDate != null) {
            if (mindAuditNextDate.getData().getMindAuditDateCount() != 0) {
                cardview_mindaudit.setVisibility(View.VISIBLE);
                txt_next_date.setText(DateTimeUtils.convertAPIDateMonthFormat(mindAuditNextDate.getData().getMindAuditBasicAssesmentDate()) + " " + "|" + " " + "View Detailed Report"
                );
                txt_mindaudit_days_count.setText("Next Scan In " + mindAuditNextDate.getData().getMindAuditDateCount() + " Days");
            } else {
                cardview_mindaudit.setVisibility(View.GONE);
                cardview_mindaudit_new_user_include.setVisibility(View.VISIBLE);
            }
        } else {
            cardview_mindaudit.setVisibility(View.GONE);
            cardview_mindaudit_new_user_include.setVisibility(View.VISIBLE);
        }
    }


    // On click
    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btn_rerecord_voicescan) {
            Intent intent = new Intent(RLPageActivity.this, VoiceScanActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
        } else if (viewId == R.id.btn_recheck_health) {
            Intent intent = new Intent(RLPageActivity.this, HealthAuditActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
        } else if (viewId == R.id.btn_continue_healthcam) {
            Intent intent = new Intent(RLPageActivity.this, HealthCamActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
        } else if (viewId == R.id.rlmenu) {
            //Toast.makeText(HealthPageMainActivity.this, "RLpage clicked", Toast.LENGTH_SHORT).show();
            // Start new activity here
            Intent intent = new Intent(RLPageActivity.this, RLPageActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            //startActivity(intent);
        } else if (viewId == R.id.ll_homemenuclick) {
            Intent intent = new Intent(RLPageActivity.this, HomeNewActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
            finish();
        } else if (viewId == R.id.quicklinkmenu) {
           /* if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }*/
            if (bottom_sheet.getVisibility() == View.VISIBLE) {
                bottom_sheet.setVisibility(View.GONE);
                img_healthmenu.setBackgroundResource(R.drawable.homeselected);
                txt_healthmenu.setTextColor(getResources().getColor(R.color.menuselected));
                Typeface typeface = ResourcesCompat.getFont(this, R.font.dmsans_bold);
                txt_healthmenu.setTypeface(typeface);
            } else {
                bottom_sheet.setVisibility(View.VISIBLE);
                img_healthmenu.setBackgroundColor(Color.TRANSPARENT);
                txt_healthmenu.setTextColor(getResources().getColor(R.color.txt_color_header));
                Typeface typeface = ResourcesCompat.getFont(this, R.font.dmsans_regular);
                txt_healthmenu.setTypeface(typeface);
            }
            view.setSelected(!view.isSelected());
        } else if (viewId == R.id.ll_journal) {
            //Toast.makeText(RLPageActivity.this, "journal clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RLPageActivity.this, JournalingActivity.class));
        } else if (viewId == R.id.ll_affirmations) {
            //Toast.makeText(RLPageActivity.this, "Affirmations clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RLPageActivity.this, ExploreAffirmationsListActivity.class));
        } else if (viewId == R.id.ll_sleepsounds) {
            //Toast.makeText(RLPageActivity.this, "sleepsounds clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RLPageActivity.this, ExploreSleepSoundsActivity.class));
        } else if (viewId == R.id.ll_guided_journal) {
            startActivity(new Intent(RLPageActivity.this, JournalingActivity.class));
        } else if (viewId == R.id.ll_normal_journal) {
            startActivity(new Intent(RLPageActivity.this, JournalingActivity.class));
        } else if (viewId == R.id.layout_rl_journalarrow) {
            Intent intent = new Intent(RLPageActivity.this, JournalingListActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
        } else if (viewId == R.id.ll_rlpage_phq9) {
            Intent intent = new Intent(RLPageActivity.this, MASuggestedAssessmentActivity.class);
            intent.putExtra("SelectedAssessment", "PHQ-9");
            startActivity(intent);
        } else if (viewId == R.id.ll_rlpage_dass21) {
            Intent intent = new Intent(RLPageActivity.this, MASuggestedAssessmentActivity.class);
            intent.putExtra("SelectedAssessment", "DASS-21");
            startActivity(intent);
        } else if (viewId == R.id.ll_rlpage_ohq) {
            Intent intent = new Intent(RLPageActivity.this, MASuggestedAssessmentActivity.class);
            intent.putExtra("SelectedAssessment", "OHQ");
            startActivity(intent);
        } else if (viewId == R.id.ll_rlpage_gad7) {
            Intent intent = new Intent(RLPageActivity.this, MASuggestedAssessmentActivity.class);
            intent.putExtra("SelectedAssessment", "GAD-7");
            startActivity(intent);
        }
    }

    private void getFaceScanResultsForId() {
        Call<ResponseBody> call = apiService.getScanPastReport(sharedPreferenceManager.getAccessToken(), "faceScan");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Face scan past result: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " Face scan past result - " + jsonString);

                        Gson gson = new Gson();
                        FaceScanPastResultResponse faceScanPastResultResponse = gson.fromJson(jsonString, FaceScanPastResultResponse.class);

                        if (faceScanPastResultResponse != null && faceScanPastResultResponse.getData() != null && !faceScanPastResultResponse.getData().isEmpty()) {
                            Data firstData = faceScanPastResultResponse.getData().get(0);
                            Report report = firstData.getReport();
                            double snr = report.getSnr(); // Access the SNR value
                            Utils.logDebug("Face scan past result", "AnswerId: " + firstData.getAnswerId());
                            getFacialScanReport(firstData.getAnswerId());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    //   Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.d("MyRLHealthCamResult", "Error:" + response.message());
                    cardview_voicescan.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }


    private void getFacialScanReport(String reportId) {
        Call<ResponseBody> call = apiService.getFacialScanReport(sharedPreferenceManager.getAccessToken(), reportId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RLPageActivity.this, "Face scan ONE result: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Log.d("Response Body", " Face scan ONE result - " + jsonString);

                        Gson gson = new Gson();
                        FacialScanResponse faceScanSingleResultResponse = gson.fromJson(jsonString, FacialScanResponse.class);

                        if (faceScanSingleResultResponse != null && faceScanSingleResultResponse.getData() != null) {
                            FacialReportData firstData = faceScanSingleResultResponse.getData();
                            com.jetsynthesys.rightlife.apimodel.rlpagemodels.faceScanReportForId.Report report = firstData.getReport();
                            double snr = report.getSnr(); // Access the SNR value
                            Utils.logDebug("Face scan Single result", "Diastolic: " + report.getBP_DIASTOLIC());
                            Utils.logDebug("Face scan Single result", "Systolic: " + report.getBP_SYSTOLIC());

                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    //   Toast.makeText(RLPageActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.d("MyRLHealthCamResult", "Error:" + response.message());
                    cardview_voicescan.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }
}