package com.angcyo.uiview.less.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.angcyo.http.Json;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.BuildConfig;
import com.angcyo.uiview.less.RCrashHandler;
import com.angcyo.uiview.less.base.helper.ActivityHelper;
import com.angcyo.uiview.less.base.helper.FragmentHelper;
import com.angcyo.uiview.less.picture.RPicture;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.utils.RUtils;
import com.angcyo.uiview.less.utils.Reflect;
import com.angcyo.uiview.less.utils.Root;
import com.angcyo.uiview.less.utils.Tip;
import com.angcyo.uiview.less.widget.group.FragmentSwipeBackLayout;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.List;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/11/13
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    /**
     * 画中画状态判断
     */
    public boolean isActivityResume = false;
    protected RBaseViewHolder viewHolder;
    protected RxPermissions mRxPermissions;
    protected FragmentSwipeBackLayout fragmentSwipeBackLayout;
    protected int fragmentParentLayoutId = -1;
    protected RecyclerView.RecycledViewPool recycledViewPool;

    //<editor-fold desc="生命周期, 系统的方法">
    long lastBackTime = 0L;

    protected ActivityInfo mActivityInfo;

    @NonNull
    public static String logActivityInfo(ActivityInfo activityInfo) {
        StringBuilder builder = new StringBuilder();
        if (activityInfo != null) {
            L.i("ActivityInfo:");
            String to = Json.to(activityInfo);
            L.json(to);
        }
        return builder.toString();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);//TBS X5
        if (enableLayoutFull()) {
            ActivityHelper.setNoTitleNoActionBar(this);
            ActivityHelper.enableLayoutFullScreen(this, true);
        }

        viewHolder = new RBaseViewHolder(getWindow().getDecorView());

        //系统Fragment操作日志输出
        //FragmentManager.enableDebugLogging(BuildConfig.DEBUG);

        String className = this.getClass().getSimpleName();
        Intent parentActivityIntent = getParentActivityIntent();
        Intent supportParentActivityIntent = getSupportParentActivityIntent();

        mActivityInfo = (ActivityInfo) Reflect.getFieldValue(this, Activity.class, "mActivityInfo");
        logActivityInfo(mActivityInfo);
        if (mActivityInfo != null) {
            L.v(className +
                    " taskId:" + getTaskId() +
                    " root:" + isTaskRoot() +
                    " taskAffinity:" + mActivityInfo.taskAffinity);
        } else {
            L.v(className +
                    " taskId:" + getTaskId() +
                    " root:" + isTaskRoot());
        }

        L.v(className + " parentActivityIntent:" + parentActivityIntent);
        L.v(className + " supportParentActivityIntent:" + supportParentActivityIntent);

        //ActivityHelper.setStatusBarColor(this, Color.YELLOW);
        //ActivityHelper.setStatusBarDrawable(this, getDrawableCompat(R.drawable.base_nav_shadow));
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    /**
     * 统一处理Intent的地方
     */
    public void handleIntent(@Nullable Intent intent) {

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        checkCrash();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearRecycledViewPool();
    }

    @Override
    public boolean isImmersive() {
        return enableLayoutFull();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Debug.addDebugTextView(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentSwipeBackLayout != null) {
            if (fragmentSwipeBackLayout.isSwipeDrag()) {
                return;
            }

            if (fragmentSwipeBackLayout.isInDebugLayout()) {
                fragmentSwipeBackLayout.closeDebugLayout();
                return;
            }
        }

        int fragmentParentLayoutId = getFragmentParentLayoutId();

        if (fragmentParentLayoutId != -1) {

            List<Fragment> fragments = FragmentHelper.getFragmentList(getSupportFragmentManager(), fragmentParentLayoutId);
            int size = fragments.size();

            if (size > 1) {
                if (!onFragmentBackPressed(fragmentParentLayoutId, fragments.get(size - 1))) {
                    return;
                }
            } else if (size == 1) {
                boolean canBack = true;
                Fragment fragment = fragments.get(0);
                if (fragment instanceof IFragment) {
                    canBack = ((IFragment) fragment).onBackPressed(this);
                }
                if (!canBack) {
                    return;
                }
            }
        }

        if (needMoreTaskToBack()) {
            moveTaskToBack();
        } else {
            if (needCheckBackPressed()) {
                if (checkBackPressed()) {
                    onBackPressedInner(false);
                }
            } else {
                onBackPressedInner(false);
            }
        }
    }

    /**
     * 关闭activity
     */
    public void onBackPressedInner(boolean just) {
        if (just) {
            super.onBackPressed();
        } else {
            ActivityHelper.build(this)
                    .defaultExitAnim()
                    .finish(true);
        }
    }

    /**
     * 返回键, 退出Fragment的默认处理
     *
     * @return true activity can back .
     */
    protected boolean onFragmentBackPressed(int layoutId, @Nullable Fragment lastFragment) {

        FragmentHelper.Builder builder = FragmentHelper.build(getSupportFragmentManager())
                .parentLayoutId(layoutId)
                .defaultExitAnim();

        if (lastFragment instanceof BaseFragment) {
            ((BaseFragment) lastFragment).configBackBuilder(builder);
        }

        return builder.back(this);
    }

    /**
     * Fragment所在的ViewGroup id
     */
    @IdRes
    public int getFragmentParentLayoutId() {
        if (fragmentParentLayoutId == -1) {
            fragmentParentLayoutId = FragmentHelper.getFragmentContainerId(getSupportFragmentManager());
        }
        return fragmentParentLayoutId;
    }

    protected boolean needMoreTaskToBack() {
        return false;
    }

    /**
     * 是否可以back
     */
    protected boolean checkBackPressed() {
        long nowTime = System.currentTimeMillis();
        if (nowTime - lastBackTime < 1_000L) {
            return true;
        } else {
            lastBackTime = nowTime;
            onBackPressedTime();
            return false;
        }
    }

    protected void onBackPressedTime() {
        Tip.tip("再按一次, 退出程序!");
    }

    protected boolean needCheckBackPressed() {
        return false;
    }

    public void moveTaskToBack() {
        moveTaskToBack(true);
    }

    //</editor-fold desc="生命周期, 系统的方法">

    //<editor-fold desc="权限相关方法">

    /**
     * 激活沉浸式, 5.0以下不支持.
     */
    protected boolean enableLayoutFull() {
        return true;
    }

    /**
     * 请求忽略电池优化, 在后台存活机会会加大
     */
    public void ignoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    //some device doesn't has activity to handle this intent
                    //so add try catch
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
        }
    }

    protected String[] needPermissions() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.ACCESS_WIFI_STATE
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.CAMERA,
//                Manifest.permission.READ_CONTACTS
        };
    }

    protected void checkPermissions() {
        checkPermissionsResult(needPermissions(), new Action1<String>() {
            @Override
            public void call(String string) {
//                onPermissionDenied(string);
                if (string.contains("0")) {
                    //有权限被拒绝
                    onPermissionDenied(string);
                } else {
                    //所有权限通过
                    onLoadViewAfterPermission(getIntent());
                }
            }
        });
    }

    public void checkPermissionsResult(String[] permissions, final Action1<String> onResult) {
        if (mRxPermissions == null) {
            mRxPermissions = new RxPermissions(this);
        }
        mRxPermissions.requestEach(permissions)
                .map(new Func1<Permission, String>() {
                    @Override
                    public String call(Permission permission) {
                        if (permission.granted) {
                            return permission.name + "1";
                        }
                        return permission.name + "0";
                    }
                })
                .scan(new Func2<String, String, String>() {
                    @Override
                    public String call(String s, String s2) {
                        return s + ":" + s2;
                    }
                })
                .last()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        L.e("\n" + BaseAppCompatActivity.this.getClass().getSimpleName() + " 权限状态-->\n"
                                + s.replaceAll("1", " 允许").replaceAll("0", " 拒绝").replaceAll(":", "\n"));
                        onResult.call(s);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        onResult.call("0");
                    }
                });
