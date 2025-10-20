<script lang="ts" setup>
import { computed, defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Input, QuickSelect, ReviewStatus, SearchPanel, Select } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { Button, Switch } from 'ant-design-vue';
import { appContext, duration, Priority, ReviewStatus as ReviewStatusEnum, SearchCriteria } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { CaseTestResult } from '@/enums/enums';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import TaskPriority from '@/components/task/TaskPriority/index.vue';
import TestResult from '@/components/test/TestResult/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
const TagList = defineAsyncComponent(() => import('@/views/test/case/list/TagSelector.vue'));

const { t } = useI18n();

// Props Definition
interface Props {
  established: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  established: false
});

// Emits Definition

const emits = defineEmits<{
  (e: 'change', value: { pageNo: number; pageSize: number, filters: SearchCriteria[] }),
  (e: 'handleAddCase')
}>();

// Reactive Data
const currentUserInfo = ref(appContext.getUser());

/**
 * Get default user options for select components
 */
const defaultUserOptions = computed(() => {
  if (currentUserInfo.value) {
    return { [currentUserInfo.value.id]: { fullName: currentUserInfo.value.fullName, id: currentUserInfo.value.id } };
  }
  return {};
});

const searchPanelRef = ref();

/**
 * Get search panel options configuration
 */
const searchPanelOptions = computed(() => [
  {
    placeholder: t('common.placeholders.searchKeyword'),
    valueKey: 'name',
    type: 'input',
    allowClear: true
  },
  {
    placeholder: t('common.placeholders.selectCreator'),
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: t('common.placeholders.selectTester'),
    valueKey: 'testerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: t('common.placeholders.selectDeveloper'),
    valueKey: 'developerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: t('common.placeholders.selectPriority'),
    valueKey: 'priority',
    type: 'select-enum',
    enumKey: Priority,
    allowClear: true
  },
  {
    placeholder: t('common.placeholders.selectTestResult'),
    valueKey: 'testResult',
    type: 'select-enum',
    enumKey: CaseTestResult,
    allowClear: true
  },
  {
    placeholder: t('common.placeholders.selectReviewer'),
    valueKey: 'reviewerId',
    type: 'select-user',
    allowClear: true
  },
  {
    placeholder: t('common.placeholders.selectReviewStatus'),
    valueKey: 'reviewStatus',
    type: 'select-enum',
    enumKey: ReviewStatusEnum,
    allowClear: true
  },
  {
    placeholder: [
      t('common.placeholders.selectReviewDateRange.0'),
      t('common.placeholders.selectReviewDateRange.1')
    ],
    valueKey: 'reviewDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [
      t('common.placeholders.selectModifiedDateRange.0'),
      t('common.placeholders.selectModifiedDateRange.1')
    ],
    valueKey: 'lastModifiedDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [
      t('common.placeholders.selectDeadlineRange.0'),
      t('common.placeholders.selectDeadlineRange.1')
    ],
    valueKey: 'deadlineDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [
      t('common.placeholders.selectCreatedDateRange.0'),
      t('common.placeholders.selectCreatedDateRange.1')
    ],
    valueKey: 'createdDate',
    type: 'date-range',
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [
      t('common.placeholders.selectProcessedDateRange.0'),
      t('common.placeholders.selectProcessedDateRange.1')
    ],
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

// Quick Search Configuration
const quickSearchOptions = [
  {
    type: 'all',
    name: t('common.all'),
    selected: false,
    group: 'all'
  },
  {
    type: 'createdBy',
    name: t('quickSearch.createdByMe'),
    selected: false,
    group: 'createdBy'
  },
  {
    type: 'testerId',
    name: t('common.tester'),
    selected: false,
    group: 'testerId'
  },
  {
    type: 'last1Day',
    name: t('quickSearch.last1Day'),
    selected: false,
    group: 'time'
  },
  {
    type: 'last3Days',
    name: t('quickSearch.last3Days'),
    selected: false,
    group: 'time'
  },
  {
    type: 'last7Days',
    name: t('quickSearch.last7Days'),
    selected: false,
    group: 'time'
  }
];

