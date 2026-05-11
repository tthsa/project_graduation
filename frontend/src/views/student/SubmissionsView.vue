<template>
  <div class="student-submissions">
    <el-card class="table-card">
      <template #header>
        <span>我的提交记录</span>
      </template>

      <el-table :data="submissionList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="提交ID" width="100" />
        <el-table-column label="作业标题" min-width="180">
          <template #default="{ row }">
            {{ homeworkTitleMap[row.homeworkId] || `作业#${row.homeworkId}` }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="测试分数" width="100">
          <template #default="{ row }">
            {{ scoreMap[row.id]?.testScore ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="LLM分数" width="100">
          <template #default="{ row }">
            {{ scoreMap[row.id]?.llmScore ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="goToDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getEvaluationResult,
  type Submission,
  type EvaluationResult,
} from '@/api/report'
import { getMySubmissions } from '@/api/submission'
import { getHomeworkListForStudent } from '@/api/homework'

const router = useRouter()

const loading = ref(false)
const submissionList = ref<Submission[]>([])
const homeworkTitleMap = ref<Record<number, string>>({})
const scoreMap = ref<Record<number, EvaluationResult | null>>({})

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const getStatusType = (status: number) => {
  switch (status) {
    case 0:
      return 'info'
    case 1:
      return 'warning'
    case 2:
      return 'success'
    case 3:
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 0:
      return '待评测'
    case 1:
      return '评测中'
    case 2:
      return '已完成'
    case 3:
      return '失败'
    default:
      return '未知'
  }
}

const fetchSubmissions = async () => {
  loading.value = true
  try {
    const [submissions, homeworkList] = await Promise.all([
      getMySubmissions(),
      getHomeworkListForStudent(),
    ])
    submissionList.value = submissions || []

    const map: Record<number, string> = {}
    for (const item of homeworkList || []) {
      map[item.homework.id] = item.homework.title
    }
    homeworkTitleMap.value = map

    // 拉取已完成提交的评测结果（用于显示分数）
    const completed = submissionList.value.filter((s) => s.status === 2)
    const results = await Promise.all(
      completed.map((s) =>
        getEvaluationResult(s.id)
          .then((r) => ({ id: s.id, result: r }))
          .catch(() => ({ id: s.id, result: null })),
      ),
    )
    const scores: Record<number, EvaluationResult | null> = {}
    for (const { id, result } of results) {
      scores[id] = result
    }
    scoreMap.value = scores
  } catch {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

const goToDetail = (row: Submission) => {
  router.push(`/student/submission/${row.id}`)
}

onMounted(() => {
  fetchSubmissions()
})
</script>

<style scoped>
.student-submissions {
  padding: 0;
}
</style>
