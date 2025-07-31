<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Colon, Icon, IconText, SearchPanel, Select } from '@xcan-angus/vue-ui';
import { TESTER, XCanDexie, enumLoader } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';

import { MenuItem } from './PropsType';

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
  (e: 'import'): void;
  (e: 'delete'): void;
  (e: 'cancelDelete'): void;
  (e: 'auth'): void;
  (e: 'change', value: { key: string; op: string; value: boolean | string | string[]; }[]): void;
}>();

let db: Dexie<{ id: string; data: any; }>;

const searchPanelRef = ref();

// 保存快速搜索转换后的时间
const quickDateMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());
const typeDataMap = ref<Map<string, string>>(new Map());
const selectedMenuMap = ref(new Map<string, Omit<MenuItem, 'name'>>());

const filters = ref<{ key: string; op: string; value: string; }[]>([]);
const serviceIdFilter = ref<{ key: 'serviceId', op: 'EQUAL', value: string | undefined }>({ key: 'serviceId', op: 'EQUAL', value: undefined });
const sourceIdFilter = ref<{ key: 'sourceId', op: 'EQUAL', value: string | undefined }>({ key: 'sourceId', op: 'EQUAL', value: undefined });

const scriptTypeOpt = ref<MenuItem[]>([]);
const loadEnum = async () => {
  const [, data] = await enumLoader.load('ScriptType');
  scriptTypeOpt.value = (data || []).map(i => ({ name: i.message, key: i.value })).filter(i => i.key !== 'MOCK_APIS');
};

