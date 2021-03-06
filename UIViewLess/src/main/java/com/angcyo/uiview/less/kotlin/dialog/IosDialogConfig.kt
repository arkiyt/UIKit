package com.angcyo.uiview.less.kotlin.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.angcyo.uiview.less.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class IosDialogConfig : BaseDialogConfig() {

    init {
        dialogLayoutId = R.layout.dialog_normal_ios_layout
        dialogBgDrawable = ColorDrawable(Color.TRANSPARENT)
    }
}