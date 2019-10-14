package com.angcyo.uiview.less.widget.behavior

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.annotation.CallSuper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.uiview.less.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/09/10
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
abstract class BaseDependsBehavior<T : View>(
    context: Context? = null,
    attrs: AttributeSet? = null
) :
    LogBehavior<T>(context, attrs) {

    /**依赖的视图, 用于触发[onDependentViewChanged]*/
    var dependsLayout: View? = null

    /**辅助确定依赖视图的索引值*/
    var dependsLayoutIndex = -1

    /**是否需要监听[dependsLayout]的改变*/
    var enableDependsOn = true

    /**依赖视图的 l t r b*/
    protected val dependsRect: Rect by lazy {
        Rect()
    }

    /**视图偏移后的差值 lf tf rf bf*/
    protected val dependsOffsetRect: Rect by lazy {
        Rect()
    }

    protected var dyConsumedAll = 0
    protected var dxConsumedAll = 0

    init {

        showLog = false

        context?.let {
            val array = it.obtainStyledAttributes(attrs, R.styleable.BaseDependsBehavior_Layout)

            //参照物的索引
            dependsLayoutIndex = array.getInt(
                R.styleable.BaseDependsBehavior_Layout_layout_depends_layout_index,
                dependsLayoutIndex
            )

            array.recycle()
        }
    }

    @CallSuper
    override fun layoutDependsOn(parent: CoordinatorLayout, child: T, dependency: View): Boolean {
        if (dependsLayout == null) {
            if (dependsLayoutIndex in 0 until parent.childCount) {
                dependsLayout = parent.getChildAt(dependsLayoutIndex)
            }
        }
        return enableDependsOn && dependsLayout == dependency
    }

    @CallSuper
    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: T,
        dependency: View
    ): Boolean {
        if (dependsRect.isEmpty) {
            updateDependsRect(dependency)
        }
        dependency.run {
            dependsOffsetRect.left = left - dependsRect.left
            dependsOffsetRect.top = top - dependsRect.top
            dependsOffsetRect.right = right - dependsRect.right
            dependsOffsetRect.bottom = bottom - dependsRect.bottom
        }
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type
        )
        dxConsumedAll += dxConsumed
        dyConsumedAll += dyConsumed

        w("this....dxConsumedAll:$dxConsumedAll dyConsumedAll:$dyConsumedAll")
    }

    //<editor-fold desc="辅助方法">

    protected fun View.clp(): CoordinatorLayout.LayoutParams =
        this.layoutParams as CoordinatorLayout.LayoutParams

    protected fun updateDependsRect(dependency: View) {
        dependsRect.set(dependency.left, dependency.top, dependency.right, dependency.bottom)
    }

    //</editor-fold desc="辅助方法">

}