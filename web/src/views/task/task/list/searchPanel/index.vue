<script setup lang="ts">
import { computed, onMounted, ref, watch, inject, Ref } from 'vue';
import { useRoute } from 'vue-router';
import { Button, Switch, Tag } from 'ant-design-vue';
import {
  Colon,
  Dropdown,
  DropdownGroup,
  DropdownSort,
  Icon,
  IconCount,
  IconRefresh,
  IconTask,
  IconText,
  Input,
  SearchPanel,
  Select,
  TaskPriority,
  TaskStatus,
  Tooltip
} from '@xcan-angus/vue-ui';
import { TESTER, duration, enumUtils, XCanDexie, Priority, Result } from '@xcan-angus/infra';
import { TaskType, TaskStatus as TaskStatusEnum, TestType } from '@/enums/enums';
import { debounce } from 'throttle-debounce';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';

import SelectEnum from '@/components/SelectEnum/index.vue';
import { MenuItem, SelectOption } from './PropsType';

type Props = {
  collapse: boolean;// 展开、折叠统计
  viewMode: 'table' | 'detail' | 'kanban' | 'gantt';
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
  (e: 'viewModeChange', value: 'table' | 'detail' | 'kabban' | 'gantt'): void;
  (e: 'uploadTask'): void;
  (e: 'exportTemplate'): void;
  (e: 'update:moduleFlag', value: boolean): void;
}>();

const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));
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
const taskTypeList = ref<MenuItem[]>([]);

const loadTaskTypeEnum = () => {
  const data = enumUtils.enumToMessages(TaskType);
  taskTypeList.value = data.map(item => {
    return {
      name: item.message,
      key: item.value
    };
  });
};

