package com.atakmap.android.pulsetool.plugin.sleep;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.BaseFragment;
import com.atakmap.android.pulsetool.plugin.ui.charts.GHLineChart;
import com.garmin.health.sleep.RawSleepData;
import com.garmin.health.sleep.SleepResult;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;
import static com.atakmap.android.pulsetool.plugin.ui.sync.MotionIntensityChart.MOTION_INTENSITY_DATA;
import static com.atakmap.android.pulsetool.plugin.ui.sync.SleepResultChart.SLEEP_RESULT;

/**
 * @author ioana.morari on 3/16/17.
 */

public class SleepGraphFragment extends BaseFragment
{
    private SleepResult mSleepResult;
    private ArrayList<Parcelable> mRawSleepData;
    private TextView mStartLabel;
    private TextView mEndLabel;

    public static SleepGraphFragment getInstance(SleepResult sleepResult, RawSleepData rawSleepData)
    {
        SleepGraphFragment sleepGraphFragment = new SleepGraphFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(SLEEP_RESULT, sleepResult);

        if(rawSleepData != null)
        {
            bundle.putParcelableArrayList(MOTION_INTENSITY_DATA, new ArrayList<>(rawSleepData.getMotionIntensities()));
        }

        sleepGraphFragment.setArguments(bundle);
        return sleepGraphFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        GHLineChart sleepLevelGraph = view.findViewById(R.id.sleep_level_graph);
        sleepLevelGraph.createChart(getArguments());

        mStartLabel = view.findViewById(R.id.start_text);
        mEndLabel = view.findViewById(R.id.end_text);

        GHLineChart motionGraph = view.findViewById(R.id.raw_motion_graph);
        motionGraph.createChart(getArguments());

        if(getArguments() != null)
        {
            mRawSleepData = getArguments().getParcelableArrayList(MOTION_INTENSITY_DATA);
            mSleepResult = getArguments().getParcelable(SLEEP_RESULT);
        }

        if(mRawSleepData == null)
        {
            motionGraph.setVisibility(GONE);
            view.findViewById(R.id.motion_chart_card).setVisibility(GONE);

            LayoutParams params = sleepLevelGraph.getLayoutParams();
            params.height = (int)Math.round(params.height * 1.6);
            sleepLevelGraph.setLayoutParams(params);
        }

        if(mSleepResult != null && mRawSleepData == null)
        {
            mStartLabel.setText(String.format("Start: %s", DateTimeFormat.forPattern("MM/dd/yy hh:mm a").withZone(DateTimeZone.getDefault()).print(TimeUnit.SECONDS.toMillis(mSleepResult.getStartTimestamp()))));
            mEndLabel.setText(String.format("End: %s", DateTimeFormat.forPattern("MM/dd/yy hh:mm a").withZone(DateTimeZone.getDefault()).print(TimeUnit.SECONDS.toMillis(mSleepResult.getEndTimestamp()))));

            mStartLabel.setVisibility(View.VISIBLE);
            mEndLabel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.fragment_sleep;
    }
}
