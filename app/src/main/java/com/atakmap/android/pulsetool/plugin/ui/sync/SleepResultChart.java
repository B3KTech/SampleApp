package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;

import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.charts.ChartData;
import com.atakmap.android.pulsetool.plugin.ui.charts.GHLineChart;
import com.garmin.health.sleep.SleepInterval;
import com.garmin.health.sleep.SleepResult;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.DateFormat;
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
public class SleepResultChart extends GHLineChart
{
    public static final String SLEEP_RESULT = "sleep.result.key";

    private static final double REM_SLEEP_VAlUE = 6;
    private static final double DEEP_SLEEP_VALUE = 2;
    private static final double LIGHT_SLEEP_VALUE = 4;
    private static final double AWAKE_VALUE = 8;

    private static final String SLEEP_CHART_LABELS = "SLEEP_CHART_LABELS";

    private static final String MIN_VALUE_ENTRIES = "MIN_VALUE_ENTRIES";
    private static final String REM_SLEEP_ENTRIES = "REM_SLEEP_ENTRIES";
    private static final String DEEP_SLEEP_ENTRIES = "DEEP_SLEEP_ENTRIES";
    private static final String LIGHT_SLEEP_ENTRIES = "LIGHT_SLEEP_ENTRIES";
    private static final String AWAKE_VALUE_ENTRIES = "AWAKE_VALUE_ENTRIES";

    private int count = 0;

    public SleepResultChart(Context context)
    {
        super(context);
    }

