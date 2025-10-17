<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, Input, Toggle } from '@xcan-angus/vue-ui';
import { issue } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { TaskDetail } from '@/views/issue/types';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  taskId: undefined
});

const { t } = useI18n();


const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
}>();

// Editing state
const evalWorkloadInputRef = ref();
const isEvalWorkloadEditing = ref(false);
const evalWorkloadInputValue = ref<number>();

const actualWorkloadInputRef = ref();
const isActualWorkloadEditing = ref(false);
const actualWorkloadInputValue = ref<number>();

// Computed values
const currentEvalWorkload = computed(() => props.dataSource?.evalWorkload);
const currentActualWorkload = computed(() => props.dataSource?.actualWorkload);

// Methods - Eval workload
const startEvalWorkloadEditing = () => {
  evalWorkloadInputValue.value = currentEvalWorkload.value as any;
  isEvalWorkloadEditing.value = true;
  nextTick(() => {
    setTimeout(() => {
      if (typeof evalWorkloadInputRef.value?.focus === 'function') {
        evalWorkloadInputRef.value?.focus();
      }
    }, 100);
  });
};

const handleEvalWorkloadBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newValue = Number(target?.value);
  if (newValue === currentEvalWorkload.value) {
    isEvalWorkloadEditing.value = false;
    return;
  }
  emit('loadingChange', true);
  const [error] = await issue.editEvalWorkloadApi(props.taskId || props.dataSource.id, { workload: newValue });
  emit('loadingChange', false);
  isEvalWorkloadEditing.value = false;
  if (error) {
    return;
  }
  emit('change', { id: props.taskId, evalWorkload: newValue });
};

const handleEvalWorkloadEnter = () => {
  if (typeof evalWorkloadInputRef.value?.blur === 'function') {
    evalWorkloadInputRef.value.blur();
  }
};

// Methods - Actual workload
const startActualWorkloadEditing = () => {
  actualWorkloadInputValue.value = currentActualWorkload.value as any;
  isActualWorkloadEditing.value = true;
  nextTick(() => {
    setTimeout(() => {
      if (typeof actualWorkloadInputRef.value?.focus === 'function') {
        actualWorkloadInputRef.value?.focus();
      }
    }, 100);
  });
};

const handleActualWorkloadBlur = async (event: FocusEvent) => {
  const target = event.target as HTMLInputElement;
  const newValue = Number(target?.value);
  if (newValue === currentActualWorkload.value) {
    isActualWorkloadEditing.value = false;
    return;
  }
  emit('loadingChange', true);
  const [error] = await issue.editActualWorkload(props.taskId || props.dataSource.id, { workload: newValue });
  emit('loadingChange', false);
  isActualWorkloadEditing.value = false;
  if (error) {
    return;
  }
  emit('change', { id: props.taskId, actualWorkload: newValue });
};

