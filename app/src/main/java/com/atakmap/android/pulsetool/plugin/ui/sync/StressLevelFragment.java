package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.BaseFragment;
import com.atakmap.android.pulsetool.plugin.ui.charts.GHLineChart;
import com.garmin.health.sync.Stress;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author morari on 6/27/17.
 */

public class StressLevelFragment extends BaseFragment
{
    static final String STRESS_LEVEL = "stress";

    public static StressLevelFragment getInstance(List<Stress> stressList)
    {
        StressLevelFragment stressLevelFragment = new StressLevelFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(STRESS_LEVEL, (ArrayList<? extends Parcelable>)stressList);

        stressLevelFragment.setArguments(bundle);
        return stressLevelFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        GHLineChart stressLevelGraph = view.findViewById(R.id.stress_level_graph);
        stressLevelGraph.createChart(getArguments());

        GHLineChart energyLevelGraph = view.findViewById(R.id.energy_level_graph);
        energyLevelGraph.createChart(getArguments());
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.fragment_stress_level;
    }

}
