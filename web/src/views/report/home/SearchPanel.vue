<script setup lang="ts">
import { Button } from 'ant-design-vue';
import { Colon, Icon, SearchPanel, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useSearchPanel } from './composables/useSearchPanel';
import type { SearchPanelEmits, SearchPanelProps } from './types';

// Props and emits definition
const props = withDefaults(defineProps<SearchPanelProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const emit = defineEmits<SearchPanelEmits>();

// Use search panel composable
const {
  searchOptions,
  menuItems,
  selectedMenuMap,
  targetIdFilter,
  menuItemClick,
  searchPanelChange,
  targetIdChange,
  toRefresh,
  toAuth,
  searchPanelRef,
  isProjectTargetType,
  isServiceTargetType,
  isAPITargetType,
  isTaskTargetType,
  isSprintTargetType,
  isPlanTargetType,
  isCaseTargetType,
  isExecutionTargetType,
  isScenarioTargetType
} = useSearchPanel(props, emit);

</script>

<template>
  <div class="text-3 leading-5">
    <!-- Quick Query Section -->
    <div class="flex items-start justify-between mb-1.5">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
          <span>{{ $t('reportHome.searchPanel.quickQuery') }}</span>
          <Colon />
        </div>
        <div class="flex flex-wrap ml-2">
          <div
            v-for="item in menuItems"
            :key="item.key"
            :class="{ 'active-key': selectedMenuMap.has(item.key) }"
            class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
            @click="menuItemClick(item)">
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>

    <!-- Search Panel Section -->
    <div class="flex items-start justify-between space-x-5">
      <SearchPanel
        ref="searchPanelRef"
        class="flex-1"
        :options="searchOptions"
        @change="searchPanelChange">
        <template #targetId>
          <!-- Project Select -->
          <Select
            v-if="isProjectTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/project?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="$t('reportHome.searchPanel.resourceSelect.project')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => targetIdChange(String(value || ''))" />

          <!-- Service Select -->
          <Select
            v-if="isServiceTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="$t('reportHome.searchPanel.resourceSelect.service')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => targetIdChange(String(value || ''))" />

          <!-- API Select -->
          <Select
            v-if="isAPITargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/apis?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'summary', value: 'id' }"
            :allowClear="true"
            :placeholder="$t('reportHome.searchPanel.resourceSelect.api')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => targetIdChange(String(value || ''))">
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

          <!-- Task Select -->
          <Select
            v-if="isTaskTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="$t('reportHome.searchPanel.resourceSelect.task')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => targetIdChange(String(value || ''))" />

          <!-- Sprint Select -->
          <Select
            v-if="isSprintTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="$t('reportHome.searchPanel.resourceSelect.sprint')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => targetIdChange(String(value || ''))" />

          <!-- Plan Select -->
          <Select
            v-if="isPlanTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/func/plan?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="$t('reportHome.searchPanel.resourceSelect.plan')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => targetIdChange(String(value || ''))" />

          <!-- Case Select -->
          <Select
            v-if="isCaseTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/func/case?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="$t('reportHome.searchPanel.resourceSelect.case')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => targetIdChange(String(value || ''))" />

          <!-- Execution Select -->
          <Select
            v-if="isExecutionTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/exec?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="$t('reportHome.searchPanel.resourceSelect.execution')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => targetIdChange(String(value || ''))" />

          <!-- Scenario Select -->
          <Select
            v-if="isScenarioTargetType"
            :value="targetIdFilter.value"
            :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }"
            :allowClear="true"
            :placeholder="$t('reportHome.searchPanel.resourceSelect.scenario')"
            class="w-72 ml-2"
            showSearch
            @change="(value: any) => targetIdChange(String(value || ''))" />
        </template>
      </SearchPanel>

      <!-- Action Buttons -->
      <div class="flex-shrink-0 flex items-center space-x-2.5">
        <Button
          type="primary"
          size="small"
          class="flex space-x-1"
          @click="emit('add')">
          <Icon icon="icon-jia" class="text-3.5" />
          <span>{{ $t('reportHome.searchPanel.actions.addReport') }}</span>
        </Button>

        <Button size="small" @click="toAuth">
          <Icon icon="icon-quanxian1" class="mr-1 text-3.5" />
          <span>{{ $t('reportHome.searchPanel.actions.reportPermission') }}</span>
        </Button>

        <Button size="small" @click="toRefresh">
          <Icon icon="icon-shuaxin" class="mr-1 text-3.5" />
          <span>{{ $t('actions.refresh') }}</span>
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
