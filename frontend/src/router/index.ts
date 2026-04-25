import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/student',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true, role: 'student' },
    children: [
      {
        path: '',
        name: 'StudentHome',
        component: () => import('@/views/student/IndexView.vue'),
        meta: { title: '学生首页' },
      },
    ],
  },
  {
    path: '/teacher',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true, role: 'teacher' },
    children: [
      {
        path: '',
        name: 'TeacherHome',
        component: () => import('@/views/teacher/IndexView.vue'),
        meta: { title: '教师首页' },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: '',
        name: 'AdminHome',
        component: () => import('@/views/admin/IndexView.vue'),
        meta: { title: '管理员首页' },
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/TeacherView.vue'),
        meta: { title: '教师管理' },
      },
    ],
  },
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/LoginView.vue'),
  },
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = (to.meta.title as string) || '毕业设计管理系统'

  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    if (!token) {
      // 未登录，跳转登录页
      next('/login')
    } else {
      // 检查角色权限
      const userRole = userStore.userInfo?.userType
      const requiredRole = to.meta.role as string

      if (requiredRole && userRole !== requiredRole) {
        // 角色不匹配，跳转到对应首页
        switch (userRole) {
          case 'student':
            next('/student')
            break
          case 'teacher':
            next('/teacher')
            break
          case 'admin':
            next('/admin')
            break
          default:
            next('/login')
        }
      } else {
        next()
      }
    }
  } else {
    // 已登录访问登录页，跳转到对应首页
    if (to.path === '/login' && token) {
      const userRole = userStore.userInfo?.userType
      switch (userRole) {
        case 'student':
          next('/student')
          break
        case 'teacher':
          next('/teacher')
          break
        case 'admin':
          next('/admin')
          break
        default:
          next()
      }
    } else {
      next()
    }
  }
})

export default router
