package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final UserRepository userRepository;

    @GetMapping("/actuator/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("database", checkDatabaseConnection());
        return status;
    }

    @GetMapping("/health")
    public Map<String, String> simpleHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "OK");
        return status;
    }

    private String checkDatabaseConnection() {
        try {
            // Попытка выполнить простой запрос к БД
            userRepository.count();
            return "connected";
        } catch (Exception e) {
            return "disconnected: " + e.getMessage();
        }
    }
}