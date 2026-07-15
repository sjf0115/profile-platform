<template>
  <div class="create-label-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">{{ isEditMode ? '编辑标签 - 数据源导入' : '创建标签 - 数据源导入' }}</h2>
      </div>
    </div>

    <div class="page-content">
      <el-steps :active="activeStep" finish-status="success" class="steps">
        <el-step title="标签信息" />
        <el-step title="标签来源" />
      </el-steps>

      <!-- 第一步：标签信息 -->
      <div v-if="activeStep === 0" class="step-content">
        <el-form :model="formData" :rules="step1Rules" ref="step1FormRef" label-width="120px" class="label-form">
          <el-form-item label="标签名称" prop="label_name">
            <el-input v-model="formData.label_name" placeholder="请输入标签名称" clearable />
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

          <el-form-item label="标签描述" prop="label_desc">
            <el-input 
              v-model="formData.label_desc" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入标签描述"
              clearable
            />
          </el-form-item>

          <el-divider />

          <el-form-item label="标签类型" prop="label_type">
            <el-select v-model="formData.label_type" placeholder="请选择标签类型" style="width: 100%">
              <el-option 
                v-for="item in config.label_type" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="数据类型" prop="data_type">
            <el-select v-model="formData.data_type" placeholder="请选择数据类型" style="width: 100%">
              <el-option 
                v-for="item in config.data_type" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="数据分布类型" prop="dist_type">
            <el-select v-model="formData.dist_type" placeholder="请选择数据分布类型" style="width: 100%">
              <el-option 
                v-for="item in config.dist_type" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="加工类型" prop="produce_type">
            <el-select v-model="formData.produce_type" placeholder="请选择加工类型" style="width: 100%">
              <el-option 
                v-for="item in config.produce_type" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="时效性类型" prop="time_type">
            <el-select v-model="formData.time_type" placeholder="请选择时效性类型" style="width: 100%">
              <el-option 
                v-for="item in config.time_type" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="组织类型" prop="organize_type">
            <el-radio-group v-model="formData.organize_type">
              <el-radio 
                v-for="item in config.organize_type" 
                :key="item.id" 
                :label="item.id"
              >
                {{ item.name }}
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item class="form-actions">
            <el-button type="primary" @click="handleNext">下一步</el-button>
            <el-button @click="goBack">取消</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步：标签来源 -->
      <div v-if="activeStep === 1" class="step-content">
        <el-form :model="formData" label-width="120px" class="label-form">
          <el-form-item>
            <el-checkbox v-model="skipSource">跳过数据集配置</el-checkbox>
            <div class="skip-tip">选择跳过，标签为待上架，后续在数据集中绑定标签</div>
          </el-form-item>

          <el-form-item label="实体标识" prop="entity_identifier_id">
            <el-select 
              v-model="formData.entity_identifier_id" 
              placeholder="请选择实体标识"
              clearable
              style="width: 100%"
            >
              <el-option 
                v-for="item in entityIdentifierList" 
                :key="item.entity_identifier_id" 
                :label="`${item.entity_name}>${item.entity_identifier_name}`" 
                :value="item.entity_identifier_id" 
              />
            </el-select>
          </el-form-item>

          <template v-if="!skipSource">
            <el-form-item label="数据集名称" prop="dataset_id">
              <el-select 
                v-model="formData.dataset_id" 
                placeholder="请选择标签数据集"
                clearable
                style="width: 100%"
              >
                <el-option 
                  v-for="item in datasetList" 
                  :key="item.dataset_id" 
                  :label="item.dataset_name" 
                  :value="item.dataset_id" 
                />
              </el-select>
            </el-form-item>

            <el-form-item label="数据集字段" prop="dataset_field">
              <el-select 
                v-model="formData.dataset_field" 
                placeholder="请选择数据集字段"
                clearable
                style="width: 100%"
                :disabled="!formData.dataset_id"
              >
                <el-option 
                  v-for="field in datasetFieldList" 
                  :key="field.field_name" 
                  :label="field.field_name" 
                  :value="field.field_name" 
                />
              </el-select>
            </el-form-item>
          </template>

          <el-form-item class="form-actions">
            <el-button @click="handlePrev">上一步</el-button>
            <el-button type="primary" @click="handleSubmit">保存</el-button>
            <el-button @click="goBack">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { labelApi, type LabelConfigResponse } from '@/api/label'
import { labelCategoryApi } from '@/api/labelCategory'
import type { LabelCategory } from '@/types'
import { datasetApi } from '@/api/dataset'
import { entityIdentifierApi } from '@/api/entity'

const route = useRoute()
const router = useRouter()

// 判断是否为编辑模式
const isEditMode = computed(() => !!route.params.id)
const labelId = computed(() => route.params.id as string)

// 当前步骤
const activeStep = ref(0)
const step1FormRef = ref<FormInstance>()

// 是否跳过标签来源
const skipSource = ref(false)

