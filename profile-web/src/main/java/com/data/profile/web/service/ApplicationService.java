package com.data.profile.web.service;

import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.web.dao.ApplicationMapper;
import com.data.profile.web.model.Application;
import com.data.profile.web.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 应用管理服务
 */
@Slf4j
@Service
public class ApplicationService {
    @Resource
    private ApplicationMapper applicationMapper;

    /**
     * 根据查询条件获取应用列表
     */
    public List<Application> getList(Application application) {
        return applicationMapper.selectByParams(application);
    }

    /**
     * 模糊查询
     */
    public List<Application> getByKeyword(String keyword) {
        return applicationMapper.selectByKeyword(keyword);
    }

    /**
     * 根据 ID 获取应用详情
     */
    public Optional<Application> getDetail(Long id) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            return Optional.empty();
        }
        return Optional.of(application);
    }

    /**
     * 根据 appKey 获取应用
     */
    public Application getByAppKey(String appKey) {
        return applicationMapper.selectByAppKey(appKey);
    }

    /**
     * 保存应用（新增/修改）
     */
    @Transactional
    public Application save(Application application) throws RuntimeException {
        String userId = RequestContext.currentUserId();
        if (application.getId() == null) {
            // 新增
            // 检查应用名称是否重复
            Application check = new Application();
            check.setAppName(application.getAppName());
            List<Application> existing = applicationMapper.selectByParams(check);
            if (!existing.isEmpty()) {
                throw new RuntimeException("应用名称已存在");
            }
            // 生成 AppKey 和 AppSecret
            application.setAppKey(generateAppKey());
            application.setAppSecret(generateAppSecret());
            application.setStatus(Status.ENABLE.getCode());
            application.setSourceType(SourceType.CUSTOM.getCode());
            application.setOwner(userId);
            application.setCreator(userId);
            application.setModifier(userId);
            applicationMapper.insertSelective(application);
            return application; // 返回包含 appKey 和 appSecret 的完整对象
        } else {
            // 修改
            application.setModifier(userId);
            applicationMapper.updateByIdSelective(application);
            return applicationMapper.selectById(application.getId());
        }
    }

    /**
     * 删除应用
     */
    public int delete(Long id) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("应用不存在");
        }
        if (Objects.equals(application.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("内置应用不允许删除");
        }
        return applicationMapper.deleteById(id);
    }

    /**
     * 重置 AppSecret
     */
    public String resetSecret(Long id) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw new RuntimeException("应用不存在");
        }
        String newSecret = generateAppSecret();
        Application update = new Application();
        update.setId(id);
        update.setAppSecret(newSecret);
        update.setModifier(RequestContext.currentUserId());
        applicationMapper.updateByIdSelective(update);
        return newSecret;
    }

    /**
     * 生成 AppKey: app_ 前缀 + 16位随机字母数字
     */
    private String generateAppKey() {
        String key;
        do {
            key = "app_" + RandomStringUtils.randomAlphanumeric(16).toLowerCase();
        } while (applicationMapper.selectByAppKey(key) != null);
        return key;
    }

    /**
     * 生成 AppSecret: 32位随机字符串
     */
    private String generateAppSecret() {
        return RandomStringUtils.random(32, true, true);
    }
}
