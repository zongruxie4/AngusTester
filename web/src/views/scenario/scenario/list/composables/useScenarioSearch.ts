import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { ScriptType, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { SortKey } from './useScenarioData';

/**
 * Composable for managing scenario search and filtering
 */
export function useScenarioSearch () {
  const { t } = useI18n();

  const groupedKey = ref<'createdBy' | 'plugin' | 'scriptType' | 'none'>('none');
  const orderBy = ref<SortKey>('createdDate');
  const orderSort = ref<PageQuery.OrderSort>(PageQuery.OrderSort.Desc);
  const filters = ref<SearchCriteria[]>();

  /**
   * Search options configuration
   */
  const searchOptions = computed(() => [
    {
      type: 'input' as const,
      placeholder: t('scenario.list.searchPlaceholder'),
      valueKey: 'name',
      allowClear: true,
      maxlength: 100,
      trim: true
    },
    {
      type: 'input' as const,
      placeholder: t('scenario.list.pluginTypePlaceholder'),
      valueKey: 'plugin',
      allowClear: true,
      trim: true,
      op: 'EQUAL' as const,
      maxlength: 100,
      trimAll: true
    },
    {
      type: 'select-enum' as const,
      placeholder: t('scenario.list.scriptTypePlaceholder'),
      valueKey: 'scriptType',
      allowClear: true,
      enumKey: ScriptType,
      excludes: ({ value }: { value: ScriptType }) => [ScriptType.MOCK_DATA, ScriptType.MOCK_APIS].includes(value)
    },
    {
      type: 'date-range' as const,
      showTime: true,
      valueKey: 'createdDate',
      valueType: 'multiple',
      allowClear: true
    }
  ]);

  /**
   * Dropdown menu items for creating scenarios
   */
  const buttonDropdownMenuItems = computed(() => [
    {
      name: t('scenario.list.pluginTypes.jdbc'),
      key: 'Jdbc',
      noAuth: true
    },
    {
      name: t('scenario.list.pluginTypes.webSocket'),
      key: 'WebSocket',
      noAuth: true
    },
    {
      name: t('scenario.list.pluginTypes.ftp'),
      key: 'Ftp',
      noAuth: true
    },
    {
      name: t('scenario.list.pluginTypes.ldap'),
      key: 'Ldap',
      noAuth: true
    },
    {
      name: t('scenario.list.pluginTypes.mail'),
      key: 'Mail',
      noAuth: true
    },
    {
      name: t('scenario.list.pluginTypes.smtp'),
      key: 'Smtp',
      noAuth: true
    },
    {
      name: t('scenario.list.pluginTypes.tcp'),
      key: 'Tcp',
      noAuth: true
    }
  ]);

  /**
   * Sort menu items
   */
  const sortMenuItems = computed(() => [
    {
      name: t('scenario.list.sortOptions.byAddTime'),
      key: 'createdDate' as SortKey,
      orderSort: PageQuery.OrderSort.Desc
    }, {
      name: t('scenario.list.sortOptions.byName'),
      key: 'name' as SortKey,
      orderSort: PageQuery.OrderSort.Asc
    }, {
      name: t('scenario.list.sortOptions.byCreator'),
      key: 'createdByName' as SortKey,
      orderSort: PageQuery.OrderSort.Asc
    }
  ]);

  /**
   * Grouping menu items
   */
  const groupingMenuItems = computed(() => [
    {
      key: 'none',
      name: t('scenario.list.groupOptions.noGroup')
    },
    {
      key: 'createdBy',
      name: t('scenario.list.groupOptions.byCreator')
    },
    {
      key: 'plugin',
      name: t('scenario.list.groupOptions.byPlugin')
    },
    {
      key: 'scriptType',
      name: t('scenario.list.groupOptions.byScriptType')
    }
  ]);

  /**
   * Handle search changes
   */
  const handleSearchChange = (value: SearchCriteria[]) => {
    filters.value = value;
  };

  /**
   * Handle sort changes
   */
  const handleSortChange = (value: { orderBy: SortKey; orderSort: PageQuery.OrderSort }) => {
    orderBy.value = value.orderBy;
    orderSort.value = value.orderSort;
  };

  /**
   * Handle grouping changes
   */
  const handleGroupingChange = (value: 'createdBy' | 'plugin' | 'scriptType' | 'none') => {
    groupedKey.value = value;
  };

  return {
    // State
    groupedKey,
    orderBy,
    orderSort,
    filters,

    // Configurations
    searchOptions,
    buttonDropdownMenuItems,
    sortMenuItems,
    groupingMenuItems,

    // Methods
    handleSearchChange,
    handleSortChange,
    handleGroupingChange
  };
}
