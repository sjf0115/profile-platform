package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 血缘图 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageGraphVO {
    @Builder.Default
    private List<LineageNodeVO> nodes = new ArrayList<>();
    @Builder.Default
    private List<LineageEdgeVO> edges = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineageNodeVO {
        private String id;
        private String type;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineageEdgeVO {
        private String source;
        private String target;
        private String relation;
        private String relationName;
    }
}
