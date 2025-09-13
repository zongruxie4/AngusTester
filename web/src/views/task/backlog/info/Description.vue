<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { task } from '@/api/tester';

import { TaskInfo } from '../../types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

// Async Components
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Reactive State Variables
const isDescriptionExpanded = ref(true);
const isDescriptionEditing = ref(false);
const descriptionContent = ref<string>('');

// Computed Properties
const currentTaskId = computed(() => {
  return props.dataSource?.id;
});

/**
 * <p>Initialize description editing mode</p>
 * <p>Sets the current description content and enables editing state</p>
 */
const startDescriptionEditing = () => {
  isDescriptionExpanded.value = true;
  isDescriptionEditing.value = true;
  descriptionContent.value = props.dataSource?.description || '';
};

/**
 * <p>Handle rich editor content change</p>
 * <p>Updates the description content when user types in the editor</p>
 */
const handleDescriptionContentChange = (value: string) => {
  descriptionContent.value = value;
};

/**
 * <p>Cancel description editing</p>
 * <p>Exits editing mode without saving changes</p>
 */
const cancelDescriptionEditing = () => {
  isDescriptionEditing.value = false;
};

// Description Validation
const hasDescriptionValidationError = ref(false);
const richEditorRef = ref();

/**
 * <p>Validate description content length</p>
 * <p>Checks if description exceeds maximum allowed length (8000 characters)</p>
 */
const validateDescriptionLength = () => {
  return !(richEditorRef.value && richEditorRef.value.getLength() > 8000);
};

/**
 * <p>Save description changes</p>
 * <p>Validates content and calls API to update task description</p>
 */
const saveDescriptionChanges = async () => {
  if (!validateDescriptionLength()) {
    hasDescriptionValidationError.value = true;
    return;
  }
  hasDescriptionValidationError.value = false;

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

// Lifecycle Hooks
onMounted(() => {
  watch(() => props.dataSource, (newTaskData) => {
    descriptionContent.value = newTaskData?.description || '';
  }, { immediate: true });
});
</script>

<template>
  <div class="mt-4">
    <div class="flex items-center text-theme-title mb-1.75">
      <span>{{ t('backlog.info.description.title') }}</span>
      <Button
        v-show="!isDescriptionEditing"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="startDescriptionEditing">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>

    <AsyncComponent :visible="isDescriptionEditing">
      <div v-show="isDescriptionEditing">
        <div>
          <RichEditor
            ref="richEditorRef"
            :value="descriptionContent"
            :height="300"
            @change="handleDescriptionContentChange" />
          <div v-show="hasDescriptionValidationError" class="text-status-error">
            {{ t('backlog.info.description.messages.maxLength') }}
          </div>
        </div>

        <div class="mt-2.5 space-x-2.5 w-full flex items-center justify-end">
          <Button size="small" @click="cancelDescriptionEditing">
            {{ t('backlog.info.description.cancel') }}
          </Button>
          <Button
            size="small"
            type="primary"
            @click="saveDescriptionChanges">
            {{ t('backlog.info.description.confirm') }}
          </Button>
        </div>
      </div>
    </AsyncComponent>

    <AsyncComponent :visible="!isDescriptionEditing">
      <div v-show="!isDescriptionEditing">
        <RichEditor :value="props?.dataSource?.description" mode="view" />
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
