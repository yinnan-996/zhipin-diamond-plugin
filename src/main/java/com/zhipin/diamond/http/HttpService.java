package com.zhipin.diamond.http;

import java.util.List;

import com.zhipin.diamond.model.HotfixResult;
import com.zhipin.diamond.model.JvmProcess;
import com.zhipin.diamond.model.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * @author yinnan@kanzhun.com
 * @date 2023/8/18 12:05
 */
public interface HttpService {

    @Multipart
    @POST("/hotfix")
    Call<Result<HotfixResult>> reloadClass(@Part MultipartBody.Part file,
                                           @Part("targetPid") RequestBody targetPid, @Part("proxyServer") RequestBody hostName);

    @GET("/processList")
    Call<Result<List<JvmProcess>>> processList(@Query("proxyServer") String hostName);

    @GET("/hostList")
    Call<Result<List<String>>> hostList();

    @GET("/applicationList")
    Call<Result<List<String>>> applicationList();



    @Multipart
    @POST("/containerHotfix")
    Call<Result<HotfixResult>> reloadContainerClass(@Part MultipartBody.Part file, @Part("applicationName") RequestBody applicationName);
}
