<script setup lang="ts">
import { computed, defineAsyncComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import {
  AsyncComponent,
  Dropdown,
  DropdownGroup,
  DropdownSort,
  Icon,
  NoData,
  SearchPanel,
  Spin
} from '@xcan-angus/vue-ui';

// Import composables
import { useScenarioActions, useScenarioData, useScenarioSearch } from './composables';

import { GroupedKey } from './types';

const { t } = useI18n();

const ModuleTree = defineAsyncComponent(() => import('@/components/module/ModuleTree.vue'));

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

// Inject tab management functions
// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });
const deleteTabPane = inject<(data: string[]) => void>('deleteTabPane', () => { });

// Initialize composables
const {
  loaded,
  loading,
  errorMessage,
  dataList,
  loadData,
  moduleId
} = useScenarioData(
  computed(() => props.projectId),
  computed(() => props.notify)
);

const {
  createHttpScenario,
  buttonDropdownClick,
  addScenarioAuthorize
} = useScenarioActions(
  computed(() => props.projectId),
  addTabPane,
  deleteTabPane,
  ref([]), // scenarioList - not used in this component
  ref({}), // dropdownMenuItemsMap - not used in this component
  [], // dropdownMenuItems - not used in this component
  ref() // selectedId - not used in this component
);

const {
  groupedKey,
  orderBy,
  orderSort,
  filters,
  searchOptions,
  buttonDropdownMenuItems,
  deviceDropdownMenuItems,
  sortMenuItems,
  groupingMenuItems,
  handleSearchChange,
  handleSortChange,
  handleGroupingChange
} = useScenarioSearch();

// Async components
const ScenarioDrawer = defineAsyncComponent(() => import('./Drawer.vue'));
const ScenarioList = defineAsyncComponent(() => import('./List.vue'));
const ScenarioGroup = defineAsyncComponent(() => import('./Group.vue'));

// Event handlers
const searchChange = (value: any[]): void => {
  handleSearchChange(value);
  loadData(value, orderBy.value, orderSort.value);
};

const sortHandler = (value: any): void => {
  handleSortChange(value);
  loadData(filters.value, value.orderBy, value.orderSort);
};

const groupingHandler = (value: GroupedKey): void => {
  handleGroupingChange(value);
};

const refreshHandler = (): void => {
  loadData(filters.value, orderBy.value, orderSort.value);
};

const cloneHandler = (): void => {
  loadData(filters.value, orderBy.value, orderSort.value);
};

const deleteScenarioHandler = (scenaridId: string):void => {
  deleteTabPane([scenaridId, scenaridId + '-detail']);
  refreshHandler();
};
</script>

<template>
  <div class="flex h-full ">
    <div class="h-full overflow-hidden pb-3 bg-gray-1 text-3 w-65 mr-2">
      <ModuleTree
        v-model:moduleId="moduleId"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :notify="props.notify" />
    </div>
    <Spin class="flex-1 h-full flex flex-col py-5" :spinning="loading">
      <div class="flex space-x-2 px-2 pb-2">
        <Button
          class="flex-shrink-0 flex items-center"
          type="primary"
          size="small"
          @click="buttonDropdownClick({ key: 'Mobile' })">
          <Icon icon="icon-jia" class="text-3.5 mr-1" />
          添加移动应用场景
        </Button>
        <Button
          class="flex-shrink-0 flex items-center"
          type="primary"
          size="small"
          @click="buttonDropdownClick({ key: 'Web' })">
          <Icon icon="icon-jia" class="text-3.5  mr-1" />
          添加WebUI场景
        </Button>
        
        
        <Button
          class="flex-shrink-0 flex items-center pr-0"
          type="primary"
          size="small"
          @click="createHttpScenario">
          <div class="flex items-center">
            <Icon icon="icon-jia" class="text-3.5" />
            <span class="ml-1">{{ t('scenario.list.actions.addHttpScenario') }}</span>
          </div>
          <Dropdown :menuItems="buttonDropdownMenuItems" @click="buttonDropdownClick">
            <div class="w-5 h-5 flex items-center justify-center">
              <Icon icon="icon-more" />
            </div>
          </Dropdown>
        </Button>
        
      </div>
      
      <div class="flex-shrink-0 flex items-start text-3 px-2 mb-3.5 space-x-3">
        <SearchPanel
          class="flex-1"
          :options="searchOptions"
          @change="searchChange" />

        <div class="flex-shrink-0 flex items-center flex-nowrap whitespace-nowrap leading-7 space-x-3.5">

          <DropdownSort
          v-model:orderBy="orderBy"
          v-model:orderSort="orderSort"
          :menuItems="sortMenuItems"
          @click="sortHandler">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" />
            <span>{{ t('actions.sort') }}</span>
          </div>
        </DropdownSort>

        <DropdownGroup
          :activeKey="groupedKey"
          :menuItems="groupingMenuItems"
          @click="groupingHandler">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-fenzu" />
            <span>{{ t('organization.group') }}</span>
          </div>
        </DropdownGroup>

        <div
          class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover"
          @click="refreshHandler">
          <Icon icon="icon-shuaxin" />
          <span>{{ t('actions.refresh') }}</span>
        </div>
        <div
          class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover"
          @click="addScenarioAuthorize">
          <Icon icon="icon-quanxian1" />
          <span>{{ t('actions.permission') }}</span>
        </div>

          
        </div>
      </div>

      <template v-if="loaded">
        <NoData
          v-if="dataList.length === 0"
          style="height: calc(100% - 42px);"
          :text="errorMessage" />

        <template v-else>
          <AsyncComponent :visible="groupedKey !== 'none'">
            <ScenarioGroup
              v-show="groupedKey !== 'none'"
              :dataSource="dataList"
              :notify="props.notify"
              :userInfo="props.userInfo"
              :appInfo="props.appInfo"
              :projectId="props.projectId"
              :groupedKey="groupedKey" />
          </AsyncComponent>

          <AsyncComponent :visible="groupedKey === 'none'">
            <ScenarioList
              v-show="groupedKey === 'none'"
              :dataSource="dataList"
              :notify="props.notify"
              :userInfo="props.userInfo"
              :appInfo="props.appInfo"
              :projectId="props.projectId"
              class="px-1 flex-1 overflow-auto"
              @clone="cloneHandler"
              @delete="deleteScenarioHandler" />
          </AsyncComponent>
        </template>
      </template>
    </Spin>

    <ScenarioDrawer :projectId="props.projectId" :userInfo="props.userInfo" />
  </div>
</template>
