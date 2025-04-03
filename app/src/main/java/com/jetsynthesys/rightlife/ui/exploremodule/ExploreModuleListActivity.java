package com.jetsynthesys.rightlife.ui.exploremodule;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.RetrofitData.ApiClient;
import com.jetsynthesys.rightlife.RetrofitData.ApiService;
import com.jetsynthesys.rightlife.apimodel.PromotionResponse;
import com.jetsynthesys.rightlife.apimodel.chipsmodulefilter.ModuleChipCategory;
import com.jetsynthesys.rightlife.apimodel.exploremodules.ExploreSuggestionResponse;
import com.jetsynthesys.rightlife.apimodel.exploremodules.Suggested;
import com.jetsynthesys.rightlife.apimodel.exploremodules.curated.ExploreRecomendedResponse;
import com.jetsynthesys.rightlife.apimodel.exploremodules.curated.Recommended;
import com.jetsynthesys.rightlife.apimodel.exploremodules.topcards.ThinkRightCard;
import com.jetsynthesys.rightlife.apimodel.exploremodules.topcards.ThinkRightCardResponse;
import com.jetsynthesys.rightlife.apimodel.modulecontentlist.ModuleContentDetailsList;
import com.jetsynthesys.rightlife.apimodel.submodule.SubModuleData;
import com.jetsynthesys.rightlife.apimodel.submodule.SubModuleResponse;
import com.jetsynthesys.rightlife.ui.utility.JsonUtil;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants;
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager;

import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreModuleListActivity extends AppCompatActivity {

    TextView txt_morelikethis_section, txt_categories_section, txt_curated_section,txt_coming_soon_banner;
    ImageView ic_back_dialog, close_dialog,img_explore_banner;
    private RecyclerView recyclerView, recycler_view_suggestions,recycler_view_curated,recycler_view_cards;
    private ChipGroup chipGroup;
    String[] itemNames;
    int[] itemImages;
    ModuleChipCategory ResponseObj;
    SubModuleResponse subModuleResponse;
    ThinkRightCardResponse thinkRightCardResponse;
    String moduleId;
    TextView tv_header_htw;
    private RelativeLayout rl_cards_layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exploremodulelist);

        //get views
        txt_coming_soon_banner = findViewById(R.id.txt_coming_soon_banner);
        img_explore_banner = findViewById(R.id.img_explore_banner);
        rl_cards_layout = findViewById(R.id.rl_cards_layout);
        tv_header_htw = findViewById(R.id.tv_header_htw);
        txt_morelikethis_section = findViewById(R.id.txt_morelikethis_section);
        txt_categories_section = findViewById(R.id.txt_categories_section);
        txt_curated_section = findViewById(R.id.txt_curated_section);

        Intent intent = getIntent();
        String categoryType = intent.getStringExtra("responseJson");
        moduleId = intent.getStringExtra("moduleId");

