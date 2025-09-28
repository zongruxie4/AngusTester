<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { Button, Pagination, Switch } from 'ant-design-vue';
import { DropdownSort, Icon, IconRefresh, NoData, SearchPanel, Spin } from '@xcan-angus/vue-ui';
import { EditionType } from '@xcan-angus/infra';

// Import composables
import { useNodeData } from './composables/useNodeData';
import { useSearchConfig } from './composables/useSearchConfig';
import { useSortConfig } from './composables/useSortConfig';
import { useAppContext } from './composables/useAppContext';

// Async component imports
const NodeItems = defineAsyncComponent(() => import('./components/NodeItems.vue'));
const NodeTip = defineAsyncComponent(() => import('./components/NodeTip.vue'));

// Initialize composables
const {
  nodeList,
  loading,
  pagination,
  loadNodeList,
  handlePageChange,
  handleSearchChange,
  handleSortChange,
  refreshNodeList,
  deleteItem
} = useNodeData();

// Initialize route
const route = useRoute();

const {
  roleOptions,
  searchOptions
} = useSearchConfig();

const {
  sortOptions
} = useSortConfig();

const {
  editionType,
  autoRefresh,
  isUserAdmin,
  handlePurchase,
  handleRouteQuery
} = useAppContext();

// Component references
const nodeListRef = ref();
const searchPanelRef = ref();

/**
 * Handles search form changes and triggers data reload
 *
 * @param filters - New search filters from search panel
 */
const onSearchChange = (filters: any) => {
  handleSearchChange(filters);
};

/**
 * Handles sort option changes and triggers data reload
 *
 * @param sortEvent - Sort change event from dropdown sort
 */
const onSortChange = (sortEvent: any) => {
  handleSortChange(sortEvent);
};

/**
 * Handles adding new nodes
 */
const handleAddNode = () => {
  nodeListRef.value?.add();
};

/**
 * Handles purchase flow for cloud service nodes
 */
const handleBuyNode = async () => {
  await handlePurchase();
};

// Initialize component on mount
onMounted(async () => {
  // Handle route query parameters for pre-filled search
  handleRouteQuery(searchPanelRef);

  // Load initial data if no route query
  if (!route.query.id) {
    await loadNodeList();
  }
});
</script>

<template>
  <div class="px-5 py-5 h-full overflow-auto">
    <NodeTip class="mb-5" />
    <div class="flex justify-between mb-3.5">
      <SearchPanel
        ref="searchPanelRef"
        class="flex-1 mr-3"
        :options="searchOptions"
        @change="onSearchChange" />
      <div class="flex items-center space-x-2.5">
        <Button
          type="primary"
          size="small"
          class="flex space-x-1"
          @click="handleAddNode">
          <Icon icon="icon-jia" />
          {{ $t('node.actions.addNode') }}
        </Button>
        <Button
          v-if="editionType === EditionType.CLOUD_SERVICE"
          size="small"
          class="flex space-x-1"
          @click="handleBuyNode">
          <Icon icon="icon-zaixiangoumai" />
          {{ $t('node.actions.buyNode') }}
        </Button>
        <div>
          <span class="text-3 mr-1">{{ $t('actions.autoRefresh') }}</span>
          <Switch v-model:checked="autoRefresh" size="small" />
        </div>
        <DropdownSort
          :menuItems="sortOptions as any"
          :disabled="loading"
          @click="onSortChange">
          <Button class="rounded ml-3" size="small">
            <Icon icon="icon-shunxu" class="text-3.5 mr-1" />
            <span>{{ $t('actions.sort') }}</span>
          </Button>
        </DropdownSort>
        <Button
          class="rounded ml-3"
          size="small"
          :disabled="loading"
          @click="refreshNodeList">
          <IconRefresh class="text-3.5 mr-1" :loading="loading" />
          <span>{{ $t('actions.refresh') }}</span>
        </Button>
      </div>
    </div>
    <Spin :spinning="loading" mask>
      <NodeItems
        ref="nodeListRef"
        :roleOptions="roleOptions"
        :nodeList="nodeList.list"
        :autoRefresh="autoRefresh"
        :isAdmin="isUserAdmin()"
        @loadList="refreshNodeList"
        @cancel="deleteItem" />
      <NoData v-show="pagination.total === 0" class="mt-45" />
      <Pagination
        v-show="pagination.total > 5"
        v-bind="pagination"
        class="justify-end mb-4"
        :showSizeChanger="false"
        @change="handlePageChange">
      </Pagination>
    </Spin>
  </div>
</template>

<style>
.input-sm {
  @apply h-7;
}

.install-step {
  @apply px-3 py-1.5 my-2 leading-6;

  background-color: #f6f6f6;
}

.input-sm input {
  @apply text-3 leading-3;
}

.icon-text-3.leading-3 {
  @apply text-3 !important;
  line-height: 0.75rem; /* leading-3 equivalent */
}
</style>
