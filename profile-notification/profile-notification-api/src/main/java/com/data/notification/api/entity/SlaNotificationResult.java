package com.data.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode
@ToString
public class SlaNotificationResult implements Serializable {

    private static final long serialVersionUID = -1L;

    private Boolean status;

    private List<SlaNotificationResultRecord> records;

    public SlaNotificationResult(){
        this.status = false;
    }

    public SlaNotificationResult merge(SlaNotificationResult other){
        this.status = this.status && other.status;
        if (Objects.isNull(records)){
            this.records = other.records;
        }
        if (Objects.nonNull(other) && Objects.nonNull(other.getRecords())){
            this.records.addAll(other.getRecords());
        }
        return this;
    }
}
