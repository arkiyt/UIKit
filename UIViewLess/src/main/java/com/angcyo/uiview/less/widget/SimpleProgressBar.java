package com.angcyo.uiview.less.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.kotlin.ViewExKt;
import com.angcyo.uiview.less.skin.SkinHelper;
import com.angcyo.uiview.less.utils.ThreadExecutor;


/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2016/11/28 17:57
 * 修改人员：Robi
 * 修改时间：2016/11/28 17:57
 * 修改备注：
 * Version: 1.0.0
 */
public class SimpleProgressBar extends View {
    public static final int STYLE_RECT = 1;//矩形进度条
    public static final int STYLE_CIRCLE = 2;//圆形进度条

    /**
     * 进度条样式
     */
    int mProgressStyle = STYLE_RECT;

    int mProgress = 0;//当前的进度

    int mProgressColor = -1, mProgressBgColor, mProgressWidth, mProgressTextColor;

    float mProgressTextSize;

    Paint mPaint;
    RectF mRect;

    boolean autoHide = true;

    /**
     * 不确定的进度
     */
    boolean incertitudeProgress = false;
    int drawColor;
    private ValueAnimator mColorAnimator;
    private ValueAnimator animator;

    /**
     * 使用渐变进度, 只在 incertitudeProgress = false && mProgressStyle = STYLE_RECT 时, 有效
     */
    private boolean isGradientProgress = false;
    private int gradientProgressStartColor = -1;

    public SimpleProgressBar(Context context) {
        this(context, null);
    }

    public SimpleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();

        mProgressWidth = (int) (getDensity() * 3);
        mProgressBgColor = Color.parseColor("#40000000");
        mProgressTextSize = 12 * getResources().getDisplayMetrics().density;

