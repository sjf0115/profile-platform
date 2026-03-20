<template>
  <div class="create-custom-label-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">{{ isEditMode ? '编辑标签 - 自定义标签' : '创建标签 - 自定义标签' }}</h2>
      </div>
    </div>

    <div class="page-content">
      <!-- 标签信息 -->
      <div class="section">
        <div class="section-title">标签信息</div>
        <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px" class="label-form">
          <el-form-item label="标签名称" prop="label_name">
            <div class="label-name-input">
              <el-input 
                v-model="formData.label_name" 
                placeholder="请输入"
                maxlength="128"
                show-word-limit
                clearable
              />
              <span class="field-name">标签字段 user_tag_</span>
            </div>
          </el-form-item>

          <el-form-item label="更新方式" prop="update_type">
            <el-radio-group v-model="formData.update_type">
              <el-radio :label="1">手动更新</el-radio>
              <el-radio :label="2">周期更新</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="标签类目" prop="category_id">
            <div class="category-select-group">
              <!-- 一级类目 -->
              <el-select 
                v-model="selectedLevel1" 
                placeholder="请选择一级类目"
                clearable
                style="width: 140px"
                @change="handleLevel1Change"
              >
                <el-option 
                  v-for="item in level1Categories" 
                  :key="item.category_id" 
                  :label="item.category_name" 
                  :value="item.category_id" 
                />
              </el-select>
              <!-- 二级类目 -->
              <el-select 
                v-model="selectedLevel2" 
                placeholder="请选择二级类目"
                clearable
                style="width: 140px"
                :disabled="!selectedLevel1"
                @change="handleLevel2Change"
              >
                <el-option 
                  v-for="item in level2Categories" 
                  :key="item.category_id" 
                  :label="item.category_name" 
                  :value="item.category_id" 
                />
              </el-select>
              <!-- 三级类目 -->
              <el-select 
                v-model="selectedLevel3" 
                placeholder="请选择三级类目"
                clearable
                style="width: 140px"
                :disabled="!selectedLevel2"
              >
                <el-option 
                  v-for="item in level3Categories" 
                  :key="item.category_id" 
                  :label="item.category_name" 
                  :value="item.category_id" 
                />
              </el-select>
            </div>
          </el-form-item>

          <el-form-item label="标签说明" prop="label_desc">
            <el-input 
              v-model="formData.label_desc" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入标签说明"
              maxlength="256"
              show-word-limit
              clearable
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 目标结果运算 -->
      <div class="section">
        <div class="section-title">目标结果运算</div>
        <div class="expression-builder">
          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <el-button size="small" @click="insertOperator('+')">+</el-button>
              <el-button size="small" @click="insertOperator('-')">-</el-button>
              <el-button size="small" @click="insertOperator('*')">×</el-button>
              <el-button size="small" @click="insertOperator('/')">÷</el-button>
              <el-button size="small" @click="insertOperator('(')">(</el-button>
              <el-button size="small" @click="insertOperator(')')">)</el-button>
              <el-button size="small" type="primary" plain @click="showNumberInput = true">数字</el-button>
              <el-button size="small" type="success" plain @click="showFunctionDialog = true">函数</el-button>
            </div>
            <div class="toolbar-right">
              <el-button size="small" link @click="handleUndo">
                <el-icon><RefreshLeft /></el-icon>撤销
              </el-button>
              <el-button size="small" link @click="handleRedo">
                <el-icon><RefreshRight /></el-icon>重做
              </el-button>
              <el-button size="small" link @click="handleClear">
                <el-icon><Delete /></el-icon>清空
              </el-button>
              <el-button size="small" type="success" plain @click="handleTestRun">
                <el-icon><VideoPlay /></el-icon>试运行
              </el-button>
            </div>
          </div>

          <!-- 添加字段按钮 -->
          <div class="add-field-row">
            <el-button type="primary" plain size="small" @click="showFieldDialog = true">
              <el-icon><Plus /></el-icon>增加字段
            </el-button>
          </div>

          <!-- 表达式展示区域 -->
          <div class="expression-area">
            <div class="expression-tags">
              <template v-for="(item, index) in expressionItems" :key="index">
                <el-tag 
                  v-if="item.type === 'field'"
                  closable
                  @close="removeExpressionItem(index)"
                  class="expression-tag field-tag"
                >
                  {{ item.label }}
                </el-tag>
                <el-tag 
                  v-else-if="item.type === 'number'"
                  closable
                  @close="removeExpressionItem(index)"
                  class="expression-tag number-tag"
                >
                  {{ item.value }}
                </el-tag>
                <span v-else class="operator-tag">{{ item.value }}</span>
              </template>
              <span v-if="expressionItems.length === 0" class="placeholder">点击按钮添加运算元素</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="form-actions">
        <el-button type="primary" @click="handleSubmit">保存</el-button>
        <el-button @click="goBack">取消</el-button>
      </div>
    </div>

    <!-- 数字输入对话框 -->
    <el-dialog v-model="showNumberInput" title="输入数字" width="300px">
      <el-input-number v-model="inputNumber" :precision="2" style="width: 100%" />
      <template #footer>
        <el-button @click="showNumberInput = false">取消</el-button>
        <el-button type="primary" @click="confirmNumber">确定</el-button>
      </template>
    </el-dialog>

    <!-- 函数选择对话框 -->
    <el-dialog v-model="showFunctionDialog" title="选择函数" width="500px">
      <div class="function-list">
        <div 
          v-for="func in functionList" 
          :key="func.name"
          class="function-item"
          @click="selectFunction(func)"
        >
          <div class="function-name">{{ func.name }}</div>
          <div class="function-desc">{{ func.description }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 字段选择对话框 -->
    <el-dialog v-model="showFieldDialog" title="选择字段" width="600px">
      <div class="field-list">
        <div 
          v-for="field in availableFields" 
          :key="field.name"
          class="field-item"
          @click="selectField(field)"
        >
          <div class="field-name">{{ field.name }}</div>
          <div class="field-type">{{ field.type }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, RefreshLeft, RefreshRight, Delete, VideoPlay } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { labelApi } from '@/api/label'
import { labelCategoryApi, type LabelCategory } from '@/api/labelCategory'

const route = useRoute()
const router = useRouter()

// 判断是否为编辑模式
const isEditMode = computed(() => !!route.params.id)
const labelId = computed(() => route.params.id as string)

const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  label_name: '',
  label_desc: '',
  category_id: '',
  update_type: 1,  // 1-手动更新, 2-周期更新
  expression: '',
  // 编辑模式需要的字段
  is_office: 0,
  owner: '',
  creator: ''
})

