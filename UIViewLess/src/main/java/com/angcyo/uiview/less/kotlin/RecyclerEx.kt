package com.angcyo.uiview.less.kotlin

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.angcyo.uiview.less.R
import com.angcyo.uiview.less.kotlin.dsl.DslRecyclerScroll
import com.angcyo.uiview.less.recycler.DslItemDecoration
import com.angcyo.uiview.less.recycler.RBaseViewHolder
import com.angcyo.uiview.less.recycler.RRecyclerView
import com.angcyo.uiview.less.recycler.adapter.DslAdapter
import com.angcyo.uiview.less.recycler.adapter.DslAdapterItem

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/07
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

public fun RecyclerView?.grid(spanCount: Int, orientation: Int = GridLayoutManager.VERTICAL) {
    this?.apply {
        var create = false

        if (layoutManager !is GridLayoutManager) {
            create = true
        } else {
            if ((layoutManager as GridLayoutManager).spanCount != spanCount) {
                create = true
            } else if ((layoutManager as GridLayoutManager).orientation != orientation) {
                create = true
            }
        }

        if (create) {
            layoutManager =
                RRecyclerView.GridLayoutManagerWrap(context, spanCount, orientation, false)
        }

        if (layoutManager is GridLayoutManager) {
            if ((layoutManager as GridLayoutManager).spanSizeLookup !is RRecyclerView.RSpanSizeLookup) {
                (layoutManager as GridLayoutManager).spanSizeLookup =
                    RRecyclerView.RSpanSizeLookup(this)
            }
        }
    }
}

public fun RecyclerView.dslAdapter(
    append: Boolean = false, //当已经是adapter时, 是否追加item. 需要先关闭 new
    new: Boolean = true, //始终创建新的adapter, 为true时, 则append无效
    init: DslAdapter.() -> Unit
): DslAdapter {

    var dslAdapter: DslAdapter? = null

    fun newAdapter() {
        dslAdapter = DslAdapter()
        dslAdapter!!.init()

        adapter = dslAdapter
    }

    if (new) {
        newAdapter()
    } else {
        if (adapter is DslAdapter) {
            dslAdapter = adapter as DslAdapter

            if (!append) {
                dslAdapter!!.resetData(mutableListOf())
            }

            dslAdapter!!.init()
        } else {
            newAdapter()
        }
    }

    return dslAdapter!!
}

/**
 * 支持网格布局
 * */
public fun RecyclerView.dslAdapter(
    spanCount: Int = 1,
    append: Boolean = false,
    new: Boolean = true,
    init: DslAdapter.() -> Unit
): DslAdapter {

    val dslAdapter = dslAdapter(append, new, init)

    grid(spanCount)

    return dslAdapter
}

public fun <Item : DslAdapterItem> DslAdapter.renderCustomItem(item: Item, init: Item.() -> Unit) {
    item.init()
    addLastItem(item)
}

public fun <Item : DslAdapterItem, Data> DslAdapter.renderCustomItem(
    item: Item,
    data: Data? = null,
    init: Item.() -> Unit
) {
    item.itemData = data
    item.init()
    addLastItem(item)
}

public fun DslAdapter.renderItem(count: Int = 1, init: DslAdapterItem.(index: Int) -> Unit) {
    for (i in 0 until count) {
        val adapterItem = DslAdapterItem()
        adapterItem.init(i)
        addLastItem(adapterItem)
    }
}

public fun <T> DslAdapter.renderItem(data: T, init: DslAdapterItem.() -> Unit) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemData = data
    adapterItem.init()
    addLastItem(adapterItem)
}

/**空的占位item*/
public fun DslAdapter.renderEmptyItem(height: Int = 120 * dpi, color: Int = Color.TRANSPARENT) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemLayoutId = R.layout.base_empty_item
    adapterItem.itemBind = { itemHolder, _, _ ->
        itemHolder.itemView.setBackgroundColor(color)
        itemHolder.itemView.setHeight(height)
    }
    addLastItem(adapterItem)
}

public fun <T> DslAdapter.renderItem(
    list: List<T>,
    init: DslAdapterItem.(index: Int, data: T) -> Unit
) {
    list.forEachIndexed { index, any ->
        val adapterItem = DslAdapterItem()
        adapterItem.itemData = any
        adapterItem.init(index, any)
        addLastItem(adapterItem)
    }
}

public fun RecyclerView.onScroll(init: DslRecyclerScroll.() -> Unit) {
    val dslRecyclerView = DslRecyclerScroll()
    dslRecyclerView.init()
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            super.onScrolled(recyclerView, dx, dy)

            dslRecyclerView.firstItemAdapterPosition = firstItemAdapterPosition()
            dslRecyclerView.firstItemCompletelyVisibleAdapterPosition =
                firstItemCompletelyAdapterPosition()

            dslRecyclerView.onRecyclerScrolled.invoke(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(
            recyclerView: RecyclerView,
            newState: Int
        ) {
            super.onScrollStateChanged(recyclerView, newState)
            dslRecyclerView.onRecyclerScrollStateChanged.invoke(recyclerView, newState)
        }
    })
}

