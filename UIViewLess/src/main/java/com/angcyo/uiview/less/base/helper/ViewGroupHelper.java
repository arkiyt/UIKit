package com.angcyo.uiview.less.base.helper;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.*;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.angcyo.uiview.less.kotlin.ViewExKt;
import com.angcyo.uiview.less.resources.ResUtil;
import com.angcyo.uiview.less.widget.ImageTextView;

import java.util.List;

public class ViewGroupHelper {
    View parentView;
    View selectorView;

    public ViewGroupHelper(View parentView) {
        this.parentView = parentView;
    }

    public static ViewGroupHelper build(View parentView) {
        return new ViewGroupHelper(parentView);
    }

    public static <T> void resetChild(@NonNull ViewGroup viewGroup, int newSize, @Nullable List<T> datas, @NonNull OnAddViewCallback<T> callback) {
        resetChildCount(viewGroup, newSize, callback);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            T data = null;
            if (datas != null && datas.size() > i) {
                data = datas.get(i);
            }
            callback.onInitView(viewGroup.getChildAt(i), data, i);
        }
    }

    public static void resetChildCount(ViewGroup viewGroup, int newSize, OnAddViewCallback callback) {
        int oldSize = viewGroup.getChildCount();
        int count = newSize - oldSize;
        if (count > 0) {
            //需要补充子View
            for (int i = 0; i < count; i++) {
                viewGroup.addView(callback.createView(viewGroup.getContext(), viewGroup));
            }
        } else if (count < 0) {
            //需要移除子View
            for (int i = Math.abs(count); i < count; i++) {
                viewGroup.removeViewAt(oldSize - 1 - i);
            }
        }
    }

    public ViewGroupHelper addView(@NonNull View itemView) {
        return addView(-1, itemView);
    }

    public ViewGroupHelper addView(int index, @NonNull View itemView) {
        if (parentView instanceof ViewGroup) {
            ((ViewGroup) parentView).addView(itemView, index);
        }
        return this;
    }

    public ViewGroupHelper remove(int index) {
        if (parentView instanceof ViewGroup) {
            if (((ViewGroup) parentView).getChildCount() > index) {
                ((ViewGroup) parentView).removeViewAt(index);
            }
        }
        return this;
    }

    public ViewGroupHelper remove() {
        if (parentView instanceof ViewGroup && selectorView != null) {
            ((ViewGroup) parentView).removeView(selectorView);
        }
        return this;
    }

    public ViewGroupHelper visible(int visibility) {
        if (parentView != null && selectorView != null) {
            if (selectorView.getVisibility() != visibility) {
                selectorView.setVisibility(visibility);
            }
        }
        return this;
    }

    /**
     * 显示id指定的子view
     */
    public ViewGroupHelper visibleId(int... ids) {
        if (parentView instanceof ViewGroup && ids != null) {
            for (int i = 0; i < ids.length; i++) {
                View viewById = parentView.findViewById(ids[i]);
                if (viewById != null) {
                    viewById.setVisibility(View.VISIBLE);
                }
            }
        }
        return this;
    }

    public ViewGroupHelper gone() {
        return visible(View.GONE);
    }

    public ViewGroupHelper visible() {
        return visible(View.VISIBLE);
    }

    public ViewGroupHelper invisible() {
        return visible(View.INVISIBLE);
    }

    public ViewGroupHelper selector() {
        selectorView = parentView;
        return this;
    }

    public ViewGroupHelper selector(@IdRes int id) {
        if (parentView != null) {
            selectorView = parentView.findViewById(id);
        }
        return this;
    }

    public ViewGroupHelper selector(@NonNull View view) {
        selectorView = view;
        return this;
    }

    public ViewGroupHelper selectorByIndex(int index) {
        selectorView = getView(index);
        return this;
    }

    public ViewGroupHelper setText(String text) {
        if (selectorView != null) {
            if (selectorView instanceof TextView) {
                ((TextView) selectorView).setText(text);
            } else if (selectorView instanceof ImageTextView) {
                ((ImageTextView) selectorView).setShowText(text);
            }
        }
        return this;
    }

    //<editor-fold desc="Drawable过滤颜色方法">

    public ViewGroupHelper setTextSize(float textSize) {
        if (selectorView != null) {
            if (selectorView instanceof TextView) {
                ((TextView) selectorView).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            } else if (selectorView instanceof ImageTextView) {
                ((ImageTextView) selectorView).setShowTextSize(textSize);
            }
        }
        return this;
    }

    public ViewGroupHelper setTextColor(@ColorInt int color) {
        if (selectorView != null) {
            if (selectorView instanceof TextView) {
                ((TextView) selectorView).setTextColor(color);
            } else if (selectorView instanceof ImageTextView) {
                ((ImageTextView) selectorView).setTextShowColor(color);
            }
        }
        return this;
    }

    public ViewGroupHelper colorFilter(@ColorInt int color) {
        if (selectorView != null) {
            colorFilterView(selectorView, color);
        }
        return this;
    }
    //</editor-fold>

    //<editor-fold desc="文本过滤颜色的方法">

    public ViewGroupHelper colorFilter(@Nullable ViewGroup view, @ColorInt int color) {
        if (view != null) {
            for (int i = 0; i < view.getChildCount(); i++) {
                View childAt = view.getChildAt(i);
                if (childAt instanceof ViewGroup) {
                    colorFilter((ViewGroup) childAt, color);
                } else {
                    colorFilterView(childAt, color);
                }
            }
        }
        return this;
    }

    public ViewGroupHelper colorFilterView(@Nullable View view, @ColorInt int color) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                colorFilter((ViewGroup) view, color);
            } else if (view instanceof TextView) {
                // ((TextView) view).setTextColor(color);
            } else if (view instanceof ImageView) {
                ((ImageView) view).setImageDrawable(ResUtil.filterDrawable(((ImageView) view).getDrawable(), color));
            }
        }
        return this;
    }

    public ViewGroupHelper textColorFilter(@ColorInt int color) {
        if (selectorView != null) {
            textColorFilterView(selectorView, color);
        }
        return this;
    }

    //</editor-fold>

    public ViewGroupHelper textColorFilter(@Nullable ViewGroup view, @ColorInt int color) {
        if (view != null) {
            for (int i = 0; i < view.getChildCount(); i++) {
                View childAt = view.getChildAt(i);
                if (childAt instanceof ViewGroup) {
                    textColorFilter((ViewGroup) childAt, color);
                } else {
                    textColorFilterView(childAt, color);
                }
            }
        }
        return this;
    }

    public ViewGroupHelper textColorFilterView(@Nullable View view, @ColorInt int color) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                textColorFilter((ViewGroup) view, color);
            } else if (view instanceof TextView) {
                ((TextView) view).setTextColor(color);
            } else if (view instanceof ImageTextView) {
                ((ImageTextView) view).setTextShowColor(color);
            }
        }
        return this;
    }

    public ViewGroupHelper setBackgroundColor(@ColorInt int color) {
        if (selectorView != null) {
            selectorView.setBackgroundColor(color);
        }
        return this;
    }

    public ViewGroupHelper setBackground(@Nullable Drawable background) {
        if (selectorView != null) {
            ViewCompat.setBackground(selectorView, background);
        }
        return this;
    }

    public ViewGroupHelper setImageResource(@DrawableRes int resId) {
        if (selectorView != null) {
            if (selectorView instanceof ImageTextView) {
                ((ImageTextView) selectorView).setImageResource(resId);
            } else if (selectorView instanceof ImageView) {
                ((ImageView) selectorView).setImageResource(resId);
            }
        }
        return this;
    }

    public ViewGroupHelper setImageDrawable(Drawable drawable) {
        setImageDrawable(drawable, -1);
        return this;
    }

    public ViewGroupHelper setImageDrawable(Drawable drawable, int filterColor) {

        if (filterColor != -1) {
            if (drawable != null) {
                drawable = ResUtil.filterDrawable(drawable, filterColor);
            }
        }

        if (selectorView != null) {
            if (selectorView instanceof ImageTextView) {
                ((ImageTextView) selectorView).setImageDrawable(drawable);
            } else if (selectorView instanceof ImageView) {
                ((ImageView) selectorView).setImageDrawable(drawable);
            }
        }
        return this;
    }

    public ViewGroupHelper setVisibility(int visibility) {
        if (selectorView != null) {
            selectorView.setVisibility(visibility);
        }
        return this;
    }

    public <T> T cast() {
        if (selectorView == null) {
            return null;
        }
        return (T) selectorView;
    }

    public ViewGroupHelper replace(int index, @NonNull View newView) {
        if (parentView instanceof ViewGroup) {
            if (((ViewGroup) parentView).getChildCount() > index) {
                ((ViewGroup) parentView).removeViewAt(index);
                ((ViewGroup) parentView).addView(newView, index);
            }
        }
        return this;
    }

    @Nullable
    public View getView(int index) {
        if (parentView instanceof ViewGroup) {
            if (((ViewGroup) parentView).getChildCount() > index) {
                return ((ViewGroup) parentView).getChildAt(index);
            }
        }
        return null;
    }

    public ViewGroupHelper setTextBold(boolean bold) {
        if (selectorView instanceof TextView) {
            ViewExKt.addPaintFlags((TextView) selectorView, Paint.FAKE_BOLD_TEXT_FLAG, bold, true);
        } else if (selectorView instanceof ImageTextView) {
            ViewExKt.setPaintFlags(((ImageTextView) selectorView).getTextPaint(), Paint.FAKE_BOLD_TEXT_FLAG, bold);
        }
        return this;
    }

    /**
     * 设置所以子View, 的可见性
     *
     * @param filterIds 需要排除的子view, id
     */
    public ViewGroupHelper visibilityAllId(int visibility, int... filterIds) {
        if (parentView instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) parentView).getChildCount(); i++) {
                View child = ((ViewGroup) parentView).getChildAt(i);
                boolean filter = false;
                if (filterIds != null) {
                    int id = child.getId();
                    for (int j = 0; j < filterIds.length; j++) {
                        if (filterIds[j] == id) {
                            filter = true;
                            break;
                        }
                    }
                }
                if (!filter) {
                    child.setVisibility(visibility);
                }
            }
        }
        return this;
    }

    public ViewGroupHelper visibilityAllView(int visibility, View... filterViews) {
        if (parentView instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) parentView).getChildCount(); i++) {
                View child = ((ViewGroup) parentView).getChildAt(i);
                boolean filter = false;
                if (filterViews != null) {
                    for (int j = 0; j < filterViews.length; j++) {
                        if (filterViews[j] == child) {
                            filter = true;
                            break;
                        }
                    }
                }
                if (!filter) {
                    child.setVisibility(visibility);
                }
            }
        }
        return this;
    }


    public static class OnAddViewCallback<T> {
        public int getLayoutId() {
            return -1;
        }

        public View createView(@NonNull Context context, @NonNull ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(getLayoutId(), viewGroup, false);
        }

        public void onInitView(@NonNull View itemView, @Nullable T data, int index) {

        }
    }
}