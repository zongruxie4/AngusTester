<script lang="ts" setup>
import { inject, onMounted, ref, watch } from 'vue';
import { setting } from '@/api/gm';
import { Icon, Select } from '@xcan-angus/vue-ui';

const readyState = inject('readyState', ref(-1));
const currentProxyUrl = inject('currentProxyUrl', ref(''));
const currentProxy = inject('currentProxy', ref(''));
const proxyOptObj = inject('proxyOptObj', ref());

const status = ref('fail');
const agentOptions = ref([{
  label: '无代理',
  value: 'NO_PROXY',
  enabled: false,
  key: 'noProxy',
  url: ''
}, {
  label: '客户端代理',
  value: 'CLIENT_PROXY',
  enabled: false,
  key: 'clientProxy',
  url: ''
}, {
  label: '服务端代理',
  value: 'SERVER_PROXY',
  enabled: false,
  key: 'serverProxy',
  url: ''
}]);

const tipsMap = {
  NO_PROXY: '通过浏览器直接请求接口，对非同源访问接口服务器端需要取消跨域限制。',
  CLIENT_PROXY: '需要在访问浏览器所在电脑中安装”<a href=\'https://www.xcan.cloud/help\' target="_blank"  class="text-text-link">代理程序<a>“，配置后将使用客户端代理发送请求。',
  SERVER_PROXY: '需要将”代理程序“单独安装在共享网络的服务器主机，使用服务端代理无需用户在各自电脑中安装代理程序，配置后将使用服务端代理发送请求。推荐方式',
  CLOUD_PROXY: '通过AngusTester云服务器节点请求接口，注意：不能访问到客户内网服务地址。'
};
const agent = ref('NO_PROXY');

const loadProxy = async () => {
  if (proxyOptObj.value) {
    agentOptions.value = Object.keys(proxyOptObj.value).map(i => {
      return {
        label: proxyOptObj.value[i].name?.message,
        value: proxyOptObj.value[i].name?.value,
        enabled: proxyOptObj.value[i]?.enabled,
        key: i,
        url: proxyOptObj.value[i].url
      };
    });
  } else {
    const [error, res = { data: {} }] = await setting.getUserApiProxy();
    if (error) {
      return;
    }
    agentOptions.value = Object.keys(res.data).map(i => {
      return {
        label: res.data[i].name?.message,
        value: res.data[i].name?.value,
        enabled: res.data[i]?.enabled,
        key: i,
        url: res.data[i].url
      };
    });
  }
  agent.value = agentOptions.value.find(item => item.enabled)?.value as string || 'NO_PROXY';
};

const changeProxy = async (value) => {
  const [error] = await setting.enabledUserApiProxy({ name: value });
  if (error) {
    return;
  }
  const targetProxy = agentOptions.value.find(item => item.value === value);

  currentProxyUrl.value = targetProxy?.url || '';
  currentProxy.value = targetProxy?.value || '';

  loadProxy();
};

watch(() => readyState, (newValue) => {
  if (newValue.value === 1) {
    status.value = 'success';
  } else {
    status.value = 'fail';
  }
}, {
  deep: true,
  immediate: true
});

onMounted(() => {
  loadProxy();
});

</script>
<template>
  <div class="text-3">
    <div class="flex items-center space-x-2">
      <div class="font-semibold">代理配置</div>
      <div v-show="currentProxy !== 'NO_PROXY'" class="flex items-center">
        <template v-if="status === 'fail'">
          <Icon icon="icon-zhongzhi1" class="text-execute-yellow" />
          未连接
        </template>
        <template v-if="status === 'success'">
          <Icon icon="icon-duihao" class="text-execute-res4" />
          已连接
        </template>
      </div>
    </div>
    <div class="flex mt-2 items-start space-x-2">
      <Select
        :value="currentProxy"
        class="w-25"
        size="small"
        :options="agentOptions"
        @change="changeProxy" />
      <div class="whitespace-normal text-gray-text" v-html="tipsMap[currentProxy]"></div>
    </div>
    <template v-if="agent !== 'NO_PROXY'">
      <div v-show="!currentProxyUrl && status === 'fail'" class="mt-3 text-status-orange">未配置或启用服务端代理，请检查“应用管理->代理配置”</div>
      <div v-show="currentProxyUrl && status === 'fail'" class="mt-3 text-status-orange">代理连接失败，请检查代理服务是否已运行。</div>
    </template>
  </div>
</template>
<style scoped>
:deep(.ant-radio-wrapper) {
  @apply mr-0 text-3;
}

:deep(.ant-radio-wrapper > span) {
  @apply font-medium;
}

:deep(.ant-radio-wrapper:not(.ant-radio-wrapper-checked) > span) {
  @apply text-black-active;
}
</style>
