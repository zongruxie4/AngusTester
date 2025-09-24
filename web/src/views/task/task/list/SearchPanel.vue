<script setup lang="ts">
import { computed, inject, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { Button, Switch, Tag } from 'ant-design-vue';
import {
  Colon,
  Dropdown,
  DropdownGroup,
  DropdownSort,
  Icon,
  IconCount,
  IconRefresh,
  IconTask,
  IconText,
  Input,
  SearchPanel,
  Select,
  Tooltip
} from '@xcan-angus/vue-ui';
import { duration, enumUtils, PageQuery, Priority, Result, SearchCriteria, TESTER, XCanDexie } from '@xcan-angus/infra';
import { TaskStatus as TaskStatusEnum, TaskType, TestType } from '@/enums/enums';
import { debounce } from 'throttle-debounce';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import TaskStatus from '@/components/TaskStatus/index.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { SearchPanelMenuItem, SearchPanelOption, TaskViewMode } from '@/views/task/task/types';

// Types & interfaces
type Props = {
  collapse: boolean; // Controls statistics panel expand/collapse state
  viewMode: TaskViewMode;
  sprintId: string;
  sprintName: string;
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName';
  orderSort: PageQuery.OrderSort;
  groupKey: 'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType';
}

const props = withDefaults(defineProps<Props>(), {
  collapse: false,
  viewMode: undefined,
  sprintId: undefined,
  sprintName: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  groupKey: 'none',
  orderBy: undefined,
  orderSort: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'add'): void;
  (e: 'export'): void;
  (e: 'change', value: SearchCriteria[]): void;
  (e: 'update:orderBy', value: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName'): void;
  (e: 'update:orderSort', value: PageQuery.OrderSort): void;
  (e: 'update:groupKey', value: 'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType'): void;
  (e: 'update:visible', value: boolean): void;
  (e: 'update:collapse', value: boolean): void;
  (e: 'viewModeChange', value: TaskViewMode): void;
  (e: 'uploadTask'): void;
  (e: 'exportTemplate'): void;
  (e: 'update:moduleFlag', value: boolean): void;
}>();

// Composables & injections
const { t } = useI18n();
const projectTypeVisibilityMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({
  showTask: true,
  showBackLog: true,
  showMeeting: true,
  showSprint: true,
  showTasStatistics: true
}));
const route = useRoute();

// eslint-disable-next-line no-undef
let database: XCanDexie<{ id: string; data: any; }>;

const searchPanelRef = ref();

// Quick search date mapping for predefined time ranges
const quickSearchDateMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());
// Map to track selected menu items for quick search
const selectedQuickSearchItems = ref(new Map<string, Omit<SearchPanelMenuItem, 'name'>>());
// Overdue tasks filter flag
const isOverdueFilterEnabled = ref(false);
// Module grouping flag
const isModuleGroupingEnabled = ref(false);

// Sprint selection state
const isSprintSelectorVisible = ref(false);
const selectedSprintOption = ref<SearchPanelOption>();
const checkedSprintId = ref<string>();

// Tag selection state
const isTagSelectorVisible = ref(false);
const selectedTagOptions = ref<SearchPanelOption[]>([]);
const checkedTagIds = ref<string[]>([]);

// Search criteria filters
const searchFilters = ref<SearchCriteria[]>([]);

// Target filters for API/Scenario tests
const targetParentIdFilter = ref<SearchCriteria>({ key: 'targetParentId', op: SearchCriteria.OpEnum.Equal, value: undefined });
const targetIdFilter = ref<SearchCriteria>({ key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined });

// Numeric filters
const workloadFilter = ref<SearchCriteria>({ key: 'evalWorkload', op: SearchCriteria.OpEnum.Equal, value: undefined });
const failureCountFilter = ref<SearchCriteria>({ key: 'failNum', op: SearchCriteria.OpEnum.Equal, value: undefined });
const totalCountFilter = ref<SearchCriteria>({ key: 'totalNum', op: SearchCriteria.OpEnum.Equal, value: undefined });
const taskTypeOptions = ref<SearchPanelMenuItem[]>([]);

/**
 * Loads task type enum options for the search panel
 * Converts enum values to menu items with display names
 */
const loadTaskTypeOptions = () => {
  const enumData = enumUtils.enumToMessages(TaskType);
  taskTypeOptions.value = enumData.map(item => ({
    name: item.message,
    key: item.value
  }));
};

/**
 * Handles sorting configuration changes
 * @param data - Contains orderBy field and orderSort direction
 */
const handleSortingChange = (
  data: { orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName';
    orderSort: PageQuery.OrderSort; }
) => {
  emit('update:orderBy', data.orderBy);
  emit('update:orderSort', data.orderSort);
};

/**
 * Handles grouping configuration changes
 * @param value - The grouping key to apply
 */
const handleGroupingChange = (
  value: 'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType'
) => {
  emit('update:groupKey', value);
};

/**
 * Handles quick search menu item clicks
 * Manages the selection/deselection of quick search filters
 * @param data - The clicked menu item data
 */
