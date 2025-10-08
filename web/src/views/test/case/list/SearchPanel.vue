<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Switch } from 'ant-design-vue';
import {
  Colon, Dropdown, DropdownGroup, DropdownSort, Icon, IconCount,
  IconRefresh, Input, ReviewStatus, SearchPanel, Select, Tooltip
} from '@xcan-angus/vue-ui';
import {
  duration, EnumMessage, enumUtils, NumberCompareCondition,
  Priority, ReviewStatus as ReviewStatusEnum, PageQuery, SearchCriteria, XCanDexie
} from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { debounce } from 'throttle-debounce';
import { isEqual, cloneDeep } from 'lodash-es';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import { CaseTestResult } from '@/enums/enums';
import { CaseViewMode } from '@/views/test/case/types';

import TaskPriority from '@/components/TaskPriority/index.vue';
import TestResult from '@/components/TestResult/index.vue';

// Type definitions
type Props = {
  viewMode: CaseViewMode;
  projectId: number;
  userInfo: { id: number; fullName: string };
  appInfo: { id: number; name: string };
  notify: string;
  orderBy?: 'priority' | 'deadlineDate' | 'createdByName' | 'testerName';
  orderSort?: PageQuery.OrderSort;
  groupKey?: 'none' | 'testerName' | 'lastModifiedByName';
  enabledGroup: boolean;
  moduleId?: number;
}

const props = withDefaults(defineProps<Props>(), {
  viewMode: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  orderBy: undefined,
  orderSort: undefined,
  groupKey: 'none',
  enabledGroup: true,
  moduleId: undefined
});

// Component emits
const emit = defineEmits<{
  (e: 'change', value: SearchCriteria[]): void;
  (e: 'update:orderBy', value: 'priority' | 'deadlineDate' | 'createdByName' | 'testerName'): void;
  (e: 'update:orderSort', value: PageQuery.OrderSort): void;
  (e: 'update:groupKey', value: 'none' | 'testerName' | 'lastModifiedByName'): void;
  (e: 'update:enabledGroup', value: boolean): void;
  (e: 'update:moduleId', value: string | undefined): void;
  (e: 'viewModeChange', value: CaseViewMode): void;
  (e: 'countChange'): void;
  (e: 'refresh'): void;
  (e: 'add'): void;
  (e: 'aiAdd'): void;
  (e: 'export'): void;
  (e: 'import'): void;
}>();

// Composables & injections
const { t } = useI18n();
const aiEnabled = inject('aiEnabled', ref(false));

// Database instance
// eslint-disable-next-line no-undef
let database: XCanDexie<{ id: string; data: any; }>;

// Component references
const searchPanelRef = ref();

// Quick search state
const selectedQuickSearchItems = ref(new Map<string, { key: string }>());
const quickSelectDate = ref<string[]>([]);

// Filter states
const overdue = ref(false);

// Numeric filters
const testNum = ref(''); // Test count
const testNumScope = ref<SearchCriteria.OpEnum>(SearchCriteria.OpEnum.Equal);
const testFailNum = ref(''); // Failure count
const testFailScope = ref<SearchCriteria.OpEnum>(SearchCriteria.OpEnum.Equal);
const reviewNum = ref(''); // Review count
const reviewNumScope = ref<SearchCriteria.OpEnum>(SearchCriteria.OpEnum.Equal);

// Search criteria filters
const searchFilters = ref<SearchCriteria[]>([]);

// Numeric comparison conditions
const numberMatchCondition = ref<EnumMessage<NumberCompareCondition>[]>([]);

/**
 * Loads numeric comparison condition options
 */
const loadEnums = () => {
  numberMatchCondition.value = enumUtils.enumToMessages(NumberCompareCondition);
};

/**
 * Handles sorting configuration changes
 */
const handleSortingChange = (
  data: { orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'testerName';
    orderSort: PageQuery.OrderSort; }
) => {
  emit('update:orderBy', data.orderBy);
  emit('update:orderSort', data.orderSort);
};

/**
 * Handles grouping configuration changes
 */
const handleGroupingChange = (
  value: 'none' | 'testerName' | 'lastModifiedByName'
) => {
  emit('update:groupKey', value);
};

/**
 * Handles overdue filter toggle
 * Removes "All" selection when overdue filter is enabled
 */
