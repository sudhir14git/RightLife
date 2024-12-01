package com.example.rlapp.ui;

import static com.example.rlapp.ui.utility.DateConverter.convertToDate;
import static com.example.rlapp.ui.utility.DateConverter.convertToTime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.rlapp.R;
import com.example.rlapp.RetrofitData.ApiClient;
import com.example.rlapp.RetrofitData.ApiService;
import com.example.rlapp.apimodel.Affirmations;
import com.example.rlapp.apimodel.PromotionResponse;
import com.example.rlapp.apimodel.liveevents.LiveEventResponse;
import com.example.rlapp.apimodel.rledit.RightLifeEditResponse;
import com.example.rlapp.apimodel.servicepane.ServicePaneResponse;
import com.example.rlapp.apimodel.submodule.SubModuleResponse;
import com.example.rlapp.apimodel.upcomingevents.UpcomingEventResponse;
import com.example.rlapp.apimodel.userdata.UserProfileResponse;
import com.example.rlapp.apimodel.welnessresponse.ContentWellness;
import com.example.rlapp.apimodel.welnessresponse.WellnessApiResponse;
import com.example.rlapp.ui.Wellness.WellnessDetailViewActivity;
import com.example.rlapp.ui.exploremodule.ExploreModuleListActivity;
import com.example.rlapp.ui.healthaudit.HealthAuditActivity;
import com.example.rlapp.ui.healthcam.HealthCamActivity;
import com.example.rlapp.ui.healthpagemain.HealthPageMainActivity;
import com.example.rlapp.ui.mindaudit.MindAuditActivity;
import com.example.rlapp.ui.rlpagemain.RLPageActivity;
import com.example.rlapp.ui.therledit.RLEditDetailViewActivity;
import com.example.rlapp.ui.utility.SharedPreferenceConstants;
import com.example.rlapp.ui.utility.SharedPreferenceManager;
import com.example.rlapp.ui.voicescan.VoiceScanActivity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.zhpan.bannerview.BannerViewPager;
import com.zhpan.bannerview.constants.PageStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private ViewPager2 viewPager;
    private CircularCardAdapter adapter;
    private List<CardItem> cardItems;  // Replace with your data model
    private Handler sliderHandler;     // Handler for scheduling auto-slide
    private Runnable sliderRunnable;   // Runnable for auto-slide logic
    BannerViewPager mViewPager;
    private TestAdapter testAdapter;
    public WellnessApiResponse wellnessApiResponse;
    public RightLifeEditResponse rightLifeEditResponse;
    public SubModuleResponse ThinkRSubModuleResponse;
    public SubModuleResponse MoveRSubModuleResponse;
    public SubModuleResponse EatRSubModuleResponse;
    public SubModuleResponse SleepRSubModuleResponse;

    // Live Classes /workshop
    private CardView liveclasscardview;
    private ImageView liveclass_banner_image,img_attending_filled,img_lvclass_host;
    private TextView liveclass_workshop_tag1,liveclass_tv_classattending,tv_classtime,tv_classrating,txt_lvclass_host,tv_title_liveclass,lvclass_date,lvclass_month;


    //Button
    private Button btn_tr_explore, btn_mr_explore, btn_er_explore, btn_sr_explore;
    //RLEdit
    TextView tv_rledt_cont_title1, tv_rledt_cont_title2, tv_rledt_cont_title3,
            nameeditor, nameeditor1, nameeditor2, count, count1, count2;
    ImageView img_rledit, img_rledit1, img_rledit2, img_contenttype_rledit;
    RelativeLayout relative_rledit3, relative_rledit2, relative_rledit1;
    RelativeLayout relative_wellness1, relative_wellness2, relative_wellness3, relative_wellness4;

    TextView tv_header_rledit, tv_description_rledit,tv_header_lvclass,tv_desc_lvclass,
            tv_header_servcepane1, tv_header_servcepane2, tv_header_servcepane3, tv_header_servcepane4;
    LinearLayout ll_health_cam, ll_mind_audit, ll_health_audit, ll_voice_scan;
    LinearLayout ll_thinkright_category, ll_moveright_category, ll_eatright_category, ll_sleepright_category,
            ll_homehealthclick, ll_homemenuclick;
    LinearLayout ll_thinkright_category1, ll_thinkright_category2, ll_thinkright_category3, ll_thinkright_category4;
    LinearLayout ll_moveright_category1, ll_moveright_category2, ll_moveright_category3;
    LinearLayout ll_eatright_category1, ll_eatright_category2, ll_eatright_category3, ll_eatright_category4;
    LinearLayout ll_sleepright_category1, ll_sleepright_category2, ll_sleepright_category3;
    ImageView rlmenu, img_homemenu, img_healthmenu;
    ImageView img1, img2, img3, img4, img5, img6, img7, img8;
    TextView tv1_header, tv1, tv2_header, tv2, tv3_header, tv3, tv4_header, tv4;
    TextView tv1_viewcount, tv2_viewcount, tv3_viewcount, tv4_viewcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        SetupviewsIdWellness();


        // RL Edit
        tv_rledt_cont_title1 = findViewById(R.id.tv_rledt_cont_title1);
        tv_rledt_cont_title2 = findViewById(R.id.tv_rledt_cont_title2);
        tv_rledt_cont_title3 = findViewById(R.id.tv_rledt_cont_title3);
        nameeditor = findViewById(R.id.nameeditor);
        nameeditor1 = findViewById(R.id.nameeditor1);
        nameeditor2 = findViewById(R.id.nameeditor2);
        count = findViewById(R.id.count);
        count1 = findViewById(R.id.count1);
        count2 = findViewById(R.id.count2);

        relative_rledit3 = findViewById(R.id.relative_rledit3);
        relative_rledit2 = findViewById(R.id.relative_rledit2);
        relative_rledit1 = findViewById(R.id.relative_rledit1);

        relative_rledit3.setOnClickListener(this);
        relative_rledit2.setOnClickListener(this);
        relative_rledit1.setOnClickListener(this);

        relative_wellness1 = findViewById(R.id.relative_wellness1);
        relative_wellness2 = findViewById(R.id.relative_wellness2);
        relative_wellness3 = findViewById(R.id.relative_wellness3);
        relative_wellness4 = findViewById(R.id.relative_wellness4);

        relative_wellness1.setOnClickListener(this);
        relative_wellness2.setOnClickListener(this);
        relative_wellness3.setOnClickListener(this);
        relative_wellness4.setOnClickListener(this);


        img_rledit = findViewById(R.id.img_rledit);
        img_rledit1 = findViewById(R.id.img_rledit1);
        img_rledit2 = findViewById(R.id.img_rledit2);
        img_contenttype_rledit = findViewById(R.id.img_contenttype_rledit);


        // Modules  find ids
        ll_thinkright_category1 = findViewById(R.id.ll_thinkright_category1);
        ll_thinkright_category2 = findViewById(R.id.ll_thinkright_category2);
        ll_thinkright_category3 = findViewById(R.id.ll_thinkright_category3);
        ll_thinkright_category4 = findViewById(R.id.ll_thinkright_category4);

        ll_moveright_category1 = findViewById(R.id.ll_moveright_category1);
        ll_moveright_category2 = findViewById(R.id.ll_moveright_categor2);
        ll_moveright_category3 = findViewById(R.id.ll_moveright_category3);

        ll_eatright_category1 = findViewById(R.id.ll_eatright_category1);
        ll_eatright_category2 = findViewById(R.id.ll_eatright_category2);
        ll_eatright_category3 = findViewById(R.id.ll_eatright_category3);
        ll_eatright_category4 = findViewById(R.id.ll_eatright_category4);

        ll_sleepright_category1 = findViewById(R.id.ll_sleepright_category1);
        ll_sleepright_category2 = findViewById(R.id.ll_sleepright_category2);
        ll_sleepright_category3 = findViewById(R.id.ll_sleepright_category3);

        btn_tr_explore = findViewById(R.id.btn_tr_explore);
        btn_mr_explore = findViewById(R.id.btn_mr_explore);
        btn_er_explore = findViewById(R.id.btn_er_explore);
        btn_sr_explore = findViewById(R.id.btn_sr_explore);


        // set click listener

        ll_thinkright_category1.setOnClickListener(this);
        ll_thinkright_category2.setOnClickListener(this);
        ll_thinkright_category3.setOnClickListener(this);
        ll_thinkright_category4.setOnClickListener(this);

        ll_moveright_category1.setOnClickListener(this);
        ll_moveright_category2.setOnClickListener(this);
        ll_moveright_category3.setOnClickListener(this);

        ll_eatright_category1.setOnClickListener(this);
        ll_eatright_category2.setOnClickListener(this);
        ll_eatright_category3.setOnClickListener(this);
        ll_eatright_category4.setOnClickListener(this);

        ll_sleepright_category1.setOnClickListener(this);
        ll_sleepright_category2.setOnClickListener(this);
        ll_sleepright_category3.setOnClickListener(this);

        btn_sr_explore.setOnClickListener(this);
        btn_tr_explore.setOnClickListener(this);
        btn_er_explore.setOnClickListener(this);
        btn_mr_explore.setOnClickListener(this);


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

        //service pane
        tv_header_rledit = findViewById(R.id.tv_header_rledit);
        tv_description_rledit = findViewById(R.id.tv_description_rledit);
        tv_header_servcepane1 = findViewById(R.id.tv_header_servcepane1);
        tv_header_servcepane2 = findViewById(R.id.tv_header_servcepane2);
        tv_header_servcepane3 = findViewById(R.id.tv_header_servcepane3);
        tv_header_servcepane4 = findViewById(R.id.tv_header_servcepane4);

        //live Class
        tv_header_lvclass = findViewById(R.id.tv_header_lvclass);
        tv_desc_lvclass = findViewById(R.id.tv_desc_lvclass);
        liveclasscardview = findViewById(R.id.liveclasscardview);
        liveclass_banner_image = findViewById(R.id.banner_image);
        img_attending_filled = findViewById(R.id.img_attending_filled);
        img_lvclass_host = findViewById(R.id.img_lvclass_host);
        lvclass_month = findViewById(R.id.lvclass_month);
        lvclass_date = findViewById(R.id.lvclass_date);

        liveclass_workshop_tag1= findViewById(R.id.workshop_tag1);
        tv_title_liveclass = findViewById(R.id.tv_title_liveclass);
        liveclass_tv_classattending = findViewById(R.id.tv_classattending);
        tv_classtime = findViewById(R.id.tv_classtime);
        tv_classrating = findViewById(R.id.tv_classrating);
        txt_lvclass_host = findViewById(R.id.txt_lvclass_host);



        ll_voice_scan = findViewById(R.id.ll_voice_scan);
        ll_health_cam = findViewById(R.id.ll_health_cam);
        ll_mind_audit = findViewById(R.id.ll_mind_audit);
        ll_health_audit = findViewById(R.id.ll_health_audit);

        ll_thinkright_category = findViewById(R.id.ll_thinkright_category);
        ll_moveright_category = findViewById(R.id.ll_moveright_category);
        ll_eatright_category = findViewById(R.id.ll_eatright_category);
        ll_sleepright_category = findViewById(R.id.ll_sleepright_category);

        viewPager = findViewById(R.id.viewPager);
        mViewPager = findViewById(R.id.banner_viewpager);
        cardItems = getCardItems();  // Initialize your data list here
        adapter = new CircularCardAdapter(HomeActivity.this, cardItems);
  /*      adapter = new CircularCardAdapter(cardItems, new CircularCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                // Handle click event here
                Toast.makeText(HomeActivity.this, "Clicked: " + item, Toast.LENGTH_SHORT).show();
            }
        });*/
        viewPager.setAdapter(adapter);
        testAdapter = new TestAdapter(cardItems);


        // Set up the initial position for circular effect
        int initialPosition = Integer.MAX_VALUE / 2;
        viewPager.setCurrentItem(initialPosition - initialPosition % cardItems.size(), false);

        // Set offscreen page limit and page margin
        viewPager.setOffscreenPageLimit(5);  // Load adjacent pages
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);

        // Set up custom PageTransformer to show partial visibility of next and previous cards
        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            private static final float MIN_SCALE = 0.85f;
            private static final float MIN_ALPHA = 0.5f;

            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setZ(0);
                // Ensure center card is on top
                if (position == 0) {
                    page.setZ(1);  // Bring center card to the top
                } else {
                    page.setZ(0);  // Push other cards behind
                }
                // Set alpha based on the card's position:
                if (position <= -1 || position >= 1) {
                    // Far off-screen cards (adjacent cards)
                    page.setAlpha(0.5f);  // Make adjacent cards 50% transparent
                } else if (position == 0) {
                    // The center card (focused card)
                    page.setAlpha(1f);  // Make the center card fully opaque
                } else {
                    // Cards between center and adjacent
                    page.setAlpha(0.9f + (1 - Math.abs(position)) * 0.9f);  // Gradual transparency
                }

                float scaleFactor = 0.80f + (1 - Math.abs(position)) * 0.20f;
                page.setScaleY(scaleFactor);
                page.setScaleX(scaleFactor);

                // Adjust translation for left/right stacking
                page.setTranslationX(-position * page.getWidth() / 5.9f);


            }
        });

        // Initialize Handler and Runnable for Auto-Sliding
        sliderHandler = new Handler(Looper.getMainLooper());
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = viewPager.getCurrentItem() + 1;  // Move to the next item
                Log.d("Next Item", "Next Item: " + viewPager.getCurrentItem());
                viewPager.setCurrentItem(nextItem, true);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(this, 5000);  // Change slide every 3 seconds
            }
        };

        sliderHandler.removeCallbacks(sliderRunnable);
        // Start Auto-Sliding
        sliderHandler.postDelayed(sliderRunnable, 5000);  // Start sliding after 3 seconds


        setupViewPager();


        // calls for APIs
        getUserDetails("");
        getPromotionList("");
        getPromotionList2(""); // Service pane
        getRightlifeEdit("");
         getAffirmations("");
        // getUpcomingEvents("");
        getLiveEvents("");
       // getUpcomingLiveEvents("");

        getWelnessPlaylist("");
        getCuratedContent("");
        getModuleContent("");


        getSubModuleContent("THINK_RIGHT");
        getSubModuleContent("MOVE_RIGHT");
        getSubModuleContent("EAT_RIGHT");
        getSubModuleContent("SLEEP_RIGHT");

        //getQuestionerList("");

        getContentlist("");   // api to get module
        getContentlistdetails("");
        getContentlistdetailsfilter("");

        ll_voice_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start new activity here
                Intent intent = new Intent(HomeActivity.this, VoiceScanActivity.class);
                // Optionally pass data
                //intent.putExtra("key", "value");
                startActivity(intent);
            }
        });
        ll_health_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, HealthCamActivity.class);
                // Optionally pass data
                //intent.putExtra("key", "value");
                startActivity(intent);
            }
        });
        ll_mind_audit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// Start new activity here
                Intent intent = new Intent(HomeActivity.this, MindAuditActivity.class);
                // Optionally pass data
                //intent.putExtra("key", "value");
                startActivity(intent);
            }
        });
        ll_health_audit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start new activity here
                Intent intent = new Intent(HomeActivity.this, HealthAuditActivity.class);
                // Optionally pass data
                //intent.putExtra("key", "value");
                startActivity(intent);
            }
        });


        printAccessToken();


    }

    private void SetupviewsIdWellness() {
        //Wellness list Vewsids
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img4 = findViewById(R.id.img4);
        img5 = findViewById(R.id.img5);
        img6 = findViewById(R.id.img6);
        img7 = findViewById(R.id.img7);
        img8 = findViewById(R.id.img8);
        //-------
        // Initialize TextView variables using findViewById
        tv1_header = findViewById(R.id.tv1_header);
        tv1 = findViewById(R.id.tv1);
        tv2_header = findViewById(R.id.tv2_header);
        tv2 = findViewById(R.id.tv2);
        tv3_header = findViewById(R.id.tv3_header);
        tv3 = findViewById(R.id.tv3);
        tv4_header = findViewById(R.id.tv4_header);
        tv4 = findViewById(R.id.tv4);

        tv1_viewcount = findViewById(R.id.tv1_viewcount);
        tv2_viewcount = findViewById(R.id.tv2_viewcount);
        tv3_viewcount = findViewById(R.id.tv3_viewcount);
        tv4_viewcount = findViewById(R.id.tv4_viewcount);
    }

    private String printAccessToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);
        Log.d("AccessToken", "Success: TOKEN " + accessToken);
        return accessToken;
    }

    private void getPromotionList(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getPromotionList(accessToken, "HOME_PAGE", "THINK_RIGHT", "TOP");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement promotionResponse2 = response.body();
                    Log.d("API Response", "Success: " + promotionResponse2.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    PromotionResponse promotionResponse = gson.fromJson(jsonResponse, PromotionResponse.class);
                    Log.d("API Response body", "Success: promotion " + jsonResponse);
                    if (promotionResponse.getSuccess()) {
                        Toast.makeText(HomeActivity.this, "Success: " + promotionResponse.getStatusCode(), Toast.LENGTH_SHORT).show();
                        Log.d("API Response", "Image Urls: " + promotionResponse.getPromotiondata().getPromotionList().get(0).getContentUrl());
                        //  adapter.updateData(cardItems);
                        handlePromotionResponse(promotionResponse);
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed: " + promotionResponse.getStatusCode(), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }

    private void handlePromotionResponse(PromotionResponse promotionResponse) {

        if (promotionResponse.getPromotiondata().getPromotionList() != null && !promotionResponse.getPromotiondata().getPromotionList().isEmpty()) {
            cardItems.clear();
        }
        for (int i = 0; i < promotionResponse.getPromotiondata().getPromotionList().size(); i++) {
            Log.d("API image", "Image : " + promotionResponse.getPromotiondata().getPromotionList().get(i).getContentUrl());
            CardItem cardItem = new CardItem(promotionResponse.getPromotiondata().getPromotionList().get(i).getName(),
                    R.drawable.facialconcept,
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getThumbnail().getUrl(),
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getContent(),
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getButtonName(),
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getCategory(),
                    String.valueOf(promotionResponse.getPromotiondata().getPromotionList().get(i).getViews())
            );
            cardItems.add(i, cardItem);
        }
        Log.e("API image List", "list : " + cardItems.size());
        adapter = new CircularCardAdapter(HomeActivity.this, cardItems);
        viewPager.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }

    private void setupViewPager() {
        // Initialize the ViewPager
        mViewPager = findViewById(R.id.banner_viewpager);

        // Set up the ViewPager
        mViewPager.setAdapter(testAdapter);
        // mViewPager.setLifecycleRegistry(getLifecycle());
        mViewPager.setPageStyle(PageStyle.MULTI_PAGE);
        mViewPager.create(cardItems);
    }

    private List<CardItem> getCardItems() {
        List<CardItem> items = new ArrayList<>();
        // Add your CardItem instances here
        items.add(new CardItem("Card 1", R.drawable.facialconcept, "", "", "scan now", "", ""));
        items.add(new CardItem("Card 2", R.drawable.facialconcept, "", "", "scan now", "", ""));
        items.add(new CardItem("Card 3", R.drawable.facialconcept, "", "", "scan now", "", ""));
        items.add(new CardItem("Card 4", R.drawable.facialconcept, "", "", "scan now", "", ""));
        items.add(new CardItem("Card 5", R.drawable.facialconcept, "", "", "scan now", "", ""));
        return items;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-slide when activity is not visible
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume auto-slide when activity is visible
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up to prevent memory leaks
        sliderHandler.removeCallbacks(sliderRunnable);
    }


    //second API
    private void getPromotionList2(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getPromotionList2(accessToken);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement promotionResponse2 = response.body();
                    Log.d("API Response", "SErvice Pane: " + promotionResponse2.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    ServicePaneResponse ResponseObj = gson.fromJson(jsonResponse, ServicePaneResponse.class);
                    Log.d("API Response body", "Success: Servicepane" + ResponseObj.getData().getHomeServices().get(0).getTitle());
                    handleServicePaneResponse(ResponseObj);


                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }

    private void handleServicePaneResponse(ServicePaneResponse responseObj) {
        //  tv_header_rledit.setText(responseObj.getData().getTitle());
        // tv_description_rledit.setText(responseObj.getData().getSubtitle());
        if (!responseObj.getData().getHomeServices().isEmpty()) {
            for (int i = 0; i < responseObj.getData().getHomeServices().size(); i++) {
                switch (i) {
                    case 0:
                        //  tv_header_servcepane1.setText(responseObj.getData().getHomeServices().get(i).getTitle());
                        break;
                    case 1:
                        //tv_header_servcepane2.setText(responseObj.getData().getHomeServices().get(i).getTitle());
                        break;
                    case 2:
                        //tv_header_servcepane3.setText(responseObj.getData().getHomeServices().get(i).getTitle());
                        break;
                    case 3:
                        //tv_header_servcepane4.setText(responseObj.getData().getHomeServices().get(i).getTitle());
                        break;
                }
            }

        }
    }


    // get Affirmation list

    private void getAffirmations(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getAffirmationList(accessToken);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Affirmation list: " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    Affirmations ResponseObj = gson.fromJson(jsonResponse, Affirmations.class);
                    Log.d("API Response body", "Success: " + ResponseObj.getAffirmationList().get(0).getTitle());

                } else {
                    //Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }


//Get RightLife Edit

    private void getRightlifeEdit(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getRightlifeEdit(accessToken, "HOME");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "RightLife Edit list: " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    RightLifeEditResponse ResponseObj = gson.fromJson(jsonResponse, RightLifeEditResponse.class);
                    rightLifeEditResponse = gson.fromJson(jsonResponse, RightLifeEditResponse.class);
                    Log.d("API Response body", "c " + ResponseObj.getData().getTopList().get(0).getDesc());
                    setupRLEditContent(rightLifeEditResponse);
                } else {
                    int statusCode = response.code();
                    try {
                        String errorMessage = response.errorBody().string();
                        Log.e("Error", "HTTP error code: " + statusCode + ", message: " + errorMessage);
                        Log.e("Error", "Error message: " + errorMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }

    private void setupRLEditContent(RightLifeEditResponse rightLifeEditResponse) {

        if (rightLifeEditResponse != null && rightLifeEditResponse.getData() != null
                && rightLifeEditResponse.getData().getTopList() != null
                && !rightLifeEditResponse.getData().getTopList().isEmpty()) {

            tv_rledt_cont_title1.setText(rightLifeEditResponse.getData().getTopList().get(0).getDesc());
            nameeditor.setText(rightLifeEditResponse.getData().getTopList().get(0).getArtist().get(0).getFirstName()
                    + " " + rightLifeEditResponse.getData().getTopList().get(0).getArtist().get(0).getLastName());
            count.setText("" + rightLifeEditResponse.getData().getTopList().get(0).getViewCount());

            if (rightLifeEditResponse.getData().getTopList().get(0).getContentType().equalsIgnoreCase("VIDEO")) {
                img_contenttype_rledit.setImageResource(R.drawable.ic_playrledit);
            } else {
                img_contenttype_rledit.setImageResource(R.drawable.read);
            }

            Glide.with(getApplicationContext())
                    .load(ApiClient.CDN_URL_QA + rightLifeEditResponse.getData().getTopList().get(0).getThumbnail().getUrl())
                    .placeholder(R.drawable.img_logintop) // Replace with your placeholder image
                    .into(img_rledit);


            tv_rledt_cont_title2.setText(rightLifeEditResponse.getData().getTopList().get(1).getTitle());
            nameeditor1.setText(rightLifeEditResponse.getData().getTopList().get(1).getArtist().get(0).getFirstName()
                    + " " + rightLifeEditResponse.getData().getTopList().get(1).getArtist().get(0).getLastName());
            count1.setText("" + rightLifeEditResponse.getData().getTopList().get(1).getViewCount());
            Glide.with(getApplicationContext())
                    .load(ApiClient.CDN_URL_QA + rightLifeEditResponse.getData().getTopList().get(1).getThumbnail().getUrl())
                    .placeholder(R.drawable.img_logintop) // Replace with your placeholder image
                    .error(R.drawable.img_logintop)
                    .transform(new RoundedCorners(25))
                    .into(img_rledit1);


            tv_rledt_cont_title3.setText(rightLifeEditResponse.getData().getTopList().get(2).getTitle());
            nameeditor2.setText(rightLifeEditResponse.getData().getTopList().get(2).getArtist().get(0).getFirstName()
                    + " " + rightLifeEditResponse.getData().getTopList().get(2).getArtist().get(0).getLastName());
            count2.setText("" + rightLifeEditResponse.getData().getTopList().get(2).getViewCount());
            Glide.with(getApplicationContext())
                    .load(ApiClient.CDN_URL_QA + rightLifeEditResponse.getData().getTopList().get(2).getThumbnail().getUrl())
                    .placeholder(R.drawable.img_logintop) // Replace with your placeholder image
                    .error(R.drawable.img_logintop)
                    .transform(new RoundedCorners(25))
                    .into(img_rledit2);
        }

    }

    //Upcoming Event List -
    private void getUpcomingLiveEvents(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getUpcomingLiveEvent(accessToken,"LIVE_EVENT", "UPCOMING", "HOME");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Upcoming Event list: " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    UpcomingEventResponse ResponseObj = gson.fromJson(jsonResponse, UpcomingEventResponse.class);
                    Log.d("API Response body eVEnt", "Success:RLEventComing " + ResponseObj.getData().get(0).getEventDate().getDate() + ResponseObj.getData().get(0).getTitle());

                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }


    //WElness PlayList
    private void getWelnessPlaylist(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getWelnessPlaylist(accessToken, "SERIES", "WELLNESS");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Wellness Play list: " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    wellnessApiResponse = gson.fromJson(jsonResponse, WellnessApiResponse.class);
                    Log.d("API Response body", "Wellness:RLEdit " + wellnessApiResponse.getData().getContentList().get(0).getTitle());
                    setupWellnessContent(wellnessApiResponse.getData().getContentList());

                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }


    private void setupWellnessContent(List<ContentWellness> contentList) {
        if (contentList == null || contentList.size() < 4) return;

        // Bind data for item 1
        bindContentToView(contentList.get(0), tv1_header, tv1, img1, tv1_viewcount, img5);

        // Bind data for item 2
        bindContentToView(contentList.get(1), tv2_header, tv2, img2, tv2_viewcount, img6);

        // Bind data for item 3
        bindContentToView(contentList.get(2), tv3_header, tv3, img3, tv3_viewcount, img7);

        // Bind data for item 4
        bindContentToView(contentList.get(3), tv4_header, tv4, img4, tv4_viewcount, img8);
    }

    //Bind Wellnes content
    private void bindContentToView(ContentWellness content, TextView header, TextView category, ImageView thumbnail, TextView viewcount, ImageView imgcontenttype) {
        // Set title in the header TextView
        header.setText(content.getTitle());
        viewcount.setText("" + content.getViewCount());
        // Set categoryName in the category TextView
        category.setText(content.getCategoryName());

        // Load thumbnail using Glide
        Glide.with(this)
                .load(ApiClient.CDN_URL_QA + content.getThumbnail().getUrl()) // URL of the thumbnail
                .placeholder(R.drawable.img_logintop) // Optional placeholder
                .error(R.drawable.img_logintop)
                .transform(new RoundedCorners(25))// Optional error image
                .into(thumbnail);


    }


    private void getLiveEvents(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getLiveEvent(accessToken,"HOME");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Live Events list: " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    LiveEventResponse ResponseObj = gson.fromJson(jsonResponse, LiveEventResponse.class);
                    Log.d("API Response body", "Success:AuthorName " + ResponseObj.getData().getEvents().get(0).getEventType());
                    setupLiveEvent(ResponseObj);
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }

    private void setupLiveEvent(LiveEventResponse responseObj) {
        liveclass_workshop_tag1.setText(responseObj.getData().getEvents().get(0).getEventType());
        tv_title_liveclass.setText(responseObj.getData().getEvents().get(0).getTitle());
        txt_lvclass_host.setText(String.format("%s %s", responseObj.getData().getEvents().get(0).getInstructor().getFirstName(),
                responseObj.getData().getEvents().get(0).getInstructor().getLastName()));
        liveclass_tv_classattending.setText(String.valueOf( responseObj.getData().getEvents().get(0).getParticipantsCount()));
        String formattedDate = convertToDate(responseObj.getData().getEvents().get(0).getEndDateTime());
        //tv_classrating.setText(String.valueOf(responseObj.getData().getEvents().get(1).getRating()));
        if (formattedDate != null) {
            String[] dateParts = formattedDate.split(" ");

            // Set the day and month in separate TextViews
            lvclass_date.setText(dateParts[0]);   // "20"
            lvclass_month.setText(dateParts[1]); // "Nov"
        }
        String formattedtime = convertToTime(responseObj.getData().getEvents().get(0).getEndDateTime());
        tv_classtime.setText(formattedtime);

        Glide.with(getApplicationContext())
                .load(ApiClient.CDN_URL_QA + responseObj.getData().getEvents().get(0).getThumbnail().getUrl())
                .placeholder(R.drawable.img_logintop) // Replace with your placeholder image
                .into(liveclass_banner_image);

        tv_header_lvclass.setText(responseObj.getData().getSectionTitle());
        tv_desc_lvclass.setText(responseObj.getData().getSectionSubtitle());
    }
//getCuratedContent


    private void getCuratedContent(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getCuratedContent(accessToken);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Curated  Content list: " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    // LiveEventResponse ResponseObj = gson.fromJson(jsonResponse,LiveEventResponse.class);
                    //Log.d("API Response body", "Success:AuthorName " + ResponseObj.getData().get(0).getAuthorName());

                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }

    // get Module list
    private void getModuleContent(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getmodule(accessToken);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Module list - : " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    // LiveEventResponse ResponseObj = gson.fromJson(jsonResponse,LiveEventResponse.class);
                    //Log.d("API Response body", "Success:AuthorName " + ResponseObj.getData().get(0).getAuthorName());

                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }


    //"THINK_RIGHT", "CATEGORY", "ygjh----g"
    private void getQuestionerList(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getsubmoduletest(accessToken, "HEALTH_REPORT");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "SUB subModule list - : " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    // LiveEventResponse ResponseObj = gson.fromJson(jsonResponse,LiveEventResponse.class);
                    //Log.d("API Response body", "Success:AuthorName " + ResponseObj.getData().get(0).getAuthorName());

                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }

    @Override
    public void onClick(View view) {

        int viewId = view.getId();
        if (viewId == R.id.rlmenu) {
            //Toast.makeText(HomeActivity.this, "Button 1 clicked", Toast.LENGTH_SHORT).show();
            // Start new activity here
            Intent intent = new Intent(HomeActivity.this, RLPageActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
        } else if (viewId == R.id.ll_homehealthclick) {
            //Toast.makeText(HomeActivity.this, "TextView clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, HealthPageMainActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
            finish();
        } else if (viewId == R.id.img_homemenu) {
            //  Toast.makeText(HomeActivity.this, "ImageView clicked", Toast.LENGTH_SHORT).show();
        }

        if (viewId == R.id.ll_thinkright_category1) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", ThinkRSubModuleResponse.getData().get(0).getCategoryId());
            intent.putExtra("moduleId", ThinkRSubModuleResponse.getData().get(0).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_thinkright_category2) {
            ThinkRSubModuleResponse.getData().get(1).getName();
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", ThinkRSubModuleResponse.getData().get(1).getCategoryId());
            intent.putExtra("moduleId", ThinkRSubModuleResponse.getData().get(1).getModuleId());
            startActivity(intent);

        } else if (viewId == R.id.ll_thinkright_category3) {
            ThinkRSubModuleResponse.getData().get(2).getName();
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", ThinkRSubModuleResponse.getData().get(2).getCategoryId());
            intent.putExtra("moduleId", ThinkRSubModuleResponse.getData().get(2).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_thinkright_category4) {
            ThinkRSubModuleResponse.getData().get(3).getName();
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", ThinkRSubModuleResponse.getData().get(3).getCategoryId());
            intent.putExtra("moduleId", ThinkRSubModuleResponse.getData().get(3).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_moveright_category1) {

            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", MoveRSubModuleResponse.getData().get(0).getCategoryId());
            intent.putExtra("moduleId", MoveRSubModuleResponse.getData().get(0).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_moveright_categor2) {

            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", MoveRSubModuleResponse.getData().get(1).getCategoryId());
            intent.putExtra("moduleId", MoveRSubModuleResponse.getData().get(1).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_moveright_category3) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", MoveRSubModuleResponse.getData().get(2).getCategoryId());
            intent.putExtra("moduleId", MoveRSubModuleResponse.getData().get(2).getModuleId());
            startActivity(intent);

        } else if (viewId == R.id.ll_eatright_category1) {

            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", EatRSubModuleResponse.getData().get(0).getCategoryId());
            intent.putExtra("moduleId", EatRSubModuleResponse.getData().get(0).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_eatright_category2) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", EatRSubModuleResponse.getData().get(1).getCategoryId());
            intent.putExtra("moduleId", EatRSubModuleResponse.getData().get(1).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_eatright_category3) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", EatRSubModuleResponse.getData().get(2).getCategoryId());
            intent.putExtra("moduleId", EatRSubModuleResponse.getData().get(2).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_eatright_category4) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", EatRSubModuleResponse.getData().get(3).getCategoryId());
            intent.putExtra("moduleId", EatRSubModuleResponse.getData().get(3).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_sleepright_category1) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", SleepRSubModuleResponse.getData().get(0).getCategoryId());
            intent.putExtra("moduleId", SleepRSubModuleResponse.getData().get(0).getModuleId());
            startActivity(intent);

        } else if (viewId == R.id.ll_sleepright_category2) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", SleepRSubModuleResponse.getData().get(1).getCategoryId());
            intent.putExtra("moduleId", SleepRSubModuleResponse.getData().get(1).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_sleepright_category3) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", SleepRSubModuleResponse.getData().get(2).getCategoryId());
            intent.putExtra("moduleId", SleepRSubModuleResponse.getData().get(2).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.relative_rledit3) {
            CallRlEditDetailActivity(2);
        } else if (viewId == R.id.relative_rledit2) {
            CallRlEditDetailActivity(1);
        } else if (viewId == R.id.relative_rledit1) {
            CallRlEditDetailActivity(0);
        } else if (viewId == R.id.relative_wellness1) {
            CallWellnessDetailActivity(0);
        } else if (viewId == R.id.relative_wellness2) {
            CallWellnessDetailActivity(1);
        } else if (viewId == R.id.relative_wellness3) {
            CallWellnessDetailActivity(2);
        } else if (viewId == R.id.relative_wellness4) {
            CallWellnessDetailActivity(3);
        } else if (viewId == R.id.btn_tr_explore) {
            CallExploreModuleActivity(ThinkRSubModuleResponse);
        } else if (viewId == R.id.btn_mr_explore) {
            CallExploreModuleActivity(MoveRSubModuleResponse);
        } else if (viewId == R.id.btn_er_explore) {
            CallExploreModuleActivity(EatRSubModuleResponse);
        } else if (viewId == R.id.btn_sr_explore) {
            CallExploreModuleActivity(SleepRSubModuleResponse);
        }


    }

    private void CallRlEditDetailActivity(int position) {
        Gson gson = new Gson();
        String json = gson.toJson(rightLifeEditResponse);
        Intent intent = new Intent(HomeActivity.this, RLEditDetailViewActivity.class);
        intent.putExtra("Categorytype", json);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void CallWellnessDetailActivity(int position) {
        Gson gson = new Gson();
        String json = gson.toJson(wellnessApiResponse);
        Intent intent = new Intent(HomeActivity.this, WellnessDetailViewActivity.class);
        intent.putExtra("responseJson", json);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void CallExploreModuleActivity(SubModuleResponse responseJson) {
        Gson gson = new Gson();
        String json = gson.toJson(responseJson);
        Intent intent = new Intent(HomeActivity.this, ExploreModuleListActivity.class);
        intent.putExtra("responseJson", json);
        //intent.putExtra("position", position);
        startActivity(intent);
    }

    // ----- Test API

    private void getSubModuleContent(String moduleid) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getsubmodule(accessToken, moduleid, "CATEGORY");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "SUB subModule list - : " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    // Log.d("API Response", "SUB subModule list - : " + jsonResponse);


                    if (moduleid.equalsIgnoreCase("THINK_RIGHT")) {
                        ThinkRSubModuleResponse = gson.fromJson(affirmationsResponse.toString(), SubModuleResponse.class);
                        Log.d("API Response body", "Success:AuthorName " + ThinkRSubModuleResponse.getData().get(0).getName());
                    } else if (moduleid.equalsIgnoreCase("MOVE_RIGHT")) {
                        MoveRSubModuleResponse = gson.fromJson(affirmationsResponse.toString(), SubModuleResponse.class);
                        Log.d("API Response body", "Success:AuthorName " + MoveRSubModuleResponse.getData().get(0).getName());
                    } else if (moduleid.equalsIgnoreCase("EAT_RIGHT")) {
                        EatRSubModuleResponse = gson.fromJson(affirmationsResponse.toString(), SubModuleResponse.class);
                    } else if (moduleid.equalsIgnoreCase("SLEEP_RIGHT")) {
                        SleepRSubModuleResponse = gson.fromJson(affirmationsResponse.toString(), SubModuleResponse.class);
                    }


                } else {
                    // Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }


    // get user details
    private void getUserDetails(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        Call<JsonElement> call = apiService.getUserDetais(accessToken);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement promotionResponse2 = response.body();
                    Log.d("API Response", "User Details: " + promotionResponse2.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    UserProfileResponse ResponseObj = gson.fromJson(jsonResponse, UserProfileResponse.class);
                    Log.d("API Response body", "Success: User Details" + ResponseObj.getUserdata().getId());
                    //handleServicePaneResponse(ResponseObj);
                    //saveUserId(ResponseObj.getUserdata().getId());
                    SharedPreferenceManager.getInstance(getApplicationContext()).saveUserId(ResponseObj.getUserdata().getId());

                    Log.d("UserID", "USerID: User Details" + SharedPreferenceManager.getInstance(getApplicationContext()).getUserId());


                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }


    private void saveUserId(String userid) {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.USER_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.USER_ID, userid);
        editor.apply();
    }


    // get module content chip list
    private void getContentlist(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create a request body (replace with actual email and phone number)
        // SignupOtpRequest request = new SignupOtpRequest("+91"+mobileNumber);

        // Make the API call
        //Call<JsonElement> call = apiService.getContent(accessToken,"HEALTH_REPORT");
        // Make the GET request
        Call<JsonElement> call = apiService.getContent(
                accessToken,
                "CATEGORY",
                "SLEEP_RIGHT",
                false,
                true
        );
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement promotionResponse5 = response.body();
                    Log.d("API Response", "Content  list: " + promotionResponse5.toString());
                    //Log.d("API Response", "Content  list - : " + affirmationsResponse.toString());
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    // LiveEventResponse ResponseObj = gson.fromJson(jsonResponse,LiveEventResponse.class);
                    //Log.d("API Response body", "Success:AuthorName " + ResponseObj.getData().get(0).getAuthorName());

                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorMessage = response.errorBody().string();
                            System.out.println("Request failed with error: " + errorMessage);
                            Log.d("API Response 2", "Success: " + errorMessage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.d("API Response 2", "Success: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API ERROR", "onFailure: " + t.getMessage());
                t.printStackTrace();  // Print the full stack trace for more details

            }
        });

    }


    private void getContentlistdetails(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

// Create an instance of the ApiService


        // Make the GET request
        Call<ResponseBody> call = apiService.getContentdetailslist(
                accessToken,
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
                System.out.println("Request failed: " + t.getMessage());
            }
        });

    }

    private void getContentlistdetailsfilter(String s) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);


        // Make the GET request with the required query parameters
        Call<ResponseBody> call = apiService.getContentfilters(
                accessToken,
                "SUB_CATEGORY",
                "THINK_RIGHT",
                "THINK_RIGHT_POSITIVE_PSYCHOLOGY",
                true,
                false
        );

        // Handle the API response
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        // Print the successful response
                        if (response.body() != null) {
                            String successMessage = response.body().string();
                            System.out.println("Request successful filter: " + successMessage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Print the error response
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
                System.out.println("Request failed: " + t.getMessage());
            }
        });
    }
}

