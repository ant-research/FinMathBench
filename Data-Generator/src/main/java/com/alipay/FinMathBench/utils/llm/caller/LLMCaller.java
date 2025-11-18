/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils.llm.caller;

import java.util.List;

import com.alipay.FinMathBench.utils.llm.Message;

/**
 * @author luojing.wp
 * @version LLMCaller.java, v 0.1 2025年11月10日 下午5:39 luojing.wp
 */
public interface LLMCaller {
    /**
     * Call the LLM service
     *
     * @param messageList the list of input messages
     * @return the output content from the LLM
     */
    String call(List<Message> messageList);
}
