package com.jetsynthesys.rightlife.ui;

import android.app.Activity;
import android.app.ComponentCaller;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity;
import com.jetsynthesys.rightlife.apimodel.PromotionResponse;
import com.jetsynthesys.rightlife.apimodel.affirmations.AffirmationResponse;
import com.jetsynthesys.rightlife.apimodel.liveevents.LiveEventResponse;
import com.jetsynthesys.rightlife.apimodel.rledit.Artist;
import com.jetsynthesys.rightlife.apimodel.rledit.RightLifeEditResponse;
import com.jetsynthesys.rightlife.apimodel.rledit.Top;
import com.jetsynthesys.rightlife.apimodel.servicepane.ServicePaneResponse;
import com.jetsynthesys.rightlife.apimodel.submodule.SubModuleData;
import com.jetsynthesys.rightlife.apimodel.submodule.SubModuleResponse;
import com.jetsynthesys.rightlife.apimodel.upcomingevents.UpcomingEventResponse;
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse;
import com.jetsynthesys.rightlife.apimodel.userdata.Userdata;
import com.jetsynthesys.rightlife.apimodel.welnessresponse.ContentWellness;
import com.jetsynthesys.rightlife.apimodel.welnessresponse.WellnessApiResponse;
import com.jetsynthesys.rightlife.databinding.ActivityHomeBinding;
import com.jetsynthesys.rightlife.databinding.BottomsheetTrialEndedBinding;
import com.jetsynthesys.rightlife.newdashboard.HomeDashboardActivity;
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistManager;
import com.jetsynthesys.rightlife.ui.Articles.ArticlesDetailActivity;
import com.jetsynthesys.rightlife.ui.NewSleepSounds.NewSleepSoundActivity;
import com.jetsynthesys.rightlife.ui.affirmation.TodaysAffirmationActivity;
import com.jetsynthesys.rightlife.ui.breathwork.BreathworkActivity;
import com.jetsynthesys.rightlife.ui.contentdetailvideo.ContentDetailsActivity;
import com.jetsynthesys.rightlife.ui.contentdetailvideo.SeriesListActivity;
import com.jetsynthesys.rightlife.ui.healthaudit.HealthAuditActivity;
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamActivity;
import com.jetsynthesys.rightlife.ui.healthpagemain.HealthPageMainActivity;
import com.jetsynthesys.rightlife.ui.jounal.new_journal.JournalListActivity;
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditActivity;
import com.jetsynthesys.rightlife.ui.profile_new.ProfileSettingsActivity;
import com.jetsynthesys.rightlife.ui.search.SearchActivity;
import com.jetsynthesys.rightlife.ui.therledit.ViewCountRequest;
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager;
import com.jetsynthesys.rightlife.ui.voicescan.VoiceScanActivity;
import com.zhpan.bannerview.BannerViewPager;
import com.zhpan.bannerview.constants.PageStyle;
import com.zhpan.indicator.enums.IndicatorStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.jetsynthesys.rightlife.ui.utility.DateConverter.convertToDate;
import static com.jetsynthesys.rightlife.ui.utility.DateConverter.convertToTime;

public class HomeActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    public WellnessApiResponse wellnessApiResponse;
    public RightLifeEditResponse rightLifeEditResponse;
    public SubModuleResponse ThinkRSubModuleResponse;
    public SubModuleResponse MoveRSubModuleResponse;
    public SubModuleResponse EatRSubModuleResponse;
    public SubModuleResponse SleepRSubModuleResponse;
    BannerViewPager mViewPager;
    //
    LinearLayout bottom_sheet;
    LinearLayout ll_journal, ll_affirmations, ll_sleepsounds, ll_health_cam_ql, ll_breathwork, ll_mealplan;
    //RLEdit
    TextView tv_rledt_cont_title1, tv_rledt_cont_title2, tv_rledt_cont_title3,
            nameeditor, nameeditor1, nameeditor2, count, count1, count2;
    ImageView searchIcon, img_rledit, img_rledit1, img_rledit2, img_contenttype_rledit;
    RelativeLayout rl_wellness_lock;
    Button btn_wellness_preference;
    RelativeLayout relative_rledit3, relative_rledit2, relative_rledit1;
    LinearLayout rl_rightlife_edit;
    RelativeLayout relative_wellness1, relative_wellness2, relative_wellness3, relative_wellness4, rl_wellness_main;
    TextView tv_header_rledit, tv_description_rledit, tv_header_lvclass, tv_desc_lvclass,
            tv_header_servcepane1, tv_header_servcepane2, tv_header_servcepane3, tv_header_servcepane4;
    //LinearLayout ll_health_cam, ll_mind_audit, ll_health_audit, ll_voice_scan;
    LinearLayout ll_thinkright_category, ll_moveright_category, ll_eatright_category, ll_sleepright_category,
            ll_homehealthclick, ll_homemenuclick, rlmenu;
    LinearLayout ll_thinkright_category1, ll_thinkright_category2, ll_thinkright_category3, ll_thinkright_category4;
    LinearLayout ll_moveright_category1, ll_moveright_category2, ll_moveright_category3;
    LinearLayout ll_eatright_category1, ll_eatright_category2, ll_eatright_category3, ll_eatright_category4;
    LinearLayout ll_sleepright_category1, ll_sleepright_category2, ll_sleepright_category3;
    ImageView img_homemenu, img_healthmenu, quicklinkmenu;
    TextView txt_homemenu, txt_healthmenu;
    ImageView img1, img2, img3, img4, img5, img6, img7, img8;
    TextView tv1_header, tv1, tv2_header, tv2, tv3_header, tv3, tv4_header, tv4;
    ImageView imgtag_tv4, imgtag_tv3, imgtag_tv2, imgtag_tv1;
    TextView tv1_viewcount, tv2_viewcount, tv3_viewcount, tv4_viewcount;
    private ViewPager2 viewPager;
    private CircularCardAdapter adapter;
    private List<CardItem> cardItems;  // Replace with your data model
    private Handler sliderHandler;     // Handler for scheduling auto-slide
    private Runnable sliderRunnable;   // Runnable for auto-slide logic
    private TestAdapter testAdapter;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    // Live Classes /workshop
    private CardView liveclasscardview;
    private ImageView liveclass_banner_image, img_attending_filled, img_lvclass_host;
    private TextView liveclass_workshop_tag1, liveclass_tv_classattending, tv_classtime, tv_classrating, txt_lvclass_host, tv_title_liveclass, lvclass_date, lvclass_month;
    //Button
    private Button btn_tr_explore, btn_mr_explore, btn_er_explore, btn_sr_explore;
    private ImageView profileImage;
    private TextView tvUserName;

    private final ActivityResultLauncher<Intent> profileActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            getUserDetails();
        }
    });
    private boolean isAdd = true;
    private ActivityHomeBinding homeBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setChildContentView(R.layout.activity_home);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());
        //////--------
        // Handle menu item clicks
        homeBinding.menuHome.setOnClickListener(v -> {
            // loadFragment(new HomeFragment());
            updateMenuSelection(R.id.menu_home);
            startActivity(new Intent(this, HomeDashboardActivity.class));
            updateMenuSelection(R.id.menu_explore);
        });

        homeBinding.menuExplore.setOnClickListener(v -> {
            // loadFragment(new ExploreFragment());
            startActivity(new Intent(this, HomeActivity.class));
            updateMenuSelection(R.id.menu_explore);
        });

