package com.example.rlapp.ui.mindaudit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.rlapp.ui.mindaudit.questions.Group;
import com.example.rlapp.ui.mindaudit.questions.Question;

class MAAssessmentPagerAdapter extends FragmentStateAdapter {
    private Group group;
    public MAAssessmentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Question question = group.getQuestions().get(position);
        return MindAuditQuestionListFragment.newInstance(question);
    }

    @Override
    public int getItemCount() {
        if (group != null)
            return group.getQuestions().size();
        return 0;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}