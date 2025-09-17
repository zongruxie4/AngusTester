<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/types';
import { TaskInfoProps } from '@/views/task/task/list/types';

// Component props and emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

// Async components
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Description editing state
const isDescriptionExpanded = ref(true);
const isDescriptionEditing = ref(false);
const descriptionContent = ref<string>('');

// Description editing methods
/**
 * Enter description editing mode
 */
const enterDescriptionEditMode = () => {
  isDescriptionExpanded.value = true;
  isDescriptionEditing.value = true;
};

/**
 * Handle rich editor content change
 * @param value - New editor content
 */
const handleEditorContentChange = (value: string) => {
  descriptionContent.value = value;
};

/**
 * Cancel description editing
 */
const cancelDescriptionEdit = () => {
  isDescriptionEditing.value = false;
};

/**
 * Confirm description changes and update task description
 */
const confirmDescriptionEdit = async () => {
  if (descriptionContent.value.length > 8000) {
    return;
  }

  const updateParams = { description: descriptionContent.value };
  emit('loadingChange', true);
  const [error] = await task.editTaskDescription(currentTaskId.value, updateParams);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  isDescriptionEditing.value = false;
  emit('change', { id: currentTaskId.value, description: descriptionContent.value });
};

// Lifecycle and watchers
/**
 * Initialize component and watch for data source changes
 */
onMounted(() => {
  watch(() => props.dataSource, (newDataSource) => {
    descriptionContent.value = newDataSource?.description || '';
  }, { immediate: true });
});

// Computed properties
const currentTaskId = computed(() => {
  return props.dataSource?.id;
});

const hasContentLengthError = computed(() => {
  if (!descriptionContent.value) {
    return false;
  }
  return descriptionContent.value.length > 8000;
});
</script>

<template>
  <div class="mt-4">
    <div class="flex items-center text-theme-title mb-1.75">
      <span class="font-semibold">{{ t('task.detailInfo.description.title') }}</span>
      <Button
        v-show="!isDescriptionEditing"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="enterDescriptionEditMode">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>

    <AsyncComponent :visible="isDescriptionEditing">
      <div v-show="isDescriptionEditing">
        <div>
          <RichEditor
            :value="descriptionContent"
            :height="300"
            @change="handleEditorContentChange" />
          <div v-show="hasContentLengthError" class="text-status-error">
            {{ t('task.detailInfo.description.validation.maxLength') }}
          </div>
        </div>

        <div class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
          <Button size="small" @click="cancelDescriptionEdit">{{ t('actions.cancel') }}</Button>
          <Button
            size="small"
            type="primary"
            @click="confirmDescriptionEdit">
            {{ t('actions.confirm') }}
          </Button>
        </div>
      </div>
    </AsyncComponent>

    <AsyncComponent :visible="!isDescriptionEditing">
      <div v-show="!isDescriptionEditing">
        <RichEditor :value="descriptionContent" mode="view" />
      </div>

      <NoData
        v-show="!isDescriptionEditing&&!descriptionContent?.length"
        size="small"
        class="my-10" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.border-none {
  border: none;
}
</style>