const handleOverdueChange = (checked: any) => {
  overdue.value = checked;

  // Remove "All" selection when overdue filter is enabled
  if (checked) {
    selectedQuickSearchItems.value.delete('all');
  }

  emit('change', buildSearchCriteria());
};

/**
 * Handles module grouping toggle
 */
const handleModuleGroupingChange = (checked: any) => {
  emit('update:enabledGroup', checked);
  if (checked) {
    emit('update:moduleId', '');
  } else {
    emit('update:moduleId', undefined);
  }

  // Save module grouping preference to database
  if (database) {
    database.add({ id: dbModuleKey.value, data: checked + '' });
  }
};

/**
 * Handles numeric filter changes with debouncing
 */
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
  emit('change', buildSearchCriteria());
});

/**
 * Handles numeric filter scope changes
 */
const handleScopeChange = (value: SearchCriteria.OpEnum, type: 'testNum' | 'testFailNum' | 'reviewNum') => {
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
  emit('change', buildSearchCriteria());
};

/**
 * Formats date range for quick search time periods
 */
const getQuickDate = (type: 'last1Day' | 'last3Days' | 'last7Days') => {
  let _startDate: Dayjs | undefined;
  let _endDate: Dayjs | undefined;

  if (type === 'last1Day') {
    _startDate = dayjs().startOf('date');
    _endDate = dayjs().endOf('date');
  }

  if (type === 'last3Days') {
    _startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    _endDate = dayjs();
  }

  if (type === 'last7Days') {
    _startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    _endDate = dayjs();
  }

  return [_startDate ? _startDate.format(DATE_TIME_FORMAT) : '', _endDate ? _endDate.format(DATE_TIME_FORMAT) : ''];
};


/**
 * Handles quick search menu item clicks
 * Manages the selection/deselection of quick search filters
 * @param data - The clicked menu item data
 */
const handleQuickSearchMenuItemClick = (data: { key: string; name: string }) => {
  const itemKey = data.key;

  // Handle deselection of already selected items
  if (selectedQuickSearchItems.value.has(itemKey)) {
    // "All" button remains selected when clicked again
    if (itemKey === 'all') {
      return;
    }

    // Remove the selected item
    selectedQuickSearchItems.value.delete(itemKey);

    // Clear corresponding search panel configurations
    if (itemKey === 'createdBy') {
      updateSearchPanelConfigs([{ valueKey: 'createdBy', value: undefined }]);
      return;
    }

    if (itemKey === 'testerId') {
      updateSearchPanelConfigs([
        { valueKey: 'testerId', value: undefined },
        { valueKey: 'testResult', value: undefined }
      ]);
      return;
    }

    if (['last1Day', 'last3Days', 'last7Days'].includes(itemKey)) {
      quickSelectDate.value = [];
      updateSearchPanelConfigs([{ valueKey: 'createdDate', value: undefined }]);
      return;
    }

    return;
  }

  // Handle "All" selection - clears all other filters
  if (itemKey === 'all') {
    clearAllFilters();
    return;
  }

  // Handle specific filter selections
  if (itemKey === 'createdBy') {
    updateSearchPanelConfigs([{ valueKey: 'createdBy', value: props.userInfo.id }]);
    return;
  }

  if (itemKey === 'testerId') {
    updateSearchPanelConfigs([
      { valueKey: 'testerId', value: props.userInfo.id },
      { valueKey: 'testResult', value: CaseTestResult.PENDING }
    ]);
    return;
  }

  if (['last1Day', 'last3Days', 'last7Days'].includes(itemKey)) {
    // Clear other time selections first (mutual exclusion)
    selectedQuickSearchItems.value.delete('last1Day');
    selectedQuickSearchItems.value.delete('last3Days');
    selectedQuickSearchItems.value.delete('last7Days');

    // Set the selected time option
    selectedQuickSearchItems.value.set(itemKey, { key: itemKey });

    quickSelectDate.value = [];
    const dateRange = getQuickDate(itemKey as 'last1Day' | 'last3Days' | 'last7Days');
    quickSelectDate.value = dateRange;
    updateSearchPanelConfigs([{ valueKey: 'createdDate', value: dateRange }]);
  }
};

/**
 * Clears all filters and resets the search panel
 */
