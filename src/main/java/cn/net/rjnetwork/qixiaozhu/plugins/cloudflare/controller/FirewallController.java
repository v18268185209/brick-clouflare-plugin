package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.CloudflareInvokeRequest;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cloudflare/firewall")
@RequiredArgsConstructor
public class FirewallController {

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/rules")
    public ApiResponse<JsonNode> listRules(
            @RequestParam Long accountId,
            @RequestParam String zoneId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer perPage
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "FIREWALL",
                "LIST_RULE",
                HttpMethod.GET,
                "/zones/" + zoneId + "/firewall/rules",
                Map.of("page", page, "per_page", perPage),
                null
        );
        return ApiResponse.ok(result);
    }

    @PostMapping("/rules")
    public ApiResponse<JsonNode> createRules(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        Object rules = payload.get("rules");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "FIREWALL",
                "CREATE_RULE",
                HttpMethod.POST,
                "/zones/" + zoneId + "/firewall/rules",
                null,
                rules
        );
        return ApiResponse.ok(result);
    }

    @PutMapping("/rules/{ruleId}")
    public ApiResponse<JsonNode> updateRule(
            @PathVariable String ruleId,
            @RequestBody @Valid CloudflareInvokeRequest request
    ) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "FIREWALL",
                "UPDATE_RULE",
                HttpMethod.PUT,
                "/zones/" + zoneId + "/firewall/rules/" + ruleId,
                null,
                ControllerPayloads.copyWithout(payload, "zoneId")
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/rules/{ruleId}")
    public ApiResponse<JsonNode> deleteRule(
            @PathVariable String ruleId,
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "FIREWALL",
                "DELETE_RULE",
                HttpMethod.DELETE,
                "/zones/" + zoneId + "/firewall/rules/" + ruleId,
                null,
                null
        );
        return ApiResponse.ok(result);
    }
}
