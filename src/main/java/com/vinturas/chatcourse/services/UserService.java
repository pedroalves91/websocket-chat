package com.vinturas.chatcourse.services;

import com.vinturas.chatcourse.data.User;
import com.vinturas.chatcourse.data.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User findById(String id) {
        return userRepository.findById(id).orElseThrow();
    }
}