const clearAllFilters = () => {
  selectedQuickSearchItems.value.clear();
  selectedQuickSearchItems.value.set('all', { key: 'all' });

  // Clear search panel
  if (typeof searchPanelRef.value?.clear === 'function') {
    searchPanelRef.value.clear();
  }

  // Reset all filter states
  testNum.value = '';
  testFailNum.value = '';
  reviewNum.value = '';
  overdue.value = false;
  quickSelectDate.value = [];
};

/**
 * Updates search panel configurations
 * @param configs - Array of configuration objects
 */
const updateSearchPanelConfigs = (configs: { valueKey: string; value: any }[]) => {
  if (typeof searchPanelRef.value?.setConfigs === 'function') {
    searchPanelRef.value.setConfigs(configs);
  }
};

/**
 * Handles search panel changes
 */
const handleSearchPanelChange = (
  data: SearchCriteria[],
  _headers?: { [key: string]: string },
  _key?: string
) => {
  searchFilters.value = data;
  emit('change', buildSearchCriteria());
};

/**
 * Builds the complete search criteria array from all active filters
 */
const buildSearchCriteria = () => {
  const searchCriteria: SearchCriteria[] = [...searchFilters.value];

  // Add numeric filters
  if (testNum.value) {
    searchCriteria.push({ key: 'testNum', op: testNumScope.value, value: testNum.value });
  }
  if (testFailNum.value) {
    searchCriteria.push({ key: 'testFailNum', op: testFailScope.value, value: testFailNum.value });
  }
  if (reviewNum.value) {
    searchCriteria.push({ key: 'reviewNum', op: reviewNumScope.value, value: reviewNum.value });
  }

  // Add overdue filter
  if (overdue.value) {
    searchCriteria.push({
      key: 'overdue',
      op: SearchCriteria.OpEnum.Equal,
      value: true
    });
  }

  return searchCriteria;
};

/**
 * Handles view mode changes
 */
const handleViewModeChange = (viewMode: CaseViewMode) => {
  emit('viewModeChange', viewMode);
};

/**
 * Handles button dropdown menu clicks
 */
const handleButtonDropdownClick = ({ key }: { key: 'import' | 'export' }) => {
  if (key === 'import') {
    emit('import');
    return;
  }
  if (key === 'export') {
    emit('export');
  }
};

/**
 * Handles add case action
 */
const handleAdd = () => {
  emit('add');
};

/**
 * Handles AI add case action
 */
const handleAiAdd = () => {
  emit('aiAdd');
};

/**
 * Handles refresh action
 */
const handleRefresh = () => {
  emit('refresh');
};

/**
 * Handles count toggle action
 */
const handleCountChange = () => {
  emit('countChange');
};

// Computed properties
const menuItems = computed(() => [
  {
    key: 'all',
    name: t('common.all')
  },
  {
    key: 'createdBy',
    name: t('testCase.mainView.myAdded')
  },
  {
    key: 'testerId',
    name: t('testCase.mainView.waitingForTest')
  },
  {
    key: 'last1Day',
    name: t('quickSearch.last1Day')
  },
  {
    key: 'last3Days',
    name: t('quickSearch.last3Days')
  },
  {
    key: 'last7Days',
    name: t('quickSearch.last7Days')
  }
]);

