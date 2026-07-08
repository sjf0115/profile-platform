<template>
  <div class="distribution-chart-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-title">
        <span class="label-name">{{ distribution.label_name }}</span>
        <el-tooltip :content="`${distribution.dataset_name} · ${distribution.update_type}`" placement="top">
          <el-icon class="help-icon"><QuestionFilled /></el-icon>
        </el-tooltip>
      </div>
      <div class="header-actions">
        <div class="action-item">
          <span class="action-label">排除空值</span>
          <el-switch v-model="excludeEmpty" size="small" />
        </div>
        <el-select
          v-model="selectedValues"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="标签值选择"
          size="small"
          style="width: 130px"
          clearable
        >
          <el-option
            v-for="item in allValues"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
        <!-- 更多操作弹出菜单 -->
        <el-popover
          :visible="popoverVisible"
          placement="bottom-end"
          :width="160"
          popper-class="chart-more-popover"
          @update:visible="onPopoverVisibleChange"
        >
          <template #reference>
            <span class="more-btn" @click.stop="popoverVisible = !popoverVisible">
              <el-icon><MoreFilled /></el-icon>
            </span>
          </template>

          <!-- 主菜单：4项 -->
          <div v-if="menuView === 'main'" class="custom-menu">
            <div class="menu-item" @click="menuView = 'chart'">
              <el-icon class="mi-icon"><Histogram /></el-icon>
              <span class="mi-label">图表切换</span>
              <el-icon class="mi-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="menu-item" @click="menuView = 'sort'">
              <el-icon class="mi-icon"><Sort /></el-icon>
              <span class="mi-label">排序</span>
              <el-icon class="mi-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="menu-item" @click="toggleDataTable">
              <el-icon class="mi-icon"><Grid /></el-icon>
              <span class="mi-label">数据查看</span>
            </div>
            <div v-if="showExport" class="menu-item" @click="doExportImage">
              <el-icon class="mi-icon"><Download /></el-icon>
              <span class="mi-label">导出</span>
            </div>
          </div>

          <!-- 图表切换子菜单 -->
          <div v-else-if="menuView === 'chart'" class="custom-menu">
            <div class="menu-item back-item" @click="menuView = 'main'">
              <el-icon class="mi-icon"><ArrowLeft /></el-icon>
              <span class="mi-label">图表切换</span>
            </div>
            <div class="menu-divider"></div>
            <div class="menu-item" @click="switchChartType('bar')">
              <el-icon class="mi-icon"><Histogram /></el-icon>
              <span class="mi-label">纵向柱状图</span>
              <el-icon v-if="chartType === 'bar'" class="mi-check"><Check /></el-icon>
            </div>
            <div class="menu-item" @click="switchChartType('horizontal')">
              <el-icon class="mi-icon"><Histogram /></el-icon>
              <span class="mi-label">横向柱状图</span>
              <el-icon v-if="chartType === 'horizontal'" class="mi-check"><Check /></el-icon>
            </div>
            <div class="menu-item" @click="switchChartType('pie')">
              <el-icon class="mi-icon"><PieChart /></el-icon>
              <span class="mi-label">饼图</span>
              <el-icon v-if="chartType === 'pie'" class="mi-check"><Check /></el-icon>
            </div>
          </div>

          <!-- 排序子菜单 -->
          <div v-else-if="menuView === 'sort'" class="custom-menu">
            <div class="menu-item back-item" @click="menuView = 'main'">
              <el-icon class="mi-icon"><ArrowLeft /></el-icon>
              <span class="mi-label">排序</span>
            </div>
            <div class="menu-divider"></div>
            <div class="menu-item" @click="switchSortMode('count-desc')">
              <span class="mi-label">按人数降序</span>
              <el-icon v-if="sortMode === 'count-desc'" class="mi-check"><Check /></el-icon>
            </div>
            <div class="menu-item" @click="switchSortMode('count-asc')">
              <span class="mi-label">按人数升序</span>
              <el-icon v-if="sortMode === 'count-asc'" class="mi-check"><Check /></el-icon>
            </div>
            <div class="menu-item" @click="switchSortMode('alpha')">
              <span class="mi-label">按名称排序</span>
              <el-icon v-if="sortMode === 'alpha'" class="mi-check"><Check /></el-icon>
            </div>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-wrapper">
      <v-chart ref="chartRef" :option="chartOption" autoresize style="height: 250px" />
    </div>

    <!-- 数据表格（可折叠） -->
    <div class="data-table" v-if="showTable">
      <el-table :data="tableData" size="small" max-height="180" stripe>
        <el-table-column prop="value" label="标签值" min-width="80" show-overflow-tooltip />
        <el-table-column label="当前占比" width="80" align="right">
          <template #default="{ row }">
            <span style="color: #409EFF">{{ row.current_rate.toFixed(1) }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="current_count" label="当前人数" width="80" align="right" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { QuestionFilled, MoreFilled, Histogram, PieChart, Sort, Download, Check, Grid, ArrowRight, ArrowLeft } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { BarChart, PieChart as EPieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import type { LabelDistribution } from '@/types'

use([BarChart, EPieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const props = withDefaults(defineProps<{
  distribution: LabelDistribution
  showExport?: boolean
}>(), {
  showExport: true,
})

const emit = defineEmits<{
  'remove': [labelId: string]
}>()

const chartRef = ref<InstanceType<typeof VChart> | null>(null)
const excludeEmpty = ref(false)
const selectedValues = ref<string[]>([])
const showTable = ref(false)
const chartType = ref<'bar' | 'horizontal' | 'pie'>('bar')
const sortMode = ref<'count-desc' | 'count-asc' | 'alpha'>('count-desc')

// 弹出菜单状态
const popoverVisible = ref(false)
const menuView = ref<'main' | 'chart' | 'sort'>('main')

const onPopoverVisibleChange = (visible: boolean) => {
  popoverVisible.value = visible
  if (!visible) menuView.value = 'main'
}

// 是否有对比群组
const hasCompare = computed(() => {
  return props.distribution.values.some(v => v.compare_count != null)
})

// 所有可选值
const allValues = computed(() => {
  return props.distribution.values
    .filter(v => !excludeEmpty.value || (v.value && v.value.trim() !== ''))
    .map(v => v.value)
})

// 排序后的过滤数据
const filteredData = computed(() => {
  let data = [...props.distribution.values]
  if (excludeEmpty.value) {
    data = data.filter(v => v.value && v.value.trim() !== '')
  }
  if (selectedValues.value.length > 0) {
    data = data.filter(v => selectedValues.value.includes(v.value))
  }
  // 排序
  if (sortMode.value === 'count-desc') {
    data.sort((a, b) => b.current_count - a.current_count)
  } else if (sortMode.value === 'count-asc') {
    data.sort((a, b) => a.current_count - b.current_count)
  } else if (sortMode.value === 'alpha') {
    data.sort((a, b) => (a.value || '').localeCompare(b.value || ''))
  }
  return data.slice(0, 15)
})

const tableData = computed(() => filteredData.value)

// 菜单操作
const switchChartType = (type: 'bar' | 'horizontal' | 'pie') => {
  chartType.value = type
  popoverVisible.value = false
}

const switchSortMode = (mode: 'count-desc' | 'count-asc' | 'alpha') => {
  sortMode.value = mode
  popoverVisible.value = false
}

const toggleDataTable = () => {
  showTable.value = !showTable.value
  popoverVisible.value = false
  if (showTable.value && props.showExport) exportCSV()
}

const doExportImage = () => {
  popoverVisible.value = false
  exportImage()
}

// 导出 CSV
const exportCSV = () => {
  const data = filteredData.value
  const header = '标签值,当前人数,当前占比(%)\n'
  const rows = data.map(d =>
    `${d.value || '(空)'},${d.current_count},${d.current_rate.toFixed(2)}`
  ).join('\n')
  const csv = '\uFEFF' + header + rows // BOM for Excel 中文兼容
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${props.distribution.label_name}_分布数据.csv`
  link.click()
  URL.revokeObjectURL(url)
}

// 导出图片
const exportImage = () => {
  const chart = chartRef.value?.chart
  if (!chart) return
  const url = chart.getDataURL({
    type: 'png',
    pixelRatio: 2,
    backgroundColor: '#fff',
  })
  const link = document.createElement('a')
  link.href = url
  link.download = `${props.distribution.label_name}_分布图.png`
  link.click()
}

// ECharts 配置
const chartOption = computed(() => {
  const data = filteredData.value
  const categories = data.map(d => d.value || '(空)')
  const currentSeries = data.map(d => parseFloat(d.current_rate.toFixed(2)))
  const compareSeries = data.map(d => d.compare_rate != null ? parseFloat(d.compare_rate.toFixed(2)) : 0)

  // 饼图模式
  if (chartType.value === 'pie') {
    const pieData = data.map((d, i) => ({
      name: categories[i],
      value: currentSeries[i],
    }))
    return {
      tooltip: {
        trigger: 'item',
        formatter: (p: any) => `${p.name}: ${p.value}% (${p.data.count || '-'})`,
      },
      legend: {
        bottom: 0,
        type: 'scroll',
        textStyle: { fontSize: 11, color: '#909399' },
        itemWidth: 12,
        itemHeight: 8,
      },
      series: [{
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '45%'],
        data: pieData,
        label: {
          show: true,
          formatter: '{b}: {c}%',
          fontSize: 11,
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 1,
        },
      }],
    }
  }

  // 横向柱状图
  const isHorizontal = chartType.value === 'horizontal'
  const barRadius = isHorizontal ? [0, 2, 2, 0] : [2, 2, 0, 0]

  const series: any[] = [
    {
      name: '当前人群',
      type: 'bar',
      data: currentSeries,
      itemStyle: { color: '#409EFF', borderRadius: barRadius },
      barMaxWidth: 18,
    },
  ]

  if (hasCompare.value) {
    series.push({
      name: '对比人群',
      type: 'bar',
      data: compareSeries,
      itemStyle: { color: '#E6A23C', borderRadius: barRadius },
      barMaxWidth: 18,
    })
  }

  const gridConfig = isHorizontal
    ? { left: 80, right: 20, top: 8, bottom: 32 }
    : { left: 42, right: 12, top: 8, bottom: 32 }

  const categoryAxis: any = {
    type: 'category',
    data: categories,
    axisLabel: {
      interval: 0,
      rotate: !isHorizontal && categories.length > 5 ? 20 : 0,
      fontSize: 11,
      color: '#909399',
    },
    axisLine: { lineStyle: { color: '#e4e7ed' } },
    axisTick: { show: false },
  }

  const valueAxis: any = {
    type: 'value',
    axisLabel: {
      formatter: '{value}%',
      fontSize: 11,
      color: '#909399',
    },
    splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } },
  }

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        let html = `<strong>${params[0]?.name}</strong><br/>`
        for (const p of params) {
          html += `${p.marker} ${p.seriesName}: ${p.value}%<br/>`
        }
        return html
      },
    },
    legend: {
      bottom: 0,
      data: series.map(s => s.name),
      textStyle: { fontSize: 11, color: '#909399' },
      itemWidth: 12,
      itemHeight: 8,
    },
    grid: gridConfig,
    xAxis: isHorizontal ? valueAxis : categoryAxis,
    yAxis: isHorizontal ? categoryAxis : valueAxis,
    series,
  }
})
</script>

<style scoped lang="scss">
.distribution-chart-card {
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  overflow: hidden;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 14px;
    border-bottom: 1px solid #ebeef5;

    .header-title {
      display: flex;
      align-items: center;
      gap: 6px;

      .label-name {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      .help-icon {
        font-size: 13px;
        color: #c0c4cc;
        cursor: help;
      }
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 10px;

      .action-item {
        display: flex;
        align-items: center;
        gap: 4px;

        .action-label {
          font-size: 12px;
          color: #909399;
          white-space: nowrap;
        }
      }

      .more-btn {
        cursor: pointer;
        font-size: 16px;
        color: #c0c4cc;
        &:hover { color: #409eff; }
      }
    }
  }

  .chart-wrapper {
    padding: 8px 10px 2px;
  }

  .data-table {
    padding: 0 10px 10px;
    border-top: 1px solid #ebeef5;
    padding-top: 8px;
  }
}
</style>

<!-- 弹出菜单全局样式（不可 scoped，因 popover 渲染在 body 下） -->
<style lang="scss">
.chart-more-popover {
  padding: 4px 0 !important;

  .custom-menu {
    .menu-item {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      cursor: pointer;
      font-size: 13px;
      color: #303133;
      transition: background 0.15s;

      &:hover {
        background: #f5f7fa;
      }

      .mi-icon {
        width: 18px;
        margin-right: 6px;
        font-size: 15px;
        color: #606266;
      }

      .mi-label {
        flex: 1;
      }

      .mi-arrow {
        font-size: 12px;
        color: #c0c4cc;
      }

      .mi-check {
        font-size: 13px;
        color: #409eff;
        margin-left: 4px;
      }

      &.back-item {
        font-weight: 600;
        color: #303133;
      }
    }

    .menu-divider {
      height: 1px;
      background: #ebeef5;
      margin: 4px 0;
    }
  }
}
</style>
