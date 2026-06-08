import axios from 'axios'
import type { Todo, TodoRequest, QuadrantTodos, ExportResult, LoginRequest, LoginResponse, RegisterRequest } from '@/types/todo'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

// 请求拦截器 - 添加 token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 处理 token 过期
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Auth API
export const authApi = {
  login: (data: LoginRequest) =>
    api.post<LoginResponse>('/auth/login', data),

  register: (data: RegisterRequest) =>
    api.post<LoginResponse>('/auth/register', data),

  logout: () =>
    api.post('/auth/logout'),
}

// Todo API
export const todoApi = {
  /** 获取指定日期的待办列表 */
  getTodos: (date: string) =>
    api.get<QuadrantTodos[]>('/todos', { params: { date } }),

  /** 创建待办 */
  createTodo: (data: TodoRequest) =>
    api.post<Todo>('/todos', data),

  /** 更新待办 */
  updateTodo: (id: number, data: TodoRequest) =>
    api.put<Todo>(`/todos/${id}`, data),

  /** 删除待办 */
  deleteTodo: (id: number) =>
    api.delete(`/todos/${id}`),

  /** 标记待办完成 */
  completeTodo: (id: number) =>
    api.patch<Todo>(`/todos/${id}/complete`),

  /** 取消完成 */
  uncompleteTodo: (id: number) =>
    api.patch<Todo>(`/todos/${id}/uncomplete`),

  /** 移动到象限 */
  moveToQuadrant: (id: number, quadrant: number) =>
    api.patch<Todo>(`/todos/${id}/quadrant`, { quadrant }),

  /** 导出待办 */
  exportTodos: (month: string) =>
    api.get<ExportResult>(`/todos/export?month=${month}`, {
      responseType: 'blob',
    }),

  /** 获取所有已完成的待办 */
  getCompletedTodos: () =>
    api.get<Todo[]>('/todos/completed'),
}

// User API
export const userApi = {
  /** 获取所有用户 */
  getUsers: () =>
    api.get<User[]>('/users'),

  /** 获取单个用户 */
  getUser: (id: number) =>
    api.get<User>(`/users/${id}`),

  /** 创建用户 */
  createUser: (data: { username: string; password: string }) =>
    api.post<User>('/users', data),

  /** 更新用户 */
  updateUser: (id: number, data: { username: string; password?: string }) =>
    api.put<User>(`/users/${id}`, data),

  /** 删除用户 */
  deleteUser: (id: number) =>
    api.delete(`/users/${id}`),

  /** 重置密码 */
  resetPassword: (id: number, password: string) =>
    api.patch(`/users/${id}/password`, { password }),
}

export default api