package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cf_operation_log")
public class CloudflareOperationLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long accountId;

    private String module;

    private String action;

    private String endpoint;

    private String requestBody;

    private Integer responseCode;

    private Integer success;

    private String message;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
