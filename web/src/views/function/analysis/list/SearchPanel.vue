<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { XCanDexie, SearchCriteria, PageQuery } from '@xcan-angus/infra';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { MenuItem } from '@/views/function/analysis/list/types';

const { t } = useI18n();

type Props = {
  collapse: boolean;
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  orderBy: 'createdByName' ;
  orderSort: PageQuery.OrderSort;
  groupKey: 'none' | 'lastModifiedByName';
}

const props = withDefaults(defineProps<Props>(), {
  collapse: false,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  groupKey: 'none',
  orderBy: undefined,
  orderSort: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'add'): void;
  (e: 'change', value: SearchCriteria[]): void;
  (e: 'update:orderBy', value: 'createdByName'): void;
  (e: 'update:orderSort', value: PageQuery.OrderSort): void;
  (e: 'update:groupKey', value: 'none' | 'lastModifiedByName'): void;
  (e: 'update:visible', value: boolean): void;
}>();

let db: XCanDexie<{ id: string; data: any; }>;

const searchPanelRef = ref();

const quickDateMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());
const selectedMenuMap = ref(new Map<string, Omit<MenuItem, 'name'>>());

const filters = ref<SearchCriteria[]>([]);

const toSort = (data: { orderBy: 'createsDate' | 'createdByName' ; orderSort: PageQuery.OrderSort; }) => {
  emit('update:orderBy', data.orderBy);
  emit('update:orderSort', data.orderSort);
};

const menuItemClick = (data: MenuItem) => {
  const key = data.key;
  if (selectedMenuMap.value.has(key)) {
    if (key === 'none') {
      return;
    }

    selectedMenuMap.value.delete(key);

    if (key === 'createdBy') {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', value: undefined }]);
      }
      return;
    }

    if (key === 'lastModifiedBy') {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'lastModifiedBy', value: undefined }
        ]);
      }
      return;
    }

    if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      quickDateMap.value.clear();
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'createdDate', value: undefined }
        ]);
      }
    }
    return;
  }

  if (key === 'none') {
    selectedMenuMap.value.clear();
    selectedMenuMap.value.set('none', { key: 'none' });

    if (typeof searchPanelRef.value?.clear === 'function') {
      searchPanelRef.value.clear();
    }
    return;
  }

  if (key === 'createdBy') {
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', value: userId.value }]);
    }
    return;
  }

  if (key === 'lastModifiedBy') {
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([
        { valueKey: 'lastModifiedBy', value: userId.value }
      ]);
    }
    return;
  }

  if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
    quickDateMap.value.clear();
    quickDateMap.value.set(key, formatDateString(key));
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([
        { valueKey: 'createdDate', value: quickDateMap.value.get(key) }
      ]);
    }
  }
};

const formatDateString = (key: MenuItem['key']) => {
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
};

const toCreate = () => {
  emit('add');
};

const toRefresh = () => {
  emit('change', getFilters());
};

const searchPanelChange = (
  data: SearchCriteria[],
  _headers?: { [key: string]: string },
  key?: string
) => {
  filters.value = data;

  if (key === 'createdDate') {
    selectedMenuMap.value.delete('lastDay');
    selectedMenuMap.value.delete('lastThreeDays');
    selectedMenuMap.value.delete('lastWeek');
  }
};

const getFilters = () => {
  const _filters: SearchCriteria[] = cloneDeep(filters.value);
  return _filters;
};

