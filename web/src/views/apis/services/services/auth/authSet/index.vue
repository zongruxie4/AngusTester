<script setup lang="ts">
import { computed, inject, nextTick, onBeforeUnmount, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox, Switch } from 'ant-design-vue';
import { debounce, throttle } from 'throttle-debounce';
import elementResizeDetector from 'element-resize-detector';
import { AuthObjectType, duration, EnumMessage, ProjectPageQuery, SearchCriteria } from '@xcan-angus/infra';
import { Arrow, IconText, Input, NoData, Spin } from '@xcan-angus/vue-ui';
import { apis, services } from '@/api/tester';
import { ApiPermission, ServicesPermission } from '@/enums/enums';
import { ArrayItem, TreeElement } from '../types';

import CheckboxGroup from './CheckboxGroup.vue';

interface Props {
  authObjectId: string | undefined;
  type: AuthObjectType;
  apiPermissions: EnumMessage<ApiPermission>[];
  servicePermissions: EnumMessage<ServicesPermission>[];
}

interface PermissionMapItem {
  id: string;
  creatorFlag: boolean;
  permissions: string[];
}

interface PaginationData {
  pageNo: number;
  total: number;
}

const props = withDefaults(defineProps<Props>(), {
  authObjectId: undefined,
  type: AuthObjectType.USER,
  apiPermissions: () => [],
  servicePermissions: () => []
});

const { t } = useI18n();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const HTTP_METHOD_COLORS = {
  GET: 'color:rgba(30, 136, 229, 1);',
  POST: 'color:rgba(51, 183, 130, 1);',
  DELETE: 'color:rgba(255, 82, 82, 1);',
  PATCH: 'color:rgba(171, 71, 188, 1);',
  PUT: 'color:rgba(255, 167, 38, 1);',
  OPTIONS: 'color:rgba(0, 150, 136, 1);',
  HEAD: 'color:rgba(255, 82, 82, 1);',
  TRACE: 'color:rgba(255, 82, 82, 1);'
};

const ITEM_LINE_HEIGHT = 44;

const erd = elementResizeDetector({ strategy: 'scroll' });
const containerRef = ref();

// Request controllers for aborting requests
let serviceRequestController: AbortController;
let serviceAuthRequestController: AbortController;
let apiRequestController: AbortController;
let apiAuthRequestController: AbortController;

// UI state management
const expandedItems = ref<Set<string>>(new Set<string>());
const openItems = ref<Set<string>>(new Set<string>());
const visibleItems = ref<Set<string>>(new Set<string>());
const loadedApiItems = ref<Set<string>>(new Set<string>());

// Style objects for dynamic styling
const serviceItemStyles = ref<{ [key: string]: { [key: string]: string } }>({});
const apiItemStyles = ref<{ [key: string]: { [key: string]: string } }>({});

// Loading and pagination state
const isLoading = ref(true);
const currentPage = ref(1);
const pageSize = ref(1);
const totalCount = ref(0);
const itemIdList = ref<string[]>([]);
const itemDataMap = ref<{ [key: string]: ArrayItem }>({});
const permissionsMap = ref<{ [key: string]: PermissionMapItem | undefined }>({});

// Loading states for individual items
const itemLoadingStates = ref<{ [key: string]: boolean }>({});
let permissionUpdateStates: { [key: string]: boolean } = {}; // Track permission update operations

let currentProjectId: string | undefined;

const visibleItemList = computed(() => {
  const visibleSet = visibleItems.value;
  return itemIdList.value.filter(item => visibleSet.has(item));
});

const apiCheckboxOptions = computed(() => {
  return props.apiPermissions?.map(item => ({ label: item.message, value: item.value }));
});

const serviceCheckboxOptions = computed(() => {
  return props.servicePermissions?.map(item => ({ label: item.message, value: item.value }));
});

const searchInputValue = ref<string>();
let isSearchMode = false; // Track if currently in search mode

// Pagination data for each project/service's API list
let paginationDataMap: { [key: string]: PaginationData } = {};

