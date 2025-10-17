<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { SoftwareVersionStatus } from '@/enums/enums';
import { LoadingProps } from '@/types/types';
import { QuickSearchOptions, createEnumOptions, createTimeOptions, type QuickSearchConfig } from '@/components/quickSearch';

// Component props with default values
const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
});

const { t } = useI18n();

// Component emits

const emits = defineEmits<{
  (e: 'change', value: PageQuery): void;
  (e: 'refresh'): void;
  (e: 'toMerge'): void;
  (e: 'add'): void;
}>();

// Component references
const searchPanelRef = ref();

/**
 * Search panel configuration options
 * Defines available search fields and their properties
 */
const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input' as const,
    placeholder: t('common.placeholders.searchKeyword'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'createdBy',
    type: 'select-user' as const,
    allowClear: true,
    placeholder: t('common.placeholders.selectCreator')
  },
  {
    type: 'date-range' as const,
    valueKey: 'createdDate',
    showTime: true
  }
];

/**
 * Quick search configuration for version search panel
 * Provides predefined filter options for common searches
 */
const quickSearchConfig = computed<QuickSearchConfig>(() => ({
  title: t('quickSearch.title'),
  // Enum status options for version status
  enumOptions: createEnumOptions([
    {
      key: SoftwareVersionStatus.NOT_RELEASED,
      name: t('version.searchPanelOptions.notReleased'),
      fieldKey: 'status',
      fieldValue: SoftwareVersionStatus.NOT_RELEASED
    },
    {
      key: SoftwareVersionStatus.RELEASED,
      name: t('version.searchPanelOptions.released'),
      fieldKey: 'status',
      fieldValue: SoftwareVersionStatus.RELEASED
    },
    {
      key: SoftwareVersionStatus.ARCHIVED,
      name: t('version.searchPanelOptions.archived'),
      fieldKey: 'status',
      fieldValue: SoftwareVersionStatus.ARCHIVED
    }
  ]),
  // Time options
  timeOptions: createTimeOptions([
    { key: 'last1Day', name: t('quickSearch.last1Day'), timeRange: 'last1Day' },
    { key: 'last3Days', name: t('quickSearch.last3Days'), timeRange: 'last3Days' },
    { key: 'last7Days', name: t('quickSearch.last7Days'), timeRange: 'last7Days' }
  ], 'createdDate'),
  // External clear function
  externalClearFunction: () => {
    if (typeof searchPanelRef.value?.clear === 'function') {
      searchPanelRef.value.clear();
    }
  }
}));

// Search state
const orderBy = ref<string>();
const orderSort = ref<PageQuery.OrderSort>();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const assocFilters = ref<SearchCriteria[]>([]);

// Configuration constants
const assocKeys: string[] = [];

/**
 * Get current search parameters
 * Combines all filter types into single parameter object
 * @returns Combined search parameters
 */
const getParams = (): PageQuery => {
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
 * Handle quick search changes
 * Processes quick search filters and updates state
 * @param selectedKeys - Array of selected option keys
 * @param searchCriteria - Array of search criteria from quick search
 */
const handleQuickSearchChange = (_selectedKeys: string[], searchCriteria: SearchCriteria[]): void => {
  quickSearchFilters.value = searchCriteria;
  emits('change', getParams());
};

/**
 * Handle search panel changes
 * Processes search filters and updates state
 * @param data - Array of search filter objects
 */
const searchChange = (data: SearchCriteria[]): void => {
  searchFilters.value = data.filter(item => !assocKeys.includes(item.key as string));
  assocFilters.value = data.filter(item => assocKeys.includes(item.key as string));
  emits('change', getParams());
};

/**
 * Handle refresh action
 * Emits refresh event to parent component
 */
const refresh = (): void => {
  emits('refresh');
};

/**
 * Handle add action
 * Emits add event to parent component
 */
const add = (): void => {
  emits('add');
};

/**
 * Handle merge action
 * Emits merge event to parent component
 */
const toMerge = (): void => {
  emits('toMerge');
};

onMounted(() => {
  // Component initialization logic if needed
});
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />
    <div class="flex items-start justify-between ">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="searchChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          @click="add">
          <Icon icon="icon-jia" class="text-3.5 mr-1" />
          <span>{{ t('version.actions.addVersion') }}</span>
        </Button>

        <Button
          size="small"
          @click="toMerge">
          <Icon icon="icon-hebingbanben1" class="text-3.5 mr-1" />
          <span>{{ t('version.actions.mergeVersion') }}</span>
        </Button>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="refresh">
          <template #default>
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-shuaxin" class="text-3.5" />
              <span class="ml-1">{{ t('actions.refresh') }}</span>
            </div>
          </template>
        </IconRefresh>
      </div>
    </div>
  </div>
</template>
<style scoped>
/* Styles are now handled by QuickSearchOptions component */
</style>
