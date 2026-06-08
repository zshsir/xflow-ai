<template>
  <div
    class="quadrant"
    :class="[`quadrant--${quadrant.id}`, { 'quadrant--drop-target': isDragOver }]"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDrop"
  >
    <div
      class="quadrant__header"
      :style="{ borderColor: quadrant.color }"
      @click="handleAddClick"
    >
      <el-icon :color="quadrant.color">
        <component :is="getIcon(quadrant.icon)" />
      </el-icon>
      <span class="quadrant__title">{{ quadrant.name }}</span>
      <span class="quadrant__count">{{ quadrant.uncompletedTodos.length }}</span>
      <el-icon class="quadrant__add-icon"><Plus /></el-icon>
    </div>

    <!-- 未完成区域 - 可拖拽 -->
    <VueDraggable
      v-model="uncompletedList"
      class="quadrant__list"
      group="todos"
      :animation="200"
      ghost-class="todo-ghost"
      :item-key="'id'"
      @add="handleAdd"
      @update="handleUpdate"
    >
      <TodoCard
        v-for="todo in uncompletedList"
        :key="todo.id"
        :todo="todo"
        @complete="handleComplete"
        @edit="handleEdit"
        @delete="handleDelete"
      />
    </VueDraggable>

    <!-- 已完成区域 (当天完成，显示灰色) -->
    <template v-if="quadrant.hasCompleted">
      <div class="quadrant__divider">
        <span>已完成 ({{ quadrant.completedTodos.length }})</span>
      </div>
      <div class="quadrant__list quadrant__list--completed">
        <TodoCard
          v-for="todo in quadrant.completedTodos"
          :key="todo.id"
          :todo="todo"
          :completed="true"
          @uncomplete="handleUncomplete"
          @edit="handleEdit"
          @delete="handleDelete"
        />
      </div>
    </template>

    <!-- 空状态 -->
    <div
      v-if="uncompletedList.length === 0 && !quadrant.hasCompleted"
      class="quadrant__empty"
    >
      <span>暂无待办</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import type { Todo } from '@/types/todo'
import TodoCard from './TodoCard.vue'
import { Lightning, Calendar, Clock, Delete, Plus } from '@element-plus/icons-vue'

const iconMap: Record<string, any> = {
  Lightning,
  Calendar,
  Clock,
  Delete,
}

function getIcon(name: string) {
  return iconMap[name] || Calendar
}

interface QuadrantData {
  id: number
  quadrant: number
  name: string
  description: string
  color: string
  icon: string
  quadrantName: string
  quadrantDescription: string
  uncompletedTodos: Todo[]
  completedTodos: Todo[]
  hasCompleted: boolean
}

const props = defineProps<{
  quadrant: QuadrantData
}>()

const emit = defineEmits<{
  complete: [id: number]
  uncomplete: [id: number]
  edit: [todo: Todo]
  delete: [id: number]
  move: [id: number, quadrant: number]
  create: [quadrant: number]
}>()

const isDragOver = ref(false)

const uncompletedList = ref<Todo[]>([...props.quadrant.uncompletedTodos])

watch(
  () => props.quadrant.uncompletedTodos,
  (newVal) => {
    uncompletedList.value = [...newVal]
  },
  { deep: true }
)

function onDragOver() {
  isDragOver.value = true
}

function onDragLeave() {
  isDragOver.value = false
}

function onDrop() {
  isDragOver.value = false
}

function handleAddClick() {
  emit('create', props.quadrant.quadrant)
}

function handleAdd(event: any) {
  // 元素从其他象限移入
  const todo = event.data as Todo
  if (todo && todo.id) {
    if (todo.quadrant !== props.quadrant.quadrant) {
      emit('move', todo.id, props.quadrant.quadrant)
    }
  }
}

function handleUpdate() {
  // 排序变化，不处理
}

function handleComplete(id: number) {
  emit('complete', id)
}

function handleUncomplete(id: number) {
  emit('uncomplete', id)
}

function handleEdit(todo: Todo) {
  emit('edit', todo)
}

function handleDelete(id: number) {
  emit('delete', id)
}
</script>

<style scoped>
.quadrant {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  transition: all 0.2s;
  overflow: hidden;
  min-height: 0;
}

.quadrant--drop-target {
  background: var(--el-fill-color-light);
  box-shadow: inset 0 0 0 2px var(--el-color-primary);
}

.quadrant__header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 2px solid;
  flex-shrink: 0;
  cursor: pointer;
  user-select: none;
  border-radius: 4px;
  transition: all 0.2s;
  padding: 4px 8px 12px;
  margin-left: -8px;
  margin-right: -8px;
  width: calc(100% + 16px);
}

.quadrant__header:hover {
  background: var(--el-fill-color-light);
}

.quadrant__title {
  flex: 1;
  font-weight: 600;
  font-size: 15px;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quadrant__count {
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.quadrant__add-icon {
  color: var(--el-text-color-secondary);
  opacity: 0;
  transition: opacity 0.2s;
  font-size: 16px;
  flex-shrink: 0;
}

.quadrant__header:hover .quadrant__add-icon {
  opacity: 1;
  color: var(--el-color-primary);
}

.quadrant__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 0;
  flex: 1 1 50%;
  overflow-y: auto;
  padding-right: 4px;
}

.quadrant__list--completed {
  opacity: 0.7;
  margin-top: 4px;
  flex: 1 1 50%;
  min-height: 30%;
}

.quadrant__divider {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 0 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.quadrant__divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--el-border-color);
}

.quadrant__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 0;
  color: var(--el-text-color-placeholder);
  font-size: 13px;
}

.todo-ghost {
  opacity: 0.5;
  background: var(--el-color-primary-light-9);
}

.quadrant__list::-webkit-scrollbar {
  width: 6px;
}

.quadrant__list::-webkit-scrollbar-track {
  background: transparent;
}

.quadrant__list::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 3px;
}

.quadrant__list::-webkit-scrollbar-thumb:hover {
  background: var(--el-text-color-placeholder);
}

@media (max-width: 768px) {
  .quadrant {
    padding: 10px;
  }

  .quadrant__header {
    padding: 4px 6px 10px;
  }

  .quadrant__add-icon {
    opacity: 1;
    font-size: 18px;
  }

  .quadrant__title {
    font-size: 14px;
  }
}
</style>