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
        <StatCard
          :icon="Reading"
          :value="stats.courseCount"
          label="我的课程"
          color="#409eff"
        />
      </el-col>

      <el-col :span="6">
        <StatCard
          :icon="Document"
          :value="stats.homeworkCount"
          label="我的作业"
          color="#67c23a"
        />
      </el-col>

      <el-col :span="6">
        <StatCard
          :icon="Edit"
          :value="stats.pendingCount"
          label="待评测提交"
          color="#e6a23c"
        />
      </el-col>

      <el-col :span="6">
        <StatCard
          :icon="CircleCheck"
          :value="stats.completedCount"
          label="已完成评测"
          color="#f56c6c"
        />
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
import { formatTime } from '@/utils/format'
import StatCard from '@/components/StatCard.vue'

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

.quick-actions {
  display: flex;
  gap: 10px;
}
</style>