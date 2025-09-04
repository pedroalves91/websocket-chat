package com.vinturas.chatcourse.services;

import com.vinturas.chatcourse.data.User;
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
    private final UserService userService;

    // Constructor injection (no need for @Autowired in recent Spring versions)
    public TicketService(RedisTemplate<String, String> redisTemplate,
                         TokenProvider tokenProvider,
                         UserService userService) {
        this.redisTemplate = redisTemplate;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    public String buildAndSaveTicket(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token is required");
        }

        String ticket = UUID.randomUUID().toString();
        Map<String, String> user = tokenProvider.decode(token);
        String userId = user.get("id");
        redisTemplate.opsForValue().set(ticket, userId, Duration.ofSeconds(10L));
        saveUser(user);
        return ticket;
    }

    private void saveUser(Map<String, String> user) {
        String userId = user.get("id");
        String name = user.get("name");
        String email = user.get("email");
        userService.save(new User(userId, name, email));
    }

    public Optional<String> getUserIdByTicket(String ticket) {
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(ticket));
    }
}
