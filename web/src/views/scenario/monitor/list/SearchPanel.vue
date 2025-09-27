<script lang="ts" setup>
import { ref } from 'vue';
import { Colon, DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { PageQuery } from '@xcan-angus/infra';

// Import types and composables
import { useSearchPanelData } from './composables/useSearchPanelData';
import { useSearchPanelAction } from './composables/useSearchPanelAction';

// Component props
interface Props {
  loading: boolean;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  projectId: undefined
});

// Component emits
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: PageQuery): void;
  (e: 'refresh'): void;
}>();

// Use composables
const {
  searchPanelOptions,
  sortMenuItems,
  menuItems,
  selectedMenuMap,
  orderBy,
  orderSort,
  searchFilters,
  quickSearchFilters,
  assocFilters,
  getParams
} = useSearchPanelData(props.projectId);

// Search panel ref
const searchPanelRef = ref();

// Use logic composable
const {
  searchChange,
  toSort,
  menuItemClick,
  refresh
} = useSearchPanelAction(
  searchPanelRef,
  selectedMenuMap,
  searchFilters,
  assocFilters,
  quickSearchFilters,
  orderBy,
  orderSort,
  getParams,
  (value: PageQuery) => emits('change', value),
  () => emits('refresh')
);
</script>

<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex">
      <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
        <span>{{ $t('quickSearch.title') }}</span>
        <Colon />
      </div>
      <div class="flex  flex-wrap ml-2">
        <div
          v-for="item in menuItems"
          :key="item.key"
          :class="{ 'active-key': selectedMenuMap[item.key] }"
          class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
          @click="menuItemClick(item)">
          {{ item.name }}
        </div>
      </div>
    </div>
    <div class="flex items-start justify-between ">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="searchChange" />

      <div class="flex items-center space-x-3">
        <Button
          type="primary"
          size="small"
          class="p-0">
          <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/scenario#monitor?type=ADD`">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ $t('scenarioMonitor.searchPanel.addMonitor') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort
          v-model:orderBy="orderBy"
          v-model:orderSort="orderSort"
          :menuItems="sortMenuItems as any"
          @click="toSort">
          <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
            <Icon icon="icon-shunxu" class="text-3.5" />
            <span>{{ $t('actions.sort') }}</span>
          </div>
        </DropdownSort>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="refresh">
          <template #default>
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-shuaxin" class="text-3.5" />
              <span class="ml-1">{{ $t('actions.refresh') }}</span>
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