const menuItemClick = (data: MenuItem) => {
  const key = data.key;
  // 当前操作是取消选中
  const typeKeys = scriptTypeOpt.value.map(i => i.key);
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
      return;
    }

    if (typeKeys.includes(key)) {
      typeDataMap.value.clear();
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'type', value: undefined }
        ]);
      }
      return;
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

    // 清空serviceId
    serviceIdFilter.value.value = undefined;

    // 清空sourceId
    sourceIdFilter.value.value = undefined;

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
    return;
  }

  if (typeKeys.includes(key)) {
    typeDataMap.value.clear();
    typeDataMap.value.set(key, { key });
    // selectedMenuMap.value.set(key, {key});
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([
        { valueKey: 'type', value: key }
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

const toImport = () => {
  emit('import');
};

const toAuth = () => {
  emit('auth');
};

const toRefresh = () => {
  emit('change', getData());
};

// const typeExcludes = (data: { value: string; message: string }) => {
//   return ['MOCK_APIS'].includes(data.value);
// };

const sourceExcludes = (data: { value: string; message: string }) => {
  return ['TASK'].includes(data.value);
};

const searchPanelChange = (data: { key: string; op: string; value: string }[], _headers?: { [key: string]: string }, key?: string) => {
  filters.value = data;

  if (key === 'source') {
    // 重置服务、接口、场景
    serviceIdFilter.value.value = undefined;
    sourceIdFilter.value.value = undefined;
  }

  // 选择添加时间清空快速搜索已选中的时间选项
  if (key === 'createdDate') {
    selectedMenuMap.value.delete('lastDay');
    selectedMenuMap.value.delete('lastThreeDays');
    selectedMenuMap.value.delete('lastWeek');
  }
};

const sourceIdChange = (value: string) => {
  sourceIdFilter.value = { key: 'sourceId', op: 'EQUAL', value };
};

const serviceIdChange = (value: string) => {
  serviceIdFilter.value = { key: 'serviceId', op: 'EQUAL', value };
};

const getData = () => {
  // 包装数据
  const _filters: { key: string; op: string; value: boolean | string | string[] }[] = cloneDeep(filters.value);
  if (serviceIdFilter.value.value) {
    _filters.push({ ...(serviceIdFilter.value as { key: string; op: string; value: string; }) });
  }

  if (sourceIdFilter.value.value) {
    _filters.push({ ...(sourceIdFilter.value as { key: string; op: string; value: string; }) });
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
    const scriptTypeMap: { [key: string]: string | string[] } = {};
    if (Object.prototype.hasOwnProperty.call(dbData, 'a')) {
      filters.value = dbData.a || [];
      const dateTimeKeys = ['createdDate', 'startDate', 'deadlineDate', 'processedDate', 'confirmedDate', 'completedDate', 'canceledDate', 'execDate', 'lastModifiedDate'];
      const typeKeys = ['type'];
      const dateTimeMap: { [key: string]: string[] } = {};
      filters.value.every(({ key, value }) => {
        if (dateTimeKeys.includes(key)) {
          if (dateTimeMap[key]) {
            dateTimeMap[key].push(value);
          } else {
            dateTimeMap[key] = [value];
          }
        } else if (typeKeys.includes(key)) {
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

    if (Object.prototype.hasOwnProperty.call(dbData, 'j')) {
      serviceIdFilter.value = dbData.j || { key: 'serviceId', op: 'EQUAL', value: undefined };
    } else {
      serviceIdFilter.value = { key: 'serviceId', op: 'EQUAL', value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(dbData, 'k')) {
      sourceIdFilter.value = dbData.k || { key: 'sourceId', op: 'EQUAL', value: undefined };
    } else {
      sourceIdFilter.value = { key: 'sourceId', op: 'EQUAL', value: undefined };
    }

    const configs:{valueKey: string; value: string|string[]}[] = [];
    // 非查询面板的快速筛选
    const taskTypeKeys = Object.values(scriptTypeMap);
    if (taskTypeKeys.length) {
      taskTypeKeys.forEach(i => {
        selectedMenuMap.value.set(i, { key: i });
      });
      configs.push({
        valueKey: 'type',
        value: taskTypeKeys[0]
      });
      if (Object.keys(valueMap).length === taskTypeKeys.length) {
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
  typeDataMap.value.clear();
  filters.value = [];
  serviceIdFilter.value = { key: 'serviceId', op: 'EQUAL', value: undefined };
  sourceIdFilter.value = { key: 'sourceId', op: 'EQUAL', value: undefined };
};

onMounted(async () => {
  await loadEnum();
  watch(() => dbParamsKey.value, (newValue) => {
    if (!newValue) {
      return;
    }

    initialize();
  }, { immediate: true });

  watch(
    [
      () => filters.value,
      () => serviceIdFilter.value,
      () => sourceIdFilter.value,
      () => selectedMenuMap.value
    ], () => {
      const _filters = filters.value;
      if (!(_filters.length ||
        !!serviceIdFilter.value.value ||
        !!sourceIdFilter.value.value)) {
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

        // 脚本类型
        const scriptType = _filters.find(item => item.key === 'type')?.value;
        scriptTypeOpt.value.forEach(item => {
          selectedMenuMap.value.delete(item.key);
        });
        if (scriptType) {
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
          b?: true;
          c?: string;
          d?: {
            id: string;
            name: string;
            showTitle: string;
            showName: string;
          };
          e?: string[];
          f?: {
            id: string;
            name: string;
            showTitle: string;
            showName: string;
          }[];
          g?: { key: 'evalWorkload'; op: string; value: string | undefined; };
          h?: { key: 'failNum'; op: string; value: string | undefined; };
          i?: { key: 'totalNum'; op: string; value: string | undefined; };
          j?: { key: 'serviceId'; op: string; value: string | undefined; };
          k?: { key: 'sourceId'; op: string; value: string | undefined; };
        } = {};
        if (_filters.length) {
          dbData.a = cloneDeep(_filters);
        }

        if (serviceIdFilter.value.value) {
          dbData.j = cloneDeep(serviceIdFilter.value);
        }

        if (sourceIdFilter.value.value) {
          dbData.k = cloneDeep(sourceIdFilter.value);
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
  return btoa(dbBaseKey.value + 'script');
});

const apiParams = computed(() => {
  return {
    serviceId: serviceIdFilter.value.value
  };
});

const source = computed(() => {
  return filters.value.find(item => item.key === 'source')?.value;
});

const isAPISource = computed(() => {
  return source.value === 'API';
});

const isScenarioSource = computed(() => {
  return source.value === 'SCENARIO';
});

const menuItems = computed(():MenuItem[] => {
  return [
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
  ];
});

const searchOptions = [
  {
    valueKey: 'name',
    placeholder: '查询ID、名称、描述、插件',
    type: 'input',
    maxlength: 100
  },
  // {
  //   valueKey: 'type',
  //   placeholder: '选择脚本类型',
  //   type: 'select-enum',
  //   enumKey: 'ScriptType',
  //   excludes: typeExcludes
  // },
  {
    valueKey: 'source',
    placeholder: '选择来源',
    type: 'select-enum',
    enumKey: 'ScriptSource',
    excludes: sourceExcludes
  },
  {
    valueKey: 'tag',
    placeholder: '查询标签',
    type: 'input',
    op: 'EQUAL',
    maxlength: 100
  },
  {
    type: 'select',
    valueKey: 'serviceId',
    placeholder: '选择服务',
    noDefaultSlot: true
  },
  {
    type: 'select',
    valueKey: 'sourceId',
    noDefaultSlot: true
  },
  {
    type: 'select-user',
    valueKey: 'createdBy',
    placeholder: '添加人'
  },
  {
    type: 'select-user',
    valueKey: 'lastModifiedBy',
    placeholder: '最后修改人'
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: ['添加时间从', '添加时间到'],
    showTime: true
  },
  {
    type: 'date-range',
    valueKey: 'lastModifiedDate',
    placeholder: ['修改时间从', '修改时间到'],
    showTime: true
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
        <template #serviceId>
          <Select
            v-if="isAPISource"
            :value="serviceIdFilter.value"
            :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择服务"
            class="w-72 ml-2"
            showSearch
            @change="serviceIdChange">
            <template #option="record">
              <div class="text-3 leading-3 flex items-center h-6.5">
                <IconText
                  class="flex-shrink-0"
                  style="width:16px;height: 16px;background-color: rgba(162,222,236,100%);"
                  text="S" />
                <div :title="record.name" class="truncate ml-1.5">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </template>

        <template #sourceId>
          <Select
            v-if="isAPISource"
            :value="sourceIdFilter.value"
            :action="`${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`"
            :params="apiParams"
            :fieldNames="{ label: 'summary', value: 'id' }"
            :allowClear="true"
            placeholder="选择接口"
            class="w-72 ml-2"
            showSearch
            @change="sourceIdChange">
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
            v-if="isScenarioSource"
            :value="sourceIdFilter.value"
            :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择场景"
            class="w-72 ml-2"
            showSearch
            @change="sourceIdChange">
            <template #option="record">
              <div class="flex items-center">
                <Icon icon="icon-changjing" class="text-4 flex-shrink-0" />
                <div :title="record.name" class="truncate ml-1.5">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </template>
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2.5">
        <Button
          type="primary"
          size="small"
          class="py-0">
          <RouterLink
            class="h-6.5 leading-6.5 flex items-center space-x-1"
            to="/script/edit?type=edit">
            <Icon icon="icon-jia" />
            <span>添加脚本</span>
          </RouterLink>
        </Button>

        <Button
          type="primary"
          size="small"
          class="flex space-x-1"
          @click="toImport">
          <Icon icon="icon-shangchuan" class="text-3.5" />
          <span>导入脚本</span>
        </Button>

        <Button size="small" @click="toAuth">
          <Icon icon="icon-quanxian1" class="mr-1 text-3.5" />
          <span>脚本权限</span>
        </Button>

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
