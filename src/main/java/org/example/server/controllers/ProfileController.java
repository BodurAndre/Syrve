package org.example.server.controllers;

import org.example.server.models.User;
import org.example.server.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ProfileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/admin/profile/upload-photo")
    public String uploadPhoto(@RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.getAuthenticatedUser(); // Получаем ID пользователя

        Long userId = user.getId();
        // Проверяем, существует ли директория для пользователя
        Path userDir = Paths.get(uploadDir, userId.toString(), "Profile");
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        // Сохраняем файл
        String fileName = file.getOriginalFilename();
        Path filePath = userDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Сохраняем ссылку в БД
        String fileUrl = "/uploads/profiles/" + userId + "/Profile/" + fileName;
        userService.updateUserProfilePhoto(user, fileUrl);
        return "Фотография успешно загружена: " + fileUrl;
    }


    @GetMapping("/admin/getPhoto")
    public ResponseEntity<Map<String, String>> getPhoto() {
        User user = userService.getAuthenticatedUser();

        // Если фото не указано, возвращаем относительный путь к стандартному аватару
        String photoUrl = (user.getProfilePhotoUrl() != null)
                ? user.getProfilePhotoUrl()
                : "uploads/profiles/standardProfile/default-avatar.jpg"; // Без ведущего "/"

        Map<String, String> response = new HashMap<>();
        response.put("photoUrl", photoUrl);

        return ResponseEntity.ok(response);
    }

}

