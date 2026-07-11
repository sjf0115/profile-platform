package com.data.profile.web.config;

import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.PermissionType;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.dao.PermissionMapper;
import com.data.profile.web.dao.RoleMapper;
import com.data.profile.web.dao.RolePermissionMapper;
import com.data.profile.web.model.Permission;
import com.data.profile.web.model.Role;
import com.data.profile.web.model.RolePermission;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static com.data.profile.common.domain.Constant.*;

/**
 * 权限码自动同步组件
 * 启动时扫描所有 @RequiresPermission 注解，将缺失的权限码自动写入数据库
 */
@Slf4j
@Component
public class PermissionAutoSync implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private List<Object> controllers;

    /**
     * 分析师权限码（业务操作权限，无系统管理）
     */
    private static final Set<String> ANALYST_CODES = new HashSet<>(Arrays.asList(
            "label:edit", "label:delete",
            "group:edit", "group:delete", "group:execute",
            "export:edit", "export:delete", "export:execute",
            "groupAnalysis:edit", "groupAnalysis:delete"
    ));

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            // 启动时扫描所有注解获取所有权限点
            syncPermissions();
            // 为默认角色分配权限
            assignPresetRoles();
        } catch (Exception e) {
            log.error("权限码自动同步失败（可能 DB 尚未就绪）: {}", e.getMessage());
        }
    }

    private void syncPermissions() {
        // 1. 扫描所有 Controller 的 @RequiresPermission，收集 code→name
        Map<String, String> codeNameMap = new LinkedHashMap<>();
        for (Object bean : controllers) {
            Class<?> clazz = bean.getClass();
            if (AnnotationUtils.findAnnotation(clazz, RestController.class) == null) {
                continue;
            }
            for (Method method : clazz.getDeclaredMethods()) {
                RequiresPermission ann = AnnotationUtils.findAnnotation(method, RequiresPermission.class);
                if (ann != null) {
                    String name = ann.name();
                    String code = ann.code();
                    if (StringUtils.isEmpty(name) || StringUtils.isEmpty(code)) {
                        log.error("无效权限点: {}:{}", code, name);
                        continue;
                    }
                    codeNameMap.putIfAbsent(code, name);
                }
            }
        }

        log.info("扫描到 {} 个权限码", codeNameMap.size());

        // 2. 对比 DB，插入缺失的
        int inserted = 0;
        for (Map.Entry<String, String> entry : codeNameMap.entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue();
            Permission existing = permissionMapper.selectByPermissionCode(code);
            if (existing != null) {
                continue;
            }
            Permission permission = Permission.builder()
                    .permissionId(IDGenerator.getInstance().generate(ModelType.PERMISSION))
                    .permissionCode(code)
                    .permissionName(name)
                    .permissionType(PermissionType.API.getCode())
                    .parentId("0")
                    .sort(0)
                    .sourceType(1) // 系统内置
                    .creator("system")
                    .modifier("system")
                    .build();
            permissionMapper.insertSelective(permission);
            inserted++;
            log.info("自动注册权限码: {} ({})", code, name);
        }

        if (inserted > 0) {
            log.info("权限码同步完成，新增 {} 条", inserted);
        } else {
            log.info("权限码无变化，无需同步");
        }
    }

    /**
     * 预置角色权限分配
     * - 管理员：role_type=1 → 超管，代码级放行，无需 role_permission
     * - 分析师：业务操作权限
     * - 访客：无写权限码，仅访问无注解的读接口
     */
    private void assignPresetRoles() {
        // 确保预置角色存在
        ensurePresetRoles();

        // 为分析师分配权限
        assignRolePermissions(DEFAULT_ANALYST_ROLE_ID, ANALYST_CODES);

        // 访客无需权限码（所有写接口都有 @RequiresPermission，未标注的读接口天然可访问）
        // 管理员由代码层判断，无需 role_permission
    }

    // 增加默认角色
    private void ensurePresetRoles() {
        String[][] presets = {
                {DEFAULT_ADMIN_ROLE_ID,   "1", "管理员",   "拥有系统所有权限，可管理用户、角色及全部业务功能"},
                {DEFAULT_ANALYST_ROLE_ID, "2", "分析师",   "可使用标签、群组、分析、投递等业务功能，无系统管理权限"},
                {DEFAULT_VISITOR_ROLE_ID,  "2", "访客", "仅有查看权限，不可执行任何写操作"}
        };
        for (String[] preset : presets) {
            Role existing = roleMapper.selectByRoleId(preset[0]);
            if (existing == null) {
                Role role = Role.builder()
                        .roleId(preset[0])
                        .roleType(Integer.parseInt(preset[1]))
                        .roleName(preset[2])
                        .roleDesc(preset[3])
                        .sourceType(1)
                        .creator("system")
                        .modifier("system")
                        .build();
                roleMapper.insertSelective(role);
                log.info("自动创建预置角色: {} ({})", preset[0], preset[2]);
            }
        }
    }

    // 为默认角色分配权限
    private void assignRolePermissions(String roleId, Set<String> targetCodes) {
        // 查出目标权限码对应的 permissionId
        List<Permission> allPerms = permissionMapper.selectAll();
        List<String> targetIds = allPerms.stream()
                .filter(p -> targetCodes.contains(p.getPermissionCode()))
                .map(Permission::getPermissionId)
                .collect(Collectors.toList());

        // 先删后插（事务一致性由组件启动阶段保证）
        rolePermissionMapper.deleteByRoleId(roleId);
        if (targetIds.isEmpty()) {
            return;
        }
        List<RolePermission> list = targetIds.stream()
                .map(pid -> RolePermission.builder()
                        .roleId(roleId)
                        .permissionId(pid)
                        .creator("system")
                        .build())
                .collect(Collectors.toList());
        rolePermissionMapper.batchInsert(list);
        log.info("角色 {} 已分配 {} 条权限", roleId, list.size());
    }
}
