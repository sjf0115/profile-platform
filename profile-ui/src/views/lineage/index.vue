<template>
  <div class="lineage-page">
    <div class="page-header">
      <h2 class="page-title">数据血缘</h2>
    </div>

    <div class="page-card">
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-select v-model="nodeType" placeholder="节点类型" style="width: 140px" @change="onNodeTypeChange">
          <el-option v-for="t in assetTypes" :key="t.code" :label="t.name" :value="t.code" />
        </el-select>
        <el-select v-model="nodeId" placeholder="选择节点" style="width: 240px" filterable :loading="nodesLoading">
          <el-option v-for="n in nodeOptions" :key="n.id" :label="n.name" :value="n.id" />
        </el-select>
        <el-radio-group v-model="direction" style="margin-left: 12px">
          <el-radio-button value="upstream">上游</el-radio-button>
          <el-radio-button value="downstream">下游</el-radio-button>
          <el-radio-button value="both">全部</el-radio-button>
        </el-radio-group>
        <el-input-number v-model="depth" :min="1" :max="10" style="width: 120px; margin-left: 12px" />
        <el-button type="primary" @click="fetchGraph" :loading="loading" style="margin-left: 12px">查询</el-button>
        <el-button @click="handleRebuild" :loading="rebuilding" style="margin-left: auto">全量重建</el-button>
      </div>

      <!-- 血缘图 -->
      <div class="chart-container" v-loading="loading">
        <v-chart v-if="chartOption" :option="chartOption" style="height: 600px" autoresize @click="onNodeClick" />
        <el-empty v-else description="请选择节点并查询血缘关系" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { use } from 'echarts/core'
import { GraphChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { lineageApi } from '@/api/lineage'
import type { LineageGraph, LineageNode } from '@/api/lineage'

use([GraphChart, TooltipComponent, LegendComponent, CanvasRenderer])

const router = useRouter()

const assetTypes = [
  { code: 'datasource', name: '数据源' },
  { code: 'dataset', name: '数据集' },
  { code: 'label', name: '标签' },
  { code: 'event', name: '事件' },
  { code: 'group', name: '群组' },
  { code: 'analysis', name: '分析' },
  { code: 'export', name: '投递' },
  { code: 'application', name: '应用' },
]

const typeColorMap: Record<string, string> = {
  datasource: '#409EFF',
  dataset: '#67C23A',
  label: '#E6A23C',
  event: '#F56C6C',
  group: '#909399',
  analysis: '#B37FEB',
  export: '#FF85C0',
  application: '#36CFC9',
}

const nodeType = ref('dataset')
const nodeId = ref('')
const direction = ref('both')
const depth = ref(3)
const loading = ref(false)
const rebuilding = ref(false)
const nodesLoading = ref(false)
const graphData = ref<LineageGraph | null>(null)
const nodeOptions = ref<{ id: string; name: string }[]>([])

const chartOption = computed(() => {
  if (!graphData.value || graphData.value.nodes.length === 0) return null

  const categories = [...new Set(graphData.value.nodes.map(n => n.type))].map(t => ({
    name: assetTypes.find(a => a.code === t)?.name || t,
  }))

  const nodes = graphData.value.nodes.map(n => ({
    id: `${n.type}:${n.id}`,
    name: n.name || n.id,
    category: categories.findIndex(c => c.name === (assetTypes.find(a => a.code === n.type)?.name || n.type)),
    symbolSize: 30,
    itemStyle: { color: typeColorMap[n.type] || '#999' },
    label: { show: true, fontSize: 11 },
  }))

  const links = graphData.value.edges.map(e => ({
    source: e.source,
    target: e.target,
    label: { show: true, formatter: e.relation_name || e.relation, fontSize: 10, color: '#666' },
    lineStyle: { color: '#ccc', curveness: 0.1 },
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => {
        if (p.dataType === 'node') return `${p.data.name}`
        return `${p.data.source} → ${p.data.target}`
      },
    },
    legend: { data: categories.map(c => c.name), top: 10 },
    animationDuration: 500,
    series: [{
      type: 'graph',
      layout: 'force',
      data: nodes,
      links,
      categories,
      roam: true,
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: 8,
      force: { repulsion: 300, edgeLength: 150, gravity: 0.1 },
      emphasis: { focus: 'adjacency', lineStyle: { width: 3 } },
    }],
  }
})

const onNodeTypeChange = async () => {
  nodeId.value = ''
  nodeOptions.value = []
  await fetchNodes()
}

const fetchNodes = async () => {
  nodesLoading.value = true
  try {
    const res = await lineageApi.getNodes({ node_type: nodeType.value })
    nodeOptions.value = (res.data.data || []).map((n: any) => ({ id: n.id, name: n.name || n.id }))
  } catch {
    nodeOptions.value = []
  } finally {
    nodesLoading.value = false
  }
}

const fetchGraph = async () => {
  if (!nodeId.value) {
    ElMessage.warning('请选择或输入节点ID')
    return
  }
  loading.value = true
  try {
    const res = await lineageApi.getGraph({
      node_type: nodeType.value,
      node_id: nodeId.value,
      direction: direction.value,
      depth: depth.value,
    })
    graphData.value = res.data.data
  } catch (e: any) {
    ElMessage.error(e.message || '查询血缘失败')
  } finally {
    loading.value = false
  }
}

const onNodeClick = (params: any) => {
  if (params.dataType === 'node') {
    const id = params.data.id as string
    const parts = id.split(':', 2)
    if (parts.length === 2) {
      nodeType.value = parts[0]
      nodeId.value = parts[1]
      fetchGraph()
    }
  }
}

const handleRebuild = async () => {
  try {
    await ElMessageBox.confirm('全量重建将清除并重新计算所有血缘关系，确定继续？', '全量重建', {
      type: 'warning',
    })
    rebuilding.value = true
    const res = await lineageApi.rebuild()
    ElMessage.success(`重建完成，共处理 ${res.data.data} 个资产`)
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '重建失败')
  } finally {
    rebuilding.value = false
  }
}

// 从 URL query 获取初始参数
const route = useRoute()
if (route.query.node_type) nodeType.value = route.query.node_type as string
if (route.query.node_id) nodeId.value = route.query.node_id as string
// 初始化加载节点列表
fetchNodes()
if (nodeId.value) fetchGraph()
</script>

<style scoped>
.lineage-page {
  padding: 0;
}
.page-header {
  margin-bottom: 16px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
.page-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  min-height: calc(100vh - 180px);
}
.toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.chart-container {
  min-height: 600px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
</style>
