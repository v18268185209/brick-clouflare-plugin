package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.BizException;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.entity.PluginMarketOperationLogEntity;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.PluginMarketGatewayService;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.PluginMarketOperationLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cloudflare/plugin-market-admin")
@RequiredArgsConstructor
public class PluginMarketAdminController {

    private final PluginMarketGatewayService gatewayService;
    private final PluginMarketOperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @GetMapping("/health")
    public ApiResponse<JsonNode> health() {
        return ApiResponse.ok(gatewayService.health());
    }

    @GetMapping("/plugins")
    public ApiResponse<JsonNode> listPlugins(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("keyword", keyword);
        query.put("status", status);
        query.put("page", sanitizePositive(page, 1));
        query.put("pageSize", sanitizePositive(pageSize, 20));
        return ApiResponse.ok(gatewayService.listPlugins(query));
    }

    @GetMapping("/releases")
    public ApiResponse<JsonNode> listReleases(@RequestParam String pluginCode) {
        if (!StringUtils.hasText(pluginCode)) {
            throw new BizException("pluginCode is required");
        }
        return ApiResponse.ok(gatewayService.listReleases(pluginCode.trim()));
    }

    @PostMapping("/plugin/save")
    public ApiResponse<JsonNode> savePlugin(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String pluginCode = normalizeString(body.get("pluginCode"));
        JsonNode data = gatewayService.savePlugin(body);
        addOperationLog(request, "plugin.save", pluginCode, body, true, "ok");
        return ApiResponse.ok(data);
    }

    @PostMapping("/release/save")
    public ApiResponse<JsonNode> saveRelease(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String target = normalizeString(body.get("pluginCode")) + "@" + normalizeString(body.get("version"));
        JsonNode data = gatewayService.saveRelease(body);
        addOperationLog(request, "release.save", target, body, true, "ok");
        return ApiResponse.ok(data);
    }

    @PostMapping("/plugin/publish")
    public ApiResponse<JsonNode> publishPlugin(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return changeStatus("/admin/api/plugin-market/plugin/publish", "plugin.publish", body, request);
    }

    @PostMapping("/plugin/offline")
    public ApiResponse<JsonNode> offlinePlugin(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return changeStatus("/admin/api/plugin-market/plugin/offline", "plugin.offline", body, request);
    }

    @PostMapping("/plugin/draft")
    public ApiResponse<JsonNode> draftPlugin(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return changeStatus("/admin/api/plugin-market/plugin/draft", "plugin.draft", body, request);
    }

    @PostMapping("/plugin/delete")
    public ApiResponse<JsonNode> deletePlugin(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        return changeStatus("/admin/api/plugin-market/plugin/delete", "plugin.delete", body, request);
    }

    @GetMapping("/orders")
    public ApiResponse<JsonNode> listOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("keyword", keyword);
        query.put("status", status);
        query.put("page", sanitizePositive(page, 1));
        query.put("pageSize", sanitizePositive(pageSize, 20));
        return ApiResponse.ok(gatewayService.listOrders(query));
    }

    @GetMapping("/operation-logs")
    public ApiResponse<List<PluginMarketOperationLogEntity>> operationLogs(
            @RequestParam(defaultValue = "60") Integer limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit == null ? 60 : limit, 200));
        return ApiResponse.ok(operationLogService.latest(safeLimit));
    }

    private ApiResponse<JsonNode> changeStatus(
            String actionPath,
            String action,
            Map<String, Object> body,
            HttpServletRequest request
    ) {
        String pluginCode = normalizeString(body.get("pluginCode"));
        if (!StringUtils.hasText(pluginCode)) {
            throw new BizException("pluginCode is required");
        }
        JsonNode data = gatewayService.changePluginStatus(actionPath, pluginCode);
        addOperationLog(request, action, pluginCode, body, true, "ok");
        return ApiResponse.ok(data);
    }

    private int sanitizePositive(Integer value, int fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }

    private String normalizeString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void addOperationLog(
            HttpServletRequest request,
            String action,
            String target,
            Map<String, Object> requestBody,
            boolean success,
            String message
    ) {
        String operator = resolveOperatorName(request);
        String payload;
        try {
            payload = requestBody == null ? "" : objectMapper.writeValueAsString(requestBody);
        } catch (Exception ex) {
            payload = String.valueOf(requestBody);
        }
        operationLogService.addLog(operator, action, target, payload, success, message);
    }

    private String resolveOperatorName(HttpServletRequest request) {
        String[] keys = new String[]{"x-user-name", "headerUserName", "headerRealName", "x-operator"};
        for (String key : keys) {
            String value = request.getHeader(key);
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "eqadmin-user";
    }
}
