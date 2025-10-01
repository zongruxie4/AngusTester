<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, Scroll } from '@xcan-angus/vue-ui';
import { PageQuery, TESTER, utils } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { Remark } from '@/views/issue/issue/types';
import { BaseProps } from '@/types/types';

/**
 * Props interface for Remark component
 */
const props = withDefaults(defineProps<BaseProps>(), {
  id: '',
  notify: undefined
});

// Composables
const { t } = useI18n();

/**
 * Event emitter for component communication
 * <p>
 * Emits events to notify parent components about data changes
 * and refresh triggers
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:notify', value: string): void;
}>();

/**
 * Lazy-loaded rich text editor component
 * <p>
 * Provides a rich text editing interface for creating and editing remarks
 */
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

/**
 * Reference to the rich text editor component instance
 * <p>
 * Used to programmatically control the editor and access its methods
 */
const richTextEditorRef = ref();

/**
 * Current content being edited in the rich text editor
 * <p>
 * Contains the HTML/rich text content that the user is typing
 */
const currentRemarkContent = ref<string>('');

/**
 * List of existing remarks to be displayed
 * <p>
 * Contains the remark history data fetched from the server
 */
const remarkDataList = ref<Remark[]>([]);

/**
 * Validation error state for content length
 * <p>
 * Indicates whether the current content exceeds the maximum allowed length
 */
const isValidationError = ref(false);

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
 * Deletes a specific remark by ID
 * <p>
 * Removes the remark from the server and updates the local data list
 *
 * @param remarkId - The ID of the remark to delete
 */
const deleteRemark = async (remarkId: string) => {
  const [error] = await task.deleteTaskRemark(remarkId);
  if (error) {
    return;
  }

  remarkDataList.value = remarkDataList.value.filter(item => item.id !== remarkId);
};

/**
 * Handles data changes from the Scroll component
 * <p>
 * Updates the local remark data when new data is received from the API
 *
 * @param newData - Array of remark data from the server
 */
const handleScrollDataChange = (newData: Remark[]) => {
  remarkDataList.value = newData;
};

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
 * Handles the submission of a new remark
 * <p>
 * Validates the content, submits it to the server, and refreshes the data
 * if successful. Shows validation errors if content is invalid.
 */
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
  const [error] = await task.addTaskRemark(requestParams);
  if (error) {
    return;
  }

  currentRemarkContent.value = '';
  emit('update:notify', utils.uuid());
};

/**
 * API parameters for fetching remark data
 * <p>
 * Constructs the query parameters for retrieving remarks
 * ordered by creation date in descending order
 */
const apiParams = computed(() => {
  return {
    orderBy: 'createdDate',
    orderSort: PageQuery.OrderSort.Desc,
    taskId: props.id
  };
});

</script>

<template>
  <div class="h-full overflow-auto pr-5">
    <!-- Rich text editor section -->
    <div>
      <div class="mb-2.5">
        <RichEditor
          ref="richTextEditorRef"
          :value="currentRemarkContent"
          :height="150"
          :options="{placeholder: t('common.placeholders.inputDescription30')}"
          @change="handleEditorContentChange" />
        <div v-show="isValidationError" class="text-status-error">
          {{ t('common.placeholders.inputDescription30') }}
        </div>
      </div>

      <!-- Submit button section -->
      <div class="space-x-2.5 w-full flex items-center justify-end">
        <Button
          size="small"
          type="primary"
          @click="submitRemark">
          {{ t('actions.confirm') }}
        </Button>
      </div>
    </div>

    <!-- Remarks list section -->
    <Scroll
      :action="`${TESTER}/task/remark`"
      :hideNoData="true"
      :params="apiParams"
      :lineHeight="56"
      :notify="props.notify"
      style="height: calc(100% - 238px);"
      transition
      @change="handleScrollDataChange">
      <div
        v-for="(remarkItem) in remarkDataList"
        :key="remarkItem.id"
        class="mb-1.5 last:mb-0">
        <!-- Remark header with creator info and actions -->
        <div class="flex items-center mb-0.5">
          <div class="border-2 border-theme-text-box w-2.5 h-2.5 rounded-full"></div>

          <div class="ml-3 font-normal text-3 flex items-center space-x-2 leading-4">
            <div class="text-theme-content font-medium">{{ remarkItem.createdByName }}</div>
            <div class="text-theme-content font-medium">{{ t('task.remark.actions.addRemark') }}</div>
            <div class="text-theme-sub-content">{{ remarkItem.createdDate }}</div>
            <Icon
              icon="icon-qingchu"
              class="cursor-pointer text-theme-text-hover text-3.5"
              @click="deleteRemark(remarkItem.id)" />
          </div>
        </div>

        <!-- Remark content display -->
        <div class="browser-container">
          <RichEditor :value="remarkItem.content" mode="view" />
        </div>
      </div>
    </Scroll>
  </div>
</template>

<style scoped>
.browser-container  {
  padding-left: 22px;
}
</style>
