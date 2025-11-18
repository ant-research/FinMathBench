/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import java.text.DecimalFormat;

/**
 * @author luojing.wp
 * @version NumberFormatter.java, v 0.1 2025年11月10日 下午5:35 luojing.wp
 */
public class NumberFormatter {
    /**
     * Format numbers in string form, adding thousands separators
     *
     * @param numberStr Input number string
     * @return Formatted string
     */
    public static String formatNumber(String numberStr) {
        try {
            // Check if it contains decimal point
            int dotIndex = numberStr.indexOf('.');
            String integerPart;
            String decimalPart = "";

            if (dotIndex != -1) {
                // Separate integer and decimal parts
                integerPart = numberStr.substring(0, dotIndex);
                // Include decimal point and decimal part
                decimalPart = numberStr.substring(dotIndex);
            } else {
                // Only integer part
                integerPart = numberStr;
            }

            // Use DecimalFormat to format integer part
            DecimalFormat formatter = new DecimalFormat("#,###");
            String formattedInteger = formatter.format(Long.parseLong(integerPart));

            // Concatenate integer and decimal parts
            return formattedInteger + decimalPart;
        } catch (NumberFormatException e) {
            return numberStr; // Return original string
        }
    }
}