// Set initial selection
        updateMenuSelection(R.id.menu_explore);

// Handle FAB click
        homeBinding.fab.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        homeBinding.fab.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));

        homeBinding.fab.setOnClickListener(v -> {

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
            v.setSelected(!v.isSelected());

    /*BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());*/

            homeBinding.fab.animate().rotationBy(180f).setDuration(60).setInterpolator(new DecelerateInterpolator()).withEndAction(() -> {
                // Change icon after rotation
                if (isAdd) {
                    homeBinding.fab.setImageResource(R.drawable.icon_quicklink_plus_black);  // Change to close icon
                    homeBinding.fab.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.rightlife));
                    homeBinding.fab.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                } else {
                    homeBinding.fab.setImageResource(R.drawable.icon_quicklink_plus);    // Change back to add icon
                    homeBinding.fab.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
                    homeBinding.fab.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.rightlife)));
                }
                isAdd = !isAdd;  // Toggle the state
            }).start();
        });


        //////--------
        SetupviewsIdWellness();

        profileImage = findViewById(R.id.profileImage);
        tvUserName = findViewById(R.id.userName);
        TextView tvGreetingText = findViewById(R.id.greetingText);
        tvGreetingText.setText("Good " + DateTimeUtils.getWishingMessage() + " ,");

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
                    callAPIs();
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

        //Swipe to refresh enable only for top position
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> swipeRefreshLayout.setEnabled(scrollY <= 5));

        profileImage.setOnClickListener(view -> {
            /*if (!drawer.isDrawerOpen(Gravity.LEFT)) drawer.openDrawer(Gravity.LEFT);
            else drawer.closeDrawer(Gravity.RIGHT);*/
            startActivity(new Intent(HomeActivity.this, ProfileSettingsActivity.class));
        });

        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // bottom pop menu
        bottom_sheet = findViewById(R.id.bottom_sheet);
        ll_journal = findViewById(R.id.ll_journal);
        ll_journal.setOnClickListener(this);
        ll_affirmations = findViewById(R.id.ll_affirmations);
        ll_affirmations.setOnClickListener(this);
        ll_sleepsounds = findViewById(R.id.ll_sleepsounds);
        ll_sleepsounds.setOnClickListener(this);

        homeBinding.includedhomebottomsheet.llFoodLog.setOnClickListener(this);
        homeBinding.includedhomebottomsheet.llActivityLog.setOnClickListener(this);
        homeBinding.includedhomebottomsheet.llMoodLog.setOnClickListener(this);
        homeBinding.includedhomebottomsheet.llSleepLog.setOnClickListener(this);
        homeBinding.includedhomebottomsheet.llWeightLog.setOnClickListener(this);
        homeBinding.includedhomebottomsheet.llWaterLog.setOnClickListener(this);


        ll_health_cam_ql = findViewById(R.id.ll_health_cam_ql);
        ll_health_cam_ql.setOnClickListener(this);

        ll_breathwork = findViewById(R.id.ll_breathwork);
        ll_breathwork.setOnClickListener(this);

        ll_mealplan = findViewById(R.id.ll_mealplan);
        ll_mealplan.setOnClickListener(this);

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

        rl_rightlife_edit = findViewById(R.id.rl_rightlife_edit);
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

        searchIcon = findViewById(R.id.searchIcon);
        searchIcon.setOnClickListener(this);
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

        // Wellness lock
        rl_wellness_main = findViewById(R.id.rl_wellness_main);
        rl_wellness_lock = findViewById(R.id.rl_wellness_lock);
        btn_wellness_preference = findViewById(R.id.btn_wellness_preference);
        btn_wellness_preference.setOnClickListener(this);

        // MENU
        rlmenu = findViewById(R.id.rlmenu);
        rlmenu.setOnClickListener(this);

        img_homemenu = findViewById(R.id.img_homemenu);
        //img_homemenu.setOnClickListener(this);
        txt_homemenu = findViewById(R.id.txt_homemenu);
        img_healthmenu = findViewById(R.id.img_healthmenu);
        txt_healthmenu = findViewById(R.id.txt_healthmenu);

        //img_healthmenu.setOnClickListener(this);

        quicklinkmenu = findViewById(R.id.quicklinkmenu);
        quicklinkmenu.setOnClickListener(this);
        ll_homemenuclick = findViewById(R.id.ll_homemenuclick);
        ll_homemenuclick.setOnClickListener(this);

        //service pane
        tv_header_rledit = findViewById(R.id.tv_header_rledit);
        tv_description_rledit = findViewById(R.id.tv_description_rledit);
        /*tv_header_servcepane1 = findViewById(R.id.tv_header_servcepane1);
        tv_header_servcepane2 = findViewById(R.id.tv_header_servcepane2);
        tv_header_servcepane3 = findViewById(R.id.tv_header_servcepane3);
        tv_header_servcepane4 = findViewById(R.id.tv_header_servcepane4);*/

        //live Class
        tv_header_lvclass = findViewById(R.id.tv_header_lvclass);
        tv_desc_lvclass = findViewById(R.id.tv_desc_lvclass);
        liveclasscardview = findViewById(R.id.liveclasscardview);
        liveclass_banner_image = findViewById(R.id.banner_image);
        img_attending_filled = findViewById(R.id.img_attending_filled);
        img_lvclass_host = findViewById(R.id.img_lvclass_host);
        lvclass_month = findViewById(R.id.lvclass_month);
        lvclass_date = findViewById(R.id.lvclass_date);

        liveclass_workshop_tag1 = findViewById(R.id.workshop_tag1);
        tv_title_liveclass = findViewById(R.id.tv_title_liveclass);
        liveclass_tv_classattending = findViewById(R.id.tv_classattending);
        tv_classtime = findViewById(R.id.tv_classtime);
        tv_classrating = findViewById(R.id.tv_classrating);
        txt_lvclass_host = findViewById(R.id.txt_lvclass_host);


        /*ll_voice_scan = findViewById(R.id.ll_voice_scan);
        ll_health_cam = findViewById(R.id.ll_health_cam);
        ll_mind_audit = findViewById(R.id.ll_mind_audit);
        ll_health_audit = findViewById(R.id.ll_health_audit);*/

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


        //setupViewPager();

        callAPIs();

        /*ll_voice_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start new activity here
                Intent intent = new Intent(HomeActivity.this, VoiceScanActivity.class);
                // Optionally pass data
                //intent.putExtra("key", "value");
                startActivity(intent);
            }
        });
        ll_health_cam.setOnClickListener(view -> {
            getMyRLHealthCamResult();
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
        });*/


        printAccessToken();


    }


    private void updateMenuSelection(int selectedMenuId) {
        // Reset all icons and labels
        homeBinding.iconHome.setImageResource(R.drawable.new_home_unselected_svg); // Unselected icon
        homeBinding.iconExplore.setImageResource(R.drawable.new_explore_unselected_svg); // Unselected icon
        homeBinding.labelHome.setTextColor(ContextCompat.getColor(this, R.color.gray));
        homeBinding.labelExplore.setTextColor(ContextCompat.getColor(this, R.color.gray));
        homeBinding.labelHome.setTypeface(null, Typeface.NORMAL); // Reset to normal font
        homeBinding.labelExplore.setTypeface(null, Typeface.NORMAL); // Reset to normal font

        // Highlight selected icon and label
        if (selectedMenuId == R.id.menu_home) {
            homeBinding.iconHome.setImageResource(R.drawable.new_home_selected_svg); // Selected icon
            homeBinding.labelHome.setTextColor(ContextCompat.getColor(this, R.color.rightlife));
            homeBinding.labelHome.setTypeface(null, Typeface.BOLD); // Make text bold
        } else if (selectedMenuId == R.id.menu_explore) {
            homeBinding.iconExplore.setImageResource(R.drawable.new_explore_selected_svg); // Selected icon
            homeBinding.labelExplore.setTextColor(ContextCompat.getColor(this, R.color.rightlife));
            homeBinding.labelExplore.setTypeface(null, Typeface.BOLD); // Make text bold
        }
    }


    private void callAPIs() {
        getUserDetails();

        getPromotionList2(); // ModuleService pane
        getRightlifeEdit();

        getAffirmations();

        getWelnessPlaylist();
        //getCuratedContent();
        getModuleContent();


        getSubModuleContent("THINK_RIGHT");
        getSubModuleContent("MOVE_RIGHT");
        getSubModuleContent("EAT_RIGHT");
        getSubModuleContent("SLEEP_RIGHT");
    }

    private void setDrawerHeader(View view) {

        UserProfileResponse userProfileResponse = SharedPreferenceManager.getInstance(this).getUserProfile();
        Userdata userdata = userProfileResponse.getUserdata();


        ImageView ivProfileImage = view.findViewById(R.id.iv_profile_image);
        TextView tvUserName = view.findViewById(R.id.tv_name);
        TextView tvAddress = view.findViewById(R.id.tv_address);
        TextView tvWellnessDays = view.findViewById(R.id.tv_wellness_days);
        TextView tvHealthCheckins = view.findViewById(R.id.tv_health_checkins);

        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(view1 -> drawer.close());
        if (userdata.getProfilePicture() != null) {
            Glide.with(this).load(ApiClient.CDN_URL_QA + userdata.getProfilePicture())
                    .placeholder(R.drawable.rl_profile)
                    .error(R.drawable.rl_profile)
                    .into(ivProfileImage);
        } else {

        }

        tvAddress.setText(userdata.getCountry());

        String username = userdata.getFirstName();
        if (userdata.getLastName() != null) {
            username += " " + userdata.getLastName();
        }
        tvUserName.setText(username);
        tvWellnessDays.setText(userProfileResponse.getWellnessStreak().toString());
        tvHealthCheckins.setText(userProfileResponse.getDaysCount().toString());


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
        imgtag_tv1 = findViewById(R.id.imgtag_tv1);
        tv2_header = findViewById(R.id.tv2_header);
        tv2 = findViewById(R.id.tv2);
        imgtag_tv2 = findViewById(R.id.imgtag_tv2);
        tv3_header = findViewById(R.id.tv3_header);
        tv3 = findViewById(R.id.tv3);
        imgtag_tv3 = findViewById(R.id.imgtag_tv3);
        tv4_header = findViewById(R.id.tv4_header);
        tv4 = findViewById(R.id.tv4);
        imgtag_tv4 = findViewById(R.id.imgtag_tv4);

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

    private void updateViewCount(ViewCountRequest viewCountRequest) {
        Call<ResponseBody> call = apiService.UpdateBannerViewCount(sharedPreferenceManager.getAccessToken(), viewCountRequest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = jsonString = response.body().string();
                        Log.d("API_RESPONSE", "View Count content: " + jsonString);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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

    private void getPromotionList() {
        Call<JsonElement> call = apiService.getPromotionList(sharedPreferenceManager.getAccessToken(), "HOME_PAGE", sharedPreferenceManager.getUserId(), "TOP");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement promotionResponse2 = response.body();
                    Log.d("API Response", "Success: " + promotionResponse2);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    PromotionResponse promotionResponse = gson.fromJson(jsonResponse, PromotionResponse.class);
                    Log.d("API Response body", "Success: promotion " + jsonResponse);
                    if (promotionResponse.getSuccess()) {


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
                handleNoInternetView(t);
            }
        });

    }

    private void handlePromotionResponse(PromotionResponse promotionResponse) {

        cardItems.clear();
        for (int i = 0; i < promotionResponse.getPromotiondata().getPromotionList().size(); i++) {

            CardItem cardItem = new CardItem(promotionResponse.getPromotiondata().getPromotionList().get(i).getId(),
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getName(),
                    R.drawable.facialconcept,
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getThumbnail().getUrl(),
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getContent(),
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getButtonName(),
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getCategory(),
                    String.valueOf(promotionResponse.getPromotiondata().getPromotionList().get(i).getViews()),
                    promotionResponse.getPromotiondata().getPromotionList().get(i).getSeriesId());
            cardItems.add(i, cardItem);
        }

        if (!cardItems.isEmpty()) {
            viewPager.setVisibility(View.VISIBLE);
            adapter = new CircularCardAdapter(HomeActivity.this, cardItems);
            viewPager.setAdapter(adapter);
        } else {
            viewPager.setVisibility(View.GONE);
        }
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
        items.add(new CardItem("0", "Card 1", R.drawable.facialconcept, "", "", "scan now", "", "", ""));
        items.add(new CardItem("1", "Card 2", R.drawable.facialconcept, "", "", "scan now", "", "", ""));
        items.add(new CardItem("2", "Card 3", R.drawable.facialconcept, "", "", "scan now", "", "", ""));
        items.add(new CardItem("3", "Card 4", R.drawable.facialconcept, "", "", "scan now", "", "", ""));
        items.add(new CardItem("4", "Card 5", R.drawable.facialconcept, "", "", "scan now", "", "", ""));
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
        getPromotionList();
        getRightlifeEdit();
        getWelnessPlaylist();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up to prevent memory leaks
        sliderHandler.removeCallbacks(sliderRunnable);
    }


    //second API
    private void getPromotionList2() {
        Call<JsonElement> call = apiService.getPromotionList2(sharedPreferenceManager.getAccessToken());
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement promotionResponse2 = response.body();
                    Log.d("API Response", "SErvice Pane: " + promotionResponse2);
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
                handleNoInternetView(t);
            }
        });

    }

    private void handleServicePaneResponse(ServicePaneResponse responseObj) {
        RecyclerView recyclerView = findViewById(R.id.rv_service_pane);
        ServicePaneAdapter adapter = new ServicePaneAdapter(this, responseObj.getData().getHomeServices(), homeService -> {
            switch (homeService.getTitle()) {
                case "Voice Scan":
                    /*Intent intentVoice = new Intent(HomeActivity.this, VoiceScanActivity.class);
                    startActivity(intentVoice);*/
                    Intent intentVoice = new Intent(HomeActivity.this, MindAuditActivity.class);
                    startActivity(intentVoice);
                    break;
                case "Mind Audit":
                    Intent intentMind = new Intent(HomeActivity.this, MindAuditActivity.class);
                    startActivity(intentMind);
                    break;
                case "Health Cam":
                    startActivity(new Intent(HomeActivity.this, HealthCamActivity.class));
                    break;
                default:
                    Intent intentHealthAudit = new Intent(HomeActivity.this, HealthCamActivity.class);
                    startActivity(intentHealthAudit);
                    break;
            }
        });
        int spanCount = responseObj.getData().getHomeServices().size();
        spanCount = (spanCount > 3) ? 2 : spanCount;

        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerView.setAdapter(adapter);
    }


    // get Affirmation list

    private void getAffirmations() {
        Call<JsonElement> call = apiService.getAffirmationList(sharedPreferenceManager.getAccessToken(), SharedPreferenceManager.getInstance(getApplicationContext()).getUserId(), true);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Affirmation list: " + affirmationsResponse);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    AffirmationResponse ResponseObj = gson.fromJson(jsonResponse, AffirmationResponse.class);
                    setupAfirmationContent(ResponseObj);

                } else {
                    //Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                handleNoInternetView(t);
            }
        });

    }

    private void setupAfirmationContent(AffirmationResponse responseObj) {
        //setupViewPager();
        mViewPager = findViewById(R.id.banner_viewpager);

        int selectedColor = ContextCompat.getColor(HomeActivity.this, R.color.menuselected);
        int unselectedColor = ContextCompat.getColor(HomeActivity.this, R.color.gray);
        // Set up the ViewPager
        if (!responseObj.getData().getSortedServices().isEmpty()) {
            mViewPager.setVisibility(View.VISIBLE);
            testAdapter = new TestAdapter(responseObj.getData().getSortedServices());
            mViewPager.setAdapter(testAdapter);
            // mViewPager.setLifecycleRegistry(getLifecycle());
            mViewPager.setScrollDuration(1000);
            mViewPager.setPageStyle(PageStyle.MULTI_PAGE);
            mViewPager.setIndicatorSliderGap(20) // Adjust spacing if needed
                    .setIndicatorStyle(IndicatorStyle.ROUND_RECT)
                    .setIndicatorHeight(20) // Adjust height for a pill-like shape
                    .setIndicatorSliderWidth(20, 50) // Unselected: 10px, Selected: 20px
                    .setIndicatorSliderColor(unselectedColor, selectedColor) // Adjust colors accordingly
                    .create(responseObj.getData().getSortedServices());
        } else {
            mViewPager.setVisibility(View.GONE);
        }

    }


