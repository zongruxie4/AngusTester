<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Colon, DropdownSort, Icon, Input, SearchPanel, Select, SelectEnum } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { XCanDexie, TESTER, duration, site, enumLoader } from '@xcan-angus/tools';
import { debounce } from 'throttle-debounce';
import { setting } from '@/api/gm';

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
  (e: 'sort', value: { orderBy: OrderByKey; orderSort: OrderSortKey; }): void;
  (e: 'change', value: { key: string; op: string; value: boolean | string | string[]; }[]): void;
}>();

const editionType = ref<string>();

let db: Dexie<{ id: string; data: any; }>;

const isPrivate = ref(false);
const searchPanelRef = ref();
const nodeQuota = ref(0);

// 保存快速搜索转换后的时间
const quickDateMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());
const selectedMenuMap = ref(new Map<string, Omit<MenuItem, 'name'>>());
let scriptTypeKeys: string[] = [];

const scriptTypeOpt = ref<{name: string; key: string}[]>([]);

const filters = ref<{ key: string; op: string; value: string; }[]>([]);
const scriptSourceIdFilter = ref<{ key: 'scriptSourceId', op: 'EQUAL', value: string | undefined }>({ key: 'scriptSourceId', op: 'EQUAL', value: undefined });
const priorityFilter = ref<{ key: 'priority', op: 'EQUAL'|'GREATER_THAN'|'GREATER_THAN_EQUAL'|'LESS_THAN'|'LESS_THAN_EQUAL', value: string | undefined }>({ key: 'priority', op: 'EQUAL', value: undefined });

const loadScriptTypeEnum = async () => {
  const [, data] = await enumLoader.load('ScriptType');
  scriptTypeOpt.value = (data || []).map(i => ({ name: i.message, key: i.value })).filter(i => i.key !== 'API_MOCK');
  scriptTypeKeys = scriptTypeOpt.value.map(i => i.key);
};

