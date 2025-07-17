package com.jetsynthesys.rightlife.ui.healthpagemain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity;
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity;
import com.jetsynthesys.rightlife.ui.Articles.ReceipeDetailActivity;
import com.jetsynthesys.rightlife.ui.rlpagemain.RLPageActivity;

public class HealthPageMainActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llHealthAudit, llHealthCam, llVoiceScan, llmindaudit,
            ll_homehealthclick,ll_homemenuclick;
    private ImageView rlmenu,img_homemenu,img_healthmenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setChildContentView(R.layout.activity_healthmain);
        llHealthAudit = findViewById(R.id.ll_health_audit);
        llHealthCam = findViewById(R.id.ll_health_cam);
        llVoiceScan = findViewById(R.id.ll_voice_scan);
        llmindaudit = findViewById(R.id.ll_mind_audit);


        llHealthAudit.setOnClickListener(this);
        llHealthCam.setOnClickListener(this);
        llVoiceScan.setOnClickListener(this);
        llmindaudit.setOnClickListener(this);



        // MENU
        rlmenu = findViewById(R.id.rlmenu);
        rlmenu.setOnClickListener(this);

        img_homemenu = findViewById(R.id.img_homemenu);
        //img_homemenu.setOnClickListener(this);
        img_healthmenu = findViewById(R.id.img_healthmenu);
        //img_healthmenu.setOnClickListener(this);


        ll_homehealthclick = findViewById(R.id.ll_homehealthclick);
        ll_homehealthclick.setOnClickListener(this);
        ll_homemenuclick = findViewById(R.id.ll_homemenuclick);
        ll_homemenuclick.setOnClickListener(this);

        /*llHealthAudit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HealthPageMainActivity.this, "Health Audit card click", Toast.LENGTH_SHORT).show();
            }
        });*/

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (view.getId() == R.id.ll_health_audit) {

            Toast.makeText(HealthPageMainActivity.this, "Health Audit card click", Toast.LENGTH_SHORT).show();
            callArticleActivity();
        } else if (view.getId() == R.id.ll_health_cam) {
            Toast.makeText(HealthPageMainActivity.this, "Health Cam card click", Toast.LENGTH_SHORT).show();
            callReceipeDeatilsActivity();
        } else if (view.getId() == R.id.ll_voice_scan) {
            Toast.makeText(HealthPageMainActivity.this, "Voice Scan card click", Toast.LENGTH_SHORT).show();

        } else if (view.getId() == R.id.ll_mind_audit) {
            Toast.makeText(HealthPageMainActivity.this, "Mind Audit card click", Toast.LENGTH_SHORT).show();
        }

        if (viewId == R.id.rlmenu) {
            //Toast.makeText(HealthPageMainActivity.this, "RLpage clicked", Toast.LENGTH_SHORT).show();
            // Start new activity here
            Intent intent = new Intent(HealthPageMainActivity.this, RLPageActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
            view.setSelected(!view.isSelected());
        } else if (viewId == R.id.img_healthmenu) {
            //Toast.makeText(HealthPageMainActivity.this, "Health Menu clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HealthPageMainActivity.this, HealthPageMainActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            //startActivity(intent);
        } else if (viewId == R.id.ll_homemenuclick) {
            //Toast.makeText(HealthPageMainActivity.this, "Home Menu clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HealthPageMainActivity.this, HomeNewActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
            finish();
        }
    }
    public void callArticleActivity(){
        // Start new activity here
        Intent intent = new Intent(HealthPageMainActivity.this, ArticlesDetailActivity.class);
        // Optionally pass data
        //intent.putExtra("key", "value");
        startActivity(intent);
    }
    public void callReceipeDeatilsActivity(){
        // Start new activity here
        Intent intent = new Intent(HealthPageMainActivity.this, ReceipeDetailActivity.class);
        // Optionally pass data
        //intent.putExtra("key", "value");
        startActivity(intent);
    }
}