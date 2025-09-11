<script setup lang="ts">
import { computed, inject, nextTick, onBeforeUnmount, onMounted, ref, watch, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox, Switch } from 'ant-design-vue';
import { debounce, throttle } from 'throttle-debounce';
import elementResizeDetector from 'element-resize-detector';
import { duration } from '@xcan-angus/infra';
import { Arrow, IconText, Input, NoData, Spin } from '@xcan-angus/vue-ui';
import { apis, services } from '@/api/tester';

import CheckboxGroup from './checkboxGroup.vue';

import { ArrayItem, TreeElement } from './PropsType';

interface Props {
  authObjectId: string | undefined;
  type: 'USER' | 'DEPT' | 'GROUP';
  apiPermissions: { value: string; message: string }[];
  projectPermissions: { value: string; message: string }[];
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  authObjectId: undefined,
  type: 'USER',
  apiPermissions: () => [],
  projectPermissions: () => []
});

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const TEXT_COLOR = {
  GET: 'color:rgba(30, 136, 229, 1);',
  POST: 'color:rgba(51, 183, 130, 1);',
  DELETE: 'color:rgba(255, 82, 82, 1);',
  PATCH: 'color:rgba(171, 71, 188, 1);',
  PUT: 'color:rgba(255, 167, 38, 1);',
  OPTIONS: 'color:rgba(0, 150, 136, 1);',
  HEAD: 'color:rgba(255, 82, 82, 1);',
  TRACE: 'color:rgba(255, 82, 82, 1);'
};

const LINE_HEIGHT = 44;

const erd = elementResizeDetector({ strategy: 'scroll' });
const containerRef = ref();
let projectController: AbortController;// 用于终止请求的实例
let projectAuthController: AbortController;// 用于终止请求的实例
let apiController: AbortController;// 用于终止请求的实例
let apiAuthController: AbortController;// 用于终止请求的实例

const arrowSet = ref<Set<string>>(new Set<string>());
const openSet = ref<Set<string>>(new Set<string>());
const visibleSet = ref<Set<string>>(new Set<string>());
const apiLoadedSet = ref<Set<string>>(new Set<string>());

const projectStyle = ref<{ [key: string]: { [key: string]: string } }>({});
const apiStyle = ref<{ [key: string]: { [key: string]: string } }>({});

const loading = ref(true);
const pageNo = ref(1);
const pageSize = ref(1);
const total = ref(0);
const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: ArrayItem }>({});
const permissionsMap = ref<{ [key: string]: { id: string; creatorFlag: boolean; permissions: string[] } | undefined }>({});

const enabledLoadingMap = ref<{ [key: string]: boolean }>({});
let updatingMap: { [key: string]: boolean; } = {};// 修改权限时，等待修改接口返回

let currentProjectId: string | undefined;

const searchInputValue = ref<string>();
let isSearch = false;// 是否是搜索

// 每个项目、服务下的接口列表的分页器数据
let paginationMap: {
  [key: string]: {
    pageNo: number;
    total: number;
  }
} = {};

const totalPage = computed(() => {
  return Math.ceil(total.value / pageSize.value);
});

const arrowChange = (open: boolean, id: string) => {
  const cids = JSON.parse(JSON.stringify(dataMap.value[id].children));
  for (let i = 0; i < cids.length; i++) {
    const _id = cids[i];
    if (open) {
      visibleSet.value.add(_id);
    } else {
      visibleSet.value.delete(_id);
      if (dataMap.value[_id].children?.length) {
        openSet.value.delete(_id);
        cids.push(...dataMap.value[_id].children);
      }
    }
  }

  if (open) {
    openSet.value.add(id);
    currentProjectId = id;

    if (!isSearch && !apiLoadedSet.value.has(id)) {
      paginationMap[id] = { pageNo: 1, total: 0 };
      loadApiList(id);
    }

    return;
  }

  openSet.value.delete(id);
  currentProjectId = undefined;
};

const searchInputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  reset();
  const value = event.target.value.trim();
  searchInputValue.value = value;

  loadProjectTree();
});