// 表单验证规则
const formRules = {
  label_name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  category_id: [{ required: true, message: '请选择标签类目', trigger: 'change' }],
  update_type: [{ required: true, message: '请选择更新方式', trigger: 'change' }]
}

// 类目树
const categoryTree = ref<LabelCategory[]>([])

// 层级类目选择
const selectedLevel1 = ref('')
const selectedLevel2 = ref('')
const selectedLevel3 = ref('')

// 计算各级类目
const level1Categories = computed(() => categoryTree.value)
const level2Categories = computed(() => {
  const level1 = categoryTree.value.find(item => item.category_id === selectedLevel1.value)
  return level1?.children || []
})
const level3Categories = computed(() => {
  const level1 = categoryTree.value.find(item => item.category_id === selectedLevel1.value)
  const level2 = level1?.children?.find(item => item.category_id === selectedLevel2.value)
  return level2?.children || []
})

// 一级类目变化
const handleLevel1Change = () => {
  selectedLevel2.value = ''
  selectedLevel3.value = ''
}

// 二级类目变化
const handleLevel2Change = () => {
  selectedLevel3.value = ''
}

// 监听层级选择变化，设置最终的 category_id
import { watch } from 'vue'
watch([selectedLevel1, selectedLevel2, selectedLevel3], ([level1, level2, level3]) => {
  if (level3) {
    formData.category_id = level3
  } else if (level2) {
    formData.category_id = level2
  } else if (level1) {
    formData.category_id = level1
  } else {
    formData.category_id = ''
  }
})

