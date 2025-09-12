<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { PageQuery, SearchCriteria, XCanDexie } from '@xcan-angus/infra';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import { TaskStatus } from '@/enums/enums';

import { MenuItem, SelectOption } from '@/views/task/analysis/list/types';
import { TaskViewMode } from '@/views/task/task/types';

// Type Definitions
type Props = {
  collapse: boolean;
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

// Props and Emits
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
}>();

// Composables and External Dependencies
const route = useRoute();
const { t } = useI18n();

/**
 * Database instance for storing search parameters
 */
// eslint-disable-next-line no-undef
let searchParametersDatabase: XCanDexie<{ id: string; data: any; }>;

/**
 * Reference to the search panel component
 */
const searchPanelComponentRef = ref();

/**
 * Map storing quick date search converted time ranges
 */
const quickDateSearchMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());

/**
 * Map storing selected menu items for quick search
 */
const selectedQuickSearchItems = ref(new Map<string, Omit<MenuItem, 'name'>>());

/**
 * Flag indicating if overdue filter is active
 */
const isOverdueFilterActive = ref(false);

/**
 * Sprint selection visibility state
 */
const isSprintSelectionVisible = ref(false);

/**
 * Currently selected sprint option
 */
const selectedSprintOption = ref<SelectOption>();

/**
 * ID of the checked sprint
 */
const checkedSprintId = ref<string>();

/**
 * Tag selection visibility state
 */
const isTagSelectionVisible = ref(false);

/**
 * Currently selected tag options
 */
const selectedTagOptions = ref<SelectOption[]>([]);

/**
 * IDs of checked tags
 */
const checkedTagIds = ref<string[]>([]);

/**
 * Current search filter criteria
 */
const currentSearchFilters = ref<SearchCriteria[]>([]);

/**
 * Filter for target parent ID
 */
const targetParentIdSearchFilter = ref<SearchCriteria>({
  key: 'targetParentId',
  op: SearchCriteria.OpEnum.Equal,
  value: undefined
});

/**
 * Filter for target ID
 */
const targetIdSearchFilter = ref<SearchCriteria>({
  key: 'targetId',
  op: SearchCriteria.OpEnum.Equal,
  value: undefined
});

/**
 * Filter for evaluation workload
 */
const evaluationWorkloadFilter = ref<SearchCriteria>({
  key: 'evalWorkload',
  op: SearchCriteria.OpEnum.Equal,
  value: undefined
});

/**
 * Filter for failure count
 */
const failureCountFilter = ref<SearchCriteria>({
  key: 'failNum',
  op: SearchCriteria.OpEnum.Equal,
  value: undefined
});

/**
 * Filter for total count
 */
const totalCountFilter = ref<SearchCriteria>({
  key: 'totalNum',
  op: SearchCriteria.OpEnum.Equal,
  value: undefined
});

/**
 * Handle sorting configuration changes
 * @param {object} data - Sorting data containing orderBy and orderSort
 */
