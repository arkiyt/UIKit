package com.angcyo.uiview.less.widget.pager;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.angcyo.lib.L;
import com.angcyo.uiview.less.skin.SkinHelper;
import com.angcyo.uiview.less.utils.RUtils;
import com.angcyo.uiview.less.utils.Reflect;

/**
 * Created by angcyo on 2017-01-14.
 */

public class RViewPager extends ViewPager {

    private static final String TAG = "angcyo";
    private int mOrientation = LinearLayout.HORIZONTAL;
    private GestureDetectorCompat mGestureDetectorCompat;

    private OnPagerEndListener mOnPagerEndListener;

    private int heightMeasureMode = MeasureSpec.EXACTLY;

    public RViewPager(Context context) {
        this(context, null);
    }

    public RViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (mOrientation == LinearLayout.HORIZONTAL) {
            setPageTransformer(true, new FadeInOutPageTransformer());
        } else {
            setPageTransformer(true, new DefaultVerticalTransformer());
        }
        mGestureDetectorCompat = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (mOnPagerEndListener != null &&
                        getAdapter() != null &&
                        getCurrentItem() == getAdapter().getCount() - 1) {
                    if (mOrientation == LinearLayout.VERTICAL) {
                        if (velocityY < -1000) {
                            mOnPagerEndListener.onPagerEnd();
                        }
                    } else {
                        if (velocityX < -1000) {
                            mOnPagerEndListener.onPagerEnd();
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                if (heightMeasureMode != MeasureSpec.EXACTLY) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            requestLayout();
                        }
                    });
                }
            }
        });
    }

    public static void ensureGlow(ViewPager viewPager, int color) {
        if (RUtils.isLollipop()) {
            if (viewPager != null) {
                viewPager.setOverScrollMode(OVER_SCROLL_NEVER);
            }
            return;
        }
        try {
            Object mGlow = Reflect.getMember(ViewPager.class, viewPager, "mLeftEdge");
            setEdgeEffect(mGlow, color);
            mGlow = Reflect.getMember(ViewPager.class, viewPager, "mRightEdge");
            setEdgeEffect(mGlow, color);
        } catch (Exception e) {
            L.e(e.getMessage());
        }
    }

    public static void ensureGlow(HorizontalScrollView horizontalScrollView, int color) {
        if (RUtils.isLollipop()) {
            if (horizontalScrollView != null) {
                horizontalScrollView.setOverScrollMode(OVER_SCROLL_NEVER);
            }
            return;
        }
        try {
            Object mGlow = Reflect.getMember(HorizontalScrollView.class, horizontalScrollView, "mEdgeGlowLeft");
            setEdgeEffect(mGlow, color);
            mGlow = Reflect.getMember(HorizontalScrollView.class, horizontalScrollView, "mEdgeGlowRight");
            setEdgeEffect(mGlow, color);
        } catch (Exception e) {
            L.e(e.getMessage());
        }
    }

    public static void setEdgeEffect(Object edgeEffectCompat, @ColorInt int color) {
        if (edgeEffectCompat instanceof EdgeEffect && RUtils.isLollipop()) {
            ((EdgeEffect) edgeEffectCompat).setColor(color);
            return;
        }

        Object mEdgeEffect = Reflect.getMember(edgeEffectCompat, "mEdgeEffect");
        Object mPaint;

        if (mEdgeEffect != null) {
            mPaint = Reflect.getMember(mEdgeEffect, "mPaint");
        } else {
            mPaint = Reflect.getMember(edgeEffectCompat, "mPaint");
        }

        if (mPaint instanceof Paint) {
            ((Paint) mPaint).setColor(color);
        }
    }

    public void setOnPagerEndListener(OnPagerEndListener onPagerEndListener) {
        mOnPagerEndListener = onPagerEndListener;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void resetItem(int position) {
        PagerAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.destroyItem(this, position, getChildAt(position));
            adapter.instantiateItem(this, position);
        }
//        if (adapter instanceof RPagerAdapter) {
//            WeakReference<View> viewWeakReference = ((RPagerAdapter) adapter).mViewCache.get(position);
//            View view = null;
//            if (viewWeakReference != null) {
//                view = viewWeakReference.get();
//            }
//            if (view != null) {
//                ((RPagerAdapter) adapter).initItemView(view, position);
//            }
//        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        heightMeasureMode = heightMode;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
        } else {
            //支持高度的wrap_content
            if (getChildCount() > getCurrentItem()) {
                View childAt = getChildAt(getCurrentItem());
                measureChild(childAt, widthMeasureSpec, heightMeasureSpec);
                setMeasuredDimension(widthSize, childAt.getMeasuredHeight() + getPaddingLeft() + getPaddingRight());
            }
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!isInEditMode()) {
            ensureGlow(this, SkinHelper.getSkin().getThemeSubColor());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return true;
        }

        try {
            if (mOrientation == LinearLayout.VERTICAL) {
                return mGestureDetectorCompat.onTouchEvent(ev)
                        || super.onTouchEvent(swapTouchEvent(ev));
            }
            return mGestureDetectorCompat.onTouchEvent(ev)
                    || super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        try {
            if (mOrientation == LinearLayout.VERTICAL) {
                return super.onInterceptTouchEvent(swapTouchEvent(ev));
            }
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private MotionEvent swapTouchEvent(MotionEvent event) {
        float width = getWidth();
        float height = getHeight();

        float swappedX = (event.getY() / height) * width;
        float swappedY = (event.getX() / width) * height;

        event.setLocation(swappedX, swappedY);

        return event;
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        PagerAdapter oldAdapter = getAdapter();
        if (oldAdapter instanceof RPagerAdapter) {
            removeOnPageChangeListener((OnPageChangeListener) oldAdapter);
        }
        super.setAdapter(adapter);
        if (adapter instanceof RPagerAdapter) {
            addOnPageChangeListener((OnPageChangeListener) adapter);
        }
    }

    public interface OnPagerEndListener {
        /**
         * 最后一一页快速滚动
         */
        void onPagerEnd();
    }
}
