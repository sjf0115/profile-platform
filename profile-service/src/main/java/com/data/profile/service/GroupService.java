package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.GroupMapper;
import com.data.profile.model.Group;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：群组服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class GroupService {
    private static final Gson gson = new GsonBuilder().create();
    @Resource
    private GroupMapper groupMapper;

    /**
     * 根据查询条件获取群组列表
     * @param group 群组
     */
    public List<Group> getList(Group group) {
        List<Group> groups = groupMapper.selectByParams(group);
        log.info("根据查询条件获取 {} 个群组: {}", groups.size(), gson.toJson(groups));
        return groups;
    }

    /**
     * 根据群组ID获取群组详细信息
     * @param groupId 群组ID
     */
    public Optional<Group> getDetail(String groupId) {
        Group group = groupMapper.selectByGroupId(groupId);
        log.info("根据群组ID获取群组详细信息: {}", gson.toJson(group));
        if (group == null) {
            return Optional.empty();
        }
        return Optional.of(group);
    }

    /**
     * 保存群组 新增/修改
     * @param group 群组
     */
    public int save(Group group) throws RuntimeException {
        if (StringUtils.isBlank(group.getGroupId())) {
            // 新增
            List<Group> groups = groupMapper.selectSimpleByGroupName(group.getGroupName());
            if (!groups.isEmpty()) {
                log.error("群组 {} 已经存在，不允许重复添加", group.getGroupName());
                throw new RuntimeException("群组已经存在，不允许重复添加");
            }
            String groupId = IDGenerator.getInstance().generate(ModelType.GROUP);
            Group target = groupMapper.selectSimpleByGroupId(groupId);
            if (!Objects.equals(target, null)) {
                log.error("群组ID {} 已经存在，不允许重复添加", groupId);
                throw new RuntimeException("群组ID已经存在，不允许重复添加");
            }
            group.setGroupId(groupId);
            group.setGroupStatus(Status.ENABLE.getCode());
            group.setCreator(RequestContext.currentUserId());
            group.setModifier(RequestContext.currentUserId());
            log.info("新增群组: {}", gson.toJson(group));
            return groupMapper.insertSelective(group);
        } else {
            // 修改
            group.setModifier(RequestContext.currentUserId());
            log.info("更新群组: {}", gson.toJson(group));
            return groupMapper.updateByGroupIdSelective(group);
        }
    }

    /**
     * 删除群组
     * @param groupId 群组ID
     */
    public int delete(String groupId) {
        Group group = groupMapper.selectSimpleByGroupId(groupId);
        if (Objects.equals(group, null)) {
            log.error("群组 {} 不存在，无法删除", groupId);
            throw new RuntimeException("群组不存在，无法删除");
        }
        if (Objects.equals(group.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置群组 {} 不允许删除", groupId);
            throw new RuntimeException("内置群组不允许删除");
        }
        // TODO 检查依赖确保无下游使用
        log.info("删除群组: {}", groupId);
        return groupMapper.deleteByGroupId(groupId);
    }
}