const switchChange = async (checked: boolean, id: string) => {
  const isApi = dataMap.value[id].isApi;
  enabledLoadingMap.value[id] = true;
  const [error] = await (isApi ? apis.enabledAuth({ id, enabled: checked }) : services.updateAuthEnabled({ id, enabled: checked }));
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
  const isApi = dataMap.value[id].isApi;
  let permissions: string[] = [];
  if (isApi) {
    permissions = checked ? apiCheckboxOptions.value.map(item => item.value) : [];
  } else {
    permissions = checked ? projectCheckboxOptions.value.map(item => item.value) : [];
  }

  checkChange(permissions, id);
};

const checkChange = async (permissions: string[], id: string) => {
  if (updatingMap[id]) {
    return;
  }

  const isApi = dataMap.value[id].isApi;
  // 如果permissionMap.value[id].id为undefined，说明是未授权，反之已经授权
  const authId = permissionsMap.value[id]?.id;
  if (!permissions.length) {
    updatingMap[id] = true;
    const [error] = await (isApi ? apis.deleteAuth(authId as string) : services.delAuth(authId as string));
    updatingMap[id] = false;
    if (error) {
      return;
    }

    permissionsMap.value[id] = undefined;
    return;
  }

  if (authId) {
    updatingMap[id] = true;
    const [error] = await (isApi ? apis.updateAuth(authId, { permissions }) : services.updateAuth(authId, { permissions }));
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
  const [error, { data = { id: '' } }] = await (isApi ? apis.addAuth(id, params) : services.addAuth(id, params));
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

const getApiParams = (id: string) => {
  const params: {
    pageNo: number;
    pageSize: number;
    hasPermission: string;
    admin: boolean;
  } = {
    hasPermission: 'GRANT',
    admin: true,
    pageNo: paginationMap[id].pageNo,
    pageSize: pageSize.value
  };

  return params;
};

const loadApiList = async (id: string) => {
  if (loading.value) {
    return;
  }

  apiController = new AbortController();
  const axiosConfig = {
    signal: apiController.signal
  };
  const params = getApiParams(id);
  loading.value = true;
  const [error, { data }] = await services.loadApis({ ...params, id }, axiosConfig);
  if (error || !data) {
    loading.value = false;
    return;
  }

  apiLoadedSet.value.add(id);
  paginationMap[id].total = +data.total;

  const list = data.list;
  if (!list?.length) {
    loading.value = false;
    return;
  }

  const level = dataMap.value[id].level;
  const ids: string[] = [];
  for (let i = 0, len = list.length; i < len; i++) {
    const id = list[i].id;
    ids.push(id);
    dataMap.value[id] = { ...list[i], isApi: true };
    visibleSet.value.add(id);
    apiStyle.value[id] = {
      paddingLeft: level > 1 ? ((level - 1) * 28 + 54 + 'px') : '54px'
    };
  }

  const index = idList.value.findIndex(item => item === id);
  const cidsLen = dataMap.value[id].children.reduce((prev, cur) => {
    prev += 1;
    if (dataMap.value[cur]?.children?.length) {
      prev += dataMap.value[cur].children.reduce((_p) => {
        _p += 1;
        return _p;
      }, 0);
    }

    return prev;
  }, 0);

  dataMap.value[id].children.push(...ids);
  idList.value.splice((index + cidsLen + 1), 0, ...ids);

  if (ids.length) {
    loadApiAuths(ids);
  } else {
    loading.value = false;
  }
};

const loadApiAuths = async (ids: string[]) => {
  const params: {
    pageSize: 2000; // IN查找有可能超过当前ids的长度，按2000传值
    authObjectId: string;
    authObjectType: 'USER' | 'DEPT' | 'GROUP';
    filters?: [{ key: 'apisId'; op: 'IN'; value: string[]; }]
  } = {
    pageSize: 2000,
    authObjectId: props.authObjectId!,
    authObjectType: props.type,
    filters: [{ key: 'apisId', op: 'IN', value: ids }]
  };

  apiAuthController = new AbortController();
  const axiosConfig = {
    signal: apiAuthController.signal
  };

  loading.value = true;
  const [error, { data = { list: [] } }] = await apis.getAuthList(params, axiosConfig);
  if (error || !data) {
    loading.value = false;
    return;
  }

  const list = data.list || [];
  if (list.length) {
    for (let i = 0, len = list.length; i < len; i++) {
      const { id, apisId, permissions, creatorFlag } = list[i];
      const currentMap = permissionsMap.value[apisId];
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
        permissionsMap.value[apisId] = {
          id,
          creatorFlag,
          permissions: permissions.map(item => item.value)
        };
      }
    }
  }

  loading.value = false;
};

const loadProjectAuths = async (ids: string[]) => {
  const params: {
    pageSize: 2000, // IN查找有可能超过当前ids的长度，按2000传值
    authObjectId: string;
    authObjectType: 'USER' | 'DEPT' | 'GROUP';
    filters?: [{ key: 'serviceId'; op: 'IN'; value: string[]; }]
  } = {
    pageSize: 2000,
    authObjectId: props.authObjectId!,
    authObjectType: props.type,
    filters: [{ key: 'serviceId', op: 'IN', value: ids }]
  };

  projectAuthController = new AbortController();
  const axiosConfig = {
    signal: projectAuthController.signal
  };

  loading.value = true;
  const [error, { data = { list: [] } }] = await services.loadAuthority(params, axiosConfig);
  if (error || !data) {
    loading.value = false;
    return;
  }

  const list = data.list || [];
  if (list.length) {
    for (let i = 0, len = list.length; i < len; i++) {
      const { id, serviceId, permissions, creatorFlag } = list[i];
      const currentMap = permissionsMap.value[serviceId];
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
        permissionsMap.value[serviceId] = {
          id,
          creatorFlag,
          permissions: permissions.map(item => item.value)
        };
      }
    }
  }

  loading.value = false;
};

