package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.BizException;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.entity.CloudflareAccountEntity;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.AccountUpsertRequest;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.AccountView;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.CloudflareCredential;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareAccountService;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cloudflare/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final CloudflareAccountService accountService;
    private final CloudflareGatewayService gatewayService;

    @GetMapping
    public ApiResponse<List<AccountView>> list() {
        return ApiResponse.ok(accountService.listViews());
    }

    @PostMapping
    public ApiResponse<AccountView> create(@RequestBody @Valid AccountUpsertRequest request) {
        CloudflareAccountEntity entity = accountService.create(request);
        return ApiResponse.ok(accountService.listViews()
                .stream()
                .filter(v -> v.getId().equals(entity.getId()))
                .findFirst()
                .orElseThrow(() -> new BizException("created account cannot be loaded")));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountView> update(@PathVariable Long id, @RequestBody @Valid AccountUpsertRequest request) {
        CloudflareAccountEntity entity = accountService.update(id, request);
        return ApiResponse.ok(accountService.listViews()
                .stream()
                .filter(v -> v.getId().equals(entity.getId()))
                .findFirst()
                .orElseThrow(() -> new BizException("updated account cannot be loaded")));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        accountService.removeById(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/verify")
    public ApiResponse<JsonNode> verify(@PathVariable Long id) {
        CloudflareCredential credential = accountService.resolveCredential(id);
        boolean isGlobalKey = "GLOBAL_KEY".equalsIgnoreCase(credential.getAuthType());
        JsonNode response = gatewayService.invokeJson(
                id,
                "ACCOUNT",
                isGlobalKey ? "VERIFY_GLOBAL_KEY" : "VERIFY_TOKEN",
                HttpMethod.GET,
                isGlobalKey ? "/user" : "/user/tokens/verify",
                null,
                null
        );
        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}/zones")
    public ApiResponse<JsonNode> listZones(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer perPage
    ) {
        Map<String, Object> query = new HashMap<>();
        query.put("page", page);
        query.put("per_page", perPage);
        JsonNode response = gatewayService.invokeJson(
                id,
                "ZONE",
                "LIST",
                HttpMethod.GET,
                "/zones",
                query,
                null
        );
        return ApiResponse.ok(response);
    }
}
