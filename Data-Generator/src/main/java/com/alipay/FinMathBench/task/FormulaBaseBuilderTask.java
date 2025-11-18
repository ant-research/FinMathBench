/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.task;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.alipay.FinMathBench.config.Config;
import com.alipay.FinMathBench.entity.Formula;
import com.alipay.FinMathBench.utils.ExcelReader;
import com.alipay.FinMathBench.utils.ExcelWriter;
import com.alipay.FinMathBench.utils.GsonUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luojing.wp
 * @version FormulaBaseBuilderTask.java, v 0.1 2025年11月10日 下午5:46 luojing.wp
 */
@Slf4j(topic = "consoleLog")
public class FormulaBaseBuilderTask {
    /**
     * Initialize L1 level formulas
     */
    @SneakyThrows
    private static void initL1() {
        // Read manually organized formulas
        List<LinkedHashMap<String, String>> lines = ExcelReader.readFirstSheetWithHeader(
                Config.BASE_FORMULA_PATH
        );
        // Currently processing L1 difficulty formulas
        List<Formula> formulaList = lines.stream()
                .filter(x -> "L1".equals(x.get("难度")))
                .flatMap(x -> Formula.fromManualV2(x, null).stream())
                .filter(x -> x.getBasic())
                .collect(Collectors.toList());
        formulaList.forEach(x -> x.setSource("manual"));
        log.info("formulaList.size()={}", formulaList.size());

        List<Formula> invalidList = formulaList.stream()
                .filter(x -> !x.check())
                .collect(Collectors.toList());
        log.info("invalidList.size()={}", invalidList.size());

        for (int n = 0; n < formulaList.size(); n++) {
            log.info("【{}/{}】start", n + 1, formulaList.size());
            Formula formula = formulaList.get(n);

            // check expression
            boolean expPass = formula.checkExp();
            formula.setActualParameterList(null);
            formula.setFormulaId(UUID.randomUUID().toString().replace("-", ""));

            log.info("{} - {}, checkResult={}", formula.getCategory(), formula.getName(), expPass);

            // save
            log.info("add a sample >>>>>>>>>>>>>>>>>>>>>>>>>");
            log.info(GsonUtils.toStringPretty(formula));
            log.info("add a sample <<<<<<<<<<<<<<<<<<<<<<<<<");
            log.info("【{}/{}】end", n + 1, formulaList.size());
        }

        List<LinkedHashMap<String, String>> outLines = formulaList.stream()
                .map(Formula::toMap).collect(Collectors.toList());
        ExcelWriter.appendMapAutoTitle(Config.BASE_FORMULA_L1_SAVE_PATH, outLines);
    }

    @SneakyThrows
    private static void initL2() {
        System.setProperty("formula.L2.parse", "false");
        // load manually organized formulas
        List<LinkedHashMap<String, String>> lines = ExcelReader.readFirstSheetWithHeader(
                Config.BASE_FORMULA_PATH
        );

        // level L2 formulas
        List<Formula> formulaList = lines.stream()
                .filter(x -> "L2".equals(x.get("难度")))
                .map(x -> Formula.fromManualV2(x, "计算公式（中文）"))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        formulaList.forEach(x -> x.getParamInstanceByCode(1));
        formulaList.forEach(Formula::checkExp);
        formulaList.forEach(x -> x.setSource("manual"));
        log.info("formulaList.size()={}", formulaList.size());
        log.info("formulaList.actual-null.size()={}", formulaList.stream().filter(x -> x.getActualParameterList() == null).count());
        List<LinkedHashMap<String, String>> outLines = formulaList.stream()
                .map(Formula::toMap).collect(Collectors.toList());
        ExcelWriter.appendMapAutoTitle(Config.BASE_FORMULA_L2_SAVE_PATH, outLines);
    }

    @SneakyThrows
    public static void main(String[] args) {
        initL1();
        initL2();
    }
}
