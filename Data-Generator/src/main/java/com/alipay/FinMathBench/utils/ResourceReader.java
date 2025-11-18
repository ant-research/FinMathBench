/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author luojing.wp
 * @version ResourceReader.java, v 0.1 2025年11月11日 下午4:05 luojing.wp
 */
public class ResourceReader {
    /**
     * load resource from classpath
     *
     * @param resourcePath file path
     * @return content
     * @throws Exception
     */
    public static String read(String resourcePath) throws RuntimeException {
        try (InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
            scanner.useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            return content;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }
}