const searchOptions = computed(() => [
  {
    placeholder: t('testCase.case.selectCreator'),
    valueKey: 'name',
    type: 'input' as const,
    allowClear: true
  },
  {
    placeholder: t('testCase.case.selectCreator'),
    valueKey: 'createdBy',
    type: 'select-user' as const,
    allowClear: true
  },
  {
    placeholder: t('testCase.case.selectTester'),
    valueKey: 'testerId',
    type: 'select-user' as const,
    allowClear: true
  },
  {
    placeholder: t('testCase.case.selectDeveloper'),
    valueKey: 'developerId',
    type: 'select-user' as const,
    allowClear: true
  },
  {
    placeholder: t('testCase.case.selectPriority'),
    valueKey: 'priority',
    type: 'select-enum' as const,
    enumKey: Priority,
    allowClear: true
  },
  {
    placeholder: t('testCase.case.selectTestResult'),
    valueKey: 'testResult',
    type: 'select-enum' as const,
    enumKey: CaseTestResult,
    allowClear: true
  },
  {
    placeholder: t('testCase.case.selectReviewer'),
    valueKey: 'reviewerId',
    type: 'select-user' as const,
    allowClear: true
  },
  {
    placeholder: t('testCase.case.selectReviewStatus'),
    valueKey: 'reviewStatus',
    type: 'select-enum' as const,
    enumKey: ReviewStatusEnum,
    allowClear: true
  },
  {
    placeholder: [
      t('testCase.case.searchPanel.reviewDateFrom'),
      t('testCase.case.searchPanel.reviewDateTo')
    ],
    valueKey: 'reviewDate',
    type: 'date-range' as const,
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [
      t('testCase.case.searchPanel.updateDateFrom'),
      t('testCase.case.searchPanel.updateDateTo')
    ],
    valueKey: 'lastModifiedDate',
    type: 'date-range' as const,
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [
      t('testCase.case.searchPanel.deadlineDateFrom'),
      t('testCase.case.searchPanel.deadlineDateTo')
    ],
    valueKey: 'deadlineDate',
    type: 'date-range' as const,
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [
      t('testCase.case.searchPanel.createdDateFrom'),
      t('testCase.case.searchPanel.createdDateTo')
    ],
    valueKey: 'createdDate',
    type: 'date-range' as const,
    allowClear: true,
    showTime: true
  },
  {
    placeholder: [
      t('testCase.case.searchPanel.testResultHandleDateFrom'),
      t('testCase.case.searchPanel.testResultHandleDateTo')
    ],
    valueKey: 'testResultHandleDate',
    type: 'date-range' as const,
    allowClear: true,
    showTime: true
  },
  {
    valueKey: 'testNum',
    type: 'input' as const
  },
  {
    valueKey: 'testFailNum',
    type: 'input' as const
  },
  {
    valueKey: 'reviewNum',
    type: 'input' as const
  }
].filter(Boolean));

const buttonDropdownMenuItems = computed(() => [
  {
    name: t('testCase.mainView.exportCase'),
    key: 'export',
    icon: 'icon-daochu1'
  },
  {
    name: t('testCase.mainView.importCase'),
    key: 'import',
    icon: 'icon-shangchuan'
  }
]);

const modeOptions = [
  {
    key: CaseViewMode.flat,
    name: t('testCase.mainView.tileView'),
    label: ''
  },
  {
    key: CaseViewMode.table,
    name: t('testCase.mainView.listView'),
    label: ''
  },
  {
    key: CaseViewMode.kanban,
    name: t('testCase.mainView.kanbanView'),
    label: ''
  }
];

const modeTitle = computed(() => {
  if (props.viewMode === CaseViewMode.kanban) {
    return t('testCase.mainView.kanbanView');
  }

  if (props.viewMode === CaseViewMode.flat) {
    return t('testCase.mainView.tileView');
  }

  return t('testCase.mainView.listView');
});

const modeIcon = computed(() => {
  if (props.viewMode === CaseViewMode.kanban) {
    return 'icon-kanbanshitu';
  }

  if (props.viewMode === CaseViewMode.flat) {
    return 'icon-pingpushitu';
  }

  return 'icon-liebiaoshitu';
});

const groupMenuItems = [
  {
    key: 'none',
    name: t('actions.noGroup')
  },
  {
    key: 'testerName',
    name: t('testCase.mainView.groupByTester')
  },
  {
    key: 'lastModifiedByName',
    name: t('testCase.mainView.groupByLastModifier')
  }
];

