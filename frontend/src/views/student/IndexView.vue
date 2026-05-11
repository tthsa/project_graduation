<template>
  <div class="student-home">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <h2>欢迎回来，{{ userStore.userInfo?.name }}同学！</h2>
          <p>今天是 {{ currentDate }}，祝你学习愉快！</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 统计卡片 -->
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #409eff">
            <el-icon size="24"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待提交作业</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a">
            <el-icon size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completedCount }}</div>
            <div class="stat-label">已批改作业</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c">
            <el-icon size="24"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.averageScore }}</div>
            <div class="stat-label">平均分</div>
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
            <el-button type="primary" @click="$router.push('/student/homework')">
              <el-icon><Document /></el-icon>
              作业列表
            </el-button>
            <el-button type="success" @click="$router.push('/student/submissions')">
              <el-icon><Folder /></el-icon>
              我的提交
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 待提交作业 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待提交作业</span>
          </template>
          <el-table :data="pendingHomework" v-if="pendingHomework.length > 0">
            <el-table-column label="标题">
              <template #default="{ row }">
                {{ row.homework.title }}
              </template>
            </el-table-column>
            <el-table-column label="截止时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.homework.deadline) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="没有待提交的作业" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Document, TrendCharts, Folder, CircleCheck } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getHomeworkListForStudent, type HomeworkWithStatus } from '@/api/homework'

const userStore = useUserStore()

const stats = reactive({
  pendingCount: 0,
  completedCount: 0,
  averageScore: '-' as string | number,
})
const pendingHomework = ref<HomeworkWithStatus[]>([])

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

const fetchStats = async () => {
  try {
    const list = await getHomeworkListForStudent()
    if (!list) return

    const pending = list.filter((item) => item.submitStatus == null && !item.expired)
    const completed = list.filter((item) => item.submitStatus === 2)
    const scores = list
      .map((item) => item.score)
      .filter((s): s is number => typeof s === 'number')

    stats.pendingCount = pending.length
    stats.completedCount = completed.length
    if (scores.length > 0) {
      const avg = scores.reduce((sum, n) => sum + n, 0) / scores.length
      stats.averageScore = Math.round(avg * 10) / 10
    } else {
      stats.averageScore = '-'
    }

    pendingHomework.value = pending.slice(0, 5)
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
