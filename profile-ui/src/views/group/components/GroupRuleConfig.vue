<template>
  <div class="group-rule-config">
    <!-- 规则组列表容器（带左侧垂直线） -->
    <div class="rule-groups-container" :class="{ 'has-logic': ruleGroups.length > 1 }">
      <!-- 规则组间逻辑选择器（只有一个，在最左侧） -->
      <div v-if="ruleGroups.length > 1" class="groups-logic-wrapper">
        <div
          class="logic-toggle-btn"
          :class="{ 'is-or': groupsLogic === 'OR', 'is-disabled': readonly }"
          @click="!readonly && toggleGroupsLogic()"
        >
          {{ groupsLogic === 'AND' ? '且' : '或' }}
        </div>
      </div>

      <!-- 规则组列表 -->
      <div class="rule-group-list">
        <div
          v-for="(group, groupIndex) in ruleGroups"
          :key="group.id"
          class="rule-group-wrapper"
        >
          <div class="rule-group">
            <!-- 规则组内容 -->
            <div class="rule-group-content">
              <div class="rules-wrapper">
                <!-- 左侧垂直线条（包含组内逻辑选择器） -->
                <div class="group-left-line">
                  <div v-if="group.rules.length > 1" class="inner-logic-wrapper">
                    <div
                      class="logic-toggle-btn"
                      :class="{ 'is-or': group.inner_logic === 'OR', 'is-disabled': readonly }"
                      @click="!readonly && toggleInnerLogic(group)"
                    >
                      {{ group.inner_logic === 'AND' ? '且' : '或' }}
                    </div>
                  </div>
                </div>

              <!-- 规则列表 -->
              <div class="rule-list">
                <div
                  v-for="(rule, ruleIndex) in group.rules"
                  :key="rule.id"
                  class="rule-item"
                >
                  <!-- 规则类型标签 -->
                  <div class="rule-type-tag" :class="rule.rule_type">
                    {{ getRuleTypeLabel(rule.rule_type) }}
                  </div>

                  <!-- 规则内容 -->
                  <div class="rule-content">
                    <!-- 标签规则 -->
                    <template v-if="rule.rule_type === 'tag'">
                      <el-select
                        v-model="rule.tag_id"
                        placeholder="选择标签"
                        size="default"
                        style="width: 180px"
                        filterable
                        :disabled="readonly || !hasEntityIdentifier"
                        @change="(val: string) => handleTagChange(rule, val)"
                      >
                        <el-option
                          v-for="tag in tagList"
                          :key="tag.label_id"
                          :label="tag.label_name"
                          :value="tag.label_id"
                        />
                      </el-select>

                      <el-select
                        v-model="rule.operator"
                        placeholder="操作符"
                        size="default"
                        style="width: 120px"
                        :disabled="readonly"
                      >
                        <el-option
                          v-for="op in getTagOperators(rule.tag_data_type)"
                          :key="op.value"
                          :label="op.label"
                          :value="op.value"
                        />
                      </el-select>

                      <el-input
                        v-model="rule.value"
                        placeholder="请输入值"
                        size="default"
                        style="width: 150px"
                        :disabled="readonly"
                      />
                    </template>

                    <!-- 群组规则 -->
                    <template v-if="rule.rule_type === 'group'">
                      <el-select v-model="rule.relation" size="default" style="width: 100px" :disabled="readonly">
                        <el-option label="包含" value="in" />
                        <el-option label="不包含" value="not_in" />
                      </el-select>

                      <el-select
                        v-model="rule.group_id"
                        placeholder="选择群组"
                        size="default"
                        style="width: 200px"
                        filterable
                        :disabled="readonly || !hasEntityIdentifier"
                      >
                        <el-option
                          v-for="g in groupList"
                          :key="g.group_id"
                          :label="g.group_name"
                          :value="g.group_id"
                        />
                      </el-select>
                    </template>

                    <!-- 事件规则 -->
                    <template v-if="rule.rule_type === 'event'">
                      <el-date-picker
                        v-model="rule.time_range"
                        type="daterange"
                        size="default"
                        style="width: 220px"
                        placeholder="选择时间范围"
                        :shortcuts="dateShortcuts"
                        :disabled="readonly"
                      />

                      <el-select v-model="rule.happen_type" size="default" style="width: 90px" :disabled="readonly">
                        <el-option label="做过" value="done" />
                        <el-option label="没做过" value="not_done" />
                      </el-select>

                      <el-select
                        v-model="rule.event_code"
                        placeholder="选择事件"
                        size="default"
                        style="width: 180px"
                        filterable
                        :disabled="readonly || !hasEntityIdentifier"
                      >
                        <el-option
                          v-for="event in eventList"
                          :key="event.event_code"
                          :label="event.event_name"
                          :value="event.event_code"
                        />
                      </el-select>

                      <el-select v-model="rule.metric" size="default" style="width: 100px" :disabled="readonly">
                        <el-option label="总次数" value="total_count" />
                        <el-option label="总人数" value="total_users" />
                      </el-select>

                      <el-select v-model="rule.operator" size="default" style="width: 70px" :disabled="readonly">
                        <el-option label=">=" value="gte" />
                        <el-option label=">" value="gt" />
                        <el-option label="=" value="eq" />
                        <el-option label="<" value="lt" />
                        <el-option label="<=" value="lte" />
                      </el-select>

                      <el-input-number
                        v-model="rule.value"
                        :min="0"
                        size="default"
                        style="width: 80px"
                        :disabled="readonly"
                      />
                    </template>

                    <!-- 行为序列规则 -->
                    <template v-if="rule.rule_type === 'sequence'">
                      <el-date-picker
                        v-model="rule.time_range"
                        type="daterange"
                        size="default"
                        style="width: 220px"
                        placeholder="选择时间范围"
                        :shortcuts="dateShortcuts"
                        :disabled="readonly"
                      />

                      <span class="sequence-label">依次发生过</span>

                      <div class="sequence-list">
                        <div
                          v-for="(seq, sIndex) in rule.sequence_events"
                          :key="seq.id"
                          class="sequence-item"
                        >
                          <span class="seq-num">{{ sIndex + 1 }}</span>
                          <el-select
                            v-model="seq.event_code"
                            placeholder="选择事件"
                            size="default"
                            style="width: 180px"
                            filterable
                            :disabled="readonly || !hasEntityIdentifier"
                          >
                            <el-option
                              v-for="event in eventList"
                              :key="event.event_code"
                              :label="event.event_name"
                              :value="event.event_code"
                            />
                          </el-select>
                          <el-button
                            v-if="rule.sequence_events && rule.sequence_events.length > 2"
                            link
                            type="danger"
                            :disabled="readonly"
                            @click="removeSequenceEvent(rule, sIndex)"
                          >
                            <el-icon><Close /></el-icon>
                          </el-button>
                        </div>
                        <el-button link type="primary" :disabled="readonly" @click="addSequenceEvent(rule)">
                          <el-icon><Plus /></el-icon>
                          添加事件
                        </el-button>
                      </div>
                    </template>
                  </div>

                  <!-- 规则操作栏 -->
                  <div v-if="!readonly" class="rule-actions">
                    <el-tooltip content="删除" placement="top">
                      <el-button link type="danger" @click="deleteRule(group, ruleIndex)">
                        <el-icon><CircleClose /></el-icon>
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="复制" placement="top">
                      <el-button link @click="copyRule(group, rule)">
                        <el-icon><CopyDocument /></el-icon>
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="添加规则" placement="top">
                      <el-button link @click="addRuleAfter(group, ruleIndex)">
                        <el-icon><Operation /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </div>

            <!-- 添加规则组按钮 -->
            <div v-if="!readonly && groupIndex === ruleGroups.length - 1" class="add-rule-btn-wrapper">
              <el-button link type="primary" size="small" @click="addRuleGroup">
                <el-icon><Plus /></el-icon>
                添加规则组
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加规则类型选择弹窗 -->
    <el-dialog
      v-model="showRuleTypeDialog"
      title="选择规则类型"
      width="400px"
      :show-close="false"
    >
      <div class="rule-type-options">
        <div class="rule-type-option" @click="selectRuleType('tag')">
          <div class="option-icon tag">
            <el-icon><PriceTag /></el-icon>
          </div>
          <div class="option-info">
            <div class="option-title">标签</div>
            <div class="option-desc">根据标签值筛选用户</div>
          </div>
        </div>
        <div class="rule-type-option" @click="selectRuleType('group')">
          <div class="option-icon group">
            <el-icon><User /></el-icon>
          </div>
          <div class="option-info">
            <div class="option-title">群组</div>
            <div class="option-desc">根据群组关系筛选用户</div>
          </div>
        </div>
        <div class="rule-type-option" @click="selectRuleType('event')">
          <div class="option-icon event">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="option-info">
            <div class="option-title">事件</div>
            <div class="option-desc">根据用户行为事件筛选（仅用户实体）</div>
          </div>
        </div>
        <div class="rule-type-option" @click="selectRuleType('sequence')">
          <div class="option-icon sequence">
            <el-icon><Sort /></el-icon>
          </div>
          <div class="option-info">
            <div class="option-title">行为序列</div>
            <div class="option-desc">根据事件序列筛选用户（仅用户实体）</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import {
  Plus, Close, CopyDocument, CircleClose, Operation,
  User, Calendar, PriceTag, Sort
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { Group, Label } from '@/types'
import { groupApi } from '@/api/group'
import { labelApi } from '@/api/label'

const props = defineProps<{
  modelValue: any
  entityIdentifierId?: string
  readonly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: any]
}>()

