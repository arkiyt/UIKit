package com.angcyo.uiview.less.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.angcyo.uiview.less.resources.ResUtil;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/02/21 10:24
 * 修改人员：Robi
 * 修改时间：2017/02/21 10:24
 * 修改备注：
 * Version: 1.0.0
 */
public class UI {
    public static void setBackgroundDrawable(View view, Drawable drawable) {
        ResUtil.setBgDrawable(view, drawable);
    }

    public static void setViewHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
        if (view.getParent() != null) {
            view.getParent().requestLayout();
        }
    }

    public static void setViewMarginTop(View view, int top) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).topMargin = top;
            view.setLayoutParams(layoutParams);

            if (view.getParent() != null) {
                view.getParent().requestLayout();
            }
        }
    }

    public static void setViewWidth(View view, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
        if (view.getParent() != null) {
            view.getParent().requestLayout();
        }
    }

    public static void setView(View view, int width, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        layoutParams.width = width;
        view.setLayoutParams(layoutParams);
        if (view.getParent() != null) {
            view.getParent().requestLayout();
        }
    }

    public static void setViewF(View view, float width, float height) {
        setView(view, (int) width, (int) height);
    }

    /**
     * View 顶部是否还有可滚动的距离
     *
     * @param view
     */
    public static boolean canChildScrollUp(View view) {
        return canChildScroll(view, -1);
    }

    /**
     * View 底部是否还有可滚动的距离
     *
     * @param view
     */
    public static boolean canChildScrollDown(View view) {
        return canChildScroll(view, 1);
    }

    public static boolean canChildScroll(View view, int direction) {

        if (view == null) {
            return false;
        }

        if (view instanceof RecyclerView) {

        } else if (view instanceof ViewGroup) {
            final ViewGroup vGroup = (ViewGroup) view;
            View child;
            boolean result;
            for (int i = 0; i < vGroup.getChildCount(); i++) {
                child = vGroup.getChildAt(i);
                if (child instanceof RecyclerView) {
                    result = child.canScrollVertically(direction);
                } else if (child instanceof ViewGroup) {
                    result = canChildScroll(child, direction);
                } else if (child != null) {
                    result = child.canScrollVertically(direction);
                } else {
                    result = canChildScroll(child, direction);
                }

                if (result) {
                    return true;
                }
            }
        }

        return view.canScrollVertically(direction);
    }

    public static RecyclerView getRecyclerView(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View childAt = parent.getChildAt(i);
            if (childAt instanceof RecyclerView) {
                return (RecyclerView) childAt;
            } else if (childAt instanceof ViewGroup) {
                return getRecyclerView((ViewGroup) childAt);
            }
        }
        return null;
    }

    public static boolean copyAssetsTo(Context context, String assetsName, String fullPath) {
        if (TextUtils.isEmpty(assetsName) || TextUtils.isEmpty(fullPath)) {
            return false;
        }

        try {
            File targetFile = new File(fullPath);
            InputStream inputStream = context.getAssets().open(assetsName);

            if (!targetFile.exists()) {
                targetFile.getParentFile().mkdir();
            }

            OutputStream os = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(targetFile));
                byte data[] = new byte[1024];
                int len;
                while ((len = inputStream.read(data, 0, 1024)) != -1) {
                    os.write(data, 0, len);
                }
                os.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 修改EditText的光标
     */
    public static void setCursorDrawableRes(TextView textView, @DrawableRes int res) {
        Reflect.setMember(TextView.class, textView, "mCursorDrawableRes", res);
    }

    public static void setHorizontalTrackDrawable(View view, Drawable drawable) {
        Object mScrollCache = Reflect.getMember(View.class, view, "mScrollCache");
        Object scrollBar = Reflect.getMember(mScrollCache, "scrollBar");
        Reflect.invokeMethod(scrollBar, "setHorizontalTrackDrawable", new Class<?>[]{Drawable.class}, drawable);
    }

    public static void setHorizontalThumbDrawable(View view, Drawable drawable) {
        Object mScrollCache = Reflect.getMember(View.class, view, "mScrollCache");
        Object scrollBar = Reflect.getMember(mScrollCache, "scrollBar");
        Reflect.invokeMethod(scrollBar, "setHorizontalThumbDrawable", new Class<?>[]{Drawable.class}, drawable);
    }

    public static void setVerticalTrackDrawable(View view, Drawable drawable) {
        Object mScrollCache = Reflect.getMember(View.class, view, "mScrollCache");
        Object scrollBar = Reflect.getMember(mScrollCache, "scrollBar");
        Reflect.invokeMethod(scrollBar, "setVerticalTrackDrawable", new Class<?>[]{Drawable.class}, drawable);
    }

    public static void setVerticalThumbDrawable(View view, Drawable drawable) {
        Object mScrollCache = Reflect.getMember(View.class, view, "mScrollCache");
        Object scrollBar = Reflect.getMember(mScrollCache, "scrollBar");
        Reflect.invokeMethod(scrollBar, "setVerticalThumbDrawable", new Class<?>[]{Drawable.class}, drawable);
    }
}
