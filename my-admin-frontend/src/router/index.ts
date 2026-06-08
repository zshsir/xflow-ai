import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import TodoView from '@/views/TodoView.vue'
import LoginView from '@/views/LoginView.vue'
// import RegisterView from '@/views/RegisterView.vue'  // 注册功能已注释，由管理员在人员管理中创建
import UserView from '@/views/UserView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/todos',
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    // {
    //   path: '/register',
    //   name: 'register',
    //   component: RegisterView,
    // },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'todos',
          name: 'todos',
          component: TodoView,
        },
        {
          path: 'users',
          name: 'users',
          component: UserView,
        },
      ],
    },
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/todos')
  } else {
    next()
  }
})

export default router