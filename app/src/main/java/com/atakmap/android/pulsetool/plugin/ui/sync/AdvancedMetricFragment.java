package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.BaseFragment;
import com.atakmap.android.pulsetool.plugin.ui.charts.GHLineChart;
import com.garmin.health.sync.Respiration;
import com.garmin.health.sync.SPO2Reading;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2017 Garmin International. All Rights Reserved.
 * <p></p>
 * This software is the confidential and proprietary information of
 * Garmin International.
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Garmin International.
 * <p></p>
 * Garmin International MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. Garmin International SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * <p></p>
 * Created by jacksoncol on 8/27/18.
 */
public class AdvancedMetricFragment extends BaseFragment
{
    public static final String SPO2 = "spo2";
    public static final String RESPIRATION = "respiration";

    public static AdvancedMetricFragment getInstance(List<SPO2Reading> spo2List, List<Respiration> respirationList)
    {
        AdvancedMetricFragment advancedMetricFragment = new AdvancedMetricFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(SPO2, (ArrayList<? extends Parcelable>)spo2List);
        bundle.putParcelableArrayList(RESPIRATION, (ArrayList<? extends Parcelable>)respirationList);

        advancedMetricFragment.setArguments(bundle);
        return advancedMetricFragment;
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

        GHLineChart spo2LevelGraph = view.findViewById(R.id.spo2_graph);
        spo2LevelGraph.createChart(getArguments());

        GHLineChart respirationLevelGraph = view.findViewById(R.id.respiration_graph);
        respirationLevelGraph.createChart(getArguments());
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.fragment_advanced_metrics;
    }
}
