package com.data.connector.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ColumnLineage {

    private List<String> inputColumns;

    private List<String> outputColumns;
}
