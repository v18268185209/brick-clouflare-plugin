package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudflareCredential {

    private Long accountId;

    private String authType;

    private String apiToken;

    private String apiKey;

    private String apiEmail;

    private String cloudflareAccountId;
}
