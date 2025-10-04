<script lang="ts" setup>
// Vue core imports
import { watch, ref, onMounted, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Icon, Tooltip, Grid, Input, Select, NoData } from '@xcan-angus/vue-ui';

// Local component imports
import BaseVirtualList from './BaseVirtualList.vue';

// Third-party library imports
import { debounce } from 'throttle-debounce';
import elementResizeDetector from 'element-resize-detector';

const { t } = useI18n();

/**
 * API test item interface for service test APIs
 */
interface ApiTestItem {
  apisName: string;
  caseId: string;
  passed: boolean; // Whether the test passed
  enabled?: boolean;
  caseName?: string;
  caseType?: {
    value: string;
  };
  summary?: string;
  tested?: boolean;
  failed?: boolean;
  enabledTest?: boolean;
  id?: string;
  funcTestPassed?: boolean;
  perfTestPassed?: boolean;
  stabilityTestPassed?: boolean;
  funcTestFailureMessage?: string;
  perfTestFailureMessage?: string;
  stabilityTestFailureMessage?: string;
  result?: any;
}

/**
 * Enabled test API IDs configuration interface
 */
interface EnabledTestApiIds {
  FUNCTIONAL: string[];
  PERFORMANCE: string[];
  STABILITY: string[];
}

/**
 * Component props interface for service test APIs
 */
interface Props {
  dataSource: ApiTestItem[];
  enabledTestApiIds: EnabledTestApiIds;
}
// Element resize detector for responsive list
const elementResizeDetectorInstance = elementResizeDetector({ strategy: 'scroll' });

// Test result tooltip table configuration
const testResultTooltipTableColumns = [
  [
    { dataIndex: 'funcTestPassed', label: t('xcan_httpTestInfo.functionalTest') },
    { dataIndex: 'perfTestPassed', label: t('xcan_httpTestInfo.performanceTest') },
    { dataIndex: 'stabilityTestPassed', label: t('xcan_httpTestInfo.stabilityTest') }
  ]
];

// Test status filter options
const testStatusFilterOptions = [
  {
    label: t('xcan_httpTestInfo.passed'),
    value: 'passed'
  },
  {
    label: t('xcan_httpTestInfo.failed'),
    value: 'unpassed'
  },
  {
    label: t('xcan_httpTestInfo.untested'),
    value: 'unTested'
  }
];

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([]),
  enabledTestApiIds: () => ({
    FUNCTIONAL: [],
    PERFORMANCE: [],
    STABILITY: []
  })
});

// Case type icon configuration mapping
const caseTypeIconMapping = {
  SMOKE: 'icon-maoyanceshi',
  SECURITY: 'icon-anquanceshi',
  USER_DEFINED: 'icon-zidingyiceshi'
};

// Component state management
const filteredApiTestData = ref<ApiTestItem[]>([]);
const searchKeywords = ref<string>();
const selectedTestStatus = ref<string>();
const virtualListContainerRef = ref();
const virtualListHeight = ref(100);

/**
 * Update virtual list height when container size changes
 */
const handleListHeightResize = () => {
  virtualListHeight.value = virtualListContainerRef.value.clientHeight;
  console.log(virtualListHeight.value);
};

/**
 * Debounced search handler for keyword changes
 */
const handleSearchKeywordsChange = debounce(500, () => {
  applyDataFilters();
});

/**
 * Apply search and status filters to the data source
 */
const applyDataFilters = () => {
  filteredApiTestData.value = props.dataSource;

  if (searchKeywords.value) {
    filteredApiTestData.value = filteredApiTestData.value.filter(item =>
      item.summary?.includes(searchKeywords.value!)
    );
  }

  if (selectedTestStatus.value) {
    if (selectedTestStatus.value === 'passed') {
      filteredApiTestData.value = filteredApiTestData.value.filter(item => item.passed === true);
    } else if (selectedTestStatus.value === 'unpassed') {
      filteredApiTestData.value = filteredApiTestData.value.filter(item =>
        item.tested === true && item.failed === true
      );
    } else {
      filteredApiTestData.value = filteredApiTestData.value.filter(item => item.tested === false);
    }
  }
};

/**
 * Initialize component and set up watchers and resize listeners
 */
onMounted(() => {
  watch(() => props.dataSource, () => {
    applyDataFilters();
  }, {
    immediate: true
  });
  elementResizeDetectorInstance.listenTo(virtualListContainerRef.value, handleListHeightResize);
});

/**
 * Clean up resize listeners when component unmounts
 */
onBeforeUnmount(() => {
  elementResizeDetectorInstance.removeListener(virtualListContainerRef.value, handleListHeightResize);
});

