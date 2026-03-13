package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.entity.CloudflareOperationLogEntity;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.mapper.CloudflareOperationLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CloudflareOperationLogService extends ServiceImpl<CloudflareOperationLogMapper, CloudflareOperationLogEntity> {

    public void addLog(
            Long accountId,
            String module,
            String action,
            String endpoint,
            String requestBody,
            Integer responseCode,
            boolean success,
            String message
    ) {
        CloudflareOperationLogEntity entity = new CloudflareOperationLogEntity();
        entity.setAccountId(accountId);
        entity.setModule(module);
        entity.setAction(action);
        entity.setEndpoint(endpoint);
        entity.setRequestBody(truncate(requestBody, 4000));
        entity.setResponseCode(responseCode);
        entity.setSuccess(success ? 1 : 0);
        entity.setMessage(truncate(message, 1000));
        entity.setCreatedAt(LocalDateTime.now());
        save(entity);
    }

    public List<CloudflareOperationLogEntity> latest(int limit) {
        return lambdaQuery()
                .orderByDesc(CloudflareOperationLogEntity::getId)
                .last("limit " + Math.max(1, limit))
                .list();
    }

    private String truncate(String input, int max) {
        if (input == null || input.length() <= max) {
            return input;
        }
        return input.substring(0, max);
    }
}