//Get RightLife Edit

    private void getRightlifeEdit() {
        Call<JsonElement> call = apiService.getRightlifeEdit(sharedPreferenceManager.getAccessToken(), "HOME");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "RightLife Edit list: " + affirmationsResponse);
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
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                handleNoInternetView(t);
            }
        });

    }

    private void setupRLEditContent(RightLifeEditResponse response) {
        if (response == null || response.getData() == null) return;

        List<Top> topList = response.getData().getTopList();
        if (topList == null || topList.isEmpty())
        {rl_rightlife_edit.setVisibility(View.GONE);
            return;
        }else {
            rl_rightlife_edit.setVisibility(View.VISIBLE);
        }

        if (topList.size() > 0) {
            Top item0 = topList.get(0);
            tv_rledt_cont_title1.setText(item0.getDesc());

            if (item0.getArtist() != null && !item0.getArtist().isEmpty()) {
                Artist artist = item0.getArtist().get(0);
                nameeditor.setText((artist.getFirstName() != null ? artist.getFirstName() : "") +
                        " " +
                        (artist.getLastName() != null ? artist.getLastName() : ""));
            }

            count.setText(String.valueOf(item0.getViewCount()));

            if ("VIDEO".equalsIgnoreCase(item0.getContentType())) {
                img_contenttype_rledit.setImageResource(R.drawable.ic_playrledit);
            } else {
                img_contenttype_rledit.setImageResource(R.drawable.read);
            }

            if (item0.getThumbnail() != null) {
                Glide.with(getApplicationContext())
                        .load(ApiClient.CDN_URL_QA + item0.getThumbnail().getUrl())
                        .placeholder(R.drawable.rl_placeholder)
                        .error(R.drawable.rl_placeholder)
                        .into(img_rledit);
            }
        }else {
            rl_rightlife_edit.setVisibility(View.GONE);
        }

        if (topList.size() > 1) {
            Top item1 = topList.get(1);
            tv_rledt_cont_title2.setText(item1.getTitle());

            if (item1.getArtist() != null && !item1.getArtist().isEmpty()) {
                Artist artist = item1.getArtist().get(0);
                nameeditor1.setText((artist.getFirstName() != null ? artist.getFirstName() : "") +
                        " " +
                        (artist.getLastName() != null ? artist.getLastName() : ""));
            }

            count1.setText(String.valueOf(item1.getViewCount()));

            if (item1.getThumbnail() != null) {
                Glide.with(getApplicationContext())
                        .load(ApiClient.CDN_URL_QA + item1.getThumbnail().getUrl())
                        .placeholder(R.drawable.rl_placeholder)
                        .error(R.drawable.rl_placeholder)
                        .transform(new RoundedCorners(25))
                        .into(img_rledit1);
            }
        }else {
            relative_rledit2.setVisibility(View.GONE);
        }

        if (topList.size() > 2) {
            Top item2 = topList.get(2);
            tv_rledt_cont_title3.setText(item2.getTitle());

            if (item2.getArtist() != null && !item2.getArtist().isEmpty()) {
                Artist artist = item2.getArtist().get(0);
                nameeditor2.setText((artist.getFirstName() != null ? artist.getFirstName() : "") +
                        " " +
                        (artist.getLastName() != null ? artist.getLastName() : ""));
            }

            count2.setText(String.valueOf(item2.getViewCount()));

            if (item2.getThumbnail() != null) {
                Glide.with(getApplicationContext())
                        .load(ApiClient.CDN_URL_QA + item2.getThumbnail().getUrl())
                        .placeholder(R.drawable.rl_placeholder)
                        .error(R.drawable.rl_placeholder)
                        .transform(new RoundedCorners(25))
                        .into(img_rledit2);
            }
        }else {
            relative_rledit3.setVisibility(View.GONE);
        }
    }


    /*private void setupRLEditContent(RightLifeEditResponse rightLifeEditResponse) {

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

    }*/

    //Upcoming Event List -
    private void getUpcomingLiveEvents(String s) {
        Call<JsonElement> call = apiService.getUpcomingLiveEvent(sharedPreferenceManager.getAccessToken(), "LIVE_EVENT", "UPCOMING", "HOME");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Upcoming Event list: " + affirmationsResponse);
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
                handleNoInternetView(t);
            }
        });

    }


    //WElness PlayList
    private void getWelnessPlaylist() {
        Call<JsonElement> call = apiService.getWelnessPlaylist(sharedPreferenceManager.getAccessToken(), "SERIES", "WELLNESS");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Wellness Play list: " + affirmationsResponse);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    wellnessApiResponse = gson.fromJson(jsonResponse, WellnessApiResponse.class);
                    Log.d("API Response body", "Wellness:RLEdit " + wellnessApiResponse.getData().getContentList().get(0).getTitle());
                    /*if (wellnessApiResponse.getData().isPreference()) {
                        setupWellnessContent(wellnessApiResponse.getData().getContentList());
                    } else {
                        rl_wellness_lock.setVisibility(View.VISIBLE);
                    }*/
                    setupWellnessContent(wellnessApiResponse.getData().getContentList());

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


    private void setupWellnessContent(List<ContentWellness> contentList) {
        if (contentList.isEmpty()) return;

        rl_wellness_main.setVisibility(View.VISIBLE);
        // Bind data for item 1
        if (!contentList.isEmpty()) {
            bindContentToView(contentList.get(0), tv1_header, tv1, img1, tv1_viewcount, img5, imgtag_tv1);
        }else {
            relative_wellness1.setVisibility(View.GONE);
        }

        // Bind data for item 2
        if (contentList.size() > 1) {
            bindContentToView(contentList.get(1), tv2_header, tv2, img2, tv2_viewcount, img6, imgtag_tv2);
        }else {
            relative_wellness2.setVisibility(View.GONE);
        }

        // Bind data for item 3
        if (contentList.size() > 2) {
            bindContentToView(contentList.get(2), tv3_header, tv3, img3, tv3_viewcount, img7, imgtag_tv3);
        }else {
            relative_wellness3.setVisibility(View.GONE);
        }

        // Bind data for item 4
        if (contentList.size() > 3) {
            bindContentToView(contentList.get(3), tv4_header, tv4, img4, tv4_viewcount, img8, imgtag_tv4);
        }else {
            relative_wellness4.setVisibility(View.GONE);
        }
    }

    //Bind Wellnes content
    private void bindContentToView(ContentWellness content, TextView header, TextView category, ImageView thumbnail, TextView viewcount, ImageView imgcontenttype, ImageView imgtag) {
        // Set title in the header TextView
        header.setText(content.getTitle());
        viewcount.setText("" + content.getViewCount());
        // Set categoryName in the category TextView
        category.setText(content.getCategoryName());

        // Load thumbnail using Glide
        if (!isFinishing() && !isDestroyed()) {
            Glide.with(this)
                    .load(ApiClient.CDN_URL_QA + content.getThumbnail().getUrl()) // URL of the thumbnail
                    .placeholder(R.drawable.rl_placeholder)
                    .error(R.drawable.rl_placeholder)
                    .transform(new RoundedCorners(25))// Optional error image
                    .into(thumbnail);
        }
        setModuleColor(imgtag, content.getModuleId());
    }

    private void setModuleColor(ImageView imgtag, String moduleId) {
        if (moduleId.equalsIgnoreCase("EAT_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.eatright);
            imgtag.setImageTintList(colorStateList);

        } else if (moduleId.equalsIgnoreCase("THINK_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.thinkright);
            imgtag.setImageTintList(colorStateList);

        } else if (moduleId.equalsIgnoreCase("SLEEP_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.sleepright);
            imgtag.setImageTintList(colorStateList);

        } else if (moduleId.equalsIgnoreCase("MOVE_RIGHT")) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.moveright);
            imgtag.setImageTintList(colorStateList);

        }
    }


    private void getLiveEvents(String s) {
        Call<JsonElement> call = apiService.getLiveEvent(sharedPreferenceManager.getAccessToken(), "HOME");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Live Events list: " + affirmationsResponse);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    LiveEventResponse ResponseObj = gson.fromJson(jsonResponse, LiveEventResponse.class);
                    setupLiveEvent(ResponseObj);
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                handleNoInternetView(t);
            }
        });

    }

    private void setupLiveEvent(LiveEventResponse responseObj) {
        liveclass_workshop_tag1.setText(responseObj.getData().getEvents().get(0).getEventType());
        tv_title_liveclass.setText(responseObj.getData().getEvents().get(0).getTitle());
        txt_lvclass_host.setText(String.format("%s %s", responseObj.getData().getEvents().get(0).getInstructor().getFirstName(),
                responseObj.getData().getEvents().get(0).getInstructor().getLastName()));
        liveclass_tv_classattending.setText(String.valueOf(responseObj.getData().getEvents().get(0).getParticipantsCount()));
        String formattedDate = convertToDate(responseObj.getData().getEvents().get(0).getStartDateTime());
        //tv_classrating.setText(String.valueOf(responseObj.getData().getEvents().get(1).getRating()));
        if (formattedDate != null) {
            String[] dateParts = formattedDate.split(" ");

            // Set the day and month in separate TextViews
            lvclass_date.setText(dateParts[0]);   // "20"
            lvclass_month.setText(dateParts[1]); // "Nov"
        }
        String formattedtime = convertToTime(responseObj.getData().getEvents().get(0).getStartDateTime());
        tv_classtime.setText(formattedtime);

        Glide.with(getApplicationContext())
                .load(ApiClient.CDN_URL_QA + responseObj.getData().getEvents().get(0).getThumbnail().getUrl())
                .placeholder(R.drawable.rl_placeholder)
                .error(R.drawable.rl_placeholder)// Replace with your placeholder image
                .into(liveclass_banner_image);

        tv_header_lvclass.setText(responseObj.getData().getSectionTitle());
        tv_desc_lvclass.setText(responseObj.getData().getSectionSubtitle());
        liveclasscardview.setVisibility(View.VISIBLE);
        tv_header_lvclass.setVisibility(View.VISIBLE);
        tv_desc_lvclass.setVisibility(View.VISIBLE);

    }
