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
        <el-table-column label="所属课程" width="160">
          <template #default="{ row }">
            {{ courseNameMap[row.courseId] || `课程#${row.courseId}` }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="deadline" label="截止时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="320">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button type="success" size="small" @click="handleReview(row)"> 评审结果 </el-button>
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
      width="680px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="所属课程" prop="courseId">
          <el-select
            v-model="formData.courseId"
            placeholder="请选择课程"
            style="width: 100%"
            :disabled="isEdit"
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
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
        <el-collapse>
          <el-collapse-item title="评分配置（可选）" name="scoring">
            <div class="scoring-section">
              <div class="section-title">权重配置（总和必须为 100）</div>
              <div class="weight-row">
                <el-form-item label="测试权重" prop="testWeight" class="inline-form-item">
                  <el-input-number
                    v-model="formData.testWeight"
                    :min="0"
                    :max="100"
                    controls-position="right"
                  />
                </el-form-item>
                <el-form-item label="LLM 权重" prop="llmWeight" class="inline-form-item">
                  <el-input-number
                    v-model="formData.llmWeight"
                    :min="0"
                    :max="100"
                    controls-position="right"
                  />
                </el-form-item>
              </div>
              <div class="section-title" style="margin-top: 12px">
                等级阈值（A &gt; B &gt; C ≥ 0）
              </div>
              <div class="weight-row">
                <el-form-item label="A 阈值" prop="gradeAThreshold" class="inline-form-item">
                  <el-input-number
                    v-model="formData.gradeAThreshold"
                    :min="0"
                    :max="100"
                    controls-position="right"
                  />
                </el-form-item>
                <el-form-item label="B 阈值" prop="gradeBThreshold" class="inline-form-item">
                  <el-input-number
                    v-model="formData.gradeBThreshold"
                    :min="0"
                    :max="100"
                    controls-position="right"
                  />
                </el-form-item>
                <el-form-item label="C 阈值" prop="gradeCThreshold" class="inline-form-item">
                  <el-input-number
                    v-model="formData.gradeCThreshold"
                    :min="0"
                    :max="100"
                    controls-position="right"
                  />
                </el-form-item>
              </div>
              <div class="hint">
                综合分 = 测试分 × 测试权重 + LLM 分 × 10 × LLM 权重（再 / 100）。最终分 ≥ A 阈值得 A,
                依次类推, 否则 D。
              </div>
              <div class="section-title" style="margin-top: 16px">
                LLM 评分维度（各项权重之和 = 100, 最多 5 项）
              </div>
              <el-form-item prop="llmDimensions" :show-message="true" class="dim-form-item">
                <div class="dim-list">
                  <div
                    v-for="(d, idx) in formData.llmDimensions"
                    :key="idx"
                    class="dim-row"
                  >
                    <el-input
                      v-model="d.name"
                      placeholder="维度名(如:代码质量)"
                      style="width: 200px"
                    />
                    <el-input-number
                      v-model="d.weight"
                      :min="0"
                      :max="100"
                      controls-position="right"
                    />
                    <span class="dim-unit">%</span>
                    <el-button
                      type="danger"
                      :icon="Delete"
                      size="small"
                      plain
                      @click="removeDimension(idx)"
                      :disabled="formData.llmDimensions.length <= 1"
                    />
                  </div>
                  <div class="dim-footer">
                    <el-button
                      type="primary"
                      :icon="Plus"
                      size="small"
                      plain
                      @click="addDimension"
                      :disabled="formData.llmDimensions.length >= 5"
                    >
                      添加维度
                    </el-button>
                    <span class="dim-sum" :class="{ 'sum-ok': dimensionWeightSum === 100 }">
                      权重和: {{ dimensionWeightSum }}
                    </span>
                  </div>
                </div>
              </el-form-item>
            </div>
          </el-collapse-item>
        </el-collapse>
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
import { Plus, Delete } from '@element-plus/icons-vue'
import {
  getHomeworkList,
  createHomework,
  updateHomework,
  deleteHomework,
  type Homework,
  type AddHomeworkParams,
  type UpdateHomeworkParams,
} from '@/api/homework'
import { getCourseList, type Course } from '@/api/course'

interface LlmDimensionInput {
  name: string
  weight: number
}

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const homeworkList = ref<Homework[]>([])
const courseList = ref<Course[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const formData = reactive<{
  id: number
  courseId: number | null
  title: string
  description: string
  deadline: string
  testWeight: number
  llmWeight: number
  gradeAThreshold: number
  gradeBThreshold: number
  gradeCThreshold: number
  llmDimensions: LlmDimensionInput[]
}>({
  id: 0,
  courseId: null,
  title: '',
  description: '',
  deadline: '',
  testWeight: 70,
  llmWeight: 30,
  gradeAThreshold: 90,
  gradeBThreshold: 75,
  gradeCThreshold: 60,
  llmDimensions: [{ name: '代码质量', weight: 100 }],
})

const dialogTitle = computed(() => (isEdit.value ? '编辑作业' : '发布作业'))

const courseNameMap = computed<Record<number, string>>(() => {
  const map: Record<number, string> = {}
  for (const c of courseList.value) {
    map[c.id] = c.name
  }
  return map
})

const dimensionWeightSum = computed(() =>
  formData.llmDimensions.reduce((sum, d) => sum + (d.weight ?? 0), 0),
)

const validateWeightSum = (_rule: unknown, _value: unknown, callback: (e?: Error) => void) => {
  const sum = (formData.testWeight ?? 0) + (formData.llmWeight ?? 0)
  if (sum !== 100) {
    callback(new Error(`测试权重 + LLM 权重必须等于 100, 当前为 ${sum}`))
  } else {
    callback()
  }
}

const validateGradeOrder = (_rule: unknown, _value: unknown, callback: (e?: Error) => void) => {
  const { gradeAThreshold: a, gradeBThreshold: b, gradeCThreshold: c } = formData
  if (a == null || b == null || c == null) {
    callback(new Error('A/B/C 等级阈值必填'))
    return
  }
  if (!(a > b && b > c)) {
    callback(new Error(`等级阈值必须满足 A > B > C, 当前 A=${a} B=${b} C=${c}`))
    return
  }
  callback()
}

const validateDimensions = (
  _rule: unknown,
  _value: unknown,
  callback: (e?: Error) => void,
) => {
  if (formData.llmDimensions.length === 0) {
    callback(new Error('至少配置 1 个 LLM 评分维度'))
    return
  }
  for (const d of formData.llmDimensions) {
    if (!d.name || !d.name.trim()) {
      callback(new Error('维度名称不能为空'))
      return
    }
    if (d.weight == null || d.weight < 0 || d.weight > 100) {
      callback(new Error('维度权重必须在 0-100 之间'))
      return
    }
  }
  if (dimensionWeightSum.value !== 100) {
    callback(new Error(`各维度权重之和必须等于 100, 当前为 ${dimensionWeightSum.value}`))
    return
  }
  callback()
}

const formRules = {
  courseId: [{ required: true, message: '请选择所属课程', trigger: 'change' }],
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入作业描述', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  testWeight: [{ validator: validateWeightSum, trigger: 'change' }],
  llmWeight: [{ validator: validateWeightSum, trigger: 'change' }],
  gradeAThreshold: [{ validator: validateGradeOrder, trigger: 'change' }],
  gradeBThreshold: [{ validator: validateGradeOrder, trigger: 'change' }],
  gradeCThreshold: [{ validator: validateGradeOrder, trigger: 'change' }],
  llmDimensions: [{ validator: validateDimensions, trigger: 'change' }],
}

const addDimension = () => {
  if (formData.llmDimensions.length >= 5) {
    ElMessage.warning('最多 5 个维度')
    return
  }
  formData.llmDimensions.push({ name: '', weight: 0 })
}

const removeDimension = (idx: number) => {
  if (formData.llmDimensions.length <= 1) {
    ElMessage.warning('至少保留 1 个维度')
    return
  }
  formData.llmDimensions.splice(idx, 1)
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const fetchCourseList = async () => {
  try {
    const res = await getCourseList()
    courseList.value = res || []
  } catch {
    // 错误已处理
  }
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
  if (courseList.value.length === 0) {
    ElMessage.warning('请先在「我的课程」中创建一门课程')
    return
  }
  dialogVisible.value = true
}

const handleEdit = (row: Homework) => {
  isEdit.value = true
  formData.id = row.id
  formData.courseId = row.courseId
  formData.title = row.title
  formData.description = row.description
  formData.deadline = row.deadline ? row.deadline.replace('T', ' ').substring(0, 19) : ''
  formData.testWeight = row.testWeight ?? 70
  formData.llmWeight = row.llmWeight ?? 30
  formData.gradeAThreshold = row.gradeAThreshold ?? 90
  formData.gradeBThreshold = row.gradeBThreshold ?? 75
  formData.gradeCThreshold = row.gradeCThreshold ?? 60
  formData.llmDimensions = parseDimensionsOrDefault(row.llmDimensions)
  dialogVisible.value = true
}

const parseDimensionsOrDefault = (json: string | null): LlmDimensionInput[] => {
  if (!json) return [{ name: '代码质量', weight: 100 }]
  try {
    const parsed = JSON.parse(json) as LlmDimensionInput[]
    if (Array.isArray(parsed) && parsed.length > 0) {
      return parsed.map((d) => ({ name: d.name ?? '', weight: d.weight ?? 0 }))
    }
  } catch {
    // 忽略解析错误,落到默认
  }
  return [{ name: '代码质量', weight: 100 }]
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const llmDimensions = JSON.stringify(formData.llmDimensions)
    if (isEdit.value) {
      const params: UpdateHomeworkParams = {
        id: formData.id,
        courseId: formData.courseId!,
        title: formData.title,
        description: formData.description,
        deadline: formData.deadline,
        status: 1,
        testWeight: formData.testWeight,
        llmWeight: formData.llmWeight,
        gradeAThreshold: formData.gradeAThreshold,
        gradeBThreshold: formData.gradeBThreshold,
        gradeCThreshold: formData.gradeCThreshold,
        llmDimensions,
      }
      await updateHomework(params)
      ElMessage.success('更新成功')
    } else {
      const params: AddHomeworkParams = {
        courseId: formData.courseId!,
        title: formData.title,
        description: formData.description,
        deadline: formData.deadline,
        status: 1,
        testWeight: formData.testWeight,
        llmWeight: formData.llmWeight,
        gradeAThreshold: formData.gradeAThreshold,
        gradeBThreshold: formData.gradeBThreshold,
        gradeCThreshold: formData.gradeCThreshold,
        llmDimensions,
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
  formData.courseId = null
  formData.title = ''
  formData.description = ''
  formData.deadline = ''
  formData.testWeight = 70
  formData.llmWeight = 30
  formData.gradeAThreshold = 90
  formData.gradeBThreshold = 75
  formData.gradeCThreshold = 60
  formData.llmDimensions = [{ name: '代码质量', weight: 100 }]
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

const handleReview = (row: Homework) => {
  router.push(`/teacher/homework/${row.id}/review`)
}

fetchCourseList()
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

.scoring-section {
  padding: 4px 0;
}

.section-title {
  font-size: 13px;
  color: var(--el-text-color-regular);
  margin-bottom: 8px;
  font-weight: 500;
}

.weight-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.inline-form-item {
  margin-bottom: 12px;
}

.inline-form-item :deep(.el-form-item__label) {
  width: auto !important;
  padding-right: 8px;
}

.hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.dim-form-item {
  margin-bottom: 0;
}

.dim-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.dim-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dim-unit {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-left: -4px;
}

.dim-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 4px;
}

.dim-sum {
  font-size: 12px;
  color: var(--el-color-danger);
}

.dim-sum.sum-ok {
  color: var(--el-color-success);
}
</style>
