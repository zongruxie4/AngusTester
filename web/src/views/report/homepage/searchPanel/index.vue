<script setup lang="ts">
import { computed, onMounted, ref, watch, inject, Ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Colon, Icon, SearchPanel, Select } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { XCanDexie, TESTER, CombinedTargetType } from '@xcan-angus/infra';

import { MenuItem } from './PropsType';

type OrderByKey = 'createdDate' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'openAuth'): void;
  (e: 'add'): void;
  (e: 'sort', value: { orderBy: OrderByKey; orderSort: OrderSortKey; }): void;
  (e: 'change', value: { key: string; op: string; value: boolean | string | string[]; }[]): void;
}>();

const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

let db: Dexie<{ id: string; data: any; }>;

const searchPanelRef = ref();

// 保存快速搜索转换后的时间
const quickDateMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());
const selectedMenuMap = ref(new Map<string, Omit<MenuItem, 'name'>>());

const filters = ref<{ key: string; op: string; value: string; }[]>([]);
const targetIdFilter = ref<{ key: 'targetId', op: 'EQUAL', value: string | undefined }>({ key: 'targetId', op: 'EQUAL', value: undefined });

const menuItemClick = (data: MenuItem) => {
  const key = data.key;
  // 当前操作是取消选中
  if (selectedMenuMap.value.has(key)) {
    // 【所有】这个按钮选中后再次点击不做处理
    if (key === 'none') {
      return;
    }

    // 删除该条选中数据
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

  // 选中【所有】，清除其他按钮的选中
  if (key === 'none') {
    selectedMenuMap.value.clear();
    selectedMenuMap.value.set('none', { key: 'none' });

    // 清空搜索面板
    if (typeof searchPanelRef.value?.clear === 'function') {
      searchPanelRef.value.clear();
    }

    // 清空sourceId
    targetIdFilter.value.value = undefined;

    return;
  }

  // 其他按钮会通过watchEffect中的filters的值进行自动设置
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

  return [startDate ? startDate.format('YYYY-MM-DD HH:mm:ss') : '', endDate ? endDate.format('YYYY-MM-DD HH:mm:ss') : ''];
};

const toSort = (data: { orderBy: OrderByKey; orderSort: OrderSortKey; }): void => {
  emit('sort', data);
};

const toRefresh = () => {
  emit('change', getData());
};

const toAuth = () => {
  emit('openAuth');
};

const targetIdChange = (value: string) => {
  targetIdFilter.value = { key: 'targetId', op: 'EQUAL', value };
};

const searchPanelChange = (data: { key: string; op: string; value: string }[], _headers?: { [key: string]: string }, key?: string) => {
  filters.value = data;

  if (key === 'targetType') {
    targetIdFilter.value.value = undefined;
  }

  // 选择添加时间清空快速搜索已选中的时间选项
  if (key === 'createdDate') {
    selectedMenuMap.value.delete('lastDay');
    selectedMenuMap.value.delete('lastThreeDays');
    selectedMenuMap.value.delete('lastWeek');
  }
};

const getData = () => {
  // 包装数据
  const _filters: { key: string; op: string; value: boolean | string | string[] }[] = cloneDeep(filters.value);
  if (targetIdFilter.value.value) {
    _filters.push({ ...(targetIdFilter.value as { key: string; op: string; value: string; }) });
  }

  return _filters;
};

const initialize = async () => {
  if (!db) {
    db = new XCanDexie<{ id: string; data: any; }>('parameter');
  }

  // 设置搜索条件数据
  const [, data2] = await db.get(dbParamsKey.value);
  const dbData = data2?.data;
  if (dbData) {
    const valueMap: { [key: string]: string | string[] } = {};
    if (Object.prototype.hasOwnProperty.call(dbData, 'a')) {
      filters.value = dbData.a || [];
      const dateTimeKeys = ['createdDate'];
      const dateTimeMap: { [key: string]: string[] } = {};
      filters.value.every(({ key, value }) => {
        if (dateTimeKeys.includes(key)) {
          if (dateTimeMap[key]) {
            dateTimeMap[key].push(value);
          } else {
            dateTimeMap[key] = [value];
          }
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

    if (Object.prototype.hasOwnProperty.call(dbData, 'b')) {
      targetIdFilter.value = dbData.b || { key: 'targetId', op: 'EQUAL', value: undefined };
    } else {
      targetIdFilter.value = { key: 'targetId', op: 'EQUAL', value: undefined };
    }

    // 设置搜索面板数据
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      if (Object.keys(valueMap).length) {
        // 其他数据设置为空
        const configs = searchOptions.value.map(item => {
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

  // 没有缓存数据，需要重置搜索面板数据
  resetData();
  resetSearchPanel();
};

const resetSearchPanel = () => {
  if (typeof searchPanelRef.value?.setConfigs === 'function') {
    const configs = searchOptions.value.map(item => {
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
  targetIdFilter.value = { key: 'targetId', op: 'EQUAL', value: undefined };
};

onMounted(async () => {
  watch(() => dbParamsKey.value, (newValue) => {
    if (!newValue) {
      return;
    }

    initialize();
  }, { immediate: true });

  watch(
    [
      () => filters.value,
      () => targetIdFilter.value,
      () => selectedMenuMap.value
    ], () => {
      const _filters = filters.value;
      if (!(_filters.length || !!targetIdFilter.value.value)) {
        selectedMenuMap.value.clear();
        selectedMenuMap.value.set('none', { key: 'none' });

        emit('change', []);
      } else {
        // 删除快速查询选中的【所有】选项
        selectedMenuMap.value.delete('none');

        // 设置快速搜索
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

          const createdDateStart = _filters.find(item => item.key === 'createdDate' && item.op === 'GREATER_THAN_EQUAL')?.value;
          const createdDateEnd = _filters.find(item => item.key === 'createdDate' && item.op === 'LESS_THAN_EQUAL')?.value;
          const dateString = [createdDateStart, createdDateEnd];
          const entries = quickDateMap.value.entries();
          for (const [key, value] of entries) {
            if (isEqual(value, dateString)) {
              selectedMenuMap.value.set(key, { key });
            }
          }

          quickDateMap.value.clear();
        }

        emit('change', getData());
      }

      // 保存到db
      if (db) {
        const dbData: {
          a?: {
            key: string;
            op: string;
            value: string;
          }[];
          b?: { key: 'targetId'; op: string; value: string | undefined; };
        } = {};
        if (_filters.length) {
          dbData.a = cloneDeep(_filters);
        }

        if (targetIdFilter.value.value) {
          dbData.b = cloneDeep(targetIdFilter.value);
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
  return btoa(dbBaseKey.value + 'report');
});

const targetType = computed(() => {
  return filters.value.find(item => item.key === 'targetType')?.value;
});

const isProjectTargetType = computed(() => {
  return targetType.value === 'PROJECT';
});

const isServiceTargetType = computed(() => {
  return targetType.value === 'SERVICE';
});

const isAPITargetType = computed(() => {
  return targetType.value === 'API';
});

const isTaskTargetType = computed(() => {
  return targetType.value === 'TASK';
});

const isSprintTargetType = computed(() => {
  return targetType.value === 'TASK_SPRINT';
});

const isPlanTargetType = computed(() => {
  return targetType.value === 'FUNC_PLAN';
});

const isCaseTargetType = computed(() => {
  return targetType.value === 'FUNC_CASE';
});

const isExecutionTargetType = computed(() => {
  return targetType.value === 'EXECUTION';
});

const isScenarioTargetType = computed(() => {
  return targetType.value === 'SCENARIO';
});

const menuItems: MenuItem[] = [
  {
    key: 'none',
    name: '所有'
  },
  {
    key: 'createdBy',
    name: '我添加的'
  },
  {
    key: 'lastModifiedBy',
    name: '我修改的'
  },
  {
    key: 'lastDay',
    name: '近1天'
  },
  {
    key: 'lastThreeDays',
    name: '近3天'
  },
  {
    key: 'lastWeek',
    name: '近7天'
  }
];

const searchOptions = computed(() => [
  {
    valueKey: 'name',
    placeholder: '查询名称、描述',
    type: 'input',
    maxlength: 100
  },
  // {
  //   valueKey: 'category',
  //   placeholder: '选择分类',
  //   type: 'select-enum',
  //   enumKey: 'ReportCategory'
  // },
  {
    valueKey: 'targetType',
    placeholder: '选择资源类型',
    type: 'select-enum',
    enumKey: CombinedTargetType,
    excludes: (data: { message: string; value: 'PROJECT' | 'TAG' | 'MODULE' | 'SERVICE' | 'API' | 'API_CASE' | 'SCENARIO' | 'TASK' | 'TASK_SPRINT' | 'VARIABLE' | 'DATASET' | 'FUNC_PLAN' | 'FUNC_CASE' | 'SCRIPT' | 'MOCK_SERVICE' | 'MOCK_APIS' | 'REPORT' | 'EXECUTION' }) => {
      return !(['PROJECT', 'SERVICE', 'API', proTypeShowMap.value.showTask && 'TASK', proTypeShowMap.value.showSprint && 'TASK_SPRINT', 'FUNC_PLAN', 'FUNC_CASE', 'EXECUTION', 'SCENARIO'].filter(Boolean)).includes(data.value);
    }
  },
  {
    type: 'select',
    valueKey: 'targetId',
    placeholder: '选择资源',
    noDefaultSlot: true
  },
  {
    type: 'select-user',
    valueKey: 'createdBy',
    placeholder: '添加人'
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: ['添加时间从', '添加时间到'],
    showTime: true
  }
]);

const sortMenus = [
  {
    name: '按添加时间',
    key: 'createdDate',
    orderSort: 'DESC'
  },
  {
    name: '按添加人',
    key: 'createdByName',
    orderSort: 'DESC'
  }
];
</script>
<template>
  <div class="text-3 leading-5">
    <div class="flex items-start justify-between mb-1.5">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>快速查询</span>
          <Colon />
        </div>
        <div class="flex flex-wrap ml-2">
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

    <div class="flex items-start justify-between space-x-5">
      <SearchPanel
        ref="searchPanelRef"
        class="flex-1"
        :options="searchOptions"
        @change="searchPanelChange">
        <template #targetId>
          <Select
            v-if="isProjectTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/project?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择项目"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange" />

          <Select
            v-if="isServiceTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择服务"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange" />

          <Select
            v-if="isAPITargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'summary', value: 'id' }"
            :allowClear="true"
            placeholder="选择接口"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange">
            <template #option="record">
              <div class="flex items-center">
                <Icon
                  icon="icon-jiekou"
                  class="text-4 flex-shrink-0"
                  style="color:rgba(82,196,26,100%);" />
                <div :title="record.summary" class="truncate ml-1.5">{{ record.summary }}</div>
              </div>
            </template>
          </Select>

          <Select
            v-if="isTaskTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择任务"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange" />

          <Select
            v-if="isSprintTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择迭代"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange" />

          <Select
            v-if="isPlanTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/func/plan?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择计划"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange" />

          <Select
            v-if="isCaseTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/func/case?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择用例"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange" />

          <Select
            v-if="isExecutionTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/exec?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择执行"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange" />

          <Select
            v-if="isScenarioTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择场景"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange" />
        </template>
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2.5">
        <Button
          type="primary"
          size="small"
          class="flex space-x-1"
          @click="emit('add')">
          <Icon icon="icon-jia" class="text-3.5" />
          <span>添加报告</span>
        </Button>

        <Button size="small" @click="toAuth">
          <Icon icon="icon-quanxian1" class="mr-1 text-3.5" />
          <span>报告权限</span>
        </Button>

        <!-- <DropdownSort :menuItems="sortMenus" @click="toSort">
          <Button size="small">
            <Icon icon="icon-biaotoupaixu" class="text-3.5 mr-1" />
            <span>排序</span>
          </Button>
        </DropdownSort> -->

        <Button size="small" @click="toRefresh">
          <Icon icon="icon-shuaxin" class="mr-1 text-3.5" />
          <span>刷新</span>
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
</style>
