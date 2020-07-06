package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.charts.ChartData;
import com.atakmap.android.pulsetool.plugin.ui.charts.GHLineChart;
import com.garmin.health.sync.MotionIntensity;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
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
public class MotionIntensityChart extends GHLineChart
{
    public final static String MOTION_INTENSITY_DATA = "motion.intensity.data";

    private static final String MOTION_INTENSITY_ENTRIES = "MOTION_INTENSITY_ENTRIES";
    private static final String MOTION_INTENSITY_LABELS = "MOTION_INTENSITY_LABELS";
    private List<MotionIntensity> mMotionIntensity = new ArrayList<>();

    public MotionIntensityChart(Context context)
    {
        super(context);
    }

    public MotionIntensityChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public MotionIntensityChart(Context context, AttributeSet attrs, int defStyle)
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
                ArrayList<MotionIntensity> result = savedInstanceState.getParcelableArrayList(MotionIntensityChart.MOTION_INTENSITY_DATA);

                if(result != null && !result.isEmpty())
                {
                    startTime = result.get(0).getTimestamp().getBeginTimestampSeconds() * 1000;

                    LineDataSet dataSet = createDataSet(null, getContext().getString(R.string.motion_level_dataset), ColorTemplate
                            .getHoloBlue());
                    dataSet.setDrawFilled(true);
                    dataSet.setDrawCubic(false);
                    dataSet.setFillColor(ColorTemplate.getHoloBlue());
                    createGHChart(null, Collections.singletonList(dataSet), startTime);

                    for(MotionIntensity log : result)
                    {
                        mMotionIntensity.add(log);
                        addBar(log);
                        startChartYAxisAtZero(true);
                        setChartYAxisMax(8);
                    }
                }
                else
                {
                    setVisibility(GONE);
                }
            }
            else
            {
                entries = savedInstanceState.getParcelableArrayList(MOTION_INTENSITY_ENTRIES);
                labels = savedInstanceState.getStringArrayList(MOTION_INTENSITY_LABELS);

                LineDataSet dataSet = createDataSet(entries, getContext().getString(R.string.motion_level_dataset), ColorTemplate.getHoloBlue());
                dataSet.setDrawFilled(true);
                dataSet.setDrawCubic(false);
                dataSet.setFillColor(ColorTemplate.getHoloBlue());
                createGHChart(labels, Collections.singletonList(dataSet), startTime);
                startChartYAxisAtZero(true);
                setChartYAxisMax(8);
            }
        }
    }

    private void addBar(MotionIntensity motionIntensity)
    {
        int duration = (int) (motionIntensity.getTimestamp().getDuration() / 60);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(motionIntensity.getTimestamp().getBeginTimestampSeconds() * 1000);

        for(int i = 0; i < duration; i++)
        {
            updateChart(new ChartData((int)motionIntensity.getIntensityLevel(), calendar.getTimeInMillis()), calendar.getTimeInMillis());
            mMotionIntensity.add(motionIntensity);
            calendar.add(Calendar.MINUTE, 1);
        }
    }

    @Override
    protected int getChartXVisibleRange()
    {
        return mMotionIntensity.size() * 2;
    }

    @Override
    public void updateChart(ChartData result, long updateTime)
    {
        if(result != null)
        {
            updateDataSet(result, getContext().getString(R.string.motion_level_dataset));
            updateGHChart(result);
        }
    }

    @Override
    public void saveChartState(Bundle outState)
    {
        outState.putParcelableArrayList(MOTION_INTENSITY_ENTRIES, getChartEntries(getContext().getString(R.string.motion_level_dataset)));
        outState.putStringArrayList(MOTION_INTENSITY_LABELS, getLabels());
    }
}
