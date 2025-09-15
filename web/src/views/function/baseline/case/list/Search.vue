<script lang="ts" setup>
import { computed, defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Input, QuickSelect, ReviewStatus, SearchPanel, Select } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { Button, Switch } from 'ant-design-vue';
import { duration, appContext, Priority, ReviewStatus as ReviewStatusEnum } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { CaseTestResult } from '@/enums/enums';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import TaskPriority from '@/components/TaskPriority/index.vue';
import TestResult from '@/components/TestResult/index.vue';

const { t } = useI18n();

interface Props {
  established: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  established: false
});

const emits = defineEmits<{
  (e: 'change', value: { pageNo: number; pageSize: number, filters: any[] }),
  (e: 'handleAddCase')
}>();

type FilterOp =
  'EQUAL'
  | 'NOT_EQUAL'
  | 'GREATER_THAN'
  | 'GREATER_THAN_EQUAL'
  | 'LESS_THAN'
  | 'LESS_THAN_EQUAL'
  | 'CONTAIN'
  | 'NOT_CONTAIN'
  | 'MATCH'
  | 'MATCH'
  | 'IN'
  | 'NOT_IN';
type Filters = { key: string, value: string | boolean | string[], op: FilterOp }

const userInfo = ref(appContext.getUser());

const TagList = defineAsyncComponent(() => import('@/views/function/case/list/case/TagSelector.vue'));

const defaultUser = computed(() => {
  if (userInfo.value) {
    return { [userInfo.value.id]: { fullName: userInfo.value.fullName, id: userInfo.value.id } };
  }
  return {};
});

const NumberMatchCondition = ref<{ value: string, message: string }[]>([{ value: 'EQUAL', message: t('functionBaseline.case.equal') }]);
const searchPanelRef = ref();

const searchOptions = computed(() => [
  {
    placeholder: t('functionBaseline.case.queryCaseCodeName'),
    valueKey: 'name',
    type: 'input',
    allowClear: true
  },
  {
    placeholder: t('functionBaseline.case.selectCreator'),
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: t('functionBaseline.case.selectTester'),
    valueKey: 'testerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: t('functionBaseline.case.selectDeveloper'),
    valueKey: 'developerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: t('functionBaseline.case.selectPriority'),
    valueKey: 'priority',
    type: 'select-enum',
    enumKey: Priority,
    allowClear: true
  },
  {
    placeholder: t('functionBaseline.case.selectTestResult'),
    valueKey: 'testResult',
    type: 'select-enum',
    enumKey: CaseTestResult,
    allowClear: true
  },
  {
    placeholder: t('functionBaseline.case.selectReviewer'),
    valueKey: 'reviewerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: t('functionBaseline.case.selectReviewStatus'),
    valueKey: 'reviewStatus',
    type: 'select-enum',
    enumKey: ReviewStatusEnum,
    allowClear: true
  },
  {
    placeholder: [t('functionBaseline.case.searchPanel.reviewDateFrom'), t('functionBaseline.case.searchPanel.reviewDateTo')],
    valueKey: 'reviewDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [t('functionBaseline.case.searchPanel.updateDateFrom'), t('functionBaseline.case.searchPanel.updateDateTo')],
    valueKey: 'lastModifiedDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [t('functionBaseline.case.searchPanel.deadlineDateFrom'), t('functionBaseline.case.searchPanel.deadlineDateTo')],
    valueKey: 'deadlineDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [t('functionBaseline.case.searchPanel.createdDateFrom'), t('functionBaseline.case.searchPanel.createdDateTo')],
    valueKey: 'createdDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [t('functionBaseline.case.searchPanel.testResultHandleDateFrom'), t('functionBaseline.case.searchPanel.testResultHandleDateTo')],
    valueKey: 'testResultHandleDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    valueKey: 'testNum'
  },
  {
    valueKey: 'testFailNum'
  },
  {
    valueKey: 'reviewNum'
  }
].filter(Boolean));
const qulickList = [
  {
    type: 'all',
    name: t('quickSearchTags.all'),
    selected: false,
    group: 'all'
  },
  {
    type: 'createdBy',
    name: t('quickSearchTags.addByMe'),
    selected: false,
    group: 'createdBy'
  },
  {
    type: 'testerId',
    name: t('quickSearchTags.tester'),
    selected: false,
    group: 'testerId'
  },
  {
    type: 'lastDay',
    name: t('quickSearchTags.past1Day'),
    selected: false,
    group: 'time'
  },
  {
    type: 'lastThreeDays',
    name: t('quickSearchTags.past3Day'),
    selected: false,
    group: 'time'
  },
  {
    type: 'lastWeek',
    name: t('quickSearchTags.past7Day'),
    selected: false,
    group: 'time'
  }
];
const quickSelectDate = ref<string[]>([]);
const selectedTypes = ref<string[]>([]);
const tagIds = ref<string[]>([]);
const testNum = ref(''); // 测试次数
const testNumScope = ref<FilterOp>('EQUAL'); // 测试次数p
const testFailNum = ref(''); // 失败次数
const testFailScope = ref<FilterOp>('EQUAL'); // 失败次数op
const reviewNum = ref(''); // 评审次数
const reviewNumScope = ref<FilterOp>('EQUAL'); // 评审次数op
const overdue = ref(false);

