<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { task } from '@/api/tester';

import { TaskDetail } from '../../types';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

const { t } = useI18n();

// Component Props & Emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
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
  <div class="info-row description-row">
    <div class="info-label">
      <span>{{ t('common.description') }}</span>
    </div>
    <div class="info-value">
      <div v-show="!isDescriptionEditing" class="info-value-content">
        <div class="description-content">
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
        <Button
          type="link"
          class="edit-btn"
          @click="startDescriptionEditing">
          <Icon icon="icon-shuxie" />
        </Button>
      </div>

      <AsyncComponent :visible="isDescriptionEditing">
        <div v-show="isDescriptionEditing" class="description-edit-container">
          <div>
            <RichEditor
              ref="richEditorRef"
              :value="descriptionContent"
              :height="200"
              @change="handleDescriptionContentChange" />
            <div v-show="hasDescriptionValidationError" class="text-status-error">
              {{ t('common.placeholders.inputDescription30') }}
            </div>
          </div>

          <div class="description-edit-actions">
            <Button size="small" @click="cancelDescriptionEditing">
              {{ t('actions.cancel') }}
            </Button>
            <Button
              size="small"
              type="primary"
              @click="saveDescriptionChanges">
              {{ t('actions.confirm') }}
            </Button>
          </div>
        </div>
      </AsyncComponent>
    </div>
  </div>
</template>

<style scoped>
/* Description row styles */
.description-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-height: auto;
  margin-bottom: 8px;
}

.info-label {
  flex-shrink: 0;
  width: 70px;
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #696666;
  font-weight: 500;
  line-height: 1.4;
}

.info-label span {
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
}

.info-value {
  width: 100%;
  display: flex;
  align-items: flex-start;
  min-height: 20px;
}

.info-value-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  width: 100%;
  min-height: 20px;
}

/* Description content styles */
.description-content {
  width: 100%;
  min-height: 100px;
  flex: 1;
}

.dash-text {
  color: #8c8c8c;
}

/* Edit button styles */
.edit-btn {
  flex-shrink: 0;
  padding: 0;
  height: 16px;
  width: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #1890ff !important;
  cursor: pointer;
  transition: color 0.2s;
}

.edit-btn:focus {
  color: #1890ff !important;
  background: none !important;
  border: none !important;
  box-shadow: none !important;
}

.edit-btn:hover {
  color: #1890ff;
}

.edit-btn .anticon {
  font-size: 12px;
}

/* Edit container styles */
.description-edit-container {
  width: 100%;
  margin-top: -8px;
}

.description-edit-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.border-none {
  border: none;
}
</style>
