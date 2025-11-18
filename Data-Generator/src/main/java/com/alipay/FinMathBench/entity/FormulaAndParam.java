/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.entity;

import java.util.List;

import com.alipay.FinMathBench.utils.GsonUtils;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luojing
 * @version FormulaAndParam.java, v 0.1 2025/4/1 20:30
 * @Description Formula and parameter representation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormulaAndParam {
    /** List of formulas */
    private List<Formula> formula;
    
    /** List of parameters */
    private List<FormulaField> paramList;
    
    /** List of calculable information */
    private List<CancalInfo> canCal;
    
    /** List of constraints */
    private List<String> constraint;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancalInfo {
        /** List element index */
        private Integer listElementIndex;
        /** Calculable parameter symbol */
        private String  canCalParamSymbol;
        /** Is list type flag */
        private Boolean isListType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Formula {
        /** Formula expression */
        private String  expression;
        @SerializedName(value = "isBasic")
        /** Is basic formula flag */
        private Boolean basic;
        /** Result value */
        private String  result;
    }

    /**
     * Create FormulaAndParam from string
     *
     * @param str the string representation
     * @return a FormulaAndParam object
     */
    public static FormulaAndParam fromStr(String str) {
        FormulaAndParam formulaAndParam = GsonUtils.toObject(FormulaAndParam.class, str);
        return formulaAndParam;
    }
}