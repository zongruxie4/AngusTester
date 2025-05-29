<script setup lang="ts">
import { ref, watch } from 'vue';
import { Colon, NoData, IconDownload, Spin } from '@xcan-angus/vue-ui';
import { getDataByProxy } from '@/api/proxyRequest/index';

interface Props {
  execId:string;
  execNode:{id:string, name:string, ip:string, agentPort:string, publicIp:string};
  schedulingResult:{
    success:boolean;
    exitCode:string;
    console:string[]
  };
}

const props = withDefaults(defineProps<Props>(), {
  execId: '',
  execNode: undefined,
  schedulingResult: undefined
});

const nodeId = ref<string>();
const nodeIp = ref<string>();
const nodePort = ref<string>('6807');

const execLogContent = ref();
const execLogPath = ref('');
const execLogErr = ref(false);
const errorText = ref();

const loading = ref(!!props.execNode?.id);
const loadExecLog = async () => {
  loading.value = true;
  const [error, res] = await getDataByProxy(`http://${nodeIp.value}:${nodePort.value}/actuator/runner/log/${props.execId}`, {}, { timeout: 0 });
  loading.value = false;
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

watch(() => props.execNode, (newValue) => {
  if (newValue && newValue?.id) {
    nodeId.value = newValue.id;
    nodeIp.value = newValue?.publicIp || newValue.ip;
    nodePort.value = newValue.agentPort || '6807';
    loadExecLog();
  }
}, {
  deep: true,
  immediate: true
});

const downloadLog = () => {
  const content = execLogContent.value;
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
  a.download = 'runner.log';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  window.URL.revokeObjectURL(url);
};

</script>

<template>
  <Spin
    class="flex-1 px-5 py-3 h-full overflow-y-auto"
    style="scrollbar-gutter: stable;"
    :spinning="loading"
    :delay="0">
    <div v-if="!!props.execNode?.id" class="h-full text-3">
      <div class="flex items-center leading-5 mb-2.5">
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">节点</span>
          <Colon class="mr-2" />
          <span>{{ props.execNode.name }}({{ props.execNode.publicIp || props.execNode.ip }})</span>
        </div>
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">调度结果</span>
          <Colon class="mr-2" />
          <template v-if="props.schedulingResult?.success">
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-success"></span>
            <span>成功</span>
          </template>
          <template v-else>
            <span class="inline-block w-1.5 h-1.5 mr-1 rounded bg-status-error"></span>
            <span>失败</span>
          </template>
        </div>
        <div class="flex items-center mr-15">
          <span class="text-theme-sub-content">进程退出码</span>
          <Colon class="mr-2" />
          <span>{{ props.schedulingResult?.exitCode }}</span>
        </div>
        <IconDownload class="text-4" @click="downloadLog" />
      </div>
      <div style="height:calc(100% - 30px);" class="px-3.5 py-2.5 space-y-1 rounded border border-solid border-theme-text-box overflow-auto whitespace-pre-wrap">
        {{ execLogContent }}
      </div>
    </div>
    <NoData v-if="!loading && !props.execNode?.id" size="small" />
  </Spin>
</template>
