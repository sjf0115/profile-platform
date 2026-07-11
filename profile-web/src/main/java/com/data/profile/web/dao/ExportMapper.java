package com.data.profile.web.dao;

import com.data.profile.web.model.Export;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExportMapper {
    // 查询
    Export selectSimpleByExportId(String exportId); // 根据ID查询

    Export selectByExportId(String exportId); // 根据ID查询

    List<Export> selectSimpleByExportName(String exportName); // 根据名字查询

    List<Export> selectByExportName(String entityName); // 根据名字查询

    List<Export> selectSimpleByKeyword(String keyword); // 模糊查询

    List<Export> selectByKeyword(String keyword); // 模糊查询

    List<Export> selectSimpleByParams(Export export); // 根据参数查询

    List<Export> selectByParams(Export export); // 根据参数查询

    // 插入
    int insert(Export export); // 插入全部

    int insertSelective(Export export); // 选择性插入

    // 删除
    int deleteByExportId(String exportId); // 根据ID删除

    // 更新
    int updateByExportIdSelective(Export export);

    int updateByExportId(Export export);
}