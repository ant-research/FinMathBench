/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils.llm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alipay.FinMathBench.utils.GsonUtils;
import com.alipay.FinMathBench.utils.llm.caller.CallerMistral_Medium;
import com.alipay.FinMathBench.utils.llm.caller.LLMCaller;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author luojing.wp
 * @version LLMInvoker.java, v 0.1 2025年11月10日 下午5:38 luojing.wp
 */
public class LLMInvoker {
    private static final Pattern P_THINK = Pattern.compile("<think>([\\s\\S]+?)</think>([\\s\\S]+)");

    @Getter
    private List<Message> history = new ArrayList<>();

    /**
     * Run a single query with the specified LLM
     *
     * @param llm The LLM to use for the query
     * @param query The query string
     * @return The response from the LLM
     */
    public String runSingle(LLM llm, String query) {
        return runSingle(llm, null, query, "human", false);
    }

    /**
     * Run a single query with the specified LLM and settings
     *
     * @param llm The LLM to use for the query
     * @param setting The settings to apply
     * @param query The query string
     * @return The response from the LLM
     */
    public String runSingle(LLM llm, String setting, String query) {
        return runSingle(llm, setting, query, "human", false);
    }

    /**
     * Run a single query with the specified LLM, settings, source and print option
     *
     * @param llm The LLM to use for the query
     * @param setting The settings to apply
     * @param query The query string
     * @param source The source identifier
     * @param print Whether to print the messages
     * @return The response from the LLM
     */
    public String runSingle(LLM llm, String setting, String query, String source, boolean print) {
        history.addAll(Message.fromSeqWithSystem(setting, query));
        history.get(history.size() - 1).setSource(source);
        return execMessage(llm, print);
    }

    /**
     * Execute the message with the specified LLM
     *
     * @param llm The LLM to use
     * @param print Whether to print the messages
     * @return The response from the LLM
     */
    @SneakyThrows
    private String execMessage(LLM llm, boolean print) {
        if (print) {
            System.out.println(GsonUtils.toStringPretty(history.get(history.size() - 1)));
        }
        String resp = llm.clazz.newInstance().call(history);
        Matcher matcher = P_THINK.matcher(resp);
        if (matcher.find()) {
            history.add(Message.builder().role(Message.Role.ASSISTANT.getCode()).content(matcher.group(2).trim()).think(
                    matcher.group(1).trim()).source(llm.name()).build());
        } else {
            history.add(Message.builder().role(Message.Role.ASSISTANT.getCode()).content(resp).source(llm.name()).build());
        }
        if (print) {
            System.out.println(GsonUtils.toStringPretty(history.get(history.size() - 1)));
        }
        return resp;
    }

    /**
     * Get the last content from the history
     *
     * @return The last message content, or null if history is empty
     */
    public String lastContent() {
        if (CollectionUtils.isEmpty(history)) {
            return null;
        }
        return history.get(history.size() - 1).getContent();
    }

    @Getter
    @AllArgsConstructor
    public enum LLM {
        // TODO: Add your model that implements LLMCaller
        Mistral_MEDIUM_2508("Mistral_MEDIUM_2508", "Mistral_MEDIUM_2508", CallerMistral_Medium.class);

        private String                     name;
        private String                     desc;
        private Class<? extends LLMCaller> clazz;

        /**
     * Find an LLM enum by its name
     *
     * @param name The name to search for
     * @return The matching LLM enum, or null if not found
     */
        public static LLM fromName(String name) {
            for (LLM llm : values()) {
                if (StringUtils.equals(llm.name(), name)) {
                    return llm;
                }
            }
            return null;
        }
    }
}