// 表达式相关
const expressionItems = ref<Array<{type: string, value: string, label?: string}>>([])
const showNumberInput = ref(false)
const showFunctionDialog = ref(false)
const showFieldDialog = ref(false)
const inputNumber = ref(0)

// 函数列表
const functionList = [
  { name: 'ABS', description: '绝对值' },
  { name: 'ROUND', description: '四舍五入' },
  { name: 'CEIL', description: '向上取整' },
  { name: 'FLOOR', description: '向下取整' },
  { name: 'MAX', description: '最大值' },
  { name: 'MIN', description: '最小值' },
  { name: 'SUM', description: '求和' },
  { name: 'AVG', description: '平均值' }
]

// 可用字段列表
const availableFields = ref([
  { name: 'age', type: '数值型' },
  { name: 'gender', type: '文本型' },
  { name: 'order_amount', type: '数值型' },
  { name: 'order_count', type: '数值型' },
  { name: 'last_login_time', type: '时间型' }
])

// 插入运算符
const insertOperator = (operator: string) => {
  expressionItems.value.push({ type: 'operator', value: operator })
}

// 确认数字
const confirmNumber = () => {
  expressionItems.value.push({ type: 'number', value: inputNumber.value.toString() })
  showNumberInput.value = false
  inputNumber.value = 0
}

// 选择函数
const selectFunction = (func: any) => {
  expressionItems.value.push({ type: 'function', value: func.name + '()' })
  showFunctionDialog.value = false
}

// 选择字段
const selectField = (field: any) => {
  expressionItems.value.push({ type: 'field', value: field.name, label: field.name })
  showFieldDialog.value = false
}

// 移除表达式项
const removeExpressionItem = (index: number) => {
  expressionItems.value.splice(index, 1)
}

// 撤销
const handleUndo = () => {
  // TODO: 实现撤销功能
  ElMessage.info('撤销功能开发中')
}

// 重做
const handleRedo = () => {
  // TODO: 实现重做功能
  ElMessage.info('重做功能开发中')
}

// 清空
const handleClear = () => {
  expressionItems.value = []
}

// 试运行
const handleTestRun = () => {
  // TODO: 实现试运行功能
  ElMessage.info('试运行功能开发中')
}

// 获取类目树
const fetchCategoryTree = async () => {
  try {
    const res = await labelCategoryApi.getList()
    categoryTree.value = res.data.data || []
  } catch (error) {
    console.error('获取类目列表失败:', error)
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 构建表达式字符串
        const expression = expressionItems.value.map(item => item.value).join(' ')
        const submitData: any = {
          label_name: formData.label_name,
          label_desc: formData.label_desc,
          label_category_id: formData.category_id,
          update_type: formData.update_type,
          config: expression,  // 自定义标签直接传递表达式字符串
          label_type: 2,  // 自定义标签
          label_status: 1,  // 在线
          source_type: 6  // 自定义规则
        }
        
        // 编辑模式添加 label_id 和其他字段
        if (isEditMode.value) {
          submitData.label_id = labelId.value
          submitData.is_office = formData.is_office
          submitData.owner = formData.owner
          submitData.creator = formData.creator
        }
        
        console.log('提交数据:', submitData)
        await labelApi.save(submitData)
        ElMessage.success(isEditMode.value ? '编辑成功' : '创建成功')
        router.push('/label-market')
      } catch (error) {
        console.error('保存失败:', error)
      }
    }
  })
}

