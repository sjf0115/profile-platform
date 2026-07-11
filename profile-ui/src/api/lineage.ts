import request from '@/utils/request'
import type { ApiResponse } from '@/types'
import { ElMessage } from 'element-plus'

export interface LineageNode {
  id: string
  type: string
  name: string
}

export interface LineageEdgeVO {
  source: string
  target: string
  relation: string
  relation_name: string
}

export interface LineageGraph {
  nodes: LineageNode[]
  edges: LineageEdgeVO[]
}

export interface DependentVO {
  downstream_type: string
  downstream_type_name: string
  downstream_id: string
  relation_type: string
  relation_type_name: string
}

export interface ImpactVO {
  node_type: string
  node_id: string
  node_name: string
  depth: number
}

export const lineageApi = {
  /** 查询节点列表（下拉框） */
  getNodes: (params: { node_type: string }) => {
    return request.get<ApiResponse<{ type: string; id: string; name: string }[]>>('/lineage/nodes', { params })
  },

  /** 查询下游依赖（删除保护用） */
  getDependents: (params: { node_type: string; node_id: string }) => {
    return request.get<ApiResponse<DependentVO[]>>('/lineage/dependents', { params })
  },

  /** 血缘图 */
  getGraph: (params: { node_type: string; node_id: string; direction?: string; depth?: number }) => {
    return request.get<ApiResponse<LineageGraph>>('/lineage/graph', { params })
  },

  /** 影响分析 */
  getImpact: (params: { node_type: string; node_id: string }) => {
    return request.get<ApiResponse<ImpactVO[]>>('/lineage/impact', { params })
  },

  /** 全量重建 */
  rebuild: () => {
    return request.post<ApiResponse<number>>('/lineage/rebuild')
  },
}

/**
 * 删除前血缘预检查：查询下游依赖，有依赖则弹出提示并返回 false
 * @returns true 表示可以删除，false 表示有下游依赖不可删除
 */
export async function checkLineageDeletable(nodeType: string, nodeId: string, assetName?: string): Promise<boolean> {
  try {
    const res = await lineageApi.getDependents({ node_type: nodeType, node_id: nodeId })
    const dependents = res.data.data || []
    if (dependents.length === 0) return true

    const name = assetName ? ` "${assetName}"` : ''
    ElMessage.warning(`无法删除${name}：下游有 ${dependents.length} 个依赖，请先解除依赖`)
    return false
  } catch {
    // 接口异常时放行，后端 checkDeletable 会兜底
    return true
  }
}
