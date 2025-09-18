<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, NoData, Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { task } from '@/api/tester';

import { TaskInfo } from '@/views/task/types';
import { TaskInfoProps } from '@/views/task/task/list/types';

// Component props and emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  taskId: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  loading: false
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

// Async component
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Description editing state
const richEditorRef = ref();
const isDescriptionOpen = ref(true);
const isDescriptionEditing = ref(false);
const descriptionContent = ref<string>('');
const isValidationError = ref(false);

// Description editing methods
/**
 * <p>Initiates description editing mode by opening the toggle and enabling edit flag.</p>
 * <p>Sets the content value to the current task description.</p>
 */
const startDescriptionEditing = () => {
  isDescriptionOpen.value = true;
  isDescriptionEditing.value = true;
  descriptionContent.value = props?.dataSource?.description || '';
};

/**
 * <p>Handles rich editor content change to update the description content.</p>
 * @param value - New content value from the rich editor
 */
const handleEditorContentChange = (value: string) => {
  descriptionContent.value = value;
};

/**
 * <p>Cancels description editing and resets content to original value.</p>
 */
const cancelDescriptionEditing = () => {
  isDescriptionEditing.value = false;
  descriptionContent.value = props.dataSource?.description || '';
};

/**
 * <p>Saves the description changes by calling the API to update task description.</p>
 * <p>Validates content length before saving and handles errors appropriately.</p>
 */
const saveDescriptionChanges = async () => {
  if (isContentLengthInvalid()) {
    isValidationError.value = true;
    return;
  }
  isValidationError.value = false;

  const params = { description: descriptionContent.value };
  emit('loadingChange', true);
  const [error] = await task.editTaskDescription(props.taskId || '', params);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  isDescriptionEditing.value = false;
  emit('change', { id: props.taskId, description: descriptionContent.value });
};

// Lifecycle hooks
onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    descriptionContent.value = newValue?.description || '';
  }, { immediate: true });
});

/**
 * <p>Validates if the content length exceeds the maximum allowed length.</p>
 * <p>Returns true if content length is greater than 6000 characters.</p>
 * @returns true if content length is invalid, false otherwise
 */
const isContentLengthInvalid = () => {
  if (!descriptionContent.value) {
    return false;
  }
  const length = richEditorRef.value.getLength();
  return length > 6000;
};
</script>

<template>
  <Toggle v-model:open="isDescriptionOpen">
    <template #title>
      <div class="flex items-center text-3.5">
        <span>{{ t('task.detailInfo.description.title') }}</span>
        <template v-if="isDescriptionEditing">
          <Button
            size="small"
            type="link"
            @click="cancelDescriptionEditing">
            {{ t('task.detailInfo.description.actions.cancel') }}
          </Button>
          <Button
            size="small"
            type="link"
            @click="saveDescriptionChanges">
            {{ t('task.detailInfo.description.actions.confirm') }}
          </Button>
        </template>
        <Button
          v-else
          type="link"
          class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
          @click="startDescriptionEditing">
          <Icon icon="icon-shuxie" class="text-3.5" />
        </Button>
      </div>
    </template>

    <template #default>
      <template v-if="isDescriptionEditing">
        <div class="mb-2.5 mt-2.5 ml-5.5">
          <RichEditor
            ref="richEditorRef"
            :value="descriptionContent"
            :options="{placeholder: t('task.detailInfo.description.placeholder')}"
            :placeholder="t('task.detailInfo.description.validation.maxLength')"
            @change="handleEditorContentChange" />
          <div v-show="isValidationError" class="text-status-error text-3">
            {{ t('task.detailInfo.description.validation.maxLength') }}
          </div>
        </div>
      </template>

      <div v-if="!isDescriptionEditing" class="browser-container">
        <RichEditor :value="props.dataSource?.description" mode="view" />
      </div>

      <NoData
        v-if="!isDescriptionEditing&&!descriptionContent?.length"
        size="small"
        style="min-height: 160px;" />
    </template>
  </Toggle>
</template>

<style scoped>
.border-none {
  border: none;
}

.browser-container  {
  padding-left: 21px;
  transform: translateY(1px);
}
</style>