const handleActualWorkloadEnter = () => {
  if (typeof actualWorkloadInputRef.value?.blur === 'function') {
    actualWorkloadInputRef.value.blur();
  }
};
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3.5 font-medium">{{ t('common.evalWorkloadMethod') }}</div>
    </template>

    <template #default>
      <div class="workload-container">
        <!-- Evaluation Method Section -->
        <div class="workload-method-item">
          <div class="workload-method-header">
            <Icon icon="icon-pinggugongzuoliang" class="text-3.5 text-theme-primary mr-1" />
            <span class="workload-method-label">{{ t('common.evalWorkloadMethod') }}</span>
          </div>
          <div class="workload-method-value">
            <span :class="{ 'placeholder-text': !props.dataSource?.evalWorkloadMethod?.message }">
              {{ props.dataSource?.evalWorkloadMethod?.message || '--' }}
            </span>
          </div>
        </div>

        <!-- Workload Values Grid -->
        <div class="workload-grid">
          <!-- Evaluation Workload -->
          <div class="workload-item">
            <div class="workload-item-content">
              <div class="workload-item-header">
                <Icon icon="icon-wanchenggongzuoliang" class="text-3.5 text-blue-500 mr-1" />
                <span class="workload-label">
                  {{ t('common.evalWorkload') }}
                </span>
                <Button
                  v-show="!isEvalWorkloadEditing"
                  type="link"
                  class="workload-edit-btn"
                  @click="startEvalWorkloadEditing">
                  <Icon icon="icon-shuxie" class="text-3" />
                </Button>
              </div>

              <div v-show="!isEvalWorkloadEditing" class="workload-value">
                <span :class="{ 'placeholder-text': !currentEvalWorkload }">
                  {{ currentEvalWorkload || '--' }}
                </span>
              </div>

              <AsyncComponent :visible="isEvalWorkloadEditing">
                <Input
                  v-show="isEvalWorkloadEditing"
                  ref="evalWorkloadInputRef"
                  v-model:value="evalWorkloadInputValue"
                  class="workload-input"
                  dataType="float"
                  trimAll
                  :min="0.1"
                  :max="1000"
                  :placeholder="t('common.placeholders.inputActualWorkload')"
                  @blur="handleEvalWorkloadBlur"
                  @pressEnter="handleEvalWorkloadEnter" />
              </AsyncComponent>
            </div>
          </div>

          <!-- Actual Workload -->
          <div class="workload-item">
            <div class="workload-item-content">
              <div class="workload-item-header">
                <Icon icon="icon-jieshenggongzuoliang" class="text-3.5 text-green-500 mr-1" />
                <span class="workload-label">
                  {{ t('common.actualWorkload') }}
                </span>
                <Button
                  v-show="!isActualWorkloadEditing"
                  type="link"
                  class="workload-edit-btn"
                  @click="startActualWorkloadEditing">
                  <Icon icon="icon-shuxie" class="text-3" />
                </Button>
              </div>

              <div v-show="!isActualWorkloadEditing" class="workload-value">
                <span :class="{ 'placeholder-text': !currentActualWorkload }">
                  {{ currentActualWorkload || '--' }}
                </span>
              </div>

              <AsyncComponent :visible="isActualWorkloadEditing">
                <Input
                  v-show="isActualWorkloadEditing"
                  ref="actualWorkloadInputRef"
                  v-model:value="actualWorkloadInputValue"
                  class="workload-input"
                  dataType="float"
                  trimAll
                  :min="0.1"
                  :max="1000"
                  :placeholder="t('common.placeholders.inputActualWorkload')"
                  @blur="handleActualWorkloadBlur"
                  @pressEnter="handleActualWorkloadEnter" />
              </AsyncComponent>
            </div>
          </div>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
/* Container and Layout */
.workload-container {
  padding-top: 1rem;
  padding-left: 1.375rem;
  padding-right: 1.375rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.workload-method-item {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  padding: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s ease;
}

.workload-method-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.workload-method-header {
  display: flex;
  align-items: center;
  flex: 1;
}

.workload-method-label {
  font-size: 0.75rem;
  font-weight: 400;
  color: #7c8087;
}

.workload-method-value {
  font-size: 0.775rem;
  font-weight: 700;
  color: #1f2937;
  text-align: right;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  min-width: 2rem;
}

/* Placeholder text styling */
.placeholder-text {
  color: #7c8087 !important;
  font-weight: 400 !important;
  background: none !important;
  -webkit-background-clip: unset !important;
  -webkit-text-fill-color: unset !important;
  background-clip: unset !important;
}

/* Grid Layout for Workload Values */
.workload-grid {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: 1fr 1fr;
}

@media (max-width: 768px) {
  .workload-grid {
    grid-template-columns: 1fr;
  }
}

/* Individual Workload Item */
.workload-item {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.375rem;
  padding: 0.5rem;
  position: relative;
  transition: all 0.2s ease;
}

.workload-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.workload-item-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.workload-item-header {
  display: flex;
  align-items: center;
  flex: 1;
}

.workload-label {
  font-size: 0.75rem;
  font-weight: 400;
  color: #7c8087;
}

.workload-edit-btn {
  padding: 0.125rem;
  height: 1.25rem;
  width: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.25rem;
  transition: background-color 0.2s;
  margin-right: 0.375rem;
}

.workload-edit-btn:hover {
  background-color: #f3f4f6;
}

.workload-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  text-align: right;
  min-width: 2rem;
  padding-left: 0.25rem;
}

.workload-input {
  width: 80%;
}

/* Responsive Design */
@media (max-width: 640px) {
  .workload-container {
    padding-left: 0.75rem;
    padding-right: 0.75rem;
  }

  .workload-method-item,
  .workload-item {
    padding: 0.375rem;
  }

  .workload-grid {
    gap: 0.5rem;
  }

  .workload-edit-btn {
    height: 1rem;
    width: 1rem;
    margin-right: 0.25rem;
  }
}

/* Animation for smooth transitions */
.workload-item {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Legacy support for old positioning */
.left-component {
  position: absolute;
  top: -4px;
  left: 98px;
  width: calc(100% - 98px);
}
</style>
