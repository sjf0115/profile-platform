<template>
  <div class="analysis-create-page">
    <!-- 顶部导航 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="page-title">{{ isEdit ? '编辑群组分析' : '创建群组分析' }}</h2>
      <div class="header-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ isEdit ? '保存修改' : '保存' }}
        </el-button>
      </div>
    </div>

    <div class="page-body">
      <!-- 顶部选择区 -->
      <div class="entity-group-bar">
        <!-- 群体所属实体 -->
        <div class="bar-item">
          <span class="bar-label">群体所属实体</span>
          <el-select
            v-model="selectedEntityIdentifierId"
            placeholder="请选择实体标识"
            filterable
            clearable
            style="width: 220px"
            @change="handleEntityIdentifierChange"
          >
            <el-option
              v-for="item in entityIdentifierList"
              :key="item.entity_identifier_id"
              :label="`${item.entity_name} > ${item.entity_identifier_name}`"
              :value="item.entity_identifier_id"
            />
          </el-select>
        </div>

        <!-- 群组下拉 -->
        <div class="bar-item">
          <span class="bar-label">群组</span>
          <el-select
            v-model="form.group_id"
            placeholder="选择主分析群组"
            filterable
            clearable
            :disabled="!selectedEntityIdentifierId"
            style="width: 220px"
            @change="handleGroupChange"
          >
            <el-option
              v-for="group in groupList"
              :key="group.group_id"
              :label="group.group_name"
              :value="group.group_id"
            >
              <span>{{ group.group_name }}</span>
              <span style="color: #909399; font-size: 12px; margin-left: 8px">
                ({{ group.group_count || 0 }})
              </span>
            </el-option>
          </el-select>
        </div>

        <!-- 数量展示 -->
        <div class="bar-item bar-info" v-if="form.group_id">
          <span class="group-count">数量：{{ selectedGroupCount }}</span>
          <span class="group-coverage">{{ selectedGroupRate }}% 覆盖度</span>
        </div>

        <!-- 增加/删除对比群体按钮 -->
        <div class="bar-item" v-if="form.group_id">
          <el-button
            v-if="!showCompareGroup"
            type="primary"
            plain
            size="small"
            @click="showCompareGroup = true"
          >
            <el-icon style="margin-right: 4px"><Plus /></el-icon>
            增加对比群体
          </el-button>
          <el-button
            v-else
            type="danger"
            plain
            size="small"
            @click="handleRemoveCompareGroup"
          >
            删除对比群体
          </el-button>
        </div>
      </div>

      <!-- 对比群组下拉（下一行） -->
      <div class="compare-group-bar" v-if="showCompareGroup">
        <span class="bar-label">对比群体</span>
        <el-select
          v-model="form.compare_group_ids"
          placeholder="选择对比群组（可多选）"
          filterable
          clearable
          multiple
          collapse-tags
          style="width: 360px"
        >
          <el-option
            v-for="group in compareGroupOptions"
            :key="group.group_id"
            :label="group.group_name"
            :value="group.group_id"
          >
            <span>{{ group.group_name }}</span>
            <span style="color: #909399; font-size: 12px; margin-left: 8px">({{ group.group_count || 0 }})</span>
          </el-option>
        </el-select>
      </div>

      <!-- 标签选择 -->
      <div class="form-section">
        <div class="section-header">
          <span class="section-title">选择分析标签</span>
          <span class="section-desc">选择需要分析分布情况的标签，支持多标签同时分析</span>
        </div>
        <LabelSelector
          v-if="availableLabels.length > 0"
          v-model="form.label_ids"
          :labels="availableLabels"
        />
        <el-empty v-else-if="!selectedEntityIdentifierId" description="请先选择群体所属实体" :image-size="80" />
        <el-empty v-else description="该实体暂无可用标签" :image-size="80" />
      </div>

      <!-- 已选标签栏 -->
      <div class="selected-tags-bar" v-if="form.label_ids.length > 0">
        <span
          v-for="labelId in form.label_ids"
          :key="labelId"
          class="tag-item"
        >
          {{ getLabelName(labelId) }}
          <span class="tag-close" @click="handleRemoveLabel(labelId)">×</span>
        </span>
        <span class="tag-clear" @click="handleClearLabels">清空</span>
      </div>

      <!-- 标签分布图表 -->
      <div class="chart-section">
        <div class="chart-grid" v-if="distributions.length > 0 && !analyzing">
          <LabelDistributionChart
            v-for="dist in distributions"
            :key="dist.label_id"
            :distribution="dist"
            :showExport="false"
            @remove="handleRemoveLabel"
          />
        </div>
        <div class="chart-loading" v-if="analyzing">
          <el-icon class="is-loading" :size="24" color="#409eff"><Loading /></el-icon>
          <span>正在计算标签分布...</span>
        </div>
        <div class="chart-empty" v-if="!analyzing && distributions.length === 0 && form.label_ids.length === 0 && form.group_id">
          <el-empty description="勾选上方标签后立即展示分布图表" :image-size="80" />
        </div>
      </div>

      <!-- 基本信息（放在最下方） -->
      <div class="form-section">
        <div class="section-header">
          <span class="section-title">基本信息</span>
        </div>
        <el-form :model="form" label-width="100px" style="max-width: 600px">
          <el-form-item label="分析名称" required>
            <el-input v-model="form.analysis_name" placeholder="请输入分析名称" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="分析描述">
            <el-input
              v-model="form.analysis_desc"
              type="textarea"
              placeholder="请输入分析描述（可选）"
              :rows="3"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading, Plus } from '@element-plus/icons-vue'
