package com.vinturas.chatcourse.handler;

import com.vinturas.chatcourse.services.TicketService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.IIOException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final static Logger LOGGER = Logger.getLogger(WebSocketHandler.class.getName());

    private final TicketService ticketService;

    private final Map<String, WebSocketSession> sessions;

    public WebSocketHandler(TicketService ticketService) {
        this.ticketService = ticketService;
        this.sessions = new ConcurrentHashMap<>();
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
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOGGER.info("WebSocket connection closed: " + session.getId() + " with status: " + status.toString());
    }
}