        mProgressTextColor = Color.WHITE;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleProgressBar);
        mProgressWidth = typedArray.getDimensionPixelOffset(R.styleable.SimpleProgressBar_r_progress_width, mProgressWidth);
        mProgressTextSize = typedArray.getDimensionPixelOffset(R.styleable.SimpleProgressBar_r_progress_text_size, (int) mProgressTextSize);
        mProgressBgColor = typedArray.getColor(R.styleable.SimpleProgressBar_r_progress_bg_color, mProgressBgColor);
        mProgressColor = typedArray.getColor(R.styleable.SimpleProgressBar_r_progress_color, mProgressColor);
        gradientProgressStartColor = typedArray.getColor(R.styleable.SimpleProgressBar_r_progress_gradient_start_color, gradientProgressStartColor);
        mProgressTextColor = typedArray.getColor(R.styleable.SimpleProgressBar_r_progress_text_color, mProgressTextColor);
        setProgressStyle(typedArray.getInt(R.styleable.SimpleProgressBar_r_progress_style, STYLE_RECT));
        incertitudeProgress = typedArray.getBoolean(R.styleable.SimpleProgressBar_r_incertitude_progress, incertitudeProgress);
        isGradientProgress = typedArray.getBoolean(R.styleable.SimpleProgressBar_r_progress_gradient, isGradientProgress);
        autoHide = typedArray.getBoolean(R.styleable.SimpleProgressBar_r_progress_auto_hide, autoHide);

        typedArray.recycle();

    }

    public float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    private void init() {
        if (mProgressColor == -1) {
            if (isInEditMode()) {
                mProgressColor = ContextCompat.getColor(getContext(), R.color.colorPrimary);
                mProgress = 100;
            } else {
                mProgressColor = SkinHelper.getSkin().getThemeSubColor();
            }
        }
        if (gradientProgressStartColor == -1) {
            if (isInEditMode()) {
                gradientProgressStartColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
                mProgress = 100;
            } else {
                gradientProgressStartColor = SkinHelper.getSkin().getThemeColor();
            }
        }
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mProgressColor);
        mRect = new RectF();
    }

    public void setProgress(int progress) {
        if (incertitudeProgress) {
            return;
        }

        endIncertitudeAnimator();

        progress = Math.max(0, Math.min(100, progress));
        if (autoHide && (progress >= 100 || progress <= 0)) {
//            setVisibility(GONE);
            ViewCompat.animate(this)
                    .translationY(-getMeasuredHeight())
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mProgress = 0;
                        }
                    })
                    .start();
        } else {
            if (getVisibility() != View.VISIBLE) {
                setVisibility(View.VISIBLE);
            }

            if (getTranslationY() == -getMeasuredHeight()) {
                ViewCompat.animate(this).translationY(0).setDuration(300).start();
            }
        }
        //mProgress = progress;
        final int finalProgress = progress;
        ThreadExecutor.instance().onMain(new Runnable() {
            @Override
            public void run() {
                animToProgress(mProgress, finalProgress);
            }
        });
        postInvalidate();
    }

    public void setIncertitudeProgress(boolean incertitudeProgress) {
        boolean old = this.incertitudeProgress;

        if (old == incertitudeProgress) {
            return;
        }

        this.incertitudeProgress = incertitudeProgress;
        drawColor = mProgressColor;
        if (incertitudeProgress) {
            startIncertitudeAnimator();
        } else {
            endIncertitudeAnimator();
        }
    }

    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
        mPaint.setColor(mProgressColor);
    }

    public void setAutoHide(boolean autoHide) {
        this.autoHide = autoHide;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            if (mProgressStyle == STYLE_RECT) {
                height = (int) (getDensity() * 4);
            } else {
                height = (int) (getDensity() * 30);
            }
        }

        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            if (mProgressStyle == STYLE_RECT) {
                width = (int) (getDensity() * 100);
            } else {
                width = (int) (getDensity() * 30);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRect.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
        if (mProgressStyle == STYLE_CIRCLE) {
            mRect.inset(mProgressWidth, mProgressWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        if (mProgressStyle == STYLE_RECT) {
            mPaint.setStyle(Paint.Style.FILL);
            if (incertitudeProgress) {
                mPaint.setColor(drawColor);
                canvas.drawRect(0, 0, mRect.width(), mRect.height(), mPaint);
            } else {
                mPaint.setColor(mProgressColor);
                float right = mRect.width() * (mProgress / 100f);
                if (isGradientProgress) {
                    mPaint.setShader(new LinearGradient(0f, 0f, right, 0,
                            new int[]{gradientProgressStartColor, mProgressColor}, null, Shader.TileMode.CLAMP));
                }
                canvas.drawRect(0, 0, right, mRect.height(), mPaint);
            }
        } else {
            mPaint.setStyle(Paint.Style.STROKE);

            mPaint.setColor(mProgressBgColor);
            mPaint.setStrokeWidth(mProgressWidth);
            canvas.drawArc(mRect, 0f, 360f, false, mPaint);

            if (incertitudeProgress) {
                mPaint.setColor(drawColor);

                canvas.drawArc(mRect, 0f, 360f, false, mPaint);
            } else {
                mPaint.setColor(mProgressColor);
                mPaint.setStrokeWidth(mProgressWidth + .2f);
                canvas.drawArc(mRect, -90f, 360 * mProgress / 100f, false, mPaint);
            }

            if (!incertitudeProgress) {
                //绘制进度文本
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mPaint.setTextSize(mProgressTextSize);
                mPaint.setColor(mProgressTextColor);
                mPaint.setStrokeWidth(1);
                String text = mProgress + "%";
                canvas.drawText(text,
                        ViewExKt.getDrawCenterTextCx(this, mPaint, text),
                        ViewExKt.getDrawCenterTextCy(this, mPaint),
                        mPaint);
            }
        }
        canvas.restore();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        endIncertitudeAnimator();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (incertitudeProgress) {
            startIncertitudeAnimator();
        }
    }

    private void endIncertitudeAnimator() {
        if (mColorAnimator != null) {
            mColorAnimator.cancel();
            mColorAnimator = null;
        }
    }

    private void startIncertitudeAnimator() {
        if (mColorAnimator == null) {
            mColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mProgressColor, SkinHelper.getTranColor(mProgressColor, 0x10));
            mColorAnimator.setInterpolator(new LinearInterpolator());
            mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    drawColor = (int) animation.getAnimatedValue();//之后就可以得到动画的颜色了.
                    postInvalidate();
                }
            });
            mColorAnimator.setDuration(1000);
            mColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        }
        mColorAnimator.start();
    }

    public void setProgressStyle(int progressStyle) {
        mProgressStyle = progressStyle;
        if (mProgressStyle == STYLE_CIRCLE) {
            autoHide = false;
        } else {
            autoHide = true;
        }
    }

    public void setProgressBgColor(int progressBgColor) {
        mProgressBgColor = progressBgColor;
    }

    public void setProgressWidth(int progressWidth) {
        mProgressWidth = progressWidth;
    }

    public void setProgressTextColor(int progressTextColor) {
        mProgressTextColor = progressTextColor;
    }

    public void setProgressTextSize(float progressTextSize) {
        mProgressTextSize = progressTextSize;
    }

    /**
     * 动画的方式执行动画
     */
    private void animToProgress(int startP, int endP) {
        if (animator == null) {

        } else {
            animator.cancel();
        }

        animator = ValueAnimator.ofInt(startP, endP);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mProgress = (int) valueAnimator.getAnimatedValue();
                postInvalidateOnAnimation();
            }
        });
        animator.start();
    }
}
