package com.dylan.util;



import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

public class UrlUtils {
    public static String urlEncode(String str) {
        String result = "";
        if (StringUtils.isBlank(str)) {
            return result;
        }
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
