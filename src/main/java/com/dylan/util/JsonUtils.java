package com.dylan.util;

import org.junit.Test;

public class JsonUtils {
    public static String buildJsonStr(Integer code, String text) {
        return "{\"code\":" + code + ",\"text\":\"" + text +"\"}";
    }

@Test
    public void testBuildJsonStr() {
        String result = buildJsonStr(0, "结果正常");
    System.out.println(result);
    }
}
