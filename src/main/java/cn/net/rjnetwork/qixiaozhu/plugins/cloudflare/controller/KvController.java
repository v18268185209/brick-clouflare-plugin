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
@RequestMapping("/cloudflare/kv")
@RequiredArgsConstructor
public class KvController {

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/namespaces")
    public ApiResponse<JsonNode> listNamespaces(
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "100") Integer perPage
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "KV",
                "LIST_NAMESPACE",
                HttpMethod.GET,
                "/accounts/" + cfAccountId + "/storage/kv/namespaces",
                Map.of("page", page, "per_page", perPage),
                null
        );
        return ApiResponse.ok(result);
    }

    @PostMapping("/namespaces")
    public ApiResponse<JsonNode> createNamespace(@RequestBody @Valid CloudflareInvokeRequest request) {
        Map<String, Object> payload = request.getPayload();
        String title = ControllerPayloads.requireString(payload, "title");
        String cfAccountId = gatewayService.resolveCloudflareAccountId(
                request.getAccountId(),
                (String) payload.get("cloudflareAccountId")
        );
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "KV",
                "CREATE_NAMESPACE",
                HttpMethod.POST,
                "/accounts/" + cfAccountId + "/storage/kv/namespaces",
                null,
                Map.of("title", title)
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/namespaces/{namespaceId}")
    public ApiResponse<JsonNode> deleteNamespace(
            @PathVariable String namespaceId,
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "KV",
                "DELETE_NAMESPACE",
                HttpMethod.DELETE,
                "/accounts/" + cfAccountId + "/storage/kv/namespaces/" + namespaceId,
                null,
                null
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/namespaces/{namespaceId}/keys")
    public ApiResponse<JsonNode> listKeys(
            @PathVariable String namespaceId,
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId,
            @RequestParam(required = false) String prefix,
            @RequestParam(defaultValue = "1000") Integer limit
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        java.util.Map<String, Object> query = new java.util.HashMap<>();
        query.put("limit", limit);
        if (prefix != null) {
            query.put("prefix", prefix);
        }
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "KV",
                "LIST_KEYS",
                HttpMethod.GET,
                "/accounts/" + cfAccountId + "/storage/kv/namespaces/" + namespaceId + "/keys",
                query,
                null
        );
        return ApiResponse.ok(result);
    }

    @GetMapping("/namespaces/{namespaceId}/values/{key}")
    public ApiResponse<String> getValue(
            @PathVariable String namespaceId,
            @PathVariable String key,
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        String result = gatewayService.invokeText(
                accountId,
                "KV",
                "GET_VALUE",
                HttpMethod.GET,
                "/accounts/" + cfAccountId + "/storage/kv/namespaces/" + namespaceId + "/values/" + key,
                null,
                null,
                MediaType.TEXT_PLAIN
        );
        return ApiResponse.ok(result);
    }

    @PutMapping("/namespaces/{namespaceId}/values/{key}")
    public ApiResponse<JsonNode> putValue(
            @PathVariable String namespaceId,
            @PathVariable String key,
            @RequestBody @Valid CloudflareInvokeRequest request
    ) {
        Map<String, Object> payload = request.getPayload();
        String cfAccountId = gatewayService.resolveCloudflareAccountId(
                request.getAccountId(),
                (String) payload.get("cloudflareAccountId")
        );
        String value = ControllerPayloads.requireString(payload, "value");
        JsonNode result = gatewayService.invokeJson(
                request.getAccountId(),
                "KV",
                "PUT_VALUE",
                HttpMethod.PUT,
                "/accounts/" + cfAccountId + "/storage/kv/namespaces/" + namespaceId + "/values/" + key,
                null,
                value,
                MediaType.TEXT_PLAIN
        );
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/namespaces/{namespaceId}/values/{key}")
    public ApiResponse<JsonNode> deleteValue(
            @PathVariable String namespaceId,
            @PathVariable String key,
            @RequestParam Long accountId,
            @RequestParam(required = false) String cloudflareAccountId
    ) {
        String cfAccountId = gatewayService.resolveCloudflareAccountId(accountId, cloudflareAccountId);
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "KV",
                "DELETE_VALUE",
                HttpMethod.DELETE,
                "/accounts/" + cfAccountId + "/storage/kv/namespaces/" + namespaceId + "/values/" + key,
                null,
                null
        );
        return ApiResponse.ok(result);
    }
}
