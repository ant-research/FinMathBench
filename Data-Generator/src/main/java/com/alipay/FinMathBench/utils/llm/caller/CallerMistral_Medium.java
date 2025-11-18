/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils.llm.caller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alipay.FinMathBench.utils.GsonUtils;
import com.alipay.FinMathBench.utils.HttpUtils;
import com.alipay.FinMathBench.utils.JsonParser;
import com.alipay.FinMathBench.utils.llm.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author luojing.wp
 * @version CallerMistral_Medium.java, v 0.1 2025年11月11日 下午2:16 luojing.wp
 */
@Slf4j(topic = "consoleLog")
public class CallerMistral_Medium implements LLMCaller {

    /**
     * Call the Mistral Medium API to process messages
     *
     * @param messageList the list of input messages
     * @return the concatenated response content
     */
    @Override
    public String call(List<Message> messageList) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Content-Accept", "application/json");
        headers.put("Authorization", "Bearer your token");
        Map<String, Object> body = new HashMap<>();
        body.put("model", "mistral-medium-2508");
        body.put("messages", messageList.stream().map(Message::toMap).collect(Collectors.toList()));
        body.put("stream", true);
        List<String> lines = new ArrayList<>();
        try {
            HttpUtils.doSyncPostAcceptSSE(
                    "https://api.mistral.ai/v1/chat/completions",
                    headers,
                    GsonUtils.toString(body),
                    line -> {
                        if (StringUtils.isBlank(line)) {
                            return;
                        }
                        if (!line.startsWith("data:")) {
                            log.error("api response error: {}", line);
                            return;
                        }
                        String content = (String) JsonParser.read(line.substring(5), "$.choices[0].delta.content");
                        System.out.print(content);
                        lines.add(content);
                    }
            );
        } catch (IOException e) {
            log.error("CallerMistral_Medium call error", e);
        }
        return StringUtils.join(lines, "");
    }
}
