package com.EMS.service;

import com.EMS.dao.UserDAO;
import com.EMS.entity.User;
import com.EMS.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO = new UserDAO();

    public Map<String, String> login(String username, String password) {
        User user = userDAO.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Login failed: unknown username '{}'", username);
                    return new SecurityException("Invalid username or password.");
                });

        if (!password.equals(user.getPasswordHash())) {
            logger.warn("Login failed: wrong password for '{}'", username);
            throw new SecurityException("Invalid username or password.");
        }

        String token = JwtUtil.generateToken(user.getUsername(), user.getRole().name());
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("role", user.getRole().name());
        result.put("username", user.getUsername());
        return result;
    }
}