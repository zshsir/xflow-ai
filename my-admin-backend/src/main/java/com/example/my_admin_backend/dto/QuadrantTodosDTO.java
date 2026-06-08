package com.example.my_admin_backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuadrantTodosDTO {

    private Integer quadrant;
    private String quadrantName;
    private String quadrantDescription;
    private List<TodoDTO> uncompletedTodos;
    private List<TodoDTO> completedTodos;
}