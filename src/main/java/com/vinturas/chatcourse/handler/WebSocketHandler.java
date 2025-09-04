package com.vinturas.chatcourse.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinturas.chatcourse.data.User;
import com.vinturas.chatcourse.dtos.ChatMessage;
import com.vinturas.chatcourse.events.Event;
import com.vinturas.chatcourse.events.EventType;
import com.vinturas.chatcourse.pubsub.Publisher;
import com.vinturas.chatcourse.services.TicketService;
import com.vinturas.chatcourse.services.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final static Logger LOGGER = Logger.getLogger(WebSocketHandler.class.getName());

    private final TicketService ticketService;
    private final UserService userService;
    private final Publisher publisher;

    private final Map<String, WebSocketSession> sessions;
    private final Map<String, String> userIds;

    public WebSocketHandler(TicketService ticketService, UserService userService, Publisher publisher) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.publisher = publisher;
        this.sessions = new ConcurrentHashMap<>();
        this.userIds = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Optional<String> ticket = getTicket(session);
        if (ticket.isEmpty() || ticket.get().isBlank()) {
            LOGGER.warning("Connection rejected: Missing ticket in session " + session.getId());
            closeConnection(session, CloseStatus.POLICY_VIOLATION);
            return;
        }

        Optional<String> userId = ticketService.getUserIdByTicket(ticket.get());
        if (userId.isEmpty()) {
            LOGGER.warning("Connection rejected: Invalid or expired ticket in session " + session.getId());
            closeConnection(session, CloseStatus.POLICY_VIOLATION);
            return;
        }

        sessions.put(userId.get(), session);
        userIds.put(session.getId(), userId.get());
        sendChatUsers(session);
        LOGGER.info("New WebSocket connection established: " + session.getId() + " for user: " + userId.get());
    }

    private void closeConnection(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException e) {
            LOGGER.severe("Error closing session " + session.getId() + ": " + e.getMessage());
        }
    }

    private Optional<String> getTicket(WebSocketSession session) {
        if (session.getUri() == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                UriComponentsBuilder.fromUri(session.getUri())
                        .build()
                        .getQueryParams()
                        .getFirst("ticket")
        ).map(String::trim);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        LOGGER.info("Received message: " + message.getPayload() + " from session: " + session.getId());
        try {
            if (message.getPayload().equals("ping")) {
                session.sendMessage(new TextMessage("pong"));
                return;
            }
            MessagePayload payload = new ObjectMapper().readValue(message.getPayload(), MessagePayload.class);
            String senderUserId = userIds.get(session.getId());
            publisher.publish(senderUserId, payload.to(), payload.content());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOGGER.info("WebSocket connection closed: " + session.getId() + " with status: " + status.toString());
        String userId = userIds.get(session.getId());
        sessions.remove(userId);
        userIds.remove(session.getId());
    }

    private void sendChatUsers(WebSocketSession session) {
        List<User> users = userService.findAll();
        Event<List<User>> event = new Event<>(EventType.CHAT_USERS_UPDATED, users);
        sendEvent(session, event);
    }

    private void sendEvent(WebSocketSession session, Event<?> event) {
        try {
            String eventSerialized = new ObjectMapper().writeValueAsString(event);
            session.sendMessage(new TextMessage(eventSerialized));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void notify(ChatMessage chatMessage) {
        Event<ChatMessage> event = new Event<>(EventType.CHAT_MESSAGE_CREATED, chatMessage);
        List<String> userIds = List.of(chatMessage.from().id(), chatMessage.to().id());
        userIds.stream()
                .distinct()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .forEach(session -> sendEvent(session, event));
    }
}
