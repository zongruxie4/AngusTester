<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Icon, NoData } from '@xcan-angus/vue-ui';
import { issue } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { TaskDetail } from '@/views/issue/types';
import { TaskDetailProps } from '@/views/issue/issue/list/types';

// Async components
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Component props and emits
const props = withDefaults(defineProps<TaskDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
}>();

// Description editing state
const isDescriptionExpanded = ref(true);
const isDescriptionEditing = ref(false);
const descriptionContent = ref<string>('');

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
  const [error] = await issue.editTaskDescription(currentTaskId.value, updateParams);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  isDescriptionEditing.value = false;
  emit('change', { id: currentTaskId.value, description: descriptionContent.value });
};

/**
 * Initialize component and watch for data source changes
 */
onMounted(() => {
  watch(() => props.dataSource, (newDataSource) => {
    descriptionContent.value = newDataSource?.description || '';
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
              <RichEditor :value="descriptionContent" mode="view" />
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
          @click="enterDescriptionEditMode">
          <Icon icon="icon-shuxie" />
        </Button>
      </div>

      <AsyncComponent :visible="isDescriptionEditing">
        <div v-show="isDescriptionEditing" class="description-edit-container">
          <div>
            <RichEditor
              :value="descriptionContent"
              :height="200"
              @change="handleEditorContentChange" />
            <div v-show="hasContentLengthError" class="text-status-error">
              {{ t('common.placeholders.inputDescription30') }}
            </div>
          </div>

          <div class="description-edit-actions">
            <Button size="small" @click="cancelDescriptionEdit">
              {{ t('actions.cancel') }}
            </Button>
            <Button
              size="small"
              type="primary"
              @click="confirmDescriptionEdit">
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
.description-content { width: 100%; min-height: 100px; flex: 1; }

.dash-text { color: #8c8c8c; }

/* Edit button styles */
.edit-btn { flex-shrink: 0; padding: 0; height: 16px; width: 16px; display: flex; align-items: center; justify-content: center; border: none; background: none; color: #1890ff !important; cursor: pointer; transition: color 0.2s; }
.edit-btn:focus { color: #1890ff !important; background: none !important; border: none !important; box-shadow: none !important; }
.edit-btn:hover { color: #1890ff; }
.edit-btn .anticon { font-size: 12px; }

/* Edit container styles */
.description-edit-container { width: 100%; margin-top: -8px; }
.description-edit-actions { margin-top: 8px; display: flex; gap: 8px; justify-content: flex-end; }

.border-none { border: none; }
</style>
