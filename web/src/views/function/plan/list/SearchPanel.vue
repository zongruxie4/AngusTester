<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { enumUtils, appContext } from '@xcan-angus/infra';
import { FuncPlanStatus } from '@/enums/enums';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
});

type OrderByKey = string;
type OrderSortKey = 'ASC' | 'DESC';

const emits = defineEmits<{(e: 'change', value: {
  orderBy?: string;
  orderSort?: 'ASC'|'DESC';
  filters: {key: string; op: string; value: string|string[]}[];
}):void,
 (e: 'refresh'):void}>();
const userInfo = ref(appContext.getUser());

const searchPanelRef = ref();
const selectedMenuMap = ref<{[key: string]: boolean}>({});

const planStatusTypeOpt = ref<{name: string; key: string}[]>([]);
const loadStatusEnum = () => {
  const data = enumUtils.enumToMessages(FuncPlanStatus);
  planStatusTypeOpt.value = data.map(i => ({ name: i.message, key: i.value }));
};

const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('functionPlan.list.searchPlanName'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'review',
    type: 'select',
    allowClear: true,
    options: [{ label: t('status.yes'), value: true }, { label: t('status.no'), value: false }],
    placeholder: t('functionPlan.list.isReview')
  },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('functionPlan.list.selectOwner'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date',
    valueType: 'start',
    op: 'GREATER_THAN_EQUAL',
    placeholder: t('functionPlan.list.planStartTime'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', 'HH:mm:ss') },
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date',
    valueType: 'start',
    op: 'LESS_THAN_EQUAL',
    placeholder: t('functionPlan.list.planDeadlineTime'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', 'HH:mm:ss') },
    allowClear: true
  }
];

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: OrderSortKey;
}[] = [
  {
    name: t('functionPlan.list.sortByName'),
    key: 'name',
    orderSort: 'DESC'
  },
  {
    name: t('functionPlan.list.sortByOwner'),
    key: 'ownerId',
    orderSort: 'ASC'
  },
  {
    name: t('functionPlan.list.sortByCreator'),
    key: 'createdBy',
    orderSort: 'ASC'
  },
  {
    name: t('functionPlan.list.sortByAddTime'),
    key: 'createdDate',
    orderSort: 'ASC'
  }
];

const menuItems = computed(() => [
  {
    key: '',
    name: t('quickSearchTags.all')
  },
  {
    key: 'ownerId',
    name: t('quickSearchTags.myResponsible')
  },
  {
    key: 'createdBy',
    name: t('quickSearchTags.createdByMe')
  },
  {
    key: 'lastModifiedBy',
    name: t('quickSearchTags.modifiedByMe')
  },
  ...planStatusTypeOpt.value,
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
]);

const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const quickSearchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocKeys = ['ownerId'];
const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];

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

  return [
    startDate
      ? {
          value: startDate.format('YYYY-MM-DD HH:mm:ss'),
          op: 'GREATER_THAN_EQUAL',
          key: 'createdDate'
        }
      : '',
    endDate
      ? {
          value: endDate.format('YYYY-MM-DD HH:mm:ss'),
          op: 'LESS_THAN_EQUAL',
          key: 'createdDate'
        }
      : ''].filter(Boolean);
};

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

const searchChange = (data: {key: string; op: string; value: string|string[]}[]) => {
  searchFilters.value = data.filter(item => !assocKeys.includes(item.key));
  assocFilters.value = data.filter(item => assocKeys.includes(item.key));

  if (!assocFilters.value.length) {
    assocKeys.forEach(i => delete selectedMenuMap.value[i]);
  } else {
    assocKeys.forEach(key => {
      if (key === 'ownerId') {
        const filterItem = assocFilters.value.find(i => i.key === key);
        if (!filterItem || filterItem.value !== userInfo.value?.id) {
          delete selectedMenuMap.value[key];
        }
      }
    });
  }

  emits('change', getParams());
};
const toSort = (sortData) => {
  orderBy.value = sortData.orderBy;
  orderSort.value = sortData.orderSort;
  emits('change', getParams());
};

const menuItemClick = (data) => {
  const key = data.key;
  const statusTypeKeys = planStatusTypeOpt.value.map(i => i.key);
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
    } else if (statusTypeKeys.includes(key)) {
      statusTypeKeys.forEach(statusKey => delete selectedMenuMap.value[statusKey]);
      selectedMenuMap.value[key] = true;
    } else {
      selectedMenuMap.value[key] = true;
    }
  }
  const userId = userInfo.value?.id;
  let timeFilters: {key: string; op: string; value: string}[] = [];
  const assocFiltersInQuick = [];
  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (statusTypeKeys.includes(key)) {
      return {
        key: 'status',
        op: 'EQUAL',
        value: key
      };
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      timeFilters = formatDateString(key);
      return undefined;
    } else if (assocKeys.includes(key)) {
      if (key === 'ownerId') {
        assocFiltersInQuick.push({ valueKey: i, value: userId });
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

const refresh = () => {
  emits('refresh');
};

onMounted(() => {
  loadStatusEnum();
});
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex">
      <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
        <span>{{ t('quickSearchTags.title') }}</span>
        <Colon />
      </div>
      <div class="flex  flex-wrap ml-2">
        <div
          v-for="item in menuItems"
          :key="item.key"
          :class="{ 'active-key': selectedMenuMap[item.key] }"
          class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
          @click="menuItemClick(item)">
          {{ item.name }}
        </div>
      </div>
    </div>
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
          class="p-0">
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/function#plans?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('functionPlan.list.addPlan') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="orderBy"
          v-model:orderSort="orderSort"
          :menuItems="sortMenuItems"
          @click="toSort">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" class="text-3.5" />
            <span>{{ t('sort') }}</span>
          </div>
        </DropdownSort>

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
.active-key {
  background-color: #4ea0fd;
  color: #fff;
}
</style>
