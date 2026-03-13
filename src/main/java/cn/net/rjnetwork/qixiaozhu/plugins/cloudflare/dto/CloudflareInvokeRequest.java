package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CloudflareInvokeRequest {

    @NotNull(message = "accountId is required")
    private Long accountId;

    @NotNull(message = "payload is required")
    private Map<String, Object> payload;
}
