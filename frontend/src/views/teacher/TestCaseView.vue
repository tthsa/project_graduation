<template>
  <div class="testcase-management">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button @click="handleBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="title">作业：{{ homeworkTitle }}</span>
          </div>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加测试用例
          </el-button>
        </div>
      </template>

      <el-table :data="testcaseList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="input" label="输入" show-overflow-tooltip />
        <el-table-column prop="expectedOutput" label="预期输出" show-overflow-tooltip />
        <el-table-column prop="isPublic" label="公开" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isPublic === 1 ? 'success' : 'info'">
              {{ row.isPublic === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)"> 编辑 </el-button>
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
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入测试用例名称" />
        </el-form-item>
        <el-form-item label="输入" prop="input">
          <el-input
            v-model="formData.input"
            type="textarea"
            :rows="4"
            placeholder="请输入测试输入"
          />
        </el-form-item>
        <el-form-item label="预期输出" prop="expectedOutput">
          <el-input
            v-model="formData.expectedOutput"
            type="textarea"
            :rows="4"
            placeholder="请输入预期输出"
          />
        </el-form-item>
        <el-form-item label="是否公开" prop="isPublic">
          <el-radio-group v-model="formData.isPublic">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft } from '@element-plus/icons-vue'
import {
  getTestCaseList,
  createTestCase,
  updateTestCase,
  deleteTestCase,
  type TestCase,
} from '@/api/testcase'
import { getHomeworkDetail } from '@/api/homework'

const route = useRoute()
const router = useRouter()

const homeworkId = computed(() => Number(route.params.homeworkId))

const loading = ref(false)
const submitLoading = ref(false)
const testcaseList = ref<TestCase[]>([])
const homeworkTitle = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const formData = reactive({
  id: 0,
  homeworkId: 0,
  name: '',
  input: '',
  expectedOutput: '',
  isPublic: 0,
  sortOrder: 0,
})

const dialogTitle = computed(() => (isEdit.value ? '编辑测试用例' : '添加测试用例'))

const formRules = {
  name: [{ required: true, message: '请输入测试用例名称', trigger: 'blur' }],
  input: [{ required: true, message: '请输入测试输入', trigger: 'blur' }],
  expectedOutput: [{ required: true, message: '请输入预期输出', trigger: 'blur' }],
}

const fetchHomework = async () => {
  try {
    const res = await getHomeworkDetail(homeworkId.value)
    homeworkTitle.value = res?.title || ''
  } catch {
    // 错误已处理
  }
}

const fetchTestcaseList = async () => {
  loading.value = true
  try {
    const res = await getTestCaseList(homeworkId.value)
    testcaseList.value = res || []
  } catch {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push('/teacher/homework')
}

const handleAdd = () => {
  isEdit.value = false
  formData.homeworkId = homeworkId.value
  dialogVisible.value = true
}

const handleEdit = (row: TestCase) => {
  isEdit.value = true
  formData.id = row.id
  formData.homeworkId = row.homeworkId
  formData.name = row.name
  formData.input = row.input
  formData.expectedOutput = row.expectedOutput
  formData.isPublic = row.isPublic
  formData.sortOrder = row.sortOrder
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateTestCase(formData)
      ElMessage.success('更新成功')
    } else {
      await createTestCase(formData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchTestcaseList()
  } catch {
    // 错误已处理
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.id = 0
  formData.homeworkId = 0
  formData.name = ''
  formData.input = ''
  formData.expectedOutput = ''
  formData.isPublic = 0
  formData.sortOrder = 0
}

const handleDelete = async (row: TestCase) => {
  try {
    await ElMessageBox.confirm('确定要删除该测试用例吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error',
    })
    await deleteTestCase(row.id)
    ElMessage.success('删除成功')
    fetchTestcaseList()
  } catch {
    // 取消操作
  }
}

onMounted(() => {
  fetchHomework()
  fetchTestcaseList()
})
</script>

<style scoped>
.testcase-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title {
  font-size: 16px;
  font-weight: 500;
}
</style>