const totalPageCount = computed(() => {
  return Math.ceil(totalCount.value / pageSize.value);
});

/**
 * Handle arrow expand/collapse for tree items
 * Manages visibility of child items and loads API data when expanding
 */
const handleArrowToggle = (isOpen: boolean, itemId: string) => {
  const childIds = JSON.parse(JSON.stringify(itemDataMap.value[itemId].children));

  for (let i = 0; i < childIds.length; i++) {
    const childId = childIds[i];
    if (isOpen) {
      visibleItems.value.add(childId);
    } else {
      visibleItems.value.delete(childId);
      if (itemDataMap.value[childId].children?.length) {
        openItems.value.delete(childId);
        childIds.push(...itemDataMap.value[childId].children);
      }
    }
  }

  if (isOpen) {
    openItems.value.add(itemId);
    currentProjectId = itemId;

    // Load API list if not in search mode and not already loaded
    if (!isSearchMode && !loadedApiItems.value.has(itemId)) {
      paginationDataMap[itemId] = { pageNo: 1, total: 0 };
      loadApiListForProject(itemId);
    }
    return;
  }

  openItems.value.delete(itemId);
  currentProjectId = undefined;
};

/**
 * Handle search input change with debounce
 * Resets state and reloads project tree with search criteria
 */
const handleSearchInputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  resetAllState();
  searchInputValue.value = event.target.value.trim();
  loadProjectTree();
});

/**
 * Handle auth enable/disable switch toggle
 * Updates the auth status for the specific item (API or service)
 */
const handleAuthSwitchToggle = async (isEnabled: boolean, itemId: string) => {
  const isApiItem = itemDataMap.value[itemId].isApi;
  itemLoadingStates.value[itemId] = true;

  const [error] = await (isApiItem
    ? apis.enabledAuth({ id: itemId, enabled: isEnabled })
    : services.updateAuthEnabled({ id: itemId, enabled: isEnabled }));

  itemLoadingStates.value[itemId] = false;

  if (error) {
    return;
  }

  itemDataMap.value[itemId].auth = isEnabled;
};

/**
 * Handle select all checkbox change
 * Selects or deselects all available permissions for the item
 */
const handleSelectAllChange = async (event: { target: { checked: boolean } }, itemId: string) => {
  if (permissionUpdateStates[itemId]) {
    return;
  }

  const isChecked = event.target.checked;
  const isApiItem = itemDataMap.value[itemId].isApi;
  let selectedPermissions: string[] = [];

  if (isApiItem) {
    selectedPermissions = isChecked ? apiCheckboxOptions.value.map(item => item.value) : [];
  } else {
    selectedPermissions = isChecked ? serviceCheckboxOptions.value.map(item => item.value) : [];
  }

  await updateItemPermissions(selectedPermissions, itemId);
};

/**
 * Update permissions for a specific item
 * Handles adding, updating, or deleting permissions based on current state
 */
const updateItemPermissions = async (permissions: string[], itemId: string) => {
  if (permissionUpdateStates[itemId]) {
    return;
  }

  const isApiItem = itemDataMap.value[itemId].isApi;
  const existingAuthId = permissionsMap.value[itemId]?.id;

  // Handle permission deletion
  if (!permissions.length) {
    permissionUpdateStates[itemId] = true;
    const [error] = await (isApiItem ? apis.deleteAuth(existingAuthId as string) : services.delAuth(existingAuthId as string));
    permissionUpdateStates[itemId] = false;

    if (error) {
      return;
    }

    permissionsMap.value[itemId] = undefined;
    return;
  }

  // Handle permission update
  if (existingAuthId) {
    permissionUpdateStates[itemId] = true;
    const [error] = await (isApiItem ? apis.updateAuth(existingAuthId, { permissions }) : services.updateAuth(existingAuthId, { permissions }));
    permissionUpdateStates[itemId] = false;

    if (error) {
      return;
    }

    permissionsMap.value[itemId]?.permissions = permissions;
    return;
  }

  // Handle new permission creation
  permissionUpdateStates[itemId] = true;
  const authParams = { permissions, authObjectId: props.authObjectId, authObjectType: props.type };
  const [error, { data = { id: '' } }] = await (isApiItem ? apis.addAuth(itemId, authParams) : services.addAuth(itemId, authParams));
  permissionUpdateStates[itemId] = false;

  if (error) {
    return;
  }

  permissionsMap.value[itemId] = {
    id: data.id,
    creatorFlag: false,
    permissions: permissions
  };
};

