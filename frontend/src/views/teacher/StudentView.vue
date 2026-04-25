<template>
  <div class="student-management">
    <!-- 操作栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>学生列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加学生
          </el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table :data="studentList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="classId" label="班级ID" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="formData.studentNo" placeholder="请输入学号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="班级ID" prop="classId">
          <el-input-number v-model="formData.classId" :min="1" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getStudentList,
  addStudent,
  updateStudent,
  deleteStudent,
  type Student,
  type AddStudentParams,
  type UpdateStudentParams,
} from '@/api/student'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 表格数据
const studentList = ref<Student[]>([])

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 表单数据
const formData = reactive<AddStudentParams & { id?: number }>({
  studentNo: '',
  name: '',
  email: '',
  classId: 1,
  password: '',
  status: 1,
  firstLogin: 1,
})

// 对话框标题
const dialogTitle = computed(() => (isEdit.value ? '编辑学生' : '添加学生'))

// 表单验证规则
const formRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  classId: [{ required: true, message: '请输入班级ID', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

// 获取学生列表
const fetchStudentList = async () => {
  loading.value = true
  try {
    const res = await getStudentList()
    studentList.value = res || []
  } catch {
    // 错误已在 request.ts 中处理
  } finally {
    loading.value = false
  }
}

// 添加
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Student) => {
  isEdit.value = true
  formData.id = row.id
  formData.studentNo = row.studentNo
  formData.name = row.name
  formData.email = row.email
  formData.classId = row.classId
  formData.status = row.status
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      // 编辑
      const params: UpdateStudentParams = {
        id: formData.id!,
        name: formData.name,
        email: formData.email,
        classId: formData.classId,
        status: formData.status,
      }
      await updateStudent(params)
      ElMessage.success('更新成功')
    } else {
      // 添加
      const params: AddStudentParams = {
        studentNo: formData.studentNo,
        name: formData.name,
        email: formData.email,
        classId: formData.classId,
        password: formData.password,
        status: 1,
        firstLogin: 1,
      }
      await addStudent(params)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchStudentList()
  } catch {
    // 错误已在 request.ts 中处理
  } finally {
    submitLoading.value = false
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.id = undefined
  formData.studentNo = ''
  formData.name = ''
  formData.email = ''
  formData.classId = 1
  formData.password = ''
  formData.status = 1
  formData.firstLogin = 1
}

// 删除
const handleDelete = async (row: Student) => {
  try {
    await ElMessageBox.confirm('确定要删除该学生吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error',
    })
    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    fetchStudentList()
  } catch {
    // 取消操作
  }
}

// 初始化
fetchStudentList()
</script>

<style scoped>
.student-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
