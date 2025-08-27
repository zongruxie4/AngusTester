<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, provide, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Spin, TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline, Drawer, Icon, notification } from '@xcan-angus/vue-ui';
import { cookieUtils, DomainManager, appContext } from '@xcan-angus/infra';

const { t } = useI18n();

import store from '@/store';
import { apis, services } from 'src/api/tester';
import { navs, serviceNav, socketNavs, StateType } from './PropsType';

const HttpTestInfo = defineAsyncComponent(() => import('@xcan-angus/vue-ui').then(resp => resp.HttpTestInfo));
const Indicator = defineAsyncComponent(() => import('@xcan-angus/vue-ui').then(resp => resp.Indicator));
const InterfaceHeader = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/header/index.vue'));
const InterfaceList = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/list/index.vue'));
const HomePage = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/homepage/index.vue'));
const ApiInfoVue = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/slider/apis/info/index.vue'));
// const VariableVue = defineAsyncComponent(() => import('@/views/apis/services/components/Variable/index.vue')); // 变量
const ShareListVue = defineAsyncComponent(() => import('@/components/share/list.vue')); // 分享记录
const AgentVue = defineAsyncComponent(() => import('@/views/apis/services/components/agent/index.vue')); // 代理
const CodeSnippetVue = defineAsyncComponent(() => import('@/views/apis/services/components/codeSnippet/index.vue')); // 代码
const ApiMockVue = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/slider/apis/ApiMock.vue')); //
const ServiceMockVue = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/slider/services/mockService/mockService.vue')); //
const ProjectInfoVue = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/slider/services/info/index.vue')); // 项目信息
const OpenApiVue = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/slider/services/openapiInfo/index.vue'));
const SyncConfigVue = defineAsyncComponent(() => import('@/views/apis/services/components/syncConfig/index.vue'));
const SecurityVue = defineAsyncComponent(() => import('@/views/apis/services/components/securityConfig/index.vue'));
const ServerConfigVue = defineAsyncComponent(() => import('@/views/apis/services/components/serverConfig/index.vue'));
const TagVue = defineAsyncComponent(() => import('@/views/apis/services/components/tag/index.vue'));
const ComponentVue = defineAsyncComponent(() => import('@/views/apis/services/components/oas/index.vue'));
const SocketConfigVue = defineAsyncComponent(() => import('@/views/apis/services/apiWebSocket/components/config.vue'));
const OpenApiDocument = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/oas/doc.vue'));
const TestCase = defineAsyncComponent(() => import('@/views/apis/services/components/case/index.vue'));
const QuickEntrace = defineAsyncComponent(() => import('@/views/apis/homepage/quickStarted.vue'));
const ServiceTestInfo = defineAsyncComponent(() => import('./slider/services/serviceTestInfo/index.vue'));

interface Props {
  serviceId: string;
  info?: any;
}

const props = withDefaults(defineProps<Props>(), {
  serviceId: '',
  info: {}
});
const showQuckEbtrace = ref(false); // 当前服务不存在时显示快速入口
const userInfo = ref(appContext.getUser());

const allAuths = ['VIEW', 'MODIFY', 'DELETE', 'DEBUG', 'TEST', 'GRANT', 'SHARE', 'RELEASE', 'EXPORT'];
// const allProjectAuths = ['ADD', 'VIEW', 'MODIFY', 'DELETE', 'DEBUG', 'TEST', 'GRANT', 'SHARE', 'RELEASE', 'EXPORT'];

const loading = ref(false);
const drawerRef = ref();
const activeDrawerKey = ref();
const apiAuths = ref<string[]>(['VIEW', 'MODIFY', 'DELETE', 'DEBUG', 'TEST', 'GRANT', 'SHARE', 'RELEASE', 'EXPORT']);
const pageType = ref<'default' | 'success' | undefined>('success');
const projectAuths = ref<string[]>(['ADD', 'VIEW', 'MODIFY', 'DELETE', 'DEBUG', 'TEST', 'GRANT', 'SHARE', 'RELEASE', 'EXPORT']);

