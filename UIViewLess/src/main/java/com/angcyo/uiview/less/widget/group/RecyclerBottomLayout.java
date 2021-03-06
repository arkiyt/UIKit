package com.angcyo.uiview.less.widget.group;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * 此布局会占满RecycleView第一屏的底部所有空间
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/10/09
 */
public class RecyclerBottomLayout extends FrameLayout {

    public RecyclerBottomLayout(@NonNull Context context) {
        super(context);
    }

    public RecyclerBottomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerBottomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int layoutMeasureHeight = -1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode != MeasureSpec.EXACTLY) {
            layoutMeasureHeight = getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final ViewParent parent = getParent();
        //Log.w("angcyo", "layout:" + top + " " + bottom);
        boolean callSuper = true;
        if (parent instanceof RecyclerView) {

            final RecyclerView recyclerView = ((RecyclerView) parent);
            final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) getLayoutParams();
            int parentHeight = recyclerView.getMeasuredHeight();

            //只处理第一屏
            if (recyclerView.computeVerticalScrollOffset() == 0
                    && top < parentHeight //布局有部分展示了
                    && bottom > top) {
                if ((bottom + layoutParams.bottomMargin) != parentHeight) {
                    //布局未全部展示
                    //当前布局在RecyclerView的第一屏(没有任何滚动的状态), 并且底部没有显示全.

                    int spaceHeight = parentHeight - top - layoutParams.bottomMargin;

                    boolean handle;

                    if (layoutMeasureHeight > 0) {
                        handle = spaceHeight - layoutParams.topMargin - layoutParams.bottomMargin > layoutMeasureHeight;
                        if (!handle) {
                            //如果缓存了布局, 会出现此情况. 高度变高后, 无法回退到真实高度
                            if (layoutMeasureHeight != getMeasuredHeight()) {
                                spaceHeight = layoutMeasureHeight;
                                handle = true;
                            }
                        }
                    } else {
                        handle = spaceHeight - layoutParams.topMargin - layoutParams.bottomMargin > bottom - top;
                    }

                    if (handle) {
                        //剩余空间足够大, 同时也解决了动态隐藏导航栏带来的BUG
                        callSuper = false;

                        layoutParams.height = spaceHeight;
                        setLayoutParams(layoutParams);

                        post(new Runnable() {
                            @Override
                            public void run() {
                                //Log.e("angcyo", "重置高度:" + layoutParams.height);
                                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                                if (adapter != null) {
                                    adapter.notifyItemChanged(layoutParams.getViewAdapterPosition());
                                } else {
                                    requestLayout();
                                }
                            }
                        });
                    }

                }
            }
        }

        if (callSuper) {
            super.onLayout(changed, left, top, right, bottom);
        }
    }
}
