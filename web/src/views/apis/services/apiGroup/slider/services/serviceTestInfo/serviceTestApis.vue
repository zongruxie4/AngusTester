<script lang="ts" setup>
import { watch, ref, onMounted, onBeforeUnmount, inject } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Tooltip, Grid, Input, Select, NoData, HttpMethodText } from '@xcan-angus/vue-ui';
import BaseVirtualList from '@/views/apis/services/apiGroup/list/baseVirtualList.vue';
import { debounce } from 'throttle-debounce';
import elementResizeDetector from 'element-resize-detector';

const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

interface API {
  apisName: string;
  caseId: string;
  passed: boolean; // 是否通过测试
  enabled?: boolean;
  caseName?: string;
  caseType?: {
    value: string
  }
}

interface Props {
  dataSource: API[];
  enabledTestApiIds: {
    FUNCTIONAL: string[];
    PERFORMANCE: string[];
    STABILITY: string[];
  }
}
const erd = elementResizeDetector({ strategy: 'scroll' });

const { t } = useI18n();

const columns = [
  [
    { dataIndex: 'funcTestPassed', label: t('service.serviceTestDetail.columns.functionalTest') },
    { dataIndex: 'perfTestPassed', label: t('service.serviceTestDetail.columns.performanceTest') },
    { dataIndex: 'stabilityTestPassed', label: t('service.serviceTestDetail.columns.stabilityTest') }
  ]
];

const statusOptions = [
  {
    label: t('service.serviceTestDetail.status.passed_'),
    value: 'passed'
  },
  {
    label: t('service.serviceTestDetail.status.unpassed'),
    value: 'unpassed'
  },
  {
    label: t('service.serviceTestDetail.status.unTested'),
    value: 'unTested'
  }
];
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([
    // { apisName: '12432342354235asdf阿萨德', caseId: '1123', passed: false }
  ]),
  enabledTestApiIds: () => ({
    FUNCTIONAL: [],
    PERFORMANCE: [],
    STABILITY: []
  })
});

const CaseTypeIconConfig = {
  SMOKE: 'icon-maoyanceshi',
  SECURITY: 'icon-anquanceshi',
  USER_DEFINED: 'icon-zidingyiceshi'
};

const showData = ref([]);
const keywords = ref();
const statusCode = ref();
const listWrapRef = ref();
const listHeight = ref(100);

const resizeHeight = () => {
  listHeight.value = listWrapRef.value.clientHeight;
};

const keywordsChange = debounce(500, () => {
  getShowData();
});

const getShowData = () => {
  showData.value = props.dataSource;
  if (keywords.value) {
    showData.value = showData.value.filter(i => i.summary.includes(keywords.value));
  }
  if (statusCode.value) {
    if (statusCode.value === 'passed') {
      showData.value = showData.value.filter(i => i.passed === true);
    } else if (statusCode.value === 'unpassed') {
      showData.value = showData.value.filter(i => i.tested === true && i.failed === true);
    } else {
      showData.value = showData.value.filter(i => i.tested === false);
    }
  }
};

const changeStatus = (opt: {value:string; label: string}) => {
  if (opt.value === statusCode.value) {
    statusCode.value = undefined;
  } else {
    statusCode.value = opt.value;
  }
  getShowData();
};

const openApiDetail = (api: {id: string; summary: string}) => {
  const { id, summary } = api;
  addTabPane({ _id: id + 'API', id, name: summary, value: 'API' });
};

onMounted(() => {
  watch(() => props.dataSource, () => {
    getShowData();
  }, {
    immediate: true
  });
  erd.listenTo(listWrapRef.value, resizeHeight);
});

onBeforeUnmount(() => {
  erd.removeListener(listWrapRef.value, resizeHeight);
});

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
                        <span class="text-status-success">{{ t('service.serviceTestDetail.status.passed') }}</span>
                      </template>
                      <template v-else-if="item.funcTestPassed === false">
                        <div class="text-status-error"> {{ t('service.serviceTestDetail.status.unpassed') }}<span>{{ item.funcTestFailureMessage }}</span></div>
                      </template>
                      <template v-else>
                        {{ props.enabledTestApiIds.FUNCTIONAL.includes(item.id) ? '未测试' : '未启用' }}
                      </template>
                    </template>
                    <template #perfTestPassed>
                      <template v-if="item.perfTestPassed === true">
                        <span class="text-status-success">{{ t('service.serviceTestDetail.status.passed') }}</span>
                      </template>
                      <template v-else-if="item.perfTestPassed === false">
                        <div class="text-status-error"> {{ t('service.serviceTestDetail.status.unpassed') }}<span>{{ item.perfTestFailureMessage }}</span></div>
                      </template>
                      <template v-else>
                        {{ props.enabledTestApiIds.PERFORMANCE.includes(item.id) ? '未测试' : '未启用' }}
                      </template>
                    </template>
                    <template #stabilityTestPassed>
                      <template v-if="item.stabilityTestPassed === true">
                        <span class="text-status-success">{{ t('service.serviceTestDetail.status.passed') }}</span>
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
                  class="px-2 rounded">{{ t('service.serviceTestDetail.status.unTested') }}
                </span>
                <span
                  v-else-if="!item.tested"
                  class="px-2 rounded">{{ t('service.serviceTestDetail.status.unTested') }}
                </span>
                <span
                  v-else-if="item.passed"
                  class="px-2 rounded text-status-success">
                  <template v-if="(props.enabledTestApiIds.FUNCTIONAL.includes(item.id) && item.funcTestPassed === undefined) || (props.enabledTestApiIds.PERFORMANCE.includes(item.id) && item.perfTestPassed === undefined) || (props.enabledTestApiIds.STABILITY.includes(item.id) && item.stabilityTestPassed === undefined)">
                    {{ t('service.serviceTestDetail.status.partiallyPassed') }}
                  </template>
                  <template v-else>
                    {{ t('service.serviceTestDetail.status.passed') }}
                  </template>
                </span>
                <span v-else class="px-2 rounded text-status-error ">{{ t('service.serviceTestDetail.status.unpassed') }}</span>
              </div>
            </Tooltip>
          </div>
        </template>
      </BaseVirtualList>
      <NoData v-show="!showData.length" size="small" />
    </div>
  </div>
</template>
