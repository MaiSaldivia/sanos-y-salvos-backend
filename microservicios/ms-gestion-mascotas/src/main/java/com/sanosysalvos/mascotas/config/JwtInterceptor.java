package com.sanosysalvos.mascotas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;
        RequireAuth requireAuth = method.getMethodAnnotation(RequireAuth.class);
        if (requireAuth == null) {
            requireAuth = method.getBeanType().getAnnotation(RequireAuth.class);
        }

        if (requireAuth != null) {
            String token = extractToken(request);
            if (token == null || !jwtUtil.validarToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            String userId = jwtUtil.extraerUserId(token);
            String role = jwtUtil.extraerRol(token);
            
            request.setAttribute("userId", userId);
            request.setAttribute("userRole", role);

            RequireRole requireRole = method.getMethodAnnotation(RequireRole.class);
            if (requireRole != null) {
                com.sanosysalvos.mascotas.model.Rol[] allowedRoles = requireRole.value();
                boolean hasRole = Arrays.stream(allowedRoles).anyMatch(r -> r.name().equals(role));
                if (!hasRole) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }
        }
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
