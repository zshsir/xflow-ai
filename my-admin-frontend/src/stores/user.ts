import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api/todo'
import type { User } from '@/types/todo'

export const useUserStore = defineStore('user', () => {
  const users = ref<User[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchUsers() {
    loading.value = true
    error.value = null
    try {
      const { data } = await userApi.getUsers()
      users.value = data
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '获取用户列表失败'
    } finally {
      loading.value = false
    }
  }

  async function createUser(username: string, password: string) {
    try {
      await userApi.createUser({ username, password })
      await fetchUsers()
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '创建用户失败'
      throw e
    }
  }

  async function updateUser(id: number, username: string, password?: string) {
    try {
      await userApi.updateUser(id, { username, password })
      await fetchUsers()
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '更新用户失败'
      throw e
    }
  }

  async function deleteUser(id: number) {
    try {
      await userApi.deleteUser(id)
      await fetchUsers()
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '删除用户失败'
      throw e
    }
  }

  async function resetPassword(id: number, password: string) {
    try {
      await userApi.resetPassword(id, password)
    } catch (e: any) {
      error.value = e.response?.data?.message || e.message || '重置密码失败'
      throw e
    }
  }

  return {
    users,
    loading,
    error,
    fetchUsers,
    createUser,
    updateUser,
    deleteUser,
    resetPassword,
  }
})