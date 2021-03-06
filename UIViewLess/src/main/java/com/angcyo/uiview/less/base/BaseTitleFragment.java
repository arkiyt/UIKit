package com.angcyo.uiview.less.base;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.angcyo.uiview.less.ContainerActivity;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.base.helper.ViewGroupHelper;
import com.angcyo.uiview.less.iview.AffectUI;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.resources.ResUtil;
import com.angcyo.uiview.less.resources.ViewResConfig;
import com.angcyo.uiview.less.widget.group.FragmentContentWrapperLayout;
import com.angcyo.uiview.less.widget.group.RSoftInputLayout;
import com.angcyo.uiview.less.widget.group.TitleBarLayout;

/**
 * Email:angcyo@126.com
 * 统一标题管理的Fragment
 *
 * @author angcyo
 * @date 2018/12/07
 */
public abstract class BaseTitleFragment extends BaseFragment implements AffectUI.OnAffectListener {

    public ViewResConfig viewResConfig = new ViewResConfig();
    /**
     * 内容布局是否需要被软键盘布局包裹
     */
    public boolean contentNeedSoftInputLayout = false;
    /**
     * Fragment 根布局.
     */
    protected FragmentContentWrapperLayout fragmentContentWrapperLayout;
    /**
     * Fragment 内容布局, 将add到这个ViewGroup
     */
    protected FrameLayout contentWrapperLayout;
    /**
     * 标题栏和padding控制的布局
     */
    protected TitleBarLayout titleBarLayout;
    /**
     * 子类的内容布局
     */
    protected View contentView;
    /**
     * 情感图控制
     */
    protected AffectUI affectUI;
    protected BaseUI.UIFragment uiFragment;

    //<editor-fold desc="初始化方法">

    @NonNull
    @Override
    protected View createRootView() {
        return super.createRootView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.base_title_fragment_layout;
    }

    protected int getTitleBarLayoutId() {
        return R.layout.base_fragment_title_layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUiFragment().initFragment(this);
    }

    @Override
    protected void initBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.initBaseView(viewHolder, arguments, savedInstanceState);

