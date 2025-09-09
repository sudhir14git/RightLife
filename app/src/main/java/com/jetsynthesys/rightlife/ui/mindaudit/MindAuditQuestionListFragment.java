package com.jetsynthesys.rightlife.ui.mindaudit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jetsynthesys.rightlife.R;
import com.jetsynthesys.rightlife.ui.mindaudit.questions.Question;
import com.jetsynthesys.rightlife.ui.mindaudit.questions.ScoringPattern;

import java.util.ArrayList;

public class MindAuditQuestionListFragment extends Fragment {
    private static final String ARG_QUESTION = "QUESTION";
    private static final String ARG_POSITION = "POSITION";
    private Question question;
    private TextView txt_question;
    private RecyclerView recyclerView;
    //private OnNextFragmentClickListener onNextFragmentClickListener;
    private MindAuditOptionsAdapter adapter;
    private int position = 0;
    private ActivityResultLauncher<Intent> resultLauncher;

    public static MindAuditQuestionListFragment newInstance(Question question, int position) {
        MindAuditQuestionListFragment fragment = new MindAuditQuestionListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_QUESTION, question);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = (Question) getArguments().getSerializable(ARG_QUESTION);
            position = getArguments().getInt(ARG_POSITION, 0);
        }

        // Register the launcher
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        new Handler().postDelayed(() -> {
                            if (question.isContinueFurtherIfTrue()) {
                                ((MAAssessmentQuestionaireActivity) requireActivity()).submitButton.setVisibility(View.VISIBLE);
                            } else {
                                ((MAAssessmentQuestionaireActivity) requireActivity()).navigateToNextPage();
                            }
                        }, 200);
                    }
                }
        );
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //onNextFragmentClickListener = (OnNextFragmentClickListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mind_audit_question_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        txt_question = view.findViewById(R.id.txt_question);

        txt_question.setText(question.getQuestion());
        ((MAAssessmentQuestionaireActivity) requireActivity()).nextButton.setVisibility(View.GONE);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        adapter = new MindAuditOptionsAdapter(requireContext(), (ArrayList<ScoringPattern>) question.getScoringPattern(), scoringPattern -> {
            ((MAAssessmentQuestionaireActivity) requireActivity()).addScore(question, scoringPattern);
            //change this condition as per your need
            //if (question.getQuestion().equalsIgnoreCase("") && scoringPattern.getOption().equalsIgnoreCase(""))
            if (position == 2) {
                openSecondActivity();
            } else {
                new Handler().postDelayed(() -> {
                    if (question.isContinueFurtherIfTrue()) {
                        ((MAAssessmentQuestionaireActivity) requireActivity()).submitButton.setVisibility(View.VISIBLE);
                    } else {
                        ((MAAssessmentQuestionaireActivity) requireActivity()).navigateToNextPage();
                    }
                }, 800);
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void openSecondActivity() {
        Intent intent = new Intent(requireContext(), RedFlagAlertActivity.class);
        resultLauncher.launch(intent);
    }
}
