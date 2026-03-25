package com.data.profile.common.domain.sql;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ListWithQueryColumn implements Serializable {

    private List<Map<String, Object>> resultList = new ArrayList<>();

    private List<QueryColumn> columns;

    private int pageNumber;

    private int pageSize;

    private long totalCount;
}
