package com.founq.sdk.layoutmanager;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ring on 2019/7/18.
 */
public class SlideLayoutManager extends RecyclerView.LayoutManager {

    private int mItemViewWidth;
    private int mItemViewHeight;
    private int mScrollOffset = Integer.MAX_VALUE;
    private int mItemCount;


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 || state.isPreLayout()) return;

        mItemViewHeight = (int) (getVerticalSpace() * 0.8f);
        mItemViewWidth = (int) (mItemViewHeight * 0.7f);
        mItemCount = getItemCount();
        mScrollOffset = Math.min(Math.max(mItemViewWidth, mScrollOffset), mItemCount * mItemViewWidth);

        layoutChild(0, recycler);
    }

    private void layoutChild(int dx, RecyclerView.Recycler recycler) {
        if (getItemCount() == 0) return;

        int bottomItemPosition = (int) Math.floor(mScrollOffset / mItemViewWidth);
        int bottomVisibleHeight = mScrollOffset % mItemViewWidth;

        int remainSpace = (getVerticalSpace() - mItemViewHeight) / 2;
        int bottomItemVisibleWidth = mScrollOffset % mItemViewHeight;
        float percent = 1 - 1.0f * bottomItemVisibleWidth / mItemViewHeight;
        float initScale = 1.0f;

        List<Integer> bottoms = new ArrayList<>();
        List<Float> scales = new ArrayList<>();
        List<Float> rotations = new ArrayList<>();
        List<Integer> lefts = new ArrayList<>();
        for (int i = bottomItemPosition - 1; i >= 0; i--) {
            remainSpace = remainSpace - 60;
            initScale = initScale - 0.05f;
            int left = (getHorizontalSpace() - mItemViewWidth) / 2;
            int bottom = (int) (remainSpace + percent * 60);
            float scale = initScale + 0.05f * percent;
            if (remainSpace <= 0) {
                bottoms.add(remainSpace + 60);
                scales.add(initScale + 0.05f);
                rotations.add(0f);
                lefts.add(left);
                break;
            }
            bottoms.add(bottom);
            scales.add(scale);
            rotations.add(0f);
            lefts.add(left);
        }


        if (bottomItemPosition < mItemCount){
            bottomItemPosition = bottomItemPosition + 1;
            int left = getHorizontalSpace() - bottomVisibleHeight;
            float rotation;
//            if (dx < 0){
                rotation = (1 - 1.0f * bottomVisibleHeight / mItemViewHeight) * 45;
//            }else {
//                rotation = - (1 - 1.0f * bottomVisibleHeight / mItemViewHeight) * 45;
//            }
            lefts.add(0, left);
            rotations.add(0, rotation);
            scales.add(0, 1.0f);
            if (bottoms.size() > 0){
                bottoms.add(0, bottoms.get(0));
            }else {
                bottoms.add(0, (getVerticalSpace() - mItemViewHeight) / 2);
            }
        }

        int start = bottomItemPosition - bottoms.size();
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            int position = getPosition(childView);
            if (position > bottomItemPosition || position < start) {
                removeAndRecycleView(childView, recycler);
            }
        }

        detachAndScrapAttachedViews(recycler);

        for (int i = bottoms.size()-1; i >= 0; i--){
            View view = recycler.getViewForPosition(bottomItemPosition - i - 1);
            addView(view);
            measureChildWithExactlySize(view);
            layoutDecoratedWithMargins(view, lefts.get(i),
                    getVerticalSpace() - bottoms.get(i) - mItemViewHeight - 120,
                    lefts.get(i) + mItemViewWidth, getVerticalSpace() - bottoms.get(i) - 120);
            view.setPivotX(view.getWidth() / 2);//下边缩放的中心点
            view.setPivotY(view.getHeight());
            view.setScaleY(scales.get(i));
            view.setScaleX(scales.get(i));
            view.setRotation(rotations.get(i));
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pending = dx + mScrollOffset;
        mScrollOffset = Math.min(Math.max(mItemCount, pending), mItemCount * mItemViewWidth);
        layoutChild(dx, recycler);
        return mScrollOffset - pending + dx;
    }

    /**
     * 测量itemview的确切大小
     */
    private void measureChildWithExactlySize(View child) {
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(mItemViewWidth, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(mItemViewHeight, View.MeasureSpec.EXACTLY);
        child.measure(widthSpec, heightSpec);
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }
}
