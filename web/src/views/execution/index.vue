<script setup lang="ts">
import { defineAsyncComponent, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Dropdown, Icon, Input, NoData, ScriptTypeTag, ShortDuration, Spin, Tooltip } from '@xcan-angus/vue-ui';
import { appContext, ScriptType } from '@xcan-angus/infra';
import { Button, Pagination, Progress, Switch } from 'ant-design-vue';
import { ExecStatus } from '@/enums/enums';
import { getCurrentDuration } from '@/utils';
import { useExecutionList } from './composables/useExecutionList';
import { useExecutionAction } from './composables/useExecutionAction';
import type { DropdownMenuItem, ExecutionInfo, ScriptTypeConfig, StatusColorMap } from './types';

// Lazy load components
const Introduce = defineAsyncComponent(() => import('./Introduce.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/execution/SearchPanel.vue'));

const { t } = useI18n();

const userInfo = ref(appContext.getUser());

// Use execution list composable
const {
  loaded,
  loading,
  pagination,
  dataList,
  total,
  projectId,
  nameInputRefs,
  priorityInputRefs,
  reportIntervalInputRefs,
  durationInputRefs,
  threadInputRefs,
  iterationsInputRefs,
  toSort,
  searchPanelChange,
  loadDataList,
  loadDataListByIds,
  showTotal,
  paginationChange,
  initialize,
  cleanup,
  splitTime
} = useExecutionList();

// Use execution actions composable
const {
  updateName,
  editName,
  updateThread,
  threadsMax,
  editThread,
  updateDuration,
  editDuration,
  updateIterations,
  editIterations,
  updatePriority,
  editPriority,
  updateReportInterval,
  editReportInterval,
  handleIgnoreAssertions,
  handleUpdateTestResult,
  handleRestart,
  handleStop,
  dropdownClick
} = useExecutionAction();

const getNumFixed = (str: string): string => {
  return str ? Number(str).toFixed(2) : '0';
};

// UI configuration constants
const isPrivate = ref(false);

const scriptTypes: Record<string, ScriptTypeConfig> = {
  TEST_PERFORMANCE: {
    text: 'P',
    color: 'bg-bg-performance'
  },
  TEST_FUNCTIONALITY: {
    text: 'F',
    color: 'bg-bg-function'
  },
  TEST_STABILITY: {
    text: 'S',
    color: 'bg-bg-stability'
  },
  MOCK_DATA: {
    text: 'M',
    color: 'bg-bg-mock'
  },
  TEST_CUSTOMIZATION: {
    text: 'C',
    color: 'bg-bg-custom'
  }
};

const resBgColorMap: StatusColorMap = {
  CREATED: 'bg-status-pending',
  PENDING: 'bg-status-pending',
  RUNNING: 'bg-status-process',
  STOPPED: 'bg-status-error',
  FAILED: 'bg-status-error',
  TIMEOUT: 'bg-status-error',
  COMPLETED: 'bg-status-success'
};

const dropdownMockMenuItems: DropdownMenuItem[] = [
  {
    key: 'viewLog',
    icon: 'icon-zhihangcanshu',
    name: t('execution.actions.viewScheduleLog'),
    noAuth: true
  },
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: t('actions.delete'),
    permission: 'delete'
  }
];

const dropdownMenuItems: DropdownMenuItem[] = [
  {
    key: 'edit',
    icon: 'icon-shuxie',
    name: t('execution.actions.editExecutionConfig'),
    permission: 'edit'
  },
  {
    key: 'viewLog',
    icon: 'icon-zhihangcanshu',
    name: t('execution.actions.viewScheduleLog'),
    noAuth: true
  },
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: t('actions.delete'),
    permission: 'delete'
  }
];

const selectProps = {
  style: { width: '60px' }
};

const reportIntervalSelectProps = {
  ...selectProps,
  excludes: ({ message }: { message: string }): boolean => {
    return ['ms', 'min', 'h', 'd'].includes(message);
  }
};

/**
 * Handle restart with proper context
 */
const handleRestartWithContext = async (item: ExecutionInfo): Promise<void> => {
  await handleRestart(item, loadDataListByIds);
};