// 是否已选择分析主体
const hasEntityIdentifier = computed(() => !!props.entityIdentifierId)

// 规则类型定义
interface SequenceEvent {
  id: number
  event_code: string
}

interface GroupRule {
  id: number
  rule_type: 'tag' | 'group' | 'event' | 'sequence'
  // 标签规则
  tag_id?: string
  tag_data_type?: number
  operator?: string
  value?: string | number
  // 群组规则
  group_id?: string
  relation?: string
  // 事件规则
  event_code?: string
  happen_type?: string
  time_range?: [Date, Date]
  metric?: string
  // 行为序列
  sequence_events?: SequenceEvent[]
}

interface RuleGroup {
  id: number
  expanded: boolean
  inner_logic: 'AND' | 'OR'  // 组内规则之间的逻辑
  logic: 'AND' | 'OR'  // 与其他规则组的逻辑
  rules: GroupRule[]
}

// 数据
const ruleGroups = ref<RuleGroup[]>([])
const showRuleTypeDialog = ref(false)
const currentGroup = ref<RuleGroup | null>(null)
const currentRuleIndex = ref<number>(-1)

// 列表数据
const tagList = ref<Label[]>([])
const groupList = ref<Group[]>([])
const eventList = ref<{ event_code: string; event_name: string }[]>([])