        onInitBaseView(viewHolder, arguments, savedInstanceState);
    }

    protected void onInitBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        if (viewHolder.itemView instanceof FragmentContentWrapperLayout) {
            fragmentContentWrapperLayout = (FragmentContentWrapperLayout) viewHolder.itemView;
        }

        contentWrapperLayout = baseViewHolder.v(R.id.base_content_wrapper_layout);
        titleBarLayout = baseViewHolder.v(R.id.base_title_bar_layout);

        initBaseTitleLayout(arguments);
        initLeftControlLayout();
        initRightControlLayout();
        initContentLayout(arguments);
        //情感图
        affectUI = createAffectUI();
    }

    protected boolean needTitleBar() {
        return topFragment() == this;
    }

    /**
     * 初始化标题部分
     */
    protected void initBaseTitleLayout(@Nullable Bundle arguments) {
        if (titleBarLayout == null) {
            return;
        }
        if (!needTitleBar()) {
            hideTitleBar();
            return;
        }
        LayoutInflater.from(mAttachContext).inflate(getTitleBarLayoutId(), titleBarLayout);

        //设置标题背景颜色
        getUiFragment().initBaseTitleLayout(this, arguments);

        //设置标题
        setTitleString(getFragmentTitle());

        //标题栏事件拦截
        if (interceptRootTouchEvent()) {
            titleBarLayout.setClickable(true);
        }
    }

    /**
     * 左边控制按钮初始化
     */
    protected void initLeftControlLayout() {
        if (needBackItem()) {
            //添加返回按钮
            View backItem = createBackItem();
            if (backItem != null) {
                addLeftItem(backItem);
            }
        }
    }

    /**
     * 是否需要返回按钮
     */
    protected boolean needBackItem() {
        if (mAttachContext instanceof ContainerActivity) {
            return true;
        }
        return getParentFragment() == null &&
                getFragmentManager() != null &&
                getFragmentManager().getFragments().size() > 1;
    }

    /**
     * 右边控制按钮初始化
     */
    protected void initRightControlLayout() {

    }

    /**
     * 内容布局的parent
     */
    protected ViewGroup getContentWrapperLayout() {
        return contentWrapperLayout;
    }

    /**
     * 初始化内容部分
     */
    protected void initContentLayout(@Nullable Bundle arguments) {
        ViewGroup contentWrapperLayout = getContentWrapperLayout();

        if (contentWrapperLayout == null) {
            return;
        }
        int contentLayoutId = getContentLayoutId();
        if (contentLayoutId == -1) {
            contentView = createContentView(contentWrapperLayout);
        } else {
            contentView = LayoutInflater.from(mAttachContext).inflate(contentLayoutId, contentWrapperLayout, false);
        }

        if (contentNeedSoftInputLayout) {
            RSoftInputLayout inputLayout = new RSoftInputLayout(mAttachContext);
            inputLayout.setId(R.id.base_soft_input_layout);
            inputLayout.addView(contentView, new ViewGroup.LayoutParams(-1, -1));
            inputLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            contentView = inputLayout;
        }

        contentWrapperLayout.addView(contentView);
    }

    /**
     * 创建情感图控制类
     */
    protected AffectUI createAffectUI() {
        return getUiFragment().createAffectUI(this).create();
    }

    /**
     * 切换情感图
     */
    protected void switchAffectUI(int affect) {
        switchAffectUI(affect, null);
    }

    protected void switchAffectUI(int affect, Object extraObj) {
        if (affectUI != null) {
            affectUI.showAffect(affect, extraObj);
        }
    }

    /**
     * 浮动标题栏, 自动设置透明背景标题栏
     */
    public void floatTitleBar() {
        floatTitleBar(false, true);
    }

    public void floatTitleBar(boolean paddingContent, boolean tranTitleBar) {
        if (fragmentContentWrapperLayout != null) {
            if (paddingContent) {
                fragmentContentWrapperLayout.setContentLayoutState(FragmentContentWrapperLayout.CONTENT_BACK_OF_TITLE_PADDING);
            } else {
                fragmentContentWrapperLayout.setContentLayoutState(FragmentContentWrapperLayout.CONTENT_BACK_OF_TITLE);
            }
        }
        if (tranTitleBar) {
            setTitleBarLayoutColor(Color.TRANSPARENT);
        }
        hideTitleShadow();
    }

    /**
     * 隐藏默认的装饰
     */
    public void hideBaseStyle() {
        hideTitleView();
        hideTitleShadow();
        hideBackView();
    }

    /**
     * 设置标题栏的背景颜色
     */
    public void setTitleBarLayoutColor(@ColorInt int color) {
        titleControl()
                .selector(R.id.base_title_bar_layout)
                .setBackgroundColor(color);
    }

    /**
     * 设置显示的标题
     */
    public void setTitleString(@NonNull CharSequence title) {
        titleControl().selector(R.id.base_title_view).setText(title);
    }

    /**
     * 隐藏标题视图
     */
    public void hideTitleView() {
        titleControl().selector(R.id.base_title_view).gone();
    }

    /**
     * 替换标题栏
     */
    public void replaceTitleBarLayout(@LayoutRes int layoutId) {
        if (titleBarLayout != null) {
            titleBarLayout.removeAllViews();
            LayoutInflater.from(titleBarLayout.getContext()).inflate(layoutId, titleBarLayout, true);
        }
    }

    public void replaceTitleBarLayout(@NonNull View view) {
        if (titleBarLayout != null) {
            titleBarLayout.removeAllViews();
            titleBarLayout.addView(view);
        }
    }

    /**
     * 创建返回按钮
     */
    @Nullable
    protected View createBackItem() {
        return getUiFragment().createBackItem(this);
    }

    public void onTitleBackClick() {
        onTitleBackClick(null);
    }

    /**
     * 标题栏默认的返回按钮点击事件
     */
    public void onTitleBackClick(@Nullable View view) {
        //hideTitleBar();
        hideSoftInput();
        backFragment();
    }

    /**
     * 关闭Fragment
     */
    public void backFragment() {
        backFragment(true);
    }

    /**
     * 添加左边控制按钮
     */
    public void addLeftItem(@NonNull View itemView) {
        leftControl().addView(itemView);
    }

    public void addRightItem(@NonNull View itemView) {
        rightControl().addView(itemView);
    }

    public ViewGroupHelper rootControl() {
        return new ViewGroupHelper(baseViewHolder.itemView);
    }

    public ViewGroupHelper contentControl() {
        return new ViewGroupHelper(baseViewHolder.vg(R.id.base_content_wrapper_layout));
    }

    public ViewGroupHelper leftControl() {
        return new ViewGroupHelper(baseViewHolder.vg(R.id.base_title_left_layout));
    }

    public ViewGroupHelper rightControl() {
        return new ViewGroupHelper(baseViewHolder.vg(R.id.base_title_right_layout));
    }

    public ViewGroupHelper titleControl() {
        return new ViewGroupHelper(titleBarLayout);
    }

    //</editor-fold>

    //<editor-fold desc="界面属性控制方法">

    /**
     * 隐藏标题栏
     */
    public void hideTitleBar() {
        ViewGroupHelper.build(baseViewHolder.itemView)
                .selector(R.id.base_title_bar_layout)
                .gone()
                .selector(R.id.base_title_shadow_view)
                .gone();
    }

    public void showTitleBar() {
        ViewGroupHelper.build(baseViewHolder.itemView)
                .selector(R.id.base_title_bar_layout)
                .visible()
                .selector(R.id.base_title_shadow_view)
                .visible();
    }

    /**
     * 隐藏返回按钮
     */
    public void hideBackView() {
        leftControl().selector(R.id.base_title_back_view).gone();
    }

    /**
     * 移除返回按钮
     */
    public void removeBackView() {
        leftControl().selector(R.id.base_title_back_view).remove();
    }

    public void hideTitleShadow() {
        ViewGroupHelper.build(baseViewHolder.itemView).selector(R.id.base_title_shadow_view).gone();
    }

    public void removeTitleShadow() {
        ViewGroupHelper.build(baseViewHolder.itemView).selector(R.id.base_title_shadow_view).remove();
    }

    /**
     * 将阴影, 切换成线 展示
     */
    public void showTitleLine() {
        showTitleLine(ResUtil.getColor(R.color.base_line_color), ResUtil.getDimen(R.dimen.base_line_px));
    }

    public void showTitleLine(int color, int height) {
        ViewGroupHelper.build(baseViewHolder.itemView)
                .selector(R.id.base_title_shadow_view)
                .visible()
                .setBackgroundColor(color)
                .setHeight(height)
        ;
    }

    //</editor-fold>

    //<editor-fold desc="需要重写的方法">

    /**
     * 获取内容布局id
     */
    @LayoutRes
    protected int getContentLayoutId() {
        return -1;
    }

    @NonNull
    protected View createContentView(@NonNull ViewGroup contentWrapperLayout) {
        TextView textView = new TextView(contentWrapperLayout.getContext(), null, R.style.BaseMainTextStyle);
        textView.setText("这里是默认的内容布局\n请重写\ngetContentLayoutId()\n或\ncreateContentView()\n方法, 实现自定义布局.");
        return textView;
    }
    //</editor-fold>

    //<editor-fold desc="情感图回调方法">

    @Override
    public void onAffectChangeBefore(@NonNull AffectUI affectUI, int fromAffect, int toAffect) {

    }

    @Override
    public void onAffectChange(@NonNull AffectUI affectUI, int fromAffect, int toAffect,
                               @Nullable View fromView, @Nullable View toView) {
    }

    @Override
    public void onAffectInitLayout(@NonNull AffectUI affectUI, int affect, @NonNull View rootView) {

    }

    //</editor-fold>

    //<editor-fold desc="页面样式定制">

    @NonNull
    public BaseUI.UIFragment getUiFragment() {
        if (uiFragment == null) {
            return BaseUI.uiFragment;
        }
        return uiFragment;
    }

    public void setUiFragment(@NonNull BaseUI.UIFragment uiFragment) {
        this.uiFragment = uiFragment;
    }

    //</editor-fold desc="页面样式定制">
}
