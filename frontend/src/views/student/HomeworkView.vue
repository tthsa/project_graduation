<template>
  <div class="student-homework">
    <el-card class="table-card">
      <template #header>
        <span>作业列表</span>
      </template>

      <el-table :data="homeworkList" v-loading="loading" stripe border>
        <el-table-column prop="homework.title" label="标题" width="200" />
        <el-table-column prop="homework.description" label="描述" show-overflow-tooltip />
        <el-table-column prop="homework.deadline" label="截止时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.homework.deadline) }}
          </template>
        </el-table-column>
        <el-table-column label="提交状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.submitStatus)">
              {{ getStatusText(row.submitStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分数" width="80">
          <template #default="{ row }">
            {{ row.score ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleSubmit(row)"
              :disabled="row.expired || row.submitStatus === 1"
            >
              提交作业
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="提交作业" width="600px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="作业标题">
          <el-input :value="currentHomework?.homework?.title" disabled />
        </el-form-item>
        <el-form-item label="作业描述">
          <el-input
            :value="currentHomework?.homework?.description"
            type="textarea"
            :rows="3"
            disabled
          />
        </el-form-item>
        <el-form-item label="代码">
          <el-input
            v-model="formData.code"
            type="textarea"
            :rows="10"
            placeholder="请粘贴你的Java代码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSubmit" :loading="submitLoading">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHomeworkListForStudent, type HomeworkWithStatus } from '@/api/homework'
import { submitHomework } from '@/api/submission'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const submitLoading = ref(false)
const homeworkList = ref<HomeworkWithStatus[]>([])
const dialogVisible = ref(false)
const currentHomework = ref<HomeworkWithStatus | null>(null)

const formData = ref({
  code: '',
})

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
    default:
      return 'info'
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 0:
      return '未提交'
    case 1:
      return '已提交'
    case 2:
      return '已批改'
    default:
      return '未知'
  }
}

const fetchHomeworkList = async () => {
  loading.value = true
  try {
    const studentId = userStore.userInfo?.userId
    if (!studentId) return
    const res = await getHomeworkListForStudent(studentId)
    homeworkList.value = res || []
  } catch {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

const handleSubmit = (row: HomeworkWithStatus) => {
  currentHomework.value = row
  formData.value.code = ''
  dialogVisible.value = true
}

const handleConfirmSubmit = async () => {
  if (!formData.value.code.trim()) {
    ElMessage.warning('请输入代码')
    return
  }

  submitLoading.value = true
  try {
    await submitHomework({
      homeworkId: currentHomework.value!.homework.id,
      studentId: userStore.userInfo!.userId,
      code: formData.value.code,
    })
    ElMessage.success('提交成功')
    dialogVisible.value = false
    fetchHomeworkList()
  } catch {
    // 错误已处理
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchHomeworkList()
})
</script>

<style scoped>
.student-homework {
  padding: 0;
}
</style>
