package com.vk.url_shortner.utils;

import java.util.Base64;

public class UrlCodec<T> {

    public String encode(T input) {
        String str = input.toString();
        return Base64.getUrlEncoder().encodeToString(str.getBytes());
    }

    public String decode(String encoded) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
        return new String(decodedBytes);
    }
}
