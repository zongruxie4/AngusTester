<script lang="ts" setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue';
import { Icon, notification, Select } from '@xcan-angus/vue-ui';
import { Button, RadioGroup } from 'ant-design-vue';
import axios from 'axios';
import { routerUtils, ApiType, ApiUrlBuilder } from '@xcan-angus/infra';

export interface Props {
  ip: string;
  port?: string
}

const props = withDefaults(defineProps<Props>(), {
  port: '6807'
});

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

const showErr = ref(false);
const errorText = ref();

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

const loadFileList = async () => {
  // const host = await site.getUrl('apis');
  // const privHost = await site.getUrl('at');
  // const isPrivate = await site.isPrivate();
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.PUB_API);
  const host = ApiUrlBuilder.buildApiUrl(routeConfig, '');
  // const url = `${isPrivate ? privHost : host}${isPrivate ? '/pubapi/v1' : PUB_TESTER}/proxy/actuator/log/names?filePrefix=agent&targetAddr=http://${props.ip}:${props.port}`
  const url = `${host}/proxy/actuator/log/names?filePrefix=agent&targetAddr=http://${props.ip}:${props.port}`;
  axios.get(url, {})
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
        errorText.value = err.response?.data?.msg;
      } else if (err.message) {
        errorText.value = err.message;
      } else {
        errorText.value = undefined;
      }
    });
};

let timer;
const loadLogContent = async () => {
  const { logName, tail, linesNum } = logTextParam;
  if (!logName) {
    return;
  }
  // example

  //   http://192.168.0.102:1830/swagger-ui/?urls.primaryName=Public%20Api

  // /altester/pubapi/v1/proxy + 元原接口地址 + 代理地址查询参数（ targetAddr）

  // http://192.168.0.101:6807/actuator/runner/log/204764938806239665

  // Dev 环境 >>>   http://dev-apis.xcan.cloud/altester/pubapi/v1/proxy/actuator/runner/log/204764938806239665?targetAddr=http://192.168.0.101:6807

  // Prod 环境 >>>   https://bj-c1-prod-apis.xcan.cloud/altester/pubapi/v1/proxy/actuator/runner/log/204764938806239665?targetAddr=http://192.168.0.101:6807
  // const host = await site.getUrl('apis');
  // const privHost = await site.getUrl('at');
  // const isPrivate = await site.isPrivate();

  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.PUB_API);
  const host = ApiUrlBuilder.buildApiUrl(routeConfig, '');
  // // const url = `${isPrivate ? privHost : host}${isPrivate ? '/pubapi/v1' : PUB_TESTER}/proxy/actuator/log/${logName}?tail=${tail}&linesNum=${linesNum}&targetAddr=http://${props.ip}:${props.port}`
  const url = `${host}/proxy/actuator/log/${logName}?tail=${tail}&linesNum=${linesNum}&targetAddr=http://${props.ip}:${props.port}`;
  axios.get(url, {})
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
        errorText.value = err.response?.data?.msg;
      } else if (err.message) {
        errorText.value = err.message;
      } else {
        errorText.value = undefined;
      }
    });
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

watch(() => props.ip, newValue => {
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
  <div class="text-3 pb-5">
    <div class="flex items-center space-x-2 my-2">
      <span>浏览日志:</span>
      <Select
        v-model:value="logTextParam.logName"
        class="w-70"
        :options="fileList">
      </Select>
      <RadioGroup
        v-model:value="logTextParam.linesNum"
        :options="linesOpt"
        size="small" />
      <div v-show="showErr">{{ errorText || `无访问代理信息，连接失败地址：http://${props.ip}:${props.port}` }}</div>
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
          :disabled="!logTextParam.logName"
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
    <div class=" bg-white log-text border min-h-50 px-3 py-4 whitespace-pre-wrap break-words overflow-y-auto" :class="{'fixed left-0 top-0 right-0 bottom-0 h-full z-99': isFullScreen, 'content-fit-height': !isFullScreen}">
      <Icon
        v-if="isFullScreen"
        class="text-3.5 fixed right-7 top-7 cursor-pointer"
        :icon="fullScreenIcon"
        @click.stop="handleZoom" />
      {{ content }}
    </div>
  </div>
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
