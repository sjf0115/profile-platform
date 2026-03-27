<template>
  <div class="datasource-create-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">
        {{ isEdit ? '编辑' : '创建' }}{{ dataSourceTypeName }}数据源
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

        <!-- 连接信息 - 动态渲染 -->
        <div class="form-section" v-if="pluginParams.length > 0">
          <h3 class="section-title">连接信息</h3>
          
          <template v-for="param in pluginParams" :key="param.field">
            <!-- 输入框类型 -->
            <el-form-item
              v-if="param.type === 'input'"
              :label="param.title"
              :prop="`config.${param.field}`"
              :rules="buildValidationRules(param)"
            >
              <el-input
                v-model="formData.config[param.field]"
                :placeholder="getPlaceholder(param)"
                :type="getInputType(param)"
                :show-password="isPasswordField(param)"
                clearable
              />
            </el-form-item>

            <!-- 文本域类型 -->
            <el-form-item
              v-else-if="param.type === 'textarea'"
              :label="param.title"
              :prop="`config.${param.field}`"
              :rules="buildValidationRules(param)"
            >
              <el-input
                v-model="formData.config[param.field]"
                type="textarea"
                :rows="getTextareaRows(param)"
                :placeholder="getPlaceholder(param)"
                clearable
              />
            </el-form-item>

            <!-- 选择框类型 -->
            <el-form-item
              v-else-if="param.type === 'select'"
              :label="param.title"
              :prop="`config.${param.field}`"
              :rules="buildValidationRules(param)"
            >
              <el-select
                v-model="formData.config[param.field]"
                :placeholder="getPlaceholder(param)"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="option in param.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>

            <!-- 单选框类型 -->
            <el-form-item
              v-else-if="param.type === 'radio'"
              :label="param.title"
              :prop="`config.${param.field}`"
              :rules="buildValidationRules(param)"
            >
              <el-radio-group v-model="formData.config[param.field]">
                <el-radio
                  v-for="option in param.options"
                  :key="option.value"
                  :label="option.value"
                >
                  {{ option.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 开关类型 -->
            <el-form-item
              v-else-if="param.type === 'checkbox'"
              :label="param.title"
              :prop="`config.${param.field}`"
            >
              <el-switch
                v-model="formData.config[param.field]"
                :active-value="true"
                :inactive-value="false"
              />
            </el-form-item>

            <!-- 级联选择类型 -->
            <el-form-item
              v-else-if="param.type === 'cascader'"
              :label="param.title"
              :prop="`config.${param.field}`"
              :rules="buildValidationRules(param)"
            >
              <el-cascader
                v-model="formData.config[param.field]"
                :options="param.options"
                :placeholder="getPlaceholder(param)"
                clearable
                style="width: 100%"
              />
            </el-form-item>

            <!-- 默认输入框（未知类型） -->
            <el-form-item
              v-else
              :label="param.title"
              :prop="`config.${param.field}`"
              :rules="buildValidationRules(param)"
            >
              <el-input
                v-model="formData.config[param.field]"
                :placeholder="getPlaceholder(param)"
                clearable
              />
            </el-form-item>
          </template>
        </div>

        <!-- 加载中 -->
        <el-skeleton v-else-if="loadingConfig" :rows="4" animated />

        <!-- 空状态 -->
        <el-empty v-else description="暂无配置信息" />
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
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { DataSource, PluginParam } from '@/types'
import { dataSourceApi, dataSourceTypeApi } from '@/api/datasource'

const route = useRoute()
const router = useRouter()

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)

// 数据源类型（从 URL 参数获取）
const dataSourceType = computed(() => route.query.type as string)

// 数据源类型名称
const dataSourceTypeName = ref('')

// 数据源 ID
const datasourceId = computed(() => route.params.id as string)

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 测试连接状态
const testing = ref(false)

// 加载配置状态
const loadingConfig = ref(false)

// 插件参数列表（动态表单配置）
const pluginParams = ref<PluginParam[]>([])

// 表单数据
const formData = reactive<{
  datasourceName: string
  datasourceDesc: string
  config: Record<string, any>
}>({
  datasourceName: '',
  datasourceDesc: '',
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
})

// 获取数据源类型配置（动态表单）
const fetchDataSourceConfig = async () => {
  if (!dataSourceType.value) {
    ElMessage.error('未指定数据源类型')
    return
  }
  
  loadingConfig.value = true
  try {
    const res = await dataSourceTypeApi.getConfig(dataSourceType.value)
    let configData = res.data.data
    
    // 后端返回的是 JSON 字符串，需要解析
    if (typeof configData === 'string') {
      try {
        configData = JSON.parse(configData)
      } catch (e) {
        console.error('解析配置数据失败:', e)
        ElMessage.error('配置数据格式错误')
        return
      }
    }
    
    if (configData && Array.isArray(configData)) {
      pluginParams.value = configData
      dataSourceTypeName.value = dataSourceType.value.toUpperCase()
      
      // 初始化配置项默认值
      configData.forEach((param) => {
        if (formData.config[param.field] === undefined) {
          formData.config[param.field] = param.value ?? ''
        }
      })
    } else {
      ElMessage.error('获取数据源配置失败')
    }
  } catch (error) {
    console.error('获取数据源配置失败:', error)
    ElMessage.error('获取数据源配置失败')
  } finally {
    loadingConfig.value = false
  }
}

// 获取数据源详情（编辑模式）
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
        const config = typeof data.config === 'string' 
          ? JSON.parse(data.config) 
          : data.config
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

// 构建验证规则
const buildValidationRules = (param: PluginParam) => {
  const rules: any[] = []
  
  if (param.validate) {
    param.validate.forEach((v) => {
      rules.push({
        required: v.required,
        message: v.message || `请输入${param.title}`,
        trigger: v.trigger || 'blur',
        min: v.min,
        max: v.max,
        pattern: v.pattern ? new RegExp(v.pattern) : undefined,
      })
    })
  }
  
  return rules
}

// 获取占位符文本
const getPlaceholder = (param: PluginParam) => {
  if (param.props && 'placeholder' in param.props) {
    return param.props.placeholder
  }
  return `请输入${param.title}`
}

// 获取输入框类型
const getInputType = (param: PluginParam) => {
  if (param.props && 'type' in param.props) {
    return param.props.type
  }
  return 'text'
}

// 判断是否为密码字段
const isPasswordField = (param: PluginParam) => {
  return getInputType(param) === 'password'
}

// 获取文本域行数
const getTextareaRows = (param: PluginParam) => {
  if (param.props && 'rows' in param.props) {
    return param.props.rows
  }
  return 3
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
        schema_id: dataSourceType.value,
        schema_name: dataSourceTypeName.value,
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
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请完善表单信息后再测试连接')
    return
  }
  
  testing.value = true
  try {
    const res = await dataSourceApi.testConnection({
      type: dataSourceType.value,
      config: formData.config,
    })
    
    if (res.data.code === 0) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.error(res.data.message || '连接失败')
    }
  } catch (error) {
    console.error('测试连接失败:', error)
    ElMessage.error('连接失败')
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  fetchDataSourceConfig()
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
