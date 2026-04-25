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
            <el-icon size="24"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ homeworkCount }}</div>
            <div class="stat-label">作业数量</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a">
            <el-icon size="24"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ studentCount }}</div>
            <div class="stat-label">学生数量</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c">
            <el-icon size="24"><Edit /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">0</div>
            <div class="stat-label">待评审</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background-color: #f56c6c">
            <el-icon size="24"><Bell /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">0</div>
            <div class="stat-label">待办事项</div>
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
            <el-button type="primary" @click="goToHomework">
              <el-icon><Document /></el-icon>
              发布作业
            </el-button>
            <el-button type="success" @click="goToStudent">
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, User, Edit, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getHomeworkList, type Homework } from '@/api/homework'
import { getStudentList } from '@/api/student'

const router = useRouter()
const userStore = useUserStore()

const homeworkCount = ref(0)
const studentCount = ref(0)
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

const goToHomework = () => {
  router.push('/teacher/homework')
}

const goToStudent = () => {
  router.push('/teacher/student')
}

const fetchStats = async () => {
  try {
    const [homeworkList, studentList] = await Promise.all([getHomeworkList(), getStudentList()])
    homeworkCount.value = homeworkList?.length || 0
    studentCount.value = studentList?.length || 0
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