/**
 * Get API list parameters for a specific project
 * Returns pagination and permission parameters for API loading
 */
const getApiListParams = (projectId: string) => {
  const params: {
    pageNo: number;
    pageSize: number;
    hasPermission: string;
    admin: boolean;
  } = {
    hasPermission: ApiPermission.GRANT,
    admin: true,
    pageNo: paginationDataMap[projectId].pageNo,
    pageSize: pageSize.value
  };
  return params;
};

/**
 * Load API list for a specific project
 * Handles pagination and adds API items to the tree structure
 */
const loadApiListForProject = async (projectId: string) => {
  if (isLoading.value) {
    return;
  }

  apiRequestController = new AbortController();
  const axiosConfig = {
    signal: apiRequestController.signal
  };

  const params = getApiListParams(projectId);
  isLoading.value = true;
  const [error, { data }] = await services.loadApis({ ...params, id: projectId }, axiosConfig);

  if (error || !data) {
    isLoading.value = false;
    return;
  }

  loadedApiItems.value.add(projectId);
  paginationDataMap[projectId].total = +data.total;

  const apiList = data.list;
  if (!apiList?.length) {
    isLoading.value = false;
    return;
  }

  const projectLevel = itemDataMap.value[projectId].level;
  const apiIds: string[] = [];

  for (let i = 0, len = apiList.length; i < len; i++) {
    const apiId = apiList[i].id;
    apiIds.push(apiId);
    itemDataMap.value[apiId] = { ...apiList[i], isApi: true };
    visibleItems.value.add(apiId);
    apiItemStyles.value[apiId] = {
      paddingLeft: projectLevel > 1 ? ((projectLevel - 1) * 28 + 54 + 'px') : '54px'
    };
  }

  const projectIndex = itemIdList.value.findIndex(item => item === projectId);
  const childCount = itemDataMap.value[projectId].children.reduce((prev, cur) => {
    prev += 1;
    if (itemDataMap.value[cur]?.children?.length) {
      prev += itemDataMap.value[cur].children.reduce((_p) => {
        _p += 1;
        return _p;
      }, 0);
    }
    return prev;
  }, 0);

  itemDataMap.value[projectId].children.push(...apiIds);
  itemIdList.value.splice((projectIndex + childCount + 1), 0, ...apiIds);

  if (apiIds.length) {
    await loadApiPermissions(apiIds);
  } else {
    isLoading.value = false;
  }
};

/**
 * Load API permissions for multiple API items
 * Fetches and processes permission data for the specified API IDs
 */
const loadApiPermissions = async (apiIds: string[]) => {
  const params: {
    pageSize: number;
    authObjectId: string;
    authObjectType: AuthObjectType;
    filters?: SearchCriteria[]
  } = {
    pageSize: 2000, // Large page size for IN lookup
    authObjectId: props.authObjectId!,
    authObjectType: props.type,
    filters: [{ key: 'apisId', op: SearchCriteria.OpEnum.In, value: apiIds }]
  };

  apiAuthRequestController = new AbortController();
  const axiosConfig = {
    signal: apiAuthRequestController.signal
  };

  isLoading.value = true;
  const [error, { data = { list: [] } }] = await apis.getAuthList(params, axiosConfig);

  if (error || !data) {
    isLoading.value = false;
    return;
  }

  const permissionList = data.list || [];
  if (permissionList.length) {
    for (let i = 0, len = permissionList.length; i < len; i++) {
      const { id, apisId, permissions, creatorFlag } = permissionList[i];
      const existingPermissionMap = permissionsMap.value[apisId];

      if (existingPermissionMap) {
        const previousPermissions = existingPermissionMap.permissions;
        const currentPermissions = permissions.map(item => item.value);

        if (currentPermissions.length) {
          previousPermissions.push(...currentPermissions);
        }

        // Remove duplicates and update permissions
        const uniquePermissions = new Set(previousPermissions);
        existingPermissionMap.permissions = Array.from(uniquePermissions);
        existingPermissionMap.creatorFlag = creatorFlag || existingPermissionMap.creatorFlag;
      } else {
        permissionsMap.value[apisId] = {
          id,
          creatorFlag,
          permissions: permissions.map(item => item.value)
        };
      }
    }
  }

  isLoading.value = false;
};

