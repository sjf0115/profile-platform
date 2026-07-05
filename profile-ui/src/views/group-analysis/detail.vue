<template>
  <div class="analysis-detail-page" v-loading="loading">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">{{ analysisName || '群组分析' }}</h2>
        <el-tag v-if="isSavedAnalysis" size="small" type="info" style="margin-left: 8px">已保存</el-tag>
      </div>
      <div class="header-right">
        <div class="group-info" v-if="currentGroupName">
          <span class="info-label">分析群组</span>
          <el-tag type="primary" effect="plain" size="small">{{ currentGroupName }}</el-tag>
          <span class="info-count" v-if="currentGroupCount !== null">({{ currentGroupCount }})</span>
        </div>
        <div class="group-info" v-if="compareGroupIds.length > 0">
          <span class="info-label">对比</span>
          <el-tag
            v-for="cgId in compareGroupIds"
            :key="cgId"
            type="warning"
            effect="plain"
            size="small"
          >
            {{ getGroupName(cgId) }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 主体滚动区 -->
    <div class="page-body">
      <!-- 标签选择面板 -->
      <div class="label-panel">
        <LabelSelector
          v-model="selectedLabelIds"
          :labels="availableLabels"
        />
      </div>

      <!-- 已选标签栏 -->
      <div class="selected-tags-bar" v-if="selectedLabelIds.length > 0">
        <span
          v-for="labelId in selectedLabelIds"
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
        <div class="chart-empty" v-if="!analyzing && distributions.length === 0 && !loading">
          <el-empty
            :description="availableLabels.length > 0 ? '勾选上方标签后立即展示分布图表' : '该群组暂无可用标签'"
            :image-size="80"
          />
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

// 是否已保存的分析
const isSavedAnalysis = computed(() => !!route.params.id)

// 状态
const loading = ref(false)
const analyzing = ref(false)
const groupList = ref<Group[]>([])
const availableLabels = ref<AnalysisLabel[]>([])
const distributions = ref<LabelDistribution[]>([])

// 分析记录数据
const analysisName = ref('')
const currentGroupId = ref<string>('')
const compareGroupIds = ref<string[]>([])
const selectedLabelIds = ref<string[]>([])

// 是否初始化完成（防止 init 过程中 watcher 重复触发）
const initDone = ref(false)

// 计算属性
const currentGroupName = computed(() => {
  const g = groupList.value.find(g => g.group_id === currentGroupId.value)
  return g?.group_name || ''
})

const currentGroupCount = computed(() => {
  const g = groupList.value.find(g => g.group_id === currentGroupId.value)
  return g?.group_count ?? null
})

const getGroupName = (groupId: string): string => {
  const g = groupList.value.find(g => g.group_id === groupId)
  return g?.group_name || groupId
}

const getLabelName = (labelId: string): string => {
  const label = availableLabels.value.find(l => l.label_id === labelId)
  return label ? label.label_name : labelId
}

// 返回
const goBack = () => {
  if (isSavedAnalysis.value) {
    router.push('/group/analysis')
  } else {
    router.back()
  }
}

// 移除标签
const handleRemoveLabel = (labelId: string) => {
  selectedLabelIds.value = selectedLabelIds.value.filter(id => id !== labelId)
}

const handleClearLabels = () => {
  selectedLabelIds.value = []
}

// 标签变化时获取分布数据（唯一触发点）
watch(selectedLabelIds, (newIds) => {
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
  if (!currentGroupId.value || labelIds.length === 0) {
    distributions.value = []
    return
  }

  analyzing.value = true
  try {
    const res = await withTimeout(
      groupAnalysisApi.getDistribution({
        group_id: currentGroupId.value,
        label_ids: labelIds,
        compare_group_ids: compareGroupIds.value.length > 0 ? compareGroupIds.value : undefined,
      }),
      15000
    )
    const data = res.data.data || []
    distributions.value = data.map((dist: any) => {
      const label = availableLabels.value.find(l => l.label_id === dist.label_id)
      return {
        ...dist,
        label_name: label?.label_name || dist.label_name,
        dataset_name: label?.dataset_name || dist.dataset_name,
        update_type: label?.update_type === 2 ? '周期更新' : '手动更新',
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

// 初始化
const init = async () => {
  loading.value = true
  let savedLabelIds: string[] = []
  try {
    // 1. 加载群组列表
    const groupsRes = await groupAnalysisApi.getGroups()
    groupList.value = groupsRes.data.data || []

    const analysisId = route.params.id as string
    const queryGroupId = route.query.group_id as string

    if (analysisId) {
      // === 已保存分析模式 ===
      const detailRes = await groupAnalysisApi.getAnalysisDetail(analysisId)
      const data = detailRes.data.data
      if (!data) {
        ElMessage.error('分析记录不存在')
        router.replace('/group/analysis')
        return
      }
      analysisName.value = data.analysis_name || ''
      currentGroupId.value = data.group_id || ''
      compareGroupIds.value = data.compare_group_ids || []
      // 缓存已选标签，后续恢复
      savedLabelIds = data.label_ids || []
    } else if (queryGroupId) {
      // === 独立分析模式（从群组页跳转） ===
      const group = groupList.value.find(g => g.group_id === queryGroupId)
      if (!group) {
        ElMessage.error('群组不存在')
        router.replace('/group/analysis')
        return
      }
      analysisName.value = `${group.group_name} - 分析`
      currentGroupId.value = queryGroupId
    } else {
      ElMessage.error('缺少分析参数')
      router.replace('/group/analysis')
      return
    }

    // 2. 加载可分析标签
    if (currentGroupId.value) {
      const group = groupList.value.find(g => g.group_id === currentGroupId.value)
      if (group?.entity_identifier_id) {
        const labelsRes = await groupAnalysisApi.getLabels(group.entity_identifier_id)
        availableLabels.value = labelsRes.data.data || []
      }

      // 3. 恢复已选标签（已保存分析模式）
      if (analysisId && savedLabelIds.length > 0) {
        selectedLabelIds.value = [...savedLabelIds]
      }
    }

    // 标记初始化完成，后续标签变化才触发分布请求
    initDone.value = true

    // 如果有已选标签，立即获取分布
    if (selectedLabelIds.value.length > 0) {
      await fetchDistribution(selectedLabelIds.value)
    }
  } catch (error) {
    console.error('加载分析详情失败:', error)
    ElMessage.error('加载分析详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  init()
})
</script>

<style scoped lang="scss">
.analysis-detail-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .page-title {
      margin: 0;
      font-size: 17px;
      font-weight: 500;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .group-info {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;

      .info-label {
        color: #909399;
      }
      .info-count {
        color: #909399;
        font-size: 12px;
      }
    }
  }
}

.page-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.label-panel {
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
}

.selected-tags-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
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
