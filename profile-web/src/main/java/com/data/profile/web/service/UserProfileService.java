package com.data.profile.web.service;

import com.data.profile.web.converter.UserConverter;
import com.data.profile.web.converter.UserLabelConverter;
import com.data.profile.web.converter.UserProfileConverter;
import com.data.profile.web.dao.DatasetFieldMapper;
import com.data.profile.web.dao.DatasetMapper;
import com.data.profile.web.dao.UserLabelMapper;
import com.data.profile.web.dto.UserLabelDTO;
import com.data.profile.web.dto.UserProfileDTO;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.UserLabel;
import com.data.profile.web.vo.LabelCategoryVO;
import com.data.profile.web.vo.UserProfileRowVO;
import com.data.profile.web.vo.UserProfileVO;
import com.data.profile.web.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能：用户画像服务
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Slf4j
@Service
public class UserProfileService {

    @Autowired
    private UserLabelMapper userLabelMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private DatasetFieldMapper datasetFieldMapper;

    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private AnalysisEngineService analysisEngineService;

    /**
     * 获取用户画像（聚合接口）
     */
    public UserProfileVO getUserProfile(String userId) {
        // 1. 查询用户标签（Mapper JOIN 查询，返回 DTO）
        List<UserLabelDTO> labelDTOs = userLabelMapper.selectUserLabelsWithCategory(userId);

        // 2. 按类目分组
        Map<String, List<UserLabelDTO>> categoryMap = labelDTOs.stream()
                .collect(Collectors.groupingBy(
                        UserLabelDTO::getCategoryId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 3. 构建 DTO
        UserProfileDTO profileDTO = new UserProfileDTO();
        // 获取用户基础信息
        Optional<com.data.profile.web.dto.UserDTO> userOpt = userService.getDetail(userId);
        profileDTO.setUser(userOpt.map(UserConverter::dto2vo).orElse(null));
        profileDTO.setLabels(labelDTOs);
        profileDTO.setGroups(new ArrayList<>()); // TODO: 后续实现用户所属人群查询

        // 4. DTO → VO 转换
        UserProfileVO vo = UserProfileConverter.dto2vo(profileDTO);

        // 5. 填充类目分组（特殊处理：扁平列表 → 分组结构）
        vo.setLabelCategories(buildLabelCategories(categoryMap));

        return vo;
    }

    /**
     * 添加用户标签
     */
    public int addLabel(String userId, String labelId, String labelValue) {
        UserLabel userLabel = new UserLabel();
        userLabel.setUserId(userId);
        userLabel.setLabelId(labelId);
        userLabel.setLabelValue(labelValue);
        userLabel.setSortOrder(0);
        return userLabelMapper.insertSelective(userLabel);
    }

    /**
     * 删除用户单个标签
     */
    public int deleteLabel(String userId, String labelId) {
        return userLabelMapper.deleteByUserAndLabel(userId, labelId);
    }

    /**
     * 更新类目排序（拖拽后调用）
     */
    public int updateCategorySort(String userId, List<String> categoryIds) {
        return userLabelMapper.batchUpdateSortOrder(userId, categoryIds);
    }

    /**
     * 删除类目（解绑该类目下所有标签）
     */
    public int deleteCategory(String userId, String categoryId) {
        return userLabelMapper.deleteByUserAndCategory(userId, categoryId);
    }

    /**
     * 构建类目分组结构
     */
    private List<LabelCategoryVO> buildLabelCategories(Map<String, List<UserLabelDTO>> categoryMap) {
        return categoryMap.entrySet().stream()
                .map(entry -> {
                    LabelCategoryVO category = new LabelCategoryVO();
                    category.setCategoryId(entry.getKey());
                    category.setCategoryName(entry.getValue().get(0).getCategoryName());
                    category.setSortOrder(entry.getValue().get(0).getSortOrder());
                    category.setLabels(entry.getValue().stream()
                            .map(UserLabelConverter::dto2vo)
                            .collect(Collectors.toList()));
                    return category;
                })
                .collect(Collectors.toList());
    }

    // ==================== 用户画像主页相关方法 ====================

    /**
     * 根据标签ID从 ClickHouse 数据集表随机抽取用户
     * 路径: Label → DatasetField(relatedId=labelId) → datasetId → profile_dataset_{datasetId}
     */
    public List<UserProfileRowVO> getRandomUsersByLabelId(String labelId, int limit) {
        DatasetField datasetField = datasetFieldMapper.selectByRelatedId(labelId);
        if (datasetField == null) {
            log.warn("标签 {} 未找到对应的数据集字段", labelId);
            return Collections.emptyList();
        }
        String tableName = "profile_dataset_" + datasetField.getDatasetId();
        return executeRandomUserQuery(tableName, limit);
    }

    /**
     * 根据实体标识ID从 ClickHouse 数据集表随机抽取用户
     * 路径: EntityIdentifier.entityId → Dataset.entityId → profile_dataset_{datasetId}
     */
    public List<UserProfileRowVO> getRandomUsersByEntityIdentifierId(String entityIdentifierId, int limit) {
        // 1. 通过实体标识ID查找对应的数据集
        Dataset query = new Dataset();
        query.setEntityId(entityIdentifierId);
        List<Dataset> datasets = datasetMapper.selectByParams(query);
        if (datasets == null || datasets.isEmpty()) {
            log.warn("实体标识 {} 未找到对应的数据集", entityIdentifierId);
            return Collections.emptyList();
        }
        String datasetId = datasets.get(0).getDatasetId();

        // 2. 构建并执行 ClickHouse SQL
        String tableName = "profile_dataset_" + datasetId;
        return executeRandomUserQuery(tableName, limit);
    }

    /**
     * 根据群组ID从 ClickHouse 引擎表随机抽取用户
     * 路径: 直接查询 profile_group_{groupId}
     */
    public List<UserProfileRowVO> getRandomUsersByGroupId(String groupId, int limit) {
        String tableName = "profile_group_" + groupId;
        return executeRandomUserQuery(tableName, limit);
    }

    /**
     * 执行随机用户查询
     */
    private List<UserProfileRowVO> executeRandomUserQuery(String tableName, int limit) {
        String sql = String.format(
                "SELECT * FROM %s ORDER BY rand() LIMIT %d",
                tableName, limit
        );
        try {
            List<Map<String, Object>> rows = analysisEngineService.executeQueryList(sql);
            return rows.stream()
                    .map(this::mapToUserProfileRowVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询引擎表 {} 失败: {}", tableName, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 将 ClickHouse 结果行映射为 UserProfileRowVO
     */
    private UserProfileRowVO mapToUserProfileRowVO(Map<String, Object> row) {
        UserProfileRowVO vo = new UserProfileRowVO();
        vo.setUserId(getStringValue(row, "entity_id"));
        vo.setUserName(getStringValue(row, "user_name"));
        vo.setApp(getStringValue(row, "app_name"));
        vo.setLastAccessTime(getStringValue(row, "event_time"));
        vo.setDeviceModel(getStringValue(row, "device_model"));
        vo.setOs(getStringValue(row, "os"));
        vo.setSoftwareVersion(getStringValue(row, "software_version"));
        vo.setChannel(getStringValue(row, "channel"));
        vo.setDeviceBrand(getStringValue(row, "device_brand"));
        vo.setRegion(getStringValue(row, "region"));
        return vo;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }
}