const handleSortingChange = (data: { orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName' ; orderSort: PageQuery.OrderSort; }) => {
  emit('update:orderBy', data.orderBy);
  emit('update:orderSort', data.orderSort);
};

/**
 * Handle quick search menu item click events
 * @param {MenuItem} menuItem - The clicked menu item
 */
const handleQuickSearchMenuItemClick = (menuItem: MenuItem) => {
  const itemKey = menuItem.key;

  // Handle deselection if item is already selected
  if (selectedQuickSearchItems.value.has(itemKey)) {
    // Do nothing if "All" button is clicked again
    if (itemKey === 'none') {
      return;
    }

    // Remove the selected item
    selectedQuickSearchItems.value.delete(itemKey);

    // Clear corresponding search panel configurations
    if (itemKey === 'createdBy') {
      clearSearchPanelConfig(['createdBy']);
      return;
    }

    if (itemKey === 'assigneeId' || itemKey === 'progress') {
      clearSearchPanelConfig(['assigneeId', 'status']);
      return;
    }

    if (itemKey === 'lastModifiedBy') {
      clearSearchPanelConfig(['lastModifiedBy']);
      return;
    }

    if (itemKey === 'confirmerId') {
      clearSearchPanelConfig(['confirmerId', 'status']);
      return;
    }

    if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(itemKey)) {
      quickDateSearchMap.value.clear();
      clearSearchPanelConfig(['createdDate']);
    }
    return;
  }

  // Handle "All" selection - clear all other selections
  if (itemKey === 'none') {
    selectedQuickSearchItems.value.clear();
    selectedQuickSearchItems.value.set('none', { key: 'none' });

    // Clear search panel
    if (typeof searchPanelComponentRef.value?.clear === 'function') {
      searchPanelComponentRef.value.clear();
    }

    // Reset all filter states
    resetAllFilterStates();
    return;
  }

  // Handle other menu item selections
  if (itemKey === 'createdBy') {
    setSearchPanelConfig([{ valueKey: 'createdBy', value: currentUserId.value }]);
    return;
  }

  if (itemKey === 'assigneeId' || itemKey === 'progress') {
    const statusValue = itemKey === 'assigneeId' ? TaskStatus.PENDING : TaskStatus.IN_PROGRESS;
    setSearchPanelConfig([
      { valueKey: 'assigneeId', value: currentUserId.value },
      { valueKey: 'status', value: statusValue }
    ]);
    return;
  }

  if (itemKey === 'lastModifiedBy') {
    setSearchPanelConfig([{ valueKey: 'lastModifiedBy', value: currentUserId.value }]);
    return;
  }

  if (itemKey === 'confirmerId') {
    setSearchPanelConfig([
      { valueKey: 'confirmerId', value: currentUserId.value },
      { valueKey: 'status', value: TaskStatus.CONFIRMING }
    ]);
    return;
  }

  if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(itemKey)) {
    quickDateSearchMap.value.clear();
    quickDateSearchMap.value.set(itemKey, formatDateRange(itemKey));
    setSearchPanelConfig([{ valueKey: 'createdDate', value: quickDateSearchMap.value.get(itemKey) }]);
  }
};

/**
 * Format date range based on quick search key
 * @param {string} key - The quick search key
 * @returns {string[]} Formatted date range array
 */
const formatDateRange = (key: MenuItem['key']) => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  if (key === 'lastDay') {
    startDate = dayjs().startOf('date');
    endDate = dayjs();
  }

  if (key === 'lastThreeDays') {
    startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    endDate = dayjs();
  }

  if (key === 'lastWeek') {
    startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    endDate = dayjs();
  }

  return [startDate ? startDate.format(DATE_TIME_FORMAT) : '', endDate ? endDate.format(DATE_TIME_FORMAT) : ''];
};

/**
 * Format data for display with truncation
 * @param {object} data - Data containing id and name
 * @returns {object} Formatted data with display properties
 */
const formatDisplayData = ({ id, name }: { id: string; name: string; }) => {
  let showTitle = '';
  let showName = name;
  const MAX_LENGTH = 10;
  if (name.length > MAX_LENGTH) {
    showTitle = name;
    showName = showName.slice(0, MAX_LENGTH) + '...';
  }

  return { id, name, showTitle, showName };
};

/**
 * Handle create button click
 */
const handleCreateClick = () => {
  emit('add');
};

/**
 * Handle refresh button click
 */
const handleRefreshClick = () => {
  emit('change', buildSearchCriteria());
};

/**
 * Handle search panel configuration changes
 * @param {SearchCriteria[]} data - New search criteria
 * @param {object} headers - Optional headers
 * @param {string} key - Optional key that triggered the change
 */
const handleSearchPanelChange = (data: SearchCriteria[], _headers?: { [key: string]: string }, key?: string) => {
  currentSearchFilters.value = data;

  // Clear quick date search selections when manual date selection is made
  if (key === 'createdDate') {
    selectedQuickSearchItems.value.delete('lastDay');
    selectedQuickSearchItems.value.delete('lastThreeDays');
    selectedQuickSearchItems.value.delete('lastWeek');
  }
};

/**
 * Build complete search criteria from all active filters
 * @returns {SearchCriteria[]} Complete search criteria array
 */
