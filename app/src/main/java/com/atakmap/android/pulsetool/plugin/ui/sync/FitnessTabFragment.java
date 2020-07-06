package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.atakmap.android.pulsetool.plugin.R;
import com.garmin.health.sync.RestingHeartRate;
import com.garmin.health.sync.VO2Max;

import java.util.ArrayList;
import java.util.List;


/**
 * @author morari on 5/23/17.
 */

public class FitnessTabFragment extends BaseTabFragment {

    private final static String VO2MAX_DATA = "vo2max.data";
    private final static String RESTING_HR = "resting.hr.data";

    private VO2Max mVO2Max;
    private List<RestingHeartRate> mRestingHeartRateList;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_child_tab;
    }

    public static FitnessTabFragment getInstance(VO2Max vo2Max, List<RestingHeartRate> restingHeartRates) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(VO2MAX_DATA, vo2Max);
        bundle.putParcelableArrayList(RESTING_HR, (ArrayList<? extends Parcelable>)restingHeartRates);

        FitnessTabFragment fitnessTabFragment = new FitnessTabFragment();
        fitnessTabFragment.setArguments(bundle);
        return fitnessTabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.mVO2Max = getArguments().getParcelable(VO2MAX_DATA);
            this.mRestingHeartRateList = getArguments().getParcelableArrayList(RESTING_HR);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mVO2Max == null && mRestingHeartRateList.isEmpty()) {
            emptyListView(R.string.empty_fitness_list_message);
            return;
        }

        if (mVO2Max != null) {
            displayTitle(getString(R.string.vo2_max));
            displayVO2MaxData(mVO2Max);
            displayDivider();
        }

        if (mRestingHeartRateList != null && !mRestingHeartRateList.isEmpty()) {
            displayTitle(getString(R.string.resting_heart_rate));
            displayDivider();
            for (RestingHeartRate restingHeartRate : mRestingHeartRateList) {
                displayRHRTextData(restingHeartRate);
                displayDivider();
            }
        }
    }

    private void displayTitle(String text) {

        TextView title = (TextView)getActivity().getLayoutInflater().inflate(R.layout.text_view_summary_title, null, false);
        title.setText(text);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.addView(title, layoutParams);
    }

    private void displayRHRTextData(RestingHeartRate restingHeartRate) {

        TextView data = (TextView)getActivity().getLayoutInflater().inflate(R.layout.text_view_summary, null, false);
        String result = "";

        result += "Date: " + DateFormat.format("MM/dd/yyyy HH:mm", restingHeartRate.getTimestamp() * 1000) + "\n";
        result += "RHR: " + restingHeartRate.getRestingHeartRate() + "\n";
        result += "Current Day RHR: " + restingHeartRate.getCurrentDayRestingHeartRate();
        data.setText(result);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.addView(data, layoutParams);
    }

    private void displayVO2MaxData(VO2Max vo2Max) {

        TextView data = (TextView)getActivity().getLayoutInflater().inflate(R.layout.text_view_summary, null, false);
        String result = "";
        result += "Date: " + vo2Max.getTimestamp() + "\n";
        result += "VO2 max: " + vo2Max.getVO2MaxValue() + "\n \n";
        data.setText(result);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.addView(data, layoutParams);
    }
}