// Search State
const quickSelectDateRange = ref<string[]>([]);
const selectedQuickSearchTypes = ref<string[]>([]);
const selectedTagIds = ref<string[]>([]);
const testExecutionCount = ref('');
const testExecutionCountScope = ref<SearchCriteria.OpEnum>(SearchCriteria.OpEnum.Equal);
const testFailureCount = ref('');
const testFailureCountScope = ref<SearchCriteria.OpEnum>(SearchCriteria.OpEnum.Equal);
const reviewCount = ref('');
const reviewCountScope = ref<SearchCriteria.OpEnum>(SearchCriteria.OpEnum.Equal);
const isOverdueFilterEnabled = ref(false);

// Search Parameters
const searchParameters = ref({
  pageNo: 1,
  pageSize: 10,
  filters: []
});

const searchData = ref<SearchCriteria[]>([]);

/**
 * Update search parameters and trigger data loading
 */
const updateSearchParametersAndLoad = () => {
  searchParameters.value.pageNo = 1;
  setSearchFilters();
  updateQuickSearchTypes();
  loadSearchData();
};

/**
 * Handle tag selection change
 * @param tagIds - Array of selected tag IDs
 */
const handleTagSelectionChange = (tagIds: string[]) => {
  selectedTagIds.value = tagIds;
  updateSearchParametersAndLoad();
};

/**
 * Check if created date matches quick select date range
 * @param createdDateItems - Array of created date filter items
 * @param quickSelectDate - Quick select date range
 * @returns Whether the dates match
 */
const isCreatedDateMatchingQuickSelect = (createdDateItems, quickSelectDate) => {
  let hasMatchingDateTime = false;
  if (createdDateItems.length) {
    for (let i = 0; i < createdDateItems.length; i++) {
      const item = createdDateItems[i];
      if (item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.GreaterThanEqual) {
        hasMatchingDateTime = item.value === quickSelectDate[0];
      }

      if (item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.LessThanEqual) {
        hasMatchingDateTime = item.value === quickSelectDate[1];
      }
    }
  }
  return hasMatchingDateTime;
};

/**
 * Update quick search types based on current filters
 */
const updateQuickSearchTypes = () => {
  const currentFilters = searchParameters.value.filters || [];
  if (currentFilters.length === 0) {
    selectedQuickSearchTypes.value = ['all'];
    return;
  }

  const filterKeys = currentFilters.map(item => item.key);
  if (filterKeys.every(item => ['planId', 'tagId'].includes(item))) {
    selectedQuickSearchTypes.value = ['all'];
    return;
  }

  selectedQuickSearchTypes.value = selectedQuickSearchTypes.value.filter(item => item !== 'all');
  const createdByElement = currentFilters.find(element => element.key === 'createdBy');
  const testerIdItem = currentFilters.find(item => item.key === 'testerId');
  const testResultItem = currentFilters.find(item => item.key === 'testResult');
  const createdDateItems = currentFilters.filter(item => item.key === 'createdDate');
  if (createdByElement?.value === currentUserInfo.value.id) {
    if (!selectedQuickSearchTypes.value.includes('createdBy')) {
      selectedQuickSearchTypes.value.push('createdBy');
    }
  } else {
    if (selectedQuickSearchTypes.value.includes('createdBy')) {
      selectedQuickSearchTypes.value = selectedQuickSearchTypes.value.filter(item => item !== 'createdBy');
    }
  }
  if (testerIdItem?.value === currentUserInfo.value.id && testResultItem?.value === CaseTestResult.PENDING) {
    if (!selectedQuickSearchTypes.value.includes('testerId')) {
      selectedQuickSearchTypes.value.push('testerId');
    }
  } else {
    if (selectedQuickSearchTypes.value.includes('testerId')) {
      selectedQuickSearchTypes.value = selectedQuickSearchTypes.value.filter(item => item !== 'testerId');
    }
  }

  if (createdDateItems.length && (
    selectedQuickSearchTypes.value.includes('last1Day') ||
    selectedQuickSearchTypes.value.includes('last3Days') ||
    selectedQuickSearchTypes.value.includes('last7Days')
  )) {
    let hasMatchingDateTime = false;
    for (let i = 0; i < createdDateItems.length; i++) {
      const item = createdDateItems[i];
      if (item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.GreaterThanEqual) {
        hasMatchingDateTime = item.value === quickSelectDateRange.value[0];
      }

      if (item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.LessThanEqual) {
        hasMatchingDateTime = item.value === quickSelectDateRange.value[1];
      }
    }

    if (!hasMatchingDateTime) {
      selectedQuickSearchTypes.value = selectedQuickSearchTypes.value.filter(item => item !== 'last1Day' && item !== 'last3Days' && item !== 'last7Days');
    }
  } else {
    selectedQuickSearchTypes.value = selectedQuickSearchTypes.value.filter(item => item !== 'last1Day' && item !== 'last3Days' && item !== 'last7Days');
  }
};

