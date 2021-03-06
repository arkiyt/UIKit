package com.angcyo.uiview.less.recycler.dslitem

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import com.angcyo.uiview.less.R
import com.angcyo.uiview.less.kotlin.color
import com.angcyo.uiview.less.kotlin.getDrawable
import com.angcyo.uiview.less.kotlin.inflate
import com.angcyo.uiview.less.kotlin.setLeftIco
import com.angcyo.uiview.less.recycler.RBaseViewHolder
import com.angcyo.uiview.less.recycler.adapter.DslAdapterItem
import com.angcyo.uiview.less.widget.group.RLinearLayout


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslBaseInfoItem : DslAdapterItem() {
    init {
        itemLayoutId = R.layout.dsl_info_item
    }

    /**背景*/
    var itemBackgroundDrawable: Drawable? = ColorDrawable(Color.WHITE)

    /**条目文本*/
    var itemInfoText: CharSequence? = null

    @DrawableRes
    var itemInfoIcon: Int = -1
    var itemInfoIconColor: Int = -2

    /**扩展布局信息*/
    @LayoutRes
    var itemExtendLayoutId: Int = -1

    override fun onItemBind(
        itemHolder: RBaseViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        itemHolder.clear()

        (itemHolder.itemView as? RLinearLayout)?.setRBackgroundDrawable(itemBackgroundDrawable)

        //文本信息
        itemHolder.tv(R.id.text_view)?.apply {
            text = itemInfoText

            if (itemInfoIconColor == -2) {
                setLeftIco(itemInfoIcon)
            } else {
                setLeftIco(getDrawable(itemInfoIcon).color(itemInfoIconColor))
            }
        }

        //扩展布局
        if (itemExtendLayoutId > 0) {
            itemHolder.group(R.id.wrap_layout)?.apply {
                var inflate = true
                if (childCount > 0) {
                    val tag = getChildAt(0).getTag(R.id.tag)
                    if (tag == itemExtendLayoutId) {
                        inflate = false
                    } else {
                        removeAllViews()
                    }
                }

                if (inflate) {
                    inflate(itemExtendLayoutId, true)
                    val view = getChildAt(0)
                    view.setTag(R.id.tag, itemExtendLayoutId)
                }
            }
        } else {
            itemHolder.group(R.id.wrap_layout)?.removeAllViews()
        }
    }
}