<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { Icon, notification, PureCard, Select } from '@xcan-angus/vue-ui';
import { Button, RadioGroup } from 'ant-design-vue';
import { PUB_TESTER, site } from '@xcan-angus/tools';
import axios from 'axios';

import { mock } from 'src/api/tester';

export interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
});

const ip = ref();
const port = ref('6807');
const showErr = ref(false);
const errorInfo = ref('');

const linesOpt = [
  {
    value: '500',
    label: '末尾500行'
  },
  {
    value: '1000',
    label: '末尾1000行'
  },
  {
    value: '10000',
    label: '末尾10000行'
  }
];

const content = ref('');
const refresh = ref(false);

const isFullScreen = ref(false);

const fullScreenIcon = computed(() => {
  return isFullScreen.value ? 'icon-tuichuzuida' : 'icon-zuidahua';
});

const handleZoom = () => {
  isFullScreen.value = !isFullScreen.value;
};

const logTextParam = reactive({
  logName: '',
  tail: true,
  linesNum: '500'
});
const fileList = ref<{label: string; value: string}[]>([]);

const loadIp = async () => {
  const [error, { data }] = await mock.loadServiceInfo(props.id);
  if (error) {
    return;
  }
  ip.value = data.nodePublicIp || data.nodeIp;
  port.value = data.agentPort || '6807';
};

const loadFileList = async () => {
  const host = await site.getUrl('apis');
  const privHost = await site.getUrl('at');
  const isPrivate = await site.isPrivate();
  axios.get(`${isPrivate ? privHost : host}${isPrivate ? '/pubapi/v1' : PUB_TESTER}/proxy/actuator/log/names?filePrefix=mockservice&targetAddr=http://${ip.value}:${port.value}`, {
  })
    .then(resp => {
      const { data } = resp;
      fileList.value = (data || []).map(i => ({ label: i, value: i }));
      if (data.length) {
        logTextParam.logName = data[0];
      }
      showErr.value = false;
    })
    .catch(err => {
      showErr.value = true;
      if (err.response?.data) {
        errorInfo.value = err.response?.data?.msg;
      } else if (err.message) {
        errorInfo.value = err.message;
      } else {
        errorInfo.value = '';
      }
    });
};

let timer;
const loadingLog = ref(false);
const loadLogContent = async () => {
  const { logName, tail, linesNum } = logTextParam;
  if (!logName) {
    return;
  }
  const host = await site.getUrl('apis');
  const privHost = await site.getUrl('at');
  const isPrivate = await site.isPrivate();
  axios.get(`${isPrivate ? privHost : host}${isPrivate ? '/pubapi/v1' : PUB_TESTER}/proxy/actuator/log/${logName}?tail=${tail}&linesNum=${linesNum}&targetAddr=http://${ip.value}:${port.value}`, {})
    .then(resp => {
      const { data } = resp;
      content.value = data;
      showErr.value = false;
      if (refresh.value) {
        timer = setTimeout(() => {
          loadLogContent();
          timer = null;
        }, 3000);
      }
    }).catch(err => {
      showErr.value = true;
      if (err.response?.data) {
        errorInfo.value = err?.response?.data?.msg;
      } else if (err.message) {
        errorInfo.value = err.message;
      } else {
        errorInfo.value = '';
      }
    });
};

const closeErrInfo = () => {
  showErr.value = false;
};

const downloadLog = () => {
  if (!content.value) {
    notification.warning('无可下载内容');
    return;
  }
  const blob = new Blob([content.value], {
    type: 'text/plain'
  });
  const url = URL.createObjectURL(blob);

  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = 'log.text';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  window.URL.revokeObjectURL(url);
};

watch(() => logTextParam, () => {
  if (logTextParam.logName) {
    if (timer) {
      clearTimeout(timer);
      timer = null;
    }
    loadLogContent();
  }
}, {
  immediate: true,
  deep: true
});

onMounted(async () => {
  await loadIp();
});

watch(() => ip.value, async newValue => {
  if (newValue) {
    loadFileList();
  }
}, {
  immediate: true
});

onBeforeUnmount(() => {
  if (timer) {
    clearTimeout(timer);
    timer = null;
  }
});

</script>
<template>
  <PureCard class="text-3 px-5 h-full flex flex-col pb-5 pt-3">
    <div v-if="showErr" class="border border-border-error rounded-xl px-2 py-1 flex items-center text-3 space-x-1 my-4 bg-bg-red">
      <Icon icon="icon-tishi1" class="text-blue-icon text-3.5" />
      <span class="text-rule flex-1">{{ errorInfo || `无访问代理信息，连接失败地址：http://${ip}:${port}` }}</span>
      <Icon
        icon="icon-cuowu"
        class="text-3.5 cursor-pointer"
        @click="closeErrInfo" />
    </div>
    <div>
      <span>日志文件:</span>
      <Select
        v-model:value="logTextParam.logName"
        class="w-70 ml-2"
        :options="fileList">
      </Select>
    </div>
    <div class="flex items-center space-x-2 my-2">
      <span>浏览日志:</span>
      <RadioGroup
        v-model:value="logTextParam.linesNum"
        :options="linesOpt"
        size="small" />
      <div class="inline-flex items-center flex-1 justify-end">
        <Button
          class="py-0 h-5"
          size="small"
          @click="downloadLog">
          下载
        </Button>
        <Button
          class="py-0 h-5 ml-2"
          size="small"
          :disabled="!logTextParam.logName || loadingLog"
          @click="loadLogContent">
          刷新
        </Button>
        <!-- <span class="ml-2">
          自动刷新：
        </span>
        <Switch v-model:checked="refresh" size="small" /> -->
        <Icon
          class="text-3.5 ml-2 cursor-pointer"
          :icon="fullScreenIcon"
          @click.stop="handleZoom" />
      </div>
    </div>
    <div class="border  bg-white log-text min-h-50 px-3 py-4 whitespace-pre-wrap break-words overflow-y-auto" :class="{'fixed left-0 top-0 right-0 bottom-0 h-full z-99': isFullScreen, 'flex-1': !isFullScreen}">
      <Icon
        v-if="isFullScreen"
        class="text-3.5 fixed right-7 top-7  cursor-pointer"
        :icon="fullScreenIcon"
        @click.stop="handleZoom" />
      {{ content }}
    </div>
  </PureCard>
</template>
<style scoped>
.content-fit-height {
  height: calc(100vh - 450px);
}

.log-text{
  /* color: #f2f2f2; */
  font-family: Monaco, Consolas, monospace !important;
}
</style>