/**
 * Load service permissions for multiple service items
 * Fetches and processes permission data for the specified service IDs
 */
const loadServicePermissions = async (serviceIds: string[]) => {
  const params: {
    pageSize: number;
    authObjectId: string;
    authObjectType: AuthObjectType;
    filters?: SearchCriteria[]
  } = {
    pageSize: 2000, // Large page size for IN lookup
    authObjectId: props.authObjectId!,
    authObjectType: props.type,
    filters: [{ key: 'serviceId', op: SearchCriteria.OpEnum.In, value: serviceIds }]
  };

  serviceAuthRequestController = new AbortController();
  const axiosConfig = {
    signal: serviceAuthRequestController.signal
  };

  isLoading.value = true;
  const [error, { data = { list: [] } }] = await services.loadAuthority(params, axiosConfig);

  if (error || !data) {
    isLoading.value = false;
    return;
  }

  const permissionList = data.list || [];
  if (permissionList.length) {
    for (let i = 0, len = permissionList.length; i < len; i++) {
      const { id, serviceId, permissions, creatorFlag } = permissionList[i];
      const existingPermissionMap = permissionsMap.value[serviceId];

      if (existingPermissionMap) {
        const previousPermissions = existingPermissionMap.permissions;
        const currentPermissions = permissions.map(item => item.value);

        if (currentPermissions.length) {
          previousPermissions.push(...currentPermissions);
        }

        // Remove duplicates and update permissions
        const uniquePermissions = new Set(previousPermissions);
        existingPermissionMap.permissions = Array.from(uniquePermissions);
        existingPermissionMap.creatorFlag = creatorFlag || existingPermissionMap.creatorFlag;
      } else {
        permissionsMap.value[serviceId] = {
          id,
          creatorFlag,
          permissions: permissions.map(item => item.value)
        };
      }
    }
  }

  isLoading.value = false;
};

/**
 * Get service list parameters for project tree loading
 * Returns pagination and search parameters for service loading
 */
const getServiceListParams = () => {
  const params: ProjectPageQuery & {
    queryHasApis: boolean;
    hasPermission: string;
    admin: boolean;
  } = {
    hasPermission: ServicesPermission.VIEW,
    admin: true,
    queryHasApis: true,
    pageNo: currentPage.value,
    pageSize: pageSize.value,
    projectId: projectId.value
  };

  if (searchInputValue.value) {
    params.filters = [{ key: 'name', op: SearchCriteria.OpEnum.Match, value: searchInputValue.value }];
  }
  return params;
};

/**
 * Load project tree structure
 * Fetches service hierarchy and processes tree data for display
 */