//getCuratedContent


    private void getCuratedContent() {
        Call<JsonElement> call = apiService.getCuratedContent(sharedPreferenceManager.getAccessToken());
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Curated  Content list: " + affirmationsResponse);
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
                handleNoInternetView(t);
            }
        });

    }

    // get Module list
    private void getModuleContent() {
        Call<JsonElement> call = apiService.getmodule(sharedPreferenceManager.getAccessToken());
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "Module list - : " + affirmationsResponse);
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
                handleNoInternetView(t);
            }
        });

    }

    @Override
    public void onClick(View view) {

        int viewId = view.getId();

        if (viewId == R.id.searchIcon) {
            startActivity(new Intent(this, SearchActivity.class));
        } else if (viewId == R.id.rlmenu) {
            //Toast.makeText(HomeActivity.this, "Button 1 clicked", Toast.LENGTH_SHORT).show();
            // Start new activity here
            //Intent intent = new Intent(HomeActivity.this, RLPageActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            //startActivity(intent);

            if (bottom_sheet.getVisibility() == View.VISIBLE) {
                bottom_sheet.setVisibility(View.GONE);
                img_healthmenu.setBackgroundResource(R.drawable.homeselected);
                txt_healthmenu.setTextColor(getResources().getColor(R.color.menuselected));
                Typeface typeface = ResourcesCompat.getFont(this, R.font.dmsans_bold);
                txt_healthmenu.setTypeface(typeface);
                quicklinkmenu.setSelected(!quicklinkmenu.isSelected());
            }

        } else if (viewId == R.id.ll_homehealthclick) {
            //Toast.makeText(HomeActivity.this, "TextView clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, HealthPageMainActivity.class);
            // Optionally pass data
            //intent.putExtra("key", "value");
            startActivity(intent);
            finish();
        } else if (viewId == R.id.ll_homemenuclick) {
            //Toast.makeText(HomeActivity.this, "New Home Coming Soon...", Toast.LENGTH_LONG).show();
            startActivity(new Intent(HomeActivity.this, HomeDashboardActivity.class));
            /*if (bottom_sheet.getVisibility() == View.VISIBLE) {
                bottom_sheet.setVisibility(View.GONE);
                img_homemenu.setBackgroundResource(R.drawable.homeselected);
                txt_homemenu.setTextColor(getResources().getColor(R.color.menuselected));
                Typeface typeface = ResourcesCompat.getFont(this, R.font.dmsans_bold);
                txt_homemenu.setTypeface(typeface);
                quicklinkmenu.setSelected(!quicklinkmenu.isSelected());
            }*/
            /* else {
                bottom_sheet.setVisibility(View.VISIBLE);
                img_homemenu.setBackgroundColor(Color.TRANSPARENT);
                txt_homemenu.setTextColor(getResources().getColor(R.color.txt_color_header));
                Typeface typeface = ResourcesCompat.getFont(this, R.font.dmsans_regular);
                txt_homemenu.setTypeface(typeface);

            }*/
        }

        List<SubModuleData> thinkRightSubmodule = ThinkRSubModuleResponse.getData();
        List<SubModuleData> moveRightSubmodule = MoveRSubModuleResponse.getData();
        List<SubModuleData> eatRightSubmodule = EatRSubModuleResponse.getData();
        List<SubModuleData> sleepRightSubmodule = SleepRSubModuleResponse.getData();

        if (viewId == R.id.ll_thinkright_category1 && !thinkRightSubmodule.isEmpty()) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", thinkRightSubmodule.get(0).getCategoryId());
            intent.putExtra("moduleId", thinkRightSubmodule.get(0).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_thinkright_category2 && thinkRightSubmodule.size() > 1) {
            thinkRightSubmodule.get(1).getName();
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", thinkRightSubmodule.get(1).getCategoryId());
            intent.putExtra("moduleId", thinkRightSubmodule.get(1).getModuleId());
            startActivity(intent);

        } else if (viewId == R.id.ll_thinkright_category3 && thinkRightSubmodule.size() > 2) {
            thinkRightSubmodule.get(2).getName();
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", thinkRightSubmodule.get(2).getCategoryId());
            intent.putExtra("moduleId", thinkRightSubmodule.get(2).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_thinkright_category4 && thinkRightSubmodule.size() > 3) {
            thinkRightSubmodule.get(3).getName();
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", thinkRightSubmodule.get(3).getCategoryId());
            intent.putExtra("moduleId", thinkRightSubmodule.get(3).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_moveright_category1 && !moveRightSubmodule.isEmpty()) {

            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", moveRightSubmodule.get(0).getCategoryId());
            intent.putExtra("moduleId", moveRightSubmodule.get(0).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_moveright_categor2 && moveRightSubmodule.size() > 2) {

            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", moveRightSubmodule.get(2).getCategoryId());
            intent.putExtra("moduleId", moveRightSubmodule.get(2).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_moveright_category3 && moveRightSubmodule.size() > 2) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", moveRightSubmodule.get(1).getCategoryId());
            intent.putExtra("moduleId", moveRightSubmodule.get(1).getModuleId());
            startActivity(intent);

        } else if (viewId == R.id.ll_eatright_category1 && !eatRightSubmodule.isEmpty()) {

            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", eatRightSubmodule.get(0).getCategoryId());
            intent.putExtra("moduleId", eatRightSubmodule.get(0).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_eatright_category2 && eatRightSubmodule.size() > 1) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", eatRightSubmodule.get(1).getCategoryId());
            intent.putExtra("moduleId", eatRightSubmodule.get(1).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_eatright_category3 && eatRightSubmodule.size() > 2) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", eatRightSubmodule.get(2).getCategoryId());
            intent.putExtra("moduleId", eatRightSubmodule.get(2).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_eatright_category4 && eatRightSubmodule.size() > 3) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", eatRightSubmodule.get(3).getCategoryId());
            intent.putExtra("moduleId", eatRightSubmodule.get(3).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_sleepright_category1 && !sleepRightSubmodule.isEmpty()) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", sleepRightSubmodule.get(0).getCategoryId());
            intent.putExtra("moduleId", sleepRightSubmodule.get(0).getModuleId());
            startActivity(intent);

        } else if (viewId == R.id.ll_sleepright_category2 && sleepRightSubmodule.size() > 1) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", sleepRightSubmodule.get(1).getCategoryId());
            intent.putExtra("moduleId", sleepRightSubmodule.get(1).getModuleId());
            startActivity(intent);
        } else if (viewId == R.id.ll_sleepright_category3 && sleepRightSubmodule.size() > 2) {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            intent.putExtra("Categorytype", sleepRightSubmodule.get(2).getCategoryId());
            intent.putExtra("moduleId", sleepRightSubmodule.get(2).getModuleId());
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
            if (checkTrailEndedAndShowDialog()) {
                startActivity(new Intent(HomeActivity.this, JournalListActivity.class));
            }
        } else if (viewId == R.id.ll_affirmations) {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(new Intent(HomeActivity.this, TodaysAffirmationActivity.class));
            }
        } else if (viewId == R.id.ll_sleepsounds) {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(new Intent(HomeActivity.this, NewSleepSoundActivity.class));
            }
        } else if (viewId == R.id.ll_breathwork) {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(new Intent(HomeActivity.this, BreathworkActivity.class));
            }
        } else if (viewId == R.id.ll_health_cam_ql) {
            startActivity(new Intent(HomeActivity.this, HealthCamActivity.class));
        } else if (viewId == R.id.ll_mealplan) {
            Intent intent = new Intent(HomeActivity.this, MainAIActivity.class);
            intent.putExtra("ModuleName", "EatRight");
            intent.putExtra("BottomSeatName", "SnapMealTypeEat");
            if (!sharedPreferenceManager.getSnapMealId().isEmpty()){
                intent.putExtra("snapMealId", sharedPreferenceManager.getSnapMealId()); // make sure snapMealId is declared and initialized
            }
            startActivity(intent);

        } else if (viewId == R.id.btn_wellness_preference) {
        } else if (view.getId() == R.id.ll_food_log) {
            if (checkTrailEndedAndShowDialog()) {
                Intent intent = new Intent(HomeActivity.this, MainAIActivity.class);
                intent.putExtra("ModuleName", "EatRight");
                intent.putExtra("BottomSeatName", "MealLogTypeEat");
                startActivity(intent);
            }

        } else if (view.getId() == R.id.ll_activity_log) {
            if (checkTrailEndedAndShowDialog()) {
                Intent intent = new Intent(HomeActivity.this, MainAIActivity.class);
                intent.putExtra("ModuleName", "MoveRight");
                intent.putExtra("BottomSeatName", "SearchActivityLogMove");
                startActivity(intent);
            }

        } else if (view.getId() == R.id.ll_mood_log) {
            if (checkTrailEndedAndShowDialog()) {
                Intent intent = new Intent(HomeActivity.this, MainAIActivity.class);
                intent.putExtra("ModuleName", "ThinkRight");
                intent.putExtra("BottomSeatName", "RecordEmotionMoodTracThink");
                startActivity(intent);
            }

        } else if (view.getId() == R.id.ll_sleep_log) {
            if (checkTrailEndedAndShowDialog()) {
                Intent intent = new Intent(HomeActivity.this, MainAIActivity.class);
                intent.putExtra("ModuleName", "SleepRight");
                intent.putExtra("BottomSeatName", "LogLastNightSleep");
                startActivity(intent);
            }

        } else if (view.getId() == R.id.ll_weight_log) {
            if (checkTrailEndedAndShowDialog()) {
                Intent intent = new Intent(HomeActivity.this, MainAIActivity.class);
                intent.putExtra("ModuleName", "EatRight");
                intent.putExtra("BottomSeatName", "LogWeightEat");
                startActivity(intent);
            }

        } else if (view.getId() == R.id.ll_water_log) {
            if (checkTrailEndedAndShowDialog()) {
                Intent intent = new Intent(HomeActivity.this, MainAIActivity.class);
                intent.putExtra("ModuleName", "EatRight");
                intent.putExtra("BottomSeatName", "LogWaterIntakeEat");
                startActivity(intent);
            }
        }


    }

    private void CallRlEditDetailActivity(int position) {

        if (rightLifeEditResponse.getData().getTopList().get(position).getContentType().equalsIgnoreCase("TEXT")) {
            Intent intent = new Intent(HomeActivity.this, ArticlesDetailActivity.class);
            intent.putExtra("contentId", rightLifeEditResponse.getData().getTopList().get(position).getId());
            startActivity(intent);
        } else {
            Gson gson = new Gson();
            String json = gson.toJson(rightLifeEditResponse);
            Intent intent = new Intent(HomeActivity.this, ContentDetailsActivity.class);
            intent.putExtra("Categorytype", rightLifeEditResponse.getData().getTopList().get(position).getId());
            intent.putExtra("position", position);
            intent.putExtra("contentId", rightLifeEditResponse.getData().getTopList().get(position).getId());
            startActivity(intent);
        }

    }

    private void CallWellnessDetailActivity(int position) {
      /*  Gson gson = new Gson();
        String json = gson.toJson(wellnessApiResponse);
        Intent intent = new Intent(HomeActivity.this, WellnessDetailViewActivity.class);
        intent.putExtra("responseJson", json);
        intent.putExtra("position", position);
        startActivity(intent);*/

        if (wellnessApiResponse != null) {
            Gson gson = new Gson();
            String json = gson.toJson(wellnessApiResponse);
            Intent intent = new Intent(HomeActivity.this, SeriesListActivity.class);
            intent.putExtra("responseJson", json);
            intent.putExtra("position", position);
            intent.putExtra("contentId", wellnessApiResponse.getData().getContentList().get(position).get_id());
            startActivity(intent);
        } else {
            // Handle null case
            Toast.makeText(HomeActivity.this, "Response is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void CallExploreModuleActivity(SubModuleResponse responseJson) {
        Gson gson = new Gson();
        String json = gson.toJson(responseJson);
        Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
        intent.putExtra("moduleId", responseJson.getData().get(0).getModuleId());
        //intent.putExtra("responseJson", json);
        //intent.putExtra("position", position);
        startActivity(intent);
    }

    // ----- Test API

    private void getSubModuleContent(String moduleid) {
        Call<JsonElement> call = apiService.getsubmodule(sharedPreferenceManager.getAccessToken(), moduleid, "CATEGORY");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement affirmationsResponse = response.body();
                    Log.d("API Response", "SUB subModule list - : " + affirmationsResponse);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    // Log.d("API Response", "SUB subModule list - : " + jsonResponse);


                    if (moduleid.equalsIgnoreCase("THINK_RIGHT")) {
                        ThinkRSubModuleResponse = gson.fromJson(affirmationsResponse.toString(), SubModuleResponse.class);
                    } else if (moduleid.equalsIgnoreCase("MOVE_RIGHT")) {
                        MoveRSubModuleResponse = gson.fromJson(affirmationsResponse.toString(), SubModuleResponse.class);
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
                handleNoInternetView(t);
            }
        });

    }


    // get user details
    private void getUserDetails() {
        Call<JsonElement> call = apiService.getUserDetais(sharedPreferenceManager.getAccessToken());
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement promotionResponse2 = response.body();
                    Log.d("API Response", "User Details: " + promotionResponse2);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    UserProfileResponse ResponseObj = gson.fromJson(jsonResponse, UserProfileResponse.class);
                    Log.d("API Response body", "Success: User Details" + ResponseObj.getUserdata().getId());
                    SharedPreferenceManager.getInstance(getApplicationContext()).saveUserId(ResponseObj.getUserdata().getId());
                    SharedPreferenceManager.getInstance(getApplicationContext()).saveUserProfile(ResponseObj);

                    setDrawerHeader(navigationView.getHeaderView(0));
                    if (ResponseObj.getUserdata().getProfilePicture() != null) {
                        Glide.with(HomeActivity.this).load(ApiClient.CDN_URL_QA + ResponseObj.getUserdata().getProfilePicture()).placeholder(R.drawable.rl_profile).error(R.drawable.rl_profile).into(profileImage);
                    }
                    tvUserName.setText(ResponseObj.getUserdata().getFirstName());


                    Log.d("UserID", "USerID: User Details" + SharedPreferenceManager.getInstance(getApplicationContext()).getUserId());
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                handleNoInternetView(t);
            }
        });

    }

    @Override
    public void onNewIntent(@NonNull Intent intent, @NonNull ComponentCaller caller) {
        super.onNewIntent(intent, caller);
        if (intent.getBooleanExtra("start_journal", false)) {
            startActivity(new Intent(this, JournalListActivity.class));
        } else if (intent.getBooleanExtra("start_profile", false)) {
            startActivity(new Intent(this, ProfileSettingsActivity.class));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }


    public boolean checkTrailEndedAndShowDialog() {
        if (!DashboardChecklistManager.INSTANCE.getPaymentStatus()) {
            showTrailEndedBottomSheet();
            return false; // Return false if condition is true and dialog is shown
        } else {
            if (!DashboardChecklistManager.INSTANCE.getChecklistStatus()) {
                DialogUtils.INSTANCE.showCheckListQuestionCommonDialog(this, "Finish Checklist to Unlock");
                return false;
            } else {
                return true;// Return true if condition is false
            }
        }
    }


    private void showTrailEndedBottomSheet() {
        // Create and configure BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // Inflate the BottomSheet layout
        BottomsheetTrialEndedBinding dialogBinding = BottomsheetTrialEndedBinding.inflate(getLayoutInflater());
        View bottomSheetView = dialogBinding.getRoot();

        bottomSheetDialog.setContentView(bottomSheetView);

        // Set up the animation
        LinearLayout bottomSheetLayout = bottomSheetView.findViewById(R.id.design_bottom_sheet);
        if (bottomSheetLayout != null) {
            Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up);
            bottomSheetLayout.setAnimation(slideUpAnimation);
        }

    /*dialogBinding.tvTitle.setText("Leaving early?");
    dialogBinding.tvDescription.setText(
        "A few more minutes of breathing practise will make a world of difference.");*/

        //dialogBinding.btnCancel.setText("Continue Practise");
        //dialogBinding.btnYes.setText("Leave");

        dialogBinding.ivDialogClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        dialogBinding.btnExplorePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //finish();
            }
        });

        bottomSheetDialog.show();
    }

}