const localGroupBy = localStorage.getItem(`${props.serviceId}_groupBy`);
const localOrder = localStorage.getItem(`${props.serviceId}_order`);
const scrollListData = ref<any[]>([]);
const state = reactive<StateType>({
  drawerComp: [],
  allData: [],
  dataSource: [],
  id: '', // 接口Id
  name: '',
  order: localOrder
    ? JSON.parse(localOrder)
    : {
        orderBy: undefined,
        orderSort: undefined
      },
  serviceId: '',
  showGroupList: !!(localGroupBy && localGroupBy !== ''),
  groupedBy: localStorage.getItem(`${props.serviceId}_groupBy`) || '',
  type: undefined,
  serviceAuth: false
});

const updateInterface = inject('updateInterface', {
  reloadKey: 0,
  reloadId: ''
});

const updateHosts = inject('updateHosts', { reloadKey: 0, reloadId: '' });
const isAdmin = inject('isAdmin', ref(false));

const apiListRef = ref();
const openapiRef = ref();
const tabActiveKey = ref<'doc' | 'api'>('api');

// 当前选中的API
const currentApi = ref();

const currentAPITestFlag = computed(() => {
  if (currentApi.value) {
    // eslint-disable-next-line no-prototype-builtins
    if (currentApi.value.hasOwnProperty('testFuncFlag') && currentApi.value.hasOwnProperty('testStabilityFlg') && currentApi.value.hasOwnProperty('testPerfFlag')) {
      const { testFuncFlag, testStabilityFlg, testPerfFlag } = currentApi.value;
      return {
        testFuncFlag,
        testStabilityFlg,
        testPerfFlag
      };
    }
  }
  return undefined;
});

const searchParams = computed(() => {
  const filters = state.name ? [{ key: 'summary', op: 'MATCH', value: state.name.trim() }] : [];
  return {
    filters,
    ...state.order,
    pageSize: 200
  };
});

// 查询接口列表
const loadApis = async () => {
  const params: any = {
    pageNo: 1,
    pageSize: store.state.maxPageSize,
    id: props.serviceId,
    filters: [],
    infoScope: 'DETAIL',
    ...state.order
  };
  if (state.name) {
    const keyword = state.name.trim();
    params.filters.push({ key: 'summary', op: 'MATCH', value: keyword });
  }
  loading.value = true;
  const [error, { data = {} }] = await services.loadApis(params);
  if (error) {
    loading.value = false;
    return;
  }
  state.allData = data.list || [];
  setDataSource();
  setTimeout(() => {
    loading.value = false;
  }, 100);
};

const setDataSource = () => {
  state.dataSource = state.allData;
};

//  分组
const grouped = (groupKey) => {
  if (groupKey !== state.groupedBy) {
    state.groupedBy = groupKey;
    state.id = '';
  }
};

const handleConfig = (value) => {
  drawerRef.value?.open(value);
};

const viewMode = ref<'code' | 'UI'>('UI');
const viewModeChange = () => {
  if (viewMode.value === 'UI') {
    viewMode.value = 'code';
  } else {
    viewMode.value = 'UI';
  }
};

const refreshDoc = () => {
  if (typeof openapiRef.value?.getData === 'function') {
    openapiRef.value.loadData();
  }
};

watch(() => state.name, () => {
  state.id = '';
  refresh();
});

watch(() => state.order, () => {
  state.id = '';
  refresh();
}, {
  deep: true
});

const setProjectDrawerComp = () => {
  state.drawerComp = serviceNav.map((i: any) => {
    return {
      ...i,
      key: i.value
    };
  });
};

watch(() => state.type, () => {
  if (state.type === 'WEBSOCKET') {
    state.drawerComp = socketNavs.map(i => ({ ...i, key: i.value }));
  } else if (state.type === 'API') {
    state.drawerComp = navs.map(i => ({ ...i, key: i.value }));
  }
});

// const setApiDrawerComp = () => {
//   if (state.type === 'WEBSOCKET') {
//     state.drawerComp = socketNavs.map(i => ({ ...i, key: i.value }));
//   } else {
//     state.drawerComp = navs.map(i => ({ ...i, key: i.value }));
//   }
// };

