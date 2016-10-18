package com.beloo.widget.spanlayoutmanager.layouter;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;

import com.beloo.widget.spanlayoutmanager.ChipsLayoutManager;
import com.beloo.widget.spanlayoutmanager.cache.IViewCacheStorage;
import com.beloo.widget.spanlayoutmanager.gravity.IChildGravityResolver;

class LTRUpLayouter extends AbstractLayouter implements ILayouter {

    private int viewRight;

    LTRUpLayouter(ChipsLayoutManager layoutManager,
                  IChildGravityResolver childGravityResolver,
                  IViewCacheStorage cacheStorage,
                  int topOffset, int bottomOffset, int rightOffset) {
        super(layoutManager, topOffset, bottomOffset, cacheStorage, childGravityResolver);
        this.viewRight = rightOffset;
    }

    @Override
    public void layoutRow() {
        super.layoutRow();

        //if new view doesn't fit in row and it isn't only one view (we have to layout views with big width somewhere)
        //if previously row finished and we have to fill it
        viewTop = layoutRow(rowViews, viewTop, viewBottom, viewRight);

        //clear row data
        rowViews.clear();

        //go to next row, increase top coordinate, reset left
        viewRight = getCanvasWidth();
        viewBottom = viewTop;
    }

    @Override
    void addView(View view) {
        getLayoutManager().addView(view, 0);
    }

    @Override
    Rect createViewRect(View view) {
        int left = viewRight - currentViewWidth;
        int viewTop = viewBottom - currentViewHeight;

        Rect viewRect = new Rect(left, viewTop, viewRight, viewBottom);
        viewRight = viewRect.left;
        return viewRect;
    }

    @Override
    public boolean onAttachView(View view) {

        if (viewRight != getCanvasWidth() && viewRight - getLayoutManager().getDecoratedMeasuredWidth(view) < 0) {
            //new row
            viewRight = getCanvasWidth();
            viewBottom = viewTop;
        } else {
            viewRight = getLayoutManager().getDecoratedLeft(view);
        }

        viewTop = Math.min(viewTop, getLayoutManager().getDecoratedTop(view));

        return super.onAttachView(view);
    }

    @Override
    public boolean isFinishedLayouting() {
        return viewBottom < 0;
    }

    @Override
    public boolean canNotBePlacedInCurrentRow() {
        //when go up, check cache to layout according previous down algorithm
        boolean stopDueToCache = getCacheStorage().isPositionEndsRow(getCurrentViewPosition());
        if (stopDueToCache) return true;

        int bufLeft = viewRight - currentViewWidth;
        return bufLeft < 0 && viewRight < getCanvasWidth();
    }

    @Override
    public AbstractPositionIterator positionIterator() {
        return new DecrementalPositionIterator();
    }

}