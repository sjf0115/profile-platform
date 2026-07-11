package com.data.profile.web.controller;

import com.data.profile.web.enums.AssetType;
import com.data.profile.web.enums.RelationType;
import com.data.profile.web.model.*;
import com.data.profile.web.service.LineageService;
import com.data.profile.web.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 血缘关系
 */
@Slf4j
@RestController
@RequestMapping(value = "/lineage", produces = MediaType.APPLICATION_JSON_VALUE)
public class LineageController {

    @Autowired
    private LineageService lineageService;

    /**
     * 按资产类型查询节点列表
     */
    @GetMapping("/nodes")
    public Response<List<NodeRef>> listNodes(@RequestParam("node_type") String nodeType) {
        return Response.success(lineageService.listNodes(nodeType));
    }

    /**
     * 查询下游依赖
     */
    @GetMapping("/dependents")
    public Response<List<DependentVO>> getDependents(@RequestParam("node_type") String nodeType, @RequestParam("node_id") String nodeId) {
        List<LineageEdge> edges = lineageService.findDependents(nodeType, nodeId);
        List<DependentVO> list = edges.stream()
                .map(e -> DependentVO.builder()
                        .downstreamType(e.getDownstreamType())
                        .downstreamTypeName(AssetType.getNameByCode(e.getDownstreamType()))
                        .downstreamId(e.getDownstreamId())
                        .relationType(e.getRelationType())
                        .relationTypeName(RelationType.getNameByCode(e.getRelationType()))
                        .build())
                .collect(Collectors.toList());
        return Response.success(list);
    }

    /**
     * 血缘图
     */
    @GetMapping("/graph")
    public Response<LineageGraphVO> getGraph(@RequestParam("node_type") String nodeType,
                                              @RequestParam("node_id") String nodeId,
                                              @RequestParam(value = "direction", defaultValue = "both") String direction,
                                              @RequestParam(value = "depth", defaultValue = "3") int depth) {
        LineageGraphVO graph = lineageService.getGraph(nodeType, nodeId, direction, Math.min(depth, 10));
        return Response.success(graph);
    }

    /**
     * 影响分析
     */
    @GetMapping("/impact")
    public Response<List<ImpactVO>> getImpact(@RequestParam("node_type") String nodeType, @RequestParam("node_id") String nodeId) {
        List<ImpactVO> impact = lineageService.getImpact(nodeType, nodeId);
        return Response.success(impact);
    }

    /**
     * 全量重建血缘
     */
    @PostMapping("/rebuild")
    public Response<Integer> rebuild() {
        int count = lineageService.rebuildAll();
        return Response.success(count);
    }
}