const initialize = async () => {
  if (!db) {
    db = new XCanDexie<{ id: string; data: any; }>('parameter');
  }

  const [, data2] = await db.get(dbParamsKey.value);
  const dbData = data2?.data;
  if (dbData) {
    const valueMap: { [key: string]: string | string[] } = {};
    const notSearchPanelMap:{ [key: string]: string | string[] } = {};
    if (Object.prototype.hasOwnProperty.call(dbData, 'a')) {
      filters.value = dbData.a || [];
      const notSearchPanelKeys = ['lastModifiedBy'];
      const dateTimeKeys = ['createdDate'];
      const dateTimeMap: { [key: string]: string[] } = {};
      filters.value.every(({ key, value }) => {
        if (dateTimeKeys.includes(key)) {
          if (dateTimeMap[key]) {
            dateTimeMap[key].push(value);
          } else {
            dateTimeMap[key] = [value];
          }
        } else if (notSearchPanelKeys.includes(key)) {
          notSearchPanelMap[key] = value;
        } else {
          valueMap[key] = value;
        }

        return true;
      });

      Object.entries(dateTimeMap).every(([key, [date1, date2]]: [string, string[]]) => {
        const dateTimes: string[] = [];
        if (date1 && date2) {
          if (dayjs(date1).isBefore(dayjs(date2))) {
            dateTimes[0] = date1;
            dateTimes[1] = date2;
          } else {
            dateTimes[0] = date2;
            dateTimes[1] = date1;
          }
        }

        if (dateTimes.length === 2) {
          valueMap[key] = dateTimes;
        }
        return true;
      });
    } else {
      filters.value = [];
    }

    // 非查询面板的快速筛选
    const notSearchPanelKeys = Object.keys(notSearchPanelMap);
    if (notSearchPanelKeys.length) {
      notSearchPanelKeys.forEach(i => {
        selectedMenuMap.value.set(i, { key: i });
      });
      if (!Object.keys(valueMap).length) {
        emit('change', filters.value);
      }
    }

    // 设置搜索面板数据
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      const valueMapKey = Object.keys(valueMap);
      if (valueMapKey.length) {
        // 其他数据设置为空
        const configs = searchOptions.map(item => {
          return {
            valueKey: item.valueKey,
            value: valueMap[item.valueKey]
          };
        });
        searchPanelRef.value.setConfigs(configs);
      }
    }
    return;
  }

  resetData();
  resetSearchPanel();
};

const resetSearchPanel = () => {
  if (typeof searchPanelRef.value?.setConfigs === 'function') {
    const configs = searchOptions.map(item => {
      return {
        valueKey: item.valueKey,
        value: undefined
      };
    });
    searchPanelRef.value.setConfigs(configs);
  }
};

const resetData = () => {
  quickDateMap.value.clear();
  selectedMenuMap.value.clear();

  filters.value = [];
};

onMounted(async () => {
  watch([() => dbParamsKey.value, () => dbCountKey.value], ([key1, key2, key3]) => {
    if (!key1 || !key2 || !key3) {
      return;
    }
    initialize();
  }, { immediate: true });

  watch(
    [
      () => filters.value,
      () => selectedMenuMap.value
    ], () => {
      const _filters = filters.value;
      if (!(_filters.length)) {
        selectedMenuMap.value.clear();
        selectedMenuMap.value.set('none', { key: 'none' });

        emit('change', getFilters());
      } else {
        selectedMenuMap.value.delete('none');

        const createdBy = _filters.find(item => item.key === 'createdBy')?.value;
        if (createdBy && createdBy === userId.value) {
          selectedMenuMap.value.set('createdBy', { key: 'createdBy' });
        } else {
          selectedMenuMap.value.delete('createdBy');
        }

        const lastModifiedBy = _filters.find(item => item.key === 'lastModifiedBy')?.value;
        if (lastModifiedBy && lastModifiedBy === userId.value) {
          selectedMenuMap.value.set('lastModifiedBy', { key: 'lastModifiedBy' });
        } else {
          selectedMenuMap.value.delete('lastModifiedBy');
        }

        if (quickDateMap.value.size > 0) {
          selectedMenuMap.value.delete('lastDay');
          selectedMenuMap.value.delete('lastThreeDays');
          selectedMenuMap.value.delete('lastWeek');

          const createdDateStart = _filters.find(item => item.key === 'createdDate' &&
            item.op === SearchCriteria.OpEnum.GreaterThanEqual)?.value;
          const createdDateEnd = _filters.find(item => item.key === 'createdDate' &&
            item.op === SearchCriteria.OpEnum.LessThanEqual)?.value;
          const dateString = [createdDateStart, createdDateEnd];
          const entries = quickDateMap.value.entries();
          for (const [key, value] of entries) {
            if (isEqual(value, dateString)) {
              selectedMenuMap.value.set(key, { key });
            }
          }
          quickDateMap.value.clear();
        }
        emit('change', getFilters());
      }

      if (db) {
        const dbData: { filter?: SearchCriteria[]; } = {};
        if (_filters.length) {
          dbData.filter = cloneDeep(_filters);
        }

        if (Object.keys(dbData).length) {
          db.add({
            id: dbParamsKey.value,
            data: dbData
          });
        } else {
          db.delete(dbParamsKey.value);
        }
      }
    }, { immediate: false, deep: false });
});

