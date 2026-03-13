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
@RequestMapping("/cloudflare/r2")
@RequiredArgsConstructor
public class R2Controller {

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/buckets")
    public ApiResponse<JsonNode> listBuckets(
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "R2",
                "LIST_BUCKET",
                HttpMethod.GET,
                "/accounts/" + cfAccountId + "/r2/buckets",
                null,
                null
        );
        return ApiResponse.ok(result);
    }

    @PostMapping("/buckets")
    public ApiResponse<JsonNode> createBucket(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String bucketName = ControllerPayloads.requireString(payload, "bucketName");
        String cfAccountId = gatewayService.resolveCloudflareAccountId(
                request.getAccountId(),
                (String) payload.get("cloudflareAccountId")
        );
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("name", bucketName);
        if (payload.get("locationHint") != null) {
            body.put("locationHint", payload.get("locationHint"));
        }
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "R2",
                "CREATE_BUCKET",
                HttpMethod.POST,
                "/accounts/" + cfAccountId + "/r2/buckets",
                null,
                body
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/buckets/{bucketName}")
    public ApiResponse<JsonNode> deleteBucket(
            @PathVariable String bucketName,
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "R2",
                "DELETE_BUCKET",
                HttpMethod.DELETE,
                "/accounts/" + cfAccountId + "/r2/buckets/" + bucketName,
                null,
                null
        );
        return ApiResponse.ok(result);
    }
}
