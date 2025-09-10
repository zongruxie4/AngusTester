<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';
import { appContext } from '@xcan-angus/infra';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { TaskMeetingType } from '@/enums/enums';

interface Props {
  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
});

type OrderByKey = string;
type OrderSortKey = 'ASC' | 'DESC';

const { t } = useI18n();
const emits = defineEmits<{(e: 'change', value: {
  orderBy?: string;
  orderSort?: 'ASC'|'DESC';
  filters: {key: string; op: string; value: string|string[]}[];
}):void,
 (e: 'refresh'):void}>();
const userInfo = ref(appContext.getUser());

const searchPanelRef = ref();
const selectedMenuMap = ref<{[key: string]: boolean}>({});

const searchPanelOptions = [
  {
    valueKey: 'subject',
    type: 'input',
    placeholder: t('taskMeeting.placeholder.searchSubject'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'type',
    type: 'select-enum',
    enumKey: TaskMeetingType,
    allowClear: true,
    placeholder: t('taskMeeting.placeholder.selectType')
  },
  {
    valueKey: 'moderatorId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('taskMeeting.placeholder.selectModeratorSearch')
  },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true,
    placeholder: t('taskMeeting.placeholder.selectCreator')
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: [t('taskMeeting.placeholder.createTimeFrom'), t('taskMeeting.placeholder.createTimeTo')],
    showTime: true
  }
];

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: OrderSortKey;
}[] = [
  {
    name: t('taskMeeting.sort.byCreateTime'),
    key: 'createdDate',
    orderSort: 'DESC'
  },
  {
    name: t('taskMeeting.sort.byCreator'),
    key: 'createdBy',
    orderSort: 'ASC'
  },
  {
    name: t('taskMeeting.sort.bySubject'),
    key: 'subject',
    orderSort: 'ASC'
  }
];

const menuItems = computed(() => [
  {
    key: '',
    name: t('taskMeeting.quickSearch.all')
  },
  {
    key: 'moderatorId',
    name: t('taskMeeting.quickSearch.myModerated')
  },
  {
    key: 'createdBy',
    name: t('taskMeeting.quickSearch.myCreated')
  },
  {
    key: 'lastModifiedBy',
    name: t('taskMeeting.quickSearch.myModified')
  },
  {
    key: 'lastDay',
    name: t('taskMeeting.quickSearch.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('taskMeeting.quickSearch.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('taskMeeting.quickSearch.lastWeek')
  }
]);

const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const quickSearchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocKeys = ['createdDate', 'moderatorId', 'createdBy'];
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

  return [startDate ? startDate.format(DATE_TIME_FORMAT) : '', endDate ? endDate.format(DATE_TIME_FORMAT) : ''];

  // return [
  //   startDate ? {
  //     value: startDate.format(DATE_TIME_FORMAT),
  //     op: 'GREATER_THAN_EQUAL',
  //     key: 'createdDate'
  //   } : '',
  //   endDate ? {
  //     value: endDate.format(DATE_TIME_FORMAT),
  //     op: 'LESS_THAN_EQUAL',
  //     key: 'createdDate'
  //   }  : ''].filter(Boolean);
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
    assocKeys.forEach(i => {
      if (i === 'createdDate') {
        timeKeys.forEach(t => delete selectedMenuMap.value[t]);
      } else {
        delete selectedMenuMap.value[i];
      }
    });
  } else {
    assocKeys.forEach(key => {
      if (['createdBy', 'moderatorId'].includes(key)) {
        const filterItem = assocFilters.value.find(i => i.key === key);
        if (!filterItem || filterItem.value !== userInfo.value?.id) {
          delete selectedMenuMap.value[key];
        }
      } else if (key === 'createdDate') {
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
const toSort = (sortData) => {
  orderBy.value = sortData.orderBy;
  orderSort.value = sortData.orderSort;
  emits('change', getParams());
};

const menuItemClick = (data) => {
  const key = data.key;
  const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];
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
  // let timeFilters: {key: string; op: string; value: string}[] = [];
  const assocFiltersInQuick:{valueKey: string, value: string|string[]}[] = [];
  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      assocFiltersInQuick.push({
        valueKey: 'createdDate',
        value: formatDateString(key)
      });
      return undefined;
    } else if (assocKeys.includes(key)) {
      if (['moderatorId', 'createdBy'].includes(key)) {
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
});
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex">
      <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
        <span>{{ t('quickSearch') }}</span>
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
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/task#meeting?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('taskMeeting.addMeeting') }}</span>
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
