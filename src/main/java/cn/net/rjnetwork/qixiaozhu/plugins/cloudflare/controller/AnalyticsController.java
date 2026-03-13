package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareGatewayService;
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
@RequestMapping("/cloudflare/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final CloudflareGatewayService gatewayService;

    @GetMapping("/dashboard")
    public ApiResponse<JsonNode> dashboard(
            @RequestParam Long accountId,
            @RequestParam String zoneId,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until
    ) {
        Map<String, Object> query = new HashMap<>();
        query.put("since", since);
        query.put("until", until);
        JsonNode result = gatewayService.invokeJson(
                accountId,
                "ANALYTICS",
                "DASHBOARD",
                HttpMethod.GET,
                "/zones/" + zoneId + "/analytics/dashboard",
                query,
                null
        );
        return ApiResponse.ok(result);
    }
}
