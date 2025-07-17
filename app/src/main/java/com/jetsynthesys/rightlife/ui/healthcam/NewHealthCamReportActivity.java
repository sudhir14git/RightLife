package com.jetsynthesys.rightlife.ui.healthcam;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.gson.Gson;
import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.apimodel.newreportfacescan.FacialReportResponseNew;
import com.jetsynthesys.rightlife.apimodel.newreportfacescan.HealthCamItem;
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse;
import com.jetsynthesys.rightlife.apimodel.userdata.Userdata;
import com.jetsynthesys.rightlife.databinding.ActivityNewhealthcamreportBinding;
import com.jetsynthesys.rightlife.databinding.ItemScanCircleBinding;
import com.jetsynthesys.rightlife.databinding.LayoutScanProgressBinding;
import com.jetsynthesys.rightlife.newdashboard.FacialScanReportDetailsActivity;
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity;
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity;
import com.jetsynthesys.rightlife.ui.CommonAPICall;
import com.jetsynthesys.rightlife.ui.healthcam.basicdetails.HealthCamBasicDetailsNewActivity;
import com.jetsynthesys.rightlife.ui.settings.SubscriptionHistoryActivity;
import com.jetsynthesys.rightlife.ui.utility.AppConstants;
import com.jetsynthesys.rightlife.ui.utility.ConversionUtils;
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager;
import com.jetsynthesys.rightlife.ui.utility.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewHealthCamReportActivity extends BaseActivity {
    private static final String TAG = "NewHealthCamReportActivity";
    ActivityNewhealthcamreportBinding binding;
    LayoutScanProgressBinding scanBinding;
    List<HealthCamItem> allHealthCamItems = new ArrayList<>();
    private FacialReportResponseNew facialReportResponseNew;
    private String reportId;
    private String isFrom = null;
    private ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_newhealthcamreport);
        binding = ActivityNewhealthcamreportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        scanBinding = LayoutScanProgressBinding.bind(binding.scanProgressLayout.getRoot());
        reportId = getIntent().getStringExtra("REPORT_ID");
        isFrom = getIntent().getStringExtra("FROM");

        // Register the result launcher
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            boolean returnedValue = data.getBooleanExtra("result", false);
                            getMyRLHealthCamResult();
                        }
                    }
                }
        );

        findViewById(R.id.ic_back_dialog).setOnClickListener(view -> onBackPressHandle());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressHandle();
            }
        });


        binding.icCloseDialog.setOnClickListener(v -> showDisclaimerDialog());
        binding.cardviewLastCheckin.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubscriptionPlanListActivity.class);
            intent.putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN");
            resultLauncher.launch(intent);
        });

        binding.cardviewRightlifedays.setOnClickListener(view -> startNextOverAllWellnessDetails());

        binding.imageNext.setOnClickListener(view -> startNextOverAllWellnessDetails());

        binding.btnBuyFacescan.setOnClickListener(v -> {
            // put check here if facescan remaning count is 0 buy new else Scan again
            if (facialReportResponseNew.data.boosterLimit > 0 && facialReportResponseNew.data.boosterUsed < facialReportResponseNew.data.boosterLimit) {
                startActivity(new Intent(NewHealthCamReportActivity.this, HealthCamBasicDetailsNewActivity.class));
            } else {
                Intent intent = new Intent(this, SubscriptionPlanListActivity.class);
                intent.putExtra("SUBSCRIPTION_TYPE", "FACIAL_SCAN");
                resultLauncher.launch(intent);
            }

        });
        binding.scanProgressLayout.btnScanAgain.setOnClickListener(v -> startActivity(new Intent(NewHealthCamReportActivity.this, HealthCamBasicDetailsNewActivity.class)));
        getMyRLHealthCamResult();

        UserProfileResponse userProfileResponse = SharedPreferenceManager.getInstance(this).getUserProfile();
        if (userProfileResponse == null) return;
        Userdata userdata = userProfileResponse.getUserdata();
        /*if (userdata != null) {
            binding.txtuserName.setText("Hi " + userdata.getFirstName());
        }*/
        updateChecklistStatus();
       /* if (DashboardChecklistManager.INSTANCE.getPaymentStatus()) {
            binding.cardFacescanBooster.setVisibility(View.GONE);
            binding.scanProgressLayout.scanContainer.setVisibility(View.VISIBLE);
        } else {
            binding.cardFacescanBooster.setVisibility(View.VISIBLE);
            binding.scanProgressLayout.scanContainer.setVisibility(View.GONE);
        }*/

        binding.btnSyncNow.setOnClickListener(v -> DownLaodReport(facialReportResponseNew.data.pdf));
    }

    private void startNextOverAllWellnessDetails() {
        ArrayList<ParameterModel> unifiedList = getUnifiedParameterList();
        Intent intent = new Intent(this, FacialScanReportDetailsActivity.class);
        List<HealthCamItem> healthCamItems = new ArrayList<>();
        intent.putExtra("healthCamItemList", new ArrayList<>(healthCamItems)); // Serializable list
        intent.putExtra("UNIFIED_LIST", new ArrayList<>(unifiedList)); // Serializable list
        intent.putExtra("position", 0);
        startActivity(intent);
    }

    private ArrayList<ParameterModel> getUnifiedParameterList() {
        ArrayList<ParameterModel> resultList = new ArrayList<>();
        if (facialReportResponseNew.data.overallWellnessScore != null) {
            String key = facialReportResponseNew.data.overallWellnessScore.fieldName;
            String name = facialReportResponseNew.data.overallWellnessScore.parameter;
            resultList.add(new ParameterModel(key, name));
        }
        return resultList;
    }

    private void onBackPressHandle() {
        finish();
        if (isFrom != null && !isFrom.isEmpty()) {
            Intent intent = new Intent(this, HomeNewActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("finish_healthCam", true);
            startActivity(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getMyRLHealthCamResult();
    }

    private void updateChecklistStatus() {
        CommonAPICall.INSTANCE.updateChecklistStatus(this, "vital_facial_scan", AppConstants.CHECKLIST_COMPLETED,null);
    }

    private void getMyRLHealthCamResult() {
        Utils.showLoader(this);

        Call<ResponseBody> call;
        if (reportId != null && !reportId.isEmpty())
            call = apiService.getHealthCamByReportId(sharedPreferenceManager.getAccessToken(), reportId);
        else
            call = apiService.getMyRLHealthCamResult(sharedPreferenceManager.getAccessToken());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.dismissLoader(NewHealthCamReportActivity.this);
                if (response.isSuccessful() && response.body() != null) {
                    //Toast.makeText(NewHealthCamReportActivity.this, "Success: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        String jsonString = response.body().string();
                        Gson gson = new Gson();
                        facialReportResponseNew = gson.fromJson(jsonString, FacialReportResponseNew.class);
                        HandleNewReportUI(facialReportResponseNew);
                        //  HandleContinueWatchUI(facialReportResponseNew);
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
                Utils.dismissLoader(NewHealthCamReportActivity.this);
                handleNoInternetView(t);
            }
        });
    }

    private void HandleNewReportUI(FacialReportResponseNew facialReportResponseNew) {
        if (facialReportResponseNew.success) {

            if (facialReportResponseNew.data.overallWellnessScore != null) {
                String formatted = ConversionUtils.decimalFormat0Decimal.format(facialReportResponseNew.data.overallWellnessScore.value);
                Log.d("FormattedValue", formatted);
                binding.txtWellnessScore1.setText(formatted);
                setTextAccordingToWellnessScore(facialReportResponseNew.data.overallWellnessScore.value);
                binding.halfCurveProgressBar.setProgress(facialReportResponseNew.data.overallWellnessScore.value.floatValue(), Color.parseColor("#" + facialReportResponseNew.data.overallWellnessScore.colour));
            }

            binding.txtAlertMessage.setText(facialReportResponseNew.data.summary);
            binding.tvLastReportDate.setText(DateTimeUtils.convertAPIDateMonthFormatWithTime(facialReportResponseNew.data.createdAt));
            allHealthCamItems.clear();
            if (facialReportResponseNew.data.healthCamReportByCategory != null) {

                if (facialReportResponseNew.data.healthCamReportByCategory.healthCamGood != null) {
                    List<HealthCamItem> healthCamGoodItems = facialReportResponseNew.data.healthCamReportByCategory.healthCamGood;
                    allHealthCamItems.addAll(healthCamGoodItems);
                }
                if (facialReportResponseNew.data.healthCamReportByCategory.healthCamPayAttention != null) {
                    List<HealthCamItem> healthCamPayAttentionItems = facialReportResponseNew.data.healthCamReportByCategory.healthCamPayAttention;
                    allHealthCamItems.addAll(healthCamPayAttentionItems);
                }

                // Combine the lists if you want to display them together


                HealthCamVitalsAdapter adapter = new HealthCamVitalsAdapter(this, allHealthCamItems);

                binding.recyclerViewVitalCards.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
                binding.recyclerViewVitalCards.setAdapter(adapter);
                if (facialReportResponseNew.data.limit > 0) {  //if (facialReportResponseNew.data.usedCount > 0 && facialReportResponseNew.data.limit > 0) {
                    setupScanTracker(scanBinding, facialReportResponseNew.data.usedCount, facialReportResponseNew.data.limit);
                    binding.cardFacescanBooster.setVisibility(View.VISIBLE);
                    binding.scanProgressLayout.scanContainer.setVisibility(View.VISIBLE);
                    binding.cardviewLastCheckin.setVisibility(View.GONE);
                } else {
                    binding.cardviewLastCheckin.setVisibility(View.VISIBLE);
                    binding.cardFacescanBooster.setVisibility(View.VISIBLE);
                    binding.scanProgressLayout.scanContainer.setVisibility(View.GONE);
                }
                setupBoosterTracker(facialReportResponseNew.data.boosterUsed, facialReportResponseNew.data.boosterLimit);
            } else {

            }
        }
    }

    private void setTextAccordingToWellnessScore(Double wellnessScore) {
        String message;

        if (wellnessScore >= 0 && wellnessScore <= 19.99) {
            message = "Your wellness needs urgent attention—let’s work on getting you back on track.";
        } else if (wellnessScore <= 40.0) {
            message = "Your wellness is under strain—time to make a few adjustments for improvement.";
        } else if (wellnessScore <= 60.0) {
            message = "You’re doing okay, but there’s room to optimize your health and resilience.";
        } else if (wellnessScore <= 80.0) {
            message = "Great job! Your vitals are strong, and you’re building a solid foundation.";
        } else if (wellnessScore <= 100.0) {
            message = "You're thriving! Your vitals are at their peak—keep up the fantastic work.";
        } else {
            message = "Invalid wellness score.";
        }

        binding.txtWellStreak.setText(message);
    }

    /*private void HandleContinueWatchUI(FacialReportResponseNew facialReportResponseNew) {
        if (!facialReportResponseNew.data.recommendation.isEmpty()) {
            HealthCamRecommendationAdapter adapter = new HealthCamRecommendationAdapter(this, facialReportResponseNew.data.recommendation);
            LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            binding.recyclerViewContinue.setLayoutManager(horizontalLayoutManager);
            binding.recyclerViewContinue.setAdapter(adapter);
        }
    }*/

    private void showDisclaimerDialog() {
        // Create the dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_disclaimer_facescan_result);
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
        });
        // Show the dialog
        dialog.show();
    }


    // new Booster ui
    private void setupBoosterTracker(int boosterUsed, int boosterLimit) {
        if ((boosterLimit-boosterUsed) == 0) {
            //binding.txtBoosterCount.setText("0");
            binding.txtBoosterCount.setVisibility(View.GONE);
            binding.txtRemainingMsg.setText("Need More Scans? \nBuy booster packs anytime.");
            binding.viewDivider.setVisibility(View.GONE);
        } else if (boosterLimit > 0 && boosterUsed < boosterLimit) {
            binding.btnBuyFacescan.setText("Scan Again");
            binding.txtBoosterCount.setText(String.valueOf(boosterLimit - boosterUsed));
            binding.viewDivider.setVisibility(View.VISIBLE);
            binding.txtRemainingMsg.setText("Your scans remaining.");
        }
    }

    // new scan ui
    private void setupScanTracker(LayoutScanProgressBinding layout, int usedCount, int limit) {
        layout.scanIndicators.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(layout.getRoot().getContext());

        for (int i = 1; i <= limit; i++) {
            ItemScanCircleBinding itemBinding = ItemScanCircleBinding.inflate(inflater);

            itemBinding.circleNumber.setText(String.valueOf(i));

            if (i <= usedCount) {
                itemBinding.circleImage.setImageResource(R.drawable.ic_checklist_complete);
                itemBinding.circleNumber.setTextColor(ContextCompat.getColor(layout.getRoot().getContext(), android.R.color.black));
                itemBinding.txtGreenDot.setVisibility(View.VISIBLE);
            } else {
                itemBinding.circleImage.setImageResource(R.drawable.ic_checklist_tick_bg);
                itemBinding.circleNumber.setTextColor(ContextCompat.getColor(layout.getRoot().getContext(), android.R.color.black));
                itemBinding.txtGreenDot.setVisibility(View.INVISIBLE);
            }

            layout.scanIndicators.addView(itemBinding.getRoot());
        }
        if (usedCount == 1) {
            layout.infoText.setText("One scan a week is all you need to stay on top of your vitals.");
        } else {
            layout.infoText.setText("One scan a week is all you need to stay on top of your vitals.");
            // layout.infoText.setText("One step closer to better health! Your scans refresh monthly—stay on track!");
        }
        if (usedCount == limit) {
            layout.buttonText.setText("Scan Again");
            layout.btnScanAgain.setVisibility(View.GONE);
            layout.infoText.setText("One step closer to better health! Your scans refresh monthly—stay on track!");
            binding.cardFacescanBooster.setVisibility(View.VISIBLE);
        } else {
            layout.buttonText.setText("Scan Again");
            layout.btnScanAgain.setVisibility(View.VISIBLE);
        }
        layout.btnScanAgain.setOnClickListener(v -> {
            if (layout.buttonText.getText().toString().equalsIgnoreCase("Scan Again @ 99")) {
                startActivity(new Intent(NewHealthCamReportActivity.this, SubscriptionHistoryActivity.class));
            } else {
                startActivity(new Intent(NewHealthCamReportActivity.this, HealthCamBasicDetailsNewActivity.class));
            }
        });

    }

    private void DownLaodReport(String pdf) {
        String pdfUrl = pdf;//"https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl));
        request.setTitle("Downloading PDF");
        request.setDescription("Downloading file, please wait...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "sample.pdf");

// Enqueue download
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
        Toast.makeText(this, "Download Started...", Toast.LENGTH_SHORT).show();
    }

}
