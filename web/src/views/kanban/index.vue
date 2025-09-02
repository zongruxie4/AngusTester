<script setup lang="ts">
import { defineAsyncComponent, inject, Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Colon, DatePicker, Icon, IconRefresh, Image, Select } from '@xcan-angus/vue-ui';
import { Radio, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import DefaultAvatar from './images/default.png';
import type { ProjectInfo } from './types';
import type { CreatorObjectType } from './types';
import { useKanbanFilters } from './composables/useKanbanFilters';
import { useKanbanView } from './composables/useKanbanView';
import { useKanbanOptions } from './composables/useKanbanOptions';

const { t } = useI18n();

// external contexts
const projectInfo = inject<Ref<ProjectInfo | undefined>>('projectInfo', ref());
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showSprint: true }));

// async components
const SelectCreatorModal = defineAsyncComponent(() => import('./SelectCreatorModal.vue'));
const DataView = defineAsyncComponent(() => import('@/views/kanban/assets/index.vue'));
const EffectivenessView = defineAsyncComponent(() => import('@/views/kanban/effectiveness/index.vue'));
const CtoView = defineAsyncComponent(() => import('@/views/kanban/cto/index.vue'));

// view management
const { isMaximized, viewMode, dataViewRef, effectivenessRef, ctoRef, refreshActive, toggleMaximize } = useKanbanView();

// filters and derived safe values
const {
  projectId,
  avatar,
  dateRange,
  creatorObjectType,
  creatorObjectId,
  creatorObjectName,
  countType,
  selectedSprintId,
  selectedPlanId,
  projectIdSafe,
  sprintIdSafe,
  planIdSafe,
  createdDateStartSafe,
  createdDateEndSafe,
  creatorObjectTypeSafe,
  creatorObjectIdSafe,
  clearCreator,
  setCreator
} = useKanbanFilters({ projectInfo });

// react to product type visibility
watch(
  () => proTypeShowMap.value,
  () => {
    if (!proTypeShowMap.value.showTask) {
      countType.value = 'useCase';
    }
  },
  { immediate: true }
);

// ui state
const creatorModalVisible = ref(false);
const openCreatorModal = () => { creatorModalVisible.value = true; };

// options
const { viewTypeOptions } = useKanbanOptions();

