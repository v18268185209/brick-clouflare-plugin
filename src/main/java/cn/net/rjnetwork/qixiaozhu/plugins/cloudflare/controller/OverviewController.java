package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareAccountService;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareGatewayService;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareOperationLogService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cloudflare/overview")
@RequiredArgsConstructor
public class OverviewController {

    private final CloudflareAccountService accountService;
    private final CloudflareOperationLogService operationLogService;
    private final CloudflareGatewayService gatewayService;

    @GetMapping
    public ApiResponse<Map<String, Object>> overview(@RequestParam(required = false) Long accountId) {
        Map<String, Object> result = new HashMap<>();
        result.put("accountCount", accountService.count());
        result.put("latestLogs", operationLogService.latest(10));
        if (accountId != null) {
            JsonNode zones = gatewayService.invokeJson(
                    accountId,
                    "ZONE",
                    "LIST",
                    HttpMethod.GET,
                    "/zones",
                    Map.of("page", 1, "per_page", 20),
                    null
            );
            result.put("zones", zones);
        }
        return ApiResponse.ok(result);
    }
}
