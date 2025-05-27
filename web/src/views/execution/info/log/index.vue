<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { Arrow, Colon, Icon, Select, Tooltip } from '@xcan-angus/vue-ui';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { http, PUB_TESTER } from '@xcan-angus/tools';

interface Props {
  execId: string;
  loading: boolean;
  execNodes:{id:string, name:string, ip:string, agentPort: string; publicIp?:string}[];
  lastSchedulingDate: string;
  schedulingNum: string;
  lastSchedulingResult:{console:string[], deviceId:string, success:boolean, exitCode:string}[];
}

const props = withDefaults(defineProps<Props>(), {
  execId: '',
  loading: false,
  execNodes: () => [],
  lastSchedulingDate: '',
  schedulingNum: '',
  lastSchedulingResult: () => []
});

const emit = defineEmits<{(e: 'update:loading', value: boolean): void}>();

const nodeId = ref<string>();
const nodeIp = ref<string>();
const nodePort = ref<string>('6807');

const execLogContent = ref();
const execLogPath = ref('');
const execLogErr = ref(false);
const errorText = ref();
const loadExecLog = async () => {
  emit('update:loading', true);
  const url = `${PUB_TESTER}/proxy/actuator/runner/log/${props.execId}?targetAddr=http://${nodeIp.value}:${nodePort.value}`;
  const [error, res] = await http.get(url, {}, { timeout: 0 });
  emit('update:loading', false);
  if (error) {
    execLogErr.value = true;
    if (error.response?.data) {
      errorText.value = error.response.data;
    } else {
      errorText.value = undefined;
    }
    return;
  }

  execLogErr.value = false;
  execLogPath.value = res.headers?.['xc-agent-log-path'];
  execLogContent.value = res?.data || '';
};

const schedulingLogItem = computed(() => {
  debugger;
  if (props.lastSchedulingResult.length) {
    const foundItem = props.lastSchedulingResult.find(item => item.deviceId === nodeId.value);
    if (foundItem) {
      return foundItem;
    } else {
      const emptyItem = props.lastSchedulingResult.find(item => !item?.deviceId);
      if (emptyItem) {
        return emptyItem;
      }

      return undefined;
    }
  }

  return undefined;
});

const nodeSelectChange = (_nodeId, options) => {
  nodeId.value = _nodeId;
  nodeIp.value = options?.publicIp || options.ip;
  loadExecLog();
};

watch(() => props.execId, (newValue) => {
  if (newValue && props.execNodes?.length) {
    nodeId.value = props.execNodes[0]?.id;
    nodeIp.value = props.execNodes[0]?.publicIp || props.execNodes[0]?.ip;
    nodePort.value = props.execNodes[0]?.agentPort || '6807';
    loadExecLog();
  }
}, {
  immediate: true
});

const showSchedulingLog = ref<string>('1');
const showExecLog = ref<string>('1');

const handleDoubleClick = (type:'scheduling' | 'exec') => {
  if (type === 'exec') {
    showExecLog.value = showExecLog.value === '1' ? '2' : '1';
    return;
  }
  showSchedulingLog.value = showSchedulingLog.value === '1' ? '2' : '1';
};

const openSchedulingLog = () => {
  showSchedulingLog.value = showSchedulingLog.value === '1' ? '2' : '1';
};

const openExecLog = () => {
  showExecLog.value = showExecLog.value === '1' ? '2' : '1';
};

const downloadLog = (type:'scheduling' | 'exec') => {
  const content = type === 'exec' ? execLogContent.value : schedulingLogItem.value?.console.join('\n');
  if (!content) {
    return;
  }

  const blob = new Blob([content], {
    type: 'text/plain'
  });

  const url = URL.createObjectURL(blob);

  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = type === 'exec' ? 'runner.log' : 'scheduling.log';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  window.URL.revokeObjectURL(url);
};

const refreshExecLog = (event) => {
  event.preventDefault();
  loadExecLog();
};

</script>
<template>
  <div class="h-full flex text-3 py-2 pl-2">
    <div class="w-60 flex-none pr-2">
      <Select
        :value="nodeId"
        :options="props.execNodes"
        :fieldNames="{label:'name',value:'id'}"
        class="w-full"
        placeholder="选择节点"
        size="small"
        @change="nodeSelectChange">
        <template #option="item">
          {{ `${item.name} ( ${item?.publicIp || item.ip} )` }}
        </template>
      </Select>
      <div class="flex ml-1.5 mt-5">
        <div class="mr-2 space-y-5">
          <div class="h-3">总调度次数<Colon /></div>
          <div class="h-3">最后调度时间<Colon /></div>
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
                <span class="text-text-title font-medium text-3 leading-3" @click.stop="openSchedulingLog">调度日志</span>
                <Arrow :open="showSchedulingLog === '1'" @click.stop="openSchedulingLog" />
                <div class="!ml-20">调度结果<Colon /></div>
                <template v-if="props.execNodes?.length">
                  <div class="flex items-center">
                    <div class="w-1.5 h-1.5 mr-1 rounded" :class="schedulingLogItem?.success?'bg-status-success':'bg-status-error'"></div>
                    {{ schedulingLogItem?.success?'成功':'失败' }}
                  </div>
                </template>
                <template v-else>
                  --
                </template>
                <div class="!ml-20">进程退出码<Colon /></div>
                <template v-if="props.execNodes?.length">
                  <div>{{ schedulingLogItem?.exitCode || '--' }}</div>
                </template>
                <template v-else>
                  --
                </template>
              </div>
              <div>
                <Tooltip title="导出" placement="top">
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
                  <span class="text-text-title font-medium text-3 leading-3" @click.stop="openExecLog">执行日志</span>
                  <Arrow :open="showExecLog === '1'" @click.stop="openExecLog" />
                </div>
                <template v-if="!execLogErr && execLogPath">
                  <div>
                    <span class="mr-2">文件<Colon /></span>
                    <span>{{ execLogPath }}</span>
                  </div>
                </template>
                <template v-else>
                  <span v-if="!loading" class="text-rule">{{ errorText || `无访问代理信息，连接失败地址：http://${nodeIp}:6807/actuator/runner/log/${props.execId}` }}</span>
                </template>
              </div>
              <div>
                <Tooltip title="导出" placement="top">
                  <Icon
                    icon="icon-daochu1"
                    class="text-4 cursor-pointer hover:text-text-link-hover mr-2"
                    @click="downloadLog('exec')" />
                </Tooltip>
                <Tooltip title="刷新" placement="top">
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
