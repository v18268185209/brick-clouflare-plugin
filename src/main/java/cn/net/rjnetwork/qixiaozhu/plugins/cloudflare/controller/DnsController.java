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
@RequestMapping("/cloudflare/dns")
@RequiredArgsConstructor
public class DnsController {

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/records")
    public ApiResponse<JsonNode> listRecords(
            @RequestParam Long accountId,
            @RequestParam String zoneId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer perPage,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String content
    ) {
        Map<String, Object> query = new HashMap<>();
        query.put("page", page);
        query.put("per_page", perPage);
        query.put("type", type);
        query.put("name", name);
        query.put("content", content);
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "DNS",
                "LIST_RECORD",
                HttpMethod.GET,
                "/zones/" + zoneId + "/dns_records",
                query,
                null
        );
        return ApiResponse.ok(result);
    }

    @PostMapping("/records")
    public ApiResponse<JsonNode> createRecord(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "DNS",
                "CREATE_RECORD",
                HttpMethod.POST,
                "/zones/" + zoneId + "/dns_records",
                null,
                ControllerPayloads.copyWithout(payload, "zoneId")
        );
        return ApiResponse.ok(result);
    }

    @PutMapping("/records/{recordId}")
    public ApiResponse<JsonNode> updateRecord(
            @PathVariable String recordId,
            @RequestBody @Valid CloudflareInvokeRequest request
    ) {
        Map<String, Object> payload = request.getPayload();
        String zoneId = ControllerPayloads.requireString(payload, "zoneId");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "DNS",
                "UPDATE_RECORD",
                HttpMethod.PUT,
                "/zones/" + zoneId + "/dns_records/" + recordId,
                null,
                ControllerPayloads.copyWithout(payload, "zoneId")
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/records/{recordId}")
    public ApiResponse<JsonNode> deleteRecord(
            @PathVariable String recordId,
            @RequestParam Long accountId,
            @RequestParam String zoneId
    ) {
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "DNS",
                "DELETE_RECORD",
                HttpMethod.DELETE,
                "/zones/" + zoneId + "/dns_records/" + recordId,
                null,
                null
        );
        return ApiResponse.ok(result);
    }
}
