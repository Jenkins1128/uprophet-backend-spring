package com.uprophet.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uprophet.auth.security.AuthUser;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/currentUser")
    public JsonNode currentUser(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return JsonMapper.shared().valueToTree(user.username());
    }
}
