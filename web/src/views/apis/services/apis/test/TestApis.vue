<script lang="ts" setup>
import { watch, ref, onMounted, onBeforeUnmount, inject } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Tooltip, Grid, Input, NoData, HttpMethodText } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import elementResizeDetector from 'element-resize-detector';
import { APITestResult } from '@/views/apis/services/apis/types';

import BaseVirtualList from '@/views/apis/services/apis/list/BaseVirtualList.vue';

// Status filter literal type
type StatusCode = 'passed' | 'unpassed' | 'unTested';

/**
 * Props: data list and enabled API ids for each test type
 */
interface Props {
  dataSource: APITestResult[];
  enabledTestApiIds: {
    FUNCTIONAL: string[];
    PERFORMANCE: string[];
    STABILITY: string[];
  }
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([]),
  enabledTestApiIds: () => ({
    FUNCTIONAL: [],
    PERFORMANCE: [],
    STABILITY: []
  })
});

// Open a new tab in parent container
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

const erd = elementResizeDetector({ strategy: 'scroll' });

const { t } = useI18n();

// Reactive states for filtering and list sizing
const showData = ref<APITestResult[]>([]);
const keywords = ref<string | undefined>();
const statusCode = ref<StatusCode | undefined>();
const listWrapRef = ref<HTMLElement | null>(null);
const listHeight = ref(100);

// Recalculate virtual list height on container resize
const resizeHeight = () => {
  if (listWrapRef.value) listHeight.value = listWrapRef.value.clientHeight;
};

// Debounced keyword filtering
const keywordsChange = debounce(500, () => {
  getShowData();
});

/**
 * Apply keyword and status filters to the data source
 */
const getShowData = () => {
  const source = props.dataSource || [];
  let data = source;
  if (keywords.value) {
    const kw = keywords.value.toLowerCase();
    data = data.filter(i => (i.summary || i.apisName || i.caseName || '').toLowerCase().includes(kw));
  }
  if (statusCode.value) {
    if (statusCode.value === 'passed') {
      data = data.filter(i => i.passed === true);
    } else if (statusCode.value === 'unpassed') {
      data = data.filter(i => i.tested === true && i.failed === true);
    } else {
      data = data.filter(i => i.tested === false);
    }
  }
  showData.value = data;
};

// Toggle status filter
const changeStatus = (opt: {value: StatusCode; label: string}) => {
  if (opt.value === statusCode.value) {
    statusCode.value = undefined;
  } else {
    statusCode.value = opt.value;
  }
  getShowData();
};

// Open API detail in a new tab
const openApiDetail = (api: {id: string; summary: string}) => {
  const { id, summary } = api;
  addTabPane({ _id: id + 'API', id, name: summary, value: 'API' });
};

// Initialize filtering and height listeners
onMounted(() => {
  watch(() => props.dataSource, () => {
    getShowData();
  }, {
    immediate: true
  });
  if (listWrapRef.value) erd.listenTo(listWrapRef.value, resizeHeight);
});

onBeforeUnmount(() => {
  if (listWrapRef.value) erd.removeListener(listWrapRef.value, resizeHeight);
});

// Grid columns for per-type results inside tooltip
const columns = [
  [
    { dataIndex: 'funcTestPassed', label: t('service.serviceTestDetail.columns.functionalTest') },
    { dataIndex: 'perfTestPassed', label: t('service.serviceTestDetail.columns.performanceTest') },
    { dataIndex: 'stabilityTestPassed', label: t('service.serviceTestDetail.columns.stabilityTest') }
  ]
];

// Status filter options
const statusOptions: { label: string; value: StatusCode }[] = [
  {
    label: t('status.passed'),
    value: 'passed'
  },
  {
    label: t('status.notPassed'),
    value: 'unpassed'
  },
  {
    label: t('status.notTested'),
    value: 'unTested'
  }
];

