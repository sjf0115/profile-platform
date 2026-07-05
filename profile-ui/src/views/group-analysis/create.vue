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
      <!-- 基本信息 -->
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

      <!-- 群组选择 -->
      <div class="form-section">
        <div class="section-header">
          <span class="section-title">选择群组</span>
          <span class="section-desc">选择主分析群组及可选的对比群组</span>
        </div>
        <el-form label-width="100px" style="max-width: 600px">
          <el-form-item label="分析群组" required>
            <el-select
              v-model="form.group_id"
              placeholder="选择主分析群组"
              filterable
              style="width: 100%"
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
                  ({{ group.group_count || 0 }} · {{ group.entity_identifier_name || '-' }})
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="对比群组">
            <el-select
              v-model="form.compare_group_ids"
              placeholder="选择对比群组（可选，可多选）"
              filterable
              clearable
              multiple
              collapse-tags
              style="width: 100%"
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
          </el-form-item>
        </el-form>
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
        <el-empty v-else-if="!form.group_id" description="请先选择分析群组" :image-size="80" />
        <el-empty v-else description="该群组暂无可用标签" :image-size="80" />
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading } from '@element-plus/icons-vue'
import { groupAnalysisApi } from '@/api/groupAnalysis'
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
const groupList = ref<Group[]>([])
const availableLabels = ref<AnalysisLabel[]>([])
const distributions = ref<LabelDistribution[]>([])

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

// 标签变化时获取分布数据（唯一触发点）
watch(() => form.value.label_ids, (newIds) => {
  console.log('[watch] label_ids changed:', newIds, 'initDone:', initDone.value)
  if (!initDone.value) return
  fetchDistribution([...newIds])
}, { deep: true })

// 超时保护：如果 API 调用超过 15 秒未返回，强制重置 loading
const withTimeout = <T>(promise: Promise<T>, ms: number): Promise<T> => {
  return Promise.race([
    promise,
    new Promise<T>((_, reject) =>
      setTimeout(() => reject(new Error(`请求超时（${ms / 1000}秒）`)), ms)
    ),
  ])
}

const fetchDistribution = async (labelIds: string[]) => {
  console.log('[fetchDistribution] called with:', labelIds, 'groupId:', form.value.group_id)
  if (!form.value.group_id || labelIds.length === 0) {
    distributions.value = []
    return
  }

  analyzing.value = true
  try {
    const res = await withTimeout(
      groupAnalysisApi.getDistribution({
        group_id: form.value.group_id,
        label_ids: labelIds,
        compare_group_ids: form.value.compare_group_ids.length > 0 ? form.value.compare_group_ids : undefined,
      }),
      15000
    )
    const data = res.data.data || []
    console.log('[fetchDistribution] response data:', data.length, 'items')
    distributions.value = data.map((dist: any) => {
      const label = availableLabels.value.find(l => l.label_id === dist.label_id)
      return {
        ...dist,
        label_name: label?.label_name || dist.label_name,
        dataset_name: label?.dataset_name || dist.dataset_name,
        update_type: '手动更新',
      }
    })
  } catch (error: any) {
    console.error('获取标签分布失败:', error?.message || error)
    distributions.value = []
    ElMessage.error(error?.message || '获取标签分布失败')
  } finally {
    analyzing.value = false
  }
}

// 获取群组列表
const fetchGroups = async () => {
  loading.value = true
  try {
    const res = await groupAnalysisApi.getGroups()
    groupList.value = res.data.data || []

    // 编辑模式：加载已有分析数据
    if (isEdit.value && editAnalysisId.value) {
      await loadAnalysisDetail(editAnalysisId.value)
    } else {
      // 创建模式：检查 URL 中是否有 group_id 查询参数（从群组页跳转过来）
      const queryGroupId = route.query.group_id as string
      if (queryGroupId) {
        const exists = groupList.value.some(g => g.group_id === queryGroupId)
        if (exists) {
          form.value.group_id = queryGroupId
          await handleGroupChange(queryGroupId)
        }
      }
    }

    // 标记初始化完成，后续标签变化才触发分布请求
    initDone.value = true

    // 编辑模式：如果有已选标签，立即获取分布
    if (form.value.label_ids.length > 0 && form.value.group_id) {
      await fetchDistribution(form.value.label_ids)
    }
  } catch (error) {
    console.error('获取群组列表失败:', error)
    ElMessage.error('获取群组列表失败')
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

    // 如果选中了群组，自动加载标签
    if (form.value.group_id) {
      await loadLabelsForGroup(form.value.group_id)
    }
  } catch (error) {
    console.error('加载分析详情失败:', error)
    ElMessage.error('加载分析详情失败')
  }
}

// 群组变更时加载标签
const handleGroupChange = async (groupId: string) => {
  if (!groupId) {
    availableLabels.value = []
    return
  }
  // 清空分布和标签
  distributions.value = []
  form.value.label_ids = []
  form.value.compare_group_ids = form.value.compare_group_ids.filter(id => id !== groupId)
  await loadLabelsForGroup(groupId)
}

const loadLabelsForGroup = async (groupId: string) => {
  const group = groupList.value.find(g => g.group_id === groupId)
  if (!group) return

  const entityIdentifierId = group.entity_identifier_id
  if (!entityIdentifierId) {
    ElMessage.warning('该群组未关联实体标识，无法选择标签')
    availableLabels.value = []
    return
  }

  try {
    const res = await groupAnalysisApi.getLabels(entityIdentifierId)
    availableLabels.value = res.data.data || []
  } catch (error) {
    console.error('获取标签失败:', error)
    availableLabels.value = []
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
  fetchGroups()
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
