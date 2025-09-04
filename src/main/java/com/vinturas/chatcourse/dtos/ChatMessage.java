package com.vinturas.chatcourse.dtos;

import com.vinturas.chatcourse.data.User;

public record ChatMessage(User from, User to, String content) {
}
