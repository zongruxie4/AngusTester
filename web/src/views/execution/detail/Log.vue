<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Arrow, Colon, Icon, Select, Tooltip } from '@xcan-angus/vue-ui';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { useLogData } from './composables/useLogData';

/**
 * Props for the Log component
 */
interface Props {
  /** Execution ID */
  execId: string;
  /** Loading state */
  loading: boolean;
  /** Execution nodes */
  execNodes: {id:string, name:string, ip:string, agentPort: string; publicIp?:string}[];
  /** Last scheduling date */
  lastSchedulingDate: string;
  /** Scheduling number */
  schedulingNum: string;
  /** Last scheduling result */
  lastSchedulingResult: {console:string[], deviceId:string, success:boolean, exitCode:string}[];
}

// Initialize internationalization
const { t } = useI18n();

// Define component props with defaults
const props = withDefaults(defineProps<Props>(), {
  execId: '',
  loading: false,
  execNodes: () => [],
  lastSchedulingDate: '',
  schedulingNum: '',
  lastSchedulingResult: () => []
});

// Define component events
const emit = defineEmits<{(e: 'update:loading', value: boolean): void}>();

// Use log data composable for logic
const {
  nodeId,
  nodeIp,
  execLogContent,
  execLogPath,
  execLogErr,
  errorText,
  showSchedulingLog,
  showExecLog,
  schedulingLogItem,
  nodeSelectChange,
  handleDoubleClick,
  openSchedulingLog,
  openExecLog,
  downloadLog,
  refreshExecLog
} = useLogData(
  props.execId,
  () => props.loading,
  props.execNodes,
  props.lastSchedulingResult,
  emit
);
</script>
<template>
  <div class="h-full flex text-3 py-2 pl-2">
    <div class="w-60 flex-none pr-2">
      <Select
        :value="nodeId"
        :options="props.execNodes"
        :fieldNames="{label:'name',value:'id'}"
        class="w-full"
        placeholder="{{ t('execution.infoLog.selectNode') }}"
        size="small"
        @change="(value, option) => nodeSelectChange(value as string, option)">
        <template #option="item">
          {{ `${item.name} ( ${item?.publicIp || item.ip} )` }}
        </template>
      </Select>
      <div class="flex ml-1.5 mt-5">
        <div class="mr-2 space-y-5">
          <div class="h-3">{{ t('execution.infoLog.totalSchedulingCount') }}<Colon /></div>
          <div class="h-3">{{ t('execution.infoLog.lastSchedulingTime') }}<Colon /></div>
        </div>
        <div class="space-y-5">
          <div class="h-3">{{ props.schedulingNum }}</div>
          <div class="h-3">{{ props.lastSchedulingDate }}</div>
        </div>
      </div>
    </div>
    <div
      class="flex-1 pl-8 h-full overflow-y-auto pr-2"
      style="scrollbar-gutter: stable;">
      <Collapse
        v-model:activeKey="showSchedulingLog"
        class="!bg-transparent"
        :bordered="false">
        <CollapsePanel
          key="1"
          :showArrow="false"
          collapsible="disabled">
          <template #header>
            <div
              class="h-5 flex justify-between items-center text-3 text-text-content  cursor-pointer mb-1 flex-1"
              @click="handleDoubleClick('scheduling')">
              <div class="flex items-center space-x-2">
                <div class="h-3 bg-text-link-hover w-1"></div>
                <span class="text-text-title font-medium text-3 leading-3" @click.stop="openSchedulingLog">{{ t('execution.infoLog.schedulingLog') }}</span>
                <Arrow :open="showSchedulingLog === '1'" @click.stop="openSchedulingLog" />
                <div class="!ml-20">{{ t('execution.infoLog.schedulingResult') }}<Colon /></div>
                <template v-if="props.execNodes?.length">
                  <div class="flex items-center">
                    <div class="w-1.5 h-1.5 mr-1 rounded" :class="schedulingLogItem?.success?'bg-status-success':'bg-status-error'"></div>
                    {{ schedulingLogItem?.success ? t('execution.infoLog.success') : t('execution.infoLog.failure') }}
                  </div>
                </template>
                <template v-else>
                  --
                </template>
                <div class="!ml-20">{{ t('execution.infoLog.processExitCode') }}<Colon /></div>
                <template v-if="props.execNodes?.length">
                  <div>{{ schedulingLogItem?.exitCode || '--' }}</div>
                </template>
                <template v-else>
                  --
                </template>
              </div>
              <div>
                <Tooltip :title="t('execution.infoLog.export')" placement="top">
                  <Icon
                    icon="icon-daochu1"
                    class="text-4 cursor-pointer hover:text-text-link-hover mr-2"
                    @click="downloadLog('scheduling')" />
                </Tooltip>
              </div>
            </div>
          </template>
          <div
            class="whitespace-pre-wrap leading-4 text-text-content text-3 px-2 pt-2 pb-5 border border-border-divider rounded"
            style="min-height: 140px; font-family: Monaco, Consolas, monospace !important;">
            {{ schedulingLogItem ? schedulingLogItem.console?.join('\n') : '' }}
          </div>
        </CollapsePanel>
      </Collapse>
      <Collapse
        v-model:activeKey="showExecLog"
        class="!bg-transparent mt-5"
        :bordered="false">
        <CollapsePanel
          key="1"
          :showArrow="false"
          collapsible="disabled">
          <template #header>
            <div
              class="flex flex-1 justify-between items-center text-3 text-text-content cursor-pointer mb-1"
              @click="handleDoubleClick('exec')">
              <div class="flex flex-1 items-center space-x-20">
                <div class="flex items-center space-x-2">
                  <div class="h-3 bg-text-link-hover w-1"></div>
                  <span class="text-text-title font-medium text-3 leading-3" @click.stop="openExecLog">{{ t('execution.infoLog.executionLog') }}</span>
                  <Arrow :open="showExecLog === '1'" @click.stop="openExecLog" />
                </div>
                <template v-if="!execLogErr && execLogPath">
                  <div>
                    <span class="mr-2">{{ t('execution.infoLog.file') }}<Colon /></span>
                    <span>{{ execLogPath }}</span>
                  </div>
                </template>
                <template v-else-if="execLogErr">
                  <span v-if="!loading" class="text-rule">{{ errorText || t('execution.infoLog.noAccessProxyInfo') + `http://${nodeIp}:6807/actuator/runner/log/${props.execId}` }}</span>
                </template>
              </div>
              <div>
                <Tooltip :title="t('execution.infoLog.export')" placement="top">
                  <Icon
                    icon="icon-daochu1"
                    class="text-4 cursor-pointer hover:text-text-link-hover mr-2"
                    @click="downloadLog('exec')" />
                </Tooltip>
                <Tooltip :title="t('execution.infoLog.refresh')" placement="top">
                  <Icon
                    icon="icon-shuaxin"
                    class="text-4 cursor-pointer hover:text-text-link-hover mr-2"
                    @click.stop="refreshExecLog" />
                </Tooltip>
              </div>
            </div>
          </template>
          <div
            class="whitespace-pre-wrap p-2 leading-4 text-text-content text-3 border border-border-divider rounded"
            :class="showSchedulingLog === '1'?'open-info':'open-bg-info'"
            style="scrollbar-gutter: stable;font-family: Monaco, Consolas, monospace !important;">
            {{ execLogContent }}
          </div>
        </CollapsePanel>
      </Collapse>
    </div>
  </div>
</template>
<style scoped>
.open-info {
  min-height: calc(100vh - 358px);
}

.open-bg-info {
 min-height: calc(100vh - 216px);
}

:deep(.ant-collapse-header){
  padding: 0 !important;
}

:deep(.ant-collapse-content-box){
  padding: 0;
}

:deep(.ant-collapse-borderless > .ant-collapse-item){
  border-bottom: 0;
}

</style>