const getProjectTreeParams = () => {
  const params: {
    pageNo: number;
    pageSize: number;
    queryHasApisFlag: boolean;
    hasPermission: string;
    admin: boolean;
    projectId: string;
    filters?: [{ key: 'name'; op: 'MATCH'; value: string; }];
  } = {
    hasPermission: 'GRANT',
    admin: true,
    queryHasApisFlag: true,
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    projectId: projectId.value
  };

  if (searchInputValue.value) {
    params.filters = [{ key: 'name', op: 'MATCH', value: searchInputValue.value }];
  }

  return params;
};

const loadProjectTree = async () => {
  if (!props.authObjectId) {
    loading.value = false;
    return;
  }

  projectController = new AbortController();
  const axiosConfig = {
    signal: projectController.signal
  };
  const params = getProjectTreeParams();
  loading.value = true;
  const [error, { data }] = await services.getList(params, axiosConfig);
  if (error || !data) {
    loading.value = false;
    return;
  }

  isSearch = !!params.filters?.length;
  total.value = +data.total;

  const temp = traverseTree(data.list);
  const ids: string[] = [];
  const visibleChildren:string[] = [];
  for (let i = 0, len = temp.length; i < len; i++) {
    const { id, level, children, hasApis } = temp[i];

    ids.push(id);
    dataMap.value[id] = temp[i];

    if (children.length || (!isSearch && hasApis)) {
      arrowSet.value.add(id);

      if (isSearch) {
        openSet.value.add(id);
      }
    }

    if (openSet.value.has(id) && children.length) {
      visibleChildren.push(...children);
    }

    if (level === 1 || visibleChildren.includes(id)) {
      visibleSet.value.add(id);
    }

    projectStyle.value[id] = {
      paddingLeft: (level - 1) * 28 + 'px'
    };

    idList.value.push(id);
  }

  if (ids.length) {
    loadProjectAuths(ids);
  } else {
    loading.value = false;
  }
};

const traverseTree = (treeData: TreeElement[]): ArrayItem[] => {
  if (!treeData?.length) {
    return [];
  }

  const temp: ArrayItem[] = [];
  function handler (data: TreeElement[], target: ArrayItem[], pid = '-1', level = 1) {
    data.reduce((prev, cur) => {
      const temp: ArrayItem = {
        ...cur,
        pid,
        level,
        children: [],
        targetType: 'SERVICE'
      };

      prev.push(temp);

      if (cur.children?.length) {
        temp.children = cur.children.map(item => item.id);
        handler(cur.children, prev, cur.id, temp.level + 1);
      }

      return prev;
    }, target);
  }

  handler(treeData, temp);
  return temp;
};