// 标签操作符列表（从接口获取）
interface LabelOperator {
  name: string  // 展示名称
  code: string  // 操作码
  types: number[]  // 支持的标签数据类型
}
const labelOperators = ref<LabelOperator[]>([])

// 规则组间逻辑（只有一个，用于所有规则组之间）
const groupsLogic = ref<'AND' | 'OR'>('AND')

// ID生成器
let idCounter = 0
const generateId = () => ++idCounter

// 默认时间范围（昨天）
const getYesterdayRange = (): [Date, Date] => {
  const end = new Date()
  end.setHours(0, 0, 0, 0)
  const start = new Date(end)
  start.setDate(start.getDate() - 1)
  return [start, end]
}

// 日期快捷选项
const dateShortcuts = [
  { text: '昨天', value: () => getYesterdayRange() },
  { text: '过去7天', value: () => getDateRange(7) },
  { text: '过去30天', value: () => getDateRange(30) },
  { text: '过去90天', value: () => getDateRange(90) }
]

const getDateRange = (days: number): [Date, Date] => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - days)
  return [start, end]
}

// 获取标签操作符（根据标签数据类型过滤）
const getTagOperators = (dataType?: number) => {
  if (labelOperators.value.length === 0) {
    return []
  }
  // 未选择标签时显示全部操作符，选择标签后根据数据类型过滤
  if (!dataType) {
    return labelOperators.value.map(op => ({ label: op.name, value: op.code }))
  }
  return labelOperators.value
    .filter(op => op.types.includes(dataType))
    .map(op => ({ label: op.name, value: op.code }))
}

// 标签变化处理
const handleTagChange = (rule: GroupRule, tagId: string) => {
  const tag = tagList.value.find(t => t.label_id === tagId)
  if (tag) {
    rule.tag_data_type = tag.label_data_type as number
    rule.operator = undefined
  }
}