// 配置数据
const config = reactive<LabelConfigResponse>({
  label_type: [],
  data_type: [],
  dist_type: [],
  organize_type: [],
  produce_type: [],
  time_type: [],
  source_type: []
})

// 层级类目选择
const selectedLevel1 = ref('')
const selectedLevel2 = ref('')
const selectedLevel3 = ref('')

// 各级类目列表
const level1Categories = ref<LabelCategory[]>([])
const level2Categories = ref<LabelCategory[]>([])
const level3Categories = ref<LabelCategory[]>([])

// 监听层级选择变化，设置最终的 category_id
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

// 一级类目变化
const handleLevel1Change = async (val: string) => {
  selectedLevel2.value = ''
  selectedLevel3.value = ''
  level2Categories.value = []
  level3Categories.value = []
  if (val) {
    await fetchLevel2Categories(val)
  }
}

// 二级类目变化
const handleLevel2Change = async (val: string) => {
  selectedLevel3.value = ''
  level3Categories.value = []
  if (val) {
    await fetchLevel3Categories(val)
  }
}

// 获取一级类目
const fetchLevel1Categories = async () => {
  try {
    const res = await labelCategoryApi.getList({ category_level: 1 })
    level1Categories.value = res.data.data || []
  } catch (error) {
    console.error('获取一级类目失败:', error)
  }
}

// 获取二级类目
const fetchLevel2Categories = async (parentId: string) => {
  try {
    const res = await labelCategoryApi.getList({ parent_category_id: parentId })
    level2Categories.value = res.data.data || []
  } catch (error) {
    console.error('获取二级类目失败:', error)
  }
}

// 获取三级类目
const fetchLevel3Categories = async (parentId: string) => {
  try {
    const res = await labelCategoryApi.getList({ parent_category_id: parentId })
    level3Categories.value = res.data.data || []
  } catch (error) {
    console.error('获取三级类目失败:', error)
  }
}

// 实体标识列表
const entityIdentifierList = ref<any[]>([])

// 数据集列表
const datasetList = ref<any[]>([])

// 数据集字段列表
const datasetFieldList = ref<any[]>([])

// 表单数据
const formData = reactive({
  label_name: '',
  label_desc: '',
  category_id: '',
  label_type: 1,  // 默认为属性标签
  data_type: 1,   // 默认为文本型
  dist_type: 1,   // 默认为枚举
  organize_type: 1, // 默认为单值
  produce_type: 1,  // 默认为事实标签
  time_type: 1,     // 默认为离线标签
  entity_identifier_id: '',  // 实体标识ID
  dataset_id: '',
  dataset_field: '',
  // 编辑模式需要的字段
  is_office: 0,
  owner: '',
  creator: ''
})

// 第一步表单验证规则
const step1Rules = {
  label_name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  category_id: [{ required: true, message: '请选择标签类目', trigger: 'change' }],
  label_type: [{ required: true, message: '请选择标签类型', trigger: 'change' }],
  data_type: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  dist_type: [{ required: true, message: '请选择数据分布类型', trigger: 'change' }],
  organize_type: [{ required: true, message: '请选择组织类型', trigger: 'change' }],
  produce_type: [{ required: true, message: '请选择加工类型', trigger: 'change' }],
  time_type: [{ required: true, message: '请选择时效性类型', trigger: 'change' }]
}

// 获取标签配置
const fetchConfig = async () => {
  try {
    const res = await labelApi.getConfig()
    const data = res.data.data
    if (data) {
      config.label_type = data.label_type || []
      config.data_type = data.data_type || []
      config.dist_type = data.dist_type || []
      config.organize_type = data.organize_type || []
      config.produce_type = data.produce_type || []
      config.time_type = data.time_type || []
      config.source_type = data.source_type || []
    }
  } catch (error) {
    console.error('获取标签配置失败:', error)
  }
}

// 获取实体标识列表
const fetchEntityIdentifierList = async () => {
  try {
    const res = await entityIdentifierApi.list()
    entityIdentifierList.value = res.data.data || []
  } catch (error) {
    console.error('获取实体标识列表失败:', error)
  }
}

// 获取数据集列表（标签数据集）
const fetchDatasetList = async () => {
  try {
    const res = await datasetApi.list({ dataset_type: 1 }) // 标签数据集
    datasetList.value = res.data.data || []
  } catch (error) {
    console.error('获取数据集列表失败:', error)
  }
}

// 获取数据集字段
const fetchDatasetFields = async (datasetId: string) => {
  if (!datasetId) {
    datasetFieldList.value = []
    return
  }
  try {
    const res = await datasetApi.detail(datasetId)
    const dataset = res.data.data
    if (dataset && dataset.fields) {
      datasetFieldList.value = dataset.fields
    }
  } catch (error) {
    console.error('获取数据集字段失败:', error)
  }
}

// 监听数据集变化
watch(() => formData.dataset_id, (newVal) => {
  formData.dataset_field = ''
  fetchDatasetFields(newVal)
})

