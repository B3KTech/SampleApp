package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.charts.ChartData;
import com.atakmap.android.pulsetool.plugin.ui.charts.GHLineChart;
import com.garmin.health.sync.Stress;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
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
 * Created by jacksoncol on 2019-06-24.
 */
public class StressLevelChart extends GHLineChart
{
    private static final String STRESS_ENTRIES = "STRESS_ENTRIES";
    private static final String STRESS_LABELS = "STRESS_LABELS";
    private List<Stress> mStress = new ArrayList<>();

    public StressLevelChart(Context context)
    {
        super(context);
    }

    public StressLevelChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public StressLevelChart(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void createChart(Bundle savedInstanceState, long appStartTime)
    {
        long startTime = appStartTime;

        if(savedInstanceState != null)
        {
            List<Entry> entries;
            List<String> labels;

            if(startTime < 0)
            {
                ArrayList<Stress> result = savedInstanceState.getParcelableArrayList(StressLevelFragment.STRESS_LEVEL);

                if(result != null && !result.isEmpty())
                {
                    startTime = result.get(0).getTimestamp() * 1000;

                    LineDataSet dataSet = createDataSet(null, getContext().getString(R.string.stress_level_dataset), ColorTemplate
                            .getHoloBlue());
                    dataSet.setDrawFilled(true);
                    dataSet.setFillColor(ColorTemplate.getHoloBlue());
                    createGHChart(null, Collections.singletonList(dataSet), startTime);

                    for(Stress log : result)
                    {
                        mStress.add(log);
                        updateChart(new ChartData(log.getStressLevelValue(), log.getTimestamp() * 1000), log.getTimestamp() * 1000);
                    }
                }
                else
                {
                    setVisibility(GONE);
                }
            }
            else
            {
                entries = savedInstanceState.getParcelableArrayList(STRESS_ENTRIES);
                labels = savedInstanceState.getStringArrayList(STRESS_LABELS);

                LineDataSet dataSet = createDataSet(entries, getContext().getString(R.string.stress_level_dataset), ColorTemplate.getHoloBlue());
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(ColorTemplate.getHoloBlue());
                createGHChart(labels, Collections.singletonList(dataSet), startTime);
            }
        }
    }

    @Override
    protected int getChartXVisibleRange()
    {
        return mStress.size();
    }

    @Override
    public void updateChart(ChartData result, long updateTime)
    {
        if(result != null)
        {
            updateDataSet(result, getContext().getString(R.string.stress_level_dataset));
            updateGHChart(result);
        }
    }

    @Override
    public void saveChartState(Bundle outState)
    {
        outState.putParcelableArrayList(STRESS_ENTRIES, getChartEntries(getContext().getString(R.string.stress_level_dataset)));
        outState.putStringArrayList(STRESS_LABELS, getLabels());
    }
}
