import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { DataMenuKey } from '@/views/data/menu';
import { createAuditOptions, createTimeOptions, type QuickSearchConfig } from '@/components/quickSearch';
import { useDropdownMenus } from '@/views/data/dataset/list/composables';

export type DatasetSearchEmits = {
  (e: 'change', value: {
    orderBy?: string;
    orderSort?: PageQuery.OrderSort;
    filters: SearchCriteria[];
  }): void;
  (e: 'refresh'): void;
  (e: 'toBatchDelete'): void;
  (e: 'toExecuteBatchDelete'): void;
  (e: 'toImport'): void;
  (e: 'toExport'): void;
  (e: 'toCancelBatchDelete'): void;
};

export function useSearchPanel (emits: DatasetSearchEmits) {
  const { t } = useI18n();
  const router = useRouter();

  const { buttonDropdownMenuItems } = useDropdownMenus();

  // refs
  const userInfo = ref(appContext.getUser());
  const searchPanelRef = ref();

  // Search panel options
  const searchPanelOptions = [
    {
      valueKey: 'name',
      type: 'input',
      placeholder: t('common.placeholders.searchKeyword'),
      allowClear: true,
      maxlength: 100
    },
    {
      valueKey: 'createdBy',
      type: 'select-user',
      allowClear: true,
      placeholder: t('common.placeholders.selectCreator')
    },
    {
      valueKey: 'createdDate',
      type: 'date-range',
      allowClear: true,
      placeholder: [
        t('common.placeholders.selectCreatedDateRange.0'),
        t('common.placeholders.selectCreatedDateRange.1')
      ]
    }
  ];

  // Quick search config
  const quickSearchConfig = computed<QuickSearchConfig>(() => ({
    title: t('quickSearch.title'),
    auditOptions: createAuditOptions([
      {
        key: 'createdByMe',
        name: t('quickSearch.createdByMe'),
        fieldKey: 'createdBy'
      },
      {
        key: 'modifiedByMe',
        name: t('quickSearch.modifiedByMe'),
        fieldKey: 'lastModifiedBy'
      }
    ], String(userInfo.value?.id || '')),
    timeOptions: createTimeOptions([
      { key: 'last1Day', name: t('quickSearch.last1Day'), timeRange: 'last1Day' },
      { key: 'last3Days', name: t('quickSearch.last3Days'), timeRange: 'last3Days' },
      { key: 'last7Days', name: t('quickSearch.last7Days'), timeRange: 'last7Days' }
    ], 'createdDate'),
    externalClearFunction: () => {
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
      }
    }
  }));

  // search states
  const orderBy = ref<string>();
  const orderSort = ref<PageQuery.OrderSort>();
  const searchFilters = ref<SearchCriteria[]>([]);
  const quickSearchFilters = ref<SearchCriteria[]>([]);
  const assocFilters = ref<SearchCriteria[]>([]);
  const assocKeys = ['createdBy', 'lastModifiedBy', 'createdDate'];

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

  const searchChange = (data: SearchCriteria[]) => {
    searchFilters.value = data.filter(item => !assocKeys.includes(item.key as string));
    assocFilters.value = data.filter(item => assocKeys.includes(item.key as string));
    emits('change', getParams());
  };

  const handleQuickSearchChange = (_selectedKeys: string[], searchCriteria: SearchCriteria[]): void => {
    quickSearchFilters.value = searchCriteria;
    emits('change', getParams());
  };

  const refresh = () => {
    emits('refresh');
  };

  const buttonDropdownClick = ({ key }: { key: 'file' | 'jdbc' }) => {
    if (key === 'file') {
      router.push(`/data#${DataMenuKey.DATASET}?source=FILE`);
      return;
    }
    if (key === 'jdbc') {
      router.push(`/data#${DataMenuKey.DATASET}?source=JDBC`);
    }
  };

  const toCreateStaticDataSet = () => {
    router.push(`/data#${DataMenuKey.DATASET}?source=VALUE`);
  };

  const toBatchDelete = () => {
    emits('toBatchDelete');
  };

  const toExecuteBatchDelete = () => {
    emits('toExecuteBatchDelete');
  };

  const handleImport = () => {
    emits('toImport');
  };

  const handleExport = () => {
    emits('toExport');
  };

  const toCancelBatchDelete = () => {
    emits('toCancelBatchDelete');
  };

  return {
    // refs
    searchPanelRef,

    // configs
    searchPanelOptions,
    quickSearchConfig,

    // states
    orderBy,
    orderSort,

    // methods
    buttonDropdownMenuItems,
    searchChange,
    handleQuickSearchChange,
    refresh,
    buttonDropdownClick,
    toCreateStaticDataSet,
    toBatchDelete,
    toExecuteBatchDelete,
    handleImport,
    handleExport,
    toCancelBatchDelete
  };
}
