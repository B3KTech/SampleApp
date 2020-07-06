package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.os.Bundle;
import android.view.View;

import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.BaseFragment;
import com.atakmap.android.pulsetool.plugin.ui.charts.GHLineChart;
import com.garmin.health.sync.MotionIntensity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.atakmap.android.pulsetool.plugin.ui.sync.MotionIntensityChart.MOTION_INTENSITY_DATA;

/**
 * @author ioana.morari on 1/19/17.
 */

public class MotionIntensityTabFragment extends BaseFragment
{
    public static MotionIntensityTabFragment getInstance(List<MotionIntensity> motionIntensities)
    {
        MotionIntensityTabFragment motionIntensityTabFragment = new MotionIntensityTabFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MOTION_INTENSITY_DATA, new ArrayList<>(motionIntensities));
        motionIntensityTabFragment.setArguments(bundle);

        return motionIntensityTabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        final GHLineChart motionIntensityGraph = view.findViewById(R.id.motion_intensity_graph);
        motionIntensityGraph.createChart(getArguments());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_motion_intensity;
    }
}
