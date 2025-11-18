/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alipay.FinMathBench.config.Config;
import com.alipay.FinMathBench.entity.ActualValue;
import com.alipay.FinMathBench.entity.Formula;
import com.alipay.FinMathBench.entity.FormulaField;
import com.alipay.FinMathBench.entity.Question;
import com.alipay.FinMathBench.utils.ExcelReader;
import com.alipay.FinMathBench.utils.ExcelWriter;
import com.alipay.FinMathBench.utils.ExpRunner;
import com.alipay.FinMathBench.utils.GsonUtils;
import com.alipay.FinMathBench.utils.MapCartesianProduct;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @author luojing
 * @version SampleBuilderTask.java, v 0.1 2025/3/25 13:38
 * @Description
 */
@Slf4j(topic = "consoleLog")
public class QuestionBuilderTask {
    /**
     * Synthesize questions, the first one is the final question to be solved
     *
     * @param finQ         the final question text
     * @param questionList the list of questions to synthesize
     * @return the synthesized question
     */
    private static Question synthesis(String finQ, List<Question> questionList) {
        if (CollectionUtils.isEmpty(questionList) || questionList.size() < 2) {
            // At least two questions are needed for synthesis
            return null;
        }
        Question question0 = questionList.get(0);
        ActualValue toAsk0 = question0.getActualParameterDisplay().stream()
                .filter(x -> x.isFinAsk())
                .findFirst().orElse(null);

        if (!toAsk0.getSymbol().equals(question0.getFormula().getResult().getSymbol())) {
            // Currently there is only one forward calculation formula, so the target of the final question must be result
            return null;
        }

        long q0OtherParamCnt = question0.getActualParameterDisplay().stream()
                .filter(x -> !x.isFinAsk() && !x.isMask()).count();
        if (q0OtherParamCnt < questionList.size() - 1) {
            // The parameters to be replaced in the question are not enough
            return null;
        }

        // start: Find the solution field for each sub-question
        List<Triple<Question, ActualValue, FormulaField>> subQAFList = new ArrayList<>();
        for (Question question : questionList.subList(1, questionList.size())) {
            ActualValue toAsk = question.getActualParameterDisplay().stream()
                    .filter(x -> x.isFinAsk())
                    .findFirst().orElse(null);
            if (toAsk == null || toAsk.getField() == null) {
                return null;
            }
            subQAFList.add(Triple.of(question, toAsk, toAsk.getField()));
        }
        // end: Find the solution field for each sub-question

        // start: Find all replaceable pairs
        List<ActualValue> valueInQ0List = question0.getActualParameterDisplay().stream()
                .filter(x -> !x.isMask() && !x.isFinAsk())
                .filter(x -> x != null && x.getInQuestion().stream().filter(y -> y != null).count() == 1L)
                .collect(Collectors.toList());
        List<Pair<Triple<Question, ActualValue, FormulaField>, ActualValue>> matchList = new ArrayList<>();
        for (Triple<Question, ActualValue, FormulaField> triple : subQAFList) {
            for (ActualValue actualValue : valueInQ0List) {
                FormulaField field = actualValue.getField();
                if (field == null) {
                    continue;
                }

                ActualValue subAsk = triple.getMiddle();
                FormulaField subField = triple.getRight();
                Double subValue = "NUM".equals(subAsk.getType()) ?
                        ((Number) subAsk.getValue()).doubleValue() :
                        ((List<Number>) subAsk.getValue()).get(subAsk.getRidx()).doubleValue();
                // start: Ensure the first question calculation result has the same dimension/rounding/range as the content to be
                // replaced in the second question
                if (StringUtils.isBlank(subField.getUnit()) ||
                        "%".equals(field.getUnit()) ||
                        //                        !subField.getUnit().equals(field.getUnit()) ||
                        !subField.getInteger().equals(field.getInteger()) ||
                        !subField.getSolvable() || !field.getSolvable() ||
                        subValue < field.getMinValue() ||
                        subValue > field.getMaxValue()
                ) {
                    continue;
                }
                // end: Ensure the first question calculation result has the same dimension/rounding/range as the content to be replaced
                // in the second question

                String q2 = question0.getQuestion();
                boolean skip = false;
                for (String s : actualValue.getInQuestion()) {
                    int len1 = q2.length();
                    q2 = q2.replace(s, "");
                    int len2 = q2.length();
                    if (len1 - len2 > s.length() || len1 == len2) {
                        // Indicates that the string appears more than once, cannot be accurately replaced // Did not appear
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }
                matchList.add(Pair.of(triple, actualValue));
            }
        }
        // end: Find all replaceable pairs

        // start: Find questionList.size()-1 pairs from all pairs to cover all subQAFList
        Map<Triple<Question, ActualValue, FormulaField>, List<ActualValue>> map = new HashMap<>();
        matchList.forEach(x -> map.computeIfAbsent(x.getLeft(), y -> new ArrayList<>()).add(x.getRight()));
        if (map.size() != subQAFList.size()) {
            // Not all sub-questions can find a replaceable variable
            return null;
        }
        List<List<Pair<Triple<Question, ActualValue, FormulaField>, ActualValue>>> list =
                MapCartesianProduct.generateProduct(map);
        List<Pair<Triple<Question, ActualValue, FormulaField>, ActualValue>> replacePairList = list.stream()
                .filter(x -> x.stream().map(y -> y.getRight()).distinct().count() == subQAFList.size())
                .findFirst().orElse(null);
        if (replacePairList == null) {
            return null;
        }
        // end: Find questionList.size()-1 pairs from all pairs to cover all subQAFList

        // start: Recalculate the result of question 0
        Map<String, Object> context = new HashMap<>();
        question0.getActualParameterDisplay().stream()
                .filter(x -> !x.isMask() && !x.isFinAsk())
                .forEach(x -> context.put(x.getSymbol(), x.getValue()));
        for (Pair<Triple<Question, ActualValue, FormulaField>, ActualValue> pair : replacePairList) {
            ActualValue toAsk1 = pair.getLeft().getMiddle();
            Double x1 = "NUM".equals(toAsk1.getType()) ? ((Number) toAsk1.getValue()).doubleValue() :
                    ((List<Number>) toAsk1.getValue()).get(toAsk1.getRidx()).doubleValue();
            context.put(pair.getRight().getSymbol(), x1);
        }
        Double q2ReCalResult = ExpRunner.evaluate(question0.getFormula().getExpressionGroovy()
                .replace("??????", "")
                .replace("?????", "")
                .replace("????", "")
                .replace("???", "")
                .replace("??", "")
                .replace("? ", ""), context);
        if (q2ReCalResult < question0.getFormula().getResult().getMinValue() ||
                q2ReCalResult > question0.getFormula().getResult().getMaxValue()) {
            return null;
        }
        // end: Recalculate the result of question 0

        // start: Replace known values in the final question with x
        String q2Str = question0.getQuestion();
        List<Integer> idxXList = new ArrayList<>();
        int idxX = -1;
        for (int i = 0; i < replacePairList.size(); i++) {
            if (replacePairList.get(i).getLeft().getLeft().getReplaceX() != null) {
                Integer tmpX = replacePairList.get(i).getLeft().getLeft().getReplaceX();
                idxXList.add(tmpX);
                String toReplace = "x" + tmpX + "（x" + tmpX + "需要通过其他步骤求解得到）";
                q2Str = q2Str.replace(replacePairList.get(i).getRight().getInQuestion().get(0), toReplace);
            } else {
                String xStr = null;
                String qContainsXStr = null;
                while (qContainsXStr != null || xStr == null) {
                    idxX++;
                    String fxStr = "x" + idxX;
                    qContainsXStr = replacePairList.stream().map(x -> x.getLeft().getLeft().getQuestion())
                            .filter(x -> x.contains(fxStr))
                            .findFirst().orElse(null);
                    xStr = fxStr;
                }
                idxXList.add(idxX);
                String toReplace = "x" + idxX + "（x" + idxX + "需要通过其他步骤求解得到）";
                q2Str = q2Str.replace(replacePairList.get(i).getRight().getInQuestion().get(0), toReplace);
            }
        }
        // end: Replace known values in the final question with x

        // start: Find question marks and insert descriptions
        List<String> subQList = new ArrayList<>();
        for (int i = 0; i < replacePairList.size(); i++) {
            String qStr = replacePairList.get(i).getLeft().getLeft().getQuestion();
            if (replacePairList.get(i).getLeft().getLeft().getReplaceX() == null) {
                String xStr = "(将该值记为x" + idxXList.get(i) + ")";
                if (qStr.endsWith("？")) {
                    qStr = qStr + xStr;
                } else {
                    int idx = qStr.lastIndexOf("？");
                    if (idx < 0) {
                        return null;
                    }
                    qStr = qStr.substring(0, idx + 1) + xStr + qStr.substring(idx + 1);
                }
            }
            subQList.add(qStr);
        }
        // end: Find question marks and insert descriptions

        Integer askReplaceX = null;
        if (StringUtils.isBlank(finQ)) {
            String xStr = null;
            String qContainsXStr = null;
            while (qContainsXStr != null || xStr == null) {
                idxX++;
                String fxStr = "x" + idxX;
                qContainsXStr = subQList.stream()
                        .filter(x -> x.contains(fxStr))
                        .findFirst().orElse(null);
                xStr = fxStr;
            }
            finQ = "(将该值记为x" + idxX + ")";
            askReplaceX = idxX;
        }

        if (q2Str.endsWith("？")) {
            q2Str = q2Str + finQ;
        } else {
            int idx = q2Str.lastIndexOf("？");
            if (idx < 0) {
                return null;
            }
            q2Str = q2Str.substring(0, idx + 1) + finQ + q2Str.substring(idx + 1);
        }
        subQList.add(q2Str);
        String answer = StringUtils.join(subQList, "\n......\n......\n");

        toAsk0.setValue(q2ReCalResult, 8);
        toAsk0.setFinAsk(true);
        Formula newFormula = new Formula();
        newFormula.setBasic(false);
        newFormula.setFormulaId(questionList.stream().map(x -> x.getFormula().getFormulaId()).collect(Collectors.joining("|")));
        newFormula.setCategory(questionList.stream().map(x -> x.getFormula().getCategory()).collect(Collectors.joining("|")));
        newFormula.setLevel(questionList.stream().map(x -> x.getFormula().getLevel()).collect(Collectors.joining("|")));
        newFormula.setName(questionList.stream().map(x -> x.getFormula().getName()).collect(Collectors.joining("|")));
        newFormula.setResult(question0.getFormula().getResult());
        newFormula.setTextZhcn(questionList.stream().map(x -> x.getFormula().getTextZhcn()).collect(Collectors.joining("|")));
        newFormula.setTextEn(questionList.stream().map(x -> x.getFormula().getTextEn()).collect(Collectors.joining("|")));
        newFormula.setExpressionGroovy(
                questionList.stream().map(x -> x.getFormula().getExpressionGroovy()).collect(Collectors.joining("##")));
        newFormula.setExpPass(true);
        newFormula.setSource("synthesis" + questionList.size());
        newFormula.setConstraintList(
                questionList.stream().flatMap(
                                x -> (x.getFormula().getConstraintList() == null ? Stream.empty() :
                                        x.getFormula().getConstraintList().stream()))
                        .collect(Collectors.toList())
        );

        if (CollectionUtils.isNotEmpty(question0.getFormula().getConstraintList())) {
            for (String exp : question0.getFormula().getConstraintList()) {
                Boolean pass = (Boolean) ExpRunner.evaluateGroovy(exp, context);
                if (!Boolean.TRUE.equals(pass)) {
                    return null;
                }
            }
        }

        Question question = new Question();
        question.setQuestionId(questionList.stream().map(x -> x.getQuestionId()).collect(Collectors.joining("|")));
        question.setGroundTruth(Formula.regularDouble(q2ReCalResult, Config.ROUND_SIZE));
        question.setFormula(newFormula);
        question.setQuestion(answer);
        question.setActualParameterDisplay(
                questionList.stream()
                        .flatMap(x -> (x.getActualParameterDisplay() == null ? Stream.empty() : x.getActualParameterDisplay().stream()))
                        .collect(Collectors.toList())
        );
        question.getActualParameterDisplay().forEach(x -> {
            if (x != toAsk0) {
                x.setFinAsk(false);
            }
        });
        question.setReplaceX(askReplaceX);

        return question;
    }

    /**
     * Build N2 level questions by synthesizing N1 level questions
     *
     * @param maxCount the maximum number of questions to generate
     */
    private static void buildN2(int maxCount) {
        List<LinkedHashMap<String, String>> questionN1List = new ArrayList<>();
        if (new File(Config.QUESTION_N1_SAVE_PATH).exists()) {
            try {
                List<LinkedHashMap<String, String>> lines = ExcelReader.readFirstSheetWithHeader(Config.QUESTION_N1_SAVE_PATH);
                questionN1List.addAll(lines);
            } catch (IOException e) {
                log.error("Failed to read file:{}", Config.QUESTION_N1_SAVE_PATH, e);
                return;
            }
        } else {
            log.info("N1 difficulty sample set does not exist, please generate N1 sample set first");
            return;
        }

        if (new File(Config.QUESTION_N2_SAVE_PATH).exists()) {
            new File(Config.QUESTION_N2_SAVE_PATH).delete();
        }

        List<Question> questionList = questionN1List.stream()
                .filter(x -> "L1".equals(x.get("level")))
                .map(Question::fromMap)
                .collect(Collectors.toList());
        log.info("questionList.size()={}", questionList.size());

        // Determine field reference relationships
        for (Question question : questionList) {
            for (ActualValue actualValue : question.getActualParameterDisplay()) {
                List<FormulaField> q1Params = new ArrayList<>(question.getFormula().getParameterList());
                q1Params.add(question.getFormula().getResult());
                FormulaField field = q1Params.stream()
                        .filter(x -> x.getSymbol().equalsIgnoreCase(actualValue.getSymbol()))
                        .findFirst().orElse(null);
                actualValue.setField(field);
                if (actualValue.isMask()) {
                    actualValue.setFinAsk(true);
                }
            }
        }

        int count = 0;
        List<Question> newQuestionList = new ArrayList<>();
        Collections.shuffle(questionList);
        Map<String, Integer> counter = new HashMap<>();
        for (int i = 0; i < questionList.size(); i++) {
            List<Question> questionList2 = new ArrayList<>(questionList);
            Collections.shuffle(questionList2);
            Question question1 = questionList.get(i);
            for (int j = 0; j < questionList2.size(); j++) {
                Question question2 = questionList2.get(j);
                if (question2.getQuestionId().equals(question1.getQuestionId())) {
                    continue;
                }
                try {
                    Question question = synthesis("(该问题为最终求解内容)", Lists.newArrayList(question1.copy(), question2.copy()));
                    if (question != null) {
                        String[] qArray = question.getQuestionId().split("\\|");
                        Integer c1 = counter.getOrDefault(qArray[0], 0);
                        Integer c2 = counter.getOrDefault(qArray[1], 0);
                        if (c1 >= 3 || c2 >= 3) {
                            // Balance the usage count of each N1 question
                            continue;
                        }
                        counter.put(qArray[0], c1 + 1);
                        counter.put(qArray[1], c2 + 1);

                        newQuestionList.add(question);
                        count++;
                        log.info("【{}/{}】【{}/{}】, count={}", i + 1, questionList.size(), j + 1, questionList.size(), count);
                    }
                } catch (Exception e) {
                    log.error("Synthesis failed", e);
                }
            }
        }
        log.info("count={}", count);
        Collections.shuffle(newQuestionList);
        newQuestionList = newQuestionList.subList(0, Math.min(newQuestionList.size(), maxCount));
        List<LinkedHashMap<String, String>> outLines = newQuestionList.stream().map(Question::toMap).collect(Collectors.toList());
        try {
            ExcelWriter.appendMapAutoTitle(Config.QUESTION_N2_SAVE_PATH, outLines);
        } catch (IOException e) {
            log.error("Failed to write file:{}", Config.QUESTION_N2_SAVE_PATH, e);
        }
    }

    private static void buildN3(int maxCount) {
        List<LinkedHashMap<String, String>> questionN1List = new ArrayList<>();
        if (new File(Config.QUESTION_N1_SAVE_PATH).exists()) {
            try {
                List<LinkedHashMap<String, String>> lines = ExcelReader.readFirstSheetWithHeader(Config.QUESTION_N1_SAVE_PATH);
                questionN1List.addAll(lines);
            } catch (IOException e) {
                log.error("Failed to read file:{}", Config.QUESTION_N1_SAVE_PATH, e);
                return;
            }
        } else {
            log.info("N1 difficulty sample set does not exist, please generate N1 sample set first");
            return;
        }
        if (new File(Config.QUESTION_N3_SAVE_PATH).exists()) {
            new File(Config.QUESTION_N3_SAVE_PATH).delete();
        }

        List<Question> questionList = questionN1List.stream()
                .filter(x -> "L1".equals(x.get("level")))
                .map(Question::fromMap)
                .collect(Collectors.toList());
        log.info("questionList.size()={}", questionList.size());

        // Determine field reference relationships
        for (Question question : questionList) {
            for (ActualValue actualValue : question.getActualParameterDisplay()) {
                List<FormulaField> q1Params = new ArrayList<>(question.getFormula().getParameterList());
                q1Params.add(question.getFormula().getResult());
                FormulaField field = q1Params.stream()
                        .filter(x -> x.getSymbol().equalsIgnoreCase(actualValue.getSymbol()))
                        .findFirst().orElse(null);
                actualValue.setField(field);
                if (actualValue.isMask()) {
                    actualValue.setFinAsk(true);
                }
            }
        }

        int count = 0;
        List<Question> newQuestionList = new ArrayList<>();
        Map<String, Integer> counter = new HashMap<>();
        for (int i = 0; i < questionList.size(); i++) {
            for (int j = 0; j < questionList.size(); j++) {
                for (int k = 0; k < questionList.size(); k++) {
                    if (i == j || i == k || i == j) {
                        continue;
                    }
                    try {
                        Question question = synthesis("(该问题为最终求解内容)",
                                Lists.newArrayList(questionList.get(i).copy(), questionList.get(j).copy(), questionList.get(k).copy()));
                        if (question != null) {
                            String[] qArray = question.getQuestionId().split("\\|");
                            Integer c1 = counter.getOrDefault(qArray[0], 0);
                            Integer c2 = counter.getOrDefault(qArray[1], 0);
                            Integer c3 = counter.getOrDefault(qArray[2], 0);
                            if (c1 >= 4 || c2 >= 4 || c3 >= 4) {
                                // Balance the usage count of each N1 question
                                continue;
                            }
                            counter.put(qArray[0], c1 + 1);
                            counter.put(qArray[1], c2 + 1);
                            counter.put(qArray[2], c3 + 1);
                            newQuestionList.add(question);
                            count++;
                            if (count % 10 == 0) {
                                log.info("【{}/{}】【{}/{}】【{}/{}】, count={}", i + 1, questionList.size(), j + 1, questionList.size(), k + 1,
                                        questionList.size(), count);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Synthesis failed", e);
                    }
                }
            }
        }
        log.info("count={}", count);
        Collections.shuffle(newQuestionList);
        newQuestionList = newQuestionList.subList(0, Math.min(newQuestionList.size(), maxCount));
        List<LinkedHashMap<String, String>> outLines = newQuestionList.stream().map(Question::toMap).collect(Collectors.toList());
        try {
            ExcelWriter.appendMapAutoTitle(Config.QUESTION_N3_SAVE_PATH, outLines);
        } catch (IOException e) {
            log.error("Failed to write file:{}", Config.QUESTION_N3_SAVE_PATH, e);
        }
    }

    private static void buildN4(int maxCount) {
        List<LinkedHashMap<String, String>> questionN1List = new ArrayList<>();
        if (new File(Config.QUESTION_N1_SAVE_PATH).exists()) {
            try {
                List<LinkedHashMap<String, String>> lines = ExcelReader.readFirstSheetWithHeader(Config.QUESTION_N1_SAVE_PATH);
                questionN1List.addAll(lines);
            } catch (IOException e) {
                log.error("Failed to read file:{}", Config.QUESTION_N1_SAVE_PATH, e);
                return;
            }
        } else {
            log.info("N1 difficulty sample set does not exist, please generate N1 sample set first");
            return;
        }

        if (new File(Config.QUESTION_N4_SAVE_PATH).exists()) {
            new File(Config.QUESTION_N4_SAVE_PATH).delete();
        }

        List<Question> questionList = questionN1List.stream()
                .filter(x -> "L1".equals(x.get("level")))
                .map(Question::fromMap)
                .collect(Collectors.toList());
        log.info("questionList.size()={}", questionList.size());

        // Determine field reference relationships
        for (Question question : questionList) {
            for (ActualValue actualValue : question.getActualParameterDisplay()) {
                List<FormulaField> q1Params = new ArrayList<>(question.getFormula().getParameterList());
                q1Params.add(question.getFormula().getResult());
                FormulaField field = q1Params.stream()
                        .filter(x -> x.getSymbol().equalsIgnoreCase(actualValue.getSymbol()))
                        .findFirst().orElse(null);
                actualValue.setField(field);
                if (actualValue.isMask()) {
                    actualValue.setFinAsk(true);
                }
            }
        }

        int count = 0;
        int qCount = 0;
        List<Question> newQuestionList = new ArrayList<>();
        Map<String, Integer> counter = new HashMap<>();
        int threshold = 10;
        List<Boolean> skipFlag = Lists.newArrayList(false, false, false, false);
        for (int i = 0; i < questionList.size(); i++) {
            Question q1 = questionList.get(i).copy();
            Integer c1 = counter.getOrDefault(q1.getQuestionId(), 0);
            if (c1 >= threshold) {
                continue;
            }
            skipFlag.set(0, false);
            for (int j = i + 1; j < questionList.size(); j++) {
                if (skipFlag.subList(0, 1).stream().anyMatch(x -> x)) {
                    break;
                }
                Question q2 = questionList.get(j).copy();
                Integer c2 = counter.getOrDefault(q2.getQuestionId(), 0);
                if (c2 >= threshold) {
                    continue;
                }
                skipFlag.set(1, false);
                List<Question> subQSet = new ArrayList<>();
                try {
                    subQSet.addAll(Lists.newArrayList(
                            synthesis("", Lists.newArrayList(q1.copy(), q2.copy())),
                            synthesis("", Lists.newArrayList(q2.copy(), q1.copy()))
                    ).stream().filter(x -> x != null).collect(Collectors.toList()));
                    if (CollectionUtils.isEmpty(subQSet)) {
                        continue;
                    }
                } catch (Exception e) {
                    log.error("Synthesis failed", e);
                    continue;
                }

                for (int k = 0; k < questionList.size(); k++) {
                    if (skipFlag.subList(0, 2).stream().anyMatch(x -> x)) {
                        break;
                    }
                    Question q3 = questionList.get(k).copy();
                    Integer c3 = counter.getOrDefault(q3.getQuestionId(), 0);
                    if (c3 >= threshold) {
                        continue;
                    }
                    skipFlag.set(2, false);
                    for (int p = k + 1; p < questionList.size(); p++) {
                        if (skipFlag.subList(0, 3).stream().anyMatch(x -> x)) {
                            break;
                        }

                        if (Sets.newHashSet(i, j, k, p).size() < 4) {
                            // Same questions are not combined
                            continue;
                        }

                        Question q4 = questionList.get(p).copy();
                        Integer c4 = counter.getOrDefault(q4.getQuestionId(), 0);
                        if (c4 >= threshold) {
                            continue;
                        }
                        skipFlag.set(3, false);

                        Question foundQ = null;
                        for (Question question : subQSet) {
                            Question tmpQ = synthesis("(该问题为最终求解内容)",
                                    Lists.newArrayList(q3.copy(), q4.copy(), question.copy())
                            );
                            if (tmpQ != null) {
                                foundQ = tmpQ;
                                break;
                            }

                            tmpQ = synthesis("(该问题为最终求解内容)",
                                    Lists.newArrayList(q4.copy(), q3.copy(), question.copy())
                            );
                            if (tmpQ != null) {
                                foundQ = tmpQ;
                                break;
                            }
                        }

                        if (foundQ != null) {
                            String[] qArray = foundQ.getQuestionId().split("\\|");
                            c1 = counter.getOrDefault(qArray[0], 0);
                            c2 = counter.getOrDefault(qArray[1], 0);
                            c3 = counter.getOrDefault(qArray[2], 0);
                            c4 = counter.getOrDefault(qArray[3], 0);
                            if (c1 >= threshold) {
                                skipFlag.set(0, true);
                            }
                            if (c2 >= threshold) {
                                skipFlag.set(1, true);
                            }
                            if (c3 >= threshold) {
                                skipFlag.set(2, true);
                            }
                            if (c4 >= threshold) {
                                skipFlag.set(3, true);
                            }
                            if (skipFlag.stream().anyMatch(x -> x)) {
                                continue;
                            }

                            counter.put(qArray[0], c1 + 1);
                            counter.put(qArray[1], c2 + 1);
                            counter.put(qArray[2], c3 + 1);
                            counter.put(qArray[3], c4 + 1);
                            newQuestionList.add(foundQ);
                            qCount++;
                        }
                        count++;
                        if (count % 100000 == 0) {
                            log.info("【{}/{}】【{}/{}】【{}/{}】, count={}, qCount={}", i + 1, questionList.size(), j + 1, questionList.size(),
                                    k + 1, questionList.size(), count, qCount);
                        }
                    }
                }
            }
        }
        log.info("count={}", count);
        Collections.shuffle(newQuestionList);
        newQuestionList = newQuestionList.subList(0, Math.min(newQuestionList.size(), maxCount));
        List<LinkedHashMap<String, String>> outLines = newQuestionList.stream().map(Question::toMap).collect(Collectors.toList());

        try {
            ExcelWriter.appendMapAutoTitle(Config.QUESTION_N4_SAVE_PATH, outLines);
        } catch (IOException e) {
            log.error("Failed to write file:{}", Config.QUESTION_N4_SAVE_PATH, e);
        }
    }

    /**
     * Generate single formula samples
     *
     * @param maxCount the maximum number of samples, can be set smaller for testing, e.g., 10
     */
    private static void buildN1(String formulaBasePath, int maxCount) {
        List<LinkedHashMap<String, String>> existList = new ArrayList<>();
        if (new File(Config.QUESTION_N1_SAVE_PATH).exists()) {
            try {
                List<LinkedHashMap<String, String>> lines = ExcelReader.readFirstSheetWithHeader(Config.QUESTION_N1_SAVE_PATH);
                existList.addAll(lines);
            } catch (IOException e) {
                log.error("Failed to read file:{}", Config.QUESTION_N1_SAVE_PATH, e);
                return;
            }
        } else {
            log.info("Build N1 sample set from scratch");
        }

        Set<String> existUk = existList.stream().map(x -> x.get("uk")).collect(Collectors.toSet());

        List<LinkedHashMap<String, String>> baseFormulaList = new ArrayList<>();
        if (!new File(formulaBasePath).exists()) {
            log.error("Input file does not exist: " + formulaBasePath);
            throw new RuntimeException("Input file does not exist: " + formulaBasePath);
        } else {
            try {
                baseFormulaList.addAll(ExcelReader.readFirstSheetWithHeader(formulaBasePath));
            } catch (Exception e) {
                log.error("Failed to read input file: " + formulaBasePath, e);
                return;
            }
            log.info("Read input file: " + formulaBasePath);
        }
        List<Formula> formulaList = baseFormulaList.stream()
                .map(Formula::fromBase)
                .filter(x -> (Config.BASE_FORMULA_L1_SAVE_PATH.equals(formulaBasePath) ? "L1" : "L2").equalsIgnoreCase(x.getLevel()))
                .filter(Formula::isExpPass)
                .collect(Collectors.toList());
        log.info("formulaList.size()={}", formulaList.size());

        /**
         * {
         *     "保险": "保险从业/交易",
         *     "宏观": "宏观经济",
         *     "基金从业资格": "基金从业/交易",
         *     "期货从业资格": "期货从业/交易",
         *     "其他": "税收税务",
         *     "通用财务分析": "通用财务分析",
         *     "银行从业资格": "银行从业/交易",
         *     "证券从业资格": "证券从业/交易"
         * }
         */
        String json = "{\n" +
                "    \"保险\": \"保险从业/交易\",\n" +
                "    \"宏观\": \"宏观经济\",\n" +
                "    \"基金从业资格\": \"基金从业/交易\",\n" +
                "    \"期货从业资格\": \"期货从业/交易\",\n" +
                "    \"其他\": \"税收税务\",\n" +
                "    \"通用财务分析\": \"通用财务分析\",\n" +
                "    \"银行从业资格\": \"银行从业/交易\",\n" +
                "    \"证券从业资格\": \"证券从业/交易\"\n" +
                "}";
        Map<String, String> categoryMap = GsonUtils.toMap(json);
        int accCount = existList.size();
        for (int n = 0; n < formulaList.size(); n++) {
            Formula formula = formulaList.get(n);
            try {
                // (name - value - unit, mask-idx)
                List<Pair<List<ActualValue>, Integer>> actual = new ArrayList<>();
                List<FormulaField> fieldList = new ArrayList<>(formula.getParameterListRecur());
                fieldList.add(0, formula.getResult());
                for (int i = 0; i < fieldList.size(); i++) {
                    // i represents the index of the variable to be masked
                    FormulaField curField = fieldList.get(i);
                    if (!curField.getSolvable()) {
                        // Skip unsolvable variables
                        continue;
                    }
                    List<ActualValue> left = new ArrayList<>();
                    List<Map<String, Object>> context = formula.getParamInstanceByCode(1);
                    for (FormulaField field : fieldList.subList(1, fieldList.size())) {
                        if (field.isListType()) {
                            left.add(new ActualValue(field.getName(), field.getSymbol(), (List) context.get(0).get(field.getSymbol()),
                                    field.getUnit(), 4));
                        } else {
                            left.add(new ActualValue(field.getName(), field.getSymbol(), (Number) context.get(0).get(field.getSymbol()),
                                    field.getUnit(), 4));
                        }
                    }
                    left.forEach(x -> context.get(0).put(x.getSymbol(), x.getValue()));

                    Double resultValue = ExpRunner.evaluate(formula.getExpressionGroovy().replace(" ", ""), context.get(0));
                    left.add(0, new ActualValue(formula.getResult().getName(), formula.getResult().getSymbol(), resultValue,
                            formula.getResult().getUnit(), 8));
                    left.get(i).setMask(true);
                    actual.add(Pair.of(left, i));
                    if (StringUtils.isNotBlank(formula.getReplaceInfo()) && actual.size() == 1) {
                        break;
                    }
                    long subFormulaCount = formula.getParameterList().stream()
                            .filter(x -> x.getSubFormula() != null)
                            .count();
                    if (subFormulaCount > 0 && actual.size() == 1) {
                        break;
                    }
                }

                List<String> mixItems = null;
                for (int i = 0; i < actual.size(); i++) {
                    Pair<List<ActualValue>, Integer> element = actual.get(i);
                    String uk = formula.getName() + "##" + element.getLeft().get(element.getRight()).getSymbol();
                    if (existUk.contains(uk)) {
                        log.info("【{}/{}】- 【{}/{}】exist skip", n + 1, formulaList.size(), i + 1, actual.size());
                        continue;
                    }
                    // 1. Construct confusion information
                    if (CollectionUtils.isEmpty(mixItems)) {
                        mixItems = formula.getMixItems(formula.getPromptTextZhcn());
                    }
                    Map<String, Object> context = new HashMap<>();
                    element.getLeft().forEach(x -> context.put(x.getName(), "NUM".equals(x.getType()) ? x.getNumber() : x.getList()));
                    formula.setActualParameterList(Lists.newArrayList(context));

                    log.info("【{}/{}】- 【{}/{}】start", n + 1, formulaList.size(), i + 1, actual.size());
                    String domain = categoryMap.getOrDefault(formula.getCategory(), formula.getCategory());
                    Question question = null;
                    for (int j = 0; j < 3; j++) {
                        // Retry up to 3 times
                        element.getLeft().forEach(x -> x.setInQuestion(null));
                        question = formula.getSampleV2(element.getLeft(), mixItems, formula.getReplaceInfo(), domain);
                        if (question != null) {
                            break;
                        }
                    }
                    log.info("【{}/{}】- 【{}/{}】end", n + 1, formulaList.size(), i + 1, actual.size());
                    if (question == null) {
                        continue;
                    }
                    FormulaField qField = fieldList.get(element.getRight());
                    question.setNormal(qField.getNormal() + "");
                    question.setUk(uk);
                    question.setActualParameterDisplay(element.getLeft());
                    List<LinkedHashMap<String, String>> outLines = Lists.newArrayList(question.toMap());
                    if (accCount == 0) {
                        ExcelWriter.appendMapAutoTitle(Config.QUESTION_N1_SAVE_PATH, outLines);
                    } else {
                        ExcelWriter.appendMap(Config.QUESTION_N1_SAVE_PATH, outLines);
                    }
                    accCount++;
                    log.info("add a sample >>>>>>>>>>>>>>>>>>>>>>>>>");
                    log.info(GsonUtils.toStringPretty(question));
                    log.info("add a sample <<<<<<<<<<<<<<<<<<<<<<<<<");
                    log.info("acc={}", accCount);
                    if (accCount >= maxCount) {
                        break;
                    }
                }
                if (accCount >= maxCount) {
                    break;
                }
            } catch (Exception e) {
                log.error("Generation failed, formula={}", formula, e);
            }
        }
        log.info("accCount={}", accCount);
    }

    @SneakyThrows
    public static void main(String[] args) {
        buildN1(Config.BASE_FORMULA_L1_SAVE_PATH, 1000);
        buildN1(Config.BASE_FORMULA_L2_SAVE_PATH, 100);
        buildN2(300);
        buildN3(300);
        buildN4(300);
    }
}
