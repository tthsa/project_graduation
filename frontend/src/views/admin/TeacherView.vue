<template>
  <div class="teacher-management">
    <!-- 操作栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>教师列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加教师
          </el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table :data="teacherList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="teacherNo" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="电话" width="150" />
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
        <el-form-item label="工号" prop="teacherNo">
          <el-input v-model="formData.teacherNo" placeholder="请输入工号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入电话" />
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
  getTeacherList,
  addTeacher,
  updateTeacher,
  deleteTeacher,
  type Teacher,
  type AddTeacherParams,
  type UpdateTeacherParams,
} from '@/api/teacher'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 表格数据
const teacherList = ref<Teacher[]>([])

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 表单数据
const formData = reactive<AddTeacherParams & { id?: number; status?: number }>({
  teacherNo: '',
  name: '',
  email: '',
  phone: '',
  password: '',
  status: 1,
})

// 对话框标题
const dialogTitle = computed(() => (isEdit.value ? '编辑教师' : '添加教师'))

// 表单验证规则
const formRules = {
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

// 获取教师列表
const fetchTeacherList = async () => {
  loading.value = true
  try {
    const res = await getTeacherList()
    teacherList.value = res || []
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
const handleEdit = (row: Teacher) => {
  isEdit.value = true
  formData.id = row.id
  formData.teacherNo = row.teacherNo
  formData.name = row.name
  formData.email = row.email
  formData.phone = row.phone
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
      const params: UpdateTeacherParams = {
        id: formData.id!,
        teacherNo: formData.teacherNo,
        name: formData.name,
        email: formData.email,
        phone: formData.phone,
        status: formData.status ?? 1,
      }
      await updateTeacher(params)
      ElMessage.success('更新成功')
    } else {
      // 添加
      const params: AddTeacherParams = {
        teacherNo: formData.teacherNo,
        name: formData.name,
        email: formData.email,
        phone: formData.phone,
        password: formData.password,
      }
      await addTeacher(params)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchTeacherList()
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
  formData.teacherNo = ''
  formData.name = ''
  formData.email = ''
  formData.phone = ''
  formData.password = ''
  formData.status = 1
}

// 删除
const handleDelete = async (row: Teacher) => {
  try {
    await ElMessageBox.confirm('确定要删除该教师吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error',
    })
    await deleteTeacher(row.id)
    ElMessage.success('删除成功')
    fetchTeacherList()
  } catch {
    // 取消操作
  }
}

// 初始化
fetchTeacherList()
</script>

<style scoped>
.teacher-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
