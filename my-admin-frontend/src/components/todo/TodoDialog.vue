<template>
  <el-dialog
    v-model="visible"
    :title="mode === 'create' ? '新建待办' : '编辑待办'"
    :width="dialogWidth"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="right">
      <el-form-item label="内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="3"
          placeholder="请输入待办内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="象限" prop="quadrant">
        <el-select v-model="form.quadrant" placeholder="请选择象限" style="width: 100%">
          <el-option
            v-for="q in quadrants"
            :key="q.id"
            :label="q.name"
            :value="q.id"
          >
            <div class="quadrant-option">
              <span :style="{ color: q.color }">●</span>
              <span>{{ q.name }}</span>
              <span class="quadrant-desc">{{ q.description }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="计划完成时间">
        <el-date-picker
          v-model="plannedDate"
          type="date"
          placeholder="请选择计划完成时间（可选）"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          :clearable="true"
        />
        <div class="form-hint">如果计划完成时间早于今天且未完成，标红提示</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, onMounted, onUnmounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Todo, Quadrant } from '@/types/todo'
import { QuadrantMeta } from '@/utils/quadrant'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  todo?: Todo | null
  defaultQuadrant?: Quadrant
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [data: { content: string; quadrant: Quadrant; plannedDate: string | null }]
}>()

const formRef = ref<FormInstance>()

const form = reactive({
  content: '',
  quadrant: 1 as Quadrant,
})

const plannedDate = ref<string | null>(null)

const isMobile = ref(false)
const dialogWidth = computed(() => isMobile.value ? '90%' : '460px')

function checkMobile() {
  isMobile.value = window.innerWidth <= 768
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

const rules: FormRules = {
  content: [
    { required: true, message: '请输入待办内容', trigger: 'blur' },
    { max: 500, message: '内容不能超过500字符', trigger: 'blur' },
  ],
  quadrant: [
    { required: true, message: '请选择象限', trigger: 'change' },
  ],
}

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const quadrants = computed(() =>
  [1, 2, 3, 4].map(q => ({
    ...QuadrantMeta[q as Quadrant],
    id: q as Quadrant,
  }))
)

watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.mode === 'edit' && props.todo) {
      form.content = props.todo.content
      form.quadrant = props.todo.quadrant
      plannedDate.value = props.todo.plannedDate
    } else {
      form.content = ''
      form.quadrant = props.defaultQuadrant || 1
      plannedDate.value = null
    }
    formRef.value?.clearValidate()
  }
})

function handleClose() {
  visible.value = false
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    emit('submit', {
      content: form.content,
      quadrant: form.quadrant,
      plannedDate: plannedDate.value,
    })
    handleClose()
  } catch {}
}
</script>

<style scoped>
.quadrant-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quadrant-desc {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  margin-left: auto;
}

.form-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

@media (max-width: 768px) {
  :deep(.el-dialog__body) {
    padding: 16px 16px 0;
  }

  :deep(.el-form-item__label) {
    width: 80px !important;
  }

  :deep(.el-form-item) {
    display: flex;
    flex-wrap: wrap;
  }

  :deep(.el-form-item__content) {
    flex: 1;
    min-width: 0;
  }
}
</style>