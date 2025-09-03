package com.vinturas.chatcourse.services;

import com.vinturas.chatcourse.providers.TokenProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;

    // Constructor injection (no need for @Autowired in recent Spring versions)
    public TicketService(RedisTemplate<String, String> redisTemplate,
                         TokenProvider tokenProvider) {
        this.redisTemplate = redisTemplate;
        this.tokenProvider = tokenProvider;
    }

    public String buildAndSaveTicket(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token is required");
        }

        String ticket = UUID.randomUUID().toString();
        Map<String, String> user = tokenProvider.decode(token);
        String userId = user.get("id");
        redisTemplate.opsForValue().set(ticket, userId, Duration.ofSeconds(10L));
        return ticket;
    }

    public Optional<String> getUserIdByTicket(String ticket) {
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(ticket));
    }
}
