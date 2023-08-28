package com.zhipin.diamond.http;

import static com.zhipin.diamond.utils.Constants.DEFAULT_PROTOCOL;
import static com.zhipin.diamond.utils.Constants.PROTOCOL_PREFIX;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

import com.zhipin.diamond.component.ServerUrlSettingStorage;
import org.apache.commons.lang3.StringUtils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:06
 */
public class HttpServiceFactory {

    private static final String SERVER_URL = "";

    private static HttpService httpService;



    public static HttpService getInstance() {
        trySetServer(ServerUrlSettingStorage.getServerUrl());
        return httpService;
    }

    /**
     * Init httpService with serverUrl if httpService is null.
     */
    public static void trySetServer(String serverUrl) {
        if (httpService == null) {
            setServer(serverUrl);
        }
    }

    public static void setServer(String serverUrl) {
        checkNotNull(serverUrl);
        if (!hasProtocol(serverUrl)) {
            serverUrl = DEFAULT_PROTOCOL + serverUrl;
        }
        // todo: 如果域名不合法，DNS查找时间较长，后面看看怎么优化一下
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        httpService = retrofit.create(HttpService.class);
    }

    private static boolean hasProtocol(String url) {
        return url.startsWith(PROTOCOL_PREFIX);
    }

    public static String normalization(String hostName) {
        if (StringUtils.isEmpty(hostName)) {
            return hostName;
        }
        hostName = StringUtils.trim(hostName);
        if (!hasProtocol(hostName)) {
            hostName = DEFAULT_PROTOCOL + hostName;
        }
        return hostName;
    }
}

