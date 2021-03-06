package com.angcyo.uiview.less.widget.group;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.angcyo.uiview.less.R;

/**
 * 用来控制状态栏的padding
 * Created by angcyo on 2016-11-05.
 */

public class ToastLayout extends FrameLayout {

    public ToastLayout(Context context) {
        super(context);
    }

    public ToastLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
            setClipToPadding(false);
            setClipChildren(false);
            setPadding(getPaddingLeft(),
                    statusBarHeight,
                    getPaddingRight(), getPaddingBottom());
            int height = statusBarHeight +
                    getResources().getDimensionPixelSize(R.dimen.action_bar_height);
            setMinimumHeight(height);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();//MeasureSpec.getSize(heightMeasureSpec);

        int shadowHeight = getResources().getDimensionPixelSize(R.dimen.base_toast_shadow_height);
        setMeasuredDimension(width, height + shadowHeight);
    }

    //    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int heightSize = getMeasuredHeight();
//        int height;
//
//        Context context = getContext();
////        if (context instanceof Activity) {
////            if (ResUtil.isLayoutFullscreen((Activity) context)) {
//
////            }
////        }
////        setMeasuredDimension(widthMeasureSpec, height);
//    }
}
