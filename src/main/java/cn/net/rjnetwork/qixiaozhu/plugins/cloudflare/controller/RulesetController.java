package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.CloudflareInvokeRequest;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/cloudflare/rulesets")
@RequiredArgsConstructor
public class RulesetController {

    private static final String DEFAULT_PHASE = "http_request_firewall_custom";

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/entrypoint")
    public ApiResponse<JsonNode> getEntrypoint(
            @RequestParam Long accountId,
            @RequestParam String zoneId,
            @RequestParam(defaultValue = DEFAULT_PHASE) String phase
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "RULESET",
                "GET_ENTRYPOINT",
                HttpMethod.GET,
                "/zones/" + zoneId + "/rulesets/phases/" + phase + "/entrypoint",
                null,
                null
        );
        return ApiResponse.ok(result);
    }

    @PutMapping("/entrypoint")
    public ApiResponse<JsonNode> updateEntrypoint(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        String phase = ControllerPayloads.requireString(payload, "phase");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "RULESET",
                "UPDATE_ENTRYPOINT",
                HttpMethod.PUT,
                "/zones/" + zoneId + "/rulesets/phases/" + phase + "/entrypoint",
                null,
                ControllerPayloads.copyWithout(payload, "zoneId", "phase")
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/{rulesetId}")
    public ApiResponse<JsonNode> getRuleset(
            @PathVariable String rulesetId,
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "RULESET",
                "GET_RULESET",
                HttpMethod.GET,
                "/zones/" + zoneId + "/rulesets/" + rulesetId,
                null,
                null
        );
        return ApiResponse.ok(result);
    }

    @PostMapping("/{rulesetId}/rules")
    public ApiResponse<JsonNode> createRule(
            @PathVariable String rulesetId,
            @RequestBody @Valid CloudflareInvokeRequest request
    ) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        Object rule = payload.get("rule");
        Object body = rule == null ? ControllerPayloads.copyWithout(payload, "zoneId") : rule;
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "RULESET",
                "CREATE_RULE",
                HttpMethod.POST,
                "/zones/" + zoneId + "/rulesets/" + rulesetId + "/rules",
                null,
                body
        );
        return ApiResponse.ok(result);
    }

    @PutMapping("/{rulesetId}/rules/{ruleId}")
    public ApiResponse<JsonNode> updateRule(
            @PathVariable String rulesetId,
            @PathVariable String ruleId,
            @RequestBody @Valid CloudflareInvokeRequest request
    ) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        Object rule = payload.get("rule");
        Object body = rule == null ? ControllerPayloads.copyWithout(payload, "zoneId") : rule;
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "RULESET",
                "UPDATE_RULE",
                HttpMethod.PUT,
                "/zones/" + zoneId + "/rulesets/" + rulesetId + "/rules/" + ruleId,
                null,
                body
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{rulesetId}/rules/{ruleId}")
    public ApiResponse<JsonNode> deleteRule(
            @PathVariable String rulesetId,
            @PathVariable String ruleId,
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "RULESET",
                "DELETE_RULE",
                HttpMethod.DELETE,
                "/zones/" + zoneId + "/rulesets/" + rulesetId + "/rules/" + ruleId,
                null,
                null
        );
        return ApiResponse.ok(result);
    }
}

