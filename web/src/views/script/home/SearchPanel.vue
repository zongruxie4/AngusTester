<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Colon, Icon, IconRefresh, IconText, SearchPanel, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useScriptSearch } from './composables/useScriptSearch';

import { ScriptSearchProps } from '@/views/script/types';

const { t } = useI18n();

const props = withDefaults(defineProps<ScriptSearchProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'import'): void;
  (e: 'delete'): void;
  (e: 'cancelDelete'): void;
  (e: 'auth'): void;
  (e: 'change', value: { key: string; op: string; value: boolean | string | string[]; }[]): void;
}>();

// Use the search composable
const {
  searchPanelRef,
  filters,
  serviceIdFilter,
  sourceIdFilter,
  selectedMenuMap,
  loadEnum,
  handleMenuItemClick,
  handleSearchPanelChange,
  handleSourceIdChange,
  handleServiceIdChange,
  getData,
  resetData,
  resetSearchPanel,
  menuItems,
  searchOptions,
  isAPISource,
  isScenarioSource,
  apiParams
} = useScriptSearch(props.projectId, props.userInfo?.id);

/**
 * Handle import action
 */
const toImport = () => {
  emit('import');
};

/**
 * Handle auth action
 */
const toAuth = () => {
  emit('auth');
};

/**
 * Handle refresh action
 */
const toRefresh = () => {
  emit('change', getData());
};

/**
 * Initialize component
 */
const initialize = async () => {
  await loadEnum();
  // Additional initialization logic would go here
  resetData();
  resetSearchPanel();
};

onMounted(() => {
  initialize();
});

// Watch for changes and emit to parent
watch(
  [
    () => filters.value,
    () => serviceIdFilter.value,
    () => sourceIdFilter.value,
    () => selectedMenuMap.value
  ],
  () => {
    emit('change', getData());
  },
  { immediate: false, deep: false }
);
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex items-center mb-3">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 mt-1 rounded-full"></div>
        <div class="whitespace-nowrap text-3 mt-0.5 text-text-sub-content">
          <span>{{ t('projectActivity.searchPanel.ui.quickQuery') }}</span>
          <Colon />
        </div>
        <div class="flex flex-wrap ml-2">
          <div
            v-for="item in menuItems"
            :key="item.key"
            :class="{ 'active-key': selectedMenuMap.has(item.key) }"
            class="px-2.5 h-6 leading-6 mr-3 rounded bg-gray-light cursor-pointer font-semibold text-3"
            @click="handleMenuItemClick(item)">
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>

    <div class="flex items-start justify-between mb-1.5 space-x-5">
      <SearchPanel
        ref="searchPanelRef"
        class="flex-1 min-w-0"
        :options="searchOptions"
        @change="handleSearchPanelChange">
        <template #serviceId>
          <Select
            v-if="isAPISource"
            :value="serviceIdFilter.value"
            :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="t('scriptHome.searchPanel.servicePlaceholder')"
            class="w-72 ml-2"
            showSearch
            @change="handleServiceIdChange">
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

        <template #sourceId>
          <Select
            v-if="isAPISource"
            :value="sourceIdFilter.value"
            :action="`${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`"
            :params="apiParams"
            :fieldNames="{ label: 'summary', value: 'id' }"
            :allowClear="true"
            :placeholder="t('scriptHome.searchPanel.apiPlaceholder')"
            class="w-72 ml-2"
            showSearch
            @change="handleSourceIdChange">
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
            v-if="isScenarioSource"
            :value="sourceIdFilter.value"
            :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="t('scriptHome.searchPanel.scenarioPlaceholder')"
            class="w-72 ml-2"
            showSearch
            @change="handleSourceIdChange">
            <template #option="record">
              <div class="flex items-center">
                <Icon icon="icon-changjing" class="text-4 flex-shrink-0" />
                <div :title="record.name" class="truncate ml-1.5">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </template>
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2.5">
        <Button
          type="primary"
          size="small"
          class="py-0">
          <RouterLink
            class="h-6.5 leading-6.5 flex items-center space-x-1"
            to="/script/edit?type=edit">
            <Icon icon="icon-jia" />
            <span>{{ t('scriptHome.searchPanel.addScript') }}</span>
          </RouterLink>
        </Button>

        <Button
          type="primary"
          size="small"
          class="flex space-x-1"
          @click="toImport">
          <Icon icon="icon-shangchuan" class="text-3.5" />
          <span>{{ t('scriptHome.searchPanel.importScript') }}</span>
        </Button>

        <Button size="small" @click="toAuth">
          <Icon icon="icon-quanxian1" class="mr-1 text-3.5" />
          <span>{{ t('scriptHome.searchPanel.scriptAuth') }}</span>
        </Button>

        <!-- Refresh Button -->
        <IconRefresh
          @click="toRefresh">
          <template #default>
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-shuaxin" class="text-3.5" />
            </div>
          </template>
        </IconRefresh>
      </div>
    </div>
  </div>
</template>

<style scoped>
.active-key {
  background-color: #4ea0fd;
  color: #fff;
}
</style>
