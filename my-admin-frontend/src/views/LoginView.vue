<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="login-icon">
          <el-icon size="32" color="#fff"><Grid /></el-icon>
        </div>
        <h1>XFlow 待办</h1>
        <p>欢迎登录</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
        <!-- <div class="login-footer">
          还没有账号？请联系管理员开通
        </div> -->
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Grid, User, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api/todo'
import type { LoginRequest } from '@/types/todo'

const router = useRouter()

const formRef = ref()
const loading = ref(false)

const form = reactive<LoginRequest>({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
    loading.value = true
    const { data } = await authApi.login(form)
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    localStorage.setItem('userId', String(data.userId))
    ElMessage.success('登录成功')
    router.push('/todos')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// function goToRegister() {
//   router.push('/register')
// }
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  box-sizing: border-box;
}

.login-box {
  width: 400px;
  max-width: 100%;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  margin-bottom: 16px;
}

.login-header h1 {
  margin: 0 0 8px;
  color: #1f2937;
  font-size: 24px;
}

.login-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.login-footer {
  text-align: center;
  color: #6b7280;
  font-size: 13px;
}

@media (max-width: 480px) {
  .login-box {
    padding: 24px;
  }

  .login-icon {
    width: 56px;
    height: 56px;
  }

  .login-header h1 {
    font-size: 20px;
  }
}
</style>