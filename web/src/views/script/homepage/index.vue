<script setup lang="ts">
import { computed, defineAsyncComponent, inject, Ref, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { AsyncComponent, Spin } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { PermissionKey, ResourceInfo, ScriptInfo } from '../PropsType';
import { isEqual } from 'lodash-es';
import { script, analysis } from '@/api/tester';

const Introduce = defineAsyncComponent(() => import('@/views/script/homepage/introduce/index.vue'));
const PieChart = defineAsyncComponent(() => import('@/views/script/homepage/pieChart/index.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/script/homepage/searchPanel/index.vue'));
const ImportModal = defineAsyncComponent(() => import('@/views/script/homepage/Import/index.vue'));
const GlobalAuthModal = defineAsyncComponent(() => import('@/views/script/homepage/globalAuth/index.vue'));
const ScriptTable = defineAsyncComponent(() => import('@/views/script/homepage/table/index.vue'));

const route = useRoute();
const router = useRouter();

const isAdmin = inject('isAdmin', ref(false));
const userInfo = inject<Ref<{ id: string }>>('tenantInfo');
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({
  id: '',
  avatar: '',
  name: ''
}));
const appInfo = inject<Ref<{ id: string; name: string; }>>('appInfo', ref({ id: '', name: '' }));

const loaded = ref(false);
const loading = ref(false);
const orderBy = ref<string>();
const orderSort = ref<'DESC' | 'ASC'>();
const filters = ref<{ key: string; op: string; value: boolean | string | string[]; }[]>([]);

const pagination = ref<{ current: number; pageSize: number; total: number; }>({ current: 1, pageSize: 10, total: 0 });

const tableData = ref<ScriptInfo[]>([]);
const permissionsMap = ref<{ [key: string]: PermissionKey[] }>({});
const countData = ref<ResourceInfo>({
  totalScriptNum: '0',
  perfScriptNum: '0',
  functionalScriptNum: '0',
  stabilityScriptNum: '0',
  customizationScriptNum: '0',
  mockDataScriptNum: '0',
  mockApisScriptNum: '0',
  createdSourceNum: '0',
  importedSourceNum: '0',
  apisSourceNum: '0',
  caseSourceNum: '0',
  scenarioSourceNum: '0'
});

const allowImportSamplesFlag = ref(false);

const importVisible = ref(false);
const globalAuthModalVisible = ref(false);

const prevParams = ref<{ [key: string]: any }>();
const resetSelectedIdsNotify = ref<string>();

const toImport = () => {
  importVisible.value = true;
};

const importOk = () => {
  pagination.value.current = 1;
  loadScriptList();
  loadResourcesData();
};

const toAuth = () => {
  globalAuthModalVisible.value = true;
};

const searchPanelChange = (data: { key: string; op: string; value: boolean | string | string[]; }[]) => {
  const { pageNo, pageSize } = route.query;
  if (pageNo && pageSize) {
    pagination.value.current = +pageNo;
    pagination.value.pageSize = +pageSize;
    router.replace('/script');
  } else {
    pagination.value.current = 1;
  }

  filters.value = data;
  loadScriptList();
  loadResourcesData();
};

const toRefresh = () => {
  loadScriptList();
  loadResourcesData();
};

const tableChange = ({ current, pageSize }: { current: number; pageSize: number; }, sorter: {
  orderBy: string;
  orderSort: 'DESC' | 'ASC'
}) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;

  orderBy.value = sorter.orderBy;
  orderSort.value = sorter.orderSort;

  loadScriptList();
  loadResourcesData();
};

const toDelete = (ids: string[]) => {
  const { current, pageSize, total } = pagination.value;
  const totalPage = Math.ceil(total / pageSize);
  const remainder = total % pageSize;

  const deleteNum = ids.length;
  const deletePages = Math.floor(deleteNum / pageSize);
  const deleteRemainder = deleteNum % pageSize;

  if ((deleteRemainder === 0 || remainder === 0) || (deleteRemainder < remainder)) {
    if (current + deletePages <= totalPage) {
      loadScriptList();
      loadResourcesData();

      return;
    }

    pagination.value.current = current - (current + deletePages - totalPage) || 1;
    loadScriptList();
    loadResourcesData();

    return;
  }

  if (deleteRemainder >= remainder) {
    if (current + deletePages + 1 <= totalPage) {
      loadScriptList();
      loadResourcesData();

      return;
    }

    pagination.value.current = current - (current + deletePages - totalPage) - 1 || 1;
    loadScriptList();
    loadResourcesData();
  }
};

const getParams = () => {
  const params: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    filters?: { key: string; op: string; value: boolean | string | string[]; }[];
    orderBy?: string;
    orderSort?: 'DESC' | 'ASC';
  } = {
    projectId: projectId.value,
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize
  };

  if (filters.value.length) {
    params.filters = filters.value;
  }

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  return params;
};

