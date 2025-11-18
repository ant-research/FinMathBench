/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * HTTP Utility Class
 * Provides HTTP request methods using Apache HttpClient
 * 
 * @author luojing.wp
 * @version HttpUtils.java, v 0.1 November 11, 2025 2:19 PM luojing.wp
 */
public class HttpUtils {
    /**
     * Synchronous GET request
     *
     * @param url Request URL
     * @param header Request headers
     * @return Response string
     * @throws IOException Network IO exception
     */
    public static String doSyncGet(String url, Map<String, String> header) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        header.forEach(request::setHeader);
        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } finally {
            client.close();
        }
    }

    /**
     * Synchronous POST request
     *
     * @param url Request URL
     * @param header Request headers
     * @param body Request body (JSON string)
     * @return Response string
     * @throws IOException Network IO exception
     */
    public static String doSyncPost(String url, Map<String, String> header, String body)
            throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(url);
        header.forEach(request::setHeader);
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        
        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } finally {
            client.close();
        }
    }

    /**
     * Overloaded method: Convert Map to JSON string and send POST request
     *
     * @param url Request URL
     * @param header Request headers
     * @param bodyMap Request body as Map (will be converted to JSON string)
     * @return Response string
     * @throws IOException Network IO exception
     */
    public static String doSyncPost(String url, Map<String, String> header, Map<String, Object> bodyMap)
            throws IOException {
        String body = GsonUtils.toString(bodyMap);
        return doSyncPost(url, header, body);
    }

    /**
     * Overloaded method: Send POST request and handle Server-Sent Events (SSE) response
     *
     * @param url Request URL
     * @param header Request headers
     * @param body Request body (JSON string)
     * @param dataHandler Consumer to handle each line of SSE response
     * @throws IOException Network IO exception
     */
    public static void doSyncPostAcceptSSE(String url, Map<String, String> header, String body, Consumer<String> dataHandler)
            throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(url);
        header.forEach(request::setHeader);
        request.setHeader("Accept", "text/event-stream");
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            
            if (entity != null) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        dataHandler.accept(line);
                    }
                }
            }
        } finally {
            client.close();
        }
    }
}
