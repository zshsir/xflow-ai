import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { todoApi } from '@/api/todo'
import type { Todo, QuadrantTodos, Quadrant } from '@/types/todo'
import { QuadrantMeta } from '@/utils/quadrant'
import { getToday } from '@/utils/date'

export const useTodoStore = defineStore('todo', () => {
  // === State ===
  const currentDate = ref(getToday())
  const quadrantTodos = ref<QuadrantTodos[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const isCollapsed = ref(false)
  const isFloatMode = ref(false)

  // === Getters ===
  const todayUncompletedCount = computed(() => {
    return quadrantTodos.value.reduce((sum, q) => sum + q.uncompletedTodos.length, 0)
  })

  const todayCompletedCount = computed(() => {
    return quadrantTodos.value.reduce((sum, q) => sum + q.completedTodos.length, 0)
  })

  const getQuadrantTodos = (quadrant: Quadrant) => {
    return quadrantTodos.value.find(q => q.quadrant === quadrant)
  }

  const quadrants = computed(() => {
    return [1, 2, 3, 4].map(q => {
      const meta = QuadrantMeta[q as Quadrant]
      const data = getQuadrantTodos(q as Quadrant)
      return {
        ...meta,
        quadrant: q as Quadrant,
        uncompletedTodos: data?.uncompletedTodos || [],
        completedTodos: data?.completedTodos || [],
        hasCompleted: (data?.completedTodos?.length || 0) > 0,
      }
    })
  })

  // === Actions ===
  async function fetchTodos(date?: string) {
    loading.value = true
    error.value = null
    try {
      const targetDate = date || currentDate.value
      const { data } = await todoApi.getTodos(targetDate)
      quadrantTodos.value = data
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '获取待办失败'
      console.error('Fetch todos error:', e)
    } finally {
      loading.value = false
    }
  }

  async function createTodo(content: string, quadrant: Quadrant, plannedDate: string | null = null) {
    try {
      await todoApi.createTodo({
        content,
        quadrant,
        dueDate: currentDate.value,
        plannedDate: plannedDate,
      })
      await fetchTodos()
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '创建待办失败'
      throw e
    }
  }

  async function updateTodo(id: number, content: string, plannedDate: string | null = null) {
    try {
      // Find the todo's quadrant
      let quadrant: Quadrant | undefined
      for (const q of quadrantTodos.value) {
        if (q.uncompletedTodos.some(t => t.id === id) || q.completedTodos.some(t => t.id === id)) {
          quadrant = q.quadrant
          break
        }
      }
      if (!quadrant) throw new Error('待办不存在')

      await todoApi.updateTodo(id, {
        content,
        quadrant,
        dueDate: currentDate.value,
        plannedDate: plannedDate,
      })
      await fetchTodos()
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '更新待办失败'
      throw e
    }
  }

  async function deleteTodo(id: number) {
    try {
      await todoApi.deleteTodo(id)
      await fetchTodos()
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '删除待办失败'
      throw e
    }
  }

  async function completeTodo(id: number) {
    try {
      await todoApi.completeTodo(id)
      await fetchTodos()
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '完成任务失败'
      throw e
    }
  }

  async function uncompleteTodo(id: number) {
    try {
      await todoApi.uncompleteTodo(id)
      await fetchTodos()
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '取消完成失败'
      throw e
    }
  }

  async function moveToQuadrant(id: number, newQuadrant: Quadrant) {
    try {
      await todoApi.moveToQuadrant(id, newQuadrant)
      await fetchTodos()
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '移动待办失败'
      throw e
    }
  }

  async function exportTodos(month: string) {
    try {
      const response = await todoApi.exportTodos(month)
      return response.data
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '导出失败'
      throw e
    }
  }

  function setDate(date: string) {
    currentDate.value = date
    fetchTodos(date)
  }

  function toggleCollapse() {
    isCollapsed.value = !isCollapsed.value
    if (!isCollapsed.value) {
      isFloatMode.value = false
    }
  }

  function enterFloatMode() {
    isFloatMode.value = true
    isCollapsed.value = true
  }

  function exitFloatMode() {
    isFloatMode.value = false
  }

  return {
    // State
    currentDate,
    quadrantTodos,
    loading,
    error,
    isCollapsed,
    isFloatMode,
    // Getters
    todayUncompletedCount,
    todayCompletedCount,
    quadrants,
    // Actions
    fetchTodos,
    createTodo,
    updateTodo,
    deleteTodo,
    completeTodo,
    uncompleteTodo,
    moveToQuadrant,
    exportTodos,
    setDate,
    toggleCollapse,
    enterFloatMode,
    exitFloatMode,
  }
})