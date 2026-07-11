package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 血缘关系边
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageEdge {
    private Long id;
    private String lineageId;
    private String upstreamType;
    private String upstreamId;
    private String downstreamType;
    private String downstreamId;
    private String relationType;
    private Integer sourceType;
    private String remark;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;
}