// 加载标签详情（编辑模式）
const fetchLabelDetail = async () => {
  if (!isEditMode.value) return
  
  try {
    const res = await labelApi.getDetail(labelId.value)
    const label = res.data.data
    console.log('标签详情:', label)
    
    if (label) {
      // 填充表单数据
      formData.label_name = label.label_name || ''
      formData.label_desc = label.label_desc || ''
      formData.category_id = label.label_category_id || ''
      formData.update_type = label.update_type || 1
      formData.expression = label.config || ''
      // 编辑模式需要的字段
      formData.is_office = label.is_office || 0
      formData.owner = label.owner || ''
      formData.creator = label.creator || ''
      
      // 解析表达式
      if (formData.expression) {
        const items = formData.expression.split(' ').filter(v => v)
        expressionItems.value = items.map((value, index) => ({
          id: index,
          value,
          type: getExpressionItemType(value)
        }))
      }
      
      // 设置类目层级选择
      if (formData.category_id) {
        await setCategoryLevels(formData.category_id)
      }
    }
  } catch (error) {
    console.error('获取标签详情失败:', error)
    ElMessage.error('获取标签详情失败')
  }
}

// 获取表达式项类型
const getExpressionItemType = (value: string): string => {
  if (['+', '-', '*', '/', '(', ')'].includes(value)) {
    return 'operator'
  } else if (/^\d+(\.\d+)?$/.test(value)) {
    return 'number'
  } else {
    return 'field'
  }
}

// 根据类目ID设置层级选择
const setCategoryLevels = async (categoryId: string) => {
  // 递归查找类目及其所有父级
  const findCategoryPath = (categories: LabelCategory[], id: string, path: LabelCategory[] = []): LabelCategory[] | null => {
    for (const cat of categories) {
      if (cat.category_id === id) {
        return [...path, cat]
      }
      if (cat.children && cat.children.length > 0) {
        const found = findCategoryPath(cat.children, id, [...path, cat])
        if (found) return found
      }
    }
    return null
  }
  
  // 查找类目路径（从根到目标类目）
  const path = findCategoryPath(level1Categories.value, categoryId)
  console.log('类目路径:', path)
  
  if (path && path.length > 0) {
    if (path.length === 1) {
      // 一级类目
      selectedLevel1.value = path[0].category_id
    } else if (path.length === 2) {
      // 二级类目
      selectedLevel1.value = path[0].category_id
      selectedLevel2.value = path[1].category_id
    } else if (path.length === 3) {
      // 三级类目
      selectedLevel1.value = path[0].category_id
      selectedLevel2.value = path[1].category_id
      selectedLevel3.value = path[2].category_id
    }
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(async () => {
  await fetchCategoryTree()  // 等待类目加载完成
  fetchLabelDetail()  // 类目加载完成后再加载标签详情
})
</script>

<style scoped lang="scss">
.create-custom-label-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 500;
    }
  }
}

.page-content {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  flex: 1;
  overflow: auto;
}

.section {
  margin-bottom: 30px;
  
  .section-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #ebeef5;
  }
}

.label-form {
  max-width: 600px;
  
  .label-name-input {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .el-input {
      flex: 1;
    }
    
    .field-name {
      color: #909399;
      font-size: 14px;
      white-space: nowrap;
    }
  }
  
  .category-select-group {
    display: flex;
    gap: 10px;
    
    .el-select {
      flex: 1;
    }
  }
}

.expression-builder {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e4e7ed;
    
    .toolbar-left {
      display: flex;
      gap: 8px;
    }
    
    .toolbar-right {
      display: flex;
      gap: 8px;
    }
  }
  
  .add-field-row {
    margin-bottom: 16px;
  }
  
  .expression-area {
    background-color: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    padding: 16px;
    min-height: 100px;
    
    .expression-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      align-items: center;
      
      .expression-tag {
        cursor: pointer;
      }
      
      .field-tag {
        background-color: #ecf5ff;
        color: #409eff;
      }
      
      .number-tag {
        background-color: #f0f9ff;
        color: #606266;
      }
      
      .operator-tag {
        font-size: 18px;
        font-weight: bold;
        color: #606266;
        padding: 0 4px;
      }
      
      .placeholder {
        color: #c0c4cc;
        font-size: 14px;
      }
    }
  }
}

.form-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.function-list {
  .function-item {
    padding: 12px;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    .function-name {
      font-weight: 500;
      color: #409eff;
    }
    
    .function-desc {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.field-list {
  .field-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    .field-type {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