import { groupAnalysisApi } from '@/api/groupAnalysis'
import { entityIdentifierApi, type EntityIdentifier } from '@/api/entity'
import type { Group, AnalysisLabel, LabelDistribution } from '@/types'
import LabelSelector from './components/LabelSelector.vue'
import LabelDistributionChart from './components/LabelDistributionChart.vue'

const router = useRouter()
const route = useRoute()

// 是否编辑模式
const isEdit = computed(() => route.name === 'EditGroupAnalysis')
const editAnalysisId = computed(() => route.params.id as string)

// 状态
const loading = ref(false)
const saving = ref(false)
const analyzing = ref(false)
const initDone = ref(false)

// 实体标识列表 & 选中项
const entityIdentifierList = ref<EntityIdentifier[]>([])
const selectedEntityIdentifierId = ref('')
const showCompareGroup = ref(false)

// 当前实体标识下的可分析群组
const groupList = ref<Group[]>([])
const availableLabels = ref<AnalysisLabel[]>([])
const distributions = ref<LabelDistribution[]>([])
// 标签分布缓存：labelId -> 分布数据
const distributionCache = new Map<string, LabelDistribution>()

// 表单
const form = ref({
  analysis_id: undefined as string | undefined,
  analysis_name: '',
  analysis_desc: '',
  group_id: '',
  compare_group_ids: [] as string[],
  label_ids: [] as string[],
})

// 对比群组选项（排除当前群组）
const compareGroupOptions = computed(() => {
  return groupList.value.filter(g => g.group_id !== form.value.group_id)
})

// 当前选中群组的数量 & 覆盖率
const selectedGroupCount = computed(() => {
  const g = groupList.value.find(x => x.group_id === form.value.group_id)
  return g?.group_count ?? 0
})
const selectedGroupRate = computed(() => 100) // 暂时固定

// 移除对比群组
const handleRemoveCompareGroup = () => {
  showCompareGroup.value = false
  form.value.compare_group_ids = []
}

// 返回
const goBack = () => {
  router.push('/group/analysis')
}

// 获取标签名
const getLabelName = (labelId: string): string => {
  const label = availableLabels.value.find(l => l.label_id === labelId)
  return label ? label.label_name : labelId
}

// 移除标签
const handleRemoveLabel = (labelId: string) => {
  form.value.label_ids = form.value.label_ids.filter(id => id !== labelId)
}

const handleClearLabels = () => {
  form.value.label_ids = []
}

// ==================== 加载与联动 ====================

// 实体标识变更：清空群组/对比群组/标签/分布，重新加载群组和标签
const handleEntityIdentifierChange = async (identifierId: string) => {
  form.value.group_id = ''
  form.value.compare_group_ids = []
  form.value.label_ids = []
  distributions.value = []
  distributionCache.clear()
  showCompareGroup.value = false

  if (!identifierId) {
    groupList.value = []
    availableLabels.value = []
    return
  }

  loading.value = true
  try {
    // 并行加载群组和标签
    const [groupsRes, labelsRes] = await Promise.all([
      groupAnalysisApi.getGroups({ entity_identifier_id: identifierId }),
      groupAnalysisApi.getLabels(identifierId),
    ])
    groupList.value = (groupsRes.data.data || []).filter(
      (g: any) => g.group_count != null && g.group_count > 0
    )
    availableLabels.value = labelsRes.data.data || []
  } catch (e) {
    console.error('加载群组/标签失败:', e)
    ElMessage.error('加载群组/标签失败')
    groupList.value = []
    availableLabels.value = []
  } finally {
    loading.value = false
  }
}

