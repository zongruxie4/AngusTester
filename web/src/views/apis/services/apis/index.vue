<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, provide, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Spin, TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline, Drawer, Icon, notification } from '@xcan-angus/vue-ui';
import { cookieUtils, DomainManager, appContext, enumUtils, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { ApiPermission, ServicesPermission } from '@/enums/enums';

import store from '@/store';
import { apis, services } from '@/api/tester';
import { navs, serviceNav, socketNavs, StateType, DrawerType } from './PropsType';

const { t } = useI18n();

// Home page components (displayed when no data is available)
const HomePage = defineAsyncComponent(() => import('@/views/apis/services/apis/home/index.vue'));
const QuickStarted = defineAsyncComponent(() => import('@/views/apis/services/apis/home/DefaultQuickStarted.vue'));

// Main tab components (displayed when data is available)
const ApisHeader = defineAsyncComponent(() => import('@/views/apis/services/apis/header/Header.vue'));
const ApisList = defineAsyncComponent(() => import('@/views/apis/services/apis/list/index.vue'));
const ServiceTestInfo = defineAsyncComponent(() => import('@/views/apis/services/apis/slider/services/test/index.vue'));
const MockService = defineAsyncComponent(() => import('@/views/apis/services/apis/slider/services/mock/MockService.vue'));
const OpenApiDocument = defineAsyncComponent(() => import('@/views/apis/services/apis/oas/Doc.vue'));

// Service drawer components (displayed when service is selected)
const ServiceInfo = defineAsyncComponent(() => import('@/views/apis/services/apis/slider/services/Info.vue'));
const OASInfo = defineAsyncComponent(() => import('@/views/apis/services/apis/slider/services/OASInfo.vue'));
const SyncConfig = defineAsyncComponent(() => import('@/views/apis/services/components/sync/SyncConfig.vue'));
const SecurityConfig = defineAsyncComponent(() => import('@/views/apis/services/components/security/SecurityConfig.vue'));
const ServerConfig = defineAsyncComponent(() => import('@/views/apis/services/components/server/ServerConfig.vue'));
const RequestProxy = defineAsyncComponent(() => import('@/views/apis/services/components/RequestProxy.vue'));
const OASTag = defineAsyncComponent(() => import('@/views/apis/services/components/OASTag.vue'));
const OASComponent = defineAsyncComponent(() => import('@/views/apis/services/components/oas/Component.vue'));

// API drawer components (displayed when API is selected)
const ApiInfo = defineAsyncComponent(() => import('@/views/apis/services/apis/slider/apis/Info.vue'));
const Indicator = defineAsyncComponent(() => import('@/components/Indicator/index.vue'));
const HttpTestInfo = defineAsyncComponent(() => import('@/components/HttpTestInfo/index.vue'));
const TestCase = defineAsyncComponent(() => import('@/views/apis/services/components/case/index.vue'));
const CodeSnippet = defineAsyncComponent(() => import('@/views/apis/services/components/CodeSnippet.vue'));
const MockApi = defineAsyncComponent(() => import('@/views/apis/services/apis/slider/apis/MockApi.vue'));
const ShareList = defineAsyncComponent(() => import('@/components/share/list.vue'));
const WebSocketConfig = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/Config.vue'));

interface Props {
  serviceId: string;
  serviceInfo?: any;
}

const props = withDefaults(defineProps<Props>(), {
  serviceId: '',
  serviceInfo: {}
});

// UI state management
const showQuickStarted = ref(false); // Show quick start when service doesn't exist
const pageType = ref<'default' | 'success' | undefined>('success');
const loading = ref(false);
const viewMode = ref<'code' | 'UI'>('UI');

// Drawer and navigation state
const drawerRef = ref();
const activeDrawerKey = ref();
const tabActiveKey = ref<'doc' | 'api'>('api');

// Permission management
const apiAuths = ref<ApiPermission[]>([]);
const serviceAuths = ref<ServicesPermission[]>([]);

// Local storage keys for persistence
const localGroupBy = localStorage.getItem(`${props.serviceId}_groupBy`);
const localOrder = localStorage.getItem(`${props.serviceId}_order`);

// Data management
const scrollListData = ref<any[]>([]);
const apiListRef = ref();
const openapiRef = ref();
const currentApi = ref(); // Currently selected API

// Main application state
const mainState = reactive<StateType>({
  drawerComp: [],
  allData: [],
  dataSource: [],
  id: '', // API ID
  name: '',
  searchKeyword: '',
  order: localOrder ? JSON.parse(localOrder) : { orderBy: undefined, orderSort: undefined },
  serviceId: '',
  showGroupList: !!(localGroupBy && localGroupBy !== ''),
  groupedBy: localStorage.getItem(`${props.serviceId}_groupBy`) || '',
  type: undefined,
  serviceAuth: false
});

// Global state updates from parent components
const updateApi = inject('updateApi', {
  reloadKey: 0,
  reloadId: ''
});

const updateHosts = inject('updateHosts', { reloadKey: 0, reloadId: '' });

const isAdmin = computed(() => appContext.isAdmin());

// Current API test configuration flags
const currentAPITest = computed(() => {
  if (!currentApi.value) return undefined;

  const { testFunc, testStability, testPerf } = currentApi.value;

  // Check if API has test configuration properties
  if (testFunc !== undefined && testStability !== undefined && testPerf !== undefined) {
    return {
      testFunc,
      testStability,
      testPerf
    };
  }
  return undefined;
});

// Search parameters for API filtering
const searchParams = computed(() => {
  const filters = mainState.name
    ? [{ key: 'summary', op: SearchCriteria.OpEnum.Match, value: mainState.name.trim() }]
    : [];

  return {
    filters,
    ...mainState.order,
    pageSize: 200
  };
});

// Current user permissions based on context
const useAuth = computed(() => {
  return mainState.id ? apiAuths.value : serviceAuths.value;
}) as any;

// Current type for drawer components
const type = computed(() => {
  return mainState.type === 'WEBSOCKET' ? 'API' : (mainState.type || 'SERVICE');
});

/**
 * Load APIs for the current service
 * Fetches API list with pagination, filtering, and sorting
 */
const loadApis = async () => {
  const params: any = {
    pageNo: 1,
    pageSize: store.state.maxPageSize,
    id: props.serviceId,
    filters: [],
    infoScope: PageQuery.InfoScope.DETAIL,
    ...mainState.order
  };

  // Add search filter if name is provided
  if (mainState.name) {
    const keyword = mainState.name.trim();
    params.filters.push({ key: 'summary', op: SearchCriteria.OpEnum.Match, value: keyword });
  }

  loading.value = true;
  const [error, { data = {} }] = await services.loadApis(params);

  if (error) {
    loading.value = false;
    return;
  }

  mainState.allData = data.list || [];
  setDataSource();

  // Add small delay for better UX
  setTimeout(() => {
    loading.value = false;
  }, 100);
};

/**
 * Set data source for display
 * Currently just copies allData to dataSource
 */
const setDataSource = () => {
  mainState.dataSource = mainState.allData;
};

/**
 * Update state with new configuration
 * @param config - Configuration object to merge into state
 */
const updateState = (config: Record<string, any> = {}) => {
  Object.keys(config).forEach((key) => {
    mainState[key] = config[key];
  });
};

/**
 * Update scroll list data (used by virtual scrolling)
 * @param data - The new scroll data
 */
const changeScrollDataList = (data: any[]) => {
  scrollListData.value = data;
};

/**
 * Update API data in the current data source
 * TODO: state.dataSource is empty, currently using scrollListData as fallback
 * @param data - Object containing API id and auth status
 */
const updateApiData = (data: { id: string; auth: boolean; }) => {
  const dataSource = scrollListData.value;
  const { id: targetId, auth } = data;

  // Find and update the specific API in the data source
  for (let i = 0, len = dataSource.length; i < len; i++) {
    if (dataSource[i].id === targetId) {
      dataSource[i].auth = auth;
      break;
    }
  }
};

/**
 * Refresh data based on current view mode
 * Uses different refresh strategies for grouped vs ungrouped views
 */
const refresh = () => {
  if (mainState.showGroupList) {
    // Grouped view: reload all APIs
    loadApis();
  } else {
    // Ungrouped view: update virtual scroll list
    if (!apiListRef.value) {
      pageType.value = 'success';
      setTimeout(() => {
        apiListRef.value.updateScrollList();
      });
    } else {
      apiListRef.value.updateScrollList();
    }
  }
};

/**
 * Toggle between UI and code view mode
 */
const viewModeChange = () => {
  viewMode.value = viewMode.value === 'UI' ? 'code' : 'UI';
};

/**
 * Refresh OpenAPI documentation
 */
const refreshDoc = () => {
  if (typeof openapiRef.value?.getData === 'function') {
    openapiRef.value.loadData();
  }
};

/**
 * Handle grouping change
 * @param groupKey - The new grouping key
 */
const grouped = (groupKey: string) => {
  if (groupKey !== mainState.groupedBy) {
    mainState.groupedBy = groupKey;
    mainState.id = ''; // Clear selected API when grouping changes
  }
};

/**
 * Load project/service level permissions
 * Admin users have all permissions, others need to fetch from API
 */
const getServiceAuth = async () => {
  if (isAdmin.value) {
    serviceAuths.value = enumUtils.getEnumValues(ServicesPermission);
    setServiceDrawerComp();
    return;
  }

  const [error, resp] = await services.getCurrentAuth(mainState.serviceId);
  if (error) {
    return;
  }

  mainState.serviceAuth = resp.data.serviceAuth;
  if (mainState.serviceAuth) {
    serviceAuths.value = (resp.data?.permissions || []).map(i => i.value);
  }

  setServiceDrawerComp();
};

/**
 * Load API level permissions for the currently selected API
 */
const getApiAuth = async () => {
  if (isAdmin.value) {
    apiAuths.value = enumUtils.getEnumValues(ApiPermission);
    return;
  }

  const [error, { data = [] }] = await apis.getCurrentAuth(mainState.id);
  if (error) {
    return;
  }

  const auths = (data.permissions || []).map(m => m.value);
  apiAuths.value = auths;
};

/**
 * Set project drawer components based on service navigation
 */
const setServiceDrawerComp = () => {
  mainState.drawerComp = serviceNav.map((i: any) => ({
    ...i,
    key: i.value
  }));
};

/**
 * Open configuration drawer
 * @param value - The drawer key to open
 */
const handleConfig = (value: string) => {
  drawerRef.value?.open(value);
};

/**
 * Open mock configuration drawer
 */
const openMock = () => {
  drawerRef.value?.open(DrawerType.API_MOCK);
};

/**
 * Open any drawer by key
 * @param key - The drawer key to open
 */
const openDrawer = (key: string) => {
  drawerRef.value?.open(key);
};

// Watch for search name changes
watch(() => mainState.name, () => {
  mainState.id = ''; // Clear selected API when searching
  refresh();
});

// Watch for order changes (deep watch for nested object)
watch(() => mainState.order, () => {
  mainState.id = ''; // Clear selected API when sorting changes
  refresh();
}, {
  deep: true
});

// Watch for service type changes to update drawer components
watch(() => mainState.type, () => {
  if (mainState.type === 'WEBSOCKET') {
    mainState.drawerComp = socketNavs.map(i => ({ ...i, key: i.value }));
  } else if (mainState.type === 'API') {
    mainState.drawerComp = navs.map(i => ({ ...i, key: i.value }));
  }
});

// Watch for API updates from parent components
watch(() => updateApi.reloadKey, () => {
  if (updateApi.reloadId === props.serviceId || updateApi.reloadId === 'all') {
    refresh();
    updateHosts.reloadKey++;
    updateHosts.reloadId = props.serviceId as string;
  }
});

// Watch for service ID changes
watch(() => props.serviceId, (newValue) => {
  mainState.serviceId = newValue as string;
}, {
  immediate: true
});

// Watch for type changes to close drawer
watch(() => mainState.type, () => {
  drawerRef.value?.close();
});

// Watch for API selection changes
watch(() => mainState.id, async (newValue, oldValue) => {
  if (newValue) {
    // Close drawer if switching from no selection to selection
    if (!oldValue) {
      drawerRef.value?.close();
    }

    // Find the selected API from available data sources
    const selectApi = mainState.allData.find(api => api.id === mainState.id) ||
                     scrollListData.value.find(api => api.id === mainState.id);
    currentApi.value = selectApi;

    // Load API permissions if needed
    if (selectApi?.serviceAuth && selectApi?.auth) {
      if (!isAdmin.value) {
        await getApiAuth();
      }
    } else {
      // Admin users or APIs without auth restrictions get all permissions
      apiAuths.value = enumUtils.getEnumValues(ApiPermission);
    }
  } else {
    // No API selected, close drawer and show service-level components
    drawerRef.value?.close();
    setServiceDrawerComp();
  }
});

// Watch for user info changes to load permissions
watch(() => props.serviceId, newValue => {
  if (newValue) {
    getServiceAuth();
  }
}, {
  immediate: true,
  deep: true
});

// Watch for group list visibility changes
watch(() => mainState.showGroupList, () => {
  if (mainState.showGroupList && !mainState.allData.length) {
    loadApis();
  }
}, {
  immediate: true
});

const accessToken = ref();
const docOrigin = ref();

/**
 * Component mounted lifecycle
 * Initialize component state and check service existence
 */
onMounted(async () => {
  // Check if service exists when shouldCheckId is true
  if (props.serviceInfo.shouldCheckId) {
    const [error] = await services.loadInfo(props.serviceInfo?.id);
    if (error) {
      showQuickStarted.value = true;
      notification.warning(t('service.sidebar.apiGroup.messages.serviceNotExist'));
    }
  }

  // Initialize access token and document origin
  accessToken.value = cookieUtils.getTokenInfo().access_token;
  docOrigin.value = DomainManager.getInstance().getApiDomain('tester');
});

// Provide reactive state and methods to child components
provide('mainState', mainState); // Current state for API list to modify selected API ID
provide('changeGroupState', updateState); // Method to update group state
provide('id', computed(() => mainState.id)); // Current selected API ID
provide('serviceId', computed(() => mainState.serviceId)); // Current service ID
provide('loadApis', refresh); // Method to refresh API list
provide('apiAuths', apiAuths); // API-level permissions
provide('serviceAuths', serviceAuths); // Service-level permissions
provide('apiBaseInfo', ref({ serviceId: props.serviceId })); // Base API information
</script>
<template>
  <template v-if="showQuickStarted">
    <QuickStarted />
  </template>
  <div v-else class="w-full flex justify-end h-full relative">
    <Spin
      :delay="0"
      :spinning="loading"
      class="absolute w-full h-full bg-white opacity-40 leading-full z-9 pt-60">
    </Spin>
    <template v-if="pageType === 'success'">
      <div class="flex-1 flex flex-col pl-5 py-3 min-w-0">
        <Tabs v-model:activeKey="tabActiveKey" class="flex-1">
          <template v-if="tabActiveKey === 'doc'" #rightExtra>
            <div class="flex items-center space-x-3.5 pr-5">
              <Button
                v-if="viewMode === 'UI'"
                type="link"
                size="small"
                @click="viewModeChange">
                <div class="flex items-center space-x-1">
                  <Icon icon="icon-daimashitu" class="text-3.5" />
                  <span>{{ t('views.codeView') }}</span>
                </div>
              </Button>
              <Button
                v-else-if="viewMode === 'code'"
                type="link"
                size="small"
                @click="viewModeChange">
                <div class="flex items-center space-x-1">
                  <Icon icon="icon-yemianshitu" class="text-3.5" />
                  <span>{{ t('views.pageView') }}</span>
                </div>
              </Button>
              <Button
                type="link"
                size="small"
                @click="refreshDoc">
                <div class="flex items-center space-x-1">
                  <Icon icon="icon-shuaxin" class="text-3.5" />
                  <span>{{ t('actions.refresh') }}</span>
                </div>
              </Button>
            </div>
          </template>
          <TabPane
            key="api"
            :tab="t('common.api')"
            class="flex flex-col">
            <ApisHeader
              v-model:searchKeyword="mainState.searchKeyword"
              v-model:orderBy="mainState.order.orderBy"
              v-model:orderSort="mainState.order.orderSort"
              v-model:serviceId="mainState.serviceId"
              v-model:showGroupList="mainState.showGroupList"
              v-model:loading="loading"
              :serviceName="props.serviceInfo.name"
              :serviceAuths="serviceAuths"
              :groupBy="mainState.groupedBy"
              :disabled="!serviceAuths.includes(ServicesPermission.GRANT)"
              class="pr-5 mb-3.5"
              @loadApis="refresh"
              @grouped="grouped"
              @config="handleConfig" />
            <ApisList
              ref="apiListRef"
              v-model:pageType="pageType"
              v-model:spinning="loading"
              :dataSource="mainState.dataSource"
              :showGroupList="mainState.showGroupList"
              :searchParams="searchParams"
              :allData="mainState.allData"
              :groupedBy="mainState.groupedBy"
              :order="mainState.order"
              :serviceId="props.serviceId"
              :updateData="updateApiData"
              class="w-full flex-1 overflow-y-auto overflow-x-hidden"
              @scrollChange="changeScrollDataList"
              @loadApis="refresh"
              @openMock="openMock" />
          </TabPane>
          <TabPane key="testResult" :tab="t('service.apis.tabs.test')">
            <ServiceTestInfo
              :serviceId="mainState.serviceId" />
          </TabPane>
          <TabPane key="mock" :tab="t('service.apis.tabs.mock')">
            <MockService
              :id="mainState.serviceId"
              class="pt-2 pr-5"
              :disabled="!useAuth.includes(ServicesPermission.MODIFY)" />
          </TabPane>
          <TabPane
            key="doc"
            :tab="t('common.doc')"
            style="width: 100%; height: 100%">
            <OpenApiDocument
              ref="openapiRef"
              :serviceId="mainState.serviceId"
              :mode="viewMode" />
          </TabPane>
        </Tabs>
      </div>
    </template>
    <template v-if="pageType === 'default'">
      <HomePage
        :serviceInfo="serviceInfo"
        :serviceAuths="serviceAuths"
        @openDrawer="openDrawer" />
    </template>
    <Drawer
      ref="drawerRef"
      v-model:activeKey="activeDrawerKey"
      :menuItems="mainState.drawerComp">
      <template #apiInfo>
        <ApiInfo
          v-if="activeDrawerKey === DrawerType.API_INFO"
          :id="mainState.id || mainState.serviceId"
          class="pr-4"
          :type="type"
          :disabled="!useAuth.includes(ApiPermission.MODIFY)"
          :name="props.serviceInfo.name" />
      </template>
      <template #performance>
        <Indicator
          v-if="activeDrawerKey === DrawerType.PERFORMANCE"
          :id="mainState.id"
          class="mt-2 pr-6"
          type="API"
          :testFlag="currentAPITest"
          :disabled="!useAuth.includes(ApiPermission.MODIFY)"
          :name="props.serviceInfo.name" />
      </template>
      <template #activity>
        <ActivityTimeline
          v-if="activeDrawerKey === DrawerType.ACTIVITY"
          :id="mainState.id || mainState.serviceId"
          class="pt-2 pr-5"
          :type="type"
          infoKey="description"
          :disabled="!useAuth.includes(ApiPermission.VIEW)"
          :name="props.serviceInfo.name" />
      </template>
      <template #testInfo>
        <HttpTestInfo
          v-if="activeDrawerKey === DrawerType.TEST_INFO"
          :id="mainState.id || mainState.serviceId"
          class="pr-5 pt-2"
          :type="mainState.type || 'SERVICE'"
          :disabled="!useAuth.includes(ApiPermission.MODIFY)"
          :name="props.serviceInfo.name" />
      </template>
      <template #shareList>
        <ShareList
          v-if="activeDrawerKey === DrawerType.SHARE_LIST"
          :id="mainState.id || mainState.serviceId"
          :type="type"
          class="pt-2 pr-5"
          :disabled="!useAuth.includes(ApiPermission.SHARE)"
          :name="props.serviceInfo.name" />
      </template>
      <template #agent>
        <RequestProxy
          v-if="activeDrawerKey === DrawerType.PROXY"
          class="mt-2 pr-5"
          :name="props.serviceInfo.name" />
      </template>
      <template #code>
        <CodeSnippet
          v-if="activeDrawerKey === DrawerType.CODE"
          :id="mainState.id"
          class="pr-5"
          :type="type"
          :disabled="!useAuth.includes(ApiPermission.VIEW)"
          :name="props.serviceInfo.name"
          from="list" />
      </template>
      <template #apiMock>
        <MockApi
          v-if="activeDrawerKey === DrawerType.API_MOCK"
          :id="mainState.id"
          class="pt-2 pr-5"
          :disabled="!apiAuths.includes(ApiPermission.MODIFY)" />
      </template>
      <template #projectInfo>
        <div class="mt-2 pr-5">
          <ServiceInfo
            v-if="activeDrawerKey === DrawerType.SERVICE_INFO"
            :id="mainState.serviceId"
            :type="type"
            :disabled="!useAuth.includes(ServicesPermission.GRANT)"
            :name="props.serviceInfo.name" />
        </div>
      </template>
      <template #openapi>
        <div class="mt-2 pr-5">
          <OASInfo
            v-if="activeDrawerKey === DrawerType.OPENAPI"
            :id="mainState.id || mainState.serviceId"
            :type="type"
            :disabled="!useAuth.includes(ServicesPermission.GRANT)"
            :name="props.serviceInfo.name" />
        </div>
      </template>
      <template #syncConfig>
        <SyncConfig
          v-if="activeDrawerKey === DrawerType.SYNC_CONFIG"
          :id="mainState.serviceId"
          class="pt-2 pr-5"
          :disabled="!useAuth.includes(ServicesPermission.MODIFY)"
          :name="props.serviceInfo.name" />
      </template>
      <template #security>
        <SecurityConfig
          v-if="activeDrawerKey === DrawerType.SECURITY"
          :id="mainState.serviceId"
          class="pt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes(ServicesPermission.MODIFY)"
          :name="props.serviceInfo.name" />
      </template>
      <template #serverConfig>
        <ServerConfig
          v-if="activeDrawerKey === DrawerType.SERVER_CONFIG"
          :id="mainState.serviceId"
          class="pt-2 pr-5" />
      </template>
      <template #tag>
        <OASTag
          v-if="activeDrawerKey === DrawerType.TAG"
          :id="mainState.id || mainState.serviceId"
          class="pt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes(ServicesPermission.MODIFY)"
          :name="props.serviceInfo.name" />
      </template>
      <template #componnet>
        <OASComponent
          v-if="activeDrawerKey === DrawerType.COMPONENT"
          :id="mainState.serviceId"
          class="pt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes(ServicesPermission.MODIFY)"
          :name="props.serviceInfo.name" />
      </template>
      <template #socketConfig>
        <WebSocketConfig
          v-if="activeDrawerKey === DrawerType.SOCKET_CONFIG"
          :id="mainState.id || mainState.serviceId"
          class="mt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes(ServicesPermission.MODIFY)"
          :name="props.serviceInfo.name" />
      </template>
      <template #cases>
        <TestCase
          v-if="activeDrawerKey === DrawerType.CASE"
          :id="mainState.id || mainState.serviceId"
          class="mt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes(ServicesPermission.MODIFY)" />
      </template>
    </Drawer>
  </div>
</template>
<style scoped>
.ant-btn-link {
  padding: 0;
  color: var(--content-text-content);
}

.ant-btn-link:hover {
  color: var(--content-special-text-hover);
}
</style>