const loadNodeQuota = async () => {
  const [error, { data }] = await setting.getQuotaByName('AngusTesterNode');
  if (error) {
    return;
  }
  nodeQuota.value = +data?.quota || 0;
};

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

    if (key === 'execBy') {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'execBy', value: undefined }
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
      return;
    }

    if (scriptTypeKeys.includes(key)) {
      selectedMenuMap.value.delete(key);
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'scriptType', value: undefined }
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
    scriptSourceIdFilter.value.value = undefined;

    // 清空优先级
    priorityFilter.value.value = undefined;
    priorityFilter.value.op = 'EQUAL';

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

  if (key === 'execBy') {
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([
        { valueKey: 'execBy', value: userId.value }
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
    return;
  }
  if (scriptTypeKeys.includes(key)) {
    scriptTypeKeys.forEach(i => selectedMenuMap.value.delete(i));
    selectedMenuMap.value.set(key, { key });
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([
        { valueKey: 'scriptType', value: key }
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

const scriptSourceIdChange = (value:string) => {
  scriptSourceIdFilter.value = { key: 'scriptSourceId', op: 'EQUAL', value };
};

const priorityInputChange = debounce(duration.search, (event:{target:{value:string}}) => {
  const value = event.target.value;
  priorityFilter.value = {
    ...priorityFilter.value,
    value
  };
});

const prioritySelectChange = (value: 'EQUAL'|'GREATER_THAN'|'GREATER_THAN_EQUAL'|'LESS_THAN'|'LESS_THAN_EQUAL') => {
  priorityFilter.value = {
    ...priorityFilter.value,
    op: value
  };
};

const searchPanelChange = (data: { key: string; op: string; value: string }[], _headers?: { [key: string]: string }, key?: string) => {
  filters.value = data;

  if (key === 'scriptSource') {
    scriptSourceIdFilter.value.value = undefined;
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
  if (scriptSourceIdFilter.value.value) {
    _filters.push({ ...(scriptSourceIdFilter.value as { key: string; op: string; value: string; }) });
  }

  if (priorityFilter.value.value) {
    _filters.push({ ...(priorityFilter.value as { key: string; op: string; value: string; }) });
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
    const scriptTypeMap: { [key: string]: string} = {};
    if (Object.prototype.hasOwnProperty.call(dbData, 'a')) {
      filters.value = dbData.a || [];
      const dateTimeKeys = ['startDate', 'actualStartDate', 'endDate', 'createdDate', 'lastModifiedDate'];
      const dateTimeMap: { [key: string]: string[] } = {};

      filters.value.every(({ key, value }) => {
        if (dateTimeKeys.includes(key)) {
          if (dateTimeMap[key]) {
            dateTimeMap[key].push(value);
          } else {
            dateTimeMap[key] = [value];
          }
        } else if (scriptTypeKeys.includes(key)) {
          scriptTypeMap[key] = value;
          valueMap[key] = value;
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
      scriptSourceIdFilter.value = dbData.b || { key: 'scriptSourceId', op: 'EQUAL', value: undefined };
    } else {
      scriptSourceIdFilter.value = { key: 'scriptSourceId', op: 'EQUAL', value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(dbData, 'c')) {
      priorityFilter.value = dbData.c || { key: 'priority', op: 'EQUAL', value: undefined };
    } else {
      priorityFilter.value = { key: 'priority', op: 'EQUAL', value: undefined };
    }

    const scriptValue = Object.keys(scriptTypeMap);
    if (scriptValue.length) {
      scriptValue.forEach(i => {
        selectedMenuMap.value.set(i, { key: i });
      });
      if (!Object.keys(valueMap).length) {
        emit('change', filters.value);
      }
    }

    // 设置搜索面板数据
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      if (Object.keys(valueMap).length) {
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

  // 没有缓存数据，需要重置搜索面板数据
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
  scriptSourceIdFilter.value = { key: 'scriptSourceId', op: 'EQUAL', value: undefined };
};

onMounted(async () => {
  isPrivate.value = await site.isPrivate();
  await loadScriptTypeEnum();
  loadNodeQuota();
  const envContent = await site.getEnvContent();
  editionType.value = envContent?.VITE_EDITION_TYPE;

  watch(() => dbParamsKey.value, (newValue) => {
    if (!newValue) {
      return;
    }

    initialize();
  }, { immediate: true });

  watch(
    [
      () => filters.value,
      () => scriptSourceIdFilter.value,
      () => priorityFilter.value,
      () => selectedMenuMap.value
    ], () => {
      const _filters = filters.value;
      if (!(_filters.length || !!scriptSourceIdFilter.value.value)) {
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

        const execBy = _filters.find(item => item.key === 'execBy')?.value;
        if (execBy && execBy === userId.value) {
          selectedMenuMap.value.set('execBy', { key: 'execBy' });
        } else {
          selectedMenuMap.value.delete('execBy');
        }

        const scriptType = _filters.find(item => item.key === 'scriptType')?.value;
        if (!scriptType) {
          selectedMenuMap.value.delete(scriptType);
        } else {
          selectedMenuMap.value.set(scriptType, { key: scriptType });
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
          b?: { key: 'scriptSourceId'; op: string; value: string | undefined; };
          c?: { key: 'priority', op: 'EQUAL'|'GREATER_THAN'|'GREATER_THAN_EQUAL'|'LESS_THAN'|'LESS_THAN_EQUAL', value: string | undefined };
        } = {};
        if (_filters.length) {
          dbData.a = cloneDeep(_filters);
        }

        if (scriptSourceIdFilter.value.value) {
          dbData.b = cloneDeep(scriptSourceIdFilter.value);
        }

        if (priorityFilter.value.value) {
          dbData.c = cloneDeep(priorityFilter.value);
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
  return btoa(dbBaseKey.value + 'execution');
});

const scriptSource = computed(() => {
  return filters.value.find(item => item.key === 'scriptSource')?.value;
});

const isServiceTargetType = computed(() => {
  return scriptSource.value === 'SERVICE_SMOKE' || scriptSource.value === 'SERVICE_SECURITY';
});

const isAPITargetType = computed(() => {
  return scriptSource.value === 'API';
});

const isScenarioTargetType = computed(() => {
  return scriptSource.value === 'SCENARIO';
});

const menuItems = computed(() => [
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
    key: 'execBy',
    name: '我执行的'
  },
  ...scriptTypeOpt.value,
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
]);

const searchOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: '查询名称、描述',
    maxlength: 100
  },
  {
    valueKey: 'status',
    type: 'select-enum',
    enumKey: 'ExecStatus',
    placeholder: '选择状态'
  },
  {
    valueKey: 'scriptId',
    type: 'select',
    action: `${TESTER}/script?projectId=${props.projectId}&fullTextSearch=true`,
    fieldNames: { label: 'name', value: 'id' },
    placeholder: '选择脚本',
    maxlength: 100
  },
  {
    valueKey: 'priority'
  },
  {
    valueKey: 'scriptSource',
    type: 'select-enum',
    enumKey: 'ScriptSource',
    placeholder: '选择关联资源类型'
  },
  {
    valueKey: 'scriptSourceId',
    type: 'select',
    placeholder: '选择关联资源',
    noDefaultSlot: true
  },
  {
    valueKey: 'startDate',
    type: 'date-range',
    placeholder: ['计划开始时间从', '计划开始时间到'],
    showTime: true
  },
  {
    valueKey: 'actualStartDate',
    type: 'date-range',
    placeholder: ['实际开始时间从', '实际开始时间到'],
    showTime: true
  },
  {
    valueKey: 'endDate',
    type: 'date-range',
    placeholder: ['结束时间从', '结束时间到'],
    showTime: true
  },
  {
    valueKey: 'execBy',
    type: 'select-user',
    placeholder: '选择执行人'
  },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    placeholder: '选择添加人'
  },
  {
    valueKey: 'createdDate',
    type: 'date-range',
    placeholder: ['添加时间从', '添加时间到'],
    showTime: true
  },
  {
    valueKey: 'lastModifiedBy',
    placeholder: '选择最后修改人',
    type: 'select-user'
  },
  {
    valueKey: 'lastModifiedDate',
    placeholder: ['最后修改时间从', '最后修改时间到'],
    type: 'date-range',
    showTime: true
  }
];

const sortMenus = [
  {
    name: '按添加时间',
    key: 'createdDate',
    orderSort: 'DESC'
  },
  {
    name: '按优先级',
    key: 'priority',
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

    <div class="flex items-start justify-between mb-1.5 space-x-5">
      <SearchPanel
        ref="searchPanelRef"
        class="flex-1 min-w-0"
        :options="searchOptions"
        @change="searchPanelChange">
        <template #priority>
          <Input
            :value="priorityFilter.value"
            dataType="float"
            size="small"
            allowClear
            placeholder="优先级"
            class="w-72 ml-2"
            :min="0"
            @change="priorityInputChange">
            <template #prefix>
              <SelectEnum
                :value="priorityFilter.op"
                size="small"
                enumKey="NumberCompareCondition"
                :allowClear="false"
                :bordered="false"
                class="w-24"
                @change="prioritySelectChange" />
            </template>
          </Input>
        </template>

        <template #scriptSourceId>
          <Select
            v-if="isServiceTargetType"
            :value="scriptSourceIdFilter.value"
            :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择服务"
            class="w-72 ml-2"
            showSearch
            @change="scriptSourceIdChange" />

          <Select
            v-if="isAPITargetType"
            :value="scriptSourceIdFilter.value"
            :action="`${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'summary', value: 'id' }"
            :allowClear="true"
            placeholder="选择接口"
            class="w-72 ml-2"
            showSearch
            @change="scriptSourceIdChange">
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
            v-if="isScenarioTargetType"
            :value="scriptSourceIdFilter.value"
            :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择场景"
            class="w-72 ml-2"
            showSearch
            @change="scriptSourceIdChange">
            <template #option="record">
              <div class="flex items-center">
                <Icon
                  icon="icon-changjingceshi"
                  class="text-4 flex-shrink-0"
                  style="color:rgba(82,196,26,100%);" />
                <div :title="record.name" class="truncate ml-1.5">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </template>
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2.5">
        <Button
          v-if="editionType === 'CLOUD_SERVICE'"
          type="primary"
          size="small"
          class="p-0">
          <RouterLink
            class="flex items-center space-x-1 leading-6.5 px-1.75"
            to="/execution/experience?type=expr">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>体验执行</span>
          </RouterLink>
        </Button>

        <Button
          v-show="isPrivate || nodeQuota >= 1"
          type="primary"
          size="small"
          class="p-0">
          <RouterLink
            class="flex items-center space-x-1 leading-6.5 px-1.75"
            to="/execution/add">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>添加执行</span>
          </RouterLink>
        </Button>

        <DropdownSort :menuItems="sortMenus" @click="toSort">
          <Button size="small">
            <Icon icon="icon-biaotoupaixu" class="text-3.5 mr-1" />
            <span>排序</span>
          </Button>
        </DropdownSort>

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
