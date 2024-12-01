package com.example.rlapp.ui.utility;

import android.content.Context;
import android.content.res.Resources;

import com.example.rlapp.R;
import com.example.rlapp.apimodel.exploremodules.topcards.ThinkRightCardResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonUtil {

    public static ThinkRightCardResponse fetchJsonFromRaw(Context context) {
        String jsonString = null;
        try {
            // Get the raw resource
            InputStream inputStream = context.getResources().openRawResource(R.raw.think_right_newui_cards);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            jsonString = stringBuilder.toString();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert JSON string to POJO
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, ThinkRightCardResponse.class);
    }
}