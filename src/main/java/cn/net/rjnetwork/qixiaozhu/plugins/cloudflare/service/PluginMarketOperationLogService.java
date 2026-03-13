package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.entity.PluginMarketOperationLogEntity;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.mapper.PluginMarketOperationLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PluginMarketOperationLogService extends ServiceImpl<PluginMarketOperationLogMapper, PluginMarketOperationLogEntity> {

    public void addLog(String operatorName, String action, String target, String requestBody, boolean success, String message) {
        PluginMarketOperationLogEntity entity = new PluginMarketOperationLogEntity();
        entity.setOperatorName(operatorName);
        entity.setAction(action);
        entity.setTarget(target);
        entity.setRequestBody(requestBody);
        entity.setSuccess(success ? 1 : 0);
        entity.setMessage(message);
        entity.setCreatedAt(LocalDateTime.now());
        save(entity);
    }

    public List<PluginMarketOperationLogEntity> latest(int limit) {
        int safeLimit = Math.max(1, Math.min(200, limit));
        return lambdaQuery()
                .orderByDesc(PluginMarketOperationLogEntity::getId)
                .last("limit " + safeLimit)
                .list();
    }
}
