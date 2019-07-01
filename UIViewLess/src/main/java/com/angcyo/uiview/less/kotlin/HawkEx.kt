package com.angcyo.uiview.less.kotlin

import android.text.TextUtils
import com.angcyo.uiview.less.utils.RUtils
import com.orhanobut.hawk.Hawk

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**
 * this 对应的 key值,
 * 将获取到的 value 用 `,` 分割, 返回列表集合 (不含空字符)
 * @param maxCount 需要返回多少条
 * */
public fun String?.hawkGetList(maxCount: Int = Integer.MAX_VALUE): MutableList<String> {
    val result = mutableListOf<String>()
    this?.let {
        val get = Hawk.get(it, "")

        if (!TextUtils.isEmpty(get)) {
            result.addAll(RUtils.split(get, ",", false, true, maxCount))
        }
    }
    return result
}

/**将 value, 追加到 原来的value中*/
public fun String?.hawkPutList(value: String?) {
    if (TextUtils.isEmpty(value)) {
        return
    }
    this?.let {
        Hawk.put(
            it,
            //最新的在前面
            "$value," + Hawk.get(it, "")
                .replace(",,", ",")
                .replace(value ?: " ", "")
        )
    }
}

public fun String?.hawkPut(value: String?) {
    if (TextUtils.isEmpty(value)) {
        return
    }
    this?.let {
        Hawk.put(it, value ?: "")
    }
}

public fun String?.hawkGet(): String? {
    var result: String? = null
    this?.let {
        result = Hawk.get(it, null)
    }
    return result
}