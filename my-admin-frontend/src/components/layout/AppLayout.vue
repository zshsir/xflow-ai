<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'sidebar--collapsed': sidebarCollapsed }">
      <div class="sidebar__logo">
        <el-icon size="24" color="#fff"><Grid /></el-icon>
        <span v-show="!sidebarCollapsed" class="sidebar__title">XFlow 待办</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :collapse-transition="false"
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.85)"
        active-text-color="#409EFF"
        router
        class="sidebar__menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/todos">
          <el-icon><Calendar /></el-icon>
          <template #title>待办事项</template>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>人员管理</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar__footer">
        <el-button text @click="toggleSidebar" class="sidebar__toggle">
          <el-icon size="18">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </el-button>
      </div>
    </aside>

    <!-- 移动端遮罩 -->
    <div
      v-if="isMobile && !sidebarCollapsed"
      class="sidebar-mask"
      @click="toggleSidebar"
    ></div>

    <!-- 主内容区 -->
    <div class="main">
      <header class="header">
        <div class="header__left">
          <el-button
            v-if="isMobile"
            text
            class="header__menu-btn"
            @click="toggleSidebar"
          >
            <el-icon size="20">
              <Menu />
            </el-icon>
          </el-button>
          <h2 class="header__title">{{ pageTitle }}</h2>
        </div>
        <div class="header__right">
          <el-dropdown @command="handleCommand">
            <span class="header__user">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="header__username">{{ username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <!-- <el-dropdown-item command="profile">个人信息</el-dropdown-item> -->
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Grid,
  Calendar,
  User,
  UserFilled,
  Fold,
  Expand,
  ArrowDown,
  Menu,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const sidebarCollapsed = ref(false)
const isMobile = ref(false)
const username = ref(localStorage.getItem('username') || '用户')

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const map: Record<string, string> = {
    '/todos': '待办事项',
    '/users': '人员管理',
  }
  return map[route.path] || ''
})

function checkMobile() {
  isMobile.value = window.innerWidth <= 768
  if (isMobile.value) {
    sidebarCollapsed.value = true
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function handleMenuSelect() {
  // 移动端选中菜单后自动关闭侧边栏
  if (isMobile.value) {
    sidebarCollapsed.value = true
  }
}

async function handleCommand(command: string) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning',
      })
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {}
  }
}
</script>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  background: #001529;
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s, transform 0.3s;
  flex-shrink: 0;
}

.sidebar--collapsed {
  width: 64px;
}

.sidebar__logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  height: 60px;
  box-sizing: border-box;
}

.sidebar__title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar__menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar__menu :deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 6px;
  height: 44px;
  line-height: 44px;
}

.sidebar__menu :deep(.el-menu-item.is-active) {
  background: rgba(64, 158, 255, 0.15);
}

.sidebar__footer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: flex-end;
}

.sidebar__toggle {
  color: rgba(255, 255, 255, 0.65);
}

.sidebar__toggle:hover {
  color: #fff;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f7fa;
  min-width: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.04);
  flex-shrink: 0;
}

.header__title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header__user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.header__user:hover {
  background: #f5f7fa;
}

.header__username {
  font-size: 14px;
  color: #1f2937;
}

.content {
  flex: 1;
  overflow: auto;
  padding: 0;
  min-height: 0;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(0);
  }

  .sidebar--collapsed {
    transform: translateX(-100%);
    width: 220px;
  }

  .sidebar-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.4);
    z-index: 999;
    animation: fadeIn 0.2s;
  }

  @keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
  }

  .main {
    width: 100%;
  }

  .header {
    padding: 0 12px;
    height: 52px;
  }

  .header__left {
    display: flex;
    align-items: center;
    gap: 4px;
    min-width: 0;
  }

  .header__menu-btn {
    padding: 4px;
  }

  .header__title {
    font-size: 16px;
  }

  .header__username {
    display: none;
  }

  .content {
    padding: 0;
  }
}
</style>