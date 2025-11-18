/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.config;

import com.alipay.FinMathBench.utils.llm.LLMInvoker;
import com.alipay.FinMathBench.utils.llm.LLMInvoker.LLM;

/**
 * @author luojing.wp
 * @version Config.java, v 0.1 2025年11月10日 下午5:28 luojing.wp
 */
public interface Config {
    /**
     * Resource path
     */
    String PATH_RESOURCE = System.getProperty("user.dir") + "/Data-Generator/src/main/resources/";

    /**
     * Base formula path
     */
    String BASE_FORMULA_PATH = PATH_RESOURCE + "formula_seed.xlsx";

    /**
     * Base formula (L1 level) path
     */
    String BASE_FORMULA_L1_SAVE_PATH = PATH_RESOURCE + "formula_base_l1.xlsx";

    /**
     * Base formula (L2 level) path
     */
    String BASE_FORMULA_L2_SAVE_PATH = PATH_RESOURCE + "formula_base_l2.xlsx";


    /**
     * Evaluation sample (single formula composition) path
     */
    String QUESTION_N1_SAVE_PATH = PATH_RESOURCE + "question_n1.xlsx";

    /**
     * Evaluation sample (dual formula composition) path
     */
    String QUESTION_N2_SAVE_PATH = PATH_RESOURCE + "question_n2.xlsx";

    /**
     * Evaluation sample (3 formula composition) path
     */
    String QUESTION_N3_SAVE_PATH = PATH_RESOURCE + "question_n3.xlsx";

    /**
     * Evaluation sample (4 formula composition) path
     */
    String QUESTION_N4_SAVE_PATH = PATH_RESOURCE + "question_n4.xlsx";

    /**
     * Model used for generating samples
     * todo replace your model that implement LLMCaller
     */
    LLM LLM = LLMInvoker.LLM.Mistral_MEDIUM_2508;

    /**
     * Floating point precision
     */
    int ROUND_SIZE = 8;
}
