<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Colon, Dropdown, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { useRouter } from 'vue-router';
import { Button } from 'ant-design-vue';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// Component props
interface Props {
  /** Loading state */
  loading: boolean;
  /** Number of selected items in batch mode */
  selectedNum?: number;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
});

const router = useRouter();
type OrderByKey = string;
type OrderSortKey = 'ASC' | 'DESC';

// Component emits
const emits = defineEmits<{
  (e: 'change', value: {
    orderBy?: string;
    orderSort?: 'ASC'|'DESC';
    filters: {key: string; op: string; value: string|string[]}[];
  }): void;
  (e: 'refresh'): void;
  (e: 'toBatchDelete'): void;
  (e: 'toExecuteBatchDelete'): void;
  (e: 'toImport'): void;
  (e: 'toExport'): void;
  (e: 'toCancelBatchDelete'): void;
}>();

// Reactive state
const userInfo = ref(appContext.getUser());
const searchPanelRef = ref();
const selectedMenuMap = ref<{[key: string]: boolean}>({});

// Dropdown menu items
const buttonDropdownMenuItems = [
  {
    name: t('dataset.listSearchPanel.buttonDropdown.fileExtractDataset'),
    key: 'file',
    noAuth: true
  },
  {
    name: t('dataset.listSearchPanel.buttonDropdown.jdbcExtractDataset'),
    key: 'jdbc',
    noAuth: true
  }
];

// Search panel options
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('dataset.listSearchPanel.searchOptions.namePlaceholder'),
    allowClear: true,
    maxlength: 100
  },
  // {
  //   valueKey: 'status',
  //   type: 'select-enum',
  //   enumKey: 'FuncReviewStatus',
  //   placeholder: '选择状态',
  //   allowClear: true
  // },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true,
    placeholder: t('dataset.listSearchPanel.searchOptions.createdByPlaceholder'),
    maxlength: 100
  },
  {
    valueKey: 'createdDate',
    type: 'date-range',
    allowClear: true,
    placeholder: t('dataset.listSearchPanel.searchOptions.createdDatePlaceholder'),
    maxlength: 100
  }
];

// Sort menu items
const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: OrderSortKey;
}[] = [
  {
    name: t('dataset.listSearchPanel.sortMenuItems.byName'),
    key: 'name',
    orderSort: 'DESC'
  },
  {
    name: t('dataset.listSearchPanel.sortMenuItems.byCreator'),
    key: 'createdBy',
    orderSort: 'ASC'
  },
  {
    name: t('dataset.listSearchPanel.sortMenuItems.byCreateTime'),
    key: 'createdDate',
    orderSort: 'ASC'
  },
  {
    name: t('dataset.listSearchPanel.sortMenuItems.byLastModifier'),
    key: 'lastModifiedBy',
    orderSort: 'ASC'
  },
  {
    name: t('dataset.listSearchPanel.sortMenuItems.byLastModifyTime'),
    key: 'lastModifiedDate',
    orderSort: 'DESC'
  }
];

// Menu items
const menuItems = computed(() => [
  {
    key: '',
    name: t('dataset.listSearchPanel.menuItems.all')
  },
  {
    key: 'createdBy',
    name: t('dataset.listSearchPanel.menuItems.myCreated')
  },
  {
    key: 'lastModifiedBy',
    name: t('dataset.listSearchPanel.menuItems.myModified')
  },
  {
    key: 'lastDay',
    name: t('dataset.listSearchPanel.menuItems.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('dataset.listSearchPanel.menuItems.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('dataset.listSearchPanel.menuItems.lastWeek')
  }
]);

// Reactive state for search parameters
const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const quickSearchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocKeys = ['createdBy', 'createdDate'];
const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];

/**
 * Format date string for time-based filters
 * Converts relative time periods to absolute date ranges
 * 
 * @param key - Time period key
 * @returns Array of start and end date strings
 */
const formatDateString = (key: string) => {
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

  return [startDate ? startDate.format('YYYY-MM-DD HH:mm:ss') : '', endDate ? endDate.format('YYYY-MM-DD HH:mm:ss') : ''];
};

/**
 * Get search parameters
 * Combines all filter types into a single parameter object
 * 
 * @returns Search parameters object
 */
const getParams = () => {
  return {
    filters: [
      ...quickSearchFilters.value,
      ...searchFilters.value,
      ...assocFilters.value
    ],
    orderBy: orderBy.value,
    orderSort: orderSort.value
  };
};

/**
 * Handle search panel changes
 * Updates filters and emits change event
 * 
 * @param data - Search filter data
 */