const handleQuickSearchMenuItemClick = (data: SearchPanelMenuItem) => {
  const itemKey = data.key;

  // Handle deselection of already selected items
  if (selectedQuickSearchItems.value.has(itemKey)) {
    // "All" button remains selected when clicked again
    if (itemKey === 'none') {
      return;
    }

    // Remove the selected item
    selectedQuickSearchItems.value.delete(itemKey);

    // Clear corresponding search panel configurations
    if (itemKey === 'createdBy') {
      updateSearchPanelConfigs([{ valueKey: 'createdBy', value: undefined }]);
      return;
    }

    if (itemKey === 'assigneeId' || itemKey === 'progress') {
      updateSearchPanelConfigs([
        { valueKey: 'assigneeId', value: undefined },
        { valueKey: 'status', value: undefined }
      ]);
      return;
    }

    if (itemKey === 'confirmerId') {
      updateSearchPanelConfigs([
        { valueKey: 'confirmerId', value: undefined },
        { valueKey: 'status', value: undefined }
      ]);
      return;
    }

    if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(itemKey)) {
      quickSearchDateMap.value.clear();
      updateSearchPanelConfigs([{ valueKey: 'createdDate', value: undefined }]);
      return;
    }

    // Clear task type selections
    enumUtils.getEnumValues(TaskType).forEach(taskType => {
      selectedQuickSearchItems.value.delete(taskType);
    });
    updateSearchPanelConfigs([{ valueKey: 'taskType', value: undefined }]);
    return;
  }

  // Handle "All" selection - clears all other filters
  if (itemKey === 'none') {
    clearAllFilters();
    return;
  }

  // Handle specific filter selections
  if (itemKey === 'createdBy') {
    updateSearchPanelConfigs([{ valueKey: 'createdBy', value: currentUserId.value }]);
    return;
  }

  if (itemKey === 'assigneeId' || itemKey === 'progress') {
    const statusValue = itemKey === 'assigneeId' ? TaskStatusEnum.PENDING : TaskStatusEnum.IN_PROGRESS;
    updateSearchPanelConfigs([
      { valueKey: 'assigneeId', value: currentUserId.value },
      { valueKey: 'status', value: statusValue }
    ]);
    return;
  }

  if (itemKey === 'confirmerId') {
    updateSearchPanelConfigs([
      { valueKey: 'confirmerId', value: currentUserId.value },
      { valueKey: 'status', value: TaskStatusEnum.CONFIRMING }
    ]);
    return;
  }

  if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(itemKey)) {
    quickSearchDateMap.value.clear();
    const dateRange = formatDateRange(itemKey as 'lastDay' | 'lastThreeDays' | 'lastWeek');
    quickSearchDateMap.value.set(itemKey as 'lastDay' | 'lastThreeDays' | 'lastWeek', dateRange);
    updateSearchPanelConfigs([{ valueKey: 'createdDate', value: dateRange }]);
  }

  if (enumUtils.getEnumValues(TaskType).includes(itemKey)) {
    // Clear other task type selections
    enumUtils.getEnumValues(TaskType).forEach(taskType => {
      selectedQuickSearchItems.value.delete(taskType);
    });
    selectedQuickSearchItems.value.set(itemKey, { key: itemKey });
    updateSearchPanelConfigs([{ valueKey: 'taskType', value: itemKey }]);
  }
};

/**
 * Clears all filters and resets the search panel
 */
const clearAllFilters = () => {
  selectedQuickSearchItems.value.clear();
  selectedQuickSearchItems.value.set('none', { key: 'none' });

  // Clear search panel
  if (typeof searchPanelRef.value?.clear === 'function') {
    searchPanelRef.value.clear();
  }

  // Reset all filter states
  isOverdueFilterEnabled.value = false;
  targetParentIdFilter.value.value = undefined;
  targetIdFilter.value.value = undefined;
  workloadFilter.value.value = undefined;
  failureCountFilter.value.value = undefined;
  totalCountFilter.value.value = undefined;
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
 * Formats date range for quick search time periods
 * @param key - The time period key (lastDay, lastThreeDays, lastWeek)
 * @returns Array of formatted date strings [startDate, endDate]
 */
const formatDateRange = (key: SearchPanelMenuItem['key']) => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  switch (key) {
    case 'lastDay':
      startDate = dayjs().startOf('date');
      endDate = dayjs();
      break;
    case 'lastThreeDays':
      startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
      endDate = dayjs();
      break;
    case 'lastWeek':
      startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
      endDate = dayjs();
      break;
  }

  return [
    startDate ? startDate.format(DATE_TIME_FORMAT) : '',
    endDate ? endDate.format(DATE_TIME_FORMAT) : ''
  ];
};

/**
 * Handles overdue filter toggle
 * Removes "All" selection when overdue filter is enabled
 */
const handleOverdueFilterToggle = () => {
  selectedQuickSearchItems.value.delete('none');
};

/**
 * Handles module grouping toggle
 * Saves the state to database and emits the change
 */
const handleModuleGroupingToggle = () => {
  database.add({ id: dbModuleKey.value, data: isModuleGroupingEnabled.value + '' });
  emit('update:moduleFlag', isModuleGroupingEnabled.value);
};

/**
 * Formats data for display with truncation
 * @param data - Object containing id and name
 * @returns Formatted object with display properties
 */
const formatDisplayData = ({ id, name }: { id: string; name: string; }) => {
  const MAX_DISPLAY_LENGTH = 10;
  let displayTitle = '';
  let displayName = name;

  if (name.length > MAX_DISPLAY_LENGTH) {
    displayTitle = name;
    displayName = displayName.slice(0, MAX_DISPLAY_LENGTH) + '...';
  }
  return { id, name, showTitle: displayTitle, showName: displayName };
};

/**
 * Shows the sprint selector dropdown
 */
const showSprintSelector = () => {
  isSprintSelectorVisible.value = true;
};

/**
 * Handles sprint selection from dropdown
 * @param value - Selected sprint value
 * @param option - Selected sprint option data
 */
const handleSprintSelection = (value: any, option: any) => {
  if (!value) return;

  isSprintSelectorVisible.value = false;

  selectedSprintOption.value = formatDisplayData({ id: String(value), name: option.name });
  checkedSprintId.value = String(value);
};

/**
 * Hides the sprint selector dropdown
 */
const hideSprintSelector = () => {
  isSprintSelectorVisible.value = false;
};

/**
 * Toggles the selected sprint state
 */
const toggleSprintSelection = () => {
  const id = selectedSprintOption.value?.id;
  if (checkedSprintId.value === id) {
    checkedSprintId.value = undefined;
    return;
  }
  checkedSprintId.value = id;
};

/**
 * Removes the selected sprint
 */
const removeSelectedSprint = () => {
  selectedSprintOption.value = undefined;
  checkedSprintId.value = undefined;
};

/**
 * Shows the tag selector dropdown
 */
const showTagSelector = () => {
  isTagSelectorVisible.value = true;
};

/**
 * Handles tag selection from dropdown
 * @param value - Selected tag value
 * @param option - Selected tag option data
 */
const handleTagSelection = (value: any, option: any) => {
  if (!value) return;

  isTagSelectorVisible.value = false;

  const ids = selectedTagOptions.value.map(item => item.id);
  const stringValue = String(value);
  if (ids.includes(stringValue)) {
    return;
  }

  selectedTagOptions.value.push(formatDisplayData({ id: stringValue, name: option.name }));
  checkedTagIds.value.push(stringValue);
};

/**
 * Hides the tag selector dropdown
 */
const hideTagSelector = () => {
  isTagSelectorVisible.value = false;
};

/**
 * Toggles the selected tag state
 * @param data - Tag option data
 */
const toggleTagSelection = (data: SearchPanelOption) => {
  const id = data.id;
  if (checkedTagIds.value.includes(id)) {
    checkedTagIds.value = checkedTagIds.value.filter(item => item !== id);
    return;
  }
  checkedTagIds.value.push(id);
};

