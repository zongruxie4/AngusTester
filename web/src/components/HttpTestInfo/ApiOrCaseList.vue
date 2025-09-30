<script lang="ts" setup>
// Vue core imports
import { useI18n } from 'vue-i18n';

// UI component imports
import { Icon, Tooltip, Grid } from '@xcan-angus/vue-ui';

// Local component imports
import BaseVirtualList from './BaseVirtualList.vue';

const { t } = useI18n();

/**
 * Interface for API or case item data structure
 */
interface ApiOrCaseItem {
  apisName: string;
  caseId: string;
  passed: boolean; // Whether the test passed
  enabled?: boolean;
  caseName?: string;
  caseType?: {
    value: string;
  };
}

/**
 * Component props interface for API or case list
 */
interface Props {
  dataSource: ApiOrCaseItem[];
}

// Table configuration for tooltip display
const tooltipTableColumns = [
  [
    { dataIndex: 'testNum', label: t('xcan_httpTestInfo.testCount') },
    { dataIndex: 'testFailureNum', label: t('xcan_httpTestInfo.failureCount') },
    { dataIndex: 'failureMessage', label: t('xcan_httpTestInfo.failureReason') },
    { dataIndex: 'execId', label: t('xcan_httpTestInfo.lastExecutionTime') },
    { dataIndex: 'execName', label: t('xcan_httpTestInfo.executionName') },
    { dataIndex: 'execByName', label: t('xcan_httpTestInfo.lastExecutor') },
    { dataIndex: 'lastExecDate', label: t('xcan_httpTestInfo.lastExecutionTime') }
  ]
];

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([])
});

// Case type icon configuration mapping
const caseTypeIconMapping = {
  SMOKE: 'icon-maoyanceshi',
  SECURITY: 'icon-anquanceshi',
  USER_DEFINED: 'icon-zidingyiceshi'
};

</script>
<template>
  <div class="text-3">
    <BaseVirtualList
      :data="props.dataSource"
      :height="140"
      :itemHeight="28"
      :showNum="30"
      :cache="10">
      <template #default="{item}">
        <div
          :key="item.id"
          class="border border-gray-light rounded bg-gray-light mt-1">
          <Tooltip placement="rightTop">
            <template #title>
              <div class="max-h-100 overflow-y-auto">
                <Grid
                  :dataSource="item.result || item"
                  :columns="tooltipTableColumns" />
              </div>
            </template>
            <div class="px-1 flex h-6 items-center">
              <Icon :icon="caseTypeIconMapping[item.caseType?.value] || 'icon-jiekouyongli2'" class="mr-1 text-4" />
              <span class="min-w-0 truncate flex-1" :title="item.apisName || item.caseName || item.summary">{{ item.apisName || item.caseName || item.summary }}</span>
              <span
                v-if="!item.enabled"
                class="px-2 rounded">{{ t('status.disabled') }}</span>
              <span
                v-else-if="item.passed"
                class="px-2 rounded text-status-success">{{ t('xcan_httpTestInfo.passed') }}</span>
              <span v-else class="px-2 rounded text-status-error ">{{ t('xcan_httpTestInfo.failed') }}</span>
            </div>
          </Tooltip>
        </div>
      </template>
    </BaseVirtualList>
  </div>
</template>
