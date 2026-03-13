package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccountView {

    private Long id;
    private String displayName;
    private String authType;
    private String tokenMasked;
    private String keyMasked;
    private String emailMasked;
    private String cloudflareAccountId;
    private String defaultZoneId;
    private Integer enabled;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
