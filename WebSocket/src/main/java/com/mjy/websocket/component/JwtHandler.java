package com.mjy.websocket.component;

import com.mjy.websocket.service.JwtTokenService;
import org.springframework.stereotype.Component;

@Component
public class JwtHandler {

    private final JwtTokenService jwtTokenService;

    public JwtHandler(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }


}
