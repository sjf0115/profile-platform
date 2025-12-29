package com.data.profile.common.enums;

// 调度作业
public enum SchedulerJobType {
    // 群组计算
    GROUP(1, "GroupJob"),
    // 群组投递
    EXPORT(2, "ExportJob")
    ;

    private Integer code;
    private String name;

    SchedulerJobType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}