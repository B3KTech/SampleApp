package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.atakmap.android.pulsetool.plugin.R;
import com.garmin.health.sync.WellnessData;
import com.garmin.health.sync.WellnessEpoch;
import com.google.common.collect.Collections2;

import java.util.*;

/**
 * Fragment displaying a summary of activities at sync.
 *
 * @author ioana.morari on 10/27/2016.
 */

public class MonitoringTabFragment extends BaseTabFragment
{
    private final static String MONITORING_DATA = "monitoring.data";

    private List<WellnessEpoch> wellnessEpoches;

    public static MonitoringTabFragment getInstance(List<WellnessEpoch> wellnessEpoches)
    {
        ArrayList<WellnessEpoch> mymonitoring = new ArrayList<>(wellnessEpoches);

        MonitoringTabFragment monitoringTabFragment = new MonitoringTabFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MONITORING_DATA, mymonitoring);
        monitoringTabFragment.setArguments(bundle);

        return monitoringTabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(getArguments() != null)
        {
            wellnessEpoches = getArguments().getParcelableArrayList(MONITORING_DATA);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if(wellnessEpoches.isEmpty())
        {
            emptyListView(R.string.empty_monitoring_list_message);
        }

        Set<Integer> wellnessDates = new HashSet<>();

        for(WellnessEpoch epoch : wellnessEpoches)
        {
            wellnessDates.add(epoch.date());
        }

        for(Integer date : wellnessDates)
        {
            Collection<WellnessEpoch> dateCollection = Collections2.filter(wellnessEpoches, input -> input != null && input.date() == date);

            WellnessEpoch mergedEpoch = WellnessEpoch.merge(new ArrayList<>(dateCollection));

            displayTitle(mergedEpoch.getWellnessData());
            displayDivider();
            displayTextData(mergedEpoch.getWellnessData());
        }
    }

    private void displayTitle(WellnessData wellnessData)
    {
        TextView title = (TextView) getActivity().getLayoutInflater().inflate(R.layout.text_view_summary_title, null, false);
        title.setText(DateFormat.format("MM/dd/yyyy HH:mm", wellnessData.getTimestamp().getBeginTimestampSeconds() * 1000) + " -> " +
                DateFormat.format("MM/dd/yyyy HH:mm", wellnessData.getTimestamp().getEndTimestampSeconds() * 1000));

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.addView(title, layoutParams);
    }

    private void displayTextData(WellnessData wellnessData)
    {
        TextView data = (TextView) getActivity().getLayoutInflater().inflate(R.layout.text_view_summary, null, false);
        String result = "";
        result += "Steps: " + wellnessData.getSteps() + "\n";
        result += "Distance: " + wellnessData.getDistance() + "\n";
        result += "Meters Climbed: " + wellnessData.getMetersAscended() + "\n";
        result += "Meters Descended: " + wellnessData.getMetersDescended() + "\n";
        result += "Active Calories: " + wellnessData.getActiveCalories() + "\n";
        result += "Total Calories: " + wellnessData.getTotalCalories() + "\n";
        result += "Moderate Intensity Minutes: " + wellnessData.getModerateIntensityMinutes() + "\n";
        result += "Vigorous Intensity Minutes: " + wellnessData.getVigorousIntensityMinutes() + "\n\n";

        data.setText(result);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.addView(data, layoutParams);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_child_tab;
    }
}
