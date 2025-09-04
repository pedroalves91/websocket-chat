package com.vinturas.chatcourse.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinturas.chatcourse.config.RedisConfig;
import com.vinturas.chatcourse.dtos.ChatMessage;
import com.vinturas.chatcourse.handler.WebSocketHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class Subscriber {
    private static final Logger LOGGER = Logger.getLogger(Subscriber.class.getName());
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final WebSocketHandler webSocketHandler;

    public  Subscriber(WebSocketHandler webSocketHandler, ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
        this.reactiveStringRedisTemplate = reactiveStringRedisTemplate;
        this.webSocketHandler = webSocketHandler;
    }

    @PostConstruct
    private void init() {
        this.reactiveStringRedisTemplate
                .listenTo(ChannelTopic.of(RedisConfig.CHAT_MESSAGES_CHANNEL))
                .map(ReactiveSubscription.Message::getMessage)
                .subscribe(this::onChatMessage);
    }

    private void onChatMessage(final String messageSerialized) {
        LOGGER.info("Message Received");
        try {
            ChatMessage chatMessage = new ObjectMapper().readValue(messageSerialized, ChatMessage.class);
            webSocketHandler.notify(chatMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
