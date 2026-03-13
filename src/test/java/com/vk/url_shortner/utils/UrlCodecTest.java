package com.vk.url_shortner.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class UrlCodecTest {

    private final UrlCodec<String> stringCodec = new UrlCodec<>();

    @Test
    void encodeShouldReturnBase64UrlSafeString() {
        String encoded = stringCodec.encode("hello?");

        assertEquals("aGVsbG8_", encoded);
        assertFalse(encoded.contains("+"));
        assertFalse(encoded.contains("/"));
    }

    @Test
    void decodeShouldRestoreOriginalValueAfterEncode() {
        String original = "https://example.com/a path?q=openai&x=1";

        String encoded = stringCodec.encode(original);

        assertNotEquals(original, encoded);
        assertEquals(original, stringCodec.decode(encoded));
    }

    @Test
    void encodeShouldUseInputToStringForGenericType() {
        UrlCodec<Integer> integerCodec = new UrlCodec<>();

        String encoded = integerCodec.encode(12345);

        assertEquals("12345", integerCodec.decode(encoded));
    }
}
