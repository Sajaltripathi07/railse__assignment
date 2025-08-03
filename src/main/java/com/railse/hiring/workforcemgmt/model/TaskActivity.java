package com.railse.hiring.workforcemgmt.model;

import lombok.Data;

@Data
public class TaskActivity {
    private Long id;
    private Long taskId;
    private Long userId;
    private String activityType;
    private String description;
    private Long timestamp;
}
