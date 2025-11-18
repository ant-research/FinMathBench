/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.alipay.FinMathBench.config.Config;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author luojing
 * @version FormulaField.java, v 0.1 2025/4/1 20:30
 * @Description Formula field representation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormulaField {
    private static final Random      RANDOM                  = new SecureRandom();
    private static final Set<String> WHITE_LIST_UNIT_INTEGER = Sets.newHashSet("年", "元", "元/吨", "点", "手");
    /** Field name */
    private              String      name;
    /** Field symbol */
    private              String      symbol;
    /** Unit */
    private              String      unit;
    /** Minimum value */
    private              Double      minValue;
    /** Maximum value */
    private              Double      maxValue;
    @SerializedName(value = "isInteger")
    /** Whether the value is integer */
    private              Boolean     integer;
    /** Whether to include left boundary */
    private              Boolean     containLeft;
    /** Whether to include right boundary */
    private              Boolean     containRight;
    // Solvable flag
    /** Whether the field is solvable */
    private              Boolean     solvable;
    // Normal calculation content
    @SerializedName(value = "isNormalCal")
    /** Whether it's a normal calculation */
    private              Boolean     normal;
    // NUM -> scalar; PROCESS -> calculation process
    /** Field type: NUM for scalar, PROCESS for calculation process */
    private              String      type;
    // For non-scalar variables, use Formula to represent the calculation process
    /** Sub-formula for non-scalar variables */
    private              Formula     subFormula;
    // List type variable
    @SerializedName(value = "isListType")
    /** Whether it's a list type */
    private              boolean     listType;
    /** Minimum list size */
    private              Integer     listSizeMin;
    /** Maximum list size */
    private              Integer     listSizeMax;
    /** Size as parameter */
    private              String      sizeAs;
    /** Sum as parameter */
    private              String      sumAs;

    /**
     * Constructor with name and symbol
     *
     * @param name the field name
     * @param symbol the field symbol
     */
    public FormulaField(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    /**
     * Create a copy of this FormulaField object
     * 
     * @return a new FormulaField object with the same field values
     */
    public FormulaField copy() {
        return FormulaField.builder()
                .name(this.name)
                .symbol(this.symbol)
                .unit(this.unit)
                .minValue(this.minValue)
                .maxValue(this.maxValue)
                .integer(this.integer)
                .containLeft(this.containLeft)
                .containRight(this.containRight)
                .solvable(this.solvable)
                .normal(this.normal)
                .type(this.type)
                .subFormula(this.subFormula)
                .listType(this.listType)
                .listSizeMin(this.listSizeMin)
                .listSizeMax(this.listSizeMax)
                .sizeAs(this.sizeAs)
                .sumAs(this.sumAs)
                .build();
    }

    /**
     * Generate prompt text for this field
     * 
     * @return a string representation of the field prompt
     */
    public String toPrompt() {
        String range = (getContainLeft() ? "[" : "(") + getMinValue() + "," + getMaxValue() + (getContainRight() ? "]" : ")");
        String line = "参数名:" + getName() +
                ",符号:" + getSymbol() +
                ",是否整数:" + getInteger() +
                ",单位:" + (StringUtils.isBlank(getUnit()) ? "无" : getUnit()) +
                ",取值范围:" + range;
        return line;
    }

    /**
     * Check if a Double value is within the defined range
     *
     * @param value the value to check
     * @return true if the value is within range, false otherwise
     */
    public boolean withinRange(Double value) {
        if (value == null) {
            return false;
        }
        BigDecimal toVal = new BigDecimal(value);
        BigDecimal left = new BigDecimal(minValue);
        BigDecimal right = new BigDecimal(maxValue);
        boolean leftSatisfy = containLeft ? toVal.compareTo(left) >= 0 : toVal.compareTo(left) > 0;
        boolean rightSatisfy = containRight ? toVal.compareTo(right) <= 0 : toVal.compareTo(right) < 0;
        return leftSatisfy && rightSatisfy;
    }

    /**
     * Check if a String value is within the defined range
     *
     * @param value the value to check
     * @return true if the value is within range, false otherwise
     */
    public boolean withinRange(String value) {
        if (value == null) {
            return false;
        }
        BigDecimal toVal = new BigDecimal(value);
        BigDecimal left = new BigDecimal(minValue);
        BigDecimal right = new BigDecimal(maxValue);
        boolean leftSatisfy = containLeft ? toVal.compareTo(left) >= 0 : toVal.compareTo(left) > 0;
        boolean rightSatisfy = containRight ? toVal.compareTo(right) <= 0 : toVal.compareTo(right) < 0;
        return leftSatisfy && rightSatisfy;
    }

    /**
     * Generate a list of random values
     *
     * @param size the list size
     * @param sum the sum of all values
     * @return a list of value-display pairs
     */
    public List<Pair<String, Number>> randomValueList(Integer size, Double sum) {
        List<Pair<String, Number>> ans = new ArrayList<>();
        if (!isListType()) {
            return ans;
        }
        if (size == null) {
            size = listSizeMin + RANDOM.nextInt(listSizeMax - listSizeMin) + 1;
        }
        Set<String> hasMet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            Pair<String, Number> val;
            if (i == size - 1 && sum != null) {
                Double nextVal = sum - ans.stream().mapToDouble(x -> x.getRight().doubleValue()).sum();
                String valueStr = new BigDecimal(nextVal).setScale(Config.ROUND_SIZE, RoundingMode.HALF_DOWN).toPlainString();
                if (integer || WHITE_LIST_UNIT_INTEGER.contains(unit)) {
                    val = Pair.of(valueStr, nextVal.longValue());
                } else {
                    val = Pair.of(valueStr, Double.parseDouble(valueStr));
                }
            } else {
                val = randomValue();
            }
            while (hasMet.contains(val.getLeft()) && !val.getLeft().equals("0")) {
                // 确保取不同的值
                val = randomValue();
            }
            hasMet.add(val.getLeft());
            ans.add(val);
        }
        return ans;
    }

    /**
     * Generate a random value within the defined range
     *
     * @return a pair of display string and number value
     */
    public Pair<String, Number> randomValue() {
        if (new BigDecimal(minValue).compareTo(new BigDecimal(maxValue)) == 0) {
            return Pair.of(new BigDecimal(maxValue).setScale(Config.ROUND_SIZE, RoundingMode.HALF_DOWN).toPlainString(), minValue);
        }
        if (integer || WHITE_LIST_UNIT_INTEGER.contains(unit)) {
            int value = minValue.intValue() + RANDOM.nextInt(maxValue.intValue() - minValue.intValue() + (containRight ? 1 : 0));
            while (!withinRange(value + "")) {
                value = minValue.intValue() + RANDOM.nextInt(maxValue.intValue() - minValue.intValue() + (containRight ? 1 : 0));
            }
            return Pair.of(value + "", (long) value);
        } else {
            double value = minValue + RANDOM.nextDouble() * (maxValue - minValue);
            String valueStr = new BigDecimal(value).setScale(Config.ROUND_SIZE, RoundingMode.HALF_DOWN).toPlainString();
            while (!withinRange(valueStr)) {
                value = minValue + RANDOM.nextDouble() * (maxValue - minValue);
                valueStr = new BigDecimal(value).setScale(Config.ROUND_SIZE, RoundingMode.HALF_DOWN).toPlainString();
            }
            return Pair.of(valueStr, Double.parseDouble(valueStr));
        }
    }
}
