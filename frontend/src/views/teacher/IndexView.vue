<template>
  <div class="teacher-home">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <h2>欢迎回来，{{ userStore.userInfo?.name }}老师！</h2>
          <p>今天是 {{ currentDate }}，祝您工作顺利！</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #409eff">
            <el-icon size="24"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount }}</div>
            <div class="stat-label">我的课程</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a">
            <el-icon size="24"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.homeworkCount }}</div>
            <div class="stat-label">我的作业</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c">
            <el-icon size="24"><Edit /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待评测提交</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #f56c6c">
            <el-icon size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completedCount }}</div>
            <div class="stat-label">已完成评测</div>
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
            <el-button type="primary" @click="goToCourse">
              <el-icon><Reading /></el-icon>
              我的课程
            </el-button>
            <el-button type="success" @click="goToHomework">
              <el-icon><Document /></el-icon>
              发布作业
            </el-button>
            <el-button type="warning" @click="goToStudent">
              <el-icon><User /></el-icon>
              学生管理
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 最近作业 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近作业</span>
          </template>
          <el-table :data="recentHomework" v-if="recentHomework.length > 0">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="deadline" label="截止时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.deadline) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无作业" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, User, Edit, Reading, CircleCheck } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getHomeworkList, type Homework } from '@/api/homework'
import { getTeacherStatsOverview, type TeacherStatsOverview } from '@/api/teacher-stats'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive<TeacherStatsOverview>({
  courseCount: 0,
  homeworkCount: 0,
  pendingCount: 0,
  completedCount: 0,
})
const recentHomework = ref<Homework[]>([])

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  })
})

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const goToCourse = () => {
  router.push('/teacher/course')
}

const goToHomework = () => {
  router.push('/teacher/homework')
}

const goToStudent = () => {
  router.push('/teacher/student')
}

const fetchStats = async () => {
  try {
    const [overview, homeworkList] = await Promise.all([
      getTeacherStatsOverview(),
      getHomeworkList(),
    ])
    if (overview) {
      stats.courseCount = overview.courseCount
      stats.homeworkCount = overview.homeworkCount
      stats.pendingCount = overview.pendingCount
      stats.completedCount = overview.completedCount
    }
    recentHomework.value = homeworkList?.slice(0, 5) || []
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
</style>
