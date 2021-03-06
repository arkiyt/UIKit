package com.angcyo.uiview.less

import android.text.TextUtils
import com.angcyo.http.CacheAdapter
import com.angcyo.http.CacheInterceptor
import com.angcyo.lib.L
import com.angcyo.uiview.less.OfflineCacheAdapter.Companion.ENTRY_BODY
import com.angcyo.uiview.less.OfflineCacheAdapter.Companion.ENTRY_METADATA
import com.angcyo.uiview.less.kotlin.app
import com.angcyo.uiview.less.kotlin.pattern
import com.angcyo.uiview.less.kotlin.readString
import com.angcyo.uiview.less.utils.RCacheManager
import com.angcyo.uiview.less.utils.RLogFile
import com.angcyo.uiview.less.utils.RNetwork
import com.angcyo.uiview.less.utils.Root
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.internal.cache.DiskLruCache
import okhttp3.internal.io.FileSystem
import okio.*
import okio.ByteString.Companion.encodeUtf8
import java.io.File
import java.io.IOException

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/07/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class OfflineCacheAdapter(cacheFolder: String = Root.getAppExternalFolder("http_cache")) :
    CacheAdapter() {

    companion object {
        const val VERSION = 201105
        const val ENTRY_METADATA = 0
        const val ENTRY_BODY = 0
        const val ENTRY_COUNT = 1

        /**需要忽略缓存的url正则*/
        val IGNORE_URL_PATTERNS = mutableSetOf<String>()
    }

    val cache: DiskLruCache = DiskLruCache.create(
        FileSystem.SYSTEM,
        File(cacheFolder),
        VERSION,
        ENTRY_COUNT, 500 * 1024 * 1024L
    )

    var cacheAdapterListener = OfflineCacheAdapterListener()

    init {
        RCacheManager.instance()
            .addCachePath(cacheFolder)
    }

    override fun checkNeedCache(request: Request): Boolean {
        cache.initialize()

        if (RNetwork.isConnect(app())) {
            return false
        }
        if (IGNORE_URL_PATTERNS.isNotEmpty()) {
            if (request.url.toString().pattern(IGNORE_URL_PATTERNS, false)) {
                return false
            }
        }
        if (isForceCache(request)) {
            return true
        }
        //默认不开启缓存
        return false//isJsonType(request)
    }

    /**读取缓存*/
    override fun loadCache(request: Request): Response? {
        if (isForceCache(request)) {

        } else if (!isJsonType(request)) {
            return null
        }

        val key = cacheAdapterListener.key(request)

        loadCache(key)?.let { snapshot ->
            L.i("读取缓存:$key:${cacheAdapterListener.keyUrl(request)}".apply {
                RLogFile.log("http", this)
            })

            val headers = request.headers

            val contentType = request.body?.contentType()?.toString() ?: "application/json"
            val contentLength = snapshot.getLength(ENTRY_METADATA)
            val cacheRequest = request.newBuilder()
                .build()
            return Response.Builder()
                .request(cacheRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("离线数据读取成功.")
                .headers(headers)
                .body(CacheResponseBody(snapshot, contentType, "$contentLength"))
                .build()
        }

        L.i("无缓存:$key:${cacheAdapterListener.keyUrl(request)}".apply {
            RLogFile.log("http", this)
        })

        return null
    }

    fun loadCache(key: String): DiskLruCache.Snapshot? {
        val snapshot: DiskLruCache.Snapshot?
        try {
            snapshot = cache.get(key)
            if (snapshot == null) {
                return null
            }
        } catch (e: IOException) {
            // Give up because the cache cannot be read.
            return null
        }
        return snapshot
    }

    fun loadCacheJson(key: String): String? {
        val snapshot: DiskLruCache.Snapshot?
        try {
            snapshot = cache.get(key)
            if (snapshot == null) {
                return null
            }
        } catch (e: IOException) {
            // Give up because the cache cannot be read.
            return null
        }
        return snapshot.readString()
    }

    /**保存缓存数据*/
    override fun saveCache(request: Request, response: Response) {
        if (!response.isSuccessful) {
            return
        }

        if (isForceCache(request) || isJsonType(request)) {

        } else {
            return
        }

        response.body?.let {

            val key = cacheAdapterListener.key(request)
            saveCache(key, it.source().buffer.clone(), it.contentLength())
            cache.flush()

            L.i("保存缓存:$key:${cacheAdapterListener.keyUrl(request)}".apply {
                RLogFile.log("http", this)
            })
        }
    }

    fun saveCache(key: String, json: String? = null) {
        if (json == null) {
            return
        }

        val buffer = Buffer().writeString(json, Charsets.UTF_8)

        saveCache(key, buffer, buffer.size)
    }

    fun saveCache(key: String, buffer: Buffer, byteCount: Long): Boolean {
        var editor: DiskLruCache.Editor? = null

        try {
            editor = cache.edit(key)
            if (editor == null) {
                return false
            }
            editor.newSink(ENTRY_METADATA).apply {
                write(buffer, byteCount)
                close()
            }
            editor.commit()
        } catch (e: Exception) {
            editor?.abort()
            return false
        }
        return true
    }

    fun haveCache(key: String): Boolean {
        return loadCacheJson(key) != null
    }

    /**只缓存json请求数据*/
    private fun isJsonType(request: Request): Boolean {
        var isJsonType = false
        request.body?.contentType()?.let {
            if (it.toString().contains("application/json")) {
                isJsonType = true
            }
        }
        return isJsonType
    }

    /**强制使用缓存*/
    private fun isForceCache(request: Request): Boolean {

        //此请求, 是否需要缓存
        var cache = false

        //Url中, 包含了离线缓存参数
        cache = request.url.queryParameter(CacheInterceptor.OFFLINE_CACHE) != null

        if (cache) {
            return true
        }

        return request.header(CacheInterceptor.HEADER_FORCE_CACHE)?.isNotEmpty() == true
    }
}

fun CacheInterceptor?.getOfflineCacheAdapter(): OfflineCacheAdapter? {
    this?.cacheAdapter?.forEach {
        if (it is OfflineCacheAdapter) {
            return it
        }
    }
    return null
}

fun DiskLruCache.Snapshot?.readString(charsetName: String = "UTF-8"): String? {
    if (this == null) {
        return null
    }
    val contentLength = this.getLength(ENTRY_METADATA)
    return CacheResponseBody(
        this,
        "application/json",
        "$contentLength"
    ).readString(charsetName)
}

class CacheResponseBody internal constructor(
    val snapshot: DiskLruCache.Snapshot,
    val contentType: String?,
    val contentLength: String?
) : ResponseBody() {
    private val bodySource: BufferedSource

    init {

        val source = snapshot.getSource(ENTRY_BODY)
        bodySource = object : ForwardingSource(source) {
            @Throws(IOException::class)
            override fun close() {
                snapshot.close()
                super.close()
            }
        }.buffer()
    }

    override fun contentType(): MediaType? {
        return contentType?.toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        try {
            return if (contentLength != null) java.lang.Long.parseLong(contentLength) else -1
        } catch (e: NumberFormatException) {
            return -1
        }

    }

    override fun source(): BufferedSource {
        return bodySource
    }
}

open class OfflineCacheAdapterListener {

    /**请求对应的url地址, body参数会在url?后面*/
    open fun keyUrl(request: Request): String {
        val url = request.url
        val params = request.body?.readString()
        return keyUrl("$url", params ?: "")
    }

    /**请求对应的url地址, body参数会在url?后面*/
    open fun keyUrl(url: String, jsonBody: String? = ""): String {
        return if (TextUtils.isEmpty(jsonBody)) {
            url
        } else {
            "$url?$jsonBody"
        }
    }

    open fun key(request: Request): String {
        val url = request.url
        val params = request.body?.readString()
        return key("$url", "$params")
    }

    /**计算url对应的key值*/
    open fun key(url: String, jsonBody: String? = ""): String {
        return keyUrl(url, jsonBody).encodeUtf8().md5().hex()
    }
}