package com.jetsynthesys.rightlife.ui.mindaudit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.ui.CommonAPICall;
import com.jetsynthesys.rightlife.ui.mindaudit.questions.MindAuditAssessmentQuestions;
import com.jetsynthesys.rightlife.ui.mindaudit.questions.Question;
import com.jetsynthesys.rightlife.ui.mindaudit.questions.ScoringPattern;
import com.jetsynthesys.rightlife.ui.utility.AppConstants;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MAAssessmentQuestionaireActivity extends BaseActivity {
    public Button submitButton, nextButton;
    GetAssessmentScoreCASRequest CASRequest = new GetAssessmentScoreCASRequest();
    GetAssessmentScoreGad7Request gad7Request = new GetAssessmentScoreGad7Request();
    GetAssessmentScoreOHQ7Request ohq7Request = new GetAssessmentScoreOHQ7Request();
    GetAssessmentScorePHQ9Request phq9Request = new GetAssessmentScorePHQ9Request();
    GetAssessmentScoreDass21Request dass21Request = new GetAssessmentScoreDass21Request();
    private TextView tvHeader;
    private ImageView imgBack;
    private ViewPager2 viewPager;
    private Button prevButton;
    private MAAssessmentPagerAdapter adapter;
    private ProgressBar progressBar;
    private MindAuditAssessmentQuestions mindAuditAssessmentQuestions;
    private String header;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_maassessment_questions);

        tvHeader = findViewById(R.id.tv_header);
        imgBack = findViewById(R.id.ic_back_dialog);
        viewPager = findViewById(R.id.viewPager);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);

        header = getIntent().getStringExtra("AssessmentType");

        tvHeader.setText(header);

        getQuestionList(header);

        prevButton.setOnClickListener(v -> navigateToPreviousPage());
        nextButton.setOnClickListener(v -> navigateToNextPage());
        submitButton.setOnClickListener(v -> getAssessmentScoreMethod());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonVisibility(position);
                updateProgress(position);
            }
        });
        viewPager.setUserInputEnabled(false);

        imgBack.setOnClickListener(view -> finish());

        adapter = new MAAssessmentPagerAdapter(this);
        viewPager.setAdapter(adapter);


    }

    private void getAssessmentScoreMethod() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("assessment", header);
        if (header.equalsIgnoreCase(AppConstants.dass_21)) {
            requestData.put("stressLevel", dass21Request.getStressLevel());
            requestData.put("anxietyLevel", dass21Request.getAnxietyLevel());
            requestData.put("depressionSeverity", dass21Request.getDepressionSeverity());
        } else if (header.equalsIgnoreCase(AppConstants.phq_9)) {
            requestData.put("depressionSeverity", phq9Request.getDepressionSeverity());
        } else if (header.equalsIgnoreCase(AppConstants.cas)) {
            requestData.put("angerLevel", CASRequest.getAngerLevel());
        } else if (header.equalsIgnoreCase(AppConstants.gad_7)) {
            requestData.put("anxietyLevel", gad7Request.getAnxietyLevel());
        } else if (header.equalsIgnoreCase(AppConstants.ohq)) {
            requestData.put("score", ohq7Request.getScore());
        }

        getAssessmentScore(requestData);
    }

    public void addScore(Question question, ScoringPattern scoringPattern) {

        if (header.equalsIgnoreCase(AppConstants.dass_21)) {
            dass21Request.setAssessment(header);
            if (question.getScale().equalsIgnoreCase("STRESS")) {
                dass21Request.setStressLevel(dass21Request.getStressLevel() + scoringPattern.getScore());
            } else if (question.getScale().equalsIgnoreCase("ANXIETY")) {
                dass21Request.setAnxietyLevel(dass21Request.getAnxietyLevel() + scoringPattern.getScore());
            } else if (question.getScale().equalsIgnoreCase("DEPRESSION")) {
                dass21Request.setDepressionSeverity(dass21Request.getDepressionSeverity() + scoringPattern.getScore());
            }
        } else if (header.equalsIgnoreCase(AppConstants.phq_9)) {
            phq9Request.setAssessment(header);
            phq9Request.setDepressionSeverity(phq9Request.getDepressionSeverity() + scoringPattern.getScore());
        } else if (header.equalsIgnoreCase(AppConstants.cas)) {
            CASRequest.setAssessment(header);
            CASRequest.setAngerLevel(CASRequest.getAngerLevel() + scoringPattern.getScore());

        } else if (header.equalsIgnoreCase(AppConstants.gad_7)) {
            gad7Request.setAssessment(header);
            gad7Request.setAnxietyLevel(gad7Request.getAnxietyLevel() + scoringPattern.getScore());

        } else if (header.equalsIgnoreCase(AppConstants.ohq)) {
            ohq7Request.setAssessment(header);
            ohq7Request.setScore(ohq7Request.getScore() + scoringPattern.getScore());
        }
    }

    private void navigateToPreviousPage() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    public void navigateToNextPage() {
        if (viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    }


    private void submitFormData(AssessmentsTaken assessmentsTaken) {
        MindAuditAssessmentSaveRequest mindAuditAssessmentSaveRequest = SharedPreferenceManager.getInstance(this).getMindAuditRequest();

        List<AssessmentsTaken> assessmentTakens = new ArrayList<>();
        assessmentTakens.add(assessmentsTaken);
        mindAuditAssessmentSaveRequest.setAssessmentsTaken(assessmentTakens);
        UserEmotions userEmotions = SharedPreferenceManager.getInstance(this).getUserEmotions();
        mindAuditAssessmentSaveRequest.setEmotionalState(userEmotions.getEmotions());
        saveAssessment(mindAuditAssessmentSaveRequest);
    }

    private void updateButtonVisibility(int position) {
        submitButton.setVisibility(position == adapter.getItemCount() - 1 ? View.VISIBLE : View.GONE);
    }

    private void updateProgress(int fragmentIndex) {
        // Set progress percentage based on the current fragment (out of 8)
        int progressPercentage = (int) (((fragmentIndex + 1) / (double) adapter.getItemCount()) * 100);
        progressBar.setProgress(progressPercentage);
    }

    private void getQuestionList(String type) {
        Call<ResponseBody> call = apiService.getAssessmentByType(sharedPreferenceManager.getAccessToken(), type);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        Gson gson = new Gson();
                        mindAuditAssessmentQuestions = gson.fromJson(jsonString, MindAuditAssessmentQuestions.class);
                        adapter.setGroup(mindAuditAssessmentQuestions.getAssessmentQuestionnaires().getGroups().get(0));
                        adapter.notifyDataSetChanged();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(MAAssessmentQuestionaireActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

    /*private void getAssessmentResult(String assessment) {
        String accessToken = SharedPreferenceManager.getInstance(this).getAccessToken();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Call<ResponseBody> call = apiService.getMindAuditAssessmentResult(accessToken, assessment);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = null;
                    try {
                        jsonResponse = gson.toJson(response.body().string());
                        Log.d("AAAA", jsonResponse);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(MAAssessmentQuestionaireActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MAAssessmentQuestionaireActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void saveAssessment(MindAuditAssessmentSaveRequest mindAuditAssessmentSaveRequest) {
        Call<ResponseBody> call = apiService.saveMindAuditAssessment(sharedPreferenceManager.getAccessToken(), mindAuditAssessmentSaveRequest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        Gson gson = new Gson();
                        CommonAPICall.INSTANCE.postWellnessStreak(MAAssessmentQuestionaireActivity.this,"mind Audit","COMPLETE");
                        finish();
                        Intent intent = new Intent(MAAssessmentQuestionaireActivity.this, MindAuditResultActivity.class);
                        intent.putExtra("FROM", "MAAssessment");
                        intent.putExtra("Assessment", header); // pass your string or data
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(MAAssessmentQuestionaireActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }

    private void getAssessmentScore(Map<String, Object> requestData) {

        Call<ResponseBody> call = apiService.getMindAuditAssessmentScore(sharedPreferenceManager.getAccessToken(), requestData);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        Gson gson = new Gson();

                        AssessmentsTaken assessmentsTaken = gson.fromJson(jsonString, AssessmentsTaken.class);

                        submitFormData(assessmentsTaken);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(MAAssessmentQuestionaireActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }
}
