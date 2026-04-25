import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
      {
        path: 'homework',
        name: 'StudentHomework',
        component: () => import('@/views/student/HomeworkView.vue'),
        meta: { title: '作业列表' },
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
      {
        path: 'student',
        name: 'TeacherStudent',
        component: () => import('@/views/teacher/StudentView.vue'),
        meta: { title: '学生管理' },
      },
      {
        path: 'homework',
        name: 'TeacherHomework',
        component: () => import('@/views/teacher/HomeworkView.vue'),
        meta: { title: '作业管理' },
      },
      {
        path: 'homework/:homeworkId/testcases',
        name: 'TeacherTestCase',
        component: () => import('@/views/teacher/TestCaseView.vue'),
        meta: { title: '测试用例管理' },
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

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title as string) || '毕业设计管理系统'

  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')

  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
    } else {
      const userRole = userStore.userInfo?.userType
      const requiredRole = to.meta.role as string

      if (requiredRole && userRole !== requiredRole) {
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
