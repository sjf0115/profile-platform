package com.data.connector.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StatementMetadataFragment {

    private List<String> inputTables;

    private List<String> outputTables;

    private List<ColumnLineage> columnLineageList;
}
