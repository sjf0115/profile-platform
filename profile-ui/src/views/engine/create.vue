<template>
  <div class="engine-create-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">
        {{ isEdit ? '编辑计算引擎' : '创建计算引擎' }}
      </h2>
      <div class="page-desc">
        {{ isEdit ? '修改计算引擎配置信息' : '创建计算引擎，用于数据集的计算和存储' }}
      </div>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="140px"
        class="engine-form"
      >
        <!-- 基础信息 -->
        <div class="form-section">
          <h3 class="section-title">基础信息</h3>
          
          <el-form-item label="引擎名称" prop="engineName">
            <el-input
              v-model="formData.engineName"
              placeholder="引擎名称必须唯一；必须以字母、数字、下划线组合，且不能以数字和下划线开头。"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="引擎描述">
            <el-input
              v-model="formData.engineDesc"
              type="textarea"
              :rows="3"
              placeholder="请输入引擎描述"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="引擎类型" prop="engineType">
            <el-select
              v-model="formData.engineType"
              placeholder="请选择引擎类型"
              style="width: 100%"
              :disabled="isEdit"
            >
              <el-option
                v-for="item in engineTypeOptions"
                :key="item.key"
                :label="item.value"
                :value="item.key"
              />
            </el-select>
            <div v-if="isEdit" class="form-tip">引擎类型创建后不可修改</div>
          </el-form-item>

          <el-form-item label="设为默认引擎">
            <el-switch
              v-model="formData.isDefault"
              :active-value="1"
              :inactive-value="0"
              active-text="是"
              inactive-text="否"
              :disabled="isEdit"
            />
            <div v-if="isEdit" class="form-tip">默认引擎标志创建后不可修改</div>
          </el-form-item>
        </div>

        <!-- 连接信息 -->
        <div class="form-section">
          <h3 class="section-title">连接信息</h3>
          
          <el-form-item label="主机地址" prop="config.host">
            <el-input
              v-model="formData.config.host"
              placeholder="请输入主机地址"
              clearable
            />
          </el-form-item>

          <el-form-item label="端口" prop="config.port">
            <el-input
              v-model="formData.config.port"
              placeholder="请输入端口号"
              clearable
            />
          </el-form-item>

          <el-form-item label="数据库名" prop="config.database">
            <el-input
              v-model="formData.config.database"
              placeholder="请输入数据库名"
              clearable
            />
          </el-form-item>

          <el-form-item label="用户名" prop="config.username">
            <el-input
              v-model="formData.config.username"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="config.password">
            <el-input
              v-model="formData.config.password"
              placeholder="请输入密码"
              type="password"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item label="最大连接数">
            <el-input-number
              v-model="formData.config.maxConnections"
              :min="1"
              :max="100"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="连接超时时间(ms)">
            <el-input-number
              v-model="formData.config.connectionTimeout"
              :min="1000"
              :max="60000"
              style="width: 100%"
            />
          </el-form-item>
        </div>
      </el-form>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="handleBack">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { Engine, EngineTypeItem } from '@/types'
import { engineApi } from '@/api/engine'

const router = useRouter()

// 是否编辑模式
const isEdit = ref(false)

// 引擎 ID
const engineId = ref('')

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 引擎类型选项
const engineTypeOptions = ref<EngineTypeItem[]>([])

// config 工具函数
const configUtils = {
  // 将后端 config 字符串解析为对象（用于编辑时回填）
  parse(config: string | undefined): Record<string, any> {
    if (!config) return {}
    try {
      return JSON.parse(config)
    } catch (e) {
      console.error('解析 config 失败:', e)
      return {}
    }
  },
  // 将表单 config 对象序列化为字符串（用于保存）
  stringify(config: Record<string, any>): string {
    return JSON.stringify(config)
  },
}

// 表单数据
const formData = reactive<{
  engineName: string
  engineDesc: string
  engineType: string
  isDefault: number
  config: Record<string, any>
}>({
  engineName: '',
  engineDesc: '',
  engineType: '',
  isDefault: 0,
  config: {
    host: '',
    port: '',
    database: '',
    username: '',
    password: '',
    maxConnections: 10,
    connectionTimeout: 30000,
  },
})

// 表单校验规则
const formRules = reactive<FormRules>({
  engineName: [
    { required: true, message: '请输入引擎名称', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: '必须以字母开头，只能包含字母、数字、下划线',
      trigger: 'blur',
    },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' },
  ],
  engineType: [
    { required: true, message: '请选择引擎类型', trigger: 'change' },
  ],
  'config.host': [
    { required: true, message: '请输入主机地址', trigger: 'blur' },
  ],
  'config.port': [
    { required: true, message: '请输入端口号', trigger: 'blur' },
  ],
  'config.database': [
    { required: true, message: '请输入数据库名', trigger: 'blur' },
  ],
  'config.username': [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
})

// 获取引擎类型列表
const fetchEngineTypeList = async () => {
  try {
    const res = await engineApi.getTypeList()
    engineTypeOptions.value = res.data.data || []
  } catch (error) {
    console.error('获取引擎类型列表失败:', error)
  }
}

// 获取引擎详情（编辑模式）
const fetchEngineDetail = async () => {
  try {
    // 先获取默认引擎
    const res = await engineApi.getDefault()
    const data = res.data.data
    
    if (data && data.engine_id) {
      // 已存在引擎，进入编辑模式
      isEdit.value = true
      engineId.value = data.engine_id
      
      // 回填表单数据
      formData.engineName = data.engine_name
      formData.engineDesc = data.engine_desc || ''
      formData.engineType = data.engine_type || ''
      formData.isDefault = data.is_default || 0
      
      // 解析 config 并回填
      const configObj = configUtils.parse(data.config)
      Object.assign(formData.config, configObj)
    } else {
      // 不存在引擎，进入创建模式
      isEdit.value = false
    }
  } catch (error) {
    console.error('获取引擎信息失败:', error)
    // 如果获取失败，默认进入创建模式
    isEdit.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const params: Engine = {
        engine_name: formData.engineName,
        engine_desc: formData.engineDesc,
        engine_type: formData.engineType,
        is_default: formData.isDefault,
        config: configUtils.stringify(formData.config),
      }
      
      // 编辑时传入引擎ID
      if (isEdit.value && engineId.value) {
        params.engine_id = engineId.value
      }
      
      await engineApi.save(params)
      ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
      
      // 创建成功后，刷新页面进入编辑模式
      if (!isEdit.value) {
        isEdit.value = true
        // 重新获取引擎详情
        await fetchEngineDetail()
      }
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  // 获取引擎类型列表
  fetchEngineTypeList()
  // 获取引擎信息
  fetchEngineDetail()
})
</script>

<style scoped lang="scss">
.engine-create-page {
  .page-header {
    margin-bottom: 24px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .page-desc {
      font-size: 14px;
      color: #909399;
      margin: 0;
    }
  }

  .form-card {
    .engine-form {
      max-width: 800px;
    }

    .form-section {
      margin-bottom: 30px;

      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 20px;
        padding-bottom: 10px;
        border-bottom: 1px solid #ebeef5;
        display: flex;
        align-items: center;
      }

      .form-tip {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }

    .form-actions {
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid #ebeef5;
      display: flex;
      justify-content: center;
      gap: 16px;
    }
  }
}
</style>
