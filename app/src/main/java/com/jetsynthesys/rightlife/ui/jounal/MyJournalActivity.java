package com.jetsynthesys.rightlife.ui.jounal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.jetsynthesys.rightlife.BaseActivity;
import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.journal.Journals;
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyJournalActivity extends BaseActivity {

    private Journals journals;
    private TextView tvDate, tvJournalTitle, tvFeeling, tvSituation, tvMood;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildContentView(R.layout.activity_my_journal);

        journals = (Journals) getIntent().getSerializableExtra("Journal");

        tvDate = findViewById(R.id.tv_journal_date);
        tvJournalTitle = findViewById(R.id.tv_journal_title);
        tvFeeling = findViewById(R.id.tv_journal_text);
        tvSituation = findViewById(R.id.tv_journal_text1);
        tvMood = findViewById(R.id.tv_journal_text2);

        findViewById(R.id.ic_back_dialog).setOnClickListener(view -> finish());

        if (journals != null) {

            tvDate.setText(DateTimeUtils.convertAPIDateMonthFormatWithTime(journals.getUpdatedAt()));
            tvJournalTitle.setText(journals.getTitle());
            tvFeeling.setText(journals.getJournal());

            if (journals.getType().equalsIgnoreCase("SELF")) {
                tvSituation.setVisibility(View.GONE);
                tvMood.setVisibility(View.GONE);
            } else {
                tvSituation.setVisibility(View.VISIBLE);
                tvMood.setVisibility(View.VISIBLE);
                tvSituation.setText(journals.getParticularSitutation());
                tvMood.setText(journals.getChangedMood());
            }
        }

        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(view -> deleteJournal());

        TextView btnEdit = findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(MyJournalActivity.this, EditJournalActivity.class);
            intent.putExtra("Journal", journals);
            startActivity(intent);
            finish();
        });
    }

    private void deleteJournal() {
        Call<ResponseBody> call = apiService.deleteMyRLJournal(sharedPreferenceManager.getAccessToken(), journals.getId());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MyJournalActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(MyJournalActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleNoInternetView(t);
            }
        });
    }
}
