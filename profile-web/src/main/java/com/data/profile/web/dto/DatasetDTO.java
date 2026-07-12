package com.data.profile.web.dto;

import com.data.profile.web.vo.DatasetFieldVO;
import com.data.profile.web.vo.TaskInstanceVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 功能：数据集聚合传输对象（Service 层出参）
 * 包含 Dataset DB 字段 + 关联查询字段（字段列表、最新实例）
 */
@Data
public class DatasetDTO {
    private Long id;
    private Integer status;
    // 数据集ID
    private String datasetId;
    // 数据集名称
    private String datasetName;
    // 数据集类型
    private Integer datasetType;
    // 数据集描述
    private String datasetDesc;
    // 创建方式
    private Integer sourceType;
    // 数据源ID
    private String datasourceId;
    // 表名
    private String tableName;
    // 分区字段
    private String partitionField;
    // 分区格式
    private String partitionFormat;
    // 实体ID(实体标识ID)
    private String entityId;
    // 实体对应字段
    private String entityField;
    // 负责人
    private String owner;
    // 负责人名称
    private String ownerName;
    // 创建人
    private String creator;
    // 创建人名称
    private String creatorName;
    // 修改人
    private String modifier;
    // 修改人名称
    private String modifierName;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;

    //----------------------------------------------------------
    // 引擎表名（计算字段）
    private String engineTableName;
    // 数据集字段列表（跨表聚合）
    private List<DatasetFieldVO> fields;
    // 最新任务实例（跨表聚合）
    private TaskInstanceVO latestInstance;
}