/**
 * Get quick date range based on type
 * @param type - Quick date type
 * @returns Date range array
 */
const getQuickDateRange = (type) => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  if (type === 'last1Day') {
    startDate = dayjs().startOf('date');
    endDate = dayjs().endOf('date');
  }

  if (type === 'last3Days') {
    startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    endDate = dayjs();
  }

  if (type === 'last7Days') {
    startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    endDate = dayjs();
  }

  return [startDate ? startDate.format(DATE_TIME_FORMAT) : '', endDate ? endDate.format(DATE_TIME_FORMAT) : ''];
};

/**
 * Handle overdue filter change
 * @param value - Overdue filter value
 */
const handleOverdueFilterChange = (value) => {
  isOverdueFilterEnabled.value = value;
  updateSearchParametersAndLoad();
};

/**
 * Handle numeric input change with debounce
 * @param value - Input value
 * @param type - Input type
 */
const handleNumericInputChange = debounce(duration.resize, (value: string, type: 'testNum' | 'testFailNum' | 'reviewNum') => {
  if (type === 'testNum') {
    testExecutionCount.value = value;
  }
  if (type === 'testFailNum') {
    testFailureCount.value = value;
  }
  if (type === 'reviewNum') {
    reviewCount.value = value;
  }

  updateSearchParametersAndLoad();
});

/**
 * Handle scope change for numeric inputs
 * @param value - Scope value
 * @param type - Input type
 */
const handleNumericScopeChange = (value: SearchCriteria.OpEnum, type: 'testNum' | 'testFailNum' | 'reviewNum') => {
  if (type === 'testNum') {
    testExecutionCountScope.value = value;
    if (!testExecutionCount.value) {
      return;
    }
  }
  if (type === 'testFailNum') {
    testFailureCountScope.value = value;
    if (!testFailureCount.value) {
      return;
    }
  }
  if (type === 'reviewNum') {
    reviewCountScope.value = value;
    if (!reviewCount.value) {
      return;
    }
  }

  updateSearchParametersAndLoad();
};

/**
 * Handle quick search type change
 * @param types - Selected quick search types
 * @param isAllType - Whether all type is selected
 */