const sortMenuItems = [
  {
    key: 'createdByName',
    name: t('testCase.mainView.sortByCreator'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'testerName',
    name: t('testCase.mainView.sortByTester'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'priority',
    name: t('testCase.mainView.sortByPriority'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'deadlineDate',
    name: t('testCase.mainView.sortByDeadline'),
    orderSort: PageQuery.OrderSort.Asc
  }
];

/**
 * Gets the current user ID from props
 */
const currentUserId = computed(() => {
  return props.userInfo?.id;
});

/**
 * Generates base key for database storage based on user and project
 */
const dbBaseKey = computed(() => {
  let key = '';
  if (currentUserId.value) {
    key = currentUserId.value;
  }

  if (props.projectId) {
    key += props.projectId;
  }

  return key;
});

/**
 * Database key for search parameters storage
 */
const dbParamsKey = computed(() => {
  return btoa(dbBaseKey.value + '_case');
});

/**
 * Database key for module grouping preference
 */
const dbModuleKey = computed(() => {
  return btoa(dbBaseKey.value + '_moduleFlag');
});

/**
 * Initializes the component with saved data from database
 * Loads user preferences and search criteria
 */
const initializeComponent = async () => {
  if (!database) {
    database = new XCanDexie<{ id: string; data: any; }>('parameter');
  }

  // Load module grouping preference
  const [, moduleData] = await database.get(dbModuleKey.value);
  const enabledGroup = moduleData?.data === 'true';
  emit('update:enabledGroup', enabledGroup);

  // Set moduleId based on enabledGroup state
  if (enabledGroup) {
    emit('update:moduleId', '');
  } else {
    emit('update:moduleId', undefined);
  }

  // Load search criteria data
  const [, searchData] = await database.get(dbParamsKey.value);
  const savedSearchData = searchData?.data;
  if (savedSearchData) {
    const valueMap: { [key: string]: string | string[] } = {};
    const dateTimeMap: { [key: string]: string[] } = {};

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'filters')) {
      const _cacheFilters = savedSearchData.filters || [];
      searchFilters.value = _cacheFilters;

      // Process special filters
      for (let i = 0; i < _cacheFilters.length; i++) {
        const item = _cacheFilters[i] as SearchCriteria;
        if (item.key && ['planId', 'tagId', 'overdue', 'testNum', 'testFailNum', 'reviewNum'].includes(item.key)) {
          if (item.key === 'overdue') {
            overdue.value = item.value as boolean;
          }
          if (item.key === 'testNum') {
            testNum.value = item.value as string;
            testNumScope.value = item.op || SearchCriteria.OpEnum.Equal;
          }
          if (item.key === 'testFailNum') {
            testFailNum.value = item.value as string;
            testFailScope.value = item.op || SearchCriteria.OpEnum.Equal;
          }
          if (item.key === 'reviewNum') {
            reviewNum.value = item.value as string;
            reviewNumScope.value = item.op || SearchCriteria.OpEnum.Equal;
          }
        } else {
          // Handle date range filters
          const dateTimeKeys = ['createdDate', 'deadlineDate', 'reviewDate', 'lastModifiedDate', 'testResultHandleDate'];
          if (item.key && dateTimeKeys.includes(item.key)) {
            if (item.value !== undefined) {
              if (dateTimeMap[item.key]) {
                dateTimeMap[item.key].push(item.value as string);
              } else {
                dateTimeMap[item.key] = [item.value as string];
              }
            }
          } else {
            if (item.key && item.value !== undefined) {
              valueMap[item.key] = item.value;
            }
          }
        }
      }

      // Process date ranges
      Object.entries(dateTimeMap).every(([key, dates]: [string, string[]]) => {
        const dateTimes: string[] = [];
        if (dates.length >= 2) {
          const [date1, date2] = dates;
          if (date1 && date2) {
            if (dayjs(date1).isBefore(dayjs(date2))) {
              dateTimes[0] = date1;
              dateTimes[1] = date2;
            } else {
              dateTimes[0] = date2;
              dateTimes[1] = date1;
            }
          }
        }

        if (dateTimes.length === 2) {
          valueMap[key] = dateTimes;
        }
        return true;
      });
    } else {
      searchFilters.value = [];
    }

    // Load overdue filter state
    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'isOverdueEnabled')) {
      overdue.value = savedSearchData.isOverdueEnabled || false;
    } else {
      overdue.value = false;
    }

    // Load numeric filter states
    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'testNum')) {
      testNum.value = savedSearchData.testNum || '';
      testNumScope.value = savedSearchData.testNumScope || SearchCriteria.OpEnum.Equal;
    }

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'testFailNum')) {
      testFailNum.value = savedSearchData.testFailNum || '';
      testFailScope.value = savedSearchData.testFailScope || SearchCriteria.OpEnum.Equal;
    }

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'reviewNum')) {
      reviewNum.value = savedSearchData.reviewNum || '';
      reviewNumScope.value = savedSearchData.reviewNumScope || SearchCriteria.OpEnum.Equal;
    }

    // Set search panel data
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      const configs: { valueKey: string; value: string | string[] | undefined }[] = [];

      // Add other search criteria
      Object.entries(valueMap).forEach(([key, value]) => {
        configs.push({ valueKey: key, value });
      });

      if (configs.length) {
        searchPanelRef.value.setConfigs(configs);
      }
    }

    // Set up quick search based on loaded filters
    const createdBy = searchFilters.value.find(item => item.key === 'createdBy')?.value;
    if (createdBy && createdBy === props.userInfo.id) {
      selectedQuickSearchItems.value.set('createdBy', { key: 'createdBy' });
    }

    const testerId = searchFilters.value.find(item => item.key === 'testerId')?.value;
    const testResult = searchFilters.value.find(item => item.key === 'testResult')?.value;
    if (testerId === props.userInfo.id && testResult === CaseTestResult.PENDING) {
      selectedQuickSearchItems.value.set('testerId', { key: 'testerId' });
    }

    // Check for date filters
    const createdDateStart = searchFilters.value.find(item => item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.GreaterThanEqual)?.value;
    const createdDateEnd = searchFilters.value.find(item => item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.LessThanEqual)?.value;
    if (createdDateStart && createdDateEnd) {
      const dateString = [createdDateStart, createdDateEnd];
      if (isEqual(dateString, getQuickDate('last1Day'))) {
        selectedQuickSearchItems.value.set('last1Day', { key: 'last1Day' });
      } else if (isEqual(dateString, getQuickDate('last3Days'))) {
        selectedQuickSearchItems.value.set('last3Days', { key: 'last3Days' });
      } else if (isEqual(dateString, getQuickDate('last7Days'))) {
        selectedQuickSearchItems.value.set('last7Days', { key: 'last7Days' });
      }
    }

    // If no quick search items are selected, set "all" as default
    if (selectedQuickSearchItems.value.size === 0) {
      selectedQuickSearchItems.value.set('all', { key: 'all' });
    }

    // Emit the search criteria
    emit('change', buildSearchCriteria());
    return;
  }

  // No cached data, reset to default
  resetData();
};