// 下一步
const handleNext = async () => {
  if (!step1FormRef.value) return
  await step1FormRef.value.validate((valid) => {
    if (valid) {
      activeStep.value = 1
    }
  })
}

// 上一步
const handlePrev = () => {
  activeStep.value = 0
}

// 提交
const handleSubmit = async () => {
  try {
    const submitData: any = {
      label_name: formData.label_name,
      label_desc: formData.label_desc,
      label_category_id: formData.category_id,
      label_type: formData.label_type,
      label_data_type: formData.data_type,
      label_dist_type: formData.dist_type,
      label_organize_type: formData.organize_type,
      label_produce_type: formData.produce_type,
      label_time_type: formData.time_type,
      source_type: 2,  // 数据源导入方式
      entity_identifier_id: formData.entity_identifier_id  // 实体标识始终提交
    }

    // 编辑模式添加额外字段
    if (isEditMode.value) {
      submitData.is_office = formData.is_office
      submitData.owner = formData.owner
    }

    console.log('提交数据:', submitData, 'isEditMode:', isEditMode.value, 'labelId:', labelId.value)

    // 如果不跳过，添加数据集信息
    if (!skipSource.value) {
      if (formData.dataset_id) {
        submitData.dataset_id = formData.dataset_id
      }
      if (formData.dataset_field) {
        submitData.dataset_field_name = formData.dataset_field
      }
    }

    submitData.config = {}

    if (isEditMode.value) {
      await labelApi.update(labelId.value, submitData)
    } else {
      await labelApi.create(submitData)
    }
    ElMessage.success(isEditMode.value ? '编辑成功' : '创建成功')
    router.push('/label-market')
  } catch (error) {
    console.error('保存失败:', error)
  }
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
      // 标签类型需要转换为数字类型以匹配 config 中的 id
      formData.label_type = label.label_type ? Number(label.label_type) : 1
      formData.data_type = label.label_data_type || 1
      formData.dist_type = label.label_dist_type || 1
      formData.organize_type = label.label_organize_type || 1
      formData.produce_type = label.label_produce_type || 1
      formData.time_type = label.label_time_type || 1
      formData.entity_identifier_id = label.entity_identifier_id || ''
      // 编辑模式需要的字段
      formData.is_office = label.is_office || 0
      formData.owner = label.owner || ''
      formData.creator = label.creator || ''
      
      // 设置类目层级选择
      if (formData.category_id) {
        await setCategoryLevels(formData.category_id)
      }
      
      // 判断是否跳过数据集配置：没有绑定字段则跳过
      skipSource.value = !label.dataset_field_name
      
      // 如果有数据集信息，填充
      if (label.dataset_id) {
        formData.dataset_id = label.dataset_id
        // 等待数据集字段加载
        await fetchDatasetFields(label.dataset_id)
        formData.dataset_field = label.dataset_field_name || ''
      }
    }
  } catch (error) {
    console.error('获取标签详情失败:', error)
    ElMessage.error('获取标签详情失败')
  }
}

// 根据类目ID设置层级选择（分级加载模式）
const setCategoryLevels = async (categoryId: string) => {
  // 先检查当前一级类目列表是否包含目标类目
  const level1Match = level1Categories.value.find(cat => cat.category_id === categoryId)
  if (level1Match) {
    // 目标是一级类目
    selectedLevel1.value = categoryId
    return
  }
  
  // 遍历一级类目，加载二级查找
  for (const level1 of level1Categories.value) {
    await fetchLevel2Categories(level1.category_id)
    const level2Match = level2Categories.value.find(cat => cat.category_id === categoryId)
    if (level2Match) {
      // 目标是二级类目
      selectedLevel1.value = level1.category_id
      selectedLevel2.value = categoryId
      return
    }
    
    // 遍历二级类目，加载三级查找
    for (const level2 of level2Categories.value) {
      await fetchLevel3Categories(level2.category_id)
      const level3Match = level3Categories.value.find(cat => cat.category_id === categoryId)
      if (level3Match) {
        // 目标是三级类目
        selectedLevel1.value = level1.category_id
        selectedLevel2.value = level2.category_id
        selectedLevel3.value = categoryId
        return
      }
    }
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(async () => {
  console.log('onMounted - route.params:', route.params, 'isEditMode:', isEditMode.value, 'labelId:', labelId.value)
  fetchConfig()
  await fetchLevel1Categories()  // 只加载一级类目
  fetchEntityIdentifierList()
  fetchDatasetList()
  fetchLabelDetail()  // 类目加载完成后再加载标签详情
})
</script>

<style scoped lang="scss">
.create-label-page {
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

.steps {
  max-width: 600px;
  margin: 0 auto 40px;
}

.step-content {
  max-width: 600px;
  margin: 0 auto;
}

.label-form {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .skip-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

.category-select-group {
  display: flex;
  gap: 10px;
  
  .el-select {
    flex: 1;
  }
}

.form-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