const searchChange = (data: {key: string; op: string; value: string|string[]}[]) => {
  searchFilters.value = data.filter(item => !assocKeys.includes(item.key));
  assocFilters.value = data.filter(item => assocKeys.includes(item.key));

  if (!assocFilters.value.length) {
    assocKeys.forEach(i => {
      if (i === 'createdDate') {
        timeKeys.forEach(t => delete selectedMenuMap.value[t]);
      } else {
        delete selectedMenuMap.value[i];
      }
    });
  } else {
    assocKeys.forEach(key => {
      if (key === 'createdBy' || key === 'lastModifiedBy') {
        const filterItem = assocFilters.value.find(i => i.key === key);
        if (!filterItem || filterItem.value !== userInfo.value?.id) {
          delete selectedMenuMap.value[key];
        }
      }
      if (key === 'createdDate') {
        const filterItem = assocFilters.value.filter(i => i.key === key);
        const timeKey = timeKeys.find(t => selectedMenuMap.value[t]);
        if (timeKey) {
          const timeValue = formatDateString(timeKey);
          if (timeValue[0] !== filterItem[0].value || timeValue[1] !== filterItem[1].value) {
            delete selectedMenuMap.value[timeKey];
          }
        }
      }
    });
  }

  emits('change', getParams());
};

/**
 * Handle sort changes
 * Updates sorting parameters and emits change event
 * 
 * @param sortData - Sorting data
 */
const toSort = (sortData) => {
  orderBy.value = sortData.orderBy;
  orderSort.value = sortData.orderSort;
  emits('change', getParams());
};

/**
 * Handle menu item clicks
 * Updates quick search filters based on selected menu items
 * 
 * @param data - Menu item data
 */
const menuItemClick = (data) => {
  const key = data.key;
  // const statusTypeKeys = planStatusTypeOpt.value.map(i => i.key);
  let searchChangeFlag = false;
  if (selectedMenuMap.value[key]) {
    delete selectedMenuMap.value[key];
    if (timeKeys.includes(key) && assocKeys.includes('createdDate')) {
      searchPanelRef.value.setConfigs([
        { valueKey: 'createdDate', value: undefined }
      ]);
      searchChangeFlag = true;
    } else if (assocKeys.includes(key)) {
      searchPanelRef.value.setConfigs([
        { valueKey: key, value: undefined }
      ]);
      searchChangeFlag = true;
    }
  } else {
    if (key === '') {
      selectedMenuMap.value = { '': true };
      quickSearchFilters.value = [];
      // 清空搜索面板
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
        searchChangeFlag = true;
      }
    } else {
      delete selectedMenuMap.value[''];
    }
    if (timeKeys.includes(key)) {
      timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
      selectedMenuMap.value[key] = true;
    } else {
      selectedMenuMap.value[key] = true;
    }
  }
  const userId = userInfo.value?.id;
  const timeFilters: {key: string; op: string; value: string}[] = [];
  const assocFiltersInQuick = [];
  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      assocFiltersInQuick.push({ valueKey: 'createdDate', value: formatDateString(key) });
      return undefined;
    } else if (assocKeys.includes(key)) {
      if (key === 'createdBy') {
        assocFiltersInQuick.push({ valueKey: key, value: userId });
      }
      return undefined;
    } else {
      return {
        key,
        op: 'EQUAL',
        value: userId
      };
    }
  }).filter(Boolean);
  quickSearchFilters.value.push(...timeFilters);
  if (assocFiltersInQuick.length) {
    searchPanelRef.value.setConfigs([
      ...assocFiltersInQuick
    ]);
    searchChangeFlag = true;
  }
  if (!searchChangeFlag) {
    emits('change', getParams());
  }
};

/**
 * Refresh the dataset list
 * Emits refresh event to parent component
 */
const refresh = () => {
  emits('refresh');
};

/**
 * Handle button dropdown clicks
 * Navigates to dataset creation pages based on selection
 * 
 * @param param0 - Dropdown menu item
 */
const buttonDropdownClick = ({ key }: { key: 'file' | 'jdbc' }) => {
  if (key === 'file') {
    router.push('/data#dataSet?source=FILE');
    return;
  }

  if (key === 'jdbc') {
    router.push('/data#dataSet?source=JDBC');
  }
};

/**
 * Navigate to create static dataset page
 */
const toCreateStaticDataSet = () => {
  router.push('/data#dataSet?source=STATIC');
};

/**
 * Initialize batch delete mode
 * Emits toBatchDelete event to parent component
 */
const toBatchDelete = () => {
  emits('toBatchDelete');
};

/**
 * Execute batch delete operation
 * Emits toExecuteBatchDelete event to parent component
 */
const toExecuteBatchDelete = () => {
  emits('toExecuteBatchDelete');
};

/**
 * Open import modal
 * Emits toImport event to parent component
 */
const toImport = () => {
  emits('toImport');
};

/**
 * Open export modal
 * Emits toExport event to parent component
 */
const toExport = () => {
  emits('toExport');
};

/**
 * Cancel batch delete mode
 * Emits toCancelBatchDelete event to parent component
 */
const toCancelBatchDelete = () => {
  emits('toCancelBatchDelete');
};

onMounted(() => {
  // loadStatusEnum();
});
</script>

// ... existing template and style code ...