/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.entity;

import java.util.Arrays;

/**
 * @author luojing.wp
 * @version JoinOperator.java, v 0.1 2025年11月10日 下午5:58 luojing.wp
 */
public enum JoinOperator {
    /** Cumulative Sum */
    CS,
    /** Cumulative Product */
    CP,
    /** Maximum */
    MAX,
    /** Minimum */
    MIN;

    /**
     * Get JoinOperator by name
     *
     * @param name the name of the operator
     * @return the JoinOperator with the specified name, or null if not found
     */
    public static JoinOperator getByName(String name) {
        return Arrays.stream(values()).filter(x -> x.name().equals(name)).findFirst().orElse(null);
    }
}