// 获取规则类型标签
const getRuleTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    tag: '标签',
    group: '群组',
    event: '事件',
    sequence: '行为序列'
  }
  return labels[type] || type
}

// 加载标签列表（根据分析主体过滤）
const fetchTagList = async () => {
  if (!props.entityIdentifierId) {
    tagList.value = []
    return
  }
  try {
    const res = await labelApi.getOnline(props.entityIdentifierId)
    tagList.value = res.data.data || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

// 加载标签操作符配置
const fetchLabelOperators = async () => {
  try {
    const res = await groupApi.getLabelConfig()
    labelOperators.value = res.data.data || []
  } catch (error) {
    console.error('获取标签操作符配置失败:', error)
  }
}

// 加载群组列表（根据分析主体过滤）
const fetchGroupList = async () => {
  if (!props.entityIdentifierId) {
    groupList.value = []
    return
  }
  try {
    const res = await groupApi.getList({ entity_identifier_id: props.entityIdentifierId })
    groupList.value = res.data.data || []
  } catch (error) {
    console.error('获取群组列表失败:', error)
  }
}

// 加载事件列表
const fetchEventList = async () => {
  eventList.value = [
    { event_code: 'app_launch', event_name: 'App启动' },
    { event_code: 'page_view', event_name: '页面浏览' },
    { event_code: 'product_click', event_name: '商品点击' },
    { event_code: 'add_cart', event_name: '加入购物车' },
    { event_code: 'order_submit', event_name: '提交订单' },
    { event_code: 'payment', event_name: '支付成功' },
    { event_code: 'login', event_name: '登录' },
    { event_code: 'register', event_name: '注册' }
  ]
}

// 切换规则组展开/折叠
const toggleGroup = (group: RuleGroup) => {
  group.expanded = !group.expanded
}

// 切换所有规则组间的逻辑（只有一个）
const toggleGroupsLogic = () => {
  groupsLogic.value = groupsLogic.value === 'AND' ? 'OR' : 'AND'
}

// 切换规则组内逻辑
const toggleInnerLogic = (group: RuleGroup) => {
  group.inner_logic = group.inner_logic === 'AND' ? 'OR' : 'AND'
}

// 添加规则组
const addRuleGroup = () => {
  const newGroup: RuleGroup = {
    id: generateId(),
    expanded: true,
    inner_logic: 'AND',
    logic: 'AND',
    rules: []
  }
  ruleGroups.value.push(newGroup)
  addRuleToGroup(newGroup, 'tag')
}

// 删除规则组
const deleteRuleGroup = (index: number) => {
  ruleGroups.value.splice(index, 1)
  updateModelValue()
}

// 添加规则到组
const addRuleToGroup = (group: RuleGroup, type?: string) => {
  currentGroup.value = group
  currentRuleIndex.value = -1
  if (type) {
    createRule(type)
  } else {
    showRuleTypeDialog.value = true
  }
}

// 在规则后添加
const addRuleAfter = (group: RuleGroup, index: number) => {
  currentGroup.value = group
  currentRuleIndex.value = index
  showRuleTypeDialog.value = true
}

// 选择规则类型
const selectRuleType = (type: string) => {
  createRule(type)
  showRuleTypeDialog.value = false
}

// 创建规则
const createRule = (type: string) => {
  if (!currentGroup.value) return

  const newRule: GroupRule = {
    id: generateId(),
    rule_type: type as 'tag' | 'group' | 'event' | 'sequence'
  }

  switch (type) {
    case 'tag':
      newRule.tag_id = ''
      newRule.operator = 'eq'
      newRule.value = ''
      break
    case 'group':
      newRule.relation = 'in'
      newRule.group_id = ''
      break
    case 'event':
      newRule.time_range = getYesterdayRange()
      newRule.happen_type = 'done'
      newRule.event_code = ''
      newRule.metric = 'total_count'
      newRule.operator = 'gte'
      newRule.value = 1
      break
    case 'sequence':
      newRule.time_range = getYesterdayRange()
      newRule.sequence_events = [
        { id: generateId(), event_code: '' },
        { id: generateId(), event_code: '' }
      ]
      break
  }

  if (currentRuleIndex.value >= 0) {
    currentGroup.value.rules.splice(currentRuleIndex.value + 1, 0, newRule)
  } else {
    currentGroup.value.rules.push(newRule)
  }

  updateModelValue()
}

// 复制规则
const copyRule = (group: RuleGroup, rule: GroupRule) => {
  const index = group.rules.indexOf(rule)
  const copiedRule = { ...rule, id: generateId() }
  if (copiedRule.sequence_events) {
    copiedRule.sequence_events = copiedRule.sequence_events.map(se => ({
      ...se,
      id: generateId()
    }))
  }
  group.rules.splice(index + 1, 0, copiedRule)
  updateModelValue()
}

// 删除规则
const deleteRule = (group: RuleGroup, index: number) => {
  group.rules.splice(index, 1)
  updateModelValue()
}

// 添加序列事件
const addSequenceEvent = (rule: GroupRule) => {
  if (rule.sequence_events) {
    rule.sequence_events.push({
      id: generateId(),
      event_code: ''
    })
  }
  updateModelValue()
}

// 删除序列事件
const removeSequenceEvent = (rule: GroupRule, index: number) => {
  if (rule.sequence_events) {
    rule.sequence_events.splice(index, 1)
  }
  updateModelValue()
}

// 更新模型值
const updateModelValue = () => {
  emit('update:modelValue', { rule_groups: ruleGroups.value })
}

// 验证
const validate = () => {
  if (ruleGroups.value.length === 0) {
    ElMessage.warning('请至少添加一个规则组')
    return false
  }
  return true
}

/**
 * 将前端规则转换为后端 Rule 格式（新 DSL）
 * 结构: RuleExpression -> RuleGroup -> Rule -> RuleFilterExpression/RuleEvent/RuleSequence
 * 
 * 规则类型:
 * - tag (1): 标签规则，使用 filter_expression
 * - group (2): 群组规则，使用 filter_expression
 * - event (3): 事件规则，使用 filter_expression + event
 * - sequence (4): 行为序列规则，使用 events
 */
const convertToBackendRule = (rule: GroupRule) => {
  const baseRule: any = {
    type: mapRuleTypeToBackend(rule.rule_type)
  }

  switch (rule.rule_type) {
    case 'tag':
      return {
        ...baseRule,
        filter_expression: {
          logic: 'AND',
          filter_groups: [{
            logic: 'AND',
            filters: [{
              type: 1,  // 1-标签
              id: rule.tag_id || '',
              name: getTagName(rule.tag_id),
              op: mapOperatorToBackend(rule.operator),
              values: rule.value !== undefined && rule.value !== '' ? [String(rule.value)] : []
            }]
          }]
        }
      }

    case 'group':
      return {
        ...baseRule,
        filter_expression: {
          logic: 'AND',
          filter_groups: [{
            logic: 'AND',
            filters: [{
              type: 2,  // 2-群组
              id: rule.group_id || '',
              name: getGroupName(rule.group_id),
              op: rule.relation === 'in' ? '=' : '!=',
              values: ['是']
            }]
          }]
        }
      }

    case 'event':
      const timePeriod = convertTimePeriod(rule.time_range)
      return {
        ...baseRule,
        filter_expression: {
          logic: 'AND',
          filter_groups: []
        },
        event: {
          event: {
            eventId: rule.event_code || '',
            eventName: getEventName(rule.event_code)
          },
          measure: {
            name: rule.metric === 'total_count' ? '总次数' : '总人数',
            type: rule.metric === 'total_count' ? 'count' : 'users',
            op: mapOperatorToBackend(rule.operator),
            values: rule.value !== undefined ? [String(rule.value)] : []
          },
          period: timePeriod ? {
            type: 2,  // 绝对时间
            beginTimestamp: timePeriod.timestamp[0],
            endTimestamp: timePeriod.timestamp[1],
            unit: 'day',
            amount: 0
          } : undefined
        }
      }

    case 'sequence':
      const seqTimePeriod = convertTimePeriod(rule.time_range)
      return {
        ...baseRule,
        events: (rule.sequence_events || []).map(seq => ({
          event: {
            event: {
              eventId: seq.event_code,
              eventName: getEventName(seq.event_code)
            }
          },
          filter_expression: {
            logic: 'AND',
            filter_groups: []
          }
        }))
      }

    default:
      return baseRule
  }
}

// 映射规则类型到后端
const mapRuleTypeToBackend = (type: string): string => {
  const map: Record<string, string> = {
    'tag': '1',
    'group': '2',
    'event': '3',
    'sequence': '4'
  }
  return map[type] || '1'
}

// 映射操作符到后端
const mapOperatorToBackend = (op?: string): string => {
  // 直接透传后端操作符名称，不做符号转换
  return op || 'eq'
}

// 获取标签名称
const getTagName = (tagId?: string): string => {
  if (!tagId) return ''
  const tag = tagList.value.find(t => t.label_id === tagId)
  return tag?.label_name || ''
}

// 获取群组名称
const getGroupName = (groupId?: string): string => {
  if (!groupId) return ''
  const group = groupList.value.find(g => g.group_id === groupId)
  return group?.group_name || ''
}

// 获取事件名称
const getEventName = (eventCode?: string): string => {
  if (!eventCode) return ''
  const event = eventList.value.find(e => e.event_code === eventCode)
  return event?.event_name || ''
}

/**
 * 转换时间范围为后端 TimePeriod 格式
 */
const convertTimePeriod = (timeRange?: [Date, Date]) => {
  if (!timeRange || timeRange.length !== 2) {
    return undefined
  }
  return {
    type: 'absolute_time',
    period: 'timestamp',
    timestamp: [timeRange[0].getTime(), timeRange[1].getTime()]
  }
}

/**
 * 获取后端 GroupRule 格式的数据
 * GroupRule 包含 RuleExpression 字段
 * 结构: GroupRule -> RuleExpression -> RuleGroup -> Rule
 */
const getGroupRule = () => {
  return {
    type: 'rule',
    expression: {
      logic: groupsLogic.value,
      rule_groups: ruleGroups.value.map(group => ({
        logic: group.inner_logic,
        rules: group.rules.map(rule => convertToBackendRule(rule))
      }))
    }
  }
}

// 监听变化
watch(ruleGroups, updateModelValue, { deep: true })

// 监听分析主体变化，重新加载标签和群组列表
watch(() => props.entityIdentifierId, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    // 清空已选择的标签和群组
    ruleGroups.value.forEach(group => {
      group.rules.forEach(rule => {
        if (rule.rule_type === 'tag') {
          rule.tag_id = ''
          rule.operator = undefined
          rule.value = ''
        } else if (rule.rule_type === 'group') {
          rule.group_id = ''
        }
      })
    })
    fetchTagList()
    fetchGroupList()
  }
})