const toSort = (data: { orderBy: 'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName'; orderSort: 'DESC' | 'ASC'; }) => {
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
      return;
    }

    if (['REQUIREMENT', 'STORY', 'TASK', 'BUG', 'API_TEST', 'SCENARIO_TEST']) {
      ['REQUIREMENT', 'STORY', 'TASK', 'BUG', 'API_TEST', 'SCENARIO_TEST'].forEach(item => {
        selectedMenuMap.value.delete(item);
      });
      if (typeof searchPanelRef.value?.setConfigs === 'function') {
        searchPanelRef.value.setConfigs([
          { valueKey: 'taskType', value: undefined }
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

  if (['REQUIREMENT', 'STORY', 'TASK', 'BUG', 'API_TEST', 'SCENARIO_TEST'].includes(key)) {
    ['REQUIREMENT', 'STORY', 'TASK', 'BUG', 'API_TEST', 'SCENARIO_TEST'].forEach(item => {
      selectedMenuMap.value.delete(item);
    });
    selectedMenuMap.value.set(key, { key: key });
    if (typeof searchPanelRef.value?.setConfigs === 'function') {
      searchPanelRef.value.setConfigs([
        { valueKey: 'taskType', value: key }
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

const overdueChange = () => {
  selectedMenuMap.value.delete('none');
};

const moduleFlagChange = () => {
  db.add({ id: dbModuleKey.value, data: moduleFlag.value + '' });
  emit('update:moduleFlag', moduleFlag.value);
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

const toSelectSprint = () => {
  sprintSelectVisible.value = true;
};

const sprintChange = (id: string, { name }: { name: string }) => {
  sprintSelectVisible.value = false;

  selectedSprint.value = formatData({ id, name });
  checkedSprintId.value = id;
};

const sprintBlur = () => {
  sprintSelectVisible.value = false;
};

const toggleSprint = () => {
  const id = selectedSprint.value?.id;
  if (checkedSprintId.value === id) {
    checkedSprintId.value = undefined;
    return;
  }

  checkedSprintId.value = id;
};

const deleteSprint = () => {
  selectedSprint.value = undefined;
  checkedSprintId.value = undefined;
};

const toSelectTag = () => {
  tagSelectVisible.value = true;
};

const tagChange = (id: string, { name }: { name: string }) => {
  tagSelectVisible.value = false;

  const ids = selectedTags.value.map(item => item.id);
  if (ids.includes(id)) {
    return;
  }

  selectedTags.value.push(formatData({ id, name }));
  checkedTagIds.value.push(id);
};

const tagBlur = () => {
  tagSelectVisible.value = false;
};

const toggleTag = (data: SelectOption) => {
  const id = data.id;
  if (checkedTagIds.value.includes(id)) {
    checkedTagIds.value = checkedTagIds.value.filter(item => item !== id);
    return;
  }

  checkedTagIds.value.push(id);
};

const deleteTag = (data: SelectOption) => {
  const id = data.id;
  selectedTags.value = selectedTags.value.filter(item => item.id !== id);
  checkedTagIds.value = checkedTagIds.value.filter(item => item !== id);
};

const toCreate = () => {
  emit('add');
};

const buttonDropdownClick = ({ key }: { key: 'import' | 'export' }) => {
  if (key === 'import') {
    emit('uploadTask');
    return;
  }

  if (key === 'export') {
    toExport();
  }
};

const toViewFlowChart = () => {
  emit('update:visible', true);
};

const toExport = () => {
  emit('export');
};

const viewModeChange = (viewMode: 'table' | 'detail' | 'kanban' | 'gantt') => {
  // let viewMode: 'table' | 'detail' | 'kanban' = 'table';
  // if (props.viewMode === 'kanban') {
  //   viewMode = 'table';
  // } else if (props.viewMode === 'table') {
  //   viewMode = 'detail';
  // } else {
  //   viewMode = 'kanban';
  // }

  emit('viewModeChange', viewMode);

  if (db) {
    db.add({ id: dbViewModeKey.value, data: viewMode });
  }
};

const toRefresh = () => {
  emit('change', getData());
};

const toggleCollapse = () => {
  const _collapse = !props.collapse;
  emit('update:collapse', _collapse);

  if (db) {
    db.add({ id: dbCountKey.value, data: _collapse });
  }
};

const searchPanelChange = (data: { key: string; op: string; value: string }[], _headers?: { [key: string]: string }, key?: string) => {
  filters.value = data;

  if (key === 'taskType') {
    // 重置服务、接口、场景
    targetParentIdFilter.value.value = undefined;
    targetIdFilter.value.value = undefined;
  }

  // 选择添加时间清空快速搜索已选中的时间选项
  if (key === 'createdDate') {
    selectedMenuMap.value.delete('lastDay');
    selectedMenuMap.value.delete('lastThreeDays');
    selectedMenuMap.value.delete('lastWeek');
  }
};

const targetIdChange = (value: string) => {
  targetIdFilter.value = { key: 'targetId', op: 'EQUAL', value };
};

const targetParentIdChange = (value: string) => {
  targetParentIdFilter.value = { key: 'targetParentId', op: 'EQUAL', value };
};

const evalWorkloadChange = debounce(duration.search, (event: { target: { value: string }; }) => {
  const value = event.target.value;
  evalWorkloadFilter.value.value = value;
});

const failNumChange = debounce(duration.search, (event: { target: { value: string }; }) => {
  const value = event.target.value;
  failNumFilter.value.value = value;
});

const totalNumChange = debounce(duration.search, (event: { target: { value: string }; }) => {
  const value = event.target.value;
  totalNumFilter.value.value = value;
});

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
  const [, data] = await db.get(dbCountKey.value);
  emit('update:collapse', !!data?.data);

  // 设置任务列表展现形式
  const [, data1] = await db.get(dbViewModeKey.value);
  const viewMode = ['detail', 'table', 'kanban', 'gantt'].includes(data1?.data) ? data1?.data : 'detail';
  emit('viewModeChange', viewMode);

  // 设置是否按模块分组
  const [, dataModule] = await db.get(dbModuleKey.value);
  moduleFlag.value = dataModule?.data === 'true';
  emit('update:moduleFlag', moduleFlag.value);

  // 设置搜索条件数据
  const [, data2] = await db.get(dbParamsKey.value);
  const dbData = data2?.data;
  if (dbData) {
    const valueMap: { [key: string]: string | string[] } = {};
    const taskTypeMap: { [key: string]: string } = {};
    if (Object.prototype.hasOwnProperty.call(dbData, 'a')) {
      filters.value = dbData.a || [];
      const dateTimeKeys = ['createdDate', 'startDate', 'deadlineDate', 'processedDate', 'confirmedDate', 'completedDate', 'canceledDate', 'execDate', 'lastModifiedDate'];
      const taskTypeKeys = ['taskType'];
      const dateTimeMap: { [key: string]: string[] } = {};
      filters.value.every(({ key, value }) => {
        if (dateTimeKeys.includes(key)) {
          if (dateTimeMap[key]) {
            dateTimeMap[key].push(value);
          } else {
            dateTimeMap[key] = [value];
          }
        } else if (taskTypeKeys.includes(key)) {
          taskTypeMap[key] = value;
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

    const configs:{valueKey: string; value: string|string[]}[] = [];
    // 非查询面板的快速筛选
    const taskTypeKeys = Object.values(taskTypeMap);
    if (taskTypeKeys.length) {
      taskTypeKeys.forEach(i => {
        selectedMenuMap.value.set(i, { key: i });
      });
      configs.push({
        valueKey: 'taskType',
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
        configs.push(...searchOptions.map(item => {
          return {
            valueKey: item.valueKey,
            value: valueMap[item.valueKey]
          };
        }));
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
  await loadTaskTypeEnum();
  watch([() => dbParamsKey.value, () => dbCountKey.value, () => dbViewModeKey.value], ([key1, key2, key3]) => {
    if (!key1 || !key2 || !key3) {
      return;
    }

    initialize();
  }, { immediate: true });

  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#task')) {
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

        // 任务类型
        const taskType = _filters.find(item => item.key === 'taskType')?.value;
        if (taskType) {
          selectedMenuMap.value.set(taskType, { key: taskType });
        } else {
          ['REQUIREMENT', 'STORY', 'TASK', 'BUG', 'API_TEST', 'SCENARIO_TEST'].forEach(item => {
            selectedMenuMap.value.delete(item);
          });
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
  return btoa(dbBaseKey.value + 'task');
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

const showAddSprintBtn = computed(() => {
  if (sprintSelectVisible.value) {
    return false;
  }

  if (selectedSprint.value) {
    return false;
  }

  return true;
});

const showAddTagBtn = computed(() => {
  if (tagSelectVisible.value) {
    return false;
  }

  if (selectedTags.value.length >= 3) {
    return false;
  }

  return true;
});

const apiParams = computed(() => {
  return {
    serviceId: targetParentIdFilter.value.value
  };
});

const taskType = computed(() => {
  return filters.value.find(item => item.key === 'taskType')?.value;
});

const isAPITest = computed(() => {
  return taskType.value === 'API_TEST';
});

const isScenarioTest = computed(() => {
  return taskType.value === 'SCENARIO_TEST';
});

// 'table' | 'detail' | 'kanban'
const modeOptions = [
  {
    key: 'detail',
    name: '平铺视图',
    label: ''

  },
  {
    key: 'table',
    name: '列表视图',
    label: ''
  },
  {
    key: 'kanban',
    name: '看板视图',
    label: ''
  },
  {
    key: 'gantt',
    name: '甘特视图',
    label: ''
  }
];

const modeTitle = computed(() => {
  if (props.viewMode === 'kanban') {
    return '看板视图';
  }

  if (props.viewMode === 'detail') {
    return '平铺视图';
  }

  if (props.viewMode === 'table') {
    return '列表视图';
  }

  return '甘特视图';
});

const modeIcon = computed(() => {
  if (props.viewMode === 'kanban') {
    return 'icon-kanbanshitu';
  }

  if (props.viewMode === 'detail') {
    return 'icon-pingpushitu';
  }

  if (props.viewMode === 'table') {
    return 'icon-liebiaoshitu';
  }

  return 'icon-yemianshitu';
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
      key: 'assigneeId',
      name: '待我处理'
    },
    {
      key: 'progress',
      name: '我处理中'
    },
    {
      key: 'confirmorId',
      name: '待我确认'
    },
    ...taskTypeList.value,
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
    type: 'input',
    valueKey: 'name',
    placeholder: '查询任务名称、编码'
  },
  {
    type: 'select-enum',
    valueKey: 'status',
    placeholder: '选择任务状态',
    enumKey: TaskStatusEnum
  },
  // {
  //   type: 'select-enum',
  //   valueKey: 'taskType',
  //   placeholder: '选择任务类型',
  //   enumKey: 'TaskType'
  // },
  {
    type: 'select-enum',
    valueKey: 'priority',
    placeholder: '选择优先级',
    enumKey: Priority
  },
  {
    type: 'select-user',
    valueKey: 'assigneeId',
    placeholder: '选择经办人',
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-user',
    valueKey: 'confirmorId',
    placeholder: '选择确认人',
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-user',
    valueKey: 'execBy',
    placeholder: '选择执行人',
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-user',
    valueKey: 'createdBy',
    placeholder: '选择添加人',
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-user',
    valueKey: 'lastModifiedBy',
    placeholder: '选择最后修改人',
    fieldNames: { label: 'fullName', value: 'id' }
  },
  {
    type: 'select-enum',
    valueKey: 'testType',
    placeholder: '选择测试类型',
    enumKey: TestType
  },
  {
    type: 'select',
    action: `${TESTER}/module?fullTextSearch=true`,
    params: { projectId: props.projectId },
    valueKey: 'moduleId',
    showSearch: true,
    placeholder: '选择所属模块',
    fieldNames: { label: 'name', value: 'id' }
  },
  {
    type: 'select-enum',
    valueKey: 'execResult',
    placeholder: '选择执行结果',
    enumKey: Result
  },
  {
    type: 'select',
    valueKey: 'targetParentId',
    noDefaultSlot: true
  },
  {
    type: 'select',
    valueKey: 'targetId',
    noDefaultSlot: true
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: ['添加时间从', '添加时间到'],
    showTime: true
  },
  {
    type: 'date-range',
    valueKey: 'startDate',
    placeholder: ['开始时间从', '开始时间到'],
    showTime: true
  },
  {
    type: 'date-range',
    valueKey: 'deadlineDate',
    placeholder: ['截止时间从', '截止时间到'],
    showTime: true
  },
  {
    type: 'date-range',
    valueKey: 'processedDate',
    placeholder: ['处理时间从', '处理时间到'],
    showTime: true
  },
  {
    type: 'date-range',
    valueKey: 'confirmedDate',
    placeholder: ['确认时间从', '确认时间到'],
    showTime: true
  },
  {
    type: 'date-range',
    valueKey: 'completedDate',
    placeholder: ['完成时间从', '完成时间到'],
    showTime: true
  },
  {
    type: 'date-range',
    valueKey: 'canceledDate',
    placeholder: ['取消时间从', '取消时间到'],
    showTime: true
  },
  {
    type: 'date-range',
    valueKey: 'execDate',
    placeholder: ['执行时间从', '执行时间到'],
    showTime: true
  },
  {
    type: 'date-range',
    valueKey: 'lastModifiedDate',
    placeholder: ['修改时间从', '修改时间到'],
    showTime: true
  },
  {
    type: 'input',
    valueKey: 'evalWorkload',
    noDefaultSlot: true
  },
  {
    type: 'input',
    valueKey: 'failNum',
    noDefaultSlot: true
  },
  {
    type: 'input',
    valueKey: 'totalNum',
    noDefaultSlot: true
  }
];

const buttonDropdownMenuItems = [
  {
    name: '导出任务',
    key: 'export',
    icon: 'icon-daochu1',
    noAuth: true
  },
  {
    name: '导入任务',
    key: 'import',
    icon: 'icon-shangchuan',
    noAuth: true
  }
];

const EXEC_RESULT_COLOR = {
  FAIL: '#F5222D',
  SUCCESS: '#52C41A'
};

const fieldNames = { label: 'name', value: 'id' };

const groupMenuItems = [
  {
    key: 'none',
    name: '不分组'
  },
  {
    key: 'assigneeName',
    name: '按经办人分组'
  },
  {
    key: 'lastModifiedByName',
    name: '按最后修改人分组'
  },
  {
    key: 'taskType',
    name: '按任务类型分组'
  }
];

const sortMenuItems = [
  {
    key: 'createdByName',
    name: '按添加人排序',
    orderSort: 'ASC'
  },
  {
    key: 'assigneeName',
    name: '按经办人排序',
    orderSort: 'ASC'
  },
  {
    key: 'priority',
    name: '按优先级排序',
    orderSort: 'ASC'
  },
  {
    key: 'deadlineDate',
    name: '按截止时间排序',
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

          <div class="inline-flex items-center mr-3 mb-3 space-x-1">
            <Switch
              v-model:checked="overdue"
              size="small"
              @change="overdueChange">
            </Switch>
            <span>已逾期</span>
          </div>

          <div class="inline-flex items-center mr-3 mb-3 space-x-1">
            <Switch
              v-model:checked="moduleFlag"
              size="small"
              style="margin-left:0;"
              @change="moduleFlagChange">
            </Switch>
            <span>按模块分组</span>
          </div>

          <template v-if="selectedSprint?.id">
            <Tag
              :title="selectedSprint?.showTitle"
              :class="checkedSprintId === selectedSprint.id ? 'sprint tag-checked' : ''"
              closable
              class="h-6 mr-5 mb-3 rounded-xl px-2.5"
              @click="toggleSprint"
              @close="deleteSprint">
              {{ selectedSprint?.showName }}
            </Tag>
          </template>

          <Select
            v-if="sprintSelectVisible && proTypeShowMap.showSprint"
            :value="selectedSprint?.id"
            size="small"
            class="w-43 h-7 transform-gpu -translate-y-0.5 mr-5 mb-3"
            placeholder="选择迭代"
            showSearch
            autofocus
            :fieldNames="fieldNames"
            :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
            @change="sprintChange"
            @blur="sprintBlur" />

          <Button
            v-if="showAddSprintBtn && proTypeShowMap.showSprint"
            class="h-6 mr-5 mb-3 px-2.5 flex items-center"
            size="small"
            @click="toSelectSprint">
            <Icon icon="icon-jia" class="text-3 mr-1" />
            <span>迭代</span>
          </Button>

          <Tag
            v-for="item in selectedTags"
            :key="item.id"
            :title="item.showTitle"
            :class="checkedTagIds.includes(item.id) ? 'tag tag-checked' : ''"
            closable
            class="h-6 mb-3 rounded-xl px-2.5"
            @click="toggleTag(item)"
            @close="deleteTag(item)">
            {{ item.showName }}
          </Tag>

          <Select
            v-if="tagSelectVisible"
            :value="selectedTags"
            size="small"
            class="w-43 h-7 transform-gpu -translate-y-0.5 mb-3 mr-5"
            placeholder="选择标签"
            showSearch
            autofocus
            :fieldNames="fieldNames"
            :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
            @change="tagChange"
            @blur="tagBlur" />

          <Button
            v-if="showAddTagBtn"
            class="h-6 px-2.5 mb-3 flex items-center mr-5"
            size="small"
            @click="toSelectTag">
            <Icon icon="icon-jia" class="text-3 mr-1" />
            <span>标签</span>
          </Button>

          <template v-if="props.viewMode === 'kanban'">
            <DropdownSort
              :orderBy="props.orderBy"
              :orderSort="props.orderSort"
              :menuItems="sortMenuItems"
              @click="toSort">
              <Button
                size="small"
                type="text"
                class="flex items-center px-0 h-5 leading-5 border-0 cursor-pointer mr-5 mb-3">
                <Icon icon="icon-biaotoupaixu" class="text-3.5" />
                <span class="ml-1">排序</span>
              </Button>
            </DropdownSort>

            <DropdownGroup
              :activeKey="props.groupKey"
              :menuItems="groupMenuItems"
              @click="toGroup">
              <Button
                size="small"
                type="text"
                class="flex items-center px-0 h-5 leading-5 border-0 cursor-pointer mr-5 mb-3">
                <Icon icon="icon-fenzu" class="text-3.5" />
                <span class="ml-1">分组</span>
              </Button>
            </DropdownGroup>
          </template>
        </div>
      </div>
    </div>

    <div class="flex items-start justify-between mb-1.5 space-x-4">
      <SearchPanel
        ref="searchPanelRef"
        class="flex-1"
        :options="searchOptions"
        @change="searchPanelChange">
        <template #status_option="record">
          <TaskStatus :value="record" />
        </template>

        <template #moduleId_option="record">
          <div class="flex items-center truncate" :title="record.name">
            <Icon icon="icon-mokuai" class="mr-1 text-4" />
            <div class="truncate">{{ record.name }}</div>
          </div>
        </template>

        <template #taskType_option="record">
          <div class="flex items-center">
            <IconTask :value="record.value" class="text-4 flex-shrink-0" />
            <span class="ml-1.5">{{ record.message }}</span>
          </div>
        </template>

        <template #priority_option="record">
          <TaskPriority :value="record" />
        </template>

        <template #execResult_option="record">
          <span :style="'color:' + EXEC_RESULT_COLOR[record.value]">{{ record.message }}</span>
        </template>

        <template #targetParentId>
          <Select
            v-if="isAPITest"
            :value="targetParentIdFilter.value"
            :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择选择服务"
            class="w-72 ml-2"
            showSearch
            @change="targetParentIdChange">
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

        <template #targetId>
          <Select
            v-if="isAPITest"
            :value="targetIdFilter.value"
            :action="`${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`"
            :params="apiParams"
            :fieldNames="{ label: 'summary', value: 'id' }"
            :allowClear="true"
            placeholder="选择选择接口"
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
            v-if="isScenarioTest"
            :value="targetIdFilter.value"
            :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            placeholder="选择选择场景"
            class="w-72 ml-2"
            showSearch
            @change="targetIdChange">
            <template #option="record">
              <div class="flex items-center">
                <Icon icon="icon-changjing" class="text-4 flex-shrink-0" />
                <div :title="record.name" class="truncate ml-1.5">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </template>

        <template #evalWorkload>
          <Input
            :value="evalWorkloadFilter.value"
            dataType="float"
            allowClear
            :max="100"
            placeholder="工作量"
            class="w-72 ml-2 scope-select"
            @change="evalWorkloadChange">
            <template #prefix>
              <SelectEnum
                v-model:value="evalWorkloadFilter.op"
                :bordered="false"
                enumKey="NumberCompareCondition"
                class="w-26" />
            </template>
          </Input>
        </template>

        <template #failNum>
          <Input
            :value="failNumFilter.value"
            dataType="float"
            allowClear
            :max="100"
            placeholder="失败次数"
            class="w-72 ml-2 scope-select"
            @change="failNumChange">
            <template #prefix>
              <SelectEnum
                v-model:value="failNumFilter.op"
                :bordered="false"
                enumKey="NumberCompareCondition"
                class="w-26" />
            </template>
          </Input>
        </template>

        <template #totalNum>
          <Input
            :value="totalNumFilter.value"
            dataType="float"
            allowClear
            :max="100"
            placeholder="处理次数"
            class="w-72 ml-2 scope-select"
            @change="totalNumChange">
            <template #prefix>
              <SelectEnum
                v-model:value="totalNumFilter.op"
                :bordered="false"
                enumKey="NumberCompareCondition"
                class="w-26" />
            </template>
          </Input>
        </template>
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2">
        <Button
          class="flex-shrink-0 flex items-center pr-0"
          type="primary"
          size="small"
          @click="toCreate">
          <div class="flex items-center">
            <Icon icon="icon-jia" class="text-3.5" />
            <span class="ml-1">添加任务</span>
          </div>
          <Dropdown :menuItems="buttonDropdownMenuItems" @click="buttonDropdownClick">
            <div class="w-5 h-5 flex items-center justify-center">
              <Icon icon="icon-more" />
            </div>
          </Dropdown>
        </Button>

        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          title="任务流程">
          <Icon
            icon="icon-liuchengtu"
            class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0"
            @click="toViewFlowChart" />
        </Tooltip>

        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          :title="modeTitle">
          <Dropdown
            :noAuth="true"
            :value="[props.viewMode]"
            :menuItems="modeOptions"
            @click="viewModeChange($event.key)">
            <div>
              <Icon
                :icon="modeIcon"
                class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0" />
              <Icon icon="icon-xiajiantou" class="ml-1" />
            </div>
          </Dropdown>
        </Tooltip>

        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          :title="props.collapse ? '收起统计' : '查看统计'">
          <IconCount
            :value="!props.collapse"
            class="text-4 flex-shrink-0"
            @click="toggleCollapse" />
        </Tooltip>

        <Tooltip
          arrowPointAtCenter
          placement="topLeft"
          title="刷新">
          <IconRefresh class="text-4 flex-shrink-0" @click="toRefresh" />
        </Tooltip>
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

/* :deep(.mode-select) .ant-select-selector {
  @apply px-1 py-0;
} */

/* :deep(.mode-select .ant-select-selector .ant-select-selection-item){
  @apply hidden;
} */
</style>