const setParamsAndLoadData = () => {
  params.value.pageNo = 1;
  setParamsFilters();
  setQuickType();
  loadData();
};

const tagChange = (_tagIds: string[]) => {
  tagIds.value = _tagIds;
  setParamsAndLoadData();
};
// 比较快速选中的时间和搜索框时间是否一致
const getCreatedDateIsQuickDate = (createdDateItems, quickSelectDate) => {
  let hasUpDateTime = false;
  if (createdDateItems.length) {
    for (let i = 0; i < createdDateItems.length; i++) {
      const item = createdDateItems[i];
      if (item.key === 'createdDate' && item.op === 'GREATER_THAN_EQUAL') {
        hasUpDateTime = item.value === quickSelectDate[0];
      }

      if (item.key === 'createdDate' && item.op === 'LESS_THAN_EQUAL') {
        hasUpDateTime = item.value === quickSelectDate[1];
      }
    }
  }
  return hasUpDateTime;
};

const setQuickType = () => {
  const _filters = params.value.filters || [];
  if (_filters.length === 0) {
    selectedTypes.value = ['all'];
    return;
  }

  const filterKeys = _filters.map(item => item.key);
  if (filterKeys.every(item => ['planId', 'tagId'].includes(item))) {
    selectedTypes.value = ['all'];
    return;
  }

  selectedTypes.value = selectedTypes.value.filter(item => item !== 'all');
  const createdByElement = _filters.find(element => element.key === 'createdBy');
  const testerIdItem = _filters.find(item => item.key === 'testerId');
  const testResultItem = _filters.find(item => item.key === 'testResult');
  const createdDateItems = _filters.filter(item => item.key === 'createdDate');
  if (createdByElement?.value === userInfo.value.id) {
    if (!selectedTypes.value.includes('createdBy')) {
      selectedTypes.value.push('createdBy');
    }
  } else {
    if (selectedTypes.value.includes('createdBy')) {
      selectedTypes.value = selectedTypes.value.filter(item => item !== 'createdBy');
    }
  }
  if (testerIdItem?.value === userInfo.value.id && testResultItem?.value === 'PENDING') {
    if (!selectedTypes.value.includes('testerId')) {
      selectedTypes.value.push('testerId');
    }
  } else {
    if (selectedTypes.value.includes('testerId')) {
      selectedTypes.value = selectedTypes.value.filter(item => item !== 'testerId');
    }
  }

  if (createdDateItems.length && (selectedTypes.value.includes('lastDay') || selectedTypes.value.includes('lastThreeDays') || selectedTypes.value.includes('lastWeek'))) {
    let hasUpDateTime = false;
    for (let i = 0; i < createdDateItems.length; i++) {
      const item = createdDateItems[i];
      if (item.key === 'createdDate' && item.op === 'GREATER_THAN_EQUAL') {
        hasUpDateTime = item.value === quickSelectDate.value[0];
      }

      if (item.key === 'createdDate' && item.op === 'LESS_THAN_EQUAL') {
        hasUpDateTime = item.value === quickSelectDate.value[1];
      }
    }

    if (!hasUpDateTime) {
      selectedTypes.value = selectedTypes.value.filter(item => item !== 'lastDay' && item !== 'lastThreeDays' && item !== 'lastWeek');
    }
  } else {
    selectedTypes.value = selectedTypes.value.filter(item => item !== 'lastDay' && item !== 'lastThreeDays' && item !== 'lastWeek');
  }
};

