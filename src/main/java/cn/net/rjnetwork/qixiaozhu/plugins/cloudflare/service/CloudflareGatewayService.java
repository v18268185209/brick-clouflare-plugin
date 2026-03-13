package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.BizException;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.config.CloudflarePluginProperties;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.CloudflareCredential;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudflareGatewayService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CloudflarePluginProperties properties;
    private final CloudflareAccountService accountService;
    private final CloudflareOperationLogService operationLogService;

    public JsonNode invokeJson(
            Long accountId,
            String module,
            String action,
            HttpMethod method,
            String path,
            Map<String, Object> query,
            Object body
    ) {
        return invokeJson(accountId, module, action, method, path, query, body, MediaType.APPLICATION_JSON);
    }

    public JsonNode invokeJson(
            Long accountId,
            String module,
            String action,
            HttpMethod method,
            String path,
            Map<String, Object> query,
            Object body,
            MediaType contentType
    ) {
        CloudflareCredential credential = accountService.resolveCredential(accountId);
        String endpoint = buildUrl(path, query);
        String bodyRaw = bodyToString(body);
        try {
            HttpHeaders headers = buildHeaders(credential, contentType);
            HttpEntity<?> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = executeWithRetry(endpoint, method, entity, module, action);
            JsonNode node = parseJsonOrWrap(response.getBody());
            String cloudflareError = extractCloudflareError(node);
            if (cloudflareError != null) {
                operationLogService.addLog(
                        accountId,
                        module,
                        action,
                        endpoint,
                        bodyRaw,
                        response.getStatusCode().value(),
                        false,
                        cloudflareError
                );
                throw new BizException("Cloudflare request failed: " + cloudflareError);
            }
            operationLogService.addLog(accountId, module, action, endpoint, bodyRaw, response.getStatusCode().value(), true, "ok");
            return node;
        } catch (HttpStatusCodeException ex) {
            String responseBody = ex.getResponseBodyAsString();
            operationLogService.addLog(accountId, module, action, endpoint, bodyRaw, ex.getStatusCode().value(), false, responseBody);
            throw new BizException("Cloudflare request failed: " + ex.getStatusCode().value() + " " + responseBody);
        } catch (Exception ex) {
            operationLogService.addLog(accountId, module, action, endpoint, bodyRaw, 500, false, ex.getMessage());
            throw new BizException("Cloudflare request failed: " + ex.getMessage(), ex);
        }
    }

    public String invokeText(
            Long accountId,
            String module,
            String action,
            HttpMethod method,
            String path,
            Map<String, Object> query,
            Object body,
            MediaType contentType
    ) {
        CloudflareCredential credential = accountService.resolveCredential(accountId);
        String endpoint = buildUrl(path, query);
        String bodyRaw = bodyToString(body);
        try {
            HttpHeaders headers = buildHeaders(credential, contentType);
            HttpEntity<?> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = executeWithRetry(endpoint, method, entity, module, action);
            operationLogService.addLog(accountId, module, action, endpoint, bodyRaw, response.getStatusCode().value(), true, "ok");
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            String responseBody = ex.getResponseBodyAsString();
            operationLogService.addLog(accountId, module, action, endpoint, bodyRaw, ex.getStatusCode().value(), false, responseBody);
            throw new BizException("Cloudflare request failed: " + ex.getStatusCode().value() + " " + responseBody);
        } catch (Exception ex) {
            operationLogService.addLog(accountId, module, action, endpoint, bodyRaw, 500, false, ex.getMessage());
            throw new BizException("Cloudflare request failed: " + ex.getMessage(), ex);
        }
    }

    public String resolveCloudflareAccountId(Long pluginAccountId, String accountIdParam) {
        if (StringUtils.hasText(accountIdParam)) {
            return accountIdParam.trim();
        }
        CloudflareCredential credential = accountService.resolveCredential(pluginAccountId);
        String cloudflareAccountId = credential.getCloudflareAccountId();
        if (!StringUtils.hasText(cloudflareAccountId)) {
            throw new BizException("cloudflareAccountId is required for this operation");
        }
        return cloudflareAccountId.trim();
    }

    private HttpHeaders buildHeaders(CloudflareCredential credential, MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType == null ? MediaType.APPLICATION_JSON : contentType);
        if ("GLOBAL_KEY".equalsIgnoreCase(credential.getAuthType())) {
            headers.add("X-Auth-Email", credential.getApiEmail());
            headers.add("X-Auth-Key", credential.getApiKey());
        } else {
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + credential.getApiToken());
        }
        return headers;
    }

    private String buildUrl(String path, Map<String, Object> query) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.getApiBaseUrl())
                .path(path.startsWith("/") ? path : "/" + path);
        if (!CollectionUtils.isEmpty(query)) {
            for (Map.Entry<String, Object> e : query.entrySet()) {
                if (e.getValue() != null) {
                    builder.queryParam(e.getKey(), e.getValue());
                }
            }
        }
        return builder.build(true).toUriString();
    }

    private ResponseEntity<String> executeWithRetry(
            String endpoint,
            HttpMethod method,
            HttpEntity<?> entity,
            String module,
            String action
    ) {
        int maxRetries = Math.max(0, properties.getMaxRetries());
        int baseBackoff = Math.max(100, properties.getRetryBackoffMs());
        int attempt = 0;
        while (true) {
            try {
                return restTemplate.exchange(endpoint, method, entity, String.class);
            } catch (HttpStatusCodeException ex) {
                int statusCode = ex.getStatusCode().value();
                if (attempt >= maxRetries || !isRetryableStatus(statusCode)) {
                    throw ex;
                }
                attempt++;
                log.warn("Cloudflare API retry {}/{} for {} {} due to status {} (module={}, action={})",
                        attempt, maxRetries, method, endpoint, statusCode, module, action);
                sleepBackoff(baseBackoff, attempt);
            } catch (Exception ex) {
                if (attempt >= maxRetries || !isRetryableException(ex)) {
                    throw ex;
                }
                attempt++;
                log.warn("Cloudflare API retry {}/{} for {} {} due to transient error: {} (module={}, action={})",
                        attempt, maxRetries, method, endpoint, ex.getMessage(), module, action);
                sleepBackoff(baseBackoff, attempt);
            }
        }
    }

    private boolean isRetryableStatus(int code) {
        return code == 429 || code == 500 || code == 502 || code == 503 || code == 504;
    }

    private boolean isRetryableException(Exception ex) {
        return ex instanceof ResourceAccessException;
    }

    private void sleepBackoff(int baseBackoff, int attempt) {
        long waitMs = Math.min((long) baseBackoff * attempt, 5000L);
        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new BizException("request retry interrupted", ie);
        }
    }

    private JsonNode parseJsonOrWrap(String body) {
        try {
            if (!StringUtils.hasText(body)) {
                return objectMapper.createObjectNode().put("success", true);
            }
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            return objectMapper.createObjectNode().put("raw", body);
        }
    }

    private String extractCloudflareError(JsonNode node) {
        if (node == null) {
            return "empty response body";
        }
        JsonNode successNode = node.get("success");
        if (successNode == null || successNode.asBoolean(true)) {
            return null;
        }

        JsonNode errors = node.get("errors");
        if (errors != null && errors.isArray() && !errors.isEmpty()) {
            JsonNode first = errors.get(0);
            String code = first.path("code").asText("");
            String message = first.path("message").asText("");
            if (StringUtils.hasText(code) && StringUtils.hasText(message)) {
                return code + " " + message;
            }
            if (StringUtils.hasText(message)) {
                return message;
            }
            return first.toString();
        }

        JsonNode messages = node.get("messages");
        if (messages != null && messages.isArray() && !messages.isEmpty()) {
            JsonNode first = messages.get(0);
            if (first != null && StringUtils.hasText(first.asText())) {
                return first.asText();
            }
        }

        return "success=false without detailed error";
    }

    private String bodyToString(Object body) {
        if (body == null) {
            return null;
        }
        try {
            if (body instanceof String value) {
                return value;
            }
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            log.debug("serialize request body failed", e);
            return String.valueOf(body);
        }
    }
}
