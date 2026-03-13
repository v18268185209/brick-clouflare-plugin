package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.controller;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.ApiResponse;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.entity.CloudflareOperationLogEntity;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service.CloudflareOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cloudflare/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final CloudflareOperationLogService operationLogService;

    @GetMapping
    public ApiResponse<List<CloudflareOperationLogEntity>> latest(
            @RequestParam(defaultValue = "50") Integer limit
    ) {
        return ApiResponse.ok(operationLogService.latest(limit));
    }

    @DeleteMapping
    public ApiResponse<Void> clear() {
        operationLogService.getBaseMapper().delete(null);
        return ApiResponse.ok(null);
    }
}
