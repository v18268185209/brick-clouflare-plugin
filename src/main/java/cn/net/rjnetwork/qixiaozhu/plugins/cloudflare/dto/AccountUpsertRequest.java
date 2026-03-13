package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountUpsertRequest {

    @NotBlank(message = "displayName is required")
    private String displayName;

    @NotBlank(message = "authType is required: API_TOKEN or GLOBAL_KEY")
    private String authType;

    private String apiToken;

    private String apiKey;

    private String apiEmail;

    private String cloudflareAccountId;

    private String defaultZoneId;

    @NotNull(message = "enabled is required")
    private Integer enabled;

    private String remark;
}
