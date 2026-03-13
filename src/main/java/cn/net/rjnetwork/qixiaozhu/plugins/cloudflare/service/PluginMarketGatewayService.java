package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.BizException;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.config.CloudflarePluginProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.net.URI;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PluginMarketGatewayService {

    private static final String ADMIN_COOKIE_NAME = "license_admin_session";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CloudflarePluginProperties properties;

    private final ReentrantLock sessionLock = new ReentrantLock();

    private volatile String sessionCookie;
    private volatile long sessionExpireAt;

    public JsonNode listPlugins(Map<String, Object> query) {
        return callWorkerPluginAdmin(HttpMethod.GET, "/admin/api/plugin-market/plugins", query, null);
    }

    public JsonNode listReleases(String pluginCode) {
        return callWorkerPluginAdmin(
                HttpMethod.GET,
                "/admin/api/plugin-market/releases",
                Collections.singletonMap("pluginCode", pluginCode),
                null
        );
    }

    public JsonNode savePlugin(Map<String, Object> payload) {
        return callWorkerPluginAdmin(HttpMethod.POST, "/admin/api/plugin-market/plugin/save", null, payload);
    }

    public JsonNode saveRelease(Map<String, Object> payload) {
        return callWorkerPluginAdmin(HttpMethod.POST, "/admin/api/plugin-market/release/save", null, payload);
    }

    public JsonNode changePluginStatus(String actionPath, String pluginCode) {
        return callWorkerPluginAdmin(
                HttpMethod.POST,
                actionPath,
                null,
                Collections.singletonMap("pluginCode", pluginCode)
        );
    }

    public JsonNode listOrders(Map<String, Object> query) {
        return callWorkerPluginAdmin(HttpMethod.GET, "/admin/api/plugin-market/orders", query, null);
    }

    public JsonNode health() {
        ensureEnabled();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("baseUrl", properties.getMarket().getBaseUrl());
        data.put("signatureReady", isSignatureReady());
        data.put("sessionReady", hasActiveSession());
        data.put("sessionFallbackReady", isSessionCredentialReady());
        data.put("runtimeMode", String.valueOf(properties.getRuntimeMode()));
        return objectMapper.valueToTree(data);
    }

    public JsonNode callWorkerCoreAdmin(String apiPath, HttpMethod method, Map<String, Object> query, Object body) {
        ensureEnabled();
        if (!StringUtils.hasText(apiPath) || !apiPath.startsWith("/admin/api/")) {
            throw new BizException("invalid admin api path");
        }
        if (!isSessionCredentialReady()) {
            throw new BizException("worker admin credentials are required for /admin/api features");
        }
        ensureSession();
        try {
            return executeWithSession(method, apiPath, query, body, true);
        } catch (UnauthorizedException ex) {
            clearSession();
            ensureSession();
            return executeWithSession(method, apiPath, query, body, false);
        }
    }

    private JsonNode callWorkerPluginAdmin(
            HttpMethod method,
            String path,
            Map<String, Object> query,
            Object body
    ) {
        ensureEnabled();
        BizException signatureException = null;
        if (isSignatureReady() && isPluginMarketPath(path)) {
            String signedPath = toSignedPluginMarketPath(path);
            try {
                return executeWithSignature(method, signedPath, query, body);
            } catch (BizException ex) {
                signatureException = ex;
                log.warn("Signed plugin market call failed, fallback to session mode: {}", ex.getMessage());
            }
        }

        if (!isSessionCredentialReady()) {
            if (signatureException != null) {
                throw signatureException;
            }
            throw new BizException("worker admin credentials are not configured for fallback mode");
        }
        ensureSession();
        try {
            return executeWithSession(method, path, query, body, true);
        } catch (UnauthorizedException ex) {
            log.warn("Worker admin session expired, retry after re-login: {}", ex.getMessage());
            clearSession();
            ensureSession();
            return executeWithSession(method, path, query, body, false);
        }
    }

    private JsonNode executeWithSignature(
            HttpMethod method,
            String path,
            Map<String, Object> query,
            Object body
    ) {
        String url = buildWorkerUrl(path, query);
        String pathWithQuery = buildPathWithQuery(path, query);
        String bodyRaw = buildRequestBodyRaw(method, body);
        String ts = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String canonical = method.name().toUpperCase(Locale.ROOT)
                + "\n" + pathWithQuery
                + "\n" + ts
                + "\n" + nonce
                + "\n" + sha256Hex(bodyRaw);
        String sign = hmacSha256Hex(properties.getMarket().getAccessSecret(), canonical);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-PM-AK", properties.getMarket().getAccessKey().trim());
        headers.add("X-PM-TS", ts);
        headers.add("X-PM-NONCE", nonce);
        headers.add("X-PM-SIGN", sign);

        HttpEntity<?> entity = isBodyMethod(method)
                ? new HttpEntity<>(bodyRaw, headers)
                : new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            return parseWorkerSuccess(response.getStatusCode().value(), response.getBody());
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new BizException("worker signed auth rejected: 401");
            }
            throw new BizException(
                    "worker signed request failed: "
                            + ex.getStatusCode().value()
                            + " "
                            + ex.getResponseBodyAsString()
            );
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("worker signed request failed: " + ex.getMessage(), ex);
        }
    }

    private JsonNode executeWithSession(
            HttpMethod method,
            String path,
            Map<String, Object> query,
            Object body,
            boolean allowRetry
    ) {
        String url = buildWorkerUrl(path, query);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(sessionCookie)) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }
        HttpEntity<?> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            return parseWorkerSuccess(response.getStatusCode().value(), response.getBody());
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new UnauthorizedException("status=401");
            }
            String message = ex.getResponseBodyAsString();
            throw new BizException("worker admin request failed: " + ex.getStatusCode().value() + " " + message);
        } catch (UnauthorizedException ex) {
            if (allowRetry) {
                throw ex;
            }
            throw new BizException("worker admin unauthorized after retry");
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("worker admin request failed: " + ex.getMessage(), ex);
        }
    }

    private JsonNode parseWorkerSuccess(int statusCode, String body) {
        JsonNode payload = parsePayload(body);
        if (statusCode == 401 || isUnauthorizedPayload(payload)) {
            throw new UnauthorizedException("status=" + statusCode);
        }
        if (!payload.path("success").asBoolean(false)) {
            throw new BizException(payload.path("message").asText("worker admin request failed"));
        }
        return payload.path("data");
    }

    private void ensureEnabled() {
        CloudflarePluginProperties.Market market = properties.getMarket();
        if (market == null || !market.isEnabled()) {
            throw new BizException("plugin market admin is disabled");
        }
        if (!StringUtils.hasText(market.getBaseUrl())) {
            throw new BizException("plugin market baseUrl is not configured");
        }
        if (!isSignatureReady() && !isSessionCredentialReady()) {
            throw new BizException("configure either signed api credentials or admin username/password");
        }
    }

    private boolean isSignatureReady() {
        CloudflarePluginProperties.Market market = properties.getMarket();
        return market != null
                && market.isSignatureEnabled()
                && StringUtils.hasText(market.getAccessKey())
                && StringUtils.hasText(market.getAccessSecret());
    }

    private boolean isSessionCredentialReady() {
        CloudflarePluginProperties.Market market = properties.getMarket();
        return market != null
                && StringUtils.hasText(market.getAdminUsername())
                && StringUtils.hasText(market.getAdminPassword());
    }

    private void ensureSession() {
        if (hasActiveSession()) {
            return;
        }
        sessionLock.lock();
        try {
            if (hasActiveSession()) {
                return;
            }
            loginWorkerAdmin();
        } finally {
            sessionLock.unlock();
        }
    }

    private boolean hasActiveSession() {
        long renewBefore = Math.max(60000L, properties.getMarket().getSessionRenewBeforeMs());
        return StringUtils.hasText(sessionCookie) && sessionExpireAt > System.currentTimeMillis() + renewBefore;
    }

    private void loginWorkerAdmin() {
        CloudflarePluginProperties.Market market = properties.getMarket();
        String url = buildWorkerUrl("/admin/api/login", null);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", market.getAdminUsername());
        payload.put("password", market.getAdminPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode body = parsePayload(response.getBody());
            if (!body.path("success").asBoolean(false)) {
                throw new BizException("worker admin login failed: " + body.path("message").asText("unknown error"));
            }
            String cookie = extractSessionCookie(response.getHeaders().get(HttpHeaders.SET_COOKIE));
            if (!StringUtils.hasText(cookie)) {
                throw new BizException("worker admin login failed: no session cookie returned");
            }
            this.sessionCookie = cookie;
            this.sessionExpireAt = body.path("data").path("expireAt").asLong(System.currentTimeMillis() + 3600_000L);
        } catch (HttpStatusCodeException ex) {
            throw new BizException(
                    "worker admin login failed: "
                            + ex.getStatusCode().value()
                            + " "
                            + ex.getResponseBodyAsString()
            );
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("worker admin login failed: " + ex.getMessage(), ex);
        }
    }

    private String extractSessionCookie(List<String> setCookies) {
        if (setCookies == null || setCookies.isEmpty()) {
            return "";
        }
        for (String setCookie : setCookies) {
            if (!StringUtils.hasText(setCookie)) {
                continue;
            }
            String[] pair = setCookie.split(";", 2);
            String token = pair[0].trim();
            if (token.startsWith(ADMIN_COOKIE_NAME + "=")) {
                return token;
            }
        }
        return "";
    }

    private void clearSession() {
        this.sessionCookie = "";
        this.sessionExpireAt = 0L;
    }

    private boolean isUnauthorizedPayload(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return false;
        }
        int code = payload.path("code").asInt(0);
        return code == 401;
    }

    private JsonNode parsePayload(String body) {
        if (!StringUtils.hasText(body)) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new BizException("invalid worker response body", ex);
        }
    }

    private String buildWorkerUrl(String path, Map<String, Object> query) {
        String baseUrl = properties.getMarket().getBaseUrl().trim();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .path(path.startsWith("/") ? path : "/" + path);
        if (query != null) {
            query.forEach((key, value) -> {
                if (value != null && StringUtils.hasText(String.valueOf(value))) {
                    builder.queryParam(key, value);
                }
            });
        }
        return builder.build(true).toUriString();
    }

    private String buildPathWithQuery(String path, Map<String, Object> query) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .path(path.startsWith("/") ? path : "/" + path);
        if (query != null) {
            query.forEach((key, value) -> {
                if (value != null && StringUtils.hasText(String.valueOf(value))) {
                    builder.queryParam(key, value);
                }
            });
        }
        String out = builder.build(true).toUriString();
        return out.startsWith("http") ? URI.create(out).getRawPath() : out;
    }

    private boolean isPluginMarketPath(String path) {
        return StringUtils.hasText(path) && path.startsWith("/admin/api/plugin-market/");
    }

    private String toSignedPluginMarketPath(String path) {
        return path.replace("/admin/api/plugin-market/", "/api/plugin-market/admin/");
    }

    private boolean isBodyMethod(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
    }

    private String buildRequestBodyRaw(HttpMethod method, Object body) {
        if (!isBodyMethod(method)) {
            return "";
        }
        try {
            Object source = body == null ? Collections.emptyMap() : body;
            return objectMapper.writeValueAsString(source);
        } catch (Exception ex) {
            throw new BizException("serialize request body failed: " + ex.getMessage(), ex);
        }
    }

    private String sha256Hex(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(String.valueOf(content).getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (Exception ex) {
            throw new BizException("sha256 failed: " + ex.getMessage(), ex);
        }
    }

    private String hmacSha256Hex(String secret, String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sign = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return toHex(sign);
        } catch (Exception ex) {
            throw new BizException("hmac sha256 failed: " + ex.getMessage(), ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private static class UnauthorizedException extends RuntimeException {
        UnauthorizedException(String message) {
            super(message);
        }
    }
}
