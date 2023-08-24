package com.zhipin.diamond.http;

import static com.zhipin.diamond.utils.Constants.DEFAULT_PROTOCOL;
import static com.zhipin.diamond.utils.Constants.PROTOCOL_PREFIX;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:06
 */
public class HttpServiceFactory {

    private static final String SERVER_URL = "https://diamond-web-qa.weizhipin.com/";

    private static final HttpService httpService;

    static {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        httpService = retrofit.create(HttpService.class);
    }

    public static HttpService getInstance() {
        checkNotNull(httpService);
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

