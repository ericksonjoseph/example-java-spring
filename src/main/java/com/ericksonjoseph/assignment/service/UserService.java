package com.ericksonjoseph.assignment.service;

import com.ericksonjoseph.assignment.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ericksonjoseph.assignment.repo.UserRepository;

@Service
public class UserService {

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param username
     * @param password
     * @return User
     */
    public User createUser(String username, String password) {
        return userRepository.save(new User(username, password));
    }
}

