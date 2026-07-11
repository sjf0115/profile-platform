package com.data.profile.web.service.lineage;

import com.data.profile.web.model.LineageEdge;

import java.util.List;

/**
 * 血缘抽取器接口
 */
public interface LineageExtractor {

    /**
     * 该抽取器处理的资产类型
     */
    String assetType();

    /**
     * 抽取该资产依赖的上游边
     * @param assetId 资产业务ID
     * @return 上游边列表（downstream 为该资产自身）
     */
    List<LineageEdge> extractUpstreams(String assetId);
}