const buildSearchCriteria = () => {
  const searchCriteria: SearchCriteria[] = cloneDeep(currentSearchFilters.value);

  // Add overdue filter if active
  if (isOverdueFilterActive.value) {
    searchCriteria.push({ key: 'overdue', op: SearchCriteria.OpEnum.Equal, value: true });
  }

  // Add sprint filter if selected
  if (checkedSprintId.value) {
    searchCriteria.push({ key: 'sprintId', op: SearchCriteria.OpEnum.Equal, value: checkedSprintId.value });
  }

  // Add tag filters if any tags are selected
  if (checkedTagIds.value.length) {
    const tagValues: string[] = [];
    checkedTagIds.value.forEach(item => {
      tagValues.push(item);
    });
    searchCriteria.push({ key: 'tagId', op: SearchCriteria.OpEnum.In, value: tagValues });
  }

  // Add target parent ID filter if set
  if (targetParentIdSearchFilter.value.value) {
    searchCriteria.push({ ...(targetParentIdSearchFilter.value) });
  }

  // Add target ID filter if set
  if (targetIdSearchFilter.value.value) {
    searchCriteria.push({ ...(targetIdSearchFilter.value) });
  }

  // Add evaluation workload filter if set
  if (evaluationWorkloadFilter.value.value) {
    searchCriteria.push({ ...(evaluationWorkloadFilter.value) });
  }

  // Add failure count filter if set
  if (failureCountFilter.value.value) {
    searchCriteria.push({ ...(failureCountFilter.value) });
  }

  // Add total count filter if set
  if (totalCountFilter.value.value) {
    searchCriteria.push({ ...(totalCountFilter.value) });
  }

  return searchCriteria;
};

/**
 * Initialize search panel with saved data from database
 */
const initializeSearchPanel = async () => {
  if (!searchParametersDatabase) {
    searchParametersDatabase = new XCanDexie<{ id: string; data: any; }>('parameter');
  }

  // Load saved search parameters from database
  const [, savedData] = await searchParametersDatabase.get(databaseParamsKey.value);
  const databaseData = savedData?.data;

  if (databaseData) {
    const searchPanelValueMap: { [key: string]: string | string[] } = {};
    const nonSearchPanelValueMap: { [key: string]: string | string[] } = {};

    // Process saved filters
    if (Object.prototype.hasOwnProperty.call(databaseData, 'filters')) {
      currentSearchFilters.value = databaseData.filters || [];
      const nonSearchPanelKeys = ['lastModifiedBy'];
      const dateTimeKeys = ['createdDate'];
      const dateTimeValueMap: { [key: string]: string[] } = {};

      currentSearchFilters.value.every(({ key, value }) => {
        if (key && dateTimeKeys.includes(key)) {
          if (dateTimeValueMap[key]) {
            dateTimeValueMap[key].push(value as string);
          } else {
            dateTimeValueMap[key] = [value as string];
          }
        } else if (key && nonSearchPanelKeys.includes(key)) {
          nonSearchPanelValueMap[key] = value;
        } else if (key) {
          searchPanelValueMap[key] = value;
        }
        return true;
      });

      // Process date time ranges
      Object.entries(dateTimeValueMap).every(([key, [date1, date2]]: [string, string[]]) => {
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
          searchPanelValueMap[key] = dateTimes;
        }
        return true;
      });
    } else {
      currentSearchFilters.value = [];
    }

    // Load overdue filter state
    if (Object.prototype.hasOwnProperty.call(databaseData, 'overdue')) {
      isOverdueFilterActive.value = databaseData.overdue || false;
    } else {
      isOverdueFilterActive.value = false;
    }

    // Load sprint data (prioritize props over saved data)
    if (props.sprintId) {
      checkedSprintId.value = props.sprintId;
    } else {
      if (Object.prototype.hasOwnProperty.call(databaseData, 'checkedSprintId')) {
        checkedSprintId.value = databaseData.checkedSprintId;
      } else {
        checkedSprintId.value = undefined;
      }
    }

    if (props.sprintName) {
      selectedSprintOption.value = formatDisplayData({ id: props.sprintId, name: props.sprintName });
    } else {
      if (Object.prototype.hasOwnProperty.call(databaseData, 'selectedSprint')) {
        selectedSprintOption.value = databaseData.selectedSprint;
      } else {
        selectedSprintOption.value = undefined;
      }
    }

    // Load tag data
    if (Object.prototype.hasOwnProperty.call(databaseData, 'checkedTagIds')) {
      checkedTagIds.value = databaseData.checkedTagIds || [];
    } else {
      checkedTagIds.value = [];
    }

    if (Object.prototype.hasOwnProperty.call(databaseData, 'selectedTags')) {
      selectedTagOptions.value = databaseData.selectedTags || [];
    } else {
      selectedTagOptions.value = [];
    }

    // Load custom filters
    loadCustomFiltersFromDatabase(databaseData);

    // Set up non-search panel quick filters
    const nonSearchPanelKeys = Object.keys(nonSearchPanelValueMap);
    if (nonSearchPanelKeys.length) {
      nonSearchPanelKeys.forEach(key => {
        selectedQuickSearchItems.value.set(key as any, { key: key as any });
      });
      if (!Object.keys(searchPanelValueMap).length) {
        emit('change', currentSearchFilters.value);
      }
    }

    // Configure search panel with saved values
    configureSearchPanelWithValues(searchPanelValueMap);
    return;
  }

  // No saved data, reset to defaults
  resetAllSearchStates();
  resetSearchPanelConfiguration();
};

