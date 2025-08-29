<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Colon, DatePicker, Icon, IconRefresh, Image, Select } from '@xcan-angus/vue-ui';
import { Radio, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import DefaultAvatar from './images/default.png';
import { ProjectInfo } from './PropsType';

const { t } = useI18n();

// const userInfo = inject<Ref<{ id: string; }>>('tenantInfo', ref({ id: '' }));
const projectInfo = inject<Ref<ProjectInfo | undefined>>('projectInfo', ref());

const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showSprint: true }));

const SelectCreatorModal = defineAsyncComponent(() => import('./selectCreatorModal.vue'));
const DataView = defineAsyncComponent(() => import('@/views/kanban/dataAssets/index.vue'));
const EffectivenessView = defineAsyncComponent(() => import('@/views/kanban/effectiveness/index.vue'));
const CtoView = defineAsyncComponent(() => import('@/views/kanban/cto/index.vue'));
// const SelectApisCaseModal = defineAsyncComponent(() => import('@/components/SelectApisCaseModal/index.vue'));

const dataViewRef = ref();
const effectivenessRef = ref();
const ctoRef = ref();
const Maximizing = ref(false);
const viewMode = ref<'data' | 'effectiveness' | 'cto'>('effectiveness');

const projectId = ref<string>();
const dateRange = ref<[string, string] | undefined>([]);
const creatorObjectType = ref<'GROUP' | 'USER' | 'DEPT'>();
const creatorObjectId = ref<string>();
const creatorObjectName = ref<string>();
const avatar = ref<string>();
const creatorModalVisible = ref(false);

const countType = ref<'task' | 'useCase'>('task');
const selectedSprintId = ref<string>();
const selectedPlanId = ref<string>();

const delCreator = () => {
  creatorObjectType.value = undefined;
  creatorObjectId.value = undefined;
  creatorObjectName.value = undefined;
};

const setCreator = (creator: { creatorObjectType?: 'GROUP' | 'USER' | 'DEPT'; creatorObjectId?: string; creatorObjectName?: string; avatar?: string }) => {
  creatorObjectType.value = creator.creatorObjectType;
  creatorObjectId.value = creator.creatorObjectId;
  creatorObjectName.value = creator.creatorObjectName;
};

const modifyCreator = () => {
  creatorModalVisible.value = true;
};

const refrsh = () => {
  if (typeof dataViewRef.value?.refresh === 'function' && viewMode.value === 'data') {
    dataViewRef.value.refresh();
  }
  if (typeof effectivenessRef.value?.refresh === 'function' && viewMode.value === 'effectiveness') {
    effectivenessRef.value.refresh();
  }
  if (typeof ctoRef.value?.refresh === 'function' && viewMode.value === 'cto') {
    ctoRef.value.refresh();
  }
};

const changeMax = () => {
  Maximizing.value = !Maximizing.value;
  if (typeof dataViewRef.value?.handleWindowResize === 'function' && viewMode.value === 'data') {
    dataViewRef.value.handleWindowResize();
  }
  if (typeof effectivenessRef.value?.handleWindowResize === 'function' && viewMode.value === 'effectiveness') {
    effectivenessRef.value.handleWindowResize();
  }
  if (typeof ctoRef.value?.handleWindowResize === 'function' && viewMode.value === 'cto') {
    ctoRef.value.handleWindowResize();
  }
};

onMounted(() => {
  watch(() => projectInfo.value, (newValue) => {
    if (newValue?.id) {
      projectId.value = newValue.id;
      avatar.value = newValue.avatar;
      selectedSprintId.value = undefined;
      selectedPlanId.value = undefined;
    }
  }, {
    immediate: true
  });

  watch(() => proTypeShowMap.value, () => {
    if (!proTypeShowMap.value.showTask) {
      countType.value = 'useCase';
    }
  }, {
    immediate: true
  });
});

// const userId = computed(() => {
//   return userInfo.value?.id;
// });

const viewTypeOpt = [
  {
    value: 'cto',
    label: t('kanban.viewType.cto')
  },
  {
    value: 'effectiveness',
    label: t('kanban.viewType.effectiveness')
  },
  {
    value: 'data',
    label: t('kanban.viewType.dataAssets')
  }
];

</script>
<template>
  <div class="px-5 pt-2 pb-5 h-full leading-5 text-3  overflow-y-auto bg-white" :class="{'fixed top-0 left-0 bottom-0 right-0 z-999': Maximizing}">
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
            @click="delCreator" />
          <Icon
            v-show="!creatorObjectName"
            icon="icon-shuxie"
            class="cursor-pointer text-text-link text-3.5 flex-shrink-0"
            @click="modifyCreator" />
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
          :options="viewTypeOpt" />
        <IconRefresh class="text-4" @click="refrsh" />
        <Icon
          class="text-4 cursor-pointer"
          :icon=" Maximizing ? 'icon-tuichuzuida' : 'icon-zuidahua' "
          @click="changeMax" />
      </div>
    </div>

    <Tabs v-model:activeKey="viewMode" class="board_tab">
      <TabPane key="data">
        <DataView
          ref="dataViewRef"
          :onShow="viewMode === 'data'"
          :projectId="projectId"
          :createdDateStart="dateRange?.[0]"
          :createdDateEnd="dateRange?.[1]"
          :creatorObjectType="creatorObjectType"
          :creatorObjectId="creatorObjectId" />
      </TabPane>

      <TabPane key="effectiveness">
        <EffectivenessView
          ref="effectivenessRef"
          :onShow="viewMode === 'effectiveness'"
          :projectId="projectId"
          :countType="countType"
          :sprintId="selectedSprintId"
          :planId="selectedPlanId"
          :createdDateStart="dateRange?.[0]"
          :createdDateEnd="dateRange?.[1]"
          :creatorObjectType="creatorObjectType"
          :creatorObjectId="creatorObjectId" />
      </TabPane>

      <TabPane key="cto">
        <CtoView
          ref="ctoRef"
          :onShow="viewMode === 'cto'"
          :projectId="projectId"
          :countType="countType"
          :sprintId="selectedSprintId"
          :planId="selectedPlanId"
          :createdDateStart="dateRange?.[0]"
          :createdDateEnd="dateRange?.[1]"
          :creatorObjectType="creatorObjectType"
          :creatorObjectId="creatorObjectId" />
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
