/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import java.math.BigDecimal;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luojing.wp
 * @version ExpRunner.java, v 0.1 2025年11月10日 下午5:34 luojing.wp
 */
@Slf4j(topic = "consoleLog")
public class ExpRunner {
    private static final ScriptEngine ENGINE = new ScriptEngineManager().getEngineByName("groovy");

    /**
     * Evaluate a mathematical expression with given variables
     *
     * @param expression The mathematical expression to evaluate
     * @param variables Map of variable names and their values
     * @return The result as a Double, or null if evaluation fails
     */
    public static Double evaluate(String expression, Map<String, Object> variables) {
        try {
            Bindings bindings = ENGINE.createBindings();
            if (variables != null) {
                bindings.putAll(variables);
            }
            return new BigDecimal(ENGINE.eval(expression, bindings) + "").doubleValue();
        } catch (ScriptException e) {
            log.error("Invalid expression 【{}】: {}", expression, e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            log.error("NumberFormatException 【{}】: {}", expression, e.getMessage());
            return null;
        }
    }

    /**
     * Evaluate a Groovy expression with given variables
     *
     * @param expression The Groovy expression to evaluate
     * @param variables Map of variable names and their values
     * @return The result as an Object, or null if evaluation fails
     */
    public static Object evaluateGroovy(String expression, Map<String, Object> variables) {
        try {
            Bindings bindings = ENGINE.createBindings();
            if (variables != null) {
                bindings.putAll(variables);
            }
            return ENGINE.eval(expression, bindings);
        } catch (ScriptException e) {
            log.error("Invalid expression 【{}】: {}", expression, e.getMessage());
            return null;
        }
    }

    /**
     * Main method for testing expression evaluation
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        //        Double res = evaluate("FV = P * (1 + r)**t", ImmutableMap.of("P", 100, "r", 0.05d, "t", 3));
        //        Double res2 = evaluate("r = Math.log(A/P) / t", ImmutableMap.of("A", 100, "P", 100, "t", 3));
        //        System.exit(0);
        //

        String expression = "UPR = P*RD/TD"; // Use external parameters P, RD, and TD
        Map<String, Object> variables = ImmutableMap.of(
                "P", 71830341L,
                "RD", 195L,
                "TD", 235L
        );

        expression = expression.replace("//", "#").replace("def ", "");
        Object result = ExpRunner.evaluateGroovy(expression, variables);
        System.out.println("Expression: " + expression + " = " + result);
        System.out.println(Double.parseDouble(result + "") - 600d < 0.01d);
    }
}
