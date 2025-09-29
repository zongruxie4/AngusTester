<script setup lang="ts">
import { defineAsyncComponent, inject, toRefs } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Popover } from 'ant-design-vue';
import { Icon, NoData, Spin } from '@xcan-angus/vue-ui';
import { ScenarioMonitorStatus } from '@/enums/enums';
import { BasicProps } from '@/types/types';

// Import types and composables
import type { TabPaneInjection } from '../types';
import { useMonitorData } from './composables/useMonitorData';
import { useMonitorActions } from './composables/useMonitorActions';
import { useMonitorUI } from './composables/useMonitorUI';

import SearchPanel from '@/views/scenario/monitor/list/SearchPanel.vue';

// Component setup
const { t } = useI18n();

// Props definition
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  onShow: false
});

// Extract reactive props
const { projectId, notify, onShow } = toRefs(props);

// Inject tab pane functions
const deleteTabPane = inject<TabPaneInjection['deleteTabPane']>('deleteTabPane', () => ({}));
const addTabPane = inject<TabPaneInjection['addTabPane']>('addTabPane', () => ({}));

// Async component
const Introduce = defineAsyncComponent(() => import('@/views/scenario/monitor/list/Introduce.vue'));

// Use composables
const {
  loaded,
  loading,
  searchedFlag,
  dataList,
  total,
  pageNo,
  pageSize,
  loadData,
  refresh,
  searchChange
} = useMonitorData(projectId, notify);

const {
  toDelete,
  editMonitor,
  handleDetail,
  run,
  getScenarioDetail
} = useMonitorActions(addTabPane, deleteTabPane, refresh);

const {
  dataListWrapRef,
  handleScrollList
} = useMonitorUI(dataList, total, loading, pageNo, pageSize, onShow, loadData);

</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce class="mb-7 flex-1" />
    </div>

    <div class="text-3.5 font-semibold mb-1">{{ t('scenarioMonitor.list.addedMonitors') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('scenarioMonitor.list.noMonitors') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/scenario#monitor?type=ADD`">
              {{ t('scenarioMonitor.actions.addMonitor') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :projectId="props.projectId"
            @change="(data) => searchChange(data)"
            @refresh="refresh" />

          <NoData v-if="dataList.length === 0" class="flex-1" />

          <div
            v-else
            ref="dataListWrapRef"
            class="flex-1"
            style="height: fit-content"
            @scroll="handleScrollList">
            <div class="flex flex-wrap">
              <div
                v-for="(item) in dataList"
                :key="item.id"
                style="width:calc(25% - 8px); margin-right: 8px;"
                class="border rounded py-2 px-4 flex-grow-0 h-37.5 mb-3">
                <div
                  class="text-theme-special text-3.5 font-medium cursor-pointer"
                  @click="handleDetail(item)">
                  {{ item.name }}
                </div>

                <div class="flex mt-2 justify-between items-center">
                  <div class="inline-flex items-center">
                    <span
                      class="text-6 font-semibold"
                      :class="item.status?.value">{{ item.status?.message }}</span>
                    <Popover>
                      <template #content>
                        <div class="max-w-80">{{ item.failureMessage }}</div>
                      </template>
                      <Icon
                        v-if="item.status?.value === ScenarioMonitorStatus.FAILURE"
                        icon="icon-tishi1"
                        class="text-status-warn ml-1" />
                    </Popover>
                  </div>

                  <div class="text-center">
                    <span
                      class="font-semibold text-3.5">{{ item.count?.successRate ? item.count?.successRate + '%' : '--' }}</span>
                    <div>{{ t('scenarioMonitor.list.successRate') }}</div>
                  </div>

                  <div class="text-center">
                    <span class="font-semibold text-3.5">{{ item.count?.avgDelayTime || '--' }}</span>
                    <div>{{ t('scenarioMonitor.list.averageDelay') }}</div>
                  </div>
                </div>

                <div class="mt-2 inline-flex max-w-full">
                  <template v-if="item.status?.value === ScenarioMonitorStatus.PENDING">
                    <span>{{ t('scenarioMonitor.list.willRun') }}「</span>
                    <a
                      class="text-blue-1 truncate min-w-0 flex-1 ml-1 mr-1"
                      :title="item.scenarioName"
                      @click="getScenarioDetail(item.scenarioId)">{{ item.scenarioName || '--' }}
                    </a>
                    <span>」{{ t('scenarioMonitor.list.in') }} {{ item.nextExecDate }}</span>
                  </template>

                  <template v-else>
                    <span>{{ t('scenarioMonitor.list.lastRun') }}「</span>
                    <a
                      class="text-blue-1 truncate min-w-0 flex-1 ml-1 mr-1"
                      :title="item.scenarioName"
                      @click="getScenarioDetail(item.scenarioId)">{{ item.scenarioName || '--' }}
                    </a>
                    <span>」{{ t('scenarioMonitor.list.in') }} {{ item.lastMonitorDate }}</span>
                  </template>
                </div>

                <div class="flex justify-between items-center mt-2">
                  <span
                    class="flex-1 min-w-0 truncate"
                    :title="`${item.createdByName} ${t('common.createdBy')} ${ item.createdDate }`">
                    {{ item.createdByName }} {{ t('common.createdBy') }} {{ item.createdDate }}
                  </span>

                  <div>
                    <Popover>
                      <template #content>
                        {{ t('scenarioMonitor.list.executeNow') }}
                      </template>
                      <Button size="small" type="text">
                        <Icon icon="icon-zhihang" @click="run(item)" />
                      </Button>
                    </Popover>
                    <Button
                      size="small"
                      type="text"
                      :title="t('actions.edit')"
                      @click="editMonitor(item)">
                      <Icon icon="icon-xiugai" />
                    </Button>
                    <Button
                      size="small"
                      type="text"
                      :title="t('actions.delete')"
                      @click="toDelete(item)">
                      <Icon icon="icon-qingchu" />
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </template>
    </Spin>
  </div>
</template>

<style scoped>

.SUCCESS {
  @apply text-status-success;
}

.PENDING {
  @apply text-status-pending;
}

.FAILURE {
  @apply text-status-error1;
}

.router-link,
.link {
  color: #1890ff;
  cursor: pointer;
}

.link:hover {
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
}

</style>
