<script lang="ts" setup>
import { DropdownSort, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { QuickSearchOptions } from '@/components/quickSearch';

// Import types and composables
import { useSearchPanelData } from './composables/useSearchPanelData';
import { useSearchPanelAction } from './composables/useSearchPanelAction';

// Component props
interface Props {
  loading: boolean;
  projectId: string;
  userId?: string;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  projectId: undefined,
  userId: undefined
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
  quickSearchConfig,
  orderBy,
  orderSort,
  searchFilters,
  quickSearchFilters,
  assocFilters,
  getParams,
  searchPanelRef,
} = useSearchPanelData(props.projectId);


// Use logic composable
const {
  searchChange,
  toSort,
  refresh,
  quickSearchOptionsRef,
  handleQuickSearchChange
} = useSearchPanelAction(
  searchPanelRef,
  props.userId as string,
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
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      ref="quickSearchOptionsRef"
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

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
            <span>{{ $t('scenarioMonitor.actions.addMonitor') }}</span>
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
/* Styles are now handled by QuickSearchOptions component */
</style>
