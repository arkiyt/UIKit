package com.angcyo.uiview.less.kotlin.dialog

import android.app.Dialog
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.collection.ArrayMap
import com.angcyo.uiview.less.R
import com.angcyo.uiview.less.base.BaseUI
import com.angcyo.uiview.less.iview.AffectUI
import com.angcyo.uiview.less.kotlin.find
import com.angcyo.uiview.less.kotlin.getColor
import com.angcyo.uiview.less.kotlin.setHeight
import com.angcyo.uiview.less.recycler.RBaseViewHolder
import com.angcyo.uiview.less.recycler.adapter.RBaseAdapter
import com.angcyo.uiview.less.widget.group.RTabLayout
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/27
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class OptionDialogConfig : BaseDialogConfig() {
    init {
        dialogLayoutId = R.layout.dialog_option_layout

        positiveButtonListener = { dialog, _ ->
            if (onOptionResult.invoke(dialog, optionList)) {

            } else {
                dialog.dismiss()
            }
        }
    }

    internal lateinit var affectUI: AffectUI
    internal lateinit var adapter: RBaseAdapter<Any>

    /**简单选择项, 用来控制RV的高度为[wrap_content]*/
    var singleOption = false

    /**任意选择, 不强制要求选择到最后一级*/
    var anySelector = false

    override fun onDialogInit(dialog: Dialog, dialogViewHolder: RBaseViewHolder) {
        super.onDialogInit(dialog, dialogViewHolder)

        affectUI = BaseUI.uiFragment
            .createAffectUI(
                dialogViewHolder.group(R.id.content_wrap_layout),
                object : AffectUI.OnAffectListener {
                    override fun onAffectChangeBefore(
                        affectUI: AffectUI,
                        fromAffect: Int,
                        toAffect: Int
                    ) {
                    }

                    override fun onAffectChange(
                        affectUI: AffectUI,
                        fromAffect: Int,
                        toAffect: Int,
                        fromView: View?,
                        toView: View?
                    ) {
                        toView?.isClickable = true
                        if (toAffect == AffectUI.AFFECT_ERROR) {
                            toView?.find<View>(R.id.base_retry_button)?.setOnClickListener {
                                loadOptionList(dialogViewHolder, selectorLevel)
                            }
                        }
                    }

                    override fun onAffectInitLayout(
                        affectUI: AffectUI,
                        affect: Int,
                        rootView: View
                    ) {

                    }

                })
            .setContentAffect(AffectUI.CONTENT_AFFECT_NONE).create()

        //确定按钮状态
        dialogViewHolder.enable(R.id.positive_button, false)

        //tab
        dialogViewHolder.tab(R.id.tab_layout).apply {
            onTabLayoutListener =
                object : RTabLayout.DefaultColorListener(
                    getColor(R.color.base_text_color),
                    Color.BLACK,
                    true
                ) {
                    override fun onTabSelector(
                        tabLayout: RTabLayout,
                        fromIndex: Int,
                        toIndex: Int
                    ) {
                        super.onTabSelector(tabLayout, fromIndex, toIndex)
                        if (fromIndex != toIndex) {
                            loadOptionList(dialogViewHolder, toIndex)
                        }
                    }
                }
        }

        //RecyclerView
        adapter = object : RBaseAdapter<Any>() {
            override fun getItemLayoutId(viewType: Int): Int {
                return R.layout.item_adapter_option_layout
            }

            override fun onBindView(holder: RBaseViewHolder, position: Int, bean: Any?) {
                holder.tv(R.id.text_view).text = optionItemToString(bean!!)
                holder.visible(
                    R.id.image_view,
                    optionList.size > selectorLevel && isOptionEquItem(
                        optionList[selectorLevel],
                        bean
                    )
                )

                holder.clickItem {
                    if (selectorLevel == optionList.size) {
                        //当前选择界面数据, 是最后级别的

                        optionList.add(bean)
                    } else {
                        //清除之后的选项
                        for (i in optionList.size - 1 downTo selectorLevel) {
                            _cacheMap.remove(i)
                            optionList.removeAt(i)
                        }
                        optionList.add(bean)
                    }

                    if (onCheckOptionEnd(optionList, selectorLevel)) {
                        //最后一级
                        resetTabToLevel(dialogViewHolder, selectorLevel)
                        updateAllItem()
                    } else {
                        loadOptionList(dialogViewHolder, selectorLevel + 1)
                    }
                }
            }
        }
        dialogViewHolder.rv(R.id.recycler_view).adapter = adapter
        if (singleOption) {
            dialogViewHolder.group(R.id.content_wrap_layout).setHeight(-2)
        }

        val defaultLevel = optionList.size
        val requestLevel = if (onCheckOptionEnd(optionList, defaultLevel)) {
            //已经是最后一项
            val level = max(0, defaultLevel - 1)
            resetTabToLevel(dialogViewHolder, level)
            level
        } else {
            defaultLevel
        }
        loadOptionList(dialogViewHolder, requestLevel)
    }

    //level 对应的 缓存
    var _cacheMap = ArrayMap<Int, MutableList<out Any>>()

    /**加载选项数据, 并且重置Tab*/
    internal fun loadOptionList(dialogViewHolder: RBaseViewHolder, level: Int) {
        //获取数据后的回调处理
        val onItemListCallback: (MutableList<out Any>?) -> Unit = {
            if (it?.isEmpty() == true) {
                affectUI.showAffect(AffectUI.AFFECT_EMPTY)
            } else {
                affectUI.showAffect(AffectUI.AFFECT_CONTENT)
            }

            selectorLevel = level
            adapter.resetData(it)

            it?.let {
                //缓存
                _cacheMap[selectorLevel] = it

                //滚动到目标位置
                var scrollPosition = 0
                if (selectorLevel in 0 until it.size && selectorLevel in 0 until optionList.size) {
                    for (i in 0 until it.size) {
                        if (isOptionEquItem(optionList[selectorLevel], it[i])) {
                            scrollPosition = i
                            break
                        }
                    }
                }
                dialogViewHolder.rv(R.id.recycler_view).smoothScrollToPosition(scrollPosition)
            }

            //重置Tab Items (主要是添加 "请选择" tab)
            if (selectorLevel >= optionList.size) {
                resetTabToLevel(dialogViewHolder, selectorLevel)
            }
        }

        //错误回调的处理
        val onErrorCallback: (Throwable?) -> Unit = {
            selectorLevel = level
            adapter.resetData(emptyList())

            if (selectorLevel >= optionList.size) {
                resetTabToLevel(dialogViewHolder, selectorLevel)
            }
            if (needAsyncLoad) {
                affectUI.showAffect(AffectUI.AFFECT_ERROR)
            }
        }

        val cacheList = _cacheMap[level]
        if (cacheList?.isNotEmpty() == true) {
            //有缓存
            onItemListCallback(cacheList)
        } else {
            //无缓存
            if (needAsyncLoad) {
                affectUI.showAffect(AffectUI.AFFECT_LOADING)
            }
            onLoadOptionList(optionList, level, onItemListCallback, onErrorCallback)
        }
    }

    /**清除level之后的选择数据, */
    internal fun resetTabToLevel(dialogViewHolder: RBaseViewHolder, level: Int) {
        val tabItems = mutableListOf<Any>()
        tabItems.addAll(optionList)

        if (onCheckOptionEnd(optionList, level) && optionList.size > 0) {
            dialogViewHolder.enable(R.id.positive_button, true)
        } else {
            dialogViewHolder.enable(R.id.positive_button, false)
            tabItems.add("请选择")
        }

        if (anySelector) {
            dialogViewHolder.enable(R.id.positive_button, true)
        }

        dialogViewHolder.tab(R.id.tab_layout).apply {
            resetItems(R.layout.item_tab_option_layout, tabItems) { view, data, index ->

                view.find<TextView>(R.id.text_view)?.text = if (index in 0 until optionList.size) {
                    optionItemToString(data!!)
                } else {
                    "$data"
                }
            }

            resetItemStyle()

            post {
                setCurrentItem(level, false)
            }
        }
    }

    /**已选中的选项*/
    var optionList = mutableListOf<Any>()

    /**是否需要异步加载数据, true 会开启[affectUI]*/
    var needAsyncLoad = true

    /**当前查看的选项级别*/
    internal var selectorLevel = 0

    /**将选项[item], 转成可以显示在界面的 文本类型*/
    var optionItemToString: (item: Any) -> CharSequence = { item ->
        "$item"
    }

    /**
     * 根据级别, 加载级别对应的选项列表
     * @param affectUI 情感图切换
     * @param options 之前选中的选项
     * @param level 当前需要请求级别, 从0开始
     * */
    var onLoadOptionList: (
        options: MutableList<Any>, level: Int,
        itemsCallback: (MutableList<out Any>?) -> Unit,
        errorCallback: (Throwable?) -> Unit
    ) -> Unit =
        { options, level, itemsCallback, errorCallback ->

        }

    /**是否没有下一级可以选择了*/
    var onCheckOptionEnd: (options: MutableList<Any>, level: Int) -> Boolean = { options, _ ->
        false
    }

    /**
     * 选项返回回调
     * 返回 true, 则不会自动 调用 dismiss
     * */
    var onOptionResult: (dialog: Dialog, options: MutableList<Any>) -> Boolean = { _, _ ->
        false
    }

    /**Tab中的[Item]是否和列表中的[Item]相同*/
    var isOptionEquItem: (option: Any, item: Any) -> Boolean =
        { option, item -> optionItemToString(option) == optionItemToString(item) }
}