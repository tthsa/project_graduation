<template>
  <div class="course-management">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>我的课程</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新建课程
          </el-button>
        </div>
      </template>

      <el-table :data="courseList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="课程名称" min-width="180" />
        <el-table-column prop="classId" label="班级ID" width="120">
          <template #default="{ row }">
            {{ row.classId ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="班级ID" prop="classId">
          <el-input-number v-model="formData.classId" :min="1" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getCourseList,
  addCourse,
  updateCourse,
  deleteCourse,
  type Course,
} from '@/api/course'

const loading = ref(false)
const submitLoading = ref(false)
const courseList = ref<Course[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const formData = reactive<{ id?: number; name: string; classId: number | null }>({
  name: '',
  classId: null,
})

const dialogTitle = computed(() => (isEdit.value ? '编辑课程' : '新建课程'))

const formRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
}

const fetchCourseList = async () => {
  loading.value = true
  try {
    const res = await getCourseList()
    courseList.value = res || []
  } catch {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.classId = null
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Course) => {
  isEdit.value = true
  formData.id = row.id
  formData.name = row.name
  formData.classId = row.classId
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCourse({
        id: formData.id!,
        name: formData.name,
        classId: formData.classId,
      })
      ElMessage.success('更新成功')
    } else {
      await addCourse({
        name: formData.name,
        classId: formData.classId,
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchCourseList()
  } catch {
    // 错误已处理
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

const handleDelete = async (row: Course) => {
  try {
    await ElMessageBox.confirm(`确定删除课程「${row.name}」吗？关联的作业也会失效！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error',
    })
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    fetchCourseList()
  } catch {
    // 取消
  }
}

fetchCourseList()
</script>

<style scoped>
.course-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
