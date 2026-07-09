# 后端代码深度 Review 报告

---

## 一、Bug / 逻辑错误（HIGH - 必须修复）

### Task 1: UserService 错误的 static import
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/UserService.java:34`
- **问题**: `import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;` 引入了 JSqlParser 的无关符号，可能导致命名冲突
- **修复**: 删除该 import

### Task 2: DataSourceService.delete() 空指针异常
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/DataSourceService.java:116-117`
- **问题**: `selectByDatasourceId()` 可能返回 null，但直接调用 `dataSource.getSourceType()` 无 null 检查
- **修复**: 增加 null 检查，不存在时抛出明确异常

### Task 3: ExportService.delete() 空指针异常
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/ExportService.java:128-129`
- **问题**: 同 Task 2，`selectSimpleByExportId()` 可能返回 null
- **修复**: 增加 null 检查

### Task 4: TaskInstanceController URL 路径变量注解错误
- **文件**: `profile-web/src/main/java/com/data/profile/web/controller/TaskInstanceController.java:53-54`
- **问题**: URL 定义为 `/{taskId}/list`（PathVariable），但参数注解使用了 `@RequestParam(name = "taskId")`，导致 PathVariable 未绑定，必须额外传 query 参数
- **修复**: 改为 `@PathVariable(value = "taskId")`

### Task 5: TaskExecutionService 异步任务 UserContext 丢失
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/TaskExecutionService.java:85`
- **问题**: `CompletableFuture.runAsync(() -> executeAsync(...), taskExecutorPool)` 在另一个线程执行，但 `UserContextHolder` 基于 ThreadLocal，异步线程无法获取用户上下文。后续 `createInstance()` 调用 `UserContextHolder.currentUserId()` 会抛异常
- **修复**: 在提交异步任务前捕获 UserContext，在异步线程内手动设置，完成后清除

### Task 6: DataSourceController.save() 错误码误用
- **文件**: `profile-web/src/main/java/com/data/profile/web/controller/DataSourceController.java:85`
- **问题**: 保存失败时使用 `ResponseCode.DATASOURCE_NO_ERROR`，语义为"数据源无错误"，应为 `ResponseCode.ERROR`
- **修复**: 改为 `ResponseCode.ERROR`

### Task 7: GroupAnalysisController 错误消息 typo
- **文件**: `profile-web/src/main/java/com/data/profile/web/controller/GroupAnalysisController.java:133`
- **问题**: `"获取标签分布失败，请连续管理员"` -> 应为 `"请联系管理员"`
- **修复**: 修正文案

### Task 8: UserService.delete() 操作顺序不一致
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/UserService.java:177-181`
- **问题**: 先删除用户（line 177），再删除角色关联（line 180）。如果角色删除失败，用户已不存在但角色数据残留。且该方法无 `@Transactional` 注解
- **修复**: 先删除角色关联，再删除用户，并添加 `@Transactional`

### Task 9: EventController / AttributeController GET 请求使用 @RequestBody
- **文件**: `EventController.java:33-34`, `AttributeController.java:33-34`
- **问题**: `@GetMapping` 搭配 `@RequestBody` 不符合 HTTP 规范，GET 请求不应包含 body
- **修复**: 改为 `@PostMapping`

### Task 10: 多个 Controller/Service 重复声明 Logger
- **文件**: `EventController.java:28`, `AttributeController.java:28`, `DataSourceSchemaController.java:30`
- **问题**: 类上已有 `@Slf4j`，但又手动声明 `private static Logger LOG = LoggerFactory.getLogger(...)`，造成冗余
- **修复**: 移除手动声明的 LOG，统一使用 `@Slf4j` 提供的 `log`

---

## 二、安全隐患（HIGH - 必须修复）

### Task 11: GroupService SQL 注入风险
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/GroupService.java:192-203`
- **问题**: SQL 创建群组的 dry-run 校验直接拼接用户 SQL：`"SELECT entity_id FROM (" + sqlText + ") LIMIT 0"`。虽然做了关键词黑名单检查，但基于字符串匹配容易被绕过（如子查询嵌套、注释绕过等）
- **建议**: 使用 ClickHouse 的 `EXPLAIN` 或 `SET max_rows_to_read = 0` 等安全机制替代拼接

### Task 12: UserProfileService SQL 注入风险
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/UserProfileService.java:181-183`
- **问题**: `String.format("SELECT * FROM %s ORDER BY rand() LIMIT %d", tableName, limit)` 中 `tableName` 由代码拼接（风险较低），但 `limit` 来自用户请求参数，无上限校验
- **修复**: 对 limit 加上限（如 `Math.min(limit, 1000)`），table 名做正则校验

### Task 13: AuthenticationInterceptor 每次请求查数据库
- **文件**: `profile-web/src/main/java/com/data/profile/web/interceptor/AuthenticationInterceptor.java:74`
- **问题**: 每个认证请求都调用 `userService.getDetail(userId)`，触发 2 次数据库查询（用户 + 角色），在高并发场景下是严重性能瓶颈
- **修复**: 增加用户缓存（如 Redis/Guava Cache，TTL 5 分钟）

---

## 三、性能 / 设计问题（MEDIUM - 建议优化）

### Task 14: TaskExecutionService 线程池无界队列
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/TaskExecutionService.java:54`
- **问题**: `Executors.newFixedThreadPool(10)` 内部使用无界 `LinkedBlockingQueue`，大量任务堆积可能导致 OOM
- **修复**: 使用 `ThreadPoolExecutor` 自定义有界队列和拒绝策略

