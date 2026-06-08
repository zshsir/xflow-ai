<template>
  <div class="user-view">
    <div class="user-card">
      <div class="user-card__header">
        <div class="user-card__title">
          <el-icon size="20"><User /></el-icon>
          <span>人员列表</span>
          <el-tag size="small" type="info">{{ store.users.length }} 人</el-tag>
        </div>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新增人员
        </el-button>
      </div>

      <div v-loading="store.loading" class="user-card__body">
        <el-table :data="store.users" stripe>
          <el-table-column type="index" label="#" width="60" align="center" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column label="创建时间" min-width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right" align="center">
            <template #default="{ row }">
              <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" @click="openPasswordDialog(row)">重置密码</el-button>
              <el-button
                size="small"
                type="danger"
                :disabled="row.id === currentUserId"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无人员" />
          </template>
        </el-table>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增人员' : '编辑人员'"
      :width="dialogWidth"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" maxlength="20" />
        </el-form-item>
        <el-form-item
          v-if="dialogMode === 'create'"
          label="密码"
          prop="password"
        >
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            maxlength="32"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      :width="dialogWidth"
      @close="handlePasswordDialogClose"
    >
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item label="用户名">
          <el-input :model-value="passwordForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="passwordForm.password"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handlePasswordSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { User, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { User as UserType } from '@/types/todo'
import { formatDate } from '@/utils/date'

const store = useUserStore()

const currentUserId = computed(() => Number(localStorage.getItem('userId') || 0))

const dialogWidth = ref('420px')

function updateDialogWidth() {
  dialogWidth.value = window.innerWidth <= 768 ? '92%' : '420px'
}

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const currentUser = ref<UserType | null>(null)

const form = reactive({
  username: '',
  password: '',
})

const validatePass2 = (_rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' },
  ],
}

// 密码对话框
const passwordDialogVisible = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  userId: 0,
  username: '',
  password: '',
  confirmPassword: '',
})

const passwordRules: FormRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' },
  ],
}

onMounted(() => {
  updateDialogWidth()
  window.addEventListener('resize', updateDialogWidth)
  store.fetchUsers()
})

function openCreateDialog() {
  dialogMode.value = 'create'
  form.username = ''
  form.password = ''
  currentUser.value = null
  dialogVisible.value = true
}

function openEditDialog(user: UserType) {
  dialogMode.value = 'edit'
  form.username = user.username
  form.password = ''
  currentUser.value = user
  dialogVisible.value = true
}

function openPasswordDialog(user: UserType) {
  passwordForm.userId = user.id
  passwordForm.username = user.username
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

function handleDialogClose() {
  formRef.value?.clearValidate()
}

function handlePasswordDialogClose() {
  passwordFormRef.value?.clearValidate()
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true

    if (dialogMode.value === 'create') {
      await store.createUser(form.username, form.password)
      ElMessage.success('创建成功')
    } else if (currentUser.value) {
      await store.updateUser(currentUser.value.id, form.username)
      ElMessage.success('更新成功')
    }

    dialogVisible.value = false
  } catch (e: any) {
    if (e?.message) {
      ElMessage.error(e.message)
    }
  } finally {
    submitting.value = false
  }
}

async function handlePasswordSubmit() {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
    submitting.value = true
    await store.resetPassword(passwordForm.userId, passwordForm.password)
    ElMessage.success('密码重置成功')
    passwordDialogVisible.value = false
  } catch (e: any) {
    if (e?.message) {
      ElMessage.error(e.message)
    }
  } finally {
    submitting.value = false
  }
}

async function handleDelete(user: UserType) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？`,
      '提示',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    await store.deleteUser(user.id)
    ElMessage.success('删除成功')
  } catch {}
}
</script>

<style scoped>
.user-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
}

.user-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.user-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  flex-wrap: wrap;
  gap: 12px;
}

.user-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.user-card__body {
  flex: 1;
  padding: 20px;
  overflow: auto;
}

@media (max-width: 768px) {
  .user-view {
    padding: 8px;
  }

  .user-card__header {
    padding: 12px 16px;
  }

  .user-card__body {
    padding: 8px;
  }
}
</style>