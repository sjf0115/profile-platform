<template>
  <div class="create-upload-label-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="page-title">{{ pageTitle }}</h2>
    </div>

    <!-- 内容区域 -->
    <div class="page-content">
      <!-- 基本信息区域 -->
      <div class="section">
        <div class="section-header" @click="basicInfoExpanded = !basicInfoExpanded">
          <el-icon class="section-icon"><Menu /></el-icon>
          <span class="section-title">基本信息</span>
          <el-icon class="expand-icon" :class="{ collapsed: !basicInfoExpanded }">
            <ArrowDown />
          </el-icon>
        </div>
        <div v-show="basicInfoExpanded" class="section-content">
          <el-form
            ref="basicFormRef"
            :model="formData"
            :rules="basicRules"
            label-width="120px"
            class="basic-form"
          >
            <el-form-item label="标签名称" prop="label_name" required>
              <el-input
                v-model="formData.label_name"
                placeholder="请输入标签名称"
                maxlength="50"
                show-word-limit
                style="width: 500px"
              />
            </el-form-item>
            <el-form-item label="标签主体" prop="entity_identifier_id" required>
              <el-select
                v-model="formData.entity_identifier_id"
                placeholder="请选择标签主体"
                style="width: 500px"
              >
                <el-option
                  v-for="item in entityIdentifierList"
                  :key="item.entity_identifier_id"
                  :label="`${item.entity_name} > ${item.entity_identifier_name}`"
                  :value="item.entity_identifier_id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="标签类目" prop="label_category_id">
              <div class="category-select-group">
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
            <el-form-item label="标签描述">
              <el-input
                v-model="formData.label_desc"
                type="textarea"
                placeholder="请输入标签描述"
                :rows="3"
                style="width: 500px"
              />
            </el-form-item>

            <el-divider />

            <el-form-item label="标签类型" prop="label_type">
              <el-select v-model="formData.label_type" placeholder="请选择标签类型" style="width: 500px">
                <el-option v-for="item in config.label_type" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="数据类型" prop="data_type">
              <el-select v-model="formData.data_type" placeholder="请选择数据类型" style="width: 500px">
                <el-option v-for="item in config.data_type" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="数据分布类型" prop="dist_type">
              <el-select v-model="formData.dist_type" placeholder="请选择数据分布类型" style="width: 500px">
                <el-option v-for="item in config.dist_type" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="加工类型" prop="produce_type">
              <el-select v-model="formData.produce_type" placeholder="请选择加工类型" style="width: 500px">
                <el-option v-for="item in config.produce_type" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="时效性类型" prop="time_type">
              <el-select v-model="formData.time_type" placeholder="请选择时效性类型" style="width: 500px">
                <el-option v-for="item in config.time_type" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="组织类型" prop="organize_type">
              <el-radio-group v-model="formData.organize_type">
                <el-radio v-for="item in config.organize_type" :key="item.id" :label="item.id">
                  {{ item.name }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 上传文件区域 -->
      <div class="section rule-section">
        <div class="section-header rule-header">
          <el-icon class="section-icon"><Setting /></el-icon>
          <span class="section-title">上传文件</span>
          <span class="rule-tip">
            请按照模板格式填写后再上传，点击
            <el-button link type="primary" class="download-link" @click="handleDownloadTemplate">
              下载上传模版
              <el-icon style="margin-left: 2px"><Download /></el-icon>
            </el-button>
          </span>
        </div>

        <div class="section-content upload-content">
          <div class="upload-container" :class="{ 'is-uploading': uploading }">
            <el-upload
              ref="uploadRef"
              v-loading="uploading"
              element-loading-text="文件上传中..."
              drag
              :auto-upload="true"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :limit="1"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :on-remove="handleRemove"
              :before-upload="beforeUpload"
              :show-file-list="false"
              name="file"
              accept=".csv"
            >
              <div class="upload-drag-content">
                <div class="excel-icon">
                  <svg viewBox="0 0 48 48" width="56" height="56">
                    <rect width="48" height="48" rx="6" fill="#1D7A47"/>
                    <text x="50%" y="68%" dominant-baseline="middle" text-anchor="middle"
                          font-size="26" font-weight="bold" fill="white">X</text>
                  </svg>
                </div>
                <div class="upload-text">
                  上传请 <span class="click-link">点击</span> 或拖拽文件至此区域
                </div>
              </div>
            </el-upload>

            <div class="upload-tips-box">
              1. 仅支持上传单个文件；2. 文件不能超过200M；3. 仅支持csv格式
            </div>

            <div v-if="uploaded" class="uploaded-file-card">
              <el-icon class="success-icon"><CircleCheck /></el-icon>
              <span class="file-status">已完成</span>
              <span class="file-name">{{ selectedFileName }}</span>
              <el-icon class="delete-icon" @click="handleRemove"><Delete /></el-icon>
            </div>

            <div v-if="uploaded" class="upload-success-tip">
              文件上传成功，请点击底部"保存标签"完成创建
            </div>
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="page-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving" :disabled="!uploaded">
          {{ isEdit ? '保存修改' : '保存标签' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadInstance } from 'element-plus'
import { ArrowLeft, ArrowDown, Menu, Setting, Download, CircleCheck, Delete } from '@element-plus/icons-vue'
import { labelApi, type LabelConfigResponse } from '@/api/label'
import { labelCategoryApi } from '@/api/labelCategory'
import { entityIdentifierApi } from '@/api/entity'
import { getToken } from '@/utils/auth'
import type { LabelCategory } from '@/types'

const router = useRouter()
const route = useRoute()

// 编辑模式判断
const labelId = computed(() => route.params.id as string | undefined)
const isEdit = computed(() => !!labelId.value)
const pageTitle = computed(() => isEdit.value ? '编辑标签 - 文件上传' : '创建标签 - 文件上传')

const basicInfoExpanded = ref(true)
const basicFormRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const saving = ref(false)

// 上传状态
const uploading = ref(false)
const uploaded = ref(false)
const uploadedFileKey = ref('')
const selectedFileName = ref('')
// 编辑模式下回显的存量文件 Key，用于区分"本次新上传"与"存量文件"，防止误删 MinIO 文件
const originalFileKey = ref('')
const uploadUrl = labelApi.uploadUrl
// el-upload 使用原生 XHR，不经过 axios 拦截器，需手动注入认证 Token
const uploadHeaders = computed(() => ({
  Authorization: getToken() ? `Bearer ${getToken()}` : ''
}))

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

// 实体标识列表
const entityIdentifierList = ref<any[]>([])

// 层级类目选择
const selectedLevel1 = ref('')
const selectedLevel2 = ref('')
const selectedLevel3 = ref('')
const level1Categories = ref<LabelCategory[]>([])
const level2Categories = ref<LabelCategory[]>([])
const level3Categories = ref<LabelCategory[]>([])

// 表单数据
const formData = reactive({
  label_name: '',
  label_desc: '',
  entity_identifier_id: '',
  label_category_id: '',
  label_type: 1,
  data_type: 1,
  dist_type: 1,
  organize_type: 1,
  produce_type: 1,
  time_type: 1,
  // 编辑模式
  owner: ''
})

// 表单校验规则
const basicRules = {
  label_name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  entity_identifier_id: [{ required: true, message: '请选择标签主体', trigger: 'change' }],
  label_type: [{ required: true, message: '请选择标签类型', trigger: 'change' }],
  data_type: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  dist_type: [{ required: true, message: '请选择数据分布类型', trigger: 'change' }],
  organize_type: [{ required: true, message: '请选择组织类型', trigger: 'change' }],
  produce_type: [{ required: true, message: '请选择加工类型', trigger: 'change' }],
  time_type: [{ required: true, message: '请选择时效性类型', trigger: 'change' }]
}

// 监听层级选择变化
watch([selectedLevel1, selectedLevel2, selectedLevel3], ([level1, level2, level3]) => {
  if (level3) {
    formData.label_category_id = level3
  } else if (level2) {
    formData.label_category_id = level2
  } else if (level1) {
    formData.label_category_id = level1
  } else {
    formData.label_category_id = ''
  }
})

// 一级类目变化
const handleLevel1Change = async (val: string) => {
  selectedLevel2.value = ''
  selectedLevel3.value = ''
  level2Categories.value = []
  level3Categories.value = []
  if (val) await fetchLevel2Categories(val)
}

// 二级类目变化
const handleLevel2Change = async (val: string) => {
  selectedLevel3.value = ''
  level3Categories.value = []
  if (val) await fetchLevel3Categories(val)
}

// 获取类目
const fetchLevel1Categories = async () => {
  try {
    const res = await labelCategoryApi.getList({ category_level: 1 })
    level1Categories.value = res.data.data || []
  } catch (error) {
    console.error('获取一级类目失败:', error)
  }
}
const fetchLevel2Categories = async (parentId: string) => {
  try {
    const res = await labelCategoryApi.getList({ parent_category_id: parentId })
    level2Categories.value = res.data.data || []
  } catch (error) {
    console.error('获取二级类目失败:', error)
  }
}
const fetchLevel3Categories = async (parentId: string) => {
  try {
    const res = await labelCategoryApi.getList({ parent_category_id: parentId })
    level3Categories.value = res.data.data || []
  } catch (error) {
    console.error('获取三级类目失败:', error)
  }
}

// 获取配置
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

// 上传前校验
const beforeUpload = (file: File) => {
  if (!file.name.toLowerCase().endsWith('.csv')) {
    ElMessage.error('仅支持 CSV 格式文件')
    return false
  }
  if (file.size > 200 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 200M')
    return false
  }
  selectedFileName.value = file.name
  uploading.value = true
  return true
}

// 上传成功
const handleUploadSuccess = (response: any) => {
  uploading.value = false
  if (response.code === 0) {
    uploadedFileKey.value = response.data.uuid_file_key
    selectedFileName.value = response.data.file_name || selectedFileName.value
    uploaded.value = true
  } else {
    ElMessage.error(response.message || '文件上传失败')
    uploaded.value = false
  }
}

// 上传失败
const handleUploadError = () => {
  uploading.value = false
  ElMessage.error('文件上传失败')
  uploaded.value = false
}

// 删除已上传文件
const handleRemove = async () => {
  // 仅当删除的是本次会话新上传的文件时才清理 MinIO；
  // 编辑模式下回显的存量文件只清本地状态，保存时由后端统一处理
  if (uploadedFileKey.value && uploadedFileKey.value !== originalFileKey.value) {
    try {
      await labelApi.cancelUpload(uploadedFileKey.value)
    } catch (e) {
      console.warn('删除文件失败', e)
    }
  }
  uploaded.value = false
  uploadedFileKey.value = ''
  selectedFileName.value = ''
  uploadRef.value?.clearFiles()
}

// 下载模板
const handleDownloadTemplate = async () => {
  try {
    const res = await labelApi.downloadTemplate()
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'label_upload_template.csv'
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

// 保存标签
const handleSave = async () => {
  if (!basicFormRef.value) return
  const valid = await basicFormRef.value.validate().catch(() => false)
  if (!valid) {
    basicInfoExpanded.value = true
    return
  }
  if (!uploaded.value || !uploadedFileKey.value) {
    ElMessage.warning('请先上传文件')
    return
  }

  saving.value = true
  try {
    const submitData: any = {
      label_name: formData.label_name,
      label_desc: formData.label_desc,
      label_category_id: formData.label_category_id,
      label_type: formData.label_type,
      label_data_type: formData.data_type,
      label_dist_type: formData.dist_type,
      label_organize_type: formData.organize_type,
      label_produce_type: formData.produce_type,
      label_time_type: formData.time_type,
      source_type: 3,  // 文件上传
      entity_identifier_id: formData.entity_identifier_id,
      owner: formData.owner || undefined,
      config: {
        physical_path: uploadedFileKey.value,
        origin_path: selectedFileName.value
      }
    }

    if (isEdit.value && labelId.value) {
      await labelApi.update(labelId.value, submitData)
    } else {
      await labelApi.create(submitData)
    }
    ElMessage.success(isEdit.value ? '编辑成功' : '标签创建成功')
    router.push('/label-market')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存标签失败')
  } finally {
    saving.value = false
  }
}

// 返回
const goBack = async () => {
  // 仅清理本次会话新上传的文件，避免编辑模式下误删存量文件
  if (uploaded.value && uploadedFileKey.value && uploadedFileKey.value !== originalFileKey.value) {
    try {
      await labelApi.cancelUpload(uploadedFileKey.value)
    } catch (error) {
      console.error('删除 MinIO 文件失败:', error)
    }
  }
  router.back()
}

// 加载标签详情（编辑模式）
const fetchLabelDetail = async () => {
  if (!isEdit.value || !labelId.value) return
  try {
    const res = await labelApi.getDetail(labelId.value)
    const label = res.data.data
    if (label) {
      formData.label_name = label.label_name || ''
      formData.label_desc = label.label_desc || ''
      formData.label_category_id = label.label_category_id || ''
      formData.label_type = label.label_type ? Number(label.label_type) : 1
      formData.data_type = label.label_data_type || 1
      formData.dist_type = label.label_dist_type || 1
      formData.organize_type = label.label_organize_type || 1
      formData.produce_type = label.label_produce_type || 1
      formData.time_type = label.label_time_type || 1
      formData.entity_identifier_id = label.entity_identifier_id || ''
      formData.owner = label.owner || ''

      // 设置类目层级
      if (formData.label_category_id) {
        await setCategoryLevels(formData.label_category_id)
      }

      // 回显文件信息
      if (label.config) {
        const parsedConfig = typeof label.config === 'string' ? JSON.parse(label.config) : label.config
        if (parsedConfig?.physical_path) {
          uploadedFileKey.value = parsedConfig.physical_path
          originalFileKey.value = parsedConfig.physical_path
          selectedFileName.value = parsedConfig.origin_path || ''
          uploaded.value = true
        }
      }
    }
  } catch (error) {
    console.error('获取标签详情失败:', error)
    ElMessage.error('获取标签详情失败')
  }
}

// 根据类目ID设置层级选择
const setCategoryLevels = async (categoryId: string) => {
  const level1Match = level1Categories.value.find(cat => cat.category_id === categoryId)
  if (level1Match) {
    selectedLevel1.value = categoryId
    return
  }
  for (const level1 of level1Categories.value) {
    await fetchLevel2Categories(level1.category_id)
    const level2Match = level2Categories.value.find(cat => cat.category_id === categoryId)
    if (level2Match) {
      selectedLevel1.value = level1.category_id
      selectedLevel2.value = categoryId
      return
    }
    for (const level2 of level2Categories.value) {
      await fetchLevel3Categories(level2.category_id)
      const level3Match = level3Categories.value.find(cat => cat.category_id === categoryId)
      if (level3Match) {
        selectedLevel1.value = level1.category_id
        selectedLevel2.value = level2.category_id
        selectedLevel3.value = categoryId
        return
      }
    }
  }
}

onMounted(() => {
  fetchConfig()
  fetchLevel1Categories()
  fetchEntityIdentifierList()
  fetchLabelDetail()
})
</script>

<style scoped lang="scss">
.create-upload-label-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
  }
}

.page-content {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  flex: 1;
  overflow: auto;
}

.section {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 16px;

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px 20px;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;

    .section-icon {
      color: #409eff;
      font-size: 16px;
    }

    .section-title {
      font-size: 15px;
      font-weight: 500;
      color: #303133;
    }

    .expand-icon {
      margin-left: auto;
      transition: transform 0.3s;
      &.collapsed {
        transform: rotate(-90deg);
      }
    }
  }

  .section-content {
    padding: 24px 20px;
  }
}

.basic-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.rule-section {
  .rule-header {
    cursor: default;
  }
}

.rule-tip {
  font-size: 13px;
  color: #909399;
  margin-left: 8px;
  display: flex;
  align-items: center;
  gap: 4px;

  .download-link {
    font-size: 13px;
    padding: 0;
    height: auto;
    color: #409eff;
  }
}

.category-select-group {
  display: flex;
  gap: 10px;

  .el-select {
    flex: 1;
  }
}

// 上传区域
.upload-container {
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  background: #fafafa;
  padding: 40px 20px 20px;
  transition: border-color 0.3s;

  &:hover {
    border-color: #409eff;
  }

  &.is-uploading {
    pointer-events: none;
    opacity: 0.7;
  }

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    background: transparent;
    border: none;
    padding: 20px 0;

    &:hover {
      background: transparent;
    }
  }

  .upload-drag-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;

    .excel-icon {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .upload-text {
      font-size: 14px;
      color: #606266;

      .click-link {
        color: #409eff;
        cursor: pointer;
      }
    }
  }

  .upload-tips-box {
    margin: 16px auto 0;
    padding: 10px 16px;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    font-size: 12px;
    color: #909399;
    text-align: center;
    max-width: 500px;
  }

  .uploaded-file-card {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 16px auto 0;
    padding: 12px 16px;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    max-width: 500px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

    .success-icon {
      color: #67c23a;
      font-size: 16px;
    }

    .file-status {
      font-size: 13px;
      color: #606266;
    }

    .file-name {
      flex: 1;
      font-size: 13px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .delete-icon {
      color: #909399;
      font-size: 14px;
      cursor: pointer;

      &:hover {
        color: #f56c6c;
      }
    }
  }
}

.upload-success-tip {
  margin: 12px auto 0;
  padding: 10px 16px;
  font-size: 13px;
  color: #909399;
  text-align: center;
  max-width: 500px;
}

.page-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
  margin-top: 8px;
  border-top: 1px solid #ebeef5;
}
</style>