const handleScroll = throttle(duration.scroll, (event: Event) => {
  if (loading.value) {
    return;
  }

  const target = event.target as HTMLElement;

  // 当前滚动的是api列表
  if (currentProjectId && !isSearch) {
    let _total = paginationMap[currentProjectId].total;
    let _pageNo = paginationMap[currentProjectId].pageNo;
    let _totalPage = Math.ceil(_total / pageSize.value);
    if (_pageNo >= _totalPage) {
      // 如果有父级，滚动加载其父级数据
      const pid = dataMap.value[currentProjectId].pid;
      if (pid === '-1') {
        currentProjectId = undefined;
        return;
      }

      _total = paginationMap[pid].total;
      _pageNo = paginationMap[pid].pageNo;
      _totalPage = Math.ceil(_total / pageSize.value);
      if (_pageNo >= _totalPage) {
        currentProjectId = undefined;
        return;
      }

      currentProjectId = pid;
    }

    const index = showIdList.value.findIndex(item => item === currentProjectId);
    const cidsLen = dataMap.value[currentProjectId].children.reduce((prev, cur) => {
      if (visibleSet.value.has(cur)) {
        prev += 1;
      }

      if (dataMap.value[cur]?.children?.length) {
        prev += dataMap.value[cur].children.reduce((_p, _c) => {
          if (visibleSet.value.has(_c)) {
            _p += 1;
          }
          return _p;
        }, 0);
      }

      return prev;
    }, 0);

    const projectClientTop = index * LINE_HEIGHT + (cidsLen) * LINE_HEIGHT;
    if (projectClientTop <= target.scrollTop + target.clientHeight) {
      paginationMap[currentProjectId].pageNo = _pageNo + 1;
      loadApiList(currentProjectId);
    }

    return;
  }

  // 当前滚动的是项目服务树列表
  if (pageNo.value >= totalPage.value) {
    return;
  }

  if ((target.scrollTop + target.clientHeight + LINE_HEIGHT) >= target.scrollHeight) {
    pageNo.value += 1;
    loadProjectTree();
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

  if (pageSize.value < _pageSize && showIdList.value.length < _pageSize && total.value > showIdList.value.length) {
    pageSize.value = _pageSize;
    reset();
    loadProjectTree();
  }
});

const reset = () => {
  projectController && projectController.abort();
  projectAuthController && projectAuthController.abort();
  apiController && apiController.abort();
  apiAuthController && apiAuthController.abort();

  arrowSet.value.clear();
  openSet.value.clear();
  visibleSet.value.clear();
  apiLoadedSet.value.clear();

  projectStyle.value = {};
  apiStyle.value = {};

  loading.value = false;
  pageNo.value = 1;
  total.value = 0;
  searchInputValue.value = undefined;
  idList.value = [];
  dataMap.value = {};
  permissionsMap.value = {};
  enabledLoadingMap.value = {};
  updatingMap = {};

  currentProjectId = undefined;

  // 每个项目、服务下的接口列表的分页器数据
  paginationMap = {};

  isSearch = false;
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
    loadProjectTree();

    erd.listenTo(containerRef.value, resizeHandler);
  });

  watch(() => props.authObjectId, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }

    loadProjectTree();
  });
});

onBeforeUnmount(() => {
  erd.removeListener(containerRef.value, resizeHandler);
});

const showIdList = computed(() => {
  const set = visibleSet.value;
  return idList.value.filter(item => set.has(item));
});

const apiCheckboxOptions = computed(() => {
  return props.apiPermissions?.map(item => ({ label: item.message, value: item.value }));
});

