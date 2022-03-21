package com.alibaba.sreworks.common.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author yangjinghua
 */
@Slf4j
@Data
@NoArgsConstructor
public class Requests {

    private static final OkHttpClient HTTP_CLIENT =
        new OkHttpClient.Builder().connectionPool(new ConnectionPool()).build();

    private static Request.Builder createRequestBuilder(String url, JSONObject params, JSONObject headers) {
        HttpUrl.Builder queryUrl = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        for (String key : params.keySet()) {
            queryUrl.addQueryParameter(key, params.getString(key));
        }
        Request.Builder requestBuilder = new Request.Builder().url(queryUrl.build());
        for (String key : headers.keySet()) {
            requestBuilder.addHeader(key, headers.getString(key));
        }
        return requestBuilder;
    }

    public String url;

    public JSONObject params = new JSONObject();

    public JSONObject headers = new JSONObject();

    public String postJson = "{}";

    public Boolean showLog = false;

    public Requests(String url) {
        this.url = url;
    }

    public Requests(String url, Boolean showLog) {
        this.url = url;
        this.showLog = showLog;
    }

    public Requests url(String url) {
        this.url = url;
        return this;
    }

    public Requests params(JSONObject params) {
        this.params = params;
        return this;
    }

    public Requests params(Object... args) {
        this.params = JsonUtil.map(args);
        return this;
    }

    public Requests headers(JSONObject headers) {
        this.headers = headers;
        return this;
    }

    public Requests headers(Object... args) {
        this.headers = JsonUtil.map(args);
        return this;
    }

    public Requests postJson(JSONObject postJson) {
        this.postJson = JSONObject.toJSONString(postJson);
        return this;
    }

    public Requests postJson(Object... args) {
        this.postJson = JSONObject.toJSONString(JsonUtil.map(args));
        return this;
    }

    public Requests postJson(String postJson) {
        this.postJson = postJson;
        return this;
    }

    public Response response;

    public Requests isSuccessful() throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException(String.format(
                "response is not successful: %s; retBody: %s", response.toString(), getString()
            ));
        }
        return this;
    }

    public String getString() throws IOException {
        String responseBody = response.body() == null ? "" : Objects.requireNonNull(response.body()).string();
        if (this.showLog) {
            log.info("SHOW_RESPONSE: " + responseBody);
        }
        return responseBody;
    }

    public <T> T getObject(Class<T> clazz) throws IOException {
        return JSONObject.parseObject(getString(), clazz);
    }

    public JSONObject getJSONObject() throws IOException {
        return getObject(JSONObject.class);
    }

    public JSONArray getJSONArray() throws IOException {
        return getObject(JSONArray.class);
    }

    public <T> List<T> getJSONArray(Class<T> clazz) throws IOException {
        return getObject(JSONArray.class).toJavaList(clazz);
    }

    public Boolean getBoolean() throws IOException {
        return getObject(Boolean.class);
    }

    public Requests get() throws IOException {
        Request.Builder requestBuilder = createRequestBuilder(url, params, headers);
        Request request = requestBuilder
            .get()
            .build();
        response = HTTP_CLIENT.newCall(request).execute();
        return this;
    }

    public Requests post() throws IOException {
        Request.Builder requestBuilder = createRequestBuilder(url, params, headers);
        Request request = requestBuilder
            .post(RequestBody.create(MediaType.parse("application/json"), postJson))
            .build();
        response = HTTP_CLIENT.newCall(request).execute();
        return this;
    }

    public Requests upload(File file) throws IOException {
        Request.Builder requestBuilder = createRequestBuilder(url, params, headers);
        Request request = requestBuilder
            .post(RequestBody.create(MediaType.parse("application/octet-stream"), file))
            .build();
        response = HTTP_CLIENT.newCall(request).execute();
        return this;
    }

    public Requests put() throws IOException {
        Request.Builder requestBuilder = createRequestBuilder(url, params, headers);
        Request request = requestBuilder
            .put(RequestBody.create(MediaType.parse("application/json"), postJson))
            .build();
        response = HTTP_CLIENT.newCall(request).execute();
        return this;
    }

    public Requests delete() throws IOException {
        Request.Builder requestBuilder = createRequestBuilder(url, params, headers);
        Request request = requestBuilder
            .delete()
            .build();
        response = HTTP_CLIENT.newCall(request).execute();
        return this;
    }

}

