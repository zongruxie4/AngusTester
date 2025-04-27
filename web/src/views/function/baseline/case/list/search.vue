<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, ref } from 'vue';
import { Icon, Input, QuickSelect, ReviewStatus, SearchPanel, Select, TaskPriority, TestResult } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { Button, Switch } from 'ant-design-vue';
import { duration } from '@xcan-angus/tools';
import dayjs from 'dayjs';

interface Props {
  establishedFlag: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  establishedFlag: false
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
  | 'MATCH_END'
  | 'MATCH'
  | 'IN'
  | 'NOT_IN';
type Filters = { key: string, value: string | boolean | string[], op: FilterOp }

const userInfo = inject('tenantInfo', ref());

const TagList = defineAsyncComponent(() => import('@/views/function/case/list/case/tagSelector/index.vue'));

const defaultUser = computed(() => {
  if (userInfo.value) {
    return { [userInfo.value.id]: { fullname: userInfo.value.fullname, id: userInfo.value.id } };
  }
  return {};
});

const NumberMatchCondition = ref<{ value: string, message: string }[]>([{ value: 'EQUAL', message: '等于' }]);
const searchPanelRef = ref();

const searchOptions = computed(() => [
  {
    placeholder: '查询用例编号、名称',
    valueKey: 'name',
    type: 'input',
    allowClear: true
  },
  // !enabledGroup.value && {
  //   valueKey: 'moduleId',
  //   placeholder: '选择或查询所属模块',
  //   type: 'tree-select',
  //   action: `${TESTER}/module/search?projectId=${projectInfo.value.id}`,
  //   showSearch: true,
  //   allowClear: true,
  //   fieldNames: { value: 'id', label: 'name' }
  // },
  {
    placeholder: '选择添加人',
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: '选择测试人',
    valueKey: 'testerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: '选择开发人',
    valueKey: 'developerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: '选择优先级',
    valueKey: 'priority',
    type: 'select-enum',
    enumKey: 'Priority',
    allowClear: true
  },
  {
    placeholder: '选择测试结果',
    valueKey: 'testResult',
    type: 'select-enum',
    enumKey: 'CaseTestResult',
    allowClear: true
  },
  {
    placeholder: '选择评审人',
    valueKey: 'reviewerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: '选择评审状态',
    valueKey: 'reviewStatus',
    type: 'select-enum',
    enumKey: 'ReviewStatus',
    allowClear: true
  },
  {
    placeholder: ['评审时间从', '评审时间到'],
    valueKey: 'reviewDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: ['更新时间从', '更新时间到'],
    valueKey: 'lastModifiedDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: ['完成时间从', '完成时间到'],
    valueKey: 'deadlineDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: ['添加时间从', '添加时间到'],
    valueKey: 'createdDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: ['测试时间从', '测试时间到'],
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
    name: '所有',
    selected: false,
    group: 'all'
  },
  {
    type: 'createdBy',
    name: '我添加的',
    selected: false,
    group: 'createdBy'
  },
  {
    type: 'testerId',
    name: '待我测试',
    selected: false,
    group: 'testerId'
  },
  {
    type: 'lastDay',
    name: '近1天',
    selected: false,
    group: 'time'
  },
  {
    type: 'lastThreeDays',
    name: '近3天',
    selected: false,
    group: 'time'
  },
  {
    type: 'lastWeek',
    name: '近7天',
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
const overdueFlag = ref(false);

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

  return [_startDate ? _startDate.format('YYYY-MM-DD HH:mm:ss') : '', _endDate ? _endDate.format('YYYY-MM-DD HH:mm:ss') : ''];
};

const overdueFlagChange = (_val) => {
  overdueFlag.value = _val;
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
      overdueFlag.value = false;
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

  if (overdueFlag.value) {
    othersData.push({
      key: 'overdueFlag',
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
          <span>已逾期</span>
          <Colon class="mr-2" />
          <Switch
            :checked="overdueFlag"
            size="small"
            class="w-8"
            @change="overdueFlagChange" />
        </div>
        <TagList
          ref="tagListRef"
          class="mb-3 mr-5"
          :tagIds="tagIds"
          @change="tagChange" />
      </div>
      <div class="flex-1 text-right">
        <Button
          :disabled="props.establishedFlag"
          size="small"
          type="primary"
          @click="handleAddCase">
          <Icon icon="icon-jia" class="mr-1" />
          添加基线用例
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
          placeholder="测试次数"
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
          placeholder="失败次数"
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
          placeholder="评审次数"
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
