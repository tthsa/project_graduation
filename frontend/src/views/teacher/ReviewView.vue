<template>
  <div class="review-management">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>评审结果 - {{ homeworkTitle }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-table :data="submissionList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="提交ID" width="100" />
        <el-table-column prop="studentId" label="学生ID" width="100" />
        <el-table-column prop="submitTime" label="提交时间" width="180">
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
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="提交详情" width="800px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="代码文件" name="files">
          <el-select v-model="selectedFile" placeholder="选择文件" style="margin-bottom: 16px">
            <el-option
              v-for="file in fileList"
              :key="file.id"
              :label="file.fileName"
              :value="file.id"
            />
          </el-select>
          <el-input
            v-model="currentFileContent"
            type="textarea"
            :rows="15"
            readonly
            style="font-family: monospace"
          />
        </el-tab-pane>
        <el-tab-pane label="评测结果" name="result">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="测试分数">
              {{ evaluationResult?.testScore ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="LLM评分">
              {{ evaluationResult?.llmScore ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="执行时间">
              {{ evaluationResult?.executionTime ? `${evaluationResult.executionTime}ms` : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="评测时间">
              {{ formatTime(evaluationResult?.createdAt) }}
            </el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left">LLM 评审意见</el-divider>
          <el-input
            :model-value="evaluationResult?.llmReview || ''"
            type="textarea"
            :rows="10"
            readonly
            style="font-family: monospace"
          />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getSubmissionsByHomework,
  getSubmissionFiles,
  getEvaluationResult,
  type Submission,
  type SubmissionFile,
  type EvaluationResult,
} from '@/api/report'
import { getHomeworkDetail } from '@/api/homework'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submissionList = ref<Submission[]>([])
const homeworkTitle = ref('')

const detailVisible = ref(false)
const activeTab = ref('files')
const currentSubmission = ref<Submission | null>(null)
const fileList = ref<SubmissionFile[]>([])
const selectedFile = ref<number>()
const evaluationResult = ref<EvaluationResult | null>(null)

const currentFileContent = computed(() => {
  const file = fileList.value.find((f) => f.id === selectedFile.value)
  return file?.fileContent || ''
})

const formatTime = (time: string | undefined) => {
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
      return '完成'
    case 3:
      return '失败'
    default:
      return '未知'
  }
}

const fetchSubmissions = async () => {
  const homeworkId = Number(route.params.homeworkId)
  loading.value = true
  try {
    const res = await getSubmissionsByHomework(homeworkId)
    submissionList.value = res || []
  } catch {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

const fetchHomeworkTitle = async () => {
  const homeworkId = Number(route.params.homeworkId)
  try {
    const res = await getHomeworkDetail(homeworkId)
    homeworkTitle.value = res?.title || ''
  } catch {
    // 错误已处理
  }
}

const handleViewDetail = async (row: Submission) => {
  currentSubmission.value = row
  activeTab.value = 'files'
  selectedFile.value = undefined
  fileList.value = []
  evaluationResult.value = null

  try {
    // 获取文件列表
    const files = await getSubmissionFiles(row.id)
    fileList.value = files || []
    if (files && files.length > 0 && files[0]) {
      selectedFile.value = files[0].id
    }

    // 获取评测结果
    if (row.status === 2) {
      const result = await getEvaluationResult(row.id)
      evaluationResult.value = result
    }
  } catch {
    // 错误已处理
  }

  detailVisible.value = true
}

const handleBack = () => {
  router.push('/teacher/homework')
}

onMounted(() => {
  fetchSubmissions()
  fetchHomeworkTitle()
})
</script>

<style scoped>
.review-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
