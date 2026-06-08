<template>
  <Teleport to="body">
    <div class="todo-float" ref="floatRef">
      <div class="todo-float__header" @mousedown="handleMouseDown">
        <el-icon class="todo-float__drag"><Rank /></el-icon>
        <span class="todo-float__title">待办 ({{ store.todayUncompletedCount }})</span>
        <el-button text size="small" @click="handleExit">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <div class="todo-float__content">
        <div
          v-for="quadrant in store.quadrants"
          :key="quadrant.id"
          class="todo-float__quadrant"
        >
          <div class="todo-float__quadrant-header">
            <span :style="{ color: quadrant.color }">●</span>
            <span>{{ quadrant.name }}</span>
            <span class="todo-float__count">{{ quadrant.uncompletedTodos.length }}</span>
          </div>
          <div class="todo-float__todos">
            <div
              v-for="todo in quadrant.uncompletedTodos.slice(0, 3)"
              :key="todo.id"
              class="todo-float__todo"
              :title="todo.content"
            >
              {{ todo.content }}
            </div>
            <div
              v-if="quadrant.uncompletedTodos.length > 3"
              class="todo-float__more"
            >
              +{{ quadrant.uncompletedTodos.length - 3 }} 更多
            </div>
            <div
              v-if="quadrant.uncompletedTodos.length === 0"
              class="todo-float__empty"
            >
              暂无
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onUnmounted, onMounted } from 'vue'
import { useTodoStore } from '@/stores/todo'
import { Close, Rank } from '@element-plus/icons-vue'

const store = useTodoStore()
const floatRef = ref<HTMLElement>()

let isDragging = false
let startX = 0
let startY = 0
let startTop = 0

function handleMouseDown(e: MouseEvent) {
  if (!floatRef.value) return
  isDragging = true
  startX = e.clientX
  startY = e.clientY

  const rect = floatRef.value.getBoundingClientRect()
  startTop = rect.top

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

function handleMouseMove(e: MouseEvent) {
  if (!isDragging || !floatRef.value) return
  const dy = e.clientY - startY
  const rect = floatRef.value.getBoundingClientRect()
  const newTop = Math.max(0, Math.min(window.innerHeight - rect.height, startTop + dy))
  floatRef.value.style.top = `${newTop}px`
}

function handleMouseUp() {
  isDragging = false
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
}

function handleExit() {
  store.exitFloatMode()
}

onMounted(() => {
  // 加载最新数据
  store.fetchTodos()
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
})
</script>

<style scoped>
.todo-float {
  position: fixed;
  right: 20px;
  top: 100px;
  width: 280px;
  max-height: 400px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: floatIn 0.3s ease;
}

@keyframes floatIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.todo-float__header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color);
  background: var(--el-fill-color-light);
  cursor: move;
  user-select: none;
}

.todo-float__drag {
  color: var(--el-text-color-secondary);
}

.todo-float__title {
  flex: 1;
  font-weight: 600;
  font-size: 14px;
}

.todo-float__content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.todo-float__quadrant {
  margin-bottom: 12px;
}

.todo-float__quadrant:last-child {
  margin-bottom: 0;
}

.todo-float__quadrant-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 500;
}

.todo-float__count {
  margin-left: auto;
  background: var(--el-fill-color);
  padding: 1px 6px;
  border-radius: 8px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.todo-float__todos {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.todo-float__todo {
  font-size: 12px;
  padding: 6px 10px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-float__more,
.todo-float__empty {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  padding: 4px 10px;
}
</style>