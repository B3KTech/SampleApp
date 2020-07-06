package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.cardview.widget.CardView;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.BaseFragment;


/**
 * @author morari on 1/17/17.
 */

public abstract class BaseTabFragment extends BaseFragment {

    protected LinearLayout parent;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

/*        ScrollView parentScroll = view.findViewById(R.id.tab_scroll_view);
        parent = new LinearLayout(getContext());
        parent.setOrientation(LinearLayout.VERTICAL);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parentScroll.addView(parent, layoutParams);*/
        CardView cardView = view.findViewById(R.id.tab_card_view);
        parent = new LinearLayout(getContext());
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.addView(parent, layoutParams);
       }

    protected void emptyListView(@StringRes int emptyListId) {

        TextView textView = (TextView) getActivity().getLayoutInflater().inflate(R.layout.text_view_summary_title, null, false);
        textView.setText(emptyListId);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        parent.addView(textView, layoutParams);
    }

    protected void displayDivider() {

        View divider = getActivity().getLayoutInflater().inflate(R.layout.view_divider, null, false);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        parent.addView(divider, layoutParams);
    }
}
