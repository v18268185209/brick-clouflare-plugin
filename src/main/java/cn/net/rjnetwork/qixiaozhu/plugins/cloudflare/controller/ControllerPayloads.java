package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.BizException;

import java.util.HashMap;
import java.util.Map;

final class ControllerPayloads {

    private ControllerPayloads() {
    }

    static String requireString(Map<String, Object> payload, String key) {
        if (payload == null) {
            throw new BizException("payload is required");
        }
        Object value = payload.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new BizException(key + " is required");
        }
        return String.valueOf(value);
    }

    static Long requireLong(Map<String, Object> payload, String key) {
        if (payload == null) {
            throw new BizException("payload is required");
        }
        Object value = payload.get(key);
        if (value == null) {
            throw new BizException(key + " is required");
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            throw new BizException(key + " must be number");
        }
    }

    static Map<String, Object> copyWithout(Map<String, Object> payload, String... keys) {
        if (payload == null) {
            throw new BizException("payload is required");
        }
        Map<String, Object> map = new HashMap<>(payload);
        for (String key : keys) {
            map.remove(key);
        }
        return map;
    }
}
