<template>
  <div class="application-create-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">{{ isEdit ? '编辑应用' : '创建应用' }}</h2>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="application-form"
      >
        <!-- 基础信息 -->
        <div class="form-section">
          <h3 class="section-title">基础信息</h3>

          <el-form-item label="应用名称" prop="app_name">
            <el-input
              v-model="formData.app_name"
              placeholder="请输入应用名称"
              maxlength="100"
              show-word-limit
              style="width: 400px"
            />
          </el-form-item>

          <el-form-item label="应用描述">
            <el-input
              v-model="formData.app_desc"
              type="textarea"
              :rows="3"
              placeholder="请输入应用描述"
              maxlength="200"
              show-word-limit
              style="width: 400px"
            />
          </el-form-item>

          <el-form-item label="负责人">
            <el-select
              v-model="formData.owner"
              filterable
              clearable
              placeholder="请选择负责人（默认为当前用户）"
              style="width: 400px"
            >
              <el-option
                v-for="u in userOptions"
                :key="u.user_id"
                :label="u.user_name"
                :value="u.user_id"
              />
            </el-select>
          </el-form-item>
        </div>

        <!-- 投递目标配置（共享组件） -->
        <div class="form-section">
          <TargetConfigForm
            v-model="targetConfigData"
            :show-title="true"
            :optional="true"
            ref="targetConfigRef"
          />
        </div>

        <!-- 高级配置 -->
        <div class="form-section">
          <h3 class="section-title">
            高级配置
            <span class="optional-tag">可选</span>
          </h3>

          <el-form-item label="Webhook 地址">
            <el-input
              v-model="formData.webhook_url"
              placeholder="请输入回调通知地址"
              clearable
              style="width: 400px"
            />
          </el-form-item>

          <el-form-item label="频率限制">
            <el-input-number
              v-model="formData.rate_limit"
              :min="1"
              :max="10000"
              placeholder="次/分钟"
              style="width: 200px"
            />
            <span class="form-help">次/分钟，默认 100</span>
          </el-form-item>

          <el-form-item label="IP 白名单">
            <el-input
              v-model="formData.ip_whitelist"
              type="textarea"
              :rows="2"
              placeholder="多个 IP 用逗号分隔，为空则不限制"
              style="width: 400px"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ApplicationRequest } from '@/types'
import { applicationApi } from '@/api/application'
import { userApi } from '@/api/user'
import type { User } from '@/api/user'
import { getLoginUser } from '@/utils/auth'
import TargetConfigForm from '@/components/TargetConfigForm.vue'

const route = useRoute()
const router = useRouter()

// 是否编辑模式
const isEdit = computed(() => !!route.params.appKey)

// 表单引用
const formRef = ref<FormInstance>()

// 投递目标配置组件引用
const targetConfigRef = ref<InstanceType<typeof TargetConfigForm>>()
const targetConfigData = ref<Record<string, any>>({})

// 提交状态
const submitting = ref(false)

// 用户列表（负责人选择）
const userOptions = ref<User[]>([])

// 表单数据
const formData = reactive<{
  app_name: string
  app_desc: string
  owner: string
  webhook_url: string
  rate_limit: number
  ip_whitelist: string
}>({
  app_name: '',
  app_desc: '',
  owner: '',
  webhook_url: '',
  rate_limit: 100,
  ip_whitelist: '',
})

// 表单校验规则
const formRules = reactive<FormRules>({
  app_name: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' },
  ],
})

// 获取用户列表（负责人选择）
const fetchUserOptions = async () => {
  try {
    const res = await userApi.getList()
    userOptions.value = res.data.data || []
  } catch {
    userOptions.value = []
  }
}

// 获取应用详情（编辑模式）
const fetchDetail = async () => {
  if (!isEdit.value) return
  const appKey = route.params.appKey as string
  if (!appKey) return

  try {
    const res = await applicationApi.getDetail(appKey)
    const data = res.data.data
    if (!data) {
      ElMessage.error('应用不存在')
      return
    }

    // 回填基础信息
    formData.app_name = data.app_name
    formData.app_desc = data.app_desc || ''
    formData.owner = data.owner || ''
    formData.webhook_url = data.webhook_url || ''
    formData.rate_limit = data.rate_limit ?? 100
    formData.ip_whitelist = data.ip_whitelist || ''

    // 解析并回填 target_config
    if (data.target_config) {
      try {
        const config = JSON.parse(data.target_config)
        targetConfigData.value = config
        await targetConfigRef.value?.loadConfig(config)
      } catch (e) {
        console.error('解析 target_config 失败:', e)
      }
    }
  } catch (error) {
    console.error('获取应用详情失败:', error)
    ElMessage.error('获取应用信息失败')
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 构建 target_config
const buildTargetConfig = (): string | undefined => {
  if (!targetConfigData.value?.datasource_id) return undefined
  return JSON.stringify(targetConfigData.value)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const params: ApplicationRequest = {
        app_name: formData.app_name,
        app_desc: formData.app_desc || undefined,
        owner: formData.owner || undefined,
        webhook_url: formData.webhook_url || undefined,
        rate_limit: formData.rate_limit,
        ip_whitelist: formData.ip_whitelist || undefined,
        target_config: buildTargetConfig(),
      }

      if (isEdit.value) {
        const appKey = route.params.appKey as string
        await applicationApi.update(appKey, params)
        ElMessage.success('保存成功')
        router.push('/application')
      } else {
        const res = await applicationApi.create(params)
        // 创建成功，通过 sessionStorage 传递凭证信息到列表页弹窗（避免 history.state 被路由缓存导致反复弹出）
        const savedApp = res.data.data
        sessionStorage.setItem('createdApp', JSON.stringify({
          app_key: savedApp?.app_key,
          app_secret: savedApp?.app_secret,
        }))
        router.push('/application')
      }
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  fetchUserOptions()
  if (isEdit.value) {
    fetchDetail()
  } else {
    // 创建模式：默认填充当前登录用户为负责人
    const currentUser = getLoginUser()
    if (currentUser?.user_id) {
      formData.owner = currentUser.user_id
    }
  }
})
</script>

<style scoped lang="scss">
.application-create-page {
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
    .application-form {
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

        .optional-tag {
          font-size: 12px;
          font-weight: 400;
          color: #909399;
          margin-left: 8px;
          background: #f5f7fa;
          padding: 2px 8px;
          border-radius: 4px;
        }
      }
    }

    .form-help {
      margin-left: 12px;
      color: #909399;
      font-size: 13px;
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
