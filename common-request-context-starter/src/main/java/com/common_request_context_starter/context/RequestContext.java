package com.common_request_context_starter.context;

import java.util.HashMap;
import java.util.Map;

public final class RequestContext {
    private static final ThreadLocal<Map<String, String>> HEADERS = ThreadLocal.withInitial(HashMap::new);

    private RequestContext() {}

    public static void setHeader(String name, String value){
        HEADERS.get().put(name, value);
    }

    public static String getHeader(String name){
        return HEADERS.get().get(name);
    }

    public static Map<String, String> getAllHeaders(){
        return HEADERS.get();
    }

    public static void clear(){
        HEADERS.remove();
    }
}