const getQuickDate = (type) => {
  let _startDate: Dayjs | undefined;
  let _endDate: Dayjs | undefined;

  if (type === 'lastDay') {
    _startDate = dayjs().startOf('date');
    _endDate = dayjs().endOf('date');
  }

  if (type === 'lastThreeDays') {
    _startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    _endDate = dayjs();
  }

  if (type === 'lastWeek') {
    _startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    _endDate = dayjs();
  }

  return [_startDate ? _startDate.format(DATE_TIME_FORMAT) : '', _endDate ? _endDate.format(DATE_TIME_FORMAT) : ''];
};

const overdueChange = (_val) => {
  overdue.value = _val;
  setParamsAndLoadData();
};

const searchData = ref<Filters[]>([]);
const params = ref({
  pageNo: 1,
  pageSize: 10,
  filters: []
});

const handleTimesChange = debounce(duration.resize, (value: string, type: 'testNum' | 'testFailNum' | 'reviewNum') => {
  if (type === 'testNum') {
    testNum.value = value;
  }
  if (type === 'testFailNum') {
    testFailNum.value = value;
  }
  if (type === 'reviewNum') {
    reviewNum.value = value;
  }

  setParamsAndLoadData();
});

const handleScopeChange = (value: FilterOp, type: 'testNum' | 'testFailNum' | 'reviewNum') => {
  if (type === 'testNum') {
    testNumScope.value = value;
    if (!testNum.value) {
      return;
    }
  }
  if (type === 'testFailNum') {
    testFailScope.value = value;
    if (!testFailNum.value) {
      return;
    }
  }
  if (type === 'reviewNum') {
    reviewNumScope.value = value;
    if (!reviewNum.value) {
      return;
    }
  }

  setParamsAndLoadData();
};

