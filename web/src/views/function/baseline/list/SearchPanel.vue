<script lang="ts" setup>
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { LoadingProps } from '@/types/types';

const { t } = useI18n();

const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
});

type OrderByKey = string;

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: {
  orderBy?: string;
  orderSort?: PageQuery.OrderSort;
  filters: SearchCriteria[];
}):void,
  (e: 'refresh'):void
}>();

const userInfo = ref(appContext.getUser());

const searchPanelRef = ref();
const selectedMenuMap = ref<{[key: string]: boolean}>({});

const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const assocFilters = ref<SearchCriteria[]>([]);
const assocKeys = ['ownerId'];
const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];
const establishedKeys = ['established=1', 'established=0'];

const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('functionBaseline.list.searchBaselineNameDescription'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('functionBaseline.list.selectOwner'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.GreaterThanEqual,
    placeholder: t('functionBaseline.list.baselineStartTimeGreaterEqual'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', TIME_FORMAT) },
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date',
    valueType: 'start',
    op: SearchCriteria.OpEnum.LessThanEqual,
    placeholder: t('functionBaseline.list.baselineDeadlineTimeLessEqual'),
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
    name: t('functionBaseline.list.sortByName'),
    key: 'name',
    orderSort: PageQuery.OrderSort.Desc
  },
  {
    name: t('functionBaseline.list.sortByAddPerson'),
    key: 'createdBy',
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    name: t('functionBaseline.list.sortByAddTime'),
    key: 'createdDate',
    orderSort: PageQuery.OrderSort.Asc
  }
];

const menuItems = computed(() => [
  {
    key: '',
    name: t('functionBaseline.list.all')
  },
  {
    key: 'createdBy',
    name: t('functionBaseline.list.myCreated')
  },
  {
    key: 'lastModifiedBy',
    name: t('functionBaseline.list.myModified')
  },
  {
    key: 'established=1',
    name: t('functionBaseline.list.established')
  },
  {
    key: 'established=0',
    name: t('functionBaseline.list.notEstablished')
  },
  {
    key: 'lastDay',
    name: t('functionBaseline.list.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('functionBaseline.list.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('functionBaseline.list.lastWeek')
  }
]);

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
          value: startDate.format(DATE_TIME_FORMAT),
          op: SearchCriteria.OpEnum.GreaterThanEqual,
          key: 'createdDate'
        }
      : '',
    endDate
      ? {
          value: endDate.format(DATE_TIME_FORMAT),
          op: SearchCriteria.OpEnum.LessThanEqual,
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

const searchChange = (data: SearchCriteria[]) => {
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
    } else if (establishedKeys.includes(key)) {
      establishedKeys.forEach(statusKey => delete selectedMenuMap.value[statusKey]);
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
    } else if (establishedKeys.includes(key)) {
      const value = !!(+key.split('=')[1]);
      return {
        key: 'established',
        op: SearchCriteria.OpEnum.Equal,
        value
      };
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      timeFilters = formatDateString(key);
      return undefined;
    } else if (assocKeys.includes(key)) {
      if (key === 'ownerId') {
        assocFiltersInQuick.push({ valueKey: key, value: userId });
      }
      return undefined;
    } else {
      return {
        key,
        op: SearchCriteria.OpEnum.Equal,
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
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex">
      <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
        <span>{{ t('functionBaseline.list.quickSearch') }}</span>
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
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/function#baseline?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('functionBaseline.list.addBaseline') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="orderBy"
          v-model:orderSort="orderSort"
          :menuItems="sortMenuItems"
          @click="toSort">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" class="text-3.5" />
            <span>{{ t('functionBaseline.list.sort') }}</span>
          </div>
        </DropdownSort>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="refresh">
          <template #default>
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-shuaxin" class="text-3.5" />
              <span class="ml-1">{{ t('functionBaseline.list.refresh') }}</span>
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
