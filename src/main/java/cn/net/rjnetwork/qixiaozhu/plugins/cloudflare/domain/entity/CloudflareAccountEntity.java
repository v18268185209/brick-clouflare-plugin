package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cf_account")
public class CloudflareAccountEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String displayName;

    private String authType;

    private String apiTokenEnc;

    private String apiKeyEnc;

    private String apiEmailEnc;

    /**
     * Cloudflare account identifier, such as 023e105f4ecef8ad9ca31a8372d0c353.
     */
    private String cloudflareAccountId;

    private String defaultZoneId;

    private Integer enabled;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
