package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.charts.ChartData;
import com.atakmap.android.pulsetool.plugin.ui.charts.GHLineChart;
import com.garmin.health.sync.Respiration;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

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
public class RespirationChart extends GHLineChart
{
    private static final String RESPIRATION_ENTRIES = "RESPIRATION_ENTRIES";
    private static final String RESPIRATION_LABELS = "RESPIRATION_LABELS";
    private List<Respiration> mRespiration = new ArrayList<>();

    public RespirationChart(Context context)
    {
        super(context);
    }

    public RespirationChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public RespirationChart(Context context, AttributeSet attrs, int defStyle)
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
                ArrayList<Respiration> result = savedInstanceState.getParcelableArrayList(AdvancedMetricFragment.RESPIRATION);

                if(result != null && !result.isEmpty())
                {
                    startTime = result.get(0).getTimestampMs();

                    LineDataSet dataSet = createDataSet(null, getContext().getString(R.string.respiration_dataSet), Color.GREEN);
                    dataSet.setDrawFilled(true);
                    dataSet.setFillColor(Color.GREEN);
                    createGHChart(null, Collections.singletonList(dataSet), startTime);

                    for(Respiration log : result)
                    {
                        mRespiration.add(log);
                        updateChart(new ChartData(log.getRespirationValue(), log.getTimestampMs()), log.getTimestampMs());
                    }
                }
                else
                {
                    setVisibility(GONE);
                }
            }
            else
            {
                entries = savedInstanceState.getParcelableArrayList(RESPIRATION_ENTRIES);
                labels = savedInstanceState.getStringArrayList(RESPIRATION_LABELS);

                LineDataSet dataSet = createDataSet(entries, getContext().getString(R.string.respiration_level_dataset), Color.GREEN);
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(Color.GREEN);
                createGHChart(labels, Collections.singletonList(dataSet), startTime);
            }
        }
    }

    @Override
    protected int getChartXVisibleRange()
    {
        return mRespiration.size();
    }

    @Override
    public void updateChart(ChartData result, long updateTime)
    {
        if(result != null)
        {
            updateDataSet(result, getContext().getString(R.string.respiration_level_dataset));
            updateGHChart(result);
        }
    }

    @Override
    public void saveChartState(Bundle outState)
    {
        outState.putParcelableArrayList(RESPIRATION_ENTRIES, getChartEntries(getContext().getString(R.string.respiration_level_dataset)));
        outState.putStringArrayList(RESPIRATION_LABELS, getLabels());
    }
}
