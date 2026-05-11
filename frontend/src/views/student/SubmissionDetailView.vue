<template>
  <div class="submission-detail" v-loading="loading">
    <el-page-header @back="goBack" content="提交详情" />

    <el-card style="margin-top: 16px">
      <template #header>
        <span>基本信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="提交ID">{{ submission?.id }}</el-descriptions-item>
        <el-descriptions-item label="作业ID">{{ submission?.homeworkId }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatTime(submission?.submitTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(submission?.status)">
            {{ getStatusText(submission?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="测试分数">
          {{ evaluation?.testScore ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="LLM分数">
          {{ evaluation?.llmScore ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="执行耗时(ms)" :span="2">
          {{ evaluation?.executionTime ?? '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 16px" v-if="evaluation?.llmReview">
      <template #header>
        <span>LLM 评审</span>
      </template>
      <div class="llm-review markdown-body" v-html="renderedReview"></div>
    </el-card>

    <el-card style="margin-top: 16px" v-if="files.length > 0">
      <template #header>
        <span>提交的代码文件</span>
      </template>
      <el-tabs v-model="activeFileTab">
        <el-tab-pane
          v-for="file in files"
          :key="file.id"
          :label="file.fileName"
          :name="String(file.id)"
        >
          <pre class="code-block">{{ file.fileContent }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MarkdownIt from 'markdown-it'
import {
  getEvaluationResult,
  getSubmissionFiles,
  type Submission,
  type EvaluationResult,
  type SubmissionFile,
} from '@/api/report'
import { getMySubmissionDetail } from '@/api/submission'

const route = useRoute()
const router = useRouter()

const md = new MarkdownIt({ html: false, linkify: true, breaks: false })

const loading = ref(false)
const submission = ref<Submission | null>(null)
const evaluation = ref<EvaluationResult | null>(null)
const files = ref<SubmissionFile[]>([])
const activeFileTab = ref('')

const renderedReview = computed(() => {
  const src = evaluation.value?.llmReview
  if (!src) return ''
  return md.render(src)
})

const formatTime = (time: string | undefined | null) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const getStatusType = (status: number | undefined) => {
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

const getStatusText = (status: number | undefined) => {
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

const goBack = () => {
  router.back()
}

const fetchDetail = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const [s, fs] = await Promise.all([
      getMySubmissionDetail(id),
      getSubmissionFiles(id),
    ])
    submission.value = s
    files.value = fs || []
    const first = files.value[0]
    if (first) {
      activeFileTab.value = String(first.id)
    }

    if (s?.status === 2) {
      try {
        evaluation.value = await getEvaluationResult(id)
      } catch {
        evaluation.value = null
      }
    }
  } catch {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.submission-detail {
  padding: 0;
}

.llm-review {
  background-color: #f5f7fa;
  padding: 16px 20px;
  border-radius: 4px;
  line-height: 1.7;
  color: #303133;
}

.llm-review :deep(h1),
.llm-review :deep(h2),
.llm-review :deep(h3),
.llm-review :deep(h4) {
  margin: 16px 0 8px;
  color: #303133;
  font-weight: 600;
}
.llm-review :deep(h1) { font-size: 20px; }
.llm-review :deep(h2) { font-size: 18px; }
.llm-review :deep(h3) { font-size: 16px; }
.llm-review :deep(h4) { font-size: 15px; }

.llm-review :deep(p) {
  margin: 8px 0;
}

.llm-review :deep(ul),
.llm-review :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.llm-review :deep(li) {
  margin: 4px 0;
}

.llm-review :deep(strong) {
  color: #303133;
  font-weight: 600;
}

.llm-review :deep(code) {
  background-color: #ecf0f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-size: 0.9em;
}

.llm-review :deep(pre) {
  background-color: #282c34;
  color: #abb2bf;
  padding: 12px;
  border-radius: 4px;
  overflow: auto;
  margin: 8px 0;
}

.llm-review :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
}

.llm-review :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding: 4px 12px;
  margin: 8px 0;
  background-color: #ecf5ff;
  color: #606266;
}

.code-block {
  white-space: pre-wrap;
  word-break: break-word;
  font-family:
    ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  background-color: #282c34;
  color: #abb2bf;
  padding: 12px;
  border-radius: 4px;
  max-height: 600px;
  overflow: auto;
  margin: 0;
}
</style>