// 初始化
onMounted(() => {
  fetchLabelOperators()
  fetchEventList()
  if (props.entityIdentifierId) {
    fetchTagList()
    fetchGroupList()
  }
  // 如果有传入的初始值（编辑模式），则解析
  if (props.modelValue?.expression?.rule_groups && props.modelValue.expression.rule_groups.length > 0) {
    parseGroupRule(props.modelValue)
  } else if (ruleGroups.value.length === 0) {
    addRuleGroup()
  }
})

/**
 * 从后端 GroupRule 格式解析为前端 RuleGroup 格式
 * GroupRule 包含 RuleExpression 字段
 */
const parseGroupRule = (data: any) => {
  // 获取 RuleExpression 数据
  const expression = data?.expression
  if (!expression?.rule_groups || !Array.isArray(expression.rule_groups)) {
    return
  }
  
  // 清空现有数据
  ruleGroups.value = []
  groupsLogic.value = expression.logic || 'AND'
  
  // 遍历规则组
  expression.rule_groups.forEach((group: any, index: number) => {
    const ruleGroup: RuleGroup = {
      id: generateId(),
      expanded: true,
      inner_logic: group.logic || 'AND',
      logic: index === 0 ? 'AND' : (expression.logic || 'OR'),
      rules: []
    }
    
    // 解析规则
    if (group.rules && Array.isArray(group.rules)) {
      group.rules.forEach((rule: any) => {
        const parsedRule = parseBackendRule(rule)
        if (parsedRule) {
          ruleGroup.rules.push(parsedRule)
        }
      })
    }
    
    ruleGroups.value.push(ruleGroup)
  })
  
  // 更新模型值
  updateModelValue()
}

