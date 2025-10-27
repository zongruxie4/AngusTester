<script setup lang="ts">
import { defineAsyncComponent, inject, Ref, ref, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { AsyncComponent, Spin } from '@xcan-angus/vue-ui';
import { appContext, SearchCriteria } from '@xcan-angus/infra';
import { ScriptPermission } from '@/enums/enums';
import { ProjectInfo } from '@/layout/types';

import { useScriptData } from './composables/useScriptData';
import { useScriptImport } from './composables/useScriptImport';

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
const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const projectId = computed(() => projectInfo.value?.id );
const appInfo = ref(appContext.getAccessApp());

// Computed values with proper type conversion
const userInfoForProps = computed(() => ({
  id: String(userInfo.value?.id || '')
}));

const appInfoForProps = computed(() => ({
  id: String(appInfo.value?.id || ''),
  name: appInfo.value?.name || ''
}));

// Composables
const {
  dataProjectId,
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
  handleDelete,
  updateFilters
} = useScriptData();

const {
  importProjectId,
  handleImport
} = useScriptImport({ projectId: projectId.value });

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
 * @param searchFilters - Search filters from the search panel
 */
const searchPanelChange = (searchFilters: SearchCriteria[]) => {
  const { pageNo, pageSize } = route.query;
  if (pageNo && pageSize) {
    pagination.value.current = +pageNo;
    pagination.value.pageSize = +pageSize;
    router.replace('/script');
  } else {
    pagination.value.current = 1;
  }

  // Update filters in useScriptData composable
  updateFilters(searchFilters);

  // Reload data with new filters
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

// Convert permissionsMap to proper type
const permissionsMapForProps = computed((): { [key: string]: ScriptPermission[] } => {
  const converted: { [key: string]: ScriptPermission[] } = {};
  Object.keys(permissionsMap.value).forEach(key => {
    converted[key] = permissionsMap.value[key] as ScriptPermission[];
  });
  return converted;
});

// Watch for project ID changes with better error handling
watch(() => projectId.value, (newId, oldId) => {
  // Only load data if we have a valid project ID and it's different from the previous one
  if (newId && newId !== '' && newId !== oldId) {
    dataProjectId.value = newId;
    importProjectId.value = newId;
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
      :userInfo="userInfoForProps"
      :appInfo="appInfoForProps"
      class="mb-3.5"
      @change="searchPanelChange"
      @auth="toAuth"
      @import="toImport" />

    <ScriptTable
      v-model:loading="loading"
      :loaded="loaded"
      :resetSelectedIdsNotify="resetSelectedIdsNotify"
      :projectId="projectId"
      :appId="appInfoForProps.id"
      :userId="userInfoForProps.id"
      :pagination="pagination"
      :dataSource="tableData"
      :permissionsMap="permissionsMapForProps"
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
      :appId="appInfoForProps.id" />
  </AsyncComponent>
</template>
