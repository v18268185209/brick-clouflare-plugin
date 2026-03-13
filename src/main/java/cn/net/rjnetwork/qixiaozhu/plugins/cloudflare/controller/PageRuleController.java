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
@RequestMapping("/cloudflare/page-rules")
@RequiredArgsConstructor
public class PageRuleController {

    private final CloudflareGatewayService gatewayService;

    @GetMapping
    public ApiResponse<JsonNode> list(
            @RequestParam Long accountId,
            @RequestParam String zoneId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer perPage
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "PAGE_RULE",
                "LIST",
                HttpMethod.GET,
                "/zones/" + zoneId + "/pagerules",
                Map.of("page", page, "per_page", perPage),
                null
        );
        return ApiResponse.ok(result);
    }

    @PostMapping
    public ApiResponse<JsonNode> create(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "PAGE_RULE",
                "CREATE",
                HttpMethod.POST,
                "/zones/" + zoneId + "/pagerules",
                null,
                ControllerPayloads.copyWithout(payload, "zoneId")
        );
        return ApiResponse.ok(result);
    }

    @PutMapping("/{ruleId}")
    public ApiResponse<JsonNode> update(
            @PathVariable String ruleId,
            @RequestBody @Valid CloudflareInvokeRequest request
    ) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "PAGE_RULE",
                "UPDATE",
                HttpMethod.PUT,
                "/zones/" + zoneId + "/pagerules/" + ruleId,
                null,
                ControllerPayloads.copyWithout(payload, "zoneId")
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{ruleId}")
    public ApiResponse<JsonNode> delete(
            @PathVariable String ruleId,
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "PAGE_RULE",
                "DELETE",
                HttpMethod.DELETE,
                "/zones/" + zoneId + "/pagerules/" + ruleId,
                null,
                null
        );
        return ApiResponse.ok(result);
    }
}
