package com.angcyo.uiview.less.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.angcyo.uiview.less.R
import com.angcyo.uiview.less.draw.RDrawNoRead
import com.angcyo.uiview.less.kotlin.density
import com.angcyo.uiview.less.kotlin.dpi
import com.angcyo.uiview.less.kotlin.getDrawCenterCx
import com.angcyo.uiview.less.kotlin.textWidth
import kotlin.math.max

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：图片和文本混排的View
 * 创建人员：Robi
 * 创建时间：2017/06/26 17:27
 * 修改人员：Robi
 * 修改时间：2017/06/26 17:27
 * 修改备注：
 * Version: 1.0.0
 */
class ImageTextView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatImageView(context, attributeSet) {

    /**需要绘制显示的文本*/
    var showText: String? = null
        set(value) {
            field = value
            requestLayout()
        }
    var showTextSize: Float = 14 * density
        set(value) {
            field = value
            textPaint.textSize = field
        }

    /**
     * 默认值请在init中设置
     * */
    var textOffset: Int = 0
        get() {
            if (showText.isNullOrEmpty()) {
                return 0
            }
            return field
        }
        set(value) {
            field = value
            requestLayout()
        }

    var textShowColor: Int = Color.WHITE
        set(value) {
            field = value
            postInvalidate()
        }

    var imageSize: Int = 0

    var drawNoRead: RDrawNoRead

    val textPaint: Paint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG)
    }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ImageTextView)
        showText = typedArray.getString(R.styleable.ImageTextView_r_show_text)
        showTextSize =
            typedArray.getDimensionPixelOffset(
                R.styleable.ImageTextView_r_show_text_size,
                showTextSize.toInt()
            )
                .toFloat()
        textOffset =
            typedArray.getDimensionPixelOffset(R.styleable.ImageTextView_r_text_offset, 6 * dpi)
        textShowColor =
            typedArray.getColor(R.styleable.ImageTextView_r_show_text_color, textShowColor)
        typedArray.recycle()

        drawNoRead = RDrawNoRead(this)
        drawNoRead.initAttribute(attributeSet)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val wMode = MeasureSpec.getMode(widthMeasureSpec)

        val minWidth = minimumWidth

        val textMeasureWidth = textWidth

        if (drawable == null || drawable.intrinsicWidth < 0) {
            imageSize = 0

            //无图片
            if (wMode != MeasureSpec.EXACTLY) {
                if (!TextUtils.isEmpty(showText)) {
                    val width =
                        (paddingLeft + paddingRight + textMeasureWidth).toInt()
                    setMeasuredDimension(
                        max(width, minWidth),
                        measuredHeight
                    )
                }
            }
        } else {
            //有图片
            imageSize = drawable.intrinsicWidth
            if (wMode != MeasureSpec.EXACTLY) {

                if (!TextUtils.isEmpty(showText)) {
                    val width =
                        (paddingLeft + paddingRight + imageSize + textOffset + textMeasureWidth).toInt()

                    setMeasuredDimension(
                        max(width, minWidth),
                        measuredHeight
                    )
                }
            }
        }

        //L.e("call: onMeasure -> $measuredWidth $wSize $showText")
    }

    override fun onDraw(canvas: Canvas) {
        if (!TextUtils.isEmpty(showText)) {

            textPaint.color = textShowColor

            val drawHeight = measuredHeight - paddingTop - paddingBottom
            val drawWidth = measuredWidth - paddingLeft - paddingRight

            val textMeasureWidth = textWidth
            if (imageSize > 0) {
                val centerWidth = imageSize + textOffset + textMeasureWidth
                val centerX = paddingLeft + centerWidth / 2
                val textDrawX = paddingLeft + imageSize + textOffset
                val srcOffset = imageSize / 2 + textOffset + (centerX - textDrawX)

                canvas.save()
                canvas.translate(-srcOffset, 0f)
                super.onDraw(canvas)
                canvas.restore()

                //绘制需要显示的文本文本
                canvas.drawText(
                    showText!!,
                    textDrawX.toFloat(),//getDrawCenterCx() - textMeasureWidth / 2 + textOffset,//paddingLeft + textOffset - 4 * density + drawWidth / 2 - imageSize / 2,
                    paddingTop + drawHeight / 2 + textHeight / 2 - textPaint.descent() / 2,
                    textPaint
                )
            } else {
                super.onDraw(canvas)

                //绘制需要显示的文本文本
                canvas.drawText(
                    showText!!,
                    getDrawCenterCx() - textMeasureWidth / 2,
                    paddingTop + drawHeight / 2 + textHeight / 2 - textPaint.descent() / 2,
                    textPaint
                )
            }
        } else {
            super.onDraw(canvas)
        }

        drawNoRead.onDraw(canvas)
    }

    val textHeight: Float
        get() {
            textPaint.textSize = showTextSize
            return textPaint.descent() - textPaint.ascent()
        }
    val textWidth: Float
        get() {
            if (showText.isNullOrEmpty()) {
                return 0f
            }
            textPaint.textSize = showTextSize
            return textPaint.measureText(showText)
        }
}
