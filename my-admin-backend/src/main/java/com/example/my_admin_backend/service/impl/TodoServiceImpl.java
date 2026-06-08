package com.example.my_admin_backend.service.impl;

import com.example.my_admin_backend.dto.QuadrantTodosDTO;
import com.example.my_admin_backend.dto.TodoDTO;
import com.example.my_admin_backend.entity.Todo;
import com.example.my_admin_backend.repository.TodoRepository;
import com.example.my_admin_backend.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;

    private static final Map<Integer, String[]> QUADRANT_NAMES = Map.of(
            1, new String[]{"重要且紧急", "立即处理"},
            2, new String[]{"重要不紧急", "计划处理"},
            3, new String[]{"紧急不重要", "快速处理"},
            4, new String[]{"不紧急不重要", "可委托或删除"}
    );

    @Override
    public List<QuadrantTodosDTO> getTodosByDate(LocalDate date, Long userId) {
        List<Todo> todos = todoRepository.findByUserIdAndDueDateOrderByCreatedAtDesc(userId, date);

        List<TodoDTO> completedTodos = todos.stream()
                .filter(Todo::getIsCompleted)
                .map(this::toDTO)
                .collect(Collectors.toList());

        List<TodoDTO> uncompletedTodos = todos.stream()
                .filter(t -> !t.getIsCompleted())
                .map(this::toDTO)
                .collect(Collectors.toList());

        return IntStream.rangeClosed(1, 4)
                .mapToObj(quadrant -> {
                    List<TodoDTO> completed = completedTodos.stream()
                            .filter(t -> t.getQuadrant().equals(quadrant))
                            .collect(Collectors.toList());
                    List<TodoDTO> uncompleted = uncompletedTodos.stream()
                            .filter(t -> t.getQuadrant().equals(quadrant))
                            .collect(Collectors.toList());

                    String[] names = QUADRANT_NAMES.get(quadrant);

                    return QuadrantTodosDTO.builder()
                            .quadrant(quadrant)
                            .quadrantName(names[0])
                            .quadrantDescription(names[1])
                            .completedTodos(completed)
                            .uncompletedTodos(uncompleted)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TodoDTO createTodo(TodoDTO dto, Long userId) {
        Todo todo = Todo.builder()
                .content(dto.getContent())
                .quadrant(dto.getQuadrant())
                .dueDate(LocalDate.parse(dto.getDueDate()))
                .plannedDate(dto.getPlannedDate() != null && !dto.getPlannedDate().isEmpty()
                        ? LocalDate.parse(dto.getPlannedDate()) : null)
                .userId(userId)
                .isCompleted(false)
                .build();

        todo = todoRepository.save(todo);
        return toDTO(todo);
    }

    @Override
    @Transactional
    public TodoDTO updateTodo(Long id, TodoDTO dto, Long userId) {
        Todo todo = todoRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("待办不存在"));

        todo.setContent(dto.getContent());
        todo.setQuadrant(dto.getQuadrant());
        todo.setPlannedDate(dto.getPlannedDate() != null && !dto.getPlannedDate().isEmpty()
                ? LocalDate.parse(dto.getPlannedDate()) : null);

        todo = todoRepository.save(todo);
        return toDTO(todo);
    }

    @Override
    @Transactional
    public void deleteTodo(Long id, Long userId) {
        Todo todo = todoRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("待办不存在"));

        todoRepository.delete(todo);
    }

    @Override
    @Transactional
    public TodoDTO completeTodo(Long id, Long userId) {
        Todo todo = todoRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("待办不存在"));

        todo.setIsCompleted(true);
        todo.setCompletedAt(LocalDateTime.now());

        todo = todoRepository.save(todo);
        return toDTO(todo);
    }

    @Override
    @Transactional
    public TodoDTO uncompleteTodo(Long id, Long userId) {
        Todo todo = todoRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("待办不存在"));

        todo.setIsCompleted(false);
        todo.setCompletedAt(null);

        todo = todoRepository.save(todo);
        return toDTO(todo);
    }

    @Override
    @Transactional
    public TodoDTO moveToQuadrant(Long id, Integer newQuadrant, Long userId) {
        Todo todo = todoRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("待办不存在"));

        todo.setQuadrant(newQuadrant);

        todo = todoRepository.save(todo);
        return toDTO(todo);
    }

    @Override
    public List<TodoDTO> getCompletedTodos(Long userId) {
        return todoRepository.findByUserIdAndIsCompletedTrueOrderByCompletedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportTodos(String month, Long userId) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Todo> todos = todoRepository.findByUserIdAndMonthRange(
                userId, startDate, endDate, startDateTime, endDateTime);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("待办列表");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"日期", "象限", "内容", "计划完成时间", "状态", "完成时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Todo todo : todos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(todo.getDueDate().toString());
                row.createCell(1).setCellValue(QUADRANT_NAMES.get(todo.getQuadrant())[0]);
                row.createCell(2).setCellValue(todo.getContent());
                row.createCell(3).setCellValue(todo.getPlannedDate() != null ?
                        todo.getPlannedDate().toString() : "-");
                row.createCell(4).setCellValue(todo.getIsCompleted() ? "已完成" : "未完成");
                row.createCell(5).setCellValue(todo.getCompletedAt() != null ?
                        todo.getCompletedAt().format(formatter) : "-");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    private TodoDTO toDTO(Todo todo) {
        return TodoDTO.builder()
                .id(todo.getId())
                .content(todo.getContent())
                .quadrant(todo.getQuadrant())
                .isCompleted(todo.getIsCompleted())
                .completedAt(todo.getCompletedAt() != null ? todo.getCompletedAt().toString() : null)
                .dueDate(todo.getDueDate().toString())
                .plannedDate(todo.getPlannedDate() != null ? todo.getPlannedDate().toString() : null)
                .build();
    }
}