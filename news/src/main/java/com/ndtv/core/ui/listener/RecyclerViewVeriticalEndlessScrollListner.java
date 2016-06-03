package com.ndtv.core.ui.listener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by laveen on 17/2/15.
 */
public abstract class RecyclerViewVeriticalEndlessScrollListner extends RecyclerView.OnScrollListener {

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        try {
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (manager.findLastVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {
                loadNextPage();
            }
        } catch (ClassCastException ex) {

        }

    }

    public abstract void loadNextPage();

}
