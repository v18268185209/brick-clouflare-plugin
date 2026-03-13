package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.PluginMarketGatewayService;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.PluginMarketOperationLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/cloudflare/worker-admin")
@RequiredArgsConstructor
public class WorkerAdminController {

    private final PluginMarketGatewayService gatewayService;
    private final PluginMarketOperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @GetMapping("/me")
    public ApiResponse<JsonNode> me() {
        return ApiResponse.ok(gatewayService.callWorkerCoreAdmin("/admin/api/me", HttpMethod.GET, null, null));
    }

    @GetMapping("/plan-config")
    public ApiResponse<JsonNode> getPlanConfig() {
        return ApiResponse.ok(gatewayService.callWorkerCoreAdmin("/admin/api/plan-config", HttpMethod.GET, null, null));
    }

    @PutMapping("/plan-config")
    public ApiResponse<JsonNode> updatePlanConfig(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/plan-config", HttpMethod.PUT, null, body);
        addOperationLog(request, "worker-admin.plan-config.save", "plan-config", body, true, "ok");
        return ApiResponse.ok(data);
    }

    @GetMapping("/payment-config")
    public ApiResponse<JsonNode> getPaymentConfig() {
        return ApiResponse.ok(gatewayService.callWorkerCoreAdmin("/admin/api/payment-config", HttpMethod.GET, null, null));
    }

    @PutMapping("/payment-config")
    public ApiResponse<JsonNode> updatePaymentConfig(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/payment-config", HttpMethod.PUT, null, body);
        addOperationLog(request, "worker-admin.payment-config.save", "payment-config", body, true, "ok");
        return ApiResponse.ok(data);
    }

    @GetMapping("/email-config")
    public ApiResponse<JsonNode> getEmailConfig() {
        return ApiResponse.ok(gatewayService.callWorkerCoreAdmin("/admin/api/email-config", HttpMethod.GET, null, null));
    }

    @PutMapping("/email-config")
    public ApiResponse<JsonNode> updateEmailConfig(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/email-config", HttpMethod.PUT, null, body);
        addOperationLog(request, "worker-admin.email-config.save", "email-config", body, true, "ok");
        return ApiResponse.ok(data);
    }

    @PostMapping("/email-config/test")
    public ApiResponse<JsonNode> testEmail(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/email-config/test", HttpMethod.POST, null, body);
        addOperationLog(request, "worker-admin.email-config.test", "email-config-test", body, true, "ok");
        return ApiResponse.ok(data);
    }

    @GetMapping("/menu-codes")
    public ApiResponse<JsonNode> getMenuCodes() {
        return ApiResponse.ok(gatewayService.callWorkerCoreAdmin("/admin/api/menu-codes", HttpMethod.GET, null, null));
    }

    @PutMapping("/menu-codes")
    public ApiResponse<JsonNode> updateMenuCodes(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/menu-codes", HttpMethod.PUT, null, body);
        addOperationLog(request, "worker-admin.menu-codes.save", "menu-codes", body, true, "ok");
        return ApiResponse.ok(data);
    }

    @GetMapping("/other-config")
    public ApiResponse<JsonNode> getOtherConfig() {
        return ApiResponse.ok(gatewayService.callWorkerCoreAdmin("/admin/api/other-config", HttpMethod.GET, null, null));
    }

    @PutMapping("/other-config")
    public ApiResponse<JsonNode> updateOtherConfig(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/other-config", HttpMethod.PUT, null, body);
        addOperationLog(request, "worker-admin.other-config.save", "other-config", body, true, "ok");
        return ApiResponse.ok(data);
    }

    @GetMapping("/config/list")
    public ApiResponse<JsonNode> configList() {
        return ApiResponse.ok(gatewayService.callWorkerCoreAdmin("/admin/api/config/list", HttpMethod.GET, null, null));
    }

    @PostMapping("/password/change")
    public ApiResponse<JsonNode> changePassword(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/password/change", HttpMethod.POST, null, body);
        addOperationLog(request, "worker-admin.password.change", "password", body, true, "ok");
        return ApiResponse.ok(data);
    }

    @GetMapping("/licenses")
    public ApiResponse<JsonNode> listLicenses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String status
    ) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("keyword", keyword);
        query.put("plan", plan);
        query.put("status", status);
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/licenses", HttpMethod.GET, query, null);
        return ApiResponse.ok(data);
    }

    @PostMapping("/licenses/update-duration")
    public ApiResponse<JsonNode> updateLicenseDuration(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/licenses/update-duration", HttpMethod.POST, null, body);
        addOperationLog(request, "worker-admin.license.update-duration", normalizeTarget(body), body, true, "ok");
        return ApiResponse.ok(data);
    }

    @PostMapping("/licenses/update-expire")
    public ApiResponse<JsonNode> updateLicenseExpire(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/licenses/update-expire", HttpMethod.POST, null, body);
        addOperationLog(request, "worker-admin.license.update-expire", normalizeTarget(body), body, true, "ok");
        return ApiResponse.ok(data);
    }

    @PostMapping("/licenses/adjust-expire")
    public ApiResponse<JsonNode> adjustLicenseExpire(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/licenses/adjust-expire", HttpMethod.POST, null, body);
        addOperationLog(request, "worker-admin.license.adjust-expire", normalizeTarget(body), body, true, "ok");
        return ApiResponse.ok(data);
    }

    @PostMapping("/licenses/delete")
    public ApiResponse<JsonNode> deleteLicense(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        JsonNode data = gatewayService.callWorkerCoreAdmin("/admin/api/licenses/delete", HttpMethod.POST, null, body);
        addOperationLog(request, "worker-admin.license.delete", normalizeTarget(body), body, true, "ok");
        return ApiResponse.ok(data);
    }

    private String normalizeTarget(Map<String, Object> body) {
        Object id = body == null ? null : body.get("id");
        return "license:" + String.valueOf(id == null ? "" : id);
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
