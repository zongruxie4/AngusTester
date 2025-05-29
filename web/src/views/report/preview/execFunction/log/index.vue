<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { getDataByProxy } from "@/api/proxyRequest/index";

import { ExecContent, ExecInfo, ExecResult, ReportInfo } from '../../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  reportInfo: ReportInfo;
  execInfo: ExecInfo;
  execContent: ExecContent[];
  execResult: ExecResult;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  reportInfo: undefined,
  execInfo: undefined,
  execContent: undefined,
  execResult: undefined
});

const nodeId = ref<string>();
const nodeIp = ref<string>();
const nodePort = ref<string>('6807');

const execLogContent = ref([]);
const execLogPath = ref('');
const execLogErr = ref(false);
const errorText = ref();
const loadExecLog = async () => {
  const [error, res] = await getDataByProxy(`http://${nodeIp.value}:${nodePort.value}/actuator/runner/log/${props.execInfo?.id}`, null, { timeout: 0 });
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
  execLogContent.value = (res?.data || '').split('\n');
};

onMounted(() => {
  watch(() => props.execInfo, (newValue) => {
    if (!newValue) {
      return;
    }

    const execNodes = newValue.execNodes || [];
    if (!execNodes.length) {
      return;
    }

    const { id, publicIp, ip, agentPort } = execNodes[0];
    nodeId.value = id;
    nodeIp.value = publicIp || ip;
    nodePort.value = agentPort || '6807';
    loadExecLog();
  }, { immediate: true });
});

const schedulingLog = computed(() => {
  const lastSchedulingResult = props.execInfo?.lastSchedulingResult;
  if (lastSchedulingResult?.length) {
    const checkedItem = lastSchedulingResult.find(item => item.deviceId === nodeId.value);
    if (checkedItem) {
      return checkedItem;
    }

    const emptyItem = lastSchedulingResult.find(item => !item.deviceId);
    if (emptyItem) {
      return emptyItem;
    }
  }

  return undefined;
});
</script>
<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a5" class="text-4 text-theme-title font-medium">三、<em class="inline-block w-0.25"></em>日志信息</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a6">3.1<em class="inline-block w-3.5"></em>执行调度信息</span>
      </h2>
      <div class="flex items-center mb-2">
        <div class="flex items-center mr-20">
          <div class="mr-0.5">调度结果：</div>
          <div v-if="nodeIp" class="flex items-center">
            <div
              class="w-1.5 h-1.5 mr-1 rounded"
              :class="schedulingLog?.success ? 'bg-status-success' : 'bg-status-error'">
            </div>
            {{ schedulingLog?.success ? '成功' : '失败' }}
          </div>
          <div v-else>--</div>
        </div>
        <div class="flex items-center">
          <div class="mr-0.5">进程退出码：</div>
          <div v-if="nodeIp">{{ schedulingLog?.exitCode }}</div>
          <div v-else>--</div>
        </div>
      </div>
      <div
        class="text-0 text-white bg-slate-600 whitespace-pre-wrap break-all px-2.5 py-2 rounded border border-solid border-theme-text-box">
        <div
          v-for="(item, index) in schedulingLog?.console"
          :key="`${index}s`"
          class="text-3 whitespace-pre-wrap break-all">
          {{ item }}
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a7">3.2<em class="inline-block w-3.5"></em>执行采样信息</span>
      </h2>
      <div class="flex items-center mb-2">
        <div class="flex items-start">
          <div class="mr-0.5 flex-shrink-0">文件：</div>
          <div v-if="!execLogErr && execLogPath" class="whitespace-pre-wrap break-all">
            {{ execLogPath }}
          </div>
          <div v-else class="text-rule whitespace-pre-wrap break-all">
            {{ errorText ||
              `无访问代理信息，连接失败地址：http://${nodeIp}:6807/actuator/runner/log/${props.execInfo?.id}` }}
          </div>
        </div>
      </div>
      <div
        class="text-0 text-white bg-slate-600 whitespace-pre-wrap break-all px-2.5 py-2 rounded border border-solid border-theme-text-box">
        <div
          v-for="(item, index) in execLogContent"
          :key="`${index}e`"
          class="text-3 whitespace-pre-wrap break-all">
          {{ item }}
        </div>
      </div>
    </div>
  </div>
</template>
