package com.vinturas.chatcourse.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinturas.chatcourse.config.RedisConfig;
import com.vinturas.chatcourse.data.User;
import com.vinturas.chatcourse.dtos.ChatMessage;
import com.vinturas.chatcourse.services.UserService;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class Publisher {
    private static final Logger LOGGER = Logger.getLogger(Publisher.class.getName());
    private final UserService userService;
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    public Publisher(UserService userService, ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
        this.userService = userService;
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
    }

    public void publish(String senderId, String receiverId, String content) throws JsonProcessingException {
        User from = userService.findById(senderId);
        User to = userService.findById(receiverId);
        ChatMessage chatMessage = new ChatMessage(from, to, content);
        String messageSerialized = new ObjectMapper().writeValueAsString(chatMessage);
        reactiveStringRedisTemplate.convertAndSend(RedisConfig.CHAT_MESSAGES_CHANNEL, messageSerialized)
                .doOnSuccess(aLong -> LOGGER.info("Message published to channel 'chat': " + messageSerialized))
                .doOnError(throwable -> LOGGER.severe("Failed to publish message: " + throwable.getMessage()))
                .subscribe();
    }
}