const userId = computed(() => {
  return props.userInfo?.id;
});

const dbBaseKey = computed(() => {
  let key = '';
  if (userId.value) {
    key = userId.value;
  }

  if (props.projectId) {
    key += props.projectId;
  }

  return key;
});

const dbParamsKey = computed(() => {
  return btoa(dbBaseKey.value + 'function_analysis');
});

const dbCountKey = computed(() => {
  return btoa(dbBaseKey.value + 'count');
});

const menuItems: MenuItem[] = [
  {
    key: 'none',
    name: t('functionAnalysis.searchPanel.all')
  },
  {
    key: 'createdBy',
    name: t('functionAnalysis.searchPanel.myAdded')
  },
  {
    key: 'lastModifiedBy',
    name: t('functionAnalysis.searchPanel.myModified')
  },
  {
    key: 'lastDay',
    name: t('functionAnalysis.searchPanel.lastDay')
  },
  {
    key: 'lastThreeDays',
    name: t('functionAnalysis.searchPanel.lastThreeDays')
  },
  {
    key: 'lastWeek',
    name: t('functionAnalysis.searchPanel.lastWeek')
  }
];

const searchOptions = [
  {
    type: 'input',
    valueKey: 'name',
    placeholder: t('functionAnalysis.searchPanel.searchOptions.namePlaceholder')
  },
  {
    type: 'select-user',
    valueKey: 'createdBy',
    placeholder: t('functionAnalysis.searchPanel.searchOptions.createdByPlaceholder'),
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: [
      t('functionAnalysis.searchPanel.searchOptions.createdDatePlaceholder[0]'),
      t('functionAnalysis.searchPanel.searchOptions.createdDatePlaceholder[1]')
    ],
    showTime: true
  }
];

const sortMenuItems = [
  {
    key: 'name',
    name: t('functionAnalysis.searchPanel.sortOptions.name'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdByName',
    name: t('functionAnalysis.searchPanel.sortOptions.createdByName'),
    orderSort: PageQuery.OrderSort.Asc
  },
  {
    key: 'createdDate',
    name: t('functionAnalysis.searchPanel.sortOptions.createdDate'),
    orderSort: PageQuery.OrderSort.Asc
  }];
</script>
<template>
  <div class="text-3 leading-5">
    <div class="flex items-start justify-between mb-1.5">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>{{ t('functionAnalysis.searchPanel.quickQuery') }}</span>
          <Colon />
        </div>
        <div class="flex  flex-wrap ml-2">
          <div
            v-for="item in menuItems"
            :key="item.key"
            :class="{ 'active-key': selectedMenuMap.has(item.key) }"
            class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
            @click="menuItemClick(item)">
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-between">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchOptions"
        @change="searchPanelChange">
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2">
        <Button
          class="flex-shrink-0 flex items-center"
          type="primary"
          size="small"
          @click="toCreate">
          <div class="flex items-center">
            <Icon icon="icon-jia" class="text-3.5" />
            <span class="ml-1">{{ t('functionAnalysis.searchPanel.addAnalysis') }}</span>
          </div>
        </Button>

        <DropdownSort
          :orderBy="props.orderBy"
          :orderSort="props.orderSort"
          :menuItems="sortMenuItems"
          @click="toSort">
          <Button
            size="small"
            class="flex items-center cursor-pointer ">
            <Icon icon="icon-biaotoupaixu" class="text-3.5" />
            <span class="ml-1">{{ t('sort') }}</span>
          </Button>
        </DropdownSort>

        <Button
          class="flex-shrink-0 flex items-center"
          size="small"
          @click="toRefresh">
          <IconRefresh class="text-4 flex-shrink-0" />
          {{ t('actions.refresh') }}
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.active-key {
  background-color: #4ea0fd;
  color: #fff;
}

.ant-tag {
  background-color: #fff;
}

.ant-tag.tag-checked {
  background-color: rgba(255, 129, 0, 60%);
  color: #fff;
}

.ant-tag.tag-checked :deep(.ant-tag-close-icon)>svg {
  color: #fff;
}

:deep(.sprint.tag-checked) {
  background-color: rgba(255, 129, 0, 60%);
}

.tag.tag-checked {
  background-color: #4ea0fd;
}
</style>