const loadProjectTree = async () => {
  if (!props.authObjectId) {
    isLoading.value = false;
    return;
  }

  serviceRequestController = new AbortController();
  const axiosConfig = {
    signal: serviceRequestController.signal
  };

  const params = getServiceListParams();
  isLoading.value = true;
  const [error, { data }] = await services.getList(params, axiosConfig);

  if (error || !data) {
    isLoading.value = false;
    return;
  }

  isSearchMode = !!params.filters?.length;
  totalCount.value = +data.total;

  const processedTreeData = processTreeStructure(data.list);
  const serviceIds: string[] = [];
  const visibleChildren: string[] = [];

  for (let i = 0, len = processedTreeData.length; i < len; i++) {
    const { id, level, children, hasApis } = processedTreeData[i];

    serviceIds.push(id);
    itemDataMap.value[id] = processedTreeData[i];

    // Add arrow for items with children or APIs
    if (children.length || (!isSearchMode && hasApis)) {
      expandedItems.value.add(id);

      if (isSearchMode) {
        openItems.value.add(id);
      }
    }

    // Handle visibility for search mode
    if (openItems.value.has(id) && children.length) {
      visibleChildren.push(...children);
    }

    if (level === 1 || visibleChildren.includes(id)) {
      visibleItems.value.add(id);
    }

    serviceItemStyles.value[id] = {
      paddingLeft: (level - 1) * 28 + 'px'
    };

    itemIdList.value.push(id);
  }

  if (serviceIds.length) {
    await loadServicePermissions(serviceIds);
  } else {
    isLoading.value = false;
  }
};

/**
 * Process tree structure data into flat array format
 * Converts hierarchical tree data to flat array with level and parent information
 */
const processTreeStructure = (treeData: TreeElement[]): ArrayItem[] => {
  if (!treeData?.length) {
    return [];
  }

  const processedItems: ArrayItem[] = [];

  function processTreeItems (treeItems: TreeElement[], resultArray: ArrayItem[], parentId = '-1', level = 1) {
    treeItems.reduce((accumulator, currentItem) => {
      const processedItem: ArrayItem = {
        ...currentItem,
        pid: parentId,
        level,
        children: [],
        targetType: 'SERVICE'
      };

      accumulator.push(processedItem);

      if (currentItem.children?.length) {
        processedItem.children = currentItem.children.map(child => child.id);
        processTreeItems(currentItem.children, accumulator, currentItem.id, processedItem.level + 1);
      }

      return accumulator;
    }, resultArray);
  }

  processTreeItems(treeData, processedItems);
  return processedItems;
};

/**
 * Handle scroll event for infinite loading
 * Manages pagination for both project tree and API lists
 */
const handleScroll = throttle(duration.scroll, (event: Event) => {
  if (isLoading.value) {
    return;
  }

  const scrollTarget = event.target as HTMLElement;

  // Handle API list pagination
  if (currentProjectId && !isSearchMode) {
    let totalCount = paginationDataMap[currentProjectId].total;
    let currentPageNo = paginationDataMap[currentProjectId].pageNo;
    let totalPages = Math.ceil(totalCount / pageSize.value);

    if (currentPageNo >= totalPages) {
      // If current project is fully loaded, try parent project
      const parentId = itemDataMap.value[currentProjectId].pid;
      if (parentId === '-1') {
        currentProjectId = undefined;
        return;
      }

      totalCount = paginationDataMap[parentId].total;
      currentPageNo = paginationDataMap[parentId].pageNo;
      totalPages = Math.ceil(totalCount / pageSize.value);

      if (currentPageNo >= totalPages) {
        currentProjectId = undefined;
        return;
      }

      currentProjectId = parentId;
    }

    const projectIndex = visibleItemList.value.findIndex(item => item === currentProjectId);
    const childCount = itemDataMap.value[currentProjectId].children.reduce((prev, cur) => {
      if (visibleItems.value.has(cur)) {
        prev += 1;
      }

      if (itemDataMap.value[cur]?.children?.length) {
        prev += itemDataMap.value[cur].children.reduce((_p, _c) => {
          if (visibleItems.value.has(_c)) {
            _p += 1;
          }
          return _p;
        }, 0);
      }

      return prev;
    }, 0);

    const projectScrollTop = projectIndex * ITEM_LINE_HEIGHT + (childCount) * ITEM_LINE_HEIGHT;
    if (projectScrollTop <= scrollTarget.scrollTop + scrollTarget.clientHeight) {
      paginationDataMap[currentProjectId].pageNo = currentPageNo + 1;
      loadApiListForProject(currentProjectId);
    }
    return;
  }

  // Handle project tree pagination
  if (currentPage.value >= totalPageCount.value) {
    return;
  }

  if ((scrollTarget.scrollTop + scrollTarget.clientHeight + ITEM_LINE_HEIGHT) >= scrollTarget.scrollHeight) {
    currentPage.value += 1;
    loadProjectTree();
  }
});