/**
 * Resets all component data to initial state
 */
const resetData = () => {
  selectedQuickSearchItems.value.clear();
  selectedQuickSearchItems.value.set('all', { key: 'all' });
  quickSelectDate.value = [];
  overdue.value = false;
  testNum.value = '';
  testFailNum.value = '';
  reviewNum.value = '';
  testNumScope.value = SearchCriteria.OpEnum.Equal;
  testFailScope.value = SearchCriteria.OpEnum.Equal;
  reviewNumScope.value = SearchCriteria.OpEnum.Equal;
  searchFilters.value = [];
};

/**
 * Clears search panel and resets all filters
 */
const clear = (emitChange = true) => {
  resetData();
  if (typeof searchPanelRef.value?.clear === 'function') {
    searchPanelRef.value.clear();
  }
  if (emitChange) {
    emit('change', buildSearchCriteria());
  }
};

/**
 * Sets search panel configurations
 */
const setConfigs = (configs: { valueKey: string; value: any }[]) => {
  if (typeof searchPanelRef.value?.setConfigs === 'function') {
    searchPanelRef.value.setConfigs(configs);
  }
};

// Expose methods for parent component
defineExpose({
  clear,
  setConfigs
});

onMounted(async () => {
  await loadEnums();
  await initializeComponent();
});

