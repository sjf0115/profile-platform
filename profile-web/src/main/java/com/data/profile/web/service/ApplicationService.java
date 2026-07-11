package com.data.profile.web.service;

import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.exception.ProfileException;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.converter.ApplicationConverter;
import com.data.profile.web.dao.ApplicationMapper;
import com.data.profile.web.dao.UserMapper;
import com.data.profile.web.dto.ApplicationDTO;
import com.data.profile.web.dto.ApplicationRequest;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.model.Application;
import com.data.profile.web.model.User;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private ResourceGrantService resourceGrantService;
    @Autowired
    private LineageService lineageService;
    @Resource
    private UserMapper userMapper;

    /**
     * 根据查询条件获取应用列表
     */
    public List<ApplicationDTO> getList(Application application) {
        List<Application> applications = applicationMapper.selectByParams(application);
        List<ApplicationDTO> dtos = ApplicationConverter.do2dtoList(applications);
        fillOwnerName(dtos);
        return dtos;
    }

    /**
     * 模糊查询
     */
    public List<ApplicationDTO> getByKeyword(String keyword) {
        List<Application> applications = applicationMapper.selectByKeyword(keyword);
        return ApplicationConverter.do2dtoList(applications);
    }

    /**
     * 根据 appKey 获取应用详情
     */
    public Optional<ApplicationDTO> getDetail(String appKey) {
        Application application = applicationMapper.selectByAppKey(appKey);
        if (application == null) {
            return Optional.empty();
        }
        ApplicationDTO dto = ApplicationConverter.do2dto(application);
        fillOwnerName(dto);
        return Optional.of(dto);
    }

    /**
     * 根据 appKey 获取应用（供其他模块调用）
     */
    public Application getByAppKey(String appKey) {
        return applicationMapper.selectByAppKey(appKey);
    }

    /**
     * 创建应用
     */
    @Transactional
    public ApplicationDTO create(ApplicationRequest request) throws RuntimeException {
        String userId = UserContextHolder.currentUserId();

        // 检查应用名称是否重复
        int count = applicationMapper.countByAppName(request.getAppName(), null);
        if (count > 0) {
            throw new RuntimeException("应用名称已存在");
        }
        // 生成唯一的 appKey
        String appKey = IDGenerator.getInstance().generate(ModelType.APPLICATION);
        if (applicationMapper.selectByAppKey(appKey) != null) {
            log.error("应用ID [{}] 已经存在，不允许重复创建", appKey);
            throw new RuntimeException("应用ID已经存在，不允许重复添加");
        }

        // Request -> DO
        Application application = ApplicationConverter.request2do(request);
        application.setAppKey(appKey);
        application.setAppSecret(generateAppSecret());
        application.setStatus(Status.ENABLE.getCode());
        application.setSourceType(SourceType.CUSTOM.getCode());
        // 负责人：优先使用指定的人，否则默认为创建者
        application.setOwner(StringUtils.isNotBlank(request.getOwner()) ? request.getOwner() : userId);
        application.setCreator(userId);
        application.setModifier(userId);
        applicationMapper.insertSelective(application);

        // TODO
        // 自动授权 MANAGE 给创建者
        resourceGrantService.grantOwner(ModelType.APPLICATION.getCode(), application.getAppKey(), userId);

        // 返回包含 appKey 和 appSecret 的完整对象（仅创建时返回 secret）
        ApplicationDTO dto = ApplicationConverter.do2dto(application);
        // TODO
        fillOwnerName(dto);
        return dto;
    }

    /**
     * 更新应用
     */
    public int update(String appKey, ApplicationRequest request) {
        Application existing = applicationMapper.selectByAppKey(appKey);
        if (existing == null) {
            throw new RuntimeException("应用不存在");
        }

        // 检查应用名称是否重复（排除自身）
        if (request.getAppName() != null && !request.getAppName().equals(existing.getAppName())) {
            int count = applicationMapper.countByAppName(request.getAppName(), appKey);
            if (count > 0) {
                throw new RuntimeException("应用名称已存在");
            }
        }

        // Request -> DO
        Application application = ApplicationConverter.request2do(request);
        application.setAppKey(appKey);
        application.setModifier(UserContextHolder.currentUserId());
        return applicationMapper.updateByAppKeySelective(application);
    }

    /**
     * 删除应用
     */
    public int delete(String appKey) {
        Application application = applicationMapper.selectByAppKey(appKey);
        if (application == null) {
            throw new RuntimeException("应用不存在");
        }
        if (Objects.equals(application.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("内置应用不允许删除");
        }
        // 血缘下游依赖检查
        lineageService.checkDeletable(AssetType.APPLICATION.getCode(), appKey);
        return applicationMapper.deleteByAppKey(appKey);
    }

    /**
     * 重置 AppSecret
     */
    public String resetSecret(String appKey) {
        Application application = applicationMapper.selectByAppKey(appKey);
        if (application == null) {
            throw new RuntimeException("应用不存在");
        }
        String newSecret = generateAppSecret();
        Application update = new Application();
        update.setAppKey(appKey);
        update.setAppSecret(newSecret);
        update.setModifier(UserContextHolder.currentUserId());
        applicationMapper.updateByAppKeySelective(update);
        return newSecret;
    }

    /**
     * 更新应用状态（启用/停用）
     */
    public int updateStatus(String appKey, Integer status) {
        Application application = applicationMapper.selectByAppKey(appKey);
        if (application == null) {
            throw new RuntimeException("应用不存在");
        }
        Application update = new Application();
        update.setAppKey(appKey);
        update.setStatus(status);
        update.setModifier(UserContextHolder.currentUserId());
        return applicationMapper.updateByAppKeySelective(update);
    }

    /**
     * 生成 AppSecret: 32位随机字符串
     */
    private String generateAppSecret() {
        return RandomStringUtils.random(32, true, true);
    }

    /**
     * 填充负责人名称（单个）
     */
    private void fillOwnerName(ApplicationDTO dto) {
        if (dto == null || StringUtils.isBlank(dto.getOwner())) return;
        User user = userMapper.selectByUserId(dto.getOwner());
        if (user != null) {
            dto.setOwnerName(user.getUserName());
        }
    }

    /**
     * 填充负责人名称（批量）
     */
    private void fillOwnerName(List<ApplicationDTO> dtos) {
        if (dtos == null) return;
        dtos.forEach(this::fillOwnerName);
    }
}