const projectCheckboxOptions = computed(() => {
  return props.projectPermissions?.map(item => ({ label: item.message, value: item.value }));
});
</script>
<template>
  <div style="width: calc(100% - 316px);" class="text-3 leading-4.5 h-full">
    <Input
      :value="searchInputValue"
      :allowClear="true"
      placeholder="查询名称"
      class="mb-2"
      @change="searchInputChange" />
    <div v-if="props.authObjectId" class="flex items-center h-11 pr-1.75 rounded bg-gray-light text-theme-title">
      <div class="flex-1 px-2 truncate">名称</div>
      <div style="width:70px;" class="flex-shrink-0 px-2">权限控制</div>
      <div style="width:52%">权限</div>
    </div>
    <NoData
      v-show="!loading && !showIdList?.length"
      style="height: calc(100% - 36px);"
      size="small" />
    <Spin
      v-show="loading||!!showIdList?.length"
      :spinning="loading"
      :mask="false"
      :tip="t('service.authSetting.loading.tip')"
      style="height: calc(100% - 76px);">
      <div
        ref="containerRef"
        class="h-full overflow-y-auto"
        @scroll="handleScroll">
        <template v-for="item in showIdList" :key="item.id">
          <div
            v-if="dataMap[item].isApi"
            class="flex items-center min-h-11 py-1 border-b border-solid cursor-pointer hover:bg-gray-hover border-theme-text-box">
            <div :style="apiStyle[item]" class="flex flex-1 overflow-hidden">
              <div :style="TEXT_COLOR[dataMap[item].method!]" class="inline-block w-11.25 mr-2">{{ dataMap[item].method }}</div>
              <div class="truncate" :title="dataMap[item].endpoint"><span :data-id="item">{{ dataMap[item].endpoint }}</span></div>
            </div>
            <div style="width:70px" class="px-2">
              <Switch
                :loading="enabledLoadingMap[item]"
                :checked="dataMap[item].auth"
                size="small"
                @change="switchChange($event, item)" />
            </div>
            <div style="width:52%" class="flex items-start">
              <Checkbox
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :checked="!!(permissionsMap[item]?.permissions.length === apiCheckboxOptions.length)"
                :indeterminate="!!(permissionsMap[item]?.permissions.length && permissionsMap[item]?.permissions.length! < apiCheckboxOptions.length)"
                class="whitespace-nowrap"
                @change="checkAllChange($event, item)">
                {{ t('service.authSetting.actions.selectAll') }}
              </Checkbox>
              <CheckboxGroup
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :value="permissionsMap[item]?.permissions"
                :options="apiCheckboxOptions"
                @change="checkChange($event, item)" />
            </div>
          </div>

          <div
            v-else
            :style="projectStyle[item]"
            class="flex items-center min-h-11 py-1 border-b border-solid cursor-pointer hover:bg-gray-hover border-theme-text-box">
            <div class="flex items-center flex-1 overflow-hidden">
              <div class="flex items-center ml-1.5">
                <div class="flex items-center w-4 h-4">
                  <Arrow
                    v-if="arrowSet.has(item)"
                    :open="openSet.has(item)"
                    @change="arrowChange($event, item)" />
                </div>
                <IconText
                  v-if="dataMap[item].targetType==='PROJECT'"
                  class="ml-1"
                  text="P" />
                <IconText
                  v-else
                  class="ml-1"
                  style="background-color: #a2dddc;"
                  text="S" />
              </div>
              <div class="flex-1 px-2 pt-0.5 truncate" :title="dataMap[item].name">
                <span :data-id="item">{{ dataMap[item].name }}</span>
              </div>
            </div>
            <div style="width:70px" class="px-2">
              <Switch
                :loading="enabledLoadingMap[item]"
                :checked="dataMap[item].auth"
                size="small"
                @change="switchChange($event, item)" />
            </div>
            <div style="width:52%" class="flex items-start">
              <Checkbox
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :checked="!!(permissionsMap[item]?.permissions.length === projectCheckboxOptions.length)"
                :indeterminate="!!(permissionsMap[item]?.permissions.length && permissionsMap[item]?.permissions.length! < projectCheckboxOptions.length)"
                class="whitespace-nowrap"
                @change="checkAllChange($event, item)">
                {{ t('service.authSetting.actions.selectAll') }}
              </Checkbox>
              <CheckboxGroup
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :value="permissionsMap[item]?.permissions"
                :options="projectCheckboxOptions"
                @change="checkChange($event, item)" />
            </div>
          </div>
        </template>
      </div>
    </Spin>
  </div>
</template>
