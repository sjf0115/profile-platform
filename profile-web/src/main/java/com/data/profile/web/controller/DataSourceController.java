package com.data.profile.web.controller;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.converter.DataSourceConverter;
import com.data.profile.web.dto.DataSourceDTO;
import com.data.profile.web.dto.DataSourceParam;
import com.data.profile.web.dto.DataSourceRequest;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.service.DataSourceService;
import com.data.profile.web.vo.DataSourceVO;
import com.data.profile.web.vo.Response;
import com.data.profile.common.domain.connector.jdbc.DatabaseInfo;
import com.data.profile.common.domain.connector.jdbc.TableColumnInfo;
import com.data.profile.common.domain.connector.jdbc.TableInfo;
import com.data.profile.common.domain.connector.request.ConnectorResponse;
import com.data.profile.common.domain.connector.request.TestConnectionRequestParam;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.common.utils.StringUtils;
import com.data.profile.web.vo.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：数据源
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/datasource", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 测试连接
     */
    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<String> testConnection(@RequestBody TestConnectionRequestParam param) {
        log.info("数据源请求测试连接: {}", JSONUtils.toJsonString(param));
        ConnectorResponse response = dataSourceService.testConnect(param);
        if (response == null) {
            return Response.error("Connector response is null", ResponseCode.ERROR);
        }
        boolean isSuccess = response.getStatus() != null
                && response.getStatus().isSuccess()
                && Boolean.TRUE.equals(response.getResult());
        if (isSuccess) {
            return Response.success("数据源测试连接成功");
        } else {
            String errorMsg = StringUtils.isEmpty(response.getErrorMsg())
                    ? "数据源测试连接失败"
                    : response.getErrorMsg();
            return Response.error(errorMsg, ResponseCode.ERROR);
        }
    }

    /**
     * 数据源列表
     */
    @PostMapping(value = "/list")
    public Response<List<DataSourceVO>> getList(@RequestBody DataSourceParam param) {
        log.info("请求查询数据源列表：{}", JSONUtils.toJsonString(param));
        DataSource dataSource = DataSourceConverter.param2do(param);
        List<DataSourceDTO> dtos = dataSourceService.getList(dataSource);
        return Response.success(DataSourceConverter.dto2voList(dtos));
    }

    /**
     * 数据源详情
     */
    @GetMapping(value = "/{datasourceId}/detail")
    public Response<DataSourceVO> getDetail(@PathVariable(value = "datasourceId") String datasourceId) {
        log.info("请求查询数据源 {} 详细信息", datasourceId);
        DataSourceDTO dto = dataSourceService.getDetail(datasourceId);
        if (dto == null) {
            return Response.error("数据源不存在", ResponseCode.DATASOURCE_NO_ERROR);
        }
        return Response.success(DataSourceConverter.dto2vo(dto));
    }

    /**
     * 创建数据源
     */
    @RequiresPermission(code = "datasource:create", name = "数据源-创建")
    @PostMapping
    public Response<DataSourceVO> create(@RequestBody DataSourceRequest request) {
        log.info("请求创建数据源：{}", JSONUtils.toJsonString(request));
        try {
            DataSourceDTO dto = dataSourceService.create(request);
            return Response.success(DataSourceConverter.dto2vo(dto));
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新数据源
     */
    @RequiresPermission(code = "datasource:edit", name = "数据源-编辑")
    @PutMapping("/{datasourceId}")
    public Response<Integer> update(@PathVariable(value = "datasourceId") String datasourceId,
                                    @RequestBody DataSourceRequest request) {
        log.info("请求更新数据源 {}：{}", datasourceId, JSONUtils.toJsonString(request));
        try {
            int result = dataSourceService.update(datasourceId, request);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("修改数据源失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新数据源状态（启用/停用）
     */
    @RequiresPermission(code = "datasource:edit", name = "数据源-编辑")
    @PutMapping("/{datasourceId}/status")
    public Response<Integer> updateStatus(@PathVariable(value = "datasourceId") String datasourceId,
                                          @RequestParam Integer status) {
        log.info("请求更新数据源 {} 状态为：{}", datasourceId, status);
        try {
            int result = dataSourceService.updateStatus(datasourceId, status);
            return Response.success(result);
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 删除数据源
     */
    @RequiresPermission(code = "datasource:delete", name = "数据源-删除")
    @DeleteMapping("/{datasourceId}")
    public Response<Integer> delete(@PathVariable(value = "datasourceId") String datasourceId) {
        log.info("请求删除数据源：{}", datasourceId);
        try {
            int result = dataSourceService.delete(datasourceId);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("删除数据源失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 获取指定插件类型的配置
     */
    @GetMapping(value = "/config/{type}")
    public Response<String> getConfigJson(@PathVariable String type) {
        log.info("请求指定插件类型的配置: {}", type);
        String config = dataSourceService.getConfigJson(type);
        return Response.success(config);
    }

    /**
     * 获取所有支持的插件类型
     */
    @GetMapping(value = "/type/list")
    public Response<List<Item>> getConnectorTypeList() {
        log.info("请求获取插件类型");
        List<Item> connectors = dataSourceService.getConnectorTypeList();
        return Response.success(connectors);
    }

    /**
     * 获取数据库列表
     */
    @GetMapping(value = "/{dataSourceId}/databases")
    public Response<List<DatabaseInfo>> getDatabases(@PathVariable String dataSourceId) {
        List<DatabaseInfo> databases = dataSourceService.getDatabaseList(dataSourceId);
        if (Objects.equals(databases, null) || databases.isEmpty()) {
            return Response.error("没有获取到数据库", ResponseCode.ERROR);
        } else {
            return Response.success(databases);
        }
    }

    /**
     * 获取数据表列表
     */
    @GetMapping(value = "/{dataSourceId}/{database}/tables")
    public Response<List<TableInfo>> getTables(@PathVariable String dataSourceId, @PathVariable String database) {
        List<TableInfo> tables = dataSourceService.getTableList(dataSourceId, database);
        if (Objects.equals(tables, null) || tables.isEmpty()) {
            return Response.error("没有获取到数据表", ResponseCode.ERROR);
        } else {
            return Response.success(tables);
        }
    }

    /**
     * 获取数据列
     */
    @GetMapping(value = "/{dataSourceId}/{database}/{table}/columns")
    public Response<TableColumnInfo> getColumns(@PathVariable String dataSourceId, @PathVariable String database, @PathVariable String table) {
        TableColumnInfo columns = dataSourceService.getColumnList(dataSourceId, database, table);
        if (Objects.equals(columns, null) || columns.getColumns().isEmpty()) {
            return Response.error("没有获取到数据列", ResponseCode.ERROR);
        } else {
            return Response.success(columns);
        }
    }

    /**
     * 获取投递配置表单定义
     */
    @GetMapping(value = "/export-config/{datasourceId}")
    public Response<String> getExportConfigJson(@PathVariable String datasourceId) {
        log.info("请求获取数据源的投递配置表单: {}", datasourceId);
        String config = dataSourceService.getExportConfigJson(datasourceId);
        return Response.success(config);
    }

    /**
     * 简化版：获取数据表列表（后端自动提取 database）
     */
    // TODO
    @GetMapping(value = "/tables/{datasourceId}")
    public Response<List<TableInfo>> getTablesByDatasource(@PathVariable String datasourceId) {
        List<TableInfo> tables = dataSourceService.getTableListByDatasource(datasourceId);
        if (Objects.equals(tables, null) || tables.isEmpty()) {
            return Response.error("没有获取到数据表", ResponseCode.ERROR);
        } else {
            return Response.success(tables);
        }
    }

    /**
     * 简化版：获取数据列（后端自动提取 database）
     */
    // TODO
    @GetMapping(value = "/columns/{datasourceId}/{table}")
    public Response<TableColumnInfo> getColumnsByDatasource(@PathVariable String datasourceId, @PathVariable String table) {
        TableColumnInfo columns = dataSourceService.getColumnListByDatasource(datasourceId, table);
        if (Objects.equals(columns, null) || columns.getColumns().isEmpty()) {
            return Response.error("没有获取到数据列", ResponseCode.ERROR);
        } else {
            return Response.success(columns);
        }
    }
}