// 群组变更：清空分布和缓存
const handleGroupChange = async (groupId: string) => {
  distributions.value = []
  distributionCache.clear()
  form.value.label_ids = []
  if (groupId) {
    form.value.compare_group_ids = form.value.compare_group_ids.filter(id => id !== groupId)
  }
}

// 标签变化时增量获取分布数据
watch(() => form.value.label_ids, (newIds, oldIds) => {
  if (!initDone.value) return

  const oldSet = new Set(oldIds || [])
  const newSet = new Set(newIds)

  // 移除被删除标签的缓存
  oldIds?.forEach(id => {
    if (!newSet.has(id)) distributionCache.delete(id)
  })

  // 找出新增的标签
  const addedIds = newIds.filter(id => !oldSet.has(id))

  // 更新展示列表：复用缓存 + 请求新增
  distributions.value = newIds
    .map(id => distributionCache.get(id))
    .filter((d): d is LabelDistribution => !!d)

  // 请求新增标签的分布
  if (addedIds.length > 0) {
    fetchNewDistributions(addedIds)
  }
}, { deep: true })

// 超时保护
const withTimeout = <T>(promise: Promise<T>, ms: number): Promise<T> => {
  return Promise.race([
    promise,
    new Promise<T>((_, reject) =>
      setTimeout(() => reject(new Error(`请求超时（${ms / 1000}秒）`)), ms)
    ),
  ])
}

// 增量获取新增标签的分布
const fetchNewDistributions = async (labelIds: string[]) => {
  if (!form.value.group_id) return

  analyzing.value = true
  try {
    // 并行请求所有新增标签的分布
    const results = await Promise.all(
      labelIds.map(async (labelId) => {
        try {
          const res = await withTimeout(
            groupAnalysisApi.getDistribution({
              group_id: form.value.group_id,
              label_id: labelId,
              compare_group_ids: form.value.compare_group_ids.length > 0 ? form.value.compare_group_ids : undefined,
            }),
            15000
          )
          return res.data.data as LabelDistribution
        } catch (err) {
          console.error(`获取标签 ${labelId} 分布失败:`, err)
          return null
        }
      })
    )

    // 写入缓存
    results.forEach((dist, i) => {
      if (dist) {
        const labelId = labelIds[i]
        const label = availableLabels.value.find(l => l.label_id === labelId)
        distributionCache.set(labelId, {
          ...dist,
          label_name: label?.label_name || dist.label_name,
          dataset_name: label?.dataset_name || dist.dataset_name,
          update_type: dist.update_type || '手动更新',
        })
      }
    })

    // 更新展示列表
    distributions.value = form.value.label_ids
      .map(id => distributionCache.get(id))
      .filter((d): d is LabelDistribution => !!d)
  } catch (error: any) {
    console.error('获取标签分布失败:', error?.message || error)
    ElMessage.error(error?.message || '获取标签分布失败')
  } finally {
    analyzing.value = false
  }
}

// ==================== 初始化 ====================

const initPage = async () => {
  loading.value = true
  try {
    // 加载实体标识列表
    const entityRes = await entityIdentifierApi.list()
    entityIdentifierList.value = entityRes.data.data || []

    // 编辑模式：加载分析详情
    if (isEdit.value && editAnalysisId.value) {
      await loadAnalysisDetail(editAnalysisId.value)
    } else {
      // 创建模式：检查 URL 中是否有 group_id（从群组页跳转过来）
      const queryGroupId = route.query.group_id as string
      if (queryGroupId) {
        // 需要先加载所有群组找到对应的 entity_identifier_id
        const allGroupsRes = await groupAnalysisApi.getGroups()
        const allGroups = allGroupsRes.data.data || []
        const target = allGroups.find((g: any) => g.group_id === queryGroupId)
        if (target?.entity_identifier_id) {
          selectedEntityIdentifierId.value = target.entity_identifier_id
          // handleEntityIdentifierChange 会加载该标识下的群组和标签
          await handleEntityIdentifierChange(target.entity_identifier_id)
          form.value.group_id = queryGroupId
        }
      }
    }

    // 标记初始化完成
    initDone.value = true

    // 编辑模式：如果有已选标签，并行获取所有标签分布
    if (form.value.label_ids.length > 0 && form.value.group_id) {
      await fetchNewDistributions(form.value.label_ids)
    }
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('页面初始化失败')
  } finally {
    loading.value = false
  }
}

