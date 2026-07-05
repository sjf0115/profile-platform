package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.domain.connector.jdbc.DatabaseInfo;
import com.data.profile.common.domain.connector.jdbc.TableColumnInfo;
import com.data.profile.common.domain.connector.jdbc.TableInfo;
import com.data.profile.common.domain.connector.request.ConnectorResponse;
import com.data.profile.common.domain.connector.request.TestConnectionRequestParam;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.StringUtils;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.service.DataSourceService;
import com.data.profile.web.vo.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
    private final static Gson gson = new GsonBuilder().create();

    @Autowired
    private DataSourceService dataSourceService;

    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<String> testConnection(@RequestBody TestConnectionRequestParam param)  {
        log.info("数据源请求测试连接: {}", gson.toJson(param));

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

    @PostMapping(value = "/list")
    public Response<List<DataSource>> getList(@RequestBody DataSource dataSource) {
        List<DataSource> dataSources = dataSourceService.getList(dataSource);
        return Response.success(dataSources);
    }

    @GetMapping(value = "/detail")
    public Response<DataSource> getDetail(@RequestParam(name = "datasource_id") String datasourceId) {
        DataSource dataSource = dataSourceService.getDetail(datasourceId);
        if (!Objects.equals(dataSource, null)) {
            return Response.success(dataSource);
        } else {
            return Response.error("请求的数据源不存在", ResponseCode.DATASOURCE_NO_ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response<Integer> save(@RequestBody DataSource datasource) {
        log.info("请求创建/修改数据源: {}", gson.toJson(datasource));
        int result = dataSourceService.save(datasource);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加数据源失败", ResponseCode.DATASOURCE_NO_ERROR);
        }
    }

    @DeleteMapping(value = "/delete")
    public Response<Integer> delete(@RequestParam(name = "datasource_id") String datasourceId) {
        int result = dataSourceService.delete(datasourceId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除数据源失败", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/config/{type}")
    public Response<String> getConfigJson(@PathVariable String type){
        log.info("请求指定插件类型的配置: {}", type);
        String config = dataSourceService.getConfigJson(type);
        return Response.success(config);
    }

    @GetMapping(value = "/type/list")
    public Response<List<Item>> getConnectorTypeList() {
        log.info("请求获取插件类型");
        List<Item> connectors = dataSourceService.getConnectorTypeList();
        return Response.success(connectors);
    }

    @GetMapping(value = "/{id}/databases")
    public Response<List<DatabaseInfo>> getDatabases(@PathVariable String id) {
        List<DatabaseInfo> databases = dataSourceService.getDatabaseList(id);
        if (Objects.equals(databases, null) || databases.isEmpty()) {
            return Response.error("没有获取到数据库", ResponseCode.ERROR);
        } else {
            return Response.success(databases);
        }
    }

    @GetMapping(value = "/{id}/{database}/tables")
    public Response<List<TableInfo>> getTables(@PathVariable String id, @PathVariable String database) {
        List<TableInfo> tables = dataSourceService.getTableList(id, database);
        if (Objects.equals(tables, null) || tables.isEmpty()) {
            return Response.error("没有获取到数据表", ResponseCode.ERROR);
        } else {
            return Response.success(tables);
        }
    }

    @GetMapping(value = "/{id}/{database}/{table}/columns")
    public Response<TableColumnInfo> getColumns(@PathVariable String id, @PathVariable String database, @PathVariable String table) {
        TableColumnInfo columns = dataSourceService.getColumnList(id, database, table);
        if (Objects.equals(columns, null) || columns.getColumns().isEmpty()) {
            return Response.error("没有获取到数据列", ResponseCode.ERROR);
        } else {
            return Response.success(columns);
        }
    }

    @GetMapping(value = "/export-config/{datasourceId}")
    public Response<String> getExportConfigJson(@PathVariable String datasourceId) {
        log.info("请求获取投递配置表单: datasourceId={}", datasourceId);
        String config = dataSourceService.getExportConfigJson(datasourceId);
        return Response.success(config);
    }

    @GetMapping(value = "/tables/{datasourceId}")
    public Response<List<TableInfo>> getTablesByDatasource(@PathVariable String datasourceId) {
        List<TableInfo> tables = dataSourceService.getTableListByDatasource(datasourceId);
        if (Objects.equals(tables, null) || tables.isEmpty()) {
            return Response.error("没有获取到数据表", ResponseCode.ERROR);
        } else {
            return Response.success(tables);
        }
    }

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