/**
 * Handle container resize for dynamic page size calculation
 * Adjusts page size based on container height for optimal loading
 */
const handleContainerResize = debounce(duration.resize, () => {
  if (!containerRef.value) {
    return;
  }

  const containerHeight = containerRef.value.offsetHeight;
  let calculatedPageSize = 1;

  if (containerHeight > ITEM_LINE_HEIGHT) {
    const quotient = containerHeight / ITEM_LINE_HEIGHT;
    if (Number.isInteger(quotient)) {
      calculatedPageSize = quotient + 1;
    } else {
      calculatedPageSize = Math.ceil(quotient);
    }
  }

  if (pageSize.value < calculatedPageSize && visibleItemList.value.length < calculatedPageSize && totalCount.value > visibleItemList.value.length) {
    pageSize.value = calculatedPageSize;
    resetAllState();
    loadProjectTree();
  }
});

/**
 * Reset all component state to initial values
 * Clears all data, aborts requests, and resets UI state
 */
const resetAllState = () => {
  // Abort all ongoing requests
  serviceRequestController && serviceRequestController.abort();
  serviceAuthRequestController && serviceAuthRequestController.abort();
  apiRequestController && apiRequestController.abort();
  apiAuthRequestController && apiAuthRequestController.abort();

  // Clear all sets
  expandedItems.value.clear();
  openItems.value.clear();
  visibleItems.value.clear();
  loadedApiItems.value.clear();

  // Reset style objects
  serviceItemStyles.value = {};
  apiItemStyles.value = {};

  // Reset loading and pagination state
  isLoading.value = false;
  currentPage.value = 1;
  totalCount.value = 0;
  searchInputValue.value = undefined;
  itemIdList.value = [];
  itemDataMap.value = {};
  permissionsMap.value = {};
  itemLoadingStates.value = {};
  permissionUpdateStates = {};

  currentProjectId = undefined;

  // Reset pagination data
  paginationDataMap = {};

  isSearchMode = false;
};

// Lifecycle Hooks
onMounted(() => {
  nextTick(() => {
    const containerHeight = containerRef.value.offsetHeight;
    let initialPageSize = 1;

    if (containerHeight > ITEM_LINE_HEIGHT) {
      const quotient = containerHeight / ITEM_LINE_HEIGHT;
      if (Number.isInteger(quotient)) {
        initialPageSize = quotient + 1;
      } else {
        initialPageSize = Math.ceil(quotient);
      }
    }

    pageSize.value = initialPageSize;
    loadProjectTree();

    erd.listenTo(containerRef.value, handleContainerResize);
  });

  // Watch for auth object ID changes
  watch(() => props.authObjectId, (newAuthObjectId) => {
    resetAllState();
    if (!newAuthObjectId) {
      return;
    }

    loadProjectTree();
  });
});