const loadScriptList = async () => {
  loading.value = false;
  const params = getParams();
  const [error, res] = await script.getScriptList(params);
  loaded.value = true;
  if (error) {
    loading.value = false;
    return false;
  }

  // 除了页码还有其他条件不一致，说明搜索条件发生改变，需要把选中的任务重置
  if (prevParams.value) {
    delete prevParams.value.pageNo;
    delete params.pageNo;
    if (!isEqual(prevParams.value, params)) {
      resetSelectedIdsNotify.value = utils.uuid();
      permissionsMap.value = {};
    }
  }

  prevParams.value = params;

  const data = (res?.data || { total: '0', list: [], ext: {} }) as {
    total: string;
    list: ScriptInfo[];
    ext: { allowImportSamples: boolean; }
  };
  pagination.value.total = +data.total;
  tableData.value = data.list.map(item => {
    let sourceNameLinkUrl = '';
    const source = item.source?.value;
    const sourceId = item.sourceId;
    const sourceName = item.sourceName;
    if (sourceId && sourceName) {
      if (source === 'SERVICE_SMOKE') {
        sourceNameLinkUrl = `/apis#services?id=${sourceId}&name=${sourceName}&value=group`;
      } else if (source === 'SERVICE_SECURITY') {
        sourceNameLinkUrl = `/apis#services?id=${sourceId}&name=${sourceName}&value=group`;
      } else if (source === 'API') {
        sourceNameLinkUrl = `/apis#services?id=${sourceId}&name=${sourceName}&value=API`;
      } else if (source === 'SCENARIO') {
        sourceNameLinkUrl = `/scenario#scenario?id=${sourceId}&name=${sourceName}&plugin=${item.plugin}`;
      }
    }

    return {
      ...item,
      sourceNameLinkUrl,
      nameLinkUrl: `/script/detail/${item.id}?type=view&pageNo=${pagination.value.current}&pageSize=${pagination.value.pageSize}`
    };
  });

  const allowImportSamples = data.ext?.allowImportSamples;
  if (typeof data.ext?.allowImportSamples === 'boolean') {
    allowImportSamplesFlag.value = allowImportSamples;
  }

  const ids = data.list.map(item => item.id);
  loadScriptListAuth(ids);
};

const loadScriptListAuth = async (ids: string[]) => {
  if (!ids.length) {
    return;
  }

  const [error, res] = await script.getScriptCurrentAuth(ids);
  loading.value = false;
  if (error) {
    return false;
  }

  const map: {
    [key: string]: { permissions: { value: PermissionKey; message: string }[]; scriptAuth: boolean; }
  } = res?.data;
  if (map) {
    for (const key in map) {
      const { scriptAuth, permissions } = map[key];
      let list: PermissionKey[] = [];
      const values = permissions.map(item => item.value);
      if (isAdmin.value || scriptAuth === false) {
        list = ['TEST', 'VIEW', 'MODIFY', 'DELETE', 'EXPORT', 'COLON'];
        if (values.includes('GRANT')) {
          list.push('GRANT');
        }
      } else {
        list = [...values];
        if (values.includes('VIEW') || !values.includes('COLON')) {
          list.push('COLON');
        }
      }

      permissionsMap.value[key] = list;
    }
  }
};

const loadResourcesData = async () => {
  const params = { filters: filters.value, projectId: projectId.value };
  const [error, res] = await analysis.getScriptCount(params);
  if (error) {
    return false;
  }

  countData.value = res?.data || {
    totalScriptNum: '0',
    perfScriptNum: '0',
    functionalScriptNum: '0',
    stabilityScriptNum: '0',
    customizationScriptNum: '0',
    mockDataScriptNum: '0',
    mockApisScriptNum: '0',
    createdSourceNum: '0',
    importedSourceNum: '0',
    apisSourceNum: '0',
    caseSourceNum: '0',
    scenarioSourceNum: '0'
  };
};

const projectId = computed(() => projectInfo.value?.id);
</script>
<template>
  <Spin
    :spinning="loading"
    class="flex flex-col w-full h-full py-5 px-5 overflow-y-auto"
    style="scrollbar-gutter: stable;">
    <div class="bg-gray-2 flex items-center rounded mb-5">
      <Introduce />
      <PieChart :dataSource="countData" />
    </div>

    <SearchPanel
      v-if="projectId"
      :projectId="projectId"
      :userInfo="userInfo"
      :appInfo="appInfo"
      class="mb-3.5"
      @change="searchPanelChange"
      @auth="toAuth"
      @import="toImport" />

    <ScriptTable
      v-model:loading="loading"
      :loaded="loaded"
      :resetSelectedIdsNotify="resetSelectedIdsNotify"
      :projectId="projectId"
      :appId="appInfo?.id"
      :userId="userInfo?.id"
      :pagination="pagination"
      :dataSource="tableData"
      :permissionsMap="permissionsMap"
      :allowImportSamplesFlag="allowImportSamplesFlag"
      @tableChange="tableChange"
      @delete="toDelete"
      @refresh="toRefresh" />
  </Spin>

  <AsyncComponent :visible="importVisible">
    <ImportModal
      v-model:visible="importVisible"
      :projectId="projectId"
      @ok="importOk" />
  </AsyncComponent>

  <AsyncComponent :visible="globalAuthModalVisible">
    <GlobalAuthModal
      v-model:visible="globalAuthModalVisible"
      :projectId="projectId"
      :appId="appInfo?.id" />
  </AsyncComponent>
</template>
