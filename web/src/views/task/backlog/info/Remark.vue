<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, NoData, Scroll } from '@xcan-angus/vue-ui';
import { TESTER, PageQuery, utils } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { TaskDetail } from '../../types';
import { AssocCaseProps } from '@/views/task/task/list/types';
import { Remark } from '@/views/task/task/types';

// Component Props
const props = withDefaults(defineProps<AssocCaseProps>(), {
  id: '-1'
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskDetail>): void;
  (event: 'refresh'): void;
}>();

const { t } = useI18n();

const notify = ref();

// Async Components
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Reactive State Variables
const remarkList = ref<Remark[]>([]);

/**
 * <p>Handle scroll data change</p>
 * <p>Updates remark list when new data is received from the Scroll component</p>
 */
const handleScrollDataChange = (data: Remark[]) => {
  remarkList.value = data;
};

/**
 * <p>Delete remark</p>
 * <p>Removes a remark from the server and updates the local list</p>
 */
const deleteRemark = async (remarkId: string) => {
  const [error] = await task.deleteTaskRemark(remarkId);
  if (error) {
    return;
  }

  remarkList.value = remarkList.value.filter(item => item.id !== remarkId);
};

/**
 * Current content being edited in the rich text editor
 * <p>
 * Contains the HTML/rich text content that the user is typing
 */
const currentRemarkContent = ref<string>('');

/**
 * Reference to the rich text editor component instance
 * <p>
 * Used to programmatically control the editor and access its methods
 */
const richTextEditorRef = ref();

/**
 * Validation error state for content length
 * <p>
 * Indicates whether the current content exceeds the maximum allowed length
 */
const isValidationError = ref(false);

/**
 * Checks if the content is empty or contains only whitespace
 * <p>
 * Validates rich text content by parsing JSON and checking for meaningful content
 *
 * @param contentValue - The content string to validate
 * @returns True if the content is considered empty
 */
const isContentEmpty = (contentValue: string) => {
  const parsedValues = JSON.parse(contentValue);
  if (!parsedValues?.length) {
    return true;
  }
  if (parsedValues?.length > 1) {
    return false;
  }
  if (parsedValues.length === 1) {
    return !parsedValues[0].insert.replaceAll('\n', '');
  }
  return false;
};

const submitRemark = async () => {
  if (!currentRemarkContent.value) {
    return;
  }
  if (isContentEmpty(currentRemarkContent.value)) {
    return;
  }

  if (isContentTooLong()) {
    isValidationError.value = true;
    return;
  }
  isValidationError.value = false;

  const requestParams = { taskId: props.id, content: currentRemarkContent.value };

  emit('loadingChange', true);
  const [error] = await task.addTaskRemark(requestParams);
  emit('loadingChange', false);
  if (error) {
    return;
  }

  currentRemarkContent.value = '';
  notify.value = utils.uuid();
};

/**
 * Checks if the content exceeds the maximum allowed length
 * <p>
 * Validates that the rich text content does not exceed the 6000 character limit
 *
 * @returns True if the content exceeds the maximum length
 */
const isContentTooLong = () => {
  if (!currentRemarkContent.value) {
    return false;
  }
  const contentLength = richTextEditorRef.value.getLength();
  return contentLength > 6000;
};

/**
 * Handles content changes in the rich text editor
 * <p>
 * Updates the current content state when user types or modifies text
 *
 * @param newContent - The new content from the editor
 */
const handleEditorContentChange = (newContent: string) => {
  currentRemarkContent.value = newContent;
};

/**
 * <p>Query parameters for remark fetching</p>
 * <p>Constructs parameters for fetching remarks ordered by creation date</p>
 */
const queryParams = computed(() => {
  // Only fetch remarks if we have a valid task ID
  if (!props.id || props.id === '' || props.id === '-1') {
    return null; // Return null to prevent API call
  }
  return { orderBy: 'createdDate', orderSort: PageQuery.OrderSort.Asc, taskId: props.id };
});
</script>
<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('backlog.remark') }}</h3>
    </div>

    <div class="pr-2">
      <div class="mb-2.5">
        <RichEditor
          ref="richTextEditorRef"
          :value="currentRemarkContent"
          :height="150"
          :options="{placeholder: t('task.remark.placeholder')}"
          @change="handleEditorContentChange" />
        <div v-show="isValidationError" class="text-status-error">
          {{ t('task.remark.validation.maxLength') }}
        </div>
      </div>
      <!-- Submit button section -->
      <div class="space-x-2.5 w-full flex items-center justify-end">
        <Button
          size="small"
          type="primary"
          @click="submitRemark">
          {{ t('task.editModal.actions.confirm') }}
        </Button>
      </div>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Only render Scroll component when we have valid taskId -->
        <Scroll
          v-if="queryParams"
          :action="`${TESTER}/task/remark`"
          :hideNoData="true"
          :params="queryParams"
          :lineHeight="56"
          :notify="notify"
          style="height: calc(100% - 30px);"
          transition
          @change="handleScrollDataChange">
          <template v-if="remarkList?.length">
            <div
              v-for="(remark) in remarkList"
              :key="remark.id"
              class="mb-1.5 last:mb-0">
              <div class="flex items-center mb-0.5">
                <div class="border-2 border-theme-text-box w-2.5 h-2.5 rounded-full"></div>

                <div class="ml-3 font-normal text-3 flex items-center space-x-2 leading-4">
                  <div class="text-theme-content font-medium">{{ remark.createdByName }}</div>
                  <div class="text-theme-content font-medium">{{ t('backlog.addedRemark') }}</div>
                  <div class="text-theme-sub-content">{{ remark.createdDate }}</div>
                  <Icon
                    icon="icon-qingchu"
                    class="cursor-pointer text-theme-text-hover text-3.5"
                    @click="deleteRemark(remark.id)" />
                </div>
              </div>

              <div class="browser-container">
                <RichEditor :value="remark.content" mode="view" />
              </div>
            </div>
          </template>

          <NoData v-else size="small" />
        </Scroll>

        <!-- Show message when no valid taskId -->
        <div v-else class="flex items-center justify-center h-full">
          <NoData size="small" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.browser-container  {
  padding-left: 22px;
}
</style>
