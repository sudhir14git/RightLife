package com.jetsynthesys.rightlife.ui.new_design;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.databinding.ActivityHappyHaveYouBinding;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants;

@SuppressLint("CustomSplashScreen")
public class HappyToHaveYouActivity extends BaseActivity {
    private ActivityHappyHaveYouBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHappyHaveYouBinding.inflate(getLayoutInflater());
        setChildContentView(binding.getRoot());

        String username = sharedPreferenceManager.getUserName();
        binding.dialogWelcome.tvUsername.setText(username);


        new Handler().postDelayed(() -> {

            SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean(SharedPreferenceConstants.IS_LOGGED_IN, false);

            if (sharedPreferenceManager.getAccessToken() != null && isLoggedIn) {
                // User is logged in, proceed to main activity
                Intent intent = new Intent(HappyToHaveYouActivity.this, WellnessFocusListActivity.class);
                startActivity(intent);
                finish();
            }

        }, 1500);
    }
}