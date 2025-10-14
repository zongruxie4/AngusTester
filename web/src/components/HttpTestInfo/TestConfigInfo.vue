<script setup lang="ts">
import { computed, watch, ref, inject, Ref } from 'vue';
import { useI18n } from 'vue-i18n';

import { Grid, Icon } from '@xcan-angus/vue-ui';
import { ProjectInfo } from '@/layout/types';

const { t } = useI18n();

/**
 * Component props interface for test configuration information display
 */
interface Props {
  value: Record<string, any>;
  enabled?: boolean;
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  value: () => ({}),
  enabled: undefined
});

// Injected project information
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));

// Test configuration data source
const testConfigDataSource = ref<Record<string, any>>({});

/**
 * Computed property for test configuration table columns
 */
const testConfigTableColumns = computed(() => {
  return [[
    {
      label: t('common.testResult'),
      dataIndex: 'passed'
    },
    {
      label: t('common.failureReason'),
      dataIndex: 'failureMessage'
    },
    {
      label: t('common.counts.testCount'),
      dataIndex: 'testNum'
    },
    {
      label: t('xcan_httpTestInfo.failureCount'),
      dataIndex: 'testFailureNum'
    },
    {
      label: t('common.scriptId'),
      dataIndex: 'scriptId'
    },
    {
      label: t('common.scriptName'),
      dataIndex: 'scriptName',
      ellipsis: true
    },
    {
      label: t('xcan_httpTestInfo.executionId'),
      dataIndex: 'execId'
    },
    {
      label: t('xcan_httpTestInfo.executionName'),
      dataIndex: 'execName',
      ellipsis: true
    },
    {
      label: t('xcan_httpTestInfo.lastExecutor'),
      dataIndex: 'execByName'
    },
    {
      label: t('xcan_httpTestInfo.lastExecutionTime'),
      dataIndex: 'lastExecDate'
    }
  ]];
});

/**
 * Watch for changes in props value and update data source
 */
watch(() => props.value, newValue => {
  testConfigDataSource.value = newValue;
}, {
  immediate: true
});
</script>
<template>
  <Grid
    :columns="testConfigTableColumns"
    :dataSource="testConfigDataSource">
    <template #passed="{text}">
      <div class="flex items-center">
        <template v-if="!testConfigDataSource.id">
          {{ t('status.notTested') }}
        </template>
        <template v-else-if="!props.enabled && !testConfigDataSource.id">
          {{ t('status.disabled') }}
        </template>
        <template v-else-if="text">
          <Icon icon="icon-duihao" class="text-status-success" />
          {{ t('status.passed') }} {{ props.enabled === false ? `(${t('status.disabled')})` : '' }}
        </template>
        <template v-else>
          <Icon icon="icon-chahao" class="text-status-error" />
          {{ t('status.failed') }} {{ props.enabled === false ? `(${t('status.disabled')})` : '' }}
        </template>
      </div>
    </template>
    <template #execName="{text}">
      <a
        v-if="text"
        :title="text"
        class="text-theme-special"
        :href="`/execution/info/${testConfigDataSource.execId}?projectId=${projectInfo.id}`"
        target="_blank">{{ text }}</a>
      <template v-else>--</template>
    </template>
    <template #scriptName="{text}">
      <a
        v-if="text"
        :title="text"
        class="text-theme-special truncate"
        :href="`/scripts/detail/${testConfigDataSource.scriptId}?projectId=${projectInfo.id}&type=view`"
        target="_blank">{{ text }}</a>
      <template v-else>--</template>
    </template>
  </Grid>
</template>
<style scoped>
.task-item:hover .del-btn{
  @apply visible;
}
</style>