const quickSearchChange = (types: string[], allType: boolean) => {
  params.value.pageNo = 1;
  selectedTypes.value = types;
  const _filters = params.value.filters || [];
  const createdByItem = _filters.find(item => item.key === 'createdBy');
  const testerIdItem = _filters.find(item => item.key === 'testerId');
  const testResultItem = _filters.find(item => item.key === 'testResult');
  const createdDateItems = _filters.filter(item => item.key === 'createdDate');
  if (types.includes('all')) {
    // 主动选中全部
    if (allType) {
      testNum.value = '';
      testFailNum.value = '';
      reviewNum.value = '';
      overdue.value = false;
      if (_filters?.length) {
        const ohtersParams = _filters;
        if (ohtersParams.length) {
          searchPanelRef.value.clear();
        } else {
          loadData();
        }
      }
      return;
    }

    if (_filters?.length) {
      const hasUpDateTime = getCreatedDateIsQuickDate(createdDateItems, quickSelectDate.value);

      const configItems = [];
      if (hasUpDateTime) {
        configItems.push({ valueKey: 'createdDate', value: undefined });
      }

      if (createdByItem?.value === userInfo.value.id) {
        configItems.push({ valueKey: 'createdBy', value: undefined });
      }

      if (testerIdItem?.value === userInfo.value.id && testResultItem?.value === 'PENDING') {
        configItems.push({ valueKey: 'testerId', value: undefined });
        configItems.push({ valueKey: 'testResult', value: undefined });
      }

      if (configItems.length) {
        searchPanelRef.value.setConfigs(configItems);
      } else {
        selectedTypes.value = [];
      }
    }

    return;
  }

  if (types.includes('lastDay') || types.includes('lastThreeDays') || types.includes('lastWeek')) {
    if (types.includes('lastDay')) {
      const _date = getQuickDate('lastDay');
      quickSelectDate.value = _date;
    }

    if (types.includes('lastThreeDays')) {
      const _date = getQuickDate('lastThreeDays');
      quickSelectDate.value = _date;
    }

    if (types.includes('lastWeek')) {
      const _date = getQuickDate('lastWeek');
      quickSelectDate.value = _date;
    }
  }

  const configItems: { valueKey: string, value: string, defaultOptions: Record<string, any> }[] = [];

  if (types.includes('createdBy')) {
    configItems.push({ valueKey: 'createdBy', value: userInfo.value.id, defaultOptions: defaultUser.value });
  } else {
    if (createdByItem?.value === userInfo.value.id) {
      configItems.push({ valueKey: 'createdBy', value: undefined });
    }
  }

  if (types.includes('testerId')) {
    configItems.push({ valueKey: 'testerId', value: userInfo.value.id, defaultOptions: defaultUser.value });
    configItems.push({ valueKey: 'testResult', value: 'PENDING' });
  } else {
    if (testerIdItem?.value === userInfo.value.id && testResultItem?.value === 'PENDING') {
      configItems.push({ valueKey: 'testerId', value: undefined });
      configItems.push({ valueKey: 'testResult', value: undefined });
    }
  }

  if (types.includes('lastDay') || types.includes('lastThreeDays') || types.includes('lastWeek')) {
    configItems.push({ valueKey: 'createdDate', value: quickSelectDate.value });
  } else {
    const hasUpDateTime = getCreatedDateIsQuickDate(createdDateItems, quickSelectDate.value);
    if (hasUpDateTime) {
      configItems.push({ valueKey: 'createdDate', value: undefined });
    }
  }

  if (configItems.length) {
    searchPanelRef.value.setConfigs(configItems);
  }
};

const setParamsFilters = () => {
  const othersData: Filters[] = [];
  if (testNum.value) {
    othersData.push({ key: 'testNum', op: testNumScope.value, value: testNum.value });
  }
  if (testFailNum.value) {
    othersData.push({ key: 'testFailNum', op: testFailScope.value, value: testFailNum.value });
  }
  if (testFailNum.value) {
    othersData.push({ key: 'reviewNum', op: reviewNumScope.value, value: reviewNum.value });
  }

  if (overdue.value) {
    othersData.push({
      key: 'overdue',
      op: 'EQUAL',
      value: true
    });
  }

  if (tagIds.value?.length) {
    const set = new Set(tagIds.value);
    othersData.push({
      key: 'tagId',
      op: 'IN',
      value: Array.from(set)
    });
  }

  params.value.filters = [...searchData.value, ...othersData];
};
const searchChange = (data: Filters[], _header, valueKey) => {
  params.value.pageNo = 1;
  searchData.value = data;
  setParamsFilters();

  if (valueKey) {
    setQuickType();
  } else {
    if (selectedTypes.value.includes('all')) {
      const _filters = params.value.filters.filter(item => !['planId', 'tagId'].includes(item.key));
      if (_filters.length) {
        selectedTypes.value = [];
      }
    }
  }

  loadData();
};

const loadData = () => {
  emits('change', params.value);
};

const handleAddCase = () => {
  emits('handleAddCase');
};

</script>

