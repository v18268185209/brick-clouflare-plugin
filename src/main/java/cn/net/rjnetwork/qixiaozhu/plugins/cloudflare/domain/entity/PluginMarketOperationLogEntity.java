package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cf_pm_operation_log")
public class PluginMarketOperationLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String operatorName;

    private String action;

    private String target;

    private String requestBody;

    private Integer success;

    private String message;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
