package com.angcyo.uiview.less.skin;

import android.content.Context;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import com.angcyo.uiview.less.R;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/04/01 15:37
 * 修改人员：Robi
 * 修改时间：2017/04/01 15:37
 * 修改备注：
 * Version: 1.0.0
 */
public class BaseSkin extends SkinImpl {

    protected Context mContext;

    public BaseSkin(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }

    final public int getColor(@ColorRes int id) {
        return ContextCompat.getColor(mContext, id);
    }

    @Override
    public int getThemeColor() {
        return ContextCompat.getColor(mContext, R.color.theme_color_primary);
    }

    @Override
    public int getThemeSubColor() {
        return ContextCompat.getColor(mContext, R.color.theme_color_primary);
    }

    @Override
    public int getThemeDarkColor() {
        return ContextCompat.getColor(mContext, R.color.theme_color_primary_dark);
    }

    @Override
    public int getThemeDisableColor() {
        return ContextCompat.getColor(mContext, R.color.theme_color_disable);
    }

    @Override
    public int getThemeColorAccent() {
        return ContextCompat.getColor(mContext, R.color.theme_color_accent);
    }

    @Override
    public int getThemeColorPrimaryDark() {
        return ContextCompat.getColor(mContext, R.color.theme_color_primary_dark);
    }

    @Override
    public int getThemeColorPrimary() {
        return ContextCompat.getColor(mContext, R.color.theme_color_primary);
    }
}
