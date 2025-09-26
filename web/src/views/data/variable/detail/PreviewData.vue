<script setup lang="ts">
import { Button } from 'ant-design-vue';
import { Hints, Icon, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { usePreviewData } from './composables/usePreviewData';

/**
 * Component props definition
 */
interface Props {
  /** Data source for the preview */
  dataSource: {
    name: string;
    value: string;
    id: string;
    projectId: string;
    extraction: any;
  };
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

// Use the preview data composable for preview logic
const {
  // State
  loading,
  content,
  errorMessage,

  // Methods
  refresh
} = usePreviewData(props);
</script>

<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <!-- Header with title and refresh button -->
    <div class="flex items-center justify-between mb-2.5">
      <Hints :text="t('dataVariable.detail.previewData.tip')" />
      <Button
        :disabled="loading"
        size="small"
        type="link"
        class="px-0 h-5 leading-5 border-0 text-theme-content text-theme-text-hover"
        @click="refresh">
        <Icon icon="icon-shuaxin" class="text-3.5" />
        <span class="ml-1">{{ t('common.refresh') }}</span>
      </Button>
    </div>

    <!-- Preview content area -->
    <div class="content-container rounded border border-solid border-theme-text-box">
      <!-- Error message -->
      <span v-if="errorMessage" class="text-status-error">{{ errorMessage }}</span>

      <!-- No data message -->
      <span v-else-if="!content">{{ t('common.noData') }}</span>

      <!-- Preview content -->
      <span v-else>{{ content }}</span>
    </div>
  </Spin>
</template>

<style scoped>
.content-container {
  height: 316px;
  padding: 4px 7px;
  overflow: auto;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
