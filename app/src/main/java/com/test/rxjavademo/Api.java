package com.test.rxjavademo;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by W.J on 2017/9/7.
 */

public interface Api {
    @GET("type")
    Observable<ResultBean> queryList(@Query("key") String key);
}
