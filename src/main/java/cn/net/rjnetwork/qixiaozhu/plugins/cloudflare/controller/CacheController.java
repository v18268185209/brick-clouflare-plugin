package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.CloudflareInvokeRequest;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cloudflare/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/settings")
    public ApiResponse<Map<String, JsonNode>> getSettings(
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        Map<String, JsonNode> settings = new HashMap<>();
        settings.put("cacheLevel", gatewayService.invokeJson(
                accountId, "CACHE", "GET_CACHE_LEVEL", HttpMethod.GET,
                "/zones/" + zoneId + "/settings/cache_level", null, null));
        settings.put("browserCacheTtl", gatewayService.invokeJson(
                accountId, "CACHE", "GET_BROWSER_CACHE_TTL", HttpMethod.GET,
                "/zones/" + zoneId + "/settings/browser_cache_ttl", null, null));
        settings.put("developmentMode", gatewayService.invokeJson(
                accountId, "CACHE", "GET_DEVELOPMENT_MODE", HttpMethod.GET,
                "/zones/" + zoneId + "/settings/development_mode", null, null));
        return ApiResponse.ok(settings);
    }

    @PostMapping("/purge")
    public ApiResponse<JsonNode> purge(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "CACHE",
                "PURGE",
                HttpMethod.POST,
                "/zones/" + zoneId + "/purge_cache",
                null,
                ControllerPayloads.copyWithout(payload, "zoneId")
        );
        return ApiResponse.ok(result);
    }

    @PatchMapping("/settings")
    public ApiResponse<Map<String, JsonNode>> patchSettings(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");

        Map<String, JsonNode> changed = new HashMap<>();
        if (payload.containsKey("cacheLevel")) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("value", payload.get("cacheLevel"));
            changed.put("cacheLevel", gatewayService.invokeJson(
                    request.getAccountId(),
                    "CACHE",
                    "PATCH_CACHE_LEVEL",
                    HttpMethod.PATCH,
                    "/zones/" + zoneId + "/settings/cache_level",
                    null,
                    body
            ));
        }
        if (payload.containsKey("browserCacheTtl")) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("value", payload.get("browserCacheTtl"));
            changed.put("browserCacheTtl", gatewayService.invokeJson(
                    request.getAccountId(),
                    "CACHE",
                    "PATCH_BROWSER_CACHE_TTL",
                    HttpMethod.PATCH,
                    "/zones/" + zoneId + "/settings/browser_cache_ttl",
                    null,
                    body
            ));
        }
        if (payload.containsKey("developmentMode")) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("value", payload.get("developmentMode"));
            changed.put("developmentMode", gatewayService.invokeJson(
                    request.getAccountId(),
                    "CACHE",
                    "PATCH_DEVELOPMENT_MODE",
                    HttpMethod.PATCH,
                    "/zones/" + zoneId + "/settings/development_mode",
                    null,
                    body
            ));
        }
        return ApiResponse.ok(changed);
    }
}