<template>
  <div>
    <div class="flex-shrink-0 flex justify-between items-start">
      <div class="flex items-center flex-wrap">
        <QuickSelect
          class="mb-3"
          :list="qulickList"
          :selectedTypes="selectedTypes"
          @change="quickSearchChange" />
        <div class="px-4 h-7 leading-7 mb-3">
          <span>{{ t('functionBaseline.case.searchPanel.overdue') }}</span>
          <Colon class="mr-2" />
          <Switch
            :checked="overdue"
            size="small"
            class="w-8"
            @change="overdueChange" />
        </div>
        <TagList
          ref="tagListRef"
          class="mb-3 mr-5"
          :tagIds="tagIds"
          @change="tagChange" />
      </div>
      <div class="flex-1 text-right">
        <Button
          :disabled="props.established"
          size="small"
          type="primary"
          @click="handleAddCase">
          <Icon icon="icon-jia" class="mr-1" />
          {{ t('functionBaseline.case.searchPanel.addCase') }}
        </Button>
      </div>
    </div>
    <SearchPanel
      ref="searchPanelRef"
      class="mb-3 flex-shrink-0"
      :options="searchOptions"
      :width="296"
      @change="searchChange">
      <template #planId_option="item">
        <div class="flex items-center" :title="item.name">
          <Icon icon="icon-jihua" class="mr-1 text-3.5 flex-none" />
          <div class="truncate">{{ item.name }}</div>
        </div>
      </template>
      <template #moduleId_option="item">
        <div class="flex items-center" :title="item.name">
          <Icon icon="icon-mokuai" class="mr-1 text-3.5 flex-none" />
          <div class="truncate">{{ item.name }}</div>
        </div>
      </template>
      <template #priority_option="item">
        <TaskPriority :value="item" />
      </template>
      <template #testResult_option="item">
        <TestResult :value="item" />
      </template>
      <template #reviewStatus_option="item">
        <ReviewStatus :value="item" />
      </template>
      <template #testNum>
        <Input
          :value="testNum"
          data-type="float"
          size="small"
          allowClear
          :placeholder="t('functionBaseline.case.searchPanel.testNumPlaceholder')"
          style="width: 296px;"
          :min="0"
          :debounce="500"
          @change="(event: ChangeEvent) => handleTimesChange(event.target.value, 'testNum')">
          <template #prefix>
            <Select
              :value="testNumScope"
              size="small"
              :options="NumberMatchCondition"
              :fieldNames="{ label: 'message', value: 'value' }"
              :allowClear="false"
              :bordered="false"
              class="w-24"
              @change="handleScopeChange($event, 'testNum')" />
          </template>
        </Input>
      </template>
      <template #testFailNum>
        <Input
          :value="testFailNum"
          data-type="float"
          size="small"
          allowClear
          :placeholder="t('functionBaseline.case.searchPanel.testFailNumPlaceholder')"
          style="width: 296px;"
          :min="0"
          :debounce="500"
          @change="handleTimesChange($event.target.value, 'testFailNum')">
          <template #prefix>
            <Select
              v-model:value="testFailScope"
              size="small"
              :options="NumberMatchCondition"
              :fieldNames="{ label: 'message', value: 'value' }"
              :allowClear="false"
              :bordered="false"
              class="w-24"
              @change="handleScopeChange($event, 'testFailNum')" />
          </template>
        </Input>
      </template>
      <template #reviewNum>
        <Input
          :value="reviewNum"
          data-type="float"
          size="small"
          allowClear
          :placeholder="t('functionBaseline.case.searchPanel.reviewNumPlaceholder')"
          style="width: 296px;"
          :min="0"
          :debounce="500"
          @change="(event: ChangeEvent) => handleTimesChange(event.target.value, 'reviewNum')">
          <template #prefix>
            <Select
              :value="reviewNumScope"
              size="small"
              :options="NumberMatchCondition"
              :fieldNames="{ label: 'message', value: 'value' }"
              :allowClear="false"
              :bordered="false"
              class="w-24"
              @change="handleScopeChange($event, 'reviewNum')" />
          </template>
        </Input>
      </template>
    </SearchPanel>
  </div>
</template>