/**
 * Handle stop with proper context
 */
const handleStopWithContext = async (item: ExecutionInfo): Promise<void> => {
  await handleStop(item, loadDataListByIds);
};

/**
 * Handle dropdown click with proper context
 */
const handleDropdownClick = (key: string, item: ExecutionInfo): void => {
  dropdownClick(key, item, pagination, loadDataList);
};

// Initialize component
onMounted(async () => {
  isPrivate.value = appContext.isPrivateEdition();
  watch(() => projectId.value, (newValue) => {
    if (!newValue) {
      return;
    }
    initialize();
  }, { immediate: true });
});

onBeforeUnmount(() => {
  cleanup();
});
</script>
<template>
  <Spin
    :spinning="loading"
    :delay="0"
    class="w-full h-full py-5 px-5 text-3 overflow-y-auto"
    style="scrollbar-gutter: stable;">
    <Introduce class="bg-gray-2 flex items-center rounded mb-5" />
    <SearchPanel
      v-if="projectId"
      :projectId="projectId"
      :userInfo="{ id: userInfo?.id?.toString() || '' }"
      class="mb-3.5"
      @sort="(data: any) => toSort(data)"
      @change="searchPanelChange" />
    <template v-if="loaded">
      <div v-if="dataList?.length" class="flex-1 overflow-y-auto space-y-3.5">
        <div
          v-for="item in dataList"
          :key="item.id"
          class="border border-border-divider rounded">
          <div class="px-5 flex items-center bg-bg-table-head border-b border-border-divider py-0.25 w-full space-x-2">
            <div
              class="w-5 h-5 rounded-full flex items-center justify-center text-white flex-none"
              :class="scriptTypes[item?.scriptType?.value]?.color">
              {{ scriptTypes[item?.scriptType?.value]?.text }}
            </div>
            <div class="flex-none flex space-x-2" style="width: calc(100% - 562px)">
              <div class="flex items-center h-7 justify-between pr-4" style="width:30%">
                <div class="flex items-center mr-2" style="width:calc(100% - 32px)">
                  <template v-if="!item?.editName">
                    <RouterLink
                      :to="`/execution/info/${item.id}?pageNo=${pagination.current}`"
                      class="text-3 font-medium truncate"
                      :title="item?.name">
                      {{ item?.name }}
                    </RouterLink>
                    <template
                      v-if="![ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(item?.status?.value) && item?.hasOperationPermission">
                      <Icon
                        icon="icon-shuxie"
                        class="text-text-link ml-2 cursor-pointer -mt-0.25 flex-none"
                        @click="updateName(item)" />
                    </template>
                  </template>
                  <template v-else>
                    <Input
                      :ref="el => { nameInputRefs[item.id] = el }"
                      :value="item.name"
                      size="small"
                      :maxlength="100"
                      @blur="editName($event.target.value, item)" />
                  </template>
                </div>
                <div style="width:42px" class="flex-none">
                  <tmeplate v-if="item?.trial">
                    <div
                      class="px-2 h-4 rounded-full text-3 flex-none leading-4 border border-status-success text-status-success whitespace-nowrap">
                      {{ t('execution.messages.trial') }}
                    </div>
                  </tmeplate>
                </div>
              </div>
              <div class="flex items-center" style="width:13%;max-width:90px;">
                <span
                  class="px-3 py-0.25 rounded-2xl text-center truncate cursor-pointer"
                  :title="item?.plugin"
                  style="min-width:60px;background-color:rgba(0, 119, 255, 10%);color:rgba(0, 119, 255, 100%)">
                  {{ item?.plugin }}
                </span>
              </div>
              <div class="flex items-center" style="width:13%">
                <ScriptTypeTag :value="item?.scriptType" class="transform-gpu -translate-y-0.25" />
              </div>
              <div class="flex items-center whitespace-nowrap" style="width:13%">
                <div class="w-1.5 h-1.5 mr-1 rounded" :class="resBgColorMap[item.status?.value]"></div>
                <span>{{ item?.status?.message }}</span>
                <template v-if="item?.status?.value !== ExecStatus.COMPLETED && item?.errMessage">
                  <Tooltip
                    placement="topLeft"
                    arrowPointAtCenter
                    :overlayStyle="{ 'max-width': '400px' }">
                    <Icon icon="icon-tishi1" class="ml-1 text-3.5 text-status-warn cursor-pointer" />
                    <template #title>
                      <div class="flex">
                        <div class="mr-2">
                          <div class="whitespace-nowrap mb-1">
                            {{ item?.errMessage?.codeName }}
                            <Colon />
                          </div>
                          <div class="whitespace-nowrap">
                            {{ item?.errMessage?.messageName }}
                            <Colon />
                          </div>
                        </div>
                        <div>
                          <div class="mb-1">{{ item?.errMessage?.code }}</div>
                          <div class="leading-5">{{ item?.errMessage?.message }}</div>
                        </div>
                      </div>
                    </template>
                  </Tooltip>
                </template>
              </div>
              <div class="flex items-center" style="width:20%">
                <span class="whitespace-nowrap">{{ t('execution.basic.concurrency') }}
                  <Colon class="mr-1.5" />
                </span>
                <div class="whitespace-nowrap flex-1">
                  {{ item?.sampleSummaryInfo?.threadPoolActiveSize || '0' }}/
                  <template v-if="!item?.editThread">
                    {{ item?.thread }}
                    <template
                      v-if="![ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(item?.status?.value)
                        && item?.hasOperationPermission && item?.scriptType?.value !== ScriptType.TEST_FUNCTIONALITY">
                      <Icon
                        icon="icon-shuxie"
                        class="text-text-link ml-2 cursor-pointer -mt-0.25 flex-none"
                        @click="updateThread(item)" />
                    </template>
                  </template>
                  <template v-else>
                    <Input
                      :ref="el => { threadInputRefs[item.id] = el }"
                      :value="item?.thread"
                      :min="1"
                      :max="threadsMax(item)"
                      class="w-25"
                      dataType="number"
                      size="small"
                      @blur="editThread($event.target.value, item)" />
                  </template>
                </div>
              </div>
            </div>
            <div class="flex items-center text-text-sub-content font-medium flex-none space-x-2 justify-end">
              <div>
                <span>{{ t('execution.messages.id') }}:</span>
                <span class="text-text-content"> {{ item?.id || "--" }}</span>
              </div>
              <div>
                <span class="whitespace-nowrap">{{ t('execution.basic.startTime') }}:</span>
                <span class="text-text-content  whitespace-nowrap">{{ item?.actualStartDate || "--" }}</span>
              </div>
              <div>
                <span class="whitespace-nowrap">{{ t('execution.basic.endTime') }}:</span>
                <span class="text-text-content whitespace-nowrap">{{ item?.endDate || "--" }}</span>
              </div>
            </div>
          </div>
          <div class="py-0.5 px-5 text-text-content flex items-center space-x-3.5">
            <div class="flex space-x-3.5 justify-between items-center" style="width: calc(100% - 68px)">
              <div class="flex flex-col justify-center flex-none" style="width:20%">
                <div
                  v-if="item?.durationProgress"
                  class="mb-1"
                  style="max-width:260px;">
                  <div class="flex justify-center items-center">
                    <div>{{ getCurrentDuration(item?.currentDuration, item?.duration) }}</div>
                    <div class="mx-1">/</div>
                    <div class="truncate flex items-center relative z-0 h-7">
                      <template v-if="!item?.editDuration">
                        {{ splitTime(item?.duration)[0] + splitTime(item?.duration)[1] }}
                        <template
                          v-if="![ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(item?.status?.value) && item?.hasOperationPermission">
                          <Icon
                            icon="icon-shuxie"
                            class="text-text-link ml-2 cursor-pointer -mt-0.25"
                            @click="updateDuration(item)" />
                        </template>
                      </template>
                      <template v-else>
                        <ShortDuration
                          :ref="el => { durationInputRefs[item.id] = el }"
                          :value="item.duration"
                          size="small"
                          class="flex-1"
                          style="max-width: 180px;"
                          :selectProps="selectProps"
                          placeholder="{{ t('execution.messages.executionDuration') }}"
                          @blur="editDuration($event, item)" />
                      </template>
                    </div>
                  </div>
                  <Progress
                    :showInfo="false"
                    :width="90"
                    :strokeWidth="10"
                    :strokeColor="(item as any).durationStrokeColor || 'rgba(45, 142, 255, 1)'"
                    :percent="item?.currentDurationProgress ? +item.currentDurationProgress : 0">
                  </Progress>
                </div>
                <div v-if="item?.iterationsProgress" style="max-width:260px;">
                  <div class="flex justify-center items-center -mb-1">
                    <div>{{ +item?.currentIterations || "0" }}</div>
                    <div class="mx-1">/</div>
                    <div class="truncate flex items-center relative z-0 h-7" :class="item?.editIterations ? 'w-30 ' : ''">
                      <template v-if="!item?.editIterations">
                        {{ +item?.iterations || "--" }}
                        <template
                          v-if="![ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(item?.status?.value) && item?.hasOperationPermission">
                          <Icon
                            icon="icon-shuxie"
                            class="text-text-link ml-2 cursor-pointer -mt-0.25"
                            @click="updateIterations(item)" />
                        </template>
                      </template>
                      <template v-else>
                        <Input
                          :ref="el => { iterationsInputRefs[item.id] = el }"
                          :value="item.iterations"
                          :min="1"
                          :max="item?.scriptType?.value === ScriptType.TEST_FUNCTIONALITY ? 10 : 100000000"
                          dataType="number"
                          size="small"
                          class="absolute top-0 h-5 z-9"
                          @blur="editIterations($event.target.value, item)" />
                      </template>
                    </div>
                  </div>
                  <Progress
                    :showInfo="false"
                    :width="90"
                    :strokeWidth="10"
                    :strokeColor="(item as any).iterationStrokeColor || 'rgba(45, 142, 255, 1)'"
                    :percent="item?.currentIterationsProgress ? +item.currentIterationsProgress : 0">
                  </Progress>
                </div>
              </div>
              <div style="width:38%" class="flex-none">
                <div class="flex items-center space-x-5 justify-between text-3" style="max-width:438px;">
                  <div class="rounded">
                    <div class="flex items-center border border-border-divider rounded">
                      <div
                        class="px-2.25 py-1.5  rounded-l text-text-sub-content flex items-center"
                        style="min-width: 90px;">
                        <Icon class="mr-1 text-4.5 text-status-pending" icon="icon-QPS" /><span>{{ t('execution.infoCard.qps') }}</span>
                      </div>
                      <div class="px-3.75 w-28.5 text-right rounded-r">
                        {{ getNumFixed(item?.sampleSummaryInfo?.ops || '0') }}
                      </div>
                    </div>
                    <div class="flex items-center border border-border-divider rounded mt-2">
                      <div
                        class="px-2.25 py-1.5 rounded-l text-text-sub-content flex items-center"
                        style="min-width: 90px;">
                        <Icon class="mr-1 text-4.5 text-status-success" icon="icon-RT" /><span>{{ t('execution.infoCard.avgResponseTime') }}</span>
                      </div>
                      <div class="px-3.75 w-28.5 text-right rounded-r">
                        {{
                          getNumFixed(item?.sampleSummaryInfo?.tranMean || '0')+'ms' }}
                      </div>
                    </div>
                  </div>
                  <div>
                    <div class="flex items-center border border-border-divider rounded">
                      <div
                        class="px-2.25 py-1.5  rounded-l text-text-sub-content flex items-center"
                        style="min-width: 90px;">
                        <Icon class="mr-1 text-4.5 text-status-warn" icon="icon-TPS" /><span>{{ t('execution.infoCard.tps') }}</span>
                      </div>
                      <div class="px-3.75 w-28.5 text-right rounded-r">
                        {{ getNumFixed(item?.sampleSummaryInfo?.tps || '0') }}
                      </div>
                    </div>
                    <div class="flex items-center border border-border-divider rounded mt-2">
                      <div
                        class="px-2.25 py-1.5 rounded-l text-text-sub-content flex items-center"
                        style="min-width: 90px;">
                        <Icon class="mr-1 text-4.5 text-status-error" icon="icon-cuowushuai1" /><span>{{ t('execution.infoCard.errorRate') }}</span>
                      </div>
                      <div class="px-3.75 w-28.5 text-right rounded-r">
                        {{
                          item?.sampleSummaryInfo?.errorRate ? item.sampleSummaryInfo.errorRate+'%':'0%' }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="flex leading-7 justify-center mx-3 flex-none" style="width:40%">
                <div class="flex w-1/2">
                  <div class="text-text-sub-content space-y-1 flex-none w-20">
                    <div class="whitespace-nowrap h-7 text-right">
                      {{ t('execution.basic.priority') }}:
                    </div>
                    <div class="whitespace-nowrap h-7 text-right">
                      {{ t('execution.basic.scriptName') }}:
                    </div>
                    <div class="whitespace-nowrap h-7 text-right">
                      {{ t('execution.messages.modifiedBy') }}:
                    </div>
                  </div>
                  <div class="ml-2 space-y-1" style="width: calc(100% - 68px);min-width:160px">
                    <div class="flex items-center relative z-0 h-7" :class="item?.editPriority ? 'w-30' : ''">
                      <template v-if="!item?.editPriority">
                        {{ item?.priority || "--" }}
                        <template
                          v-if="![ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(item?.status?.value) && item?.hasOperationPermission">
                          <Icon
                            icon="icon-shuxie"
                            class="text-text-link ml-2 cursor-pointer -mt-0.25"
                            @click="updatePriority(item)" />
                        </template>
                      </template>
                      <template v-else>
                        <Input
                          :ref="el => { priorityInputRefs[item.id] = el }"
                          :value="item.priority"
                          :min="1"
                          :max="2147483647"
                          dataType="number"
                          size="small"
                          class="absolute top-0 h-5 z-9"
                          @blur="editPriority($event.target.value, item)" />
                      </template>
                    </div>
                    <div class="truncate h-7" :title="item?.scriptName">
                      <template v-if="item?.scriptName">
                        <RouterLink
                          :to="`/script/detail/${item?.scriptId}?type=view`"
                          class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                          {{ item?.scriptName }}
                        </RouterLink>
                      </template>
                      <template v-else>--</template>
                    </div>
                    <div class="h-7 flex items-center">
                      <div class="truncate cursor-pointer" :title="item?.lastModifiedByName">
                        {{ item?.lastModifiedByName || "--" }}
                      </div>
                      <!--  <span class="ml-2 flex-none">{{ item?.lastModifiedDate || "&#45;&#45;" }}</span>-->
                    </div>
                  </div>
                </div>
                <div class="flex ml-3 flex-1 ">
                  <div class="text-text-sub-content leading-7 space-y-1">
                    <div class="whitespace-nowrap h-7 text-right">
                      {{ t('execution.messages.reportInterval') }}:
                    </div>
                    <div class="whitespace-nowrap h-7 text-right">
                      {{ t('execution.messages.ignoreAssertions') }}:
                    </div>
                    <div class="whitespace-nowrap h-7 text-right">
                      {{ t('execution.messages.updateTestResult') }}:
                    </div>
                  </div>
                  <div class="ml-2 space-y-1 2xl:min-w-35">
                    <div
                      class="truncate flex items-center relative z-0 h-7"
                      :class="item?.editReportInterval ? 'w-25 2xl:w-30' : ''">
                      <template v-if="!item?.editReportInterval">
                        <template v-if="item?.reportInterval">
                          {{ splitTime(item.reportInterval)[0] + splitTime(item.reportInterval)[1] }}
                        </template>
                        <template v-else>
                          --
                        </template>
                        <template
                          v-if="![ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(item?.status?.value) && item?.hasOperationPermission">
                          <Icon
                            icon="icon-shuxie"
                            class="text-text-link ml-2 cursor-pointer -mt-0.25"
                            @click="updateReportInterval(item)" />
                        </template>
                      </template>
                      <template v-else>
                        <ShortDuration
                          :ref="el => { reportIntervalInputRefs[item.id] = el }"
                          :value="item.reportInterval"
                          :max="300"
                          size="small"
                          class="flex-1"
                          :selectProps="reportIntervalSelectProps"
                          @blur="editReportInterval($event, item)" />
                      </template>
                    </div>
                    <div
                      class="truncate flex items-center relative z-0 h-7"
                      :class="item?.editReportInterval ? 'w-25' : ''">
                      <Switch
                        :checked="!!item?.ignoreAssertions"
                        :disabled="[ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(item?.status?.value) || !item?.hasOperationPermission"
                        size="small"
                        class="w-8"
                        @change="handleIgnoreAssertions(!!$event, item)" />
                    </div>
                    <div
                      class="truncate flex items-center relative z-0 h-7"
                      :class="item?.editReportInterval ? 'w-25' : ''">
                      <Switch
                        :checked="item?.updateTestResult"
                        :disabled="[ExecStatus.CREATED, ExecStatus.PENDING, ExecStatus.RUNNING].includes(item?.status?.value) || !item?.hasOperationPermission || !item.canUpdateTestResult"
                        size="small"
                        class="w-8"
                        @change="handleUpdateTestResult(!!$event, item)" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="w-15 flex-none flex flex-col items-center justify-center space-y-2">
              <template
                v-if="[ExecStatus.CREATED, ExecStatus.STOPPED, ExecStatus.FAILED, ExecStatus.COMPLETED, ExecStatus.TIMEOUT].includes(item?.status.value)
                  && item?.hasOperationPermission">
                <a class="flex items-center" @click="handleRestartWithContext(item)">
                  <Icon icon="icon-huifu" class="mr-2" /><span>{{ t('execution.actions.start') }}</span>
                </a>
              </template>
              <template v-else>
                <span class="flex items-center text-theme-disabled cursor-not-allowed">
                  <Icon icon="icon-huifu" class="mr-2" /><span>{{ t('execution.actions.start') }}</span>
                </span>
              </template>
              <template v-if="[ExecStatus.PENDING, ExecStatus.RUNNING].includes(item?.status.value) && item?.hasOperationPermission">
                <a class="flex items-center" @click="handleStopWithContext(item)">
                  <Icon icon="icon-jinyong" class="mr-2 transform -rotate-45" /><span>{{ t('execution.actions.stop') }}</span>
                </a>
              </template>
              <template v-else>
                <span class="flex items-center text-theme-disabled cursor-not-allowed">
                  <Icon icon="icon-jinyong" class="mr-2 transform -rotate-45" /><span>{{ t('execution.actions.stop') }}</span>
                </span>
              </template>
              <div class="dropdown-row">
                <Dropdown
                  v-if="item.scriptType?.value === ScriptType.MOCK_APIS"
                  :menuItems="dropdownMockMenuItems"
                  :permissions="item.actionPermission"
                  @click="handleDropdownClick($event.key, item)">
                  <Button
                    size="small"
                    type="text"
                    class="action-button more-action">
                    <Icon icon="icon-gengduo" class="more-options-icon" />
                    {{ t('actions.more') }}
                  </Button>
                </Dropdown>
                <Dropdown
                  v-else
                  :menuItems="dropdownMenuItems"
                  :permissions="item.actionPermission"
                  @click="handleDropdownClick($event.key, item)">
                  <Button
                    size="small"
                    type="text"
                    class="action-button more-action">
                    <Icon icon="icon-gengduo" class="more-options-icon mr-1" />
                    {{ t('actions.more') }}
                  </Button>
                </Dropdown>
              </div>
            </div>
          </div>
        </div>
        <Pagination
          v-if="total > 5"
          :current="pagination.current"
          :pageSize="pagination.pageSize"
          :total="total"
          :hideOnSinglePage="false"
          :showTotal="showTotal"
          :showSizeChanger="false"
          size="default"
          class="justify-end"
          @change="paginationChange" />
      </div>

      <div v-else-if="!loading">
        <NoData class="my-20" />
      </div>
    </template>
  </Spin>
</template>
<style scoped>
:deep(.ant-progress-steps-item) {
  width: 4px !important;
}
</style>
