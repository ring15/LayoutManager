package com.founq.sdk.layoutmanager;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ring on 2019/7/18.
 */
public class HorizontalEchelonManager extends RecyclerView.LayoutManager {

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
        mScrollOffset = Math.min(Math.max(mItemViewWidth, mScrollOffset), mItemViewWidth * mItemCount);

        //正式测量item的宽高
        layoutChild(recycler);

    }

    private void layoutChild(RecyclerView.Recycler recycler) {
        if (getItemCount() == 0) return;

        int bottomItemPosition = (int) Math.floor(mScrollOffset / mItemViewWidth);
        int remainSpace = getHorizontalSpace() - mItemViewWidth;//剩余部分的总长度
        int top = (getVerticalSpace() - mItemViewHeight) / 2;
        float initScale = 1.0f;

        //这个其实就想当于位移距离吧
        //初值0，从item的宽度877到0（滑动时)
        int bottomItemVisibleWidth = mScrollOffset % mItemViewWidth;

        //初值1，从0到1
        float percent = 1 - 1.0f * bottomItemVisibleWidth / mItemViewWidth;


        List<Integer> lefts = new ArrayList<>();
        List<Float> scales = new ArrayList<>();
        for (int i = bottomItemPosition - 1, j = 0; i >= 0; i--, j++) {
            remainSpace = remainSpace - 60;
            initScale = initScale - 0.1f;
            int left = (int) (remainSpace + percent * 60);//第一个就应该是remainspace，剩下的会多，不能用percent控制初值
            float scale = initScale + percent * 0.1f;//第一个是initScale， 其余会多

            if (remainSpace <= 0) {
                lefts.add(remainSpace + 60);//让它基于remainspace+60，是为了让它不随着滑动而移动
                scales.add(initScale + 0.1f);
                break;
            }
            lefts.add(left);//从0开始
            scales.add(scale);
        }

        if (bottomItemPosition < mItemCount) {//view是8个，size是9
            int left = getHorizontalSpace() - bottomItemVisibleWidth;
            lefts.add(0, left);
            scales.add(0, 1.0f);
            bottomItemPosition = bottomItemPosition + 1;//当bottomItemPosition = 4时，size = 5，会出现view获取-1的状态，所以将整体都往前移动一位
        }
//        else {//view是8个，size是8
//            bottomItemPosition = bottomItemPosition - 1;//-1同理，但是下边要加+，j也变成<= bottomItemPosition
//        }

        int start = bottomItemPosition - lefts.size(); //-1应该是为了多加一个界面，第一个界面直接出去，多加带动画的
        int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            int position = getPosition(childView);
            if (position > bottomItemPosition || position < start) {
                removeAndRecycleView(childView, recycler);
            }
        }

        detachAndScrapAttachedViews(recycler);

        for (int i = lefts.size() - 1, j = bottomItemPosition - lefts.size(); j < bottomItemPosition; i--, j++) {
            View view = recycler.getViewForPosition(j);
            addView(view);
            measureChildWithExactlySize(view);
            layoutDecoratedWithMargins(view, lefts.get(i), top,
                    lefts.get(i) + mItemViewWidth,
                    top + mItemViewHeight);
            view.setPivotX(0);
            view.setPivotY(view.getWidth() / 2);
            view.setScaleX(scales.get(i));
            view.setScaleY(scales.get(i));
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pendingScrollOffset = mScrollOffset + dx;
        //值的变化范围7016--877每个item的宽度是877，总共8个，7016
        mScrollOffset = Math.min(Math.max(mItemViewWidth, mScrollOffset + dx), mItemCount * mItemViewWidth);
        layoutChild(recycler);
        return mScrollOffset - pendingScrollOffset + dx;
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
