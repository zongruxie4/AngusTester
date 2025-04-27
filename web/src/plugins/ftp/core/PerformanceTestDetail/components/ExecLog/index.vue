<script lang="ts" setup>
import { ref, watch, computed } from 'vue';
import { http } from '@xcan-angus/tools';
import { Select, Icon, Arrow, Colon, Tooltip } from '@xcan-angus/vue-ui';

interface Props {
  execId: string;
  execNodes:{id:string, name:string, ip:string}[];
  lastSchedulingDate: string;
  lastSchedulingResult:{console:string[], deviceId:string, success:boolean, exitCode:string}[];
}

const props = withDefaults(defineProps<Props>(), {
  execId: '',
  execNodes: () => [],
  lastSchedulingDate: '',
  lastSchedulingResult: () => []
});

const emit = defineEmits<{(e: 'update:loading', value: boolean): void}>();

const nodeId = ref<string>();
const nodeIp = ref<string>();

const execLogContent = ref();
const execLogPath = ref('');
const execLogErr = ref(false);
const loadExecLog = async () => {
  emit('update:loading', true);
  const [error, res] = await http.get(`http://${nodeIp.value}:6807/actuator/runner/log/${props.execId}`);
  emit('update:loading', false);
  if (error) {
    execLogErr.value = true;
    return;
  }

  execLogPath.value = res.headers?.['xc-agent-log-path'];
  execLogContent.value = res?.data;
};

const schedulingLogItem = computed(() => {
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

const nodeSelectChange = (_nodeId) => {
  nodeId.value = _nodeId;
  loadExecLog();
};

watch(() => props.execId, (newValue) => {
  if (newValue) {
    nodeId.value = props.execNodes[0].id;
    nodeIp.value = props.execNodes[0].ip;
    loadExecLog();
  }
}, {

  immediate: true
});

const showSchedulingLog = ref(true);
const showExecLog = ref(false);

const handleDoubleClick = (type:'scheduling' | 'exec') => {
  if (type === 'exec') {
    showExecLog.value = !showExecLog.value;
    return;
  }
  showSchedulingLog.value = !showSchedulingLog.value;
};

const downloadLog = (type:'scheduling' | 'exec') => {
  const content = type === 'exec' ? execLogContent.value : schedulingLogItem.value?.console.join('\n');
  if (!content) {
    return;
  }

  const blob = new Blob([content], {
    type: 'application/text'
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
  <div class="h-full space-y-2">
    <Select
      :value="nodeId"
      :options="props.execNodes"
      :fieldNames="{label:'name',value:'id'}"
      class="w-80"
      placeholder="选择节点"
      size="small"
      @change="nodeSelectChange">
      <template #option="item">
        {{ `${item.name} ( ${item.ip} )` }}
      </template>
    </Select>
    <div class="flex flex-col border border-divider rounded  transition-height duration-500 overflow-hidden" :class="showSchedulingLog ? 'open-info' : 'stop-info'">
      <div
        class="h-8.5 flex flex-none justify-between items-center text-3 px-3.5 text-text-content border-b border-theme-divider cursor-pointer"
        style="background-color:rgb(252, 253, 255);"
        @click="handleDoubleClick('scheduling')">
        <div class="flex flex-1 items-center space-x-20">
          <span class="text-text-title font-medium">调度日志</span>
          <div>
            <span class="mr-2">最后调度时间<Colon /></span>
            <span>{{ props.lastSchedulingDate }}</span>
          </div>
          <div class="flex items-center">
            <span class="mr-2">结果<Colon /></span>
            <div class="flex items-center whitespace-nowrap">
              <template v-if="schedulingLogItem ">
                <div class="w-1.5 h-1.5 mr-1 rounded -mt-0.5" :class="schedulingLogItem?.success?'bg-status-success':'bg-status-error'"></div>
                {{ schedulingLogItem?.success?'成功':'失败' }}
              </template>
              <template v-else>
                --
              </template>
            </div>
          </div>
          <div>
            <span class="mr-2">退出码<Colon /></span>
            <span>{{ schedulingLogItem?.exitCode }}</span>
          </div>
        </div>
        <div>
          <Tooltip title="导出" placement="top">
            <Icon
              icon="icon-daochu1"
              class="text-4 cursor-pointer hover:text-text-link-hover mr-2"
              @click="downloadLog('scheduling')" />
          </Tooltip>
          <Arrow v-model:open="showSchedulingLog" />
        </div>
      </div>
      <div
        class="bg-log-bg whitespace-pre-wrap p-3.5 leading-4 flex-1 overflow-y-auto text-log-text text-3"
        style="scrollbar-gutter: stable;font-family: Monaco, Consolas, monospace !important;">
        {{ schedulingLogItem ? schedulingLogItem.console?.join('\n') : '' }}
      </div>
    </div>
    <div
      class="flex flex-col border border-divider rounded  transition-height duration-500 overflow-hidden"
      :class="showExecLog ? 'open-info' : 'stop-info'">
      <div
        class="h-8.5 flex flex-none justify-between items-center text-3 px-3.5 text-text-content cursor-pointer"
        style="background-color:rgb(252, 253, 255);"
        @click="handleDoubleClick('exec')">
        <div class="flex flex-1 items-center space-x-20">
          <span class="text-text-title font-medium">执行日志</span>
          <template v-if="!execLogErr && execLogPath">
            <div>
              <span class="mr-2">文件<Colon /></span>
              <span>{{ execLogPath }}</span>
            </div>
          </template>
          <template v-else>
            <span class="text-rule">无访问代理信息，连接失败地址：{{ `http://${nodeIp}:6807/actuator/runner/log/${props.execId}` }}</span>
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
          <Arrow v-model:open="showExecLog" />
        </div>
      </div>
      <div
        class="bg-log-bg whitespace-pre-wrap p-3.5 leading-4 flex-1 overflow-y-auto text-log-text text-3"
        style="scrollbar-gutter: stable;font-family: Monaco, Consolas, monospace !important;">
        {{ execLogContent }}
      </div>
    </div>
  </div>
</template>
<style scoped>

.open-info {
  height:calc(100% - 80px);
}

.stop-info {
 height: 34px;
}
</style>
