/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alipay.FinMathBench.config.Config;
import com.alipay.FinMathBench.utils.ExpRunner;
import com.alipay.FinMathBench.utils.GsonUtils;
import com.alipay.FinMathBench.utils.NumberFormatter;
import com.alipay.FinMathBench.utils.ResourceReader;
import com.alipay.FinMathBench.utils.TimeUtils;
import com.alipay.FinMathBench.utils.llm.LLMInvoker;
import com.google.common.reflect.TypeToken;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author luojing
 * @version Formula.java, v 0.1 2025/4/1 20:30
 * @Description Mathematical formula representation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Formula {
    private static final SecureRandom              SECURE_RANDOM = new SecureRandom();
    /**
     * Basic formula flag
     */
    private              Boolean                   basic;
    /**
     * Formula ID
     */
    private              String                    formulaId;
    /**
     * Formula category
     */
    private              String                    category;
    /**
     * Formula name
     */
    private              String                    name;
    /**
     * List of parameters
     */
    private              List<FormulaField>        parameterList;
    /**
     * Result field
     */
    private              FormulaField              result;
    /**
     * Chinese text description
     */
    private              String                    textZhcn;
    /**
     * English text description
     */
    private              String                    textEn;
    /**
     * Groovy expression
     */
    private              String                    expressionGroovy;
    /**
     * List of actual parameter values
     */
    private              List<Map<String, Object>> actualParameterList;
    /**
     * Difficulty level
     */
    private              String                    level;
    /**
     * Expression validation flag
     */
    private              boolean                   expPass;
    /**
     * Source
     */
    private              String                    source;
    // 累加/累乘/求最大/求最小
    // Cumulative sum/cumulative product/max/min
    private              JoinOperator              joinOperator;

    /**
     * Create a copy of this Formula object
     *
     * @return a new Formula object with the same field values
     */
    public Formula copy() {
        return Formula.builder()
                .basic(this.basic)
                .formulaId(this.formulaId)
                .category(this.category)
                .name(this.name)
                .parameterList(
                        this.parameterList == null ? null : this.parameterList.stream().map(x -> x.copy()).collect(Collectors.toList()))
                .result(this.result == null ? null : this.result.copy())
                .textZhcn(this.textZhcn)
                .textEn(this.textEn)
                .expressionGroovy(this.expressionGroovy)
                .actualParameterList(actualParameterList == null ? null
                        : actualParameterList.stream().map(x -> new HashMap<>(x)).collect(Collectors.toList()))
                .level(this.level)
                .expPass(this.expPass)
                .source(this.source)
                .joinOperator(this.joinOperator)
                .build();
    }

    /**
     * Get the Chinese prompt text
     *
     * @return the Chinese text description
     */
    public String getPromptTextZhcn() {
        if (!isComplexInner(this)) {
            return this.textZhcn;
        } else {
            List<String> list = getTextZhcnCurr(this);
            return StringUtils.join(list, "\n");
        }
    }

    /**
     * Get the recursive parameter list
     *
     * @return the list of all parameters including nested ones
     */
    public List<FormulaField> getParameterListRecur() {
        if (!isComplexInner(this)) {
            return this.parameterList;
        } else {
            Set<FormulaField> fields = new HashSet<>();
            getNumFields(this, fields);
            return new ArrayList<>(fields);
        }
    }

    /**
     * Constraints between field values
     */
    @SerializedName("constraint")
    private List<String> constraintList;
    // 合成信息，仅仅合成的公式有这个字段
    // Composition information, only composite formulas have this field
    private String       replaceInfo;

    /**
     * Convert Formula to a LinkedHashMap
     *
     * @return a LinkedHashMap representation of this Formula
     */
    public LinkedHashMap<String, String> toMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("category", category == null ? "null" : category);
        map.put("name", name == null ? "null" : name);
        map.put("parameterList", CollectionUtils.isEmpty(parameterList) ? "null" : GsonUtils.toString(parameterList));
        map.put("result", result == null ? "null" : GsonUtils.toString(result));
        map.put("textZhcn", textZhcn == null ? "null" : getPromptTextZhcn());
        map.put("textEn", textEn == null ? "null" : textEn);
        map.put("expressionGroovy", expressionGroovy == null ? "null" : expressionGroovy);
        map.put("actualParameterList", CollectionUtils.isEmpty(actualParameterList) ? "null" : GsonUtils.toString(actualParameterList));
        map.put("level", level == null ? "null" : level);
        map.put("expPass", expPass + "");
        map.put("source", source);
        map.put("formulaId", formulaId);
        if (StringUtils.isNotBlank(replaceInfo)) {
            map.put("replaceInfo", replaceInfo);
        } else {
            map.put("replaceInfo", "");
        }
        if (CollectionUtils.isNotEmpty(constraintList)) {
            map.put("constraint", GsonUtils.toString(constraintList));
        } else {
            map.put("constraint", "");
        }
        return map;
    }

    /**
     * Create Formula from base map
     *
     * @param map the map containing formula data
     * @return a Formula object parsed from the map
     */
    public static Formula fromBase(Map<String, String> map) {
        Formula answer = new Formula();
        answer.setCategory(map.get("category"));
        answer.setName(map.get("name"));
        answer.setParameterList(GsonUtils.toObject(new TypeToken<List<FormulaField>>() {
        }.getType(), map.get("parameterList")));
        answer.setResult(GsonUtils.toObject(FormulaField.class, map.get("result")));
        answer.setTextZhcn(map.get("textZhcn"));
        answer.setTextEn(map.get("textEn"));
        answer.setExpressionGroovy(map.get("expressionGroovy"));
        answer.setActualParameterList(GsonUtils.toObject(new TypeToken<List<Map<String, Object>>>() {
        }.getType(), map.get("actualParameterList")));
        answer.setLevel(map.get("level"));
        answer.setExpPass(Boolean.parseBoolean(map.get("expPass")));
        answer.setSource(map.get("source"));
        if (map.get("formulaId") != null) {
            answer.setFormulaId(map.get("formulaId"));
        } else {
            answer.setFormulaId(map.get("id"));
        }
        answer.setReplaceInfo(map.get("replaceInfo"));
        if (StringUtils.isNotBlank(map.get("constraint"))) {
            answer.setConstraintList(GsonUtils.stringToList(map.get("constraint"), String.class));
        }
        return answer;
    }

    /**
     * Create Formula from manual V2 format
     *
     * @param map         the map containing formula data
     * @param textZhcnKey the key for Chinese text
     * @return a list of Formula objects
     */
    public static List<Formula> fromManualV2(Map<String, String> map, String textZhcnKey) {
        List<Formula> answer = new ArrayList<>();
        FormulaAndParam formulaAndParam = null;
        try {
            formulaAndParam = FormulaAndParam.fromStr(map.get("formulaAndParam"));
            formulaAndParam.getParamList().sort(Comparator.comparing(a -> a.getSymbol().length()));
            Collections.reverse(formulaAndParam.getParamList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        Set<String> solvableSymbolSet = formulaAndParam.getCanCal().stream()
                .map(x -> x.getCanCalParamSymbol())
                .collect(Collectors.toSet());
        for (FormulaAndParam.Formula tmp : formulaAndParam.getFormula()) {
            String textZhcn = tmp.getExpression();
            for (FormulaField formulaField : formulaAndParam.getParamList()) {
                textZhcn = textZhcn.replace(formulaField.getSymbol(), formulaField.getName());
            }
            if (StringUtils.isNotBlank(textZhcnKey)) {
                textZhcn = map.get(textZhcnKey);
            }
            Formula formula = Formula.builder()
                    .category(map.get("科目"))
                    .name(map.get("公式名称（中文）"))
                    .level(map.get("难度"))
                    .textZhcn(textZhcn)
                    .textEn(tmp.getExpression())
                    .expressionGroovy(tmp.getExpression())
                    .basic(tmp.getBasic())
                    .constraintList(formulaAndParam.getConstraint())
                    .build();
            String resultSymbol = tmp.getExpression().split("=")[0].trim();
            if ("L2".equals(map.get("难度"))) {
                resultSymbol = tmp.getResult();
            }
            final String resultSymbolFin = resultSymbol;
            FormulaField resultField = formulaAndParam.getParamList().stream()
                    .filter(x -> x.getSymbol().equals(resultSymbolFin))
                    .findFirst()
                    .orElse(null);
            if (resultField == null) {
                throw new RuntimeException("公式格式不正确: " + formula);
            }
            formula.setResult(resultField);
            List<FormulaField> paramList = formulaAndParam.getParamList().stream()
                    .filter(x -> !x.getSymbol().equals(resultField.getSymbol()))
                    .collect(Collectors.toList());
            resultField.setSolvable(solvableSymbolSet.contains(resultField.getSymbol()));
            paramList.forEach(x -> x.setSolvable(solvableSymbolSet.contains(x.getSymbol())));
            formula.setParameterList(paramList);
            answer.add(formula);

            // start:处理累加/累乘/max/min
            // 先对多个分解公式进行优先级排序
            if ("true".equals(System.getProperty("formula.L2.parse"))) {
                if (StringUtils.isNotBlank(map.get("def"))) {
                    Pattern pattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z_0-9]*\\b");
                    String def = map.get("def");
                    String[] subFormulaArray = def.split(";");
                    List<List<String>> lineVarList = new ArrayList<>();
                    Map<List<String>, String> mapSplit2Array = new HashMap<>();
                    for (String line : subFormulaArray) {
                        Matcher matcher = pattern.matcher(line);
                        // 用来存储提取出的变量名
                        List<String> variables = new ArrayList<>();
                        // 提取变量名
                        while (matcher.find()) {
                            String variable = matcher.group();
                            // 排除关键字，比如：数字或其他符号（可按需扩展）
                            if (!variable.matches("\\d+")) {
                                variables.add(variable);
                            }
                        }
                        lineVarList.add(variables);
                        mapSplit2Array.put(variables, line);
                    }
                    List<Pair<String, List<String>>> sortResult = new ArrayList<>();
                    Set<String> knownVars = new HashSet<>();
                    List<Integer> hasDoneIdx = new ArrayList<>();
                    while (sortResult.size() < subFormulaArray.length) {
                        for (int i = 0; i < lineVarList.size(); i++) {
                            if (hasDoneIdx.contains(i)) {
                                continue;
                            }
                            List<String> tmp1 = lineVarList.get(i);
                            List<Boolean> depList = new ArrayList<>();
                            for (int j = 0; j < lineVarList.size(); j++) {
                                if (i == j) {
                                    continue;
                                }
                                List<String> tmp2 = lineVarList.get(j);
                                boolean depOther = false;
                                for (String item : tmp1.subList(1, tmp1.size())) {
                                    if (knownVars.contains(item)) {
                                        continue;
                                    }
                                    if (tmp2.get(0).equals(item)) {
                                        depOther = true;
                                    }
                                }
                                depList.add(depOther);
                            }
                            if (!depList.contains(true)) {
                                tmp1.forEach(x -> knownVars.add(x));
                                String f = mapSplit2Array.get(tmp1);
                                sortResult.add(Pair.of(f, tmp1));
                                hasDoneIdx.add(i);
                            }
                        }
                    }
                    Map<String, Formula> subFormulaMap = new HashMap<>();
                    Formula subFormula = null;
                    for (Pair<String, List<String>> pair : sortResult) {
                        subFormula = new Formula();
                        List<FormulaField> newFields = new ArrayList<>();
                        List<String> vars = pair.getRight().subList(1, pair.getRight().size());
                        for (String var : vars) {
                            FormulaField existField = null;
                            try {
                                existField = newFields.stream()
                                        .filter(x -> x.getSymbol().equals(var)).findFirst()
                                        .orElse(null);
                            } catch (Exception e) {
                                return null;
                            }
                            if (existField != null) {
                                continue;
                            }
                            FormulaField field = new FormulaField();
                            if (var.startsWith("T_CS_") || var.startsWith("T_CP_") ||
                                    var.startsWith("T_MAX_") || var.startsWith("T_MIN_")) {
                                // 累加
                                Formula fieldFormula = subFormulaMap.get(var);
                                field.setSymbol(var);
                                field.setType("PROCESS");
                                field.setSubFormula(fieldFormula);
                                fieldFormula.setJoinOperator(JoinOperator.getByName(
                                        var.split("_")[0] + "_" +
                                                var.split("_")[1]
                                ));
                                newFields.add(field);
                            } else {
                                field = formula.getParameterList().stream()
                                        .filter(x -> x.getSymbol().equals(var))
                                        .findFirst().orElse(null);
                                newFields.add(field);
                            }
                        }
                        subFormula.setExpressionGroovy(pair.getLeft().trim().split("=")[1].trim());
                        subFormula.setParameterList(newFields);
                        subFormulaMap.put(pair.getRight().get(0), subFormula);
                    }
                    formula.setParameterList(subFormula.getParameterList());
                }
                // end:处理累加/累乘/max/min
            }
        }
        return answer;
    }

    /**
     * Check if the formula and variables are complete
     *
     * @return true if the formula is complete, false otherwise
     */
    public boolean check() {
        for (FormulaField field : parameterList) {
            if (!this.textEn.contains(field.getSymbol())) {
                return false;
            }
        }
        if (!this.textEn.contains(this.result.getSymbol())) {
            return false;
        }
        return true;
    }

    /**
     * Get the current Chinese text list
     *
     * @param formula the formula to process
     * @return a list of Chinese text descriptions
     */
    private static List<String> getTextZhcnCurr(Formula formula) {
        List<String> list = new ArrayList<>();
        Queue<Formula> queue = new LinkedList<>();
        queue.add(formula);
        while (!queue.isEmpty()) {
            Formula cur = queue.poll();
            if (cur.getTextZhcn() != null) {
                list.add(cur.getTextZhcn());
            }
            if (cur.getParameterList() != null) {
                for (FormulaField field : cur.getParameterList()) {
                    if (field.getSubFormula() != null) {
                        queue.add(field.getSubFormula());
                    }
                }
            }
        }
        Collections.reverse(list);
        return list;
    }

    /**
     * Get numeric fields from formula
     *
     * @param formula the formula to process
     * @param fields  the set to store the numeric fields
     */
    private static void getNumFields(Formula formula, Set<FormulaField> fields) {
        if (CollectionUtils.isEmpty(formula.getParameterList())) {
            return;
        }
        for (FormulaField field : formula.getParameterList()) {
            if (field.getSubFormula() != null) {
                getNumFields(field.getSubFormula(), fields);
            } else {
                fields.add(field);
            }
        }
    }

    /**
     * Check if formula is complex with inner formulas
     *
     * @param formula the formula to check
     * @return true if formula is complex, false otherwise
     */
    private static boolean isComplexInner(Formula formula) {
        if (CollectionUtils.isEmpty(formula.getParameterList())) {
            return false;
        }
        for (FormulaField field : formula.getParameterList()) {
            if (field.getSubFormula() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a set of actual parameter values
     *
     * @param count the number of parameter sets to generate
     * @return a list of parameter value maps
     */
    public List<Map<String, Object>> getParamInstanceByCode(int count) {
        // First check if the formula is a complex type
        List<Map<String, Object>> returnValue = new ArrayList<>();
        if (!isComplexInner(this)) {
            int loop = 0;
            while (returnValue.size() < count) {
                if (loop >= 500) {
                    return null;
                }
                try {
                    Map<String, Object> value = new HashMap<>();
                    List<FormulaField> fields = new ArrayList<>(this.parameterList);
                    List<FormulaField> tmp1 = fields.stream()
                            .filter(x -> StringUtils.isBlank(x.getSizeAs()) && StringUtils.isBlank(x.getSumAs()))
                            .collect(Collectors.toList());
                    List<FormulaField> tmp2 = fields.stream()
                            .filter(x -> StringUtils.isNotBlank(x.getSizeAs()) || StringUtils.isNotBlank(x.getSumAs()))
                            .collect(Collectors.toList());
                    Set<String> symbolTmp = tmp1.stream().map(x -> x.getSymbol()).collect(Collectors.toSet());
                    symbolTmp.addAll(tmp2.stream().map(x -> x.getSymbol()).collect(Collectors.toSet()));
                    List<FormulaField> tmp3 = fields.stream()
                            .filter(x -> !symbolTmp.contains(x.getSymbol()))
                            .collect(Collectors.toList());
                    fields.clear();
                    fields.addAll(tmp1);
                    fields.addAll(tmp2);
                    fields.addAll(tmp3);
                    Map<String, FormulaField> fieldMap = new HashMap<>();
                    for (FormulaField field : fields) {
                        fieldMap.put(field.getSymbol(), field);
                    }
                    Iterator<FormulaField> iterator = fields.iterator();
                    boolean meetZero = false;
                    boolean outRang = false;
                    Map<String, Integer> symbolSizeMap = new HashMap<>();
                    while (iterator.hasNext()) {
                        FormulaField field = iterator.next();
                        Object val;
                        if (field.isListType()) {
                            Integer size = null;
                            if (StringUtils.isNotBlank(field.getSizeAs())) {
                                FormulaField sizeLikeF = fieldMap.get((field.getSizeAs()));
                                if (sizeLikeF.isListType()) {
                                    size = symbolSizeMap.get(field.getSizeAs());
                                } else {
                                    size = new BigDecimal(value.get(field.getSizeAs()) + "").intValue();
                                }
                            }
                            Double sum = null;
                            if (StringUtils.isNotBlank(field.getSumAs())) {
                                sum = Double.parseDouble(field.getSumAs());
                            }
                            List<Pair<String, Number>> list = field.randomValueList(size, sum);
                            Pair<String, Number> outRangePair = list.stream()
                                    .filter(x -> !field.withinRange(x.getLeft()))
                                    .findFirst().orElse(null);
                            if (outRangePair != null) {
                                outRang = true;
                                break;
                            }
                            val = list.stream()
                                    .map(x -> field.getInteger() ? x.getRight().longValue() : x.getRight().doubleValue())
                                    .collect(Collectors.toList());
                            symbolSizeMap.put(field.getSymbol(), list.size());
                        } else {
                            Pair<String, Number> valuePair = field.randomValue();
                            if ("0".equals(regularDouble(valuePair.getValue()))) {
                                meetZero = true;
                                break;
                            }
                            if (field.getInteger()) {
                                val = valuePair.getRight().longValue();
                            } else {
                                val = valuePair.getRight().doubleValue();
                            }
                        }
                        value.put(field.getSymbol(), val);
                        iterator.remove();
                    }

                    if (meetZero || outRang) {
                        continue;
                    }

                    boolean allPass = true;
                    if (CollectionUtils.isNotEmpty(this.constraintList)) {
                        for (String exp : this.constraintList) {
                            Boolean pass = (Boolean) ExpRunner.evaluateGroovy(exp, value);
                            if (!Boolean.TRUE.equals(pass)) {
                                allPass = false;
                                break;
                            }
                        }
                    }

                    if (!allPass) {
                        continue;
                    }

                    boolean resultRangePass = false;
                    Double resultValue = ExpRunner.evaluate(getExpressionGroovy().replace(" ", " "), value);
                    if (result.withinRange(resultValue) && !"0".equals(regularDouble(resultValue))) {
                        resultRangePass = true;
                    }

                    if (allPass && resultRangePass) {
                        returnValue.add(value);
                    }
                } finally {
                    loop++;
                }
            }
            this.actualParameterList = returnValue;
            return returnValue;
        } else {
            int loop = 0;
            while (returnValue.size() < count) {
                if (loop >= 100) {
                    return null;
                }
                Set<FormulaField> fields = new HashSet<>();
                getNumFields(this, fields);
                try {
                    Map<String, Object> value = new HashMap<>();
                    List<FormulaField> tmp1 = fields.stream()
                            .filter(x -> StringUtils.isBlank(x.getSizeAs()) && StringUtils.isBlank(x.getSumAs()))
                            .collect(Collectors.toList());
                    List<FormulaField> tmp2 = fields.stream()
                            .filter(x -> StringUtils.isNotBlank(x.getSizeAs()) || StringUtils.isNotBlank(x.getSumAs()))
                            .collect(Collectors.toList());
                    Set<String> symbolTmp = tmp1.stream().map(x -> x.getSymbol()).collect(Collectors.toSet());
                    symbolTmp.addAll(tmp2.stream().map(x -> x.getSymbol()).collect(Collectors.toSet()));
                    List<FormulaField> tmp3 = fields.stream()
                            .filter(x -> !symbolTmp.contains(x.getSymbol()))
                            .collect(Collectors.toList());
                    fields.clear();
                    fields.addAll(tmp1);
                    fields.addAll(tmp2);
                    fields.addAll(tmp3);
                    List<FormulaField> fieldsBak = new ArrayList<>(fields);
                    Iterator<FormulaField> iterator = fields.iterator();
                    boolean meetZero = false;
                    Map<String, Integer> symbolSizeMap = new HashMap<>();
                    while (iterator.hasNext()) {
                        FormulaField field = iterator.next();
                        Object val;
                        if (field.isListType()) {
                            Integer size = null;
                            if (StringUtils.isNotBlank(field.getSizeAs())) {
                                size = symbolSizeMap.get(field.getSizeAs());
                            }
                            Double sum = null;
                            if (StringUtils.isNotBlank(field.getSumAs())) {
                                sum = Double.parseDouble(field.getSumAs());
                            }
                            List<Pair<String, Number>> list = field.randomValueList(size, sum);
                            val = list.stream()
                                    .map(x -> field.getInteger() ? x.getRight().intValue() : x.getRight().doubleValue())
                                    .collect(Collectors.toList());
                            symbolSizeMap.put(field.getSymbol(), list.size());
                        } else {
                            Pair<String, Number> valuePair = field.randomValue();
                            if ("0".equals(regularDouble(valuePair.getValue()))) {
                                meetZero = true;
                                break;
                            }
                            if (field.getInteger()) {
                                val = valuePair.getRight().longValue();
                            } else {
                                val = valuePair.getRight().doubleValue();
                            }
                        }
                        value.put(field.getSymbol(), val);
                        iterator.remove();
                    }

                    if (value.isEmpty()) {
                        continue;
                    }

                    if (meetZero) {
                        continue;
                    }

                    boolean allPass = true;
                    if (CollectionUtils.isNotEmpty(this.constraintList)) {
                        for (String exp : this.constraintList) {
                            Boolean pass = (Boolean) ExpRunner.evaluateGroovy(exp, value);
                            if (!Boolean.TRUE.equals(pass)) {
                                allPass = false;
                                break;
                            }
                        }
                    }

                    if (!allPass) {
                        continue;
                    }

                    boolean resultRangePass = false;
                    Double resultValue = ExpRunner.evaluate(getExpressionGroovy().replace(" ", " "), value);
                    if (result.withinRange(resultValue) && !"0".equals(regularDouble(resultValue))) {
                        resultRangePass = true;
                    }

                    if (allPass && resultRangePass) {
                        returnValue.add(value);
                    }
                } finally {
                    loop++;
                }
            }
            this.actualParameterList = returnValue;
            return returnValue;
        }
    }

    /**
     * Validate groovy expression
     *
     * @return true if the expression is valid, false otherwise
     */
    public boolean checkExp() {
        if (CollectionUtils.isEmpty(this.actualParameterList)) {
            this.expPass = false;
            return false;
        }
        if (StringUtils.isBlank(this.expressionGroovy)) {
            this.expPass = false;
            return false;
        }
        for (Map<String, Object> objectMap : this.actualParameterList) {
            Double result = ExpRunner.evaluate(this.expressionGroovy.replace(" ", " "), objectMap);
            if (result == null) {
                this.expPass = false;
                return false;
            }
        }
        this.expPass = true;
        return true;
    }

    /**
     * Obfuscate answer with error answers
     *
     * @param answer       the correct answer
     * @param errorAnswers the list of error answers
     * @return a map containing the obfuscated answers
     */
    private static Map<String, String> confuse(String answer, List<String> errorAnswers) {
        Map<String, String> ans = new HashMap<>();
        int answerIdx = SECURE_RANDOM.nextInt(errorAnswers.size() + 1);
        List<String> allItems = new ArrayList<>(errorAnswers);
        allItems.add(answerIdx, answer);
        String answerItems = new String(new char[] {(char) ('A' + answerIdx)});
        ans.put("answer", answerItems);
        for (int i = 0; i < allItems.size(); i++) {
            String item = new String(new char[] {(char) ('A' + i)});
            ans.put(item, allItems.get(i));
        }
        return ans;
    }

    /**
     * Regularize integer number
     *
     * @param num the number to regularize
     * @return the regularized integer string
     */
    private String regularInt(Double num) {
        String resultStr = new BigDecimal(num).setScale(0, RoundingMode.HALF_DOWN).toPlainString();
        if (resultStr.contains(".")) {
            return resultStr.split("\\.")[0];
        }
        return resultStr;
    }

    /**
     * Regularize double number
     *
     * @param num       the number to regularize
     * @param roundSize the number of decimal places
     * @return the regularized double string
     */
    public static String regularDouble(Number num, int roundSize) {
        if (num == null) {
            System.out.println(1);
        }
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

    /**
     * Regularize double number with default round size
     *
     * @param num the number to regularize
     * @return the regularized double string
     */
    public static String regularDouble(Number num) {
        return regularDouble(num, Config.ROUND_SIZE);
    }

    /**
     * Get mix items related to input
     *
     * @param input the input string
     * @return a list of mix items
     */
    public List<String> getMixItems(String input) {
        String prompt = ResourceReader.read("prompt/mix_items.txt");
        prompt = prompt.replace("{input}", input);
        String resp = new LLMInvoker().runSingle(Config.LLM, prompt);
        Pattern patternJson = Pattern.compile("```json([\\s\\S]+?)```");
        Matcher matcher = patternJson.matcher(resp);
        if (matcher.find()) {
            resp = matcher.group(1);
        }
        List<String> list = GsonUtils.stringToList(resp, String.class);
        if (list != null && list.size() > 10) {
            list = list.subList(0, 10);
        }
        return list;
    }

    /**
     * Generate a sample question
     *
     * @param actual      list of actual values
     * @param mixItems    list of mix items
     * @param replaceInfo replacement information
     * @param domain      the domain
     * @return a Question object
     */
    public Question getSampleV2(
            List<ActualValue> actual, List<String> mixItems, String replaceInfo,
            String domain) {
        List<ActualValue> inQuestion = actual.stream().filter(x -> !x.isMask()).collect(Collectors.toList());
        ActualValue toAsk = actual.stream().filter(x -> x.isMask()).findFirst().orElse(null);
        if (CollectionUtils.isEmpty(inQuestion) || toAsk == null) {
            throw new RuntimeException("CollectionUtils.isEmpty(inQuestion) || toAsk == null");
        }

        List<String> condList = inQuestion.stream().map(x -> x.toCond()).collect(Collectors.toList());
        Pair<String, String> toAskPair = toAsk.toQuestion();
        String target = toAskPair.getRight();
        if (toAskPair.getLeft() != null) {
            condList.add(toAskPair.getLeft());
        }

        String resultStr = toAsk.getDisplayValue();

        Map<String, String> items = new HashMap<>();
        items.put("groundTruth", resultStr);

        String oneShot = ResourceReader.read("prompt/one_shot.txt");
        String promptSystem = ResourceReader.read("prompt/system.txt");
        promptSystem = promptSystem.replace("{date}", TimeUtils.formatDate(new Date(), "yyyy年MM月dd日"))
                .replace("{domain}", domain)
                .replace("{otherInfo}", StringUtils.join(mixItems, "、"))
                .replace("{format}",
                        SECURE_RANDOM.nextDouble() > 0.5d ? "- 题目中引入表格元素，例如利率表、收益表等，表格使用markdown格式表示\n" : "")
                .replace("{example}", oneShot);
        String promptUser = ResourceReader.read("prompt/get_question.txt");
        promptUser = promptUser
                .replace("{name}", this.getName())
                .replace("{desc}", this.getPromptTextZhcn())
                .replace("{condition}", StringUtils.join(condList, "\n"))
                .replace("{target}", target);

        LLMInvoker llmInvoker = new LLMInvoker();
        llmInvoker.runSingle(Config.LLM, promptSystem, promptUser);
        String resp = llmInvoker.lastContent();
        Pattern patternJson = Pattern.compile("```json([\\s\\S]+?)```");
        Matcher matcher = patternJson.matcher(resp);
        if (matcher.find()) {
            resp = matcher.group(1);
        }
        try {
            Map<String, String> respMap = GsonUtils.toMap(resp);
            String questionStr = respMap.get("question");
            if (StringUtils.isBlank(questionStr)) {
                return null;
            }
            // start：校验，有时候模型会把比例类型的数值自动转换为%
            for (int i = 0; i < actual.size(); i++) {
                ActualValue item = actual.get(i);
                if (!item.judgeDisplayInQuestion(questionStr)) {
                    return null;
                }
            }
            // end：校验，有时候模型会把比例类型的数值自动转换为%

            // start：校验，单位不要随意修改
            for (int i = 0; i < actual.size(); i++) {
                ActualValue item = actual.get(i);
                if (!item.isMask()) {
                    if ("元".equals(item.getUnit())) {
                        if ("NUM".equals(item.getType())) {
                            String numStr = item.getDisplayValue();
                            String numStr2 = NumberFormatter.formatNumber(numStr);
                            if (!questionStr.contains(numStr) && !questionStr.contains(numStr2)) {
                                return null;
                            }
                        } else if ("LIST".equals(item.getType())) {
                            for (int j = 0; j < item.getDisplayValueList().size(); j++) {
                                String numStr = item.getDisplayValueList().get(j);
                                String numStr2 = NumberFormatter.formatNumber(numStr);
                                if (!questionStr.contains(numStr) && !questionStr.contains(numStr2)) {
                                    return null;
                                }
                            }
                        } else {
                            throw new RuntimeException("未知的参数类型");
                        }
                    }
                }
            }
            // end：校验，单位不要随意修改

            // start：校验，结果不要保留位数
            if (questionStr.contains("保留")) {
                return null;
            }
            // end：校验，结果不要保留位数

            // start:将元替换位万元
            for (ActualValue actualValue : actual) {
                if (actualValue.isMask() || !"NUM".equals(actualValue.getType())) {
                    continue;
                }
                String actV = actualValue.getInQuestion().get(0);
                if (!"元".equals(actualValue.getUnit()) || actV.contains(".")) {
                    continue;
                }
                int len1 = questionStr.length();
                String qTmp = questionStr.replace(actV + "元", "");
                int len2 = qTmp.length();
                if (len2 - len1 > actV.length() + 1) {
                    break;
                } else {
                    if (SECURE_RANDOM.nextDouble() > 0.5d) {
                        String newVal = scaleDownBy10000(actV);
                        questionStr = questionStr.replace(actV + "元", newVal + "万元");
                    }
                }
            }
            // end:将元替换位万元

            items.put("question", questionStr);
            Question question = Question.fromMap(items, this);
            question.setPromptSystem(promptSystem);
            question.setPromptUser(promptUser);
            question.setParam(GsonUtils.toString(actual));
            return question;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Scale down a string number by 10000
     *
     * @param input the input string number
     * @return the scaled down string number
     */
    public static String scaleDownBy10000(String input) {
        try {
            // Convert string to BigDecimal
            BigDecimal number = new BigDecimal(input.replace(",", "").replace(" ", ""));

            // Scale down by 10000
            BigDecimal scaledNumber = number.divide(new BigDecimal("10000"));

            // Return the scaled down string
            return scaledNumber.toString();
        } catch (NumberFormatException e) {
            // If input is not a valid number string, throw exception or return error message
            throw new IllegalArgumentException("Input string is not a valid number: " + input, e);
        }
    }
}
