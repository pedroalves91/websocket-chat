package com.vinturas.chatcourse.events;

public record Event<T>(EventType type, T payload) {
}