// Now you can use the categoryType variable to perform actions or set up the UI
        if (categoryType != null) {
            // Do something with the category type
            Log.d("CategoryListActivity", "Received category type: " + categoryType);

            // For example, set a TextView's text or load data based on the category type
            Gson gson = new Gson();
            subModuleResponse = gson.fromJson(categoryType, SubModuleResponse.class);
            Log.d("Exploremodule", "Received Module type: " + subModuleResponse.getData().get(0).getModuleId());
            moduleId = subModuleResponse.getData().get(0).getModuleId();
            tv_header_htw.setText(moduleId);
        } else {
            // Handle the case where the extra is not present
            Log.d("CategoryListActivity", "Category type not found in intent");
        }

        recyclerView = findViewById(R.id.recycler_view);
        recycler_view_suggestions = findViewById(R.id.recycler_view_suggestions);
        recycler_view_curated = findViewById(R.id.recycler_view_curated);
        recycler_view_cards = findViewById(R.id.recycler_view_cards);


        ic_back_dialog = findViewById(R.id.ic_back_dialog);
        close_dialog = findViewById(R.id.ic_close_dialog);

        itemNames = new String[]{"Sleep Right with sounds", "Move right", "Sleep music", "Video category", "Audio 1", "Audio 2", "Audio 2", "Audio 2", "Audio 2", "Audio 2"};
        itemImages = new int[]{R.drawable.contents, R.drawable.eat_home, R.drawable.facial_scan,
                R.drawable.first_look, R.drawable.generic_02, R.drawable.meal_plan, R.drawable.generic_02,
                R.drawable.meal_plan, R.drawable.generic_02, R.drawable.meal_plan};


        //API Call
        //getContentlistdetails(categoryType,moduleId);

        //getContentlist(moduleId);   // api to get module


        List<SubModuleData> contentList = Collections.emptyList();
        ExploreModuleRecyclerAdapter adapter = new ExploreModuleRecyclerAdapter(this, itemNames, itemImages, contentList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setAdapter(adapter);

        // showCustomDialog();
        //setupModuleListData(subModuleResponse.getData());
        getCuratedContent(moduleId);
        getMoreLikeContent(moduleId);
        getTopBannerImage(moduleId);


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
                //showExitDialog();
                startActivity(new Intent(ExploreModuleListActivity.this, ExploreSleepSoundsActivity.class));
            }
        });
        ArrayList<String> tags = new ArrayList<>();
        tags.add("technology");
        tags.add("programming");
        tags.add("Java");
        tags.add("web development");
        tags.add("data science");
        tags.add("machine learning");
        tags.add("artificial intelligence");
        for (String tag : tags) {
            //addChip(tag); // Add each tag as a chip
        }


         thinkRightCardResponse = JsonUtil.fetchJsonFromRaw(this);
        if (thinkRightCardResponse != null && thinkRightCardResponse.getThinkRightCard() != null) {
            for (ThinkRightCard card : thinkRightCardResponse.getThinkRightCard()) {
                // Use the card data System.out.println("ID: " + card.getId());
                System.out.println("Title: " + card.getTitle());
                System.out.println("Icon URL: " + card.getIconUrl());
                System.out.println("Background URL: " + card.getBgUrl());
            }
        }

        if (moduleId.equalsIgnoreCase("THINK_RIGHT")) {
            rl_cards_layout.setVisibility(View.GONE);
        }
        else if (moduleId.equalsIgnoreCase("SLEEP_RIGHT")) {
            rl_cards_layout.setVisibility(View.VISIBLE);
        } else if (moduleId.equalsIgnoreCase("MOVE_RIGHT")) {
            rl_cards_layout.setVisibility(View.GONE);
        }else if (moduleId.equalsIgnoreCase("EAT_RIGHT")) {
            rl_cards_layout.setVisibility(View.GONE);
        }
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


    private void getContentlistdetails(String categoryId, String moduleId) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

// Create an instance of the ApiService


        // Make the GET request
        Call<ResponseBody> call = apiService.getContentdetailslist(
                accessToken,
                categoryId,
                10,
                0,
                moduleId
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
                            //setupListData(ResponseObj.getData().getContentList());
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



    private void getCuratedContent(String moduleId) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

// Create an instance of the ApiService

        Call<ResponseBody> call = apiService.getRecommendedLikeContent(accessToken, 5, 0, moduleId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parse the raw JSON into the LikeResponse class
                        String jsonString = response.body().string();
                        Gson gson = new Gson();
                        Log.d("API_RESPONSE", "Might Curated content: " + jsonString);
                    /*LikeResponse likeResponse = gson.fromJson(jsonString, LikeResponse.class);

                    // Use the parsed object
                    Log.d("API_RESPONSE", "Status: " + likeResponse.getStatus());
                    for (LikeResponse.Content content : likeResponse.getData()) {
                        Log.d("API_RESPONSE", "Content Title: " + content.getTitle());
                        Log.d("API_RESPONSE", "Like Count: " + content.getLikeCount());
                    }*/

                        ExploreRecomendedResponse ResponseObj = gson.fromJson(jsonString, ExploreRecomendedResponse.class);
                        Log.d("API Response", "List Details: " + ResponseObj.getData().getRecommendedList().get(0).getContentType()
                                + " " + ResponseObj.getData().getRecommendedList().get(0).getTitle()
                                + " " + ResponseObj.getData().getRecommendedList().get(0).getTitle());
                        setupCuratedList(ResponseObj.getData().getRecommendedList());

                    } catch (Exception e) {
                        Log.e("JSON_PARSE_ERROR", "Error parsing response: " + e.getMessage());
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("API_FAILURE", "Failure: " + t.getMessage());
            }
        });

    }


    private void getMoreLikeContent(String moduleId) {
        //-----------
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPreferenceConstants.ACCESS_TOKEN, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, null);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

