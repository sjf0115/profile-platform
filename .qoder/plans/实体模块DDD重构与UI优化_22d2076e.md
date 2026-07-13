# 实体模块 DDD 重构与 UI 优化

## 背景

Entity 和 EntityIdentifier 模块当前直接使用 DO 贯穿 Controller-Service-DAO，缺少 DTO/VO/Param/Request/Converter 分层，接口风格非 RESTful，前端操作列与数据源不一致。

## Task 1: Entity 模块后端分层 — 创建 DTO/VO/Param/Request/Converter

**新建 5 个文件：**

- `dto/EntityDTO.java` — 服务层传输对象，含 creatorName/modifierName
- `vo/EntityVO.java` — 视图对象，含 creatorName/modifierName
- `dto/EntityParam.java` — 查询参数（entityName, status）
- `dto/EntityRequest.java` — 创建/编辑请求（entityName）
- `converter/EntityConverter.java` — MapStruct 转换器，方法集：do2dto, do2dtoList, dto2vo, dto2voList, request2do, param2do, do2vo, do2voList

**参考模板：** `DataSourceConverter.java`（121行）

## Task 2: EntityIdentifier 模块后端分层 — 创建 DTO/VO/Param/Request/Converter

**新建 5 个文件：**

- `dto/EntityIdentifierDTO.java` — 含 creatorName/modifierName/entityName
- `vo/EntityIdentifierVO.java` — 同上
- `dto/EntityIdentifierParam.java` — 查询参数（entityIdentifierName, entityId, status）
- `dto/EntityIdentifierRequest.java` — 创建/编辑请求（entityIdentifierName, entityId）
- `converter/EntityIdentifierConverter.java` — MapStruct 转换器

## Task 3: Entity Controller RESTful 改造

**文件：** `EntityController.java`

| 旧接口 | 新接口 | 说明 |
|---|---|---|
| `POST /entity/list` + `@RequestBody Entity` | `POST /entity/list` + `@RequestBody EntityParam` | 入参改为 Param |
| `GET /entity/detail?entity_id=` | `GET /entity/{entityId}/detail` | RESTful 路径参数 |
| `POST /entity/save` | `POST`（创建）+ `PUT /{entityId}`（更新） | 创建/更新分离 |
| `DELETE /entity/delete?entity_id=` | `DELETE /entity/{entityId}` | RESTful 路径参数 |
| 无 | `PUT /entity/{entityId}/status` | 独立状态变更接口 |

- Controller 接收 Param/Request，返回 VO
- 添加 `@RequiresPermission` 权限注解
- Service 方法签名改为返回 DTO

## Task 4: EntityIdentifier Controller RESTful 改造

**文件：** `EntityIdentifierController.java`

与 Task 3 模式一致：
- `POST /entity/identifier/list` + `EntityIdentifierParam`
- `GET /entity/identifier/{id}/detail`
- `POST`（创建）+ `PUT /{id}`（更新）
- `DELETE /entity/identifier/{id}`
- `PUT /entity/identifier/{id}/status`

## Task 5: Entity Service 重构

**文件：** `EntityService.java`

- `getList(Entity)` → `getList(Entity): List<EntityDTO>` — 内部通过 `UserService.getUserNameMap()` 填充 creatorName/modifierName
- `getDetail(String)` → `getDetail(String): EntityDTO` — 同上填充名称
- `save(Entity)` → 拆分为 `create(EntityRequest): EntityDTO` + `update(String, EntityRequest): int`
- 新增 `updateStatus(String, Integer): int`

**Mapper XML 变更：** `EntityMapper.xml`
- `selectByParams` 改用 `Join_Column_List`（无 JOIN，仅 creator/modifier 名称在 Service 层批量填充）
- 修复 `insert` 语句中表名错误（`profile_meta_entity_type` → `profile_meta_entity`）

## Task 6: EntityIdentifier Service 重构

**文件：** `EntityIdentifierService.java`

与 Task 5 模式一致：
- `getList` 返回 `List<EntityIdentifierDTO>`，填充 creatorName/modifierName
- `getDetail` 返回 `EntityIdentifierDTO`
- 拆分 `save` 为 `create` + `update`
- 新增 `updateStatus`

## Task 7: 前端 API 层改造

**文件：** `api/entity.ts`

- 更新 `Entity` / `EntityIdentifier` TypeScript 接口（增加 creator_name, modifier_name，移除不存在的 desc 字段）
- `entityApi` 改为 RESTful 风格：
  - `list(params)` → `POST /entity/list`
  - `detail(entityId)` → `GET /entity/{entityId}/detail`
  - `create(data)` → `POST /entity`
  - `update(entityId, data)` → `PUT /entity/{entityId}`
  - `updateStatus(entityId, status)` → `PUT /entity/{entityId}/status`
  - `delete(entityId)` → `DELETE /entity/{entityId}`
- `entityIdentifierApi` 同理

## Task 8: 前端 EntityList.vue 优化 + 新增详情页

**文件：** `entity/components/EntityList.vue`、新建 `entity/detail.vue`

**列表列（固定）：** 序号、实体名称、状态、创建方式、创建人(creator_name)、创建时间、修改时间、操作

**操作列对齐数据源风格：** 查看（跳转详情页）、编辑、禁用/启用、... (下拉包含删除)

- 移除详情弹窗，改为 `router.push('/entity/detail/{entityId}')` 跳转独立详情页
- 启用/禁用调用 `entityApi.updateStatus()` 独立接口
- 创建/编辑分离调用 `entityApi.create()` / `entityApi.update()`

**详情页 `entity/detail.vue`：** 参考 `datasource/detail.vue` 布局
- 顶部面包屑 + 返回按钮
- el-descriptions 展示完整信息（实体ID、名称、状态、创建方式、创建人名称、修改人名称、创建时间、修改时间）

## Task 9: 前端 EntityIdentifierList.vue 优化 + 新增详情页

**文件：** `entity/components/EntityIdentifierList.vue`、新建 `entity/identifier-detail.vue`

**列表列（固定）：** 序号、实体标识名称、实体名称、状态、创建方式、创建人(creator_name)、创建时间、修改时间、操作

**操作列对齐数据源风格：** 查看（跳转详情页）、编辑、禁用/启用、... (下拉包含删除)

- 移除详情弹窗，改为跳转独立详情页
- API 调用改为 RESTful

**详情页 `entity/identifier-detail.vue`：** 参考 `datasource/detail.vue` 布局
- 展示：标识ID、名称、所属实体、状态、创建方式、创建人名称、修改人名称、创建时间、修改时间

## Task 10: 路由配置 + 编译验证 + Review

- `router/index.ts` 新增 Entity 详情页和 EntityIdentifier 详情页路由
- `mvn clean compile` 验证后端
- `npx vue-tsc --noEmit` 验证前端
- Review 前端 UI 交互、数据流、TypeScript 类型
