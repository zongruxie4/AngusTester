<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox, Switch } from 'ant-design-vue';
import { debounce, throttle } from 'throttle-debounce';
import elementResizeDetector from 'element-resize-detector';
import { duration } from '@xcan-angus/infra';
import { Icon, IconText, Input, NoData, Spin } from '@xcan-angus/vue-ui';
import { scenario } from '@/api/tester';

import CheckboxGroup from './checkboxGroup.vue';

const { t } = useI18n();

import { DataItem } from './PropsType';

interface Props {
  projectId: string;
  authObjectId: string;
  type: 'user' | 'dept' | 'group';
  permissions: { value: string; label: string }[];
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  authObjectId: undefined,
  type: 'user',
  permissions: () => []
});

const LINE_HEIGHT = 44;

const erd = elementResizeDetector({ strategy: 'scroll' });
const containerRef = ref();
let controller: AbortController;// 用于终止请求项目树的实例
let authController: AbortController;// 用于终止请求权限的实例

const loading = ref(true);
const pageNo = ref(1);
const pageSize = ref(1);
const total = ref(0);
const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: DataItem }>({});
const permissionsMap = ref<{ [key: string]: { id: string; creatorFlag: boolean; permissions: string[] } | undefined }>({});

const enabledLoadingMap = ref<{ [key: string]: boolean }>({});
let updatingMap: { [key: string]: boolean; } = {};// 修改权限时，等待修改接口返回

const searchInputValue = ref<string>();

const totalPage = computed(() => {
  return Math.ceil(total.value / pageSize.value);
});

const searchInputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  reset();
  const value = event.target.value.trim();
  searchInputValue.value = value;

  loadData();
});

const switchChange = async (checked: boolean, id: string) => {
  enabledLoadingMap.value[id] = true;
  const [error] = await scenario.enableScenarioAuth(id, checked);
  enabledLoadingMap.value[id] = false;
  if (error) {
    return;
  }

  if (checked) {
    dataMap.value[id].auth = true;
    return;
  }

  dataMap.value[id].auth = false;
};

const checkAllChange = async (event: { target: { checked: boolean } }, id: string) => {
  if (updatingMap[id]) {
    return;
  }

  const checked = event.target.checked;
  let permissions: string[] = [];
  permissions = checked ? permissionValues.value : [];

  checkChange(permissions, id);
};

const checkChange = async (permissions: string[], id: string) => {
  if (updatingMap[id]) {
    return;
  }

  // 如果permissionMap.value[id].id为undefined，说明是未授权，反之已经授权
  const authId = permissionsMap.value[id]?.id;
  if (!permissions.length) {
    updatingMap[id] = true;
    const [error] = await scenario.deleteScenarioAuth(authId);
    updatingMap[id] = false;
    if (error) {
      return;
    }

    permissionsMap.value[id] = undefined;
    return;
  }

  if (authId) {
    updatingMap[id] = true;
    const [error] = await scenario.putScenarioAuth(authId, { permissions });
    updatingMap[id] = false;
    if (error) {
      return;
    }

    permissionsMap.value[id]!.permissions = permissions;
    return;
  }

  // 没有进行过授权
  updatingMap[id] = true;
  const params = { permissions, authObjectId: props.authObjectId, authObjectType: props.type };
  const [error, { data = { id: '' } }] = await scenario.addScenarioAuth(id, params);
  updatingMap[id] = false;
  if (error) {
    return;
  }

  permissionsMap.value[id] = {
    id: data.id,
    creatorFlag: false,
    permissions: permissions
  };
};

const loadAuths = async (ids: string[]) => {
  const params: {
    pageSize: 2000; // IN查找有可能超过当前ids的长度，按2000传值
    authObjectId: string;
    authObjectType: 'user' | 'dept' | 'group';
    filters?: [{ key: 'scenarioId'; op: 'IN'; value: string[]; }]
  } = {
    pageSize: 2000,
    authObjectId: props.authObjectId!,
    authObjectType: props.type,
    filters: [{ key: 'scenarioId', op: 'IN', value: ids }]
  };

  authController = new AbortController();
  const axiosConfig = {
    signal: authController.signal
  };

  loading.value = true;
  const [error, { data = { list: [] } }] = await scenario.getScenarioAuthList(params, axiosConfig);
  if (error || !data) {
    loading.value = false;
    return;
  }

  const list = data.list || [];
  if (list.length) {
    for (let i = 0, len = list.length; i < len; i++) {
      const { id, scenarioId, permissions, creatorFlag } = list[i];
      const currentMap = permissionsMap.value[scenarioId];
      if (currentMap) {
        const prevPermissions = currentMap.permissions;
        const curPermissions = permissions.map(item => item.value);
        if (curPermissions.length) {
          prevPermissions.push(...curPermissions);
        }

        const set = new Set(prevPermissions);
        const newPermissions = Array.from(set);

        currentMap.permissions = newPermissions;
        currentMap.creatorFlag = creatorFlag || currentMap.permissions;
      } else {
        permissionsMap.value[scenarioId] = {
          id,
          creatorFlag,
          permissions: permissions.map(item => item.value)
        };
      }
    }
  }

  loading.value = false;
};

const getParams = () => {
  const params: {
    pageNo: number;
    pageSize: number;
    projectId: string;
    hasPermission: string;
    admin: boolean;
    filters?: [{ key: 'name'; op: 'MATCH_END'; value: string; }];
  } = {
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    projectId: props.projectId,
    hasPermission: 'GRANT',
    admin: true
  };

  if (searchInputValue.value) {
    params.filters = [{ key: 'name', op: 'MATCH_END', value: searchInputValue.value }];
  }

  return params;
};