/**
 * Load custom filters from database data
 * @param {any} databaseData - Database data object
 */
const loadCustomFiltersFromDatabase = (databaseData: any) => {
  // Load evaluation workload filter
  if (Object.prototype.hasOwnProperty.call(databaseData, 'evalWorkloadFilter')) {
    evaluationWorkloadFilter.value = databaseData.evalWorkloadFilter ||
        { key: 'evalWorkload', op: SearchCriteria.OpEnum.Equal, value: undefined };
  } else {
    evaluationWorkloadFilter.value = { key: 'evalWorkload', op: SearchCriteria.OpEnum.Equal, value: undefined };
  }

  // Load failure count filter
  if (Object.prototype.hasOwnProperty.call(databaseData, 'failNumFilter')) {
    failureCountFilter.value = databaseData.failNumFilter ||
        { key: 'failNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
  } else {
    failureCountFilter.value = { key: 'failNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
  }

  // Load total count filter
  if (Object.prototype.hasOwnProperty.call(databaseData, 'totalNumFilter')) {
    totalCountFilter.value = databaseData.totalNumFilter ||
        { key: 'totalNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
  } else {
    totalCountFilter.value = { key: 'totalNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
  }

  // Load target parent ID filter
  if (Object.prototype.hasOwnProperty.call(databaseData, 'targetParentIdFilter')) {
    targetParentIdSearchFilter.value = databaseData.targetParentIdFilter ||
        { key: 'targetParentId', op: SearchCriteria.OpEnum.Equal, value: undefined };
  } else {
    targetParentIdSearchFilter.value = { key: 'targetParentId', op: SearchCriteria.OpEnum.Equal, value: undefined };
  }

  // Load target ID filter
  if (Object.prototype.hasOwnProperty.call(databaseData, 'targetIdFilter')) {
    targetIdSearchFilter.value = databaseData.targetIdFilter ||
        { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined };
  } else {
    targetIdSearchFilter.value = { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined };
  }
};

/**
 * Configure search panel with saved values
 * @param {object} valueMap - Map of values to configure
 */
const configureSearchPanelWithValues = (valueMap: { [key: string]: string | string[] }) => {
  if (typeof searchPanelComponentRef.value?.setConfigs === 'function') {
    const valueMapKeys = Object.keys(valueMap);
    if (valueMapKeys.length) {
      const configs = searchPanelOptions.map(item => ({
        valueKey: item.valueKey,
        value: valueMap[item.valueKey]
      }));
      searchPanelComponentRef.value.setConfigs(configs);
    }
  }
};

/**
 * Clear search panel configuration for specified keys
 * @param {string[]} keys - Keys to clear
 */
const clearSearchPanelConfig = (keys: string[]) => {
  if (typeof searchPanelComponentRef.value?.setConfigs === 'function') {
    const configs = keys.map(key => ({ valueKey: key, value: undefined }));
    searchPanelComponentRef.value.setConfigs(configs);
  }
};

/**
 * Set search panel configuration
 * @param {object[]} configs - Configuration objects
 */
const setSearchPanelConfig = (configs: { valueKey: string; value: any }[]) => {
  if (typeof searchPanelComponentRef.value?.setConfigs === 'function') {
    searchPanelComponentRef.value.setConfigs(configs);
  }
};

/**
 * Reset all filter states to default values
 */
const resetAllFilterStates = () => {
  isOverdueFilterActive.value = false;
  targetParentIdSearchFilter.value.value = undefined;
  targetIdSearchFilter.value.value = undefined;
  evaluationWorkloadFilter.value.value = undefined;
  failureCountFilter.value.value = undefined;
  totalCountFilter.value.value = undefined;
};

/**
 * Reset search panel configuration to default state
 */
const resetSearchPanelConfiguration = () => {
  if (typeof searchPanelComponentRef.value?.setConfigs === 'function') {
    const configs = searchPanelOptions.map(item => ({
      valueKey: item.valueKey,
      value: undefined
    }));
    searchPanelComponentRef.value.setConfigs(configs);
  }
};

/**
 * Reset all search states to default values
 */
const resetAllSearchStates = () => {
  quickDateSearchMap.value.clear();
  selectedQuickSearchItems.value.clear();
  isOverdueFilterActive.value = false;

  isSprintSelectionVisible.value = false;
  selectedSprintOption.value = undefined;
  checkedSprintId.value = undefined;

  isTagSelectionVisible.value = false;
  selectedTagOptions.value = [];
  checkedTagIds.value = [];

  currentSearchFilters.value = [];

  targetParentIdSearchFilter.value = { key: 'targetParentId', op: SearchCriteria.OpEnum.Equal, value: undefined };
  targetIdSearchFilter.value = { key: 'targetId', op: SearchCriteria.OpEnum.Equal, value: undefined };

  evaluationWorkloadFilter.value = { key: 'evalWorkload', op: SearchCriteria.OpEnum.Equal, value: undefined };
  failureCountFilter.value = { key: 'failNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
  totalCountFilter.value = { key: 'totalNum', op: SearchCriteria.OpEnum.Equal, value: undefined };
};

// Lifecycle Hooks
onMounted(async () => {
  // Watch for database key changes and initialize
  watch([
    () => databaseParamsKey.value,
    () => databaseCountKey.value,
    () => databaseViewModeKey.value
  ], ([key1, key2, key3]) => {
    if (!key1 || !key2 || !key3) {
      return;
    }
    initializeSearchPanel();
  }, { immediate: true });

  // Watch for route hash changes to handle URL parameters
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#list')) {
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

  // Watch for search criteria changes and update UI accordingly
  watch(
    [
      () => currentSearchFilters.value,
      () => isOverdueFilterActive.value,
      () => checkedSprintId.value,
      () => checkedTagIds.value.length,
      () => targetParentIdSearchFilter.value,
      () => targetIdSearchFilter.value,
      () => evaluationWorkloadFilter.value,
      () => failureCountFilter.value,
      () => totalCountFilter.value,
      () => selectedQuickSearchItems.value
    ], () => {
      const currentFilters = currentSearchFilters.value;

      // Check if any filters are active
      const hasActiveFilters = currentFilters.length ||
        isOverdueFilterActive.value ||
        !!targetParentIdSearchFilter.value.value ||
        !!targetIdSearchFilter.value.value ||
        !!evaluationWorkloadFilter.value.value ||
        !!failureCountFilter.value.value ||
        !!totalCountFilter.value.value;

      if (!hasActiveFilters) {
        // No active filters, show "All" option
        selectedQuickSearchItems.value.clear();
        selectedQuickSearchItems.value.set('none', { key: 'none' });
        emit('change', buildSearchCriteria());
      } else {
        // Active filters, update quick search selections
        updateQuickSearchSelections(currentFilters);
        emit('change', buildSearchCriteria());
      }

      // Save current state to database
      saveSearchStateToDatabase();
    }, { immediate: false, deep: false });
});

/**
 * Get current user ID
 */
const currentUserId = computed(() => {
  return props.userInfo?.id;
});

/**
 * Generate base key for database operations
 */
const databaseBaseKey = computed(() => {
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
 * Generate database key for search parameters
 */
const databaseParamsKey = computed(() => {
  return btoa(databaseBaseKey.value + 'task_analysis');
});

/**
 * Generate database key for count data
 */
const databaseCountKey = computed(() => {
  return btoa(databaseBaseKey.value + 'count');
});

/**
 * Generate database key for view mode
 */
const databaseViewModeKey = computed(() => {
  return btoa(databaseBaseKey.value + 'viewMode');
});

/**
 * Update quick search selections based on current filters
 * @param {SearchCriteria[]} filters - Current search filters
 */
const updateQuickSearchSelections = (filters: SearchCriteria[]) => {
  // Remove "All" option when filters are active
  selectedQuickSearchItems.value.delete('none');

  // Update created by selection
  const createdBy = filters.find(item => item.key === 'createdBy')?.value;
  if (createdBy && createdBy === currentUserId.value) {
    selectedQuickSearchItems.value.set('createdBy', { key: 'createdBy' });
  } else {
    selectedQuickSearchItems.value.delete('createdBy');
  }

  // Update last modified by selection
  const lastModifiedBy = filters.find(item => item.key === 'lastModifiedBy')?.value;
  if (lastModifiedBy && lastModifiedBy === currentUserId.value) {
    selectedQuickSearchItems.value.set('lastModifiedBy', { key: 'lastModifiedBy' });
  } else {
    selectedQuickSearchItems.value.delete('lastModifiedBy');
  }

  // Update assignee and status selections
  const status = filters.find(item => item.key === 'status')?.value;
  const assigneeId = filters.find(item => item.key === 'assigneeId')?.value;

  if (status && status === TaskStatus.PENDING && assigneeId === currentUserId.value) {
    selectedQuickSearchItems.value.set('assigneeId', { key: 'assigneeId' });
    selectedQuickSearchItems.value.delete('confirmerId');
    selectedQuickSearchItems.value.delete('progress');
  } else {
    selectedQuickSearchItems.value.delete('assigneeId');
  }

  if (status && status === TaskStatus.IN_PROGRESS && assigneeId === currentUserId.value) {
    selectedQuickSearchItems.value.set('progress', { key: 'progress' });
    selectedQuickSearchItems.value.delete('confirmerId');
    selectedQuickSearchItems.value.delete('assigneeId');
  } else {
    selectedQuickSearchItems.value.delete('progress');
  }

  // Update confirmer selection
  const confirmerId = filters.find(item => item.key === 'confirmerId')?.value;
  if (status && status === TaskStatus.CONFIRMING && confirmerId === currentUserId.value) {
    selectedQuickSearchItems.value.set('confirmerId', { key: 'confirmerId' });
    selectedQuickSearchItems.value.delete('assigneeId');
    selectedQuickSearchItems.value.delete('progress');
  } else {
    selectedQuickSearchItems.value.delete('confirmerId');
  }

  // Update date range selections
  if (quickDateSearchMap.value.size > 0) {
    selectedQuickSearchItems.value.delete('lastDay');
    selectedQuickSearchItems.value.delete('lastThreeDays');
    selectedQuickSearchItems.value.delete('lastWeek');

    const createdDateStart = filters.find(
      item => item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.GreaterThanEqual)?.value;
    const createdDateEnd = filters.find(
      item => item.key === 'createdDate' && item.op === SearchCriteria.OpEnum.LessThanEqual)?.value;
    const dateString = [createdDateStart, createdDateEnd];

    const entries = quickDateSearchMap.value.entries();
    for (const [key, value] of entries) {
      if (isEqual(value, dateString)) {
        selectedQuickSearchItems.value.set(key, { key });
      }
    }
    quickDateSearchMap.value.clear();
  }
};

/**
 * Save current search state to database
 */
const saveSearchStateToDatabase = () => {
  if (searchParametersDatabase) {
    const databaseData: {
      filters?: SearchCriteria[];
      overdue?: true;
      checkedSprintId?: string;
      selectedSprint?: {
        id: string;
        name: string;
        showTitle: string;
        showName: string;
      };
      checkedTagIds?: string[];
      selectedTags?: {
        id: string;
        name: string;
        showTitle: string;
        showName: string;
      }[];
      evalWorkloadFilter?: SearchCriteria;
      failNumFilter?: SearchCriteria;
      totalNumFilter?: SearchCriteria;
      targetParentIdFilter?: SearchCriteria;
      targetIdFilter?: SearchCriteria;
    } = {};

    if (currentSearchFilters.value.length) {
      databaseData.filters = cloneDeep(currentSearchFilters.value);
    }

    if (isOverdueFilterActive.value) {
      databaseData.overdue = isOverdueFilterActive.value;
    }

    if (checkedSprintId.value) {
      databaseData.checkedSprintId = checkedSprintId.value;
    }

    if (selectedSprintOption.value) {
      databaseData.selectedSprint = cloneDeep(selectedSprintOption.value);
    }

    if (checkedTagIds.value.length) {
      databaseData.checkedTagIds = cloneDeep(checkedTagIds.value);
    }

    if (selectedTagOptions.value.length) {
      databaseData.selectedTags = cloneDeep(selectedTagOptions.value);
    }

    if (evaluationWorkloadFilter.value.value) {
      databaseData.evalWorkloadFilter = cloneDeep(evaluationWorkloadFilter.value);
    }

    if (failureCountFilter.value.value) {
      databaseData.failNumFilter = cloneDeep(failureCountFilter.value);
    }

    if (totalCountFilter.value.value) {
      databaseData.totalNumFilter = cloneDeep(totalCountFilter.value);
    }

    if (targetParentIdSearchFilter.value.value) {
      databaseData.targetParentIdFilter = cloneDeep(targetParentIdSearchFilter.value);
    }

    if (targetIdSearchFilter.value.value) {
      databaseData.targetIdFilter = cloneDeep(targetIdSearchFilter.value);
    }

    if (Object.keys(databaseData).length) {
      searchParametersDatabase.add({
        id: databaseParamsKey.value,
        data: databaseData
      });
    } else {
      searchParametersDatabase.delete(databaseParamsKey.value);
    }
  }
};

/**
 * Quick search menu items configuration
 */
const quickSearchMenuItems: MenuItem[] = [
  {
    key: 'none',
    name: t('quickSearchTags.all')
  },
  {
    key: 'createdBy',
    name: t('quickSearchTags.addByMe')
  },
  {
    key: 'lastModifiedBy',
    name: t('quickSearchTags.modifiedByMe')
  },
  {
    key: 'lastDay',
    name: t('quickSearchTags.past1Day')
  },
  {
    key: 'lastThreeDays',
    name: t('quickSearchTags.past3Day')
  },
  {
    key: 'lastWeek',
    name: t('quickSearchTags.past7Day')
  }
];

/**
 * Search panel options configuration
 */
const searchPanelOptions = [
  {
    type: 'input' as const,
    valueKey: 'name',
    placeholder: t('taskAnalysis.searchNameDesc')
  },
  {
    type: 'select-user' as const,
    valueKey: 'createdBy',
    placeholder: t('taskAnalysis.selectCreator'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'date-range' as const,
    valueKey: 'createdDate',
    placeholder: [t('taskAnalysis.createTimeFrom'), t('taskAnalysis.createTimeTo')],
    showTime: true
  }
];

/**
 * Sort menu items configuration
 */
const sortMenuItems = [
  {
    key: 'name',
    name: t('taskAnalysis.columns.name'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdByName',
    name: t('taskAnalysis.sortByCreator'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdDate',
    name: t('taskAnalysis.sortByCreateTime'),
    orderSort: PageQuery.OrderSort.Asc
  }
];
</script>
<template>
  <div class="text-3 leading-5">
    <div class="flex items-start justify-between mb-1.5">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>{{ t('quickSearch') }}</span>
          <Colon />
        </div>
        <div class="flex  flex-wrap ml-2">
          <div
            v-for="item in quickSearchMenuItems"
            :key="item.key"
            :class="{ 'active-key': selectedQuickSearchItems.has(item.key) }"
            class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
            @click="handleQuickSearchMenuItemClick(item)">
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-between">
      <SearchPanel
        ref="searchPanelComponentRef"
        :options="searchPanelOptions"
        @change="handleSearchPanelChange">
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2">
        <Button
          class="flex-shrink-0 flex items-center"
          type="primary"
          size="small"
          @click="handleCreateClick">
          <div class="flex items-center">
            <Icon icon="icon-jia" class="text-3.5" />
            <span class="ml-1">{{ t('taskAnalysis.addAnalysis') }}</span>
          </div>
        </Button>

        <DropdownSort
          :orderBy="props.orderBy"
          :orderSort="props.orderSort"
          :menuItems="sortMenuItems"
          @click="handleSortingChange">
          <Button
            size="small"
            class="flex items-center cursor-pointer ">
            <Icon icon="icon-biaotoupaixu" class="text-3.5" />
            <span class="ml-1">{{ t('sort') }}</span>
          </Button>
        </DropdownSort>

        <Button
          class="flex-shrink-0 flex items-center"
          size="small"
          @click="handleRefreshClick">
          <IconRefresh class="text-4 flex-shrink-0" />
          {{ t('actions.refresh') }}
        </Button>
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
</style>