// Watch for changes in search filters and update quick search state accordingly
watch(
  [
    () => searchFilters.value,
    () => overdue.value,
    () => testNum.value,
    () => testFailNum.value,
    () => reviewNum.value,
    () => selectedQuickSearchItems.value
  ], () => {
    const _filters = searchFilters.value;
    if (!(_filters.length ||
      overdue.value ||
      !!testNum.value ||
      !!testFailNum.value ||
      !!reviewNum.value)) {
      selectedQuickSearchItems.value.clear();
      selectedQuickSearchItems.value.set('all', { key: 'all' });

      emit('change', buildSearchCriteria());
    } else {
      // Remove the selected "All" option from quick search
      selectedQuickSearchItems.value.delete('all');

      // Set up quick search based on current filters
      const createdBy = _filters.find(item => item.key === 'createdBy')?.value;
      if (createdBy && createdBy === props.userInfo.id) {
        selectedQuickSearchItems.value.set('createdBy', { key: 'createdBy' });
      } else {
        selectedQuickSearchItems.value.delete('createdBy');
      }

      const testerId = _filters.find(item => item.key === 'testerId')?.value;
      const testResult = _filters.find(item => item.key === 'testResult')?.value;
      if (testerId === props.userInfo.id && testResult === CaseTestResult.PENDING) {
        selectedQuickSearchItems.value.set('testerId', { key: 'testerId' });

        // Remove other conflicting options
        selectedQuickSearchItems.value.delete('createdBy');
      } else {
        selectedQuickSearchItems.value.delete('testerId');
      }

      // Check for date filters (mutual exclusion)
      const createdDateStart = _filters.find(item => item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.GreaterThanEqual)?.value;
      const createdDateEnd = _filters.find(item => item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.LessThanEqual)?.value;

      // Clear all time selections first
      selectedQuickSearchItems.value.delete('last1Day');
      selectedQuickSearchItems.value.delete('last3Days');
      selectedQuickSearchItems.value.delete('last7Days');

      if (createdDateStart && createdDateEnd) {
        const dateString = [createdDateStart, createdDateEnd];
        if (isEqual(dateString, getQuickDate('last1Day'))) {
          selectedQuickSearchItems.value.set('last1Day', { key: 'last1Day' });
        } else if (isEqual(dateString, getQuickDate('last3Days'))) {
          selectedQuickSearchItems.value.set('last3Days', { key: 'last3Days' });
        } else if (isEqual(dateString, getQuickDate('last7Days'))) {
          selectedQuickSearchItems.value.set('last7Days', { key: 'last7Days' });
        }
      }

      emit('change', buildSearchCriteria());
    }

    // Save to database
    if (database) {
      const dbData: {
        filters?: SearchCriteria[];
        isOverdueEnabled?: boolean;
        testNum?: string;
        testFailNum?: string;
        reviewNum?: string;
        testNumScope?: SearchCriteria.OpEnum;
        testFailScope?: SearchCriteria.OpEnum;
        reviewNumScope?: SearchCriteria.OpEnum;
      } = {};

      if (_filters.length) {
        dbData.filters = cloneDeep(_filters);
      }

      if (overdue.value) {
        dbData.isOverdueEnabled = overdue.value;
      }

      if (testNum.value) {
        dbData.testNum = testNum.value;
        dbData.testNumScope = testNumScope.value;
      }

      if (testFailNum.value) {
        dbData.testFailNum = testFailNum.value;
        dbData.testFailScope = testFailScope.value;
      }

      if (reviewNum.value) {
        dbData.reviewNum = reviewNum.value;
        dbData.reviewNumScope = reviewNumScope.value;
      }

      if (Object.keys(dbData).length) {
        database.add({
          id: dbParamsKey.value,
          data: dbData
        });
      } else {
        database.delete(dbParamsKey.value);
      }
    }
  }, { immediate: false, deep: false });
</script>

