package com.jetsynthesys.rightlife.ui.healthaudit;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.ui.healthaudit.questionlist.Question;

import java.util.ArrayList;

public class HAFromBPFragment extends Fragment {

    TextView dateText;

    private static final String ARG_PAGE_INDEX = "page_index";
    private int pageIndex;
    private EditText edtSystolicBP, edtDiastolicBP;
    private Button btnOK;
    private OnNextFragmentClickListener onNextFragmentClickListener;
    private Question question;

    public static HAFromBPFragment newInstance(int pageIndex, Question question) {
        HAFromBPFragment fragment = new HAFromBPFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_INDEX, pageIndex);
        args.putSerializable(HealthAuditFormActivity.ARG_QUESTION, question);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pageIndex = getArguments().getInt(ARG_PAGE_INDEX, -1);
            question = (Question) getArguments().getSerializable(HealthAuditFormActivity.ARG_QUESTION);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onNextFragmentClickListener = (OnNextFragmentClickListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_health_bp, container, false);
        dateText = view.findViewById(R.id.dateText);

        getViews(view);
        return view;
    }

    private void getViews(View view) {
        edtDiastolicBP = view.findViewById(R.id.edt_diastolic_bp);
        edtSystolicBP = view.findViewById(R.id.edt_systolic_bp);
        btnOK = view.findViewById(R.id.btn_ok);

        btnOK.setOnClickListener(view1 -> {
            if (edtDiastolicBP.getText().toString().isEmpty() || edtSystolicBP.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), requireActivity().getString(R.string.general_error), Toast.LENGTH_SHORT).show();
            } else {
                int diastolicBP = Integer.parseInt(edtDiastolicBP.getText().toString());
                int systolicBP = Integer.parseInt(edtSystolicBP.getText().toString());
                if (systolicBP < 40 || systolicBP > 250) {
                    Toast.makeText(getContext(), requireActivity().getString(R.string.systolic_range_error), Toast.LENGTH_SHORT).show();
                } else if (diastolicBP < 30 || diastolicBP > 300) {
                    Toast.makeText(getContext(), requireActivity().getString(R.string.diastolic_range_error), Toast.LENGTH_SHORT).show();
                } else {
                    ArrayList<String> data = new ArrayList<>();
                    data.add(edtSystolicBP.getText().toString());
                    data.add(edtDiastolicBP.getText().toString());
                    onNextFragmentClickListener.getDataFromFragment(question.getQuestion(), data);
                    ((HealthAuditFormActivity) requireActivity()).navigateToNextPage();
                }
            }

        });
    }

}