const ploadProjectAuthInfo = async () => {
  if (isAdmin.value) {
    setProjectDrawerComp();
    return;
  }
  const [error, resp] = await services.getCurrentAuth(state.serviceId);
  if (error) {
    return;
  }
  state.serviceAuth = resp.data.serviceAuth;
  if (state.serviceAuth) {
    projectAuths.value = (resp.data?.permissions || []).map(i => i.value);
    setProjectDrawerComp();
  } else {
    setProjectDrawerComp();
  }
};

// 获取 api 操作权限
const getApiAuth = async () => {
  const [error, { data = [] }] = await apis.getCurrentAuth(state.id);
  if (error) {
    return;
  }
  const auths = (data.permissions || []).map(m => m.value);
  apiAuths.value = auths;
};

const changeScrollDataList = (data) => {
  scrollListData.value = data;
};

// @TODO state.dataSource 为空，目前先使用scrollListData
const updateApiData = (data: { id: string; auth: boolean; }) => {
  const dataSource = scrollListData.value;
  const { id: targetId, auth } = data;
  for (let i = 0, len = dataSource.length; i < len; i++) {
    if (dataSource[i].id === targetId) {
      dataSource[i].auth = auth;
      break;
    }
  }
};

watch(() => updateInterface.reloadKey, () => {
  if (updateInterface.reloadId === props.serviceId || updateInterface.reloadId === 'all') {
    refresh();
    updateHosts.reloadKey++;
    updateHosts.reloadId = props.serviceId as string;
  }
});

watch(() => props.serviceId, (newValue) => {
  state.serviceId = newValue as string;
}, {
  immediate: true
});

watch(() => state.type, () => {
  drawerRef.value?.close();
});

watch(() => state.id, async (newValue, oldValue) => {
  if (newValue) {
    if (!oldValue) {
      drawerRef.value?.close();
    }

    const selectApi = state.allData.find(api => api.id === state.id) || scrollListData.value.find(api => api.id === state.id);
    currentApi.value = selectApi;

    if (selectApi?.serviceAuth && selectApi?.auth) {
      if (!isAdmin.value) {
        await getApiAuth();
      }
    } else {
      apiAuths.value = allAuths;
    }
    // setApiDrawerComp();
  } else {
    drawerRef.value?.close();
    setProjectDrawerComp();
  }
});

const useAuth = computed(() => {
  return state.id ? apiAuths.value : projectAuths.value;
});

watch(() => userInfo.value, newValue => {
  if (newValue?.id) {
    ploadProjectAuthInfo();
  }
}, {
  immediate: true,
  deep: true
});

const type = computed(() => {
  return state.type === 'WEBSOCKET' ? 'API' : (state.type || 'SERVICE');
});

const openMock = () => {
  drawerRef.value?.open('apiMock');
};

const openDrawer = (key) => {
  drawerRef.value?.open(key);
};

