package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.adapters.SyncPagerAdapter;
import com.atakmap.android.pulsetool.plugin.ui.BaseFragment;
import com.garmin.health.sync.SyncData;



/**
 * Fragment used as a container for summary and activity fragments.
 *
 * @author ioana.morari on 10/27/2016.
 */

public class TabFragment extends BaseFragment
{
    private final static String TITLE = "title";
    private final static String SYNC_RESULT = "sync.result";

    public static TabFragment getInstance(String title, SyncData syncResult)
    {
        TabFragment tabFragment = new TabFragment();

        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
        bundle.putParcelable(SYNC_RESULT, syncResult);
        tabFragment.setArguments(bundle);

        Log.d("TabFragment", "activities: " + syncResult.getActivityList().size());
        Log.d("TabFragment", "summary: " + syncResult.getWellnessEpochs().size());

        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(getArguments() != null)
        {
            setTitleInActionBar(getArguments().getString(TITLE));
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        ViewPager viewPager = view.findViewById(R.id.pager);

        if(getArguments() != null && getArguments().getParcelable(SYNC_RESULT) != null)
        {
            SyncPagerAdapter tabPagerAdapter = new SyncPagerAdapter(getChildFragmentManager(), getContext(), getArguments().getParcelable(SYNC_RESULT));
            viewPager.setAdapter(tabPagerAdapter);
        }
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.fragment_tab;
    }
}
