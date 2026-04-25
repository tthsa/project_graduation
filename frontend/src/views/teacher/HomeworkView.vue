<template>
  <div class="homework-management">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>作业列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            发布作业
          </el-button>
        </div>
      </template>

      <el-table :data="homeworkList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button type="warning" size="small" @click="handleTestCases(row)">
              测试用例
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入作业描述"
          />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="formData.deadline"
            type="datetime"
            placeholder="选择截止时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading"> 确定 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getHomeworkList,
  createHomework,
  updateHomework,
  deleteHomework,
  type Homework,
  type AddHomeworkParams,
  type UpdateHomeworkParams,
} from '@/api/homework'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const homeworkList = ref<Homework[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const formData = reactive({
  id: 0,
  courseId: 1,
  title: '',
  description: '',
  deadline: '',
})

const dialogTitle = computed(() => (isEdit.value ? '编辑作业' : '发布作业'))

const formRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入作业描述', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const fetchHomeworkList = async () => {
  loading.value = true
  try {
    const res = await getHomeworkList()
    homeworkList.value = res || []
  } catch {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: Homework) => {
  isEdit.value = true
  formData.id = row.id
  formData.courseId = row.courseId || 1
  formData.title = row.title
  formData.description = row.description
  formData.deadline = row.deadline ? row.deadline.replace('T', ' ').substring(0, 19) : ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      const params: UpdateHomeworkParams = {
        id: formData.id,
        courseId: formData.courseId,
        title: formData.title,
        description: formData.description,
        deadline: formData.deadline,
        status: 1,
      }
      await updateHomework(params)
      ElMessage.success('更新成功')
    } else {
      const params: AddHomeworkParams = {
        courseId: formData.courseId,
        title: formData.title,
        description: formData.description,
        deadline: formData.deadline,
        status: 1,
      }
      await createHomework(params)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchHomeworkList()
  } catch {
    // 错误已处理
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.id = 0
  formData.title = ''
  formData.description = ''
  formData.deadline = ''
}

const handleDelete = async (row: Homework) => {
  try {
    await ElMessageBox.confirm('确定要删除该作业吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error',
    })
    await deleteHomework(row.id)
    ElMessage.success('删除成功')
    fetchHomeworkList()
  } catch {
    // 取消操作
  }
}

const handleTestCases = (row: Homework) => {
  router.push(`/teacher/homework/${row.id}/testcases`)
}

fetchHomeworkList()
</script>

<style scoped>
.homework-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
