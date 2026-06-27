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

        <!-- 上传区域 -->
        <div class="section-content upload-content">
          <div class="upload-container" :class="{ 'is-uploading': uploading }">
            <el-upload
              ref="uploadRef"
              v-loading="uploading"
              element-loading-text="文件上传中..."
              drag
              :auto-upload="true"
              :action="uploadUrl"
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
                <!-- Excel 图标 -->
                <div class="excel-icon">
                  <svg viewBox="0 0 48 48" width="56" height="56">
                    <rect width="48" height="48" rx="6" fill="#1D7A47"/>
                    <text x="50%" y="68%" dominant-baseline="middle" text-anchor="middle"
                          font-size="26" font-weight="bold" fill="white">X</text>
                  </svg>
                </div>
                <!-- 上传提示文字 -->
                <div class="upload-text">
                  上传请 <span class="click-link">点击</span> 或拖拽文件至此区域
                </div>
              </div>
            </el-upload>

            <!-- 提示信息 -->
            <div class="upload-tips-box">
              1. 仅支持上传单个文件；2. 文件不能超过200M；3. 仅支持csv格式
            </div>

            <!-- 已上传文件展示 -->
            <div v-if="uploaded" class="uploaded-file-card">
              <el-icon class="success-icon"><CircleCheck /></el-icon>
              <span class="file-status">已完成</span>
              <span class="file-name">{{ selectedFileName }}</span>
              <el-icon class="delete-icon" @click="handleRemove"><Delete /></el-icon>
            </div>

            <!-- 上传成功提示 -->
            <div v-if="uploaded" class="upload-success-tip">
              文件上传成功，请先保存分群，在分群列表中查看计算结果
            </div>
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="page-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving" :disabled="!uploaded">
          保存群组
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadInstance } from 'element-plus'
import { ArrowLeft, ArrowDown, Menu, Setting, Download, CircleCheck, Delete } from '@element-plus/icons-vue'
import { groupApi } from '@/api/group'
import { entityIdentifierApi } from '@/api/entity'

const router = useRouter()

const basicInfoExpanded = ref(true)
const basicFormRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const saving = ref(false)

// 上传状态
const uploading = ref(false)
const uploaded = ref(false)
const uploadedFileKey = ref('')
const selectedFileName = ref('')
const uploadUrl = '/api/group/upload'

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
    { required: true, message: '请输入群组名称', trigger: 'blur' }
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
  // 暂存文件名（用于上传成功后显示）
  selectedFileName.value = file.name
  uploading.value = true
  return true
}

// 上传成功回调
const handleUploadSuccess = (response: any) => {
  console.log('上传成功响应:', response)
  uploading.value = false

  if (response.code === 0) {
    uploadedFileKey.value = response.data.uuid_file_key
    selectedFileName.value = response.data.file_list?.[0] || selectedFileName.value
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

// 点击删除按钮：删除 MinIO 文件，重置状态
const handleRemove = async () => {
  if (uploadedFileKey.value) {
    try {
      await groupApi.deleteUpload(uploadedFileKey.value)
    } catch (e) {
      console.warn('删除文件失败', e)
    }
  }
  uploaded.value = false
  uploadedFileKey.value = ''
  selectedFileName.value = ''
  // 清除 el-upload 内部文件列表，允许重新上传
  uploadRef.value?.clearFiles()
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

// 保存群组
const handleSave = async () => {
  if (!basicFormRef.value) return
  const basicValid = await basicFormRef.value.validate().catch(() => false)
  if (!basicValid) {
    basicInfoExpanded.value = true
    return
  }
  if (!uploaded.value || !uploadedFileKey.value) {
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
        uuid_file_key: uploadedFileKey.value,
        file_list: [selectedFileName.value]
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
  if (uploaded.value && uploadedFileKey.value) {
    try {
      await groupApi.deleteUpload(uploadedFileKey.value)
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

  // el-upload 拖拽区域样式覆盖
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

  // 提示信息框
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

  // 已上传文件卡片
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

// 上传成功提示
.upload-success-tip {
  margin: 12px auto 0;
  padding: 10px 16px;
  font-size: 13px;
  color: #909399;
  text-align: center;
  max-width: 500px;
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
