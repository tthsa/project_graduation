<template>
  <div class="review-management">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>评审结果 - {{ homeworkTitle }}</span>
          <div class="header-actions">
            <el-button
              type="primary"
              :disabled="!hasPending || batchLoading"
              :loading="batchLoading"
              @click="handleTriggerBatch"
            >
              评测全部待评测 ({{ pendingCount }})
            </el-button>
            <el-button @click="fetchSubmissions" :disabled="loading">刷新</el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-table :data="submissionList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="提交ID" width="90" />
        <el-table-column prop="studentId" label="学生ID" width="90" />
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="测试分" width="80">
          <template #default="{ row }">
            {{ scoreMap[row.id]?.testScore ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="LLM分" width="80">
          <template #default="{ row }">
            {{ scoreMap[row.id]?.llmScore ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="综合分" width="90">
          <template #default="{ row }">
            <strong v-if="scoreMap[row.id]?.finalScore != null">
              {{ scoreMap[row.id]?.finalScore }}
            </strong>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="80">
          <template #default="{ row }">
            <el-tag
              v-if="scoreMap[row.id]?.grade"
              :type="getGradeType(scoreMap[row.id]?.grade)"
              effect="dark"
              size="small"
            >
              {{ scoreMap[row.id]?.grade }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              :loading="itemLoading[row.id]"
              @click="handleTriggerOne(row)"
            >
              开始评测
            </el-button>
            <el-button
              v-else-if="row.status === 3"
              type="warning"
              size="small"
              :loading="itemLoading[row.id]"
              @click="handleTriggerOne(row)"
            >
              重新评测
            </el-button>
            <el-button v-else-if="row.status === 1" size="small" disabled>
              评测中...
            </el-button>
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
            <el-descriptions-item label="综合分">
              <span v-if="evaluationResult?.finalScore != null">
                <strong>{{ evaluationResult.finalScore }}</strong> / 100
              </span>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="等级">
              <el-tag
                v-if="evaluationResult?.grade"
                :type="getGradeType(evaluationResult.grade)"
                effect="dark"
              >
                {{ evaluationResult.grade }}
              </el-tag>
              <span v-else>-</span>
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
import { ElMessage } from 'element-plus'
import {
  getSubmissionsByHomework,
  getSubmissionFiles,
  getEvaluationResult,
  type Submission,
  type SubmissionFile,
  type EvaluationResult,
} from '@/api/report'
import { getHomeworkDetail } from '@/api/homework'
import { triggerEvaluation, triggerBatchEvaluation } from '@/api/evaluation'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const batchLoading = ref(false)
const itemLoading = ref<Record<number, boolean>>({})
const submissionList = ref<Submission[]>([])
const homeworkTitle = ref('')
const scoreMap = ref<Record<number, EvaluationResult | null>>({})

const pendingCount = computed(
  () => submissionList.value.filter((s) => s.status === 0 || s.status === 3).length,
)
const hasPending = computed(() => pendingCount.value > 0)

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

const getGradeType = (grade: 'A' | 'B' | 'C' | 'D' | null | undefined) => {
  switch (grade) {
    case 'A':
      return 'success'
    case 'B':
      return 'warning'
    case 'C':
      return 'info'
    case 'D':
      return 'danger'
    default:
      return 'info'
  }
}

const fetchSubmissions = async () => {
  const homeworkId = Number(route.params.homeworkId)
  loading.value = true
  try {
    const res = await getSubmissionsByHomework(homeworkId)
    submissionList.value = res || []

    // 预取已完成提交的评测结果用于表格展示
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

const handleTriggerOne = async (row: Submission) => {
  itemLoading.value[row.id] = true
  try {
    await triggerEvaluation(row.id)
    ElMessage.success('已开始评测')
    await fetchSubmissions()
  } catch {
    // 拦截器已弹错
  } finally {
    itemLoading.value[row.id] = false
  }
}

const handleTriggerBatch = async () => {
  const homeworkId = Number(route.params.homeworkId)
  batchLoading.value = true
  try {
    const stats = await triggerBatchEvaluation(homeworkId)
    ElMessage.success(`已触发 ${stats.triggered} 条,跳过 ${stats.skipped} 条`)
    await fetchSubmissions()
  } catch {
    // 拦截器已弹错
  } finally {
    batchLoading.value = false
  }
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

.header-actions {
  display: flex;
  gap: 8px;
}
</style>
