<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Button } from 'ant-design-vue';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { XCanDexie } from '@xcan-angus/infra';

import { MenuItem, SelectOption } from './PropsType';

type Props = {
  collapse: boolean;// 展开、折叠统计
  viewMode: 'table' | 'detail' | 'kanban';
  sprintId: string;
  sprintName: string;
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName';
  orderSort: 'DESC' | 'ASC';
  groupKey: 'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType';
}

const props = withDefaults(defineProps<Props>(), {
  collapse: false,
  viewMode: undefined,
  sprintId: undefined,
  sprintName: undefined,
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
  (e: 'export'): void;
  (e: 'change', value: { key: string; op: string; value: boolean | string | string[]; }[]): void;
  (e: 'update:orderBy', value: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName'): void;
  (e: 'update:orderSort', value: 'DESC' | 'ASC'): void;
  (e: 'update:groupKey', value: 'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType'): void;
  (e: 'update:visible', value: boolean): void;
  (e: 'update:collapse', value: boolean): void;
  (e: 'viewModeChange', value: 'table' | 'detail' | 'boards'): void;
  (e: 'uploadTask'): void;
  (e: 'exportTemplate'): void;
  (e: 'update:moduleFlag', value: boolean): void;
}>();

const route = useRoute();

let db: Dexie<{ id: string; data: any; }>;

const searchPanelRef = ref();

// 保存快速搜索转换后的时间
const quickDateMap = ref<Map<'lastDay' | 'lastThreeDays' | 'lastWeek', string[]>>(new Map());
const selectedMenuMap = ref(new Map<string, Omit<MenuItem, 'name'>>());
const overdue = ref(false); // 逾期
const moduleFlag = ref(false); // 按模块分组

const sprintSelectVisible = ref(false);
const selectedSprint = ref<SelectOption>();
const checkedSprintId = ref<string>();

const tagSelectVisible = ref(false);
const selectedTags = ref<SelectOption[]>([]);
const checkedTagIds = ref<string[]>([]);

const filters = ref<{ key: string; op: string; value: string; }[]>([]);

const targetParentIdFilter = ref<{ key: 'targetParentId', op: 'EQUAL', value: string | undefined }>({ key: 'targetParentId', op: 'EQUAL', value: undefined });
const targetIdFilter = ref<{ key: 'targetId', op: 'EQUAL', value: string | undefined }>({ key: 'targetId', op: 'EQUAL', value: undefined });

const evalWorkloadFilter = ref<{ key: 'evalWorkload'; op: string; value: string | undefined; }>({ key: 'evalWorkload', op: 'EQUAL', value: undefined });
const failNumFilter = ref<{ key: 'failNum'; op: string; value: string | undefined; }>({ key: 'failNum', op: 'EQUAL', value: undefined });
const totalNumFilter = ref<{ key: 'totalNum'; op: string; value: string | undefined; }>({ key: 'totalNum', op: 'EQUAL', value: undefined });

const toSort = (data: { orderBy: 'createsDate' | 'createdByName' ; orderSort: 'DESC' | 'ASC'; }) => {
  emit('update:orderBy', data.orderBy);
  emit('update:orderSort', data.orderSort);
};

const toGroup = (value: 'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType') => {
  emit('update:groupKey', value);
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

    if (key === 'assigneeId' || key === 'progress') {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'assigneeId', value: undefined },
          { valueKey: 'status', value: undefined }
        ]);
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

    if (key === 'confirmorId') {
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'confirmorId', value: undefined },
          { valueKey: 'status', value: undefined }
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

  // 选中【所有】，清除其他按钮的选中，保留迭代和标签
  if (key === 'none') {
    selectedMenuMap.value.clear();
    selectedMenuMap.value.set('none', { key: 'none' });

    // 清空搜索面板
    if (typeof searchPanelRef.value?.clear === 'function') {
      searchPanelRef.value.clear();
    }

    // 关闭逾期
    overdue.value = false;

    // // 清空选中的迭代
    // checkedSprintId.value = undefined;

    // // 清空选中的标签
    // checkedTagIds.value = [];

    // 清空targetParentId
    targetParentIdFilter.value.value = undefined;

    // 清空targetId
    targetIdFilter.value.value = undefined;

    // 清空evalWorkload
    evalWorkloadFilter.value.value = undefined;

    // 清空failNum
    failNumFilter.value.value = undefined;

    // 清空totalNum
    totalNumFilter.value.value = undefined;

    return;
  }

  // 其他按钮会通过watchEffect中的filters的值进行自动设置
  if (key === 'createdBy') {
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([{ valueKey: 'createdBy', value: userId.value }]);
    }

    return;
  }

  if (key === 'assigneeId' || key === 'progress') {
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      const value = key === 'assigneeId' ? 'PENDING' : 'IN_PROGRESS';
      searchPanelRef.value.setConfigs([
        { valueKey: 'assigneeId', value: userId.value },
        { valueKey: 'status', value }
      ]);
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

  if (key === 'confirmorId') {
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([
        { valueKey: 'confirmorId', value: userId.value },
        { valueKey: 'status', value: 'CONFIRMING' }
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

const formatData = ({ id, name }: { id: string; name: string; }) => {
  let showTitle = '';
  let showName = name;
  const MAX_LENGTH = 10;
  if (name.length > MAX_LENGTH) {
    showTitle = name;
    showName = showName.slice(0, MAX_LENGTH) + '...';
  }

  return { id, name, showTitle, showName };
};

const toCreate = () => {
  emit('add');
};

const toRefresh = () => {
  emit('change', getData());
};

const searchPanelChange = (data: { key: string; op: string; value: string }[], _headers?: { [key: string]: string }, key?: string) => {
  filters.value = data;

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
  if (overdue.value) {
    _filters.push({ key: 'overdue', op: 'EQUAL', value: true });
  }

  if (checkedSprintId.value) {
    _filters.push({ key: 'sprintId', op: 'EQUAL', value: checkedSprintId.value });
  }

  if (checkedTagIds.value.length) {
    const value: string[] = [];
    checkedTagIds.value.forEach(item => {
      value.push(item);
    });
    _filters.push({ key: 'tagId', op: 'IN', value });
  }

  if (targetParentIdFilter.value.value) {
    _filters.push({ ...(targetParentIdFilter.value as { key: string; op: string; value: string; }) });
  }

  if (targetIdFilter.value.value) {
    _filters.push({ ...(targetIdFilter.value as { key: string; op: string; value: string; }) });
  }

  if (evalWorkloadFilter.value.value) {
    _filters.push({ ...(evalWorkloadFilter.value as { key: string; op: string; value: string; }) });
  }

  if (failNumFilter.value.value) {
    _filters.push({ ...(failNumFilter.value as { key: string; op: string; value: string; }) });
  }

  if (totalNumFilter.value.value) {
    _filters.push({ ...(totalNumFilter.value as { key: string; op: string; value: string; }) });
  }

  return _filters;
};

const initialize = async () => {
  if (!db) {
    db = new XCanDexie<{ id: string; data: any; }>('parameter');
  }

  // 设置统计区域展开收起
  // const [, data] = await db.get(dbCountKey.value);
  // emit('update:collapse', !!data?.data);

  // 设置任务列表展现形式
  // const [, data1] = await db.get(dbViewModeKey.value);
  // const viewMode = ['case', 'table', 'kanban'].includes(data1?.data) ? data1?.data : 'case';
  // emit('viewModeChange', viewMode);

  // 设置是否按模块分组
  // const [, dataModule] = await db.get(dbModuleKey.value);
  // moduleFlag.value = dataModule?.data === 'true';
  // emit('update:moduleFlag', moduleFlag.value);

  // 设置搜索条件数据
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

    if (Object.prototype.hasOwnProperty.call(dbData, 'b')) {
      overdue.value = dbData.b || false;
    } else {
      overdue.value = false;
    }

    // 从迭代跳转过来，需要替换为该迭代
    if (props.sprintId) {
      checkedSprintId.value = props.sprintId;
    } else {
      if (Object.prototype.hasOwnProperty.call(dbData, 'c')) {
        checkedSprintId.value = dbData.c;
      } else {
        checkedSprintId.value = undefined;
      }
    }

    if (props.sprintName) {
      selectedSprint.value = formatData({ id: props.sprintId, name: props.sprintName });
    } else {
      if (Object.prototype.hasOwnProperty.call(dbData, 'd')) {
        selectedSprint.value = dbData.d;
      } else {
        selectedSprint.value = undefined;
      }
    }

    if (Object.prototype.hasOwnProperty.call(dbData, 'e')) {
      checkedTagIds.value = dbData.e || [];
    } else {
      checkedTagIds.value = [];
    }

    if (Object.prototype.hasOwnProperty.call(dbData, 'f')) {
      selectedTags.value = dbData.f || [];
    } else {
      selectedTags.value = [];
    }

    if (Object.prototype.hasOwnProperty.call(dbData, 'g')) {
      evalWorkloadFilter.value = dbData.g || { key: 'evalWorkload', op: 'EQUAL', value: undefined };
    } else {
      evalWorkloadFilter.value = { key: 'evalWorkload', op: 'EQUAL', value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(dbData, 'h')) {
      failNumFilter.value = dbData.h || { key: 'failNum', op: 'EQUAL', value: undefined };
    } else {
      failNumFilter.value = { key: 'failNum', op: 'EQUAL', value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(dbData, 'i')) {
      totalNumFilter.value = dbData.i || { key: 'totalNum', op: 'EQUAL', value: undefined };
    } else {
      totalNumFilter.value = { key: 'totalNum', op: 'EQUAL', value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(dbData, 'j')) {
      targetParentIdFilter.value = dbData.j || { key: 'targetParentId', op: 'EQUAL', value: undefined };
    } else {
      targetParentIdFilter.value = { key: 'targetParentId', op: 'EQUAL', value: undefined };
    }

    if (Object.prototype.hasOwnProperty.call(dbData, 'k')) {
      targetIdFilter.value = dbData.k || { key: 'targetId', op: 'EQUAL', value: undefined };
    } else {
      targetIdFilter.value = { key: 'targetId', op: 'EQUAL', value: undefined };
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
  overdue.value = false;

  sprintSelectVisible.value = false;
  selectedSprint.value = undefined;
  checkedSprintId.value = undefined;

  tagSelectVisible.value = false;
  selectedTags.value = [];
  checkedTagIds.value = [];

  filters.value = [];

  targetParentIdFilter.value = { key: 'targetParentId', op: 'EQUAL', value: undefined };
  targetIdFilter.value = { key: 'targetId', op: 'EQUAL', value: undefined };

  evalWorkloadFilter.value = { key: 'evalWorkload', op: 'EQUAL', value: undefined };
  failNumFilter.value = { key: 'failNum', op: 'EQUAL', value: undefined };
  totalNumFilter.value = { key: 'totalNum', op: 'EQUAL', value: undefined };
};

onMounted(async () => {
  watch([() => dbParamsKey.value, () => dbCountKey.value, () => dbViewModeKey.value], ([key1, key2, key3]) => {
    if (!key1 || !key2 || !key3) {
      return;
    }

    initialize();
  }, { immediate: true });

  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#list')) {
      return;
    }

    const queryString = route.hash.split('?')[1];
    if (!queryString) {
      return;
    }

    const queryParameters = queryString.split('&').reduce((prev, cur) => {
      const [key, value] = cur.split('=');
      prev[key] = value;
      return prev;
    }, {} as { [key: string]: string });

    const { sprintId, sprintName } = queryParameters;
    if (sprintId) {
      checkedSprintId.value = sprintId;
    }

    if (sprintName) {
      selectedSprint.value = formatData({ id: sprintId, name: sprintName });
    }
  }, { immediate: false });

  watch(
    [
      () => filters.value,
      () => overdue.value,
      () => checkedSprintId.value,
      () => checkedTagIds.value.length,
      () => targetParentIdFilter.value,
      () => targetIdFilter.value,
      () => evalWorkloadFilter.value,
      () => failNumFilter.value,
      () => totalNumFilter.value,
      () => selectedMenuMap.value
    ], () => {
      const _filters = filters.value;
      if (!(_filters.length ||
        overdue.value ||
        // !!checkedSprintId.value ||
        // !!checkedTagIds.value.length ||
        !!targetParentIdFilter.value.value ||
        !!targetIdFilter.value.value ||
        !!evalWorkloadFilter.value.value ||
        !!failNumFilter.value.value ||
        !!totalNumFilter.value.value)) {
        selectedMenuMap.value.clear();
        selectedMenuMap.value.set('none', { key: 'none' });

        emit('change', getData());
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

        const status = _filters.find(item => item.key === 'status')?.value;

        const assigneeId = _filters.find(item => item.key === 'assigneeId')?.value;
        if (status && status === 'PENDING' && assigneeId === userId.value) {
          selectedMenuMap.value.set('assigneeId', { key: 'assigneeId' });

          // 删除【待我确认】
          selectedMenuMap.value.delete('confirmorId');
          // 删除【我处理中】
          selectedMenuMap.value.delete('progress');
        } else {
          selectedMenuMap.value.delete('assigneeId');
        }

        if (status && status === 'IN_PROGRESS' && assigneeId === userId.value) {
          selectedMenuMap.value.set('progress', { key: 'progress' });

          // 删除【待我确认】
          selectedMenuMap.value.delete('confirmorId');
          // 删除【待我处理】
          selectedMenuMap.value.delete('assigneeId');
        } else {
          selectedMenuMap.value.delete('progress');
        }

        const confirmorId = _filters.find(item => item.key === 'confirmorId')?.value;
        if (status && status === 'CONFIRMING' && confirmorId === userId.value) {
          selectedMenuMap.value.set('confirmorId', { key: 'confirmorId' });

          // 删除【待我处理】
          selectedMenuMap.value.delete('assigneeId');
          // 删除【我处理中】
          selectedMenuMap.value.delete('progress');
        } else {
          selectedMenuMap.value.delete('confirmorId');
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
          j?: { key: 'targetParentId'; op: string; value: string | undefined; };
          k?: { key: 'targetId'; op: string; value: string | undefined; };
        } = {};
        if (_filters.length) {
          dbData.a = cloneDeep(_filters);
        }

        if (overdue.value) {
          dbData.b = overdue.value;
        }

        if (checkedSprintId.value) {
          dbData.c = checkedSprintId.value;
        }

        if (selectedSprint.value) {
          dbData.d = cloneDeep(selectedSprint.value);
        }

        if (checkedTagIds.value.length) {
          dbData.e = cloneDeep(checkedTagIds.value);
        }

        if (selectedTags.value.length) {
          dbData.f = cloneDeep(selectedTags.value);
        }

        if (evalWorkloadFilter.value.value) {
          dbData.g = cloneDeep(evalWorkloadFilter.value);
        }

        if (failNumFilter.value.value) {
          dbData.h = cloneDeep(failNumFilter.value);
        }

        if (totalNumFilter.value.value) {
          dbData.i = cloneDeep(totalNumFilter.value);
        }

        if (targetParentIdFilter.value.value) {
          dbData.j = cloneDeep(targetParentIdFilter.value);
        }

        if (targetIdFilter.value.value) {
          dbData.k = cloneDeep(targetIdFilter.value);
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
  return btoa(dbBaseKey.value + 'task_analysis');
});

const dbCountKey = computed(() => {
  return btoa(dbBaseKey.value + 'count');
});

const dbViewModeKey = computed(() => {
  return btoa(dbBaseKey.value + 'viewMode');
});

const dbModuleKey = computed(() => {
  return btoa(dbBaseKey.value + 'moduleFlag');
});

const taskType = computed(() => {
  return filters.value.find(item => item.key === 'taskType')?.value;
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

const searchOptions = [
  {
    type: 'input',
    valueKey: 'name',
    placeholder: '查询名称、描述'
  },
  {
    type: 'select-user',
    valueKey: 'createdBy',
    placeholder: '选择添加人',
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: ['添加时间从', '添加时间到'],
    showTime: true
  }
];

const sortMenuItems = [
  {
    key: 'name',
    name: '名称',
    orderSort: 'ASC'
  },
  {
    key: 'createdByName',
    name: '按添加人排序',
    orderSort: 'ASC'
  },
  {
    key: 'createdDate',
    name: '按添加时间排序',
    orderSort: 'ASC'
  }];
</script>
<template>
  <div class="text-3 leading-5">
    <div class="flex items-start justify-between mb-1.5">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>快速查询</span>
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
            <span class="ml-1">添加分析</span>
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
            <span class="ml-1">排序</span>
          </Button>
        </DropdownSort>

        <Button
          class="flex-shrink-0 flex items-center"
          size="small"
          @click="toRefresh">
          <IconRefresh class="text-4 flex-shrink-0" />
          刷新
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
