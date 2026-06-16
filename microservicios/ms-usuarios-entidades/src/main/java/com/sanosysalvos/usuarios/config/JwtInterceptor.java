package com.sanosysalvos.usuarios.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanosysalvos.usuarios.model.Rol;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        boolean requiresAuth = handlerMethod.hasMethodAnnotation(RequireAuth.class) 
                || handlerMethod.getBeanType().isAnnotationPresent(RequireAuth.class);
        boolean requiresRole = handlerMethod.hasMethodAnnotation(RequireRole.class)
                || handlerMethod.getBeanType().isAnnotationPresent(RequireRole.class);

        // Si no requiere auth ni rol, dejamos pasar
        if (!requiresAuth && !requiresRole) {
            // Sin embargo, si hay un token válido presente, extraemos los datos igualmente 
            // (por ejemplo para el endpoint de reportar mascota que asocia el id_usuario si está logueado)
            extraerTokenSiExiste(request);
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token requerido");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validarToken(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
            return false;
        }

        Claims claims = jwtUtil.extraerClaims(token);
        String userId = claims.getSubject();
        String rolString = (String) claims.get("rol");
        Rol rol = Rol.valueOf(rolString);

        request.setAttribute("userId", userId);
        request.setAttribute("userRol", rol);
        request.setAttribute("userNombre", claims.get("nombre"));
        request.setAttribute("userEmail", claims.get("email"));

        if (requiresRole) {
            RequireRole requireRoleAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRoleAnnotation == null) {
                requireRoleAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }

            if (requireRoleAnnotation != null) {
                Rol[] rolesPermitidos = requireRoleAnnotation.value();
                boolean tieneRol = Arrays.asList(rolesPermitidos).contains(rol);
                if (!tieneRol) {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Permisos insuficientes");
                    return false;
                }
            }
        }

        return true;
    }

    private void extraerTokenSiExiste(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validarToken(token)) {
                Claims claims = jwtUtil.extraerClaims(token);
                if (claims != null) {
                    request.setAttribute("userId", claims.getSubject());
                    request.setAttribute("userRol", Rol.valueOf((String) claims.get("rol")));
                }
            }
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("success", false);
        errorMap.put("message", message);

        String json = objectMapper.writeValueAsString(errorMap);
        response.getWriter().write(json);
    }
}