const handleQuickSearchTypeChange = (types: string[], isAllType: boolean) => {
  searchParameters.value.pageNo = 1;
  selectedQuickSearchTypes.value = types;
  const currentFilters = searchParameters.value.filters || [];
  const createdByItem = currentFilters.find(item => item.key === 'createdBy');
  const testerIdItem = currentFilters.find(item => item.key === 'testerId');
  const testResultItem = currentFilters.find(item => item.key === 'testResult');
  const createdDateItems = currentFilters.filter(item => item.key === 'createdDate');
  if (types.includes('all')) {
    // Handle all type selection
    if (isAllType) {
      testExecutionCount.value = '';
      testFailureCount.value = '';
      reviewCount.value = '';
      isOverdueFilterEnabled.value = false;
      if (currentFilters?.length) {
        if (currentFilters.length) {
          searchPanelRef.value.clear();
        } else {
          loadSearchData();
        }
      }
      return;
    }

    if (currentFilters?.length) {
      const hasMatchingDateTime = isCreatedDateMatchingQuickSelect(createdDateItems, quickSelectDateRange.value);

      const configItems = [];
      if (hasMatchingDateTime) {
        configItems.push({ valueKey: 'createdDate', value: undefined });
      }

      if (createdByItem?.value === currentUserInfo.value.id) {
        configItems.push({ valueKey: 'createdBy', value: undefined });
      }

      if (testerIdItem?.value === currentUserInfo.value.id && testResultItem?.value === CaseTestResult.PENDING) {
        configItems.push({ valueKey: 'testerId', value: undefined });
        configItems.push({ valueKey: 'testResult', value: undefined });
      }

      if (configItems.length) {
        searchPanelRef.value.setConfigs(configItems);
      } else {
        selectedQuickSearchTypes.value = [];
      }
    }

    return;
  }

  if (types.includes('last1Day') || types.includes('last3Days') || types.includes('last7Days')) {
    if (types.includes('last1Day')) {
      quickSelectDateRange.value = getQuickDateRange('last1Day');
    }

    if (types.includes('last3Days')) {
      quickSelectDateRange.value = getQuickDateRange('last3Days');
    }

    if (types.includes('last7Days')) {
      quickSelectDateRange.value = getQuickDateRange('last7Days');
    }
  }

  const configItems: { valueKey: string, value: string, defaultOptions: Record<string, any> }[] = [];

  if (types.includes('createdBy')) {
    configItems.push({ valueKey: 'createdBy', value: currentUserInfo.value.id, defaultOptions: defaultUserOptions.value });
  } else {
    if (createdByItem?.value === currentUserInfo.value.id) {
      configItems.push({ valueKey: 'createdBy', value: undefined });
    }
  }

  if (types.includes('testerId')) {
    configItems.push({ valueKey: 'testerId', value: currentUserInfo.value.id, defaultOptions: defaultUserOptions.value });
    configItems.push({ valueKey: 'testResult', value: CaseTestResult.PENDING });
  } else {
    if (testerIdItem?.value === currentUserInfo.value.id && testResultItem?.value === CaseTestResult.PENDING) {
      configItems.push({ valueKey: 'testerId', value: undefined });
      configItems.push({ valueKey: 'testResult', value: undefined });
    }
  }

  if (types.includes('last1Day') || types.includes('last3Days') || types.includes('last7Days')) {
    configItems.push({ valueKey: 'createdDate', value: quickSelectDateRange.value });
  } else {
    const hasMatchingDateTime = isCreatedDateMatchingQuickSelect(createdDateItems, quickSelectDateRange.value);
    if (hasMatchingDateTime) {
      configItems.push({ valueKey: 'createdDate', value: undefined });
    }
  }

  if (configItems.length) {
    searchPanelRef.value.setConfigs(configItems);
  }
};

/**
 * Set search filters from various input sources
 */
const setSearchFilters = () => {
  const additionalFilters: SearchCriteria[] = [];
  if (testExecutionCount.value) {
    additionalFilters.push({ key: 'testNum', op: testExecutionCountScope.value, value: testExecutionCount.value });
  }
  if (testFailureCount.value) {
    additionalFilters.push({ key: 'testFailNum', op: testFailureCountScope.value, value: testFailureCount.value });
  }
  if (reviewCount.value) {
    additionalFilters.push({ key: 'reviewNum', op: reviewCountScope.value, value: reviewCount.value });
  }

  if (isOverdueFilterEnabled.value) {
    additionalFilters.push({
      key: 'overdue',
      op: SearchCriteria.OpEnum.Equal,
      value: true
    });
  }

  if (selectedTagIds.value?.length) {
    const uniqueTagIds = new Set(selectedTagIds.value);
    additionalFilters.push({
      key: 'tagId',
      op: SearchCriteria.OpEnum.In,
      value: Array.from(uniqueTagIds)
    });
  }

  searchParameters.value.filters = [...searchData.value, ...additionalFilters];
};

/**
 * Handle search panel change
 * @param data - Search criteria data
 * @param header - Header information
 * @param valueKey - Value key
 */
