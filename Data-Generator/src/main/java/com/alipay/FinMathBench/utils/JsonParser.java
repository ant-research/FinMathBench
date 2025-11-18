/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;

/**
 * JSON Parser Utility Class
 * Provides methods to parse JSON strings using JSONPath expressions
 * 
 * @author luojing.wp
 * @version JsonParser.java, v 0.1 November 10, 2025 6:03 PM luojing.wp
 */
public class JsonParser {
    /**
     * Parse JSON according to the path
     *
     * @param json JSON string to parse
     * @param path JSONPath expression
     * @param defaultValue Default value to return if parsing fails or input is invalid
     * @return Parsed result as string, or default value if parsing fails
     */
    public static Object read(String json, String path, String defaultValue) {
        if (StringUtils.isBlank(json) || StringUtils.isBlank(path)) {
            return defaultValue;
        }
        try {
            return JsonPath.read(json, path).toString();
        } catch (Exception e) {
            // Parsing target does not exist
            return defaultValue;
        }
    }

    /**
     * Parse JSON according to the path with empty string as default value
     *
     * @param json JSON string to parse
     * @param path JSONPath expression
     * @return Parsed result as string, or empty string if parsing fails
     */
    public static Object read(String json, String path) {
        return read(json, path, "");
    }
}