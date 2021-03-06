package com.angcyo.uiview.less.base;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.*;
import android.os.Build;
import com.angcyo.http.Rx;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.RApplication;
import com.angcyo.uiview.less.utils.utilcode.utils.NetworkUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：网络状态监听广播
 * 创建人员：Robi
 * 创建时间：2017/03/10 10:58
 * 修改人员：Robi
 * 修改时间：2017/03/10 10:58
 * 修改备注：
 * Version: 1.0.0
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public static NetworkWrapper sNetworkWrapper = new NetworkWrapper();
    static Set<NetworkStateListener> sListeners = new HashSet<>();
    static Object lock = new Object();
    private static NetworkUtils.NetworkType netType = NetworkUtils.NetworkType.NETWORK_NO;

    public static void addNetworkStateListener(NetworkStateListener listener) {
        synchronized (lock) {
            sListeners.add(listener);
        }
    }

    public static synchronized void removeNetworkStateListener(NetworkStateListener listener) {
        synchronized (lock) {
            sListeners.remove(listener);
        }
    }

    public static void init(Application application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    //会走 onLinkPropertiesChanged 方法
                    L.w("NetworkStateReceiver: onAvailable([network]) 网络已连接 -> " + network);
                    sNetworkWrapper.network = network;

//                    if (sNetworkWrapper.network == null) {
//                        oldType = NetworkUtils.NetworkType.NETWORK_NO;
//                    } else {
//                        oldType = NetworkUtils.NetworkType.NETWORK_UNKNOWN;
//                    }
//
//                    synchronized (lock) {
//                        for (NetworkStateListener listener : sListeners) {
//                            listener.onNetworkChange(oldType, NetworkUtils.NetworkType.NETWORK_NO);
//                        }
//                    }
                }

                @Override
                public void onLosing(Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    L.i("NetworkStateReceiver: onLosing([network, maxMsToLive]) 正在断开 -> ");
                    //sNetworkWrapper.network = null;
                    //sNetworkWrapper.lostNetwork = network;
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    L.w("NetworkStateReceiver: onLost([network]) 网络断开-> " + network);
                    NetworkUtils.NetworkType oldType;
                    if (sNetworkWrapper.network == null) {
                        oldType = NetworkUtils.NetworkType.NETWORK_NO;
                    } else {
                        oldType = NetworkUtils.NetworkType.NETWORK_UNKNOWN;
                    }

                    sNetworkWrapper.network = null;
                    sNetworkWrapper.lostNetwork = network;
                    synchronized (lock) {
                        for (NetworkStateListener listener : sListeners) {
                            listener.onNetworkChange(oldType, NetworkUtils.NetworkType.NETWORK_NO);
                        }
                    }
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    L.i("NetworkStateReceiver: onUnavailable([]) 无网络 -> ");
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    L.d("NetworkStateReceiver: onCapabilitiesChanged([network, networkCapabilities])-> " + networkCapabilities.describeContents());

                    sNetworkWrapper.networkCapabilities = networkCapabilities;
                    sNetworkWrapper.network = network;
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                    L.i("NetworkStateReceiver: onLinkPropertiesChanged([network, linkProperties])-> ");
                    sNetworkWrapper.network = network;
                    sNetworkWrapper.linkProperties = linkProperties;

                    final NetworkUtils.NetworkType oldType;
                    if (sNetworkWrapper.lostNetwork == null) {
                        oldType = NetworkUtils.NetworkType.NETWORK_NO;
                    } else {
                        oldType = NetworkUtils.NetworkType.NETWORK_UNKNOWN;
                    }

                    sNetworkWrapper.lostNetwork = null;
                    synchronized (lock) {
                        Rx.main(new Runnable() {
                            @Override
                            public void run() {
                                for (NetworkStateListener listener : sListeners) {
                                    listener.onNetworkChange(oldType, NetworkUtils.NetworkType.NETWORK_UNKNOWN);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 获取网络类型
     */
    public static NetworkUtils.NetworkType getNetType() {
        //if (netType == NetworkUtils.NetworkType.NETWORK_NO) {
        netType = NetworkUtils.getNetworkType(RApplication.getApp());
        //}
        return netType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        NetworkUtils.NetworkType oldType = netType;

        netType = NetworkUtils.getNetworkType(activeNetworkInfo);

        if (oldType != netType) {
            synchronized (lock) {
                for (NetworkStateListener listener : sListeners) {
                    listener.onNetworkChange(oldType, netType);
                }
            }
        }
        if (netType == NetworkUtils.NetworkType.NETWORK_WIFI) {
            //RxBus.get().post("update", "Wifi");
        }
        L.w("网络变化至:" + netType + " " + (netType.value() > 0 ? "有网" : "没网"));
    }

    public interface NetworkStateListener {
        void onNetworkChange(NetworkUtils.NetworkType from, NetworkUtils.NetworkType to);
    }

    public static class NetworkWrapper {
        public LinkProperties linkProperties;
        public Network network, lostNetwork;
        public NetworkCapabilities networkCapabilities;
    }
}
