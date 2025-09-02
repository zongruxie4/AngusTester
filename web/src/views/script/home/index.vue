<script setup lang="ts">
import { defineAsyncComponent, inject, Ref, ref, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { AsyncComponent, Spin } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';

import { useScriptData } from './composables/useScriptData';
import { useScriptImport } from './composables/useScriptImport';
import { PermissionKey } from './types';

// Async components
const Introduce = defineAsyncComponent(() => import('@/views/script/home/Introduce.vue'));
const PieChart = defineAsyncComponent(() => import('@/views/script/home/PieChart.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/script/home/SearchPanel.vue'));
const ImportModal = defineAsyncComponent(() => import('@/views/script/home/Import.vue'));
const GlobalAuthModal = defineAsyncComponent(() => import('@/views/script/home/auth/index.vue'));
const ScriptTable = defineAsyncComponent(() => import('@/views/script/home/Table.vue'));

// Route and router
const route = useRoute();
const router = useRouter();

// Injected values
const isAdmin = inject('isAdmin', ref(false));
const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({
  id: '',
  avatar: '',
  name: ''
}));
const appInfo = inject<Ref<{ id: string; name: string; }>>('appInfo', ref({ id: '', name: '' }));

// Composables
const {
  tableData,
  permissionsMap,
  loading,
  loaded,
  pagination,
  countData,
  resetSelectedIdsNotify,
  allowImportSamplesFlag,
  loadScriptList,
  loadResourcesData,
  handleTableChange,
  handleDelete
} = useScriptData(projectInfo.value.id, isAdmin.value);

const {
  handleImport
} = useScriptImport(projectInfo.value.id);

// Modal visibility states
const importVisible = ref(false);
const globalAuthModalVisible = ref(false);

/**
 * Navigate to import modal
 */
const toImport = () => {
  importVisible.value = true;
};

/**
 * Navigate to auth modal
 */
const toAuth = () => {
  globalAuthModalVisible.value = true;
};

/**
 * Handle search panel changes
 */
const searchPanelChange = (data: { key: string; op: string; value: boolean | string | string[]; }[]) => {
  const { pageNo, pageSize } = route.query;
  if (pageNo && pageSize) {
    pagination.value.current = +pageNo;
    pagination.value.pageSize = +pageSize;
    router.replace('/script');
  } else {
    pagination.value.current = 1;
  }

  // Update filters and reload data
  // This would need to be handled in the composable
  loadScriptList();
  loadResourcesData();
};

/**
 * Refresh data
 */
const toRefresh = () => {
  loadScriptList();
  loadResourcesData();
};

// Computed projectId
const projectId = computed(() => projectInfo.value?.id);

// Watch for project ID changes
watch(() => projectInfo.value.id, (newId) => {
  if (newId) {
    loadScriptList();
    loadResourcesData();
  }
}, { immediate: true });
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
      @tableChange="handleTableChange"
      @delete="handleDelete"
      @refresh="toRefresh" />
  </Spin>

  <AsyncComponent :visible="importVisible">
    <ImportModal
      v-model:visible="importVisible"
      :projectId="projectId"
      @ok="handleImport(() => {})" />
  </AsyncComponent>

  <AsyncComponent :visible="globalAuthModalVisible">
    <GlobalAuthModal
      v-model:visible="globalAuthModalVisible"
      :projectId="projectId"
      :appId="appInfo?.id" />
  </AsyncComponent>
</template>
