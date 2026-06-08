<template>
  <div
    class="todo-card"
    :class="{
      'todo-card--completed': completed,
      'todo-card--dragging': isDragging,
      'todo-card--overdue': isOverdueTodo,
      'todo-card--due-today': isDueTodayTodo,
    }"
    draggable="true"
    @dragstart="onDragStart"
    @dragend="onDragEnd"
  >
    <el-checkbox
      :model-value="completed"
      @change="handleToggle"
      :class="{ 'checkbox--completed': completed }"
    />
    <div class="todo-card__main">
      <span class="todo-card__content" :title="todo.content" @click="handleEdit">
        {{ todo.content }}
      </span>
      <div v-if="todo.plannedDate || isOverdueTodo || isDueTodayTodo" class="todo-card__meta">
        <el-icon
          v-if="isOverdueTodo"
          color="#f56c6c"
        ><Warning /></el-icon>
        <el-icon
          v-else-if="isDueTodayTodo"
          color="#e6a23c"
        ><Bell /></el-icon>
        <span
          v-if="todo.plannedDate"
          class="todo-card__date"
          :class="{
            'todo-card__date--overdue': isOverdueTodo,
            'todo-card__date--today': isDueTodayTodo,
          }"
        >
          计划: {{ todo.plannedDate }}
        </span>
        <span v-if="isOverdueTodo" class="todo-card__overdue-label">已超时</span>
        <span v-else-if="isDueTodayTodo" class="todo-card__today-label">今日到期</span>
      </div>
    </div>
    <el-dropdown trigger="click" @command="handleCommand">
      <el-button text size="small">
        <el-icon><MoreFilled /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="edit">编辑</el-dropdown-item>
          <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Todo } from '@/types/todo'
import { MoreFilled, Warning, Bell } from '@element-plus/icons-vue'
import { isOverdue, isDueToday } from '@/utils/date'

const props = defineProps<{
  todo: Todo
  completed?: boolean
}>()

const emit = defineEmits<{
  complete: [id: number]
  uncomplete: [id: number]
  edit: [todo: Todo]
  delete: [id: number]
  dragstart: [event: DragEvent, todo: Todo]
  dragend: [event: DragEvent]
}>()

const isDragging = ref(false)

const isOverdueTodo = computed(() => {
  if (props.completed) return false
  return isOverdue(props.todo.plannedDate)
})

const isDueTodayTodo = computed(() => {
  if (props.completed) return false
  if (isOverdueTodo.value) return false
  return isDueToday(props.todo.plannedDate)
})

function onDragStart(event: DragEvent) {
  isDragging.value = true
  event.dataTransfer?.setData(
    'application/json',
    JSON.stringify({
      id: props.todo.id,
      quadrant: props.todo.quadrant,
    }),
  )
  emit('dragstart', event, props.todo)
}

function onDragEnd(event: DragEvent) {
  isDragging.value = false
  emit('dragend', event)
}

function handleToggle(value: boolean) {
  if (value) {
    emit('complete', props.todo.id)
  } else {
    emit('uncomplete', props.todo.id)
  }
}

function handleEdit() {
  emit('edit', props.todo)
}

function handleCommand(command: string) {
  if (command === 'edit') {
    emit('edit', props.todo)
  } else if (command === 'delete') {
    emit('delete', props.todo.id)
  }
}
</script>

<style scoped>
.todo-card {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  transition: all 0.2s;
  cursor: grab;
  min-width: 0;
  width: 100%;
  box-sizing: border-box;
  border-left: 3px solid transparent;
}

.todo-card:hover {
  background: var(--el-fill-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.todo-card:active {
  cursor: grabbing;
}

.todo-card--completed {
  background: var(--el-fill-color-lighter);
  opacity: 0.7;
}

.todo-card--completed .todo-card__content {
  text-decoration: line-through;
  color: var(--el-text-color-secondary);
}

.todo-card--dragging {
  opacity: 0.5;
}

.todo-card--overdue {
  background: #fef0f0;
  border-left-color: #f56c6c;
}

.todo-card--overdue:hover {
  background: #fde2e2;
}

.todo-card--due-today {
  background: #fdf6ec;
  border-left-color: #e6a23c;
}

.todo-card--due-today:hover {
  background: #faecd8;
}

.todo-card__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.todo-card__content {
  cursor: pointer;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  word-break: break-all;
}

.todo-card__meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.todo-card__date {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.todo-card__date--overdue {
  color: #f56c6c;
  font-weight: 500;
}

.todo-card__date--today {
  color: #e6a23c;
  font-weight: 500;
}

.todo-card__overdue-label {
  color: #fff;
  background: #f56c6c;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 11px;
  line-height: 16px;
  font-weight: 500;
}

.todo-card__today-label {
  color: #fff;
  background: #e6a23c;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 11px;
  line-height: 16px;
  font-weight: 500;
}

.checkbox--completed :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #909399;
  border-color: #909399;
}
</style>
