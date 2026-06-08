package com.example.my_admin_backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoDTO {

    private Long id;

    @NotBlank(message = "待办内容不能为空")
    @Size(max = 500, message = "待办内容不能超过500字符")
    private String content;

    @NotNull(message = "象限不能为空")
    @Min(value = 1, message = "象限值必须为1-4")
    @Max(value = 4, message = "象限值必须为1-4")
    private Integer quadrant;

    private Boolean isCompleted;

    private String completedAt;

    @NotNull(message = "日期不能为空")
    private String dueDate;

    private String plannedDate;  // 计划完成时间（可选）
}