const refresh = () => {
  if (state.showGroupList) {
    loadApis();
  } else {
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

watch(() => state.showGroupList, () => {
  if (state.showGroupList) {
    if (!state.allData.length) {
      loadApis();
    }
  }
}, {
  immediate: true
});

const accessToken = ref();
const docOrigin = ref();
onMounted(async () => {
  if (props.info.shouldCheckId) {
    const [error] = await services.loadInfo(props.info?.id);
    if (error) {
      showQuckEbtrace.value = true;
      notification.warning(t('service.apiGroup.messages.serviceNotExist'));
    }
  }
  accessToken.value = cookieUtils.getTokenInfo().access_token;
  // docOrigin.value = await site.getUrl('apis');
  docOrigin.value = DomainManager.getInstance().getApiDomain('tester');
});

// 更新state
const updateState = (config = {}) => {
  Object.keys(config).forEach((key) => {
    state[key] = config[key];
  });
};
provide('api', state); // 当前 state,  用于提供给 api list 修改当前选中的 apid id
provide('changeGroupState', updateState);
provide('id', computed(() => state.id));
provide('serviceId', computed(() => state.serviceId));
provide('loadApis', refresh);
provide('apiAuths', apiAuths);
provide('projectAuths', projectAuths);
provide('apiBaseInfo', ref({ serviceId: props.serviceId }));
</script>
<template>
  <template v-if="showQuckEbtrace">
    <QuickEntrace />
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
                  <span>{{ t('service.apiGroup.viewMode.codeView') }}</span>
                </div>
              </Button>
              <Button
                v-else-if="viewMode === 'code'"
                type="link"
                size="small"
                @click="viewModeChange">
                <div class="flex items-center space-x-1">
                  <Icon icon="icon-yemianshitu" class="text-3.5" />
                  <span>{{ t('service.apiGroup.viewMode.pageView') }}</span>
                </div>
              </Button>
              <Button
                type="link"
                size="small"
                @click="refreshDoc">
                <div class="flex items-center space-x-1">
                  <Icon icon="icon-shuaxin" class="text-3.5" />
                  <span>{{ t('service.apiGroup.viewMode.refresh') }}</span>
                </div>
              </Button>
            </div>
          </template>
          <TabPane
            key="api"
            :tab="t('service.apiGroup.tabs.api')"
            class="flex flex-col">
            <InterfaceHeader
              v-model:name="state.name"
              v-model:orderBy="state.order.orderBy"
              v-model:orderSort="state.order.orderSort"
              v-model:serviceId="state.serviceId"
              v-model:showGroupList="state.showGroupList"
              v-model:loading="loading"
              :serviceName="props.info.name"
              :projectAuths="projectAuths"
              :groupBy="state.groupedBy"
              projectTargetType="SERVICE"
              :disabled="!projectAuths.includes('GRANT')"
              :type="props?.info?.type === 'P' ? 'PROJECT' : 'SERVICE'"
              class="pr-5 mb-3.5"
              @loadInteface="refresh"
              @grouped="grouped"
              @config="handleConfig" />
            <InterfaceList
              ref="apiListRef"
              v-model:pageType="pageType"
              v-model:spinning="loading"
              :dataSource="state.dataSource"
              :showGroupList="state.showGroupList"
              :searchParams="searchParams"
              :allData="state.allData"
              :groupedBy="state.groupedBy"
              :order="state.order"
              :serviceId="props.serviceId"
              :updateData="updateApiData"
              class="w-full flex-1 overflow-y-auto overflow-x-hidden"
              @scrollChange="changeScrollDataList"
              @loadApis="refresh"
              @openMock="openMock" />
          </TabPane>
          <TabPane key="testResult" :tab="t('service.apiGroup.tabs.testResult')">
            <ServiceTestInfo
              :serviceId="state.serviceId" />
          </TabPane>
          <TabPane key="mock" :tab="t('service.apiGroup.tabs.mock')">
            <ServiceMockVue
              :id="state.serviceId"
              class="pt-2 pr-5"
              :disabled="!useAuth.includes('MODIFY')" />
          </TabPane>
          <TabPane
            key="doc"
            :tab="t('service.apiGroup.tabs.doc')"
            style="width: 100%; height: 100%">
            <OpenApiDocument
              ref="openapiRef"
              :serviceId="state.serviceId"
              :mode="viewMode" />
          </TabPane>
        </Tabs>
      </div>
    </template>
    <template v-if="pageType === 'default'">
      <HomePage
        :info="info"
        :projectAuths="projectAuths"
        @openDrawer="openDrawer" />
    </template>
    <Drawer
      ref="drawerRef"
      v-model:activeKey="activeDrawerKey"
      :menuItems="state.drawerComp">
      <template #apiInfo>
        <ApiInfoVue
          v-if="activeDrawerKey === 'apiInfo'"
          :id="state.id || state.serviceId"
          class="pr-4"
          :type="type"
          :disabled="!useAuth.includes('MODIFY')"
          :name="props.info.name" />
      </template>
      <template #performance>
        <Indicator
          v-if="activeDrawerKey === 'performance'"
          :id="state.id"
          class="mt-2 pr-6"
          type="API"
          :testFlag="currentAPITestFlag"
          :disabled="!useAuth.includes('MODIFY')"
          :name="props.info.name" />
      </template>
      <template #activity>
        <ActivityTimeline
          v-if="activeDrawerKey === 'activity'"
          :id="state.id || state.serviceId"
          class="pt-2 pr-5"
          :type="type"
          infoKey="description"
          :disabled="!useAuth.includes('VIEW')"
          :name="props.info.name" />
      </template>
      <template #testInfo>
        <HttpTestInfo
          v-if="activeDrawerKey === 'testInfo'"
          :id="state.id || state.serviceId"
          class="pr-5 pt-2"
          :type="state.type || 'SERVICE'"
          :disabled="!useAuth.includes('MODIFY')"
          :name="props.info.name" />
      </template>
      <template #shareList>
        <ShareListVue
          v-if="activeDrawerKey === 'shareList'"
          :id="state.id || state.serviceId"
          :type="type"
          class="pt-2 pr-5"
          :disabled="!useAuth.includes('SHARE')"
          :name="props.info.name" />
      </template>
      <template #agent>
        <AgentVue
          v-if="activeDrawerKey === 'agent'"
          class="mt-2 pr-5"
          :name="props.info.name" />
      </template>
      <template #code>
        <CodeSnippetVue
          v-if="activeDrawerKey === 'code'"
          :id="state.id"
          class="pr-5"
          :type="type"
          :disabled="!useAuth.includes('VIEW')"
          :name="props.info.name"
          from="list" />
      </template>
      <template #apiMock>
        <ApiMockVue
          v-if="activeDrawerKey === 'apiMock'"
          :id="state.id"
          class="pt-2 pr-5"
          :disabled="!apiAuths.includes('MODIFY')" />
      </template>
      <template #projectInfo>
        <div class="mt-2 pr-5">
          <ProjectInfoVue
            v-if="activeDrawerKey === 'projectInfo'"
            :id="state.serviceId"
            :type="type"
            :disabled="!useAuth.includes('GRANT')"
            :name="props.info.name" />
        </div>
      </template>
      <template #openapi>
        <div class="mt-2 pr-5">
          <OpenApiVue
            v-if="activeDrawerKey === 'openapi'"
            :id="state.id || state.serviceId"
            :type="type"
            :disabled="!useAuth.includes('GRANT')"
            :name="props.info.name" />
        </div>
      </template>
      <template #syncConfig>
        <SyncConfigVue
          v-if="activeDrawerKey === 'syncConfig'"
          :id="state.serviceId"
          class="pt-2 pr-5"
          :disabled="!useAuth.includes('MODIFY')"
          :name="props.info.name" />
      </template>
      <template #security>
        <SecurityVue
          v-if="activeDrawerKey === 'security'"
          :id="state.serviceId"
          class="pt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes('MODIFY')"
          :name="props.info.name" />
      </template>
      <template #serverConfig>
        <ServerConfigVue
          v-if="activeDrawerKey === 'serverConfig'"
          :id="state.serviceId"
          class="pt-2 pr-5" />
      </template>
      <template #tag>
        <TagVue
          v-if="activeDrawerKey === 'tag'"
          :id="state.id || state.serviceId"
          class="pt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes('MODIFY')"
          :name="props.info.name" />
      </template>
      <template #componnet>
        <ComponentVue
          v-if="activeDrawerKey === 'componnet'"
          :id="state.serviceId"
          class="pt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes('MODIFY')"
          :name="props.info.name" />
      </template>
      <template #socketConfig>
        <SocketConfigVue
          v-if="activeDrawerKey === 'socketConfig'"
          :id="state.id || state.serviceId"
          class="mt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes('MODIFY')"
          :name="props.info.name" />
      </template>
      <template #case>
        <TestCase
          v-if="activeDrawerKey === 'case'"
          :id="state.id || state.serviceId"
          class="mt-2 pr-5"
          :type="type"
          :disabled="!useAuth.includes('MODIFY')" />
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