onBeforeUnmount(() => {
  erd.removeListener(containerRef.value, handleContainerResize);
});
</script>
<template>
  <div style="width: calc(100% - 316px);" class="text-3 leading-4.5 h-full">
    <Input
      :value="searchInputValue"
      :allowClear="true"
      placeholder="查询名称"
      class="mb-2"
      @change="handleSearchInputChange" />
    <div v-if="props.authObjectId" class="flex items-center h-11 pr-1.75 rounded bg-gray-light text-theme-title">
      <div class="flex-1 px-2 truncate">名称</div>
      <div style="width:70px;" class="flex-shrink-0 px-2">权限控制</div>
      <div style="width:52%">权限</div>
    </div>
    <NoData
      v-show="!isLoading && !visibleItemList?.length"
      style="height: calc(100% - 36px);"
      size="small" />
    <Spin
      v-show="isLoading||!!visibleItemList?.length"
      :spinning="isLoading"
      :mask="false"
      :tip="t('service.authSetting.loading.tip')"
      style="height: calc(100% - 76px);">
      <div
        ref="containerRef"
        class="h-full overflow-y-auto"
        @scroll="handleScroll">
        <template v-for="item in visibleItemList" :key="item.id">
          <div
            v-if="itemDataMap[item].isApi"
            class="flex items-center min-h-11 py-1 border-b border-solid cursor-pointer hover:bg-gray-hover border-theme-text-box">
            <div :style="apiItemStyles[item]" class="flex flex-1 overflow-hidden">
              <div :style="HTTP_METHOD_COLORS[itemDataMap[item].method!]" class="inline-block w-11.25 mr-2">{{ itemDataMap[item].method }}</div>
              <div class="truncate" :title="itemDataMap[item].endpoint"><span :data-id="item">{{ itemDataMap[item].endpoint }}</span></div>
            </div>
            <div style="width:70px" class="px-2">
              <Switch
                :loading="itemLoadingStates[item]"
                :checked="itemDataMap[item].auth"
                size="small"
                @change="handleAuthSwitchToggle($event, item)" />
            </div>
            <div style="width:52%" class="flex items-start">
              <Checkbox
                :disabled="permissionsMap[item]?.creatorFlag || itemDataMap[item]?.auth === false"
                :checked="(permissionsMap[item]?.permissions.length === apiCheckboxOptions.length)"
                :indeterminate="!!(permissionsMap[item]?.permissions.length && permissionsMap[item]?.permissions.length! < apiCheckboxOptions.length)"
                class="whitespace-nowrap"
                @change="handleSelectAllChange($event, item)">
                {{ t('actions.selectAll') }}
              </Checkbox>
              <CheckboxGroup
                :disabled="permissionsMap[item]?.creatorFlag || itemDataMap[item]?.auth === false"
                :value="permissionsMap[item]?.permissions"
                :options="apiCheckboxOptions"
                @change="updateItemPermissions($event, item)" />
            </div>
          </div>

          <div
            v-else
            :style="serviceItemStyles[item]"
            class="flex items-center min-h-11 py-1 border-b border-solid cursor-pointer hover:bg-gray-hover border-theme-text-box">
            <div class="flex items-center flex-1 overflow-hidden">
              <div class="flex items-center ml-1.5">
                <div class="flex items-center w-4 h-4">
                  <Arrow
                    v-if="expandedItems.has(item)"
                    :open="openItems.has(item)"
                    @change="handleArrowToggle($event, item)" />
                </div>
                <IconText
                  class="ml-1"
                  style="background-color: #a2dddc;"
                  text="S" />
              </div>
              <div class="flex-1 px-2 pt-0.5 truncate" :title="itemDataMap[item].name">
                <span :data-id="item">{{ itemDataMap[item].name }}</span>
              </div>
            </div>
            <div style="width:70px" class="px-2">
              <Switch
                :loading="itemLoadingStates[item]"
                :checked="itemDataMap[item].auth"
                size="small"
                @change="handleAuthSwitchToggle($event, item)" />
            </div>
            <div style="width:52%" class="flex items-start">
              <Checkbox
                :disabled="permissionsMap[item]?.creatorFlag || itemDataMap[item]?.auth === false"
                :checked="(permissionsMap[item]?.permissions.length === serviceCheckboxOptions.length)"
                :indeterminate="!!(permissionsMap[item]?.permissions.length && permissionsMap[item]?.permissions.length! < serviceCheckboxOptions.length)"
                class="whitespace-nowrap"
                @change="handleSelectAllChange($event, item)">
                {{ t('actions.selectAll') }}
              </Checkbox>
              <CheckboxGroup
                :disabled="permissionsMap[item]?.creatorFlag || itemDataMap[item]?.auth === false"
                :value="permissionsMap[item]?.permissions"
                :options="serviceCheckboxOptions"
                @change="updateItemPermissions($event, item)" />
            </div>
          </div>
        </template>
      </div>
    </Spin>
  </div>
</template>