// Create an instance of the ApiService

        Call<ResponseBody> call = apiService.getMightLikeContent(accessToken, 5, 0, moduleId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parse the raw JSON into the LikeResponse class
                        String jsonString = response.body().string();
                        Gson gson = new Gson();
                        Log.d("API_RESPONSE", "Might like content: " + jsonString);

                        ExploreSuggestionResponse ResponseObj = gson.fromJson(jsonString, ExploreSuggestionResponse.class);
                        Log.d("API Response", "List Details: " + ResponseObj.getData().getSuggestedList().get(0).getContentType()
                                + " " + ResponseObj.getData().getSuggestedList().get(0).getTitle()
                                + " " + ResponseObj.getData().getSuggestedList().get(0).getTitle());
                        setupSuggestionListData(ResponseObj.getData().getSuggestedList());

                    } catch (Exception e) {
                        Log.e("JSON_PARSE_ERROR", "Error parsing response: " + e.getMessage());
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("API_FAILURE", "Failure: " + t.getMessage());
            }
        });

    }

    private void setStaticCardListData(List<ThinkRightCard> contentList) {
        ExploreTRStaticCardsAdapter adapter = new ExploreTRStaticCardsAdapter(this, itemNames, itemImages, thinkRightCardResponse.getThinkRightCard());
        recycler_view_cards.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recycler_view_cards.setAdapter(adapter);
    }
    private void setupModuleListData(List<SubModuleData> contentList) {
        ExploreModuleRecyclerAdapter adapter = new ExploreModuleRecyclerAdapter(this, itemNames, itemImages, subModuleResponse.getData());
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        recyclerView.setAdapter(adapter);
    }

    private void setupCuratedList(List<Recommended> contentList){
        recycler_view_curated.setVisibility(View.VISIBLE);
        ExploreRecommendedAdapter adapter1 = new ExploreRecommendedAdapter(this, itemNames, itemImages, contentList);
        LinearLayoutManager horizontalLayoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_view_curated.setLayoutManager(horizontalLayoutManager1);
        recycler_view_curated.setAdapter(adapter1);
    }

    private void setupSuggestionListData(List<Suggested> contentList) {
        ExploremightlikeAdapter adapter = new ExploremightlikeAdapter(this, itemNames, itemImages, contentList);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_view_suggestions.setLayoutManager(horizontalLayoutManager);
        recycler_view_suggestions.setAdapter(adapter);

        setupModuleListData(subModuleResponse.getData());
      //  setStaticCardListData(thinkRightCardResponse.getThinkRightCard());

        recyclerView.post(new Runnable() {
            @Override
            public void run() {
               recyclerView.requestLayout();
            }
        });
    }


   // get top banner image
   private void getTopBannerImage(String moduleId) {
       //-----------
       String authToken = SharedPreferenceManager.getInstance(this).getAccessToken();
       ApiService apiService = ApiClient.getClient().create(ApiService.class);

       Call<JsonElement> call = apiService.getPromotionList(authToken, moduleId, null, "TOP");
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
                       Toast.makeText(ExploreModuleListActivity.this, "Success: " + promotionResponse.getStatusCode(), Toast.LENGTH_SHORT).show();
                       Log.d("API Response", "Image Urls: " + promotionResponse.getPromotiondata().getPromotionList().get(0).getContentUrl());

                       handleBannerResponse(promotionResponse);
                   } else {
                       Toast.makeText(ExploreModuleListActivity.this, "Failed: " + promotionResponse.getStatusCode(), Toast.LENGTH_SHORT).show();
                   }

               } else {
                   //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
               }
           }

           @Override
           public void onFailure(Call<JsonElement> call, Throwable t) {
               Toast.makeText(ExploreModuleListActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
               t.printStackTrace();  // Print the full stack trace for more details

           }
       });

   }

    private void handleBannerResponse(PromotionResponse promotionResponse) {
        Log.d("API Response", "Image Urls: " + promotionResponse.getPromotiondata().getPromotionList().get(0).getContentUrl());
        Glide.with(this).load(ApiClient.CDN_URL_QA + promotionResponse.getPromotiondata().getPromotionList().get(0).getContentUrl())
                .placeholder(R.drawable.logo_rightlife)
                .into(img_explore_banner);
    }

}
