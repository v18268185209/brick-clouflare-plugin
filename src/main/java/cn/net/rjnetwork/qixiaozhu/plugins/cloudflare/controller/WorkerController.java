package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.CloudflareInvokeRequest;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cloudflare/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/scripts")
    public ApiResponse<JsonNode> listScripts(
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "WORKER",
                "LIST_SCRIPT",
                HttpMethod.GET,
                "/accounts/" + cfAccountId + "/workers/scripts",
                null,
                null
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/scripts/{scriptName}")
    public ApiResponse<String> getScript(
            @PathVariable String scriptName,
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        String result = gatewayService.invokeText(
                accountId,
                "WORKER",
                "GET_SCRIPT",
                HttpMethod.GET,
                "/accounts/" + cfAccountId + "/workers/scripts/" + scriptName,
                null,
                null,
                MediaType.TEXT_PLAIN
        );
        return ApiResponse.ok(result);
    }

    @PutMapping("/scripts/{scriptName}")
    public ApiResponse<JsonNode> putScript(
            @PathVariable String scriptName,
            @RequestBody @Valid CloudflareInvokeRequest request
    ) {
        Map<String, Object> payload = request.getPayload();
        String cfAccountId = gatewayService.resolveCloudflareAccountId(
                request.getAccountId(),
                (String) payload.get("cloudflareAccountId")
        );
        String script = ControllerPayloads.requireString(payload, "script");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "WORKER",
                "PUT_SCRIPT",
                HttpMethod.PUT,
                "/accounts/" + cfAccountId + "/workers/scripts/" + scriptName,
                null,
                script,
                MediaType.parseMediaType("application/javascript")
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/scripts/{scriptName}")
    public ApiResponse<JsonNode> deleteScript(
            @PathVariable String scriptName,
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "WORKER",
                "DELETE_SCRIPT",
                HttpMethod.DELETE,
                "/accounts/" + cfAccountId + "/workers/scripts/" + scriptName,
                null,
                null
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/routes")
    public ApiResponse<JsonNode> listRoutes(
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "WORKER",
                "LIST_ROUTE",
                HttpMethod.GET,
                "/zones/" + zoneId + "/workers/routes",
                null,
                null
        );
        return ApiResponse.ok(result);
    }

    @PostMapping("/routes")
    public ApiResponse<JsonNode> createRoute(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "WORKER",
                "CREATE_ROUTE",
                HttpMethod.POST,
                "/zones/" + zoneId + "/workers/routes",
                null,
                ControllerPayloads.copyWithout(payload, "zoneId")
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/routes/{routeId}")
    public ApiResponse<JsonNode> deleteRoute(
            @PathVariable String routeId,
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "WORKER",
                "DELETE_ROUTE",
                HttpMethod.DELETE,
                "/zones/" + zoneId + "/workers/routes/" + routeId,
                null,
                null
        );
        return ApiResponse.ok(result);
    }
}
