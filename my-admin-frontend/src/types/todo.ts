/** 象限枚举 */
export enum Quadrant {
  IMPORTANT_URGENT = 1,       // 重要且紧急
  IMPORTANT_NOT_URGENT = 2,   // 重要不紧急
  NOT_IMPORTANT_URGENT = 3,   // 紧急不重要
  NOT_IMPORTANT_NOT_URGENT = 4 // 不紧急不重要
}

/** 象限元数据 */
export interface QuadrantMeta {
  id: Quadrant
  name: string
  description: string
  color: string
  icon: string
}

/** 待办项 */
export interface Todo {
  id: number
  content: string
  quadrant: Quadrant
  isCompleted: boolean
  completedAt: string | null
  dueDate: string
  plannedDate: string | null
  createdAt: string
  updatedAt: string
}

/** 象限待办数据 */
export interface QuadrantTodos {
  quadrant: Quadrant
  quadrantName: string
  quadrantDescription: string
  uncompletedTodos: Todo[]
  completedTodos: Todo[]
}

/** 创建/更新待办请求 */
export interface TodoRequest {
  content: string
  quadrant: Quadrant
  dueDate: string
  plannedDate?: string | null
}

/** 导出请求参数 */
export interface ExportParams {
  month: string  // YYYY-MM
}

/** 导出结果 */
export interface ExportResult {
  success: boolean
  completed: Todo[]
  uncompleted: Todo[]
  summary: {
    totalCompleted: number
    totalUncompleted: number
    byQuadrant: Record<Quadrant, { completed: number; uncompleted: number }>
  }
}

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  userId: number
  username: string
}

/** 注册请求 */
export interface RegisterRequest {
  username: string
  password: string
}

/** 用户信息 */
export interface User {
  id: number
  username: string
  createdAt: string
  updatedAt: string
}

/** 用户创建/编辑请求 */
export interface UserRequest {
  username: string
  password?: string
}