public fun RecyclerView.clearItemDecoration(filter: (RecyclerView.ItemDecoration) -> Boolean = { false }) {
    for (i in itemDecorationCount - 1 downTo 0) {
        if (filter.invoke(getItemDecorationAt(i))) {
        } else {
            removeItemDecorationAt(i)
        }
    }
}

public fun RecyclerView.dslItemDecoration(init: DslItemDecoration.() -> Unit) {
    addItemDecoration(DslItemDecoration().apply {
        init()
    })
}

/**
 * 取消RecyclerView的默认动画
 * */
public fun RecyclerView.noItemAnim() {
    itemAnimator = null
}

/**
 * 取消默认的change动画
 * */
public fun RecyclerView.noItemChangeAnim() {
    if (itemAnimator == null) {
        itemAnimator = DefaultItemAnimator().apply {
            supportsChangeAnimations = false
        }
    } else if (itemAnimator is SimpleItemAnimator) {
        (itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
    }
}

public fun RecyclerView.eachChildViewHolder(
    targetView: View? = null,/*指定目标, 则只回调目标前后的ViewHolder*/
    callback: (
        beforeViewHolder: RecyclerView.ViewHolder?,
        viewHolder: RecyclerView.ViewHolder,
        afterViewHolder: RecyclerView.ViewHolder?
    ) -> Unit
) {

    val childCount = childCount
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        val childViewHolder = findContainingViewHolder(child)

        childViewHolder?.let {

            //前一个child
            var beforeViewHolder: RecyclerView.ViewHolder? = null
            //后一个child
            var afterViewHolder: RecyclerView.ViewHolder? = null

            if (i >= 1) {
                beforeViewHolder = findContainingViewHolder(getChildAt(i - 1))
            }
            if (i < childCount - 1) {
                afterViewHolder = findContainingViewHolder(getChildAt(i + 1))
            }

            if (targetView != null) {
                if (targetView == child) {
                    callback.invoke(beforeViewHolder, it as RBaseViewHolder, afterViewHolder)
                    return
                }
            } else {
                callback.invoke(beforeViewHolder, it as RBaseViewHolder, afterViewHolder)
            }
        }
    }
}

public fun RecyclerView.eachChildRViewHolder(
    targetView: View? = null,/*指定目标, 则只回调目标前后的ViewHolder*/
    callback: (
        beforeViewHolder: RBaseViewHolder?,
        viewHolder: RBaseViewHolder,
        afterViewHolder: RBaseViewHolder?
    ) -> Unit
) {

    val childCount = childCount
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        val childViewHolder = findContainingViewHolder(child)

        childViewHolder?.let {

            //前一个child
            var beforeViewHolder: RBaseViewHolder? = null
            //后一个child
            var afterViewHolder: RBaseViewHolder? = null

            if (i >= 1) {
                beforeViewHolder = findContainingViewHolder(getChildAt(i - 1)) as RBaseViewHolder?
            }
            if (i < childCount - 1) {
                afterViewHolder = findContainingViewHolder(getChildAt(i + 1)) as RBaseViewHolder?
            }

            if (targetView != null) {
                if (targetView == child) {
                    callback.invoke(beforeViewHolder, it as RBaseViewHolder, afterViewHolder)
                    return
                }
            } else {
                callback.invoke(beforeViewHolder, it as RBaseViewHolder, afterViewHolder)
            }
        }
    }
}

/**
 * 第一个item所在的Adapter position
 * */
public fun RecyclerView.firstItemAdapterPosition(): Int {
    if (childCount > 0) {
        return getChildAdapterPosition(getChildAt(0))
    }
    return RecyclerView.NO_POSITION
}

/**
 * 第一个item完全可见所在的Adapter position
 * */
public fun RecyclerView.firstItemCompletelyAdapterPosition(): Int {
    val childCount = childCount
    var result = RecyclerView.NO_POSITION
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child.top >= 0 && child.left >= 0) {
            result = getChildAdapterPosition(child)
            break
        }
    }
    return result
}

/**
 * 枚举adapter 中的 item
 * @param callback 返回true, 终端for 循环
 * */
public fun RecyclerView.eachDslAdapterItem(callback: (index: Int, dslItem: DslAdapterItem?) -> Boolean) {
    adapter?.apply {
        if (this is DslAdapter) {
            for (i in 0 until itemCount) {
                if (callback(i, getItemData(i))) {
                    break
                }
            }
        }
    }
}

public fun RecyclerView.findViewHolder(position: Int): RBaseViewHolder? {
    return findViewHolderForAdapterPosition(position) as? RBaseViewHolder
}

/**
 * 通过Tag, 刷新指定Item
 */
public fun DslAdapter?.notifyItemChangedByTag(tag: String?) {
    this?.apply {
        allDatas.forEachIndexed { index, dslAdapterItem ->
            if (TextUtils.equals(dslAdapterItem.itemTag, tag)) {
                notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }
}

/**
 * 回收需回收的Item。
 */
public fun RecyclerView.LayoutManager.recycleScrapList(recycle: RecyclerView.Recycler) {
    for (i in recycle.scrapList.size - 1 downTo 0) {
        removeAndRecycleView(recycle.scrapList[i].itemView, recycle)
    }
}