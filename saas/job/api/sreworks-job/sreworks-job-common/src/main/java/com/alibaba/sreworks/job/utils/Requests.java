package com.alibaba.sreworks.job.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;

public class Requests {

    static HttpClient client = HttpClient.newBuilder().build();

    private static String addParams(String url, JSONObject params) {

        if (params == null) {
            return url;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        for (String key : params.keySet()) {
            String value = params.getString(key);
            builder.queryParam(key, value);
        }
        return builder.build().toUri().toString();
    }

    private static void addHeaders(HttpRequest.Builder builder, JSONObject headers) {

        if (headers == null) {
            return;
        }
        for (String key : headers.keySet()) {
            String value = headers.getString(key);
            builder.header(key, value);
        }

    }

    public static HttpResponse<String> post(
        String url, JSONObject headers, JSONObject params, String postBody) throws IOException, InterruptedException {

        if (postBody == null) {
            postBody = "";
        }
        url = addParams(url, params);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(postBody));
        addHeaders(builder, headers);
        return client.send(builder.build(), BodyHandlers.ofString());

    }

    public static HttpResponse<String> get(
        String url, JSONObject headers, JSONObject params) throws IOException, InterruptedException {

        url = addParams(url, params);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET();
        addHeaders(builder, headers);
        return client.send(builder.build(), BodyHandlers.ofString());

    }

    public static HttpResponse<String> delete(
        String url, JSONObject headers, JSONObject params) throws IOException, InterruptedException {

        url = addParams(url, params);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .DELETE();
        addHeaders(builder, headers);
        return client.send(builder.build(), BodyHandlers.ofString());

    }

    public static HttpResponse<String> upload(File uploadFile, String url, JSONObject headers, JSONObject params) throws IOException, InterruptedException {
        url = addParams(url, params);
        String boundary = "boundary_" + System.currentTimeMillis();
        byte[] bytes = Files.readAllBytes(uploadFile.toPath());

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(createMultipartRequestEntity(bytes, uploadFile.getName(), boundary)));
        addHeaders(builder, headers);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * 创建多部分请求实体
     *
     * @param bytes 文件字节流
     * @param fileName 文件名
     * @param boundary boundary
     * @return 多部分请求实体字节流
     */
    private static byte[] createMultipartRequestEntity(byte[] bytes, String fileName, String boundary) {
        String lineSeparator = "\r\n";
        StringBuilder builder = new StringBuilder();

        builder.append("--").append(boundary).append(lineSeparator);
        builder.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"").append(lineSeparator);
        builder.append("Content-Type: application/octet-stream").append(lineSeparator);
        builder.append(lineSeparator);

        String header = builder.toString();
        byte[] headerBytes = header.getBytes();

        byte[] footerBytes = (lineSeparator + "--" + boundary + "--" + lineSeparator).getBytes();

        byte[] result = new byte[headerBytes.length + bytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(bytes, 0, result, headerBytes.length, bytes.length);
        System.arraycopy(footerBytes, 0, result, headerBytes.length + bytes.length, footerBytes.length);

        return result;
    }

    public static void checkResponseStatus(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 300) {
            throw new Exception("response statusCode is " + response.statusCode() + " body is " + response.body());
        } else {
            String strBody = response.body();
            if (StringUtils.isNotEmpty(strBody)) {
                JSONValidator validator = JSONValidator.from(response.body());
                boolean validJSON = validator.validate();
                if (validJSON) {
                    JSONObject body = JSONObject.parseObject(response.body());
                    int execCode = body.getIntValue("code");
                    if (execCode >= 300) {
                        throw new Exception("response is " + response.body());
                    }
                }
            }
        }
    }

}
