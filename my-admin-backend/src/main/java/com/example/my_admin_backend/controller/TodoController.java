package com.example.my_admin_backend.controller;

import com.example.my_admin_backend.dto.QuadrantTodosDTO;
import com.example.my_admin_backend.dto.TodoDTO;
import com.example.my_admin_backend.entity.User;
import com.example.my_admin_backend.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public ResponseEntity<List<QuadrantTodosDTO>> getTodos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.getTodosByDate(date, user.getId()));
    }

    @PostMapping
    public ResponseEntity<TodoDTO> createTodo(
            @Valid @RequestBody TodoDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.createTodo(dto, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoDTO> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.updateTodo(id, dto, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        todoService.deleteTodo(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TodoDTO> completeTodo(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.completeTodo(id, user.getId()));
    }

    @PatchMapping("/{id}/uncomplete")
    public ResponseEntity<TodoDTO> uncompleteTodo(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.uncompleteTodo(id, user.getId()));
    }

    @PatchMapping("/{id}/quadrant")
    public ResponseEntity<TodoDTO> moveToQuadrant(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload,
            @AuthenticationPrincipal User user) {
        Integer newQuadrant = payload.get("quadrant");
        return ResponseEntity.ok(todoService.moveToQuadrant(id, newQuadrant, user.getId()));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTodos(
            @RequestParam String month,
            @AuthenticationPrincipal User user) {
        byte[] excelData = todoService.exportTodos(month, user.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "待办导出_" + month + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<TodoDTO>> getCompletedTodos(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.getCompletedTodos(user.getId()));
    }
}