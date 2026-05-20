<template>
  <div class="admin-home">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <h2>欢迎回来，{{ userStore.userInfo?.name }}管理员！</h2>
          <p>今天是 {{ currentDate }}，祝您工作顺利！</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #409eff">
            <el-icon size="24"><Avatar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.teacherCount }}</div>
            <div class="stat-label">教师数量</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a">
            <el-icon size="24"><School /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.studentCount }}</div>
            <div class="stat-label">学生数量</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c">
            <el-icon size="24"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount }}</div>
            <div class="stat-label">课程数量</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #f56c6c">
            <el-icon size="24"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.homeworkCount }}</div>
            <div class="stat-label">作业数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 快捷操作 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/user')">
              <el-icon><Avatar /></el-icon>
              教师管理
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 系统信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统信息</span>
          </template>
          <div class="system-info">
            <p><strong>系统名称：</strong>Java 作业评测系统</p>
            <p><strong>系统版本：</strong>v1.0.0</p>
            <p><strong>技术栈：</strong>Vue 3 + Element Plus + Spring Boot</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, onMounted } from 'vue'
import { Document, Avatar, School, Reading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getAdminStatsOverview } from '@/api/admin-stats'

const userStore = useUserStore()

const stats = reactive({
  teacherCount: 0,
  studentCount: 0,
  courseCount: 0,
  homeworkCount: 0,
})

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  })
})

const fetchStats = async () => {
  try {
    const data = await getAdminStatsOverview()
    if (data) {
      stats.teacherCount = data.teacherCount
      stats.studentCount = data.studentCount
      stats.courseCount = data.courseCount
      stats.homeworkCount = data.homeworkCount
    }
  } catch {
    // 错误已处理
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.welcome-card h2 {
  margin: 0 0 10px 0;
}

.welcome-card p {
  margin: 0;
  opacity: 0.9;
}

.stat-card {
  display: flex;
  align-items: center;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  margin-left: 15px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.quick-actions {
  display: flex;
  gap: 10px;
}

.system-info p {
  margin: 10px 0;
  color: #606266;
}
</style>
