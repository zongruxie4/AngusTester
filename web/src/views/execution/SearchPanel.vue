<script setup lang="ts">
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { DropdownSort, Icon, Input, SearchPanel, Select } from '@xcan-angus/vue-ui';
import { TESTER, PageQuery, SearchCriteria, EditionType } from '@xcan-angus/infra';
import SelectEnum from '@/components/form/enum/SelectEnum.vue';
import { useSearchPanel } from './composables/useSearchPanel';
import { BasicProps } from '@/types/types';
import { QuickSearchOptions } from 'src/components/form/quickSearch';

type OrderByKey = 'createdDate' | 'createdByName';

const { t } = useI18n();

// Define component props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' }),
  appInfo: () => ({ id: '' }),
  notify: ''
});

// Define component emits

const emit = defineEmits<{
  (e: 'sort', value: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort; }): void;
  (e: 'change', value: SearchCriteria[]): void;
}>();

// Use search panel composable
const {
  editionType,
  isPrivate,
  searchPanelRef,
  nodeQuota,
  scriptSourceIdFilter,
  priorityFilter,
  isServiceTargetType,
  isAPITargetType,
  isScenarioTargetType,
  searchOptions,
  sortMenus,
  loadScriptTypeEnum,
  loadNodeQuota,
  searchPanelChange,
  scriptSourceIdChange,
  priorityInputChange,
  prioritySelectChange,
  initialize,
  toRefresh,
  quickSearchConfig,
  handleQuickSearchChange
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
    <!-- Quick Search Options Component -->
    <QuickSearchOptions
      :config="quickSearchConfig"
      @change="handleQuickSearchChange" />

    <div class="flex items-start justify-between mb-1.5 space-x-5">
      <SearchPanel
        ref="searchPanelRef"
        class="flex-1 min-w-0"
        :options="searchOptions as any"
        @change="handleSearchPanelChange">
        <template #priority>
          <Input
            :value="priorityFilter.value"
            dataType="float"
            size="small"
            allowClear
            :placeholder="t('common.priority')"
            class="!w-72 ml-2"
            :min="0"
            @change="(e: any) => priorityInputChange(e)">
            <template #prefix>
              <SelectEnum
                :value="priorityFilter.op"
                size="small"
                enumKey="NumberCompareCondition"
                :allowClear="false"
                :bordered="false"
                class="w-24"
                @change="(value: any) => prioritySelectChange(value)" />
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
            @change="(value: any) => scriptSourceIdChange(value)" />

          <Select
            v-if="isAPITargetType"
            :value="scriptSourceIdFilter.value"
            :action="`${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'summary', value: 'id' }"
            :allowClear="true"
            :placeholder="t('execution.searchPanel.selectApi')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => scriptSourceIdChange(value)">
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
            @change="(value: any) => scriptSourceIdChange(value)">
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
          v-if="editionType === EditionType.CLOUD_SERVICE"
          type="primary"
          size="small"
          class="p-0">
          <RouterLink
            class="flex items-center space-x-1 leading-6.5 px-1.75"
            to="/execution/experience?type=expr">
            <Icon icon="icon-jia" class="text-3.5" />
            <span>{{ t('actions.experienceExec') }}</span>
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
            <span>{{ t('actions.addExecution') }}</span>
          </RouterLink>
        </Button>

        <DropdownSort :menuItems="sortMenus as any" @click="toSort">
          <Button size="small">
            <Icon icon="icon-biaotoupaixu" class="text-3.5 mr-1" />
            <span>{{ t('actions.sort') }}</span>
          </Button>
        </DropdownSort>

        <Button size="small" @click="handleRefresh">
          <Icon icon="icon-shuaxin" class="mr-1 text-3.5" />
          <span>{{ t('actions.refresh') }}</span>
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Styles are now handled by QuickSearchOptions component */
</style>