// Case type to icon mapping
const CaseTypeIconConfig = {
  SMOKE: 'icon-maoyanceshi',
  SECURITY: 'icon-anquanceshi',
  USER_DEFINED: 'icon-zidingyiceshi'
};
</script>
<template>
  <div class="text-3 space-y-1 min-h-0 flex flex-col">
    <div class="flex space-x-2">
      <Input
        v-model:value="keywords"
        class="w-100"
        :allowClear="true"
        :placeholder="t('service.serviceTestDetail.placeholder.searchName')"
        @change="keywordsChange" />
      <div class="inline-flex items-center space-x-3">
        <div
          v-for="opt in statusOptions"
          :key="opt.value"
          class="py-1 px-2 rounded bg-gray-light cursor-pointer"
          :class="{'!bg-blue-bg text-white': opt.value === statusCode}"
          @click="changeStatus(opt)">
          {{ opt.label }}
        </div>
      </div>
    </div>
    <div ref="listWrapRef" class="flex-1 min-h-50 mt-2">
      <BaseVirtualList
        v-show="showData.length"
        :data="showData"
        :height="listHeight"
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
                    :columns="columns">
                    <template #funcTestPassed>
                      <template v-if="item.funcTestPassed === true">
                        <span class="text-status-success">{{ t('status.passed') }}</span>
                      </template>
                      <template v-else-if="item.funcTestPassed === false">
                        <div class="text-status-error"> {{ t('status.notPassed') }}<span>{{ item.funcTestFailureMessage }}</span></div>
                      </template>
                      <template v-else>
                        {{ props.enabledTestApiIds.FUNCTIONAL.includes(item.id) ? t('status.notTested') : t('status.disabled') }}
                      </template>
                    </template>
                    <template #perfTestPassed>
                      <template v-if="item.perfTestPassed === true">
                        <span class="text-status-success">{{ t('status.passed') }}</span>
                      </template>
                      <template v-else-if="item.perfTestPassed === false">
                        <div class="text-status-error"> {{ t('status.notPassed') }}<span>{{ item.perfTestFailureMessage }}</span></div>
                      </template>
                      <template v-else>
                        {{ props.enabledTestApiIds.PERFORMANCE.includes(item.id) ? t('status.notTested') : t('status.disabled') }}
                      </template>
                    </template>
                    <template #stabilityTestPassed>
                      <template v-if="item.stabilityTestPassed === true">
                        <span class="text-status-success">{{ t('status.passed') }}</span>
                      </template>
                      <template v-else-if="item.stabilityTestPassed === false">
                        <div class="text-status-error"> {{ t('status.notPassed') }}<span>{{ item.stabilityTestFailureMessage }}</span></div>
                      </template>
                      <template v-else>
                        {{ props.enabledTestApiIds.STABILITY.includes(item.id) ? t('status.notTested') : t('status.disabled') }}
                      </template>
                    </template>
                  </Grid>
                </div>
              </template>
              <div class="px-1 flex h-6 items-center">
                <Icon :icon="CaseTypeIconConfig[item.caseType?.value] || 'icon-jiekouyongli2'" class="mr-1 text-4" />
                <HttpMethodText :value="item.method" />
                <span class="min-w-0 truncate flex-1" :title="item.url">{{ item.url }}</span>
                <span
                  class="min-w-0 truncate flex-1 cursor-pointer text-theme-special"
                  :title="item.apisName || item.caseName || item.summary"
                  @click="openApiDetail(item)">{{ item.apisName || item.caseName || item.summary }}</span>
                <span
                  v-if="!item.enabledTest"
                  class="px-2 rounded">{{ t('status.notTested') }}
                </span>
                <span
                  v-else-if="!item.tested"
                  class="px-2 rounded">{{ t('status.notTested') }}
                </span>
                <span
                  v-else-if="item.passed"
                  class="px-2 rounded text-status-success">
                  <template v-if="(props.enabledTestApiIds.FUNCTIONAL.includes(item.id) && item.funcTestPassed === undefined) || (props.enabledTestApiIds.PERFORMANCE.includes(item.id) && item.perfTestPassed === undefined) || (props.enabledTestApiIds.STABILITY.includes(item.id) && item.stabilityTestPassed === undefined)">
                    {{ t('service.serviceTestDetail.status.partiallyPassed') }}
                  </template>
                  <template v-else>
                    {{ t('status.passed') }}
                  </template>
                </span>
                <span v-else class="px-2 rounded text-status-error ">{{ t('status.notPassed') }}</span>
              </div>
            </Tooltip>
          </div>
        </template>
      </BaseVirtualList>
      <NoData v-show="!showData.length" size="small" />
    </div>
  </div>
</template>
