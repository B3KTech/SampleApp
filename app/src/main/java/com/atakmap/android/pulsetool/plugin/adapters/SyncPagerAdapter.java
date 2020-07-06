package com.atakmap.android.pulsetool.plugin.adapters;

import android.content.Context;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.sync.*;
import com.garmin.health.sync.SyncData;

/**
 * ViewPagerAdapter used to scroll between summary tabs.
 *
 * @author ioana.morari on 10/27/2016.
 */

public class SyncPagerAdapter extends FragmentStatePagerAdapter
{
    private final static String TAG = SyncPagerAdapter.class.getSimpleName();
    private final static int PAGES = 7;

    private Context context;
    private SyncData syncResult;

    public SyncPagerAdapter(FragmentManager fm, Context context, SyncData syncResult)
    {
        super(fm);
        this.context = context;
        this.syncResult = syncResult;
    }

    @Override
    public Fragment getItem(int position)
    {
        Log.d(TAG, "getItem(position = " + position + ")");

        Fragment page = null;

        switch(position)
        {
            case 0:
                page = MonitoringTabFragment.getInstance(syncResult.getWellnessEpochs());
                break;

            case 1:
                page = ActivityTabFragment.getInstance(syncResult.getActivityList());
                break;

            case 2:
                page = MoveIQTabFragment.getInstance(syncResult.getMoveIQEventList());
                break;

            case 3:
                page = MotionIntensityTabFragment.getInstance(syncResult.getMotionIntensityList());
                break;

            case 4:
                page = FitnessTabFragment.getInstance(syncResult.getVO2Max(), syncResult.getRestingHeartRates());
                break;

            case 5:
                page = StressLevelFragment.getInstance(syncResult.getStressList());
                break;

            case 6:
                page = AdvancedMetricFragment.getInstance(syncResult.getSpo2DataList(), syncResult.getRespirationList());
                break;

        }
        return page;
    }

    @Override
    public int getCount()
    {
        return PAGES;
    }

    @Override
    public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return "<- " + context.getString(PageTitlePositionMatching.values()[position].resourceId) + " ->";
    }

    private enum PageTitlePositionMatching
    {
        MONITORING(R.string.monitoring_tab_title),
        ACTIVITY(R.string.activity_tab_title),
        MOVE_IQ(R.string.activity_move_iq),
        MOTION_INTENSITY(R.string.activity_motion_intensity),
        FITNESS(R.string.fitness_tab_title),
        STRESS_LEVEL(R.string.stress_level_tab_title),
        SPO2_DATA(R.string.spo2_data_tab_title);

        final int resourceId;

        PageTitlePositionMatching(int resourceId)
        {
            this.resourceId = resourceId;
        }
    }
}