### Task 15: Service 层 N+1 查询问题
- **文件**: `UserService.getList()` (Line 86-92), `GroupService.getList()` (Line 74-80), `ExportService.getList()` (Line 42-45)
- **问题**: 循环中逐条查询关联数据（角色、任务实例），导致 N+1 查询
- **修复**: 使用 Mapper JOIN 或批量查询替代循环单条查询

### Task 16: LabelService 全表加载 DatasetField
- **文件**: `LabelService.java:216, 247, 274`
- **问题**: `datasetFieldService.getList(new DatasetField())` 每次调用都加载全表数据到内存
- **修复**: 增加按 entityIdentifierId 或 labelId 列表过滤的查询方法

### Task 17: Service 层仍混用 Gson（与 Task 1 统一规范冲突）
- **文件**: `GroupService.java:44`, `LabelService.java:41`, `DataSourceService.java:40`, `DatasetService.java:45`, `EngineService.java:32`, `GroupAnalysisService.java:39`
- **问题**: Controller 层已统一为 `JSONUtils`，但 Service 层仍有 6 个文件使用 `private static final Gson gson = new GsonBuilder().create()`
- **修复**: 统一替换为 `JSONUtils.toJsonString()`

### Task 18: DatasetService.getDataSources() 参数未使用
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/DatasetService.java:219-223`
- **问题**: `datasetType` 参数传入但完全未使用，查询所有数据源而非按类型过滤
- **修复**: 根据 `datasetType` 过滤数据源，或移除参数

### Task 19: UserService.getOverview() 性能问题
- **文件**: `profile-web/src/main/java/com/data/profile/web/service/UserService.java:187-217`
- **问题**: 调用 `getList(new User())` 加载全部用户及角色（N+1），再内存遍历统计。应用 SQL 聚合
- **修复**: 在 Mapper 层增加聚合查询

### Task 20: GroupController.getLabelConfig() 硬编码
- **文件**: `profile-web/src/main/java/com/data/profile/web/controller/GroupController.java:164-184`
- **问题**: 标签操作符配置硬编码在 Controller 中，不利于维护和扩展
- **建议**: 移到配置文件或 Service 层

### Task 21: ControllerExceptionAspect 返回原始 Response
- **文件**: `profile-web/src/main/java/com/data/profile/web/aspect/ControllerExceptionAspect.java:21`
- **问题**: `public Response handleException(Exception e)` 缺少泛型，应为 `Response<?>`
- **修复**: 改为 `Response<?>`

---

## 四、实现不完整（LOW - 待补充）

### Task 22: UserController.update() 字段映射不完整
- **文件**: `UserController.java:67` 标记 `// TODO`
- **问题**: 手动逐字段赋值 UserRequest → User，遗漏了 `phone`、`avatar` 等字段
- **修复**: 使用 `BeanUtils.copyProperties()` 或在 UserRequest 中定义完整字段

### Task 23: GroupService.create() 文件上传/SQL 创建逻辑不完整
- **文件**: `GroupService.java:175-182, 184-209`
- **问题**: 文件上传类型仅打了 log，未实际导入 CSV 到引擎表；SQL 创建类型未创建引擎表数据
- **说明**: 标记了多处 `// TODO 优化`

### Task 24: GroupService.create() 原子性问题
- **文件**: `GroupService.java:216-217`
- **问题**: `createGroupEngineTable()` 失败仅打 warn log 不阻塞群组创建，导致群组记录存在但引擎表缺失
- **建议**: 要么改为强一致（失败回滚），要么异步补偿创建

### Task 25: DatasetController.execute() 标注 TODO
- **文件**: `DatasetController.java:95`
- **问题**: `// TODO 需要根据数据集ID和任务类型` 表明当前 executeByRelatedId 逻辑可能不准确

### Task 26: UserProfileService.getUserProfile() groups 字段未实现
- **文件**: `UserProfileService.java:70`
- **问题**: `profileDTO.setGroups(new ArrayList<>()); // TODO: 后续实现用户所属人群查询`

### Task 27: UserProfileController.addLabel() 缺少参数校验
- **文件**: `UserProfileController.java:74`
- **问题**: 使用 `Map<String, String>` 接收请求体，未校验 `label_id` 和 `label_value` 是否为空
- **修复**: 增加参数非空校验
