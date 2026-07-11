package com.data.profile.web.dao;

import com.data.profile.web.model.LineageEdge;
import com.data.profile.web.model.NodeRef;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LineageEdgeMapper {

    int batchInsert(@Param("list") List<LineageEdge> list);

    int deleteByDownstream(@Param("type") String type, @Param("id") String id);

    int deleteByNode(@Param("type") String type, @Param("id") String id);

    List<LineageEdge> selectByUpstream(@Param("type") String type, @Param("id") String id);

    List<LineageEdge> selectByDownstream(@Param("type") String type, @Param("id") String id);

    List<LineageEdge> selectUpstreamEdgesBatch(@Param("nodes") List<NodeRef> nodes);

    List<LineageEdge> selectDownstreamEdgesBatch(@Param("nodes") List<NodeRef> nodes);

    int deleteAll();
}