const handleSearchPanelChange = (data: SearchCriteria[], _header, valueKey) => {
  searchParameters.value.pageNo = 1;
  searchData.value = data;
  setSearchFilters();

  if (valueKey) {
    updateQuickSearchTypes();
  } else {
    if (selectedQuickSearchTypes.value.includes('all')) {
      const filteredItems = searchParameters.value.filters.filter(item => !['planId', 'tagId'].includes(item.key));
      if (filteredItems.length) {
        selectedQuickSearchTypes.value = [];
      }
    }
  }

  loadSearchData();
};

/**
 * Load search data by emitting change event
 */
const loadSearchData = () => {
  emits('change', searchParameters.value);
};

/**
 * Handle add case button click
 */
const handleAddCaseClick = () => {
  emits('handleAddCase');
};
</script>

<template>
  <div>
    <div class="flex-shrink-0 flex justify-between items-start">
      <!-- Quick Search -->
      <div class="flex items-center flex-wrap">
        <QuickSelect
          class="mb-3"
          :list="quickSearchOptions"
          :selectedTypes="selectedQuickSearchTypes"
          @change="handleQuickSearchTypeChange" />
        <div class="px-4 h-7 leading-7 mb-3">
          <span>{{ t('status.overdue') }}</span>
          <Colon class="mr-2" />
          <Switch
            :checked="isOverdueFilterEnabled"
            size="small"
            class="w-8"
            @change="handleOverdueFilterChange" />
        </div>
        <TagList
          ref="tagListRef"
          class="mb-3 mr-5"
          :tagIds="selectedTagIds"
          @change="handleTagSelectionChange" />
      </div>
      <div class="flex-1 text-right">
        <Button
          :disabled="props.established"
          size="small"
          type="primary"
          @click="handleAddCaseClick">
          <Icon icon="icon-jia" class="mr-1" />
          {{ t('testCaseBaseline.actions.addBaselineCase') }}
        </Button>
      </div>
    </div>

    <!-- Search Panel -->
    <SearchPanel
      ref="searchPanelRef"
      class="mb-3 flex-shrink-0"
      :options="searchPanelOptions"
      :width="296"
      @change="handleSearchPanelChange">
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

      <!-- Test Num -->
      <template #testNum>
        <Input
          :value="testExecutionCount"
          dataType="float"
          allowClear
          :max="100"
          :placeholder="t('testCaseBaseline.case.placeholders.testNumPlaceholder')"
          style="width: 296px;"
          @change="(event: ChangeEvent) => handleNumericInputChange(event.target.value, 'testNum')">
          <template #prefix>
            <SelectEnum
              v-model:value="testExecutionCountScope"
              :bordered="false"
              enumKey="NumberCompareCondition"
              class="w-38" />
          </template>
        </Input>
      </template>

      <!-- Test Fail Num -->
      <template #testFailNum>
        <Input
          :value="testFailureCount"
          dataType="float"
          allowClear
          :max="100"
          :placeholder="t('testCaseBaseline.case.placeholders.testFailNumPlaceholder')"
          style="width: 296px;"
          @change="handleNumericInputChange($event.target.value, 'testFailNum')">
          <template #prefix>
            <SelectEnum
              v-model:value="testFailureCountScope"
              :bordered="false"
              enumKey="NumberCompareCondition"
              class="w-38" />
          </template>
        </Input>
      </template>

      <!-- Review Num -->
      <template #reviewNum>
        <Input
          :value="reviewCount"
          dataType="float"
          allowClear
          :max="100"
          :placeholder="t('testCaseBaseline.case.placeholders.reviewNumPlaceholder')"
          style="width: 296px;"
          @change="(event: ChangeEvent) => handleNumericInputChange(event.target.value, 'reviewNum')">
          <template #prefix>
            <SelectEnum
              v-model:value="reviewCountScope"
              :bordered="false"
              enumKey="NumberCompareCondition"
              class="w-38" />
          </template>
        </Input>
      </template>
    </SearchPanel>
  </div>
</template>
