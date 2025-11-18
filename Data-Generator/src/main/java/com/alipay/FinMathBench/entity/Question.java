/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.alipay.FinMathBench.utils.GsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luojing.wp
 * @version Question.java, v 0.1 2025年11月10日 下午5:36 luojing.wp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    /** Unique key */
    private String            uk;
    /** Question ID */
    private String            questionId;
    /** Question text */
    private String            question;
    /** Ground truth answer */
    private String            groundTruth;
    /** Parameters */
    private String            param;
    /** Formula */
    private Formula           formula;
    /** Normalized text */
    private String            normal;
    /** Actual parameter display values */
    private List<ActualValue> actualParameterDisplay;
    /** System prompt */
    private String            promptSystem;
    /** User prompt */
    private String            promptUser;
    /** Replacement count for X */
    private Integer           replaceX;

    /**
     * Create a copy of this Question object
     * 
     * @return a new Question object with the same field values
     */
    public Question copy() {
        return Question.builder()
                .uk(this.uk)
                .questionId(this.questionId)
                .question(this.question)
                .groundTruth(this.groundTruth)
                .param(this.param)
                .formula(this.formula == null ? null : formula.copy())
                .normal(this.normal)
                .actualParameterDisplay(
                        actualParameterDisplay == null ? null :
                                actualParameterDisplay.stream().map(ActualValue::copy).collect(Collectors.toList())
                )
                .promptSystem(this.promptSystem)
                .promptUser(this.promptUser)
                .replaceX(this.replaceX)
                .build();
    }

    /**
     * Parse Question from a Map with formula
     *
     * @param map the map containing question data
     * @param formula the formula object
     * @return a Question object parsed from the map
     */
    public static Question fromMap(Map<String, String> map, Formula formula) {
        Question returnVal = Question.builder()
                .questionId(map.get("questionId") != null ? map.get("questionId") : map.get("id"))
                .question(map.get("question"))
                .param(map.get("param"))
                .groundTruth(map.get("groundTruth"))
                .formula(formula)
                .normal(map.get("normal"))
                .uk(map.get("uk"))
                .actualParameterDisplay(map.get("actualParameterDisplay") == null ? null
                        : GsonUtils.stringToList(map.get("actualParameterDisplay"), ActualValue.class))
                .promptSystem(map.get("promptSystem"))
                .promptUser(map.get("promptUser"))
                .build();
        return returnVal;
    }

    /**
     * Parse Question from a LinkedHashMap
     *
     * @param map the LinkedHashMap containing question data
     * @return a Question object parsed from the map
     */
    public static Question fromMap(LinkedHashMap<String, String> map) {
        return Question.builder()
                .questionId(map.get("questionId") != null ? map.get("questionId") : map.get("id"))
                .question(map.get("question"))
                .groundTruth(map.get("groundTruth"))
                .param(map.get("param"))
                .formula(Formula.fromBase(map))
                .normal(map.get("normal"))
                .uk(map.get("uk"))
                .actualParameterDisplay(map.get("actualParameterDisplay") == null ? null
                        : GsonUtils.stringToList(map.get("actualParameterDisplay"), ActualValue.class))
                .promptSystem(map.get("promptSystem"))
                .promptUser(map.get("promptUser"))
                .build();
    }

    /**
     * Convert Question to a LinkedHashMap
     *
     * @return a LinkedHashMap representation of this Question
     */
    public LinkedHashMap<String, String> toMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("questionId", questionId == null ? UUID.randomUUID().toString().replace("-", "") : questionId);
        map.put("question", question == null ? "null" : question);
        map.put("param", param == null ? "null" : param);
        map.put("groundTruth", groundTruth == null ? "null" : groundTruth);
        map.putAll(formula.toMap());
        map.put("normal", normal == null ? "null" : normal);
        map.put("uk", uk == null ? "null" : uk);
        map.put("actualParameterDisplay", actualParameterDisplay == null ? "null" : GsonUtils.toString(actualParameterDisplay));
        map.put("promptSystem", promptSystem == null ? null : promptSystem);
        map.put("promptUser", promptUser == null ? null : promptUser);
        return map;
    }
}
