<template>
  <div class="create-upload-group-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="page-title">上传文件创建</h2>
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
            :model="basicForm"
            :rules="basicRules"
            label-width="100px"
            class="basic-form"
          >
            <el-form-item label="群组名称" prop="group_name" required>
              <el-input
                v-model="basicForm.group_name"
                placeholder="请输入群组名称"
                maxlength="50"
                show-word-limit
                style="width: 500px"
              />
            </el-form-item>
            <el-form-item label="分析主体" prop="entity_identifier_id" required>
              <el-select
                v-model="basicForm.entity_identifier_id"
                placeholder="请选择分析主体"
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
            <el-form-item label="群组描述">
              <el-input
                v-model="basicForm.group_desc"
                type="textarea"
                placeholder="请输入群组描述"
                :rows="3"
                style="width: 500px"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 创建规则区域 -->
      <div class="section rule-section">
        <div class="section-header rule-header">
          <el-icon class="section-icon"><Setting /></el-icon>
          <span class="section-title">创建规则</span>
          <span class="rule-tip">
            请按照模板格式填写后再上传，点击
            <el-button link type="primary" class="download-link" @click="handleDownloadTemplate">
              下载上传模版
              <el-icon style="margin-left: 2px"><Download /></el-icon>
            </el-button>
          </span>
        </div>

        <!-- 列头预览提示条 -->
        <div class="column-preview-bar">
          <span class="col-tag">entity_id</span>
        </div>

        <!-- 上传区域 -->
        <div class="section-content upload-content">
          <el-upload
            ref="uploadRef"
            class="upload-dragger"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".csv"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            :show-file-list="false"
            :disabled="uploaded"
          >
            <div class="upload-inner">
              <div class="excel-icon">
                <svg viewBox="0 0 48 48" width="52" height="52">
                  <rect width="48" height="48" rx="6" fill="#1D7A47"/>
                  <text x="50%" y="68%" dominant-baseline="middle" text-anchor="middle"
                        font-size="26" font-weight="bold" fill="white">X</text>
                </svg>
              </div>
              <div class="upload-text">
                上传请
                <span class="click-link">点击</span>
                或拖拽文件至此区域
              </div>
              <div class="upload-tips">
                1. 仅支持上传单个文件；2. 文件不能超过200M；3. 仅支持csv格式
              </div>
            </div>
          </el-upload>

          <!-- 已选文件展示 -->
          <div v-if="selectedFile" class="selected-file">
            <el-icon class="file-icon"><Document /></el-icon>
            <span class="file-name">{{ selectedFile.name }}</span>
            <span class="file-size">（{{ formatFileSize(selectedFile.size || 0) }}）</span>
            <el-tag v-if="uploaded" type="success" size="small">已上传</el-tag>
            <el-button link type="danger" @click="removeFile">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>

          <!-- 上传成功提示 -->
          <div v-if="uploaded" class="upload-success-tip">
            <el-icon style="color: #67c23a"><CircleCheck /></el-icon>
            <span>文件上传成功，请先保存群组，在群组列表中查看计算结果</span>
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="page-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="success" @click="handleUpload" :loading="saving" :disabled="!selectedFile || uploaded">
          {{ uploaded ? '已上传' : '上传文件' }}
        </el-button>
        <el-button type="primary" @click="handleSave" :loading="saving" :disabled="!uploaded">保存群组</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UploadFile, FormInstance, UploadInstance } from 'element-plus'
import { ArrowLeft, ArrowDown, Menu, Setting, Download, Document, Delete, CircleCheck } from '@element-plus/icons-vue'
import { groupApi } from '@/api/group'
import { entityIdentifierApi } from '@/api/entity'

const router = useRouter()

const basicInfoExpanded = ref(true)
const basicFormRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const saving = ref(false)
const selectedFile = ref<UploadFile | null>(null)
const uploaded = ref(false)
const uploadResult = ref<{ uuid_file_key: string; file_list: string[] } | null>(null)

// 实体标识列表
const entityIdentifierList = ref<any[]>([])

// 基本信息表单
const basicForm = reactive({
  group_name: '',
  group_desc: '',
  entity_identifier_id: ''
})

