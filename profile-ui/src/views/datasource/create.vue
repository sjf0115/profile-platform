<template>
  <div class="datasource-create-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">
        {{ isEdit ? '编辑' : '创建' }}{{ schemaInfo?.schema_name || '' }}数据源
      </h2>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="140px"
        class="datasource-form"
      >
        <!-- 基础信息 -->
        <div class="form-section">
          <h3 class="section-title">基础信息</h3>
          
          <el-form-item label="数据源名称" prop="datasourceName">
            <el-input
              v-model="formData.datasourceName"
              placeholder="数据源名称必须唯一；必须以字母、数字、下划线组合，且不能以数字和下划线开头。"
              maxlength="64"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="数据源描述">
            <el-input
              v-model="formData.datasourceDesc"
              type="textarea"
              :rows="3"
              placeholder="请输入数据源描述"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </div>

        <!-- 连接信息 -->
        <div class="form-section">
          <h3 class="section-title">连接信息</h3>
          
          <template v-if="schemaInfo?.config_template">
            <el-form-item
              v-for="item in schemaInfo.config_template"
              :key="item.key"
              :label="item.show_name"
              :prop="`config.${item.key}`"
              :rules="{
                required: item.required === 1,
                message: `请输入${item.show_name}`,
                trigger: 'blur',
              }"
            >
              <!-- 密码类型 -->
              <el-input
                v-if="item.encrypt === 1"
                v-model="formData.config[item.key]"
                type="password"
                show-password
                :placeholder="item.tip || `请输入${item.show_name}`"
              />
              <!-- 普通输入 -->
              <el-input
                v-else
                v-model="formData.config[item.key]"
                :placeholder="item.tip || `请输入${item.show_name}`"
              />
            </el-form-item>
          </template>

          <!-- 认证选项 -->
          <el-form-item label="认证选项" prop="authType">
            <el-radio-group v-model="formData.authType">
              <el-radio label="none">无认证</el-radio>
              <el-radio label="ssl">SSL认证</el-radio>
            </el-radio-group>
          </el-form-item>
          
        </div>

      </el-form>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="handleBack">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
        <el-button type="success" :loading="testing" @click="handleTestConnection">
          测试连接
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, QuestionFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { DataSource, DataSourceSchema } from '@/types'
import { dataSourceApi, dataSourceSchemaApi } from '@/api/datasource'

const route = useRoute()
const router = useRouter()

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)

// Schema ID
const schemaId = computed(() => route.query.schema_id as string)

// 数据源 ID
const datasourceId = computed(() => route.params.id as string)

// Schema 信息
const schemaInfo = ref<DataSourceSchema | null>(null)

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 测试连接状态
const testing = ref(false)

// 显示高级设置
const showAdvanced = ref(false)

// 表单数据
const formData = reactive<{
  datasourceName: string
  datasourceDesc: string
  envType: string
  authType: string
  version: string
  connectTimeout: number
  readTimeout: number
  maxConnections: number
  config: Record<string, string>
}>({
  datasourceName: '',
  datasourceDesc: '',
  envType: 'prod',
  authType: 'none',
  version: 'auto',
  connectTimeout: 30,
  readTimeout: 30,
  maxConnections: 10,
  config: {},
})

// 表单校验规则
const formRules = reactive<FormRules>({
  datasourceName: [
    { required: true, message: '请输入数据源名称', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: '必须以字母开头，只能包含字母、数字、下划线',
      trigger: 'blur',
    },
    { min: 2, max: 64, message: '长度在 2 到 64 个字符', trigger: 'blur' },
  ],
  authType: [{ required: true, message: '请选择认证选项', trigger: 'change' }],
})

// 获取 Schema 详情（根据 schema_id 获取 config_template）
const fetchSchemaDetail = async () => {
  if (!schemaId.value) return
  try {
    // 调用 /datasource/schema/detail 接口获取具体的 config_template
    const res = await dataSourceSchemaApi.getDetail(schemaId.value)
    schemaInfo.value = res.data.data
    
    if (!schemaInfo.value) {
      ElMessage.error('未找到该数据源类型的配置信息')
      return
    }
    
    // 初始化配置项
    if (schemaInfo.value?.config_template) {
      schemaInfo.value.config_template.forEach((item) => {
        if (!formData.config[item.key]) {
          formData.config[item.key] = item.value || ''
        }
      })
    }
  } catch (error) {
    console.error('获取 Schema 详情失败:', error)
    ElMessage.error('获取数据源类型信息失败')
  }
}

// 获取数据源详情
const fetchDataSourceDetail = async () => {
  if (!isEdit.value || !datasourceId.value) return
  try {
    const res = await dataSourceApi.getDetail(datasourceId.value)
    const data = res.data.data
    
    if (!data) {
      ElMessage.error('数据源不存在')
      return
    }
    
    // 回填表单数据
    formData.datasourceName = data.datasource_name
    formData.datasourceDesc = data.datasource_desc || ''
    
    // 解析 config
    if (data.config) {
      try {
        const config = JSON.parse(data.config)
        Object.assign(formData.config, config)
      } catch (e) {
        console.error('解析配置失败:', e)
      }
    }
  } catch (error) {
    console.error('获取数据源详情失败:', error)
    ElMessage.error('获取数据源信息失败')
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
      const params: DataSource = {
        datasource_name: formData.datasourceName,
        datasource_desc: formData.datasourceDesc,
        schema_id: schemaId.value,
        schema_name: schemaInfo.value?.schema_name,
        schema_type: schemaInfo.value?.schema_type,
        config: formData.config,
      }
      
      // 编辑时传入数据源ID
      if (isEdit.value) {
        params.datasource_id = datasourceId.value
      }
      
      await dataSourceApi.save(params)
      ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
      router.push('/datasource')
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 测试连接
const handleTestConnection = async () => {
  testing.value = true
  try {
    // 这里可以调用测试连接 API
    await new Promise((resolve) => setTimeout(resolve, 1000))
    ElMessage.success('连接成功')
  } catch (error) {
    ElMessage.error('连接失败')
  } finally {
    testing.value = false
  }
}

// 生成 ID
const generateId = () => {
  return 'ds_' + Date.now().toString(36) + Math.random().toString(36).substr(2, 5)
}

onMounted(() => {
  fetchSchemaDetail()
  if (isEdit.value) {
    fetchDataSourceDetail()
  }
})
</script>

<style scoped lang="scss">
.datasource-create-page {
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    .back-btn {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #606266;
      cursor: pointer;
      margin-right: 16px;
      padding: 6px 12px;
      border-radius: 4px;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
        color: #409eff;
      }
    }

    .page-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }

  .form-card {
    .datasource-form {
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

        .section-switch {
          margin-left: 12px;
        }
      }
    }

    .help-icon {
      margin-left: 8px;
      color: #909399;
      cursor: help;
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