//                .subscribe(new Action1<Permission>() {
//                    @Override
//                    public void call(Permission permission) {
//                        if (permission.granted) {
//                            T.show(UILayoutActivity.this, "权限允许");
//                        } else {
//                            notifyAppDetailView();
//                            T.show(UILayoutActivity.this, "权限被拒绝");
//                        }
//                    }
//                });
    }

    public void checkPermissions(String[] permissions, final Action1<Boolean> onResult) {
        if (this.isDestroyed()) {
            return;
        }

        checkPermissionsResult(permissions, new Action1<String>() {
            @Override
            public void call(String s) {
                if (s.contains("0")) {
                    /*有权限被拒绝*/
                    onResult.call(false);
                } else {
                    /*所欲权限通过*/
                    onResult.call(true);
                }
            }
        });
    }

    /**
     * 权限通过后回调
     */
    protected void onLoadViewAfterPermission(Intent intent) {
        L.i("设备自定义IMEI:" + Root.initImei());
    }

    /**
     * 权限拒绝后回调
     */
    protected void onPermissionDenied(String permission) {
//        startIView(new PermissionDeniedUIView(
//                        permission.replaceAll("1", "").replaceAll("0", "")),
//                false);
        RUtils.openAppDetailView(this);
        finish();
        Tip.tip("权限被拒绝!");
    }

    public Drawable getDrawableCompat(@DrawableRes int res) {
        return ContextCompat.getDrawable(this, res);
    }

    //</editor-fold desc="权限相关方法">

    //<editor-fold desc="其他方法">

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment lastFragment = FragmentHelper.getLastFragment(getSupportFragmentManager(), getFragmentParentLayoutId(), 0);
        if (lastFragment instanceof AbsFragment) {
            lastFragment.onActivityResult(requestCode, resultCode, data);
        }
        RPicture.INSTANCE.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Fragment lastFragment = FragmentHelper.getLastFragment(getSupportFragmentManager(), getFragmentParentLayoutId(), 0);
        if (lastFragment instanceof AbsFragment) {
            lastFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void setFragmentSwipeBackLayout(FragmentSwipeBackLayout fragmentSwipeBackLayout) {
        this.fragmentSwipeBackLayout = fragmentSwipeBackLayout;
    }

    public void checkLightStatusBar(boolean lightStatusBar) {
        if (fragmentSwipeBackLayout != null &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragmentSwipeBackLayout.setDimStatusBar(lightStatusBar);
        }
    }

    protected void checkCrash() {
        if (BuildConfig.DEBUG) {
            RCrashHandler.checkCrash(this);
        }
    }

    /**
     * 需要的权限是否都允许了
     */
    protected boolean haveSelfPermission() {
        boolean have = true;
        for (String permission : needPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                have = false;
                break;
            }
        }
        return have;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        L.w("\n" + newConfig.toString());
        onOrientationChanged(newConfig.orientation);
    }

    protected void onOrientationChanged(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //切换到横屏
            onOrientationToLandscape();
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            //切换到竖屏
            onOrientationToPortrait();
        }
    }

    //</editor-fold desc="其他方法">

    //<editor-fold desc="画中画支持">

    protected void onOrientationToLandscape() {

    }

    protected void onOrientationToPortrait() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityResume = true;
    }

    //</editor-fold desc="画中画支持">

    //<editor-fold desc="RecyclerView共享二级缓存池">

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        isActivityResume = false;
    }

    public RecyclerView.RecycledViewPool getRecycledViewPool() {
        if (recycledViewPool == null) {
            recycledViewPool = new RecyclerView.RecycledViewPool();
        }
        return recycledViewPool;
    }

    public void clearRecycledViewPool() {
        if (recycledViewPool != null) {
            recycledViewPool.clear();
            recycledViewPool = null;
        }
    }

    //</editor-fold desc="RecyclerView共享二级缓存池">
}
