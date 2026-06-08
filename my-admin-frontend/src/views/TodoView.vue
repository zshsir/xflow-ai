<template>
  <div class="todo-view">
    <TodoPanel />
  </div>
</template>

<script setup lang="ts">
import TodoPanel from '@/components/todo/TodoPanel.vue'
</script>

<style scoped>
.todo-view {
  height: 100%;
  padding: 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

@media (max-width: 768px) {
  .todo-view {
    padding: 8px;
  }
}
</style>