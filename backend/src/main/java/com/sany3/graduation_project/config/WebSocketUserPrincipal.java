package com.sany3.graduation_project.config;

import java.security.Principal;

public class WebSocketUserPrincipal implements Principal {

    private final String name;

    public WebSocketUserPrincipal(Long userId) {
        this.name = userId.toString();
    }

    @Override
    public String getName() {
        return name;
    }
}
