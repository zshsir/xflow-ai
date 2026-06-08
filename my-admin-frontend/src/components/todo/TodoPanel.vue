<template>
  <div class="todo-panel">
    <div class="todo-toolbar">
      <div class="todo-toolbar__left">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          size="default"
          @change="handleDateChange"
        />
        <el-button-group>
          <el-button @click="goToday">今天</el-button>
          <el-button @click="goPrev">前一天</el-button>
          <el-button @click="goNext">后一天</el-button>
        </el-button-group>
      </div>
      <div class="todo-toolbar__right">
        <el-button @click="openCompletedDialog">
          <el-icon><Finished /></el-icon>
          查看已完成
        </el-button>
        <!-- <el-button @click="store.toggleCollapse">
          <el-icon><Aim /></el-icon>
          悬浮显示
        </el-button> -->
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出当月
        </el-button>
        <el-button type="primary" @click="openCreateDialog(1)">
          <el-icon><Plus /></el-icon>
          新建待办
        </el-button>
      </div>
    </div>

    <div v-loading="store.loading" class="todo-grid">
      <TodoQuadrant
        v-for="quadrant in store.quadrants"
        :key="quadrant.id"
        :quadrant="quadrant"
        @complete="store.completeTodo"
        @uncomplete="store.uncompleteTodo"
        @edit="openEditDialog"
        @delete="handleDelete"
        @move="handleMove"
        @create="openCreateDialog"
      />
    </div>

    <!-- 新建/编辑对话框 -->
    <TodoDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :todo="currentTodo"
      :default-quadrant="defaultQuadrant"
      @submit="handleDialogSubmit"
    />

    <!-- 悬浮窗 -->
    <TodoFloat v-if="store.isFloatMode" />

    <!-- 已完成待办对话框 -->
    <el-dialog
      v-model="completedDialogVisible"
      title="已完成待办"
      :width="completedDialogWidth"
      top="6vh"
    >
      <div v-loading="completedLoading" class="completed-list">
        <div v-if="completedTodos.length === 0" class="completed-list__empty">
          <el-empty description="暂无已完成的待办" />
        </div>
        <el-table v-else :data="completedTodos" stripe>
          <el-table-column label="完成时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.completedAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="所属日期" width="120" />
          <el-table-column label="象限" width="120">
            <template #default="{ row }">
              <span :style="{ color: getQuadrantColor(row.quadrant) }">
                {{ getQuadrantName(row.quadrant) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="180" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Aim, Finished } from '@element-plus/icons-vue'
import { useTodoStore } from '@/stores/todo'
import { todoApi } from '@/api/todo'
import type { Todo, Quadrant } from '@/types/todo'
import { getToday, formatDate } from '@/utils/date'
import { QuadrantMeta } from '@/utils/quadrant'
import TodoQuadrant from './TodoQuadrant.vue'
import TodoDialog from './TodoDialog.vue'
import TodoFloat from './TodoFloat.vue'

const store = useTodoStore()

const selectedDate = ref(store.currentDate)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentTodo = ref<Todo | null>(null)
const defaultQuadrant = ref<Quadrant>(1)

// 已完成对话框
const completedDialogVisible = ref(false)
const completedTodos = ref<Todo[]>([])
const completedLoading = ref(false)
const completedDialogWidth = ref('720px')

function updateDialogWidth() {
  completedDialogWidth.value = window.innerWidth <= 768 ? '92%' : '720px'
}

function formatDateTime(dateStr: string | null): string {
  if (!dateStr) return '-'
  // 后端返回的是 "2024-01-15T10:30:00" 格式
  return dateStr.replace('T', ' ').substring(0, 16)
}

function getQuadrantName(quadrant: number): string {
  return QuadrantMeta[quadrant as Quadrant]?.name || '-'
}

function getQuadrantColor(quadrant: number): string {
  return QuadrantMeta[quadrant as Quadrant]?.color || '#909399'
}

async function openCompletedDialog() {
  updateDialogWidth()
  completedDialogVisible.value = true
  completedLoading.value = true
  try {
    const { data } = await todoApi.getCompletedTodos()
    completedTodos.value = data
  } catch (e: any) {
    ElMessage.error(e.message || '获取已完成待办失败')
  } finally {
    completedLoading.value = false
  }
}

onMounted(() => {
  store.fetchTodos()
})

function handleDateChange(date: string) {
  store.setDate(date)
}

function goToday() {
  const today = getToday()
  selectedDate.value = today
  store.setDate(today)
}

function goPrev() {
  const d = new Date(selectedDate.value)
  d.setDate(d.getDate() - 1)
  const date = formatDate(d)
  selectedDate.value = date
  store.setDate(date)
}

function goNext() {
  const d = new Date(selectedDate.value)
  d.setDate(d.getDate() + 1)
  const date = formatDate(d)
  selectedDate.value = date
  store.setDate(date)
}

function openCreateDialog(quadrant: Quadrant) {
  defaultQuadrant.value = quadrant
  currentTodo.value = null
  dialogMode.value = 'create'
  dialogVisible.value = true
}

function openEditDialog(todo: Todo) {
  currentTodo.value = todo
  dialogMode.value = 'edit'
  dialogVisible.value = true
}

async function handleDialogSubmit(data: {
  content: string
  quadrant: Quadrant
  plannedDate: string | null
}) {
  try {
    if (dialogMode.value === 'create') {
      await store.createTodo(data.content, data.quadrant, data.plannedDate)
      ElMessage.success('创建成功')
    } else if (currentTodo.value) {
      await store.updateTodo(currentTodo.value.id, data.content, data.plannedDate)
      ElMessage.success('更新成功')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这条待办吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await store.deleteTodo(id)
    ElMessage.success('删除成功')
  } catch {}
}

function handleMove(id: number, newQuadrant: number) {
  store.moveToQuadrant(id, newQuadrant as Quadrant)
}

async function handleExport() {
  try {
    const month = store.currentDate.substring(0, 7)
    const blob = await store.exportTodos(month)

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `待办导出_${month}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (e: any) {
    ElMessage.error(e.message || '导出失败')
  }
}
</script>

<style scoped>
.todo-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;
}

.todo-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.todo-toolbar__left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.todo-toolbar__right {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.todo-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, minmax(0, 1fr));
  gap: 16px;
  min-height: 0;
  overflow: hidden;
}

.todo-grid > * {
  min-height: 0;
  min-width: 0;
  overflow: hidden;
}

.completed-list {
  max-height: 60vh;
  overflow-y: auto;
}

.completed-list__empty {
  padding: 40px 0;
}

@media (max-width: 900px) {
  .todo-grid {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(4, minmax(200px, auto));
    overflow-y: auto;
  }

  .todo-toolbar {
    padding: 8px 0;
  }
}

@media (max-width: 768px) {
  .todo-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .todo-toolbar__left,
  .todo-toolbar__right {
    width: 100%;
    justify-content: space-between;
  }

  .todo-toolbar :deep(.el-button) {
    font-size: 12px;
    padding: 8px 12px;
  }

  .todo-toolbar :deep(.el-date-editor) {
    width: 100% !important;
  }
}
</style>
