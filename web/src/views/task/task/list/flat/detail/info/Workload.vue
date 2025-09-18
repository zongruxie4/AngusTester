<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, Input, Toggle } from '@xcan-angus/vue-ui';
import { EvalWorkloadMethod } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/types';

type Props = {
  projectId: string;
  userInfo: { id: string; fullName: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
  taskId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  taskId: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

// Editing state
const evalWorkloadInputRef = ref();
const isEvalWorkloadEditing = ref(false);
const evalWorkloadInputValue = ref<string>();

const actualWorkloadInputRef = ref();
const isActualWorkloadEditing = ref(false);
const actualWorkloadInputValue = ref<string>();

// Computed values
const currentEvalWorkloadMethod = computed(() => props.dataSource?.evalWorkloadMethod?.value);
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
  const newValue = target?.value;
  if (newValue === currentEvalWorkload.value) {
    isEvalWorkloadEditing.value = false;
    return;
  }
  emit('loadingChange', true);
  const [error] = await task.editEvalWorkloadApi(props.taskId, { workload: newValue });
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
  const newValue = target?.value;
  if (newValue === currentActualWorkload.value) {
    isActualWorkloadEditing.value = false;
    return;
  }
  emit('loadingChange', true);
  const [error] = await task.editActualWorkload(props.taskId, { workload: newValue });
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
      <div class="text-3.5">{{ t('task.detailInfo.basic.workloadEvaluation') }}</div>
    </template>

    <template #default>
      <div class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="relative flex items-start">
          <div class="w-24.5 flex items-start whitespace-pre-wrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.evalWorkloadMethod') }}</span>
          </div>
          <div class="font-medium whitespace-pre-wrap break-words break-all">
            {{ props.dataSource?.evalWorkloadMethod?.message || '--' }}
          </div>
        </div>

        <div class="relative flex items-start">
          <div class="w-24.5 flex items-start whitespace-pre-wrap flex-shrink-0">
            <span>
              {{ currentEvalWorkloadMethod === EvalWorkloadMethod.STORY_POINT
                ? t('task.detailInfo.basic.columns.evalWorkload')
                : t('task.detailInfo.basic.columns.evalWorkHours') }}
            </span>
          </div>

          <div v-show="!isEvalWorkloadEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div class="font-medium">{{ currentEvalWorkload || '--' }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="startEvalWorkloadEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="isEvalWorkloadEditing">
            <Input
              v-show="isEvalWorkloadEditing"
              ref="evalWorkloadInputRef"
              v-model:value="evalWorkloadInputValue"
              class="left-component max-w-52"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('task.detailInfo.basic.columns.actualWorkloadPlaceholder')"
              @blur="handleEvalWorkloadBlur"
              @pressEnter="handleEvalWorkloadEnter" />
          </AsyncComponent>
        </div>

        <div class="relative flex items-start">
          <div class="w-24.5 flex items-start whitespace-pre-wrap flex-shrink-0">
            <span>
              {{ currentEvalWorkloadMethod === EvalWorkloadMethod.STORY_POINT
                ? t('task.detailInfo.basic.columns.actualStoryPoint')
                : t('task.detailInfo.basic.columns.actualWorkload') }}
            </span>
          </div>

          <div v-show="!isActualWorkloadEditing" class="flex items-start whitespace-pre-wrap break-words break-all">
            <div class="font-medium">{{ currentActualWorkload || '--' }}</div>
            <Button
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
              @click="startActualWorkloadEditing">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>

          <AsyncComponent :visible="isActualWorkloadEditing">
            <Input
              v-show="isActualWorkloadEditing"
              ref="actualWorkloadInputRef"
              v-model:value="actualWorkloadInputValue"
              class="left-component max-w-52"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              :placeholder="t('task.detailInfo.basic.columns.actualWorkloadPlaceholder')"
              @blur="handleActualWorkloadBlur"
              @pressEnter="handleActualWorkloadEnter" />
          </AsyncComponent>
        </div>
      </div>
    </template>
  </Toggle>
</template>

<style scoped>
.left-component {
  position: absolute;
  top: -4px;
  left: 98px;
  width: calc(100% - 98px);
}
</style>
