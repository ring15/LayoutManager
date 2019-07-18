package com.founq.sdk.layoutmanager;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ring on 2019/7/18.
 */
public class SlideLayoutManager extends RecyclerView.LayoutManager {

    //item高度
    private int mItemViewWidth;
    //item宽度
    private int mItemViewHeight;
    //item数量
    private int mItemCount;
    //偏移量
    private int mScrollOffset = Integer.MAX_VALUE;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 || state.isPreLayout()) return;

        //初始化变量值
        mItemViewHeight = (int) (getVerticalSpace() * 0.88f);
        mItemViewWidth = (int) (mItemViewHeight * 0.63f);
        mItemCount = getItemCount();
        mScrollOffset = Math.min(Math.max(mItemViewWidth, mScrollOffset), mItemViewWidth * mScrollOffset);

        //正式测量item的宽高
        layoutChild(recycler);

    }

    private void layoutChild(RecyclerView.Recycler recycler) {

        List<Integer> lefts = new ArrayList<>();
        List<Integer> tops = new ArrayList<>();
        List<Float> scales = new ArrayList<>();
        for (int i = mItemCount - 1; i >= 0; i--) {
            int left = getHorizontalSpace() - mItemViewWidth - 60 * i;
            int top = (getVerticalSpace() - mItemViewHeight) / 2;
            float scale = (1.0f - i*0.1f);
            lefts.add(left);
            tops.add(top);
            scales.add(scale);
        }

        detachAndScrapAttachedViews(recycler);

        for (int j = 0; j < mItemCount; j++) {
            View view = recycler.getViewForPosition(j);
            addView(view);
            measureChildWithExactlySize(view);
            layoutDecoratedWithMargins(view, lefts.get(j), tops.get(j), lefts.get(j) + mItemViewWidth, tops.get(j) + mItemViewHeight);
            view.setPivotX(0);
            view.setPivotY(view.getHeight() / 2);
            view.setScaleX(scales.get(j));
            view.setScaleY(scales.get(j));
        }

    }


    /**
     * 测量itemview的确切大小
     */
    private void measureChildWithExactlySize(View child) {
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(mItemViewWidth, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(mItemViewHeight, View.MeasureSpec.EXACTLY);
        child.measure(widthSpec, heightSpec);
    }

    /**
     * 获取高度
     *
     * @return
     */
    private int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    /**
     * 获取宽度
     *
     * @return
     */
    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
}