/**
 * Removes the selected tag
 * @param data - Tag option data to remove
 */
const removeSelectedTag = (data: SearchPanelOption) => {
  const id = data.id;
  selectedTagOptions.value = selectedTagOptions.value.filter(item => item.id !== id);
  checkedTagIds.value = checkedTagIds.value.filter(item => item !== id);
};

/**
 * Handles task creation action
 */
const handleCreateTask = () => {
  emit('add');
};

/**
 * Handles button dropdown menu clicks
 * @param param - Contains the clicked menu key
 */
const handleButtonDropdownClick = ({ key }: { key: 'import' | 'export' }) => {
  if (key === 'import') {
    emit('uploadTask');
    return;
  }
  if (key === 'export') {
    handleExportTasks();
  }
};

/**
 * Shows the flow chart view
 */
const showFlowChart = () => {
  emit('update:visible', true);
};

/**
 * Handles task export action
 */
const handleExportTasks = () => {
  emit('export');
};

/**
 * Handles view mode changes
 * @param viewMode - The new view mode
 */
const handleViewModeChange = (viewMode: TaskViewMode) => {
  emit('viewModeChange', viewMode);

  if (database) {
    database.add({ id: dbViewModeKey.value, data: viewMode });
  }
};

/**
 * Refreshes the search results
 */
const refreshSearchResults = () => {
  emit('change', buildSearchCriteria());
};

/**
 * Toggles the collapse state of statistics panel
 */
const toggleStatisticsCollapse = () => {
  const newCollapseState = !props.collapse;
  emit('update:collapse', newCollapseState);

  if (database) {
    database.add({ id: dbCountKey.value, data: newCollapseState });
  }
};

/**
 * Handles search panel changes
 * @param data - Updated search criteria
 * @param _headers - Optional headers
 * @param key - The changed field key
 */
const handleSearchPanelChange = (
  data: SearchCriteria[],
  _headers?: { [key: string]: string },
  key?: string
) => {
  searchFilters.value = data;

  // Reset service/interface/scenario filters when task type changes
  if (key === 'taskType') {
    targetParentIdFilter.value.value = undefined;
    targetIdFilter.value.value = undefined;
  }

  // Clear quick search date selections when manual date selection is made
  if (key === 'createdDate') {
    selectedQuickSearchItems.value.delete('lastDay');
    selectedQuickSearchItems.value.delete('lastThreeDays');
    selectedQuickSearchItems.value.delete('lastWeek');
  }
};

/**
 * Handles target ID filter changes
 * @param value - The selected target ID
 */
const handleTargetIdChange = (value: any) => {
  targetIdFilter.value = { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value };
};

/**
 * Handles target parent ID filter changes
 * @param value - The selected target parent ID
 */
const handleTargetParentIdChange = (value: any) => {
  targetParentIdFilter.value = { key: 'targetParentId', op: SearchCriteria.OpEnum.Equal, value };
};

/**
 * Handles workload filter changes with debouncing
 * @param event - Input event containing the value
 */
const handleWorkloadFilterChange = debounce(duration.search, (event: any) => {
  workloadFilter.value.value = event.target.value;
});

/**
 * Handles failure count filter changes with debouncing
 * @param event - Input event containing the value
 */
const handleFailureCountFilterChange = debounce(duration.search, (event: any) => {
  failureCountFilter.value.value = event.target.value;
});

/**
 * Handles total count filter changes with debouncing
 * @param event - Input event containing the value
 */
const handleTotalCountFilterChange = debounce(duration.search, (event: any) => {
  totalCountFilter.value.value = event.target.value;
});

/**
 * Builds the complete search criteria array from all active filters
 * @returns Array of search criteria for API calls
 */
const buildSearchCriteria = () => {
  const searchCriteria: SearchCriteria[] = cloneDeep(searchFilters.value);

  // Add overdue filter if enabled
  if (isOverdueFilterEnabled.value) {
    searchCriteria.push({ key: 'overdue', op: SearchCriteria.OpEnum.Equal, value: true });
  }

  // Add sprint filter if selected
  if (checkedSprintId.value) {
    searchCriteria.push({ key: 'sprintId', op: SearchCriteria.OpEnum.Equal, value: checkedSprintId.value });
  }

  // Add tag filters if any tags are selected
  if (checkedTagIds.value.length) {
    const tagValues: string[] = [...checkedTagIds.value];
    searchCriteria.push({ key: 'tagId', op: SearchCriteria.OpEnum.In, value: tagValues });
  }

  // Add target parent ID filter if set
  if (targetParentIdFilter.value.value) {
    searchCriteria.push({ ...targetParentIdFilter.value });
  }

  // Add target ID filter if set
  if (targetIdFilter.value.value) {
    searchCriteria.push({ ...targetIdFilter.value });
  }

  // Add workload filter if set
  if (workloadFilter.value.value) {
    searchCriteria.push({ ...workloadFilter.value });
  }

  // Add failure count filter if set
  if (failureCountFilter.value.value) {
    searchCriteria.push({ ...failureCountFilter.value });
  }

  // Add total count filter if set
  if (totalCountFilter.value.value) {
    searchCriteria.push({ ...totalCountFilter.value });
  }

  return searchCriteria;
};

/**
 * Initializes the component with saved data from database
 * Loads user preferences and search criteria
 */
