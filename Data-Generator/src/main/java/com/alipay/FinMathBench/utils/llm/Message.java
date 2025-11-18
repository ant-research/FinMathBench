/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils.llm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alipay.FinMathBench.utils.GsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * @author luojing.wp
 * @version Message.java, v 0.1 2025年11月10日 下午5:38 luojing.wp
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String role;
    private String content;
    private String source;
    private String think;
    private String type;
    private String image_url;

    /**
     * Convert to Mixtral format
     *
     * @return Map representation of the message
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    /**
     * Create a Message from a JSON string
     *
     * @param jsonStr The JSON string to parse
     * @return A new Message object
     */
    public static Message fromJsonStr(String jsonStr) {
        Map<String, String> map = GsonUtils.toMap(jsonStr);
        return fromMap(map);
    }

    /**
     * Create a Message from a Map
     *
     * @param map The map containing message data
     * @return A new Message object
     */
    public static Message fromMap(Map<String, String> map) {
        String roleStr = map.get("role");
        String contentStr = map.get("content");
        Role role = Role.fromCode(roleStr);
        if (role == null) {
            throw new RuntimeException("Role is missing or invalid, role=" + roleStr);
        }
        if (StringUtils.isBlank(contentStr)) {
            throw new RuntimeException("Content is empty for role=" + roleStr);
        }
        return Message.builder().role(role.getCode()).content(contentStr).source(map.get("source")).build();
    }

    /**
     * Create a sequence of messages in user, assistant, user, assistant order
     *
     * @param jsonStr Array of message content strings
     * @return List of messages in alternating user/assistant roles
     */
    public static List<Message> fromSeq(String... jsonStr) {
        List<Message> answer = new ArrayList<>();
        for (int i = 0; i < jsonStr.length; i++) {
            Role role = i % 2 == 0 ? Role.USER : Role.ASSISTANT;
            answer.add(Message.builder().role(role.getCode()).type("text").content(jsonStr[i]).build());
        }
        return answer;
    }

    /**
     * Create a sequence of messages in user, assistant, user, assistant order with system message
     *
     * @param system The system message content
     * @param jsonStr Array of message content strings
     * @return List of messages with system message followed by alternating user/assistant roles
     */
    public static List<Message> fromSeqWithSystem(String system, String... jsonStr) {
        List<Message> answer = new ArrayList<>();
        if (StringUtils.isNotBlank(system)) {
            answer.add(Message.builder().role(Role.SYSTEM.getCode()).type("text").content(system).build());
        }
        for (int i = 0; i < jsonStr.length; i++) {
            Role role = i % 2 == 0 ? Role.USER : Role.ASSISTANT;
            answer.add(Message.builder().role(role.getCode()).type("text").content(jsonStr[i]).build());
        }
        return answer;
    }

    @Getter
    @AllArgsConstructor
    public enum Role {
        USER("user", "用户"),
        ASSISTANT("assistant", "助手"),
        SYSTEM("system", "系统");

        private String code;
        private String desc;

        /**
     * Find a Role enum by its code
     *
     * @param code The role code to search for
     * @return The matching Role enum, or null if not found
     */
        public static Role fromCode(String code) {
            for (Role role : values()) {
                if (StringUtils.equals(role.getCode(), code)) {
                    return role;
                }
            }
            return null;
        }
    }
}
