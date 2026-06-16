package com.sanosysalvos.usuarios.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Value("${server.port:3004}")
    private String port;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "OK");
        res.put("service", "ms-usuarios-entidades");
        res.put("port", Integer.parseInt(port));
        return res;
    }
}
