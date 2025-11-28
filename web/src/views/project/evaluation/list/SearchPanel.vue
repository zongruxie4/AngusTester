<script lang="ts" setup>
import { computed, inject, onMounted, ref } from 'vue';
import { DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { TIME_FORMAT } from '@/utils/constant';

import {
  QuickSearchOptions, createAuditOptions, createTimeOptions, type QuickSearchConfig
} from 'src/components/form/quickSearch';

const { t } = useI18n();

const props = withDefaults(defineProps<{
  loading: boolean;
  userId: string;
}>(), {
  loading: false,
  userId: ''
});

type OrderByKey = string;

const emits = defineEmits<{
  (e: 'change', value: { orderBy?: string; orderSort?: PageQuery.OrderSort; filters: SearchCriteria[]; }): void,
  (e: 'refresh'): void
}>();

const userInfo = ref(appContext.getUser());
const searchPanelRef = ref();
const quickSearchOptionsRef = ref();

const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const assocFilters = ref<SearchCriteria[]>([]);
const assocKeys = ['createdBy'];

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
  }
}));

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
    placeholder: t('common.placeholders.selectCreator'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.GreaterThanEqual,
    placeholder: t('common.placeholders.selectStartDate'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', TIME_FORMAT) },
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.LessThanEqual,
    placeholder: t('common.placeholders.selectDeadline'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', TIME_FORMAT) },
    allowClear: true
  }
];

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: PageQuery.OrderSort;
}[] = [
  {
    name: t('common.name'),
    key: 'name',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('common.creator'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('common.createdDate'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Asc
  }
];

const getSearchParams = () => {
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
    quickSearchOptionsRef.value.clearSelectedMap(['createdByMe']);
    quickSearchFilters.value = quickSearchFilters.value.filter(f => f.key !== 'createdBy');
  }
  searchFilters.value = [...(data || []).filter(f => !assocKeys.includes(f.key as string))];
  assocFilters.value = data.filter(item => assocKeys.includes(item.key as string));

  emits('change', getSearchParams());
};

const handleQuickSearchChange = (selectedKeys: string[], searchCriteria: SearchCriteria[], key?: string): void => {
  if (key === 'createdByMe') {
    if (selectedKeys.includes(key)) {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{
          valueKey: 'createdBy',
          type: 'select-user',
          value: props.userId
        }]);
      }
    } else {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{
          valueKey: 'createdBy',
          type: 'select-user',
          value: undefined
        }]);
      }
    }
  }
  if (key === 'all') {
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.clear();
    }
  }
  searchCriteria = searchCriteria.filter(f => !assocKeys.includes(f.key as string));
  quickSearchFilters.value = searchCriteria;
  if (key === 'createdByMe' || key === 'all') {
    return
  }
  emits('change', getSearchParams());
};

const handleSortChange = (sortData) => {
  orderBy.value = sortData.orderBy;
  orderSort.value = sortData.orderSort;
  emits('change', getSearchParams());
};

const handleRefresh = () => {
  emits('refresh');
};

const addTabPane = inject<(data: any) => void>('addTabPane', () => ({}));

onMounted(() => {
  // Component mounted
});
</script>

<template>
  <div class="mt-2.5 mb-3.5">
    <QuickSearchOptions
      ref="quickSearchOptionsRef"
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

    <div class="flex items-start justify-between">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions as any"
        class="flex-1 mr-3.5"
        @change="handleSearchChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          class="p-0">
          <a
            class="flex items-center space-x-1 leading-6.5 px-1.75"
            @click="addTabPane({
              _id: 'new-evaluation',
              name: t('evaluation.actions.addEvaluation'),
              value: 'evaluationEdit',
              noCache: true,
              data: { _id: 'new-evaluation' }
            })">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('evaluation.actions.addEvaluation') }}</span>
          </a>
        </Button>

        <DropdownSort
          v-model:orderBy="orderBy"
          v-model:orderSort="orderSort"
          :menuItems="sortMenuItems"
          @click="handleSortChange">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" class="text-3.5" />
            <span>{{ t('actions.sort') }}</span>
          </div>
        </DropdownSort>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="handleRefresh">
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

