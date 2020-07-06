package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.atakmap.android.pulsetool.plugin.R;
import com.garmin.health.sync.Activity;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment displaying a summary of activities at sync.
 *
 * @author ioana.morari on 10/27/2016.
 */

public class ActivityTabFragment extends BaseTabFragment
{
    private final static String ACTIVITY_DATA = "activity.data";

    private List<Activity> activitySyncData;

    public static ActivityTabFragment getInstance(List<Activity> activitySyncData)
    {
        ActivityTabFragment activityTabFragment = new ActivityTabFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ACTIVITY_DATA, (ArrayList<? extends Parcelable>) activitySyncData);
        activityTabFragment.setArguments(bundle);

        return activityTabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            activitySyncData = getArguments().getParcelableArrayList(ACTIVITY_DATA);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (activitySyncData.isEmpty()){
            emptyListView(R.string.empty_activity_list_message);
        }

        for(Activity activity : activitySyncData)
        {
            displayTitle(activity);
            displayDivider();
            displayTextData(activity);
        }
    }

    private void displayTitle(Activity activity) {

        TextView title = (TextView) getActivity().getLayoutInflater().inflate(R.layout.text_view_summary_title, null, false);

        String titleString = "%s (%s)";
        titleString = String.format(titleString, activity.getSportName() == null ? "UNNAMED" : activity.getSportName(), activity.getSportType());

        title.setText(titleString);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.addView(title, layoutParams);
    }

    @SuppressLint("DefaultLocale")
    private void displayTextData(Activity activity)
    {
        TextView data = (TextView) getActivity().getLayoutInflater().inflate(R.layout.text_view_summary, null, false);
        String result = "";
        result += String.format("Distance: %.2f meters\n", activity.getTotalDistance());
        result += String.format("Speed: %.2f meters/second\n", activity.getAverageSpeed());
        result += String.format("Heart Rate: %d bpm\n \n", activity.getAverageHeartRate());
        data.setText(result);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.addView(data, layoutParams);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_child_tab;
    }
}