// 加载分析详情（编辑模式）
const loadAnalysisDetail = async (analysisId: string) => {
  try {
    const res = await groupAnalysisApi.getAnalysisDetail(analysisId)
    const data = res.data.data
    if (!data) {
      ElMessage.error('分析记录不存在')
      router.replace('/group/analysis')
      return
    }
    form.value.analysis_id = data.analysis_id
    form.value.analysis_name = data.analysis_name || ''
    form.value.analysis_desc = data.analysis_desc || ''
    form.value.group_id = data.group_id || ''
    form.value.compare_group_ids = data.compare_group_ids || []
    form.value.label_ids = data.label_ids || []
    if (form.value.compare_group_ids.length > 0) {
      showCompareGroup.value = true
    }

    // 根据 group_id 反查 entity_identifier_id，加载该标识下的群组和标签
    if (form.value.group_id) {
      // 先加载所有群组找到对应的 entity_identifier_id
      const allGroupsRes = await groupAnalysisApi.getGroups()
      const allGroups = allGroupsRes.data.data || []
      const target = allGroups.find((g: any) => g.group_id === form.value.group_id)
      if (target?.entity_identifier_id) {
        selectedEntityIdentifierId.value = target.entity_identifier_id
        // 加载该标识下的群组和标签
        await handleEntityIdentifierChange(target.entity_identifier_id)
        // 恢复表单数据（handleEntityIdentifierChange 会清空）
        form.value.group_id = data.group_id || ''
        form.value.compare_group_ids = data.compare_group_ids || []
        form.value.label_ids = data.label_ids || []
      }
    }
  } catch (error) {
    console.error('加载分析详情失败:', error)
    ElMessage.error('加载分析详情失败')
  }
}

// 保存
const handleSave = async () => {
  if (!form.value.analysis_name.trim()) {
    ElMessage.warning('请输入分析名称')
    return
  }
  if (!form.value.group_id) {
    ElMessage.warning('请选择分析群组')
    return
  }
  if (form.value.label_ids.length === 0) {
    ElMessage.warning('请选择至少一个分析标签')
    return
  }

  saving.value = true
  try {
    await groupAnalysisApi.saveAnalysis({
      analysis_id: form.value.analysis_id,
      analysis_name: form.value.analysis_name.trim(),
      analysis_desc: form.value.analysis_desc.trim(),
      group_id: form.value.group_id,
      compare_group_ids: form.value.compare_group_ids,
      label_ids: form.value.label_ids,
    } as any)
    ElMessage.success(isEdit.value ? '修改成功' : '创建成功')
    router.push('/group/analysis')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  initPage()
})
</script>

<style scoped lang="scss">
.analysis-create-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;

  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
    flex: 1;
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.page-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 20px;
}

/* 对比群组下拉（下一行） */
.compare-group-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 10px 20px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;

  .bar-label {
    font-size: 14px;
    color: #606266;
    white-space: nowrap;
    flex-shrink: 0;
  }
}

/* 顶部横向选择区 */
.entity-group-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
  padding: 14px 20px;
  background: linear-gradient(to right, #f0f4fa, #e8ecf4);
  border-radius: 4px;
  border: 1px solid #d9dee8;

  .bar-item {
    display: flex;
    align-items: center;
    gap: 8px;

    .bar-label {
      font-size: 14px;
      color: #606266;
      white-space: nowrap;
      flex-shrink: 0;
    }
  }

  .bar-info {
    gap: 12px;

    .group-count {
      font-size: 14px;
      color: #303133;
      font-weight: 500;
    }

    .group-coverage {
      font-size: 13px;
      color: #909399;
    }
  }
}

.form-section {
  background: #fff;
  margin-top: 16px;
  padding: 16px 20px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;

  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;

    .section-title {
      font-size: 15px;
      font-weight: 500;
      color: #303133;
    }

    .section-desc {
      font-size: 13px;
      color: #909399;
      margin-left: 12px;
    }
  }
}

.selected-tags-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 16px;
  padding: 8px 14px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  min-height: 40px;

  .tag-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 2px 10px;
    background: #f0f2f5;
    border-radius: 3px;
    font-size: 13px;
    color: #303133;
    line-height: 24px;

    .tag-close {
      cursor: pointer;
      color: #c0c4cc;
      font-size: 14px;
      margin-left: 2px;
      &:hover {
        color: #f56c6c;
      }
    }
  }

  .tag-clear {
    cursor: pointer;
    font-size: 13px;
    color: #409eff;
    margin-left: 8px;
    &:hover {
      text-decoration: underline;
    }
  }
}

.chart-section {
  margin-top: 14px;

  .chart-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .chart-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 60px;
    background: #fff;
    border-radius: 4px;
    border: 1px solid #e4e7ed;
    color: #909399;
    font-size: 14px;
  }

  .chart-empty {
    background: #fff;
    border-radius: 4px;
    border: 1px solid #e4e7ed;
    padding: 30px;
    text-align: center;
  }
}
</style>
