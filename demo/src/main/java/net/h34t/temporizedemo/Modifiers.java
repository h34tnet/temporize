package net.h34t.temporizedemo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Modifiers {

    public static String html(String raw) {
        return raw.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;");
    }

    public static String urlenc(String url) {
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
