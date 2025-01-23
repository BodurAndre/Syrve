package org.example.server.service;

import jakarta.transaction.Transactional;
import org.example.server.models.User;
import org.example.server.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user){
        if (userRepository.findUserByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User newUser = userRepository.save(user);
        userRepository.flush();
        return newUser;
    }

    public void updateUserProfilePhoto(User user, String fileUrl) {
        user.setProfilePhotoUrl(fileUrl);
        userRepository.save(user);
    }

    @Transactional
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findUserByEmail(username);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
