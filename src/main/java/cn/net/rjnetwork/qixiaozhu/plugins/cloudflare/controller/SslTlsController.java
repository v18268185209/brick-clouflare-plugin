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
@RequestMapping("/cloudflare/ssl")
@RequiredArgsConstructor
public class SslTlsController {

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/settings")
    public ApiResponse<Map<String, JsonNode>> getSettings(
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        Map<String, JsonNode> settings = new HashMap<>();
        settings.put("ssl", gatewayService.invokeJson(
                accountId, "SSL", "GET_SSL", HttpMethod.GET,
                "/zones/" + zoneId + "/settings/ssl", null, null));
        settings.put("alwaysUseHttps", gatewayService.invokeJson(
                accountId, "SSL", "GET_ALWAYS_HTTPS", HttpMethod.GET,
                "/zones/" + zoneId + "/settings/always_use_https", null, null));
        settings.put("minTlsVersion", gatewayService.invokeJson(
                accountId, "SSL", "GET_MIN_TLS", HttpMethod.GET,
                "/zones/" + zoneId + "/settings/min_tls_version", null, null));
        settings.put("tls13", gatewayService.invokeJson(
                accountId, "SSL", "GET_TLS13", HttpMethod.GET,
                "/zones/" + zoneId + "/settings/tls_1_3", null, null));
        return ApiResponse.ok(settings);
    }

    @PatchMapping("/settings")
    public ApiResponse<Map<String, JsonNode>> patchSettings(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");

        Map<String, JsonNode> changed = new HashMap<>();
        if (payload.containsKey("ssl")) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("value", payload.get("ssl"));
            changed.put("ssl", gatewayService.invokeJson(
                    request.getAccountId(), "SSL", "PATCH_SSL", HttpMethod.PATCH,
                    "/zones/" + zoneId + "/settings/ssl", null, body
            ));
        }
        if (payload.containsKey("alwaysUseHttps")) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("value", payload.get("alwaysUseHttps"));
            changed.put("alwaysUseHttps", gatewayService.invokeJson(
                    request.getAccountId(), "SSL", "PATCH_ALWAYS_HTTPS", HttpMethod.PATCH,
                    "/zones/" + zoneId + "/settings/always_use_https", null, body
            ));
        }
        if (payload.containsKey("minTlsVersion")) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("value", payload.get("minTlsVersion"));
            changed.put("minTlsVersion", gatewayService.invokeJson(
                    request.getAccountId(), "SSL", "PATCH_MIN_TLS", HttpMethod.PATCH,
                    "/zones/" + zoneId + "/settings/min_tls_version", null, body
            ));
        }
        if (payload.containsKey("tls13")) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("value", payload.get("tls13"));
            changed.put("tls13", gatewayService.invokeJson(
                    request.getAccountId(), "SSL", "PATCH_TLS13", HttpMethod.PATCH,
                    "/zones/" + zoneId + "/settings/tls_1_3", null, body
            ));
        }
        return ApiResponse.ok(changed);
    }
}
