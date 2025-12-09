import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { ScriptType, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { SortKey } from './useScenarioData';
import { TestPlatform } from '@/enums/enums';

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
      placeholder: t('common.placeholders.searchKeyword'),
      valueKey: 'name',
      allowClear: true,
      maxlength: 100,
      trim: true
    },
    {
      type: 'input' as const,
      placeholder: t('common.placeholders.searchPluginType'),
      valueKey: 'plugin',
      allowClear: true,
      trim: true,
      op: 'EQUAL' as const,
      maxlength: 100,
      trimAll: true
    },
    {
      type: 'select-enum' as const,
      placeholder: t('common.placeholders.selectScriptType'),
      valueKey: 'scriptType',
      allowClear: true,
      enumKey: ScriptType,
      excludes: ({ value }: { value: ScriptType }) => [ScriptType.MOCK_DATA, ScriptType.MOCK_APIS].includes(value)
    },
    {
      type: 'select-enum' as const,
      placeholder: '测试对象平台',
      valueKey: 'testPlatform',
      allowClear: true,
      enumKey: TestPlatform,
    },
    {
      type: 'date-range' as const,
      showTime: true,
      valueKey: 'createdDate',
      valueType: 'multiple',
      allowClear: true,
      placeholder: [
        t('common.placeholders.selectCreatedDateRange.0'),
        t('common.placeholders.selectCreatedDateRange.1')
      ]
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


  const deviceDropdownMenuItems = computed(() => [
    {
      name: '新增移动端测试场景',
      key: 'Mobile',
      noAuth: true
    },
    {
      name: '新增WEB端测试场景',
      key: 'Web',
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
      name: t('actions.noGroup')
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
    deviceDropdownMenuItems,
    sortMenuItems,
    groupingMenuItems,

    // Methods
    handleSearchChange,
    handleSortChange,
    handleGroupingChange
  };
}