/**
 * 解析单个后端 Rule 为前端 GroupRule 格式
 */
const parseBackendRule = (rule: any): GroupRule | null => {
  if (!rule?.type) return null
  
  const ruleType = mapBackendTypeToFrontend(rule.type)
  
  const baseRule: GroupRule = {
    id: generateId(),
    rule_type: ruleType
  }
  
  switch (ruleType) {
    case 'tag':
      const tagFilter = rule.filter_expression?.filter_groups?.[0]?.filters?.[0]
      return {
        ...baseRule,
        tag_id: tagFilter?.id || '',
        tag_data_type: undefined,
        operator: mapBackendOperatorToFrontend(tagFilter?.op) || 'eq',
        value: tagFilter?.values?.[0] || ''
      }
    case 'group':
      const groupFilter = rule.filter_expression?.filter_groups?.[0]?.filters?.[0]
      return {
        ...baseRule,
        group_id: groupFilter?.id || '',
        relation: groupFilter?.op === '=' ? 'in' : 'not_in'
      }
    case 'event':
      const eventPeriod = rule.event?.period
      return {
        ...baseRule,
        event_code: rule.event?.event?.eventId || '',
        happen_type: 'done',
        time_range: eventPeriod ? [
          new Date(eventPeriod.beginTimestamp),
          new Date(eventPeriod.endTimestamp)
        ] : getYesterdayRange(),
        metric: rule.event?.measure?.type === 'count' ? 'total_count' : 'total_users',
        operator: mapBackendOperatorToFrontend(rule.event?.measure?.op) || 'gte',
        value: parseInt(rule.event?.measure?.values?.[0] || '1')
      }
    case 'sequence':
      const seqPeriod = rule.events?.[0]?.event?.period
      return {
        ...baseRule,
        time_range: seqPeriod ? [
          new Date(seqPeriod.beginTimestamp),
          new Date(seqPeriod.endTimestamp)
        ] : getYesterdayRange(),
        sequence_events: (rule.events || []).map((e: any) => ({
          id: generateId(),
          event_code: e.event?.event?.eventId || ''
        }))
      }
    default:
      return null
  }
}

