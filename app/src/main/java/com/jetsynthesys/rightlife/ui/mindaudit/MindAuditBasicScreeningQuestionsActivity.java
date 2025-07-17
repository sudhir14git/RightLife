package com.jetsynthesys.rightlife.ui.mindaudit;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity;

public class MindAuditBasicScreeningQuestionsActivity extends BaseActivity {
    public Button nextButton, submitButton;
    ImageView ic_back_dialog, close_dialog;
    private ViewPager2 viewPager;
    private Button prevButton;
    private ProgressBar progressBar;
    private MindAuditBasicQuestionsAdapter adapter;
    private BasicScreeningQuestion basicScreeningQuestions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_mind_audit_from);
        ic_back_dialog = findViewById(R.id.ic_back_dialog);
        close_dialog = findViewById(R.id.ic_close_dialog);
        viewPager = findViewById(R.id.viewPager);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);

        basicScreeningQuestions = (BasicScreeningQuestion) getIntent().getSerializableExtra(MindAuditFeelingsFragment.ARG_BASIC_QUESTION);

        adapter = new MindAuditBasicQuestionsAdapter(this);
        adapter.setData(basicScreeningQuestions);
        viewPager.setAdapter(adapter);

        prevButton.setOnClickListener(v -> navigateToPreviousPage());
        nextButton.setOnClickListener(v -> navigateToNextPage());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonVisibility(position);
                updateProgress(position);
            }
        });

        ic_back_dialog.setOnClickListener(view -> {
            int currentItem = viewPager.getCurrentItem();
            int totalItems = adapter.getItemCount();

            if (currentItem == 0) {
                finish();
            }
            // If on any other page, move to the previous page
            else {
                viewPager.setCurrentItem(currentItem - 1);
            }
        });
        close_dialog.setOnClickListener(view -> {
            //finish();
            showExitDialog();
        });
    }

    private void updateButtonVisibility(int position) {
        int totalItems = adapter.getItemCount();

        if (position == totalItems - 1) {
            nextButton.setText("Submit");
        } else {
            nextButton.setText("Next");
        }
    }

    private void navigateToPreviousPage() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    public void navigateToNextPage() {

        int currentItem = viewPager.getCurrentItem();
        int totalItems = adapter.getItemCount();
        // Go to the next page if it's not the last one
        if (currentItem < totalItems - 1) {
            viewPager.setCurrentItem(currentItem + 1);
        }
    }

    private void updateProgress(int fragmentIndex) {
        // Set progress percentage based on the current fragment (out of 8)
        int progressPercentage = (int) (((fragmentIndex + 1) / (double) adapter.getItemCount()) * 100);
        progressBar.setProgress(progressPercentage);
    }

    // Exit Dailog
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
            //this.finish();
            finishAffinity(); // Finishes Activity D and all activities below it in the same task
            Intent intent = new Intent(MindAuditBasicScreeningQuestionsActivity.this, HomeNewActivity.class);
            startActivity(intent);
        });

        // Show the dialog
        dialog.show();
    }
}
