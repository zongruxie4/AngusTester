<script lang="ts" setup>
import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { setting } from '@/api/gm';
import { Icon, Select } from '@xcan-angus/vue-ui';

const { t } = useI18n();

//  Props & Injections
const readyState = inject('readyState', ref(-1));
const currentProxyUrl = inject('currentProxyUrl', ref(''));
const currentProxy = inject('currentProxy', ref(''));
const proxyOptObj = inject('proxyOptObj', ref());

const status = ref('fail');
const agent = ref('NO_PROXY');

/**
 * Proxy agent options configuration
 */
const agentOptions = ref([{
  label: t('proxy.types.noProxy'),
  value: 'NO_PROXY',
  enabled: false,
  key: 'noProxy',
  url: ''
}, {
  label: t('proxy.types.clientProxy'),
  value: 'CLIENT_PROXY',
  enabled: false,
  key: 'clientProxy',
  url: ''
}, {
  label: t('proxy.types.serverProxy'),
  value: 'SERVER_PROXY',
  enabled: false,
  key: 'serverProxy',
  url: ''
}]);

/**
 * Tips mapping for different proxy types
 */
const tipsMap = {
  NO_PROXY: t('proxy.types.noProxyDescription'),
  CLIENT_PROXY: t('proxy.types.clientProxyDescription'),
  SERVER_PROXY: t('proxy.types.serverProxyDescription'),
  CLOUD_PROXY: t('proxy.types.cloudProxyDescription')
};

/**
 * Load proxy configuration from API or injected data
 */
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

/**
 * Handle proxy change event
 * @param value - Selected proxy value
 */
const handleProxyChange = async (value) => {
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
      <div class="font-semibold">{{ t('proxy.proxyConfig') }}</div>
      <div v-show="currentProxy !== 'NO_PROXY'" class="flex items-center">
        <template v-if="status === 'fail'">
          <Icon icon="icon-zhongzhi1" class="text-execute-yellow" />
          {{ t('status.notConnected') }}
        </template>
        <template v-if="status === 'success'">
          <Icon icon="icon-duihao" class="text-execute-res4" />
          {{ t('status.connected') }}
        </template>
      </div>
    </div>
    <div class="flex mt-2 items-start space-x-2">
      <Select
        :value="currentProxy"
        class="w-25"
        size="small"
        :options="agentOptions"
        @change="handleProxyChange" />
      <div class="whitespace-normal text-gray-text" v-html="tipsMap[currentProxy]"></div>
    </div>
    <template v-if="agent !== 'NO_PROXY'">
      <div v-show="!currentProxyUrl && status === 'fail'" class="mt-3 text-status-orange">{{ t('proxy.messages.serverProxyNotConfigured') }}</div>
      <div v-show="currentProxyUrl && status === 'fail'" class="mt-3 text-status-orange">{{ t('proxy.messages.proxyConnectionFailed') }}</div>
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
