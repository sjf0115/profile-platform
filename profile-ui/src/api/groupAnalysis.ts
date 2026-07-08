import request from '@/utils/request'
import type {
  ApiResponse,
  Group,
  AnalysisLabel,
  LabelDistribution,
  DistributionRequest,
  GroupAnalysis,
  GroupAnalysisQueryParams
} from '@/types'

export const groupAnalysisApi = {
  // ============ 群组分析 CRUD ============

  // 获取群组分析列表
  getAnalysisList: (params?: GroupAnalysisQueryParams) => {
    return request.post<ApiResponse<GroupAnalysis[]>>('/group/analysis/list', params || {})
  },

  // 获取群组分析详情
  getAnalysisDetail: (analysisId: string) => {
    return request.get<ApiResponse<GroupAnalysis>>(`/group/analysis/${analysisId}/detail`)
  },

  // 保存群组分析（新增/修改）
  saveAnalysis: (data: GroupAnalysis) => {
    return request.post<ApiResponse<string>>('/group/analysis/save', data)
  },

  // 删除群组分析
  deleteAnalysis: (analysisId: string) => {
    return request.delete<ApiResponse<void>>('/group/analysis/delete', {
      params: { analysis_id: analysisId }
    })
  },

  // ============ 分析计算 ============

  // 获取可分析群组列表
  getGroups: (params?: any) => {
    return request.post<ApiResponse<Group[]>>('/group/analysis/groups', params || {})
  },

  // 获取可分析标签列表
  getLabels: (entity_identifier_id: string) => {
    return request.get<ApiResponse<AnalysisLabel[]>>('/group/analysis/labels', {
      params: { entity_identifier_id }
    })
  },

  // 获取单个标签的分布数据
  getDistribution: (data: DistributionRequest) => {
    return request.post<ApiResponse<LabelDistribution>>('/group/analysis/distribution', data)
  }
}
