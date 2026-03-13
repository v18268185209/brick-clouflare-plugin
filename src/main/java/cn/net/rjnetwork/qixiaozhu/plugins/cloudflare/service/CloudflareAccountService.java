package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.BizException;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.entity.CloudflareAccountEntity;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.domain.enums.AuthType;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.AccountUpsertRequest;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.AccountView;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.dto.CloudflareCredential;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.mapper.CloudflareAccountMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CloudflareAccountService extends ServiceImpl<CloudflareAccountMapper, CloudflareAccountEntity> {

    private final CredentialCryptoService cryptoService;

    public CloudflareAccountService(CredentialCryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public List<AccountView> listViews() {
        return lambdaQuery()
                .orderByDesc(CloudflareAccountEntity::getId)
                .list()
                .stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    public CloudflareAccountEntity create(AccountUpsertRequest req) {
        validateAuth(req);
        CloudflareAccountEntity entity = new CloudflareAccountEntity();
        fill(entity, req);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        save(entity);
        return entity;
    }

    public CloudflareAccountEntity update(Long id, AccountUpsertRequest req) {
        CloudflareAccountEntity entity = getById(id);
        if (entity == null) {
            throw new BizException("account not found: " + id);
        }
        validateAuthForUpdate(req, entity);
        fill(entity, req);
        entity.setUpdatedAt(LocalDateTime.now());
        updateById(entity);
        return entity;
    }

    public CloudflareCredential resolveCredential(Long id) {
        CloudflareAccountEntity entity = getById(id);
        if (entity == null) {
            throw new BizException("account not found: " + id);
        }
        if (entity.getEnabled() == null || entity.getEnabled() != 1) {
            throw new BizException("account is disabled: " + id);
        }
        return CloudflareCredential.builder()
                .accountId(entity.getId())
                .authType(entity.getAuthType())
                .apiToken(cryptoService.decrypt(entity.getApiTokenEnc()))
                .apiKey(cryptoService.decrypt(entity.getApiKeyEnc()))
                .apiEmail(cryptoService.decrypt(entity.getApiEmailEnc()))
                .cloudflareAccountId(entity.getCloudflareAccountId())
                .build();
    }

    private void fill(CloudflareAccountEntity entity, AccountUpsertRequest req) {
        entity.setDisplayName(normalize(req.getDisplayName()));
        entity.setAuthType(req.getAuthType().toUpperCase(Locale.ROOT));
        if (StringUtils.hasText(req.getApiToken())) {
            entity.setApiTokenEnc(cryptoService.encrypt(req.getApiToken().trim()));
        }
        if (StringUtils.hasText(req.getApiKey())) {
            entity.setApiKeyEnc(cryptoService.encrypt(req.getApiKey().trim()));
        }
        if (StringUtils.hasText(req.getApiEmail())) {
            entity.setApiEmailEnc(cryptoService.encrypt(req.getApiEmail().trim()));
        }
        entity.setCloudflareAccountId(normalize(req.getCloudflareAccountId()));
        entity.setDefaultZoneId(normalize(req.getDefaultZoneId()));
        entity.setEnabled(req.getEnabled());
        entity.setRemark(normalize(req.getRemark()));
    }

    private void validateAuth(AccountUpsertRequest req) {
        AuthType authType;
        try {
            authType = AuthType.valueOf(req.getAuthType().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BizException("unsupported authType: " + req.getAuthType());
        }
        if (authType == AuthType.API_TOKEN && !StringUtils.hasText(req.getApiToken())) {
            throw new BizException("apiToken is required for API_TOKEN");
        }
        if (authType == AuthType.GLOBAL_KEY) {
            if (!StringUtils.hasText(req.getApiKey())) {
                throw new BizException("apiKey is required for GLOBAL_KEY");
            }
            if (!StringUtils.hasText(req.getApiEmail())) {
                throw new BizException("apiEmail is required for GLOBAL_KEY");
            }
        }
    }

    private void validateAuthForUpdate(AccountUpsertRequest req, CloudflareAccountEntity oldEntity) {
        AuthType authType;
        try {
            authType = AuthType.valueOf(req.getAuthType().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BizException("unsupported authType: " + req.getAuthType());
        }
        if (authType == AuthType.API_TOKEN) {
            boolean hasNew = StringUtils.hasText(req.getApiToken());
            boolean hasOld = StringUtils.hasText(oldEntity.getApiTokenEnc());
            if (!hasNew && !hasOld) {
                throw new BizException("apiToken is required for API_TOKEN");
            }
        }
        if (authType == AuthType.GLOBAL_KEY) {
            boolean hasKey = StringUtils.hasText(req.getApiKey()) || StringUtils.hasText(oldEntity.getApiKeyEnc());
            boolean hasEmail = StringUtils.hasText(req.getApiEmail()) || StringUtils.hasText(oldEntity.getApiEmailEnc());
            if (!hasKey) {
                throw new BizException("apiKey is required for GLOBAL_KEY");
            }
            if (!hasEmail) {
                throw new BizException("apiEmail is required for GLOBAL_KEY");
            }
        }
    }

    private AccountView toView(CloudflareAccountEntity entity) {
        AccountView view = new AccountView();
        view.setId(entity.getId());
        view.setDisplayName(entity.getDisplayName());
        view.setAuthType(entity.getAuthType());
        view.setTokenMasked(mask(cryptoService.decrypt(entity.getApiTokenEnc())));
        view.setKeyMasked(mask(cryptoService.decrypt(entity.getApiKeyEnc())));
        view.setEmailMasked(maskEmail(cryptoService.decrypt(entity.getApiEmailEnc())));
        view.setCloudflareAccountId(entity.getCloudflareAccountId());
        view.setDefaultZoneId(entity.getDefaultZoneId());
        view.setEnabled(entity.getEnabled());
        view.setRemark(entity.getRemark());
        view.setCreatedAt(entity.getCreatedAt());
        view.setUpdatedAt(entity.getUpdatedAt());
        return view;
    }

    private String mask(String raw) {
        if (!StringUtils.hasText(raw) || raw.length() <= 8) {
            return raw;
        }
        return raw.substring(0, 4) + "****" + raw.substring(raw.length() - 4);
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@", 2);
        String name = parts[0];
        if (name.length() <= 2) {
            return "*@" + parts[1];
        }
        return name.substring(0, 2) + "***@" + parts[1];
    }

    private String normalize(String input) {
        if (input == null) {
            return null;
        }
        String value = input.trim();
        return value.isEmpty() ? null : value;
    }
}
