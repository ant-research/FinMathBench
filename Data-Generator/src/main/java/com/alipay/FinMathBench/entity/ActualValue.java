/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.alipay.FinMathBench.utils.NumberFormatter;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author luojing.wp
 * @version ActualValue.java, v 0.1 2025年11月10日 下午5:35 luojing.wp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualValue {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** Parameter name */
    private String       name;
    /** Parameter symbol */
    private String       symbol;
    /** Unit */
    private String       unit;
    // 展示值
    // Display value
    private String       displayValue;
    // 展示值
    // Display values
    private List<String> displayValueList;

    // NUM-标量/LIST-列表
    // NUM-scalar/LIST-list
    /** Type: NUM for scalar, LIST for list */
    private String       type;
    // type=NUM的时候有值
    // Value when type is NUM
    private Number       number;
    // type=LIST的时候有值
    // Values when type is LIST
    private List<Number> list;
    /** Values used in question */
    private List<String> inQuestion;
    /** Mask flag */
    private boolean      mask;
    // 列表的mask下标
    // Mask index for list
    private int          ridx = -1;
    // 标识最终求解的变量（合成过程中使用）
    // Flag indicating the final variable to solve (used in composition process)
    private boolean      finAsk;
    // 对应的定义（因为合成过程中可能出现重名的字段合成到一个问题，所以需要在一开始确定引用关系）
    // Corresponding definition (because fields with the same name may be combined into one question during composition, it is necessary to determine the reference relationship at the beginning)
    private FormulaField field;

    /**
     * Get the unit, return empty string if unit is blank or "无"
     * 
     * @return the unit string
     */
    public String getUnit() {
        return (StringUtils.isBlank(unit) || "无".equals(unit) ? "" : unit);
    }

    /**
     * Check if the display value is in the question string
     *
     * @param questionStr the question string to check
     * @return true if the display value is in the question, false otherwise
     */
    public boolean judgeDisplayInQuestion(String questionStr) {
        if (StringUtils.isBlank(this.getUnit())) {
            if ("NUM".equals(this.getType())) {
                if (this.isMask()) {
                    return true;
                }
                String valueStr = this.getDisplayValue();
                if (questionStr.contains(valueStr + "%")) {
                    return false;
                }
                boolean flag1 = questionStr.contains(valueStr);
                if (flag1) {
                    this.setInQuestion(Lists.newArrayList(valueStr));
                }
                String numStr2 = NumberFormatter.formatNumber(valueStr);
                boolean flag2 = questionStr.contains(numStr2);
                if (flag2) {
                    this.setInQuestion(Lists.newArrayList(numStr2));
                }
                String numStr3 = regularDouble(
                        this.getNumber().doubleValue() * 100,
                        this.displayValue.contains(".") ? this.displayValue.split("\\.")[1].length() : 0
                );
                boolean flag3 = questionStr.contains(numStr3);
                if (flag3) {
                    this.setInQuestion(Lists.newArrayList(numStr3));
                }
                String numStr4 = NumberFormatter.formatNumber(numStr3);
                boolean flag4 = questionStr.contains(numStr4);
                if (flag4) {
                    this.setInQuestion(Lists.newArrayList(numStr4));
                }
                return flag1 || flag2 || flag3 || flag4;
            } else if ("LIST".equals(this.getType())) {
                List<String> inQuestionList = new ArrayList<>();
                boolean allCondContains = true;
                for (int i = 0; i < this.getList().size(); i++) {
                    if (this.isMask() && ridx == i) {
                        inQuestionList.add(null);
                        continue;
                    }
                    Number tmp = this.getList().get(i);
                    String valueStr = this.displayValueList.get(i);
                    if (questionStr.contains(valueStr + "%")) {
                        return false;
                    }
                    boolean flag1 = questionStr.contains(valueStr);
                    if (flag1) {
                        inQuestionList.add(valueStr);
                    }
                    String numStr2 = NumberFormatter.formatNumber(valueStr);
                    boolean flag2 = questionStr.contains(numStr2);
                    if (!flag1 && flag2) {
                        inQuestionList.add(numStr2);
                    }
                    String numStr3 = regularDouble(tmp.doubleValue() * 100,
                            valueStr.contains(".") ? valueStr.split("\\.")[1].length() : 0
                    );
                    boolean flag3 = questionStr.contains(numStr3);
                    if (!flag1 && !flag2 && flag3) {
                        inQuestionList.add(numStr3);
                    }
                    String numStr4 = NumberFormatter.formatNumber(numStr3);
                    boolean flag4 = questionStr.contains(numStr4);
                    if (!flag1 && !flag2 && !flag3 && flag4) {
                        inQuestionList.add(numStr4);
                    }
                    allCondContains = allCondContains && (flag1 || flag2 || flag3 || flag4);
                }
                if (inQuestionList.size() != this.getDisplayValueList().size()) {
                    return false;
                }
                this.setInQuestion(inQuestionList);
                return allCondContains;
            } else {
                throw new RuntimeException("未知的参数类型");
            }
        } else {
            if ("NUM".equals(this.getType())) {
                if (this.isMask()) {
                    return true;
                }
                String numStr = this.getDisplayValue();
                boolean flag1 = questionStr.contains(numStr);
                if (flag1) {
                    this.setInQuestion(Lists.newArrayList(numStr));
                }
                String numStr2 = NumberFormatter.formatNumber(numStr);
                boolean flag2 = questionStr.contains(numStr2);
                if (flag2) {
                    this.setInQuestion(Lists.newArrayList(numStr2));
                }
                return questionStr.contains(numStr) || questionStr.contains(numStr2);
            } else if ("LIST".equals(this.getType())) {
                List<String> inQuestionList = new ArrayList<>();
                boolean allCondContains = true;
                for (int i = 0; i < this.getList().size(); i++) {
                    if (this.isMask() && ridx == i) {
                        inQuestionList.add(null);
                        continue;
                    }
                    String numStr = this.displayValueList.get(i);
                    boolean flag1 = questionStr.contains(numStr);
                    if (flag1) {
                        inQuestionList.add(numStr);
                    }
                    boolean flag2 = true;
                    if (!flag1) {
                        String numStr2 = NumberFormatter.formatNumber(numStr);
                        flag2 = questionStr.contains(numStr2);
                        if (flag2) {
                            inQuestionList.add(numStr2);
                        }
                    }
                    allCondContains = allCondContains & (flag1 || flag2);
                }
                if (inQuestionList.size() != this.getDisplayValueList().size()) {
                    return false;
                }
                this.setInQuestion(inQuestionList);
                return allCondContains;
            } else {
                throw new RuntimeException("未知的参数类型");
            }
        }
    }

    /**
     * Get the value as Object
     * 
     * @return the value as Object
     */
    public Object getValue() {
        if ("LIST".equalsIgnoreCase(type)) {
            List<Double> doubles = new ArrayList<>();
            list.forEach(x -> doubles.add(x.doubleValue()));
            return doubles;
        } else {
            return number.doubleValue();
        }
    }

    /**
     * Convert to condition string
     *
     * @return the condition string
     */
    public String toCond() {
        if ("NUM".equals(this.type)) {
            return this.name + "是" + displayValue + getUnit();
        } else if ("LIST".equals(this.type)) {
            String textVal = this.displayValueList.stream().map(x -> x + getUnit()).collect(Collectors.toList()).toString();
            String text = this.name + "的列表是:" + textVal;
            return text;
        } else {
            throw new RuntimeException("invalid type");
        }
    }

    /**
     * Convert to question pair
     *
     * @return a Pair containing the condition text and the target question
     */
    public Pair<String, String> toQuestion() {
        if ("NUM".equals(this.type)) {
            return Pair.of(null, this.name + "是多少" + ("%".equals(this.getUnit()) ? "" : this.getUnit()) + "?");
        } else if ("LIST".equals(this.type)) {
            int rIdx = SECURE_RANDOM.nextInt(this.list.size());
            this.ridx = rIdx;
            String target = this.name + "的第" + (rIdx + 1) + "个值是多少" + ("%".equals(this.getUnit()) ? ":" : this.getUnit()) + "?";
            List<String> textVal = new ArrayList<>();
            for (int j = 0; j < this.displayValueList.size(); j++) {
                if (j == rIdx) {
                    textVal.add("未知");
                    continue;
                }
                textVal.add(displayValueList.get(j) + this.getUnit());
            }
            String text = this.name + "的列表是:" + textVal;

            // 把求解的值赋给number和displayValue
            this.number = list.get(rIdx);
            this.displayValue = this.displayValueList.get(rIdx);

            return Pair.of(text, target);
        } else {
            throw new RuntimeException("invalid type");
        }
    }

    /**
     * Set value with rounding
     *
     * @param number the number value
     * @param roundSize the number of decimal places
     */
    public void setValue(Number number, int roundSize) {
        String valStr = regularDouble(number.doubleValue(), roundSize);
        if (valStr.contains(".")) {
            number = Double.parseDouble(valStr);
            this.displayValue = regularDouble(number, roundSize);
        } else {
            number = Long.parseLong(valStr);
            this.displayValue = number + "";
        }
        this.number = number;
    }

    /**
     * Create a copy of this ActualValue object
     * 
     * @return a new ActualValue object with the same field values
     */
    public ActualValue copy() {
        return ActualValue.builder()
                .name(this.name)
                .symbol(this.symbol)
                .unit(this.unit)
                .displayValue(this.displayValue)
                .displayValueList(this.displayValueList == null ? null : this.displayValueList)
                .type(this.type)
                .number(this.number)
                .list(this.list == null ? null : this.list)
                .inQuestion(this.inQuestion == null ? null : this.inQuestion)
                .mask(this.mask)
                .ridx(this.ridx)
                .finAsk(this.finAsk)
                .field(this.field == null ? null : this.field.copy())
                .build();
    }

    /**
     * Constructor for numeric type
     *
     * @param name the parameter name
     * @param symbol the parameter symbol
     * @param number the number value
     * @param unit the unit
     * @param roundSize the number of decimal places
     */
    public ActualValue(String name, String symbol, Number number, String unit, int roundSize) {
        setValue(number, roundSize);
        this.name = name;
        this.symbol = symbol;
        this.unit = unit;
        this.type = "NUM";
    }

    /**
     * Constructor for list type
     *
     * @param name the parameter name
     * @param symbol the parameter symbol
     * @param list the list of numbers
     * @param unit the unit
     * @param roundSize the number of decimal places
     */
    public ActualValue(String name, String symbol, List<Number> list, String unit, int roundSize) {
        List<String> displayTmp = new ArrayList<>();
        this.displayValueList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Number num = list.get(i);
            String valStr = regularDouble(num.doubleValue(), roundSize);
            if (valStr.contains(".")) {
                num = Double.parseDouble(valStr);
                String val = regularDouble(num, roundSize);
                displayTmp.add(val);
                this.displayValueList.add(val);
            } else {
                num = Long.parseLong(valStr);
                displayTmp.add(num + "");
                this.displayValueList.add(num + "");
            }
            list.set(i, num);
        }
        this.displayValue = displayTmp.toString();
        this.name = name;
        this.symbol = symbol;
        this.list = list;
        this.unit = unit;
        this.type = "LIST";
    }

    /**
     * Regularize double number
     *
     * @param num the number to regularize
     * @param roundSize the number of decimal places
     * @return the regularized double string
     */
    public static String regularDouble(Number num, int roundSize) {
        String resultStr = new BigDecimal(num.doubleValue()).setScale(roundSize, RoundingMode.HALF_DOWN).toPlainString();
        if (resultStr.contains(".")) {
            while (resultStr.endsWith("0")) {
                resultStr = resultStr.substring(0, resultStr.length() - 1);
            }
        }
        if (resultStr.endsWith(".")) {
            resultStr = resultStr.substring(0, resultStr.length() - 1);
        }
        return resultStr;
    }
}