// 映射后端规则类型到前端
const mapBackendTypeToFrontend = (type: string): 'tag' | 'group' | 'event' | 'sequence' => {
  const map: Record<string, 'tag' | 'group' | 'event' | 'sequence'> = {
    '1': 'tag',
    '2': 'group',
    '3': 'event',
    '4': 'sequence'
  }
  return map[type] || 'tag'
}

// 映射后端操作符到前端
const mapBackendOperatorToFrontend = (op?: string): string => {
  const map: Record<string, string> = {
    '=': 'eq',
    '!=': 'ne',
    '>': 'gt',
    '>=': 'gte',
    '<': 'lt',
    '<=': 'lte',
    'contains': 'contains',
    'not_contains': 'not_contains'
  }
  return map[op || ''] || 'eq'
}

/**
 * 解析时间范围
 */
const parseTimeRange = (timeRange?: any): [Date, Date] | undefined => {
  if (!timeRange || !Array.isArray(timeRange) || timeRange.length !== 2) {
    return undefined
  }
  try {
    const start = new Date(timeRange[0])
    const end = new Date(timeRange[1])
    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      return getYesterdayRange()
    }
    return [start, end]
  } catch {
    return getYesterdayRange()
  }
}

defineExpose({
  validate,
  getGroupRule,
  parseGroupRule // 导出解析方法供外部调用
})
</script>

