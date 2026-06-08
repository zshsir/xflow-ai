package com.example.my_admin_backend.service;

import com.example.my_admin_backend.dto.QuadrantTodosDTO;
import com.example.my_admin_backend.dto.TodoDTO;

import java.time.LocalDate;
import java.util.List;

public interface TodoService {
    List<QuadrantTodosDTO> getTodosByDate(LocalDate date, Long userId);
    TodoDTO createTodo(TodoDTO dto, Long userId);
    TodoDTO updateTodo(Long id, TodoDTO dto, Long userId);
    void deleteTodo(Long id, Long userId);
    TodoDTO completeTodo(Long id, Long userId);
    TodoDTO uncompleteTodo(Long id, Long userId);
    TodoDTO moveToQuadrant(Long id, Integer newQuadrant, Long userId);
    byte[] exportTodos(String month, Long userId);
    List<TodoDTO> getCompletedTodos(Long userId);
}