<template>
  <div class="text-3 leading-5">
    <div class="flex items-start justify-between mb-1.5">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-1 mt-1.5 rounded-full"></div>
        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>{{ t('quickSearch.title') }}</span>
          <Colon />
        </div>
        <div class="flex flex-wrap ml-2">
          <div
            v-for="item in menuItems"
            :key="item.key"
            :class="{ 'active-key': selectedQuickSearchItems.has(item.key) }"
            class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
            @click="handleQuickSearchMenuItemClick(item)">
            {{ item.name }}
          </div>

          <div class="px-4 h-7 leading-7 mb-3">
            <span>{{ t('status.overdue') }}</span>
            <Colon class="mr-2" />
            <Switch
              :checked="overdue"
              size="small"
              class="w-8"
              @change="handleOverdueChange" />
          </div>

          <div class="h-7 leading-7 mb-3 mr-5">
            <span class="text-3 text-theme-content">
              <span>{{ t('common.module') }}</span>
              <Colon class="mr-2" />
            </span>

            <Switch
              :checked="enabledGroup"
              size="small"
              class="w-8"
              @change="handleModuleGroupingChange" />
          </div>

          <template v-if="props.viewMode === CaseViewMode.kanban">
            <DropdownSort
              :orderBy="props.orderBy"
              :orderSort="props.orderSort"
              :menuItems="sortMenuItems"
              @click="handleSortingChange">
              <Button
                size="small"
                type="text"
                class="flex items-center px-0 h-5 leading-5 border-0 cursor-pointer mr-5 mb-3">
                <Icon icon="icon-biaotoupaixu" class="text-3.5" />
                <span class="ml-1">{{ t('actions.sort') }}</span>
              </Button>
            </DropdownSort>

            <DropdownGroup
              :activeKey="props.groupKey"
              :menuItems="groupMenuItems"
              @click="handleGroupingChange">
              <Button
                size="small"
                type="text"
                class="flex items-center px-0 h-5 leading-5 border-0 cursor-pointer mr-5 mb-3">
                <Icon icon="icon-fenzu" class="text-3.5" />
                <span class="ml-1">{{ t('organization.group') }}</span>
              </Button>
            </DropdownGroup>
          </template>
        </div>
      </div>
    </div>

    <div class="flex items-start justify-between mb-1.5 space-x-4">
      <SearchPanel
        ref="searchPanelRef"
        class="flex-1"
        :options="searchOptions"
        @change="handleSearchPanelChange">
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
            :placeholder="t('testCase.mainView.testTimes')"
            style="width: 296px;"
            :min="0"
            :debounce="500"
            @change="(event: any) => handleTimesChange(event.target.value, 'testNum')">
            <template #prefix>
              <Select
                :value="testNumScope.toString()"
                size="small"
                :options="numberMatchCondition"
                :fieldNames="{ label: 'description', value: 'value' }"
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
            :placeholder="t('testCase.mainView.failTimes')"
            style="width: 296px;"
            :min="0"
            :debounce="500"
            @change="handleTimesChange($event.target.value, 'testFailNum')">
            <template #prefix>
              <Select
                :value="testFailScope.toString()"
                size="small"
                :options="numberMatchCondition"
                :fieldNames="{ label: 'description', value: 'value' }"
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
            :placeholder="t('testCase.mainView.reviewTimes')"
            style="width: 296px;"
            :min="0"
            :debounce="500"
            @change="(event: any) => handleTimesChange(event.target.value, 'reviewNum')">
            <template #prefix>
              <Select
                :value="reviewNumScope.toString()"
                size="small"
                :options="numberMatchCondition"
                :fieldNames="{ label: 'message', value: 'value' }"
                :allowClear="false"
                :bordered="false"
                class="w-24"
                @change="handleScopeChange($event, 'reviewNum')" />
            </template>
          </Input>
        </template>
      </SearchPanel>

      <div class="flex-shrink-0 flex items-center space-x-2">
        <Button
          v-if="aiEnabled"
          class="flex-shrink-0 flex items-center"
          size="small"
          ghost
          type="primary"
          @click="handleAiAdd">
          <Icon icon="icon-jia" class="text-3.5" />
          <span class="ml-1">{{ t('testCase.mainView.smartAddCase') }}</span>
        </Button>

        <Button
          class="flex-shrink-0 flex items-center pr-0"
          type="primary"
          size="small"
          @click="handleAdd">
          <div class="flex items-center">
            <Icon icon="icon-jia" class="text-3.5" />
            <span class="ml-1">{{ t('testCase.mainView.addCase') }}</span>
          </div>
          <Dropdown :menuItems="buttonDropdownMenuItems" @click="handleButtonDropdownClick">
            <div class="w-5 h-5 flex items-center justify-center">
              <Icon icon="icon-more" />
            </div>
          </Dropdown>
        </Button>

        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          :title="modeTitle">
          <Dropdown
            :noAuth="true"
            :value="[props.viewMode]"
            :menuItems="modeOptions"
            @click="handleViewModeChange($event.key)">
            <div>
              <Icon
                :icon="modeIcon"
                class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0" />
              <Icon icon="icon-xiajiantou" class="ml-1" />
            </div>
          </Dropdown>
        </Tooltip>

        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          :title="t('testCase.mainView.viewStatistics')">
          <IconCount
            :value="true"
            class="text-4 flex-shrink-0"
            @click="handleCountChange" />
        </Tooltip>

        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          :title="t('actions.refresh')">
          <IconRefresh class="text-4 flex-shrink-0" @click="handleRefresh" />
        </Tooltip>
      </div>
    </div>
  </div>
</template>

<style scoped>
.active-key {
  background-color: #4ea0fd;
  color: #fff;
}
</style>
