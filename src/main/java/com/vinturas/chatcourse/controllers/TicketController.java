package com.vinturas.chatcourse.controllers;

import com.vinturas.chatcourse.services.TicketService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("v1/tickets")
@CrossOrigin
public class TicketController {
    private final TicketService ticketService;

    public  TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public Map<String, String> buildTicket(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        String token = Optional.ofNullable(authorizationHeader)
                .map(it -> it.replace("Bearer ", ""))
                .orElse("");

        String ticket = ticketService.buildAndSaveTicket(token);
        return Map.of("ticket", ticket);
    }
}