// 表单校验规则
const basicRules = {
  group_name: [
    { required: true, message: '请输入分群名称', trigger: 'blur' }
  ],
  entity_identifier_id: [
    { required: true, message: '请选择分析主体', trigger: 'change' }
  ]
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

// 文件变化
const handleFileChange = (file: UploadFile) => {
  // 校验文件格式
  const name = file.name || ''
  if (!name.toLowerCase().endsWith('.csv')) {
    ElMessage.error('仅支持 csv 格式文件')
    uploadRef.value?.clearFiles()
    return
  }
  // 校验文件大小 200M
  const maxSize = 200 * 1024 * 1024
  if (file.size && file.size > maxSize) {
    ElMessage.error('文件不能超过 200M')
    uploadRef.value?.clearFiles()
    return
  }
  selectedFile.value = file
}

// 超出数量限制
const handleExceed = () => {
  ElMessage.warning('只能上传单个文件，请先删除已选文件')
}

// 删除文件（同时删除 MinIO 中的文件）
const removeFile = async () => {
  // 如果已上传到 MinIO，先删除
  if (uploaded.value && uploadResult.value?.uuid_file_key) {
    try {
      await groupApi.deleteUploadedFile(uploadResult.value.uuid_file_key)
    } catch (error) {
      console.error('删除 MinIO 文件失败:', error)
    }
  }
  selectedFile.value = null
  uploaded.value = false
  uploadResult.value = null
  uploadRef.value?.clearFiles()
}

// 格式化文件大小
const formatFileSize = (size: number): string => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

// 下载模板
const handleDownloadTemplate = async () => {
  try {
    const res = await groupApi.downloadTemplate()
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'group_upload_template.csv'
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

// 上传文件
const handleUpload = async () => {
  if (!selectedFile.value || !selectedFile.value.raw) {
    ElMessage.warning('请选择 CSV 文件')
    return
  }

  saving.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value.raw)

    const res = await groupApi.uploadFile(formData)
    if (res.data.code === 200) {
      uploaded.value = true
      uploadResult.value = res.data.data
      ElMessage.success('文件上传成功，请先保存群组，在群组列表中查看计算结果')
    } else {
      ElMessage.error(res.data.message || '文件上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('文件上传失败')
  } finally {
    saving.value = false
  }
}

// 保存群组
const handleSave = async () => {
  if (!basicFormRef.value) return
  const basicValid = await basicFormRef.value.validate().catch(() => false)
  if (!basicValid) {
    basicInfoExpanded.value = true
    return
  }
  if (!uploaded.value || !uploadResult.value) {
    ElMessage.warning('请先上传文件')
    return
  }

  saving.value = true
  try {
    const groupData = {
      group_name: basicForm.group_name,
      group_desc: basicForm.group_desc,
      entity_identifier_id: basicForm.entity_identifier_id,
      group_type: 2,   // 2-文件上传
      trigger_type: 1,
      source_type: 2,
      group_rule: {
        type: 'upload',
        uuid_file_key: uploadResult.value.uuid_file_key,
        file_list: uploadResult.value.file_list
      }
    }

    await groupApi.save(groupData)
    ElMessage.success('群组保存成功')
    router.push('/group/filter')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('群组保存失败')
  } finally {
    saving.value = false
  }
}

const goBack = async () => {
  // 如果已上传到 MinIO，先删除文件
  if (uploaded.value && uploadResult.value?.uuid_file_key) {
    try {
      await groupApi.deleteUploadedFile(uploadResult.value.uuid_file_key)
    } catch (error) {
      console.error('删除 MinIO 文件失败:', error)
    }
  }
  router.back()
}

onMounted(() => {
  fetchEntityIdentifierList()
})
</script>

<style scoped lang="scss">
.create-upload-group-page {
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
  }
}

.page-content {
  flex: 1;
  padding: 20px;
  overflow: auto;
}

.section {
  background: #fff;
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

// 列头预览条
.column-preview-bar {
  padding: 8px 20px;
  background: #f9f9f9;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;

  .col-tag {
    font-size: 12px;
    color: #606266;
    background: #e8f4ff;
    border: 1px solid #c6e2ff;
    border-radius: 3px;
    padding: 2px 10px;
  }
}

// 上传区域
.upload-content {
  padding: 20px;
}

.upload-dragger {
  width: 100%;

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    height: 280px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fafafa;
    border: 1.5px dashed #d9d9d9;
    border-radius: 6px;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.upload-inner {
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
    font-size: 15px;
    color: #303133;

    .click-link {
      color: #409eff;
      cursor: pointer;
    }
  }

  .upload-tips {
    font-size: 12px;
    color: #909399;
  }
}

// 已选文件
.selected-file {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 16px;
  background: #f0f7ff;
  border: 1px solid #c6e2ff;
  border-radius: 4px;

  .file-icon {
    color: #409eff;
    font-size: 18px;
  }

  .file-name {
    font-size: 14px;
    color: #303133;
    flex: 1;
  }

  .file-size {
    font-size: 12px;
    color: #909399;
  }
}

// 上传成功提示
.upload-success-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 16px;
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  font-size: 13px;
  color: #67c23a;
}

// 底部操作
.page-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}
</style>
