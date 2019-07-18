package com.founq.sdk.layoutmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by ring on 2019/7/17.
 */
public class EchelonLayoutManager extends RecyclerView.LayoutManager {

    private Context mContext;
    //item高度
    private int mItemViewWidth;
    //item宽度
    private int mItemViewHeight;
    //item数量
    private int mItemCount;
    //偏移量
    private int mScrollOffset = Integer.MAX_VALUE;
    //缩放倍数
    private float mScale = 0.9f;

    public EchelonLayoutManager(Context context) {
        mContext = context;
    }

    /**
     * 重写layout manager唯一必须覆盖的
     * 实现只需要返回默认实例即可
     *
     * @return
     */
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    /**
     * layout manager的主要入口点，初始化时需要调用此方法（两处调用）
     *
     * @param recycler
     * @param state
     */
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //下边两句基本每个人都会这么写
        if (state.getItemCount() == 0 || state.isPreLayout()) return;
        removeAndRecycleAllViews(recycler);

        //想要实现的效果是从上向下滑动，那么最下边一张的位置基本是确定的，其他图片的位置，基于最后一张利用offset重新定位
        //设置itemview宽高初值（应该就是最下边一个想要放置的位置）
        mItemViewWidth = (int) (getHorizontalSpace() * 0.87f);//0.87应该是经验值吧，可以试着改一下
        mItemViewHeight = (int) (mItemViewWidth * 1.46f);//设置高是宽的倍数

        //获取一下item的总数
        mItemCount = getItemCount();

        //定义偏移量
        //第一次调用时，初值被设置为mItemCount * mItemViewHeight
        mScrollOffset = Math.min(Math.max(mItemViewHeight, mScrollOffset), mItemCount * mItemViewHeight);

        //计算每个itemview的位置
        layoutChild(recycler);

    }

    /**
     * 真正计算每个itemview的位置
     *
     * @param recycler
     */
    private void layoutChild(RecyclerView.Recycler recycler) {
        //item数量为0，不用计算
        if (getItemCount() == 0) return;

        //因为最下边一个item底部与recycler重合，计算距底部距离比较容易，初值就等于mItemCount（乘完除）
        int bottomItemPosition = (int) Math.floor(mScrollOffset / mItemViewHeight);//Math.floor()执行向下舍入，即它总是将数值向下舍入为最接近的整数

        int remainSpace = getVerticalSpace() - mItemViewHeight;//recycler padding

        int bottomItemVisibleHeight = mScrollOffset % mItemViewHeight;//偏移量，折叠，重复覆盖

        float offsetPercentRelativeToItemView = bottomItemVisibleHeight * 1.0f / mItemViewHeight;//偏移量与高度的比值

        //这里将每个itemview的偏移量保存了？
        ArrayList<ItemViewInfo> layoutInfo = new ArrayList<>();
        //第一次遍历整个item的数量
        for (int i = bottomItemPosition - 1, j = 1; i >= 0; i--, j++) {
            //最大偏移量设置的是padding的0.4倍，后一个是前一个的0.8倍（就是上边，每两个之间距离的比值，前一个几乎都是后一个的一半差点， 0.8换成1就只有一个了）
            double maxOffset = (getVerticalSpace() - mItemViewHeight) / 2 * Math.pow(0.8, j);//Math.pow(a,b):a的b次方
            //初始值，距上
            int start = (int) (remainSpace - offsetPercentRelativeToItemView * maxOffset);
            //计算值真的烦死了，不想看它是啥了 //控制缩放的，怎么算的，都不知道，算法好烦
            float scaleXY = (float) (Math.pow(mScale, j - 1) * (1 - offsetPercentRelativeToItemView * (1 - mScale)));
            float positionOffset = offsetPercentRelativeToItemView;
            //这个值是控制前后两个item之间的距离吧，反正设置1.0变成0.4，会造成两个item在底部出现重叠
            float layoutPercent = start * 1.0f / getVerticalSpace();
            ItemViewInfo info = new ItemViewInfo(start, scaleXY, positionOffset, layoutPercent);
            //为了让最后一个在最上边
            layoutInfo.add(0, info);
            remainSpace = (int) (remainSpace - maxOffset);
            //上边有部分不展示，设置与前一个重合
            if (remainSpace <= 0) {
                info.setTop((int) (remainSpace + maxOffset));
                info.setPositionOffset(0);
                info.setLayoutPercent(info.getTop() / getVerticalSpace());
                info.setScaleXY((float) Math.pow(mScale, j - 1));
                break;//只展示上边部分，以后的不会保存，也不展示
            }
        }

        if (bottomItemPosition < mItemCount) {
            int start = getVerticalSpace() - bottomItemVisibleHeight;
            layoutInfo.add(new ItemViewInfo(start, 1.0f,
                    bottomItemVisibleHeight * 1.0f / mItemViewHeight,
                    start * 1.0f / getVerticalSpace()).setIsBottom());
        } else {
            //出屏幕外的
            bottomItemPosition = bottomItemPosition - 1;
        }

        //获取item的位置，超出屏幕的会被回收
        int layoutCount = layoutInfo.size();
        final int startPos = bottomItemPosition - (layoutCount - 1);
        final int endPos = bottomItemPosition;
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            int pos = getPosition(childView);
            if (pos > endPos || pos < startPos) {
                removeAndRecycleView(childView, recycler);
            }
        }

        //这个应该是因为要调用两遍这个函数，所以先暂存再取出来，防止新建两次
        detachAndScrapAttachedViews(recycler);

        //开始绘制界面，距上，缩放等
        for (int i = 0; i < layoutCount; i++) {
            View view = recycler.getViewForPosition(startPos + i);
            ItemViewInfo itemViewInfo = layoutInfo.get(i);
            addView(view);
            measureChildWithExactlySize(view);
            int left = (getHorizontalSpace() - mItemViewWidth) / 2;
            layoutDecoratedWithMargins(view, left, itemViewInfo.getTop(), left + mItemViewWidth, itemViewInfo.getTop() + mItemViewHeight);
            view.setPivotX(view.getWidth() / 2);
            view.setPivotY(0);
            view.setScaleX(itemViewInfo.getScaleXY());
            view.setScaleY(itemViewInfo.getScaleXY());
        }
    }

    /**
     * 设置滑动事件，具体怎么计算的，再看
     *
     * @param dy
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pendingScrollOffset = mScrollOffset + dy;
        mScrollOffset = Math.min(Math.max(mItemViewHeight, pendingScrollOffset), mItemCount * mItemViewHeight);
        layoutChild(recycler);
        return mScrollOffset - pendingScrollOffset + dy;
    }

    /**
     * 设置能竖直滑动
     *
     * @return
     */
    @Override
    public boolean canScrollVertically() {
        return true;
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
     * 获取recyclerview的gaodu
     *
     * @return
     */
    private int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    /**
     * 获取recyclerview的宽度
     *
     * @return
     */
    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }
}
