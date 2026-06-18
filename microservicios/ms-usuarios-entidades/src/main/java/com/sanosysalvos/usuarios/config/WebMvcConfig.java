package com.sanosysalvos.usuarios.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                // Excluir rutas públicas que no necesitan token
                .excludePathPatterns(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/mascotas",
                    "/api/mascotas/busqueda",
                    "/api/mascotas/estadisticas",
                    "/api/mascotas/{id}",
                    "/api/mascotas/encuentros/revision"
                );
    }
}