</script>
<template>
  <div class="px-5 pt-2 pb-5 h-full leading-5 text-3  overflow-y-auto bg-white" :class="{'fixed top-0 left-0 bottom-0 right-0 z-999': isMaximized}">
    <div class="flex items-center border px-2 py-1 rounded">
      <Image
        :src="avatar"
        :defaultImg="DefaultAvatar"
        class="w-10 h-10 rounded-full flex-shrink-0 mr-2.5" />
      <div class="flex items-center flex-1 flex-nowrap space-x-5">
        <div class="flex items-center space-x-1">
          <div class="flex items-center flex-shrink-0">
            <span class="flex-shrink-0 whitespace-nowrap">{{ t('kanban.organizationPersonnel') }}</span>
            <Colon class="mr-1 flex-shrink-0" />
            <div :title="creatorObjectName" class="truncate max-w-20 flex-shrink-0 whitespace-nowrap">
              {{
                creatorObjectName || '--' }}
            </div>
          </div>
          <Icon
            v-show="!!creatorObjectName"
            icon="icon-cuowu"
            class="cursor-pointer text-text-link text-3.5 flex-shrink-0"
            @click="clearCreator" />
          <Icon
            v-show="!creatorObjectName"
            icon="icon-shuxie"
            class="cursor-pointer text-text-link text-3.5 flex-shrink-0"
            @click="openCreatorModal" />
        </div>

        <template v-if="['effectiveness', 'cto'].includes(viewMode)">
          <div class="flex items-center flex-shrink-0">
            <div class="mr-1 flex-shrink-0">
              <span class="flex-shrink-0 whitespace-nowrap">{{ t('kanban.statisticsType') }}</span>
              <Colon />
            </div>
            <RadioGroup
              v-model:value="countType"
              name="countType"
              class="radio-group-small">
              <Radio v-if="proTypeShowMap.showTask" value="task">{{ t('kanban.task') }}</Radio>
              <Radio value="useCase">{{ t('kanban.test') }}</Radio>
            </RadioGroup>
          </div>

          <Select
            v-show="countType === 'task' && proTypeShowMap.showSprint"
            v-model:value="selectedSprintId"
            class="w-45 flex-shrink-0"
            allowClear
            :disabled="!projectId"
            :fieldNames="{ value: 'id', label: 'name' }"
            :action="projectId ? `${TESTER}/task/sprint?projectId=${projectId}&fullTextSearch=true` : undefined"
            :placeholder="t('kanban.pleaseSelectSprint')" />

          <Select
            v-show="countType === 'useCase'"
            v-model:value="selectedPlanId"
            class="w-45 flex-shrink-0"
            allowClear
            :disabled="!projectId"
            :fieldNames="{ value: 'id', label: 'name' }"
            :action="projectId ? `${TESTER}/func/plan?projectId=${projectId}&fullTextSearch=true` : undefined"
            :placeholder="t('kanban.pleaseSelectPlan')" />
        </template>

        <div class="flex items-center flex-shrink-0">
          <span class="flex-shrink-0 whitespace-nowrap">{{ t('kanban.time') }}</span>
          <Colon class="mr-1 flex-shrink-0" />
          <DatePicker
            v-model:value="dateRange"
            showTime
            class="w-71 flex-shrink-0"
            type="date-range" />
        </div>
      </div>

      <div class="flex items-center flex-nowrap flex-shrink-0 space-x-2.5">
        <RadioGroup
          v-model:value="viewMode"
          size="small"
          optionType="button"
          buttonStyle="solid"
          class="child-px-2.5"
          :options="viewTypeOptions" />
        <IconRefresh class="text-4" @click="refreshActive" />
        <Icon
          class="text-4 cursor-pointer"
          :icon=" isMaximized ? 'icon-tuichuzuida' : 'icon-zuidahua' "
          @click="toggleMaximize" />
      </div>
    </div>

    <Tabs v-model:activeKey="viewMode" class="board_tab">
      <TabPane key="data">
        <DataView
          ref="dataViewRef"
          :onShow="viewMode === 'data'"
          :projectId="projectIdSafe"
          :createdDateStart="createdDateStartSafe"
          :createdDateEnd="createdDateEndSafe"
          :creatorObjectType="creatorObjectTypeSafe as CreatorObjectType"
          :creatorObjectId="creatorObjectIdSafe" />
      </TabPane>

      <TabPane key="effectiveness">
        <EffectivenessView
          ref="effectivenessRef"
          :onShow="viewMode === 'effectiveness'"
          :projectId="projectIdSafe"
          :countType="countType"
          :sprintId="sprintIdSafe"
          :planId="planIdSafe"
          :createdDateStart="createdDateStartSafe"
          :createdDateEnd="createdDateEndSafe"
          :creatorObjectType="creatorObjectTypeSafe as CreatorObjectType"
          :creatorObjectId="creatorObjectIdSafe" />
      </TabPane>

      <TabPane key="cto">
        <CtoView
          ref="ctoRef"
          :onShow="viewMode === 'cto'"
          :projectId="projectIdSafe"
          :countType="countType"
          :sprintId="sprintIdSafe"
          :planId="planIdSafe"
          :createdDateStart="createdDateStartSafe"
          :createdDateEnd="createdDateEndSafe"
          :creatorObjectType="creatorObjectTypeSafe as CreatorObjectType"
          :creatorObjectId="creatorObjectIdSafe" />
      </TabPane>
    </Tabs>

    <AsyncComponent :visible="creatorModalVisible">
      <SelectCreatorModal v-model:visible="creatorModalVisible" @ok="setCreator" />
    </AsyncComponent>
    <!-- <SelectApisCaseModal :projectId="projectId" :visible="true" /> -->
  </div>
</template>
<style scoped>
:deep(.board_tab > .ant-tabs-nav) {
  display: none;
}

.radio-group-small :deep(.ant-radio-wrapper) {
  margin-right: 8px;
}

.radio-group-small :deep(.ant-radio-wrapper:last-child) {
  margin-right: 0;
}

.radio-group-small :deep(.ant-radio-wrapper) span.ant-radio+* {
  padding-right: 0;
}

.child-px-2\.5 :deep(.ant-radio-button-wrapper) {
  padding: 0 10px;
}

</style>
