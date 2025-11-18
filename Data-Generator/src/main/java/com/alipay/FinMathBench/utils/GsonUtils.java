/*
 * Ant Group
 * Copyright (c) 2004-2025 All Rights Reserved.
 */
package com.alipay.FinMathBench.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.bind.ObjectTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;

/**
 * Gson utility class for JSON serialization and deserialization.
 * 
 * @author luojing.wp
 * @version GsonUtils.java, v 0.1 2025年11月10日 下午5:30 luojing.wp
 */
public class GsonUtils {
    // Gson instance with date format, complex map key serialization, special floating point values, and null serialization
    private static final Gson GSON = (new GsonBuilder()).setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization()
            .serializeSpecialFloatingPointValues().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                public Date deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
                        throws JsonParseException {
                    return new Date(json.getAsJsonPrimitive().getAsLong());
                }
            }).serializeNulls().create();

    // Gson instance with date format, complex map key serialization, and special floating point values (without null serialization)
    private static final Gson GSON2 = (new GsonBuilder()).setDateFormat("yyyy-MM-dd HH:mm:ss").enableComplexMapKeySerialization()
            .serializeSpecialFloatingPointValues().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                public Date deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
                        throws JsonParseException {
                    return new Date(json.getAsJsonPrimitive().getAsLong());
                }
            }).create();

    // Gson instance with pretty printing and date format
    public static final Gson PRETTY_GSON = (new GsonBuilder()).setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss")
            .disableHtmlEscaping().create();
    // Gson instance with lower case with underscores field naming policy
    public static final Gson LOWER_CAMEL_GSON;

    /**
     * Default constructor
     */
    public GsonUtils() {
    }

    /**
     * Get the default Gson instance
     * 
     * @return the default Gson instance
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * Convert object to JSON string without nulls
     * 
     * @param obj the object to convert
     * @return JSON string representation
     */
    public static String toStringWithoutNulls(Object obj) {
        return obj == null ? "" : GSON2.toJson(obj);
    }

    /**
     * Convert object to JSON string with nulls
     * 
     * @param obj the object to convert
     * @return JSON string representation
     */
    public static String toString(Object obj) {
        return obj == null ? "" : GSON.toJson(obj);
    }

    /**
     * Convert object to pretty formatted JSON string
     * 
     * @param obj the object to convert
     * @return pretty formatted JSON string or null if conversion fails
     */
    public static String toStringPretty(Object obj) {
        if (obj == null) {
            return "";
        } else {
            try {
                return PRETTY_GSON.toJson(obj);
            } catch (JsonParseException var2) {
                return null;
            }
        }
    }

    /**
     * Convert JSON string to Map
     * 
     * @param <T> the value type
     * @param json the JSON string
     * @return Map representation of JSON
     */
    public static <T> Map<String, T> toMap(String json) {
        return StringUtils.isEmpty(json) ? Collections.emptyMap() : (Map) GSON.fromJson(json, (new TypeToken<Map<String, T>>() {
        }).getType());
    }

    /**
     * Convert JSON string to Map without throwing exceptions
     * 
     * @param <T> the value type
     * @param json the JSON string
     * @return Map representation of JSON or empty map if conversion fails
     */
    public static <T> Map<String, T> toMapNoExcept(String json) {
        try {
            return StringUtils.isEmpty(json) ? Collections.emptyMap() : (Map) GSON.fromJson(json, (new TypeToken<Map<String, T>>() {
            }).getType());
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Convert JSON string to Map using custom Gson instance
     * 
     * @param <T> the value type
     * @param json the JSON string
     * @return Map representation of JSON
     */
    public static <T> Map<String, T> toMap1(String json) {
        return StringUtils.isEmpty(json) ? Collections.emptyMap() : (Map) getGson1().fromJson(json, (new TypeToken<Map<String, T>>() {
        }).getType());
    }

    /**
     * Get a custom Gson instance with MapTypeAdapter
     * 
     * @return custom Gson instance
     */
    public static Gson getGson1() {
        Gson gson = (new GsonBuilder()).create();

        try {
            Field factories = Gson.class.getDeclaredField("factories");
            factories.setAccessible(true);
            Object o = factories.get(gson);
            Class<?>[] declaredClasses = Collections.class.getDeclaredClasses();
            Class[] var4 = declaredClasses;
            int var5 = declaredClasses.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Class c = var4[var6];
                if ("java.util.Collections$UnmodifiableList".equals(c.getName())) {
                    Field listField = c.getDeclaredField("list");
                    listField.setAccessible(true);
                    List<TypeAdapterFactory> list = (List) listField.get(o);
                    int i = list.indexOf(ObjectTypeAdapter.getFactory(null));
                    list.set(i, MapTypeAdapter.FACTORY);
                    break;
                }
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        return gson;
    }

    /**
     * Convert JSON string to object of specified class
     * 
     * @param <T> the target type
     * @param clazz the target class
     * @param json the JSON string
     * @return object of specified class or null if json is empty
     */
    public static <T> T toObject(Class<T> clazz, String json) {
        return StringUtils.isEmpty(json) ? null : GSON.fromJson(json, clazz);
    }

    /**
     * Convert JSON string to object of specified type with null handling
     * 
     * @param <T> the target type
     * @param typeOfT the target type
     * @param json the JSON string
     * @return object of specified type or null if json is empty or conversion fails
     */
    public static <T> T toObjectWithNull(Type typeOfT, String json) {
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            try {
                return GSON.fromJson(json, typeOfT);
            } catch (JsonParseException var3) {
                return null;
            }
        }
    }

    /**
     * Convert JSON string to object of specified type
     * 
     * @param <T> the target type
     * @param typeOfT the target type
     * @param json the JSON string
     * @return object of specified type or null if json is empty
     */
    public static <T> T toObject(Type typeOfT, String json) {
        return StringUtils.isEmpty(json) ? null : GSON.fromJson(json, typeOfT);
    }

    /**
     * Convert JSON string to object of specified type using lower case with underscores naming policy
     * 
     * @param <T> the target type
     * @param typeOfT the target type
     * @param json the JSON string
     * @return object of specified type or null if json is empty
     */
    public static <T> T toObjectWithNamePolicy(Type typeOfT, String json) {
        return StringUtils.isEmpty(json) ? null : LOWER_CAMEL_GSON.fromJson(json, typeOfT);
    }

    /**
     * Convert JSON string to object of specified class using lower case with underscores naming policy
     * 
     * @param <T> the target type
     * @param typeOfT the target class
     * @param json the JSON string
     * @return object of specified class or null if json is empty
     */
    public static <T> T toObjectWithNamePolicy(Class<T> typeOfT, String json) {
        return StringUtils.isEmpty(json) ? null : LOWER_CAMEL_GSON.fromJson(json, typeOfT);
    }

    /**
     * Convert JSON string to List of specified class objects
     * 
     * @param <T> the target type
     * @param json the JSON string
     * @param cls the target class
     * @return List of objects or empty list if json is empty
     */
    public static <T> List<T> stringToList(String json, Class<T> cls) {
        if (StringUtils.isEmpty(json)) {
            return new ArrayList();
        } else {
            List<T> list = new ArrayList();
            JsonArray array = (new JsonParser()).parse(json).getAsJsonArray();
            Iterator var4 = array.iterator();

            while (var4.hasNext()) {
                JsonElement elem = (JsonElement) var4.next();
                list.add(GSON.fromJson(elem, cls));
            }

            return list;
        }
    }

    /**
     * Add key and value to JSON string
     * 
     * @param oriExtStr the original JSON string
     * @param key the key to add
     * @param value the value to add
     * @return updated JSON string or original string if operation fails
     */
    public static String addKeyAndValue(String oriExtStr, String key, Object value) {
        if (key != null && value != null) {
            if (StringUtils.isBlank(oriExtStr)) {
                try {
                    Map<String, Object> map = new HashMap();
                    map.put(key, value);
                    return toString(map);
                } catch (Exception var5) {
                    return null;
                }
            } else {
                try {
                    Map<String, Object> map = toMap(oriExtStr);
                    map.put(key, value);
                    return toString(map);
                } catch (Exception var6) {
                    return oriExtStr;
                }
            }
        } else {
            return oriExtStr;
        }
    }

    // Initialize the lower camel Gson instance with lower case with underscores field naming policy
    static {
        LOWER_CAMEL_GSON = (new GsonBuilder()).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    /**
     * Custom TypeAdapter for handling Map types in JSON
     */
    private static class MapTypeAdapter extends TypeAdapter<Object> {
        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() == Object.class) {
                    return (TypeAdapter<T>) new MapTypeAdapter(gson);
                }
                return null;
            }
        };

        private final Gson gson;

        /**
         * Constructor for MapTypeAdapter
         * 
         * @param gson the Gson instance
         */
        private MapTypeAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            // Determine the actual type of the string
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;

                case BEGIN_OBJECT:
                    Map<String, Object> map = new LinkedTreeMap<>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), read(in));
                    }
                    in.endObject();
                    return map;
                case STRING:
                    return in.nextString();
                case NUMBER:
                    String s = in.nextString();
                    return s;
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            //noinspection unchecked
            TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) gson.getAdapter(value.getClass());
            if (typeAdapter instanceof ObjectTypeAdapter) {
                out.beginObject();
                out.endObject();
                return;
            }
            typeAdapter.write(out, value);
        }
    }
}