const loadData = async () => {
  if (!props.authObjectId) {
    loading.value = false;
    return;
  }

  controller = new AbortController();
  const axiosConfig = {
    signal: controller.signal
  };

  const params = getParams();
  loading.value = true;
  const [error, { data }] = await scenario.getScenarioList(params, axiosConfig);
  if (error || !data) {
    loading.value = false;
    return;
  }

  total.value = +data.total;
  const dataList = data.list;
  const ids: string[] = [];
  for (let i = 0, len = dataList.length; i < len; i++) {
    const { id } = dataList[i];

    ids.push(id);
    dataMap.value[id] = dataList[i];

    idList.value.push(id);
  }

  if (ids.length) {
    loadAuths(ids);
  } else {
    loading.value = false;
  }
};

const handleScroll = throttle(duration.scroll, (event: Event) => {
  if (loading.value) {
    return;
  }

  if (pageNo.value >= totalPage.value) {
    return;
  }

  const target = event.target as HTMLElement;
  if ((target.scrollTop + target.clientHeight + LINE_HEIGHT) >= target.scrollHeight) {
    pageNo.value += 1;
    loadData();
  }
});

const resizeHandler = debounce(duration.resize, () => {
  if (!containerRef.value) {
    return;
  }

  const height = containerRef.value.offsetHeight;
  let _pageSize = 1;
  if (height > LINE_HEIGHT) {
    const quotient = height / (LINE_HEIGHT);
    if (Number.isInteger(quotient)) {
      _pageSize = quotient + 1;
    } else {
      _pageSize = Math.ceil(quotient);
    }
  }

  if (pageSize.value < _pageSize && idList.value.length < _pageSize && total.value > idList.value.length) {
    pageSize.value = _pageSize;
    reset();
    loadData();
  }
});

const reset = () => {
  controller && controller.abort();
  authController && authController.abort();

  loading.value = false;
  pageNo.value = 1;
  total.value = 0;
  searchInputValue.value = undefined;
  idList.value = [];
  dataMap.value = {};
  permissionsMap.value = {};
  enabledLoadingMap.value = {};
  updatingMap = {};
};

onMounted(() => {
  nextTick(() => {
    const height = containerRef.value.offsetHeight;
    let _pageSize = 1;
    if (height > LINE_HEIGHT) {
      const quotient = height / (LINE_HEIGHT);
      if (Number.isInteger(quotient)) {
        _pageSize = quotient + 1;
      } else {
        _pageSize = Math.ceil(quotient);
      }
    }

    pageSize.value = _pageSize;
    loadData();

    erd.listenTo(containerRef.value, resizeHandler);
  });

  watch(() => props.authObjectId, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }

    loadData();
  });
});

onBeforeUnmount(() => {
  erd.removeListener(containerRef.value, resizeHandler);
});

const permissionValues = computed(() => {
  return props.permissions.map(item => item.value);
});
</script>
<template>
  <div style="width: calc(100% - 316px);" class="text-3 leading-4.5 h-full">
    <Input
      :value="searchInputValue"
      :allowClear="true"
      :placeholder="t('scenario.auth.authSet.searchPlaceholder')"
      class="w-75 mb-3.5"
      @change="searchInputChange">
      <template #suffix>
        <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
      </template>
    </Input>
    <div v-if="props.authObjectId" class="flex items-center h-11 pr-1.75 rounded bg-gray-light text-theme-title">
      <div class="flex-1 px-2 truncate">{{ t('scenario.auth.authSet.table.columns.name') }}</div>
      <div style="width:70px;" class="flex-shrink-0 px-2">{{ t('scenario.auth.authSet.table.columns.permissionControl') }}</div>
      <div style="width:52%">{{ t('scenario.auth.authSet.table.columns.permission') }}</div>
    </div>
    <NoData
      v-show="!loading && !idList?.length"
      style="height: calc(100% - 36px);"
      size="small" />
    <Spin
      v-show="loading || !!idList?.length"
      :mask="false"
      :spinning="loading"
      :tip="t('scenario.auth.authSet.loading')"
      style="height: calc(100% - 76px);">
      <div
        ref="containerRef"
        class="h-full overflow-y-auto"
        @scroll="handleScroll">
        <template v-for="item in idList" :key="item.id">
          <div
            class="flex items-center min-h-11 py-1 border-b border-solid cursor-pointer hover:bg-gray-hover border-theme-text-box">
            <div class="flex items-center flex-1 overflow-hidden">
              <IconText class="ml-1.5" />
              <div class="flex-1 px-2 pt-0.5 truncate" :title="dataMap[item].name">
                <span :data-id="item">{{ dataMap[item].name }}</span>
              </div>
            </div>
            <div style="width:70px;" class="px-2">
              <Switch
                :loading="enabledLoadingMap[item]"
                :checked="dataMap[item].auth"
                size="small"
                @change="switchChange($event, item)" />
            </div>
            <div style="width:52%" class="flex items-start">
              <Checkbox
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :checked="!!(permissionsMap[item]?.permissions.length === props.permissions.length)"
                :indeterminate="!!(permissionsMap[item]?.permissions.length && permissionsMap[item]?.permissions.length! < props.permissions.length)"
                class="whitespace-nowrap"
                @change="checkAllChange($event, item)">
                {{ t('scenario.auth.authSet.selectAll') }}
              </Checkbox>
              <CheckboxGroup
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :value="permissionsMap[item]?.permissions"
                :options="props.permissions"
                @change="checkChange($event, item)" />
            </div>
          </div>
        </template>
      </div>
    </Spin>
  </div>
</template>