    public SleepResultChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SleepResultChart(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void createChart(Bundle savedInstanceState, long appStartTime)
    {
        long startTime = appStartTime;

        if(savedInstanceState != null)
        {
            List<String> sleepLabels;

            List<Entry> minEntries = new ArrayList<>();
            List<Entry> remEntries = new ArrayList<>();
            List<Entry> deepEntries = new ArrayList<>();
            List<Entry> lightEntries = new ArrayList<>();
            List<Entry> awakeEntries = new ArrayList<>();

            if(startTime < 0)
            {
                SleepResult result = savedInstanceState.getParcelable(SleepResultChart.SLEEP_RESULT);

                if(result != null && result.getSleepLevelMap() != null)
                {
                    startTime = result.getStartTimestamp() * 1000;

                    count = (int)((result.getEndTimestamp() - result.getStartTimestamp()) / 60) + 1;

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(startTime);

                    List<String> xLabels = new ArrayList<>();

                    for(int i = 0; i < count; i++)
                    {
                        calendar.add(Calendar.MINUTE, 1);
                        xLabels.add(String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)));
                    }

                    int time = 0;
                    int startMinute;
                    int endMinute;

                    for(SleepInterval interval : result.getSleepLevelMap().getAwake() != null ? result.getSleepLevelMap().getAwake() : Collections.<SleepInterval>emptyList())
                    {
                        startMinute = (int)(interval.getStartSecond() / 60);
                        endMinute = (int)(interval.getEndSecond() / 60);

                        for(int i = time; i < startMinute; i++, time++)
                        {
                            awakeEntries.add(new Entry(-1, i));
                        }

                        for(int i = startMinute; i < endMinute; i++, time++)
                        {
                            awakeEntries.add(new Entry((float)AWAKE_VALUE, i));
                        }

                        if(time < count)
                        {
                            awakeEntries.add(new Entry(-1, time));
                            time++;
                        }
                    }

                    time = 0;

                    for(SleepInterval interval : result.getSleepLevelMap().getDeepSleep() != null ? result.getSleepLevelMap().getDeepSleep() : Collections.<SleepInterval>emptyList())
                    {
                        startMinute = (int)(interval.getStartSecond() / 60);
                        endMinute = (int)(interval.getEndSecond() / 60);

                        for(int i = time; i < startMinute; i++, time++)
                        {
                            deepEntries.add(new Entry(-1, i));
                        }

                        for(int i = startMinute; i < endMinute; i++, time++)
                        {
                            deepEntries.add(new Entry((float)DEEP_SLEEP_VALUE, i));
                        }

                        if(time < count)
                        {
                            deepEntries.add(new Entry(-1, time));
                            time++;
                        }
                    }

                    time = 0;

                    for(SleepInterval interval : result.getSleepLevelMap().getLightSleep() != null ? result.getSleepLevelMap().getLightSleep() : Collections.<SleepInterval>emptyList())
                    {
                        startMinute = (int)(interval.getStartSecond() / 60);
                        endMinute = (int)(interval.getEndSecond() / 60);

                        for(int i = time; i < startMinute; i++, time++)
                        {
                            lightEntries.add(new Entry(-1, i));
                        }

                        for(int i = startMinute; i < endMinute; i++, time++)
                        {
                            lightEntries.add(new Entry((float)LIGHT_SLEEP_VALUE, i));
                        }

                        if(time < count)
                        {
                            lightEntries.add(new Entry(-1, time));
                            time++;
                        }
                    }

                    time = 0;

                    for(SleepInterval interval : result.getSleepLevelMap().getRemSleep() != null ? result.getSleepLevelMap().getRemSleep() : Collections.<SleepInterval>emptyList())
                    {
                        startMinute = (int)(interval.getStartSecond() / 60);
                        endMinute = (int)(interval.getEndSecond() / 60);

                        for(int i = time; i < startMinute; i++, time++)
                        {
                            remEntries.add(new Entry(-1, i));
                        }

                        for(int i = startMinute; i < endMinute; i++, time++)
                        {
                            remEntries.add(new Entry((float)REM_SLEEP_VAlUE, i));
                        }

                        if(time < count)
                        {
                            remEntries.add(new Entry(-1, time));
                            time++;
                        }
                    }

                    LineDataSet minSet = createDataSet(minEntries, getContext().getString(R.string.min_dataset), Color.TRANSPARENT);
                    LineDataSet remSet = createDataSet(remEntries, getContext().getString(R.string.rem_dataset), Color.CYAN);
                    LineDataSet deepSet = createDataSet(deepEntries, getContext().getString(R.string.deep_dataset), Color.DKGRAY);
                    LineDataSet lightSet = createDataSet(lightEntries, getContext().getString(R.string.light_dataset), Color.LTGRAY);
                    LineDataSet awakeSet = createDataSet(awakeEntries, getContext().getString(R.string.awake_dataset), Color.RED);

                    List<LineDataSet> lineDataSets = new ArrayList<>();

                    lineDataSets.add(minSet);
                    lineDataSets.add(remSet);
                    lineDataSets.add(deepSet);
                    lineDataSets.add(lightSet);
                    lineDataSets.add(awakeSet);

                    for(LineDataSet set : lineDataSets)
                    {
                        set.setDrawFilled(true);
                        set.setDrawCubic(false);
                        set.setFillColor(set.getColor());
                    }

                    createGHChart(xLabels, lineDataSets, startTime, count);
                    startChartYAxisAtZero(true);
                    setChartYAxisMax(9);
                    setNoDataText("");
                }
                else
                {
                    setVisibility(GONE);
                }
            }
            else
            {
                sleepLabels = savedInstanceState.getStringArrayList(SLEEP_CHART_LABELS);

                minEntries = savedInstanceState.getParcelableArrayList(MIN_VALUE_ENTRIES);
                remEntries = savedInstanceState.getParcelableArrayList(REM_SLEEP_ENTRIES);
                deepEntries = savedInstanceState.getParcelableArrayList(DEEP_SLEEP_ENTRIES);
                lightEntries = savedInstanceState.getParcelableArrayList(LIGHT_SLEEP_ENTRIES);
                awakeEntries = savedInstanceState.getParcelableArrayList(AWAKE_VALUE_ENTRIES);

                LineDataSet minSet = createDataSet(minEntries, getContext().getString(R.string.min_dataset), Color.TRANSPARENT);
                LineDataSet remSet = createDataSet(remEntries, getContext().getString(R.string.rem_dataset), Color.CYAN);
                LineDataSet deepSet = createDataSet(deepEntries, getContext().getString(R.string.deep_dataset), Color.DKGRAY);
                LineDataSet lightSet = createDataSet(lightEntries, getContext().getString(R.string.light_dataset), Color.LTGRAY);
                LineDataSet awakeSet = createDataSet(awakeEntries, getContext().getString(R.string.awake_dataset), Color.RED);

                List<LineDataSet> lineDataSets = new ArrayList<>();

                lineDataSets.add(minSet);
                lineDataSets.add(remSet);
                lineDataSets.add(deepSet);
                lineDataSets.add(lightSet);
                lineDataSets.add(awakeSet);

                for(LineDataSet set : lineDataSets)
                {
                    set.setDrawFilled(true);
                    set.setDrawCubic(false);
                    set.setFillColor(set.getColor());
                    set.setDrawValues(false);
                }

                createGHChart(sleepLabels, lineDataSets, startTime);
                setChartYAxis(0, 9);
                setNoDataText("");
            }
        }
    }

    @Override
    protected int getChartXVisibleRange()
    {
        return count;
    }

    @Override
    public void saveChartState(Bundle outState)
    {
        outState.putStringArrayList(SLEEP_CHART_LABELS, getLabels());

        outState.putParcelableArrayList(MIN_VALUE_ENTRIES, getChartEntries(getContext().getString(R.string.min_dataset)));
        outState.putParcelableArrayList(REM_SLEEP_ENTRIES, getChartEntries(getContext().getString(R.string.rem_dataset)));
        outState.putParcelableArrayList(DEEP_SLEEP_ENTRIES, getChartEntries(getContext().getString(R.string.deep_dataset)));
        outState.putParcelableArrayList(LIGHT_SLEEP_ENTRIES, getChartEntries(getContext().getString(R.string.light_dataset)));
        outState.putParcelableArrayList(AWAKE_VALUE_ENTRIES, getChartEntries(getContext().getString(R.string.awake_dataset)));
    }

    @Override
    public void updateChart(ChartData result, long updateTime)
    {
        if(result != null)
        {
            updateDataSet(result, result.getSet());
            updateGHChart(result);
        }
    }
}
