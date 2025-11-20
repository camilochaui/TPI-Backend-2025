package org.example.serviciocliente.service;

import lombok.extern.slf4j.Slf4j;
import org.example.serviciocliente.dto.ClienteRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KeycloakAdminService {

    @Value("${keycloak.admin.url}")
    private String keycloakUrl;

    @Value("${keycloak.admin.username}")
    private String adminUser;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.realm}")
    private String realm;

    private final RestTemplate rest = new RestTemplate();

    public void ensureUserExists(ClienteRequestDTO dto) {
        String username = dto.getMail();
        try {
            String token = obtainAdminToken();
            if (userExists(username, token)) {
                log.info("Keycloak: usuario {} ya existe", username);
                return;
            }

            String userId = createUser(dto, token);
            String password = String.valueOf(dto.getDni()); // contraseña inicial: DNI
            setPassword(userId, password, token);
            log.info("Keycloak: usuario creado y contraseña asignada para {} (id={})", username, userId);

        } catch (Exception e) {
            log.error("Error integrando con Keycloak: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear el usuario en Keycloak: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String obtainAdminToken() {
        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", adminClientId);
        form.add("username", adminUser);
        form.add("password", adminPassword);
        form.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<Map<String, Object>> resp = rest.postForEntity(tokenUrl, request, (Class) Map.class);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            Object token = resp.getBody().get("access_token");
            return token != null ? token.toString() : null;
        }
        throw new RuntimeException("No se obtuvo token admin desde Keycloak");
    }

    private boolean userExists(String username, String token) {
        String url = String.format("%s/admin/realms/%s/users?username=%s", keycloakUrl, realm, username);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> req = new HttpEntity<>(headers);
        ResponseEntity<List> resp = rest.exchange(url, HttpMethod.GET, req, List.class);
        return resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null && !resp.getBody().isEmpty();
    }

    private String createUser(ClienteRequestDTO dto, String token) {
        String url = String.format("%s/admin/realms/%s/users", keycloakUrl, realm);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("username", dto.getMail());
        body.put("firstName", dto.getNombre());
        body.put("lastName", dto.getApellido());
        body.put("email", dto.getMail());
        body.put("enabled", true);

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Void> resp = rest.postForEntity(url, req, Void.class);
            if (resp.getStatusCode() == HttpStatus.CREATED) {
                URI location = resp.getHeaders().getLocation();
                if (location != null) {
                    String path = location.getPath();
                    String[] parts = path.split("/");
                    return parts[parts.length - 1];
                }
            }
            throw new RuntimeException("No se pudo crear usuario en Keycloak. Status: " + resp.getStatusCode());
        } catch (HttpClientErrorException.Conflict ex) {
            // Usuario ya existe (race condition)
            log.warn("Keycloak returned conflict al crear usuario {}: {}", dto.getMail(), ex.getMessage());
            // Intentar recuperar id
            return getUserIdByUsername(dto.getMail(), token);
        }
    }

    private String getUserIdByUsername(String username, String token) {
        String url = String.format("%s/admin/realms/%s/users?username=%s", keycloakUrl, realm, username);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> req = new HttpEntity<>(headers);
        ResponseEntity<List> resp = rest.exchange(url, HttpMethod.GET, req, List.class);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null && !resp.getBody().isEmpty()) {
            @SuppressWarnings("unchecked") Map<String, Object> first = (Map<String, Object>) resp.getBody().get(0);
            return first.get("id").toString();
        }
        throw new RuntimeException("Usuario no encontrado en Keycloak tras conflicto");
    }

    private void setPassword(String userId, String password, String token) {
        String url = String.format("%s/admin/realms/%s/users/%s/reset-password", keycloakUrl, realm, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("type", "password");
        body.put("temporary", false);
        body.put("value", password);

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        rest.put(url, req);
    }
}
