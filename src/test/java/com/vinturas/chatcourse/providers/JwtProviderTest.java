package com.vinturas.chatcourse.providers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class JwtProviderTest {
    @Autowired
    private TokenProvider tokenProvider;

    @Test
    void test() {
        Map<String, String> decoded = tokenProvider.decode("INVALID_TOKEN");
        System.out.println(decoded);
    }
}