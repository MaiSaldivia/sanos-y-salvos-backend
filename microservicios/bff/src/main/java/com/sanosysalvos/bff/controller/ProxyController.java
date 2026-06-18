package com.sanosysalvos.bff.controller;

import com.sanosysalvos.bff.pattern.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProxyController {

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;

    // Headers que NO deben reenviarse al cliente — causan Parse Error en proxies
    private static final List<String> SKIP_RESPONSE_HEADERS = Arrays.asList(
        "transfer-encoding", "connection", "keep-alive",
        "proxy-authenticate", "proxy-authorization",
        "te", "trailers", "upgrade"
    );

    // Headers de request que NO deben reenviarse al microservicio
    private static final List<String> SKIP_REQUEST_HEADERS = Arrays.asList(
        "host", "connection", "keep-alive"
    );

    @Value("${microservicios.usuarios.url}")
    private String usuariosUrl;

    @Value("${microservicios.mascotas.url}")
    private String mascotasUrl;

    @Value("${microservicios.geolocalizacion.url}")
    private String geolocalizacionUrl;

    @Value("${microservicios.motor.url}")
    private String motorUrl;

    @RequestMapping("/**")
    public ResponseEntity<?> proxyRequest(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {

        String requestUri = request.getRequestURI();
        String targetUrl  = determineTargetUrl(requestUri);
        String serviceName = determineServiceName(requestUri);

        if (targetUrl == null) {
            return ResponseEntity.notFound().build();
        }

        String queryString = request.getQueryString();
        String fullUrl = targetUrl + requestUri + (queryString != null ? "?" + queryString : "");

        // Construir headers de request filtrando los problemáticos
        HttpHeaders requestHeaders = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!SKIP_REQUEST_HEADERS.contains(name.toLowerCase())) {
                requestHeaders.add(name, request.getHeader(name));
            }
        }

        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, requestHeaders);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        return circuitBreaker.execute(
            serviceName,
            () -> {
                ResponseEntity<byte[]> response = restTemplate.exchange(
                    URI.create(fullUrl), method, httpEntity, byte[].class);

                // Construir headers de respuesta filtrando Transfer-Encoding y similares
                HttpHeaders responseHeaders = new HttpHeaders();
                response.getHeaders().forEach((name, values) -> {
                    if (!SKIP_RESPONSE_HEADERS.contains(name.toLowerCase())) {
                        responseHeaders.addAll(name, values);
                    }
                });

                return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(responseHeaders)
                    .body(response.getBody());
            },
            () -> ResponseEntity.status(503)
                .body(("Servicio " + serviceName + " no disponible.").getBytes())
        );
    }

    private String determineTargetUrl(String uri) {
        if (uri.startsWith("/api/usuarios") || uri.startsWith("/api/auth"))
            return usuariosUrl;
        if (uri.startsWith("/api/mascotas") || uri.startsWith("/api/alertas"))
            return mascotasUrl;
        if (uri.startsWith("/api/geolocalizacion") || uri.startsWith("/api/geo"))
            return geolocalizacionUrl;
        if (uri.startsWith("/api/motor") || uri.startsWith("/api/coincidencias"))
            return motorUrl;
        return null;
    }

    private String determineServiceName(String uri) {
        if (uri.startsWith("/api/usuarios") || uri.startsWith("/api/auth"))   return "ms-usuarios-entidades";
        if (uri.startsWith("/api/mascotas") || uri.startsWith("/api/alertas")) return "ms-gestion-mascotas";
        if (uri.startsWith("/api/geolocalizacion") || uri.startsWith("/api/geo")) return "ms-geolocalizacion";
        if (uri.startsWith("/api/motor") || uri.startsWith("/api/coincidencias")) return "ms-motor-coincidencias";
        return "unknown";
    }
}
