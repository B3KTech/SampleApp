package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.atakmap.android.pulsetool.plugin.R;
import com.garmin.health.sync.MoveIQEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying a summary of activities at sync.
 *
 * @author ioana.morari on 11/12/2016.
 */

public class MoveIQTabFragment extends BaseTabFragment {

    private final static String MOVE_IQ_DATA = "move.iq.data";

    private List<MoveIQEvent> moveIQEvents;

    public static MoveIQTabFragment getInstance(List<MoveIQEvent> moveIQEvents) {

        MoveIQTabFragment moveIQTabFragment = new MoveIQTabFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MOVE_IQ_DATA, (ArrayList<? extends Parcelable>) moveIQEvents);
        moveIQTabFragment.setArguments(bundle);

        return moveIQTabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            moveIQEvents = getArguments().getParcelableArrayList(MOVE_IQ_DATA);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (moveIQEvents.isEmpty()) {
            emptyListView(R.string.empty_move_iq_list_message);
        }

        for (MoveIQEvent moveIQEvent : moveIQEvents) {
            displayTitle(moveIQEvent);
            displayDivider();
            displayTextData(moveIQEvent);
        }
    }

    private void displayTitle(MoveIQEvent moveIQEvent) {

        TextView title = (TextView) getActivity().getLayoutInflater().inflate(R.layout.text_view_summary_title, null, false);
        title.setText(moveIQEvent.getActivityType().name() + "      " + DateFormat.format("MM/dd/yyyy HH:mm", moveIQEvent.getStartTimestamp() * 1000));

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.addView(title, layoutParams);
    }

    private void displayTextData(MoveIQEvent moveIQEvent) {

        TextView data = (TextView) getActivity().getLayoutInflater().inflate(R.layout.text_view_summary, null, false);
        String result = "";
        result += "Duration: " + moveIQEvent.getDuration() + " min \n";
        data.setText(result);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parent.addView(data, layoutParams);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_child_tab;
    }
}
