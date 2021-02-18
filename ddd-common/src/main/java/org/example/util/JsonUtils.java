/**
 * @(#)JsonUtils.java, 2月 08, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liubin01
 */
@Slf4j
public class JsonUtils {
    public static ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<HashMap<String, String>> TYPE_MAP;

    static {
        objectMapper.registerModule(new Jdk8Module());
        TYPE_MAP = new TypeReference<HashMap<String, String>>() {
        };
    }

    public JsonUtils() {
    }

    public static String writeValue(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception var2) {
            log.error("exception occur when Json serialize", var2);
            return "";
        }
    }

    public static <T> T readValue(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException var3) {
            log.warn("Failed to deserialize from JSON string", var3);
            return null;
        }
    }

    public static <T> T readValue(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException var3) {
            log.warn("Failed to deserialize from JSON string", var3);
            return null;
        }
    }

    public static Map<?, ?> jsonToMap(String json) {
        try {
            return objectMapper.readValue(json, TYPE_MAP);
        } catch (IOException var2) {
            log.warn("Failed to deserialize from JSON string", var2);
            return null;
        }
    }
}
