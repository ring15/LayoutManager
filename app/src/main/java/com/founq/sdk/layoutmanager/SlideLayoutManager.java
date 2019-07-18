package com.founq.sdk.layoutmanager;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

        int remainSpace = getHorizontalSpace() - mItemViewWidth;//剩余部分的总长度

        int start = -1;
        List<Integer> lefts = new ArrayList<>();
        List<Integer> tops = new ArrayList<>();
        List<Float> scales = new ArrayList<>();
        for (int i = mItemCount - 1, j = 0; i >= 0; i--, j++) {
            int left = getHorizontalSpace() - mItemViewWidth - 60 * i;
            int top = (getVerticalSpace() - mItemViewHeight) / 2;
            float scale = (1.0f - i * 0.1f);
            lefts.add(left);
            tops.add(top);
            scales.add(scale);
            //每加一个view，增加的显示宽度是60（上边的60*i），这边剩余部分就要减去60
            remainSpace = remainSpace - 60;
            //这边要加一个判断，不要将所有item都新建，只新建在屏幕内的部分
            if (remainSpace <= 0 && start < 0) {
                lefts.set(j, left + 60);
                scales.set(j, scale + 0.1f);
                start = i;
            }
        }

        detachAndScrapAttachedViews(recycler);

        start = start == -1 ? 0 : start;
        for (int i = start; i < mItemCount; i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithExactlySize(view);
            layoutDecoratedWithMargins(view, lefts.get(i), tops.get(i), lefts.get(i) + mItemViewWidth, tops.get(i) + mItemViewHeight);
            //更新坐标系中心点位置
            view.setPivotX(0);
            view.setPivotY(view.getHeight() / 2);
            view.setScaleX(scales.get(i));
            view.setScaleY(scales.get(i));
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