const initializeComponent = async () => {
  if (!database) {
    database = new XCanDexie<{ id: string; data: any; }>('parameter');
  }

  // Load statistics panel collapse state
  const [, collapseData] = await database.get(dbCountKey.value);
  emit('update:collapse', !!collapseData?.data);

  // Load task list view mode
  const [, viewModeData] = await database.get(dbViewModeKey.value);
  const viewMode = [TaskViewMode.flat, TaskViewMode.table, TaskViewMode.kanban, TaskViewMode.gantt]
    .includes(viewModeData?.data)
    ? viewModeData?.data
    : TaskViewMode.flat;
  emit('viewModeChange', viewMode);

  // Load module grouping preference
  const [, moduleData] = await database.get(dbModuleKey.value);
  isModuleGroupingEnabled.value = moduleData?.data === 'true';
  emit('update:moduleFlag', isModuleGroupingEnabled.value);

  // Load search criteria data
  const [, searchData] = await database.get(dbParamsKey.value);
  const savedSearchData = searchData?.data;
  if (savedSearchData) {
    const valueMap: { [key: string]: string | string[] } = {};
    const taskTypeMap: { [key: string]: string } = {};
    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'searchFilters')) {
      searchFilters.value = savedSearchData.searchFilters || [];
      const dateTimeKeys = ['createdDate', 'startDate', 'deadlineDate', 'processedDate', 'confirmedDate', 'completedDate', 'canceledDate', 'execDate', 'lastModifiedDate'];
      const taskTypeKeys = ['taskType'];
      const dateTimeMap: { [key: string]: string[] } = {};
      searchFilters.value.every(({ key, value }) => {
        if (key && dateTimeKeys.includes(key)) {
          if (value !== undefined) {
            if (dateTimeMap[key]) {
              dateTimeMap[key].push(value as string);
            } else {
              dateTimeMap[key] = [value as string];
            }
          }
        } else if (key && taskTypeKeys.includes(key)) {
          if (value !== undefined) {
            taskTypeMap[key] = value;
            valueMap[key] = value;
          }
        } else {
          if (key && value !== undefined) {
            valueMap[key] = value;
          }
        }
        return true;
      });

      Object.entries(dateTimeMap).every(([key, [date1, date2]]: [string, string[]]) => {
        const dateTimes: string[] = [];
        if (date1 && date2) {
          if (dayjs(date1).isBefore(dayjs(date2))) {
            dateTimes[0] = date1;
            dateTimes[1] = date2;
          } else {
            dateTimes[0] = date2;
            dateTimes[1] = date1;
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

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'isOverdueEnabled')) {
      isOverdueFilterEnabled.value = savedSearchData.isOverdueEnabled || false;
    } else {
      isOverdueFilterEnabled.value = false;
    }

    // Replace with the sprint when navigating from sprint
    if (props.sprintId) {
      checkedSprintId.value = props.sprintId;
    } else {
      if (Object.prototype.hasOwnProperty.call(savedSearchData, 'selectedSprintId')) {
        checkedSprintId.value = savedSearchData.selectedSprintId;
      } else {
        checkedSprintId.value = undefined;
      }
    }

    if (props.sprintName) {
      selectedSprintOption.value = formatDisplayData({ id: props.sprintId, name: props.sprintName });
    } else {
      if (Object.prototype.hasOwnProperty.call(savedSearchData, 'selectedSprintOption')) {
        selectedSprintOption.value = savedSearchData.selectedSprintOption;
      } else {
        selectedSprintOption.value = undefined;
      }
    }

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'selectedTagIds')) {
      checkedTagIds.value = savedSearchData.selectedTagIds || [];
    } else {
      checkedTagIds.value = [];
    }

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'selectedTagOptions')) {
      selectedTagOptions.value = savedSearchData.selectedTagOptions || [];
    } else {
      selectedTagOptions.value = [];
    }

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'workloadFilter')) {
      workloadFilter.value = savedSearchData.workloadFilter ||
        { key: 'evalWorkload', op: SearchCriteria.OpEnum.Equal, value: undefined };
    } else {
      workloadFilter.value = { key: 'evalWorkload', op: SearchCriteria.OpEnum.Equal, value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'failureCountFilter')) {
      failureCountFilter.value = savedSearchData.failureCountFilter ||
        { key: 'failNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
    } else {
      failureCountFilter.value = { key: 'failNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'totalCountFilter')) {
      totalCountFilter.value = savedSearchData.totalCountFilter ||
        { key: 'totalNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
    } else {
      totalCountFilter.value = { key: 'totalNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'targetParentIdFilter')) {
      targetParentIdFilter.value = savedSearchData.targetParentIdFilter ||
        { key: 'targetParentId', op: SearchCriteria.OpEnum.Equal, value: undefined };
    } else {
      targetParentIdFilter.value = { key: 'targetParentId', op: SearchCriteria.OpEnum.Equal, value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(savedSearchData, 'targetIdFilter')) {
      targetIdFilter.value = savedSearchData.targetIdFilter ||
        { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined };
    } else {
      targetIdFilter.value = { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined };
    }

    const configs:{valueKey: string; value: string|string[]}[] = [];
    // Quick filters outside search panel
    const taskTypeKeys = Object.values(taskTypeMap);
    if (taskTypeKeys.length) {
      taskTypeKeys.forEach(i => {
        selectedQuickSearchItems.value.set(i, { key: i });
      });
      configs.push({
        valueKey: 'taskType',
        value: taskTypeKeys[0]
      });
      if (Object.keys(valueMap).length === taskTypeKeys.length) {
        emit('change', searchFilters.value);
      }
    }

    // Set search panel data
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      if (Object.keys(valueMap).length) {
        // Set other data to empty
        configs.push(...searchOptions.map(item => {
          return {
            valueKey: item.valueKey,
            value: valueMap[item.valueKey]
          };
        }));
        searchPanelRef.value.setConfigs(configs);
      }
    }
    return;
  }

  // No cached data, need to reset search panel data
  resetData();
  resetSearchPanel();
};

/**
 * Resets the search panel to default state
 */
const resetSearchPanel = () => {
  if (typeof searchPanelRef.value?.setConfigs === 'function') {
    const configs = searchOptions.map(item => {
      return {
        valueKey: item.valueKey,
        value: undefined
      };
    });
    searchPanelRef.value.setConfigs(configs);
  }
};

/**
 * Resets all component data to initial state
 */
const resetData = () => {
  quickSearchDateMap.value.clear();
  selectedQuickSearchItems.value.clear();
  isOverdueFilterEnabled.value = false;

  isSprintSelectorVisible.value = false;
  selectedSprintOption.value = undefined;
  checkedSprintId.value = undefined;

  isTagSelectorVisible.value = false;
  selectedTagOptions.value = [];
  checkedTagIds.value = [];

  searchFilters.value = [];

  targetParentIdFilter.value = { key: 'targetParentId', op: SearchCriteria.OpEnum.Equal, value: undefined };
  targetIdFilter.value = { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined };

  workloadFilter.value = { key: 'evalWorkload', op: SearchCriteria.OpEnum.Equal, value: undefined };
  failureCountFilter.value = { key: 'failNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
  totalCountFilter.value = { key: 'totalNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
};

onMounted(async () => {
  await loadTaskTypeOptions();
  watch([() => dbParamsKey.value, () => dbCountKey.value, () => dbViewModeKey.value], ([key1, key2, key3]) => {
    if (!key1 || !key2 || !key3) {
      return;
    }

    initializeComponent();
  }, { immediate: true });

  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#task')) {
      return;
    }

    const queryString = route.hash.split('?')[1];
    if (!queryString) {
      return;
    }

    const queryParameters = queryString.split('&').reduce((prev, cur) => {
      const [key, value] = cur.split('=');
      prev[key] = value;
      return prev;
    }, {} as { [key: string]: string });

    const { sprintId, sprintName } = queryParameters;
    if (sprintId) {
      checkedSprintId.value = sprintId;
    }

    if (sprintName) {
      selectedSprintOption.value = formatDisplayData({ id: sprintId, name: sprintName });
    }
  }, { immediate: false });

  watch(
    [
      () => searchFilters.value,
      () => isOverdueFilterEnabled.value,
      () => checkedSprintId.value,
      () => checkedTagIds.value.length,
      () => targetParentIdFilter.value,
      () => targetIdFilter.value,
      () => workloadFilter.value,
      () => failureCountFilter.value,
      () => totalCountFilter.value,
      () => selectedQuickSearchItems.value
    ], () => {
      const _filters = searchFilters.value;
      if (!(_filters.length ||
        isOverdueFilterEnabled.value ||
        !!targetParentIdFilter.value.value ||
        !!targetIdFilter.value.value ||
        !!workloadFilter.value.value ||
        !!failureCountFilter.value.value ||
        !!totalCountFilter.value.value)) {
        selectedQuickSearchItems.value.clear();
        selectedQuickSearchItems.value.set('none', { key: 'none' });

        emit('change', buildSearchCriteria());
      } else {
        // Remove the selected "All" option from quick search
        selectedQuickSearchItems.value.delete('none');

        // Set up quick search
        const createdBy = _filters.find(item => item.key === 'createdBy')?.value;
        if (createdBy && createdBy === currentUserId.value) {
          selectedQuickSearchItems.value.set('createdBy', { key: 'createdBy' });
        } else {
          selectedQuickSearchItems.value.delete('createdBy');
        }

        const status = _filters.find(item => item.key === 'status')?.value;

        const assigneeId = _filters.find(item => item.key === 'assigneeId')?.value;
        if (status && status === TaskStatusEnum.PENDING && assigneeId === currentUserId.value) {
          selectedQuickSearchItems.value.set('assigneeId', { key: 'assigneeId' });

          // Remove "Awaiting my confirmation"
          selectedQuickSearchItems.value.delete('confirmerId');
          // Remove "In my progress"
          selectedQuickSearchItems.value.delete('progress');
        } else {
          selectedQuickSearchItems.value.delete('assigneeId');
        }

        if (status && status === TaskStatusEnum.IN_PROGRESS && assigneeId === currentUserId.value) {
          selectedQuickSearchItems.value.set('progress', { key: 'progress' });

          // Remove "Awaiting my confirmation"
          selectedQuickSearchItems.value.delete('confirmerId');
          // Remove "Awaiting my processing"
          selectedQuickSearchItems.value.delete('assigneeId');
        } else {
          selectedQuickSearchItems.value.delete('progress');
        }

        const confirmerId = _filters.find(item => item.key === 'confirmerId')?.value;
        if (status && status === TaskStatusEnum.CONFIRMING && confirmerId === currentUserId.value) {
          selectedQuickSearchItems.value.set('confirmerId', { key: 'confirmerId' });

          // Remove "Awaiting my processing"
          selectedQuickSearchItems.value.delete('assigneeId');
          // Remove "In my progress"
          selectedQuickSearchItems.value.delete('progress');
        } else {
          selectedQuickSearchItems.value.delete('confirmerId');
        }

        // Task type
        const taskType = _filters.find(item => item.key === 'taskType')?.value;
        if (taskType) {
          selectedQuickSearchItems.value.set(taskType, { key: taskType });
        } else {
          [TaskType.REQUIREMENT, TaskType.STORY, TaskType.TASK, TaskType.BUG, TaskType.API_TEST, TaskType.SCENARIO_TEST].forEach(item => {
            selectedQuickSearchItems.value.delete(item);
          });
        }

        if (quickSearchDateMap.value.size > 0) {
          selectedQuickSearchItems.value.delete('lastDay');
          selectedQuickSearchItems.value.delete('lastThreeDays');
          selectedQuickSearchItems.value.delete('lastWeek');

          const createdDateStart = _filters.find(item => item.key === 'createdDate' &&
            item.op === SearchCriteria.OpEnum.GreaterThanEqual)?.value;
          const createdDateEnd = _filters.find(item => item.key === 'createdDate' &&
            item.op === SearchCriteria.OpEnum.LessThanEqual)?.value;
          const dateString = [createdDateStart, createdDateEnd];
          const entries = quickSearchDateMap.value.entries();
          for (const [key, value] of entries) {
            if (isEqual(value, dateString)) {
              selectedQuickSearchItems.value.set(key, { key });
            }
          }
          quickSearchDateMap.value.clear();
        }

        emit('change', buildSearchCriteria());
      }

      // Save to database
      if (database) {
        const dbData: {
          searchFilters?: SearchCriteria[];
          isOverdueEnabled?: true;
          selectedSprintId?: string;
          selectedSprintOption?: {
            id: string;
            name: string;
            showTitle: string;
            showName: string;
          };
          selectedTagIds?: string[];
          selectedTagOptions?: {
            id: string;
            name: string;
            showTitle: string;
            showName: string;
          }[];
          workloadFilter?: SearchCriteria;
          failureCountFilter?: SearchCriteria;
          totalCountFilter?: SearchCriteria;
          targetParentIdFilter?: SearchCriteria;
          targetIdFilter?: SearchCriteria;
        } = {};
        if (_filters.length) {
          dbData.searchFilters = cloneDeep(_filters);
        }

        if (isOverdueFilterEnabled.value) {
          dbData.isOverdueEnabled = isOverdueFilterEnabled.value;
        }

        if (checkedSprintId.value) {
          dbData.selectedSprintId = checkedSprintId.value;
        }

        if (selectedSprintOption.value) {
          dbData.selectedSprintOption = cloneDeep(selectedSprintOption.value);
        }

        if (checkedTagIds.value.length) {
          dbData.selectedTagIds = cloneDeep(checkedTagIds.value);
        }

        if (selectedTagOptions.value.length) {
          dbData.selectedTagOptions = cloneDeep(selectedTagOptions.value);
        }

        if (workloadFilter.value.value) {
          dbData.workloadFilter = cloneDeep(workloadFilter.value);
        }

        if (failureCountFilter.value.value) {
          dbData.failureCountFilter = cloneDeep(failureCountFilter.value);
        }

        if (totalCountFilter.value.value) {
          dbData.totalCountFilter = cloneDeep(totalCountFilter.value);
        }

        if (targetParentIdFilter.value.value) {
          dbData.targetParentIdFilter = cloneDeep(targetParentIdFilter.value);
        }

        if (targetIdFilter.value.value) {
          dbData.targetIdFilter = cloneDeep(targetIdFilter.value);
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
});

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
  return btoa(dbBaseKey.value + TaskType.TASK);
});

/**
 * Database key for statistics panel collapse state
 */
const dbCountKey = computed(() => {
  return btoa(dbBaseKey.value + 'count');
});

/**
 * Database key for view mode preference
 */
const dbViewModeKey = computed(() => {
  return btoa(dbBaseKey.value + 'viewMode');
});

/**
 * Database key for module grouping preference
 */
const dbModuleKey = computed(() => {
  return btoa(dbBaseKey.value + 'moduleFlag');
});

/**
 * Determines whether to show the add sprint button
 */
const showAddSprintBtn = computed(() => {
  if (isSprintSelectorVisible.value) {
    return false;
  }

  return !selectedSprintOption.value;
});

/**
 * Determines whether to show the add tag button (max 3 tags)
 */
const showAddTagBtn = computed(() => {
  if (isTagSelectorVisible.value) {
    return false;
  }
  return selectedTagOptions.value.length < 3;
});

/**
 * API parameters for target parent ID filter
 */
const apiParams = computed(() => {
  return {
    serviceId: targetParentIdFilter.value.value
  };
});

/**
 * Current task type from search filters
 */
const taskType = computed(() => {
  return searchFilters.value.find(item => item.key === 'taskType')?.value;
});

/**
 * Checks if current task type is API test
 */
const isAPITest = computed(() => {
  return taskType.value === TaskType.API_TEST;
});

/**
 * Checks if current task type is scenario test
 */
const isScenarioTest = computed(() => {
  return taskType.value === TaskType.SCENARIO_TEST;
});

const modeOptions = [
  {
    key: TaskViewMode.flat,
    name: t('task.searchPanel.viewMode.flat'),
    label: ''

  },
  {
    key: TaskViewMode.table,
    name: t('task.searchPanel.viewMode.table'),
    label: ''
  },
  {
    key: TaskViewMode.kanban,
    name: t('task.searchPanel.viewMode.kanban'),
    label: ''
  },
  {
    key: TaskViewMode.gantt,
    name: t('task.searchPanel.viewMode.gantt'),
    label: ''
  }
];

/**
 * Gets the display title for current view mode
 */
const modeTitle = computed(() => {
  if (props.viewMode === TaskViewMode.kanban) {
    return t('task.searchPanel.viewMode.kanban');
  }

  if (props.viewMode === TaskViewMode.flat) {
    return t('task.searchPanel.viewMode.flat');
  }

  if (props.viewMode === TaskViewMode.table) {
    return t('task.searchPanel.viewMode.table');
  }

  return t('task.searchPanel.viewMode.gantt');
});

/**
 * Gets the icon class for current view mode
 */
const modeIcon = computed(() => {
  if (props.viewMode === TaskViewMode.kanban) {
    return 'icon-kanbanshitu';
  }

  if (props.viewMode === TaskViewMode.flat) {
    return 'icon-pingpushitu';
  }

  if (props.viewMode === TaskViewMode.table) {
    return 'icon-liebiaoshitu';
  }

  return 'icon-yemianshitu';
});

/**
 * Quick search menu items for filtering tasks
 */
const menuItems = computed(():SearchPanelMenuItem[] => {
  return [
    {
      key: 'none',
      name: t('task.searchPanel.menuItems.all')
    },
    {
      key: 'createdBy',
      name: t('task.searchPanel.menuItems.myCreated')
    },
    {
      key: 'assigneeId',
      name: t('task.searchPanel.menuItems.myAssigned')
    },
    {
      key: 'progress',
      name: t('task.searchPanel.menuItems.myProgress')
    },
    {
      key: 'confirmerId',
      name: t('task.searchPanel.menuItems.myConfirm')
    },
    ...taskTypeOptions.value,
    {
      key: 'lastDay',
      name: t('task.searchPanel.menuItems.lastDay')
    },
    {
      key: 'lastThreeDays',
      name: t('task.searchPanel.menuItems.lastThreeDays')
    },
    {
      key: 'lastWeek',
      name: t('task.searchPanel.menuItems.lastWeek')
    }
  ];
});

const searchOptions = [
  {
    type: 'input' as const,
    valueKey: 'name',
    placeholder: t('task.searchPanel.searchOptions.name')
  },
  {
    type: 'select-enum' as const,
    valueKey: 'status',
    placeholder: t('task.searchPanel.searchOptions.status'),
    enumKey: TaskStatusEnum
  },
  {
    type: 'select-enum' as const,
    valueKey: 'priority',
    placeholder: t('task.searchPanel.searchOptions.priority'),
    enumKey: Priority
  },
  {
    type: 'select-user' as const,
    valueKey: 'assigneeId',
    placeholder: t('task.searchPanel.searchOptions.assigneeId'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-user' as const,
    valueKey: 'confirmerId',
    placeholder: t('task.searchPanel.searchOptions.confirmerId'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-user' as const,
    valueKey: 'execBy',
    placeholder: t('task.searchPanel.searchOptions.execBy'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-user' as const,
    valueKey: 'createdBy',
    placeholder: t('task.searchPanel.searchOptions.createdBy'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-user' as const,
    valueKey: 'lastModifiedBy',
    placeholder: t('task.searchPanel.searchOptions.lastModifiedBy'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-enum' as const,
    valueKey: 'testType',
    placeholder: t('task.searchPanel.searchOptions.testType'),
    enumKey: TestType
  },
  {
    type: 'select' as const,
    action: `${TESTER}/module?fullTextSearch=true`,
    params: { projectId: props.projectId },
    valueKey: 'moduleId',
    showSearch: true,
    placeholder: t('task.searchPanel.searchOptions.moduleId'),
    fieldNames: { label: 'name', value: 'id' }
  },
  {
    type: 'select-enum' as const,
    valueKey: 'execResult',
    placeholder: t('task.searchPanel.searchOptions.execResult'),
    enumKey: Result
  },
  {
    type: 'select' as const,
    valueKey: 'targetParentId',
    noDefaultSlot: true
  },
  {
    type: 'select' as const,
    valueKey: 'targetId',
    noDefaultSlot: true
  },
  {
    type: 'date-range' as const,
    valueKey: 'createdDate',
    placeholder: t('task.searchPanel.searchOptions.createdDate').split(','),
    showTime: true
  },
  {
    type: 'date-range' as const,
    valueKey: 'startDate',
    placeholder: t('task.searchPanel.searchOptions.startDate').split(','),
    showTime: true
  },
  {
    type: 'date-range' as const,
    valueKey: 'deadlineDate',
    placeholder: t('task.searchPanel.searchOptions.deadlineDate').split(','),
    showTime: true
  },
  {
    type: 'date-range' as const,
    valueKey: 'processedDate',
    placeholder: t('task.searchPanel.searchOptions.processedDate').split(','),
    showTime: true
  },
  {
    type: 'date-range' as const,
    valueKey: 'confirmedDate',
    placeholder: t('task.searchPanel.searchOptions.confirmedDate').split(','),
    showTime: true
  },
  {
    type: 'date-range' as const,
    valueKey: 'completedDate',
    placeholder: t('task.searchPanel.searchOptions.completedDate').split(','),
    showTime: true
  },
  {
    type: 'date-range' as const,
    valueKey: 'canceledDate',
    placeholder: t('task.searchPanel.searchOptions.canceledDate').split(','),
    showTime: true
  },
  {
    type: 'date-range' as const,
    valueKey: 'execDate',
    placeholder: t('task.searchPanel.searchOptions.execDate').split(','),
    showTime: true
  },
  {
    type: 'date-range' as const,
    valueKey: 'lastModifiedDate',
    placeholder: t('task.searchPanel.searchOptions.lastModifiedDate').split(','),
    showTime: true
  },
  {
    type: 'input' as const,
    valueKey: 'evalWorkload',
    noDefaultSlot: true
  },
  {
    type: 'input' as const,
    valueKey: 'failNum',
    noDefaultSlot: true
  },
  {
    type: 'input' as const,
    valueKey: 'totalNum',
    noDefaultSlot: true
  }
];

const buttonDropdownMenuItems = [
  {
    name: t('task.list.actions.exportTasks'),
    key: 'export',
    icon: 'icon-daochu1',
    noAuth: true
  },
  {
    name: t('task.list.actions.importTasks'),
    key: 'import',
    icon: 'icon-shangchuan',
    noAuth: true
  }
];

const EXEC_RESULT_COLOR = {
  FAIL: '#F5222D',
  SUCCESS: '#52C41A'
};

const fieldNames = { label: 'name', value: 'id' };

const groupMenuItems = [
  {
    key: 'none',
    name: t('task.searchPanel.groupOptions.none')
  },
  {
    key: 'assigneeName',
    name: t('task.searchPanel.groupOptions.assigneeName')
  },
  {
    key: 'lastModifiedByName',
    name: t('task.searchPanel.groupOptions.lastModifiedByName')
  },
  {
    key: 'taskType',
    name: t('task.searchPanel.groupOptions.taskType')
  }
];

const sortMenuItems = [
  {
    key: 'createdByName',
    name: t('task.searchPanel.sortOptions.createdByName'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'assigneeName',
    name: t('task.searchPanel.sortOptions.assigneeName'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'priority',
    name: t('task.searchPanel.sortOptions.priority'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'deadlineDate',
    name: t('task.searchPanel.sortOptions.deadlineDate'),
    orderSort: PageQuery.OrderSort.Asc
  }];
</script>
<template>
  <div class="text-3 leading-5">
    <div class="flex items-start justify-between mb-1.5">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-1 mt-1.5 rounded-full"></div>
        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>{{ t('task.searchPanel.quickSearch') }}</span>
          <Colon />
        </div>
        <div class="flex  flex-wrap ml-2">
          <div
            v-for="item in menuItems"
            :key="item.key"
            :class="{ 'active-key': selectedQuickSearchItems.has(item.key) }"
            class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
            @click="handleQuickSearchMenuItemClick(item)">
            {{ item.name }}
          </div>

          <div class="inline-flex items-center mr-3 mb-3 space-x-1">
            <Switch
              v-model:checked="isOverdueFilterEnabled"
              size="small"
              @change="handleOverdueFilterToggle">
            </Switch>
            <span>{{ t('status.overdue') }}</span>
          </div>

          <div class="inline-flex items-center mr-3 mb-3 space-x-1">
            <Switch
              v-model:checked="isModuleGroupingEnabled"
              size="small"
              style="margin-left:0;"
              @change="handleModuleGroupingToggle">
            </Switch>
            <span>{{ t('task.searchPanel.moduleGroup') }}</span>
          </div>

          <template v-if="selectedSprintOption?.id">
            <Tag
              :title="selectedSprintOption?.showTitle"
              :class="checkedSprintId === selectedSprintOption.id ? 'sprint tag-checked' : ''"
              closable
              class="h-6 mr-5 mb-3 rounded-xl px-2.5"
              @click="toggleSprintSelection"
              @close="removeSelectedSprint">
              {{ selectedSprintOption?.showName }}
            </Tag>
          </template>

          <Select
            v-if="isSprintSelectorVisible && projectTypeVisibilityMap.showSprint"
            :value="selectedSprintOption?.id"
            size="small"
            class="w-43 h-7 transform-gpu -translate-y-0.5 mr-5 mb-3"
            :placeholder="t('task.list.search.sprintPlaceholder')"
            showSearch
            autofocus
            :fieldNames="fieldNames"
            :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
            @change="handleSprintSelection"
            @blur="hideSprintSelector" />

          <Button
            v-if="showAddSprintBtn && projectTypeVisibilityMap.showSprint"
            class="h-6 mr-5 mb-3 px-2.5 flex items-center"
            size="small"
            @click="showSprintSelector">
            <Icon icon="icon-jia" class="text-3 mr-1" />
            <span>{{ t('task.list.search.sprint') }}</span>
          </Button>

          <Tag
            v-for="item in selectedTagOptions"
            :key="item.id"
            :title="item.showTitle"
            :class="checkedTagIds.includes(item.id) ? 'tag tag-checked' : ''"
            closable
            class="h-6 mb-3 rounded-xl px-2.5"
            @click="toggleTagSelection(item)"
            @close="removeSelectedTag(item)">
            {{ item.showName }}
          </Tag>

          <Select
            v-if="isTagSelectorVisible"
            :value="checkedTagIds"
            size="small"
            class="w-43 h-7 transform-gpu -translate-y-0.5 mb-3 mr-5"
            :placeholder="t('task.list.search.tagsPlaceholder')"
            showSearch
            autofocus
            :fieldNames="fieldNames"
            :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
            @change="handleTagSelection"
            @blur="hideTagSelector" />

          <Button
            v-if="showAddTagBtn"
            class="h-6 px-2.5 mb-3 flex items-center mr-5"
            size="small"
            @click="showTagSelector">
            <Icon icon="icon-jia" class="text-3 mr-1" />
            <span>{{ t('task.list.search.tags') }}</span>
          </Button>

          <template v-if="props.viewMode === TaskViewMode.kanban">
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
                <span class="ml-1">{{ t('actions.group') }}</span>
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
        <template #status_option="record">
          <TaskStatus :value="record" />
        </template>

        <template #moduleId_option="record">
          <div class="flex items-center truncate" :title="record.name">
            <Icon icon="icon-mokuai" class="mr-1 text-4" />
            <div class="truncate">{{ record.name }}</div>
          </div>
        </template>

        <template #taskType_option="record">
          <div class="flex items-center">
            <IconTask :value="record.value" class="text-4 flex-shrink-0" />
            <span class="ml-1.5">{{ record.message }}</span>
          </div>
        </template>

        <template #priority_option="record">
          <TaskPriority :value="record" />
        </template>

        <template #execResult_option="record">
          <span :style="'color:' + EXEC_RESULT_COLOR[record.value]">{{ record.message }}</span>
        </template>

        <template #targetParentId>
          <Select
            v-if="isAPITest"
            :value="targetParentIdFilter.value"
            :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="t('task.list.search.servicePlaceholder')"
            class="w-72 ml-2"
            showSearch
            @change="handleTargetParentIdChange">
            <template #option="record">
              <div class="text-3 leading-3 flex items-center h-6.5">
                <IconText
                  class="flex-shrink-0"
                  style="width:16px;height: 16px;background-color: rgba(162,222,236,100%);"
                  text="S" />
                <div :title="record.name" class="truncate ml-1.5">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </template>

        <template #targetId>
          <Select
            v-if="isAPITest"
            :value="targetIdFilter.value"
            :action="`${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`"
            :params="apiParams"
            :fieldNames="{ label: 'summary', value: 'id' }"
            :allowClear="true"
            :placeholder="t('task.list.search.apiPlaceholder')"
            class="w-72 ml-2"
            showSearch
            @change="handleTargetIdChange">
            <template #option="record">
              <div class="flex items-center">
                <Icon
                  icon="icon-jiekou"
                  class="text-4 flex-shrink-0"
                  style="color:rgba(82,196,26,100%);" />
                <div :title="record.summary" class="truncate ml-1.5">{{ record.summary }}</div>
              </div>
            </template>
          </Select>

          <Select
            v-if="isScenarioTest"
            :value="targetIdFilter.value"
            :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="t('task.list.search.scenarioPlaceholder')"
            class="w-72 ml-2"
            showSearch
            @change="handleTargetIdChange">
            <template #option="record">
              <div class="flex items-center">
                <Icon icon="icon-changjing" class="text-4 flex-shrink-0" />
                <div :title="record.name" class="truncate ml-1.5">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </template>

        <template #evalWorkload>
          <Input
            :value="workloadFilter.value"
            dataType="float"
            allowClear
            :max="100"
            :placeholder="t('task.searchPanel.searchOptions.workloadPlaceholder')"
            class="w-72 ml-2 scope-select"
            @change="handleWorkloadFilterChange">
            <template #prefix>
              <SelectEnum
                v-model:value="workloadFilter.op"
                :bordered="false"
                enumKey="NumberCompareCondition"
                class="w-26" />
            </template>
          </Input>
        </template>

        <template #failNum>
          <Input
            :value="failureCountFilter.value"
            dataType="float"
            allowClear
            :max="100"
            :placeholder="t('task.searchPanel.searchOptions.failNumPlaceholder')"
            class="w-72 ml-2 scope-select"
            @change="handleFailureCountFilterChange">
            <template #prefix>
              <SelectEnum
                v-model:value="failureCountFilter.op"
                :bordered="false"
                enumKey="NumberCompareCondition"
                class="w-26" />
            </template>
          </Input>
        </template>

        <template #totalNum>
          <Input
            :value="totalCountFilter.value"
            dataType="float"
            allowClear
            :max="100"
            :placeholder="t('task.searchPanel.searchOptions.totalNumPlaceholder')"
            class="w-72 ml-2 scope-select"
            @change="handleTotalCountFilterChange">
            <template #prefix>
              <SelectEnum
                v-model:value="totalCountFilter.op"
                :bordered="false"
                enumKey="NumberCompareCondition"
                class="w-26" />
            </template>
          </Input>
        </template>
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2">
        <Button
          class="flex-shrink-0 flex items-center pr-0"
          type="primary"
          size="small"
          @click="handleCreateTask">
          <div class="flex items-center">
            <Icon icon="icon-jia" class="text-3.5" />
            <span class="ml-1">{{ t('task.list.actions.addTask') }}</span>
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
          :title="t('task.list.actions.flowChart')">
          <Icon
            icon="icon-liuchengtu"
            class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0"
            @click="showFlowChart" />
        </Tooltip>

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
          :title="props.collapse ? t('actions.collapse') : t('actions.expand')">
          <IconCount
            :value="!props.collapse"
            class="text-4 flex-shrink-0"
            @click="toggleStatisticsCollapse" />
        </Tooltip>

        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          :title="t('actions.refresh')">
          <IconRefresh class="text-4 flex-shrink-0" @click="refreshSearchResults" />
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

.ant-tag {
  background-color: #fff;
}

.ant-tag.tag-checked {
  background-color: rgba(255, 129, 0, 60%);
  color: #fff;
}

.ant-tag.tag-checked :deep(.ant-tag-close-icon)>svg {
  color: #fff;
}

:deep(.sprint.tag-checked) {
  background-color: rgba(255, 129, 0, 60%);
}

.tag.tag-checked {
  background-color: #4ea0fd;
}

/* :deep(.mode-select) .ant-select-selector {
  @apply px-1 py-0;
} */

/* :deep(.mode-select .ant-select-selector .ant-select-selection-item){
  @apply hidden;
} */
</style>
