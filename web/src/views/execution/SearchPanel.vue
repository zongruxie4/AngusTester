<script setup lang="ts">
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Colon, DropdownSort, Icon, Input, SearchPanel, Select } from '@xcan-angus/vue-ui';
import { TESTER, PageQuery } from '@xcan-angus/infra';
import SelectEnum from '@/components/selectEnum/index.vue';
import { useSearchPanel } from './composables/useSearchPanel';
import type { FilterItem, SearchPanelProps } from './types';

type OrderByKey = 'createdDate' | 'createdByName';

const { t } = useI18n();

// Define component props
const props = withDefaults(defineProps<SearchPanelProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' }),
  appInfo: () => ({ id: '' }),
  notify: ''
});

// Define component emits
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'sort', value: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort; }): void;
  (e: 'change', value: FilterItem[]): void;
}>();

// Use search panel composable
const {
  editionType,
  isPrivate,
  searchPanelRef,
  nodeQuota,
  selectedMenuMap,
  scriptSourceIdFilter,
  priorityFilter,
  isServiceTargetType,
  isAPITargetType,
  isScenarioTargetType,
  menuItems,
  searchOptions,
  sortMenus,
  loadScriptTypeEnum,
  loadNodeQuota,
  menuItemClick,
  searchPanelChange,
  scriptSourceIdChange,
  priorityInputChange,
  prioritySelectChange,
  initialize,
  toRefresh
} = useSearchPanel(props);

/**
 * Handle sorting
 */
const toSort = (data: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort; }): void => {
  emit('sort', data);
};

/**
 * Handle refresh action
 */
const handleRefresh = (): void => {
  toRefresh(emit);
};

/**
 * Handle menu item click
 */
const handleMenuItemClick = (data: any): void => {
  menuItemClick(data, emit);
};

/**
 * Handle search panel change
 */
const handleSearchPanelChange = (data: any): void => {
  searchPanelChange(data);
};

// Initialize component
onMounted(async () => {
  loadScriptTypeEnum();
  await loadNodeQuota();
  await initialize(emit);
});
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex items-center mb-3">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 mt-1 rounded-full"></div>
        <div class="whitespace-nowrap text-3 mt-0.5 text-text-sub-content">
          <span>{{ t('execution.searchPanel.quickSearch') }}</span>
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
        <template #priority>
          <Input
            :value="priorityFilter.value"
            dataType="float"
            size="small"
            allowClear
            :placeholder="t('execution.searchPanel.priority')"
            class="!w-72 ml-2"
            :min="0"
            @change="priorityInputChange">
            <template #prefix>
              <SelectEnum
                :value="priorityFilter.op"
                size="small"
                enumKey="NumberCompareCondition"
                :allowClear="false"
                :bordered="false"
                class="w-24"
                @change="prioritySelectChange" />
            </template>
          </Input>
        </template>

        <template #scriptSourceId>
          <Select
            v-if="isServiceTargetType"
            :value="scriptSourceIdFilter.value"
            :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="t('execution.searchPanel.selectService')"
            class="w-72 ml-2"
            showSearch
            @change="scriptSourceIdChange" />

          <Select
            v-if="isAPITargetType"
            :value="scriptSourceIdFilter.value"
            :action="`${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'summary', value: 'id' }"
            :allowClear="true"
            :placeholder="t('execution.searchPanel.selectApi')"
            class="w-72 ml-2"
            showSearch
            @change="scriptSourceIdChange">
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
            v-if="isScenarioTargetType"
            :value="scriptSourceIdFilter.value"
            :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="t('execution.searchPanel.selectScenario')"
            class="w-72 ml-2"
            showSearch
            @change="scriptSourceIdChange">
            <template #option="record">
              <div class="flex items-center">
                <Icon
                  icon="icon-changjingceshi"
                  class="text-4 flex-shrink-0"
                  style="color:rgba(82,196,26,100%);" />
                <div :title="record.name" class="truncate ml-1.5">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </template>
      </SearchPanel>
      <div class="flex-shrink-0 flex items-center space-x-2.5">
        <Button
          v-if="editionType === 'CLOUD_SERVICE'"
          type="primary"
          size="small"
          class="p-0">
          <RouterLink
            class="flex items-center space-x-1 leading-6.5 px-1.75"
            to="/execution/experience?type=expr">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('execution.searchPanel.experienceExecution') }}</span>
          </RouterLink>
        </Button>

        <Button
          v-show="isPrivate || nodeQuota >= 1"
          type="primary"
          size="small"
          class="p-0">
          <RouterLink
            class="flex items-center space-x-1 leading-6.5 px-1.75"
            to="/execution/add">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('execution.searchPanel.addExecution') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort :menuItems="sortMenus" @click="toSort">
          <Button size="small">
            <Icon icon="icon-biaotoupaixu" class="text-3.5 mr-1" />
            <span>{{ t('execution.searchPanel.sort') }}</span>
          </Button>
        </DropdownSort>

        <Button size="small" @click="handleRefresh">
          <Icon icon="icon-shuaxin" class="mr-1 text-3.5" />
          <span>{{ t('execution.searchPanel.refresh') }}</span>
        </Button>
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