</script>
<template>
  <div class="text-3 space-y-1 min-h-0 flex flex-col">
    <div class="flex space-x-2">
      <Input
        v-model:value="searchKeywords"
        class="w-1/2"
        :allowClear="true"
        :placeholder="t('xcan_httpTestInfo.searchName')"
        @change="handleSearchKeywordsChange" />
      <Select
        v-model:value="selectedTestStatus"
        class="w-1/2"
        :placeholder="t('xcan_httpTestInfo.selectStatus')"
        :allowClear="true"
        :options="testStatusFilterOptions"
        @change="applyDataFilters" />
    </div>
    <div ref="virtualListContainerRef" class="flex-1 min-h-50">
      <BaseVirtualList
        v-show="filteredApiTestData.length"
        :data="filteredApiTestData"
        :height="virtualListHeight"
        :itemHeight="32"
        :showNum="30"
        :cache="10">
        <template #default="{item}">
          <div
            :key="item.id"
            class="border border-gray-light rounded bg-gray-light mb-2">
            <Tooltip placement="bottom">
              <template #title>
                <div class="max-h-100 overflow-y-auto">
                  <Grid
                    :dataSource="item.result || item"
                    :columns="testResultTooltipTableColumns">
                    <template #funcTestPassed>
                      <template v-if="item.funcTestPassed === true">
                        <span class="text-status-success">{{ t('status.passed') }}</span>
                      </template>
                      <template v-else-if="item.funcTestPassed === false">
                        <div class="text-status-error">{{ t('status.failed') }} <span>{{ item.funcTestFailureMessage }}</span></div>
                      </template>
                      <template v-else>
                        {{ props.enabledTestApiIds.FUNCTIONAL.includes(item.id) ? t('status.untTested') : t('status.disabled') }}
                      </template>
                    </template>
                    <template #perfTestPassed>
                      <template v-if="item.perfTestPassed === true">
                        <span class="text-status-success">{{ t('status.passed') }}</span>
                      </template>
                      <template v-else-if="item.perfTestPassed === false">
                        <div class="text-status-error">{{ t('status.failed') }} <span>{{ item.perfTestFailureMessage }}</span></div>
                      </template>
                      <template v-else>
                        {{ props.enabledTestApiIds.PERFORMANCE.includes(item.id) ? t('status.unTested') : t('status.disabled') }}
                      </template>
                    </template>
                    <template #stabilityTestPassed>
                      <template v-if="item.stabilityTestPassed === true">
                        <span class="text-status-success">{{ t('status.passed') }}</span>
                      </template>
                      <template v-else-if="item.stabilityTestPassed === false">
                        <div class="text-status-error">{{ t('status.failed') }} <span>{{ item.stabilityTestFailureMessage }}</span></div>
                      </template>
                      <template v-else>
                        {{ props.enabledTestApiIds.STABILITY.includes(item.id) ? t('status.unTested') : t('status.disabled') }}
                      </template>
                    </template>
                  </Grid>
                </div>
              </template>
              <div class="px-1 flex h-6 items-center">
                <Icon :icon="caseTypeIconMapping[item.caseType?.value] || 'icon-jiekouyongli2'" class="mr-1 text-4" />
                <span class="min-w-0 truncate flex-1" :title="item.apisName || item.caseName || item.summary">{{ item.apisName || item.caseName || item.summary }}</span>
                <span
                  v-if="!item.enabledTest"
                  class="px-2 rounded">{{ t('status.disabled') }}
                </span>
                <span
                  v-else-if="!item.tested"
                  class="px-2 rounded">{{ t('xcan_httpTestInfo.untested') }}
                </span>
                <span
                  v-else-if="item.passed"
                  class="px-2 rounded text-status-success">
                  <template v-if="(props.enabledTestApiIds.FUNCTIONAL.includes(item.id) && item.funcTestPassed === undefined) || (props.enabledTestApiIds.PERFORMANCE.includes(item.id) && item.perfTestPassed === undefined) || (props.enabledTestApiIds.STABILITY.includes(item.id) && item.stabilityTestPassed === undefined)">
                    {{ t('xcan_httpTestInfo.partiallyPassed') }}
                  </template>
                  <template v-else>
                    {{ t('status.passed') }}
                  </template>
                </span>
                <span v-else class="px-2 rounded text-status-error ">{{ t('status.failed') }}</span>
              </div>
            </Tooltip>
          </div>
        </template>
      </BaseVirtualList>
      <NoData v-show="!filteredApiTestData.length" size="small" />
    </div>
  </div>
</template>
