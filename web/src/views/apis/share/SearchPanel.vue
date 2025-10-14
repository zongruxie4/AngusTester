<script lang="ts" setup>
import { computed, ref } from 'vue';
import { Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { LoadingProps } from '@/types/types';
import {
  QuickSearchOptions, createAuditOptions, createTimeOptions, type QuickSearchConfig
} from '@/components/quickSearch';

const { t } = useI18n();

const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
});

const emits = defineEmits<{(e: 'change', value: {
  orderBy?: string;
  orderSort?: PageQuery.OrderSort;
  filters: SearchCriteria[];
}):void,
 (e: 'refresh'):void;
 (e: 'add'):void}>();
const userInfo = ref(appContext.getUser());

const searchPanelRef = ref();
const quickSearchOptionsRef = ref();

const searchPanelOptions: any[] = [
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
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: [
      t('common.placeholders.selectCreatedDateRange.0'),
      t('common.placeholders.selectCreatedDateRange.1')
    ],
    showTime: true
  }
];

const quickSearchConfig = computed<QuickSearchConfig>(() => ({
  title: t('quickSearch.title'),
  auditOptions: createAuditOptions([
    {
      key: 'createdBy',
      name: t('quickSearch.createdByMe'),
      fieldKey: 'createdBy'
    },
    {
      key: 'myModified',
      name: t('quickSearch.modifiedByMe'),
      fieldKey: 'lastModifiedBy'
    }
  ], String(userInfo.value?.id ?? '')),
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

const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const assocFilters = ref<SearchCriteria[]>([]);
const assocKeys = ['createdDate', 'createdBy'];

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

const handleSearchChange = (data: SearchCriteria[], _headers?: { [key: string]: string }, key?: string) => {
  if (key === 'createdBy') {
    quickSearchOptionsRef.value.clearSelectedMap(['createdBy']);
    quickSearchFilters.value = quickSearchFilters.value.filter(f => f.key !== 'createdBy');
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', type: 'select-user', value: undefined }]);
    }
  }
  if (key === 'createdDate') {
    quickSearchOptionsRef.value.clearSelectedMap(['createdDate', 'last1Day', 'last3Days', 'last7Days']);
    quickSearchFilters.value = quickSearchFilters.value.filter(f => f.key !== 'createdDate');
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([{ valueKey: 'createdDate', type: 'date-range', value: undefined }]);
    }
  }

  searchFilters.value = data.filter(item => !assocKeys.includes(String(item.key)));
  assocFilters.value = data.filter(item => assocKeys.includes(String(item.key)));
  emits('change', getParams());
};

const handleQuickSearchChange = (selectedKeys: string[], searchCriteria: SearchCriteria[], key?: string) => {
  let isAssociated = false;
  if (key === 'createdBy') {
    isAssociated = true;
    if (selectedKeys.includes(key)) {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', type: 'select-user', value: String(userInfo.value?.id ?? '') }]);
      }
    } else {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', type: 'select-user', value: '' }]);
      }
    }
  }

  if (key && key.startsWith('last') && (key.endsWith('Day') || key.endsWith('Days'))) {
    isAssociated = true;
    if (selectedKeys.includes(key)) {
      const dateCriteria = searchCriteria.filter(f => f.key === 'createdDate');
      if (dateCriteria.length > 1) {
        const value = [dateCriteria[0].value, dateCriteria[1].value];
        if (typeof searchPanelRef.value?.setConfigs === 'function') {
          searchPanelRef.value.setConfigs([{ valueKey: 'createdDate', value, type: 'date-range' }]);
        }
      }
    } else {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'createdDate', value: undefined, type: 'date-range' }]);
      }
    }
  }

  searchCriteria = searchCriteria.filter(f => !assocKeys.includes(f.key as string));
  quickSearchFilters.value = searchCriteria;
  if (isAssociated) {
    return;
  }
  emits('change', getParams());
};

const refresh = () => {
  emits('refresh');
};

const add = () => {
  emits('add');
};
</script>

<template>
  <div class="mt-2.5 mb-3.5">
    <QuickSearchOptions
      ref="quickSearchOptionsRef"
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

    <div class="flex items-start justify-between ">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="handleSearchChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          @click="add">
          <Icon icon="icon-jia" class="text-3.5 mr-1" />
          <span>{{ t('apiShare.actions.addShare') }}</span>
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
</style>
