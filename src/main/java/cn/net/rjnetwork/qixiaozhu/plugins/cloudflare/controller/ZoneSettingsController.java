package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.CloudflareInvokeRequest;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cloudflare/zone-settings")
@RequiredArgsConstructor
public class ZoneSettingsController {

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/settings")
    public ApiResponse<Map<String, JsonNode>> getSettings(
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        Map<String, JsonNode> settings = new HashMap<>();
        settings.put("brotli", getSetting(accountId, zoneId, "brotli", "GET_BROTLI"));
        settings.put("http3", getSetting(accountId, zoneId, "http3", "GET_HTTP3"));
        settings.put("alwaysOnline", getSetting(accountId, zoneId, "always_online", "GET_ALWAYS_ONLINE"));
        settings.put("automaticHttpsRewrites", getSetting(accountId, zoneId, "automatic_https_rewrites", "GET_AUTO_HTTPS_REWRITES"));
        settings.put("rocketLoader", getSetting(accountId, zoneId, "rocket_loader", "GET_ROCKET_LOADER"));
        settings.put("securityLevel", getSetting(accountId, zoneId, "security_level", "GET_SECURITY_LEVEL"));
        settings.put("minify", getSetting(accountId, zoneId, "minify", "GET_MINIFY"));
        return ApiResponse.ok(settings);
    }

    @PatchMapping("/settings")
    public ApiResponse<Map<String, JsonNode>> patchSettings(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");

        Map<String, JsonNode> changed = new HashMap<>();
        patchIfPresent(changed, request.getAccountId(), zoneId, payload, "brotli", "brotli", "PATCH_BROTLI");
        patchIfPresent(changed, request.getAccountId(), zoneId, payload, "http3", "http3", "PATCH_HTTP3");
        patchIfPresent(changed, request.getAccountId(), zoneId, payload, "alwaysOnline", "always_online", "PATCH_ALWAYS_ONLINE");
        patchIfPresent(changed, request.getAccountId(), zoneId, payload, "automaticHttpsRewrites", "automatic_https_rewrites", "PATCH_AUTO_HTTPS_REWRITES");
        patchIfPresent(changed, request.getAccountId(), zoneId, payload, "rocketLoader", "rocket_loader", "PATCH_ROCKET_LOADER");
        patchIfPresent(changed, request.getAccountId(), zoneId, payload, "securityLevel", "security_level", "PATCH_SECURITY_LEVEL");
        patchIfPresent(changed, request.getAccountId(), zoneId, payload, "minify", "minify", "PATCH_MINIFY");

        return ApiResponse.ok(changed);
    }

    private JsonNode getSetting(Long accountId, String zoneId, String settingKey, String action) {
        return gatewayService.invokeJson(
                accountId,
                "ZONE_SETTINGS",
                action,
                HttpMethod.GET,
                "/zones/" + zoneId + "/settings/" + settingKey,
                null,
                null
        );
    }

    private void patchIfPresent(
            Map<String, JsonNode> changed,
            Long accountId,
            String zoneId,
            Map<String, Object> payload,
            String payloadKey,
            String settingKey,
            String action
    ) {
        if (!payload.containsKey(payloadKey)) {
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("value", payload.get(payloadKey));
        JsonNode updated = gatewayService.invokeJson(
                accountId,
                "ZONE_SETTINGS",
                action,
                HttpMethod.PATCH,
                "/zones/" + zoneId + "/settings/" + settingKey,
                null,
                body
        );
        changed.put(payloadKey, updated);
    }
}