<style scoped lang="scss">
.group-rule-config {
  // 确保垂直排列，防止被父级flex影响
  display: flex;
  flex-direction: column;
  width: 100%;

  // 规则组容器（带左侧垂直线）
  .rule-groups-container {
    display: flex;
    position: relative;
    width: 100%;

    // 最左侧垂直线（只在有多个规则组时显示）
    &.has-logic::before {
      content: '';
      position: absolute;
      left: 20px;
      top: 0;
      bottom: 0;
      width: 2px;
      background-color: #dcdfe6;
    }

    // 规则组间逻辑选择器（最左侧）
    .groups-logic-wrapper {
      width: 40px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      z-index: 1;

      .logic-toggle-btn {
        width: 32px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #409eff;
        color: #fff;
        font-size: 12px;
        font-weight: 500;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          opacity: 0.9;
        }

        &.is-or {
          background-color: #67c23a;
        }

        &.is-disabled {
          cursor: not-allowed;
          opacity: 0.7;
        }
      }
    }

    .rule-group-list {
      flex: 1;
      min-width: 0;

      .rule-group-wrapper {
        position: relative;
        margin-bottom: 16px;

        &:last-child {
          margin-bottom: 0;
        }

        // 规则组间逻辑关系行（已移除，现在在最左侧统一显示）
        .group-logic-row {
          display: none;
        }

        .rule-group {
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }

        .rule-group-content {
          .rules-wrapper {
            display: flex;

            // 左侧垂直线条（包含组内逻辑选择器）
            .group-left-line {
              width: 40px;
              flex-shrink: 0;
              position: relative;

              &::before {
                content: '';
                position: absolute;
                left: 50%;
                top: 0;
                bottom: 0;
                width: 2px;
                background-color: #dcdfe6;
                transform: translateX(-50%);
              }

              .inner-logic-wrapper {
                position: absolute;
                left: 50%;
                top: 50%;
                transform: translate(-50%, -50%);
                z-index: 1;

                .inner-logic-select {
                  width: 50px;
                  background-color: #fff;

                  :deep(.el-input__wrapper) {
                    padding: 0 4px;
                    box-shadow: 0 0 0 1px #dcdfe6 inset;
                  }
                }

                // 点击切换按钮样式
                .logic-toggle-btn {
                  width: 32px;
                  height: 24px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  background-color: #409eff;
                  color: #fff;
                  font-size: 12px;
                  font-weight: 500;
                  border-radius: 4px;
                  cursor: pointer;
                  transition: all 0.3s;

                  &:hover {
                    opacity: 0.9;
                  }

                  &.is-or {
                    background-color: #67c23a;
                  }

                  &.is-disabled {
                    cursor: not-allowed;
                    opacity: 0.7;
                  }
                }
              }
            }

            .rule-list {
              flex: 1;

              .rule-item {
                display: flex;
                align-items: flex-start;
                margin-bottom: 12px;

                &:last-child {
                  margin-bottom: 0;
                }

                &:hover {
                  .rule-actions {
                    opacity: 1;
                    visibility: visible;
                  }
                }

                .rule-type-tag {
                  flex-shrink: 0;
                  padding: 6px 12px;
                  border-radius: 4px;
                  font-size: 13px;
                  color: #fff;
                  margin-right: 12px;

                  &.tag {
                    background-color: #409eff;
                  }

                  &.group {
                    background-color: #67c23a;
                  }

                  &.event {
                    background-color: #e6a23c;
                  }

                  &.sequence {
                    background-color: #f56c6c;
                  }
                }

                .rule-content {
                  display: flex;
                  flex-wrap: wrap;
                  align-items: center;
                  gap: 8px;

                  .sequence-label {
                    color: #606266;
                    font-size: 14px;
                  }

                  .sequence-list {
                    width: 100%;
                    margin-top: 8px;

                    .sequence-item {
                      display: flex;
                      align-items: center;
                      gap: 8px;
                      margin-bottom: 8px;

                      .seq-num {
                        width: 24px;
                        height: 24px;
                        border-radius: 50%;
                        background-color: #409eff;
                        color: #fff;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 12px;
                      }
                    }
                  }
                }

                .rule-actions {
                  flex-shrink: 0;
                  display: flex;
                  align-items: center;
                  gap: 4px;
                  margin-left: 8px;
                  opacity: 0;
                  visibility: hidden;
                  transition: opacity 0.2s, visibility 0.2s;

                  .el-button {
                    padding: 4px;
                    
                    .el-icon {
                      font-size: 16px;
                    }
                  }
                }
              }
            }
          }

          .add-rule-btn-wrapper {
            margin-top: 8px;
            padding-left: 40px;

            .el-button {
              font-size: 13px;
              color: #909399;

              &:hover {
                color: #409eff;
              }
            }
          }
        }
      }
    }
  }
}

// 规则类型选项弹窗样式
.rule-type-options {
  .rule-type-option {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      background-color: #f5f7fa;
    }

    .option-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      .el-icon {
        font-size: 24px;
        color: #fff;
      }

      &.tag {
        background-color: #409eff;
      }

      &.group {
        background-color: #67c23a;
      }

      &.event {
        background-color: #e6a23c;
      }

      &.sequence {
        background-color: #f56c6c;
      }
    }

    .option-info {
      .option-title {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 4px;
      }

      .option-desc {
        font-size: 13px;
        color: #909399;
      }
    }
  }
}
}
</style>
